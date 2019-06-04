package com.github.pierods.aeslens;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        protected void onPostExecute(final byte[] result) {
            updateContentView(result);
        }
    }
}

class Decoder {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String decode(byte[] encodedBytes) {

        String password = "abc123";
        MessageDigest sha256;

        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return e.getLocalizedMessage();
        }

        byte[] hashedPassword = sha256.digest(password.getBytes(StandardCharsets.UTF_8));
        ByteBuffer bb = ByteBuffer.wrap(encodedBytes);

        byte[] nonce = new byte[12];
        bb.get(nonce, 0, 12);
        byte[] content = new byte[bb.remaining()];
        bb.get(content);

        Cipher c = null;

        try {
            c = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            return e.getLocalizedMessage();
        } catch (NoSuchPaddingException e) {
            return e.getLocalizedMessage();
        }

        SecretKey secretKey = new SecretKeySpec(hashedPassword, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16, nonce);

        try {
            c.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
        } catch (InvalidKeyException e) {
            return e.getLocalizedMessage();
        } catch (InvalidAlgorithmParameterException e) {
            return e.getLocalizedMessage();
        }

        byte[] decodedData;
        try {
            decodedData = c.doFinal(content);
        } catch (BadPaddingException e) {
            return e.getLocalizedMessage();
        } catch (IllegalBlockSizeException e) {
            return e.getLocalizedMessage();
        }


        return new String(decodedData);
    }
}