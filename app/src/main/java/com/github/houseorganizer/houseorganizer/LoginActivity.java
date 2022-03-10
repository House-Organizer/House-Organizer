package com.github.houseorganizer.houseorganizer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    // This is used to register the Google sign-in activity triggered
    // when no user is signed in
    ActivityResultLauncher<Intent> googleSignInResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Log.w("LoginActivity", "Google sign in failed", e);
                        displayFailedSignIn();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.cloudServerID))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();





    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account == null){
            findViewById(R.id.sign_in_button).setOnClickListener(this);
            findViewById(R.id.discoverButton).setOnClickListener(this);
        }else{
            startMainActivity();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.discoverButton:
                anonSignIn();
                break;
        }
    }

    /**
     * Intermediary function to call for the Google sign-in
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInResultLauncher.launch(signInIntent);
    }

    /**
     * Intermediary function to create an anonymous Firebase account.
     *
     * Jumps directly to the main activity if there's already a registered
     * anonymous account.
     */
    private void anonSignIn() {
        if (mAuth.getCurrentUser() != null) {
            startMainActivity();
        }

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LoginActivity", "signInAnonymously:success");
                            ///FirebaseUser user = mAuth.getCurrentUser();
                            startMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginActivity", "signInAnonymously:failure", task.getException());
                            displayFailedSignIn();
                        }
                    }
                });
    }

    /**
     * Main function allowing the authentication to Firebase
     * @param idToken the id of the connected Google account
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        if (mAuth.getCurrentUser() != null) {
            firebaseLinkWithGoogle(credential);
            return;
        }

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LoginActivity", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        startMainActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithCredential:failure", task.getException());
                        displayFailedSignIn();
                    }
                });
    }

    /**
     * Links current Firebase and Google accounts.
     *
     * This allows previous anonymous users to keep their
     * initial Firebase data after logging in with Google.
     *
     * @param credential the credential of the connected Google account
     */
    private void firebaseLinkWithGoogle(AuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            startMainActivity();
                        } else {
                            Log.w("LoginActivity", "linkWithCredential:failure", task.getException());
                            displayFailedSignIn();
                        }
                    }
                });
    }

    /**
     * Launches the main activity once the user is logged in
     */
    private void startMainActivity(){
        // TODO : Does nothing for now, will be linked to the new main activity
    }

    /**
     * Shows the user when the sign-in failed
     */
    private void displayFailedSignIn(){
        TextView text = findViewById(R.id.loginStatus);
        text.setText(R.string.signInFailed);
    }

}