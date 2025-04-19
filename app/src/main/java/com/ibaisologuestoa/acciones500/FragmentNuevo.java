package com.ibaisologuestoa.acciones500;

import static com.ibaisologuestoa.acciones500.MainActivity.PREFS;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

public class FragmentNuevo extends Fragment {
    private static final String TAG = "FragmentNuevo";
    private static final String CHANNEL_ID = "chat_notifications";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long INTERVALO_ACTUALIZACION = 3000; // 3 segundos

    public String nombre = "Error";
    private EditText notasEditText;
    private String NOM_ARCHIVO;
    private Translator traductor;
    private MapView mapView;
    private IMapController mapController;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvDistancia;
    private double latitudSede, longitudSede;

    // Chat components
    private RecyclerView recyclerMensajes;
    private ChatAdapter chatAdapter;
    private EditText etMensaje;
    private String usuarioActual;
    private String currentToken;
    private Handler handler = new Handler();
    private boolean chatVisible = false;
    private Switch switchNotificaciones;

    public interface OnNombreActualizadoListener {
        void onNombreActualizado(String nombreActualizado);
    }

    private OnNombreActualizadoListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNombreActualizadoListener) {
            listener = (OnNombreActualizadoListener) context;
        } else {
            throw new RuntimeException(context + " debe implementar OnNombreActualizadoListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.framedch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapController = mapView.getController();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        tvDistancia = view.findViewById(R.id.tv_distancia);
        switchNotificaciones = view.findViewById(R.id.switch_notificaciones);

        Bundle args = getArguments();
        String nombreArg = null;
        if (args != null) {
            nombreArg = args.getString("nombre");
            Log.d(TAG, "Nombre recibido: " + nombreArg);

            if (args.getBoolean("abrirChat", false)) {
                view.findViewById(R.id.chat_cont).setVisibility(View.VISIBLE);
                chatVisible = true;
            }
        }

        gestionInfo(nombreArg);
        inicializarChat(view);
        setupFCM();
        createNotificationChannel();

        TextView tradingView = view.findViewById(R.id.tradingView);
        tradingView.setClickable(true);
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

    private void inicializarChat(View view) {
        recyclerMensajes = view.findViewById(R.id.recycler_mensajes);
        etMensaje = view.findViewById(R.id.et_mensaje);

        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        usuarioActual = prefs.getString("currentUserName", "Usuario");

        recyclerMensajes.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatAdapter = new ChatAdapter(requireContext(), usuarioActual);
        recyclerMensajes.setAdapter(chatAdapter);

        view.findViewById(R.id.btn_enviar).setOnClickListener(v -> {
            if (!etMensaje.getText().toString().trim().isEmpty()) {
                enviarMensaje();
            }
        });

        view.findViewById(R.id.btn_cerrar_chat).setOnClickListener(v -> {
            view.findViewById(R.id.chat_cont).setVisibility(View.GONE);
            chatVisible = false;
        });

        view.findViewById(R.id.fab_chat).setOnClickListener(v -> {
            view.findViewById(R.id.chat_cont).setVisibility(View.VISIBLE);
            cargarMensajes();
            chatVisible = true;
        });

        if (chatVisible) {
            cargarMensajes();
        }
    }

    private void setupFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        currentToken = task.getResult();
                        enviarTokenAlServidor(currentToken);
                        configurarNotificaciones();
                    } else {
                        Log.e(TAG, "Error obteniendo token FCM: ", task.getException());
                    }
                });
    }

    private void configurarNotificaciones() {
        if (switchNotificaciones == null || !isAdded() || nombre == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
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
        if (!isAdded() || nombre == null) return;

        FirebaseMessaging.getInstance().subscribeToTopic("stock_" + nombre)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Log.d(TAG, "Suscrito a stock_" + nombre);
                        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putBoolean("suscrito_" + nombre, true).apply();
                        Toast.makeText(requireContext(), "Suscrito a notificaciones de " + nombre,
                                Toast.LENGTH_SHORT).show();
                    } else if (isAdded()) {
                        Log.e(TAG, "Error al suscribirse: ", task.getException());
                        Toast.makeText(requireContext(), "Error al suscribirse",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void desuscribirDeTopico() {
        if (!isAdded() || nombre == null) return;

        FirebaseMessaging.getInstance().unsubscribeFromTopic("stock_" + nombre)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Log.d(TAG, "Desuscrito de stock_" + nombre);
                        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putBoolean("suscrito_" + nombre, false).apply();
                        Toast.makeText(requireContext(), "Desuscrito de notificaciones de " + nombre,
                                Toast.LENGTH_SHORT).show();
                    } else if (isAdded()) {
                        Log.e(TAG, "Error al desuscribirse: ", task.getException());
                        Toast.makeText(requireContext(), "Error al desuscribirse",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNotificationChannel() {
        if (!isAdded()) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat de Acciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notificaciones del chat de acciones");

            NotificationManager notificationManager =
                    requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void enviarTokenAlServidor(String token) {
        if (!isAdded()) return;

        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/registrar_token.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
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

    private void cargarMensajes() {
        if (!isAdded() || nombre == null) return;

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

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (chatAdapter != null && chatAdapter.getItemCount() != mensajes.size()) {
                            chatAdapter.setMensajes(mensajes);
                            recyclerMensajes.scrollToPosition(mensajes.size() - 1);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error cargando mensajes: ", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error cargando mensajes",
                                    Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void enviarMensaje() {
        if (!isAdded() || nombre == null || etMensaje == null) return;

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

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (chatAdapter != null) {
                                chatAdapter.agregarMensaje(nuevo);
                                recyclerMensajes.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error enviando mensaje: ", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error enviando mensaje",
                                    Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void gestionInfo(String nombreArg) {
        StockDB db = new StockDB(requireContext());
        Cursor cursor = db.obtenerDetallesCompletos();

        String nombreI = null;
        if (nombreArg != null) {
            NOM_ARCHIVO = nombreArg + ".txt";
            nombreI = nombreArg;
        } else {
            NOM_ARCHIVO = nombre + ".txt";
        }

        while (cursor.moveToNext()) {
            String nombreC = cursor.getString(1);
            if ((nombre != null && nombre.equals(nombreC)) ||
                    (nombreI != null && nombreI.equals(nombreC))) {

                TextView nameTextView = requireView().findViewById(R.id.stockNom);
                nombre = nombreC;
                if (listener != null) {
                    listener.onNombreActualizado(nombre);
                }
                nameTextView.setText(nombre);

                TextView descTextView = requireView().findViewById(R.id.stockDescrip);
                SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
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
                        descTextView.setText(txt);
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
                            if (!isAdded()) return;
                            traductor.translate(txt)
                                    .addOnSuccessListener(texto_traducido -> {
                                        if (isAdded()) descTextView.setText(texto_traducido);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) descTextView.setText(txt);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            if (isAdded()) descTextView.setText(txt);
                        });

                TextView precTextView = requireView().findViewById(R.id.stockPrecio);
                precTextView.setText(cursor.getString(3) + " $");

                TextView notaTextView = requireView().findViewById(R.id.notas);
                notaTextView.setText(cursor.getString(4));

                TextView simTextView = requireView().findViewById(R.id.stockSimilar);
                simTextView.setText(getString(R.string.relacionado) + " " + cursor.getString(5));

                // Cargar ubicación de la empresa
                cargarUbicacionEmpresa(nombre);

                break;
            }
        }
        cursor.close();

        notasEditText = requireView().findViewById(R.id.notas);
        String notasGuardadas = leerNotas();
        if (notasGuardadas != null) {
            notasEditText.setText(notasGuardadas);
        }
    }

    private void cargarUbicacionEmpresa(String ticker) {
        if (mapView == null || !isAdded()) return;

        Uri uri = Uri.parse("content://com.ibaisologuestoa.acciones500.ubicacionesprovider/ubicaciones");
        String[] projection = {"latitud", "longitud", "nombre_sede"};
        String selection = "ticker = ?";
        String[] selectionArgs = {ticker};

        try (Cursor cursor = requireContext().getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
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
                if (tvDistancia != null) {
                    tvDistancia.setText("Distancia a la sede: No disponible");
                }
            }
        }
    }

    private void obtenerUbicacionActual() {
        if (mapView == null || !isAdded()) return;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && isAdded()) {
                        double latitudUsuario = location.getLatitude();
                        double longitudUsuario = location.getLongitude();

                        float[] results = new float[1];
                        Location.distanceBetween(latitudUsuario, longitudUsuario,
                                latitudSede, longitudSede, results);
                        float distancia = results[0] / 1000; // en kilómetros

                        if (tvDistancia != null) {
                            tvDistancia.setText(String.format("Distancia a la sede: %.2f km", distancia));
                        }

                        GeoPoint puntoUsuario = new GeoPoint(latitudUsuario, longitudUsuario);
                        Marker marcadorUsuario = new Marker(mapView);
                        marcadorUsuario.setPosition(puntoUsuario);
                        marcadorUsuario.setTitle("Tú estás aquí");
                        marcadorUsuario.setIcon(
                                ContextCompat.getDrawable(requireContext(), R.drawable.ubi)
                        );
                        mapView.getOverlays().add(marcadorUsuario);

                        mapView.invalidate();
                    } else if (isAdded() && tvDistancia != null) {
                        tvDistancia.setText("Distancia a la sede: No disponible");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo ubicación: ", e);
                    if (isAdded() && tvDistancia != null) {
                        tvDistancia.setText("Distancia a la sede: Error");
                    }
                });
    }

    private void guardarNotas() {
        if (notasEditText == null || !isAdded()) return;

        String texto = notasEditText.getText().toString();
        try (FileOutputStream fos = requireContext().openFileOutput(NOM_ARCHIVO, Context.MODE_PRIVATE)) {
            fos.write(texto.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error guardando notas: ", e);
        }
    }

    private String leerNotas() {
        if (!isAdded()) return "";

        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = requireContext().openFileInput(NOM_ARCHIVO);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
                sb.append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error leyendo notas: ", e);
        }
        return sb.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (currentToken != null && isAdded()) {
            enviarTokenAlServidor(currentToken);
        }
        handler.post(actualizadorMensajes);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        guardarNotas();
        handler.removeCallbacks(actualizadorMensajes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (traductor != null) {
            // traductor.close();
        }
        handler.removeCallbacks(actualizadorMensajes);
    }

    private Runnable actualizadorMensajes = new Runnable() {
        @Override
        public void run() {
            if (chatVisible && isAdded()) {
                cargarMensajes();
            }
            handler.postDelayed(this, INTERVALO_ACTUALIZACION);
        }
    };
}