package com.example.android.weather;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.R.attr.max;
import static android.R.attr.name;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class Forecast extends AppCompatActivity {

    TextView T1,T2,T3,T4,T5;
    String Requrl,jsonresp;
    boolean var;
    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        T1 = (TextView) findViewById(R.id.day1);
        T2 = (TextView) findViewById(R.id.day2);
        T3 = (TextView) findViewById(R.id.day3);
        T4 = (TextView) findViewById(R.id.day4);
        T5 = (TextView) findViewById(R.id.day5);

        FetchWeatherData();

    }


    public void FetchWeatherData()
    {
        Intent i = getIntent();
        String name = i.getStringExtra("cityname");

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

            Requrl = temp.toString();

            new Forecasttask().execute();
        }

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
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray listarray = root.getJSONArray("list");

            int k=0;
            i=0;

            while(i<5)
            {
                JSONObject mainobj = listarray.getJSONObject(k).getJSONObject("main");

                Double t = mainobj.getDouble("temp");
                Double min = mainobj.getDouble("temp_min");
                Double max = mainobj.getDouble("temp_max");
                String date = listarray.getJSONObject(k).getString("dt_txt").substring(0,10);

                String res = "DATE : " + date + "\n" + "TEMP. : " + t + "\n" + "MIN/MAX TEMP. : " + min + "/" + max;

                i++;

                            switch(i)
                        {
                            case 1: T1.setText(res);break;
                            case 2: T2.setText(res);break;
                            case 3: T3.setText(res);break;
                            case 4: T4.setText(res);break;
                            case 5: T5.setText(res);break;
                        }



                k+=8;
            }




         /*   while(i<5)
            {
                if(listarray.getJSONObject(k).getString("dt_txt").substring(11,18).equals("00:00:00"))
                {
                    JSONObject mainobj = listarray.getJSONObject(k).getJSONObject("main");

                    Double t = mainobj.getDouble("temp");
                    Double min = mainobj.getDouble("temp_min");
                    Double max = mainobj.getDouble("temp_max");
                    String date = listarray.getJSONObject(k).getString("dt_txt").substring(0,9);

                    final String res = date + "\n" + t + "\n" + min + "/" + max;

                    i++;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            switch(i)
                            {
                                case 1: T1.setText(res);break;
                                case 2: T2.setText(res);break;
                                case 3: T3.setText(res);break;
                                case 4: T4.setText(res);break;
                                case 5: T5.setText(res);break;
                            }
                        }
                    });
                }


            }
*/
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    public class Forecasttask extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPreExecute()
        {
            var = true;
        }

        @Override
        protected Void doInBackground(Void... params)
        {

            URL url = null;
            jsonresp = "";

            try {
                url = new URL(Requrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                jsonresp = makeHttprequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ExtractfromJSON(jsonresp);
        }
    }

}
