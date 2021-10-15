/**
 * 
 */
package utils.parse;

import org.apache.commons.lang.StringUtils;

/**
 * @author timo
 * 
 */
public class Toker {
	protected final Input input;
	protected final Terminator[] terminators;
	protected final Literal[] literals;
	protected final Grouper[] groupers;
	protected String quoteCloser = null;
	protected final boolean includeGroupers;
	protected String current;

	/**
	 * @param input
	 */
	public Toker(Input input, Terminator[] terminators, Literal[] literals,
			String[] quoteEnclosers, boolean includeQuoteClosers) {
		super();
		this.input = input;
		this.terminators = terminators;
		this.literals = literals;
		this.groupers = new Grouper[quoteEnclosers.length];
		this.includeGroupers = includeQuoteClosers;
		for (int g = 0; g < quoteEnclosers.length; g++) {
			this.groupers[g] = new Grouper(quoteEnclosers[g].substring(0,1), quoteEnclosers[g].substring(1,2));
		}
	}

	/**
	 * @param curr
	 * @return
	 * 
	 * Side Effect: Sets quoteCloser when result is true 
	 */
	protected boolean isEnclosureStarter(String curr) {
		for (Grouper g : groupers) {
			if (curr.equals(g.getOpener())) {
				quoteCloser = g.getCloser();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return next token
	 */
	public Token next() throws Exception {
		current = input.peek();

		if (current == null) {
			return createEOFToken();
		}

		while (StringUtils.isWhitespace(current)) {
			input.next();
			current = input.peek();
		}

		String toked = "";
		boolean enclosed = isEnclosureStarter(current);
		while (!terminate(toked, current, enclosed)) {
			toked += current;
			input.next();
			current = input.peek();
		}
		if (enclosed && !current.equals(quoteCloser)) {
			throw new Exception("Unterminated quote");
		}
		if (enclosed || shouldIncludeTerminator(toked, current)) {
			toked += current;
		}
		if (enclosed || shouldConsumeTerminator(toked, current)) {
			input.next();
		}

		return createToken(toked, current);
	}

	/**
	 * @param terminator
	 * @return
	 */
	protected boolean isTerminatorToInclude(String terminator) {
		for (int t = 0; t < terminators.length; t++) {
			if (terminators[t].getText().equals(terminator)) {
				return terminators[t].isInclude();
			}
		}
		return false;
	}
	
	/**
	 * @param toked
	 * @param s
	 * @return
	 */
	protected boolean shouldIncludeTerminator(String toked, String s) {
		return (toked.equals("") && isLiteral(s)) || isTerminatorToInclude(s);
		
		/*if (toked.equals("")) {
			return isLiteral(s);
		} else {
			return " ".equals(s);
		}*/
	}

	/**
	 * @param toked
	 * @param s
	 * @return true if s should be consumed
	 */
	protected boolean shouldConsumeTerminator(String toked, String s) {
		return !isLiteral(s) || toked.endsWith(s);
	}

	/**
	 * @param s
	 * @return true is s is a literal.
	 */
	protected boolean isLiteral(String s) {
		for (int l = 0; l < literals.length; l++) {
			if (literals[l].getText().equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param s
	 * @return true is s is a terminator.
	 */
	protected boolean isTerminator(String s) {
		for (int t = 0; t < terminators.length; t++) {
			if (terminators[t].getText().equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param inputStr
	 * @return
	 */
	protected boolean terminate(String tokenStr, String inputStr,
			boolean isEnclosed) {
		boolean result = inputStr == null;
		if (!result) {
			if (isEnclosed) {
				result = !"".equals(tokenStr) && inputStr.equals(quoteCloser);
			} else {
				result = StringUtils.isWhitespace(inputStr)
						/*|| isLiteral(tokenStr) */|| isTerminator(inputStr);
			}
		}
		return result;
	}

	/**
	 * @return EOF token
	 */
	protected Token createEOFToken() {
		return new Token("EOF", "EOF", null);
	}

	/**
	 * @param tokeStr
	 * @return
	 */
	protected Token createToken(String tokeStr, String terminatedBy) {
		for (int l = 0; l < literals.length; l++) {
			if (literals[l].match(tokeStr)) {
				//if (literals[l].getParsedText().startsWith("Number")) {
					return new Token(literals[l].getParsedText(), tokeStr, terminatedBy);
				//} else
				//return new Token(tokeStr, literals[l].getParsedText());
			}
		}

		// now for some cheap ugly hacky dirty work
		if (tokeStr.length() == 1) {
			return new Token("Str1", tokeStr, terminatedBy);
		}

		/*
		 * if (tokeStr.length() == 3 && tokeStr.equals(tokeStr.toUpperCase())) {
		 * return new Token("UpStr3", tokeStr); }
		 */
		
		//Quick trick. Probably not the best place to do this
		String fixedTokeStr = tokeStr;
		if (" ".equals(terminatedBy)) {
			fixedTokeStr += " ";
		}
			
		if (!includeGroupers) {
			fixedTokeStr = fixedTokeStr.substring(1,fixedTokeStr.length()-1);
		}
		return new Token("Str", fixedTokeStr, terminatedBy);
	}

	/**
	 * @return next token without advancing position
	 */
	public Token peek() throws Exception {
		int mark = input.getCursor();
		Token result = next();
		input.setCursor(mark);
		return result;
	}

	/**
	 * get current input cursor
	 */
	public int getCursor() {
		return input.getCursor();
	}

	/**
	 * set input cursor
	 */
	public void setCursor(int cursor) {
		input.setCursor(cursor);
	}
}
