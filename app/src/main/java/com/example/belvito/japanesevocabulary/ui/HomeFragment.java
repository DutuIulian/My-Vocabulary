package com.example.belvito.japanesevocabulary.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.belvito.japanesevocabulary.DatabaseHelper;
import com.example.belvito.japanesevocabulary.Definition;
import com.example.belvito.japanesevocabulary.DefinitionsManager;
import com.example.belvito.japanesevocabulary.R;

public class HomeFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private DefinitionsManager definitionsManager;
    private Definition currentDefinition;
    private TextView question, information;
    private EditText answer, markEdit;
    private State currentState = State.NOTHING;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void setDefinitionsManager(DefinitionsManager definitionsManager) {
        this.definitionsManager = definitionsManager;
    }

    private enum State {
        NOTHING, SHOWN, VOTED
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        currentDefinition = definitionsManager.getNextDefinition(true);
        question = root.findViewById(R.id.question);
        question.setText(currentDefinition.getExpression());
        answer = root.findViewById(R.id.answer);
        markEdit = root.findViewById(R.id.markEdit);
        information = root.findViewById(R.id.information);
        information.setText(definitionsManager.getInformation());
        return root;
    }

    public void showButtonPressed(View v) {
        if (currentState == State.NOTHING && currentDefinition.getID() != 0) {
            question.append("\n" + currentDefinition.getTranslation());
            if(currentDefinition.getMark() != null && !currentDefinition.getMark().equals("")) {
                question.append("\nComment: " + currentDefinition.getMark());
            }
            currentState = State.SHOWN;
        }
    }

    public void upvoteButtonPressed(View v) {
        if (currentState == State.SHOWN) {
            definitionsManager.vote(1);
            currentState = State.VOTED;
        }
    }

    public void downvoteButtonPressed(View v) {
        if (currentState == State.SHOWN) {
            definitionsManager.vote(-1);
            currentState = State.VOTED;
        }
    }

    public void nextButtonPressed(View v) {
        if (currentState == State.VOTED) {
            showNextDefinition(true);
        }
    }

    private void showNextDefinition(boolean voted) {
        currentDefinition = definitionsManager.getNextDefinition(voted);
        question.setText(currentDefinition.getExpression());
        answer.setText("");
        markEdit.setText("");
        currentState = State.NOTHING;
    }

    public void skipButtonPressed(View v) {
        showNextDefinition(false);
    }

    public void importExprButtonPressed(View v) {
        if(databaseHelper.importNewExpressions()) {
            definitionsManager.repopulateList();
            showNextDefinition(true);
        }
    }

    public void importButtonPressed(View v) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        databaseHelper.importDBFile();
                        definitionsManager.repopulateList();
                        showNextDefinition(true);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getActivity(), "Import cancelled", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void exportButtonPressed(View v) {
        databaseHelper.exportDBFile();
    }

    public void informationChanged(String info) {
        information.setText(info);
    }

    public void mark(View v) {
        String markString = markEdit.getText().toString();
        definitionsManager.markCurrentDef(markString);
    }
}