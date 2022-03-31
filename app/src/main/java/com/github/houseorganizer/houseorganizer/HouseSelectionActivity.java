package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Map;

public class HouseSelectionActivity extends AppCompatActivity {

    public static final String HOUSEHOLD_TO_EDIT = "com.github.houseorganizer.houseorganizer.HOUSEHOLD_TO_EDIT";
    private String emailUser;
    private RecyclerView housesView;
    FirestoreRecyclerAdapter<HouseModel, HouseViewHolder> adapter;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_selection);

        housesView = findViewById(R.id.housesView);

        emailUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        this.firestore = FirebaseFirestore.getInstance();

        Query query = firestore.collection("households").whereArrayContains("residents", emailUser);
        FirestoreRecyclerOptions<HouseModel> options = new FirestoreRecyclerOptions.Builder<HouseModel>()
                .setQuery(query, HouseModel.class).build();
        adapter = new FirestoreRecyclerAdapter<HouseModel, HouseViewHolder>(options) {
            @NonNull
            @Override
            public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.house_row, parent, false);
                return new HouseViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HouseViewHolder holder, int position, @NonNull HouseModel model) {
                holder.houseName.setText(model.getName());
                holder.houseName.setTag(adapter.getSnapshots().getSnapshot(position).getId());
                holder.editButton.setTag(adapter.getSnapshots().getSnapshot(position).getId());
            }
        };

        housesView.setHasFixedSize(true);
        housesView.setLayoutManager(new LinearLayoutManager(this));
        housesView.setAdapter(adapter);
    }

    private void saveData(String selectedHouse) {
        SharedPreferences sharedPreferences = getSharedPreferences(MainScreenActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(MainScreenActivity.CURRENT_HOUSEHOLD, selectedHouse);
        editor.apply();
    }

    @SuppressWarnings("unused")
    public void houseSelected(View view) {
        saveData(view.getTag().toString());

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }

    public void editHousehold(View view) {
        String householdId = view.getTag().toString();

        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> householdData = document.getData();
                    if(householdData != null) {
                        String owner = (String) householdData.getOrDefault("owner", null);
                        if(owner == null || !owner.equals(emailUser)) {
                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.not_owner),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            sendToEditHouse(view);
                        }
                    }
                });
    }

    public void sendToEditHouse(View view){
        Intent intent = new Intent(this, EditHousehold.class);
        intent.putExtra(HOUSEHOLD_TO_EDIT, view.getTag().toString());
        startActivity(intent);
    }

    public void addHouseholdButtonPressed(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        startActivity(intent);
    }

    private static class HouseViewHolder extends RecyclerView.ViewHolder {
        TextView houseName;
        ImageButton editButton;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.houseName);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}