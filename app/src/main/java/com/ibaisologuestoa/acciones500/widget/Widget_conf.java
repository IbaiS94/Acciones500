package com.ibaisologuestoa.acciones500.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.ibaisologuestoa.acciones500.R;
import com.ibaisologuestoa.acciones500.StockDB;

import java.util.ArrayList;
import java.util.List;

public class Widget_conf extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner spinnerAcciones;
    private StockDB stockDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_conf);

        setResult(RESULT_CANCELED);

        spinnerAcciones = findViewById(R.id.spinner_acciones_widget);
        Button botonConfigurar = findViewById(R.id.boton_configurar_widget);
        stockDB = new StockDB(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        cargarAccionesEnSpinner();

        botonConfigurar.setOnClickListener(v -> {
            final Context context = Widget_conf.this;

            String accionSeleccionada = (String) spinnerAcciones.getSelectedItem();

            if (accionSeleccionada != null && !accionSeleccionada.isEmpty()) {
                guardarPreferenciaAccion(context, appWidgetId, accionSeleccionada);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] ids = new int[] { appWidgetId };
                Widget widget = new Widget();
                widget.onUpdate(context, appWidgetManager, ids);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
            } else {
                Toast.makeText(context, "Error: Debes seleccionar una acción", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void cargarAccionesEnSpinner() {
        stockDB = new StockDB(this);

        List<String> nombresAcciones = obtenerTodosLosNombresDeAcciones();

        if (nombresAcciones.isEmpty()) {
            Toast.makeText(this, "No hay acciones disponibles", Toast.LENGTH_LONG).show();
            spinnerAcciones.setEnabled(false);
            findViewById(R.id.boton_configurar_widget).setEnabled(false);
        } else {
            spinnerAcciones.setEnabled(true);
            findViewById(R.id.boton_configurar_widget).setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombresAcciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAcciones.setAdapter(adapter);
    }

    private List<String> obtenerTodosLosNombresDeAcciones() {
        List<String> nombres = new ArrayList<>();
        Cursor cursor = stockDB.getReadableDatabase().rawQuery("SELECT nombre FROM Stocks", null);

        if (cursor.moveToFirst()) {
            do {
                nombres.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return nombres;
    }

    static void guardarPreferenciaAccion(Context context, int appWidgetId, String accion) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Widget.PREFERENCIAS_WIDGET, Context.MODE_PRIVATE).edit();
        prefs.putString(Widget.PREFERENCIA_WIDGET_ACCION + appWidgetId, accion);
        prefs.apply();
    }
}