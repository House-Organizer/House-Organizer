package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {

    /* See list of houses */
    @Test
    public void seeHousesList() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.housesView))
                    .check(matches(hasDescendant(withText("House 1"))))
                    .check(matches(hasDescendant(withText("House 2"))))
                    .check(matches(hasDescendant(withText("House 3"))))
                    .check(matches(hasDescendant(withText("House 4"))));
        }
    }

    /* House selected */
    @Test
    public void selectHouse() {
        Intents.init();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.housesView))
                    .perform((ViewAction) RecyclerViewActions.actionOnItemAtPosition(3, click()));
            intended(toPackage("com.github.houseorganizer.houseorganizer"));
        }

        Intents.release();
    }
}
