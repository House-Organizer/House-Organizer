package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventCreationFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder form = new AlertDialog.Builder(getActivity());
        form.setTitle(R.string.event_creation_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the data back to addEvent()
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss()).
                setView(getActivity().findViewById(R.id.event_creation));

        return form.create();
    }
}
