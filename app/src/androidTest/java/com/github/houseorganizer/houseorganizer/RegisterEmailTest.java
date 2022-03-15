package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {
    @Rule
    public ActivityScenarioRule<RegisterEmail> registerEmailActivityScenarioRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @Test
    public void registerButtonIsEnabled() {
        onView(withId(R.id.reg_email_register_button)).check(matches(isEnabled()));
    }

    @Test
    public void registerButtonIsDisplayed() {
        onView(withId(R.id.reg_email_register_button)).check(matches(isDisplayed()));
    }

    /*@Test
    public void invalidPasswordShowsErrorMessage() {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(typeText("example@gmail.com"));
        onView(withId(R.id.reg_enter_password)).perform(typeText("Password"));
        onView(withId(R.id.reg_confirm_password)).perform(typeText("Password"));
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Intents.release();
    }

    @Test
    public void invalidEmailShowsErrorMessage() {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(typeText("example@"));
        onView(withId(R.id.reg_enter_password)).perform(typeText("P@ssw0rd"));
        onView(withId(R.id.reg_confirm_password)).perform(typeText("P@ssw0rd"));
        onView(withId(R.id.reg_email_register_button)).perform(click());
        onView(withId(R.id.reg_email_register_button)).check(matches(withText(R.string.email_not_valid)));
        Intents.release();
    }


    @Test
    public void signUpWithValidCredentialsFiresIntent() {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(typeText("example@gmail.com"));
        onView(withId(R.id.reg_enter_password)).perform(typeText("P@ssw0rd"));
        onView(withId(R.id.reg_confirm_password)).perform(typeText("P@ssw0rd"));
        onView(withId(R.id.reg_email_register_button)).perform(click());
        intended(toPackage("com.github.houseorganizer.houseorganizer"));
        Intents.release();
    }*/
}
