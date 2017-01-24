package common;

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
		
			// dzwonić
		usedAndMainWordsMap.put("dzwonić", "dzwonić");
		usedAndMainWordsMap.put("zadzwonić", "dzwonić");
		usedAndMainWordsMap.put("połączyć", "dzwonić");
		usedAndMainWordsMap.put("łączyć", "dzwonić");
		
			// pisać
		usedAndMainWordsMap.put("pisać", "pisać");
		usedAndMainWordsMap.put("napisać", "pisać");
		usedAndMainWordsMap.put("wysłać", "pisać");
		
			// dojechać
		usedAndMainWordsMap.put("dojechać", "dojechać");
		usedAndMainWordsMap.put("przyjechać ", "dojechać");
		usedAndMainWordsMap.put("zajechać", "dojechać");
		usedAndMainWordsMap.put("przybyć", "dojechać");
		
			// grać
		usedAndMainWordsMap.put("grać", "grać");
		usedAndMainWordsMap.put("puść", "grać");
		usedAndMainWordsMap.put("odtwórz", "grać");
		
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
//		usedAndMainWordsMap.put("przeglądarka", "przeglądarka");
//		usedAndMainWordsMap.put("przeglądareczka", "przeglądarka");
//		
//		usedAndMainWordsMap.put("bluetooth", "bluetooth");
//		
//		usedAndMainWordsMap.put("lokalizacja", "lokalizacja")
	}
	
	public String getMainWord(String word) {
		return this.usedAndMainWordsMap.get(word);
	}

}
