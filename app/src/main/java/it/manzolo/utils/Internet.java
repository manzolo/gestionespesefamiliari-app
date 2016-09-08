package it.manzolo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Internet {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    private static final int DEFAULT_TIMEOUT_CONNECTION = 5000;
    private static final int DEFAULT_TIMEOUT_SOCKET = 6000;
    private String url;
    private int timeoutConnection;
    private int timeoutSocket;
    private String httpMethod = "GET";

    /**
     * @param url Url a cui inviare la richiesta
     */
    public Internet(String url) {
        setUrl(url);
        setHttpMethod(Internet.METHOD_GET);
        setTimeoutConnection(DEFAULT_TIMEOUT_CONNECTION);
        setTimeoutSocket(DEFAULT_TIMEOUT_SOCKET);
    }

    public Internet(String url, String httpMethod) {
        setUrl(url);
        setHttpMethod(httpMethod);
        setTimeoutConnection(DEFAULT_TIMEOUT_CONNECTION);
        setTimeoutSocket(DEFAULT_TIMEOUT_SOCKET);
    }

    public Internet(String url, String httpMethod, int timeoutConnection) {
        setUrl(url);
        setHttpMethod(httpMethod);
        setTimeoutConnection(timeoutConnection);
        setTimeoutSocket(DEFAULT_TIMEOUT_SOCKET);
    }

    public Internet(String url, String httpMethod, int timeoutConnection, int timeoutSocket) {
        setUrl(url);
        setHttpMethod(httpMethod);
        setTimeoutConnection(timeoutConnection);
        setTimeoutSocket(timeoutSocket);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public JSONObject getJSONObject() throws Exception {
        try {
            return new JSONObject(getResponse());
        } catch (JSONException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna statusCode:" + e.getMessage());
            throw new Exception("La risposta del server non e' nel formato previsto:" + e.getMessage());
        }
    }

    public JSONArray getJSONArray() throws Exception {
        try {
            return new JSONArray(getResponse());
        } catch (JSONException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna statusCode:" + e.getMessage());
            throw new Exception("La risposta del server non e' nel formato previsto:" + e.getMessage());
        }
    }

    private DefaultHttpClient buildResponse() {
        HttpParams httpParameters = new BasicHttpParams();
        //httpParameters.setParameter("")
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = getTimeoutConnection();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = getTimeoutSocket();
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient client = new DefaultHttpClient(httpParameters);
        /*In caso di proxy*/
        //Log.i("",Build.BRAND);
        if (Build.BRAND.contains("generic")) {
            Log.w("* * * Atenzione * * * ", "Per il test è impostato il proxy se siamo nell'emulatore in " + this.getClass().toString());
            HttpHost proxy = new HttpHost("proxyhttp.comune.intranet", 8080);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return client;
    }

    public String getResponse() throws Exception {
        HttpRequestBase httpRequest = new HttpGet(getUrl());
        DefaultHttpClient client = buildResponse();

        String responseText = getResposeText(client, httpRequest);
        return responseText;

    }

    public String getPostResponse(HttpEntity postParameters) throws Exception {
        HttpPost httpRequest = new HttpPost(getUrl());
        httpRequest.setEntity(postParameters);
        DefaultHttpClient client = buildResponse();

        String responseText = getResposeText(client, httpRequest);
        return responseText;
    }

    private String getResposeText(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {
        StringBuilder builder = new StringBuilder();
        try {
            HttpResponse response = client.execute(httpRequest);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            //Log.i("statusCode:",String.valueOf(statusCode));
            switch (statusCode) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    break;
                case 404:
                    Log.e(this.toString(), "La pagina " + getUrl() + " non e' stata trovata");
                    throw new Exception("Pagina non trovata");
                case 401:
                    Log.e(this.toString(), "La pagina " + getUrl() + " richiede l'autenticazione");
                    throw new Exception("La pagina richiede l'autenticazione");
                case 500:
                    Log.e(this.toString(), "Il server alla pagina " + getUrl() + " e' andato in errore");
                    throw new Exception("La pagina richiesta al momento non funziona");
                default:
                    Log.e(this.toString(), "Il server alla pagina " + getUrl() + " ha restituito statusCode:" + statusCode);
                    throw new Exception("La pagina richiesta al momento non e' raggiungibile");
            }
        } catch (ClientProtocolException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore nel protocollo:" + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore nel protocollo:" + e.toString());
        } catch (IOException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore di Input/Output:" + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore di Input/Output:" + e.toString());
        } catch (Exception e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore:" + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore:" + e.toString());
        }
        return builder.toString();

    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public void setHttpMethod(String httpmethod) {
        this.httpMethod = httpmethod;
    }

    public int getTimeoutSocket() {
        return timeoutSocket;
    }

    public void setTimeoutSocket(int timeoutSocket) {
        this.timeoutSocket = timeoutSocket;
    }

    public int getTimeoutConnection() {
        return timeoutConnection;
    }

    public void setTimeoutConnection(int timeoutConnection) {
        this.timeoutConnection = timeoutConnection;
    }

}
