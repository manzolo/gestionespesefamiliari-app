package it.manzolo.gestionespesefamiliari.gestione;

import java.io.Serializable;

/**
 * Created by MAC on 10/04/2015.
 */
public class Tipologia implements Serializable {
    private int id;
    private String descrizione;
    private Categoria categoria;

    public Tipologia(int _id, String _descrizione, Categoria _categoria) {
        this.id = _id;
        this.descrizione = _descrizione;
        this.categoria = _categoria;

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String toString() {
        return (descrizione + " (" + categoria + ")");
    }
}
