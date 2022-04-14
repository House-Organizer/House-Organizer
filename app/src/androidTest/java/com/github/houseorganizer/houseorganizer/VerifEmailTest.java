package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.login.VerifyEmail;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class VerifEmailTest {

    @Rule
    public ActivityScenarioRule<VerifyEmail> regRule =
            new ActivityScenarioRule<>(VerifyEmail.class);

    @Test
    public void onCreateShowsRightMessage() throws ExecutionException, InterruptedException {
        Task<AuthResult> t = FirebaseAuth.getInstance().createUserWithEmailAndPassword("user1_verif@test.com", "Password");
        FirebaseAuth.getInstance().signOut();
        Tasks.await(t);

        onView(withId(R.id.verif_email_text)).check(matches(withText(
                "Please verify your email and then reload the app.\n\nThe email was sent to :\n"
                        + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                        .getEmail()
        )));

        Task<Void> t1 = FirebaseAuth.getInstance().getCurrentUser().delete();
        Tasks.await(t1);
    }
}
