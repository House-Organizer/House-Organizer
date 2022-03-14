package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class LoginEmail extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;

    private final String TAG = "LoginEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.emailactivity_signin_button).setOnClickListener(this);
        findViewById(R.id.email_activity_singup_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailactivity_signin_button:
                signInWithEmail(v);
                break;
            case R.id.email_activity_singup_button:
                signUpWithEmail(v);
                break;
        }
    }

    public void signInWithEmail(View v) {
        EditText email_field = findViewById(R.id.enter_email);
        EditText password_field = findViewById(R.id.enter_password);
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();
        TextView error_message = findViewById(R.id.login_email_error_message);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        startActivity(new Intent(LoginEmail.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "signInWithGoogleCredential:failure");
                        error_message.setText("Email or password is wrong.");
                    }
                });
    }

    public void signUpWithEmail(View v) {
        EditText email_field = findViewById(R.id.enter_email);
        EditText password_field = findViewById(R.id.enter_password);
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();
        TextView error_message = findViewById(R.id.login_email_error_message);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        startActivity(new Intent(LoginEmail.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        error_message.setText("Authentication failed.");
                    }
                });
    }
}