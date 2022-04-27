package com.github.houseorganizer.houseorganizer.panels;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.user.UserAdapter;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private DocumentReference currentHouse;
    private RecyclerView usersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        firestore = FirebaseFirestore.getInstance();
        loadData();

        if (currentHouse != null) {
            currentHouse.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    List<String> residents = (List<String>) task.getResult().get("residents");

                    firestore.collection("email-to-nickname")
                             .document("email-to-nickname-translations").get()
                             .addOnCompleteListener(trans -> {
                                 if(residents != null && trans.isSuccessful()){
                                     residents.replaceAll(e -> {
                                         String nickname = (String) trans.getResult().get(e);
                                         return nickname == null ? e : nickname;
                                     });
                                 }
                                 setupUserView(residents);
                             });
                }
            });
        } else {
            TextView text = findViewById(R.id.infoHeader);
            text.setText(R.string.no_household_info);
        }
    }

    private void setupUserView(List<String> residents){
        usersView = findViewById(R.id.info_recycler_view);
        UserAdapter adapter = new UserAdapter(getApplicationContext(),residents);
        usersView.setAdapter(adapter);
        usersView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    private void loadData() {
        SharedPreferences sharedPreferences = Util.getSharedPrefs(this);

        String householdId = sharedPreferences.getString(MainScreenActivity.CURRENT_HOUSEHOLD, "");
        if (!householdId.equals(""))
            currentHouse = firestore.collection("households").document(householdId);
    }
}
