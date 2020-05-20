package support;

import java.io.Serializable;
import java.util.*;


public class HashtableOA<K, V> implements Map<K, V>, Cloneable, Serializable {

    //************************* Maximo tamaño de la lista
    private final static int MAX_SIZE = Integer.MAX_VALUE;

    //************************* Atributos para la gestion de la tabla hash
    private Locker<Map.Entry<K, V>> table[];
    private int initial_capacity;
    private int count;
    private float load_factor;

    //************************* Atributos para el manejo de las vistas
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;

    //************************* Atributo para el control fast-fail
    protected transient int modCount;

    //************************* Constructores

    public HashtableOA() {
        this(11, 0.5f);
    }

    public HashtableOA(int initial_capacity) {
        this(initial_capacity, 0.5f);
    }

    public HashtableOA(int initial_capacity, float load_factor) {

        if(load_factor <= 0 || load_factor > 0.5f) load_factor = 0.5f;
        if(initial_capacity <= 0) initial_capacity = 11;
        if(initial_capacity > MAX_SIZE) initial_capacity = MAX_SIZE;
        if(!isPrime(initial_capacity)) initial_capacity = nextPrime(initial_capacity);

        this.table = new Locker[initial_capacity];
        for(int i=0; i<table.length; i++) table[i] = new Locker<>();
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;

    }

    public HashtableOA(Map<? extends K,? extends V> t) {
        this(11, 0.5f);
        this.putAll(t);
    }

    //************************* Metodos privados para la verificacion y generacion de numeros primos

    // Verifica si el numero es primo
    private boolean isPrime(int n) {

        if(n == 1) return false;
        if(n == 2) return true;
        if(n % 2 == 0) return false;

        int i;
        int raiz = (int) Math.sqrt(n);
        for(i=3; i<=raiz; i+=2) if(n % i == 0) return false;
        return true;
    }

    // Devuelve el siguiente numero primo a partir de n
    private int nextPrime(int n) {

        if(n % 2 == 0) n++;
        for(;!isPrime(n); n+=2 );
        return n;

    }

    //************************* Implementacion de los metodos especificados por Map

    // Retorna la cantidad de elementos de la tabla
    @Override
    public int size() {
        return this.count;
    }

    // Retorna valor booleano dependiendo de si la lista esta vacia o no
    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    // Determina si la clave esta en la tabla
    @Override
    public boolean containsKey(Object key) {
        return (this.get((K)key) != null);
    }

    // Determina si alguna clave esta asociado al valor que entra como parametro
    @Override
    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    // Retorna el objeto al cual esta asociado la clave que se pasa como parametro, null si no existe
    @Override
    public V get(Object key) {

        if(key == null) throw new NullPointerException("get(): Parámetro null");

        Map.Entry<K, V> x = this.search_for_entry((K)key);

        if(x != null)  return x.getValue();
        return null;

    }

    // Asocia el valor/clave en la tabla, si la clave ya existe, reemplaza el valor por el nuevo, retorna el viejo
    @Override
    public V put(K key, V value) {

        if(key == null || value == null) throw new NullPointerException("put(): Parámetro null");
        if (this.averageLength() >= this.load_factor * 10) this.rehash();

        int j = 0;
        int initialIndex = h(key);
        int addIndex;
        int handlerIndex = initialIndex;
        int firstTombIndex = -1;

        Locker<Map.Entry<K, V>> l = this.table[handlerIndex];

        while(l.getState() != Locker.State.OPEN) {
            if(l.getState() == Locker.State.CLOSE && Objects.equals(l.getInfo().getKey(), key)) {
                V old = l.getInfo().getValue();
                l.getInfo().setValue(value);
                return old;
            }

            if(l.getState() == Locker.State.TOMB && firstTombIndex == -1) {
                firstTombIndex = handlerIndex;
            }

            j++;
            addIndex = (int) Math.pow(j, 2);
            handlerIndex = initialIndex + addIndex;
            if(handlerIndex >= table.length) handlerIndex %= table.length;
            l = this.table[handlerIndex];
        }

        Map.Entry<K, V> entry = new Entry<>(key, value);
        if(firstTombIndex != -1) {
            l = this.table[firstTombIndex];
            l.setInfo(entry);
            l.setState(Locker.State.CLOSE);
            count++;
            modCount++;
        }
        else {
            l.setInfo(entry);
            l.setState(Locker.State.CLOSE);
            count++;
            modCount++;
        }

        return null;

    }

