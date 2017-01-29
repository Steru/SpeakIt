package pl.edu.pwr.speakit.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pwr.speakit.morfeusz.MorfeuszService;
import pl.edu.pwr.speakit.morfeusz.MorfeuszWordDO;
import pl.edu.pwr.speakit.morfeusz.IAsyncMorfeuszResponse;
import pl.edu.pwr.speakit.morfeusz.MorfeuszResponseParser;

public class CommandGenerator extends AsyncTask implements Response.Listener<String> {
    private static final String TAG = "CommandGenerator";
    private String morfeuszURL = "http://sgjp.pl/morfeusz/demo/?text=";
	private MorfeuszService mMorfeuszService;
	private MainWordService mMainWordService;
    private String mCommandString;
    private List<CommandDO> mCommandList;
    private Context mContext;

    public IAsyncMorfeuszResponse delegate = null;

    public CommandGenerator(Context context){
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        mMorfeuszService = new MorfeuszService();
        mMainWordService = new MainWordService();
        if(mCommandString.isEmpty()){
            //throw something
            Log.d(TAG, "empty cmd string");
        } else {
            try {
                generateCommand();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // used to handle the Volley http request
    @Override
    public void onResponse(String response) {
        Log.d(TAG, "response = " + response);
        List<MorfeuszWordDO> morfeuszWordList = new MorfeuszResponseParser()
                .parseFromHTML(Jsoup.parse(response));
        List<WordWithSpeechDO> wordWithSpeechList = this.generateWordWithSpeechList(morfeuszWordList);
        mCommandList = createCommandList(wordWithSpeechList);
        delegate.responseFinished(mCommandList);
    }

    public void generateCommand() throws IOException {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        Log.d(TAG, "start a request");
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                prepareRequestURL(mCommandString),
                this,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", "Ruh-roh. Error on the server end.");
                    }
        });
        queue.add(stringRequest);
	}

    private List<WordWithSpeechDO> generateWordWithSpeechList(List<MorfeuszWordDO> morfeuszWordList) {
        List<WordWithSpeechDO> morfeuszWordDOList = new ArrayList<>();
        for(MorfeuszWordDO word : morfeuszWordList) {
            WordWithSpeechDO wordWithSpeechDO = this.generateWordWithSpeech(word);
            if(wordWithSpeechDO != null) {
                morfeuszWordDOList.add(wordWithSpeechDO);
            }
        }
        return morfeuszWordDOList;
    }

    private WordWithSpeechDO generateWordWithSpeech(MorfeuszWordDO morfeuszWordDO) {
        if(morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.VERB)) {
            String mainWord = mMainWordService.getMainWord(morfeuszWordDO.getCoreWord());
            if(mainWord != null) {
                return new WordWithSpeechDO(mainWord, morfeuszWordDO.getPartOfSpeech(), morfeuszWordDO.getOriginalString());
            } else {
                return null;
            }
        } else {
            if(!morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.ANOTHER)) {
                return new WordWithSpeechDO(morfeuszWordDO.getCoreWord(), morfeuszWordDO.getPartOfSpeech(), morfeuszWordDO.getOriginalString());
            } else {
                return null;
            }
        }
    }

	private List<CommandDO> createCommandList(List<WordWithSpeechDO> wordWithSpeechList) {
		List<WordWithSpeechDO> verbList = generateVerbList(wordWithSpeechList);
		List<WordWithSpeechDO> substantiveList = generateSubstantiveAndNumberList(wordWithSpeechList);
		return joinVerbAndSubstantive(verbList, substantiveList);
	}

	private List<WordWithSpeechDO> generateVerbList(List<WordWithSpeechDO> wordWithSpeechList) {
        List<WordWithSpeechDO> wordWithSpeechDOList = new ArrayList<>();
        for(WordWithSpeechDO word : wordWithSpeechList) {
            if(word.getPartOfSpeech().equals(PartOfSpeech.VERB)) {
                wordWithSpeechDOList.add(word);
            }
        }
		return wordWithSpeechDOList;
	}

	private List<WordWithSpeechDO> generateSubstantiveAndNumberList(List<WordWithSpeechDO> wordWithSpeechList) {
        List<WordWithSpeechDO> wordWithSpeechDOList = new ArrayList<>();
        for(WordWithSpeechDO word : wordWithSpeechList) {
            if(!word.getPartOfSpeech().equals(PartOfSpeech.VERB)) {
                wordWithSpeechDOList.add(word);
            }
        }
        return wordWithSpeechDOList;
	}

    private List<CommandDO> joinVerbAndSubstantive(List<WordWithSpeechDO> verbList, List<WordWithSpeechDO> substantiveList) {
        List<CommandDO> commandList = new ArrayList<CommandDO>();
        for(int verbIndex = 0; verbIndex<verbList.size(); verbIndex++) {
            CommandDO command = generateCommand(verbList.get(verbIndex), substantiveList);
            if (command != null) {
                commandList.add(command);
            }
        }
        return commandList;
    }

    private CommandDO generateCommand(WordWithSpeechDO verb, List<WordWithSpeechDO> substantiveList) {
        if (substantiveList.size() > 0) {
            WordWithSpeechDO addresser = null;
            if (verb.getWord().equals("pisać")) {
                for(WordWithSpeechDO word : substantiveList) {
                    if(word.getPartOfSpeech().equals(PartOfSpeech.NAME) || word.getPartOfSpeech().equals(PartOfSpeech.NUMERAL)) {
                        addresser = word;
                    }
                }
                if (addresser != null) {
                    String message = generateSMSMessage(verb.getBaseString(), addresser.getWord());
                    return new CommandDO(verb.getWord(), addresser.getWord(), message);
                } else {
                    return new CommandDO(verb.getWord(), substantiveList.get(0).getWord(), "");
                }

            } else {
                return new CommandDO(verb.getWord(), substantiveList.get(0).getWord(), "");
            }
        } else {
            return null;
        }
    }

	private String generateSMSMessage(String baseString, String addressName) {
		String message = baseString.split(addressName.substring(0, addressName.length()))[1];
		while(message.charAt(0) != ' ') {
			message = message.substring(1, message.length());
		}
		return message.substring(1, message.length());
	}

    public void setCommandString(String commandString) {
        mCommandString = commandString;
    }

    private String prepareRequestURL(String stringWithAttributes) {
        String sb = this.morfeuszURL +
                stringWithAttributes.replaceAll(" ", "+");
        return sb;
    }
}
