package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.LoginActivity;
import com.github.houseorganizer.houseorganizer.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

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
        if(!TestsHelper.emulatorActivated){
            mAuth.useEmulator("10.0.2.2", 9099);
            TestsHelper.emulatorActivated = true;
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
