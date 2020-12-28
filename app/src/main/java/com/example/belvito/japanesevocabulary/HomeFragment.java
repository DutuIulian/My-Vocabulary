package com.example.belvito.japanesevocabulary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        addListenersToButtons(root);
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

    public void markButtonPressed(View v) {
        String markString = markEdit.getText().toString();
        definitionsManager.markCurrentDef(markString);
    }

    private void addListenersToButtons(View root)
    {
        Button upvoteButton = root.findViewById(R.id.upvote);
        Button downvoteButton = root.findViewById(R.id.downvote);
        Button showButton = root.findViewById(R.id.show);
        Button nextButton = root.findViewById(R.id.next);
        Button skipButton = root.findViewById(R.id.skip);
        Button markButton = root.findViewById(R.id.mark);
        Button importExprButton = root.findViewById(R.id.importExpr);
        Button importDbButton = root.findViewById(R.id.importDB);
        Button exportDbButton = root.findViewById(R.id.exportDB);

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                upvoteButtonPressed(v);
            }
        });
        downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                downvoteButtonPressed(v);
            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showButtonPressed(v);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextButtonPressed(v);
            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                skipButtonPressed(v);
            }
        });
        markButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                markButtonPressed(v);
            }
        });
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
}