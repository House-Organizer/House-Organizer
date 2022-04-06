package com.github.houseorganizer.houseorganizer.shop;

import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreShopItem extends ShopItem{
    private String houseHoldID;
    private final DocumentReference shopItemDocRef;

    public FirestoreShopItem(String name, int quantity, String unit, DocumentReference docRef) {
        super(name, quantity, unit);
        this.shopItemDocRef = docRef;
    }

    @Override
    public void togglePickedUp(){
        super.togglePickedUp();
        shopItemDocRef.update("pickedUp", this.isPickedUp());
    }

    @Override
    public void changeName(String newName){
        super.changeName(newName);
        shopItemDocRef.update("name", newName);
    }

    @Override
    public void setQuantity(int quantity) {
        super.setQuantity(quantity);
        shopItemDocRef.update("quantity", quantity);
    }

    @Override
    public void setUnit(String unit){
        super.setUnit(unit);
        shopItemDocRef.update("unit", unit);
    }

    @Override
    public void setPickedUp(boolean pickedUp){
        super.setPickedUp(pickedUp);
        shopItemDocRef.update("pickedUp", pickedUp);
    }

    public DocumentReference getItemDocRef(){
        return shopItemDocRef;
    }

    /* TODO : check if necessary or useful
    public void store() throws ExecutionException, InterruptedException {
        Task t = storeShopItem(this, shopItemDocRef.getParent());
        Tasks.await(t);
    }*/

    /**
     * To be called with the root directory and the name of the new list
     * @param shopList shopList to be stored
     * @param shopListListRoot root folder of the shop lists
     * @param documentName doc where the shopList will be stored
     * @throws ExecutionException calls Tasks.await
     * @throws InterruptedException calls Tasks.await
     */
    public static void storeShopList(ShopList shopList, CollectionReference shopListListRoot, String documentName) throws ExecutionException, InterruptedException {
        Map<String, Object> newList = new HashMap<>();

        newList.put("name", shopList.getListName());
        newList.put("owner", shopList.getOwner().uid());
        newList.put("authorized", new ArrayList<User>());

        Task<Void> t = shopListListRoot.document(documentName).set(newList);
        Tasks.await(t);

        if(t.isSuccessful()){
            DocumentReference docRef = shopListListRoot.document(documentName);
            CollectionReference shopListRef = docRef.collection("items");

            for(int i = 0; i < shopList.size(); ++i){
                Tasks.await(storeShopItem(shopList.getItemAt(i), shopListRef));
            }
        }
    }

    /**
     * Stores the item in the given Firestore Shop List
     * @param shopItem item to be stored
     * @param firestoreShopList collection to store the item
     * @return task of the firebase adding
     */
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
