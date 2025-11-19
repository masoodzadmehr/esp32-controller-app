# ESP32 Motor Controller Android App

This Android application allows you to control an ESP32-based DC motor controller with L298N driver through WiFi.

## Features

- ON/OFF control of the motor
- Speed control (0-255 range)
- Direction control (Forward/Reverse)
- Emergency stop button
- Real-time status display
- Configurable ESP32 IP address

## Requirements

- Android device with minimum API level 21 (Android 5.0)
- ESP32 running the motor controller firmware
- Both devices on the same WiFi network

## Setup Instructions

1. Make sure your ESP32 is running the motor controller firmware and is connected to the same WiFi network as your Android device.

2. Find the IP address of your ESP32:
   - Check your router's admin panel
   - Or look at the serial monitor output from the ESP32

3. Install the Android app on your device

4. Open the app and enter the ESP32 IP address in the input field (default is 192.168.1.100)

## Usage

- **IP Address Field**: Enter the IP address of your ESP32
- **ON Button**: Turn the motor on
- **OFF Button**: Turn the motor off
- **Forward/Reverse Buttons**: Change motor direction
- **Speed Slider**: Adjust motor speed from 0 to 255
- **Emergency Stop**: Immediately stops the motor and sets speed to 0

## API Commands

The app sends the following HTTP GET requests to your ESP32:

- `/on` - Turn motor on
- `/off` - Turn motor off
- `/dir/forward` - Set direction to forward
- `/dir/reverse` - Set direction to reverse
- `/speed/{value}` - Set speed (0-255)
- `/stop` - Emergency stop (same as off but ensures speed is 0)

## Troubleshooting

- Make sure both your Android device and ESP32 are on the same WiFi network
- Verify the IP address is correct
- Check that the ESP32 is powered on and the motor controller firmware is running
- Ensure the ESP32 web server is accessible by trying to access it from a browser on your Android device