package weexcel.india.fitbit;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class CommonClass extends Activity {

   // static Context context;
    static HttpGet httpGet;
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";


    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public String FormatDate(String s) {
        String split1[] = s.split(" ");
        String split2[] = split1[0].split("-");
        String date = split2[2] + "-" + split2[1] + "-" + split2[0];

        return date;

    }

    public static JSONObject getJSON(String rurl) {
        StringBuilder builder = new StringBuilder();
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        HttpClient client = new DefaultHttpClient(httpParameters);

        try {
            String url = removeSpacesFromUrl(rurl);
            httpGet = new HttpGet(url);
            httpGet.setParams(httpParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                try {
                    jObj = new JSONObject(builder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jObj;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String removeSpacesFromUrl(String url) {

        url = url.replaceAll(" ", "%20");
        return url;
    }

    public static JSONObject getJSONFromUrl(String url) {

        // Making HTTP request
        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);


            HttpPost httpPost = new HttpPost(url);
            httpPost.setParams(httpParameters);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    json = sb.toString();
                } catch (Exception e) {

                }
                try {
                    jObj = new JSONObject(json);
                    is.close();
                } catch (JSONException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return jObj;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static boolean checkWhatsapp(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.whatsapp",
                    PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static ArrayList<String> getWhatsAppContacts(Context ctx) {
        ArrayList<String> waContacts = new ArrayList<String>();
        final String[] projection = {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.MIMETYPE,
                "account_type",
                ContactsContract.Data.DATA3,
        };
        final String selection = ContactsContract.Data.MIMETYPE + " =? and account_type=?";
        final String[] selectionArgs = {
                "vnd.android.cursor.item/vnd.com.whatsapp.profile",
                "com.whatsapp"
        };
        ContentResolver cr = ctx.getContentResolver();
        Cursor c = cr.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        while (c.moveToNext()) {
            waContacts.add(c.getString(c.getColumnIndex(ContactsContract.Data.DATA3)));
        }
        return waContacts;
    }


}
