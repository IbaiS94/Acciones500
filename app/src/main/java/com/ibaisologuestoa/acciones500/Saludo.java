package com.ibaisologuestoa.acciones500;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.ImageDecoder;


public class Saludo extends AppCompatActivity {

    private static final String PREFS = "AppPrefs";
    private static final String USER_PREFS = "UserPrefs";
    private static final String CHANNEL_ID = "NotificacionesApp";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private SharedPreferences sharedPrefs;
    private RequestQueue requestQueue;
    private static final int REQUEST_CODE_GALLERY = 100;
    private static final int REQUEST_CODE_CAMERA = 101;
    private static final int PERMISSION_CODE_CAMERA = 102;
    private static final int PERMISSION_CODE_GALLERY = 103;
    private Uri rutaFotoActual;
    private View dialog_log= null;
    private View dialog_reg = null;

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

        if (layout == R.layout.dialog_registro) {
             dialog_reg = inflater.inflate(layout, null);
             builder.setView(dialog_reg);
        }
        else {
            dialog_log = inflater.inflate(layout, null);
            builder.setView(dialog_log);
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        if (layout == R.layout.dialog_registro) {
            configurarVistasDialogo(dialog_reg, dialog, esLogin);
        }
        else{
            configurarVistasDialogo(dialog_log, dialog, esLogin);
        }
    }

    private void configurarVistasDialogo(View view, AlertDialog dialog, boolean esLogin) {
        if (esLogin) {
            configurarLogin(view, dialog);
        } else {
            configurarRegistro(view, dialog);
        }
    }

    private void configurarLogin(View view, AlertDialog dialog) {
        Button btnLogin = view.findViewById(R.id.btnIniciarSesion);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPass = view.findViewById(R.id.etPassword);
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if (validarInputs(email, pass)) autenticarUsuario(email, pass, dialog);
        });
    }

    private void configurarRegistro(View view, AlertDialog dialog) {
        EditText etEmailre = view.findViewById(R.id.etEmail);
        EditText etPassre = view.findViewById(R.id.etPassword);
        EditText etNombre = view.findViewById(R.id.campoNombre);
        EditText etConfirmar = view.findViewById(R.id.etConfirmarPassword);
        Button btnRegistro = view.findViewById(R.id.btnCompletarRegistro);
        Button btnFoto = view.findViewById(R.id.botonSeleccionarFoto);
        ImageView foto = view.findViewById(R.id.fotoPerfilreg);
        btnFoto.setOnClickListener(a -> mostrarSelectorImagen());
        btnRegistro.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmailre.getText().toString().trim();
            String pass = etPassre.getText().toString().trim();
            String confirmar = etConfirmar.getText().toString().trim();
            Bitmap bfoto = ((BitmapDrawable) foto.getDrawable()).getBitmap();
            if (validarRegistro(nombre, email, pass, confirmar)) registrarUsuario(nombre, email, pass, bfoto, dialog);
        });
    }

    private boolean validarInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
                                new Thread(() -> {
                                    try {
                                        // Buscar imagen en server
                                        String imageUrl = "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/obtener_foto.php?email=" + email;
                                        URL url = new URL(imageUrl);
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setDoInput(true);
                                        connection.connect();

                                        InputStream input = connection.getInputStream();
                                        Bitmap bitmap = BitmapFactory.decodeStream(input);

                                        if (bitmap != null) {
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);

                                            SharedPreferences.Editor editor = sharedPrefs.edit();
                                            editor.putString("imagen_" + email, base64Image);
                                            editor.apply();
                                        }
                                    } catch (IOException e) {
                                        Log.e("LOGIN", "Error al descargar imagen: " + e.getMessage());
                                    } finally {
                                        runOnUiThread(() -> {
                                            Intent intent = new Intent(Saludo.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        });
                                    }
                                }).start();


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

    private void registrarUsuario(String nombre, String email, String pass, Bitmap bfoto, AlertDialog dialog) {
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

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bfoto.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            String fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                            JSONObject paramsPhoto = new JSONObject();
                            try {
                                paramsPhoto.put("email", email);
                                paramsPhoto.put("foto", fotoBase64);
                            } catch (JSONException e) {
                                Log.e("ERROR", "Error al preparar datos de la foto: " + e.getMessage());
                            }

                            JsonObjectRequest photoRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/isologuestoa001/WEB/guardar_foto.php",
                                    paramsPhoto,
                                    photoResponse -> {
                                        try {
                                            if (photoResponse.getBoolean("exito")) {
                                                Log.d("FOTO", "Foto de perfil guardada correctamente");
                                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                                editor.putString("imagen_" + email, fotoBase64);
                                                editor.apply();
                                            } else {
                                                Log.e("FOTO", "Error al guardar foto: " + photoResponse.getString("mensaje"));
                                            }
                                        } catch (JSONException e) {
                                            Log.e("FOTO", "Error en respuesta de foto: " + e.getMessage());
                                        }
                                        Intent intent = new Intent(Saludo.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    },
                                    error -> {
                                        Log.e("FOTO", "Error al subir foto: " + error.toString());
                                        finish();
                                    }
                            );

                            requestQueue.add(photoRequest);
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
        ImageView imgUsuario = findViewById(R.id.imgUsuario);

        tvSaludo.setText(getString(R.string.bienvenido, nombre));
        tvEmail.setText(getString(R.string.logeado_como, email));

        cargarImagenUsuario(email, imgUsuario);

        View rootView = findViewById(R.id.rootLayout);
        rootView.setOnClickListener(v -> {
            startActivity(new Intent(Saludo.this, MainActivity.class));
            finish();
        });
        btnCerrar.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarImagenUsuario(String email, ImageView imageView) {
        String clave = "imagen_" + email;
        String imgGuardada = sharedPrefs.getString(clave, null);

        if (imgGuardada != null) {
            byte[] dBytes = Base64.decode(imgGuardada, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(dBytes, 0, dBytes.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.person2);
        }
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
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
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_CODE_CAMERA:
                    abrirCamara();
                    break;
                case PERMISSION_CODE_GALLERY:
                    abrirGaleria();
                    break;
                case NOTIFICATION_PERMISSION_CODE:
                             mostrarNotificacion();
                             break;
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarSelectorImagen() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar imagen")
                .setItems(new String[]{"Tomar foto", "Elegir de galería"}, (dialog, which) -> {
                    if (which == 0) {
                        verificarPermisoCamara();
                    } else {
                        verificarPermisoGaleria();
                    }
                })
                .show();
    }

    private void verificarPermisoGaleria() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_CODE_GALLERY);
        }
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE_CAMERA);
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = crearArchivoImagen();
        if (photoFile != null) {
            rutaFotoActual = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, rutaFotoActual);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
    }

    private File crearArchivoImagen() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                ImageView imageView = dialog_reg.findViewById(R.id.fotoPerfilreg);
                Uri imageUri = null;

                if (requestCode == REQUEST_CODE_GALLERY && data != null) {
                    imageUri = data.getData();
                } else if (requestCode == REQUEST_CODE_CAMERA) {
                    imageUri = rutaFotoActual;
                }

                if (imageUri != null) {
                    Bitmap bitmap = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(getContentResolver(), imageUri)
                    );
                    imageView.setImageBitmap(bitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (rutaFotoActual != null) {
                new File(rutaFotoActual.getPath()).delete();
            }
            Toast.makeText(this, "Operation canceled", Toast.LENGTH_SHORT).show();
        }
    }

}

