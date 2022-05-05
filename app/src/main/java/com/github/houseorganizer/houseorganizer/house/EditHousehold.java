package com.github.houseorganizer.houseorganizer.house;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EditHousehold extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String householdId;
    private DocumentReference currentHousehold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_household);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        this.householdId = intent.getStringExtra(HouseSelectionActivity.HOUSEHOLD_TO_EDIT);
        this.currentHousehold = firestore.collection("households").document(householdId);

        firestore.collection("households").document(householdId).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> householdData = document.getData();
                    if (householdData != null) {
                        TextView tv = findViewById(R.id.edit_household_name);
                        // Ignore IDE null warning as check is done above
                        tv.setText(householdData.getOrDefault("name", "No house name").toString());
                    }
                });
    }

    private boolean verifyEmail(String email, View view) {
        if (!Verifications.verifyEmailHasCorrectFormat(email)) {
            Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean verifyEmailInput(TextView emailView, View view) {
        String email = emailView.getText().toString();
        return verifyEmail(email, view);
    }

    public void addUser(View view) {
        TextView emailView = findViewById(R.id.editTextAddUser);
        if (!verifyEmailInput(findViewById(R.id.editTextAddUser), view)) {
            return;
        }
        String email = emailView.getText().toString();

        mAuth
                .fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    List<String> signInMethods = task.getResult().getSignInMethods();

                    if (signInMethods != null && signInMethods.size() > 0)
                        addUserIfNotPresent(email, view);
                });
    }

    public void addUserIfNotPresent(String email, View view) {
        firestore.collection("households").document(householdId).get()
                .addOnCompleteListener(task -> {
                    Map<String, Object> householdData = task.getResult().getData();
                    if (householdData != null) {
                        List<String> listOfUsers =
                                (List<String>) householdData.getOrDefault("residents", "[]");
                        Long num_users = (Long) householdData.get("num_members");
                        if (!listOfUsers.contains(email)) {
                            currentHousehold.update("residents", FieldValue.arrayUnion(email));
                            currentHousehold.update("num_members", num_users + 1);
                            Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.add_user_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.duplicate_user),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void transmitOwnership(View view) {
        TextView emailView = findViewById(R.id.editTextChangeOwner);
        if (!verifyEmailInput(emailView, view)) {
            return;
        }
        String new_owner_email = emailView.getText().toString();

        mAuth
                .fetchSignInMethodsForEmail(new_owner_email)
                .addOnCompleteListener(task -> {
                    SignInMethodQueryResult querySignIn = task.getResult();
                    List<String> signInMethods = querySignIn.getSignInMethods();
                    if (signInMethods != null && signInMethods.size() > 0) {
                        changeOwner(new_owner_email, view);
                    }
                });
    }

    public void changeOwner(String email, View view) {
        firestore.collection("households").document(householdId).get()
                .addOnCompleteListener(task -> {
                    Map<String, Object> householdData = task.getResult().getData();
                    if (householdData != null) {
                        List<String> listOfUsers =
                                (List<String>) householdData.getOrDefault("residents", "[]");
                        if (listOfUsers.contains(email)) {
                            currentHousehold.update("owner", email);

                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.owner_change_success),
                                    Toast.LENGTH_SHORT).show();

                            // User is not owner anymore
                            Intent intent = new Intent(getApplicationContext(), HouseSelectionActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void removeUser(View view) {
        TextView emailView = findViewById(R.id.editTextRemoveUser);
        if (!verifyEmailInput(emailView, view)) {
            return;
        }
        String email = emailView.getText().toString();

        if (email.equals(mAuth.getCurrentUser().getEmail())) {
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.cant_remove_yourself),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask.addOnCompleteListener(task -> {
            int sizeOfSignInMethods = task.getResult().getSignInMethods().size();
            if (sizeOfSignInMethods > 0) {
                removeUserFromHousehold(email, view);
            }
        });
    }

    public void removeUserFromHousehold(String email, View view) {
        firestore.collection("households").document(householdId).get()
                .addOnCompleteListener(task -> {
                    Map<String, Object> householdData = task.getResult().getData();
                    if (householdData != null) {
                        List<String> residents =
                                (List<String>) householdData.getOrDefault("residents", "[]");
                        Long num_users = (Long) householdData.get("num_members");
                        if (residents.contains(email)) {
                            currentHousehold.update("num_members", num_users - 1);
                            currentHousehold.update("residents", FieldValue.arrayRemove(email));
                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.remove_user_success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void deleteDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The household calendar and lists will be deleted too. " +
                "Are you sure you want to delete this household?");
        builder.setTitle("Delete household");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteCalendar(view);
                deleteGroceryList(view);
                deleteTaskList(view);
                deleteHousehold(view);
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void deleteGroceryList(View view) {
        // TODO : The grocery list is not linked to households yet
    }

    public void deleteTaskList(View view) {
        OnFailureListener tlDeletionFailed =
                exception -> Toast.makeText(getApplicationContext(),
                        "Cannot remove task list", Toast.LENGTH_SHORT).show();

        firestore.collection("task_lists")
                .whereEqualTo("hh-id", currentHousehold.getId())
                .get()
                .addOnSuccessListener(docRefList -> {
                    assert docRefList.getDocuments().size() == 1;

                    DocumentSnapshot metadataSnap = docRefList.getDocuments().get(0);

                    List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                            metadataSnap.getData().getOrDefault("task-ptrs", new ArrayList<>());

                    assert taskPtrs != null;
                    Tasks.whenAllComplete(
                            taskPtrs.stream()
                            .map(DocumentReference::delete)
                            .map(t -> t.addOnFailureListener(tlDeletionFailed))
                            .collect(Collectors.toList())
                    ).addOnCompleteListener(allTasks -> {
                                if (allTasks.getResult().stream().allMatch(Task::isSuccessful)) {
                                    metadataSnap.getReference().delete().addOnFailureListener(tlDeletionFailed);
                                }
                            });
                })
                .addOnFailureListener(tlDeletionFailed);
    }

    public void deleteCalendar(View view) {
        firestore.collection("events")
                .whereEqualTo("household", currentHousehold)
                .get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                for (QueryDocumentSnapshot document : task1.getResult()) {
                    firestore.collection("events").document(document.getId()).delete()
                            .addOnCompleteListener(task2 -> {
                                if(!task2.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.remove_calendar_failure), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            } else {
                Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.remove_calendar_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteHousehold(View view) {
        firestore.collection("households").document(householdId).delete()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                view.getContext().getString(R.string.remove_household_success),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), HouseSelectionActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.remove_household_failure), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}