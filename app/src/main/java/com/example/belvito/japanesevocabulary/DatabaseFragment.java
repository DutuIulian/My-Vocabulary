package com.example.belvito.japanesevocabulary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.net.URI;

public class DatabaseFragment extends Fragment {

    private int LAUNCH_SELECT_FILE_IMPORT_EXPRESSIONS = 1;
    private int LAUNCH_SELECT_FILE_IMPORT_DB = 2;
    private int LAUNCH_SELECT_FILE_EXPORT_DB = 3;

    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_database, container, false);
        addListenersToButtons(root);
        return root;
    }

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    private void addListenersToButtons(View root)
    {
        Button importExprButton = root.findViewById(R.id.importExpr);
        Button importDbButton = root.findViewById(R.id.importDB);
        Button exportDbButton = root.findViewById(R.id.exportDB);

        importExprButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                importExprButtonPressed(v);
            }
        });
        importDbButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                importButtonPressed(v);
            }
        });
        exportDbButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exportButtonPressed(v);
            }
        });
    }

    public void importExprButtonPressed(View v) {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Alege fișierul"),
                LAUNCH_SELECT_FILE_IMPORT_EXPRESSIONS);
    }

    public void importButtonPressed(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, LAUNCH_SELECT_FILE_IMPORT_DB);
    }

    public void exportButtonPressed(View v) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "data.db");
        startActivityForResult(intent, LAUNCH_SELECT_FILE_EXPORT_DB);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            if (requestCode == LAUNCH_SELECT_FILE_IMPORT_EXPRESSIONS) {
                if(databaseHelper.importNewExpressions(uri)) {
                    HomeFragment.informDatabaseChanged();
                }
            } else if (requestCode == LAUNCH_SELECT_FILE_IMPORT_DB) {
                databaseHelper.importDBFile(uri);
                HomeFragment.informDatabaseChanged();
            } else if (requestCode == LAUNCH_SELECT_FILE_EXPORT_DB) {
                String path = uri.getPath().split(":")[1];
                databaseHelper.exportDBFile(Uri.parse(path));
            }
        }
    }
}