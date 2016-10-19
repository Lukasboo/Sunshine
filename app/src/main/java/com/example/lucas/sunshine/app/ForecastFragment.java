package com.example.lucas.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.lucas.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static java.lang.String.valueOf;

/**
 * Created by lucas on 01/10/16.
 */

public class ForecastFragment extends android.support.v4.app.Fragment {

    String postCode = "14400-BR"; //not use more
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    public ForecastAdapter mForecastAdapter;
    String[] fetchWeatherStrDates;
    ArrayList<String> list;
    ListView listview;
    SharedPreferences prefs;
    //String[] fetchWeatherStr;
    String[] fetchWeatherStr;


    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        /*mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList<String>());*/

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*String[] values = new String[] {
                "Today - Sunny -- 88/63",
                "Tomorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Rainy - 64/51",
                "Fri - Foggy 70/46",
                "Sat - Sunny - 76/68" };*/

        //updateWeather();
        //refresh();
        /*list = new ArrayList<>(
                Arrays.asList(fetchWeatherStrDates));*/

        /*adapter = new ArrayAdapter(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);*/

        setHasOptionsMenu(true);
        listview = (ListView) rootView.findViewById(R.id.listview_forecast);
        listview.setAdapter(mForecastAdapter);
        /*listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String foreCast = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, foreCast);
                startActivity(intent);
            }
        });*/
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
                updateWeather();
                return true;
            case R.id.action_settings:
                callSettings();
                return true;
            case R.id.map_settings:
                Uri geoLocation = null;
                try {
                    geoLocation = Uri.parse(valueOf(getGeoLocationDataFromJson(fetchWeatherStr)));
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

    //private void updateWeather(){
        /*refresh();
        list = new ArrayList<>(
                Arrays.asList(fetchWeatherStrDates));
        adapter.clear();
        adapter = new ArrayAdapter(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();*/
    //}

    private void updateWeather() {
        /*FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default_value));*/
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
                String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /*public void refresh(){
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
            fetchWeatherStrDates = getWeatherDataFromJson(fetchWeatherStr, 7);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

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

    private String getGeoLocationDataFromJson(String[] forecastJsonStr)
            throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_COORD = "coord";
        final String OWM_LON = "lon";
        final String OWM_LAT = "lat";

        JSONObject forecastJson = new JSONObject(forecastJsonStr[0]);
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

    /*class FetchWeatherTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

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
                        .appendQueryParameter("zip", params[0])
                        .appendQueryParameter("APPID", "349e9189951daec1d08c3b15dccebe86")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", params[1])
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
                //weatherStr = buffer.toString();
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
            //return weatherStr;
            return forecastJsonStr;
            //return null;
        }

    }*/

}
