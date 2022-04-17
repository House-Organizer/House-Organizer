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

    private static FirestoreShopList localShopList;
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
    public void retrieveShopListWorksFor() throws ExecutionException, InterruptedException {
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
        // Testing retrieved item
        ShopItem rItem = localShopList.getItemAt(0);
        assertThat(rItem.getName(), is(FirebaseTestsHelper.TEST_ITEM.getName()));
        assertThat(rItem.getUnit(), is(FirebaseTestsHelper.TEST_ITEM.getUnit()));
        assertThat(rItem.getQuantity(), is(FirebaseTestsHelper.TEST_ITEM.getQuantity()));
    }



    @Test
    public void updateItemsWorks() throws ExecutionException, InterruptedException {
        ShopItem testItem = new ShopItem("Raclette", 2, "g");
        localShopList.addItem(testItem);
        localShopList.updateItems();
        Task<FirestoreShopList> t = FirestoreShopList.
                retrieveShopList(db.collection("shop_lists"),
                        localShopList.getHousehold());
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
                db.collection("shop_lists"), localShopList.getHousehold());
        Tasks.await(t);
        FirestoreShopList listBis = t.getResult();

        ShopItem item = new ShopItem("Raclette", 2, "g");
        listBis.addItem(item);
        Task<DocumentSnapshot> t1 = listBis.updateItems().continueWithTask(r -> localShopList.refreshItems());
        Tasks.await(t1);
        assertThat(localShopList.size(), is(listBis.size()));
        assertThat(localShopList.getItemAt(0).getName(), is(listBis.getItemAt(0).getName()));
    }

    @After
    public void removeItems(){
        if(localShopList.size() > 1){
            for(int i = localShopList.size()-1; i > 0; --i){
                localShopList.removeItem(i);
            }
            localShopList.updateItems();
        }
    }

}
