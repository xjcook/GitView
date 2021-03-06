package io.xjhub.gitview;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnSignedInListener {

    public static final String LOG_TAG = "MainActivity";
    public static final String ACCESS_TOKEN_PREF_KEY = "accessTokenPrefKey";

    private static final String CLIENT_ID = "ff406479db68276af5a3";
    private static final String CLIENT_SECRET = "0dbd87cbc9b6a3bb9cec5a8d4414d2ce1a4b19df";
    private static final String CALLBACK_URL = "http://gitview.xjhub.io/oauth";

    private String mSecretState;
    private OAuth20Service mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mSecretState = "secret" + new Random().nextInt(999_999);
        mAuthService = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .state(mSecretState)
                .callback(CALLBACK_URL)
                .build(GitHubApi.instance());

        if (savedInstanceState == null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String token = sharedPref.getString(ACCESS_TOKEN_PREF_KEY, null);

            if (token != null) {
                // TODO Check token validity

                // Show RepoFragment
                Bundle args = new Bundle();
                args.putString(RepoFragment.EXTRA_ACCESS_TOKEN, token);

                RepoFragment repoFragment = new RepoFragment();
                repoFragment.setArguments(args);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem signOutItem = menu.findItem(R.id.action_sign_out);

        // Set signOut visible if RepoFragment is active
        RepoFragment repoFragment = (RepoFragment) getFragmentManager().findFragmentByTag("repo");
        signOutItem.setVisible(repoFragment != null);

        signOutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // Delete token from shared preferences
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.remove(ACCESS_TOKEN_PREF_KEY);
                editor.apply();

                // Show LoginFragment
                Bundle args = new Bundle();
                args.putString(LoginFragment.EXTRA_AUTH_URL, mAuthService.getAuthorizationUrl());
                args.putString(LoginFragment.EXTRA_STATE, mSecretState);

                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setArguments(args);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, loginFragment, "login");
                transaction.commit();

                // Refresh menu
                invalidateOptionsMenu();

                return true;
            }
        });

        return true;
    }

    @Override
    public void onSignedIn(String code) {
        Log.d(LOG_TAG, "Cookie code: " + code);
        new GetAccessTokenTask().execute(code);
    }

    private class GetAccessTokenTask extends AsyncTask<String, Void, OAuth2AccessToken> {

        @Override
        protected OAuth2AccessToken doInBackground(String... codes) {
            // Try all access codes
            for (String code : codes) {
                try {
                    return mAuthService.getAccessToken(code);
                } catch (OAuthException | IOException e) {
                    Log.d(LOG_TAG, "Temporary code: " + code);
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(OAuth2AccessToken accessToken) {
            if (accessToken != null) {
                // Save token into shared preferences
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(ACCESS_TOKEN_PREF_KEY, accessToken.getAccessToken());
                editor.apply();

                // Show RepoFragment if is not active
                RepoFragment repoFragment = (RepoFragment) getFragmentManager().findFragmentByTag("repo");
                if (repoFragment == null) {
                    Bundle args = new Bundle();
                    args.putString(RepoFragment.EXTRA_ACCESS_TOKEN, accessToken.getAccessToken());

                    repoFragment = new RepoFragment();
                    repoFragment.setArguments(args);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, repoFragment, "repo");
                    transaction.commit();
                }
            } else {
                // Show LoginFragment if token is invalidated
                if (accessToken == null) {
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

            // Refresh menu
            invalidateOptionsMenu();
        }
    }
}
