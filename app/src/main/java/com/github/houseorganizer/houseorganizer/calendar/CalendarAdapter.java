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
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
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

public abstract class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ActivityResultLauncher<String> getPicture;

    Calendar calendar;
    String eventToAttach;
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CalendarAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        this.calendar = calendar;
        this.getPicture = getPicture;
        generateItems(calendar.getEvents());
    }

    public abstract CalendarAdapter switchView();

    abstract void generateItems(List<Calendar.Event> events);

    public Calendar getCalendar() {
        return calendar;
    }

    public void showAddEventDialog(Context ctx, DocumentReference currentHouse, String errMessage) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View dialogView = inflater.inflate(R.layout.event_creation, null);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.event_creation_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> {
                    EspressoIdlingResource.increment();

                    Task<DocumentReference> pushTask = pushEventFromDialog(dialogView, currentHouse);
                    if (pushTask != null) {
                            pushTask.addOnSuccessListener(documentReference -> refreshCalendarView(ctx, currentHouse, errMessage, calendar.getView() == Calendar.CalendarView.MONTHLY));
                    }
                    dialog.dismiss();

                    EspressoIdlingResource.decrement();
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

    @SuppressLint("NotifyDataSetChanged")
    public void refreshCalendarView(Context ctx, DocumentReference currentHouse, String errMessage, boolean withPast) {
        EspressoIdlingResource.increment();
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
                        generateItems(newEvents);
                        EspressoIdlingResource.decrement();
                    } else {
                        logAndToast(ctx.toString(), errMessage, task.getException(),
                                ctx, ctx.getString(R.string.refresh_calendar_fail));
                        EspressoIdlingResource.decrement();
                    }
                });
    }

    public void pushAttachment(Uri uri) {
        // Store the image on firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // this creates the reference to the picture
        StorageReference imageRef = storage.getReference().child("event_" + eventToAttach);
        imageRef.putFile(uri);
    }
}
