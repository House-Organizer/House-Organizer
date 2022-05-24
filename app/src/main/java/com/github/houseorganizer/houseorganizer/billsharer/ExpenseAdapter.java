package com.github.houseorganizer.houseorganizer.billsharer;


import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.task.TaskAssigneeAdapter;

import java.util.HashMap;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseHolder> {

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
        Spinner spinner = dialogView.findViewById(R.id.expense_edit_payee);
        ArrayAdapter<String> aa = new ArrayAdapter<>(dialogView.getContext(),
                android.R.layout.simple_spinner_dropdown_item, billsharer.getResidents());
        spinner.setAdapter(aa);
        AlertDialog alertDialog = new AlertDialog.Builder(parent)
                .setTitle(R.string.new_expense)
                .setView(dialogView)
                .setNeutralButton(R.string.specify_shares, null)
                .setPositiveButton(R.string.confirm, (dialog, id) -> getExpenseFromDialog(dialogView))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                l -> specifyShares(alertDialog, getCostFromDialog(dialogView), dialogView.getContext())
        );
    }

    private void specifyShares(AlertDialog dialog, double cost, Context ctx) {
        dialog.dismiss();

        View sharesEditor = LayoutInflater.from(ctx).inflate(R.layout.assignee_editor, null);
        RecyclerView sharesView = sharesEditor.findViewById(R.id.assignee_editor);
        SharesAdapter sharesAdapter = new SharesAdapter(initShares(cost));

        sharesView.setAdapter(sharesAdapter);
        sharesView.setLayoutManager(new LinearLayoutManager(ctx));

        new AlertDialog.Builder(ctx)
                .setView(sharesEditor)
                .setOnDismissListener(d -> dialog.show())
                .show();
    }

    private void getExpenseFromDialog(View dialogView) {
        String title = ((EditText) dialogView.findViewById(R.id.expense_edit_title)).getText().toString();
        Spinner spinner = dialogView.findViewById(R.id.expense_edit_payee);

        double cost = getCostFromDialog(dialogView);
        HashMap<String, Double> shares = initShares(cost);

        billsharer.addExpense(new Expense(title, cost, spinner.getSelectedItem().toString(), shares));
        this.notifyItemInserted(billsharer.getExpenses().size()-1);
    }

    private double getCostFromDialog(View dialogView) {
        double cost = 0f;
        try {
            cost = Double.parseDouble(((EditText) dialogView.findViewById(R.id.expense_edit_cost)).getText().toString());
        }catch (Exception e){
            // Only possible bad input is empty field
        }
        return cost;
    }

    private HashMap<String, Double> initShares(double cost) {
        List<String> residents = billsharer.getResidents();
        HashMap<String, Double> shares = new HashMap<>();
        for (String resident : residents) {
            shares.put(resident, cost/residents.size());
        }
        return shares;
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
                    billsharer.removeExpense(pos);
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
    public ExpenseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.expense_row, parent, false);
        return new ExpenseHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseHolder holder, int position) {
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

    public static class ExpenseHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageButton removeCheck;

        public ExpenseHolder(@NonNull View expenseView) {
            super(expenseView);
            textView = expenseView.findViewById(R.id.expense_text);
            removeCheck = expenseView.findViewById(R.id.expense_remove_check);
        }
    }

}
