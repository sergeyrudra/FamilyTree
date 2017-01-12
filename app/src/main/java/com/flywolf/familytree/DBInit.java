package com.flywolf.familytree;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBInit extends SQLiteOpenHelper {
    final String LOG_TAG = "myLogs";
    private static DBInit dbInit;

    // singletone
    public static DBInit getDBHelper(Context context) {
        if (dbInit == null) {
            //set db version number
            dbInit = new DBInit(context, 1);
        }
        return dbInit;
    }

    public DBInit(Context context, int dbVersion) {
        // create constractor
        super(context, "family_tree2", null, dbVersion);
        //context.deleteDatabase("family_tree");

    }

    public static String getDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static Date toDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(date);
    }

    public static String addMonth(Date date, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, month); // minus number would decrement the days
        return getDate(cal.getTime());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        // create tables
        db.execSQL("create table relative ("
                + "id integer primary key autoincrement," + "tree_id integer,"
                + "leaf_id integer," + "women boolean default false,"
                + "name text, birthday text, death text, relation integer, photo_url string, img_b BLOB,"
                + "go_out boolean default false,"
                + "latitide DOUBLE," + "longitude DOUBLE,"
                + "description text);");

        for (int i = 1; i < 128; i++) {
            db.execSQL("INSERT INTO relative(tree_id, leaf_id) " + "VALUES(1," + i + ")");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, " --- onUpgrade database from " + oldVersion + " to "
                + newVersion + " version --- ");

       /* if (oldVersion == 1) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE relative ADD COLUMN go_out boolean default false;");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 3) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE relative ADD COLUMN death text;");
                db.execSQL("ALTER TABLE relative ADD COLUMN relation integer;");
                for (int i = 16; i < 128; i++) {
                    db.execSQL("INSERT INTO relative(tree_id, leaf_id) " + "VALUES(1," + i + ")");
                }

            } finally {
                db.endTransaction();
            }

        }*/
    }


}
