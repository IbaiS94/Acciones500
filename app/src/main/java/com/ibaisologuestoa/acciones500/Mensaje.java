package com.ibaisologuestoa.acciones500;

public class Mensaje {
    private int id;
    private String texto;
    private String remitente;
    private long ts;

    public Mensaje(int id, String texto, String remitente, long ts) {
        this.id = id;
        this.texto = texto;
        this.remitente = remitente;
        this.ts = ts;
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

    public long getTs() {
        return ts;
    }
}