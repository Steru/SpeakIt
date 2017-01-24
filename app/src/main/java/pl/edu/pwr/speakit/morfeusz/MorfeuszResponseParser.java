package pl.edu.pwr.speakit.morfeusz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.edu.pwr.speakit.common.PartOfSpeech;

public class MorfeuszResponseParser {
	
	private String baseString;
	
	public List<MorfeuszWordDO> parseFromHTML(Document document) {
		//TODO
		//setBaseString(document);
		Element tbodyElement = document.select("tbody").first();
		Elements trElements = tbodyElement.select("tr");
        List<MorfeuszWordDO> morfeuszWordDOList = new ArrayList<>();
        for(Element e : trElements) {
            MorfeuszWordDO morfeuszWordDO = this.createMorfeuszWordFromTRElement(e);
            if(morfeuszWordDO != null) {
                morfeuszWordDOList.add(morfeuszWordDO);
            }
        }
		return morfeuszWordDOList;
	}
	
	private void setBaseString(Document document) {
		baseString = "";
		System.out.println(document.baseUri().toString().split("text=")[1]);
		String[] urlBuff = document.baseUri().toString().split("text=")[1].split(Pattern.quote("+"));
		for(String urlElement : urlBuff) {
			baseString += urlElement + " ";
		}
		baseString = baseString.substring(0, baseString.length()-1);
	}
	
	private MorfeuszWordDO createMorfeuszWordFromTRElement(Element trElement) {
		Elements tdElements = trElement.select("td");
		String mainWord = tdElements.get(2).text();
		MorfeuszWordDO morfeuszWord = null;
		// TODO[AKO]: sprawdzić krótsze słowa
		if(mainWord.length() > 2) {
			morfeuszWord = new MorfeuszWordDO(mainWord,
					this.parseNewWord(tdElements.get(3)),
					generateWordType(tdElements),
					baseString);
		}
		return morfeuszWord;
	}
	
	private PartOfSpeech generateWordType(Elements tdElements) {
		return tdElements.get(5).text().equals("imię") ? PartOfSpeech.NAME :
				tdElements.get(5).text().equals("nazwa geograficzna") ? PartOfSpeech.LOKALIZATION :
						this.parseWordType(tdElements.get(4).text());
	}
	
	private String parseNewWord(Element newNotParseWord) {
		return newNotParseWord.select("b").text().indexOf(':') == -1 ? 
				newNotParseWord.select("b").text() : newNotParseWord.select("b").text().split(":")[0];
	}
	
	private PartOfSpeech parseWordType(String notParsePartOfSpeech) {
		String[] options = notParsePartOfSpeech.indexOf(":") == -1 ? 
			(String[]) Arrays.asList(notParsePartOfSpeech).toArray() : notParsePartOfSpeech.split(":");
		if(this.isVerb(options)) {
			return PartOfSpeech.VERB;
		} else if(this.isSubs(options)) {
			return PartOfSpeech.SUBSTANTIVE;
		}else if(this.findInArray("num", options)) {
			return PartOfSpeech.NUMERAL;
		} else if(this.findInArray("ign", options)) {
			return PartOfSpeech.IGNORE;
		}else {
			return PartOfSpeech.ANOTHER;
		}
	}
	
	private boolean isSubs(String[] options) {
		return this.findInArray("subst", options) ||
				this.findInArray("depr", options);
	}
	
	private boolean isVerb(String[] options) {
		return this.findInArray("fin", options) || 
				this.findInArray("bedzie", options) ||
				this.findInArray("aglt", options) ||
				this.findInArray("praet", options) ||
				this.findInArray("impt", options) ||
				this.findInArray("imps", options) ||
				this.findInArray("inf", options) ||
				this.findInArray("pcon", options) ||
				this.findInArray("pant", options) ||
				this.findInArray("ger", options) ||
				this.findInArray("pact", options) ||
				this.findInArray("ppas", options);
	}
	
	private boolean findInArray(String value, String[] array) {
		boolean find = false;
		for (String string : array) {
			if(string.equals(value)) {
				find = true;
			}
		}
		return find;
	}
	
}
