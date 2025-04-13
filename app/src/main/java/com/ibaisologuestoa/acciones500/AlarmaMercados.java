package com.ibaisologuestoa.acciones500;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.ibaisologuestoa.acciones500.ReceptorAlarma;

import java.util.Calendar;

public class AlarmaMercados {
    public static void programar(Context contexto, int hora, int minuto) {
        Intent intentAlarma = new Intent(contexto, ReceptorAlarma.class);
        intentAlarma.setAction("ALERTA_APERTURA_MERCADOS");

        PendingIntent accionAlarma = PendingIntent.getBroadcast(
                contexto,
                0,
                intentAlarma,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(System.currentTimeMillis());
        calendario.set(Calendar.HOUR_OF_DAY, hora);
        calendario.set(Calendar.MINUTE, minuto);
        calendario.set(Calendar.SECOND, 0);

        if(calendario.getTimeInMillis() <= System.currentTimeMillis()) {
            calendario.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);

        try {
            gestorAlarmas.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendario.getTimeInMillis(),
                    accionAlarma
            );
        } catch (SecurityException e) {
            Log.e("Alarma", "Error con alarma: ", e);
        }
    }

    public static void cancelar(Context contexto) {
        Intent intentAlarma = new Intent(contexto, ReceptorAlarma.class);
        PendingIntent accionAlarma = PendingIntent.getBroadcast(
                contexto,
                0,
                intentAlarma,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
        gestorAlarmas.cancel(accionAlarma);
    }
}