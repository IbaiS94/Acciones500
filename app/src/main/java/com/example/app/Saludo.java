package com.example.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


public class Saludo extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saludo);

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Saludo.this, MainActivity.class));
                finish();
            }
        });
        pedirPermisoNotificaciones();
        noti();
    }

    public void pedirPermisoNotificaciones() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            //PEDIR EL PERMISO
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }
    }
    private void noti(){
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "12");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("12", "Ratings",
                    NotificationManager.IMPORTANCE_DEFAULT);

            elManager.createNotificationChannel(elCanal);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle(getString(R.string.recordatorio))
                    .setContentText(getString(R.string.agradecimiento))
                    .setVibrate(new long[]{0, 1000, 500, 2000})
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            elCanal.setDescription("Petición de reseña");
            elCanal.enableLights(true);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 2000});
            elCanal.enableVibration(true);
            elManager.notify(1, elBuilder.build());
        }

    }
}
