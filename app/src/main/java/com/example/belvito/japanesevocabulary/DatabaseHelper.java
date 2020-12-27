package com.example.belvito.japanesevocabulary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH;
    private static String DB_NAME = "data.db";
    private static String TABLE_NAME = "expressions";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    private boolean dataBaseFileExists() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!dataBaseFileExists()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getResources().openRawResource(R.raw.data);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean importNewExpressions() {
        SQLiteDatabase db = getReadableDatabase();
        BufferedReader bfr = null;
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            FileReader fr = new FileReader(path + "/newExpressions.txt");
            bfr = new BufferedReader(fr);
            String line;
            boolean changes = false;
            while((line = bfr.readLine()) != null) {
                int i = line.indexOf('=');
                String expr = line.substring(0, i);
                String trans = line.substring(i + 1); Log.e("nmc",trans);
                Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE expression LIKE \"" + expr + "\"", null);
                if(c.getCount() == 0) {
                    db.execSQL("INSERT INTO " + TABLE_NAME + "(expression, translation)" +
                            "VALUES(\"" + expr + "\", \"" + trans + "\")");
                    changes = true;
                }
            }
            if(changes) {
                Toast.makeText(mContext, "Import successful", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(mContext, "No changes made", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void importDBFile() {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "/" + DB_NAME);
            FileInputStream mInput = new FileInputStream(file);
            FileOutputStream  mOutput = new FileOutputStream(DB_PATH + DB_NAME);
            writeFromFileToFile(mInput, mOutput);
            Toast.makeText(mContext, "Import successful", Toast.LENGTH_LONG).show();
        } catch(FileNotFoundException e) {
            Toast.makeText(mContext, "Enable storage permission", Toast.LENGTH_LONG).show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void exportDBFile() {
        try {
            FileInputStream mInput = new FileInputStream(DB_PATH + DB_NAME);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "/" + DB_NAME);
            FileOutputStream mOutput = new FileOutputStream(file);
            writeFromFileToFile(mInput, mOutput);
            Toast.makeText(mContext, "Export successful",
                    Toast.LENGTH_LONG).show();
        } catch(FileNotFoundException e) {
            Toast.makeText(mContext, "Enable storage permission",
                    Toast.LENGTH_LONG).show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFromFileToFile(FileInputStream mInput, FileOutputStream mOutput) throws IOException {
        FileChannel fromChannel = mInput.getChannel();
        FileChannel toChannel = mOutput.getChannel();
        fromChannel.transferTo(0, fromChannel.size(), toChannel);
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }
}
