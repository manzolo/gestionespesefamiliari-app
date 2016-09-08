package it.manzolo.gestionespesefamiliari.gestione;

import java.io.Serializable;

/**
 * Created by MAC on 10/04/2015.
 */
public class Categoria implements Serializable {
    private int id;
    private String descrizione;

    public Categoria(int _id, String _descrizione) {
        this.id = _id;
        this.descrizione = _descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return this.descrizione;
    }
}
