package com.example.belvito.japanesevocabulary;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private DefinitionsManager definitionsManager;
    private Definition currentDefinition;
    private TextView question, information;
    private EditText answer, markEdit;
    private State currentState = State.NOTHING;
    private static boolean databaseChanged = false;

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
        if(databaseChanged) {
            definitionsManager.repopulateList();
            currentDefinition = definitionsManager.getNextDefinition(true);
            databaseChanged = false;
        }
        if(currentDefinition == null) {
            currentDefinition = definitionsManager.getNextDefinition(true);
        }
        question = root.findViewById(R.id.question);
        question.setText(currentDefinition.getExpression());
        if(currentState == State.SHOWN || currentState == State.VOTED) {
            showTranslationAndMark();
        }
        answer = root.findViewById(R.id.answer);
        markEdit = root.findViewById(R.id.markEdit);
        information = root.findViewById(R.id.information);
        information.setText(definitionsManager.getInformation());
        addListenersToButtons(root);
        return root;
    }

    public void showButtonPressed(View v) {
        if (currentState == State.NOTHING && currentDefinition.getID() != 0) {
            showTranslationAndMark();
            currentState = State.SHOWN;
        }
    }

    private void showTranslationAndMark() {
        question.append("\n" + currentDefinition.getTranslation());
        if(currentDefinition.getMark() != null && !currentDefinition.getMark().equals("null")) {
            question.append("\nComment: " + currentDefinition.getMark());
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

    public void informationChanged(String info) {
        information.setText(info);
    }

    public void markButtonPressed(View v) {
        String markString = markEdit.getText().toString();
        definitionsManager.markCurrentDefinition(markString);
    }

    private void addListenersToButtons(View root)
    {
        Button upvoteButton = root.findViewById(R.id.upvote);
        Button downvoteButton = root.findViewById(R.id.downvote);
        Button showButton = root.findViewById(R.id.show);
        Button nextButton = root.findViewById(R.id.next);
        Button skipButton = root.findViewById(R.id.skip);
        Button markButton = root.findViewById(R.id.mark);

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
    }

    public static void informDatabaseChanged() {
        databaseChanged = true;
    }
}