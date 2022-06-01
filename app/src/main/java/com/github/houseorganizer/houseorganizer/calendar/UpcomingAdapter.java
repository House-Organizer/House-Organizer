package com.github.houseorganizer.houseorganizer.calendar;

import static com.github.houseorganizer.houseorganizer.calendar.Calendar.Event.putEventStringsInData;
import static com.github.houseorganizer.houseorganizer.util.interfaces.UpcomingRowItem.DELIMITER;
import static com.github.houseorganizer.houseorganizer.util.interfaces.UpcomingRowItem.EVENT;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import com.github.houseorganizer.houseorganizer.util.interfaces.UpcomingRowItem;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UpcomingAdapter extends CalendarAdapter {

    private ArrayList<UpcomingRowItem> items;

    public UpcomingAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        super(calendar, getPicture);
    }

    @Override
    public CalendarAdapter switchView() {
        return new MonthlyAdapter(calendar, getPicture);
    }

    @Override
    void generateItems(List<Event> events) {
        ArrayList<UpcomingRowItem> ret = new ArrayList<>();
        if (events.isEmpty()) {
            items = ret;
            return;
        }
        Event e1 = events.get(0);
        Event e2;
        ret.add(new Delimiter(e1.getStart().toLocalDate()));

        for (int i = 0; i < events.size(); i++) {
            e2 = events.get(i);
            if (!e1.getStart().toLocalDate().equals(e2.getStart().toLocalDate())) {
                ret.add(new Delimiter(e2.getStart().toLocalDate()));
            }
            ret.add(e2);
            e1 = e2;
        }

        items = ret;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public TextView descView;
        public ImageButton attachView;
        public EventViewHolder(View eventView) {
            super(eventView);
            titleView = eventView.findViewById(R.id.event_upcoming_title);
            descView = eventView.findViewById(R.id.event_upcoming_date);
            attachView = eventView.findViewById(R.id.event_upcoming_attach);
        }
    }

    public static class DelimiterViewHolder extends RecyclerView.ViewHolder {
        public TextView dayView;
        public DelimiterViewHolder(View delimiterView) {
            super(delimiterView);
            dayView = delimiterView.findViewById(R.id.calendar_delimiter_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == EVENT) {
            return new EventViewHolder(inflater.inflate(R.layout.calendar_upcoming_row, parent, false));
        }
        else {
            return new DelimiterViewHolder(inflater.inflate(R.layout.calendar_upcoming_delimiter, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == EVENT) {
            prepareUpcomingView((EventViewHolder) holder, position);
        }
        else {
            prepareDelimiter((DelimiterViewHolder) holder, position);
        }
    }

    private void prepareDelimiter(DelimiterViewHolder holder, int position) {
        Delimiter delimiter = ((Delimiter) items.get(position));
        holder.dayView.setText(delimiter.getDate().format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy")));
    }

    private void prepareUpcomingView(EventViewHolder holder, int position) {
        Event event = (Event) items.get(position);
        holder.titleView.setText(event.getTitle());
        holder.titleView.setOnClickListener(v -> titleOnClickListener(event, v.getContext(), position));
        holder.descView.setText(event.getDescription());
        holder.attachView.setOnClickListener(v -> setupPopupMenu(v.getContext(), holder, event.getId()));
    }

    private void setupPopupMenu(Context ctx, EventViewHolder holder, String eventId) {
        PopupMenu attachmentMenu = new PopupMenu(ctx, holder.attachView);

        attachmentMenu.getMenuInflater().inflate(R.menu.event_attachment_menu, attachmentMenu.getMenu());
        attachmentMenu.setOnMenuItemClickListener(menuItem -> {
            StorageReference attachment = storage.getReference().child("event_" + eventId);
            switch(menuItem.getTitleCondensed().toString()) {
                case "Attach":
                    eventToAttach = eventId;
                    getPicture.launch("image/*");
                    break;
                case "Show":
                    attachment.getDownloadUrl().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            ImageHelper.showImagePopup(task.getResult(), ctx);
                        }
                        else {
                            Toast.makeText(ctx, ctx.getString(R.string.no_attachment), Toast.LENGTH_SHORT).show();
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

    private void titleOnClickListener(Event event, Context ctx, int position) {
        if (getItemViewType(position) == EVENT) {
            new AlertDialog.Builder(ctx)
                    .setTitle(event.getTitle())
                    .setMessage(event.getDescription() + "\n\n" + ctx.getResources().getString(R.string.calendar_upcoming_date,
                            event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))))
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss())
                    .setNegativeButton(R.string.delete, (dialog, id) -> {
                        removeEventFirestoreAndAdapter(event, position);
                        storage.getReference().child("event_" + event.getId()).delete();
                        dialog.dismiss();
                    })
                    .setNeutralButton(R.string.edit, (dialog, id) -> {
                        final View dialogView = createEditDialog(ctx, event);
                        new AlertDialog.Builder(ctx)
                                .setTitle(R.string.event_editing_title)
                                .setView(dialogView)
                                .setPositiveButton(R.string.confirm, (editForm, editFormId) -> {
                                    editEvent(event, dialogView);
                                    editForm.dismiss();
                                })
                                .setNegativeButton(R.string.cancel, (editForm, editFormId) -> dialog.dismiss())
                                .show();
                    }).show();
        }
    }



    private void removeEventFirestoreAndAdapter(Event event, int position) {
        db.collection("events")
                .document(event.getId())
                .delete();
        ArrayList<Event> newEvents = new ArrayList<>(calendar.getEvents());
        newEvents.remove(event);
        calendar.setEvents(newEvents);
        items.remove(position);
        this.notifyItemRemoved(position);
        if (items.get(position - 1).getType() == DELIMITER && (items.size() == position || items.get(position).getType() == DELIMITER)) {
            items.remove(position - 1);
            this.notifyItemRemoved(position - 1);
        }
    }

    private View createEditDialog(Context ctx, Event event) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        @SuppressLint("InflateParams") View retView = inflater.inflate(R.layout.event_creation, null);
        ((EditText) retView.findViewById(R.id.new_event_title)).setText(event.getTitle());
        ((EditText) retView.findViewById(R.id.new_event_desc)).setText(event.getDescription());
        ((TextView) retView.findViewById(R.id.new_event_picked_date)).setText(event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        retView.findViewById(R.id.new_event_date).setOnClickListener(v -> showDateTimePicker(ctx, v, event.getStart()));
        return retView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void editEvent(Event eventObj, View dialogView) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((TextView) dialogView.findViewById(R.id.new_event_picked_date)).getText().toString();
        eventObj.setTitle(title);
        eventObj.setDescription(desc);
        try {
            eventObj.setStart(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch(Exception e) {
            return;
        }
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        if (putEventStringsInData(event, data)) {
            return;
        }
        db.collection("events").document(eventObj.getId()).set(data, SetOptions.merge());
        // Sorts the events again since the date of eventObj might have changed
        calendar.setEvents(new ArrayList<>(calendar.getEvents()));
        generateItems(calendar.getEvents());
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
