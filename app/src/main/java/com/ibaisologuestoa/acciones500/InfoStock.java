package com.ibaisologuestoa.acciones500;

import static com.ibaisologuestoa.acciones500.MainActivity.PREFS;
import static com.ibaisologuestoa.acciones500.MainActivity.TEMA;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoStock extends AppCompatActivity {
    private static final String TAG = "InfoStock";
    private static final String CHANNEL_ID = "chat_notifications";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private String nombre;
    private EditText notasEditText;
    private String NOM_ARCHIVO;
    private DrawerLayout dr;
    private ActionBarDrawerToggle tg;
    private Translator traductor;
    private MapView mapView;
    private IMapController mapController;
    private RecyclerView recyclerMensajes;
    private ChatAdapter chatAdapter;
    private EditText etMensaje;
    private String usuarioActual;
    private String currentToken;
    private Handler handler = new Handler();
    private static final long INTERVALO_ACTUALIZACION = 3000; // 3 segundos
    private boolean chatVisible = false;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvDistancia;
    private Switch switchNotificaciones;
    private double latitudSede, longitudSede;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        org.osmdroid.config.Configuration.getInstance().load(ctx,
                PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.stock);

        Window w = getWindow();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cv), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();

        Toolbar toolbar = findViewById(R.id.barra_menu);
        setSupportActionBar(toolbar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvDistancia = findViewById(R.id.tv_distancia);
        switchNotificaciones = findViewById(R.id.switch_notificaciones);


        gestionInfo(null);

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
            String clave = "imagen" + emailus;
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

                    MenuItem alarmaItem2 = navigationView.getMenu().findItem(R.id.nav_alarma);
                    if (alarmaItem2 != null) {
                        alarmaItem2.setChecked(nuevoEstado);
                    }

                    if (nuevoEstado) {
                        AlarmaMercados.programar(InfoStock.this, 8, 0);
                        Toast.makeText(InfoStock.this, "Alarma activada ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        AlarmaMercados.cancelar(InfoStock.this);
                        Toast.makeText(InfoStock.this, "Alarma desactivada ❌", Toast.LENGTH_SHORT).show();
                    }

                    dr.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_1 || id == R.id.nav_2 || id == R.id.nav_3) {
                    new MaterialAlertDialogBuilder(InfoStock.this)
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

        aplicarIdioma();
        inicializarChat();
        setupFCM();
        createNotificationChannel();
        configurarNotificaciones();

        if (getIntent().getBooleanExtra("abrirChat", false)) {
            findViewById(R.id.chat_cont).setVisibility(View.VISIBLE);
            cargarMensajes();
        }
    }

    private void configurarNotificaciones() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean suscrito = prefs.getBoolean("suscrito_" + nombre, false);
        switchNotificaciones.setChecked(suscrito);
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                suscribirATopico();
            } else {
                desuscribirDeTopico();
            }
        });
    }

    private void suscribirATopico() {
        FirebaseMessaging.getInstance().subscribeToTopic("stock_" + nombre)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Suscrito a stock_" + nombre);
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("suscrito_" + nombre, true).apply();
                        Toast.makeText(this, "Suscrito a notificaciones de " + nombre, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error al suscribirse: ", task.getException());
                        Toast.makeText(this, "Error al suscribirse", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void desuscribirDeTopico() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("stock_" + nombre)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Desuscrito de stock_" + nombre);
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("suscrito_" + nombre, false).apply();
                        Toast.makeText(this, "Desuscrito de notificaciones de " + nombre, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error al desuscribirse: ", task.getException());
                        Toast.makeText(this, "Error al desuscribirse", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentToken = task.getResult();
                        enviarTokenAlServidor(currentToken);
                    } else {
                        Log.e(TAG, "Error obteniendo token FCM: ", task.getException());
                    }
                });
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Chat de Acciones",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Notificaciones del chat de acciones");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void mostrarNotificacion(Context context, String ticker, String mensaje, String remitente) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, InfoStock.class);
        intent.putExtra("nombre", ticker);
        intent.putExtra("abrirChat", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                ticker.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.chat)
                .setContentTitle(context.getString(R.string.nuevo_mensaje) + " " + ticker)
                .setContentText(remitente + ": " + mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        notificationManager.notify(ticker.hashCode(), builder.build());
    }

    private void enviarTokenAlServidor(String token) {
        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/registrar_token.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String email = prefs.getString("currentUser", "");

                String parametros = "token=" + URLEncoder.encode(token, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(parametros.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Token actualizado en servidor");
                } else {
                    Log.e(TAG, "Error HTTP: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en registro de token: ", e);
            }
        }).start();
    }

    private void inicializarChat() {
        recyclerMensajes = findViewById(R.id.recycler_mensajes);
        etMensaje = findViewById(R.id.et_mensaje);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        usuarioActual = prefs.getString("currentUserName", "Usuario");

        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, usuarioActual);
        recyclerMensajes.setAdapter(chatAdapter);

        findViewById(R.id.btn_enviar).setOnClickListener(v -> {
            if (!etMensaje.getText().toString().trim().isEmpty()) {
                enviarMensaje();
            }
        });

        findViewById(R.id.btn_cerrar_chat).setOnClickListener(v -> {
            findViewById(R.id.chat_cont).setVisibility(View.GONE);
            chatVisible = false;
        });

        findViewById(R.id.fab_chat).setOnClickListener(v -> {
            findViewById(R.id.chat_cont).setVisibility(View.VISIBLE);
            cargarMensajes();
            chatVisible = true;
        });
    }

    private void cargarMensajes() {
        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/obtener_mensajes.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String parametros = "ticker=" + URLEncoder.encode(nombre, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(parametros.getBytes("UTF-8"));
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                List<Mensaje> mensajes = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mensajes.add(new Mensaje(
                            obj.getInt("id"),
                            obj.getString("mensaje"),
                            obj.getString("remitente"),
                            obj.getLong("timestamp")
                    ));
                }

                runOnUiThread(() -> {
                    if (chatAdapter.getItemCount() != mensajes.size()) {
                        chatAdapter.setMensajes(mensajes);
                        recyclerMensajes.scrollToPosition(mensajes.size() - 1);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error cargando mensajes: ", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando mensajes", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void enviarMensaje() {
        String contenido = etMensaje.getText().toString().trim();
        if (contenido.isEmpty()) return;

        etMensaje.setText("");

        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/enviar_mensaje.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String parametros = "ticker=" + URLEncoder.encode(nombre, "UTF-8") +
                        "&mensaje=" + URLEncoder.encode(contenido, "UTF-8") +
                        "&remitente=" + URLEncoder.encode(usuarioActual, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(parametros.getBytes("UTF-8"));
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getBoolean("success")) {
                    Log.d("JSONENVIO", String.valueOf(jsonResponse.getInt("http_code")));
                    Mensaje nuevo = new Mensaje(
                            jsonResponse.getInt("id"),
                            contenido,
                            usuarioActual,
                            jsonResponse.getLong("timestamp")
                    );

                    runOnUiThread(() -> {
                        chatAdapter.agregarMensaje(nuevo);
                        recyclerMensajes.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error enviando mensaje: ", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error enviando mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void gestionInfo(StockDB db2) {
        StockDB db = (db2 == null) ? new StockDB(this) : db2;
        Cursor cursor = db.obtenerDetallesCompletos();

        String nombreI;
        if (nombre == null) {
            NOM_ARCHIVO = getIntent().getStringExtra("nombre") + ".txt";
            nombreI = getIntent().getStringExtra("nombre");
        } else {
            NOM_ARCHIVO = nombre + ".txt";
            nombreI = null;
        }

        while (cursor.moveToNext()) {
            String nombreC = cursor.getString(1);
            if ((nombre != null && nombre.equals(nombreC)) || (nombreI != null && nombreI.equals(nombreC))) {
                TextView name = findViewById(R.id.stockNom);
                nombre = cursor.getString(1);
                name.setText(nombre);

                TextView desc = findViewById(R.id.stockDescrip);
                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                String idioma = prefs.getString("Idioma", "es");
                String txt = cursor.getString(2);

                TranslatorOptions options;
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
                DownloadConditions conditions = new DownloadConditions.Builder().build();

                traductor.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener(unused -> {
                            if (isFinishing()) return;
                            traductor.translate(txt)
                                    .addOnSuccessListener(texto_traducido -> {
                                        if (!isFinishing()) desc.setText(texto_traducido);
                                    })
                                    .addOnFailureListener(e -> desc.setText(txt));
                        })
                        .addOnFailureListener(e -> desc.setText(txt));

                TextView prec = findViewById(R.id.stockPrecio);
                prec.setText(cursor.getString(3) + " $");

                TextView nota = findViewById(R.id.notas);
                nota.setText(cursor.getString(4));

                TextView sim = findViewById(R.id.stockSimilar);
                sim.setText(getString(R.string.relacionado) + " " + cursor.getString(5));

                cargarUbicacionEmpresa(nombre);
            }
        }

        notasEditText = findViewById(R.id.notas);
        String notasGuardadas = leerNotas();
        if (notasGuardadas != null) {
            notasEditText.setText(notasGuardadas);
        }

        TextView tradingView = findViewById(R.id.tradingView);
        tradingView.setOnClickListener(b -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.tradingview.tradingviewapp")));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.tradingview.tradingviewapp")));
            }
        });
    }

    private void cargarUbicacionEmpresa(String ticker) {
        Uri uri = Uri.parse("content://com.ibaisologuestoa.acciones500.ubicacionesprovider/ubicaciones");
        String[] projection = {"latitud", "longitud", "nombre_sede"};
        String selection = "ticker = ?";
        String[] selectionArgs = {ticker};

        try (Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                latitudSede = cursor.getDouble(0);
                longitudSede = cursor.getDouble(1);
                String nombreSede = cursor.getString(2);

                GeoPoint punto = new GeoPoint(latitudSede, longitudSede);
                mapController.setCenter(punto);
                mapController.setZoom(3.0);

                mapView.getOverlays().clear();
                Marker marcador = new Marker(mapView);
                marcador.setPosition(punto);
                marcador.setTitle(nombreSede);
                marcador.setSnippet(getString(R.string.sede_empresa));
                mapView.getOverlays().add(marcador);
                mapView.invalidate();

                obtenerUbicacionActual();
            } else {
                GeoPoint defaultPoint = new GeoPoint(40.416775, -3.703790);
                mapController.setCenter(defaultPoint);
                tvDistancia.setText("Distancia a la sede: No disponible");
            }
        }
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitudUsuario = location.getLatitude();
                        double longitudUsuario = location.getLongitude();

                        float[] results = new float[1];
                        Location.distanceBetween(latitudUsuario, longitudUsuario,
                                latitudSede, longitudSede, results);
                        float distancia = results[0] / 1000; // en kilómetros
                        tvDistancia.setText(String.format("Distancia a la sede: %.2f km", distancia));

                        GeoPoint puntoUsuario = new GeoPoint(latitudUsuario, longitudUsuario);
                        Marker marcadorUsuario = new Marker(mapView);
                        marcadorUsuario.setPosition(puntoUsuario);
                        marcadorUsuario.setTitle("Tú estás aquí");
                        marcadorUsuario.setIcon(
                                ContextCompat.getDrawable(this, R.drawable.ubi)
                        );
                        mapView.getOverlays().add(marcadorUsuario);

                        mapView.invalidate();
                    } else {
                        tvDistancia.setText("Distancia a la sede: No disponible");
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error obteniendo ubicación: ", e);
                    tvDistancia.setText("Distancia a la sede: Error");
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                tvDistancia.setText("Distancia a la sede: Permiso denegado");
            }
        }
    }

    private String leerNotas() {
        StringBuilder contenido = new StringBuilder();
        try (FileInputStream fis = openFileInput(NOM_ARCHIVO);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo notas: ", e);
        }
        return contenido.toString();
    }

    private void guardarNotas() {
        try (FileOutputStream fos = openFileOutput(NOM_ARCHIVO, Context.MODE_PRIVATE)) {
            fos.write(notasEditText.getText().toString().getBytes());
        } catch (Exception e) {
            Log.e(TAG, "Error guardando notas: ", e);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem temaItem = menu.findItem(R.id.action_tema);
        temaItem.setOnMenuItemClickListener(item -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            boolean nuevoModo = currentMode != AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(
                    nuevoModo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putBoolean(TEMA, nuevoModo)
                    .apply();

            recreate();
            return true;
        });

        MenuItem buscarItem = menu.findItem(R.id.action_search);
        buscarItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, nombre + " stock");
            startActivity(intent);
            return true;
        });

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (currentToken != null) {
            enviarTokenAlServidor(currentToken);
        }
        handler.post(actualizadorMensajes);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        guardarNotas();
        handler.removeCallbacks(actualizadorMensajes);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (traductor != null) {
            // traductor.close();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        aplicarIdioma();
        recreate();
    }

    private Runnable actualizadorMensajes = new Runnable() {
        @Override
        public void run() {
            if (chatVisible) {
                cargarMensajes();
            }
            handler.postDelayed(this, INTERVALO_ACTUALIZACION);
        }
    };
}