package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import morfeusz.MorfeuszService;
import morfeusz.MorfeuszWordDO;

public class CommandGenerator {
	
	private MorfeuszService morfeuszService;
	private MainWordService mainWordService;
	
	public CommandGenerator() {
		morfeuszService = new MorfeuszService();
		mainWordService = new MainWordService();
	}
	
	public List<CommandDO> generateCommand(String commandString) throws IOException {
		List<MorfeuszWordDO> morfeuszWordList = morfeuszService.readMorfeuszWordList(commandString);
		List<WordWithSpeechDO> wordWithSpeechList = this.generateWordWithSpeechList(morfeuszWordList);
		return createCommandList(wordWithSpeechList);
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
		if(morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.VERB) || morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.IGNORE)) {
			String mainWord = mainWordService.getMainWord(morfeuszWordDO.getCoreWord());
			if(mainWord != null) {
				if(morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.VERB)) {
					return new WordWithSpeechDO(mainWord, morfeuszWordDO.getPartOfSpeech(), morfeuszWordDO.getOriginalString());
				} else {
					if(mainWord.charAt(mainWord.length()-1) == 'ć') {
						return new WordWithSpeechDO(mainWord, PartOfSpeech.VERB, morfeuszWordDO.getOriginalString());
					} else {
						return new WordWithSpeechDO(mainWord, PartOfSpeech.SUBSTANTIVE, morfeuszWordDO.getOriginalString());
					}
				}
			} else {
				return null;
			}
		} else {
			return new WordWithSpeechDO(morfeuszWordDO.getCoreWord(), morfeuszWordDO.getPartOfSpeech(), morfeuszWordDO.getOriginalString());
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
			commandList.add(generateCommand(verbList.get(verbIndex), substantiveList));
		}
		return commandList;
	}
	
	private CommandDO generateCommand(WordWithSpeechDO verb, List<WordWithSpeechDO> substantiveList) {
		WordWithSpeechDO addresser = null;
		if (verb.getWord().equals("pisać")) {
			for(WordWithSpeechDO word : substantiveList) {
				if(word.getPartOfSpeech().equals(PartOfSpeech.NAME) || word.getPartOfSpeech().equals(PartOfSpeech.NUMERAL)) {
					addresser = word;
				}
			}
			String message = generateSMSMessage(verb.getBaseString(), addresser.getWord());
			return new CommandDO(verb.getWord(), addresser.getWord(), message);
		} else {
			return new CommandDO(verb.getWord(), substantiveList.get(0).getWord(), "");
		}
	}
	
	private String generateSMSMessage(String baseString, String addressName) {
		String message = baseString.split(addressName.substring(0, addressName.length()))[1];
		while(message.charAt(0) != ' ') {
			message = message.substring(1, message.length());
		}
		return message.substring(1, message.length());
	}
	
	public static void main(String[] args) throws IOException {
		CommandGenerator commandGenerator = new CommandGenerator();
		List<CommandDO> commandDOList = commandGenerator.generateCommand("Napisz do Andrzeja Cześć kolego");
		System.out.println(commandDOList);
	}
}
