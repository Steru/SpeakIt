package pl.edu.pwr.speakit;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.edu.pwr.speakit.commands.CallCommand;
import pl.edu.pwr.speakit.commands.DriveCommand;
import pl.edu.pwr.speakit.commands.LaunchAppCommand;
import pl.edu.pwr.speakit.commands.PlayMusicCommand;
import pl.edu.pwr.speakit.commands.SmsCommand;
import pl.edu.pwr.speakit.common.CommandDO;
import pl.edu.pwr.speakit.common.CommandGenerator;
import pl.edu.pwr.speakit.morfeusz.IAsyncMorfeuszResponse;

public class MainActivity extends AppCompatActivity implements IAsyncMorfeuszResponse {
    private static final String TAG = "MainActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mRecognizedTextView;
    private String mRecognizedText = "init";
    private CommandGenerator mCommandGenerator = new CommandGenerator(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecognizedTextView = (TextView) findViewById(R.id.recognized_text);

        checkPermissions();

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
                    executeCommandGeneration(mRecognizedText);
                }
                break;
            }
        }
    }

    private void doCommand() {
        List<CommandDO> commandDOList = mCommandGenerator.getmCommandList();
        for(int i = 0; i<commandDOList.size(); i++) {
            String verb = commandDOList.get(i).getVerb();
            String subject = commandDOList.get(i).getSubject();
            switch (verb) {
                case "dzwonić":
                    if (isNumeric(subject)) {
                        CallCommand.makeCall(MainActivity.this, subject);
                    } else {
                        // commandDOList.get(i).getSubject() jest kontaktem
                        Log.i(TAG, subject + " to kontakt nie numer");
                    }
                    break;
                case "pisać":
                    if (isNumeric(subject)) {
                        SmsCommand.sendSms(MainActivity.this, subject, commandDOList.get(i).getContents());
                    } else {
                        // commandDOList.get(i).getSubject() jest kontaktem
                        Log.i(TAG, subject + " to kontakt nie numer");
                    }
                    break;
                case "włączyć":
                    LaunchAppCommand.launchApp(MainActivity.this, subject);
                    break;
                case "dojechać":
                    Log.i(TAG, "Rozpoznano: dojechać " + subject);
                    DriveCommand.go(this, subject);
                    break;
                case "grać":
                    new PlayMusicCommand(MainActivity.this).playSpecificSong(subject);
                    break;
                case "wyłączyć":
                    Log.i(TAG, "Rozpoznano: wyłączyć " + subject);
                    break;
            }
        }
    }

    private boolean isNumeric(String str) {
        for (char c : str.toCharArray())        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private void executeCommandGeneration(String val) {
        if(isOnline()) {
            mCommandGenerator.setCommandString(val);
            mCommandGenerator.run();
        } else {
            toastIt(getString(R.string.error_msg_no_internet));
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

    @Override
    public void responseFinished(List<CommandDO> commandList) {
        if(commandList != null) {
            if(commandList.isEmpty()){
                runOnUiThread(new Runnable() { // oh lord
                    @Override
                    public void run() {       // why
                        Toast.makeText(MainActivity.this,
                                "Niepowodzenie rozpoznania komendy.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Log.d(TAG, "zawartość = " + commandList);
                doCommand();
            }
        }
        else {
            Log.d(TAG, "cmdList empty");
        }
    }

    private boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void toastIt(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
}
