package com.sevenstringargs.plantit.plantit.broadcastReceivers;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sevenstringargs.plantit.plantit.threads.BluetoothThread;

import java.util.HashMap;

/**
 * Created by Robban on 17/11/15.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private BluetoothThread thread;
    private IntentFilter[] intentFilters = new IntentFilter[] {
            new IntentFilter(BluetoothDevice.ACTION_FOUND),
            new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED),
            new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    };
    private boolean discovering;
    private HashMap<String, BluetoothDevice> found;

    public BluetoothReceiver(BluetoothThread thread){
        this.thread = thread;
        registerIntentFilters();
        discovering = false;
        found = new HashMap<>();
    }

    private void registerIntentFilters(){
        Service service = thread.getService();

        for (IntentFilter filter : intentFilters){
            service.registerReceiver(this, filter);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch(action){
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!found.containsKey(device.getAddress())){
                    found.put(device.getAddress(), device);
                    thread.handleFoundDevice(context, intent);
                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                thread.handleStateChange(context, intent);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                if (!discovering){
                    discovering = true;
                    found = new HashMap<>();
                    thread.handleDiscoveryStarted(context, intent);
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                discovering = false;
                thread.handleDiscoveryFinished(context, intent);
                break;
            case "android.bluetooth.device.action.PAIRING_REQUEST":
                thread.handlePairingRequest(context, intent);
                break;
        }
    }

    public void unRegister(){
        thread.getService().unregisterReceiver(this);
    }
}
