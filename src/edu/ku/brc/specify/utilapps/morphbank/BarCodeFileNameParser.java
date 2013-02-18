/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;

/**
 * @author timo
 * 
 * Filenames are nothing but a specimen barcode number (or any other identifier) stored in a text field in CollectionObject.
 * 
 *  Used to import images for the Troy database.
 *
 */
public class BarCodeFileNameParser implements FileNameParserIFace
{

	protected String barCodeFieldName;  //name of the field - IN CollectionObject - where barcodes are stored.
	

	/**
	 * @param barCodeFieldName
	 */
	public BarCodeFileNameParser(String barCodeFieldName)
	{
		this.barCodeFieldName = barCodeFieldName;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.morphbank.FileNameParserIFace#getRecordIds(java.lang.String)
	 */
	@Override
	public List<Integer> getRecordIds(String fileName)
	{
		List<Integer> result = new Vector<Integer>();
		String id = fileName.replace("_", "");
		id = id.substring(0, id.length() - 4);
		String sql = "select CollectionObjectID from collectionobject where " 
			+ barCodeFieldName + " = '" + id + "'";
		//System.out.println(sql);
		//result.add(1);
		Vector<Object> idObjs = BasicSQLUtils.querySingleCol(DBConnection.getInstance().getConnection(), sql);
		if (idObjs != null)
		{
			for (Object idObj : idObjs)
			{
				result.add((Integer )idObj);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.morphbank.FileNameParserIFace#getTableId()
	 */
	@Override
	public Integer getTableId()
	{
		return CollectionObject.getClassTableId();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getAttachmentOwnerClass()
     */
    @Override
    public Class<?> getAttachmentOwnerClass()
    {
        return CollectionObject.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getAttachmentJoinClass()
     */
    @Override
    public Class<?> getAttachmentJoinClass()
    {
        return CollectionObjectAttachment.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        return barCodeFieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#isNameValid(java.lang.String)
     */
    @Override
    public boolean isNameValid(String fileName)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFieldTitle()
     */
    @Override
    public String getFieldTitle()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFormatter()
     */
    @Override
    public UIFieldFormatterIFace getFormatter()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRecordId(java.lang.String)
     */
    @Override
    public Integer getRecordId(String fileName)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getName()
     */
    @Override
    public String getTitle()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRow(edu.ku.brc.specify.datamodel.Workbench, java.lang.String)
     */
    @Override
    public WorkbenchRow getRow(Workbench workBench, String fileName)
    {
        return null;
    }

}
