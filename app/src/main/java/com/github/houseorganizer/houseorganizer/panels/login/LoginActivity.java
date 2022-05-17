package com.github.houseorganizer.houseorganizer.panels.login;

import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private ImageButton facebookLogin;
    private CallbackManager mCallbackManager;
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

        facebookLogin = findViewById(R.id.facebookLogInButton);
        facebookLogin.setOnClickListener(v -> initializeFacebookLogIn());

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

    private void initializeFacebookLogIn() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(getString(R.string.tag_login_activity), "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(getString(R.string.tag_login_activity), "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(getString(R.string.tag_login_activity), "facebook:onError", error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(getString(R.string.tag_login_activity), "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(getString(R.string.tag_login_activity), "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(getString(R.string.tag_login_activity), "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
        intent.putExtra("LoadHouse", true);
        startActivity(intent);
    }

    /**
     *  Sets up the discover and Google Sign-In buttons for
     *  the user to authenticate
     */
    private void setUpSignInButtons() {
        findViewById(R.id.google_sign_in_button).setOnClickListener(
                v -> googleSignInResultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent())));

        findViewById(R.id.discoverButton).setOnClickListener(v -> signInAnonymously());
    }

    private void manageTask(Task<AuthResult> task, String func) {
        if (task.isSuccessful()) {
            // If sign in succeeds launch MainScreenActivity
            Log.d(getString(R.string.tag_login_activity), func + ":success");
            Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
            intent.putExtra("LoadHouse", true);
            startActivity(intent);
            finish();
        } else {
            // If sign in fails, display a message to the user.
            logAndToast(getString(R.string.tag_login_activity), func + ":failure", task.getException(),
                    LoginActivity.this, "Authentication failed.");
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this,
                        task -> manageTask(task, "signInAnonymously")
                );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                task -> manageTask(task, "firebaseAuthWithGoogle")
        );
    }

    @Override
    public void onBackPressed() {
        // Leave the app instead of going to MainActivity
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}