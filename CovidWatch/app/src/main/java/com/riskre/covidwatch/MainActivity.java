package com.riskre.covidwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private ArrayList<String> contact_event_numbers = new ArrayList<String>();
    private RecyclerView.Adapter cen_adapter;
    private BluetoothAdapter bluetoothAdapter;
    private boolean currently_scanning;
    private Handler handler;
    private static final int SCAN_REQUEST_CODE = 42;
    private RxBleClient rxBleClient;
    private PendingIntent callbackIntent;
    private boolean hasClickedScan;


    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initRecyclerView();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Ask user to turn on bluetooth if they haven't already
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        rxBleClient = CovidWatchApplication.getRxBleClient(this);
        callbackIntent = PendingIntent.getBroadcast(this, SCAN_REQUEST_CODE,
                new Intent(this, ScanReceiver.class), 0);
    }

    /**
     * Initialize the RecylerView that holds the Contact Events
     */
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cen_adapter = new ContactEventsAdapter(contact_event_numbers, this);
        recyclerView.setAdapter(cen_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void onContacLoggingToggle(View view) {
        hasClickedScan = true;
        if (rxBleClient.isScanRuntimePermissionGranted()) {
            Log.i("COVIDWATCH", "Starting background scan ===================");
            scanBleDeviceInBackground();
        } else {
            LocationPermission.requestLocationPermission(this, rxBleClient);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rxBleClient.getBackgroundScanner().stopBackgroundBleScan(callbackIntent);
        }
    }

    private void scanBleDeviceInBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                rxBleClient.getBackgroundScanner().scanBleDeviceInBackground(
                        callbackIntent,
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
                                .setDeviceAddress(GattAttributes.lookup("HumanPresenceService""))
                                // add custom filters if needed
                                .build()
                );
            } catch (BleScanException scanException) {
                Log.w("BackgroundScanActivity", "Failed to start background scan", scanException);
                ScanExceptionHandler.handleException(this, scanException);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults, rxBleClient)
                && hasClickedScan) {
            hasClickedScan = false;
            scanBleDeviceInBackground();
        }
    }
}