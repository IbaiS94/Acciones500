package com.example.app;

import static java.sql.Types.NULL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StockDB extends SQLiteOpenHelper {
        private static final String DB = "stock.db";
        private static final int DB_VERSION = 1;
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
                "CREATE TABLE Detalles ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT, " +
                        "descripcion TEXT, "+
                        "Precio DOUBLE, "+
                        "Notas TEXT, "+
                        "Similares TEXT)";



        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL);
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

            db.execSQL(detalles);
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
            db.execSQL("DROP TABLE IF EXISTS " + "Stocks");
            onCreate(db);
        }
        public void actualizar(SQLiteDatabase db, String nombre2, Boolean favoritos2) {
            ContentValues valores = new ContentValues();
            valores.put("favoritos", favoritos2);

            String select = "nombre = ?";
            String[] sargs = { nombre2 };

            db.update("Stocks", valores, select, sargs);
        }


    public void insertar(SQLiteDatabase db, String nombre2, Boolean favoritos2) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre2);
            valores.put("favoritos", favoritos2);
            db.insert("Stocks", null, valores);
        }

        public void insertar(SQLiteDatabase db, String nombre, String descripcion, double precio, String similares) {
            ContentValues cv = new ContentValues();
            cv.put("nombre", nombre);
            cv.put("descripcion", descripcion);
            cv.put("Precio", precio);
            cv.put("Similares", similares);
            db.insert("Detalles", null, cv);
        }

        public Cursor obtenerNombres() {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM " + "Stocks", null);
        }
        public Cursor obtenerDescripcion() {
            Log.d("This en StockDB", String.valueOf(this.getReadableDatabase()));
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM " + "Detalles", null);
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
                esFavorito = cursor.getInt(columnIndex) == 1; // 1 = true, 0 = false
            }
        }
        cursor.close();
        db.close();
        return esFavorito;
    }
    }

