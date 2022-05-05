package com.github.houseorganizer.houseorganizer.image;

public interface QRListener {
    void QRCodeFound(String QRCode);
    void QRCodeNotFound();
}
