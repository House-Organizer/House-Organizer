package com.github.houseorganizer.houseorganizer.shop;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

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

    @Override
    public void removeItem(ShopItem shopItem){
        super.removeItem(shopItem);
        updateItems();
    }

    @Override
    public void removeItem(int index){
        super.removeItem(index);
        updateItems();
    }

    @Override
    public void addItem(ShopItem item){
        super.addItem(item);
        updateItems();
    }

    @Override
    public void removePickedUpItems(){
        super.removePickedUpItems();
        updateItems();
    }

    public void setOnlineReference(DocumentReference docRef){
        onlineReference = docRef;
    }

    public void setHousehold(DocumentReference household) {
        this.household = household;
    }

    public DocumentReference getOnlineReference() {
        return onlineReference;
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
        return onlineReference.get().continueWith( r -> {
            DocumentSnapshot snap = r.getResult();
            setItems(convertFirebaseListToItems((List<Map<String, Object>>) snap.get("items")));
            return snap;
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
            itemMap.put("pickedUp", item.isPickedUp());
            items.add(itemMap);
        }
        return items;
    }

    private static List<ShopItem> convertFirebaseListToItems(List<Map<String, Object>> list){
        List<ShopItem> items = new LinkedList<>();
        for(Map<String, Object> m : list){
            items.add(new ShopItem((String)m.get("name"),
                    new Long((long) m.get("quantity")).intValue(),
                    (String) m.get("unit")));
            items.get(items.size()-1).setPickedUp((boolean) m.get("pickedUp"));
        }
        return items;
    }

    /**
     * Add a shopList for a new house : DOES NOT CHECK if a house already have a list
     * @param shopListRoot root folder of shop lists
     * @param shopList shopList to add
     * @param household household linked to the grocery list
     * @return the adding task with as result the new document
     */
    public static Task<DocumentReference> storeNewShopList(CollectionReference shopListRoot, ShopList shopList, DocumentReference household){
        Map<String, Object> map = new HashMap<>();
        map.put("household", household);
        List<Map<String, Object>> items = convertItemsListToFirebase(shopList.getItems());
        map.put("items", items);
        return shopListRoot.add(map);
    }


    public static Task<FirestoreShopList> retrieveShopList(CollectionReference shopListRoot, DocumentReference household){
        return shopListRoot.whereEqualTo("household", household).get().continueWith( t -> {
            List<DocumentSnapshot> res = t.getResult().getDocuments();
            if(res.isEmpty())return null;
            if(res.size() > 1) throw new IllegalStateException("More than one groceries list for this house");
            return buildShopList(res.get(0));
        });
    }

    public static FirestoreShopList buildShopList(DocumentSnapshot documentSnapshot){
        if(documentSnapshot == null) return null;
        DocumentReference household = (DocumentReference) documentSnapshot.get("household");
        List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("items");
        List<ShopItem> items = convertFirebaseListToItems(list);
        return new FirestoreShopList(household, documentSnapshot.getReference(), items);
    }
}
