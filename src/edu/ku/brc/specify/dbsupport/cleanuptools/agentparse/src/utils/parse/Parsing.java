/**
 * 
 */
package utils.parse;

import java.util.List;

/**
 * @author tnoble
 *
 */
public class Parsing implements Comparable<Parsing> {
	private final Integer key;
	private final String input;
	private final List<ParseResult> parses;
	private final List<Exception> exceptions;
	/**
	 * @param input
	 * @param parses
	 * @param exceptions
	 */
	public Parsing(Integer key, String input, List<ParseResult> parses,
			List<Exception> exceptions) {
		super();
		this.key = key;
		this.input = input;
		this.parses = parses;
		this.exceptions = exceptions;
	}
	
	
	/**
	 * @return the key
	 */
	public Integer getKey() {
		return key;
	}


	/**
	 * @return the input
	 */
	public String getInput() {
		return input;
	}
	/**
	 * @return the parses
	 */
	public List<ParseResult> getParses() {
		return parses;
	}
	
	/**
	 * @param parseIdx
	 * @param rec
	 */
	public void addRecord(int parseIdx, Record rec) {
		parses.get(parseIdx).getRecords().add(rec);
	}
	
	/**
	 * @return the exceptions
	 */
	public List<Exception> getExceptions() {
		return exceptions;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Parsing arg0) {
		/*int parses = getParses().size();
		int argParses = arg0.getParses().size();
		if (parses != argParses) {
			if (argParses == 0) {
				return -1; //unparsed at end of list
			} else if (parses < argParses) {
				return -1;
			} else {
				return 1;
			}*/
		Integer parses = getParses().size();
		Integer argParses = arg0.getParses().size();
		if (!parses.equals(argParses)) {
			return parses.compareTo(argParses);

		} else {
			return getInput().compareTo(arg0.getInput());
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Parsing) {
			Parsing arg = (Parsing)obj;
			return getParses().size() == arg.getParses().size() && getInput().equals(arg.getInput());
		}
		return false;
	}	
	
	
	
	
}
