package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreShopItemUnitTest {

    private static FirestoreShopItem testLocalItem;
    private static FirebaseFirestore db;

    @BeforeClass
    public static void setUpTests() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.startAuthEmulator();

        db = FirebaseFirestore.getInstance();

        FirebaseTestsHelper.setUpFirebase();

        CollectionReference colRef = db.collection("shop_lists")
                .document(FirebaseTestsHelper.TEST_SHOPLIST_NAME).collection("items");

        List<FirestoreShopItem> list = FirestoreShopItem.retrieveShopList(colRef);

        testLocalItem = list.get(0);
    }

    @Test
    public void constructorCreatesItem(){
        assertThat(testLocalItem.getName(), is("Egg"));
    }

    @Test
    public void togglePickedUpPicksUpItem() throws ExecutionException, InterruptedException {
        testLocalItem.togglePickedUp();
        DocumentReference documentReference = testLocalItem.getItemDocRef();
        Task<DocumentSnapshot> t = documentReference.get();
        Tasks.await(t);
        assertThat(t.getResult().get("pickedUp"), is(true));
        testLocalItem.togglePickedUp();
    }
}
