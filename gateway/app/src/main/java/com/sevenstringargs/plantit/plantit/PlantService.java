package com.sevenstringargs.plantit.plantit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sevenstringargs.plantit.plantit.threads.BluetoothThread;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Robban on 17/11/15.
 */
public class PlantService extends Service {
    private BluetoothThread bluetoothThread;
    private Timer discoverTimer;
    private TimerTask discoverTask;
    private boolean runTask = true;
    private boolean running = false;
    private long interval = 1000*3*60;

    @Override
    public void onCreate(){
        Log.i("ssa", "Creating service");
        startDiscoverTask();
    }

    private TimerTask createDiscoverTask(){
        return new TimerTask(){

            @Override
            public void run(){
                bluetoothThread = new BluetoothThread(PlantService.this);
                bluetoothThread.run();
            }
        };
    }

    private void startDiscoverTask(){
        discoverTimer = new Timer();
        discoverTask = createDiscoverTask();
        discoverTimer.schedule(discoverTask, 0L, interval);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

    @Override
    public void onRebind(Intent intent){

    }


    @Override
    public void onDestroy(){
        bluetoothThread.unRegisterBroadcastListeners();
        Log.i("ssa", "Destroying service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
