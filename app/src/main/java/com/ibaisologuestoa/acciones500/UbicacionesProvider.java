package com.ibaisologuestoa.acciones500;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UbicacionesProvider extends ContentProvider {

    // Definiciones para el Content Provider
    public static final String AUTORIDAD = "com.ibaisologuestoa.acciones500.ubicacionesprovider";
    public static final Uri URI_CONTENIDO = Uri.parse("content://" + AUTORIDAD + "/ubicaciones");

    // Códigos para el UriMatcher
    private static final int UBICACIONES = 1;
    private static final int UBICACION_ID = 2;

    // UriMatcher para determinar qué tipo de URI se está solicitando
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTORIDAD, "ubicaciones", UBICACIONES);
        uriMatcher.addURI(AUTORIDAD, "ubicaciones/#", UBICACION_ID);
    }

    private StockDB mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StockDB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = db.query("Ubicaciones", projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case UBICACIONES:
                return "vnd.android.cursor.dir/vnd.com.ibaisologuestoa.ubicacion";
            case UBICACION_ID:
                return "vnd.android.cursor.item/vnd.com.ibaisologuestoa.ubicacion";
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != UBICACIONES) {
            throw new IllegalArgumentException("URI inválida para inserción: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert("Ubicaciones", null, values);

        if (id > 0) {
            Uri uriResultado = ContentUris.withAppendedId(URI_CONTENIDO, id);
            getContext().getContentResolver().notifyChange(uriResultado, null);
            return uriResultado;
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int filasEliminadas;

        switch (uriMatcher.match(uri)) {
            case UBICACIONES:
                filasEliminadas = db.delete("Ubicaciones", selection, selectionArgs);
                break;
            case UBICACION_ID:
                selection = "id = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                filasEliminadas = db.delete("Ubicaciones", selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        if (filasEliminadas > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return filasEliminadas;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int filasActualizadas;

        switch (uriMatcher.match(uri)) {
            case UBICACIONES:
                filasActualizadas = db.update("Ubicaciones", values, selection, selectionArgs);
                break;
            case UBICACION_ID:
                selection = "id = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                filasActualizadas = db.update("Ubicaciones", values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        if (filasActualizadas > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return filasActualizadas;
    }

    public static Uri getUbicacionByTickerUri(String ticker) {
        return Uri.parse(URI_CONTENIDO + "/ticker/" + ticker);
    }
}