package com.example.belvito.japanesevocabulary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Definition
{
    private final static double FACTOR = 2;
    private final static double INTERVAL_MIN = 0.0001;
    private final static String TABLE_NAME = "expressions";
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int ID;
    private String expression, translation;
    private int right, wrong;
    private String lastAnswer;
    private double rememberInterv;
    private String markString;
    private byte[] bitmapByteArray;

    public Definition() {
        ID = 0;
        expression = "There are no expressions left";
    }

    public Definition(String expression, String translation) {
        this.expression = expression;
        this.translation = translation;
        this.markString = "";
    }

    public Definition(Cursor cursor) {
        try {
            ID = cursor.getInt(0);
            expression = cursor.getString(1);
            translation = cursor.getString(2);
            right = cursor.getInt(3);
            wrong = cursor.getInt(4);
            lastAnswer = cursor.getString(5);
            rememberInterv = cursor.getDouble(6);
            markString = cursor.getString(7);
            bitmapByteArray = cursor.getBlob(8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getID() {
        return ID;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
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

    public String getLastAnswer() {
        return lastAnswer;
    }

    public double getRememberInterv() {
        return rememberInterv;
    }

    public String getMark() {
        return markString;
    }

    public void setBitmapByteArray(byte[] bitmapByteArray) {
        this.bitmapByteArray = bitmapByteArray;
    }

    public byte[] getBitmapByteArray() {
        return bitmapByteArray;
    }

    public String toString() {
        return expression + " " + translation;
    }

    public void upvote() {
        try {
            if(isLasAnswerDateValid()) {
                long lastAns = sdf.parse(lastAnswer).getTime();
                double newRememberInterv = (double) (new Date().getTime() - lastAns)
                        / (1000 * 60 * 60 * 24) * FACTOR;
                if(newRememberInterv > rememberInterv) {
                    rememberInterv = newRememberInterv;
                }
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
        if(rememberInterv < INTERVAL_MIN) {
            rememberInterv = INTERVAL_MIN;
        }
        lastAnswer = sdf.format(new Date());
        wrong++;
    }

    public void mark(String markString) {
        this.markString = markString;
    }

    public void executeUpdateQuery(SQLiteDatabase db) {
        String query = "UPDATE " + TABLE_NAME + " SET expression=?, translation=?, rightAnswers=?, "
                    + "wrongAnswers=?, lastAnswer=?, rememberInterv=?, markString=?, image=? WHERE ID=?";
        SQLiteStatement statement = db.compileStatement(query);
        statement.clearBindings();
        statement.bindString(1, expression);
        statement.bindString(2, translation);
        statement.bindString(3, Integer.toString(right));
        statement.bindString(4, Integer.toString(wrong));
        statement.bindString(5, lastAnswer);
        statement.bindString(6, Double.toString(rememberInterv));
        if(markString != null) {
            statement.bindString(7, markString);
        } else {
            statement.bindNull(7);
        }

        if(bitmapByteArray != null) {
            statement.bindBlob(8, bitmapByteArray);
        } else {
            statement.bindNull(8);
        }
        statement.bindString(9, Integer.toString(ID));

        statement.executeUpdateDelete();
    }

    public void executeInsertQuery(SQLiteDatabase db) {
        String query = "INSERT INTO " + TABLE_NAME + "(expression, translation, image) VALUES(?,?,?)";
        SQLiteStatement statement = db.compileStatement(query);
        statement.clearBindings();
        statement.bindString(1, expression);
        statement.bindString(2,translation);
        if(bitmapByteArray != null) {
            statement.bindBlob(3, bitmapByteArray);
        } else {
            statement.bindNull(3);
        }
        statement.executeInsert();
    }

    public String getDeleteQuery() {
        return "DELETE FROM " + TABLE_NAME + " WHERE ID=" + ID;
    }

    public boolean isOnTodaysList(SQLiteDatabase db) {
        if(right == 0) {
            return true;
        }
        //check if end of day (YY-mm-dd 23:59:59) - last answer time > rememberInterval
        String query = "SELECT julianday(current_date)+1 - julianday('" + lastAnswer + "') - " + rememberInterv + " > 0 ";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        return cursor.getInt(0) == 1;
    }

    public boolean isLasAnswerDateValid() {
        try {
            long lastAns = sdf.parse(lastAnswer).getTime();
            long earliestValidDate = sdf.parse("2000-01-01 00:00:00").getTime();
            return lastAns > earliestValidDate;
        } catch(Exception e) {
            return false;
        }
    }
}