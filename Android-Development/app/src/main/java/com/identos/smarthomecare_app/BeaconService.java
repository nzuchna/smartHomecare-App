package com.identos.smarthomecare_app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Created by noahz on 07.03.2018.
 */

public class BeaconService extends Service implements BeaconConsumer {
    private static final String TAG = BeaconService.class.getSimpleName();
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundScanPeriod(1000);
        beaconManager.setBackgroundBetweenScanPeriod(1000); //TODO : raise Time
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        beaconManager.bind(this);
        Log.i(TAG, "Service is running.");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "BeaconNotification")
                .setOngoing(true)
                .setSmallIcon(R.drawable.logolightblue)
                .setContentTitle("BeaconService")
                .setContentText("Scan running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        startForeground(1, builder.build());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service killed.");
        beaconManager.unbind(this);
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    long baseTime;
    @Override
    public void onBeaconServiceConnect() {
        long begin;
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                begin = System.currentTimeMillis();
                //Check if Beacon is relevant
            }

            @Override
            public void didExitRegion(Region region) {
                long end =  System.currentTimeMillis();

                long duration = end - begin;
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {   //
                   long timeStamp = System.currentTimeMillis() - baseTime;
                    int s = beacons.toArray().length;
                    Beacon[] beaconArr = beacons.toArray(new Beacon[s]);    //Obtaining the list of beacons heard
                    //List<BeaconParser> beaconParserList = beaconManager.getBeaconParsers();
                    //Log.d(TAG,Long.toString(beaconParserList.get(0).getDataFieldCount()));
                    //Log.d(TAG,Long.toString(beaconParserList.get(1).bytesToHex(
                    //       beaconParserList.get(1).getBeaconAdvertisementData()
                    //)));
                    for (int i = 0; i < s; i++) {
                        Log.i(TAG, "Time:"+timeStamp+" Address: " + beaconArr[i].getBluetoothAddress()
                                + "Beacon Address:" + beaconArr[i].getBluetoothAddress() + "Data Fields:" + beaconArr[i].getDataFields());
                       /* logToDisplay(Long.toString(timeStamp),              //Adding the information to the screen
                                beaconArr[i].getBluetoothAddress(),
                                Integer.toString(beaconArr[i].getRssi()),
                                Integer.toString(clickCount));
                        data.add(new String[]{Long.toString(timeStamp),     //Appending the information to the data variable
                                beaconArr[i].getBluetoothAddress(),
                                Integer.toString(beaconArr[i].getRssi()),
                                Integer.toString(clickCount)});*/
                    }
                }
            }
        });
        baseTime = System.currentTimeMillis();
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
