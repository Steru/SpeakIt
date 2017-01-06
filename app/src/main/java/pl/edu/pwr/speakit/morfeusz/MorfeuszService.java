package pl.edu.pwr.speakit.morfeusz;

import java.io.IOException;
import java.util.List;

public class MorfeuszService {
	
	public List<MorfeuszWordDO> readMorfeuszWordList(String words) throws IOException {
		return new MorfeuszResponseParser().parseFromHTML(
				new MorfeuszResponseRequest().readMorfeuszResponse(words));
	}

}
