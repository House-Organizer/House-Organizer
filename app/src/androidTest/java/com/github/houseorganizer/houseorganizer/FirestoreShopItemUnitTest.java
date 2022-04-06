package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopItem;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class FirestoreShopItemUnitTest {

    private FirestoreShopItem testLocalItem = new FirestoreShopItem("egg", 3, "t", null);
    private static FirebaseFirestore db;

    @BeforeClass
    public static void setUpTests() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.startAuthEmulator();

        db = FirebaseFirestore.getInstance();

        FirebaseTestsHelper.setUpFirebase();
    }

    @AfterClass
    public static void closingClass(){
        //FirebaseTestsHelper.setUpFirebase();
    }

    @Test
    public void constructorCreatesItem(){
        assertThat(testLocalItem.getName(), is("egg"));
    }
}
