package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public ActivityScenarioRule<SettingsActivity> testRule = new ActivityScenarioRule<>(SettingsActivity.class);

    @Test
    public void clearStorageWorks() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocalStorage.clearOfflineStorage(cx);
        LocalStorage.writeTxtToFile(cx, "TEST.txt", "TEST");

        File directory = cx.getFilesDir();
        assertEquals(1, directory.listFiles().length);
        onView(withText(R.string.clear_local_storage)).perform(click());
        assertEquals(0, directory.listFiles().length);
    }

}