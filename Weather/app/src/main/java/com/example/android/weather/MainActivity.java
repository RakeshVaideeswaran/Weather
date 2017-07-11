package com.example.android.weather;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {

    String reqURL;
    boolean var;
    EditText E;
    ImageView I;
    TextView C,T,MAX,MIN,P,H;
    LinearLayout C1,T1,MAX1,MIN1,P1,H1;
    String c,img;
    double t,max,min,p;
    int h;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE;
    String name = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        E = (EditText) findViewById(R.id.cityName);
        C = (TextView) findViewById(R.id.city);
        T = (TextView) findViewById(R.id.temp);
        MAX = (TextView) findViewById(R.id.tempmax);
        MIN = (TextView) findViewById(R.id.tempmin);
        P = (TextView) findViewById(R.id.pressure);
        H = (TextView) findViewById(R.id.humidity);

        C1 = (LinearLayout) findViewById(R.id.CITY);
        T1 = (LinearLayout) findViewById(R.id.TEMP);
        MAX1 = (LinearLayout) findViewById(R.id.TEMPMAX);
        MIN1 = (LinearLayout) findViewById(R.id.TEMPMIN);
        P1 = (LinearLayout) findViewById(R.id.PRESSURE);
        H1 = (LinearLayout) findViewById(R.id.HUMIDITY);

        I = (ImageView) findViewById(R.id.imgview);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.forecast,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(name.equals(""))
            Toast.makeText(MainActivity.this,"Enter CITY Name",Toast.LENGTH_LONG).show();

        else
        {
            Intent intent = new Intent(MainActivity.this, Forecast.class);
            intent.putExtra("cityname", name);
            startActivity(intent);
        }

        return true;
    }

    public void AutocompletePlaces(View view)
    {
        PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

        try {
            AutocompleteFilter filter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(filter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                E.setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            }
        }
    }

    public void FetchWeatherData(View view)
    {
        name = E.getText().toString();

        if(name.equals(""))
            Toast.makeText(getApplicationContext(),"Enter city name",Toast.LENGTH_LONG).show();


        else
        {
            StringBuilder temp = new StringBuilder();
            String baseURL = "http://api.openweathermap.org/data/2.5/forecast?q=";
            String endURL = "&APPID=87992a2cea91f0a9a50ec62cb71cc7c7";
            temp.append(baseURL);
            temp.append(name);
            temp.append(endURL);

            reqURL = temp.toString();

            new Weathertask().execute();
        }

    }

    public void UIupdate()
    {

        C.setText(c);
        P.setText(String.valueOf(p));
        T.setText(String.valueOf(t));
        MAX.setText(String.valueOf(max));
        MIN.setText(String.valueOf(min));
        H.setText(String.valueOf(h));
        Picasso.with(this).load(img).into(I);

    }

    public String StreamReader(InputStream I)throws IOException
    {
        StringBuilder builder = new StringBuilder();

        if(I != null)
        {
            InputStreamReader isr = new InputStreamReader(I, Charset.forName("UTF-8"));
            BufferedReader B = new BufferedReader(isr);

            String line = B.readLine();

            while(line!=null)
            {
                builder.append(line);
                line = B.readLine();
            }
        }

        return builder.toString();
    }

    public String makeHttprequest(URL url) throws IOException
    {

        String jsonres = "";

        if(url == null)
        {
            return jsonres;
        }

        InputStream is = null;
        HttpURLConnection urlco = null;


        try
        {
            urlco = (HttpURLConnection) url.openConnection();
            urlco.setRequestMethod("GET");
            urlco.connect();

            if (urlco.getResponseCode() == 200)
            {
                is = urlco.getInputStream();
                jsonres = StreamReader(is);
            }

            else
                var = false;

        }catch (IOException e){

        }finally {
            if(urlco!=null)
                urlco.disconnect();

            if(is!=null)
                is.close();
        }

        return jsonres;
    }

    public void ExtractfromJSON(String jsonString)
    {
        try
        {
            JSONObject root = new JSONObject(jsonString);
            c = root.getJSONObject("city").getString("name");
            JSONArray listarray = root.getJSONArray("list");
            JSONObject currdata = listarray.getJSONObject(0);
            JSONObject  mainobj = currdata.getJSONObject("main");
            JSONArray weatherarray = currdata.getJSONArray("weather");
            JSONObject weatherarrobj = weatherarray.getJSONObject(0);

            img = "http://openweathermap.org/img/w/";
            img += weatherarrobj.getString("icon");
            img+=".png";
            t = mainobj.getDouble("temp");
            min = mainobj.getDouble("temp_min");
            max = mainobj.getDouble("temp_max");
            p = mainobj.getDouble("pressure");
            h = mainobj.getInt("humidity");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void settingVisibility()
    {
        C1.setVisibility(View.VISIBLE);
        T1.setVisibility(View.VISIBLE);
        MIN1.setVisibility(View.VISIBLE);
        MAX1.setVisibility(View.VISIBLE);
        P1.setVisibility(View.VISIBLE);
        H1.setVisibility(View.VISIBLE);
        I.setVisibility(View.VISIBLE);
    }

    public void settingInvisibility()
    {
        C1.setVisibility(View.GONE);
        T1.setVisibility(View.GONE);
        MIN1.setVisibility(View.GONE);
        MAX1.setVisibility(View.GONE);
        P1.setVisibility(View.GONE);
        H1.setVisibility(View.GONE);
        I.setVisibility(View.GONE);
    }

    public class Weathertask extends AsyncTask<Void,Void,Void>{


        @Override
        protected void onPreExecute()
        {
            var = true;
            settingInvisibility();
        }

        @Override
        protected Void doInBackground(Void... params)
        {

            URL url = null;
            String jsonresp = "";

            try {
                 url = new URL(reqURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                jsonresp = makeHttprequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExtractfromJSON(jsonresp);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if(var == true)
            {
                settingVisibility();
                UIupdate();
            }

            else
                Toast.makeText(getApplicationContext(),"DATA UNAVAILABLE",Toast.LENGTH_LONG).show();

            E.setText("");
        }
    }

}
