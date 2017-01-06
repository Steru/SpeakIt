package pl.edu.pwr.speakit.common;

public class WordWithSpeechDO {

	private String word;
	private PartOfSpeech partOfSpeech;
	
	public WordWithSpeechDO() {
		super();
	}
	
	public WordWithSpeechDO(String word, PartOfSpeech partOfSpeech) {
		super();
		this.word = word;
		this.partOfSpeech = partOfSpeech;
	}
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
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
		result = prime * result + ((partOfSpeech == null) ? 0 : partOfSpeech.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
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
		WordWithSpeechDO other = (WordWithSpeechDO) obj;
		if (partOfSpeech != other.partOfSpeech)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return word + " : " + partOfSpeech;
	}
}
