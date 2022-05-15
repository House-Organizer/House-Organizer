package com.github.houseorganizer.houseorganizer.panels.login;

import static com.github.houseorganizer.houseorganizer.util.LoginHelpers.inputsEmpty;
import static com.github.houseorganizer.houseorganizer.util.LoginHelpers.isValidEmail;
import static com.github.houseorganizer.houseorganizer.util.LoginHelpers.isValidPassword;
import static com.github.houseorganizer.houseorganizer.util.Util.displayErrorMessage;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegisterEmail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean isEmailAlreadyUsed = false;
    private String email;
    private String password;
    private String confPassword;
    private TextView error_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        initializeVariables();
        findViewById(R.id.reg_email_register_button).setOnClickListener(
                v -> {
                    EspressoIdlingResource.increment();
                    checkIfEmailIsAlreadyUsed();
                    if (inputsEmpty(email, password)) {
                        displayErrorMessage(Util.ErrorType.INPUTS_EMPTY, error_field);
                    } else if (isEmailAlreadyUsed) {
                        displayErrorMessage(Util.ErrorType.EMAIL_USED, error_field);
                        isEmailAlreadyUsed = false;
                    } else if (!isValidEmail(email)) {
                        displayErrorMessage(Util.ErrorType.INVALID_EMAIL, error_field);
                    } else if (!isValidPassword(password, confPassword)) {
                        displayErrorMessage(Util.ErrorType.INVALID_PASSWORD, error_field);
                    } else {
                        signUpWithEmail(v);
                    }
                    EspressoIdlingResource.decrement();
                }
        );
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        isEmailAlreadyUsed = false;
        email = ((EditText) findViewById(R.id.reg_enter_email)).getText().toString();
        password = ((EditText) findViewById(R.id.reg_enter_password)).getText().toString();
        confPassword = ((EditText) findViewById(R.id.reg_confirm_password)).getText().toString();
        error_field = findViewById(R.id.reg_email_error_message);
    }

    // Returns true if email address is in use.
    private void checkIfEmailIsAlreadyUsed() {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (!Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty()) {
                            isEmailAlreadyUsed = true;
                        }
                    }
                });
    }

    private void sendEmailVerif(FirebaseUser user, Task<AuthResult> task) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task1 -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterEmail.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        logAndToast(getString(R.string.tag_register_email), "sendEmailVerification", task.getException(),
                                RegisterEmail.this, "Failed to send verification email.");
                    }
                });
    }

    public void signUpWithEmail(View v) {
        String email = ((EditText) findViewById(R.id.reg_enter_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.reg_enter_password)).getText().toString();

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
                        ((TextView) findViewById(R.id.reg_email_error_message)).setText(R.string.reg_email_auth_failed);
                    }
                });
    }
}