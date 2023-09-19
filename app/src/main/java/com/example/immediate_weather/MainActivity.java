package com.example.immediate_weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 123;

    private TextView currentWeatherTextView;
    private TextView upcomingWeatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentWeatherTextView = findViewById(R.id.current_weather);
        upcomingWeatherTextView = findViewById(R.id.upcoming_weather);

        // Check and request fine and coarse location permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permissions are already granted; you can proceed with location-related tasks.
            fetchWeatherBasedOnLocation();
        }
    }

    private void fetchWeatherBasedOnLocation() {
        // Use location services to fetch user's location and then fetch weather data
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Handle the retrieved location here
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Now you can use these coordinates to fetch weather data.

                            // Replace the following line with your code to fetch weather data
                            fetchWeatherData(latitude, longitude);
                        }
                    }
                });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = "ff546393f7f449c58c444433232508"; // Replace with your WeatherAPI.com API key
        String apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=" + apiKey + "&q=" + latitude + "," + longitude + "&days=2";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the current weather data from the JSON response and update the UI
                            JSONObject current = response.getJSONObject("current");
                            String weatherDescription = current.getJSONObject("condition")
                                    .getString("text");
                            double temperatureFahrenheit = current.getDouble("temp_f"); // Use temp_f for Fahrenheit

                            currentWeatherTextView.setText("Current Weather: " + weatherDescription);
                            currentWeatherTextView.append("\nTemperature: " + temperatureFahrenheit + "°F");

                            // Parse the forecast data from the JSON response and update the upcoming weather UI
                            JSONObject forecast = response.getJSONObject("forecast");
                            JSONArray forecastday = forecast.getJSONArray("forecastday");

                            // Check if there are forecast days available
                            if (forecastday.length() > 0) {
                                StringBuilder upcomingWeather = new StringBuilder("Upcoming Weather:\n");

                                for (int i = 0; i < forecastday.length(); i++) {
                                    JSONObject forecastDayData = forecastday.getJSONObject(i);
                                    String date = forecastDayData.getString("date");
                                    String condition = forecastDayData.getJSONObject("day").getString("condition");
                                    double maxTempFahrenheit = forecastDayData.getJSONObject("day").getDouble("maxtemp_f");
                                    double minTempFahrenheit = forecastDayData.getJSONObject("day").getDouble("mintemp_f");

                                    // Append the forecast data to the upcomingWeather StringBuilder
                                    upcomingWeather.append("\nDate: ").append(date);
                                    upcomingWeather.append("\nCondition: ").append(condition);
                                    upcomingWeather.append("\nMax Temp: ").append(maxTempFahrenheit).append("°F");
                                    upcomingWeather.append("\nMin Temp: ").append(minTempFahrenheit).append("°F");
                                    upcomingWeather.append("\n");
                                }

                                // Set the upcoming weather text in the TextView
                                upcomingWeatherTextView.setText(upcomingWeather.toString());
                            } else {
                                upcomingWeatherTextView.setText("No upcoming weather data available.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handleError("Error parsing weather data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError("Error fetching weather data: " + error.getMessage());
                    }
                });

        queue.add(request);
    }

    // Handle permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted; proceed with location-based tasks.
                fetchWeatherBasedOnLocation();
            } else {
                // Permissions denied; inform the user or handle as needed.
                Toast.makeText(this, "Location permissions are required for this app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to handle errors and display detailed error messages
    private void handleError(String errorMessage) {
        currentWeatherTextView.setText(errorMessage);
        upcomingWeatherTextView.setText("");
    }
}



