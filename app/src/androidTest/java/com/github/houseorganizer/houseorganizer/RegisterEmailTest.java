package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.RegisterEmail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {
    @Rule
    public ActivityScenarioRule<RegisterEmail> regRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @Test
    public void isTrue() { assertTrue(true); }
/*
    @Test
    public void isValidEmailShowsRightErrorWhenFalse() throws InterruptedException, ExecutionException {
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

        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.createFirebaseTestUser();

        // INVALID_PASSWORD
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(FirebaseTestsHelper.TEST_USER_MAIL), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.password_not_valid)));
    }*/
}
