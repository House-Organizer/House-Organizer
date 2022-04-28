package com.github.houseorganizer.houseorganizer.billsharer;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Billsharer billsharer;

    public ExpenseAdapter(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    public void addExpense() {}

    public void pushExpense() {}

    public void editExpense() {}

    public void createExpense() {}

    public void deleteExpense() {}

    public void deleteBillsharer() {}

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return billsharer.getExpenses().size();
    }
}
