package com.github.houseorganizer.houseorganizer.shop;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FirestoreShopList extends ShopList{

    private DocumentReference household;
    private DocumentReference onlineReference;

    public FirestoreShopList(DocumentReference household){
        this.household = household;
    }

    public FirestoreShopList(DocumentReference household, DocumentReference onlineReference){
        this.household = household;
        this.onlineReference = onlineReference;
    }

    public FirestoreShopList(DocumentReference household, DocumentReference onlineReference, List<ShopItem> items){
        super(items);
        this.household = household;
        this.onlineReference = onlineReference;
    }

    public DocumentReference getHousehold() {
        return household;
    }

    public void setOnlineReference(DocumentReference docRef){
        onlineReference = docRef;
    }

    public void setHousehold(DocumentReference household) {
        this.household = household;
    }

    public Task<Void> updateItems(){
        if(household == null || onlineReference == null){
            return Tasks.forCanceled();
        }
        List<Map<String, Object>> items = convertItemsListToFirebase(this.getItems());

        return onlineReference.update("items", items);
    }

    public Task<DocumentSnapshot> refreshItems(){
        if(onlineReference == null){
            return Tasks.forCanceled();
        }
        return onlineReference.get().addOnCompleteListener(t ->{
           DocumentSnapshot snap = t.getResult();
           setItems(convertFirebaseListToItems((List<Map<String, Object>>) snap.get("items")));
        });
    }

    private static List<Map<String, Object>> convertItemsListToFirebase(List<ShopItem> shopItemList){
        List<Map<String, Object>> items = new LinkedList<>();

        for(int i = 0; i < shopItemList.size(); ++i){
            ShopItem item = shopItemList.get(i);
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unit", item.getUnit());
            items.add(itemMap);
        }
        return items;
    }

    private static List<ShopItem> convertFirebaseListToItems(List<Map<String, Object>> list){
        List<ShopItem> items = new LinkedList<>();
        for(Map<String, Object> m : list){
            items.add(new ShopItem((String)m.get("name"), (int) m.get("quantity"), (String) m.get("unit")));
        }
        return items;
    }

    public static Task<DocumentReference> storeNewShopList(CollectionReference shopListRoot, ShopList shopList, DocumentReference household){
        Map<String, Object> map = new HashMap<>();
        map.put("household", household);
        List<Map<String, Object>> items = convertItemsListToFirebase(shopList.getItems());
        map.put("items", items);
        return shopListRoot.add(map);
    }

    public static Task<QuerySnapshot> fetchShopList(CollectionReference shopListRoot, DocumentReference household){
        return shopListRoot.whereArrayContains("household", household).get();
    }

    public static FirestoreShopList buildShopList(DocumentSnapshot documentSnapshot){
        if(documentSnapshot == null) return null;
        DocumentReference household = (DocumentReference) documentSnapshot.get("household");
        List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("items");
        List<ShopItem> items = convertFirebaseListToItems(list);
        return new FirestoreShopList(household, documentSnapshot.getReference(), items);
    }
}
