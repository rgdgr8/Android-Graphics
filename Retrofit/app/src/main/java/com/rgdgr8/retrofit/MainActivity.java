package com.rgdgr8.retrofit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Comment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Api api;
    private EditText tv;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        et = findViewById(R.id.et);

        Gson gson = new GsonBuilder().serializeNulls().create();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                        Request request=chain.request();

                        Request newRequest=request.newBuilder()
                                .addHeader("x-rapidapi-host", "dad-jokes.p.rapidapi.com")
                                .addHeader("x-rapidapi-key", "ecb0d1f8e1msh319c8d5abc47418p15b748jsneb8877ea1aea")
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .addInterceptor(httpLoggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dad-jokes.p.rapidapi.com/random/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        api = retrofit.create(Api.class);
    }

    public void click(View v) {
        tv.setText("");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        //getComments();
        postComments();
        //updatePost();
    }

    private void getComments() {
        String no = et.getText().toString();
        String type=tv.getText().toString();
        if (no.equals("") || no == null) {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            //return;
        }
        //Call<List<Comments>> comments=api.getComments("posts/"+no+"/comments");
        Call<List<Jokes>> comments = api.getComments(2);

        comments.enqueue(new Callback<List<Jokes>>() {
            private static final String TAG = "gc";

            @Override
            public void onResponse(Call<List<Jokes>> call, Response<List<Jokes>> response) {
                if (!response.isSuccessful()) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                    tv.setText("Code: " + response.code());
                    return;
                }
                List<Jokes> comments1 = response.body();

                assert comments1 != null;
                tv.setText("");
                for (Jokes comment : comments1) {
                    String content = "";
                    content += "ID: " + comment.getId() + "\n";
                    content += "Type: " + comment.getType() + "\n";
                    content += "Setup: " + comment.getSetup() + "\n";
                    content += "Punchline: " + comment.getPunchline() + "\n\n";
                    Log.i(TAG, "onResponse: ");
                    tv.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<Jokes>> call, Throwable t) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                tv.setText(t.getMessage());
            }
        });
    }

    public void postComments() {
        Jokes comments = new Jokes("where does my dick grow", "in your mom!","general");

        Map<String, String> map = new HashMap<>();
        map.put("userId", et.getText().toString());
        map.put("title", "Roboto");

        Call<Jokes> commentsCall = api.createPost(comments);

        commentsCall.enqueue(new Callback<Jokes>() {
            @Override
            public void onResponse(Call<Jokes> call, Response<Jokes> response) {
                if (!response.isSuccessful()) {
                    tv.setText(response.code()+"");
                    return;
                }

                Jokes comments1 = response.body();

                assert comments1 != null;
                String add = "Code: " + response.code() + "\n" + "Id: " + comments1.getId() + "\n" + "setup: " + comments1.getSetup() + "punchline: "+ comments1.getPunchline() + "type: " + comments1.getType();

                tv.setText(add);
            }

            @Override
            public void onFailure(Call<Jokes> call, Throwable t) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                tv.setText(t.getMessage());
            }
        });
    }

    /*public void updatePost() {

        Comments comments = new Comments(null, "rito", null);

        if (et.getText().toString().equals("") || et == null) {
            return;
        }

        //Call<Comments> commentsCall= api.putPost(Integer.parseInt(et.getText().toString()), comments);
        Call<Comments> commentsCall = api.patchPost(Integer.parseInt(et.getText().toString()), comments);

        commentsCall.enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                Comments comments1 = response.body();

                assert comments1 != null;
                String add = "Code: " + response.code() + "\n" + "Id: " + comments1.getId() + "\n" + "user_id: " + comments1.getUserId() + "\n" + "Name: " + comments1.getTitle() + "\n"
                        + "\n" + "Text: " + comments1.getBody();

                tv.setText(add);
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                tv.setText(t.getMessage());
            }
        });

    }

     */
}