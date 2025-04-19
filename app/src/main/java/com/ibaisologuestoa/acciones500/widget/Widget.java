package com.ibaisologuestoa.acciones500.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.ibaisologuestoa.acciones500.MainActivity;
import com.ibaisologuestoa.acciones500.R;
import com.ibaisologuestoa.acciones500.Saludo;
import com.ibaisologuestoa.acciones500.StockDB;

import java.util.Calendar;
import java.util.TimeZone;

public class Widget extends AppWidgetProvider {
    private static final String ACCION_REFRESCAR = "com.ibaisologuestoa.acciones500.widget.REFRESCAR";
    private static final String ACCION_ACTUALIZACION_AUTOMATICA = "com.ibaisologuestoa.acciones500.widget.ACTUALIZACION_AUTOMATICA";
    public static final String PREFERENCIAS_WIDGET = "WidgetPrefs";
    public static final String PREFERENCIA_WIDGET_ACCION = "widget_accion_";
    private static final int INTERVALO_ACTUALIZACION = 60000; //10 mins

    static void actualizarWidget(Context contexto, AppWidgetManager gestorWidgets, int idWidget) {
        SharedPreferences prefs = contexto.getSharedPreferences(PREFERENCIAS_WIDGET, Context.MODE_PRIVATE);
        String nombreAccion = prefs.getString(PREFERENCIA_WIDGET_ACCION + idWidget, "");
        double precioAccion = 0.0;

        StockDB db = new StockDB(contexto);
        Cursor cursor = db.obtenerPrecio(nombreAccion);
        if (cursor != null && cursor.moveToFirst()) {
            precioAccion = cursor.getDouble(0);
            cursor.close();
        }

        RemoteViews vistas = new RemoteViews(contexto.getPackageName(), R.layout.widget);
        vistas.setTextViewText(R.id.widget_nom, nombreAccion);
        vistas.setTextViewText(R.id.widget_precio, String.format("$%.2f", precioAccion));
        int colorTextoNombre = contexto.getResources().getColor(R.color.widget_texto_nombre, contexto.getTheme());
        int colorTextoPrecio = contexto.getResources().getColor(R.color.widget_texto_precio, contexto.getTheme());
        int colorTextoFecha = contexto.getResources().getColor(R.color.widget_texto_fecha, contexto.getTheme());
        vistas.setTextColor(R.id.widget_nom, colorTextoNombre);
        vistas.setTextColor(R.id.widget_precio, colorTextoPrecio);
        vistas.setTextColor(R.id.widget_fecha, colorTextoFecha);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String t = String.format("%02d:%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        vistas.setTextViewText(R.id.widget_fecha, "Actualizado: " + t);
        int colorFondo = contexto.getResources().getColor(R.color.widget_fondo, contexto.getTheme());
        vistas.setInt(R.id.widget_layout, "setBackgroundColor", colorFondo);



        Intent intentLanzar = new Intent(contexto, Saludo.class);
        PendingIntent intentoLanzar = PendingIntent.getActivity(contexto, 0, intentLanzar, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        vistas.setOnClickPendingIntent(R.id.widget_layout, intentoLanzar);
        Intent intentRefrescar = new Intent(contexto, Widget.class);
        intentRefrescar.setAction(ACCION_REFRESCAR);
        intentRefrescar.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, idWidget);
        PendingIntent intentoRefrescar = PendingIntent.getBroadcast(contexto, idWidget, intentRefrescar, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        vistas.setOnClickPendingIntent(R.id.widget_refresh, intentoRefrescar);




        gestorWidgets.updateAppWidget(idWidget, vistas);
    }

    @Override
    public void onUpdate(Context contexto, AppWidgetManager gestorWidgets, int[] idsWidgets) {
        for (int idWidget : idsWidgets) {
            actualizarWidget(contexto, gestorWidgets, idWidget);
        }
        programarActualizacionAutomatica(contexto);
    }

    @Override
    public void onEnabled(Context contexto) {
        super.onEnabled(contexto);
        programarActualizacionAutomatica(contexto);
    }

    @Override
    public void onDisabled(Context contexto) {
        super.onDisabled(contexto);
        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(contexto, Widget.class);
        PendingIntent intentoPendiente = PendingIntent.getBroadcast(
                contexto, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        gestorAlarmas.cancel(intentoPendiente);
    }

    @Override
    public void onReceive(Context contexto, Intent intent) {
        super.onReceive(contexto, intent);
        String accion = intent.getAction();

        if (ACCION_ACTUALIZACION_AUTOMATICA.equals(accion)) {
            AppWidgetManager gestorWidgets = AppWidgetManager.getInstance(contexto);
            ComponentName widget = new ComponentName(contexto, Widget.class);
            int[] idsWidgets = gestorWidgets.getAppWidgetIds(widget);
            for (int idWidget : idsWidgets) {
                actualizarWidget(contexto, gestorWidgets, idWidget);
            }
        }
        else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(accion)) {
            AppWidgetManager gestorWidgets = AppWidgetManager.getInstance(contexto);
            ComponentName widget = new ComponentName(contexto, Widget.class);
            int[] idsWidgets = gestorWidgets.getAppWidgetIds(widget);
            for (int idWidget : idsWidgets) {
                actualizarWidget(contexto, gestorWidgets, idWidget);
            }
        }
        else if (ACCION_REFRESCAR.equals(accion)) {
            int idWidget = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (idWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
                new Thread(() -> {
                    StockDB db = new StockDB(contexto);
                    SharedPreferences prefs = contexto.getSharedPreferences(PREFERENCIAS_WIDGET, Context.MODE_PRIVATE);
                    String nombreAccion = prefs.getString(PREFERENCIA_WIDGET_ACCION + idWidget, "");
                    db.actualizarDesdeAlpaca(nombreAccion);

                    AppWidgetManager gestorWidgets = AppWidgetManager.getInstance(contexto);
                    ComponentName widget = new ComponentName(contexto, Widget.class);
                    int[] idsWidgets = gestorWidgets.getAppWidgetIds(widget);
                    for (int widgetId : idsWidgets) {
                        if (widgetId == idWidget) {
                            actualizarWidget(contexto, gestorWidgets, widgetId);
                            break;
                        }
                    }
                }).start();
            }
        }
    }

    private static void programarActualizacionAutomatica(Context contexto) {
        AlarmManager gestorAlarmas = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(contexto, Widget.class);
        intent.setAction(ACCION_ACTUALIZACION_AUTOMATICA);

        PendingIntent intentoPendiente = PendingIntent.getBroadcast(
                contexto, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long tiempoInicio = System.currentTimeMillis() + INTERVALO_ACTUALIZACION;
        gestorAlarmas.setRepeating(
                AlarmManager.RTC_WAKEUP,
                tiempoInicio,
                INTERVALO_ACTUALIZACION,
                intentoPendiente
        );
    }
}