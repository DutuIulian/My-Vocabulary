package com.example.belvito.japanesevocabulary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DefinitionsFragment extends Fragment {

    private SQLiteDatabase database;

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_definitions, container, false);
        TableLayout tableLayout = root.findViewById(R.id.table);
        Context context = root.getContext();

        TableRow headerRow = new TableRow(context);
        headerRow.addView(getTextView("Expresie", context));
        headerRow.addView(getTextView("Traducere", context));
        headerRow.addView(getTextView("+1", context));
        headerRow.addView(getTextView("-1", context));
        headerRow.addView(getTextView("Ultimul raspuns", context));
        headerRow.addView(getTextView("Interval", context));

        tableLayout.addView(headerRow);

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TableRow row = new TableRow(context);
            addDataToTableRow(row, cursor, context);
            tableLayout.addView(row);
            cursor.moveToNext();
        }

        return root;
    }

    private TextView getTextView(String str, Context context)
    {
        final TextView textView = new TextView(context);
        textView.setBackground(getResources().getDrawable(R.drawable.border, context.getTheme()));
        textView.setPadding(30, 0, 30, 0);
        textView.setTextSize(9);
        textView.setText(str);

        return textView;
    }

    private void addDataToTableRow(TableRow tableRow, Cursor cursor, Context context)
    {
        Definition definition = new Definition(cursor);
        tableRow.addView(getTextView(definition.getExpression(), context));
        tableRow.addView(getTextView(definition.getTranslation(), context));
        tableRow.addView(getTextView(Integer.toString(definition.getRight()), context));
        tableRow.addView(getTextView(Integer.toString(definition.getWrong()), context));
        tableRow.addView(getTextView(definition.getLastAnswer(), context));
        tableRow.addView(getTextView(Double.toString(definition.getRememberInterv()), context));
    }
}