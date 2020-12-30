package com.example.belvito.japanesevocabulary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String TABLE_NAME = "expressions";

    private static String DB_PATH;
    private static String DB_NAME = "data.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private final Context context;
    private boolean mNeedUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.context = context;

        copyDataBase();
        //getReadableDatabase(); seems it doesn't do anything
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
        InputStream input = context.getResources().openRawResource(R.raw.data);
        OutputStream output = new FileOutputStream(DB_PATH + DB_NAME);
        writeInputStreamToOutputStream(input, output);
    }

    public boolean importNewExpressions(Uri uri) {
        SQLiteDatabase db = getReadableDatabase();
        BufferedReader bfr = null;
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            Reader fr = new InputStreamReader(input);
            bfr = new BufferedReader(fr);
            String line;
            boolean newDefinitionAdded = false;
            while((line = bfr.readLine()) != null) {
                int i = line.indexOf('=');
                String expr = line.substring(0, i);
                String trans = line.substring(i + 1);
                Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE expression LIKE \"" + expr + "\"", null);
                if(c.getCount() == 0) {
                    db.execSQL("INSERT INTO " + TABLE_NAME + "(expression, translation) " +
                            "VALUES(\"" + expr + "\", \"" + trans + "\")");
                    newDefinitionAdded = true;
                }
            }

            if(newDefinitionAdded) {
                Toast.makeText(context, "Expresiile au fost importate cu succes", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(context, "Nu a fost importată nicio definție nouă", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void importDBFile(Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            OutputStream  output = new FileOutputStream(DB_PATH + DB_NAME);
            writeInputStreamToOutputStream(input, output);
            Toast.makeText(context, "Baza de date a fost importată", Toast.LENGTH_LONG).show();
        } catch(FileNotFoundException e) {
            Toast.makeText(context, "Acordă permisiuni pentru stocare", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch(Exception e) {
            Toast.makeText(context, "A apărut o eroare", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void exportDBFile(Uri uri) {
        try {
            FileInputStream input = new FileInputStream(DB_PATH + DB_NAME);
            File outputFile = new File(uri.getPath());
            if(!outputFile.exists()) {
                outputFile.mkdirs();
                outputFile.createNewFile();
            }
            FileOutputStream output = new FileOutputStream(outputFile);
            writeFromFileToFile(input, output);
            Toast.makeText(context, "Baza de date a fost exportată",
                    Toast.LENGTH_LONG).show();
        } catch(FileNotFoundException e) {
            Toast.makeText(context, "Acordă permisiuni pentru stocare",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        catch(Exception e) {
            Toast.makeText(context, "A apărut o eroare", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void writeFromFileToFile(FileInputStream input, FileOutputStream output) throws IOException {
        FileChannel fromChannel = input.getChannel();
        FileChannel toChannel = output.getChannel();
        fromChannel.transferTo(0, fromChannel.size(), toChannel);
    }

    private void writeInputStreamToOutputStream(InputStream mInput, OutputStream mOutput) throws IOException {
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    /*public static String getDatabasePath() {
        return DB_PATH + DB_NAME;
    }*/

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
