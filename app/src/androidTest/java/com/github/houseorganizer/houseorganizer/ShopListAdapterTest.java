package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopListAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class ShopListAdapterTest {

    private static ShopListAdapter adapter;
    private static FirestoreShopList shopList;
    private static FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> testRule = new ActivityScenarioRule<>(MainScreenActivity.class);

    @BeforeClass
    public static void startingEmulators() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startStorageEmulator();
        FirebaseTestsHelper.setUpFirebase();
        db = FirebaseFirestore.getInstance();

    }

    @Before
    public void initializeAdapter() throws ExecutionException, InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        Task<ShopListAdapter> t = ShopListAdapter.initializeFirestoreShopList(
                db.collection("households").document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]),
                db);
        Tasks.await(t);

        adapter = t.getResult();
        shopList = adapter.getFirestoreShopList();
    }

    @Test
    public void setUpShopListViewWorks(){
        testRule.getScenario().onActivity(a -> adapter.setUpShopListView(a));
        onView(withId(R.id.task_list)).check(matches(hasChildCount(1)));
    }

    @Test
    public void getItemCountWorks(){
        assertThat(adapter.getItemCount(), is(1));
    }

    @Test
    public void getShopListWorks(){
        FirestoreShopList gotList = adapter.getFirestoreShopList();
        assertThat(gotList.getHousehold(), is(shopList.getHousehold()));
        assertThat(gotList.size(), is(shopList.size()));
        assertThat(gotList.getOnlineReference(), is(shopList.getOnlineReference()));
    }

    @Test
    public void addItemWorks(){
        onView(withId(R.id.list_view_change)).perform(click());
        testRule.getScenario().onActivity(a->{
            adapter.setUpShopListView(a);
            adapter.addItem(a, shopList);
        });
        onView(withId(R.id.edit_text_name)).perform(typeText("name"));
        onView(withId(R.id.edit_text_quantity)).perform(typeText(""+5));
        onView(withId(R.id.edit_text_unit)).perform(typeText("unit"));
        onView(withText(R.string.add)).perform(click());

        onView(withId(R.id.task_list)).check(matches(hasChildCount(2)));

        //Removing item
        onView(withId(R.id.task_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.delete_item_button)));
    }

    @Test
    public void setShopListWorks(){
        ShopList shopList1 = new ShopList();
        shopList1.addItem(new ShopItem("tt", 6, "dd"));
        shopList1.addItem(new ShopItem("tt", 6, "dd"));
        testRule.getScenario().onActivity(a -> {
            adapter.setUpShopListView(a);
            adapter.setShopList(shopList1);
        });
        onView(withId(R.id.task_list)).check(matches(hasChildCount(2)));
        testRule.getScenario().onActivity(a -> {
            adapter.setShopList(shopList);
        });
    }

}
