package it.manzolo.gestionespesefamiliari.gestione;

import java.io.Serializable;

/**
 * Created by MAC on 05/04/2015.
 */
public class Tipomovimento implements Serializable {



    // Okay, full acknowledgment that public members are not a good idea, however
// this is a Spinner demo not an exercise in java best practices.
    private int id;
    private String tipo;
    private String segno;

    // A simple constructor for populating our member variables for this tutorial.
    public Tipomovimento(int _id, String _tipo, String _segno)
    {
        this.id = _id;
        this.tipo = _tipo;
        this.segno = _segno;
    }

    // The toString method is extremely important to making this class work with a Spinner
// (or ListView) object because this is the method called when it is trying to represent
// this object within the control.  If you do not have a toString() method, you WILL
// get an exception.
    public String toString()
    {
        return (this.tipo + " (" + this.segno + ")");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSegno() {
        return this.segno;
    }

    public void setSegno(String segno) {
        this.segno = segno;
    }
}