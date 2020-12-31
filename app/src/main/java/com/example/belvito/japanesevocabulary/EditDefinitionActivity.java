package com.example.belvito.japanesevocabulary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditDefinitionActivity extends AppCompatActivity {

    private final static int LAUNCH_SELECT_IMAGE = 1;

    private EditText expression, translation;
    private ImageView imageView;
    private Bitmap bitmap;
    static byte[] bitmapByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_definition);

        expression = findViewById(R.id.expressionEdit);
        expression.setText(getIntent().getStringExtra("expression"));
        translation = findViewById(R.id.translationEdit);
        translation.setText(getIntent().getStringExtra("translation"));
        imageView = findViewById(R.id.imageViewId);
        if(bitmapByteArray != null && bitmapByteArray.length > 0) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(
                    bitmapByteArray, 0, bitmapByteArray.length));
        }
    }

    public void addImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, LAUNCH_SELECT_IMAGE);
    }

    public void removeImage(View v) {
        bitmap = null;
        imageView.setImageBitmap(null);
        bitmapByteArray = null;
    }

    public void updateDefinition(View v) {
        if(expression.getText().toString().isEmpty() || translation.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Completează toate câmpurile", Toast.LENGTH_SHORT).show();
            return;
        }

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            if (requestCode == LAUNCH_SELECT_IMAGE) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    imageView.setImageBitmap(bitmap);
                    bitmapByteArray = Util.convertBitmapToByteArray(bitmap);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setBitmapByteArray(byte[] bitmapByteArray) {
        EditDefinitionActivity.bitmapByteArray = bitmapByteArray;
    }

    public static byte[] getBitmapByteArray() {
        return bitmapByteArray;
    }
}