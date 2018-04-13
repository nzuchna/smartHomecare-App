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
        beaconManager.setBackgroundScanPeriod(1000);
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
        Realm realm = Realm.getDefaultInstance();
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
        /*
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

                start = System.currentTimeMillis();
                beacArr[beaconIndex].setBeacAdd(region.getBluetoothAddress());
                beacArr[beaconIndex].setStartTime(start);
                beaconIndex++;
                currentBeacons++;
                Log.i(TAG, "Timestemp Record started: " + beacArr[beaconIndex-1].getStartTime() + " BluetoothAdd: " + beacArr[beaconIndex-1].getBeacAdd());
                stopForeground(true);
                builder.setContentText("Beacons in range: " + currentBeacons);
                startForeground(1, builder.build());

                //timestamp.addTimestamp(start, client_id);
                //Check if Beacon is relevant
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {

                long end =  System.currentTimeMillis();
                int indexOf = -1;
                for(int i = 0; i <= beacArr.length; i++){
                    if(beacArr[i].getBeacAdd() == region.getBluetoothAddress()){
                        indexOf = i;
                        break;
                    }
                }
                if(indexOf == -1){
                    Log.i(TAG, "Error code: -1");
                }
                Log.i(TAG, "Timestemp Record ended: " + end + " After: " + (end - beacArr[indexOf].getStartTime()) + " Beacon Address: " + beacArr[indexOf].getBeacAdd());
                currentBeacons--;
                builder = BeaconService.this.builder.setContentText("Beacons in range: " + currentBeacons);

                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });
        */

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Realm realm = Realm.getDefaultInstance();
                int s = beacons.toArray().length;
                final Beacon[] beaconArr = beacons.toArray(new Beacon[s]);
                int recordingBeacons = 0;

                if (beacons.size() > 0) {
                    for(int i = 0; i < beacons.size(); i++){
                        final int y = i; //what a beauty of solution

                        RealmQuery<Timestamp> recorded = realm.where(Timestamp.class);
                        RealmQuery<Timestamp> recording = realm.where(Timestamp.class);

                        recorded.equalTo("Client_ID", beaconArr[i].getBluetoothAddress());
                        recorded.and().equalTo("Recording", false);
                        recording.equalTo("Client_ID", beaconArr[i].getBluetoothAddress());
                        recording.and().equalTo("Recording", true);

                        RealmResults<Timestamp> recordedThisClient = recorded.findAll();
                        RealmResults<Timestamp> recordingThisClient = recording.findAll();

                        //Case 1: Beacon just entered region
                        if (recordingThisClient == null) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Timestamp timestamp = realm.createObject(Timestamp.class);
                                    timestamp.setArrival(System.currentTimeMillis());
                                    timestamp.setClient_ID(beaconArr[y].getBluetoothAddress());
                                    timestamp.setRecording(true);
                                }
                            });
                            Log.i(TAG, "Beacon:" + beaconArr[i].getBluetoothAddress()+" just entered region.");
                        }
                        //Case 2: Beacon still in region
                        if (recordingThisClient != null) {
                            recordingBeacons++;     //Still recording...
                            Log.i(TAG, "Beacon:" + beaconArr[i].getBluetoothAddress()+" still in region.");
                        }
                        //TODO: Case 4: Beacon left short time ago
                    }
                    Log.i(TAG, "Number of Beacons in region:" + recordingBeacons);
                    /*
                    //Case 3: Beacon just left region
                    RealmQuery<Timestamp> lastRecorded = realm.where(Timestamp.class);
                    lastRecorded.equalTo("Recording", true);
                    RealmResults<Timestamp> lastRecords = lastRecorded.findAll();
                    RealmQuery<Timestamp> query = lastRecords.where();
                    for(int i = 0; i < lastRecords.size(); i++) {

                        if (lastRecords.equals("Client_ID", beaconArr[i].getBluetoothAddress())){

                        }
                        else {  //

                        }
                    }
                    */

                    long timeStamp = System.currentTimeMillis() - baseTime;


                    //Obtaining the list of beacons heard
//                    List<BeaconParser> beaconParserList = beaconManager.getBeaconParsers();
//                    Log.d(TAG,Long.toString(beaconParserList.get(0).getDataFieldCount()));
//                    Log.d(TAG,Long.toString(beaconParserList.get(1).bytesToHex(
//                           beaconParserList.get(1).getBeaconAdvertisementData()
//                    )));
                    /*
                    for (int i = 0; i < s; i++) {
                        Log.i(TAG, "Time:"+timeStamp+" Address: " + beaconArr[i].getBluetoothAddress()
                                + "Beacon Address:" + beaconArr[i].getBluetoothAddress() + "UUID:" + beaconArr[i].getServiceUuid());
                       /* logToDisplay(Long.toString(timeStamp),              //Adding the information to the screen
                                beaconArr[i].getBluetoothAddress(),
                                Integer.toString(beaconArr[i].getRssi()),
                                Integer.toString(clickCount));
                        data.add(new String[]{Long.toString(timeStamp),     //Appending the information to the data variable
                                beaconArr[i].getBluetoothAddress(),
                                Integer.toString(beaconArr[i].getRssi()),
                                Integer.toString(clickCount)});
                    }*/
                }
                else{
                    Log.i(TAG, "Currently no beacons in sight");
                }
//                // Asynchronously update objects on a background thread
//                realm.executeTransactionAsync(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm bgRealm) {
//                        Dog dog = bgRealm.where(Dog.class).equalTo("age", 1).findFirst();
//                        dog.setAge(3);
//                    }
//                }, new Realm.Transaction.OnSuccess() {
//                    @Override
//                    public void onSuccess() {
//                        // Original queries and Realm objects are automatically updated.
//                        puppies.size(); // => 0 because there are no more puppies younger than 2 years old
//                        managedDog.getAge();   // => 3 the dogs age is updated
//                    }
//                });

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