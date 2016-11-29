package io.xjhub.gitview;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnSignedInListener {

    public static final String LOG_TAG = "MainActivity";
    public static final String CODE_PREF_KEY = "codeKey";

    private static final String CLIENT_ID = "ff406479db68276af5a3";
    private static final String CLIENT_SECRET = "0dbd87cbc9b6a3bb9cec5a8d4414d2ce1a4b19df";
    private static final String CALLBACK_URL = "http://gitview.xjhub.io/oauth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (savedInstanceState != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String code = sharedPref.getString(CODE_PREF_KEY, null);

            final String secretState = "secret" + new Random().nextInt(999_999);
            final OAuth20Service service = new ServiceBuilder()
                    .apiKey(CLIENT_ID)
                    .apiSecret(CLIENT_SECRET)
                    .state(secretState)
                    .callback(CALLBACK_URL)
                    .build(GitHubApi.instance());

            OAuth2AccessToken accessToken = null;
            /*if (code != null) {
                try {
                    accessToken = service.getAccessToken(code);
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }
            }*/

//            if (accessToken == null) {
                // Show LoginFragment
                Bundle args = new Bundle();
                args.putString(LoginFragment.EXTRA_AUTH_URL, service.getAuthorizationUrl());
                args.putString(LoginFragment.EXTRA_STATE, secretState);

                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setArguments(args);

                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, loginFragment).commit();
//            }
//        }
    }

    @Override
    public void onSignedIn(String code) {
        Log.d(LOG_TAG, "Cookie code:" + code);

        // Save code into shared preferences
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(CODE_PREF_KEY, code);
        editor.apply();
    }
}
