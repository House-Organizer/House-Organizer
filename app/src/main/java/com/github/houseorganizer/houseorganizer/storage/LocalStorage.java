package com.github.houseorganizer.houseorganizer.storage;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class LocalStorage {
    public static boolean writeTxtToFile(Context context, String fileName, String content){
        File path = context.getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(content.getBytes(StandardCharsets.UTF_8));
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