    // Elimina de la tabla la clave y su correspondiente valor asociado, de vuelve el valor
    @Override
    public V remove(Object key) {

        if(key == null) throw new NullPointerException("remove(): Parámetro null");

        K k = (K) key;
        int i = this.search_for_index(k);
        V old = null;

        if(i != -1) {
            old = table[i].getInfo().getValue();
            table[i].setState(Locker.State.TOMB);
            table[i].setInfo(null);
            this.count--;
            this.modCount++;
        }
        return old;

    }

    // Copia en nuestra tabla, todos los objetos del map pasado por parametro
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

        for(Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }

    }

    // Deja la tabla vacia, retorna el vector a su tamaño inicial
    @Override
    public void clear() {

        this.table = new Locker[this.initial_capacity];
        for(int i=0; i<this.table.length; i++) this.table[i] = new Locker<>();
        this.count = 0;
        this.modCount++;

    }

    //************************* Metodos para el manejo de las vistas

    // Devuelvo el objeto manejador de la vista de todas las claves
    @Override
    public Set<K> keySet() {

        if(keySet == null) keySet = new KeySet();
        return keySet;

    }

    // Devuelvo el objeto manejador de la vista de todos los valores
    @Override
    public Collection<V> values() {

        if(values==null) values = new ValueCollection();
        return values;

    }

    // Devuelvo el objeto manejador de la vista de todos los pares clave/valor
    @Override
    public Set<Map.Entry<K, V>> entrySet() {

        if(entrySet == null) entrySet = new EntrySet();
        return entrySet;

    }

    //************************* Redefinicion de los metodos heredados desde Object

    @Override
    protected Object clone() throws CloneNotSupportedException {

        HashtableOA<K, V> t = (HashtableOA<K, V>)super.clone();
        t.table = new Locker[table.length];
        for(int i=0; i<table.length; i++) {
            t.table[i] = (Locker<Map.Entry<K,V>>) this.table[i].clone();
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;

    }

    // Determina si nuestra tabla es igual a otro objeto pasado como parametro
    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof Map)) return false;

        Map<K, V> t = (Map<K, V>) obj;

        if(t.size() != this.size()) return false;

        try {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) return false;
                else if(!value.equals(t.get(key))) return false;
            }
        }
        catch (ClassCastException | NullPointerException e) {
            return false;
        }

        return true;

    }

    // Retorna un hashCode para la tabla completa
    @Override
    public int hashCode() {

        if(this.isEmpty()) return 0;
        int hc = 0;
        for(Map.Entry<K, V> entry : this.entrySet()) hc += entry.hashCode();
        return hc;

    }

    // Devuelve el contenido de la tabla en forma de String
    @Override
    public String toString()  {

        StringBuilder cad = new StringBuilder("\n* OAHashtable:");

        for(int i=0; i<this.table.length; i++) cad.append("\n- Locker "+ i + ": " + table[i].toString());
        return cad.toString();

    }

    //************************* Metodos especificos de la clase

    // Determina si alguna clave esta asociada al objeto value que ingresa como parametro
    public boolean contains(Object value) {

        if(value == null) return false;
        for(int i=0; i<table.length; i++) {
            Locker<Map.Entry<K, V>> l = this.table[i];
            if(l.getState() == Locker.State.CLOSE && l.getInfo().getValue() == value) return true;
        }
        return false;

    }

    // Incrementa el tamaño de la tabla y reorganiza su contenido
    protected void rehash() {

        int new_length = nextPrime(this.table.length * 2);
        if(new_length > HashtableOA.MAX_SIZE) new_length = HashtableOA.MAX_SIZE;

        Locker<Map.Entry<K, V>> temp[] = new Locker[new_length];
        for(int j=0; j<temp.length; j++) temp[j] = new Locker<>();
        this.modCount++;

        Set<Map.Entry<K,V>> es = this.entrySet();
        Iterator<Map.Entry<K,V>> it = es.iterator();
        int j;
        int initialIndex;
        int addIndex;
        int handlerIndex;

        while(it.hasNext()) {

            Map.Entry<K, V> entry = it.next();
            j = 0;
            initialIndex = h(entry.getKey(), temp.length);
            handlerIndex = initialIndex;
            Locker<Map.Entry<K, V>> l = temp[handlerIndex];

            while(l.getState() != Locker.State.OPEN) {
                j++;
                addIndex = (int) Math.pow(j, 2);
                handlerIndex = initialIndex + addIndex;
                if(handlerIndex >= temp.length) handlerIndex %= temp.length;
                l = temp[handlerIndex];
            }

            l.setInfo(entry);
            l.setState(Locker.State.CLOSE);

        }

        this.table = temp;

    }

    //************************ Métodos privados.

    // Toma una clave entera k y calcula y retorna un índice  válido para esa clave para entrar en la tabla
    private int h(int k) {
        return h(k, this.table.length);
    }

    // Toma un objeto key que representa una clave y calcula y retorna un índice válido para esa
    // clave para entrar en la tabla
    private int h(K key) {
        return h(key.hashCode(), this.table.length);
    }

    // Toma un objeto key que representa una clave y un tamaño de tabla t, y calcula y retorna un
    // índice válido para esa clave dado ese tamaño
    private int h(K key, int t) {
        return h(key.hashCode(), t);
    }

    // Toma una clave entera k y un tamaño de tabla t, y calcula y retorna un índice válido para
    // esa clave dado ese tamaño.
    private int h(int k, int t) {
        if(k < 0) k *= -1;
        return k % t;
    }

    // Calcula el factor de ocupamiento de la tabla
    private int averageLength() {
        return 10 * this.count / this.table.length;
    }

    // Busca en la tabla un objeto Entry que coincida con key, devuelve el objeto Entry
    private Map.Entry<K, V> search_for_entry(K key) {

        int j = 0;
        int initialIndex = h(key);
        int addIndex;
        int handlerIndex = initialIndex;

        Locker<Map.Entry<K, V>> l = this.table[handlerIndex];

        while(l.getState() != Locker.State.OPEN) {
            if(l.getState() == Locker.State.CLOSE && Objects.equals(l.getInfo().getKey(), key)) return l.getInfo();
            j++;
            addIndex = (int) Math.pow(j, 2);
            handlerIndex = initialIndex + addIndex;
            if(handlerIndex >= table.length) handlerIndex %= table.length;
            l = this.table[handlerIndex];
        }
        return null;

    }

    // Busca en la tabla un objeto Entry que coincida con key, devuelve el indice del objeto Entry
    private int search_for_index(K key) {

        int j = 0;
        int initialIndex = h(key);
        int addIndex;
        int handlerIndex = initialIndex;

        Locker<Map.Entry<K, V>> l = this.table[handlerIndex];

        while(l.getState() != Locker.State.OPEN) {
            if(l.getState() == Locker.State.CLOSE && Objects.equals(l.getInfo().getKey(), key)) return handlerIndex;
            j++;
            addIndex = (int) Math.pow(j, 2);
            handlerIndex = initialIndex + addIndex;
            if(handlerIndex >= table.length) handlerIndex %= table.length;
            l = this.table[handlerIndex];
        }
        return -1;

    }

    //************************* Clases internas

    // Clase interna para la gestion de las entradas de datos
    private class Entry<K, V> implements Map.Entry<K, V>, Serializable {

        private K key;
        private V value;

        public Entry(K key, V value) {

            if(key == null || value == null) throw new IllegalArgumentException("Entry(): Parámetro null");
            this.key = key;
            this.value = value;

        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {

            if(value == null) throw new IllegalArgumentException("setValue(): Parámetro null");
            V old = this.value;
            this.value = value;
            return old;

        }

        @Override
        public int hashCode() {

            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;

        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) return true;
            if (obj == null) return false;
            if (this.getClass() != obj.getClass()) return false;

            final Entry other = (Entry) obj;

            if (!Objects.equals(this.key, other.key)) return false;
            if (!Objects.equals(this.value, other.value)) return false;
            return true;

        }

        @Override
        public String toString() {
            return "(" + this.key.toString() + ", " + this.value.toString() + ")";
        }


    }

    // Clase interna que representa una vista de todas las claves mapeadas en la tabla
    private class KeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public int size() {
            return HashtableOA.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return HashtableOA.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return (HashtableOA.this.remove(o) != null);
        }

        @Override
        public void clear() {
            HashtableOA.this.clear();
        }

        // Clase iteradora de KeySet
        private class KeySetIterator implements Iterator<K> {

            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public KeySetIterator() {

                current_entry = -1;
                next_ok = false;
                expected_modCount = HashtableOA.this.modCount;

            }

            @Override
            public boolean hasNext() {

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                if (HashtableOA.this.isEmpty()) return false;
                if (current_entry >= t.length - 1) return false;

                int next_entry = current_entry + 1;

                while(next_entry <= t.length - 1){
                    if(t[next_entry].getState() == Locker.State.CLOSE) return true;
                    next_entry++;
                }
                return false;

            }

            @Override
            public K next() {

                if(HashtableOA.this.modCount != expected_modCount)
                    throw new ConcurrentModificationException("next(): Modificación inesperada de tabla");
                if(!hasNext()) throw new NoSuchElementException("next(): No existe el elemento pedido");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                current_entry++;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry++;
                next_ok = true;
                K key = t[current_entry].getInfo().getKey();
                return key;

            }

            @Override
            public void remove() {

                if(!next_ok) throw new IllegalStateException("remove(): Debe invocar a next() antes de remove()");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                t[current_entry].setState(Locker.State.TOMB);
                current_entry --;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry--;
                next_ok = false;
                HashtableOA.this.count--;
                HashtableOA.this.modCount++;
                expected_modCount++;

            }

        }

    }

    // Clase interna que representa una vista de todos los valores mapeados en la tabla
    private class ValueCollection extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new ValueSetIterator();
        }

        @Override
        public int size() {
            return HashtableOA.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return HashtableOA.this.containsValue(o);
        }

        @Override
        public void clear() {
            HashtableOA.this.clear();
        }

        // Clase iteradora de ValueSet
        private class ValueSetIterator implements Iterator<V> {

            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public ValueSetIterator() {

                current_entry = -1;
                next_ok = false;
                expected_modCount = HashtableOA.this.modCount;

            }

            @Override
            public boolean hasNext() {

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                if (HashtableOA.this.isEmpty()) return false;
                if (current_entry >= t.length - 1) return false;

                int next_entry = current_entry + 1;

                while(next_entry <= t.length - 1){
                    if(t[next_entry].getState() == Locker.State.CLOSE) return true;
                    next_entry++;
                }
                return false;

            }

            @Override
            public V next() {

                if(HashtableOA.this.modCount != expected_modCount)
                    throw new ConcurrentModificationException("next(): Modificación inesperada de tabla");
                if(!hasNext()) throw new NoSuchElementException("next(): No existe el elemento pedido");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                current_entry++;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry++;
                next_ok = true;
                V value = t[current_entry].getInfo().getValue();
                return value;

            }

            @Override
            public void remove() {

                if(!next_ok) throw new IllegalStateException("remove(): Debe invocar a next() antes de remove()");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                t[current_entry].setState(Locker.State.TOMB);
                current_entry --;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry--;
                next_ok = false;
                HashtableOA.this.count--;
                HashtableOA.this.modCount++;
                expected_modCount++;

            }

        }

    }

    // Clase interna que representa una vista de todos los pares clave/valor mapeados en la tabla
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public int size() {
            return HashtableOA.this.count;
        }

        @Override
        public boolean contains(Object o) {

            if(o == null) return false;
            if(!(o instanceof Entry)) return false;

            Map.Entry<K, V> entry = (Map.Entry<K,V>) o;
            K key = entry.getKey();

            if(HashtableOA.this.containsKey(key)) return true;
            return false;

        }

        @Override
        public boolean remove(Object o) {

            if(o == null) throw new NullPointerException("remove(): Parámetro null");
            if(!(o instanceof Entry)) return false;

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();

            if(HashtableOA.this.containsKey(key)) {
                HashtableOA.this.count--;
                HashtableOA.this.modCount++;
                HashtableOA.this.remove(key);
                return true;
            }
            return false;

        }

        @Override
        public void clear() {
            HashtableOA.this.clear();
        }

        // Clase iteradora de EntrySet
        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {

            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public EntrySetIterator() {

                current_entry = -1;
                next_ok = false;
                expected_modCount = HashtableOA.this.modCount;

            }

            @Override
            public boolean hasNext() {

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                if (HashtableOA.this.isEmpty()) return false;
                if (current_entry >= t.length - 1) return false;

                int next_entry = current_entry + 1;

                while(next_entry <= t.length - 1){
                    if(t[next_entry].getState() == Locker.State.CLOSE) return true;
                    next_entry++;
                }
                return false;

            }

            @Override
            public Map.Entry<K, V> next() {

                if(HashtableOA.this.modCount != expected_modCount)
                    throw new ConcurrentModificationException("next(): Modificación inesperada de tabla");
                if(!hasNext()) throw new NoSuchElementException("next(): No existe el elemento pedido");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                current_entry++;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry++;
                next_ok = true;
                Map.Entry<K, V> entry = t[current_entry].getInfo();
                return entry;

            }

            @Override
            public void remove() {

                if(!next_ok) throw new IllegalStateException("remove(): Debe invocar a next() antes de remove()");

                Locker<Map.Entry<K, V>> t[] = HashtableOA.this.table;

                t[current_entry].setState(Locker.State.TOMB);
                current_entry --;
                while(t[current_entry].getState() != Locker.State.CLOSE) current_entry--;
                next_ok = false;
                HashtableOA.this.count--;
                HashtableOA.this.modCount++;
                expected_modCount++;

            }

        }

    }

}
