package srimobile.aspen.leidos.com.sri.web;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.gps.GPSTracker;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by blackch on 6/1/2015.
 */
public class WebServicePoster {

    public static void postToWebService(Context context, Location location, String s) {

    }

    public static void postToWebService(Context context, double lat, double lon, int siteId) {

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String URL = "http://192.168.43.8:8080/DashCon/resources/mobile";

            HttpPost httpPost = new HttpPost(URL);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            SharedPreferences prefs = context.getSharedPreferences("SRI", Context.MODE_PRIVATE);

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("timestamp", new Timestamp(Calendar.getInstance().getTime().getTime()));
            jsonObject.accumulate("commercialDriversLicense", prefs.getString("cdl", "Commercial Driver's License"));
            jsonObject.accumulate("driversLicense", prefs.getString("dl", "Driver's License"));
            jsonObject.accumulate("vin", prefs.getString("vin", "Vehicle Identification Number"));
            jsonObject.accumulate("usdotNumber", prefs.getString("usdot", "USDOT Number"));
            jsonObject.accumulate("licensePlate", prefs.getString("lp", "License Plate"));
            jsonObject.accumulate("latitude", lat);
            jsonObject.accumulate("longitude", lon);
            jsonObject.accumulate("siteId", siteId);

            ByteArrayEntity baEntity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF8"));
            baEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(baEntity);

            System.out.println("JSON -> " + jsonObject.toString());

            HttpResponse httpResponse = httpClient.execute(httpPost);

            System.out.println("TruckActivity " + httpResponse.getEntity().getContent().available());
            System.out.println("TruckActivity " + httpResponse.getEntity().getContentLength());
            System.out.println("TruckActivity " + httpResponse.getStatusLine());

            String responseStr = String.valueOf(httpResponse.getEntity().getContentLength());

            HttpEntity entity = httpResponse.getEntity();

            String retSrc;
            if (entity != null) {
                entity.getContentLength();

                Header[] headers = httpResponse.getAllHeaders();
                for (Header header : headers) {
                    System.out.println(
                        "headers -> Key : " + header.getName() +
                        " ,Value : " + header.getValue());
                }
            }
            System.out.println("ResponseStr -> " + responseStr);

        } catch (Exception e) {
            System.out.println("Exception -> " + e.toString());
        }
    }
}
