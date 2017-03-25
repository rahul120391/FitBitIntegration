package weexcel.india.fitbit;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;

/**
 * Created by Rahul on 12/18/2014.
 */
public class FitBitDialog extends Dialog {
    ProgressDialog dialog;
    Context context;
    WebView webview;
    SharedPreferences prefs;
    public static OAuthConsumer consumer;
    String url;
    public static OAuthProvider provider;

    public FitBitDialog(Context context, ProgressDialog dialog) {
        super(context);
        this.context = context;
        this.dialog = dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_dialog);
        webview = (WebView) findViewById(R.id.webview);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setWebview(dialog);
    }

    private void setWebview(final ProgressDialog dialog) {
        try {
            consumer = new CommonsHttpOAuthConsumer(Config.CONSUMER_KEY, Config.CONSUMER_SECRET);
            consumer.setMessageSigner(new HmacSha1MessageSigner());
            provider = new CommonsHttpOAuthProvider(Config.REQUEST_TOKEN, Config.ACCESS_TOKEN, Config.AUTHORIZE_URL);
            url = provider.retrieveRequestToken(consumer, Config.OAUTH_CALLBACK_URL);
            if (Build.VERSION.SDK_INT >= 11) {
                webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            webview.getSettings().setJavaScriptEnabled(true);
            webview.loadUrl(url);
            webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webview.setWebViewClient(new HelloWebViewClient(dialog));
            webview.setPictureListener(new WebView.PictureListener() {
                @Override
                public void onNewPicture(WebView webView, Picture picture) {
                    if(dialog!=null || dialog.isShowing())
                    {
                        dialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class HelloWebViewClient extends WebViewClient {
        public HelloWebViewClient(ProgressDialog dlg) {
            dialog = dlg;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (dialog == null || !dialog.isShowing()) {
                dialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
                dialog.setMessage(context.getString(R.string.loading));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (dialog.isShowing() || dialog != null) {
                dialog.dismiss();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(Config.OAUTH_CALLBACK_URL)) {
                Uri newurl = Uri.parse(url);
                String verifier = newurl.getQueryParameter(OAuth.OAUTH_VERIFIER);
                try {
                    provider.retrieveAccessToken(consumer, verifier);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("token"+consumer.getToken());
                System.out.println("tokensecret"+consumer.getTokenSecret());
                final SharedPreferences.Editor edit = prefs.edit();
                edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
                edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
                edit.putString(OAuth.OAUTH_VERIFIER, verifier);
                edit.commit();
                cancel();
                for (OnVerifyListener d : list) {
                    d.onVerify(verifier);
                }
            }
            else if (url.contains("https://google.com"))
            {
               cancel();
            }
            else
            {
                view.loadUrl(url);
            }
            return true;
        }

    }

    interface OnVerifyListener {
        public void onVerify(String verifier);
    }

    private List<OnVerifyListener> list = new ArrayList<OnVerifyListener>();

    public void setVerifierListener(OnVerifyListener listener) {
        list.add(listener);
    }

}
