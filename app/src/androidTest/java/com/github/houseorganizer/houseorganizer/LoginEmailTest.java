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
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_PWD;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.VALID_PASSWORD_FOR_APP;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.createFirebaseTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.deleteTestUser;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.startAuthEmulator;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.login.VerifyEmail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;


@RunWith(AndroidJUnit4.class)
public class LoginEmailTest {

    private static final String email1 = "user_login1@test.com", email2 = "user_login2@test.com";

    @Rule
    public ActivityScenarioRule<LoginEmail> logRule =
            new ActivityScenarioRule<>(LoginEmail.class);

    @BeforeClass
    public static void start() {
        startAuthEmulator();
    }

    @AfterClass
    public static void end() {}

    @Test
    public void signInWithEmailWorksWithCorrectInputs() {
        createFirebaseTestUserWithCredentials(email1, VALID_PASSWORD_FOR_APP);

        Intents.init();
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText(email1), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText(VALID_PASSWORD_FOR_APP), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();

        deleteTestUser();
    }


    @Test
    public void signInWithEmailShowsInputsEmptyErrorWithEmptyInputs() throws InterruptedException {
        enterInputsAndClickSignIn(email2, "");
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.inputs_empty)));
    }

    @Test
    public void signInWithEmailShowsAuthFailedErrorWithIncorrectInputs() throws InterruptedException {
        enterInputsAndClickSignIn(email2, TEST_USERS_PWD[0]);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.log_email_auth_failed)));
    }

    private void enterInputsAndClickSignIn(String email, String password) throws InterruptedException {
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText(password), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(250);
    }
}
