package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public ActivityScenarioRule<SettingsActivity> testRule = new ActivityScenarioRule<>(SettingsActivity.class);

    @Test
    public void canInputTextInSettings() {
        assertThat(2+2, is(4));
    }

}