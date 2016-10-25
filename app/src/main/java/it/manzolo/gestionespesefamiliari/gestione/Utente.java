package it.manzolo.gestionespesefamiliari.gestione;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.utils.Internet;
import it.manzolo.utils.ToolTip;

public class Utente implements Serializable {
    static private String FILENAME = ".gestionespesefamiliarisession";
    private String username;
    private String password;
    private int id;
    private String nominativo;
    private String famiglia_id;
    private boolean autenticato;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNominativo() {
        return nominativo;
    }

    public void setNominativo(String nominativo) {
        this.nominativo = nominativo;
    }

    public String getFamigliaId() {
        return famiglia_id;
    }

    public void setFamigliaId(String famiglia_id) {
        this.famiglia_id = famiglia_id;
    }

    public boolean isAutenticato() {
        return autenticato;
    }

    public void setAutenticato(boolean autenticato) {
        this.autenticato = autenticato;
    }

    public boolean login(Context ctx) {
        boolean state;
        JSONObject retval;

        try {
            List<NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("username", getUsername()));
            form.add(new BasicNameValuePair("password", getPassword()));
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String hosturl = prefs.getString("host_url_text", "");
            Internet request = new Internet(hosturl + GestionespesefammiliariUrls.LOGIN_PAGE, Internet.METHOD_POST);
            String responseText = request.getPostResponse(new UrlEncodedFormEntity(form));

            //Log.i("Parse response", responseText);

            try {
                retval = new JSONObject(responseText);
                Integer retcode = Integer.parseInt(retval.getString("retcode"));
                //Log.i("Parse json", String.valueOf(retcode));
                if (retcode.equals(0)) {
                    //OK
                    setId(retval.getInt("utente_id"));
                    setFamigliaId(retval.getString("famiglia_id"));
                    setNominativo(retval.getString("nominativo"));
                    setAutenticato(true);
                    state = true;

                } else {
                    setAutenticato(false);
                    new ToolTip(ctx, retval.getString("message"));
                    state = false;
                }
            } catch (JSONException e) {
                setAutenticato(false);
                new ToolTip(ctx, "Risposta dal server non corretta " + e.getMessage());
                state = false;

            }

        } catch (Exception e) {
            setAutenticato(false);
            new ToolTip(ctx, "Impossibile effettuare il login " + e.getMessage(), true);
            state = false;
        }

        return state;
    }

    public boolean save(Context context) {
        FileOutputStream fos;
        ObjectOutputStream os;
        try {
            //Log.i("",context.getCacheDir().getAbsolutePath());
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Utente load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            Utente listaTipologie;
            listaTipologie = (Utente) is.readObject();
            is.close();
            fis.close();
            return listaTipologie;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
