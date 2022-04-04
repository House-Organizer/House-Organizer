package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_EMAILS;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.github.houseorganizer.houseorganizer.login.VerifyEmail;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;


@RunWith(AndroidJUnit4.class)
public class LoginEmailTest {

    private static FirebaseAuth auth;

    @Rule
    public ActivityScenarioRule<LoginEmail> logRule =
            new ActivityScenarioRule<>(LoginEmail.class);

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        auth = FirebaseAuth.getInstance();

        Task<AuthResult> t = auth.createUserWithEmailAndPassword("user_login1@test.com", "A3@ef678!");
        Tasks.await(t);
        auth.signOut();
    }

    @AfterClass
    public static void end() throws ExecutionException, InterruptedException {
        try {
            Task<Void> t = auth.getCurrentUser().delete();
            Tasks.await(t);
        } catch (Error ignored) {}
    }

    @Test
    public void signInWithEmailWorksWithCorrectInputs() {
        Intents.init();
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText("user_login1@test.com"), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText("A3@ef678!"), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();
    }


    @Test
    public void signInWithEmailShowsRightErrorWhenFalse() throws InterruptedException {
        // INPUTS_EMPTY
        onView(withId(R.id.log_enter_email)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.inputs_empty)));

        // Auth Failed
        onView(withId(R.id.log_enter_email)).perform(clearText(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_enter_password)).perform(clearText(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.log_email_signin_button)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.log_email_error_message)).check(matches(withText(R.string.log_email_auth_failed)));
    }
}
