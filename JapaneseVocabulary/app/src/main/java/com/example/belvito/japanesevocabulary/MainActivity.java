package com.example.belvito.japanesevocabulary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbh;
    private DefinitionsManager defm;
    private Definition currentDefinition;
    private TextView question, information;
    private EditText answer;
    private State state = State.NOTHING;

    private enum State {
        NOTHING, SHOWN, VOTED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbh = new DatabaseHelper(this);
        defm = new DefinitionsManager(dbh, this);
        currentDefinition = defm.getNextDefinition(true);
        question = (TextView) findViewById(R.id.question);
        question.setText(currentDefinition.getExpression());
        answer = (EditText) findViewById(R.id.answer);
        information = (TextView) findViewById(R.id.information);
        information.setText(defm.getInformation());
    }

    @Override
    protected void onStop() {
        super.onStop();
        //DefinitionsManager.commitChanges();
    }

    public void showButtonPressed(View v) {
        if (state == State.NOTHING && currentDefinition.getID() != 0) {
            question.append("\n" + currentDefinition.getTranslation());
            state = State.SHOWN;
        }
    }

    public void upvoteButtonPressed(View v) {
        if (state == State.SHOWN) {
            defm.vote(1);
            state = State.VOTED;
        }
    }

    public void downvoteButtonPressed(View v) {
        if (state == State.SHOWN) {
            defm.vote(-1);
            state = State.VOTED;
        }
    }

    public void nextButtonPressed(View v) {
        if (state == State.VOTED) {
            nextDefinition(true);
        }
    }

    private void nextDefinition(boolean voted) {
        currentDefinition = defm.getNextDefinition(voted);
        question.setText(currentDefinition.getExpression());
        answer.setText("");
        state = State.NOTHING;
    }

    /*public void nextTargetPressed(View v) {
        final Context context = this;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        defm.nextTarget();
                        information.setText(defm.getInformation());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(context, "Next target cancelled", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }*/

    public void skipButtonPressed(View v) {
        nextDefinition(false);
    }

    public void importExprButtonPressed(View v) {
        if(dbh.importNewExpressions()) {
            defm.repopulateList();
            nextDefinition(true);
        }
    }

    public void importButtonPressed(View v) {
        final Context context = this;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dbh.importDBFile();
                        defm.repopulateList();
                        nextDefinition(true);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(context, "Import cancelled", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void exportButtonPressed(View v) {
        dbh.exportDBFile();
    }

    public void informationChanged(String info) {
        information.setText(info);
    }
}
