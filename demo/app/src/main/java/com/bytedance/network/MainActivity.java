package com.bytedance.network;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bytedance.network.api.GitHubService;
import com.bytedance.network.model.Repo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "network_demo";
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private int page = 0;
    private String content = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBase("JakeWharton");
            }
        });

        findViewById(R.id.btn_retrofit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRetrofit("JakeWharton");
            }
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) findViewById(R.id.tv)).setText("");
                page = 0;
            }
        });
    }

    private void showRepos(List<Repo> repoList){
        Log.d(TAG,"repo list add "+repoList.size());
        StringBuilder stringBuilder = new StringBuilder(content);
        for (int i = 0; i < repoList.size(); i++) {
            final Repo repo = repoList.get(i);
            stringBuilder.append("仓库名：").append(repo.getName())
                    .append("\n fork 数量：").append(repo.getForksCount())
                    .append("\n star 数量：").append(repo.getStarsCount())
                    .append("\n\n");
        }
        content = stringBuilder.toString();

        ((TextView) findViewById(R.id.tv)).setText(content);
    }


    private void requestBase(final String userName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Repo> repos = baseGetReposFromRemote(userName);
                if (repos != null && !repos.isEmpty()){
                    page++;
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showRepos(repos);
                        }
                    });

                }
            }
        }).start();
    }

    public List<Repo> baseGetReposFromRemote(String userName){
        String urlStr = String.format("https://api.github.com/users/%s/repos?page=%d&per_page=10",userName,page);
        List<Repo> result = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.connect();
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            result = new Gson().fromJson(reader, new TypeToken<List<Repo>>() {}.getType());
            reader.close();
            in.close();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void requestRetrofit(String userName) {

        GitHubService service = retrofit.create(GitHubService.class);

        Call<List<Repo>> repos = service.getRepos(userName,page,10);
        repos.enqueue(new Callback<List<Repo>>() {
            @Override public void onResponse(final Call<List<Repo>> call, final Response<List<Repo>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                final List<Repo> repoList = response.body();
                if (repoList == null || repoList.isEmpty()) {
                    return;
                }
                page++;
                showRepos(repoList);
            }

            @Override public void onFailure(final Call<List<Repo>> call, final Throwable t) {
                t.printStackTrace();
            }
        });
    }
}