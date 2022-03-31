package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {

    public static String TEST_USER_EMAIL = "test@gmail.com";
    public static String TEST_USER_PASSWORD = "password";

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void initializingDatabase(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(!FirebaseTestsHelper.emulatorActivated){
            mAuth.useEmulator("10.0.2.2", 9099);
            FirebaseTestsHelper.emulatorActivated = true;
        }
        mAuth.createUserWithEmailAndPassword(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        /*if(mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }*/
        Intents.init();
    }

    /*@Before
    public void initIntents(){
        Intents.init();
    }/*

    /* See logo */
    @Test
    public void test1seeHouseLogo() {
        onView(withId(R.id.house_logo)).check(matches(isDisplayed()));
    }

    /*@Test
    public void test2activityShowsMainScreenIfConnected() throws ExecutionException, InterruptedException {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Task<AuthResult> t = mAuth.signInWithEmailAndPassword(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        Tasks.await(t);
        testRule.getScenario().recreate();
        testRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        intended(hasComponent(LoginEmail.class.getName()));
        mAuth.signOut();
    }*/


    /*@Test
    public void test3activityRedirectsToLoginIfNotConnected() throws InterruptedException {
        //Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        FirebaseAuth.getInstance().signOut();
        //GoogleSignIn.getClient();
        Thread.sleep(500);
        //testRule.getScenario().recreate();
        ActivityScenario sc = ActivityScenario.launch(MainActivity.class);
        sc.moveToState(Lifecycle.State.RESUMED);
        //testRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        intended(hasComponent(LoginActivity.class.getName()));
        //testRule.getScenario().moveToState(Lifecycle.State.DESTROYED);

    }*/

    @AfterClass
    public static void releaseIntents(){
        Intents.release();
    }
}
