package pl.edu.pwr.speakit.common;

public class CommandDO {
	
	private String verb;
	private String subject;
	private String contents;
	
	public CommandDO() {
		super();
	}
	
	public CommandDO(String verb, String subject) {
		super();
		this.verb = verb;
		this.subject = subject;
	}
	
	public CommandDO(String verb, String subject, String contents) {
		super();
		this.verb = verb;
		this.subject = subject;
		this.contents = contents;
	}

	public String getVerb() {
		return verb;
	}
	
	public void setVerb(String verb) {
		this.verb = verb;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contents == null) ? 0 : contents.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((verb == null) ? 0 : verb.hashCode());
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
		CommandDO other = (CommandDO) obj;
		if (contents == null) {
			if (other.contents != null)
				return false;
		} else if (!contents.equals(other.contents))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (verb == null) {
			if (other.verb != null)
				return false;
		} else if (!verb.equals(other.verb))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CommandDO [verb=" + verb + ", subject=" + subject + ", contents=" + contents + "]";
	}	
}
