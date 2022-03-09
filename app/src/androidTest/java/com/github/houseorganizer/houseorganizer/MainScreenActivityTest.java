package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainScreenActivityTest {
    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    // House selection button
    @Test
    public void houseSelectionButtonIsEnabled() {
        onView(withId(R.id.house_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void houseSelectionButtonIsDisplayed() {
        onView(withId(R.id.house_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void houseSelectionButtonIsClickable() {
        onView(withId(R.id.house_imageButton)).check(matches(isClickable()));
    }

    @Test
    public void houseSelectionButtonDisplaysItHasBeenClicked() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.last_button_activated)).check(matches(withText("House button pressed")));
    }

    // Settings button
    @Test
    public void settingsButtonIsEnabled() {
        onView(withId(R.id.settings_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void settingsButtonIsDisplayed() {
        onView(withId(R.id.settings_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void settingsButtonIsClickable() {
        onView(withId(R.id.settings_imageButton)).check(matches(isClickable()));
    }

    @Test
    public void settingsButtonDisplaysItHasBeenClicked() {
        onView(withId(R.id.settings_imageButton)).perform(click());
        onView(withId(R.id.last_button_activated)).check(matches(withText("Settings button pressed")));
    }

    // Info button
    @Test
    public void infoButtonIsEnabled() {
        onView(withId(R.id.info_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void infoButtonIsDisplayed() {
        onView(withId(R.id.info_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void infoButtonIsClickable() {
        onView(withId(R.id.info_imageButton)).check(matches(isClickable()));
    }

    @Test
    public void infoButtonDisplaysItHasBeenClicked() {
        onView(withId(R.id.info_imageButton)).perform(click());
        onView(withId(R.id.last_button_activated)).check(matches(withText("Info button pressed")));
    }

    // Calendar upcoming view
    @Test
    public void calendarUpcomingIsEnabled() {
        onView(withId(R.id.calendar_upcoming)).check(matches(isEnabled()));
    }

    @Test
    public void calendarUpcomingIsDisplayed() {
        onView(withId(R.id.calendar_upcoming)).check(matches(isDisplayed()));
    }

    // Used in order to access RecyclerView items
    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    @Test
    public void calendarUpcomingEventDisplayed() {
        onView(withId(R.id.calendar_upcoming)).check(matches(atPosition(0, isDisplayed())));
    }

    // Calendar monthly view
    @Test
    public void calendarMonthlyIsEnabled() {
        onView(withId(R.id.calendar_monthly)).check(matches(isEnabled()));
    }

    @Test
    public void calendarMonthlyIsNotDisplayed() {
        onView(withId(R.id.calendar_monthly)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    // Calendar weekly view
    @Test
    public void calendarWeeklyIsEnabled() {
        onView(withId(R.id.calendar_weekly)).check(matches(isEnabled()));
    }

    @Test
    public void calendarWeeklyIsNotDisplayed() {
        onView(withId(R.id.calendar_weekly)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void calendarViewRotatesCorrectly() {
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar_upcoming)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.calendar_monthly)).check(matches(isDisplayed()));
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar_monthly)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.calendar_weekly)).check(matches(isDisplayed()));
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar_weekly)).check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.calendar_upcoming)).check(matches(isDisplayed()));
    }

    // TODO : Add more meaningful tests for each row in the RecyclerViews (no idea how to do it)

}
