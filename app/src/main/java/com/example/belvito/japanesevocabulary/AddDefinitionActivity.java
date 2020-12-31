 package com.example.belvito.japanesevocabulary;

 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;

 import androidx.appcompat.app.AppCompatActivity;

public class AddDefinitionActivity extends AppCompatActivity {

    private final static int LAUNCH_SELECT_IMAGE = 1;

    private SQLiteDatabase database;
    private EditText expression, translation;
    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_definition);

        database = new DatabaseHelper(this).getReadableDatabase();
        expression = findViewById(R.id.expressionEdit);
        translation = findViewById(R.id.translationEdit);
        imageView = findViewById(R.id.imageViewId);
    }

    public void addDefinition(View v) {
        if(expression.getText().toString().isEmpty() || translation.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Completează toate câmpurile", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent returnIntent = new Intent();

        try {
            Definition definition = new Definition(
                    expression.getText().toString(), translation.getText().toString());
            definition.setBitmapByteArray(Util.convertBitmapToByteArray(bitmap));
            definition.executeInsertQuery(database);
            Toast.makeText(getApplicationContext(),
                    "Definiția a fost adăugată", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK, returnIntent);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Definiția nu a putut fi adăugată", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    public void addImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, LAUNCH_SELECT_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            if (requestCode == LAUNCH_SELECT_IMAGE) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}