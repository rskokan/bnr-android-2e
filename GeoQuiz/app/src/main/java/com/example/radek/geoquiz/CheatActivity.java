package com.example.radek.geoquiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE = "solucs.extra.answer_is_true";
    private static final String EXTRA_ANSWER_SHOWN = "solucs.answer_shown";

    private boolean mAnswerIsTrue;

    private TextView mAnswerTextView;


    public static Intent newIntent(Context context, boolean isAnswerTrue) {
        Intent i = new Intent(context, CheatActivity.class);
        i.putExtra(EXTRA_ANSWER_IS_TRUE, isAnswerTrue);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);

        mAnswerTextView = (TextView) findViewById(R.id.answer_text_view);
        findViewById(R.id.show_answer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int answer = mAnswerIsTrue ? R.string.true_button : R.string.false_button;
                mAnswerTextView.setText(answer);
                setAnswerShownResult(true);
            }
        });
    }

    private void setAnswerShownResult(boolean isAnswerShown) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown);
        setResult(RESULT_OK, data);
    }

    public static boolean wasAnswerShown(Intent result) {
        return result.getBooleanExtra(EXTRA_ANSWER_SHOWN, false);
    }

}
