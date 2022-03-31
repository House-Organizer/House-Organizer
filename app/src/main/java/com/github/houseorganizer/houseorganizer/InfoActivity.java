package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private DocumentReference currentHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        firestore = FirebaseFirestore.getInstance();
        loadData();

        if (currentHouse != null) {
            currentHouse.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    TextView info_view = findViewById(R.id.info_text_view);
                    info_view.setText(document.getData().toString());
                }
            });
        }
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainScreenActivity.SHARED_PREFS, MODE_PRIVATE);

        String householdId = sharedPreferences.getString(MainScreenActivity.CURRENT_HOUSEHOLD, "");
        if (!householdId.equals(""))
            currentHouse = firestore.collection("households").document(householdId);
    }
}
