package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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

    // Test that when the button is clicked an intent is fired and the intent contains the username
    @Test
    public void clickButton() {
        Intents.init();

        onView(withId(R.id.mainName)).perform(replaceText("Sandra"));
        onView(withId(R.id.mainGoButton)).perform(click());

        intended(toPackage("com.github.houseorganizer.houseorganizer"));
        intended(hasExtra("com.github.houseorganizer.houseorganizer.MESSAGE", "Sandra"));

        Intents.release();
    }
}
