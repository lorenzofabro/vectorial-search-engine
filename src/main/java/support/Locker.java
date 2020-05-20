package support;

import java.io.Serializable;


public class Locker<E> implements Cloneable, Serializable {

    //************************* Enum para manejo de estados del casillero
    public enum State {
        OPEN,
        CLOSE,
        TOMB
    }

    //************************* Atributos para la gestion del casillero
    private State state;
    private E info;

    //************************* Constructores

    public Locker() {
        this.state = State.OPEN;
        this.info = null;
    }

    public Locker(E info) {
        this.state = State.CLOSE;
        this.info = info;
    }

    //************************* Accesores

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public E getInfo() {
        return this.info;
    }

    public void setInfo(E info) {
        this.info = info;
    }

    //************************* Redefinicion de metodos herados desde Object

    @Override
    public String toString() {

        StringBuilder cad = new StringBuilder("[State: ");
        if(this.state == State.CLOSE) cad.append("CLOSE");
        else if(this.state == State.OPEN) cad.append("OPEN");
        else cad.append("TOMB");

        cad.append(", Info: ");
        if(this.info != null) cad.append(this.info.toString());
        else cad.append("EMPTY");
        cad.append("]");

        return cad.toString();

    }

    @Override
    protected Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

}
