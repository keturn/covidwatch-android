package com.riskre.quarantain;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Arrays;


public class QuarantainApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "QuarantainApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MonitoringActivity monitoringActivity = null;
    private String cumulativeDetections = "";
    private BeaconManager beaconManager;
    private Region region;
    private Beacon beacon;

    public void onCreate() {
        super.onCreate();

        beaconManager =  BeaconManager.getInstanceForApplication(this);

        // By default the AndroidBeaconLibrary will only find AltBeacons. We configure the beaconManager
        // to detect iBeacons which is what iPhones use for cross compatibility.
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // create a new beacon to transmit
         beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004C) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();
        // TODO some phones can do bluetooth LE scan but not transmit
        // need to check for that case here at some point
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                // TODO error handling
                Log.i(TAG, "Advertisement start failed with code: "+errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });

        beaconManager.setDebug(true);

        Log.d(TAG, "setting up background monitoring for beacons and power saving");

        // wake up the app when a beacon is seen
        region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("passively logging human contact");
        Intent intent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, we disable
        // JobScheduler-based scans and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.removeAllRangeNotifiers();
                beaconManager.removeAllMonitorNotifiers();
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
                // TODO proper handling
            }
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }
    public void enableMonitoring() {
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    @Override
    public void didEnterRegion(Region region) {
        // TODO make contact w/ a beacon
    }

    @Override
    public void didExitRegion(Region region) {
        // TODO lose contact w/ a beacon
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        // TODO ???
    }
}
