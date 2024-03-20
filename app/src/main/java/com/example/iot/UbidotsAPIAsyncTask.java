package com.example.iot;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UbidotsAPIAsyncTask extends AsyncTask<Void, Void, String> {

    // Your Ubidots API Token
    private static final String API_TOKEN = "BBUS-d9d04241e72985c6d9c698c3a767d8d85ed";

    // Your Ubidots Device ID
    private static final String DEVICE_ID = "65fb16f6fdbc3a0b339a52fb";

    // Listener to handle API response
    private UbidotsAPIListener listener;

    // Interface to handle API response
    public interface UbidotsAPIListener {
        void onResponse(String response);
    }

    // Constructor to initialize listener

    public UbidotsAPIAsyncTask(MainActivity listener) {
        this.listener = (UbidotsAPIListener) listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // Construct the URL for the Ubidots API endpoint
            String apiUrl = "https://industrial.api.ubidots.com/api/v1.6/devices/" + DEVICE_ID + "/variables";

            // Create URL object
            URL url = new URL(apiUrl);

            // Create HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("GET");

            // Set Ubidots API Token as Authorization header
            connection.setRequestProperty("X-Auth-Token", API_TOKEN);

            // Get response code
            int responseCode = connection.getResponseCode();

            // Check if the response code is 200 (OK)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Close BufferedReader
                in.close();

                // Return response
                return response.toString();
            } else {
                // Return error message
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UbidotsAPIListener getListener() {
        return listener;
    }

    public void setListener(UbidotsAPIListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // Pass the response to the listener
        if (result != null) {
            listener.onResponse(result);
        } else {
            Log.e("UbidotsAPI", "API call failed");
        }
    }

}
