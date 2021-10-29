/**
 * 
 */
package utils.parse;

/**
 * @author timo
 *
 */
public class Symbol implements Cloneable
{
	protected final String name;
	protected final boolean terminal;
	protected final boolean eof;
	protected final boolean branch;
	protected Token attribute = null;
	
	//rudimentary mapping to db 
	protected final String recordType; //AgentType, ReferenceWorkType, etc...
	protected final String tblName;
	protected final String fldName;
	
	/**
	 * @param name
	 */
	public Symbol(String name)
	{
		this(name, false, false, false, null, null, null);
	}
	
	/**
	 * @param name
	 * @param branch
	 */
	public Symbol(boolean branch)
	{
		this("branch", false, false, branch, null, null, null);
	}
	
	/**
	 * @param name
	 * @param terminal
	 */
	public Symbol(String name, boolean terminal)
	{
		this(name, terminal, false, false, null, null, null);
	}
	
	
	/**
	 * @param name
	 * @param terminal
	 * @param tblName
	 * @param fldName
	 */
	public Symbol(String name, String tblName, String fldName) 
	{
		this(name, false, false, false, tblName, fldName, null);
	}

	/**
	 * @param name
	 * @param terminal
	 * @param eof
	 */
	public Symbol(String name, boolean terminal, boolean eof, boolean branch,
			String tblName, String fldName, String recordType) 
	{
		super();
		this.name = name;
		this.terminal = terminal;
		this.eof = eof;
		this.branch = branch;
		this.tblName = "".equals(tblName) ? null : tblName;
		this.fldName = "".equals(fldName) ? null : fldName;
		this.recordType = "".equals(recordType) ? null : recordType;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.name.equals(((Symbol)obj).getName());
	}

	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * @return the terminal
	 */
	public boolean isTerminal() 
	{
		return terminal;
	}

	/**
	 * @return the eof
	 */
	public boolean isEof() 
	{
		return eof;
	}

	/**
	 * @return true if branch
	 */
	public boolean isBranch() 
	{
		return branch;
	}
	
	/**
	 * @param attribute
	 */
	public void setAttribute(Token attribute)
	{
		this.attribute = attribute;
	}
	
	/**
	 * @return the attribute
	 */
	public Token getAttribute()
	{
		return attribute;
	}
	
	/**
	 * @return the tblName
	 */
	public String getTblName() {
		return tblName;
	}

	/**
	 * @return the fldName
	 */
	public String getFldName() {
		return fldName;
	}

	/**
	 * @return the recordType
	 */
	public String getRecordType() {
		return recordType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		String mapping = getMapping();
		String bools = getBoolText();
		return name + " " + bools + " " + (attribute != null ? "(" + attribute + ")" : "") + ("".equals(mapping) ? "" : " [" + mapping + "]");
	}
	
	private String getBoolText() {
		return (branch ? "1" : "0") + (eof ? "1" : "0") + (terminal ? "1" : "0");
	}
	/**
	 * @return
	 */
	public String getMapping() {
		if (tblName == null && fldName == null) return "";
		if (fldName == null) return tblName;
		return tblName + "." + fldName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	
}
