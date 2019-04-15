package com.github.pierods.aeslens;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView decodedText;
    EditText urlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decodedText = (TextView) findViewById(R.id.decodedText);
        urlText = (EditText) findViewById(R.id.urlEditText);

        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    new RetrieveURLTask().execute(urlText.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void updateContentView(final String txt) {
        decodedText.setText(txt);
    }

    private class RetrieveURLTask extends AsyncTask<String, Void, String> {
        private String retrieveURL(String urlText) {

            try {
                StringBuilder sb = new StringBuilder();
                URL url = new URL(urlText);

                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(url.openStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine);

                in.close();

                return sb.toString();

            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            return retrieveURL(strings[0]);
        }

        protected void onPostExecute(final String result) {
            updateContentView(result);
        }
    }
}
