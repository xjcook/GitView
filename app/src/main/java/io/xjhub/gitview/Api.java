package io.xjhub.gitview;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class Api {

    public static final String API_URL = "https://api.github.com";

    public static class Repo {
        public final String name;
        public final String description;

        public Repo(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public interface GitHubService {
        @GET("user/repos")
        Call<List<Repo>> listRepos(@Header("Authorization") String authorization);
    }
}
