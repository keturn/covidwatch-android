package com.riskre.covidwatch;

import android.app.Application;
import android.content.Context;


import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;

public class CovidWatchApplication extends Application {

    private RxBleClient rxBleClient;

    /**
     * TODO remove this function and use dependency injection as suggested by RxJava
     */
    public static RxBleClient getRxBleClient(Context context) {
        CovidWatchApplication application = (CovidWatchApplication) context.getApplicationContext();
        return application.rxBleClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );
    }
}