package pl.edu.pwr.speakit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.edu.pwr.speakit.commands.CallCommand;
import pl.edu.pwr.speakit.commands.LaunchAppCommand;
import pl.edu.pwr.speakit.commands.SmsCommand;
import pl.edu.pwr.speakit.common.CommandDO;
import pl.edu.pwr.speakit.common.CommandGeneratorThread;
import pl.edu.pwr.speakit.morfeusz.IAsyncMorfeuszResponse;

//TODO SIMILARITY ALGORITHM to recognize app or contact with a string

public class MainActivity extends AppCompatActivity implements IAsyncMorfeuszResponse {
    private static final String TAG = "MainActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mRecognizedTextView;
    private String mRecognizedText = "init";
    private EditText mTelephoneNumber;
    private CommandGeneratorThread mCommandGeneratorAsyncTask = new CommandGeneratorThread(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizedTextView = (TextView) findViewById(R.id.recognized_text);
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
                    mRecognizedText = result.get(0);
                    mRecognizedTextView.setText(mRecognizedText);
                }
                break;
            }
        }
    }

    public void makeCall(View v) {
        CallCommand.makeCall(MainActivity.this, mTelephoneNumber.getText().toString());
    }

    public void executeGeneratingCommands(View v){
        if(isOnline()) {
            mCommandGeneratorAsyncTask.delegate = this;
            mCommandGeneratorAsyncTask.setCommandString("pisać kod");
            mCommandGeneratorAsyncTask.run();
        } else {
            showNoInternetMessage();
        }
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
        SmsCommand.sendSms(this, mTelephoneNumber.getText().toString(), mRecognizedText);
    }

    public void launchApp(View view) {
        Thread launchThread = new Thread() {
            @Override
            public void run() {
                LaunchAppCommand.launchApp(MainActivity.this, mRecognizedText);
            }
        };
        launchThread.start();
    }

    @Override
    public void responseFinished(List<CommandDO> commandList) {
        if(commandList != null)
            Log.d(TAG, "zawartość = " + commandList);
        else
            Log.d(TAG, "cmdList empty");
    }

    private boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void showNoInternetMessage(){
        Toast.makeText(this, R.string.error_msg_no_internet, Toast.LENGTH_LONG).show();
    }
}
