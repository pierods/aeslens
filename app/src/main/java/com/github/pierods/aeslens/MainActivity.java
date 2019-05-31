package com.github.pierods.aeslens;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    static TextView decodedText;
    EditText urlText;
    static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decodedText = (TextView) findViewById(R.id.decodedText);
        decodedText.setMovementMethod(new ScrollingMovementMethod());

        urlText = (EditText) findViewById(R.id.urlEditText);
        progress = new ProgressDialog(this);

        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    progress.setMessage("Loading...");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.show();
                    new RetrieveURLTask().execute(urlText.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private static void updateContentView(final byte[] bytes) {
        progress.dismiss();
        Decoder decoder = new Decoder();

        String cleartext;

        cleartext = decoder.decode(bytes);
        decodedText.setText(cleartext);
    }

    private static class RetrieveURLTask extends AsyncTask<String, Void, byte[]> {
        private byte[] retrieveURL(String urlText) {

            try {
                URL url = new URL(urlText);

                BufferedInputStream in;
                in = new BufferedInputStream(url.openStream());

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] ba = new byte[1024];

                while (in.read(ba) > 0) {
                    out.write(ba);
                }
                in.close();

                return out.toByteArray();

            } catch (Exception e) {
                return e.getLocalizedMessage().getBytes();
            }
        }

        @Override
        protected byte[] doInBackground(String... strings) {
            return retrieveURL(strings[0]);
        }

        protected void onPostExecute(final byte[] result) {
            updateContentView(result);
        }
    }
}

class Decoder {
    public String decode(byte[] encodedBytes) {
        Cipher c;

        try {
            c = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }



        return new String(encodedBytes);
    }
}