package com.example.simpletokenfetchactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_AUTH_REQUIRED = 2;

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_PROFILE).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (available != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "play services not available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "play services available", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "play services not available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error during resolving recoverable error.", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == REQUEST_CODE_AUTH_REQUIRED) {

            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                Toast.makeText(this, "there was an error outside the scope of this demo.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                String token = "";
                try {

                    token = GoogleAuthUtil.getToken(MainActivity.this, accountName, "oauth2:" + Scopes.PROFILE);

                } catch (UserRecoverableAuthException userAuthEx) {
                    startActivityForResult(userAuthEx.getIntent(), REQUEST_CODE_AUTH_REQUIRED);
                } catch (Exception e) {

                }
                return token;
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(MainActivity.this, "this is the token:" + result, Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
            }

        }.execute();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "onConnectionFailed" + result.toString(), Toast.LENGTH_SHORT).show();
        mConnectionResult = result;
        resolveSignInError();
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
            } catch (SendIntentException e) {
                Toast.makeText(this, "Got SendIntentException for startResolutionForResult ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();

    }
}
