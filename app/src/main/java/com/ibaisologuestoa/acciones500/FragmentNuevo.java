package com.ibaisologuestoa.acciones500;
import static com.ibaisologuestoa.acciones500.MainActivity.PREFS;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import android.content.SharedPreferences;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FragmentNuevo extends Fragment {

    public String nombre = "Error";
    private EditText notasEditText;
    private String NOM_ARCHIVO;
    private Translator traductor;

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

        Bundle args = getArguments();
        String nombreArg = null;
        if (args != null) {
            nombreArg = args.getString("nombre");
            Log.d("FragmentNuevo", "Nombre recibido: " + nombreArg);
        }

        StockDB db = new StockDB(requireContext());
        Cursor cursor = db.obtenerDescripcion();

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

                TextView nameTextView = view.findViewById(R.id.stockNom);
                nombre = nombreC;
                if (listener != null) {
                    listener.onNombreActualizado(nombre);
                }
                nameTextView.setText(nombre);

                TextView descTextView = view.findViewById(R.id.stockDescrip);
                String txt = cursor.getString(2);

                SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String idioma = prefs.getString("Idioma", "es");

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
                            .addOnSuccessListener(unused ->
                                    traductor.translate(txt)
                                            .addOnSuccessListener(translatedText -> {
                                                if (isAdded()) { // Verificar que el fragment está adjunto
                                                    descTextView.setText(translatedText);
                                                    Log.d("FragmentTraducción", "Traducción exitosa");
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FragmentTraducción", "Error traducción: " + e.getMessage());
                                                if (isAdded()) descTextView.setText(txt);
                                            })
                            )
                            .addOnFailureListener(e -> {
                                Log.e("FragmentTraducción", "Error modelo: " + e.getMessage());
                                if (isAdded()) descTextView.setText(txt);
                            });


                TextView precTextView = view.findViewById(R.id.stockPrecio);
                String euro = cursor.getString(3) + "€";
                precTextView.setText(euro);

                TextView notaTextView = view.findViewById(R.id.notas);
                notaTextView.setText(cursor.getString(4));

                TextView simTextView = view.findViewById(R.id.stockSimilar);
                simTextView.setText(getString(R.string.relacionado)+" "+cursor.getString(5));
            }
        }
        cursor.close();

        notasEditText = view.findViewById(R.id.notas);
        String notasGuardadas = leerNotas();
        if (notasGuardadas != null) {
            notasEditText.setText(notasGuardadas);
        }

        TextView tradingView = view.findViewById(R.id.tradingView);
        tradingView.setClickable(true);
        tradingView.setOnClickListener(b -> {
            if (getActivity() != null) {
                Intent launchIntent = requireActivity().getPackageManager()
                        .getLaunchIntentForPackage("com.tradingview.tradingviewapp");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.tradingview.tradingviewapp"));
                    startActivity(playStoreIntent);
                }
            }
        });
    }


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
            throw new RuntimeException();
        }
    }
    private void guardarNotas() {
        String texto = notasEditText.getText().toString();
        Context context = getContext();
        if (context != null) {
            try (FileOutputStream fos = context.openFileOutput(NOM_ARCHIVO, Context.MODE_PRIVATE)) {
                fos.write(texto.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String leerNotas() {
        StringBuilder sb = new StringBuilder();
        Context context = getContext();
        if (context != null) {
            try (FileInputStream fis = context.openFileInput(NOM_ARCHIVO);
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
        }
        return sb.toString();
    }

    @Override
    public void onPause() {
        super.onPause();
        guardarNotas();
    }
}
