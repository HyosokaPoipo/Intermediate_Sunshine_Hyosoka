package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by HyosokaPoipo on 4/12/2016.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter = null;
    //Constructor
    public ForecastFragment()
    {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //ini buat ngeload menu forecastfragment.xml yang kita buat sebelumnya
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionMenu itu buat ngehandle event2 menu
        setHasOptionsMenu(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            /**
             * disini kode buat action yang akan sunshine lakukan
             * ketika menu Refresh di pencet...hehehe...:D
             */
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("94043");
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

          // Create some dummy data for the ListView.  Here's a sample weekly forecast
            String[] data = {
                    "Mon 6/23 - Sunny - 31/17",
                    "Tue 6/24 - Foggy - 21/8",
                    "Wed 6/25 - Cloudy - 22/17",
                    "Thurs 6/26 - Rainy - 18/11",
                    "Fri 6/27 - Foggy - 21/10",
                    "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                    "Sun 6/29 - Sunny - 20/7"
            };
            List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

            mForecastAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_forecast, // The name of the layout ID.
                            R.id.list_item_forecast_textview, // The ID of the textview to populate.
                            weekForecast);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String forecast = mForecastAdapter.getItem(position);

                    Toast.makeText(getActivity(), "Ini toast Bro : "+forecast , Toast.LENGTH_SHORT).show();
                }
             });

        return rootView;
    }



    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

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


        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                      throws JSONException {
            //Parameter yang akan diextract dari json string
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            //Ngubah json string ke json object
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            //Ambil parameter yang dalam "list" ajjah...
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


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

            for (String s : resultStrs) {
            Log.v("FetchWeatherTask", "Forecast entry: " + s);
                      }
        return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {

            //Klu g' ada input apa2, g' usah lakuin sst.. :D
            if(params.length == 0)
            {
                return null;
            }

              /*
            urlConnection dan reader nnti mw dipake dibagian finally, jadi deklarasinya
            diluar try-catch
             */
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // String untuk menampung respon yang berupa json format
            String forecastJsonStr = null;

            //Variabel Pembantu
            String format = "json";
            String units = "metric";
            int numDays = 7;


            try {
                // Konfigurasi URL untuk OpenWeatherMap query
                // Parameter2nya dapat dilihat di halaman OWM's forecast :
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                       "http://api.openweathermap.org/data/2.5/forecast/daily?";
               final String QUERY_PARAM = "q";
               final String FORMAT_PARAM = "mode";
               final String UNITS_PARAM = "units";
               final String DAYS_PARAM = "cnt";
               final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                               .appendQueryParameter(QUERY_PARAM, params[0])
                               .appendQueryParameter(FORMAT_PARAM, format)
                               .appendQueryParameter(UNITS_PARAM, units)
                               .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                               .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                               .build();

                       URL url = new URL(builtUri.toString());

                       Log.v("ForecastFragment", "Built URI " + builtUri.toString());


                // Membuat request ke OpenWeatherMap, kemudian membuka koneksi baru
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Nangkep response dari openweatherMap terus disiapin untuk jadi variabel string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // klu g' dapat apa2, return null
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line; //ini buat nyimpen string pas looping
                while ((line = reader.readLine()) != null) {

                    //nambain perline
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    //klu buffernya kosong, berarti g' ada datanya kaan... :v
                    return null;
                }
                //Masukin response akhir yang udah berupa string ke forecastJsonStr
                forecastJsonStr = buffer.toString();
                Log.i("Data OpenWeatherMap",forecastJsonStr);

            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                //Klu ada error pas mau ngambil data dari weatherMap, berarti kita g' dapat respon apa2
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


            try {

                return getWeatherDataFromJson(forecastJsonStr, numDays);

            } catch (JSONException e) {
                Log.e("doInBackground", e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null) {
            mForecastAdapter.clear();
            for(String dayForecastStr : strings) {
                    mForecastAdapter.add(dayForecastStr);
                }
            // New data is back from the server.  Hooray!
            }
        }
    }

}





















