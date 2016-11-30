package io.xjhub.gitview;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
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

    private String mSecretState;
    private OAuth20Service mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSecretState = "secret" + new Random().nextInt(999_999);
        mAuthService = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .state(mSecretState)
                .callback(CALLBACK_URL)
                .build(GitHubApi.instance());

        if (savedInstanceState == null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String code = sharedPref.getString(CODE_PREF_KEY, null);

            if (code != null) {
                new GetTokenTask().execute(code);

                // Show RepoFragment
                RepoFragment repoFragment = new RepoFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, repoFragment, "repo").commit();
            } else {
                // Show LoginFragment
                Bundle args = new Bundle();
                args.putString(LoginFragment.EXTRA_AUTH_URL, mAuthService.getAuthorizationUrl());
                args.putString(LoginFragment.EXTRA_STATE, mSecretState);

                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setArguments(args);

                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, loginFragment, "login").commit();
            }
        }
    }

    @Override
    public void onSignedIn(String code) {
        Log.d(LOG_TAG, "Cookie code: " + code);

        new GetTokenTask().execute(code);

        // Save code into shared preferences
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(CODE_PREF_KEY, code);
        editor.apply();
    }

    private class GetTokenTask extends AsyncTask<String, Void, OAuth2AccessToken> {

        @Override
        protected OAuth2AccessToken doInBackground(String... codes) {
            // Try all access codes
            for (String code : codes) {
                try {
                    return mAuthService.getAccessToken(code);
                } catch (OAuthException | IOException e) {
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(OAuth2AccessToken accessToken) {
            if (accessToken != null) {
                Log.d(LOG_TAG, "Got access token: " + accessToken.getRawResponse());

                // Show RepoFragment if is not active
                RepoFragment repoFragment = (RepoFragment) getFragmentManager().findFragmentByTag("repo");
                if (repoFragment == null) {
                    repoFragment = new RepoFragment();

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, repoFragment, "repo");
                    transaction.commit();
                }

            } else {
                // Show LoginFragment
                Bundle args = new Bundle();
                args.putString(LoginFragment.EXTRA_AUTH_URL, mAuthService.getAuthorizationUrl());
                args.putString(LoginFragment.EXTRA_STATE, mSecretState);

                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setArguments(args);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, loginFragment, "login");
                transaction.commit();
            }
        }
    }
}
