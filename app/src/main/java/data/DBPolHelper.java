package data;

/**
 * Created by 3yanlis1bos on 3/15/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class DBPolHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "PolDB";
    private static final String TABLE_PLATES = "PLATES";
    private static final String FTS_VIRTUAL_TABLE = "PlateInfo";
    private static final String KEY_ID = "id";


    public static final String KEY_ROWID = "rowid";
    public static final String KEY_PLATE = "plate";
    public static final String KEY_RECORD = "record";
    public static final String KEY_SEARCH = "searchData";

    private static final String TAG = "CustomersDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    //Create a FTS3 Virtual Table for fast searches
    private static final String DATABASE_CREATE =
            "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " USING fts3(" +
                    KEY_PLATE + "," +
                    KEY_RECORD + "," +
                    KEY_SEARCH + "," +
                    " UNIQUE (" + KEY_PLATE + "));";


    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }

    private static final String[] COLUMNS = {KEY_PLATE, KEY_RECORD, KEY_SEARCH};

    public DBPolHelper(Context ctx) {
        this.mCtx = ctx;
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }

    public long createPlate(String plate, String record) {

        ContentValues initialValues = new ContentValues();
        String searchValue = plate + " " + record ;
        initialValues.put(KEY_PLATE, plate);
        initialValues.put(KEY_RECORD, record);
        initialValues.put(KEY_SEARCH, searchValue);

        return mDb.insert(FTS_VIRTUAL_TABLE, null, initialValues);
    }

    public Cursor searchPlateWithCursor(String inputText)
    {
        Log.w(TAG, inputText);
        String query = "SELECT docid as _id," +
                KEY_PLATE + "," +
                KEY_RECORD +
                " from " + FTS_VIRTUAL_TABLE +
                " where " +  KEY_SEARCH + " MATCH '*" + inputText + "*';";

        Log.w(TAG, query);
        Cursor mCursor = mDb.rawQuery(query,null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public List<Plate> searchPlate(String inputText) throws SQLException {
        Log.w(TAG, inputText);

        List<Plate> plates = new LinkedList<Plate>();

        String query = "SELECT docid as _id," +
                KEY_PLATE + "," +
                KEY_RECORD +
                " from " + FTS_VIRTUAL_TABLE +
                " where " +  KEY_SEARCH + " MATCH '*" + inputText + "*';";
        Log.w(TAG, query);
        Cursor mCursor = mDb.rawQuery(query,null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        Plate plate = null;
        if (mCursor.moveToFirst()) {
            do {
                plate = new Plate();
                plate.setId(Integer.parseInt(mCursor.getString(0)));
                plate.setPlate(mCursor.getString(1));
                plate.setRecord(mCursor.getString(2));

                plates.add(plate);
            } while (mCursor.moveToNext());
        }

        Log.d("searchPlate()", plates.toString());

        return plates;

    }

    public boolean deleteAllPlates() {

        int doneDelete = 0;
        doneDelete = mDb.delete(FTS_VIRTUAL_TABLE, null, null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;

    }

    public Plate getPlate(String plateCode){

        // 2. build query
        Cursor cursor =
                mDb.query(FTS_VIRTUAL_TABLE, // a. table
                        COLUMNS, // b. column names
                        " plate = ?", // c. selections
                        new String[] { plateCode }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        Plate plate = new Plate();
        plate.setPlate(cursor.getString(0));
        plate.setRecord(cursor.getString(1));

        //log
        Log.d("getPlate(" + plateCode + ")", plate.toString());

        // 5. return book
        return plate;
    }

    public int updatePlate(Plate plate) {

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_RECORD, plate.getRecord()); // get author
        values.put(KEY_SEARCH, plate.getPlate() + " " + plate.getRecord());

        // 3. updating row
        int i = mDb.update(FTS_VIRTUAL_TABLE, //table
                values, // column/value
                KEY_PLATE + " = ?", // selections
                new String[] { plate.getPlate() }); //selection args

        // 4. close
        mDb.close();

        return i;

    }

    public void deletePlate(Plate plate) {

        // 2. delete
        mDb.delete(FTS_VIRTUAL_TABLE, //table name
                KEY_ROWID + " = ?",  // selections
                new String[]{String.valueOf(plate.getId())}); //selections args

        //log
        Log.d("deleteBook", plate.toString());

    }
}