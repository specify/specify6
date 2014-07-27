/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author timo
 *
 */
public class DarwinCoreArchiveField implements Comparable<DarwinCoreArchiveField>
{
	protected int index;
	protected String term;
	boolean id;
	
	/**
	 * @param index
	 * @param term
	 */
	public DarwinCoreArchiveField(int index, String term) 
	{
		super();
		this.index = index;
		this.term = term;
		this.id = false;
	}

	/**
	 * @param index
	 */
	public DarwinCoreArchiveField(int index)
	{
		super();
		this.index = index;
		this.term = "id";
		this.id = true;
	}
	
	/**
	 * 
	 */
	public DarwinCoreArchiveField()
	{
		super();
	}
	
	/**
	 * @param el
	 */
	public void fromXML(Element el)
	{
		index = XMLHelper.getAttr(el, "index", -1);
		term = XMLHelper.getAttr(el, "term", null);
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the term minus the uri info
	 */
	public String getTermName() {
		return getTerm().substring(getTerm().lastIndexOf("/")+1);
	}
	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @return the 'uri'
	 */
	public String getTermQualifier() {
		return getTerm().substring(0, getTerm().lastIndexOf("/"));
	}
	
	/**
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * @return the id
	 */
	public boolean isId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(boolean id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof DarwinCoreArchiveField) {
			result = getTerm().equals(DarwinCoreArchiveField.class.cast(obj).getTerm());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return getTerm();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DarwinCoreArchiveField o) {
		return getTerm().compareTo(o.getTerm());
	}
	
	
	
}
