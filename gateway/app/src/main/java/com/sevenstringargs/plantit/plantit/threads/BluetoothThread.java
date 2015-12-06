package com.sevenstringargs.plantit.plantit.threads;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.sevenstringargs.plantit.plantit.broadcastReceivers.BluetoothReceiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

/**
 * Created by Robban on 17/11/15.
 */

public class BluetoothThread extends Thread implements Tasker {
    private String blModuleName = "PlantIT";
    private Service service;
    private HashMap<String, BluetoothDevice> hasPairedDevices;
    private HashMap<String, BluetoothDevice> foundDevices;
    private BluetoothReceiver receiver;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothThread(Service service){
        this.service = service;
        hasPairedDevices = new HashMap<>();
        foundDevices = new HashMap<>();
    }


    public void handleFoundDevice(Context context, Intent intent){
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.i("ssa", "Handling found device: " + device.getAddress() + " - " + device.getName());

        if (device == null){
            return;
        }

        if (device.getName() == null){
            return;
        }

        if (device.getName().equals(blModuleName)){
            foundDevices.put(device.getAddress(), device);
        }
    }

    public void handleStateChange(Context context, Intent intent){
        Log.i("ssa", "Handling state change");
    }

    public void handleDiscoveryStarted(Context context, Intent intent){
        Log.i("ssa", "Handling discovery started");
        foundDevices = new HashMap<String, BluetoothDevice>();
    }

    public void handleDiscoveryFinished(Context context, Intent intent){
        Log.i("ssa", "Handling discovery finished");

        Log.i("ssa", "Number of potential devices found: " + foundDevices.size());
        if (foundDevices.size() > 0){
            Set<String> keys = foundDevices.keySet();
            for (String key : keys){
                BluetoothDevice device = foundDevices.get(key);
                Log.i("ssa", "Device: " + device.getAddress() + " - " + device.getName());
            }
        }

        pairWithPotentialDevices();
    }

    public void handlePairingRequest(Context context, Intent intent){
        Log.i("ssa", "Handling pairing request");
        try {
            byte[] pin = (byte[])BluetoothDevice.class.getMethod("convertPinToBytes",
                    String.class).invoke(BluetoothDevice.class, "1234");
            //       Method m = blue
        }  catch(Exception e){
            e.printStackTrace();
        }

    }

    public boolean initBluetoothAdapter(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            Log.i("ssa", "Device does not support bluetooth");
            return false;
        }

        receiver = new BluetoothReceiver(this);

        return true;
    }



    public Service getService(){
        return service;
    }


    public void pairWithPotentialDevices(){
        Log.i("ssa", "Attempting to pair with all found devices");

        for (String key : foundDevices.keySet()){
            BluetoothDevice device = foundDevices.get(key);
            ConnectThread con = new ConnectThread(service, device);
            con.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public TimerTask createTimerTask(){
        return new TimerTask(){
            @Override
            public void run(){
                BluetoothThread.this.run();
            }
        };
    }

    public boolean setup(){
        if (!initBluetoothAdapter()){
            return false;
        }

        Log.i("ssa", "Setup success");
        return true;
    }

    public void getRelevantPairedDevices(){
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> previousPairDevices = new ArrayList<>();

        for (BluetoothDevice device : devices){
            if (device.getName().equals(blModuleName)){
                hasPairedDevices.put(device.getAddress(), device);
                previousPairDevices.add(device.getAddress());
            }
        }

        Log.i("ssa", "Has paired with " + devices.size() + " possible PlantIT devices");

        if (previousPairDevices.size() == 0)
            return;

        for (String address : previousPairDevices){
            Log.i("ssa", "Device: " + address);
        }
    }


    @Override
    public void run(){
        Log.i("ssa", "Running BluetoothThread");

        if (!setup()){
            Log.i("ssa", "Setup failed");
            return;
        }

        getRelevantPairedDevices();

        if (!bluetoothAdapter.isEnabled()){
            Log.i("ssa", "Bluetooth is disabled, enable to connect to devices");
            return;
        }
        Log.i("ssa", "Bluetooth is enabled");

        //Loop with check that bluetooth is enabled
        bluetoothAdapter.startDiscovery();


    }
    
    public void unRegisterBroadcastListeners(){
        receiver.unRegister();
    }
}
