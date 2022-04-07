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
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_PWD;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.VALID_PASSWORD_FOR_APP;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.createFirebaseTestUserWithCredentials;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.deleteTestUser;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.signInTestUserWithCredentials;

import static org.hamcrest.CoreMatchers.anyOf;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.RegisterEmail;
import com.github.houseorganizer.houseorganizer.login.VerifyEmail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;


@RunWith(AndroidJUnit4.class)
public class RegisterEmailTest {

    private static final String email1 = "user_register1@test.com", email2 = "user_register2@test.com",
            email3 = "user_register3@test.com";
    private static final String invalidEmail = "invalidEmail";

    @Rule
    public ActivityScenarioRule<RegisterEmail> regRule =
            new ActivityScenarioRule<>(RegisterEmail.class);

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();

        createFirebaseTestUserWithCredentials(email2, VALID_PASSWORD_FOR_APP);
    }

    @AfterClass
    public static void end() throws ExecutionException, InterruptedException {
        signInTestUserWithCredentials(email2, VALID_PASSWORD_FOR_APP);
        deleteTestUser();
    }

    @Test
    public void signUpWithEmailWorksWithCorrectInputs() throws ExecutionException, InterruptedException {
        Intents.init();
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(email1), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText(VALID_PASSWORD_FOR_APP), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText(VALID_PASSWORD_FOR_APP), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        intended(hasComponent(VerifyEmail.class.getName()));
        Intents.release();

        deleteTestUser();
    }

    @Test
    public void signUpWithEmailShowsInputsEmptyErrorWhenInputsEmpty() throws InterruptedException {
        enterInputsAndClickRegister(email3, "");
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.inputs_empty)));
    }

    @Test
    public void signUpWithEmailShowsInvalidEmailErrorWithInvalidInput() throws InterruptedException {
        enterInputsAndClickRegister(invalidEmail, TEST_USERS_PWD[0]);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.email_not_valid)));
    }

    @Test
    public void signUpWithEmailShowsInvalidPasswordErrorWithInvalidInput() throws InterruptedException {
        enterInputsAndClickRegister(email3, TEST_USERS_PWD[0]);
        onView(withId(R.id.reg_email_error_message)).check(matches(withText(R.string.password_not_valid)));
    }

    @Test
    public void signUpWithEmailShowsEmailUsedErrorWithAlreadyUsedEmail() throws InterruptedException {
        enterInputsAndClickRegister(email2, VALID_PASSWORD_FOR_APP);
        onView(withId(R.id.reg_email_error_message)).check(matches(anyOf(
                withText(R.string.email_already_used), withText(R.string.reg_email_auth_failed)
        )));
    }

    private void enterInputsAndClickRegister(String email, String password) throws InterruptedException {
        onView(withId(R.id.reg_enter_email)).perform(clearText(), typeText(email), closeSoftKeyboard());
        onView(withId(R.id.reg_enter_password)).perform(clearText(), typeText(password), closeSoftKeyboard());
        onView(withId(R.id.reg_confirm_password)).perform(clearText(), typeText(password), closeSoftKeyboard());
        onView(withId(R.id.reg_email_register_button)).perform(click());
        Thread.sleep(250);
    }
}
