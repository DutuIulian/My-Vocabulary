package com.example.belvito.japanesevocabulary.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.belvito.japanesevocabulary.R;

public class DefinitionsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_definitions, container, false);
        TableLayout tableLayout = root.findViewById(R.id.table);
        Context context = root.getContext();
        TableRow tableRow = new TableRow(context);

        tableRow.addView(GetTextView("Expresie", context));
        tableRow.addView(GetTextView("Traducere", context));
        tableRow.addView(GetTextView("Rasp corecte", context));
        tableRow.addView(GetTextView("Rasp gresite", context));
        tableRow.addView(GetTextView("Ultimul raspuns", context));
        tableRow.addView(GetTextView("Interval", context));

        tableLayout.addView(tableRow);

        return root;
    }

    private TextView GetTextView(String str, Context context)
    {
        final TextView textView = new TextView(context);
        textView.setBackground(getResources().getDrawable(R.drawable.border, context.getTheme()));
        textView.setPadding(30, 0, 30, 0);
        textView.setTextSize(15);
        textView.setText(str);

        return textView;
    }
}