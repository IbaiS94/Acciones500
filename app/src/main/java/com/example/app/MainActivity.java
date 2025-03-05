package com.example.app;
import android.app.SearchManager;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements FragmentNuevo.OnNombreActualizadoListener {
    public static final String PREFS_NAME = "ThemePreferences";
    public static final String THEME_KEY = "isDarkMode";
    private boolean modfavorito = false;

    private String nombre = "";

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_nuevo, new com.example.app.FragmentNuevo())
                    .replace(R.id.container_antiguo, new com.example.app.FragmentAntiguo())
                    .commit();
        }

        //da null
        if(findViewById(R.id.main) == null){
            Log.d("FAKE NEWS", "MAIN NULL");
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.bottom_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_abrir, R.string.nav_cerar);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_1) {
                } else if (id == R.id.nav_2) {
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        logicaLista();
        pedirPermisoNotificaciones();

        noti();
        restoreTheme();

    }
    @Override
    public void onNombreActualizado(String nombreActualizado) {
        Log.d("NombreActualizado", nombreActualizado);
        nombre = nombreActualizado;
    }
    public void infoStockCall(String nombre){
        InfoStock infoStock = new InfoStock();
        StockDB db = new StockDB(this);
        infoStock.gestionInfo(nombre, db);
    }
    public void logicaLista(){
        modfavorito = getIntent().getBooleanExtra("favs", FALSE);

        RecyclerView recyclerView = findViewById(R.id.rv);
        Log.d("DEBUG", String.valueOf(recyclerView));
        List<String> listaDatos = new ArrayList<>();
        StockDB db = new StockDB(this);
        Cursor cursor = db.obtenerNombres();
        //db.insertar("Apple",Boolean.FALSE); //Mover a StockDB para que no se genere cada vez
        if(modfavorito){
            while(cursor.moveToNext()){
                if(cursor.getInt(2) == 1){
                    listaDatos.add(cursor.getString(1));
                }

            }
        }
        else{
            while(cursor.moveToNext()){
                listaDatos.add(cursor.getString(1));

            }}

        if(recyclerView != null){
            com.example.app.AdaptadorRv adapt = new com.example.app.AdaptadorRv(this, listaDatos);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapt);}


        /*       if (cursor.moveToFirst()) {
            Integer nombre = cursor.getCount();
            Toast.makeText(this, String.valueOf(nombre), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No hay datos", Toast.LENGTH_SHORT).show();
        } */
        cursor.close();
    }
    @Override
    public void onBackPressed() {
        if (modfavorito) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("favs", FALSE);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    private void restoreTheme() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(THEME_KEY, false); // false es el valor por defecto

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
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
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    String busqueda = "Acciones noticias";
                    if(!nombre.equals("")){
                    busqueda = "Acción "+nombre;}
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




    public void pedirPermisoNotificaciones() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
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
                    .setContentTitle("¡Recordatorio!")
                    .setContentText("Gracias por usar la aplicación, si la estas disfrutando una reseña nos ayudaria mucho")
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