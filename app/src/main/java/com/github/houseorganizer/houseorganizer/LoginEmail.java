package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

        findViewById(R.id.log_email_signin_button).setOnClickListener(this);
        findViewById(R.id.log_email_register_button).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.log_email_signin_button:
                if (inputsNotEmpty()) signInWithEmail(v);
                break;
            case R.id.log_email_register_button:
                startActivity(new Intent(LoginEmail.this, RegisterEmail.class));
                break;
        }
    }

    private boolean inputsNotEmpty() {
        EditText email_field = findViewById(R.id.log_enter_email);
        EditText password_field = findViewById(R.id.log_enter_password);
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();
        TextView error_message = findViewById(R.id.log_email_error_message);

        if (email.isEmpty() || password.isEmpty()) {
            error_message.setText(R.string.inputs_not_empty);
            return false;
        }

        return true;
    }

    public void signInWithEmail(View v) {
        EditText email_field = findViewById(R.id.log_enter_email);
        EditText password_field = findViewById(R.id.log_enter_password);
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();
        TextView error_message = findViewById(R.id.log_email_error_message);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        startActivity(new Intent(LoginEmail.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "signInWithEmail:failure");
                        error_message.setText(R.string.log_email_auth_failed);
                    }
                });
    }
}