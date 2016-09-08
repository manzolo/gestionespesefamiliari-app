package it.manzolo.gestionespesefamiliari.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.manzolo.gestionespesefamiliari.R;
import it.manzolo.gestionespesefamiliari.gestione.Utente;
import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.utils.Internet;
import it.manzolo.utils.MessageBox;
import it.manzolo.utils.ToolTip;

public class UltimiMovimentiActivity extends ActionBarActivity {

    List<NameValuePair> movimenti = new ArrayList<NameValuePair>();
    private Utente utenteloggato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultimi_movimenti);
        utenteloggato = (Utente) getIntent().getSerializableExtra("Utente");
    }

    @Override
    public void onStart() {
        super.onStart();
        getUlimiMovimenti();
        Button btnElimina = (Button) findViewById(R.id.btncancella);
        btnElimina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elimina(v);
            }
        });
    }

    public void getUlimiMovimenti() {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(GestionespesefammiliariUrls.ULTIMI_MOVIMENTI_PAGE).append("?utenteid=").append(String.valueOf(utenteloggato.getId()));
        String url = sbUrl.toString();
        LinearLayout rl = (LinearLayout) findViewById(R.id.scrollLinearLayout);
        rl.removeAllViews();
        try {
            String movimentiResposeText = new Internet(url).getResponse();
            JSONArray jsonArray = new JSONArray(movimentiResposeText);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                View tv;
                tv = new CheckBox(this);
                ((CheckBox) tv).setTextSize(15);

                int movimentoID = Integer.parseInt(jsonObject.getString("id"));
                movimenti.add(new BasicNameValuePair("id", jsonObject.getString("id")));
                tv.setId(movimentoID);

                ((TextView) tv).setTextColor(Color.BLACK);
                ((TextView) tv).setGravity(Gravity.LEFT);
                ((TextView) tv).setText(jsonObject.getString("descrizione").concat("\n"));
                rl.addView(tv);
            }
        } catch (Exception e) {
            new ToolTip(this, e.getMessage());
            return;
        }
    }

    public void elimina(View view) {
        // SI chiede conferma se si vuole cancellare
        new AlertDialog.Builder(this)
                .setTitle("Gestione spese familiari")
                .setMessage("Sicuro di voler eliminare i movimenti selezionati?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Sul SI */
                        // Si controlla quanti movimenti sono stati selezionati
                        List<NameValuePair> selezionati = new ArrayList<NameValuePair>();
                        for (NameValuePair movimento : movimenti) {
                            CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(movimento.getValue()));
                            if (elemento.isChecked()) {
                                selezionati.add(new BasicNameValuePair("movimenti[]", movimento.getValue()));
                            }
                        }
                        if (selezionati.size() <= 0) {
                            new MessageBox(UltimiMovimentiActivity.this, "Attenzione", "Seleziona almeno un movimento!");
                            return;
                        }
                        Internet request = new Internet(GestionespesefammiliariUrls.ELIMINA_MOVIMENTO_PAGE, Internet.METHOD_POST);
                        try {
                            String responseText = request.getPostResponse(new UrlEncodedFormEntity(selezionati));
                            new ToolTip(UltimiMovimentiActivity.this, "Cancellazione effettuata", true);
                        } catch (Exception e) {
                            new MessageBox(UltimiMovimentiActivity.this, "Attenzione", "Non e' stato possibile cancellare i movimenti, errore nella comunicazione col server");
                            return;
                        }

                        LinearLayout rl = (LinearLayout) findViewById(R.id.scrollLinearLayout);
                        for (NameValuePair movimeno : selezionati) {
                            CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(movimeno.getValue()));
                            movimenti.remove(new BasicNameValuePair("id", movimeno.getValue()));
                            rl.removeView(elemento);
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new ToolTip(UltimiMovimentiActivity.this, "Operazione annullata");
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
