package com.github.houseorganizer.houseorganizer.image;

import static android.graphics.ImageFormat.YUV_420_888;
import static android.graphics.ImageFormat.YUV_422_888;
import static android.graphics.ImageFormat.YUV_444_888;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.nio.ByteBuffer;

public class QRAnalyzer implements ImageAnalysis.Analyzer {
    private QRListener listener;

    public QRAnalyzer(QRListener listener) {
        this.listener = listener;
    }

    @Override
    public void analyze(ImageProxy image) {
        if (image.getFormat() == YUV_420_888 || image.getFormat() == YUV_422_888 || image.getFormat() == YUV_444_888) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] byteArray = new byte[buffer.capacity()];
            buffer.get(byteArray);

            int width = image.getWidth();
            int height = image.getHeight();

            PlanarYUVLuminanceSource src = new PlanarYUVLuminanceSource(byteArray, width, height, 0, 0, width, height, false);
            BinaryBitmap bm = new BinaryBitmap(new HybridBinarizer(src));

            try {
                Result result = new QRCodeMultiReader().decode(bm);
                listener.QRCodeFound(result.getText());
            } catch (Exception e) {
                listener.QRCodeNotFound();
            }
        }
        image.close();
    }
}
