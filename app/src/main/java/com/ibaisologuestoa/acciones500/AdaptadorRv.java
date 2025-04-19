package com.ibaisologuestoa.acciones500;

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

    private List<StockItem> listaItems;
    private Context context;

    public AdaptadorRv(Context context, List<StockItem> listaItems) {
        this.context = context;
        this.listaItems = listaItems;
    }

    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler, parent, false);
        return new MiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
        StockItem item = listaItems.get(position);
        holder.textoItem.setText(item.nombre);
        holder.textoPrecio.setText(String.format("%.2f $", item.precio));
        StockDB stockDB = new StockDB(context);
        boolean esFavorito = stockDB.esFavorito(item.nombre);
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
        TextView textoPrecio;
        ImageButton favBot;

        public MiViewHolder(@NonNull View itemView) {
            super(itemView);
            textoItem = itemView.findViewById(R.id.tvItem);
            textoPrecio = itemView.findViewById(R.id.tvPrecio);
            favBot = itemView.findViewById(R.id.fav_bot);
            itemView.setOnClickListener(this);
            favBot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    Log.d("AdaptadorRv", "Favorito clicked position=" + position);

                    new MaterialAlertDialogBuilder(itemView.getContext())
                            .setTitle(R.string.conf)
                            .setMessage(R.string.conf3)
                            .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StockDB dbHelper = new StockDB(itemView.getContext());
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    boolean nowFav;
                                    if (!"fav".equals(favBot.getTag())) {
                                        favBot.setImageResource(R.drawable.star2);
                                        favBot.setTag("fav");
                                        nowFav = true;
                                    } else {
                                        favBot.setImageResource(R.drawable.star);
                                        favBot.setTag("fav_no");
                                        nowFav = false;
                                    }
                                    StockItem currentItem = listaItems.get(position);
                                    dbHelper.actualizar(db, currentItem.nombre, nowFav);
                                    db.close();
                                    notifyItemChanged(position);
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                View containerNuevo = ((Activity) context).findViewById(R.id.container_nuevo);
                if (containerNuevo != null) {
                    FragmentNuevo fragment = new FragmentNuevo();
                    Bundle args = new Bundle();
                    args.putString("nombre", listaItems.get(position).nombre);
                    fragment.setArguments(args);
                    ((AppCompatActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_nuevo, fragment)
                            .commit();
                } else {
                    Intent intent = new Intent(context, InfoStock.class);
                    intent.putExtra("nombre", listaItems.get(position).nombre);
                    context.startActivity(intent);
                }
            }
        }
    }
}
