package com.github.houseorganizer.houseorganizer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomMatchers {
    public static Matcher<View> withBitmap(final Bitmap expected) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View target) {
                if (!(target instanceof ImageView)){
                    return false;
                }
                ImageView imageView = (ImageView) target;

                Bitmap bitmap = getBitmap (imageView.getDrawable());

                return bitmap.sameAs(expected);
            }

            private Bitmap getBitmap(Drawable drawable){
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ImageView with bitmap same as bitmap " + expected.toString());
            }
        };
    }
}
