package com.flywolf.familytree;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

public class DbWorker {
    ArrayList<Relative> relative;

    final String LOG_TAG = "myLogs";
    DBInit dbInit;

    DbWorker(Context cnt) {
        dbInit = DBInit.getDBHelper(cnt);// new DBHelper(cnt);

    }

    public Relative getRelative(int id) {
        Relative r = null;
        Iterator<Relative> it = relative.iterator();
        while (it.hasNext()) {
            r = it.next();
            if (r.id == id)
                return r;
        }
        return r;
    }

    private long deleteAll() {
        // подключаемся к БД
        SQLiteDatabase db = dbInit.getWritableDatabase();
        long rowID = db.delete("family_tree", null, null);
        Log.d(LOG_TAG, "row deleted, ID = " + rowID);
        // закрываем подключение к БД
        dbInit.close();
        return rowID;
    }

    public long saveRelative(Relative cs) {

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbInit.getWritableDatabase();

        cv.put("name", cs.name);
        cv.put("women", cs.women ? "true" : "false");
    //    cv.put("photo_url", cs.photoUrl);
        cv.put("img_b", cs.imgB);
        if (cs.birthday != null) cv.put("birthday", DBInit.getDate(cs.birthday));
        cv.put("description", cs.description);
        cv.put("go_out", cs.goOut ? "true" : "false");
        long rowID = db.update("relative", cv, "id='" + cs.getId() + "'", null);
        Log.d(LOG_TAG, "row updated, ID = " + rowID);
        dbInit.close();
        return rowID;
    }
    public static byte[] getBytes(Bitmap bitmap)
    {
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80, stream);
        return stream.toByteArray();
    }


    public static Bitmap scaleBitmap(Bitmap mBitmap) {
        int ScaleSize = FamilyTree.IMAGE_MAX_SIZE;//max Height or width to Scale
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float excessSizeRatio = width > height ? width / ScaleSize : height / ScaleSize;
        Bitmap bitmap = Bitmap.createBitmap(
                mBitmap, 0, 0,(int) (width/excessSizeRatio),(int) (height/excessSizeRatio));
        //mBitmap.recycle();
        return bitmap;
    }

    public static Bitmap getImage(byte[] image)
    {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void readFromDb() {
        // ContentValues cv = new ContentValues();

        SQLiteDatabase db = dbInit.getWritableDatabase();
        Log.d(LOG_TAG, "--- Rows in mytable: ---");
        Cursor c = db.query("relative", null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("id");
            int treeIdColIndex = c.getColumnIndex("tree_id");
            int leafIdColIndex = c.getColumnIndex("leaf_id");
            int womenColIndex = c.getColumnIndex("women");
            int nameColIndex = c.getColumnIndex("name");
            int birthdayColIndex = c.getColumnIndex("birthday");
          //  int photoUrlColIndex = c.getColumnIndex("photo_url");
            int descriptionColIndex = c.getColumnIndex("description");
            int goOutColIndex = c.getColumnIndex("go_out");
            int imgBColIndex = c.getColumnIndex("img_b");

            relative = new ArrayList<Relative>();
            do {
                Relative rl = new Relative();
                rl.id = c.getInt(idColIndex);
                rl.treeId = c.getInt(treeIdColIndex);
                rl.leafId = c.getInt(leafIdColIndex);
                rl.leafId = c.getInt(leafIdColIndex);
                rl.women = Boolean.parseBoolean(c.getString(womenColIndex));
                rl.name = c.getString(nameColIndex);
                //rl.photoUrl = c.getString(photoUrlColIndex);
                rl.description = c.getString(descriptionColIndex);
                rl.goOut = Boolean.parseBoolean(c.getString(goOutColIndex));
                rl.imgB = c.getBlob(imgBColIndex);
                try {
                    rl.birthday = c.getString(birthdayColIndex) == null ? null
                            : DBInit.toDate(c.getString(birthdayColIndex));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    Log.d(LOG_TAG, e.toString());
                    e.printStackTrace();
                }
                relative.add(rl);
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        dbInit.close();
    }
    public class Relative {
        int id;
        int treeId;
        int leafId;
        boolean women;
        boolean goOut;
        Date birthday;
        String name;
        String description;
        byte[] imgB;

        public byte[] getImgB() {
            return imgB;
        }

        public void setImgB(byte[] imgB) {
            this.imgB = imgB;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTreeId() {
            return treeId;
        }

        public void setTreeId(int treeId) {
            this.treeId = treeId;
        }

        public int getLeafId() {
            return leafId;
        }

        public void setLeafId(int leafId) {
            this.leafId = leafId;
        }

        public boolean isWomen() {
            return women;
        }

        public void setWomen(boolean women) {
            this.women = women;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isGoOut() {
            return goOut;
        }

        public void setGoOut(boolean goOut) {
            this.goOut = goOut;
        }

    }
}
