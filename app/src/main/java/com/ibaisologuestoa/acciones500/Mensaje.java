package com.ibaisologuestoa.acciones500;

public class Mensaje {
    private int id;
    private String texto;
    private String remitente;
    private long timestamp;

    public Mensaje(int id, String texto, String remitente, long timestamp) {
        this.id = id;
        this.texto = texto;
        this.remitente = remitente;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public String getRemitente() {
        return remitente;
    }

    public long getTimestamp() {
        return timestamp;
    }
}