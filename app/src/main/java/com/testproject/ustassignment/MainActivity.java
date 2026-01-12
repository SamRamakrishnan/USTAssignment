package com.testproject.ustassignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";

    private GoogleSignInClient mGoogleSignInClient;
    private Button signInButton;
    private TextView statusText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button);
        statusText = findViewById(R.id.status_text);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("854140022628-165edof06l3hfp3iek068aoa55j2l0f8.apps.googleusercontent.com")  // From google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for cached token and attempt silent authentication
        String cachedToken = prefs.getString(TOKEN_KEY, null);
        if (cachedToken != null) {
            if (isNetworkAvailable()) {
                silentSignIn();
            } else {
                forceLogout("No network available. Logging out.");
            }
        } else {
            showLoginUI();
        }

        signInButton.setOnClickListener(v -> signIn());
    }

    private void silentSignIn() {
        statusText.setText("Authenticating silently...");
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
        if (task.isSuccessful()) {
            handleSignInResult(task);
        } else {
            task.addOnCompleteListener(this, this::handleSignInResult);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken != null) {
                // Cache the token
                prefs.edit().putString(TOKEN_KEY, idToken).apply();
                statusText.setText("Logged in as: " + account.getDisplayName());
                signInButton.setVisibility(View.GONE);
                // Proceed to app's main functionality here
            }
        } catch (ApiException e) {
            Log.w("SignIn", "signInResult:failed code=" + e.getStatusCode());
            forceLogout("Silent authentication failed. Please log in.");
        }
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void forceLogout(String message) {
        // Clear cached token
        prefs.edit().remove(TOKEN_KEY).apply();
        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            showLoginUI();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void showLoginUI() {
        statusText.setText("Please log in.");
        signInButton.setVisibility(View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}