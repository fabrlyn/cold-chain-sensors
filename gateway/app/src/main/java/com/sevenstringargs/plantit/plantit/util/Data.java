package com.sevenstringargs.plantit.plantit.util;

import android.bluetooth.BluetoothClass;
import android.util.Log;

import com.ibm.iotf.client.device.DeviceClient;
import com.sevenstringargs.plantit.plantit.model.SensorData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Properties;

/**
 * Created by Robban on 05/12/15.
 */
public class Data {
    private static long minInMilli = 60000;
    private static HashMap<String, ArrayList<SensorData>> devicesData = new HashMap<>();
    private static HashMap<String, DeviceClient> mqttClients = new HashMap<>();

    public static double[] getData(String deviceId){
        dropOldData(deviceId);

        ArrayList<SensorData> list = devicesData.get(deviceId);
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double sum = 0;

        if (list == null){
            return new double[]{Double.MIN_VALUE, 0, Double.MAX_VALUE};
        }

        for (SensorData s : list){
            double value = s.getValue();

            if (max < value){
                max = value;
            }

            if (min > value){
                min = value;
            }

            sum += value;
        }

        double avg = sum / list.size();

        return new double[]{min, avg, max};
    }

    public static void createNewMqttConnection(String deviceId){
        if (!mqttClients.containsKey(deviceId)){
            DeviceClient client = createConnection(deviceId);
            mqttClients.put(deviceId, client);
        }
    }

    public static DeviceClient getClient(String deviceId){
        return mqttClients.get(deviceId);
    }

    public static DeviceClient createConnection(String deviceId){
        String token = "";
        String id = "";

        if (deviceId.equals("98:D3:31:80:47:1C")) {
            token = "fiIH-rk-eaaY43VDE*";
            id = "MedOne";
        } else {
            token = "8&_WYONxNg7Nxi?G?l";
            id = "MedTwo";
        }

        Properties options = new Properties();

        options.setProperty("org", "ql216x");
        options.setProperty("type", "RobbanIoT");
        options.setProperty("id", id);
        options.setProperty("auth-method", "token");
        options.setProperty("auth-token", token);

        DeviceClient myClient = null;
        try {
            //Instantiate the class by passing the properties file
            myClient = new DeviceClient(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Connect to the IBM IoT Foundation
        myClient.connect();

        return myClient;
    }

    public static void addSensorData(SensorData data){
        if (devicesData.containsKey(data.getDeviceId())){
            devicesData.get(data.getDeviceId()).add(data);
        } else {
            ArrayList<SensorData> list = new ArrayList<>();
            list.add(data);
            devicesData.put(data.getDeviceId(), list);
        }
    }

    private static void dropOldData(String deviceId){
        ArrayList<SensorData> deviceData = devicesData.get(deviceId);

        if (deviceData == null){
            return;
        }

        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        Date tenMinAgo = new Date(now - (1 * minInMilli));

        boolean remove;
        do {
            remove = false;
            SensorData data = deviceData.get(0);

           if (data != null && data.getDate().before(tenMinAgo)){
               remove = true;
               SensorData removed = deviceData.remove(0);

               Log.i("ssa", "Removed Sensor Data from when the time was " + removed.getDate().toString());
           }
        }while(remove);
    }
}
