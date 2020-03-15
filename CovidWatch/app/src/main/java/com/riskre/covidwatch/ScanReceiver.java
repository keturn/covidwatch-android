package com.riskre.covidwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.BackgroundScanner;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.List;

public class ScanReceiver extends BroadcastReceiver {

    @RequiresApi(26 /* Build.VERSION_CODES.O */)
    @Override
    public void onReceive(Context context, Intent intent) {
        BackgroundScanner backgroundScanner = CovidWatchApplication.getRxBleClient(context).getBackgroundScanner();

        try {
            final List<ScanResult> scanResults = backgroundScanner.onScanResultReceived(intent);
            Log.w("ScanReceiver", "Scan results received: " + scanResults);
        } catch (BleScanException exception) {
            Log.w("ScanReceiver", "Failed to scan devices", exception);
        }
    }
}
