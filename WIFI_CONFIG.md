# WiFi Configuration Guide

This document explains how to configure the WiFi credentials for the ESP32 DC Motor Controller.

## Setting up WiFi Credentials

Before uploading the code to your ESP32, you need to configure the WiFi credentials in the main code file:

1. Open `esp32_dc_motor_controller.ino` in your Arduino IDE
2. Find these lines in the code:
   ```cpp
   const char* ssid = "your_wifi_ssid";
   const char* password = "your_wifi_password";
   ```
3. Replace `"your_wifi_ssid"` with your actual WiFi network name
4. Replace `"your_wifi_password"` with your actual WiFi password
5. Upload the code to your ESP32

## Example

```cpp
// Example with actual credentials
const char* ssid = "MyHomeNetwork";
const char* password = "mySecurePassword123";
```

## Troubleshooting WiFi Connection

- Make sure your ESP32 is close enough to the WiFi router
- Verify that the SSID and password are entered correctly (case-sensitive)
- Check that your WiFi network is 2.4GHz compatible (ESP32 supports 2.4GHz WiFi)
- If connection fails, check the serial monitor for error messages
- Ensure your router is not blocking the ESP32 device

## Security Note

For production use, consider implementing additional security measures such as:
- Using HTTPS instead of HTTP
- Adding authentication to the web interface
- Using WPA2 or WPA3 encryption for your WiFi network