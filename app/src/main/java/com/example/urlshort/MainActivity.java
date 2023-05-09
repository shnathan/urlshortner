package com.example.urlshort;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String BITLY_ACCESS_TOKEN = "d85486f5f3d154c3ed1bc1079e90006f9789ba72";
    private static final String BITLY_API_URL = "https://api-ssl.bitly.com/v4/shorten";

    EditText longUrlEditText;
    Button shortenButton;
    TextView shortUrlTextView;
    Button copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        longUrlEditText = findViewById(R.id.longUrlEditText);
        shortenButton = findViewById(R.id.shortenButton);
        shortUrlTextView = findViewById(R.id.shortUrlTextView);
        copyButton = findViewById(R.id.copyButton);

        shortenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String longUrl = longUrlEditText.getText().toString();
                new ShortenUrlTask().execute(longUrl);
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Short URL", shortUrlTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Short URL copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class ShortenUrlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... longUrls) {
            OkHttpClient client = new OkHttpClient();

            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("long_url", longUrls[0]);

            Gson gson = new Gson();
            String requestBodyJson = gson.toJson(requestBodyMap);

            RequestBody requestBody = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(BITLY_API_URL)
                    .addHeader("Authorization", "Bearer " + BITLY_ACCESS_TOKEN)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                Log.d("Bitly API Response", responseBody);

                BitlyResponse bitlyResponse = gson.fromJson(responseBody, BitlyResponse.class);
                return bitlyResponse.getLink();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String shortUrl) {
            if (shortUrl != null) {
                shortUrlTextView.setText(shortUrl);
            } else {
                Toast.makeText(MainActivity.this, "Failed to shorten URL", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static class BitlyResponse {
        private String link;

        public String getLink() {
            return link;
        }
    }

}
