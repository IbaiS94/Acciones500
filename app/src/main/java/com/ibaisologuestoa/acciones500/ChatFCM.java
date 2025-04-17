package com.ibaisologuestoa.acciones500;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatFCM extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("GSDGSFDH", "agagag");
        String ticker = remoteMessage.getData().get("ticker");
            String mensaje = remoteMessage.getData().get("mensaje");
            String remitente = remoteMessage.getData().get("remitente");
            if (ticker != null && mensaje != null && remitente != null) {
                InfoStock.mostrarNotificacion(
                        getApplicationContext(),
                        ticker,
                        mensaje,
                        remitente
                );
            }
        }
    }
