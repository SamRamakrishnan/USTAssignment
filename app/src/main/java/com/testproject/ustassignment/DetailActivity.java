package com.testproject.ustassignment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private TextView deviceNameText, deviceIpText, deviceStatusText, publicIpText, geoInfoText, companyInfoText, carrierInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        deviceNameText = findViewById(R.id.device_name_detail);
        deviceIpText = findViewById(R.id.device_ip_detail);
        deviceStatusText = findViewById(R.id.device_status_detail);
        publicIpText = findViewById(R.id.public_ip);
        geoInfoText = findViewById(R.id.geo_info);
        companyInfoText = findViewById(R.id.company_info);
        carrierInfoText = findViewById(R.id.carrier_info);

        // Get device data from Intent
        String deviceName = getIntent().getStringExtra("device_name");
        if(deviceName==null){
            deviceName = "LG TV";
        }
        String deviceIp = getIntent().getStringExtra("device_ip");

        if(deviceIp==null){
            deviceIp = "116.68.67.316";
        }
        String deviceStatus = getIntent().getStringExtra("device_status");

        if(deviceStatus==null){
            deviceStatus = "Online";
        }

        deviceNameText.setText("Device Name: " + deviceName);
        deviceIpText.setText("Device IP: " + deviceIp);
        deviceStatusText.setText("Status: " + deviceStatus);

        // Fetch public IP and geo details
        new FetchPublicIpTask().execute();
    }

    private class FetchPublicIpTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return fetchDataFromUrl("https://api.ipify.org?format=json");
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String publicIp = json.getString("ip");
                    publicIpText.setText("Public IP: " + publicIp);
                    // Fetch geo details using the public IP
                    new FetchGeoDetailsTask().execute(publicIp);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    Toast.makeText(DetailActivity.this, "Failed to parse public IP", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DetailActivity.this, "Failed to fetch public IP", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchGeoDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ip = params[0];
            return fetchDataFromUrl("https://ipinfo.io/" + ip + "/geo");
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String city = json.optString("city", "N/A");
                    String region = json.optString("region", "N/A");
                    String country = json.optString("country", "N/A");
                    String company = json.optString("org", "N/A");
                    String carrier = json.optString("carrier", "N/A");

                    geoInfoText.setText("Geo Info: " + city + ", " + region + ", " + country);
                    companyInfoText.setText("Company: " + company);
                    carrierInfoText.setText("Carrier: " + carrier);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    Toast.makeText(DetailActivity.this, "Failed to parse geo details", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DetailActivity.this, "Failed to fetch geo details", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String fetchDataFromUrl(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } else {
                Log.e(TAG, "HTTP error: " + responseCode);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
        return null;
    }
}