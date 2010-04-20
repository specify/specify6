/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.commons.lang.NotImplementedException;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class MorphBankUploadButtonPlugin extends JButton implements
		UIPluginable, GetSetValueIFace
{
	protected CollectionObject colObj = null;
	protected boolean hasImages = false;
	
	public MorphBankUploadButtonPlugin()
	{
        super();
		addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                doButtonAction();
            }
        });

	}

    protected void doButtonAction()
    {
    	System.out.println("Do the button action.");
    }
    
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
	 */
	@Override
	public boolean canCarryForward()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
	 */
	@Override
	public String[] getCarryForwardFields()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return "MorphBank";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
	 */
	@Override
	public JComponent getUIComponent()
	{
		return this;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
	 */
	@Override
	public void initialize(Properties properties, boolean isViewMode)
	{
		// TODO Auto-generated method stub
        //setIcon(IconManager.getIcon("MorphBank16"));
        setText(getResourceString("MorphBankUploadButtonPlugin.ButtonText"));

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
	 */
	@Override
	public boolean isNotEmpty()
	{
        throw new NotImplementedException("isNotEmpty not implemented!");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
	 */
	@Override
	public void setCellName(String cellName)
	{
		// no op
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
	 */
	@Override
	public void setParent(FormViewObj parent)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
	 */
	@Override
	public void shutdown()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
//	@Override
//	public void propertyChange(PropertyChangeEvent arg0)
//	{
//		// TODO Auto-generated method stub
//
//	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
	 */
	@Override
	public Object getValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
	 */
	@Override
	public void setValue(Object value, String defaultValue)
	{
        colObj = null;
        hasImages = false;
        if (value != null)
        {
            if (value instanceof CollectionObject)
            {
                colObj = (CollectionObject)value;
                //this may not work...
                for (CollectionObjectAttachment at : colObj.getCollectionObjectAttachments())
                {
                	if (isImageMimeType(at.getAttachment().getMimeType()))
                	{
                		hasImages = true;
                		break;
                	}
                }
            }
        }         
        setEnabled(hasImages);

	}

	/**
	 * @param mimeType
	 */
	protected boolean isImageMimeType(final String mimeType)
	{
		return mimeType != null && mimeType.startsWith("image");
	}
}
