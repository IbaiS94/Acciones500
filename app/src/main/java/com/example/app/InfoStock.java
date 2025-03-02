package com.example.app;
import static com.example.app.MainActivity.PREFS_NAME;
import static com.example.app.MainActivity.THEME_KEY;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class InfoStock extends AppCompatActivity {
    String nombre = "Error";
    private EditText notasEditText;
    private String FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cv), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.bottom_toolbar);
        setSupportActionBar(toolbar);

        gestionInfo(null, null);
    }

    public void gestionInfo(String nombre, StockDB db2){
        StockDB db;
        if (db2 == null) {
            db = new StockDB(this);
        } else {
            db = db2;
        }
        Cursor cursor = db.obtenerDescripcion();


        ////
        String nombreI;
        if(nombre == null){
        FILE_NAME = getIntent().getStringExtra("nombre").toString()+ ".txt";
        nombreI = getIntent().getStringExtra("nombre");
        }
        else{
        FILE_NAME=nombre+ ".txt";
        nombreI = null;}
        ////
        while (cursor.moveToNext()) {
            String nombreC = cursor.getString(1);
            if ((nombre != null && nombre.equals(nombreC)) || (nombreI != null && nombreI.equals(nombreC)))  {
                TextView name = findViewById(R.id.stockName);
                nombre = cursor.getString(1);
                name.setText(nombre);
                ///
                TextView desc = findViewById(R.id.stockDescription);
                desc.setText(cursor.getString(2));
                ///
                TextView prec = findViewById(R.id.stockPrice);
                String euro = cursor.getString(3) + "€";
                prec.setText(euro);
                ///
                TextView nota = findViewById(R.id.notas);
                nota.setText(cursor.getString(4));
                ///
                TextView sim = findViewById(R.id.stockSimilar);
                sim.setText(cursor.getString(5));
            }
        }
        /////

        notasEditText = findViewById(R.id.notas);

        String notasGuardadas = leerNotas();
        if (notasGuardadas != null) {
            notasEditText.setText(notasGuardadas);
        }
        TextView tradingView = findViewById(R.id.tradingView);
        tradingView.setClickable(Boolean.TRUE);
        tradingView.setOnClickListener(b -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.tradingview.tradingviewapp");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.tradingview.tradingviewapp"));
                startActivity(playStoreIntent);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem boton = menu.findItem(R.id.action_tema);
        boton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int currentMode = AppCompatDelegate.getDefaultNightMode();
                boolean isDarkMode = currentMode != AppCompatDelegate.MODE_NIGHT_YES;

                // Guardar la preferencia
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(THEME_KEY, isDarkMode);
                editor.apply();

                // Aplicar el tema
                AppCompatDelegate.setDefaultNightMode(
                        isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );

                recreate();
                return true;
            }
        });

        MenuItem boton2 = menu.findItem(R.id.action_search);
        boton2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent2 = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent2.putExtra(SearchManager.QUERY, "Acciones "+nombre );
                    startActivity(intent2);
                    return true;
                }
        });
        return true;
    }
    private void guardarNotas() {
        String texto = notasEditText.getText().toString();
        try (FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(texto.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String leerNotas() {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = openFileInput(FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    @Override
    protected void onPause() {
        super.onPause();
        guardarNotas();
    }
}

