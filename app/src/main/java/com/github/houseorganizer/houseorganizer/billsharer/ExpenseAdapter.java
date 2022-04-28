package com.github.houseorganizer.houseorganizer.billsharer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Billsharer billsharer;

    public ExpenseAdapter(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    public void addExpense() {}

    public void editExpense() {}

    public void deleteExpense() {}

    public void deleteBillsharer() {}

    @NonNull
    @Override
    public ExpenseAdapter.ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ExpenseViewHolder(inflater.inflate(R.layout.expense_cell, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseAdapter.ExpenseViewHolder holder, int position) {
        Expense expense = billsharer.getExpenses().get(position);
        ((ExpenseViewHolder) holder).textView.setText(expense.toText());
    }

    @Override
    public int getItemCount() {
        return billsharer.getExpenses().size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public Button textView;
        // TODO there will be a title, a cost, a payee, and the date

        public ExpenseViewHolder(View eventView) {
            super(eventView);

            // TODO create layout of view
        }
    }

}
