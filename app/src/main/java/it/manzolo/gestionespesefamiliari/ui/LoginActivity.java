package it.manzolo.gestionespesefamiliari.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import it.manzolo.gestionespesefamiliari.R;
import it.manzolo.gestionespesefamiliari.UpdateNotification;
import it.manzolo.gestionespesefamiliari.gestione.SerializzaElenchi;
import it.manzolo.gestionespesefamiliari.gestione.Utente;
import it.manzolo.gestionespesefamiliari.service.NetworkChangeReceiver;
import it.manzolo.utils.ToolTip;

public class LoginActivity extends AppCompatActivity {

    static final String NETWORK_CONNECTION_CHANGE = "it.manzolo.gestionespesefamiliari.NetworkConnectionChange";
    static boolean LAST_NETWORKSTATUS;
    private final BroadcastReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private final BroadcastReceiver updateConnectionStatusUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Si controlla se lo stato della rete internet e' cambiato
            if (NetworkChangeReceiver.ACTIVE) {
                tvConnectionStatus.setVisibility(View.GONE);
                // Se c'e' connessione dati
                bLogin.setEnabled(true);
            } else {
                tvConnectionStatus.setText("Nessuna connessione disponibile");
                tvConnectionStatus.setVisibility(View.VISIBLE);
                bLogin.setEnabled(false);
            }
        }
    };
    private Utente loginUser;
    private Button bLogin;
    private TextView tvConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Just for testing, allow network access in the main thread, NEVER use this is productive code
        // StrictMode.ThreadPolicy policy = new
        // StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);
        //Si prova a prendere le informazioni di login serializzate
        loginUser = (new Utente()).load(getApplicationContext());

        //Per nascondere la barra dei menu
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        // Si impostano gli attributi della text per lo stato della connessione
        tvConnectionStatus = (TextView) findViewById(R.id.connectionStatus);
        tvConnectionStatus.setTextColor(Color.WHITE);
        tvConnectionStatus.setBackgroundColor(Color.BLACK);
        tvConnectionStatus.setGravity(Gravity.CENTER_HORIZONTAL);

        bLogin = (Button) findViewById(R.id.accedi);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Viene chiamato appena si clicca su accedi
                 */
                loginButtonClick();
            }
        });


        Button esci = (Button) findViewById(R.id.esci);
        esci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginUser != null) {
                    loginUser.setAutenticato(false);
                    loginUser.save(getApplicationContext());
                }
                finish();
            }
        });

        String lastMatricola = "";
        String lastPassword = "";
        if ((loginUser != null)) {
            //Se si sta collegando lo stesso utente
            lastMatricola = loginUser.getUsername();
            lastPassword = loginUser.getPassword();
        }

        EditText txtMatricola = (EditText) findViewById(R.id.username);
        EditText txtPassword = (EditText) findViewById(R.id.password);

        txtMatricola.setText(lastMatricola);
        txtPassword.setText(lastPassword);
        // Receiver per ricevere l'evento che lo stato della connessione e' cambiato
        registerReceiver(updateConnectionStatusUiReceiver, new IntentFilter(NETWORK_CONNECTION_CHANGE));

        Timer timerConnectionState = new Timer();
        TimerTask timerTaskConnectionState = new TimerTask() {
            @Override
            public void run() {
                if (LAST_NETWORKSTATUS != NetworkChangeReceiver.ACTIVE) {
                    // boolean NetworkActive = NetworkState.ACTIVE;
                    // intentConnectionState.putExtra("ActiveConnection", NetworkActive);
                    sendBroadcast(intentConnectionState);
                    LAST_NETWORKSTATUS = NetworkChangeReceiver.ACTIVE;
                }
            }
            Intent intentConnectionState = new Intent(NETWORK_CONNECTION_CHANGE);
        };

        timerConnectionState.scheduleAtFixedRate(timerTaskConnectionState, 0, 1000);
        new Thread() {
            @Override
            public void run() {
                // Si scaricano gli elenchi delle tipologie e tipimovimento in un thread a parte
                new SerializzaElenchi(getApplicationContext());
            }
        }.start();

        if (NetworkChangeReceiver.ACTIVE) {
            //Si controlla la presenza di nuove versioni della app
            new UpdateNotification(LoginActivity.this);
        }
    }


    private boolean loginButtonClick() {
        try {

            EditText txtUsername = (EditText) findViewById(R.id.username);
            EditText txtPassword = (EditText) findViewById(R.id.password);
            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();
            if (username.length() == 0) {
                new ToolTip(getApplicationContext(), "Inserisci l'username");
                return false;
            }
            if (password.length() == 0) {
                new ToolTip(getApplicationContext(), "Inserisci la password");
                return false;
            }

            //Se si sta collegando lo stesso utente che era salvato si controlla se sia già autenticato
            if (loginUser != null && loginUser.getUsername().equals(username) && loginUser.getPassword().equals(password) && loginUser.isAutenticato()) {
                new ToolTip(getApplicationContext(), "Accesso consentito");
                logonSuccess();
            } else {
                loginUser = new Utente();
                loginUser.setUsername(username);
                loginUser.setPassword(password);
                loginUser.login(getApplicationContext());
                if (loginUser.isAutenticato()) {
                    new ToolTip(getApplicationContext(), "Accesso effettuato");
                    loginUser.save(getApplicationContext());
                    if (NetworkChangeReceiver.ACTIVE) {
                        logonSuccess();
                        return true;
                    } else {
                        new ToolTip(getApplicationContext(), "Nessuna connessione ad internet disponibile");
                        return false;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            new ToolTip(getApplicationContext(), "Impossibile stabilire una connessione col server", true);
            return false;
        }
        return false;
    }

    private void logonSuccess() {
        Intent intent = new Intent(LoginActivity.this, InserimentoSpesaActivity.class);
        intent.putExtra("Utente", loginUser);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
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
                bLogin.setEnabled(true);
                tvConnectionStatus.setVisibility(View.GONE);
            } else {
                bLogin.setEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (loginUser != null) {
            loginUser.setAutenticato(false);
            loginUser.save(getApplicationContext());
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(updateConnectionStatusUiReceiver);
        super.onDestroy();
    }
}
