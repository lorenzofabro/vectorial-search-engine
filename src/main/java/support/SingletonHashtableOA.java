package support;

import entities.Palabra;
import java.util.List;
import persistence.Util;

public class SingletonHashtableOA {

    private HashtableOA<String, Long> h;

    private SingletonHashtableOA() {
        this.h = new HashtableOA<>();
//        List<Palabra> palabras = p.findAll();
        Util util = new Util();
        List<Palabra> palabras = util.getPalabras();
        for (Palabra palabra : palabras) {
            h.put(palabra.getNombre(), palabra.getId());
        }
    }

    //************************* Atributo para manejo de la instancia
    private static SingletonHashtableOA instance = null;

    //************************* Metodo estatico para la obtencion de la instancia
    public static SingletonHashtableOA getInstance() {

        // Si todavia no se creo la tabla hash la creo
        if (instance == null) {
            instance = new SingletonHashtableOA();
        }

        // Devuelvo la tabla hash
        return instance;
    }

    public HashtableOA<String, Long> getHashtableOA() {
        return this.h;
    }

}
