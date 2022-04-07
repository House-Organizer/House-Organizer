package com.github.houseorganizer.houseorganizer.image;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.houseorganizer.houseorganizer.R;

public class ImageHelper {
    public static void showImagePopup(Uri uri, View v) {
        Dialog attachmentPreview = new Dialog(v.getContext());
        LayoutInflater inflater = LayoutInflater.from(v.getContext());
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.image_dialog, null);
        ImageView attachmentImage = popupView.findViewById(R.id.image_dialog);
        Glide.with(v.getContext())
                .load(uri.toString())
                .into(attachmentImage);
        attachmentPreview.setContentView(popupView);
        attachmentPreview.show();
    }
}
