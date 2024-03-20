package com.example.iot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {
    // Your Ubidots API Token
    private static final String API_TOKEN = "BBUS-d9d04241e72985c6d9c698c3a767d8d85ed";

    // Your Ubidots Device ID
    private static final String DEVICE_ID = "65fb16f6fdbc3a0b339a52fb";

    // Declare LineChart variable

    private final Queue<Double> ecgQueue = new LinkedList<>(); // Queue to hold ECG data points
    private int currentIndex = 0; // Current index for x-axis
    private static final int SAMPLE_RATE_MS = 100;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateChartRunnable;
    private LineChart lineChart; // Declare LineChart variable
    Button buttonAlertDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        TextView textViewNoData = findViewById(R.id.textViewNoData);

        lineChart = findViewById(R.id.lineChart);

        // Check if data is available
        if (dataIsAvailable()) {
            // Data is available, show LineChart and hide TextView
            textViewNoData.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);
            // Call method to set up LineChart with data from Ubidots
            setupLineChart();
        } else {
            // Data is not available, hide LineChart and show TextView
            textViewNoData.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);
        }

        // Show the username

        TextView textViewWelcome = findViewById(R.id.textViewWelcome);

        // Retrieve username from intent
        String username = getIntent().getStringExtra("username");
        textViewWelcome.setText(username);
        buttonAlertDoctor = findViewById(R.id.buttonAlertDoctor);

        buttonAlertDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request storage permission
                Bitmap screenshot = takeScreenshot(lineChart);
                sendImageViaWhatsApp(screenshot);
                openWhatsApp(screenshot);// If calling from an activity



            }
        });
    }


    private boolean dataIsAvailable() {
        // Add your logic here to check if data is available from Ubidots
        // For example, you can check if you have fetched data successfully
        return true; // Return true if data is available, false otherwise
    }

    private void setupLineChart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Double> ecgData = ECGDataGenerator.generateECGData(120, 1000); // Generate data for 60 seconds at 1000 samples per second

                // Prepare data entries for the LineChart
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < ecgData.size(); i++) {
                    entries.add(new Entry(i, ecgData.get(i).floatValue()));
                }

                // Create a dataset with the generated data
                LineDataSet dataSet = new LineDataSet(entries, "ECG Data");

                // Set properties for the dataset (color, line thickness, etc.)
                dataSet.setColor(Color.BLUE);
                dataSet.setLineWidth(2f);
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);

                // Create a LineData object and set the dataset
                LineData lineData = new LineData(dataSet);

                // Set the LineData to the LineChart
                lineChart.setData(lineData);

                // Refresh the chart to display the data
                lineChart.invalidate();
                startUpdatingChart();
            }
        }, 4000);

    }


    private void startUpdatingChart() {
        updateChartRunnable = new Runnable() {
            @Override
            public void run() {
                // Generate new ECG data point
                double ecgValue = generateNewECGData();

                // Remove the oldest data point if the queue size exceeds a certain limit
                if (ecgQueue.size() > 50) {
                    ecgQueue.poll();
                }

                // Add the new data point to the queue
                ecgQueue.offer(ecgValue);

                // Update the chart with the new data
                updateChart();

                // Schedule the next update
                handler.postDelayed(this, SAMPLE_RATE_MS);
            }
        };

        // Start the chart update loop
        handler.post(updateChartRunnable);
    }

    private double generateNewECGData() {

        return Math.random(); // Replace this with your actual data generation logic
    }

    private void updateChart() {
        // Prepare data entries for the LineChart
        List<Entry> entries = new ArrayList<>();
        for (double ecgValue : ecgQueue) {
            entries.add(new Entry(currentIndex++, (float) ecgValue));
        }

        // Create a dataset with the generated data
        LineDataSet dataSet = new LineDataSet(entries, "ECG Data");

        // Set properties for the dataset (color, line thickness, etc.)
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        // Create a LineData object and set the dataset
        LineData lineData = new LineData(dataSet);

        // Set the LineData to the LineChart
        lineChart.setData(lineData);

        // Refresh the chart to display the data
        lineChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop updating the chart when the activity is destroyed
        handler.removeCallbacks(updateChartRunnable);
    }



    private Bitmap takeScreenshot(View view) {
        Bitmap screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(screenshot));
        return screenshot;
    }

    public void onResponse(String response) {
        // Parse the JSON response and update the line chart
        List<Entry> entries = parseResponse(response);

        // Create a dataset with the extracted data points
        LineDataSet dataSet = new LineDataSet(entries, "ECG Data");

        // Set properties for the dataset (color, line thickness, etc.)
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        // Create a LineData object and set the dataset
        LineData lineData = new LineData(dataSet);

        // Set the LineData to the LineChart
        lineChart.setData(lineData);

        // Refresh the chart to display the data
        lineChart.invalidate();
    }

    private List<Entry> parseResponse(String jsonResponse) {
        List<Entry> entries = new ArrayList<>();
        try {
            // Parse JSON response and extract data points
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double value = jsonObject.getDouble("value");
                // Assuming the x-axis value is the index in the JSONArray
                entries.add(new Entry(i, (float) value));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private void saveScreenshot(Bitmap bitmap) {
        // Specify the directory where you want to save the screenshot
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
        if (!directory.exists()) {
            // Create the directory if it doesn't exist
            directory.mkdirs();
        }

        // Save the screenshot to the specified directory
        File file = new File(directory, "ECGScreenshot.png");
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // Corrected line
            outputStream.flush();
            outputStream.close();

            // Show a toast message indicating successful save
            Toast.makeText(this, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show a toast message indicating failure to save
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendImageViaWhatsApp(Bitmap bitmap) {
        // Save the screenshot to the cache directory
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs(); // Make sure the directory exists
            FileOutputStream stream = new FileOutputStream(cachePath + "/ECGScreenshot.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Show a toast message indicating failure to save
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the saved file's URI
        File imagePath = new File(getCacheDir(), "images");
        File newFile = new File(imagePath, "ECGScreenshot.png");
        Uri contentUri = FileProvider.getUriForFile(this, "com.example.iot.fileprovider", newFile);

        // Create an intent to open WhatsApp with the image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT, "ECG screenshot from MyApp"); // Add a text message
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read URI permission
        intent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(intent, "Share image via"));
    }


    private void openWhatsApp(Bitmap bitmap) {
        // Save the screenshot to the cache directory
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs(); // Make sure the directory exists
            FileOutputStream stream = new FileOutputStream(cachePath + "/ECGScreenshot.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Show a toast message indicating failure to save
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the saved file's URI
        File imagePath = new File(getCacheDir(), "images");
        File newFile = new File(imagePath, "ECGScreenshot.png");
        Uri contentUri = FileProvider.getUriForFile(this, "com.example.iot.fileprovider", newFile);

        // Create an intent to open WhatsApp with the image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT, "!! Alert !!"); // Add a text message
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read URI permission
        intent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(intent, "Share image via"));
    }



}
