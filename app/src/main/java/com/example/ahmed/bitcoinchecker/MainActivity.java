package com.example.ahmed.bitcoinchecker;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "URLBUILDER";
    TextView usdValue;
    Button refreshButton;
    public static final String BTC_URL = "https://api.coindesk.com/v1/bpi/currentprice/btc.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usdValue = (TextView) findViewById(R.id.usd_value);
        refreshButton = (Button) findViewById(R.id.refresh_button);

        new FetchBtcTask().execute(BTC_URL);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchBtcTask().execute(BTC_URL);
            }
        });
        final Handler handler = new Handler();
        final int delay = 9000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                usdValue.setText("...");
//                Log.i(TAG, "run: (Text Changed) " + usdValue.getText());
                handler.postDelayed(this, delay);
                new FetchBtcTask().execute(BTC_URL);
                Log.i(TAG, "run: " + usdValue.getText());
            }
        }, delay);
    }

    private URL urlBuilder(String uri) {
        Uri builtUri = Uri.parse(uri).buildUpon().build();

        URL url = null;

        try{
            url = new URL(builtUri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public class FetchBtcTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            usdValue.setText("...");
        }

        // COMPLETED (6) Override the doInBackground method to perform your network requests
        @Override
        protected String doInBackground(String... params) {

            /* If there's no zip code, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = urlBuilder(location);

            try {
                String jsonBtcResponse = getResponseFromHttpUrl(weatherRequestUrl);

                String simpleJsonBtcData = jsonParser(MainActivity.this, jsonBtcResponse);

                return simpleJsonBtcData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // COMPLETED (7) Override the onPostExecute method to display the results of the network request
        @Override
        protected void onPostExecute(String valueData) {
            if (valueData != null) {
                usdValue.setText(valueData);
                Log.d(TAG, "onPostExecute: valueData = " + valueData);
            } else {
                Log.i(TAG, "onPostExecute: valueData (failed) = " + valueData);
            }
        }
    }

    private String jsonParser(Context context, String jsonWeatherResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonWeatherResponse);
        JSONObject bpi = jsonObject.getJSONObject("bpi");
        JSONObject usd = bpi.getJSONObject("USD");
        String value = usd.getString("rate");

        return value;
    }

}
