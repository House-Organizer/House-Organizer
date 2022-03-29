package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.LoginActivity;
import com.github.houseorganizer.houseorganizer.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public static String TEST_USER_EMAIL = "test@gmail.com";
    public static String TEST_USER_PASSWORD = "password";

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void initializingDatabase(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", 9099);
        if(mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }
        mAuth.createUserWithEmailAndPassword(TEST_USER_EMAIL, TEST_USER_PASSWORD);
    }

    /* See logo */
    @Test
    public void seeHouseLogo() {
        onView(withId(R.id.house_logo)).check(matches(isDisplayed()));
    }

    //TODO This does not work.

    @Test
    public void activityShowsMainScreenIfConnected(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Intents.init();
        testRule.getScenario().recreate();
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
        mAuth.signOut();
    }

    @Test
    public void activityRedirectsToLoginIfNotConnected() {
        //Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        FirebaseAuth.getInstance().signOut();
        Intents.init();

        testRule.getScenario().moveToState(Lifecycle.State.CREATED);//recreate().moveToState(Lifecycle.State.CREATED);
        testRule.getScenario().moveToState(Lifecycle.State.STARTED);
        testRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        intended(toPackage("com.github.houseorganizer.houseorganizer.login"));
        testRule.getScenario().moveToState(Lifecycle.State.DESTROYED);
        Intents.release();

    }
}
