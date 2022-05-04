package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class FirestoreShopListTest {

    private static FirestoreShopList shopList;
    private static FirebaseFirestore db;

    @BeforeClass
    public static void startingFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> t = db.collection("shop_lists").get();
        Tasks.await(t);
        assertThat(t.getResult().getDocuments().size() > 0, is(true));

        // Store new shop list with one item for TEST_HOUSEHOLD_NAMES[2] on Firebase
        DocumentReference household = db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[2]);
        shopList = new FirestoreShopList(household);
        shopList.addItem(FirebaseTestsHelper.TEST_ITEM);
        Task<DocumentReference> t1 = FirestoreShopList.storeNewShopList(db.collection("shop_lists"), shopList, household);
        Tasks.await(t1);
        shopList.setOnlineReference(t1.getResult());
    }

    @Test
    public void storeNewShopListWorks() throws ExecutionException, InterruptedException {
        // Get stored shop list from Firebase
        Task<DocumentSnapshot> t2 = shopList.getOnlineReference().get();
        Tasks.await(t2);

        assertThat(t2.getResult().get("household"), is(shopList.getHousehold()));
    }

    @Test
    public void retrieveShopListWorksFor() throws ExecutionException, InterruptedException {
        DocumentReference household = db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[2]);
        Task<FirestoreShopList> t = FirestoreShopList.
                retrieveShopList(db.collection("shop_lists"), household);
        Tasks.await(t);
        FirestoreShopList nList = t.getResult();
        assertThat(nList.getOnlineReference(), is(shopList.getOnlineReference()));
        assertThat(nList.getHousehold(), is(shopList.getHousehold()));
        assertThat(nList.isEmpty(), is(shopList.isEmpty()));
        assertThat(shopList.size(), is(shopList.size()));

        // Testing retrieved item
        ShopItem rItem = shopList.getItemAt(0);
        assertThat(rItem.getName(), is(FirebaseTestsHelper.TEST_ITEM.getName()));
        assertThat(rItem.getUnit(), is(FirebaseTestsHelper.TEST_ITEM.getUnit()));
        assertThat(rItem.getQuantity(), is(FirebaseTestsHelper.TEST_ITEM.getQuantity()));
    }

    @Test
    public void updateItemsWorks() throws ExecutionException, InterruptedException {
        ShopItem testItem = new ShopItem("Raclette", 2, "g");
        shopList.addItem(testItem);
        shopList.updateItems();
        Task<FirestoreShopList> t = FirestoreShopList.
                retrieveShopList(db.collection("shop_lists"),
                        shopList.getHousehold());
        Tasks.await(t);
        FirestoreShopList nList = t.getResult();
        assertThat(nList.size(), is(2));
        assertThat(nList.getItemAt(1).getName(), is(testItem.getName()));
        assertThat(nList.getItemAt(1).getQuantity(), is(testItem.getQuantity()));
        assertThat(nList.getItemAt(1).getUnit(), is(testItem.getUnit()));
    }

    @Test
    public void refreshItemWorks() throws ExecutionException, InterruptedException {
        Task<FirestoreShopList> t = FirestoreShopList.retrieveShopList(
                db.collection("shop_lists"), shopList.getHousehold());
        Tasks.await(t);
        FirestoreShopList listBis = t.getResult();

        ShopItem item = new ShopItem("Raclette", 2, "g");
        listBis.addItem(item);
        Task<DocumentSnapshot> t1 = listBis.updateItems().continueWithTask(r -> shopList.refreshItems());
        Tasks.await(t1);
        assertThat(shopList.size(), is(listBis.size()));
        assertThat(shopList.getItemAt(0).getName(), is(listBis.getItemAt(0).getName()));
    }

    @After
    public void removeItems(){
        if(shopList.size() > 1){
            for(int i = shopList.size()-1; i > 0; --i){
                shopList.removeItem(i);
            }
            shopList.updateItems();
        }
    }
}