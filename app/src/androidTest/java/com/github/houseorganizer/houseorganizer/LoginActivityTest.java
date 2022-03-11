package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> testRule = new ActivityScenarioRule<>(LoginActivity.class);

    /* Sign-in button */
    @Test
    public void signInButtonIsDisplayed() {
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
    }

    @Test
    public void signInButtonIsEnabled() {
        onView(withId(R.id.google_sign_in_button)).check(matches(isEnabled()));
    }

    // Test that when the sign in button is clicked an intent is fired
    @Test
    public void clickSignInButtonFiresIntent() {
        Intents.init();
        onView(withId(R.id.google_sign_in_button)).perform(click());
        intended(toPackage("com.github.houseorganizer.houseorganizer"));
        Intents.release();
    }

    /* Discover button */
    @Test
    public void discoverButtonIsDisplayed() {
        onView(withId(R.id.discoverButton)).check(matches(isDisplayed()));
    }

    @Test
    public void discoverButtonIsEnabled() {
        onView(withId(R.id.discoverButton)).check(matches(isEnabled()));
    }

}