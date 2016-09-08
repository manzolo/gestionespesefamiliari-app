package it.manzolo.gestionespesefamiliari.gestione;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by MAC on 10/04/2015.
 */
public class ListaTipologie implements Serializable {
    private static final long serialVersionUID = 6128016096756071380L;
    static private String FILENAME = ".gestionespesefamiliaritipologie";
    private ArrayList<Tipologia> tipologie = new ArrayList<Tipologia>();


    public ListaTipologie() {
    }

    public ArrayList<Tipologia> getTipologie() {
        return tipologie;
    }

    public void setTipologie(ArrayList<Tipologia> tipologie) {
        this.tipologie = tipologie;
    }

    public void save(Context context) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            //Log.i("",context.getCacheDir().getAbsolutePath());
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ListaTipologie load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ListaTipologie listaTipologie = null;
            listaTipologie = (ListaTipologie) is.readObject();
            is.close();
            fis.close();
            return listaTipologie;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}




