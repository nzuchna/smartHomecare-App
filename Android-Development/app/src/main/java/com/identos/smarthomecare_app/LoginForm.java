package com.identos.smarthomecare_app;

import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by noahz on 01.03.2018.
 */

public final class LoginForm {
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "LoginForm";

    public void run() throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("search", "Jurassic Park")
                .build();
        Request request = new Request.Builder()
                .url("https://en.wikipedia.org/w/index.php")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Log.i(TAG, response.body().string());
        }
    }

    public static void main(String... args) throws Exception {
        new LoginForm().run();
    }
}
