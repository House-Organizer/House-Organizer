package com.github.houseorganizer.houseorganizer.calendar;

import static com.github.houseorganizer.houseorganizer.calendar.Calendar.CalendarView.MONTHLY;
import static com.github.houseorganizer.houseorganizer.calendar.Calendar.Event.putEventStringsInData;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar.Event;
import com.github.houseorganizer.houseorganizer.image.ImageHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private final ActivityResultLauncher<String> getPicture;

    private final Calendar calendar;
    private String eventToAttach;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventsAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        this.calendar = calendar;
        this.getPicture = getPicture;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public TextView dateView = null;
        public ImageButton attachView = null;
        public ViewHolder(View eventView) {
            super(eventView);

            if (calendar.getView() == MONTHLY) {
                titleView = eventView.findViewById(R.id.event_monthly_title);
            }
            else {
                titleView = eventView.findViewById(R.id.event_upcoming_title);
                dateView = eventView.findViewById(R.id.event_upcoming_date);
                attachView = eventView.findViewById(R.id.event_upcoming_attach);
            }
        }
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (calendar.getView() == MONTHLY) {
            return new ViewHolder(inflater.inflate(R.layout.calendar_monthly_cell, parent, false));
        }
        else {
            return new ViewHolder(inflater.inflate(R.layout.calendar_upcoming_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.ViewHolder holder, int position) {
        if (calendar.getView() == MONTHLY) {
            prepareMonthlyView(holder, position);
        }
        else {
            prepareUpcomingView(holder, position);
        }
    }

    public Calendar getCalendar() {
        return calendar;
    }

    private void prepareMonthlyView(ViewHolder holder, int position) {
        holder.titleView.setText(String.format(Locale.ENGLISH, "%d", position + 1));
        holder.titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(Integer.toString(position + 1)).setMessage("List of events for this day somehow")
                .setMessage("List of events for this day somehow").show());
    }

    private void prepareUpcomingView(EventsAdapter.ViewHolder holder, int position) {
        Event event = calendar.getEvents().get(position);
        holder.titleView.setText(event.getTitle());
        holder.titleView.setOnClickListener(v -> titleOnClickListener(event, v, position));
        holder.dateView.setText(holder.dateView.getContext().getResources().getString(R.string.calendar_upcoming_date,
                event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        holder.attachView.setOnClickListener(v -> setupPopupMenu(v, holder, event.getId()));
    }

    private void setupPopupMenu(View v, ViewHolder holder, String eventId) {
        PopupMenu attachmentMenu = new PopupMenu(v.getContext(), holder.attachView);

        attachmentMenu.getMenuInflater().inflate(R.menu.event_attachment_menu, attachmentMenu.getMenu());
        attachmentMenu.setOnMenuItemClickListener(menuItem -> {
            StorageReference attachment = storage.getReference().child(eventId + ".jpg");
            switch(menuItem.getTitle().toString()) {
                case "Attach":
                    eventToAttach = eventId;
                    getPicture.launch("image/*");
                    break;
                case "Show":
                    attachment.getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            ImageHelper.showImagePopup(task.getResult(), v);
                        }
                        else {
                            Toast.makeText(v.getContext(), "Could not find the attachment", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "Remove":
                    attachment.delete();
            }
            return true;
        });
        attachmentMenu.show();
    }

    private void titleOnClickListener(Event event, View v, int position) {
        new AlertDialog.Builder(v.getContext())
                .setTitle(event.getTitle())
                .setMessage(event.getDescription())
                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss())
                .setNegativeButton(R.string.delete, (dialog, id) -> {
                    removeEventFirestoreAndCalendar(event, calendar);
                    this.notifyItemRemoved(position);
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.edit, (dialog, id) ->{
                    final View dialogView = createEditDialog(v, event);
                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.event_editing_title)
                            .setView(dialogView)
                            .setPositiveButton(R.string.confirm, (editForm, editFormId) -> {
                                    editEvent(event, dialogView, position);
                                    editForm.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (editForm, editFormId) -> dialog.dismiss())
                            .show();
                }).show();
    }

    public void showAddEventDialog(Context ctx, DocumentReference currentHouse, String errMessage) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View dialogView = inflater.inflate(R.layout.event_creation, null);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.event_creation_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> {
                    Task<DocumentReference> pushTask = pushEventFromDialog(dialogView, currentHouse);
                    if (pushTask != null) {
                        pushTask.addOnSuccessListener(documentReference -> refreshCalendarView(ctx, currentHouse, errMessage));
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
        if (Event.putEventStringsInData(event, data)) {
            return null;
        }
        data.put("household", currentHouse);
        return db.collection("events").add(data);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshCalendarView(Context ctx, DocumentReference currentHouse, String errMessage) {
        db.collection("events")
                .whereEqualTo("household", currentHouse)
                .whereGreaterThan("start", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Event> newEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // We assume the stored data is well behaved since it got added in a well behaved manner.
                            Event event = new Event(
                                    document.getString("title"),
                                    document.getString("description"),
                                    LocalDateTime.ofEpochSecond(document.getLong("start"), 0, ZoneOffset.UTC),
                                    document.getLong("duration") == null ? 0 : document.getLong("duration"),
                                    document.getId());
                            newEvents.add(event);
                        }
                        notifyDataSetChanged();
                        calendar.setEvents(newEvents);
                    } else {
                        logAndToast(ctx.toString(), errMessage, task.getException(),
                                ctx, ctx.getString(R.string.refresh_calendar_fail));
                    }
                });
    }

    private void removeEventFirestoreAndCalendar(Event event, Calendar calendar) {
        db.collection("events")
                .document(event.getId())
                .delete();
        ArrayList<Event> newEvents = new ArrayList<>(calendar.getEvents());
        newEvents.remove(event);
        calendar.setEvents(newEvents);
    }

    private View createEditDialog(View v, Event event) {
        LayoutInflater inflater = LayoutInflater.from(v.getContext());
        @SuppressLint("InflateParams") View retView = inflater.inflate(R.layout.event_creation, null);
        ((EditText) retView.findViewById(R.id.new_event_title)).setText(event.getTitle());
        ((EditText) retView.findViewById(R.id.new_event_desc)).setText(event.getDescription());
        ((EditText) retView.findViewById(R.id.new_event_date)).setText(event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        ((EditText) retView.findViewById(R.id.new_event_duration)).setText(String.format(Locale.getDefault(), "%d", event.getDuration()));
        return retView;
    }

    private void editEvent(Event eventObj, View dialogView, int position) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        eventObj.setTitle(title);
        eventObj.setDescription(desc);
        try {
            eventObj.setStart(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            eventObj.setDuration(Integer.parseInt(duration));
        } catch(Exception e) {
            return;
        }
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (putEventStringsInData(event, data)) {
            return;
        }
        db.collection("events").document(eventObj.getId()).set(data, SetOptions.merge());
        this.notifyItemChanged(position);
    }

    public void pushAttachment(Uri uri) {
        // Store the image on firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // this creates the reference to the picture
        StorageReference imageRef = storage.getReference().child(eventToAttach + ".jpg");
        imageRef.putFile(uri);
    }

    @Override
    public int getItemCount() {
        if (calendar.getView() == MONTHLY) {
            return YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()).lengthOfMonth();
        }
        else {
            return calendar.getEvents().size();
        }
    }
}
