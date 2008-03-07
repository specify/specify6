package edu.ku.brc.ui.forms.formatters;

/**
 * This class describes checked exception used to indicate that an invalid formatting code has been provided.
 * 
 * @author ricardo
 *
 * @code_status Alpha
 *
 * Created Date: Mar 05, 2008
 *
 */

public class UIFieldFormatterParsingException extends Exception 
{
	private String faultyFormat; 
	
	/*
	 * Constructor
	 */
	public UIFieldFormatterParsingException(String message, String faultyFormat) 
	{
		super(message);
		this.faultyFormat = faultyFormat;
	}

	public String getFaultyFormat() {
		return faultyFormat;
	}
}
