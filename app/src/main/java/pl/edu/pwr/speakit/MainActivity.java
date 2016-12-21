package pl.edu.pwr.speakit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import pl.edu.pwr.speakit.commands.CallCommand;
import pl.edu.pwr.speakit.commands.SmsCommand;

//TODO SIMILARITY ALGORITHM to recognize app or contact with a string

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mRecognizedText;
    private EditText mTelephoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizedText = (TextView) findViewById(R.id.recognized_text);
        mTelephoneNumber = (EditText) findViewById(R.id.telephone_number_edit_text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mRecognizedText.setText(result.get(0));
                }
                break;
            }
        }
    }

    public void makeCall(View v) {
        CallCommand.makeCall(MainActivity.this, mTelephoneNumber.getText().toString());
    }

    public void startRecognition(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSMS(View view) {
        SmsCommand.sendSms(this, mTelephoneNumber.getText().toString(),
                mRecognizedText.getText().toString());
    }
}
