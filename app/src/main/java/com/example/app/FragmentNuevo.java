package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private String FILE_NAME;

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
            Log.d("Args", "Recibido: " + nombreArg);
        }

        StockDB db = new StockDB(getContext());
        Cursor cursor = db.obtenerDescripcion();

        String nombreI = null;
        if (nombreArg != null) {
            FILE_NAME = nombreArg + ".txt";
            nombreI = nombreArg;
        } else {
            FILE_NAME = nombre + ".txt";
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
                descTextView.setText(cursor.getString(2));

                TextView precTextView = view.findViewById(R.id.stockPrecio);
                String euro = cursor.getString(3) + "€";
                precTextView.setText(euro);

                TextView notaTextView = view.findViewById(R.id.notas);
                notaTextView.setText(cursor.getString(4));

                TextView simTextView = view.findViewById(R.id.stockSimilar);
                simTextView.setText(cursor.getString(5));
            }
        }
        cursor.close();

        notasEditText = view.findViewById(R.id.notas);
        String notasGuardadas = leerNotas();
        if (notasGuardadas != null) {
            notasEditText.setText(notasGuardadas);
        }
        TextView tradingView = view.findViewById(R.id.tradingView);
        tradingView.setClickable(Boolean.TRUE);
        tradingView.setOnClickListener(b -> {
            if (getActivity() != null) {
                Intent launchIntent = getActivity().getPackageManager()
                        .getLaunchIntentForPackage("com.tradingview.tradingviewapp");
                if (launchIntent != null) {
                    getActivity().startActivity(launchIntent);
                } else {
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.tradingview.tradingviewapp"));
                    getActivity().startActivity(playStoreIntent);
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
            try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
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
            try (FileInputStream fis = context.openFileInput(FILE_NAME);
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
