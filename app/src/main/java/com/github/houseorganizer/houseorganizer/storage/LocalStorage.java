package com.github.houseorganizer.houseorganizer.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LocalStorage {

    public static final String OFFLINE_STORAGE_CALENDAR = "offline_calendar.json";

    public static boolean writeTxtToFile(Context context, String filename, String content){
        File path = context.getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, filename));
            writer.write(content.getBytes(StandardCharsets.UTF_8));
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean retrieve_calendar(Context context){
        try {
            FileInputStream fis = context.openFileInput(OFFLINE_STORAGE_CALENDAR);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            String s = buffer.toString();

            System.out.println("retrieved:" + s); //TODO REMOVE DEBUG

            Type type = TypeToken.getParameterized(ArrayList.class, OfflineEvent.class).getType();
            Gson gson = new Gson();
            ArrayList<OfflineEvent> events = gson.fromJson(s, type);
            for(OfflineEvent event : events){
                System.out.println(event); //TODO REMOVE DEBUG
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean pushEventsOffline(Context context, ArrayList<Calendar.Event> events){
        ArrayList<LocalStorage.OfflineEvent> offlineEvents = new ArrayList<>();
        for(Calendar.Event event : events){
            long duration = event.getDuration();
            offlineEvents.add(new OfflineEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getStart().toString(),
                    duration,
                    event.getId()
                    ));
        }
        System.out.println("pushed:" + offlineEvents); //TODO REMOVE DEBUG
        return writeTxtToFile(context, OFFLINE_STORAGE_CALENDAR, new Gson().toJson(offlineEvents));
    }

    public static class OfflineEvent{
        private final String title;
        private final String description;
        private final String start;
        private final long duration;
        private final String id;

        public OfflineEvent(String title, String description, String start, long duration, String id) {
            this.title = title;
            this.description = description;
            this.start = start;
            this.duration = duration;
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return "OfflineEvent{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", start='" + start + '\'' +
                    ", duration='" + duration + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }
}
