package morfeusz;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MorfeuszResponseRequest {

	private String morfeuszURL = "http://sgjp.pl/morfeusz/demo/?text=";
	
	public Document readMorfeuszResponse(String wordsToSend) throws IOException {
		String requestURL = this.prepareRequestURL(wordsToSend);
		Document document = Jsoup.connect(requestURL).get();
		return document;
	}
	
	private String prepareRequestURL(String stringWithAttributes) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.morfeuszURL);
		sb.append(stringWithAttributes.replaceAll(" ", "+"));
		return sb.toString();
	}	
}
