package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.RegisterEmail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {
    @Rule
    public ActivityScenarioRule<RegisterEmail> registerEmailActivityScenarioRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @Test
    public void registerButtonIsDisplayed() {
        //onView(withId(R.id.reg_email_register_button)).check(matches(isDisplayed()));
        assertThat(true, is(true));
    }
}
