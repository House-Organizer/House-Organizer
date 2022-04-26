package com.github.houseorganizer.houseorganizer;

import static android.provider.Settings.Global.getString;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class GroceriesActivityTest {

    private static FirebaseAuth auth;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){auth.signOut();}

    @Before
    public void openActivity() throws InterruptedException {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nav_bar_cart)).perform(click());
        Thread.sleep(300);
    }

    @Test
    public void addTaskButtonAvailable(){
        onView(withId(R.id.groceries_add)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_add)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_add)).check(matches(isClickable()));
    }

    @Test
    public void removePickedUpAvailable(){
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isClickable()));
    }

    @Test
    public void shopListHasCorrectNumberOfItems() {
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void addingItemShowsNewItem() throws InterruptedException {
        onView(withId(R.id.groceries_add)).perform(click());
        onView(withId(R.id.edit_text_name)).perform(typeText("item"));
        onView(withId(R.id.edit_text_quantity)).perform(typeText("2" ));
        onView(withId(R.id.edit_text_unit)).perform(typeText("kg"));
        onView(withText(R.string.add)).perform(click());
        // Checking item exists in the view
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(2)));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("item")))));

        onView(withId(R.id.groceries_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelperActions.clickChildViewWithId(R.id.delete_item_button)));
    }


}
