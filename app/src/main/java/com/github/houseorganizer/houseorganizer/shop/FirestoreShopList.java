package com.github.houseorganizer.houseorganizer.shop;

import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreShopList extends ShopList{
    private String houseHoldID;
    private final DocumentReference shopListDocRef;

    public FirestoreShopList(User owner, String listName, DocumentReference docRef) {
        super(owner, listName);
        this.shopListDocRef = docRef;
    }

    public FirestoreShopList(User owner, String listName, List<ShopItem> list, DocumentReference docRef) {
        super(owner, listName, list);
        this.shopListDocRef = docRef;
    }

    public static Task<DocumentReference> storeShopItem(ShopItem shopItem, CollectionReference firestoreShopList){
        Map<String, Object> toStore = new HashMap<>();

        //Putting infos
        toStore.put("name", shopItem.getName());
        toStore.put("quantity", shopItem.getQuantity());
        toStore.put("unit", shopItem.getUnit());

        return firestoreShopList.add(toStore);
    }

}
