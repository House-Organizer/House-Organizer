package com.github.houseorganizer.houseorganizer.billsharer;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.HashMap;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Billsharer billsharer;

    public ExpenseAdapter(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    public Billsharer getBillsharer() {
        return billsharer;
    }

    public void setBillsharer(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    public void addExpense(AppCompatActivity parent) {
        LayoutInflater inflater = LayoutInflater.from(parent);
        final View dialogView = inflater.inflate(R.layout.activity_dialog_expense, null);
        new AlertDialog.Builder(parent)
                .setTitle("New Expense")
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, (dialog, id) -> getExpenseFromDialog(dialogView))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void getExpenseFromDialog(View dialogView) {
        final String title = ((EditText) dialogView.findViewById(R.id.expense_edit_title)).getText().toString();
        final String payee = dialogView.findViewById(R.id.expense_edit_payee).toString();
        int cost = 0;
        try {
            cost = Integer.parseInt(((EditText) dialogView.findViewById(R.id.expense_edit_cost)).getText().toString());
        }catch (Exception e){
            // Only possible bad input is empty field
        }
        List<String> residents = billsharer.getResidents();
        HashMap<String, Double> shares = new HashMap<>();
        for (String resident : residents) {
            shares.put(resident, ((double) cost)/residents.size());
        }
        billsharer.addExpense(new Expense(title, cost, payee, shares));
        this.notifyItemInserted(billsharer.getExpenses().size()-1);
    }

    /*
    public void editExpense(Expense expense, int pos) {
        billsharer.editExpense(expense, pos);
        notifyItemChanged(pos);
    }*/

    public void removeExpense(Context ctx, Expense expense, int pos) {
        new AlertDialog.Builder(ctx)
                .setTitle("Remove Expense")
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    billsharer.removeExpense(expense);
                    this.notifyItemRemoved(pos);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    public void deleteBillsharer() {
        Billsharer.deleteBillsharer(billsharer.getOnlineReference());
        billsharer = null;
    }

    @NonNull
    @Override
    public ExpenseAdapter.ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ExpenseViewHolder(inflater.inflate(R.layout.expense_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = billsharer.getExpenses().get(position);
        holder.textView.setText(expense.toText());
        holder.removeCheck.setOnClickListener(l ->
                removeExpense(l.getContext(), expense, position)
        );
    }

    @Override
    public int getItemCount() {
        return billsharer.getExpenses().size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageButton removeCheck;

        public ExpenseViewHolder(View expenseView) {
            super(expenseView);
            textView = expenseView.findViewById(R.id.expense_text);
            removeCheck = expenseView.findViewById(R.id.expense_remove_check);
        }
    }

}
