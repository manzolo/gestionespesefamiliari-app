package it.manzolo.gestionespesefamiliari.gestione;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.utils.Internet;

/**
 * Created by MAC on 10/04/2015.
 */
public class SerializzaElenchi {
    private Context ctx;

    public SerializzaElenchi(Context _ctx) {
        this.ctx = _ctx;
        JSONObject retval;
        ArrayList<Tipologia> tipologie = new ArrayList<Tipologia>();
        ArrayList<Tipomovimento> tipimovimento = new ArrayList<Tipomovimento>();
        try {
            retval = new Internet(GestionespesefammiliariUrls.GET_TIPOLOGIE_PAGE).getJSONObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            if (Integer.parseInt(retval.getString("retcode")) == 0) {
                JSONArray jTipologie = retval.getJSONArray("tipologie");
                for (int i = 0; i < jTipologie.length(); i++) {
                    JSONObject json_data = jTipologie.getJSONObject(i);
                    tipologie.add(new Tipologia(json_data.getInt("id"), json_data.getString("descrizione"), new Categoria(json_data.getInt("categoria_id"), json_data.getString("categoria"))));
                }
                ListaTipologie salvalistatipologie = new ListaTipologie();
                salvalistatipologie.setTipologie(tipologie);
                //Log.i("",String.valueOf(salvalistatipologie.getTipologie().size()));
                salvalistatipologie.save(this.ctx);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        try {
            retval = new Internet(GestionespesefammiliariUrls.GET_TIPIMOVIMENTO_PAGE).getJSONObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            if (Integer.parseInt(retval.getString("retcode")) == 0) {
                JSONArray jTipimovimento = retval.getJSONArray("tipimovimento");
                for (int i = 0; i < jTipimovimento.length(); i++) {
                    JSONObject json_data = jTipimovimento.getJSONObject(i);
                    tipimovimento.add(new Tipomovimento(json_data.getInt("id"), json_data.getString("tipo"), json_data.getString("segno")));
                }
                ListaTipimovimento salvatipimovimento = new ListaTipimovimento();
                salvatipimovimento.setTipimovimento(tipimovimento);
                salvatipimovimento.save(this.ctx);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
