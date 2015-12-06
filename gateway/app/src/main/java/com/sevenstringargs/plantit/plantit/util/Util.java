package com.sevenstringargs.plantit.plantit.util;

/**
 * Created by Robban on 04/12/15.
 */
public class Util {
    public static final String API_ADDRESS = "http://openhack-plantit.mybluemix.net/sensor";
    private static boolean first = true;

    public static String[] decodeData(String data){
        data = String.copyValueOf(data.toCharArray(), 1, data.length()-2);
        return data.split("@");
    }

    public static char getConfig(){
        if (first) {
            first = false;
            return 'A';
        } else {
            first = true;
            return 'F';
        }
    }
}
