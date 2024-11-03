package com.example.bmical;

import static android.provider.BaseColumns._ID;
import static com.example.bmical.Constants.COLOR;
import static com.example.bmical.Constants.HEIGHT;
import static com.example.bmical.Constants.TABLE_NAME;
import static com.example.bmical.Constants.DATE;
import static com.example.bmical.Constants.WEIGHT;
import static com.example.bmical.Constants.BMI;
import static com.example.bmical.Constants.CLASSIFICATION;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventsData extends SQLiteOpenHelper {
    public EventsData(Context ctx){
        super(ctx, "events.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE + " INTEGER, "
                + WEIGHT + " TEXT NOT NULL, "
                + HEIGHT + " TEXT NOT NULL, "
                + BMI + " TEXT NOT NULL, "
                + CLASSIFICATION + " TEXT NOT NULL, "
                + COLOR + " INTEGER NOT NULL);"
                );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS events");
        onCreate(db);
    }
}