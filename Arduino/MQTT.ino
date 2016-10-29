/********** MQTT functions **********/

const char* createMqtt_topic(String mqtt_topic_syntax) {
  mqtt_topic_syntax += application;
  return strToChar(mqtt_topic_syntax);
}

void connect_MQTT() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection... ");
    // Attempting to connect
    if (client.connect(client_mac, mqtt_username, mqtt_password, mqtt_topic_log, 1, 1, strToChar("{\"name\": \"" + application + "\", \"status\": \"disconnected\"}"))) {
      Serial.println("\t/!\\ connected /!\\");
      // Sending log "connected" to broker
      //client.publish(mqtt_topic_log, strToChar("{\"name\": \"" + application + "\", \"status\": \"connected\"}"), retained);
      send_MQTT_log(ESP.getVcc());
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void send_MQTT(float t, float h, float heatIndex) {
  mqtt_payload = strToChar("{\"temperature\": " + String(t) + ", \"humidity\": " + String(h) + ", \"heatIndex\": " + String(heatIndex) + "}");
  client.publish(mqtt_topic, mqtt_payload , retained);
}

void send_MQTT_log(float vcc) {
  mqtt_payload = strToChar("{\"name\": \"" + application + "\", \"status\": \"connected\", \"battery\": " + String(vcc) + "}");
  client.publish(mqtt_topic_log, mqtt_payload , retained);
}

