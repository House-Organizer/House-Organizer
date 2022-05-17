package com.github.houseorganizer.houseorganizer;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(AndroidJUnit4.class)
public class ShopListAdapterTest {

    @Rule
    public ActivityScenarioRule<MainScreenActivity> testRule = new ActivityScenarioRule<>(MainScreenActivity.class);



}
