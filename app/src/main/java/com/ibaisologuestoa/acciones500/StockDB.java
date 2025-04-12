package com.ibaisologuestoa.acciones500;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

public class StockDB extends SQLiteOpenHelper {

    private static final String DB_NOMBRE = "stock.db";
    private static final int DB_VERSION = 1;

    private static final String TABLA_STOCKS = "Stocks";
    private static final String COL_ID_STOCKS = "id";
    private static final String COL_NOMBRE_STOCKS = "nombre";
    private static final String COL_FAVORITOS_STOCKS = "favoritos";

    private static final String TABLA_DETALLES = "Detalles";
    private static final String COL_ID_DETALLES = "id";
    private static final String COL_NOMBRE_DETALLES = "nombre";
    private static final String COL_DESCRIPCION_DETALLES = "descripcion";
    private static final String COL_PRECIO_DETALLES = "Precio";
    private static final String COL_NOTAS_DETALLES = "Notas";
    private static final String COL_SIMILARES_DETALLES = "Similares";

    private static final String SQL_CREAR_STOCKS =
            "CREATE TABLE " + TABLA_STOCKS + " (" +
                    COL_ID_STOCKS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOMBRE_STOCKS + " TEXT UNIQUE, " +
                    COL_FAVORITOS_STOCKS + " BOOLEAN" +
                    ")";

    private static final String SQL_CREAR_DETALLES =
            "CREATE TABLE " + TABLA_DETALLES + " (" +
                    COL_ID_DETALLES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOMBRE_DETALLES + " TEXT UNIQUE, " +
                    COL_DESCRIPCION_DETALLES + " TEXT, " +
                    COL_PRECIO_DETALLES + " DOUBLE, " +
                    COL_NOTAS_DETALLES + " TEXT, " +
                    COL_SIMILARES_DETALLES + " TEXT, " +
                    "FOREIGN KEY(" + COL_NOMBRE_DETALLES + ") REFERENCES " + TABLA_STOCKS + "(" + COL_NOMBRE_STOCKS + ")" +
                    ")";

    private static final List<String> CANDIDATOS_SIMBOLOS = Collections.unmodifiableList(Arrays.asList(
            "AAPL", "MSFT", "GOOGL", "GOOG", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "LLY",
            "V", "JPM", "XOM", "WMT", "JNJ", "UNH", "MA", "PG", "ORCL", "HD",
            "CVX", "MRK", "KO", "PEP", "AVGO", "COST", "ADBE", "BAC", "CRM", "MCD",
            "CSCO", "TMO", "ACN", "ABNB", "PFE", "LIN", "NFLX", "AMD", "DIS", "WFC",
            "TXN", "NEE", "CMCSA", "PM", "VZ", "UPS", "RTX", "HON", "MS", "BLK",
            "INTC", "IBM", "SBUX", "GS", "CAT", "GE", "ISRG", "UBER", "PYPL", "BMY",
            "NOW", "BA", "DE", "ELV", "SPGI", "T", "BKNG", "AMAT", "ADP", "GILD",
            "SYK", "MDLZ", "TJX", "CVS", "CI", "AMT", "TMUS", "AXP", "MMC", "COP",
            "ADI", "ETN", "SCHW", "REGN", "PNC", "LMT", "SO", "BSX", "FISV", "CB",
            "DUK", "MO", "CL", "EOG", "HUM", "FDX", "AON", "NKE", "PLD", "MU",
            "IBE.MC",
            "XIACY"
    ));

