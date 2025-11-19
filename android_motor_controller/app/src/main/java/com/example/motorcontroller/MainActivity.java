package com.example.motorcontroller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MotorController";
    
    // UI Components
    private TextView ipAddressEditText;
    private TextView statusTextView;
    private TextView speedValueTextView;
    private SeekBar speedSeekBar;
    private Button onButton, offButton, forwardButton, reverseButton, stopButton;
    
    // Motor state
    private boolean isMotorOn = false;
    private int currentSpeed = 0;
    private String currentDirection = "STOP";
    private String esp32IpAddress = "192.168.1.100";
    
    // Handler for UI updates
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupClickListeners();
        
        // Get IP address from input field
        esp32IpAddress = ((TextView) findViewById(R.id.ipAddressEditText)).getText().toString();
    }
    
    private void initViews() {
        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        statusTextView = findViewById(R.id.statusTextView);
        speedValueTextView = findViewById(R.id.speedValueTextView);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        onButton = findViewById(R.id.onButton);
        offButton = findViewById(R.id.offButton);
        forwardButton = findViewById(R.id.forwardButton);
        reverseButton = findViewById(R.id.reverseButton);
        stopButton = findViewById(R.id.stopButton);
        
        // Set initial speed value
        speedValueTextView.setText("Speed: " + currentSpeed);
        speedSeekBar.setProgress(currentSpeed);
    }
    
    private void setupClickListeners() {
        // On Button
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMotorOn = true;
                updateStatus();
                sendCommand("on");
            }
        });
        
        // Off Button
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMotorOn = false;
                updateStatus();
                sendCommand("off");
            }
        });
        
        // Forward Button
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDirection = "FORWARD";
                updateStatus();
                sendCommand("dir/forward");
            }
        });
        
        // Reverse Button
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDirection = "REVERSE";
                updateStatus();
                sendCommand("dir/reverse");
            }
        });
        
        // Stop Button (Emergency Stop)
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMotorOn = false;
                currentSpeed = 0;
                currentDirection = "STOP";
                speedSeekBar.setProgress(0);
                updateStatus();
                sendCommand("stop");
            }
        });
        
        // Speed SeekBar
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSpeed = progress;
                speedValueTextView.setText("Speed: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendCommand("speed/" + currentSpeed);
            }
        });
    }
    
    private void updateStatus() {
        String status = "Motor: " + (isMotorOn ? "ON" : "OFF") + 
                       " | Speed: " + currentSpeed + 
                       " | Direction: " + currentDirection;
        statusTextView.setText(status);
    }
    
    private void sendCommand(String command) {
        esp32IpAddress = ipAddressEditText.getText().toString();
        if (esp32IpAddress.isEmpty()) {
            Toast.makeText(this, "Please enter ESP32 IP address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String url = "http://" + esp32IpAddress + "/" + command;
        new NetworkTask().execute(url);
    }
    
    private class NetworkTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);
                
                Scanner scanner = new Scanner(connection.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();
                
                return response;
            } catch (IOException e) {
                Log.e(TAG, "Error making request: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Response: " + result);
            if (result != null && !result.contains("Error")) {
                Toast.makeText(MainActivity.this, "Command sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }
}