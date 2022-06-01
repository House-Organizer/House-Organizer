package com.github.houseorganizer.houseorganizer.calendar;

import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing any adapter linked to a calendar
 */
public abstract class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ActivityResultLauncher<String> getPicture;

    Calendar calendar;
    String eventToAttach;
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Creates a new adapter for a calendar RecyclerView
     *
     * @param calendar      The calendar this adapter is for
     * @param getPicture    The handler for fetching an attachment for an event
     */
    public CalendarAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        this.calendar = calendar;
        this.getPicture = getPicture;
        generateItems(calendar.getEvents());
    }

    /**
     * Switches views of the adapter, this creates a new adapter from another subclass
     *
     * @return The new adapter from the other view subclass
     */
    public abstract CalendarAdapter switchView();

    abstract void generateItems(List<Calendar.Event> events);

    /**
     * Displays an alert dialog to create a new event on this calendar
     *
     * @param ctx           The context of the activity
     * @param currentHouse  The current house the calendar is linked to
     * @param errMessage    The message to display in case of an error
     */
    public void showAddEventDialog(Context ctx, DocumentReference currentHouse, String errMessage) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View dialogView = inflater.inflate(R.layout.event_creation, null);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.event_creation_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add_text, (dialog, id) -> {
                    Task<DocumentReference> pushTask = pushEventFromDialog(dialogView, currentHouse);
                    if (pushTask != null) {
                            pushTask.addOnSuccessListener(documentReference -> refreshCalendarView(ctx, currentHouse, errMessage, calendar.getView() == Calendar.CalendarView.MONTHLY));
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private Task<DocumentReference> pushEventFromDialog(View dialogView, DocumentReference currentHouse) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (Calendar.Event.putEventStringsInData(event, data)) {
            return null;
        }
        data.put("household", currentHouse);
        return db.collection("events").add(data);
    }

    /**
     * Refreshes the calendar adapter contents using firebase and updates its view
     *
     * @param ctx           The context of the activity
     * @param currentHouse  The current house the calendar is linked to
     * @param errMessage    The message to display in case of an error
     * @param withPast      The boolean that decides whether to retrieve past events or not (compared to the current date and time)
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshCalendarView(Context ctx, DocumentReference currentHouse, String errMessage, boolean withPast) {
        long timeThreshold = withPast ? LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC) : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        db.collection("events").whereEqualTo("household", currentHouse)
                .whereGreaterThan("start", timeThreshold).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Calendar.Event> newEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Calendar.Event event = new Calendar.Event(
                                    document.getString("title"),
                                    document.getString("description"),
                                    LocalDateTime.ofEpochSecond(document.getLong("start"), 0, ZoneOffset.UTC),
                                    document.getLong("duration") == null ? 0 : document.getLong("duration"),
                                    document.getId());
                            newEvents.add(event);
                        }
                        notifyDataSetChanged();
                        calendar.setEvents(newEvents);
                        if(currentHouse != null) LocalStorage.pushEventsOffline(ctx, currentHouse.getId(), newEvents);
                        generateItems(newEvents);
                    } else {
                        logAndToast(ctx.toString(), errMessage, task.getException(),
                                ctx, ctx.getString(R.string.refresh_calendar_fail));
                    }
                });
    }

    /**
     * Uploads an attachment to firebase storage
     *
     * @param uri The uri of the attachment to upload
     */
    public void pushAttachment(Uri uri) {
        if (uri == null) return;
        // Store the image on firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // this creates the reference to the picture
        StorageReference imageRef = storage.getReference().child("event_" + eventToAttach);
        imageRef.putFile(uri);
    }
}
