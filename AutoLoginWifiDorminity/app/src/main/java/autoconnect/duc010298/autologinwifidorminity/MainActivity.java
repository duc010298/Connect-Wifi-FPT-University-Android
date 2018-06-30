package autoconnect.duc010298.autologinwifidorminity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText txtWifiName;
    private EditText txtURL;
    private EditText txtUsername;
    private EditText txtPassword;
    private TextView txtStatus;
    private TextView txtTotalRequest;

    private String wifiName;
    private String URL;
    private String user;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectView();

        registerReceiver(receiveData, new IntentFilter("statusSendRequest"));
    }

    //    @Override
//    public void onResume(){
//        super.onResume();
//        if(isServiceRunning(AutoSendRequest.class)) {
//            txtStatus.setText("Status: Service is running");
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiveData);
        stopService();
    }

    private void connectView() {
        txtWifiName = (EditText) findViewById(R.id.txtWifiName);
        txtURL = (EditText) findViewById(R.id.txtURL);
        txtUsername = (EditText) findViewById(R.id.txtUser);
        txtPassword = (EditText) findViewById(R.id.txtPass);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtTotalRequest = (TextView) findViewById(R.id.txtTotalRequest);
    }

    private void getConfig() {
        wifiName = '"' + txtWifiName.getText().toString().trim() + '"';
        URL = txtURL.getText().toString().trim();
        user = txtUsername.getText().toString().trim();
        pass = txtPassword.getText().toString().trim();
    }

    public void doStart(View view) {
        getConfig();
        startService();
    }

    public void doStop(View view) {
        stopService();
    }

//    private boolean isServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void startService() {
        Intent serviceIntent = new Intent(MainActivity.this, AutoSendRequest.class);
        serviceIntent.putExtra("wifiName", wifiName);
        serviceIntent.putExtra("URL", URL);
        serviceIntent.putExtra("user", user);
        serviceIntent.putExtra("pass", pass);
        this.startService(serviceIntent);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(MainActivity.this, AutoSendRequest.class);
        this.stopService(serviceIntent);
    }

    private BroadcastReceiver receiveData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = null;
            String totalRequest = null;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                status =  bundle.getString("status");
                totalRequest = bundle.getString("totalRequest");
            }
            if(!status.equals("null")) {
                txtStatus.setText("Status: " + status);
            }
            if(!totalRequest.equals("null")) {
                txtTotalRequest.setText("Total request: " + totalRequest);
            }
        }
    };
}
