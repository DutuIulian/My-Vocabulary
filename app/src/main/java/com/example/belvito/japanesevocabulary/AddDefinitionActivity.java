package com.example.belvito.japanesevocabulary;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddDefinitionActivity extends AppCompatActivity {

    private EditText expression, translation;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_definition);
        expression = findViewById(R.id.expressionEdit);
        translation = findViewById(R.id.translationEdit);
        database = new DatabaseHelper(this).getReadableDatabase();
    }

    public void addDefinition(View v) {
        Definition definition = new Definition(
                expression.getText().toString(), translation.getText().toString());
        database.execSQL(definition.getInsertQuery());
    }
}