package com.example.motorcontroller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MotorController"
    }

    // UI Components
    private lateinit var ipAddressEditText: TextView
    private lateinit var statusTextView: TextView
    private lateinit var speedValueTextView: TextView
    private lateinit var speedSeekBar: SeekBar
    private lateinit var onButton: Button
    private lateinit var offButton: Button
    private lateinit var forwardButton: Button
    private lateinit var reverseButton: Button
    private lateinit var stopButton: Button

    // Motor state
    private var isMotorOn = false
    private var currentSpeed = 0
    private var currentDirection = "STOP"
    private var esp32IpAddress = "192.168.1.100"

    // Coroutine scope for network operations
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()

        // Get IP address from input field
        esp32IpAddress = ipAddressEditText.text.toString()
    }

    private fun initViews() {
        ipAddressEditText = findViewById(R.id.ipAddressEditText)
        statusTextView = findViewById(R.id.statusTextView)
        speedValueTextView = findViewById(R.id.speedValueTextView)
        speedSeekBar = findViewById(R.id.speedSeekBar)
        onButton = findViewById(R.id.onButton)
        offButton = findViewById(R.id.offButton)
        forwardButton = findViewById(R.id.forwardButton)
        reverseButton = findViewById(R.id.reverseButton)
        stopButton = findViewById(R.id.stopButton)

        // Set initial speed value
        speedValueTextView.text = "Speed: $currentSpeed"
        speedSeekBar.progress = currentSpeed
    }

    private fun setupClickListeners() {
        // On Button
        onButton.setOnClickListener {
            isMotorOn = true
            updateStatus()
            sendCommand("on")
        }

        // Off Button
        offButton.setOnClickListener {
            isMotorOn = false
            updateStatus()
            sendCommand("off")
        }

        // Forward Button
        forwardButton.setOnClickListener {
            currentDirection = "FORWARD"
            updateStatus()
            sendCommand("dir/forward")
        }

        // Reverse Button
        reverseButton.setOnClickListener {
            currentDirection = "REVERSE"
            updateStatus()
            sendCommand("dir/reverse")
        }

        // Stop Button (Emergency Stop)
        stopButton.setOnClickListener {
            isMotorOn = false
            currentSpeed = 0
            currentDirection = "STOP"
            speedSeekBar.progress = 0
            updateStatus()
            sendCommand("stop")
        }

        // Speed SeekBar
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSpeed = progress
                speedValueTextView.text = "Speed: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                sendCommand("speed/$currentSpeed")
            }
        })
    }

    private fun updateStatus() {
        val status = "Motor: ${if (isMotorOn) "ON" else "OFF"} | Speed: $currentSpeed | Direction: $currentDirection"
        statusTextView.text = status
    }

    private fun sendCommand(command: String) {
        esp32IpAddress = ipAddressEditText.text.toString()
        if (esp32IpAddress.isEmpty()) {
            Toast.makeText(this, "Please enter ESP32 IP address", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://$esp32IpAddress/$command"
        coroutineScope.launch {
            makeNetworkRequest(url)
        }
    }

    private suspend fun makeNetworkRequest(urlString: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Response: $response")

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        Toast.makeText(this@MainActivity, "Command sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error: Response code $responseCode", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error making request: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}