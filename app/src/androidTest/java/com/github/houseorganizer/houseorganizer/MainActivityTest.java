package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    /* See logo */
    @Test
    public void seeHouseLogo() {
        onView(withId(R.id.house_logo)).check(matches(isDisplayed()));
    }

    //TODO This does not work.
    /*
    @Test
    public void MainActivitySendsIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);

        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
            Intents.init();
            intended(toPackage("com.github.houseorganizer.houseorganizer"));
            Intents.release();
        }
    }*/
}
