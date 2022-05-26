package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.login.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> testRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intents.init();
    }

    @After
    public void closeIntents() {
        Intents.release();
    }

    /* Google sign-in button */
    @Test
    public void googleSignInButtonIsDisplayed() {
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
    }

    @Test
    public void googleSignInButtonIsEnabled() {
        onView(withId(R.id.google_sign_in_button)).check(matches(isEnabled()));
    }

    /* Facebook sign-in button */
    @Test
    public void facebookSignInButtonIsDisplayed() {
        onView(withId(R.id.facebookLogInButton)).check(matches(isDisplayed()));
    }

    @Test
    public void facebookSignInButtonIsEnabled() {
        onView(withId(R.id.facebookLogInButton)).check(matches(isEnabled()));
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

    @Test
    public void backPressLeavesApp() {
        // Closing the app throws NoActivityResumedException, so we make the test fail if nothing was thrown
        try {
            pressBack();
            fail("Should have thrown NoActivityResumedException");
        } catch (NoActivityResumedException expected) { }
    }
}
