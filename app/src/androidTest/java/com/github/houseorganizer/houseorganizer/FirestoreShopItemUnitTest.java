package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopList;
import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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

        List<FirestoreShopItem> list = FirestoreShopItem.retrieveShopItems(colRef);

        testLocalItem = list.get(0);
        testLocalItem.setPickedUp(false);
        testLocalItem.setUnit(FirebaseTestsHelper.TEST_ITEM.getUnit());
        testLocalItem.setQuantity(FirebaseTestsHelper.TEST_ITEM.getQuantity());
        testLocalItem.changeName(FirebaseTestsHelper.TEST_ITEM.getName());
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

    @Test
    public void changeNameUpdatesFirestore() throws ExecutionException, InterruptedException {
        DocumentReference docRef = testLocalItem.getItemDocRef();
        Task<DocumentSnapshot> t = docRef.get();
        Tasks.await(t);
        String oldName = (String)t.getResult().get("name");
        testLocalItem.changeName("RandomNewName");
        Task<DocumentSnapshot> t1 = docRef.get();
        Tasks.await(t1);
        String newName = (String)t1.getResult().get("name");
        assertThat(oldName, is(not(newName)));
        assertThat(newName, is("RandomNewName"));
        testLocalItem.changeName(FirebaseTestsHelper.TEST_ITEM.getName());
    }

    @Test
    public void setQuantityUpdatesFirestore() throws ExecutionException, InterruptedException {
        DocumentReference docRef = testLocalItem.getItemDocRef();
        Task<DocumentSnapshot> t = docRef.get();
        Tasks.await(t);
        long oldQ = (long)t.getResult().get("quantity");
        testLocalItem.setQuantity(9);
        Task<DocumentSnapshot> t1 = docRef.get();
        Tasks.await(t1);
        long newQ = (long)t1.getResult().get("quantity");
        assertThat(oldQ, is(not(newQ)));
        assertThat(newQ, is(9L));
        testLocalItem.setQuantity(FirebaseTestsHelper.TEST_ITEM.getQuantity());
    }

    @Test
    public void setUnitUpdatesFirestore() throws ExecutionException, InterruptedException {
        DocumentReference docRef = testLocalItem.getItemDocRef();
        Task<DocumentSnapshot> t = docRef.get();
        Tasks.await(t);
        String old = (String)t.getResult().get("unit");
        testLocalItem.setUnit("m^2");
        Task<DocumentSnapshot> t1 = docRef.get();
        Tasks.await(t1);
        String new1 = (String)t1.getResult().get("unit");
        assertThat(old, is(not(new1)));
        assertThat(new1, is("m^2"));
        testLocalItem.setUnit(FirebaseTestsHelper.TEST_ITEM.getUnit());
    }

    @Test
    public void setPickedUpUpdatesFirestore() throws ExecutionException, InterruptedException {
        DocumentReference docRef = testLocalItem.getItemDocRef();
        Task<DocumentSnapshot> t = docRef.get();
        Tasks.await(t);
        boolean old = (boolean)t.getResult().get("pickedUp");
        testLocalItem.setPickedUp(true);
        Task<DocumentSnapshot> t1 = docRef.get();
        Tasks.await(t1);
        boolean new1 = (boolean)t1.getResult().get("pickedUp");
        assertThat(old, is(not(new1)));
        assertThat(new1, is(true));
        testLocalItem.setPickedUp(FirebaseTestsHelper.TEST_ITEM.isPickedUp());
    }

    @Test
    public void storeTaskWorks() throws ExecutionException, InterruptedException {
        ShopItem item = new ShopItem("lol", 3, "omegalul");
        CollectionReference colRef = testLocalItem.getItemDocRef().getParent();
        Task<DocumentReference> t = FirestoreShopItem.storeShopItem(item, colRef);
        Tasks.await(t);
        Task<DocumentSnapshot> t1 = t.getResult().get();
        Tasks.await(t1);
        assertThat(t1.getResult().get("name"), is("lol"));
        assertThat(t1.getResult().get("unit"), is("omegalul"));
        assertThat(t1.getResult().get("quantity"), is(3L));
        t.getResult().delete();
    }

    @Test
    public void storeShopListWorks() throws ExecutionException, InterruptedException {
        ShopList shopList = new ShopList(new DummyUser("Lolito", FirebaseTestsHelper.TEST_USERS_EMAILS[0]), "MyNewList");
        shopList.addItem(FirebaseTestsHelper.TEST_ITEM);
        CollectionReference root = db.collection("shop_lists");
        FirestoreShopItem.storeShopList(shopList, root, shopList.getListName());
        DocumentReference doc = root.document("MyNewList");
        Task<DocumentSnapshot> t0 = doc.get();
        Tasks.await(t0);
        doc.delete();
        assertThat(t0.getResult().get("name"), is("MyNewList"));
        assertThat(t0.getResult().get("owner"), is(FirebaseTestsHelper.TEST_USERS_EMAILS[0]));
        assertThat(((ArrayList<User>)t0.getResult().get("authorized")).isEmpty(), is(true));
    }
}
