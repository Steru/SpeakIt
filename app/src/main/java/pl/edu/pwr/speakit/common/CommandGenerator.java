package pl.edu.pwr.speakit.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pwr.speakit.morfeusz.MorfeuszService;
import pl.edu.pwr.speakit.morfeusz.MorfeuszWordDO;

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
            if(word != null) {
                morfeuszWordDOList.add(word);
            }
        }
		return morfeuszWordDOList;
	}
	
	private WordWithSpeechDO generateWordWithSpeech(MorfeuszWordDO morfeuszWordDO) {
		if(!morfeuszWordDO.equals(PartOfSpeech.NUMERAL)) {
			String mainWord = mainWordService.getMainWord(morfeuszWordDO.getCoreWord());
			if(mainWord != null) {
				if(morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.VERB) || morfeuszWordDO.getPartOfSpeech().equals(PartOfSpeech.SUBSTANTIVE)) {
					return new WordWithSpeechDO(mainWord, morfeuszWordDO.getPartOfSpeech());
				} else {
					if(mainWord.charAt(mainWord.length()-1) == 'ć') {
						return new WordWithSpeechDO(mainWord, PartOfSpeech.VERB);
					} else {
						return new WordWithSpeechDO(mainWord, PartOfSpeech.SUBSTANTIVE);
					}
				}
			} else {
				return null;
			}
		} else {
			return new WordWithSpeechDO(morfeuszWordDO.getCoreWord(), morfeuszWordDO.getPartOfSpeech());
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
            if(word.getPartOfSpeech().equals(PartOfSpeech.SUBSTANTIVE) || word.getPartOfSpeech().equals(PartOfSpeech.NUMERAL)) {
                wordWithSpeechDOList.add(word);
            }
        }
        return wordWithSpeechDOList;
	}
	
	private List<CommandDO> joinVerbAndSubstantive(List<WordWithSpeechDO> verbList, List<WordWithSpeechDO> substantiveList) {
		List<CommandDO> commandList = new ArrayList<CommandDO>();
		for(int verbIndex = 0; verbIndex<verbList.size(); verbIndex++) {
			for(int subIndex = 0; subIndex<substantiveList.size(); subIndex++) {
				commandList.add(new CommandDO(verbList.get(verbIndex).getWord(), substantiveList.get(subIndex).getWord()));
			}
		}
		return commandList;
	}
	
	public static void main(String[] args) throws IOException {
		CommandGenerator commandGenerator = new CommandGenerator();
		List<CommandDO> commandDOList = commandGenerator.generateCommand("proszę odpal mi przeglądarki");
		System.out.println(commandDOList);
	}
}
