package com.example.ahmed.bitcoinchecker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

// TODO: 12/10/2017 add more currencies
// TODO: 12/10/2017 swap between btc and usd
public class MainActivity extends AppCompatActivity {

    public static final String BTC_URL = "https://api.coindesk.com/v1/bpi/currentprice/btc.json";
    private static final String TAG = "URLBUILDER";
    TextView usdValue;
    Button refreshButton;
    EditText btcValue;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usdValue = findViewById(R.id.usd_value);
        refreshButton = findViewById(R.id.refresh_button);
        btcValue = findViewById(R.id.btc_value);


        if (!isNetworkConnected()) {
            Toast.makeText(this, "Check your connection", Toast.LENGTH_SHORT).show();
        }
        new FetchBtcTask().execute(BTC_URL);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchBtcTask().execute(BTC_URL);
            }
        });
        final Handler handler = new Handler();

        // TODO: 12/10/2017   make the user specify the time
        final int delay = 9000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
//                usdValue.setText("...");
//                Log.i(TAG, "run: (Text Changed) " + usdValue.getText());
                handler.postDelayed(this, delay);
                new FetchBtcTask().execute(BTC_URL);
                Log.i(TAG, "run: " + usdValue.getText());
            }
        }, delay);
        btcValue.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new FetchBtcTask().execute(BTC_URL);
            }
        });
    }


    private URL urlBuilder(String uri) {
        Uri builtUri = Uri.parse(uri).buildUpon().build();

        URL url;

        try {
            url = new URL(builtUri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String jsonParser(String jsonWeatherResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonWeatherResponse);
        JSONObject bpi = jsonObject.getJSONObject("bpi");
        JSONObject usd = bpi.getJSONObject("USD");

        return usd.getString("rate_float");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public class FetchBtcTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            usdValue.setTextColor(getColor(R.color.colorAccent));
        }

        @Override
        protected String doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = urlBuilder(location);

            try {
                String jsonBtcResponse = getResponseFromHttpUrl(weatherRequestUrl);

                return jsonParser(jsonBtcResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String valueData) {
            try {
//                double newValue = Double.parseDouble(valueData.toString()) * Integer.parseInt(String.valueOf(btcValue.getText()));
                double newValue;
                double newValueData = Double.parseDouble(valueData);
                double newBtc = Double.parseDouble(String.valueOf(btcValue.getText()));
                newValue = newValueData * newBtc;
//                Log.i(TAG, "onPostExecute: newValueData = " + newValueData);
                if (valueData != null) {
                    usdValue.setText(String.valueOf(newValue));
                    Log.d(TAG, "onPostExecute: valueData = " + newValue);
                } else {
                    Log.i(TAG, "onPostExecute: valueData (failed) = " + newValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Check your connection", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onPostExecute: Exception " + valueData + " btcValue = " + btcValue.getText());
            }
            usdValue.setTextColor(getColor(R.color.colorPrimary));
        }
    }

}
