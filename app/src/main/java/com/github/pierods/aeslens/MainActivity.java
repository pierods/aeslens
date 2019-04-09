package com.github.pierods.aeslens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView decodedText = (TextView) findViewById(R.id.decodedText);
        final EditText urlText = (EditText) findViewById(R.id.urlEditText);

        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    decodedText.setText(retrieveURL(urlText.getText().toString()));
                    return true;
                }
                return false;
            }
        });
    }

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
}
