package com.sevenstringargs.plantit.plantit.threads;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.sevenstringargs.plantit.plantit.util.Data;
import com.sevenstringargs.plantit.plantit.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Robban on 19/11/15.
 */
public class ConnectionThread  extends Thread {
    private BluetoothSocket socket = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private long requestInterval = 10000;
    private Timer timer;
    private String data = "";
    private byte[] buffer;
    private int bytes;
    private Service service;
    private int requestCount = 0;

    public ConnectionThread(Service service, BluetoothSocket socket){
        this.service = service;
        this.socket = socket;
    }

    public void setup() throws IOException {
        inStream = socket.getInputStream();
        outStream = socket.getOutputStream();
        Data.createNewMqttConnection(socket.getRemoteDevice().getAddress());
    }

    public boolean hasDataSet(){
        return data.charAt(data.length()-1) == '$';
    }

    public boolean addToData(String newData){
        data += newData;
        return hasDataSet();
    }

    public String getData(){
        String temp = data;
        data = "";

        return temp;
    }

    public void requestData(){
        try {
            Log.i("ssa", "Requesting data");
            requestCount++;


            if (requestCount > 1){
                Log.i("ssa", "Not receiveing data, this is what the data is currently from device: " + socket.getRemoteDevice().getAddress());
                Log.i("ssa", data);
                Log.i("ssa", String.valueOf(data.length()));
            }

            outStream.write(new byte[]{'c','A'});
            outStream.write(new byte[]{'d'});
        } catch (IOException e) {
            Log.i("ssa", "Failed to requet data");
            timer.cancel();
            e.printStackTrace();
        }
    }

    @Override
    public void run(){

        try {
            setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimerTask task = new TimerTask(){

            @Override
            public void run() {
                requestData();
            }
        };

        timer = new Timer();
        timer.schedule(task, new Date(), requestInterval);

        while(true){
            buffer = new byte[1024];
            if (requestCount > 1){

                Log.i("ssa", "Does not work");
            }
            try {
                bytes = inStream.read(buffer);
            } catch (IOException e) {
                Log.i("ssa", "Failed to read input stream, exiting thread");

                try {
                    inStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    outStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


                e.printStackTrace();
                return;
            }

            if (bytes > 0){
                String tempData = new String(Arrays.copyOfRange(buffer, 0, bytes));
                if (requestCount > 1){
                    Log.i("ssa", "Temp data for " + socket.getRemoteDevice().getAddress());
                    Log.i("ssa", tempData);

                }
                if(addToData(tempData)) {
                    String data = getData();
                    Log.i("ssa", "Received data from device: " + socket.getRemoteDevice().getAddress() + " - " + ": " + data);
                    requestCount = 0;
                    SendDataThread dataThread = new SendDataThread(service, Util.decodeData(data), socket.getRemoteDevice());
                    dataThread.start();
                }
            }
        }

    }
}
