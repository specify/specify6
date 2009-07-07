/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import it.businesslogic.ireport.IReportConnection;

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author Administrator
 *
 *Base class for IReport connections based on Specify data.
 */
public abstract class SpJRIReportConnection extends IReportConnection
{
    protected final List<JRConnectionFieldDef> fields = new ArrayList<JRConnectionFieldDef>();
    protected final String objectName; //apparently iReport has it's own uses for the
    									//name prop(s) so need to declare a new var.

    /**
     * @param objectName
     */
    public SpJRIReportConnection(String objectName)
    {
    	super();
    	this.objectName = objectName;
        this.setName(objectName);
    }
    
    /**
     * @return the object represented by the connection. 
     */
    public abstract DataModelObjBase getSpObject();
    
    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#getJRDataSource()
     */
    @Override
    public JRDataSource getJRDataSource()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see it.businesslogic.ireport.IReportConnection#test()
     */
    @Override
    public void test() throws Exception
    {
        // TODO Auto-generated method stub
        super.test();
    }

    /**
     * @param fldName
     * @return the field name
     */
    public JRConnectionFieldDef getFieldByName(final String fldName)
    {
    	for (JRConnectionFieldDef fld : fields)
    	{
    		if (fld.getFldName().equals(fldName))
    		{
    			return fld;
    		}
    	}
    	return null;
    }
    
    /**
     * @param title
     * @return the field title.
     */
    public JRConnectionFieldDef getFieldByTitle(final String title)
    {
    	for (JRConnectionFieldDef fld : fields)
    	{
    		if (fld.getFldTitle().equals(title))
    		{
    			return fld;
    		}
    	}
    	return null;
    }

    /**
     * @return number of fields.
     */
    public int getFields()
    {
        return fields.size();
    }
    
    /**
     * @param index
     * @return field at index.
     */
    public JRConnectionFieldDef getField(int index)
    {
        return fields.get(index);
    }    
}
