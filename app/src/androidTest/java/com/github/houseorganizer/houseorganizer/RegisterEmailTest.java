package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<RegisterEmail> regRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @Test
    public void isValidEmailShowsRightErrorWhenFalse() throws InterruptedException {
        // INPUTS_EMPTY
        onView(withId(R.id.reg_enter_email)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.inputs_empty)));

        // INVALID_EMAIL
        onView(withId(R.id.reg_enter_password)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.email_not_valid)));

        // INVALID_PASSWORD
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(FirebaseTestsHelper.TEST_USERS_EMAILS[0]), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.password_not_valid)));
    }
}
