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
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_EMAILS;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.createFirebaseTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.deleteTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.signInTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.test4Input;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.test8Input;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.validPassword;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.github.houseorganizer.houseorganizer.login.VerifyEmail;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;


@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {

    private static FirebaseAuth auth;
    private static final String email1 = "user_register1@test.com", email2 = "user_register2@test.com";

    @Rule
    public ActivityScenarioRule<RegisterEmail> regRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        auth = FirebaseAuth.getInstance();

        createFirebaseTestUserWithCredentials(email2, validPassword);
    }

    @AfterClass
    public static void end() throws ExecutionException, InterruptedException {
        deleteTestUserWithCredentials(email1, validPassword);

        signInTestUserWithCredentials(email2, validPassword);
        deleteTestUserWithCredentials(email2, validPassword);
    }

    @Test
    public void signUpWithEmailWorksWithCorrectInputs() {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(email1), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();
    }

    @Test
    public void signUpWithEmailShowsInputsEmptyErrorWhenInputsEmpty() throws InterruptedException {
        onView(withId(R.id.reg_enter_email)).perform(click(), typeText(test4Input), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(250);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.inputs_empty)));
    }

    @Test
    public void signUpWithEmailShowsInvalidEmailErrorWithInvalidInput() throws InterruptedException {
        onView(withId(R.id.reg_enter_password)).perform(click(), typeText(test8Input), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(250);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.email_not_valid)));
    }

    @Test
    public void signUpWithEmailShowsInvalidPasswordErrorWithInvalidInput() throws InterruptedException {
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(TEST_USERS_EMAILS[1]), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(click(), typeText(test8Input), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(250);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.password_not_valid)));
    }

    @Test
    public void signUpWithEmailShowsEmailUsedErrorWithAlreadyUsedEmail() throws InterruptedException {
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(email2), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(250);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.email_already_used)));
    }
}
