package com.sevenstringargs.plantit.plantit.threads;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Robban on 19/11/15.
 */
public class ConnectThread extends Thread {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");;

    private BluetoothSocket socket;
    private BluetoothDevice device;
    private Service service;

    public ConnectThread(Service service, BluetoothDevice device){
        this.device = device;
        this.service = service;
    }

    private void manageSocket(){
        Log.i("ssa", "Manage socket");
        ConnectionThread connectionThread = new ConnectionThread(service, socket);
        connectionThread.start();
    }


    @Override
    public void run() {
        Log.i("ssa", "Trying to establish a socket connection with device: " + device.getAddress() + " - " + device.getName());

        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
        } catch (IOException e) {
            Log.i("ssa", "Failed to establish connection with device: " + device.getAddress() + " - " + device.getName());
            e.printStackTrace();
            return;
        }

        Log.i("ssa", "Socket connected to device: " + device.getAddress() + " - " + device.getName());
        manageSocket();
    }
}
