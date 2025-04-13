package com.ibaisologuestoa.acciones500;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockDB extends SQLiteOpenHelper {
    private static final String DB = "stock.db";
    private static final int DB_VERSION = 1;

    private static final String ALPACA_API_KEY = "PKN6072L18KDD23KTP01";
    private static final String ALPACA_SECRET_KEY = "86emGQ1bud3gkvNJxbQmKm8MYV4QlcogkeNKSChz";
    private static final String ALPACA_BASE_URL = "https://data.alpaca.markets/v2/stocks/";

    public StockDB(Context context) {
        super(context, DB, null, DB_VERSION);
    }

    private static final String SQL =
            "CREATE TABLE Stocks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "favoritos BOOLEAN" +
                    ")";
    private static final String detalles =
            "CREATE TABLE Detalles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT, " +
                    "descripcion TEXT, " +
                    "Precio DOUBLE, " +
                    "Notas TEXT, " +
                    "Similares TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL);
        insertar(db, "AAPL", Boolean.FALSE);
        insertar(db, "MSFT", Boolean.FALSE);
        insertar(db, "GOOGL", Boolean.FALSE);
        insertar(db, "YUM", Boolean.FALSE); // Yum!
        insertar(db, "BLK", Boolean.FALSE); // BlackRock
        insertar(db, "AMD", Boolean.FALSE);
        insertar(db, "SPY", Boolean.FALSE); // SP500
        insertar(db, "NVO", Boolean.FALSE); // Novo Nordisk
        insertar(db, "UBER", Boolean.FALSE);
        insertar(db, "TSLA", Boolean.FALSE);
        insertar(db, "U", Boolean.FALSE); // Unity Software
        insertar(db, "INTC", Boolean.FALSE); // Intel
        insertar(db, "NVDA", Boolean.FALSE); // NVIDIA
        insertar(db, "KO", Boolean.FALSE); // Coca-Cola

        db.execSQL(detalles);
        // Insertar detalles con los tickers correspondientes
        insertarDetalles(db, "AAPL",
                "Apple es una compañía tecnológica líder reconocida por su innovación en electrónica de consumo, software y servicios digitales.",
                150.0, "MSFT, GOOGL");
        insertarDetalles(db, "MSFT",
                "Microsoft es una empresa tecnológica multinacional que desarrolla, licencia y soporta una amplia gama de productos de software, servicios y dispositivos.",
                280.0, "AAPL, INTC");
        insertarDetalles(db, "GOOGL",
                "Alphabet es la empresa matriz de Google, especializada en servicios de internet como búsqueda, publicidad y computación en la nube.",
                2700.0, "AAPL, MSFT");
        insertarDetalles(db, "YUM",
                "Yum! Brands incluye KFC, una cadena de comida rápida reconocida mundialmente por su pollo frito.",
                25.0, "KO");
        insertarDetalles(db, "BLK",
                "BlackRock es una firma global de gestión de inversiones reconocida por su experiencia en administración de activos.",
                600.0, "SPY");
        insertarDetalles(db, "AMD",
                "AMD es una compañía multinacional de semiconductores conocida por sus procesadores y soluciones gráficas.",
                100.0, "INTC, NVDA");
        insertarDetalles(db, "SPY",
                "SPY es un ETF que sigue el índice S&P 500, representando las principales empresas de EE.UU.",
                4500.0, "AAPL, MSFT");
        insertarDetalles(db, "NVO",
                "Novo Nordisk es una compañía global de salud especializada en el cuidado de la diabetes.",
                90.0, "AAPL");
        insertarDetalles(db, "UBER",
                "Uber es una empresa tecnológica que ha revolucionado el transporte urbano.",
                35.0, "TSLA, GOOGL");
        insertarDetalles(db, "TSLA",
                "Tesla es una compañía innovadora en el sector automotriz y energético.",
                700.0, "UBER, GOOGL");
        insertarDetalles(db, "U",
                "Unity Software es una plataforma líder en contenido 3D interactivo en tiempo real.",
                120.0, "MSFT, NVDA");
        insertarDetalles(db, "INTC",
                "Intel es una empresa reconocida en el sector de semiconductores.",
                55.0, "AMD, NVDA");
        insertarDetalles(db, "NVDA",
                "NVIDIA se especializa en soluciones gráficas y de inteligencia artificial.",
                500.0, "AMD, INTC");
        insertarDetalles(db, "KO",
                "Coca-Cola es un líder global en bebidas refrescantes.",
                60.0, "YUM");
        insertarDetalles(db, "IBD",
                "Iberdrola es una multinacional destacada en energías renovables.",
                10.0, "KO");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Stocks");
        db.execSQL("DROP TABLE IF EXISTS Detalles");
        onCreate(db);
    }

    public void actualizar(SQLiteDatabase db, String nombre2, Boolean favoritos2) {
        ContentValues valores = new ContentValues();
        valores.put("favoritos", favoritos2 ? 1 : 0);
        String select = "nombre = ?";
        String[] sargs = {nombre2};
        db.update("Stocks", valores, select, sargs);
    }

    public void insertar(SQLiteDatabase db, String nombre, Boolean favoritos) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("favoritos", favoritos ? 1 : 0);
        db.insert("Stocks", null, valores);
    }

    public void insertarDetalles(SQLiteDatabase db, String nombre, String descripcion, double precio, String similares) {
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("descripcion", descripcion);
        cv.put("Precio", precio);
        cv.put("Similares", similares);
        db.insert("Detalles", null, cv);
    }

    public void actualizarDetalle(SQLiteDatabase db, String nombre, double nuevoPrecio) {
        ContentValues cv = new ContentValues();
        cv.put("Precio", nuevoPrecio);
        String where = "nombre = ?";
        String[] args = {nombre};
        db.update("Detalles", cv, where, args);
    }

    public Cursor obtenerNombresYPrecios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Stocks.nombre, Detalles.Precio FROM Stocks LEFT JOIN Detalles ON Stocks.nombre = Detalles.nombre", null);
    }

    public Cursor obtenerDescripcion() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Detalles", null);
    }

    public boolean esFavorito(String nombre) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "Stocks",
                new String[]{"favoritos"},
                "nombre = ?",
                new String[]{nombre},
                null, null, null
        );
        boolean esFavorito = false;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("favoritos");
            if (columnIndex != -1) {
                esFavorito = cursor.getInt(columnIndex) == 1;
            }
        }
        cursor.close();
        db.close();
        return esFavorito;
    }

    public Cursor obtenerDetallesCompletos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Detalles", null);
    }

    public Cursor obtenerPrecio(String nombreAccion) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Precio FROM Detalles WHERE nombre = ?", new String[]{nombreAccion});
    }

    public void actualizarDesdeAlpaca() {
        SQLiteDatabase db = this.getWritableDatabase();
        OkHttpClient client = new OkHttpClient();

        Cursor cursor = db.rawQuery("SELECT nombre FROM Stocks", null);
        if (cursor.moveToFirst()) {
            do {
                String ticker = cursor.getString(cursor.getColumnIndex("nombre"));
                String url = ALPACA_BASE_URL + ticker + "/trades/latest";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("APCA-API-KEY-ID", ALPACA_API_KEY)
                        .addHeader("APCA-API-SECRET-KEY", ALPACA_SECRET_KEY)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("Alpaca", "Respuesta para " + ticker + ": " + responseBody);
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject trade = json.getJSONObject("trade");
                        double nuevoPrecio = trade.getDouble("p");
                        actualizarDetalle(db, ticker, nuevoPrecio);
                        Log.d("Alpaca", "Actualizado " + ticker + " con precio: " + nuevoPrecio);
                    } else {
                        Log.e("Alpaca", "Error en la respuesta para el ticker " + ticker + ": " + response.code() + " - " + response.message());
                        if (response.body() != null) {
                            Log.e("Alpaca", "Cuerpo de la respuesta: " + response.body().string());
                        }
                    }
                } catch (Exception e) {
                    Log.e("Alpaca", "Excepción al obtener datos para " + ticker + ": " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public void actualizarDesdeAlpaca(String ticker) {
        SQLiteDatabase db = this.getWritableDatabase();
        OkHttpClient client = new OkHttpClient();

        String url = ALPACA_BASE_URL + ticker + "/trades/latest";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", ALPACA_API_KEY)
                .addHeader("APCA-API-SECRET-KEY", ALPACA_SECRET_KEY)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Log.d("Alpaca", "Respuesta para " + ticker + ": " + responseBody);
                JSONObject json = new JSONObject(responseBody);
                JSONObject trade = json.getJSONObject("trade");
                double nuevoPrecio = trade.getDouble("p");
                actualizarDetalle(db, ticker, nuevoPrecio);
                Log.d("Alpaca", "Actualizado " + ticker + " con precio: " + nuevoPrecio);
            } else {
                Log.e("Alpaca", "Error en la respuesta para el ticker " + ticker + ": " + response.code() + " - " + response.message());
                if (response.body() != null) {
                    Log.e("Alpaca", "Cuerpo de la respuesta: " + response.body().string());
                }
            }
        } catch (Exception e) {
            Log.e("Alpaca", "Excepción al obtener datos para " + ticker + ": " + e.getMessage());
        } finally {
            db.close();
        }
    }
}