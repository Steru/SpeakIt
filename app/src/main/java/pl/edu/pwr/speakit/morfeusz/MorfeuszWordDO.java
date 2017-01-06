package pl.edu.pwr.speakit.morfeusz;

import pl.edu.pwr.speakit.common.PartOfSpeech;

public class MorfeuszWordDO {

	private String mainWord;
	private String coreWord;
	private PartOfSpeech partOfSpeech;
	
	
	
	public MorfeuszWordDO() {
		super();
	}

	public MorfeuszWordDO(String mainWord, String coreWord, PartOfSpeech partOfSpeech) {
		super();
		this.mainWord = mainWord;
		this.coreWord = coreWord;
		this.partOfSpeech = partOfSpeech;
	}
	
	public String getMainWord() {
		return mainWord;
	}
	public void setMainWord(String mainWord) {
		this.mainWord = mainWord;
	}
	public String getCoreWord() {
		return coreWord;
	}
	public void setCoreWord(String coreWord) {
		this.coreWord = coreWord;
	}
	public PartOfSpeech getPartOfSpeech() {
		return partOfSpeech;
	}
	public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coreWord == null) ? 0 : coreWord.hashCode());
		result = prime * result + ((mainWord == null) ? 0 : mainWord.hashCode());
		result = prime * result + ((partOfSpeech == null) ? 0 : partOfSpeech.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MorfeuszWordDO other = (MorfeuszWordDO) obj;
		if (coreWord == null) {
			if (other.coreWord != null)
				return false;
		} else if (!coreWord.equals(other.coreWord))
			return false;
		if (mainWord == null) {
			if (other.mainWord != null)
				return false;
		} else if (!mainWord.equals(other.mainWord))
			return false;
		if (partOfSpeech != other.partOfSpeech)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MorfeuszWord: " + mainWord + " => " + coreWord + " : " + partOfSpeech;
	}
	
	
	
}
