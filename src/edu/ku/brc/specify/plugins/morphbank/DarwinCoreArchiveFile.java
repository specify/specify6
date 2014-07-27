/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dom4j.Element;

/**
 * @author timo
 *
 */
public class DarwinCoreArchiveFile 
{
	protected String encoding;
	protected String fieldsTerminatedBy;
	protected String linesTerminatedBy;
	protected String fieldsEnclosedBy;
	protected String escaper = "\\";
	protected boolean ignoreHeaderLines;
	protected boolean core;
	protected List<DarwinCoreArchiveField> mappings;
	protected String rowType;
	protected List<String> files;
	
	/**
	 * @param encoding
	 * @param fieldsTerminatedBy
	 * @param linesTerminatedBy
	 * @param fieldsEnclosedBy
	 * @param ignoreHeaderLines
	 * @param core
	 */
	public DarwinCoreArchiveFile(String encoding, String fieldsTerminatedBy,
			String linesTerminatedBy, String fieldsEnclosedBy,
			boolean ignoreHeaderLines, boolean core,
			List<DarwinCoreArchiveField> mappings,
			String rowType, List<String> files) 
	{
		super();
		this.encoding = encoding;
		this.fieldsTerminatedBy = fieldsTerminatedBy;
		this.linesTerminatedBy = linesTerminatedBy;
		this.fieldsEnclosedBy = fieldsEnclosedBy;
		this.ignoreHeaderLines = ignoreHeaderLines;
		this.core = core;
		this.mappings = mappings;
		Collections.sort(mappings, new Comparator<DarwinCoreArchiveField>(){

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(DarwinCoreArchiveField arg0,
					DarwinCoreArchiveField arg1) {
				Integer idx0 = arg0.getIndex();
				Integer idx1 = arg1.getIndex();
				return idx0.compareTo(idx1);
			}			
		});
		this.rowType = rowType;
		this.files = files;
	}

	
	/**
	 * @param core
	 * @param element
	 */
	public DarwinCoreArchiveFile(boolean core, Element element)
	{
		this.core = core;
		fromXML(element);
	}
	
	/**
	 * @return
	 */
	public String getHeader() {
		StringBuilder result = new StringBuilder();
		for (DarwinCoreArchiveField fld : getMappings()) {
			if (result.length() > 0) {
				result.append(",");
			}
			result.append(fld.getTermName());
		}
		return result.toString();
	}
	
	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return the fieldsTerminatedBy
	 */
	public String getFieldsTerminatedBy() {
		return fieldsTerminatedBy;
	}

	/**
	 * @param fieldsTerminatedBy the fieldsTerminatedBy to set
	 */
	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.fieldsTerminatedBy = fieldsTerminatedBy;
	}

	/**
	 * @return the linesTerminatedBy
	 */
	public String getLinesTerminatedBy() {
		return linesTerminatedBy;
	}

	/**
	 * @param linesTerminatedBy the linesTerminatedBy to set
	 */
	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.linesTerminatedBy = linesTerminatedBy;
	}

	/**
	 * @return the fieldsEnclosedBy
	 */
	public String getFieldsEnclosedBy() {
		return fieldsEnclosedBy;
	}

	/**
	 * @param fieldsEnclosedBy the fieldsEnclosedBy to set
	 */
	public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
		this.fieldsEnclosedBy = fieldsEnclosedBy;
	}

	/**
	 * @return the ignoreHeaderLines
	 */
	public boolean isIgnoreHeaderLines() {
		return ignoreHeaderLines;
	}

	/**
	 * @param ignoreHeaderLines the ignoreHeaderLines to set
	 */
	public void setIgnoreHeaderLines(boolean ignoreHeaderLines) {
		this.ignoreHeaderLines = ignoreHeaderLines;
	}

	
	/**
	 * @return the escaper
	 */
	public String getEscaper() {
		return escaper;
	}


	/**
	 * @param escaper the escaper to set
	 */
	public void setEscaper(String escaper) {
		this.escaper = escaper;
	}


	/**
	 * @return the core
	 */
	public boolean isCore() {
		return core;
	}

	/**
	 * @param core the core to set
	 */
	public void setCore(boolean core) {
		this.core = core;
	}

	/**
	 * @return the mappings
	 */
	public List<DarwinCoreArchiveField> getMappings() {
		return mappings;
	}

	/**
	 * @return the rowType
	 */
	public String getRowType() {
		return rowType;
	}

	/**
	 * @param rowType the rowType to set
	 */
	public void setRowType(String rowType) {
		this.rowType = rowType;
	}

	/**
	 * @return the files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
	   /**
     * @param element
     */
    public void fromXML(final Element element)
    {
        encoding            = getAttr(element, "encoding", "ISO-8859-1");
        fieldsTerminatedBy     = getAttr(element, "fieldsTerminatedBy", ",");
        linesTerminatedBy  = getAttr(element, "linesTerminatedBy", "\\n");
        fieldsEnclosedBy = getAttr(element, "fieldsEnclosedBy", "\"");
        ignoreHeaderLines      = getAttr(element, "ignoreHeaderLines", true);
        rowType           = getAttr(element, "rowType", null);
        
        files = new ArrayList<String>();
        mappings = new ArrayList<DarwinCoreArchiveField>();
        for (Object obj : element.selectNodes("*"))
        {
            Element elObj = (Element)obj;
            String elName = elObj.getName();
            if ("files".equals(elName))
            {
            	for (Object locObj : elObj.selectNodes("*"))
            	{
            		if ("location".equals(((Element)locObj).getName()))
            		{
            			files.add(((Element)locObj).getText());
            		}
            	}
            } else if ("id".equals(elName) || "coreid".equals(elName))            	
            {
                DarwinCoreArchiveField field = new DarwinCoreArchiveField();
                field.fromXML(elObj);
                field.setId(true);
                field.setTerm(elName);
                mappings.add(field);
            } else if ("field".equals(elName))
            {
                DarwinCoreArchiveField field = new DarwinCoreArchiveField();
                field.fromXML(elObj);
                mappings.add(field);
            }
        }
    }

	
}
