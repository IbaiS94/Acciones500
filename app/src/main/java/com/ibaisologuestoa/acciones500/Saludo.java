package com.ibaisologuestoa.acciones500;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Saludo extends AppCompatActivity {

    private static final String PREFS = "AppPrefs";
    private static final String USER_PREFS = "UserPrefs";
    private static final String CHANNEL_ID = "NotificacionesApp";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private SharedPreferences sharedPrefs;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        aplicarIdioma();
        verificarEstadoAutenticacion();
        gestionarNotificaciones();
    }

    private void verificarEstadoAutenticacion() {
        boolean isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false);
        boolean isFirstTime = sharedPrefs.getBoolean("firstTime", true);

        if (isFirstTime) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("firstTime", false);
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            isLoggedIn = false;
        }

        if (isLoggedIn) {
            mostrarInterfazLogueado();
        } else {
            mostrarInterfazBienvenida();
        }
    }
    private void mostrarInterfazBienvenida() {
        setContentView(R.layout.activity_auth);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegistro);

        btnLogin.setOnClickListener(v -> mostrarDialogo(R.layout.dialog_login, true));
        btnRegister.setOnClickListener(v -> mostrarDialogo(R.layout.dialog_registro, false));
    }


    private void cerrarSesion() {
        sharedPrefs.edit()
                .clear()
                .apply();
        Intent intent = new Intent(this, Saludo.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarDialogo(int layout, boolean esLogin) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(layout, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        configurarVistasDialogo(view, dialog, esLogin);
    }

    private void configurarVistasDialogo(View view, AlertDialog dialog, boolean esLogin) {
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPass = view.findViewById(R.id.etPassword);

        if (esLogin) {
            configurarLogin(view, dialog, etEmail, etPass);
        } else {
            configurarRegistro(view, dialog, etEmail, etPass);
        }
    }

    private void configurarLogin(View view, AlertDialog dialog, EditText etEmail, EditText etPass) {
        Button btnLogin = view.findViewById(R.id.btnIniciarSesion);
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if (validarInputs(email, pass)) autenticarUsuario(email, pass, dialog);
        });
    }

    private void configurarRegistro(View view, AlertDialog dialog, EditText etEmail, EditText etPass) {
        EditText etNombre = view.findViewById(R.id.campoNombre);
        EditText etConfirmar = view.findViewById(R.id.etConfirmarPassword);
        Button btnRegistro = view.findViewById(R.id.btnCompletarRegistro);

        btnRegistro.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String confirmar = etConfirmar.getText().toString().trim();
            if (validarRegistro(nombre, email, pass, confirmar)) registrarUsuario(nombre, email, pass, dialog);
        });
    }

    private boolean validarInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError("Email inválido");
            return false;
        }
        return true;
    }

    private boolean validarRegistro(String nombre, String email, String password, String confirmar) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarError("Email inválido");
            return false;
        }
        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        if (!password.equals(confirmar)) {
            mostrarError("Las contraseñas no coinciden");
            return false;
        }
        return true;
    }

    private void autenticarUsuario(String email, String pass, AlertDialog dialog) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", pass);
        } catch (JSONException e) {
            mostrarError("Error en datos");
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/login.php",
                params,
                response -> {
                    try {
                        if (response.getBoolean("exito")) {
                            String nombre = response.getString("nombre");
                            guardarSesion(email, nombre);
                            dialog.dismiss();

                            Intent intent = new Intent(Saludo.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            mostrarError(response.getString("mensaje"));
                        }
                    } catch (JSONException e) {
                        mostrarError("Error en respuesta");
                    }
                },
                error -> mostrarError("Error de conexión")
        );
        requestQueue.add(request);
    }

    private void registrarUsuario(String nombre, String email, String pass, AlertDialog dialog) {
        JSONObject params = new JSONObject();
        try {
            params.put("nombre", nombre);
            params.put("email", email);
            params.put("password", pass);
        } catch (JSONException e) {
            mostrarError("Error en datos");
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/registro.php",
                params,
                response -> {
                    try {
                        if (response.getBoolean("exito")) {
                            guardarSesion(email, nombre);
                            dialog.dismiss();

                            Intent intent = new Intent(Saludo.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            mostrarError(response.getString("mensaje"));
                        }
                    } catch (JSONException e) {
                        mostrarError("Error en respuesta");
                    }
                },
                error -> mostrarError("Error de conexión")
        );
        requestQueue.add(request);
    }

    private void mostrarInterfazLogueado() {
        setContentView(R.layout.saludo_login);

        String nombre = sharedPrefs.getString("currentUserName", "");
        String email = sharedPrefs.getString("currentUser", "");

        TextView tvSaludo = findViewById(R.id.tvSaludo);
        TextView tvEmail = findViewById(R.id.tvEmail);
        Button btnCerrar = findViewById(R.id.btnCerrarSesion);

        tvSaludo.setText(getString(R.string.bienvenido, nombre));
        tvEmail.setText(getString(R.string.logeado_como, email));

        View rootView = findViewById(R.id.rootLayout);
        rootView.setOnClickListener(v -> {
            startActivity(new Intent(Saludo.this, MainActivity.class));
            finish();
        });
        btnCerrar.setOnClickListener(v -> cerrarSesion());
    }


    private void guardarSesion(String email, String nombre) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("currentUser", email);
        editor.putString("currentUserName", nombre);
        editor.putBoolean("firstTime", false);
        editor.apply();
        aplicarIdioma();
    }


    private void aplicarIdioma() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String nuevoIdioma = prefs.getString("Idioma", "es");
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        if (!nuevoIdioma.equals(config.locale.getLanguage())) {
            Locale locale = new Locale(nuevoIdioma);
            Locale.setDefault(locale);
            config.setLocale(locale);
            res.updateConfiguration(config, res.getDisplayMetrics());
            recreate();
        }
    }

    private void gestionarNotificaciones() {
        pedirPermisoNotificaciones();
        crearCanalNotificaciones();
        mostrarNotificacion();
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones importantes",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para notificaciones de la app");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void mostrarNotificacion() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Recordatorio importante")
                .setContentText("¡Gracias por usar nuestra app!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        getSystemService(NotificationManager.class).notify(1, builder.build());
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mostrarNotificacion();
        }
    }
}