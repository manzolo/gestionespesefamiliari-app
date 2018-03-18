package it.manzolo.gestionespesefamiliari.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.manzolo.gestionespesefamiliari.R;
import it.manzolo.gestionespesefamiliari.gestione.ListaTipimovimento;
import it.manzolo.gestionespesefamiliari.gestione.ListaTipologie;
import it.manzolo.gestionespesefamiliari.gestione.SerializzaElenchi;
import it.manzolo.gestionespesefamiliari.gestione.Tipologia;
import it.manzolo.gestionespesefamiliari.gestione.Tipomovimento;
import it.manzolo.gestionespesefamiliari.gestione.Utente;
import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.gestionespesefamiliari.service.NetworkChangeReceiver;
import it.manzolo.utils.Internet;
import it.manzolo.utils.MessageBox;
import it.manzolo.utils.ToolTip;

public class InserimentoSpesaActivity extends AppCompatActivity {

    static final String NETWORK_CONNECTION_CHANGE = "it.manzolo.gestionespesefamiliari.NetworkConnectionChange";
    static boolean LAST_NETWORKSTATUS;
    private final BroadcastReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private final BroadcastReceiver updateConnectionStatusUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Si controlla se lo stato della rete internet e' cambiato
            if (NetworkChangeReceiver.ACTIVE) {
                // Se c'e' linea si controlla lo stato del menu e vari messaggi
                inviaspesa.setEnabled(true);
            } else {
                inviaspesa.setEnabled(false);

            }

        }
    };
    ArrayList<Tipologia> tipologie = new ArrayList<Tipologia>();
    ArrayList<Tipomovimento> tipimovimento = new ArrayList<Tipomovimento>();
    private Button inviaspesa;
    private Utente utenteloggato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inserimentospesa);
        utenteloggato = (Utente) getIntent().getSerializableExtra("Utente");
        // Nome
        TextView welcome = (TextView) findViewById(R.id.welcome);
        welcome.setTextSize(20);
        welcome.setTextColor(Color.parseColor("#5500AA"));
        welcome.setText(utenteloggato.getNominativo());

        inviaspesa = (Button) findViewById(R.id.invia);
        inviaspesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inserisci(v);
            }
        });
        inviaspesa.setEnabled(false);
        reloadelenchi();
        // Receiver per ricevere l'evento che lo stato della connessione e' cambiato
        registerReceiver(updateConnectionStatusUiReceiver, new IntentFilter(NETWORK_CONNECTION_CHANGE));

        Timer timerConnectionState = new Timer();
        TimerTask timerTaskConnectionState = new TimerTask() {
            @Override
            public void run() {
                if (LAST_NETWORKSTATUS != NetworkChangeReceiver.ACTIVE) {
                    sendBroadcast(intentConnectionState);
                    LAST_NETWORKSTATUS = NetworkChangeReceiver.ACTIVE;
                }
            }

            Intent intentConnectionState = new Intent(NETWORK_CONNECTION_CHANGE);
        };
        timerConnectionState.scheduleAtFixedRate(timerTaskConnectionState, 0, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inserimentospesa_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ultimi_movimenti:
                Intent intent = new Intent(getApplicationContext(), UltimiMovimentiActivity.class);
                intent.putExtra("Utente", utenteloggato);
                startActivity(intent);
                return true;
            case R.id.aggiorna:
                refreshElenchi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean getTipologie() {
        Spinner dropdown = (Spinner) findViewById(R.id.tipologie);
        ListaTipologie elencotipologie = (new ListaTipologie()).load(this);
        if (elencotipologie != null) {
            tipologie = elencotipologie.getTipologie();
            // Step 2: Create and fill an ArrayAdapter with a bunch of "State" objects
            ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tipologie);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnerviewdropdown);

            // Step 3: Tell the spinner about our adapter
            dropdown.setAdapter(spinnerArrayAdapter);
            return (tipologie.size() > 0);
        } else {
            dropdown.setAdapter(null);
            return false;
        }

    }

    private boolean getTipimovimento() {
        Spinner dropdown = (Spinner) findViewById(R.id.tipomovimento);
        ListaTipimovimento elencotipimovimento = (new ListaTipimovimento()).load(this);
        if (elencotipimovimento != null) {
            tipimovimento = elencotipimovimento.getTipimovimento();
            // Step 2: Create and fill an ArrayAdapter with a bunch of "State" objects
            ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tipimovimento);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnerviewdropdown);

            // Step 3: Tell the spinner about our adapter
            dropdown.setAdapter(spinnerArrayAdapter);
            return (tipimovimento.size() > 0);
        } else {
            dropdown.setAdapter(null);
            return false;
        }
    }


    public void refreshElenchi() {
        new SerializzaElenchi(this);
        reloadelenchi();
    }

    public void reloadelenchi() {

        if (getTipologie() && getTipimovimento()) {
            inviaspesa.setEnabled(true);
        } else {
            inviaspesa.setEnabled(false);
            new ToolTip(getApplicationContext(), "Tipologie o Categorie non trovate, provare a riaggiornare gli elenchi", true);
            return;
        }
    }


    public void inserisci(View view) {
        // SI chiede conferma se si vuole inviare la spesa
        EditText txtImporto = (EditText) findViewById(R.id.importo);
        if (txtImporto.getText().toString().equals("")) {
            new MessageBox(this, "Inserimento spesa", "Inserire un importo valido");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Gestione Spese Familiari")
                .setMessage("Sicuro di voler inserire questa spesa?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Sul SI */
                        EditText txtImporto = (EditText) findViewById(R.id.importo);
                        EditText txtNota = (EditText) findViewById(R.id.nota);
                        DatePicker pkData = (DatePicker) findViewById(R.id.datamovimento);
                        Spinner spinnerTipomovimento = (Spinner) findViewById(R.id.tipomovimento);
                        Spinner spinnerTipologia = (Spinner) findViewById(R.id.tipologie);
                        float importo = Float.parseFloat(txtImporto.getText().toString());
                        String nota = txtNota.getText().toString();
                        String datamovimento = pkData.getYear() + "-" + String.format("%02d", pkData.getMonth() + 1) + "-" + String.format("%02d", pkData.getDayOfMonth());
                        Tipomovimento objTipomovimento = (Tipomovimento) spinnerTipomovimento.getSelectedItem();
                        Tipologia objTipologia = (Tipologia) spinnerTipologia.getSelectedItem();

                        int tipomovimento = objTipomovimento.getId();
                        int tipologia = objTipologia.getId();

                        List<NameValuePair> form = new ArrayList<NameValuePair>();

                        form.add(new BasicNameValuePair("utente", String.valueOf(utenteloggato.getId())));
                        Log.i("info", String.valueOf(utenteloggato.getId()));
                        form.add(new BasicNameValuePair("importo", String.valueOf(importo)));
                        Log.i("info", String.valueOf(importo));
                        form.add(new BasicNameValuePair("datamovimento", datamovimento));
                        Log.i("info", datamovimento);
                        form.add(new BasicNameValuePair("tipomovimento", String.valueOf(tipomovimento)));
                        Log.i("info", String.valueOf(tipomovimento));
                        form.add(new BasicNameValuePair("tipologia", String.valueOf(tipologia)));
                        Log.i("info", String.valueOf(tipologia));
                        form.add(new BasicNameValuePair("nota", nota));
                        Log.i("info", String.valueOf(nota));
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String hosturl = prefs.getString("host_url_text", "");
                        Internet request = new Internet(hosturl+GestionespesefammiliariUrls.REGISTRA_SPESA_PAGE, Internet.METHOD_POST);

                        try {
                            String responseText = request.getPostResponse(new UrlEncodedFormEntity(form));
                            //Log.i("Parse response", responseText);
                            try {
                                try {
                                    JSONObject jsonObj = new JSONObject(responseText);
                                    Integer retcode = Integer.parseInt(jsonObj.getString("retcode"));
                                    //Log.i("Parse json", String.valueOf(retcode));
                                    if (retcode.equals(0)) {
                                        //OK
                                        new ToolTip(getApplicationContext(), "Spesa inserita correttamente");
                                        finish();
                                    } else {
                                        new MessageBox(InserimentoSpesaActivity.this, "Inserimento spesa", "Risposta dal server non prevista");
                                        return;
                                    }
                                } catch (JSONException e) {
                                    new MessageBox(InserimentoSpesaActivity.this, "Inserimento spesa", "Risposta dal server non corretta " + e.getMessage());
                                    return;
                                }
                            } catch (Exception e) {
                                new MessageBox(InserimentoSpesaActivity.this, "Inserimento spesa", "Errore nell'elaborazione della richesta " + e.getMessage());
                                return;
                            }
                        } catch (UnsupportedEncodingException e) {
                            new MessageBox(InserimentoSpesaActivity.this, "Inserimento spesa", "Errore nella composizione della spesa da inviare al server " + e.getMessage());
                            return;
                        } catch (Exception e) {
                            new MessageBox(InserimentoSpesaActivity.this, "Inserimento spesa", "Errore nell'invio della spesa al server " + e.getMessage());
                            return;
                        }
                    }

                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Sul NO non si fa niente
                new ToolTip(getApplicationContext(), "Nessuna operazione effettuata");
            }
        }).show();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Se cambia il focus della finestra
        if (hasFocus) {
            // Se e' cambiato lo stato della connessione
            if (LAST_NETWORKSTATUS != NetworkChangeReceiver.ACTIVE) {
                //messagesRefresh();
                LAST_NETWORKSTATUS = NetworkChangeReceiver.ACTIVE;
            }
            if (NetworkChangeReceiver.ACTIVE) {
                inviaspesa.setEnabled(true);
            } else {
                inviaspesa.setEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        onActivityResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onResume();
        onActivityResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Si deregistra il receiver se cambia la connessione
        unregisterReceiver(networkChangeReceiver);
    }

    private void onActivityResume() {
        try {
            //Si registra il receiver se cambia la connessione
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkChangeReceiver, filter);

        } catch (Exception e) {
            new ToolTip(getApplicationContext(), "Impossibile stabilire una connessione con il server");
            return;
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(updateConnectionStatusUiReceiver);
        super.onDestroy();
    }
}
