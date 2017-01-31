package pl.edu.pwr.speakit;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import pl.edu.pwr.speakit.commands.PlayMusicCommand;
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

        checkPermissions();

    }

    private void checkPermissions() {
        //ugly way, cause it wont wait for a request, but what the hell
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
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
                    // ustawiam wypowiedziane słowo
                    mCommandGeneratorAsyncTask.setCommandString(mRecognizedText);
                    // wywołuje generowanie poleceń (jakie podać obiekty??)
                    mCommandGeneratorAsyncTask.mCommandGeneratorAsyncTask(null);
                    // czekam????
                    // switch z działaniem
                    doCommand();
                }
                break;
            }
        }
    }

    private void doCommand() {
        List<CommandDO> commandDOList = mCommandGeneratorAsyncTask.getmCommandList();
        for(int i = 0; i<commandDOList.size(); i++) {
            String verb = commandDOList.get(i).getVerb();
            String subject = commandDOList.get(i).getSubject();
            if (verb.equals("dzwonić")) {
                if (isNumeric(subject)) {
                    CallCommand.makeCall(MainActivity.this, subject);
                } else  {
                    // commandDOList.get(i).getSubject() jest kontaktem
                    Log.i(subject + " to kontakt nie numer");
                }
            } else if ("pisać") {
                if (isNumeric(subject)) {
                    SmsCommand.sendSms(MainActivity.this, subject, commandDOList.get(i).getContents());
                } else  {
                    // commandDOList.get(i).getSubject() jest kontaktem
                    Log.i(subject + " to kontakt nie numer");
                }
            } else if ("włączyć") {
                LaunchAppCommand.launchApp(MainActivity.this, subject);
            } else if ("dojechać") {
                Log.i("Rozpoznano: dojechać " + subject);
            } else if ("grać") {
                new PlayMusicCommand(MainActivity.this).playSpecificSong(subject);
            } else if ("wyłączyć") {
                Log.i("Rozpoznano: wyłączyć " + subject);
            }
        }
    }

    private boolean isNumeric(String str) {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
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
        mRecognizedText = "chrome";
        Thread launchThread = new Thread() {
            @Override
            public void run() {
                LaunchAppCommand.launchApp(MainActivity.this, mRecognizedText);
            }
        };
        launchThread.start();
    }

    public void playSpecificMusic(View view){
        PlayMusicCommand playMusicCommand = new PlayMusicCommand(this);
        playMusicCommand.playSpecificSong("song");

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
