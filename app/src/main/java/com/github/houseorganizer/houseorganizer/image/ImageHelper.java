package com.github.houseorganizer.houseorganizer.image;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.houseorganizer.houseorganizer.R;

public class ImageHelper {
    public static void showImagePopup(Uri uri, Context ctx) {
        Dialog attachmentPreview = new Dialog(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.image_dialog, null);
        ImageView attachmentImage = popupView.findViewById(R.id.image_dialog);
        Glide.with(ctx)
                .load(uri.toString())
                .into(attachmentImage);
        attachmentPreview.setContentView(popupView);
        attachmentPreview.show();
    }
}
