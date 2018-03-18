package it.manzolo.gestionespesefamiliari.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import it.manzolo.gestionespesefamiliari.R;
import it.manzolo.gestionespesefamiliari.parameters.GestionespesefammiliariUrls;
import it.manzolo.gestionespesefamiliari.service.DownloadService;

public class UpdateAppActivity extends AppCompatActivity {

    // declare the dialog as a member field of your activity
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);
        startUpdateApp();

    }

    public void startUpdateApp() {
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(UpdateAppActivity.this);
        mProgressDialog.setMessage("Download aggiornamento...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        Intent intent = new Intent(this, DownloadService.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String hosturl = prefs.getString("host_url_text", "");
        intent.putExtra("url", hosturl + GestionespesefammiliariUrls.APP_DOWNLOAD_PAGE);
        intent.putExtra("targetfile", Environment.getExternalStorageDirectory().getPath() + "/gestionespesefamiliari.apk");
        intent.putExtra("receiver", new DownloadUpdateReleaseAppReceiver(new Handler()));
        startService(intent);
    }

    private class DownloadUpdateReleaseAppReceiver extends ResultReceiver {
        public DownloadUpdateReleaseAppReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
                case DownloadService.UPDATE_PROGRESS:
                    int progress = resultData.getInt("progress");
                    mProgressDialog.setProgress(progress);
                    mProgressDialog.setIndeterminate(false);

                    if (progress == 100) {
                        mProgressDialog.dismiss();
                    }
                    break;
                case DownloadService.END_TASK:
                    String updatefile = resultData.getString("targetfile");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.fromFile(new File(updatefile));
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    startActivity(intent);
                    finish();
                    break;
            }

        }
    }
}
