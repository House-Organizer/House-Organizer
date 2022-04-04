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
public class RegisterEmailTest {

    private static FirebaseAuth auth;

    @Rule
    public ActivityScenarioRule<RegisterEmail> regRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        auth = FirebaseAuth.getInstance();

        Task<AuthResult> t = auth.createUserWithEmailAndPassword("user_register2@test.com", "A3@ef678");
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
    public void signUpWithEmailWorksWithCorrectInputs() {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText("user_register1@test.com"), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText("A3@ef678"), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText("A3@ef678"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();
    }

    @Test
    public void signUpWithEmailShowsRightErrorWhenFalse() throws InterruptedException {
        // INPUTS_EMPTY
        onView(withId(R.id.reg_enter_email)).perform(click(), typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.inputs_empty)));

        // INVALID_EMAIL
        onView(withId(R.id.reg_enter_password)).perform(click(), typeText("testPassword"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.email_not_valid)));

        // INVALID_PASSWORD
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(TEST_USERS_EMAILS[1]), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(click(), typeText("testPassword"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.password_not_valid)));

        // EMAIL_USED
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText("user_register2@test.com"), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText("A3@ef678!"), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText("A3@ef678!"), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.reg_email_auth_failed)));
    }
}
