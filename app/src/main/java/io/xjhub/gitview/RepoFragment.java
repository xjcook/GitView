package io.xjhub.gitview;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RepoFragment extends ListFragment {

    public static final String LOG_TAG = "RepoFragment";
    public static final String EXTRA_ACCESS_TOKEN = "accessToken";

    private String mAccessToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_repo, container, false);

        // Get Access Token
        String token = getArguments().getString(EXTRA_ACCESS_TOKEN);
        if (token != null) {
            mAccessToken = token;
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new GetReposTask().execute();
    }

    private class GetReposTask extends AsyncTask<Void, Void, List<Api.Repo>> {

        @Override
        protected List<Api.Repo> doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Api.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Api.GitHubService service = retrofit.create(Api.GitHubService.class);
            Call<List<Api.Repo>> call = service.listRepos("xjcook");

            try {
                return call.execute().body();
            } catch (IOException e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Api.Repo> repos) {
            ArrayList<String> repoList = new ArrayList<>();
            for (Api.Repo repo : repos) {
                repoList.add(repo.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, repoList);
            setListAdapter(adapter);
        }
    }
}
