package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class FirestoreShopListTest {

    private static FirestoreShopList localShopList;
    private static FirebaseFirestore db;

    @BeforeClass
    public static void startingFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();
        db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> t = db.collection("shop_lists").get();

        /*FirestoreShopList.retrieveShopList(db.collection("shop_lists"),
                db.collection("households").
                        document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]));*/
        Tasks.await(t);
        assertThat(t.getResult().getDocuments().size(), is(1));
        DocumentSnapshot snap = t.getResult().getDocuments().get(0);
        List<Map<String, Object>> map = (List<Map<String, Object>>) snap.get("items");
        assertThat(map.isEmpty(), is(false));
        String name = (String) map.get(0).get("name");
        int quantity = new Long((long) map.get(0).get("quantity")).intValue();
        String unit = (String) map.get(0).get("unit");
        List<ShopItem> list = new ArrayList<>();
        list.add(new ShopItem(name, quantity, unit));
        localShopList = new FirestoreShopList(db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]),
                snap.getReference(), list);
    }

    @Test
    public void storeNewShopListWorks() throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> t = localShopList.getOnlineReference().get();
        Tasks.await(t);
        assertThat(t.getResult().get("household"), is(localShopList.getHousehold()));
    }

    @Test
    public void retrieveShopListWorks() throws ExecutionException, InterruptedException {
        Task<FirestoreShopList> t = FirestoreShopList.
                retrieveShopList(db.collection("shop_lists"),
                        localShopList.getHousehold());
        Tasks.await(t);
        FirestoreShopList nList = t.getResult();
        assertThat(nList.getOnlineReference(),
                is(localShopList.getOnlineReference()));
        assertThat(nList.getHousehold(), is(localShopList.getHousehold()));
        assertThat(nList.isEmpty(), is(localShopList.isEmpty()));
        assertThat(localShopList.size(), is(localShopList.size()));
    }
}
