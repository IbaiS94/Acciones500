package com.ibaisologuestoa.acciones500;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReceptorAlarma extends BroadcastReceiver {
    @Override
    public void onReceive(Context contexto, Intent intent) {
        if(intent.getAction().equals("ALERTA_APERTURA_MERCADOS")) {
            mostrarNotificacion(contexto);
            AlarmaMercados.programar(contexto, 15, 30);
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences preferencias = contexto.getSharedPreferences("config_alarmas", MODE_PRIVATE);

            if(preferencias.getBoolean("alarma_activa", false)) {
                AlarmaMercados.programar(contexto, 15, 30);
            }
        }
    }

    private void mostrarNotificacion(Context contexto) {
        NotificationManager manager = (NotificationManager) contexto.getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(contexto, "canal_mercados")
                .setSmallIcon(R.drawable.stock)
                .setContentTitle("¡Mercados abiertos!")
                .setContentText("Los mercados han comenzado su actividad")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationChannel canal = new NotificationChannel(
                "canal_mercados",
                "Alertas de Mercados",
                NotificationManager.IMPORTANCE_HIGH
        );
        manager.createNotificationChannel(canal);

        manager.notify(123, builder.build());
    }
}