package com.github.houseorganizer.houseorganizer.login;


import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.activity.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.cloudServerID))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.email_signin_button).setOnClickListener(
                v -> startActivity(new Intent(this, LoginEmail.class))
        );
        findViewById(R.id.google_sign_in_button).setOnClickListener(
                v -> googleSignInResultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent()))
        );

      // If a sign out has been requested, sign out the current user
        if(getIntent().hasExtra(getString(R.string.signout_intent))){
            mAuth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, l ->
                    setUpSignInButtons());
        } else{
            setUpSignInButtons();
        }
    }

    ActivityResultLauncher<Intent> googleSignInResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    Exception exception = task.getException();
                    if (task.isSuccessful()) {
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            assert account != null;
                            Log.d(getString(R.string.tag_login_activity), "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Google Sign-in failed, update UI appropriately
                            Log.w(getString(R.string.tag_login_activity), "Google sign-in failed", e);
                        }
                    } else {
                        if (exception != null) {
                            Log.w(getString(R.string.tag_login_activity), exception.toString());
                        }
                    }
                }
            }
    );

    /**
     *  Sets up the discover and Google Sign-In buttons for
     *  the user to authenticate
     */
    private void setUpSignInButtons(){
        findViewById(R.id.google_sign_in_button).setOnClickListener(
                v -> googleSignInResultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent())));

        findViewById(R.id.discoverButton).setOnClickListener(v -> signInAnonymously());
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(getString(R.string.tag_login_activity), "signInAnonymously:success");
                        startActivity(new Intent(LoginActivity.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        logAndToast(Arrays.asList(getString(R.string.tag_login_activity),
                                "signInAnonymously:failure"), task.getException(),
                                LoginActivity.this, "Authentication failed.");
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(getString(R.string.tag_login_activity), "firebaseAuthWithGoogle:success");
                        startActivity(new Intent(LoginActivity.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        logAndToast(Arrays.asList(getString(R.string.tag_login_activity),
                                "firebaseAuthWithGoogle:failure"), task.getException(),
                                LoginActivity.this, "Authentication failed.");
                    }
                });
    }
}