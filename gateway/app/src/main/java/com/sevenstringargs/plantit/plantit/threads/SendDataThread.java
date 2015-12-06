package com.sevenstringargs.plantit.plantit.threads;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.sevenstringargs.plantit.plantit.model.SensorData;
import com.sevenstringargs.plantit.plantit.util.Data;
import com.sevenstringargs.plantit.plantit.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SendDataThread extends Thread {
    BluetoothDevice device;
    String[] data;
    Service service;

    public SendDataThread(Service service, String[] data, BluetoothDevice device){
        this.data = data;
        this.device = device;
        this.service = service;
    }

    private boolean isUsingMobileDate(){
        ConnectivityManager conMang = (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMang.getActiveNetworkInfo();

        if (netInfo == null){
            return false;
        }

        return netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @Override
    public void run(){
        Date date = new Date();

        Data.addSensorData(new SensorData(device.getAddress(), date, data[1]));

        JSONObject json = new JSONObject();
        WifiManager wifi = (WifiManager)service.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()){
            Log.i("ssa", "Wifi is not enabled, trying mobile data");

            if (!isUsingMobileDate()) {
                Log.i("ssa", "Does not have mobile data connection, saveing data for later");
                return;
            }
        }

        double[] postData = Data.getData(device.getAddress());

        try {
            json.put("device_id", device.getAddress());
            json.put("date", date.getTime());
            json.put("value_avg", postData[1]);
            json.put("value_min", postData[0]);
            json.put("value_max", postData[2]);
        } catch (JSONException e) {
            Log.i("ssa", "Failed to create json object for POST");
            e.printStackTrace();
        }

        Log.i("ssa", "Posting this json");
        post(json.toString());

        publish(device.getAddress(), date, postData);
    }

    private void publish(String deviceId, Date date, double[] postData){
        //Generate a JSON object of the event to be published
        JsonObject event = new JsonObject();
        event.addProperty("device_id", deviceId);
        event.addProperty("date",  date.getTime());
        event.addProperty("value_avg",  postData[1]);
        event.addProperty("value_min", postData[0]);
        event.addProperty("value_max", postData[2]);

        //Registered flow allows 0, 1 and 2 QoS
        Data.getClient(deviceId).publishEvent("data", event);
    }

    private void post(String json){
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(Util.API_ADDRESS);
        try {
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = client.execute(post);

            Log.i("ssa", "Response to string");
            Log.i("ssa", String.valueOf(response.getStatusLine().getStatusCode()));
        }catch(UnsupportedEncodingException e){
            Log.i("ssa", "Failed to set encoding when sending post request to /data");
        } catch (ClientProtocolException e) {
            Log.i("ssa", "Client protocol exception thrown, failed to send post");
        } catch (IOException e) {
            Log.i("ssa", "IOException thrown, failed to send post request");
        }
    }
}
