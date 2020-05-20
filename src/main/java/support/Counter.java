package support;


public class Counter {

    //************************* Atributo para el manejo del contador
    private int counter;

    //************************* Constructores

    public Counter(int cantidad) {
        if(cantidad > 1) this.counter = cantidad;
        else this.counter = 1;
    }

    public Counter() {
        this.counter = 1;
    }

    //************************* Accesor

    public int getValue() {
        return this.counter;
    }

    //************************* Metodo para incrementar el contador

    public void increase() {
        this.counter++;
    }

    //************************* Redefinicion de metodos heredados desde Object

    @Override
    public String toString() {
        Integer c = this.counter;
        return c.toString();
    }

}
