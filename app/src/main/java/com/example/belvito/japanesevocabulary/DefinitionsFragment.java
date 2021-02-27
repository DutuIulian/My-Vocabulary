package com.example.belvito.japanesevocabulary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

public class DefinitionsFragment extends Fragment {

    private int LAUNCH_ADD_DEFINITION_ACTIVITY = 1;
    private int LAUNCH_EDIT_DEFINITION_ACTIVITY = 2;

    private Definition definitionToEdit;
    private SQLiteDatabase database;
    private TableLayout tableLayout;
    private Context context;

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_definitions, container, false);
        context = root.getContext();
        tableLayout = root.findViewById(R.id.table);
        FloatingActionButton addButton = root.findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, AddDefinitionActivity.class);
                startActivityForResult(intent, LAUNCH_ADD_DEFINITION_ACTIVITY);
            }
        });
        loadDataIntoTable();

        return root;
    }

    private TextView getTextView(String str)
    {
        final TextView textView = new TextView(context);
        textView.setBackground(getResources().getDrawable(R.drawable.border, context.getTheme()));
        textView.setPadding(20, 0, 20, 0);
        textView.setTextSize(9);
        textView.setText(str);
        textView.setGravity(1);

        return textView;
    }

    private String buildIntervalString(double interval)
    {
        if(interval >=365) {
            interval /= 365;
            if((int)interval == 1)
                return "un an";
            else
                return (int)interval + " ani";
        } else if(interval >= 30) {
            interval /= 30;
            if((int)interval == 1)
                return "o lună";
            else
                return (int)interval + " luni";
        } else if(interval >= 1) {
            if((int)interval == 1)
                return "o zi";
            else if((int)interval < 20)
                return (int)interval + " zile";
            else
                return (int)interval + " de zile";
        } else if(interval * 24 >= 1) {
            return (int)(24 * interval) + "h";
        } else if(interval * 24 * 60 >= 1) {
            return (int)(24 * 60 * interval) + "m";
        } else {
            return (int)(24 * 60 * 60 * interval) + "s";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_EDIT_DEFINITION_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if(definitionToEdit == null) {
                return;
            }

            String newExpression = data.getStringExtra("newExpression");
            String newTranslation = data.getStringExtra("newTranslation");
            byte[] newImage = EditDefinitionActivity.getBitmapByteArray();

            if(newExpression != null && newTranslation != null) {
                definitionToEdit.setExpression(newExpression);
                definitionToEdit.setTranslation(newTranslation);
                definitionToEdit.setBitmapByteArray(newImage);
                definitionToEdit.executeUpdateQuery(database);
                definitionToEdit = null;
                loadDataIntoTable();
                Toast.makeText(context, "Definiția a fost salvată", Toast.LENGTH_SHORT).show();
            } else if("delete".equals(data.getStringExtra("action"))) {
                database.execSQL(definitionToEdit.getDeleteQuery());
                loadDataIntoTable();
                Toast.makeText(context, "Definiția a fost ștearsă", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LAUNCH_ADD_DEFINITION_ACTIVITY && resultCode == Activity.RESULT_OK) {
            loadDataIntoTable();
        }
    }

    private void loadDataIntoTable() {
        TableRow headerRow = new TableRow(context);
        headerRow.addView(getTextView("Expresie"));
        headerRow.addView(getTextView("Traducere"));
        headerRow.addView(getTextView("+1"));
        headerRow.addView(getTextView("-1"));
        headerRow.addView(getTextView("Răspuns"));
        headerRow.addView(getTextView("Interv."));
        headerRow.addView(getTextView("Editare"));
        tableLayout.removeAllViews();
        tableLayout.addView(headerRow);

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TableRow row = new TableRow(getContext());
            addDataToTableRow(row, cursor);
            tableLayout.addView(row);
            cursor.moveToNext();
        }
    }

    private void addDataToTableRow(TableRow tableRow, Cursor cursor)
    {
        final Definition definition = new Definition(cursor);
        String expression = definition.getExpression();
        if(expression.length() >= 15)
            expression = expression.substring(0, 11) + "...";
        tableRow.addView(getTextView(expression));

        String translation = definition.getTranslation();
        if(translation.length() >= 18)
            translation = translation.substring(0, 14) + "...";
        tableRow.addView(getTextView(translation));

        tableRow.addView(getTextView(Integer.toString(definition.getRight())));
        tableRow.addView(getTextView(Integer.toString(definition.getWrong())));
        if(definition.isLasAnswerDateValid())
            tableRow.addView(getTextView(getLastAnswerDateString(definition.getLastAnswer())));
        else
            tableRow.addView(getTextView("-"));
        String intervalString = buildIntervalString(definition.getRememberInterv());
        tableRow.addView(getTextView(intervalString));
        ImageButton button = new ImageButton(context);
        button.setImageResource(R.drawable.ic_edit);
        button.setBackground(getResources().getDrawable(R.drawable.border, context.getTheme()));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditDefinitionActivity.class);
                intent.putExtra("expression", definition.getExpression());
                intent.putExtra("translation", definition.getTranslation());
                //intent.putExtra("image", definition.getBitmapByteArray());
                EditDefinitionActivity.setBitmapByteArray(definition.getBitmapByteArray());
                definitionToEdit = definition;
                try {
                    startActivityForResult(intent, LAUNCH_EDIT_DEFINITION_ACTIVITY);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tableRow.addView(button);
        button.getLayoutParams().width = 20;
        button.getLayoutParams().height = 37;
    }

    private String getLastAnswerDateString(String lastAnswerDate) {
        try {
            long lastAnswer = DefinitionsManager.getDateFormat().parse(lastAnswerDate).getTime();
            long now = new Date().getTime();
            double diffInDays = (now - lastAnswer) / 1000.0 / 3600 / 24;

            return buildIntervalString(diffInDays);
        } catch(Exception e) {
            return "-";
        }
    }
}