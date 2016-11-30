package io.xjhub.gitview;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class Api {

    public static final String API_URL = "https://api.github.com";

    public static class Repo {
        public final String name;
        public final String url;

        public Repo(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public interface GitHubService {
        @GET("users/{user}/repos")
        Call<List<Repo>> listRepos(@Path("user") String user);
    }
}
