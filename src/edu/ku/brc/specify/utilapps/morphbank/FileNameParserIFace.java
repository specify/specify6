/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.util.List;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;

/**
 * @author timo
 * 
 * Gets records that image files should be attached to by parsing the image file names.
 *
 */
public interface FileNameParserIFace
{
	/**
	 * @return the table id
	 */
	public abstract Integer getTableId();
	
    /**
     * @return The class that 'owns' the attachment (e.g. CollectionObject)
     */
    public abstract Class<?> getAttachmentOwnerClass();
    
    /**
     * @return the class that 'joins' (CollectionObjectAttachment) the Owner class (CollectionObject) with the Attachment record.
     */
    public abstract Class<?> getAttachmentJoinClass();
    
    /**
     * @return the name of the field (not column name) in the 'owner' 
     * Class that is the 'key' to be used to look up to see of the record exists.
     */
    public abstract String getFieldName();
    
    /**
     * @return
     */
    public abstract String getFieldTitle();
    
    /**
     * @return
     */
    public abstract UIFieldFormatterIFace getFormatter();
    
	/**
	 * @param fileName
	 * @return a list of keys for records identified by the fileName
	 */
	public abstract List<Integer> getRecordIds(String fileName);
	
	/**
	 * @param fileName
	 * @param getBaseName
	 * @return
	 */
	public abstract boolean isNameValid(String fileName, boolean getBaseName);
	
	/**
	 * @param fileName
	 * @return
	 */
	public abstract Integer getRecordId(String fileName);
	
	/**
	 * @return A unique name that identifies the class/table and field
	 */
	public abstract String getTitle();
	
	/**
	 * @param workBench
	 * @return
	 */
	public abstract WorkbenchRow getRow(Workbench workBench, String fileName);
}
