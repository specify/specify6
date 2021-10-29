/**
 * 
 */
package utils.parse;


/**
 * @author timo
 *
 */
public class Input 
{
	protected final String inputStr;
	protected int cursor = 0;
	
	/**
	 * @param inputStr
	 */
	public Input(String inputStr)
	{
		this.inputStr = inputStr;
	}
	
	/**
	 * set the current position
	 */
	public void setCursor(int cursor)
	{
		this.cursor = cursor;
	}
	
	/**
	 * get the current position
	 */
	public int getCursor() 	
	{
		return cursor;
	}
	
	/**
	 * @return next character and advance cursor
	 */
	public String next()
	{
		if (cursor < inputStr.length())
		{
			return inputStr.substring(cursor, ++cursor);
		}
		return null;
	}
	
	/**
	 * @return next character without advancing cursor
	 */
	public String peek()
	{
		if (cursor < inputStr.length())
		{
			return inputStr.substring(cursor, cursor+1);
		}
		return null;
	}
}
