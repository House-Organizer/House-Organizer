package com.github.houseorganizer.houseorganizer.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

public class Util {
    public static boolean putEventStringsInData(String title, String desc, String date, String duration, Map<String, Object> data) {
        data.put("title", title);
        data.put("description", desc);
        try {
            TemporalAccessor start = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(date);
            data.put("start", LocalDateTime.from(start).toEpochSecond(ZoneOffset.UTC));
            data.put("duration", Integer.valueOf(duration));
        } catch(Exception e) {
            return true;
        }
        return false;
    }
}
