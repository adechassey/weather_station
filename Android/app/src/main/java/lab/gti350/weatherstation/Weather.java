package lab.gti350.weatherstation;

/**
 * Created by Antoine on 26/10/2016.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Weather extends Activity {

    private static final String TAG = "Weather";

    private TextView statusText;
    private String status;
    private TextView temperatureText;
    private String temperature;
    private TextView humidityText;
    private String humidity;
    private TextView heatIndexText;
    private String heatIndex;
    private Vibrator vibrator;
    private Ringtone ringtone;

    private String mqtt_payload;
    private String mqtt_topic;

    private IntentFilter mIntent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.fragment_main);
        Log.d(TAG, "The onCreate() event");

        temperatureText = (TextView) findViewById(R.id.temperature);
        humidityText = (TextView) findViewById(R.id.humidity);
        heatIndexText = (TextView) findViewById(R.id.heatIndex);
        statusText = (TextView) findViewById(R.id.status);

        startService(new Intent(this, MQTTService.class));

        //registerReceiver(statusReceiver,mIntent);
        LocalBroadcastManager.getInstance(Weather.this).registerReceiver(mqttReceiver, new IntentFilter("MQTT"));
    }

    private BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                mqtt_payload = null;
                mqtt_topic = null;
            } else {
                mqtt_topic = extras.getString("mqtt_topic");
                mqtt_payload = extras.getString("mqtt_payload");
                JSONObject json = null;
                try {
                    json = new JSONObject(mqtt_payload);
                    if (mqtt_topic.equals("home/weather")) {
                        temperature = json.get("temperature").toString();
                        humidity = json.get("humidity").toString();
                        heatIndex = json.get("heatIndex").toString();

                        temperatureText.setText(temperature + " °C");
                        humidityText.setText(humidity + " %");
                        heatIndexText.setText(heatIndex + " °C");

                        if (Double.parseDouble(temperature) <= 10 && Double.parseDouble(temperature) >= 5) {
                            temperatureText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorCold));
                            heatIndexText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorCold));
                        }
                        if (Double.parseDouble(heatIndex) < 5) {
                            heatIndexText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorVeryCold));
                        }
                        if (Double.parseDouble(temperature) >= 18) {
                            temperatureText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorCold));
                            heatIndexText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorCold));
                        }
                        if (Double.parseDouble(humidity) <= 50)
                            humidityText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorWarm));
                    }
                    if (mqtt_topic.equals("home/log")) {
                        if (json.get("status").toString().equals("connected")) {
                            status = json.get("status").toString();
                            statusText.setText(status);
                            statusText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorSuccess));
                        }
                        if (json.get("status").toString().equals("disconnected")) {
                            status = json.get("status").toString();
                            statusText.setText(status);
                            statusText.setTextColor(ContextCompat.getColor(Weather.this, R.color.colorDanger));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void notification() {
        // Notification
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        vibrator.vibrate(500);
        ringtone.play();
    }

    /**
     * Called when the activity is about to become visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "The onStart() event");
    }

    /**
     * Called when the activity has become visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "The onResume() event");
    }

    /**
     * Called when another activity is taking focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "The onPause() event");
    }

    /**
     * Called when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "The onStop() event");
    }

    /**
     * Called just before the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        if (mIntent != null) {
            unregisterReceiver(mqttReceiver);
            mIntent = null;
        }
        super.onDestroy();
        Log.d(TAG, "The onDestroy() event");
    }
}
