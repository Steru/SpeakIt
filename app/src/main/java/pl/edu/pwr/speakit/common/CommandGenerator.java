package pl.edu.pwr.speakit.common;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pwr.speakit.morfeusz.MorfeuszWordDO;
import pl.edu.pwr.speakit.morfeusz.IAsyncMorfeuszResponse;
import pl.edu.pwr.speakit.morfeusz.MorfeuszResponseParser;

public class CommandGenerator {
    private static final String TAG = "CommandGenerator";
    private static final String morfeuszURL = "http://sgjp.pl/morfeusz/demo/?text=";
	private MainWordService mMainWordService;
    private String mCommandString;
    private List<CommandDO> mCommandList;

    public IAsyncMorfeuszResponse delegate = null;

    public void run() {
        mMainWordService = new MainWordService();
        if(mCommandString.isEmpty()){
            //throw something
            Log.d(TAG, "empty cmd string");
        } else {
            tryToGetSomething();
        }
    }

    public void parseResponse(String response) {
        List<MorfeuszWordDO> morfeuszWordList = new MorfeuszResponseParser()
                .parseFromHTML(Jsoup.parse(response));
        List<WordWithSpeechDO> wordWithSpeechList = this.generateWordWithSpeechList(morfeuszWordList);
        mCommandList = createCommandList(wordWithSpeechList);
        delegate.responseFinished(mCommandList);
    }

    private void tryToGetSomething() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String urlString = prepareRequestURL(mCommandString);

                URL url = null;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                HttpURLConnection urlConnection = null;
                StringBuilder sb = new StringBuilder();
                try {
                    Log.i("Morfeusz HttpGet",urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader isr = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = isr.readLine()) != null) {
                        Log.d("Morfeusz HttpGet", "line = " + line);
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }

                Log.i("Morfeusz HttpResponse",sb.toString());
                parseResponse(sb.toString());
                return null;
            }
        }.execute();
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
        String[] messageArray = baseString.split(addressName);
        System.out.println(addressName);
        if(messageArray.length > 1) {
            String message = messageArray[1];
            while(message.charAt(0) != ' ') {
                message = message.substring(1, message.length());
            }
            if(message.length() > 0) {
                return message.substring(1, message.length());
            } else {
                return "";
            }

        }
        return "";
    }

    public void setCommandString(String commandString) {
        mCommandString = commandString;
    }

    public List<CommandDO> getmCommandList() {
        return mCommandList;
    }

    private String prepareRequestURL(String stringWithAttributes) {
        String sb = this.morfeuszURL +
                stringWithAttributes.replaceAll(" ", "+");
        return sb;
    }
}
