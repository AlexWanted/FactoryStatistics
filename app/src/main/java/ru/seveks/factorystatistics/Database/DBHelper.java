package ru.seveks.factorystatistics.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "factory_statistics_database";

    public static final String
            TABLE_FACTORIES = "factories",
            ID = "_id",
            NAME = "name",

            TABLE_PRODUCTIVITY = "productivity",
            FACTORY_ID = "factory_id",
            DATE = "date",
            PRODUCED_TOTAL = "produced_total",
            PRODUCED_FIRST_SHIFT = "produced_first_shift",
            PRODUCED_SECOND_SHIFT = "produced_second_shift",

            TABLE_RECIPES = "recipes";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String factoriesTableCreation = "CREATE TABLE "+TABLE_FACTORIES+" ( "+NAME+" text );";

        String productivityTableCreation = "CREATE TABLE "+TABLE_PRODUCTIVITY+" ( "+
                FACTORY_ID+" integer, " +
                DATE+" text, " +
                PRODUCED_TOTAL+" float, " +
                PRODUCED_FIRST_SHIFT+" float, " +
                PRODUCED_SECOND_SHIFT+" float," +
                "FOREIGN KEY ("+FACTORY_ID+") REFERENCES "+TABLE_FACTORIES+"("+ID+"))";

        db.execSQL(factoriesTableCreation);
        db.execSQL(productivityTableCreation);
    }



    public void insertProductivityRow(int factoryId, String date,
                                      float producedTotal, float producedFirstShift, float producedSecondShift){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FACTORY_ID, factoryId);
        cv.put(DATE, date);
        cv.put(PRODUCED_TOTAL, producedTotal);
        cv.put(PRODUCED_FIRST_SHIFT, producedFirstShift);
        cv.put(PRODUCED_SECOND_SHIFT, producedSecondShift);
        db.insert(TABLE_PRODUCTIVITY, null, cv);
        cv.clear();
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
