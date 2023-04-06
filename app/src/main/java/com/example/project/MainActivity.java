package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private Util util = new Util();
    private LocationService myLocationService = new LocationService();
    private Intent serviceIntent;

    private Button startButton;
    private Button stopButton;

    private static final int MY_FINE_LOCATION_REQUEST = 99;
    private static final int MY_BACKGROUND_LOCATION_REQUEST = 100;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        starServiceFunc();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("背景位置資訊存取權")
                                .setMessage("本App需要同意背景位置權限才能持續存取位置")
                                .setPositiveButton("直接啟動服務", (dialog, id) -> starServiceFunc())
                                .setNegativeButton("同意背景位置權限", (dialog, id) -> requestBackgroundLocationPermission())
                                .create().show();
                    }
                } else {
                    starServiceFunc();
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("位置存取權限")
                        .setMessage("需要同意位置存取權限")
                        .setPositiveButton("OK", (dialog, id) -> requestFineLocationPermission())
                        .create().show();
            } else {
                requestFineLocationPermission();
            }
        });

        stopButton.setOnClickListener(v -> stopServiceFunc());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void starServiceFunc() {
        myLocationService = new LocationService();
        serviceIntent = new Intent(this, myLocationService.getClass());
        if (!util.isMyServiceRunning()) {
            util.setMyServiceRunning(true);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "服務成功啟動", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "服務目前運作中", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopServiceFunc() {
        myLocationService = new LocationService();
        serviceIntent = new Intent(this, myLocationService.getClass());
        if (util.isMyServiceRunning()) {
            util.setMyServiceRunning(false);
            stopService(serviceIntent);
            Toast.makeText(this, "服務成功停止", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "服務已停止", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_BACKGROUND_LOCATION_REQUEST);
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_FINE_LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        requestBackgroundLocationPermission();
                    }
                } else {
                    Toast.makeText(this, "拒絕位置存取權限", Toast.LENGTH_LONG).show();
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.getPackageName(), null)));
                    }
                }
                break;
            case MY_BACKGROUND_LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "同意背景位置資訊存取權", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "拒絕背景位置資訊存取權", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
