package com.identos.smarthomecare_app;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

import static java.security.AccessController.getContext;

/**
 * Created by noahz on 07.03.2018.
 */

public class BeaconService extends Service implements BeaconConsumer {
    private static final String TAG = BeaconService.class.getSimpleName();
    private BeaconManager beaconManager;
    //private Timestamp timestamp; //TODO: Read real client_id

    public int counter;
    public long start;
    public BeaconEvent[] beacArr = new BeaconEvent[100];
    //public ArrayList<String> beacAdd = new ArrayList<String>();
    //public long[] beacStart = new long[100];
    //public TwoDimentionalArrayList<String> beacArr;
    public int beaconIndex;
    public int currentBeacons = 0;
    public NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundScanPeriod(1500);
        beaconManager.setBackgroundBetweenScanPeriod(3000); //TODO : raise Time
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        //Realm init
        Context context = this;
        // Initialize Realm (just once per application)
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm").deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
        // Get a Realm instance for this thread

        //Set a records to recording = false
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Timestamp> query = realm.where(Timestamp.class);

        query.equalTo("Recording", true);
        RealmResults<Timestamp> stillRecording = query.findAll();

        realm.beginTransaction();
        for (int i = 0; i < stillRecording.size(); i++) {
            stillRecording.get(i).setRecording(false);
        }
        realm.commitTransaction();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service is running.");
        builder = new NotificationCompat.Builder(this, "BeaconNotification")
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

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Realm realm = Realm.getDefaultInstance();
                int s = beacons.toArray().length;
                final Beacon[] beaconArr = beacons.toArray(new Beacon[s]);
                int recordingBeacons = 0;

                if (beacons.size() > 0) {
                    for(counter = 0; counter < beacons.size(); counter++){
                        //final int y = i; //what a beauty of solution

                        //RealmQuery<Timestamp> recorded = realm.where(Timestamp.class);
                        RealmQuery<Timestamp> recording = realm.where(Timestamp.class);

                        //recorded.equalTo("Client_ID", beaconArr[i].getBluetoothAddress());
                        //recorded.and().equalTo("Recording", false);
                        recording.equalTo("Client_ID", beaconArr[counter].getManufacturer());
                        recording.and().equalTo("Recording", true);

                        //RealmResults<Timestamp> recordedThisClient = recorded.findAll();
                        RealmResults<Timestamp> recordingThisClient = recording.findAll();

                        //Case 1: Beacon just entered region
                        if (recordingThisClient.size() == 0) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Timestamp timestamp = realm.createObject(Timestamp.class);
                                    timestamp.setArrival(System.currentTimeMillis());
                                    timestamp.setClient_ID(beaconArr[counter].getManufacturer());
                                    timestamp.setRecording(true);
                                }
                            });
                            Log.i(TAG, "Beacon:" + beaconArr[counter].getManufacturer()+" just entered region.");
                        }
                        //Case 2: Beacon still in region
                        if (recordingThisClient.size() == 1) {
                            recordingBeacons++;     //Still recording...
                            Log.i(TAG, "Beacon:" + beaconArr[counter].getManufacturer()+" still in region.");
                        }
                        else {
                            Log.i(TAG, "Error after Case 2! More than one time recording.");
                        }
                    }
                    Log.i(TAG, "Number of Beacons in region:" + recordingBeacons);

                    //Case 3: Beacon just left region
                    RealmQuery<Timestamp> recording = realm.where(Timestamp.class);
                    recording.equalTo("Recording", true);
                    RealmResults<Timestamp> recordingAll = recording.findAll();
                    for(int i = 0; i < recordingAll.size(); i++) {
                        Boolean inRegion = false;
                        for(int y = 0; y < beaconArr.length; y++) {
                            if (recordingAll.get(i).getClient_ID() == beaconArr[y].getManufacturer()){
                                inRegion = true;
                            }
                        }
                        if(inRegion == false) {
                            realm.beginTransaction();
                            recordingAll.get(i).setRecording(false);
                            recordingAll.get(i).setDeparture(System.currentTimeMillis());
                            realm.commitTransaction();
                            Log.i(TAG, "Timestamp with Client: " + recordingAll.get(i).getClient_ID() + " was recorded successfully!");
                        }
                    }
                    //TODO: Case 4: Beacon left short time ago
                }
                else{
                    Log.i(TAG, "Currently no beacons in sight");
                }
            }
        });
        baseTime = System.currentTimeMillis();
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            //beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}