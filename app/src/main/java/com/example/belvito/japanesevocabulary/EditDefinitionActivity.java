package com.example.belvito.japanesevocabulary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditDefinitionActivity extends AppCompatActivity {

    private EditText expression, translation;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_definition);
        expression = findViewById(R.id.expressionEdit);
        expression.setText(getIntent().getStringExtra("expression"));
        translation = findViewById(R.id.translationEdit);
        translation.setText(getIntent().getStringExtra("translation"));
        database = new DatabaseHelper(this).getReadableDatabase();
    }

    public void updateDefinition(View v) {
        Intent returnIntent = new Intent();

        try {
            returnIntent.putExtra("newExpression", expression.getText().toString());
            returnIntent.putExtra("newTranslation", translation.getText().toString());
            setResult(Activity.RESULT_OK, returnIntent);
        } catch(Exception e) {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        } finally {
            finish();
        }
    }

    public void deleteDefinition(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnIntent = new Intent();

                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        returnIntent.putExtra("action", "delete");
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Ești sigur?").setPositiveButton("Da", dialogClickListener)
                .setNegativeButton("Nu", dialogClickListener).show();
    }
}