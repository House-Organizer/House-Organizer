package com.github.houseorganizer.houseorganizer.billsharer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

/**
 * An adapter to RecyclerView for a list of Debt inside a Billsharer.
 */
public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtHolder> {

    private Billsharer billsharer;

    public DebtAdapter(Billsharer bs) {
        billsharer = bs;
    }

    public Billsharer getBillsharer() {
        return billsharer;
    }

    public void setBillsharer(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    /**
     * Shows a AlertDialog for the confirmation of the payment of the debt, if confirmed it removes
     * the debt by adding an expense. (Cf. removeDebt inside Billsharer)
     * @param ctx : the current Context
     * @param debt : the debt to be removed
     * @param pos : the position of the debt
     */
    public void removeDebt(Context ctx, Debt debt, int pos) {
        new AlertDialog.Builder(ctx)
                .setTitle("Confirm payment of this debt ?")
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    billsharer.removeDebt(debt);
                    this.notifyItemRemoved(pos);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    /**
     * The holder inside the RecyclerView for a single debt
     */
    public static class DebtHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageButton removeCheck;

        public DebtHolder(@NonNull View debtView) {
            super(debtView);
            textView = debtView.findViewById(R.id.debt_text);
            removeCheck = debtView.findViewById(R.id.debt_remove_check);
        }
    }

    @NonNull
    @Override
    public DebtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.debt_row, parent, false);
        return new DebtAdapter.DebtHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtHolder holder, int position) {
        Debt debt = billsharer.getDebts().get(position);
        holder.textView.setText(debt.toText());
        holder.removeCheck.setOnClickListener(l ->
                removeDebt(l.getContext(), debt, position)
        );
    }

    @Override
    public int getItemCount() {
        return billsharer.getDebts().size();
    }
}
