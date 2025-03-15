package com.ibaisologuestoa.acciones500; // Cambia esto por el nombre de tu paquete

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class AdaptadorRv extends RecyclerView.Adapter<AdaptadorRv.MiViewHolder> {

    private List<String> listaItems;
    private Context context;

    // Constructor del adaptador
    public AdaptadorRv(Context context, List<String> listaItems) {
        this.context = context;
        this.listaItems = listaItems;
    }

    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout del ítem
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler, parent, false);
        return new MiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
        String item = listaItems.get(position);
        holder.textoItem.setText(item);

        // Verificar si el elemento actual es favorito
        StockDB stockDB = new StockDB(context);
        boolean esFavorito = stockDB.esFavorito(item);
        stockDB.close();

        if (esFavorito) {
            holder.favBot.setImageResource(R.drawable.star2);
            holder.favBot.setTag("fav");
        } else {
            holder.favBot.setImageResource(R.drawable.star);
            holder.favBot.setTag("fav_no");
        }
    }

    @Override
    public int getItemCount() {
        return listaItems.size();
    }

    public class MiViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textoItem;
        ImageButton favBot;

        public MiViewHolder(@NonNull View itemView) {
            super(itemView);
            textoItem = itemView.findViewById(R.id.tvItem);
            favBot= itemView.findViewById(R.id.fav_bot);
            itemView.setOnClickListener(this);
            favBot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Debug", String.valueOf(favBot));
                    new MaterialAlertDialogBuilder(itemView.getContext())
                            .setTitle(R.string.conf)
                            .setMessage(R.string.conf3)
                            .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!"fav".equals(favBot.getTag())) {
                                        favBot.setImageResource(R.drawable.star2);
                                        favBot.setTag("fav");
                                        StockDB t = new StockDB(itemView.getContext());
                                        SQLiteDatabase db = t.getWritableDatabase();
                                        t.actualizar(db, listaItems.get(getAdapterPosition()), Boolean.TRUE);
                                    } else {
                                        favBot.setImageResource(R.drawable.favorite);
                                        favBot.setTag("fav_no");
                                        StockDB t = new StockDB(itemView.getContext());
                                        SQLiteDatabase db = t.getWritableDatabase();
                                        t.actualizar(db, listaItems.get(getAdapterPosition()), Boolean.FALSE);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
        }


        @Override
        public void onClick(View v) {
            int posicion = getAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                View containerNuevo = ((Activity) context).findViewById(R.id.container_nuevo);
                if (containerNuevo != null) {
                    //modo landscape
                    FragmentNuevo fragment = new FragmentNuevo();
                    Bundle args = new Bundle();
                    args.putString("nombre", listaItems.get(posicion));
                    fragment.setArguments(args);
                    ((AppCompatActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_nuevo, fragment)
                            .commit();

                } else {
                    //modo portrait
                    Intent intent = new Intent(context, InfoStock.class);
                    intent.putExtra("nombre", listaItems.get(posicion));
                    context.startActivity(intent);
                }
            }
        }



    }

}
