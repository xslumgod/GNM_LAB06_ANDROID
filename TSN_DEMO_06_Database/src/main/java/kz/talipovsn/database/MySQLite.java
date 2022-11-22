package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "software"; // Имя базы данных

    static final String TABLE_NAME = "softwa"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием организации
    static final String Klass = "klass";
    static final String ADDRESS = "address";
    static final String WEBSITE = "website";
    static final String VES = "ves";

    static final String ASSETS_FILE_NAME = "software.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + Klass + " TEXT,"
                + ADDRESS + " TEXT,"
                + WEBSITE + " TEXT,"
                + VES + " INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String klass1, String address, String website, String ves) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(Klass, klass1);
        values.put(ADDRESS, address);
        values.put(WEBSITE, website);
        values.put(VES, ves);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String klass1 = st.nextToken().trim();
                    String address = st.nextToken().trim();
                    String website = st.nextToken().trim();
                    String ves = st.nextToken().trim();
                        // Извлекаем из строки номер организации без пробелов на концах
                    addData(db, name, klass1, address, website,ves); // Добавляем название и телефон в базу данных
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter, Spinner spinner) {

        String selectQuery; // Переменная для SQL-запроса
        int k = (int)spinner.getSelectedItemId();
        if (filter.contains("'")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " LIMIT 0";
        } else
        switch (k) {
            case 0:
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME + " LIKE '%" +
                        filter.toLowerCase() + "%'"
                        + " OR " + Klass + " LIKE '%" + filter + "%'"
                        + " OR " + ADDRESS + " LIKE '%" + filter + "%'"
                        + " OR " + WEBSITE + " LIKE '%" + filter + "%'"
                        + " OR " + VES + " LIKE '%" + filter + "%'"
                        + ")";
                break;
            case 1:
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME + " LIKE '%" +
                        filter + "%'" + ")";
                break;
            case 2:
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("  + Klass + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
                break;
            case 3:
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" +  ADDRESS + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
                break;
            case 4:
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + WEBSITE + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
                break;
            case 5:
                if (filter.isEmpty() | !filter.matches("[-+]?\\d+")) {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " LIMIT 0";
                } else {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + VES + " >= " + Integer.parseInt(filter);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + k);
        }

        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int n = cursor.getColumnIndex(NAME);
                int t = cursor.getColumnIndex(Klass);
                int a = cursor.getColumnIndex(ADDRESS);
                int w = cursor.getColumnIndex(WEBSITE);
                int v = cursor.getColumnIndex(VES);
                String name = cursor.getString(n); // Чтение названия организации
                String klass1 = cursor.getString(t); // Чтение телефонного номера
                String address = cursor.getString(a);
                String website = cursor.getString(w);
                String ves = cursor.getString(v);
                data.append(String.valueOf(++num) + ") Название: " + name + "\n Класс: " + klass1 + "\n Обитает: " + address + "\n Сайт: " + website + "\n Вес: " + ves + "кг" + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}