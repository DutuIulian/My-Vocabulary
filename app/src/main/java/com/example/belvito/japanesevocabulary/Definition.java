package com.example.belvito.japanesevocabulary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

class Definition
{
    private final static double FACTOR = 2;
    private final static String TABLE_NAME = "hapineza";
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int ID;
    private String expression, translation;
    private int right, wrong;
    private String lastAnswer;
    private double rememberInterv;
    private String markString;

    public Definition() {
        ID = 0;
        expression = "There are no expressions left";
    }

    public Definition(Cursor cursor) {
        try {
            ID = cursor.getInt(0);
            expression = cursor.getString(1);
            translation = cursor.getString(2);
            right = cursor.getInt(3);
            wrong = cursor.getInt(4);
            lastAnswer = cursor.getString(5); //Timestamp.valueOf(cursor.getString(5));
            rememberInterv = cursor.getDouble(6);
            markString = cursor.getString(7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getID() {
        return ID;
    }

    public String getExpression() {
        return expression;
    }

    public String getTranslation() {
        return translation;
    }

    public int getRight() {
        return right;
    }

    public int getWrong() {
        return wrong;
    }


    /*public double getAnswerInterv() {
        return (double)(System.currentTimeMillis() - lastAnswer.getTime())/(1000*60*60*24);
    }*/

    public double getRememberInterv() {
        return rememberInterv;
    }

    public String getMark() {
        return markString;
    }

    public String toString() {
        return expression+" "+translation;
    }

    public void upvote() {
        try {
            long lastAns = sdf.parse(lastAnswer).getTime();
            long limit = sdf.parse("2000-01-01 00:00:00").getTime();
            if(lastAns > limit) {
                rememberInterv = (double) (new Date().getTime() - lastAns)
                        / (1000 * 60 * 60 * 24) * FACTOR;
            } else {
                rememberInterv = FACTOR * rememberInterv;
            }
            lastAnswer = sdf.format(new Date());
            right++;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void downvote() {
        rememberInterv /= FACTOR;
        lastAnswer = sdf.format(new Date());
        wrong++;
    }

    public void mark(String markString) {
        this.markString = markString;
    }

    public String getUpdateQuery() {
        return "UPDATE " + TABLE_NAME + " SET rightAnswers = " + right + ", wrongAnswers = " + wrong
                + ", lastAnswer = '" + lastAnswer + "', rememberInterv = " + rememberInterv
                + ", markString = '" + markString + "'"
                + " WHERE ID = " + ID;
    }

    public boolean isOnTodaysList(SQLiteDatabase db) {
        if(right == 0) {
            return true;
        }
        String query = "SELECT julianday(current_date)+1-julianday('" + lastAnswer + "') - " + rememberInterv + " > 0 " +
                "AND julianday(current_date)-julianday('" + lastAnswer + "') > 0";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        return cursor.getInt(0) == 1;
    }
}