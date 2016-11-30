package io.xjhub.gitview;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class LoginFragment extends Fragment {

    public static final String LOG_TAG = "LoginFragment";
    public static final String EXTRA_AUTH_URL = "authUrl";
    public static final String EXTRA_STATE = "state";

    private static final int STATE_IDLE = 0;
    private static final int STATE_WORKING = 1;

    private int mState;

    private Button mButton;
    private WebView mWebView;

    OnSignedInListener mCallback;

    // Container Activity must implement this interface
    public interface OnSignedInListener {
        void onSignedIn(String code);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Handle menu by fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mState = STATE_IDLE;
        mButton = (Button) rootView.findViewById(R.id.loginBtn);
        mWebView = (WebView) rootView.findViewById(R.id.webView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String cookies = CookieManager.getInstance().getCookie(url);
                String code = getCookie(cookies, "code");
                String state = getCookie(cookies, "state");

                Log.d(LOG_TAG, "Cookie code: " + code);
                Log.d(LOG_TAG, "Cookie state: " + state);

                // Callback MainActivity if cookie is correct
                if (code != null && state.equals(getArguments().getString(EXTRA_STATE))) {
                    mCallback.onSignedIn(code);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                mState = STATE_WORKING;

                // Refresh menu if progress <= 10% || >= 100%
                if (newProgress <= 10) {
                    ActivityCompat.invalidateOptionsMenu(getActivity());
                } else if (newProgress >= 100) {
                    mState = STATE_IDLE;
                    ActivityCompat.invalidateOptionsMenu(getActivity());
                }
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to authorization url
                mWebView.loadUrl(getArguments().getString(EXTRA_AUTH_URL));

                mButton.setVisibility(Button.GONE);
                mWebView.setVisibility(WebView.VISIBLE);
            }
        });

        if (savedInstanceState != null) {
            Boolean isWebViewVisible = savedInstanceState.getBoolean("isWebViewVisible", false);

            if (isWebViewVisible) {
                // Go to authorization url
                mWebView.loadUrl(getArguments().getString(EXTRA_AUTH_URL));

                mButton.setVisibility(Button.GONE);
                mWebView.setVisibility(WebView.VISIBLE);
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Boolean isWebViewVisible = mWebView.getVisibility() == WebView.VISIBLE;
        outState.putBoolean("isWebViewVisible", isWebViewVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present
        inflater.inflate(R.menu.menu_login, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem progressBar = menu.findItem(R.id.action_working);

        switch (mState) {
            case STATE_IDLE:
                progressBar.setVisible(false);
                break;
            case STATE_WORKING:
                progressBar.setVisible(true);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSignedInListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSignedInListener");
        }
    }

    /**
     * Get cookie value
     * @param cookies value from CookieManager.getCookie()
     * @param cookieName name of the cookie
     * @return cookieValue
     */
    public static String getCookie(String cookies, String cookieName) {
        String cookieValue = null;

        if (cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(cookieName)) {
                    String[] temp1 = ar1.split("=");
                    cookieValue = temp1[1];
                }
            }
        }

        return cookieValue;
    }
}
