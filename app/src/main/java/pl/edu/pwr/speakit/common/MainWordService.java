package pl.edu.pwr.speakit.common;

import java.util.HashMap;
import java.util.Map;

public class MainWordService {
	
	private Map<String, String> usedAndMainWordsMap = new HashMap<>();
	
	public MainWordService() {
		// czasowniki
			// włączyć
		usedAndMainWordsMap.put("włączyć", "włączyć");
		usedAndMainWordsMap.put("odpalić", "włączyć");
		usedAndMainWordsMap.put("uruchomić", "włączyć");
		usedAndMainWordsMap.put("puścić", "włączyć");
		
			// wyłączyć
		usedAndMainWordsMap.put("wyłączyć", "wyłączyć");
		usedAndMainWordsMap.put("zamknąć", "wyłączyć");
		usedAndMainWordsMap.put("wyłączyć", "wyłączyć");
		usedAndMainWordsMap.put("wyłączyć", "wyłączyć");
		
			// zadzwoń
		usedAndMainWordsMap.put("dzwonić", "dzwonić");
		usedAndMainWordsMap.put("zadzwonić", "dzwonić");
		usedAndMainWordsMap.put("połączyć", "dzwonić");
		usedAndMainWordsMap.put("łączyć", "dzwonić");
		
			// wyślij
		usedAndMainWordsMap.put("pisać", "pisać");
		usedAndMainWordsMap.put("napisać", "pisać");
		
//		
//		switrz(czasownik){
//			return CallCommand(rzeczownik);
//			
//		}
//		
//		class CallCommand extends CommandDO{
//			
//		}
		// rzeczowniki
			// przeglądarka
		usedAndMainWordsMap.put("przeglądarka", "przeglądarka");
		usedAndMainWordsMap.put("przeglądareczka", "przeglądarka");
		
		usedAndMainWordsMap.put("bluetooth", "bluetooth");
	}
	
	public String getMainWord(String word) {
		return this.usedAndMainWordsMap.get(word);
	}

}
