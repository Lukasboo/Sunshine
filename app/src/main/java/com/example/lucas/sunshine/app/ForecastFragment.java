package com.example.lucas.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static java.lang.String.valueOf;

/**
 * Created by lucas on 01/10/16.
 */

public class ForecastFragment extends android.support.v4.app.Fragment {

    String postCode = "14400-BR"; //not use more
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    public ArrayAdapter adapter;
    String[] fetchWeatherStrDates;
    ArrayList<String> list;
    ListView listview;
    SharedPreferences prefs;
    String[] fetchWeatherStr;


    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*String[] values = new String[] {
                "Today - Sunny -- 88/63",
                "Tomorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Rainy - 64/51",
                "Fri - Foggy 70/46",
                "Sat - Sunny - 76/68" };*/

        //updateWeather();
        refresh();
        list = new ArrayList<>(
                Arrays.asList(fetchWeatherStrDates));

        adapter = new ArrayAdapter(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        setHasOptionsMenu(true);
        listview = (ListView) rootView.findViewById(R.id.listview_forecast);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String foreCast = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, foreCast);
                startActivity(intent);
            }
        });
        return rootView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_settings:
                callSettings();
                return true;
            case R.id.map_settings:
                Uri geoLocation = null;
                try {
                    geoLocation = Uri.parse(valueOf(getGeoLocationDataFromJson(fetchWeatherStr[0])));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showMap(geoLocation);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void callSettings(){
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    private void updateWeather(){
        refresh();
        list = new ArrayList<>(
                Arrays.asList(fetchWeatherStrDates));
        adapter.clear();
        adapter = new ArrayAdapter(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void refresh(){
        fetchWeatherStr = null;
        //FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(), adapter);
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            fetchWeatherStr = fetchWeatherTask.execute(prefs.getString("location", "14400-BR"), prefs.getString("unit", "metric")).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            fetchWeatherStrDates = getWeatherDataFromJson(fetchWeatherStr[0], 7);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showMap(Uri geoLocation) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        /*for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }*/
        return resultStrs;
    }

    private String getGeoLocationDataFromJson(String forecastJsonStr)
            throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_COORD = "coord";
        final String OWM_LON = "lon";
        final String OWM_LAT = "lat";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        JSONObject weatherArray = cityJson.getJSONObject(OWM_COORD);

        String lat;
        String lon;

        String resultStrs = new String();

        lat = weatherArray.getString(OWM_LAT);
        lon = weatherArray.getString(OWM_LON);

        resultStrs = "geo:" + lat + "," + lon;

        return resultStrs;

    }

}
