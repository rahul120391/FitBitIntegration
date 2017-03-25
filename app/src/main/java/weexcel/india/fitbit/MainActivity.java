package weexcel.india.fitbit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;


public class MainActivity extends Activity implements View.OnClickListener {

    Button btn_login;
    FitBitDialog dialog;
    private SharedPreferences prefs;
    String token, secret;
    String verifier1;
    OAuthConsumer consumer;
    ProgressDialog progdialog;
    String url = "https://api.fitbit.com/1/user/-/profile.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (CommonClass.checkInternetConnection(MainActivity.this)) {
                    Login();
                } else {
                    System.out.println("no internet connection");
                }
                break;
        }
    }

    public void Login() {
        ProgressDialog dialog1 = new ProgressDialog(MainActivity.this);
        if (dialog == null || !dialog.isShowing()) {
            dialog = new FitBitDialog(MainActivity.this, dialog1);
            dialog.show();
        }
        dialog.setVerifierListener(new FitBitDialog.OnVerifyListener() {
            @Override
            public void onVerify(String verifier) {
                if (verifier != null) {
                    token = prefs.getString(OAuth.OAUTH_TOKEN, "");
                    verifier1 = prefs.getString(OAuth.OAUTH_VERIFIER, "");
                    secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
                    Toast.makeText(MainActivity.this, token + "\n" + secret, Toast.LENGTH_SHORT).show();
                    System.out.println("token" + token);
                    System.out.println("secret" + secret);
                    new GetData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                } else {
                    System.out.println("verifier is null");
                }
            }
        });
    }

    public String computeHmac(String baseString, String key)
            throws NoSuchAlgorithmException, InvalidKeyException,
            IllegalStateException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(),
                mac.getAlgorithm());
        mac.init(secret);
        byte[] digest = mac.doFinal(baseString.getBytes());
        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

    public String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString().trim();
    }

    private char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    private OAuthConsumer getConsumer() {
        consumer = new CommonsHttpOAuthConsumer(Config.CONSUMER_KEY,
                Config.CONSUMER_SECRET);
        consumer.setTokenWithSecret(token, secret);
        return consumer;
    }

    public void makesecuredRequest(String url, OAuthConsumer consumer) {
        try {
            HttpClient client = new DefaultHttpClient();
            String timestamp = "" + System.currentTimeMillis() / 1000L;
            String nonce = MethodClass.randomStringOfLength(32);
            String baseString = encode("GET") + "&" + encode(url) + "&" + encode("oauth_consumer_key=" + Config.CONSUMER_KEY + "&" + "oauth_token=" + token + "&" + "oauth_signature_method=HMAC-SHA1" + "&" + "oauth_timestamp=" + timestamp + "&" + "oauth_nonce=" + nonce + "&" + "oauth_version=1.0");
            String signingkey = Config.CONSUMER_SECRET + "&" + secret;
            String lSignature = computeHmac(baseString, signingkey);
            HttpGet request = new HttpGet(url);
            String header = "OAuth " + "oauth_consumer_key=\"" + encode(Config.CONSUMER_KEY) + "\"," +
                    "oauth_nonce=\"" + encode(nonce) + "\"," +
                    "oauth_signature_method=\"HMAC-SHA1\"," +
                    "oauth_timestamp=\"" + encode(timestamp) + "\"," +
                    "oauth_token=\"" + encode(token) + "\"," +
                    "oauth_signature=\"" + encode(lSignature) + "\"," +
                    "oauth_timestamp=\"" + encode(timestamp) + "\"," +
                    "oauth_version=\"" + 1.0 + "\"";
            System.out.println("header" + header);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.addHeader("Authorization", header);
            consumer.sign(request);
            HttpResponse response = client.execute(request);
            System.out.println("status code:"
                    + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(content));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            try {
                JSONObject jObj = new JSONObject(builder.toString());
                System.out.println("object" + jObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GetData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progdialog == null || !progdialog.isShowing()) {
                progdialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
                progdialog.setMessage(getString(R.string.loading));
                progdialog.setCancelable(false);
                progdialog.setIndeterminate(true);
                progdialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                makesecuredRequest(url, getConsumer());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progdialog != null || progdialog.isShowing()) {
                progdialog.dismiss();
            }
        }
    }
}
