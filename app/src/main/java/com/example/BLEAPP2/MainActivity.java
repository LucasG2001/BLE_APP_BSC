package com.example.BLEAPP2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_BLUETOOTH_ADMIN_ID = 1;
    private static final int REQUEST_LOCATION_ID = 2;
    private static final int REQUEST_BLUETOOTH_ID = 3;
    private BluetoothAdapter bleAdapter;

    private String deviceName;
    private String deviceAddress;
    private BluetoothLeService bleService;
    private boolean connected = false;
    private ImageView reconnectView;
    private ProgressBar batteryStatus;
    private ProgressBar scanProgressBar;
    private TextView scanView;
    private SharedPreferences sharedPrefBLE;

    private TextView warningtextview;
    private TextView gasConcentrationView;
    private TextView VarianceView;
       //initialize graph Series
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
            new DataPoint(0, 0),
    });

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // manipulate series
        series.setTitle("Random Curve 1");
        series.setColor(Color.RED);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
        //plots line graph
        GraphView graph = findViewById(R.id.graph);
        graph.setTitle("Source-Drain current in mA vs time");
        graph.getViewport().setScalable(true); //enables scrolling
        graph.getViewport().setXAxisBoundsManual(true); //sets x axis boundaries
        graph.getViewport().setMinX(0);
        // set axis titles
        graph.getGridLabelRenderer().setHighlightZeroLines(true);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time [s]");
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Source-Drain current in mA");
        graph.getGridLabelRenderer().setLabelsSpace(0);
        graph.getGridLabelRenderer().setPadding(100);
        // set graph such that only integer numbers are displayed
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf));
        //some nice layout
        graph.getViewport().setBackgroundColor(0x27272f);

        // add data
        graph.addSeries(series);



        sharedPrefBLE = getSharedPreferences(getString(R.string.ble_device_key),Context.MODE_PRIVATE);
        deviceName = sharedPrefBLE.getString("name",null);
        deviceAddress = sharedPrefBLE.getString("address",null);
        bleCheck();
        locationCheck();

        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bleAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeLayout();
        establishServiceConnection();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleService != null) {
            unbindService(serviceConnection);
            bleService = null;
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService.connect(deviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    // Handles various events fired by the Service.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                scanProgressBar.setVisibility(View.VISIBLE);
                scanView.setVisibility(View.GONE);
                reconnectView.setVisibility((View.GONE));

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                scanProgressBar.setVisibility(View.GONE);
                scanView.setVisibility(View.GONE);
                reconnectView.setVisibility((View.VISIBLE));

            }

            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
        }
            else if (BluetoothLeService.ACTION_DATA_READ_COMPLETED.equals(action)) {
                Log.d(TAG, "Data Read Completed");

                updateUI();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                if (intent.getStringExtra(BluetoothLeService.ACTION_BATTERY_LEVEL) != null) {
                    Log.d(TAG, "Battery level on main activity: " + intent.getStringExtra(BluetoothLeService.ACTION_BATTERY_LEVEL));

                }
            }
        }
    };

    @Override
    protected void onResume() { //comes when onPause is disabled
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() { //executed when activity gets paused, like going to homescreen
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ_COMPLETED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void establishServiceConnection() {
        if (deviceName != null && deviceAddress != null) {
            scanProgressBar.setVisibility(View.VISIBLE);
            scanView.setVisibility(View.GONE);
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void bleCheck() {
        if (ActivityCompat.checkSelfPermission(this, BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // Bluetooth permission has not been granted.
            ActivityCompat.requestPermissions(this,new String[]{BLUETOOTH},REQUEST_BLUETOOTH_ID);
        }
        if (ActivityCompat.checkSelfPermission(this, BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Bluetooth admin permission has not been granted.
            ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_ADMIN_ID);
        }
    }

    private void locationCheck() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission has not been granted.
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_ID);
        }
    }

    private void updateUI() {
        SharedPreferences sharedPrefAgm = getSharedPreferences(getString(R.string.agm_key),Context.MODE_PRIVATE);
        String xAccAvg = sharedPrefAgm.getString("x_acc_avg", null); //original: null
        String yAccAvg = sharedPrefAgm.getString("y_acc_avg", null);   //original: null
        String zAccAvg = sharedPrefAgm.getString("z_acc_avg", null);   //original: null

        gasConcentrationView.setText(zAccAvg);
        warningtextview.setTextColor(getResources().getColorStateList(R.color.colorWhite));
        VarianceView.setText("12");
        warningtextview.setTextColor(getResources().getColorStateList(R.color.colorWhite));

       if (Integer.parseInt(zAccAvg) < 50 || yAccAvg == null) {
            warningtextview.setText("Gas concentration low");
            warningtextview.setTextColor(getResources().getColorStateList(R.color.colorGreen));
        } else if (Integer.parseInt(zAccAvg) > 50 && Integer.parseInt(zAccAvg) < 200) {
           warningtextview.setText("Gas concentration medium");
           warningtextview.setTextColor(getResources().getColorStateList(R.color.colorYellow));
        } else if (Integer.parseInt(zAccAvg)>200){
           warningtextview.setText("Gas concentration high!");
           warningtextview.setTextColor(getResources().getColorStateList(R.color.colorPrimaryRed));
           }


        scanProgressBar.setVisibility(View.GONE);
        // append new value to graph
        series.appendData(new DataPoint(Integer.parseInt(yAccAvg),Integer.parseInt(xAccAvg)), true, 100000);
        Log.d("A","appended Data point");
        Log.d("A",""+Integer.parseInt(xAccAvg)+" "+Integer.parseInt(yAccAvg));
    }

    public void openBleScanner() {
        Intent i = new Intent(this, RecyclerBleDeviceActivity.class);
        startActivity(i);
    }

     @SuppressLint("WrongConstant")
    public void initializeLayout() {
        this.getSupportActionBar().setDisplayOptions(DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        final View actionView = getSupportActionBar().getCustomView();

        batteryStatus = actionView.findViewById(R.id.batteryProgressBar);
        scanView = actionView.findViewById(R.id.scan);
        scanProgressBar = actionView.findViewById(R.id.scanInProgress);
        reconnectView = actionView.findViewById(R.id.reconnect);


        warningtextview = findViewById(R.id.warningText);
        gasConcentrationView = findViewById(R.id.gasConcentrationValue);
        VarianceView = findViewById(R.id.VarianceValue);

         scanView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 openBleScanner();
             }
         });

        reconnectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanProgressBar.setVisibility(View.VISIBLE);
                scanView.setVisibility(View.GONE);
                reconnectView.setVisibility(View.GONE);
                bleService.connect(deviceAddress);
            }
        });
    }
}