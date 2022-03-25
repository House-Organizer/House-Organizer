package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HouseSelectionActivity extends AppCompatActivity {
    RecyclerView housesView;
    String emailUser;
    FirestoreRecyclerAdapter<HouseModel, HouseViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_selection);

        housesView = findViewById(R.id.housesView);
        emailUser = getIntent().getStringExtra(MainScreenActivity.CURRENT_USER);

        Query query = FirebaseFirestore.getInstance().collection("households").whereArrayContains("residents", emailUser);
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

    @SuppressWarnings("unused")
    public void houseSelected(View view) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.putExtra(MainScreenActivity.HOUSEHOLD, view.getTag().toString());
        startActivity(intent);
    }

    public void editHousehold(View view) {
        Intent intent = new Intent(this, EditHousehold.class);
        intent.putExtra(MainScreenActivity.HOUSEHOLD, view.getTag().toString());
        startActivity(intent);
    }

    public void addHouseholdButtonPressed(View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        intent.putExtra("mUserEmail", emailUser);
        startActivity(intent);
    }

    private class HouseViewHolder extends RecyclerView.ViewHolder {
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