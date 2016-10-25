/*
   ESP-8266 that connects to a WiFi network and chats with an MQTT server (publish & subscribe)
*/
#include <ArduinoJson.h>          // JSON library
#include <ESP8266WiFi.h>          // ESP8266 library
#include <PubSubClient.h>         // MQTT library
#include <DNSServer.h>            // Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     // Local WebServer used to serve the configuration portal
#include <WiFiManager.h>          // https://github.com/tzapu/WiFiManager WiFi Configuration Magic
#include <DHT.h>                  // DHT 11 library

//#define SLEEP_DELAY_IN_SECONDS   30
#define DHTTYPE                    DHT11

// Variables
float t, h, heatIndex;                 // Values read from sensor
const String application = "weather";  // Name of the application, used in the log MQTT payloads and topics
const int data_pin = 5;                // GPIO pin for the DHT data
unsigned long previousMillis = 0;      // Will store last temp was read
const long interval = 2000;            // Interval at which to read sensor

// WiFi setup
const String ssid = "ESP_" + application;
const char* password = "";
const char* client_mac;

// MQTT setup
const char* mqtt_server = "<BROKER_IP>";
const int mqtt_port = <BROKER_PORT>;
const char* mqtt_username = "esp";                  // Used while connecting to the broker
const char* mqtt_password = "";                     // Leave empty if not needed
const String mqtt_topic_syntax = "home/";           // The first part of the MQTT topic, the second part is generated with the application name
const char* mqtt_topic = "";
const char* mqtt_topic_log = "home/log";            // Used to log the hardware status to broker
char* mqtt_payload;
boolean retained = true;

WiFiClient espClient;
PubSubClient client(espClient);
DHT dht(data_pin, DHTTYPE);

void setup() {
  // Setup serial port
  Serial.begin(115200);

  // Setup WiFi
  WiFiManager wifiManager;
  // Reset saved settings
  //wifiManager.resetSettings();
  wifiManager.autoConnect(strToChar(ssid), password);

  client.setServer(mqtt_server, mqtt_port);

  Serial.println("Application name: " + application);
  Serial.println("MQTT Server: " + String(mqtt_server));

  client_mac = client_MAC();
  Serial.println("ESP mac: " + String(client_mac));

  // Create mqtt_topic with application name
  mqtt_topic = createMqtt_topic(mqtt_topic_syntax);
  Serial.println("MQTT Topic: " + String(mqtt_topic));

  // Send MQTT connection status to broker
  connect_MQTT();

  // Setup DHT
  dht.begin();

  digitalWrite(LED_BUILTIN, HIGH);   // Turn the LED off (Note that HIGH is the voltage level
}

void loop() {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    // save the last time you read the sensor
    previousMillis = currentMillis;
    
    t = dht.readTemperature();
    h = dht.readHumidity();
    if (isnan(t) || isnan(h)) {
      Serial.println("Failed to read from DHT sensor!");
      return;
    }
    // Compute heat index in Celsius (isFahreheit = false)
    heatIndex = dht.computeHeatIndex(t, h, false);

    Serial.print("Temperature: ");
    Serial.print(t);
    Serial.print(" *C\t");
    Serial.print("Humidity: ");
    Serial.print(h);
    Serial.print(" %");
    Serial.println("");
    Serial.print("Heat index: ");
    Serial.print(heatIndex);
    Serial.println(" *C");
    if (!client.connected()) {
      connect_MQTT();
    }
    send_MQTT(t, h, heatIndex);
    client.loop();
  }
}

