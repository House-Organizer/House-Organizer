package com.github.houseorganizer.houseorganizer.shop;

import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    public static void storeShopList(ShopList shopList, CollectionReference shopListListRoot, String documentName) throws ExecutionException, InterruptedException {
        Map<String, Object> newList = new HashMap<>();

        newList.put("name", shopList.getListName());
        newList.put("owner", shopList.getOwner());

        Task<Void> t = shopListListRoot.document(documentName).set(newList);
        Tasks.await(t);

        if(t.isSuccessful()){
            DocumentReference docRef = shopListListRoot.document(documentName);
            CollectionReference shopListRef = docRef.collection("groceries");

            for(int i = 0; i < shopList.size(); ++i){
                Tasks.await(storeShopItem(shopList.getItemAt(i), shopListRef));
            }
        }
    }

    public static Task<DocumentReference> storeShopItem(ShopItem shopItem, CollectionReference firestoreShopList){
        Map<String, Object> toStore = new HashMap<>();

        //Putting infos
        toStore.put("name", shopItem.getName());
        toStore.put("quantity", shopItem.getQuantity());
        toStore.put("unit", shopItem.getUnit());
        toStore.put("pickedUp", shopItem.isPickedUp());

        return firestoreShopList.add(toStore);
    }

}
