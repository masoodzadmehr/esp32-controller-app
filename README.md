# ESP32 DC Motor Controller with L298N Driver

This project provides a complete solution for controlling a DC motor using an ESP32 microcontroller and an L298N motor driver. The system features on/off control, speed control via PWM, and direction control with a web-based interface for remote operation.

## Features

- On/Off control of DC motor
- Speed control using PWM (0-255 range)
- Direction control (forward/reverse)
- Web-based interface for remote control
- Real-time status monitoring

## Hardware Requirements

- ESP32 development board
- L298N motor driver module
- DC motor
- Power supply for the motor (compatible with your motor's voltage requirements)
- Jumper wires for connections

## Wiring Diagram

```
ESP32 Pin    ->    L298N Pin
GPIO 18      ->    ENA (Enable A - PWM)
GPIO 19      ->    IN1 (Input 1 - Direction)
GPIO 23      ->    IN2 (Input 2 - Direction)
VCC (3.3V)   ->    VCC (Logic power)
GND          ->    GND (Common ground)

Motor Power Supply:
L298N VCC    <-    External power supply positive (for motor)
L298N GND    <-    External power supply negative (common ground)

DC Motor:
L298N OUT1   ->    Motor terminal 1
L298N OUT2   ->    Motor terminal 2
```

## Software Setup

1. Install the Arduino IDE with ESP32 board support
2. Install the following libraries:
   - WiFi.h (included with ESP32 core)
   - WebServer.h (included with ESP32 core)
3. Open the `esp32_dc_motor_controller.ino` file in Arduino IDE
4. Modify the WiFi credentials in the code:
   ```cpp
   const char* ssid = "your_wifi_ssid";
   const char* password = "your_wifi_password";
   ```
5. Upload the code to your ESP32 board

## Pin Configuration

- **ENA_PIN (GPIO 18)**: PWM signal for speed control
- **IN1_PIN (GPIO 19)**: Direction control input 1
- **IN2_PIN (GPIO 23)**: Direction control input 2
- **ENB_PIN (GPIO 17)**: Enable B (for dual motor mode)
- **IN3_PIN (GPIO 16)**: Input 3 (for dual motor mode)
- **IN4_PIN (GPIO 4)**: Input 4 (for dual motor mode)

## Web Interface

Once the ESP32 is connected to WiFi, the controller creates a web server accessible through the IP address shown in the serial monitor. The interface includes:

- Start/Stop buttons
- Speed control slider (0-255)
- Direction control buttons (Forward/Reverse)
- Real-time status display

## API Endpoints

The controller also provides REST API endpoints for programmatic control:

- `GET /` - Main web interface
- `GET /control?cmd=start` - Start the motor
- `GET /control?cmd=stop` - Stop the motor
- `GET /control?cmd=speed&value=[0-255]` - Set motor speed
- `GET /control?cmd=dir&value=[0|1]` - Set motor direction (0=reverse, 1=forward)
- `GET /status` - Get current motor status

## Usage Examples

### Manual Control
1. Power on the ESP32 and L298N board
2. Connect to the same WiFi network as the ESP32
3. Open a web browser and navigate to the ESP32's IP address
4. Use the interface to control the motor

### Programmatic Control
Send HTTP GET requests to control the motor from other devices:
```
http://[ESP32_IP]/control?cmd=start
http://[ESP32_IP]/control?cmd=speed&value=200
http://[ESP32_IP]/control?cmd=dir&value=0
http://[ESP32_IP]/control?cmd=stop
```

## Safety Considerations

- Ensure proper power supply ratings for both the ESP32 and motor
- Make sure all grounds are properly connected
- Monitor motor temperature during operation
- Implement emergency stop procedures in your application
- Use appropriate fuses/circuit protection for the motor power supply

## Troubleshooting

- If motor doesn't respond, check all connections
- If WiFi connection fails, verify SSID and password
- If speed control doesn't work, ensure ENA pin is connected to a PWM-capable GPIO
- Check serial monitor for error messages

## Customization

The code can be easily modified to:
- Add multiple motors
- Implement speed feedback
- Add acceleration/deceleration profiles
- Include additional sensors
- Create scheduled operations 
