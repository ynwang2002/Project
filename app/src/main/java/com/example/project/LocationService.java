package com.example.project;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;

public class LocationService extends Service {
    String msg = "定位中";

    FusedLocationProviderClient myFusedLocationClient;

    LocationRequest myLocationRequest = new LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true).build();

    LocationCallback myLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            java.util.List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                Toast.makeText(LocationService.this, "緯度：" + location.getLatitude() + '\n'
                        + "經度：" + location.getLongitude(), Toast.LENGTH_LONG).show();
                msg = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "需要同意位置權限才能存取經緯度", Toast.LENGTH_LONG).show();
        } else {
            myFusedLocationClient.requestLocationUpdates(myLocationRequest, myLocationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            while (true) {
                NotificationManager notificationManager
                        = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationChannel = new NotificationChannel("1", "位置通知", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                        .setContentTitle("定位服務")
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.img)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                startForeground(1, builder.build());

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myFusedLocationClient.removeLocationUpdates(myLocationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}