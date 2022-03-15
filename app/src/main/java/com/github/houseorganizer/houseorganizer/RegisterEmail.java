package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterEmail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String TAG = "RegisterEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.reg_email_register_button).setOnClickListener(
                v -> {
                    if (isValidEmail() && isValidPassword()) signUpWithEmail(v);
                }
        );
    }

    private boolean isValidEmail() {
        EditText email_field = findViewById(R.id.reg_enter_email);
        String email = email_field.getText().toString();

        // Regex to check valid email.
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);

        if (m.matches()) {
            return true;
        } else {
            TextView error_message = findViewById(R.id.reg_email_error_message);
            error_message.setText(R.string.email_not_valid);
            return false;
        }
    }

    private boolean isValidPassword() {
        EditText password_field = findViewById(R.id.reg_enter_password);
        String password = password_field.getText().toString();
        EditText password2_field = findViewById(R.id.reg_confirm_password);
        String password2 = password2_field.getText().toString();

        boolean samePasswords = password.equals(password2);

        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);

        if (m.matches() && samePasswords) {
            return true;
        } else {
            TextView error_message = findViewById(R.id.reg_email_error_message);
            error_message.setText(R.string.password_not_valid);
            return false;
        }
    }

    public void signUpWithEmail(View v) {
        EditText email_field = findViewById(R.id.reg_enter_email);
        EditText password_field = findViewById(R.id.reg_enter_password);
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();
        TextView error_message = findViewById(R.id.reg_email_error_message);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        startActivity(new Intent(RegisterEmail.this, MainScreenActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        error_message.setText(R.string.reg_email_auth_failed);
                    }
                });
    }
}