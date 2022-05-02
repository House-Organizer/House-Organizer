package com.github.houseorganizer.houseorganizer.house;

import static com.github.houseorganizer.houseorganizer.panels.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.image.QRAnalyzer;
import com.github.houseorganizer.houseorganizer.image.QRListener;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class QRCodeScanActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final int CAMERA_PERMISSION_REQ_CODE = 1; //Can be chosen but has to be unique
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private PreviewView cameraPreview;
    private ListenableFuture<ProcessCameraProvider> cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cameraPreview = findViewById(R.id.cameraPreview);
        cameraProvider = ProcessCameraProvider.getInstance(this);

        askForCameraPermissionsOrContinue();
    }

    private void askForCameraPermissionsOrContinue() {
        if (ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            continueWithCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_REQ_CODE);
        }
    }

    private void continueWithCamera() {
        cameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = this.cameraProvider.get();
                attachCameraToActivity(cameraProvider);
            } catch (Exception e) {
                Util.logAndToast(this.toString(), "Starting camera:failed", e, getApplicationContext(), "Could not start the camera");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQ_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                continueWithCamera();
            } else { //Permission to access the camera was denied
                Toast.makeText(this, "Camera permission has been refused", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
            }
        }
    }

    private void attachCameraToActivity(@NonNull ProcessCameraProvider cameraProvider) {
        cameraPreview.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRAnalyzer(new QRListener() {
            @Override
            public void QRCodeFound(String QRCode) {
                imageAnalysis.clearAnalyzer();
                acceptInvite(QRCode);
            }

            @Override
            public void QRCodeNotFound() {
                //Nothing, as this function has to be fast
            }
        }));
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    private void acceptInvite(String QRCode){
        String email = auth.getCurrentUser().getEmail();
        if(email == null || QRCode == null){
            return;
        }

        DocumentReference targetHousehold = db.collection("households").document(QRCode);
        targetHousehold.get().addOnCompleteListener(task -> {
            Map<String, Object> householdData = task.getResult().getData();
            if (householdData != null) {
                List<String> listOfUsers = (List<String>) householdData.getOrDefault("residents", "[]");
                Long num_users = (Long) householdData.get("num_members");
                if (!listOfUsers.contains(email)) {
                    targetHousehold.update("residents", FieldValue.arrayUnion(email));
                    targetHousehold.update("num_members", num_users + 1);
                }

                SharedPreferences.Editor editor = getSharedPrefsEditor(this);
                editor.putString(CURRENT_HOUSEHOLD, QRCode);
                editor.apply();

                Intent intent = new Intent(this, MainScreenActivity.class);
                Toast.makeText(getApplicationContext(), this.getString(R.string.add_user_success),Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, CreateHouseholdActivity.class);
                intent.putExtra("mUserEmail", email);
                Toast.makeText(getApplicationContext(), this.getString(R.string.QR_invalid),Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
    }
}