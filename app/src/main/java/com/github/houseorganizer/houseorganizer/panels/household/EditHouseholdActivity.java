package com.github.houseorganizer.houseorganizer.panels.household;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.house.Verifications;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.github.houseorganizer.houseorganizer.util.Util;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EditHouseholdActivity extends ThemedAppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String householdId;
    private DocumentReference currentHousehold;
    private ActivityResultLauncher<String> galleryLauncher;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_household);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::pushImageToHousehold);

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

    /**
     * Launchs gallery
     * @param view  base class for the changeImageOfHousehold image button on the edit household
     *              activity
     */
    public void pickImageForHousehold(View view){
        galleryLauncher.launch("image/*");
    }

    /**
     * Uploads image of a household from a content URI to the firebase StorageReference
     * @param uri content URI of the household's image
     */
    public void pushImageToHousehold(Uri uri){
        if (uri == null) return;
        StorageReference imageRef = storage.getReference().child("house_" + householdId);
        imageRef.putFile(uri);
        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.changed_picture), Toast.LENGTH_SHORT).show();
    }

    /**
     * Verifies email has the correct format
     * @param email email of the user to be verified
     * @param view  base class for the buttons on the edit household activity
     * @return      whether the email is correct
     */
    private boolean verifyEmail(String email, View view) {
        if (!Verifications.verifyEmailHasCorrectFormat(email)) {
            Toast.makeText(getApplicationContext(), view.getContext().getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Gets email from the given text view and calls to verifyEmail to verify it
     * @param emailView text view with the email to be verified
     * @param view      base class for the buttons on the edit household activity
     * @return          whether the email is correct
     */
    private boolean verifyEmailInput(TextView emailView, View view) {
        String email = emailView.getText().toString();
        return verifyEmail(email, view);
    }

    /**
     * First checks if the email specified in the editTextAddUser EditText is correct and if so
     * calls to addUserIfNotPresent method with this email
     * @param view  base class for the imageButtonAddUser image button on the edit household
     *              activity
     */
    public void addUser(View view) {
        EspressoIdlingResource.increment();

        TextView emailView = findViewById(R.id.editTextAddUser);
        if (!verifyEmailInput(findViewById(R.id.editTextAddUser), view)) {
            EspressoIdlingResource.decrement();
            return;
        }
        String email = emailView.getText().toString();

        mAuth
                .fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    List<String> signInMethods = task.getResult().getSignInMethods();

                    if (signInMethods != null && signInMethods.size() > 0)
                        addUserIfNotPresent(email, view);
                    else
                        EspressoIdlingResource.decrement();
                });
    }

    /**
     * Adds user to a household on firebase
     * @param email email of the user to be added
     * @param view  base class for the imageButtonAddUser image button on the edit household
     *              activity
     */
    private void addUserIfNotPresent(String email, View view) {
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

                    EspressoIdlingResource.decrement();
                });
    }

    /**
     * First checks if the email specified in the editTextChangeOwner EditText is correct and if so
     * calls to changeOwner method with this email
     * @param view  base class for the imageButtonChangeOwner image button on the edit household
     *              activity
     */
    public void transmitOwnership(View view) {
        EspressoIdlingResource.increment();

        TextView emailView = findViewById(R.id.editTextChangeOwner);
        if (!verifyEmailInput(emailView, view)) {
            EspressoIdlingResource.decrement();
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
                    } else {
                        EspressoIdlingResource.decrement();
                    }
                });
    }

    /**
     * Changes owner of a household in firebase
     * @param email email of the new owner
     * @param view  base class for the imageButtonChangeOwner image button on the edit household
     *              activity
     */
    private void changeOwner(String email, View view) {
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
                    EspressoIdlingResource.decrement();
                });
    }

    /**
     * First checks if the email specified in the editTextRemoveUser EditText is correct and if so
     * calls to removeUserFromHousehold method with this email
     * @param view  base class for the imageButtonRemoveUser image button on the edit household
     *              activity
     */
    public void removeUser(View view) {
        EspressoIdlingResource.increment();

        TextView emailView = findViewById(R.id.editTextRemoveUser);
        if (!verifyEmailInput(emailView, view)) {
            EspressoIdlingResource.decrement();
            return;
        }
        String email = emailView.getText().toString();

        if (email.equals(mAuth.getCurrentUser().getEmail())) {
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.cant_remove_yourself),
                    Toast.LENGTH_SHORT).show();
            EspressoIdlingResource.decrement();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask.addOnCompleteListener(task -> {
            int sizeOfSignInMethods = task.getResult().getSignInMethods().size();
            if (sizeOfSignInMethods > 0) {
                removeUserFromHousehold(email, view);
            } else {
                EspressoIdlingResource.decrement();
            }
        });
    }

    /**
     * Displays a QR code that can be scanned in the app to join this household
     * @param view The view in which to display the QR code
     */
    public void showInviteQR(View view) {
        Dialog qrDialog = new Dialog(this);
        try {
            @SuppressLint("InflateParams") View qrDialogView = LayoutInflater.from(this).inflate(R.layout.image_dialog, null);
            ImageView qrView = qrDialogView.findViewById(R.id.image_dialog);
            qrView.setImageBitmap(createQRCodeBitmap(householdId));
            qrDialog.setContentView(qrDialogView);
            qrDialog.show();

        } catch (WriterException e) {
            Util.logAndToast(this.toString(), "generateQRCode:failure", e, getApplicationContext(), "Could not generate a QR code");
        }
    }

    /**
     * Creates a QR code bitmap for the current household
     * @param householdId       The id of the household
     * @return                  The bitmap of the generated QR code
     * @throws WriterException  If the bitmap could not be created
     */
    public static Bitmap createQRCodeBitmap(String householdId) throws WriterException {
        int length = 800;
        BitMatrix qrCode = new QRCodeWriter().encode(householdId, BarcodeFormat.QR_CODE, length, length);
        return Bitmap.createBitmap(IntStream.range(0, length)
                        .flatMap(h -> IntStream.range(0, length)
                                .map(w -> qrCode.get(w, h) ? Color.BLACK : Color.WHITE))
                        .toArray(),
                length, length, Bitmap.Config.ARGB_8888);
    }

    /**
     * Removes user from the current household
     * @param email email of the user to be removed
     * @param view  base class for the imageButtonRemoveUser image button on the edit household
     *              activity
     */
    private void removeUserFromHousehold(String email, View view) {
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
                            removeUserFromTaskAssignees(email);
                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.remove_user_success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    EspressoIdlingResource.decrement();
                });
    }

    /**
     * Removes user from the assignees of a task in the current task list if present
     * @param email email of the user to be removed
     */
    private void removeUserFromTaskAssignees(String email) {
        firestore.collection("task_lists")
                .whereEqualTo("hh-id", householdId)
                .get()
                .addOnSuccessListener(docSnaps -> {
                    assert docSnaps.size() == 1;

                    Map<String, Object> metadata = docSnaps.iterator().next().getData();
                    List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                            metadata.getOrDefault("task-ptrs", new ArrayList<>());

                    assert taskPtrs != null;
                    taskPtrs.forEach(taskPtr ->
                            taskPtr.update("assignees", FieldValue.arrayRemove(email)));

                })
                .addOnFailureListener(toastExceptionFailureListener("Could not update task assignees"));
    }

    /**
     * Shows delete dialog to get the confirmation from the user and if so, deletes the calendar,
     * shop list, billsharer and task list of the current household apart from the household
     * @param view  base class for the deleteButton button on the edit household activity
     */
    public void deleteDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The household calendar and lists will be deleted too. " +
                "Are you sure you want to delete this household?");
        builder.setTitle("Delete household");
        builder.setCancelable(false);
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Yes", (dialog, which) -> {

            List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            tasks.add(deleteCalendar(view));
            tasks.add(deleteFromHousehold("shop_lists", "Could not delete groceries"));
            tasks.add(deleteFromHousehold("billsharers", "Could not delete billsharer"));
            tasks.add(deleteTaskList());

            Tasks.whenAllComplete(tasks)
                    .addOnFailureListener(toastExceptionFailureListener("Could not delete household"))
                    .addOnSuccessListener(t -> deleteHousehold(view));
            dialog.cancel();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Deletes given root document from the current household
     * @param root          document to be deleted
     * @param errorMessage  text shown when an error occurs
     * @return              Task<QuerySnapshot> with info about the firebase task to delete the
     *                      document
     */
    public Task<QuerySnapshot> deleteFromHousehold(String root, String errorMessage) {
        return firestore.collection(root)
                .whereEqualTo("household", currentHousehold)
                .get().addOnCompleteListener(doc ->{
                    if(doc.getResult().size() < 1)return;
                    doc.getResult().getDocuments().get(0).getReference().delete();
                }).addOnFailureListener(toastExceptionFailureListener(errorMessage));
    }

    /**
     * Creates toast to notify the user about a failure in the app
     * @param message   text shown to the user
     * @return          listener called when a Task fails with an exception
     */
    private OnFailureListener toastExceptionFailureListener(String message) {
        return exception -> Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Deletes task list for the current household from firebase
     * @return  Task<QuerySnapshot> with info about the firebase task to delete the task list
     */
    private Task<QuerySnapshot> deleteTaskList() {
        OnFailureListener tlDeletionFailed = toastExceptionFailureListener("Cannot remove task list");

        return firestore.collection("task_lists")
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

    /**
     * Deletes calendar for the current household from firebase
     * @param view  base class for the deleteButton button on the edit household activity
     * @return      Task<QuerySnapshot> with info about the firebase task to delete the calendar
     */
    private Task<QuerySnapshot> deleteCalendar(View view) {
        return firestore.collection("events")
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

    /**
     * Deletes current household from firebase and sends the user back to the house selection activity
     * @param view  base class for the deleteButton button on the edit household activity
     */
    private void deleteHousehold(View view) {
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), HouseSelectionActivity.class);
        startActivity(intent);
    }
}
