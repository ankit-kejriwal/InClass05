package com.example.inclass05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    ImageView mainImage;
    ImageView prev;
    ImageView next;
    Button btnGo;
    TextView tvKeyword;
    String[] imageURLs = null;
    int currIndex = 0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainImage = findViewById(R.id.imageViewMain);
        prev = findViewById(R.id.imageViewPrev);
        next = findViewById(R.id.imageViewNext);
        tvKeyword = findViewById(R.id.textViewKeyword);
        btnGo = findViewById(R.id.buttonGo);
        prev.setEnabled(false);
        next.setEnabled(false);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()){
                    new getKeyword().execute("http://dev.theappsdr.com/apis/photos/keywords.php");
                } else {
                    Toast.makeText(MainActivity.this, "No Active Internet Connecion", Toast.LENGTH_SHORT).show();
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageURLs!=null){
                    currIndex--;
                    if(currIndex<0)
                        currIndex=imageURLs.length -1;
                    new GetImageAsync(mainImage).execute(imageURLs[currIndex]);
                } else {
                    Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageURLs != null) {
                    currIndex++;
                    if(currIndex>(imageURLs.length-1))
                        currIndex=0;
                    new GetImageAsync(mainImage).execute(imageURLs[currIndex]);
                } else {
                    Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private class getKeyword extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            String result = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line ="";
                    while ((line = reader.readLine()) !=null) {
                        sb.append(line);
                    }
                    result =  sb.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null){
                    connection.disconnect();
                }
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            final String[] temp = s.split(";");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose item");

            builder.setItems(temp, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String url = "http://dev.theappsdr.com/apis/photos/index.php" + "?" +"keyword="+temp[which];
                    tvKeyword.setText(temp[which]);
                    new getImageURL().execute(url);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private class getImageURL extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            String result = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line ="";
                    while ((line = reader.readLine()) !=null) {
                        sb.append(line+" ");
                    }
                    result =  sb.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null){
                    connection.disconnect();
                }
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            imageURLs = s.split(" ");
            currIndex = 0;
            Log.d("demo",s);
            if(s.isEmpty()){
                mainImage.setImageDrawable(null);
                Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
                prev.setEnabled(false);
                next.setEnabled(false);
            } else {
                prev.setEnabled(true);
                next.setEnabled(true);
                new GetImageAsync(mainImage).execute(imageURLs[currIndex]);
            }
        }
    }

    private class GetImageAsync extends AsyncTask<String, Void, Void> {
        ImageView imageView;
        Bitmap bitmap = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading");
            progressDialog.setMax(10);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        public GetImageAsync(ImageView iv) {
            imageView = iv;
        }

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            bitmap = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Handle the exceptions
             finally {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            } else  {
                imageView.setImageDrawable(getDrawable(R.drawable.default_img));
            }
        }
    }


}
