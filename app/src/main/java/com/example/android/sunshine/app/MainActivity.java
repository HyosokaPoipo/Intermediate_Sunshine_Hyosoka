package com.example.android.sunshine.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
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

           ArrayAdapter<String> mForecastAdapter =
           new ArrayAdapter<String>(
                       getActivity(), // The current context (this activity)
                       R.layout.list_item_forecast, // The name of the layout ID.
                       R.id.list_item_forecast_textview, // The ID of the textview to populate.
                       weekForecast);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);

            /*
            urlConnection dan reader nnti mw dipake dibagian finally, jadi deklarasinya
            diluar try-catch
             */
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // String untuk menampung respon yang berupa json format
            String forecastJsonStr = null;

            try {
                // Konfigurasi URL untuk OpenWeatherMap query
                // Parameter2nya dapat dilihat di halaman OWM's forecast :
                // http://openweathermap.org/API#forecast
                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl.concat(apiKey));

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
               } catch (IOException e) {
                   Log.e("PlaceholderFragment", "Error ", e);
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
                                   Log.e("PlaceholderFragment", "Error closing stream", e);
                               }
                       }
               }


            return rootView;
        }
    }
}
