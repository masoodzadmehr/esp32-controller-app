/*
 * ESP32 DC Motor Controller with L298N Driver
 * 
 * This project controls a DC motor using an ESP32 and L298N motor driver
 * Features:
 * - On/Off control
 * - Speed control (PWM)
 * - Direction control
 */

// Pin definitions for L298N connections
#define ENA_PIN 18    // Enable A - PWM for speed control
#define IN1_PIN 19    // Input 1 - Direction control
#define IN2_PIN 23    // Input 2 - Direction control
#define ENB_PIN 17    // Enable B (if using dual motor mode)
#define IN3_PIN 16    // Input 3 (if using dual motor mode)
#define IN4_PIN 4     // Input 4 (if using dual motor mode)

// Motor state variables
bool motorRunning = false;
int motorSpeed = 0;        // 0-255 (for analogWrite)
int motorDirection = 1;    // 1 for forward, 0 for backward

// Web server for remote control (optional)
#include <WiFi.h>
#include <WebServer.h>

const char* ssid = "your_wifi_ssid";
const char* password = "your_wifi_password";

WebServer server(80);

void setup() {
  Serial.begin(115200);
  
  // Initialize motor control pins
  pinMode(ENA_PIN, OUTPUT);
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(ENB_PIN, OUTPUT);
  pinMode(IN3_PIN, OUTPUT);
  pinMode(IN4_PIN, OUTPUT);
  
  // Initialize motor in stopped state
  stopMotor();
  
  // Connect to WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  
  Serial.println("Connected to WiFi");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  
  // Define web server routes
  server.on("/", HTTP_GET, handleRoot);
  server.on("/control", HTTP_GET, handleControl);
  server.on("/status", HTTP_GET, handleStatus);
  
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();
  delay(1);
}

// Motor control functions
void startMotor() {
  digitalWrite(IN1_PIN, HIGH);
  digitalWrite(IN2_PIN, LOW);
  analogWrite(ENA_PIN, motorSpeed);
  motorRunning = true;
  Serial.println("Motor started");
}

void stopMotor() {
  digitalWrite(IN1_PIN, LOW);
  digitalWrite(IN2_PIN, LOW);
  analogWrite(ENA_PIN, 0);
  motorRunning = false;
  Serial.println("Motor stopped");
}

void setMotorSpeed(int speed) {
  if (speed < 0) speed = 0;
  if (speed > 255) speed = 255;
  
  motorSpeed = speed;
  if (motorRunning) {
    analogWrite(ENA_PIN, motorSpeed);
  }
  Serial.print("Motor speed set to: ");
  Serial.println(motorSpeed);
}

void setMotorDirection(int direction) {
  motorDirection = direction;
  if (motorRunning) {
    if (direction == 1) {  // Forward
      digitalWrite(IN1_PIN, HIGH);
      digitalWrite(IN2_PIN, LOW);
    } else {  // Backward
      digitalWrite(IN1_PIN, LOW);
      digitalWrite(IN2_PIN, HIGH);
    }
  }
  Serial.print("Motor direction set to: ");
  Serial.println(direction == 1 ? "Forward" : "Backward");
}

void handleRoot() {
  String html = "<!DOCTYPE html><html>";
  html += "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
  html += "<style>html { font-family: Arial; display: inline-block; margin: 0px auto; text-align: center;}";
  html += ".button { background-color: #4CAF50; border: none; color: white; padding: 16px 40px;";
  html += "text-decoration: none; font-size: 30px; margin: 2px; cursor: pointer;}";
  html += ".button2 {background-color: #555555;}</style></head>";
  html += "<body><h1>ESP32 DC Motor Controller</h1>";
  html += "<p><a href=\"/control?cmd=start\"><button class=\"button\">START</button></a></p>";
  html += "<p><a href=\"/control?cmd=stop\"><button class=\"button button2\">STOP</button></a></p>";
  html += "<p>Speed Control: <input type=\"range\" min=\"0\" max=\"255\" value=\"128\" id=\"speedSlider\" onchange=\"setSpeed(this.value)\">";
  html += "<span id=\"speedValue\">128</span></p>";
  html += "<p><button class=\"button\" onclick=\"setDirection(1)\">Forward</button>";
  html += "<button class=\"button button2\" onclick=\"setDirection(0)\">Reverse</button></p>";
  html += "<p>Current Status: <span id=\"status\"></span></p>";
  html += "<script>";
  html += "function setSpeed(value) {";
  html += "  document.getElementById('speedValue').innerHTML = value;";
  html += "  fetch('/control?cmd=speed&value=' + value);";
  html += "}";
  html += "function setDirection(dir) {";
  html += "  fetch('/control?cmd=dir&value=' + dir);";
  html += "}";
  html += "setInterval(function() {";
  html += "  fetch('/status').then(response => response.text()).then(data => {";
  html += "    document.getElementById('status').innerHTML = data;";
  html += "  });";
  html += "}, 1000);";
  html += "</script>";
  html += "</body></html>";
  
  server.send(200, "text/html", html);
}

void handleControl() {
  String cmd = server.arg("cmd");
  String value = server.arg("value");
  
  if (cmd == "start") {
    startMotor();
    server.send(200, "text/plain", "Motor started");
  } else if (cmd == "stop") {
    stopMotor();
    server.send(200, "text/plain", "Motor stopped");
  } else if (cmd == "speed" && value != "") {
    int speed = value.toInt();
    setMotorSpeed(speed);
    server.send(200, "text/plain", "Speed set to " + String(speed));
  } else if (cmd == "dir" && value != "") {
    int direction = value.toInt();
    setMotorDirection(direction);
    server.send(200, "text/plain", "Direction set to " + String(direction));
  } else {
    server.send(400, "text/plain", "Invalid command");
  }
}

void handleStatus() {
  String status = "Running: " + String(motorRunning ? "ON" : "OFF");
  status += ", Speed: " + String(motorSpeed);
  status += ", Direction: " + String(motorDirection == 1 ? "Forward" : "Backward");
  server.send(200, "text/plain", status);
}