package com.example.assignment2shivamgupta;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    EditText edtCityName;
    TextView txtViewCityName, txtTempDisplay, MinMaxTemp, txtWeatherType, txtWeatherType2, txtHumidityPerc, txtCloudsPerc;
    Button btnWeather;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing elements
        edtCityName = (EditText) findViewById(R.id.edtCityName);
        txtViewCityName = (TextView) findViewById(R.id.txtViewCityName);
        txtTempDisplay = (TextView) findViewById(R.id.txtTempDisplay);
        MinMaxTemp = (TextView) findViewById(R.id.txtMinMaxTemp);
        txtWeatherType = (TextView) findViewById(R.id.txtWeatherType);
        txtWeatherType2 = (TextView) findViewById(R.id.txtWeatherType2);
        txtHumidityPerc = (TextView) findViewById(R.id.txtHumidityPerc);
        txtCloudsPerc = (TextView) findViewById(R.id.txtCloudsPerc);
        btnWeather = (Button) findViewById(R.id.btnWeather);

        HideEverything();
        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String CityName = edtCityName.getText().toString();
                String ApiDetails = null;

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        getWeather(CityName);
                    }
                };
                Thread thread = new Thread(null, runnable, "background");
                thread.start();

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    public void getWeather(final String CityName) {

        final String urlWithCity = GetApiCall(CityName);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlWithCity, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String weather = jsonObject.getString("weather");
                    JSONArray array = new JSONArray(weather);

                    //Setting Response to elements to show the weather
                    String weatherCity = jsonObject.getString("name");
                    txtViewCityName.setVisibility(View.VISIBLE);
                    txtViewCityName.setText(weatherCity);

                    String temp = jsonObject.getJSONObject("main").getString("temp");
                    txtTempDisplay.setVisibility(View.VISIBLE);
                    Double Temperature = Double.parseDouble(temp);
                    String TempDisplay = new DecimalFormat("##.##").format(Temperature - 273).toString();
                    txtTempDisplay.setText(TempDisplay + "\u2103");

                    String tempMin = jsonObject.getJSONObject("main").getString("temp_min");
                    String tempMax = jsonObject.getJSONObject("main").getString("temp_max");
                    MinMaxTemp.setVisibility(View.VISIBLE);
                    Double TemperatureMin = Double.parseDouble(tempMin);
                    String TempDisplayMin = new DecimalFormat("##.##").format(TemperatureMin - 273).toString();
                    Double TemperatureMax = Double.parseDouble(tempMax);
                    String TempDisplayMax = new DecimalFormat("##.##").format(TemperatureMax - 273).toString();
                    MinMaxTemp.setText("Min " + TempDisplayMin + "\u2103" + "  Max " + TempDisplayMax + "\u2103");

                    for (int i = 0; i < array.length() && array.length() >= 1; i++) {
                        JSONObject arrayObject = array.getJSONObject(i);
                        String main, description = "";
                        main = arrayObject.getString("main");
                        description = arrayObject.getString("description");
                        txtWeatherType.setVisibility(View.VISIBLE);
                        txtWeatherType2.setVisibility(View.VISIBLE);
                        txtWeatherType.setText(main);
                        txtWeatherType2.setText(description);
                    }

                    String humidity = jsonObject.getJSONObject("main").getString("humidity");
                    txtHumidityPerc.setVisibility(View.VISIBLE);
                    txtHumidityPerc.setText(humidity + "% Humidity");

                    String clouds = jsonObject.getJSONObject("clouds").getString("all");
                    txtCloudsPerc.setVisibility(View.VISIBLE);
                    txtCloudsPerc.setText(clouds + "% clouds");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                if (e instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(),
                            "Cannot connect to Internet...Please check your connection!",
                            Toast.LENGTH_SHORT).show();
                } else if (e instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(),
                            "Connection TimeOut! Please check your internet connection.",
                            Toast.LENGTH_SHORT).show();
                } else if (CityName.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "This field cannot be left as blank...",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the City Name properly...",
                            Toast.LENGTH_SHORT).show();
                }

                HideEverything();
            }
        });

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public String GetApiCall(String CityName) {
        //Creating a API Call using OpenWeather API
        String apiKey = "60b1a7ebb158ceb6b95078e21fa41a6d";
        String API = "http://api.openweathermap.org/data/2.5/weather";
        String Url = API.concat("?q=" + CityName + "&APPID=" + apiKey);
        Log.d("XYZ", "Url generated!!" + Url);
        return Url;
    }

    public void HideEverything() {
        //Hide the elements below the generate button
        txtViewCityName.setVisibility(View.INVISIBLE);
        txtTempDisplay.setVisibility(View.INVISIBLE);
        MinMaxTemp.setVisibility(View.INVISIBLE);
        txtWeatherType.setVisibility(View.INVISIBLE);
        txtWeatherType2.setVisibility(View.INVISIBLE);
        txtHumidityPerc.setVisibility(View.INVISIBLE);
        txtCloudsPerc.setVisibility(View.INVISIBLE);
    }
}
