package com.ibaisologuestoa.acciones500;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensajeViewHolder> {
    private static final int TIPO_MENSAJE_ENVIADO = 1;
    private static final int TIPO_MENSAJE_RECIBIDO = 2;

    private List<Mensaje> mensajes = new ArrayList<>();
    private Context context;
    private String usuarioActual;
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(Context context, String usuarioActual) {
        this.context = context;
        this.usuarioActual = usuarioActual;
    }

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
        notifyDataSetChanged();
    }

    public void agregarMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        notifyItemInserted(mensajes.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje = mensajes.get(position);

        if (mensaje.getRemitente().equals(usuarioActual)) {
            return TIPO_MENSAJE_ENVIADO;
        } else {
            return TIPO_MENSAJE_RECIBIDO;
        }
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TIPO_MENSAJE_ENVIADO) {
            view = LayoutInflater.from(context).inflate(R.layout.item_mensaje_enviado, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_mensaje_recibido, parent, false);
        }

        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);

        holder.textoMensaje.setText(mensaje.getTexto());

        if (getItemViewType(position) == TIPO_MENSAJE_RECIBIDO) {
            holder.nombreRemitente.setText(mensaje.getRemitente());
        }

        String hora = formatoHora.format(new Date(mensaje.getTs()));
        holder.horaMensaje.setText(hora);
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView textoMensaje, nombreRemitente, horaMensaje;

        MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            textoMensaje = itemView.findViewById(R.id.texto_mensaje);
            horaMensaje = itemView.findViewById(R.id.hora_mensaje);

            nombreRemitente = itemView.findViewById(R.id.nombre_remitente);
        }
    }
}