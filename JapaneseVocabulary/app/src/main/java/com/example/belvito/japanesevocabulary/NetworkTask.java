package com.example.belvito.japanesevocabulary;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.app.ActivityCompat.requestPermissions;

class NetworkTask0 {/*
    private static String DB_PATH="";//"/data/data/com.example.belvito.japanesevocabulary/";
    private String link;
    private Context context;
    public NetworkTask(String link, Context c) {
        //this.DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.link = link;
        context = c;
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH + "data.sqlite", null,MODE_PRIVATE);

        SQLiteDatabase db;
        DatabaseHelper mDBHelper = new DatabaseHelper(context);
        try {
            db = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        Log.e("cv!!!,",db.getPath());
        Cursor c = db.rawQuery("SELECT * FROM hapineza WHERE ID LIKE '1'", null);
        c.moveToFirst();
        DefinitionsManager.setAnswer(c.getString(c.getColumnIndex("expression")));
        if(1==1) return null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(link);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bfr = new BufferedReader(new InputStreamReader(is));
            String answer = Html.fromHtml(bfr.readLine()).toString();
            DefinitionsManager.setAnswer(answer);
        } catch (Exception e) {
            e.printStackTrace();
            //new AlertDialog.Builder(c).setTitle("Argh").setMessage(e.toString()).setNeutralButton("Close", null).show();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
    */
}
