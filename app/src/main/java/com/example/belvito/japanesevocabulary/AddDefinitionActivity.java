package com.example.belvito.japanesevocabulary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        Intent returnIntent = new Intent();

        try {
            Definition definition = new Definition(
                    expression.getText().toString(), translation.getText().toString());
            database.execSQL(definition.getInsertQuery());
            Toast.makeText(getApplicationContext(),
                    "Definiția a fost adăugată", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK, returnIntent);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Definiția nu a putut fi adăugată", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED, returnIntent);
        } finally {
            finish();
        }
    }
}