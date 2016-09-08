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
public class ListaTipimovimento implements Serializable {
    static private String FILENAME = ".gestionespesefamiliaritipimovimento";
    private ArrayList<Tipomovimento> tipimovimento = new ArrayList<Tipomovimento>();


    public ListaTipimovimento() {
    }

    public ArrayList<Tipomovimento> getTipimovimento() {
        return tipimovimento;
    }

    public void setTipimovimento(ArrayList<Tipomovimento> tipimovimento) {
        this.tipimovimento = tipimovimento;
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

    public ListaTipimovimento load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ListaTipimovimento listaTipimovimento = null;
            listaTipimovimento = (ListaTipimovimento) is.readObject();
            is.close();
            fis.close();
            return listaTipimovimento;
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




