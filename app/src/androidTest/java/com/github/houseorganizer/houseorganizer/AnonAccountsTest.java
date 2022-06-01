package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.startAuthEmulator;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.startFirestoreEmulator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.login.LoginActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class AnonAccountsTest {

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        startAuthEmulator();
        startFirestoreEmulator();

        FirebaseTestsHelper.setUpFirebase();
        FirebaseAuth.getInstance().signOut();
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> loginRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void discoverButtonCreatesAnonUserWithCorrectEmail() throws ExecutionException, InterruptedException {
        assertNull(FirebaseAuth.getInstance().getCurrentUser());

        Intents.init();
        onView(withId(R.id.discoverButton)).perform(click());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assertNotNull(user);

        Thread.sleep(300);
        assertEquals(user.getUid().hashCode() + "@house-org.com", user.getEmail());

        Intents.release();
        Tasks.await(FirebaseAuth.getInstance().getCurrentUser().delete());
    }
}
