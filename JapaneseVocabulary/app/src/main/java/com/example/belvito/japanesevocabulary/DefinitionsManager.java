package com.example.belvito.japanesevocabulary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

class DefinitionsManager {
    private final static int MAX_QUESTIONS = 10;
    //private final static double CONSTANT = 1.045493677;
    private final static String TABLE = "hapineza";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    //private static String PATH;
    //private double desiredDaily, currentDaily;
    private int newQuestions, questionsForToday;
    private MainActivity activity;
    private SQLiteDatabase db;
    private Definition currentDefinition;
    private List<Definition> definitions = new ArrayList();

    public DefinitionsManager(DatabaseHelper dbh, MainActivity activity) {
        /*if (android.os.Build.VERSION.SDK_INT >= 17) {
            PATH = activity.getApplicationInfo().dataDir + "/databases/";
        } else {
            PATH = "/data/data/" + activity.getPackageName() + "/databases/";
        }*/
        this.activity = activity;
        db = dbh.getReadableDatabase();
        //readDesiredNumber();
        calculateRemainingQuestNmb();
        populateList();
    }

    private void calculateRemainingQuestNmb() {
        String query1 = "SELECT COUNT(*) FROM " + TABLE + " WHERE rightAnswers = 0";
        Cursor cursor = db.rawQuery(query1,null);
        cursor.moveToFirst();
        newQuestions = cursor.getInt(0);
        cursor.close();
        String query2 = "SELECT COUNT(*) FROM " + TABLE + " " +
                "WHERE rightAnswers = 0 " +
                "OR julianday(current_date) + 1 - julianday(lastAnswer) - rememberInterv > 0 " +
                "AND julianday(current_date) - julianday(lastAnswer) > 0";
        cursor = db.rawQuery(query2, null);
        cursor.moveToFirst();
        questionsForToday = cursor.getInt(0);
        cursor.close();
    }

    public Definition getNextDefinition(boolean voted) {
        try {
            if(!voted && definitions.isEmpty()) {
                populateList();
            }
            currentDefinition = definitions.get(0);
            definitions.remove(0);
            return currentDefinition;
        } catch(Exception e) {
            return new Definition();
        }
    }

    /*private void readDesiredNumber() {
        BufferedReader bfr = null;
        try {
            FileReader fr = new FileReader(PATH + "desired_daily.txt");
            bfr = new BufferedReader(fr);
            desiredDaily = Double.parseDouble(bfr.readLine());
        } catch(FileNotFoundException e) {
            desiredDaily = 164.5108741;
            writeDesiredNumberFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
        closeSilently(bfr);
    }*/

    /*private void writeDesiredNumberFile() {
        BufferedWriter bfw = null;
        try {
            FileWriter fw = new FileWriter(PATH + "desired_daily.txt");
            bfw = new BufferedWriter(fw);
            bfw.write(Double.toString(desiredDaily));
        } catch(Exception e) {
            e.printStackTrace();
        }
        closeSilently(bfw);
    }*/

    /*private void closeSilently(Closeable c) {
        try {
            if(c != null) {
                c.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/

    public void repopulateList() {
        definitions.clear();
        populateList();
        calculateRemainingQuestNmb();
        activity.informationChanged(getInformation());
    }

    private void populateList() {
        try {
            String query = "SELECT * FROM " + TABLE + " WHERE rightAnswers = 0";
            Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                definitions.add(new Definition(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            if(!definitions.isEmpty()) {
                Collections.shuffle(definitions);
                definitions.addAll(getOldQuestions(1));
            } else {
                definitions.addAll(getOldQuestions(MAX_QUESTIONS));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private List<Definition> getOldQuestions(int count) {
        List<Integer> indList = new ArrayList();
        double[][] probabilities = getProbabilities();
        if(probabilities == null) {
            return new ArrayList();
        }
        Random r = new Random();
        int n = Math.min(count, probabilities.length);
        for(int i = 0; i < n; i++) {
            int j;
            double rand;
            do {
                rand = 100 * r.nextDouble();
                j = binarySearch(rand, probabilities);
            } while(indList.contains(j));
            indList.add(j);
        }
        StringBuilder sb = new StringBuilder("(");
        for(int i = 0; i < indList.size() - 1; i++) {
            sb.append(indList.get(i));
            sb.append(",");
        }
        sb.append(indList.get(indList.size() - 1));
        sb.append(")");
        return getDefs(sb);
    }

    private double[][] getProbabilities() {
        String query1 = "SELECT ID, lastAnswer, rememberInterv FROM " + TABLE + " " +
                "WHERE rightAnswers > 0 " +
                "AND julianday(current_timestamp) - julianday(lastAnswer) - rememberInterv > 0 " +"";
                //"AND julianday(current_timestamp) - julianday(lastAnswer) - 1 > 0";
        Cursor cursor = db.rawQuery(query1, null);
        double[][] probability = null;
        try {
            if(cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            long now = new Date().getTime();
            int n = cursor.getCount();
            probability = new double[n][2];
            double sum = 0;
            for(int i = 0; i < n; i++) {
                Date lastAnswer = dateFormat.parse(cursor.getString(1));
                double rememberInterval = cursor.getDouble(2);
                probability[i][0] = ((now - lastAnswer.getTime() - rememberInterval) / 1000.0 / 3600 / 24) / rememberInterval;
                /*if(rememberInterval < 16) {
                    probability[i][0] *= 12000;
                }*/
                probability[i][1] = cursor.getInt(0);
                sum += probability[i][0];
                cursor.moveToNext();
            }
            probability[0][0] *= 100 / sum;
            for(int i = 1; i < n - 1; i++) {
                probability[i][0] *= 100 / sum;
                probability[i][0] += probability[i - 1][0];
            }
            probability[n - 1][0] = 100;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return probability;
    }

    private int binarySearch(double a, double[][] v) {
        int st = 0;
        int dr = v.length - 1;
        int m = 0;
        while(st < dr) {
            m = (st + dr) / 2;
            if(a > v[m][0])
                st = m + 1;
            else
                dr = m;
        }
        if(v[m][0] < a) {
            m++;
        }
        return (int)v[m][1];
    }

    private List getDefs(StringBuilder sb) {
        List defs = new ArrayList();
        String query2 = "SELECT * FROM " + TABLE + " WHERE ID IN " + sb;
        Cursor cursor = db.rawQuery(query2, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            defs.add(new Definition(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return defs;
    }

    public String getInformation() {
        return newQuestions + " more new questions\n"
                + "and " + questionsForToday + " questions for today";
    }

    public void vote(int v) {
        boolean b1 = currentDefinition.isOnTodaysList(db);
        if(v == 1) {
            if(currentDefinition.getRight() == 0) {
                newQuestions--;
            }
            currentDefinition.upvote();
        } else {
            currentDefinition.downvote();
        }
        boolean b2 = currentDefinition.isOnTodaysList(db);
        if(b1 && !b2) {
            questionsForToday--;
        }
        activity.informationChanged(getInformation());
        db.execSQL(currentDefinition.getUpdateQuery());
        checkEmptyListAsync();
    }

    private void checkEmptyListAsync() {
        if(definitions.isEmpty()) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    populateList();
                }
            }.start();
        }
    }
}
