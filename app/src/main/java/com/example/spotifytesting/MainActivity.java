package com.example.spotifytesting;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "73b777ffcf6545a78634deabf8d7b94f";
    private static final String REDIRECT_URI = "weihsuan://callback";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private String mAccessCode;
    private Call mCall;

    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.
    }

    private void connected() {
        // Then we will write some more code here.
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void onGetUserProfileClicked(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization","Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setResponse("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    setResponse(jsonObject.toString(3));
                } catch (JSONException e) {
                    setResponse("Failed to parse data: " + e);
                }
            }
        });
    }

    public void onRequestCodeClicked(View view) {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.CODE);
        AuthenticationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void onRequestTokenClicked(View view) {
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);
        AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email"})
                .setCampaign("your-campaign-token")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            updateTokenView();
        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            updateCodeView();
        }
    }

    private void setResponse(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView responseView = (TextView) findViewById(R.id.response_text_view);
                responseView.setText(text);
            }
        });
    }

    private void updateTokenView() {
        final TextView tokenView = (TextView) findViewById(R.id.token_text_view);
        tokenView.setText(getString(R.string.token, mAccessToken));
    }

    private void updateCodeView() {
        final TextView codeView = (TextView) findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, mAccessCode));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }
}
