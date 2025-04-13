package com.ibaisologuestoa.acciones500;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FragmentNuevo.OnNombreActualizadoListener {
    public static final String PREFS = "Prefs";
    public static final String TEMA = "esDarkMode";
    private boolean modfavorito = false;
    private String nombre = "";
    private DrawerLayout dr;
    private ActionBarDrawerToggle tg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Window w = getWindow();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_nuevo, new FragmentNuevo())
                    .replace(R.id.container_antiguo, new FragmentAntiguo())
                    .commit();
        }

        if (findViewById(R.id.main) == null) {
            Log.d("FAKE NEWS", "MAIN NULL");
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.barra_menu);
        setSupportActionBar(toolbar);

        dr = findViewById(R.id.dr);
        if (dr != null) {
            tg = new ActionBarDrawerToggle(this, dr, toolbar, R.string.nav_abrir, R.string.nav_cerar);
            dr.addDrawerListener(tg);
            tg.syncState();

            NavigationView navigationView = findViewById(R.id.nav);
            MenuItem perfilItem = navigationView.getMenu().findItem(R.id.n_perfil);
            View nav_perfil = perfilItem.getActionView();

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String emailus = prefs.getString("currentUser", "");
            String nombreus = prefs.getString("currentUserName", "");
            String clave = "imagen_" + emailus;
            String imgGuardada = prefs.getString(clave, null);

            ImageView imgV = nav_perfil.findViewById(R.id.imgPerfil);
            if (imgGuardada != null) {
                byte[] dBytes = Base64.decode(imgGuardada, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(dBytes, 0, dBytes.length);
                imgV.setImageBitmap(bitmap);
            } else {
                imgV.setImageResource(R.drawable.person2);
            }

            TextView nav_nombre = nav_perfil.findViewById(R.id.campoNombreNav);
            TextView nav_email = nav_perfil.findViewById(R.id.campoEmail);
            nav_email.setText(emailus);
            nav_nombre.setText(nombreus);

            MenuItem alarmaItem = navigationView.getMenu().findItem(R.id.nav_alarma);
            SharedPreferences prefs2 = getSharedPreferences("config_alarmas", MODE_PRIVATE);
            boolean alarmaActiva = prefs2.getBoolean("alarma_activa", false);
            alarmaItem.setChecked(alarmaActiva);

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_alarma) {
                    SharedPreferences prefsAlarma = getSharedPreferences("config_alarmas", MODE_PRIVATE);
                    boolean estadoActual = prefsAlarma.getBoolean("alarma_activa", false);
                    boolean nuevoEstado = !estadoActual;

                    prefsAlarma.edit().putBoolean("alarma_activa", nuevoEstado).apply();

                    navigationView.setCheckedItem(id);

                    MenuItem alarmaItem2= navigationView.getMenu().findItem(R.id.nav_alarma);
                    if (alarmaItem2 != null) {
                        alarmaItem2.setChecked(nuevoEstado);
                    }

                    if (nuevoEstado) {
                        AlarmaMercados.programar(MainActivity.this, 8, 0);
                        Toast.makeText(MainActivity.this, "Alarma activada ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        AlarmaMercados.cancelar(MainActivity.this);
                        Toast.makeText(MainActivity.this, "Alarma desactivada ❌", Toast.LENGTH_SHORT).show();
                    }

                    dr.closeDrawer(GravityCompat.START);
                    return true;
                }
                else if (id == R.id.nav_1 || id == R.id.nav_2 || id == R.id.nav_3) {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle(getString(R.string.conf))
                            .setMessage(getString(R.string.conf2))
                            .setPositiveButton("Ok", (dialog, which) -> {
                                SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                if (id == R.id.nav_1) editor.putString("Idioma", "es");
                                else if (id == R.id.nav_2) editor.putString("Idioma", "en");
                                else if (id == R.id.nav_3) editor.putString("Idioma", "de");
                                editor.apply();
                                aplicarIdioma();
                            })
                            .show();
                    dr.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        }
        logicaLista();
        restaurarTema();
        aplicarIdioma();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav);
        MenuItem alarmaItem = navigationView.getMenu().findItem(R.id.nav_alarma);
        SharedPreferences prefsAlarma = getSharedPreferences("config_alarmas", MODE_PRIVATE);
        boolean alarmaActiva = prefsAlarma.getBoolean("alarma_activa", false);
        if (alarmaItem != null) {
            alarmaItem.setChecked(alarmaActiva);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                StockDB db = new StockDB(MainActivity.this);
                db.actualizarDesdeAlpaca();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logicaLista();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onNombreActualizado(String nombreActualizado) {
        Log.d("NombreActualizado", nombreActualizado);
        nombre = nombreActualizado;
    }

    public void logicaLista() {
        modfavorito = getIntent().getBooleanExtra("favs", false);
        RecyclerView recyclerView = findViewById(R.id.rv);
        List<StockItem> listaDatos = new ArrayList<>();
        StockDB db = new StockDB(this);
        Cursor cursor = db.obtenerNombresYPrecios();

        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0);  // Columna 0: nombre
            double precio = cursor.getDouble(1);  // Columna 1: precio
            if (modfavorito) {
                if (db.esFavorito(nombre)) {
                    listaDatos.add(new StockItem(nombre, precio));
                }
            } else {
                listaDatos.add(new StockItem(nombre, precio));
            }
        }
        cursor.close();

        if (recyclerView != null) {
            AdaptadorRv adapt = new AdaptadorRv(this, listaDatos);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapt);
        }
    }
    @Override
    public void onBackPressed() {
        if (modfavorito) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("favs", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    private void restaurarTema() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean esDarkMode = preferences.getBoolean(TEMA, false);
        AppCompatDelegate.setDefaultNightMode(
                esDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
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
                SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(TEMA, esDarkMode);
                editor.apply();
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
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                String busqueda = getString(R.string.accnot);
                if (!nombre.equals("")) {
                    busqueda = getString(R.string.acc) + nombre;
                }
                intent.putExtra(SearchManager.QUERY, busqueda);
                startActivity(intent);
                return true;
            }
        });

        MenuItem boton3 = menu.findItem(R.id.fav_menu);
        boton3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("favs", !modfavorito);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
        });

        return true;
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