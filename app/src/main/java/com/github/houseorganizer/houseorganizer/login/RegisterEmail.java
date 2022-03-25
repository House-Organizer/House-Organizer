package com.github.houseorganizer.houseorganizer.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterEmail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean isEmailAlreadyUsed = false;

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
        TextView error_message = findViewById(R.id.reg_email_error_message);

        // Regex to check valid email.
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);

        checkIfEmailIsAlreadyUsed(email);

        if (m.matches() && !isEmailAlreadyUsed) {
            return true;
        } else if (isEmailAlreadyUsed) {
            error_message.setText(R.string.email_already_used);
            return false;
        } else {
            error_message.setText(R.string.email_not_valid);
            return false;
        }
    }

    // Returns true if email address is in use.
    private void checkIfEmailIsAlreadyUsed(String emailAddress) {
        mAuth.fetchSignInMethodsForEmail(emailAddress)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (!Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty()) {
                            isEmailAlreadyUsed = true;
                        }
                    }
                });
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
                + "(?=.*[@#$%^&+=_.-])"
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

    private void sendEmailVerif(FirebaseUser user, Task<AuthResult> task) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task1 -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterEmail.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        LoginActivity.logAndToast(Arrays.asList(getString(R.string.tag_register_email),
                                "sendEmailVerification"), task.getException(),
                                RegisterEmail.this, "Failed to send verification email.");
                    }
                });
    }

    public void signUpWithEmail(View v) {
        String email = ((EditText) findViewById(R.id.log_enter_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.log_enter_password)).getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(getString(R.string.tag_register_email), "createUserWithEmail:success");
                        if (user != null) {
                            sendEmailVerif(user, task);
                        }
                        startActivity(new Intent(RegisterEmail.this, VerifyEmail.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(getString(R.string.tag_register_email), "createUserWithEmail:failure", task.getException());
                        ((TextView) findViewById(R.id.log_email_error_message)).setText(R.string.reg_email_auth_failed);
                    }
                });
    }
}