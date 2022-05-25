package com.github.houseorganizer.houseorganizer.billsharer;


import static java.lang.Math.abs;

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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
        AtomicReference<HashMap<String, Double>> shares = new AtomicReference<>();
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
                .setPositiveButton(R.string.confirm, (dialog, id) -> getExpenseFromDialog(dialogView, shares.get()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(l ->
                specifyShares(alertDialog, dialogView.getContext(), getCostFromDialog(dialogView), shares)
        );
    }

    private void specifyShares(AlertDialog dialog, Context ctx, double cost, AtomicReference<HashMap<String, Double>> shares) {
        dialog.dismiss();

        View sharesEditor = LayoutInflater.from(ctx).inflate(R.layout.assignee_editor, null);
        RecyclerView sharesView = sharesEditor.findViewById(R.id.assignee_editor);
        SharesAdapter sharesAdapter = new SharesAdapter(initShares(cost));

        sharesView.setAdapter(sharesAdapter);
        sharesView.setLayoutManager(new LinearLayoutManager(ctx));

        AlertDialog alertDialog = new AlertDialog.Builder(ctx)
                .setTitle("Specify shares")
                .setView(sharesEditor)
                .setNegativeButton(R.string.cancel, (d, i) -> {
                    shares.set(null);
                    dialog.show();
                })
                .setPositiveButton(R.string.confirm, null)
                .show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener( l ->
                alertDoesntAddUp(alertDialog, ctx, sharesAdapter, cost, shares)
        );
    }

    private void alertDoesntAddUp(AlertDialog dialog, Context ctx, SharesAdapter adapter, double cost, AtomicReference<HashMap<String, Double>> shares) {
        dialog.dismiss();
        if (addsUpToTotal(cost, adapter.getShares())) {
            shares.set(adapter.getShares());
        } else {
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.adds_up_to_total)
                    .setPositiveButton(R.string.ok, (d, i) -> dialog.show())
                    .show();
        }
    }

    private boolean addsUpToTotal(double total, HashMap<String, Double> shares) {
        double sum = 0;
        for (double val : shares.values()) {
            sum += val;
        }

        return abs(sum - total) < 0.01;
    }

    private void getExpenseFromDialog(View dialogView, HashMap<String, Double> shares) {
        String title = ((EditText) dialogView.findViewById(R.id.expense_edit_title)).getText().toString();
        Spinner spinner = dialogView.findViewById(R.id.expense_edit_payee);

        double cost = getCostFromDialog(dialogView);
        if (shares == null) {
            shares = initShares(cost);
        }

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

    public void removeExpense(Context ctx, int pos) {
        new AlertDialog.Builder(ctx)
                .setTitle("Remove Expense")
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    billsharer.removeExpense(pos);
                    this.notifyItemRemoved(pos);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
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
                removeExpense(l.getContext(), position)
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
