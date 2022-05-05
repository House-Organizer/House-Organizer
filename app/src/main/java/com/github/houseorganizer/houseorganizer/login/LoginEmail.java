package com.github.houseorganizer.houseorganizer.login;

import static com.github.houseorganizer.houseorganizer.util.LoginHelpers.inputsEmpty;
import static com.github.houseorganizer.houseorganizer.util.Util.displayErrorMessage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginEmail extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.log_email_signin_button).setOnClickListener(
                v -> {
                    String email = ((EditText) findViewById(R.id.log_enter_email)).getText().toString();
                    String password = ((EditText) findViewById(R.id.log_enter_password)).getText().toString();
                    TextView error_message = findViewById(R.id.log_email_error_message);
                    if (!inputsEmpty(email, password)) {
                        signInWithEmail(v);
                    } else {
                        displayErrorMessage(Util.ErrorType.INPUTS_EMPTY, error_message);
                    }
                }
        );

        findViewById(R.id.log_email_register_button).setOnClickListener(
                v -> startActivity(new Intent(LoginEmail.this, RegisterEmail.class))
        );
    }

    public void signInWithEmail(View v) {
        String email = ((EditText) findViewById(R.id.log_enter_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.log_enter_password)).getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(getString(R.string.tag_login_email), "signInWithEmail:success");
                        if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {
                            Intent intent = new Intent(LoginEmail.this, MainScreenActivity.class);
                            intent.putExtra("LoadHouse", true);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(LoginEmail.this, VerifyEmail.class));
                        }
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(getString(R.string.tag_login_email), "signInWithEmail:failure");
                        ((TextView) findViewById(R.id.log_email_error_message)).setText(R.string.log_email_auth_failed);
                    }
                });
    }
}