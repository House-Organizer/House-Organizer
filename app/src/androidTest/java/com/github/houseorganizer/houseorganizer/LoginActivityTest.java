package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.is;

import android.content.Intent;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
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

    // Test that when the sign in button is clicked an intent is fired
    @Test
    public void dummyTest() {
        assertThat(2+2, is(4));
    }

    // Firebase Anon only
    @Test
    public void firebaseAnonAuthWorksFirstTime() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class);

        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.discoverButton)).perform(click());
            onView(withId(R.id.loginStatus)).check(matches(withText(R.string.firebaseAnonOk)));
        }
    }

    @Test
    public void firebaseAnonAuthIsRemembered() {
        // TODO
    }

    // Google sign-in only
    @Test
    public void googleAuthWorksFirstTime() {
        // TODO
    }

    @Test
    public void googleAuthIsRemembered() {
        // TODO
    }

    // Firebase anon + google sign-in
    @Test
    public void linkingFirebaseToGoogleWorks() {
        // TODO
    }

    @Test
    public void linkingFirebaseToGoogleIsRemembered() {
        // TODO
    }

}
