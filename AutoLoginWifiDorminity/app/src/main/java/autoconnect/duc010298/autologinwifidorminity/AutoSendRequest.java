package autoconnect.duc010298.autologinwifidorminity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class AutoSendRequest extends Service {
    private String wifiName;
    private String URLConnect;
    private String user;
    private String pass;
    private Thread t = null;
    private boolean flag = false;
    private int totalRequest = 0;
    private String status = "Service is running";

    private BroadcastReceiver networkChangeReceiver;

    public AutoSendRequest() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        registerNetworkChangeReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        wifiName = intent.getStringExtra("wifiName");
        URLConnect = intent.getStringExtra("URL");
        user = intent.getStringExtra("user");
        pass = intent.getStringExtra("pass");
        if(!isCorrectWifi()) {
            status = "Incorrect SSID, waiting to connect to correct network";
            sendStatus(status, null);
        } else {
            sendRequest();
            startThread();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.networkChangeReceiver);
        killThread();
        status = "Service have been stopped by user";
        sendStatus(status, null);
    }

    private void registerNetworkChangeReceiver() {
        final IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        this.networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isCorrectWifi()) {
                    startThread();
                    status = "Service is running";
                    sendStatus(status, null);
                } else {
                    killThread();
                    status = "Incorrect SSID, waiting to connect to correct network";
                    sendStatus(status, null);
                }
            }
        };
        this.registerReceiver(this.networkChangeReceiver, filter);
    }

    private boolean isCorrectWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiName.equals(wifiInfo.getSSID()) && wifiManager.isWifiEnabled();
    }

    private void startThread() {
        flag = true;
        if(t == null) {
            t = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if(flag) {
//                            if(isRedirected()) {
                            sendRequest();
//                            }
                            try {
                                sleep(15000);
                            } catch (InterruptedException ex) { }
                        } else {
                            break;
                        }
                    }
                }
            };
        }
        if(!t.isAlive()) {
            t.start();
        }
    }

    private void killThread() {
        if(t == null) {
            return;
        }
        flag = false;
        t.interrupt();
        t = null;
    }

//    private boolean isRedirected() {
//        URL url = null;
//        try {
//            url = new URL("http://www.google.com/");
//        } catch (MalformedURLException e) {
//            return true;
//        }
//        boolean ret = false;
//        HttpURLConnection urlConnection = null;
//        try {
//            urlConnection = (HttpURLConnection) url.openConnection();
//        } catch (IOException e) {
//            return true;
//        }
//        try {
//            try {
//                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            } catch (IOException e) {
//                ret = true;
//            }
//            if (!url.getHost().equals(urlConnection.getURL().getHost())) {
//                ret = true;
//            }
//        } finally {
//            urlConnection.disconnect();
//        }
//        if(ret) {
//            status = "Sending request, service is running";
//        } else {
//            status = "Don't need to send request now, service is running";
//        }
//        sendStatus(status, null);
//        return ret;
//    }

    private void sendRequest() {
        HashMap<String, String> dataPost = new HashMap<>();
        dataPost.put("auth_user", user);
        dataPost.put("auth_pass", pass);
        dataPost.put("accept", "true");

        try {
            URL url = new URL(URLConnect);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(false);
            conn.setDoOutput(false);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            writer.write(getPostDataString(dataPost));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                status = "Send request successfully, service is running";
                totalRequest++;
            } else {
                status = "Cannot send request, service is running";
            }
        } catch (Exception e) {
            status = "Cannot send request, service is running";
        }
        sendStatus(status, "" + totalRequest);
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private void sendStatus(String status, String totalRequest) {
        if(status == null) {
            status = "null";
        }
        if(totalRequest == null) {
            totalRequest = "null";
        }
        Intent intent = new Intent("statusSendRequest");
        intent.putExtra("status", status);
        intent.putExtra("totalRequest", totalRequest);
        sendBroadcast(intent);
    }
}
