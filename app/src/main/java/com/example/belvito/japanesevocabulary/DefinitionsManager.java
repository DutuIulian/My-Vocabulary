package com.example.belvito.japanesevocabulary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DefinitionsManager {
    private final static int MAX_QUESTIONS = 10;
    private final static String TABLE = "expressions";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private int newQuestions, questionsForToday;
    private MainActivity activity;
    private SQLiteDatabase database;
    private Definition currentDefinition;
    private List<Definition> definitions = new ArrayList();

    public DefinitionsManager(DatabaseHelper databaseHelper, MainActivity activity) {
        this.activity = activity;
        database = databaseHelper.getReadableDatabase();
        calculateRemainingNumberOfQuestions();
        populateDefinitionList();
    }

    static public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    private void calculateRemainingNumberOfQuestions() {
        String query1 = "SELECT COUNT(*) FROM " + TABLE + " WHERE rightAnswers = 0";
        Cursor cursor = database.rawQuery(query1,null);
        cursor.moveToFirst();
        newQuestions = cursor.getInt(0);
        cursor.close();

        String query2 = "SELECT COUNT(*) FROM " + TABLE + " " +
                "WHERE rightAnswers = 0 " +
                "OR julianday(current_date) + 1 - julianday(lastAnswer) - rememberInterv > 0 ";
        cursor = database.rawQuery(query2, null);
        cursor.moveToFirst();
        questionsForToday = cursor.getInt(0);
        cursor.close();
    }

    public Definition getNextDefinition(boolean voted) {
        try {
            //if user voted, the populateDefinitionList function was already called
            if(!voted && definitions.isEmpty()) {
                populateDefinitionList();
            }
            currentDefinition = definitions.get(0);
            definitions.remove(0);
            return currentDefinition;
        } catch(Exception e) {
            return new Definition();
        }
    }

    public void repopulateList() {
        definitions.clear();
        populateDefinitionList();
        calculateRemainingNumberOfQuestions();
        activity.informationChanged(getInformation());
    }

    private void populateDefinitionList() {
        try {
            //add the questions with 0 right answers
            String query = "SELECT * FROM " + TABLE + " WHERE rightAnswers = 0";
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                definitions.add(new Definition(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            if(!definitions.isEmpty()) {
                //if we have questions with 0 right answers, shuffle them and add one old question
                Collections.shuffle(definitions);
                definitions.addAll(getOldQuestions(1));
            } else {
                //otherwise, add only old questions
                definitions.addAll(getOldQuestions(MAX_QUESTIONS));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private List<Definition> getOldQuestions(int count) {
        List<Integer> indList = new ArrayList();
        double[][] probabilities = getProbabilities();
        //elementul i este ales daca nr random este cuprins intre probabilities[i][0] si probabilities[i + 1][0]
        //probabilities[i][1] reprezinta index-ul elementului i in tabela
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
            //daca elementul gasit este deja in lista, cauta altul
            indList.add(j);
        }
        StringBuilder sb = new StringBuilder("(");
        for(int i = 0; i < indList.size() - 1; i++) {
            sb.append(indList.get(i));
            sb.append(",");
        }
        sb.append(indList.get(indList.size() - 1));
        sb.append(")");
        return getDefinitions(sb);
    }

    private double[][] getProbabilities() {
        String query1 = "SELECT ID, lastAnswer, rememberInterv FROM " + TABLE + " " +
                "WHERE rightAnswers > 0 " +
                "AND julianday(current_date)+1 - julianday(lastAnswer) - rememberInterv > 0";
        Cursor cursor = database.rawQuery(query1, null);
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
                double diffInDays = (now - lastAnswer.getTime()) / 1000.0 / 3600 / 24;
                probability[i][0] = (diffInDays - rememberInterval) / rememberInterval;
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

    private List getDefinitions(StringBuilder sb) {
        List definitions = new ArrayList();
        String query = "SELECT * FROM " + TABLE + " WHERE ID IN " + sb;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            definitions.add(new Definition(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return definitions;
    }

    public String getInformation() {
        //todo handle 0/1/20+
        return newQuestions + " întrebări noi rămase\n"
                + "și " + questionsForToday + " întrebări pentru azi";
    }

    public void vote(int v) {
        boolean wasOnTodaysListBeforeVote = currentDefinition.isOnTodaysList(database);
        if(v == 1) {
            if(currentDefinition.getRight() == 0) {
                newQuestions--;
            }
            currentDefinition.upvote();
        } else {
            currentDefinition.downvote();
        }
        boolean isOnTodaysListAfterVote = currentDefinition.isOnTodaysList(database);
        if(wasOnTodaysListBeforeVote && !isOnTodaysListAfterVote) {
            questionsForToday--;
        }
        activity.informationChanged(getInformation());
        currentDefinition.executeUpdateQuery(database); //update definition inside database
        checkEmptyListAsync();
    }

    public void markCurrentDefinition(String markString) {
        //adds user's observation to the definition inside database
        currentDefinition.mark(markString);
        currentDefinition.executeUpdateQuery(database);
    }

    private void checkEmptyListAsync() {
        //if there are no definitions left, populate list in the background
        //so that the user can still see/vote the current definition
        if(definitions.isEmpty()) {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    populateDefinitionList();
                }
            }.start();
        }
    }
}