    public StockDB(Context context) {
        super(context, DB_NOMBRE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("StockDB", "Creando tablas...");
        db.execSQL(SQL_CREAR_STOCKS);
        db.execSQL(SQL_CREAR_DETALLES);
        Log.d("StockDB", "Tablas creadas.");

        insertar(db, "Apple", Boolean.FALSE);
        insertar(db, "Microsoft", Boolean.FALSE);
        insertar(db, "Alphabet", Boolean.FALSE);
        insertar(db, "KFC", Boolean.FALSE);
        insertar(db, "Blackrock", Boolean.FALSE);
        insertar(db, "AMD", Boolean.FALSE);
        insertar(db, "SP500", Boolean.FALSE);
        insertar(db, "Novo Nordisk", Boolean.FALSE);
        insertar(db, "Xiaomi", Boolean.FALSE);
        insertar(db, "Uber", Boolean.FALSE);
        insertar(db, "Tesla", Boolean.FALSE);
        insertar(db, "Unity Software", Boolean.FALSE);
        insertar(db, "Intel", Boolean.FALSE);
        insertar(db, "NVIDIA", Boolean.FALSE);
        insertar(db, "Coca-Cola", Boolean.FALSE);
        insertar(db, "Iberdrola", Boolean.FALSE);

        insertar(db, "Apple",
                "Apple es una compañía tecnológica líder reconocida por su innovación en electrónica de consumo, software y servicios digitales.",
                150.0,
                "Microsoft, Alphabet");

        insertar(db, "Microsoft",
                "Microsoft es una empresa tecnológica multinacional que desarrolla, licencia y soporta una amplia gama de productos de software, servicios y dispositivos.",
                280.0,
                "Apple, Intel");

        insertar(db, "Alphabet",
                "Alphabet es la empresa matriz de Google, especializada en servicios de internet como búsqueda, publicidad y computación en la nube.",
                2700.0,
                "Apple, Microsoft");

        insertar(db, "KFC",
                "KFC es una cadena de comida rápida reconocida mundialmente por su delicioso pollo frito y acompañamientos sabrosos.",
                25.0,
                "Coca-Cola");

        insertar(db, "Blackrock",
                "Blackrock es una firma global de gestión de inversiones reconocida por su experiencia en administración de activos y análisis financiero.",
                600.0,
                "SP500");

        insertar(db, "AMD",
                "AMD es una compañía multinacional de semiconductores conocida por sus procesadores innovadores y soluciones gráficas avanzadas.",
                100.0,
                "Intel, NVIDIA");

        insertar(db, "SP500",
                "SP500 es un índice bursátil que agrupa 500 de las empresas más grandes y representativas de la economía estadounidense.",
                4500.0,
                "Apple, Microsoft");

        insertar(db, "Novo Nordisk",
                "Novo Nordisk es una compañía global de salud especializada en el cuidado de la diabetes y tratamientos innovadores para condiciones crónicas.",
                90.0,
                "Apple");

        insertar(db, "Xiaomi",
                "Xiaomi es una empresa china de electrónica reconocida por sus smartphones asequibles y dispositivos inteligentes innovadores.",
                45.0,
                "Apple, Microsoft");

        insertar(db, "Uber",
                "Uber es una empresa tecnológica que ha revolucionado el transporte urbano a través de su plataforma de ride-sharing y servicios logísticos.",
                35.0,
                "Tesla, Alphabet");

        insertar(db, "Tesla",
                "Tesla es una compañía innovadora en el sector automotriz y energético, pionera en vehículos eléctricos y soluciones sostenibles.",
                700.0,
                "Uber, Alphabet");

        insertar(db, "Unity Software",
                "Unity Software es una plataforma líder en la creación y operación de contenido 3D interactivo en tiempo real, ampliamente utilizada en el desarrollo de videojuegos y simulaciones.",
                120.0,
                "Microsoft, NVIDIA");

        insertar(db, "Intel",
                "Intel es una empresa mundialmente reconocida en el sector de semiconductores, famosa por sus microprocesadores y avances tecnológicos.",
                55.0,
                "AMD, NVIDIA");

        insertar(db, "NVIDIA",
                "NVIDIA se especializa en soluciones gráficas y de inteligencia artificial, impulsando la innovación en computación visual y aprendizaje profundo.",
                500.0,
                "AMD, Intel");

        insertar(db, "Coca-Cola",
                "Coca-Cola es un líder global en bebidas refrescantes, famosa por sus icónicas gaseosas y una amplia variedad de productos en el mercado.",
                60.0,
                "KFC");

        insertar(db, "Iberdrola",
                "Iberdrola es una multinacional destacada en el sector de energías renovables, comprometida con la generación y distribución sostenible de energía.",
                10.0,
                "Coca-Cola");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("StockDB", "Actualizando base de datos de versión " + oldVersion + " a " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_DETALLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_STOCKS);
        onCreate(db);
    }

    public void cargarAccionesIniciales(int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> simbolosExistentes = obtenerTodosLosSimbolos(db);
        List<String> candidatos = new ArrayList<>(CANDIDATOS_SIMBOLOS);
        candidatos.removeAll(simbolosExistentes);
        Collections.shuffle(candidatos);

        List<String> simbolosACargar = new ArrayList<>();
        for (int i = 0; i < Math.min(cantidad, candidatos.size()); i++) {
            simbolosACargar.add(candidatos.get(i));
        }

        if (simbolosACargar.isEmpty()) {
            Log.d("StockDB", "No hay nuevos símbolos candidatos para la carga inicial.");
            db.close();
            return;
        }

        Log.d("StockDB", "Cargando datos para símbolos: " + simbolosACargar);
        try {
            Map<String, Stock> stocks = YahooFinance.get(simbolosACargar.toArray(new String[0]));

            db.beginTransaction();
            try {
                for (String simbolo : simbolosACargar) {
                    Stock stock = stocks.get(simbolo);
                    if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                        insertarStockCompleto(db, stock);
                    } else {
                        Log.w("StockDB", "No se pudo obtener datos para el símbolo: " + simbolo);
                    }
                }
                db.setTransactionSuccessful();
                Log.d("StockDB", "Acciones iniciales cargadas correctamente.");
            } finally {
                db.endTransaction();
            }

        } catch (IOException e) {
            Log.e("StockDB", "Error al obtener datos de Yahoo Finance para carga inicial", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void cargarMasAcciones(int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> simbolosExistentes = obtenerTodosLosSimbolos(db);
        List<String> candidatos = new ArrayList<>(CANDIDATOS_SIMBOLOS);
        candidatos.removeAll(simbolosExistentes);

        if (candidatos.isEmpty()) {
            Log.d("StockDB", "No quedan más símbolos candidatos para cargar.");
            db.close();
            return;
        }

        Collections.shuffle(candidatos);
        List<String> simbolosACargar = new ArrayList<>();
        for (int i = 0; i < Math.min(cantidad, candidatos.size()); i++) {
            simbolosACargar.add(candidatos.get(i));
        }

        Log.d("StockDB", "Cargando " + simbolosACargar.size() + " acciones adicionales: " + simbolosACargar);

        try {
            Map<String, Stock> stocks = YahooFinance.get(simbolosACargar.toArray(new String[0]));

            db.beginTransaction();
            try {
                for (String simbolo : simbolosACargar) {
                    Stock stock = stocks.get(simbolo);
                    if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                        insertarStockCompleto(db, stock);
                    } else {
                        Log.w("StockDB", "No se pudo obtener datos para el símbolo adicional: " + simbolo);
                    }
                }
                db.setTransactionSuccessful();
                Log.d("StockDB", "Acciones adicionales cargadas.");
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            Log.e("StockDB", "Error al obtener datos de Yahoo Finance para cargar más acciones", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void actualizarPreciosYDescripciones() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> simbolos = obtenerTodosLosSimbolos(db);

        if (simbolos.isEmpty()) {
            Log.d("StockDB", "No hay símbolos para actualizar.");
            db.close();
            return;
        }

        Log.d("StockDB", "Actualizando precios y descripciones para " + simbolos.size() + " símbolos");

        try {
            Map<String, Stock> stocks = YahooFinance.get(simbolos.toArray(new String[0]));

            db.beginTransaction();
            try {
                for (String simbolo : simbolos) {
                    Stock stock = stocks.get(simbolo);
                    if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                        actualizarDatosStock(db, stock);
                    } else {
                        Log.w("StockDB", "No se pudo obtener datos actualizados para: " + simbolo);
                    }
                }
                db.setTransactionSuccessful();
                Log.d("StockDB", "Precios y descripciones actualizados correctamente.");
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            Log.e("StockDB", "Error al obtener datos actualizados de Yahoo Finance", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void actualizarDatosStock(SQLiteDatabase db, Stock stock) {
        String simbolo = stock.getSymbol();
        StockQuote quote = stock.getQuote();
        BigDecimal precio = quote.getPrice();
        String nombreCompleto = stock.getName();

        ContentValues valores = new ContentValues();
        valores.put(COL_PRECIO_DETALLES, precio != null ? precio.doubleValue() : 0.0);

        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            valores.put(COL_DESCRIPCION_DETALLES, nombreCompleto);
        }

        int filasActualizadas = db.update(
                TABLA_DETALLES,
                valores,
                COL_NOMBRE_DETALLES + " = ?",
                new String[]{simbolo}
        );

        Log.d("StockDB", "Filas actualizadas para " + simbolo + ": " + filasActualizadas);
    }

    private void insertarStockCompleto(SQLiteDatabase db, Stock stock) {
        String simbolo = stock.getSymbol();
        StockQuote quote = stock.getQuote();
        BigDecimal precio = quote.getPrice();
        String nombreCompleto = stock.getName();

        ContentValues valoresStocks = new ContentValues();
        valoresStocks.put(COL_NOMBRE_STOCKS, simbolo);
        valoresStocks.put(COL_FAVORITOS_STOCKS, Boolean.FALSE);
        db.insertWithOnConflict(TABLA_STOCKS, null, valoresStocks, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues valoresDetalles = new ContentValues();
        valoresDetalles.put(COL_NOMBRE_DETALLES, simbolo);
        valoresDetalles.put(COL_DESCRIPCION_DETALLES, nombreCompleto != null ? nombreCompleto : simbolo);
        valoresDetalles.put(COL_PRECIO_DETALLES, precio != null ? precio.doubleValue() : 0.0);
        valoresDetalles.put(COL_NOTAS_DETALLES, "");
        valoresDetalles.put(COL_SIMILARES_DETALLES, "");
        db.insertWithOnConflict(TABLA_DETALLES, null, valoresDetalles, SQLiteDatabase.CONFLICT_IGNORE);

        Log.v("StockDB", "Insertado/Ignorado: " + simbolo);
    }

    private void insertarSimboloSimple(SQLiteDatabase db, String simbolo, Boolean esFavorito) {
        ContentValues valores = new ContentValues();
        valores.put(COL_NOMBRE_STOCKS, simbolo);
        valores.put(COL_FAVORITOS_STOCKS, esFavorito);
        db.insertWithOnConflict(TABLA_STOCKS, null, valores, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void insertar(String nombre2, Boolean favoritos2) {
        SQLiteDatabase db = this.getWritableDatabase();
        insertarSimboloSimple(db, nombre2, favoritos2);
        db.close();
    }

    public void insertar(SQLiteDatabase db, String nombre2, Boolean favoritos2) {
        ContentValues valores = new ContentValues();
        valores.put(COL_NOMBRE_STOCKS, nombre2);
        valores.put(COL_FAVORITOS_STOCKS, favoritos2);
        db.insert(TABLA_STOCKS, null, valores);
    }

    public void insertar(SQLiteDatabase db, String nombre, String descripcion, double precio, String similares) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE_DETALLES, nombre);
        cv.put(COL_DESCRIPCION_DETALLES, descripcion);
        cv.put(COL_PRECIO_DETALLES, precio);
        cv.put(COL_SIMILARES_DETALLES, similares);
        cv.put(COL_NOTAS_DETALLES, "");
        db.insert(TABLA_DETALLES, null, cv);
    }

    public void actualizar(String nombre2, Boolean favoritos2) {
        SQLiteDatabase db = this.getWritableDatabase();
        actualizar(db, nombre2, favoritos2);
        db.close();
    }

    public void actualizar(SQLiteDatabase db, String nombre2, Boolean favoritos2) {
        ContentValues valores = new ContentValues();
        valores.put(COL_FAVORITOS_STOCKS, favoritos2);

        String select = COL_NOMBRE_STOCKS + " = ?";
        String[] sargs = { nombre2 };

        db.update(TABLA_STOCKS, valores, select, sargs);
    }

    public void actualizarFavorito(String simbolo, boolean esFavorito) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_FAVORITOS_STOCKS, esFavorito);

        String select = COL_NOMBRE_STOCKS + " = ?";
        String[] sargs = { simbolo };

        int count = db.update(TABLA_STOCKS, valores, select, sargs);
        Log.d("StockDB", "Filas actualizadas en Stocks para favorito: " + count + " para símbolo: " + simbolo);
        db.close();
    }

    public Cursor obtenerNombres() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_STOCKS, null);
    }

    public Cursor obtenerStocks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLA_STOCKS,
                new String[]{COL_ID_STOCKS, COL_NOMBRE_STOCKS, COL_FAVORITOS_STOCKS},
                null, null, null, null, COL_NOMBRE_STOCKS + " ASC");
    }

    public Cursor obtenerDescripcion() {
        Log.d("This en StockDB", String.valueOf(this.getReadableDatabase()));
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_DETALLES, null);
    }

    public Cursor obtenerDetallesCompletos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLA_DETALLES,
                new String[]{COL_ID_DETALLES, COL_NOMBRE_DETALLES, COL_DESCRIPCION_DETALLES, COL_PRECIO_DETALLES, COL_NOTAS_DETALLES, COL_SIMILARES_DETALLES},
                null, null, null, null, COL_NOMBRE_DETALLES + " ASC");
    }

    public Cursor obtenerDetallesPorSimbolo(String simbolo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLA_DETALLES,
                new String[]{COL_ID_DETALLES, COL_NOMBRE_DETALLES, COL_DESCRIPCION_DETALLES, COL_PRECIO_DETALLES, COL_NOTAS_DETALLES, COL_SIMILARES_DETALLES},
                COL_NOMBRE_DETALLES + " = ?",
                new String[]{simbolo},
                null, null, null);
    }

    public boolean esFavorito(String simbolo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_STOCKS,
                new String[]{COL_FAVORITOS_STOCKS},
                COL_NOMBRE_STOCKS + " = ?",
                new String[]{simbolo},
                null, null, null
        );

        boolean esFavorito = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COL_FAVORITOS_STOCKS);
                if (columnIndex != -1) {
                    esFavorito = cursor.getInt(columnIndex) == 1;
                }
            }
            cursor.close();
        }
        db.close();
        return esFavorito;
    }

    private List<String> obtenerTodosLosSimbolos(SQLiteDatabase db) {
        List<String> simbolos = new ArrayList<>();
        Cursor cursor = db.query(TABLA_STOCKS, new String[]{COL_NOMBRE_STOCKS}, null, null, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(COL_NOMBRE_STOCKS);
            if (columnIndex != -1) {
                while (cursor.moveToNext()) {
                    simbolos.add(cursor.getString(columnIndex));
                }
            }
            cursor.close();
        }
        return simbolos;
    }
}