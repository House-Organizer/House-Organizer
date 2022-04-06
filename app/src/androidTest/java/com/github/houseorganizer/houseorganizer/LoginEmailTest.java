package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.createFirebaseTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.deleteTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.test4Input;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.validPassword;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.login.VerifyEmail;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;


@RunWith(AndroidJUnit4.class)
public class LoginEmailTest {

    private static FirebaseAuth auth;
    private static final String email = "user_login1@test.com";

    @Rule
    public ActivityScenarioRule<LoginEmail> logRule =
            new ActivityScenarioRule<>(LoginEmail.class);

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        auth = FirebaseAuth.getInstance();

        createFirebaseTestUserWithCredentials(email, validPassword);
    }

    @AfterClass
    public static void end() throws ExecutionException, InterruptedException {
        deleteTestUserWithCredentials(email, validPassword);
    }

    @Test
    public void signInWithEmailWorksWithCorrectInputs() {
        Intents.init();
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();
    }


    @Test
    public void signInWithEmailShowsInputsEmptyErrorWithEmptyInputs() throws InterruptedException {
        enterInputsAndClickSignIn(test4Input, "");
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.inputs_empty)));
    }

    @Test
    public void signInWithEmailShowsAuthFailedErrorWithIncorrectInputs() throws InterruptedException {
        enterInputsAndClickSignIn(test4Input, test4Input);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.log_email_auth_failed)));
    }

    private void enterInputsAndClickSignIn(String email, String password) throws InterruptedException {
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText(password), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(250);
    }
}
