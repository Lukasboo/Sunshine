package com.example.lucas.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lucas on 01/10/16.
 */

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected String[] doInBackground(String... postCode) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        String[] weatherStr = null;
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/?q=Franca,SP&APPID=349e9189951daec1d08c3b15dccebe86");
            //
            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?zip=14400,BR&APPID=349e9189951daec1d08c3b15dccebe86&mode=json&units=metric&cnt=7");

            //certa
            //http://api.openweathermap.org/data/2.5/forecast/daily?zip=14400-BR&APPID=349e9189951daec1d08c3b15dccebe86&mode=json&units=metric&cnt=7
            //http://api.openweathermap.org/data/2.5/forecast/daily?zip=14400-BR&APPID=349e9189951daec1d08c3b15dccebe86&mode=json&units=metric&cnt=7
            //.appendQueryParameter("zip", "14400-BR")

            Uri.Builder uri = new Uri.Builder();
            uri.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendEncodedPath("daily")
                    .appendQueryParameter("zip", postCode[0])
                    .appendQueryParameter("APPID", "349e9189951daec1d08c3b15dccebe86")
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cnt", "7");
            String urlBuild = uri.build().toString();
            Log.d(LOG_TAG, urlBuild);
            // Create the request to OpenWeatherMap, and open the connection
            URL url = new URL(urlBuild);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("ForecastFragment", "Error closing stream", e);
                }
            }
        }
        return weatherStr;
        //return forecastJsonStr;
        //return null;
    }

}