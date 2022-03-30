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

import com.github.houseorganizer.houseorganizer.login.LoginEmail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginEmailTest {
    @Rule
    public ActivityScenarioRule<LoginEmail> regRule =
            new ActivityScenarioRule<>(LoginEmail.class);

    @Test
    public void registerButtonIsDisplayed() {
        onView(withId(R.id.log_email_register_button)).check(matches(isDisplayed()));
    }

    @Test
    public void signInButtonIsDisplayed() {
        onView(withId(R.id.log_email_signin_button)).check(matches(isDisplayed()));
    }

    @Test
    public void isValidEmailShowsRightErrorWhenFalse() throws InterruptedException {
        // INPUTS_EMPTY
        onView(withId(R.id.log_enter_email)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.inputs_empty)));

        // Auth Failed
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.log_email_auth_failed)));
    }
}
