package com.ibaisologuestoa.acciones500;
import static com.ibaisologuestoa.acciones500.MainActivity.PREFS;
import static com.ibaisologuestoa.acciones500.MainActivity.TEMA;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class InfoStock extends AppCompatActivity {
    String nombre = null;
    private EditText notasEditText;
    private String NOM_ARCHIVO;

    private DrawerLayout dr;
    private ActionBarDrawerToggle tg;

    private Translator traductor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cv), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.barra_menu);
        setSupportActionBar(toolbar);

        gestionInfo( null);


        dr = findViewById(R.id.dr);
        if (dr != null) {
            tg = new ActionBarDrawerToggle(this, dr, toolbar, R.string.nav_abrir, R.string.nav_cerar);
            dr.addDrawerListener(tg);
            tg.syncState();

            NavigationView navigationView = findViewById(R.id.nav);

            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    new MaterialAlertDialogBuilder(InfoStock.this)
                            .setTitle(getString(R.string.conf))
                            .setMessage(getString(R.string.conf2))
                            .setPositiveButton("Ok", (dialog, which) -> {
                                SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();

                                if (id == R.id.nav_1) {
                                    editor.putString("Idioma", "es");
                                } else if (id == R.id.nav_2) {
                                    editor.putString("Idioma", "en");
                                } else if (id == R.id.nav_3) {
                                    editor.putString("Idioma", "de");
                                }

                                editor.apply();
                                aplicarIdioma();
                            })
                            .show();

                    dr.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (traductor != null) {
        traductor.close();} // Segun api, es lo recomendado
    }

    public void gestionInfo(StockDB db2){
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
        NOM_ARCHIVO = getIntent().getStringExtra("nombre").toString()+ ".txt";
        nombreI = getIntent().getStringExtra("nombre");
        }
        else{
        NOM_ARCHIVO=nombre+ ".txt";
        nombreI = null;}
        ////
        while (cursor.moveToNext()) {
            String nombreC = cursor.getString(1);
            if ((nombre != null && nombre.equals(nombreC)) || (nombreI != null && nombreI.equals(nombreC))) {
                TextView name = findViewById(R.id.stockNom);
                nombre = cursor.getString(1);
                Log.d("Nombre InfoStock", nombre);
                name.setText(nombre);
                ///
                TextView desc = findViewById(R.id.stockDescrip);

                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                String idioma = prefs.getString("Idioma", "es");
                TranslatorOptions options;
                DownloadConditions conditions;
                String txt = cursor.getString(2);

                switch (idioma) {
                    case "en":
                        options = new TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.SPANISH)
                                .setTargetLanguage(TranslateLanguage.ENGLISH)
                                .build();
                        break;

                    case "de":
                        options = new TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.SPANISH)
                                .setTargetLanguage(TranslateLanguage.GERMAN)
                                .build();
                        break;

                    default:
                        desc.setText(txt);
                        options = new TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.SPANISH)
                                .setTargetLanguage(TranslateLanguage.SPANISH)
                                .build();
                        break;
                }

                traductor = Translation.getClient(options);
                conditions = new DownloadConditions.Builder().build();

                traductor.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener(unused -> {
                            traductor.translate(txt)
                                    .addOnSuccessListener(texto_traducido -> {
                                        desc.setText(texto_traducido);
                                        Log.d("Traductor", "Traducción exitosa: " + texto_traducido);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Traductor", "Error traducción: " + e.getMessage());
                                        desc.setText(txt);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Traductor", "Error modelo: " + e.getMessage());
                            desc.setText(txt);
                        });

                TextView prec = findViewById(R.id.stockPrecio);
                String euro = cursor.getString(3) + "€";
                prec.setText(euro);
                ///
                TextView nota = findViewById(R.id.notas);
                nota.setText(cursor.getString(4));
                ///
                TextView sim = findViewById(R.id.stockSimilar);
                sim.setText(getString(R.string.relacionado)+" "+cursor.getString(5));
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
                int esteMode = AppCompatDelegate.getDefaultNightMode();
                boolean esDarkMode = esteMode != AppCompatDelegate.MODE_NIGHT_YES;

                // Guardar la preferencia
                SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(TEMA, esDarkMode);
                editor.apply();

                // Aplicar el tema
                AppCompatDelegate.setDefaultNightMode(
                        esDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
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
                    Log.d("Nombre 2", nombre);
                    intent2.putExtra(SearchManager.QUERY, getString(R.string.acc)+" "+nombre );
                    startActivity(intent2);
                    return true;
                }
        });
        return true;
    }
    private void guardarNotas() {
        String texto = "";
        if(notasEditText != null){
            texto = notasEditText.getText().toString();}
        try (FileOutputStream fos = openFileOutput(NOM_ARCHIVO, Context.MODE_PRIVATE)) {
            fos.write(texto.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String leerNotas() {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = openFileInput(NOM_ARCHIVO);
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
    private void aplicarIdioma() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String nuevoIdioma = prefs.getString("Idioma", "es");
        String idiomaActual = Locale.getDefault().getLanguage();

        if (nuevoIdioma.equals(idiomaActual)) {
            return;
        }

        Locale locale = new Locale(nuevoIdioma);
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = res.getConfiguration();

        config.setLocale(locale);
        config.setLayoutDirection(locale);

        res.updateConfiguration(config, res.getDisplayMetrics());

        if (!isFinishing()) {
            recreate();
        }
    }
}

