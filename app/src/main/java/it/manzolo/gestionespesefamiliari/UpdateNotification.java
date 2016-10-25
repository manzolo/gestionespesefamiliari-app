package it.manzolo.gestionespesefamiliari;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.gestionespesefamiliari.ui.UpdateAppActivity;
import it.manzolo.utils.Internet;

public class UpdateNotification {
    protected static final int NOTIFICATION_NEWVERSION_ID = 880;
    private Context context;

    public UpdateNotification(Context ctx) {
        try {
            this.context = ctx;
            String versionCode;
            versionCode = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            // then you use
            String hosturl = prefs.getString("host_url_text", "");

            String webVersion = new Internet(hosturl + GestionespesefammiliariUrls.APP_VERSION_PAGE).getResponse();
            //Log.i("cur", versionCode);Log.i("web",webVersion);
            if (versionCompare(webVersion, versionCode) > 0) {
                NotifyNewRelease(versionCode, webVersion);
            }
        } catch (Exception e) {
            // Se non si raggiunge pace, si controllera' la prossima volta
            Log.w("Controllo aggiornamenti", e.getMessage());
        }

    }

    protected void NotifyNewRelease(String versionCode, String webVersion) {

        NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context);

        // Titolo e testo della notifica
        notificationBuilder.setContentTitle("Gestione spese familiari");
        notificationBuilder.setContentText("E' disponibile la nuova versione!!");

        // Testo che compare nella barra di stato non appena compare la notifica
        notificationBuilder.setTicker("Gestione Spese Familiari nuova versione " + versionCode + " -> " + webVersion);

        // Data e ora della notifica
        notificationBuilder.setWhen(System.currentTimeMillis());

        // Icona della notifica
        notificationBuilder.setSmallIcon(R.drawable.appicon);

        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        // Creiamo il pending intent che verra' lanciato quando la notifica viene premuta
        Intent notificationIntent = new Intent(context, UpdateAppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);


        // Impostiamo il suono, le luci e la vibrazione di default
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE);
        //notificationBuilder.setLights(Color.GRAY, 100, 100);
        notificationBuilder.setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_NEWVERSION_ID, notificationBuilder.build());

    }

    private Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }
}
