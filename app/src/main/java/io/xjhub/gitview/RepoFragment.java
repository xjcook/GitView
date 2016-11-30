package io.xjhub.gitview;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RepoFragment extends ListFragment {

    public static final String LOG_TAG = "RepoFragment";
    public static final String EXTRA_ACCESS_TOKEN = "accessToken";

    private static final int STATE_IDLE = 0;
    private static final int STATE_WORKING = 1;

    private int mState;

    private String mAccessToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Handle menu by fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_repo, container, false);

        mState = STATE_IDLE;
        mAccessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mAccessToken != null) {
            new GetRepoListTask().execute();
        }
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

    // TODO Add ProgressBar
    private class GetRepoListTask extends AsyncTask<Void, Void, List<Api.Repo>> {

        @Override
        protected List<Api.Repo> doInBackground(Void... voids) {
            mState = STATE_WORKING;
            ActivityCompat.invalidateOptionsMenu(getActivity());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Api.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Api.GitHubService service = retrofit.create(Api.GitHubService.class);
            Call<List<Api.Repo>> call = service.listRepos("token " + mAccessToken);

            try {
                return call.execute().body();
            } catch (IOException e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Api.Repo> repos) {
            mState = STATE_IDLE;
            ActivityCompat.invalidateOptionsMenu(getActivity());

            if (repos != null) {
                RepoListAdapter adapter = new RepoListAdapter(getActivity(), repos);
                setListAdapter(adapter);
            }
        }
    }
}
