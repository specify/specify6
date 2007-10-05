package edu.ku.brc.specify.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.util.AttachmentUtils;

public class WebLinkLauncherButton extends JButton implements UIPluginable, GetSetValueIFace, ActionListener
{
    protected Logger log = Logger.getLogger(WebLinkLauncherButton.class);
    
    protected String urlFormat;
    protected Properties initProps;
    protected FormDataObjIFace dataObj;
    
    public WebLinkLauncherButton()
    {
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae)
    {
        // Parse urlFormat, looking for $fieldName$.
        // Replace those with the values of those fields from the dataObj
        // The attempt to open the URL in the default viewer
        
        // Parse the format and build the URL
        String urlString;
        try
        {
            urlString = buildURL();
        }
        catch (Exception e1)
        {
            log.error("Failed to build URL", e1);
            return;
        }
        
        // Convert to a URI
        URI uri;
        try
        {
            uri = new URI(urlString);
        }
        catch (URISyntaxException e)
        {
            log.error("Bad URL syntax: " + urlString, e);
            return;
        }

        // Open the URI
        try
        {
            AttachmentUtils.openURI(uri);
        }
        catch (Exception e)
        {
            log.error("Failed to open URL: " + uri.toString(), e);
            return;
        }
    }

    protected String buildURL() throws Exception
    {
        if (StringUtils.countMatches("$", urlFormat) % 2 != 0)
        {
            // There are an odd number of "$" in the urlFormat.
            // This is an error.
            throw new Exception("Bad URL format string.  Format string must contain an even number of '$' characters.");
        }

        StringBuilder url = new StringBuilder(urlFormat);
        while (url.indexOf("$") != -1)
        {
            int startIndex = url.indexOf("$");
            int endIndex = url.indexOf("$", startIndex+1);
            
            String fieldName = url.substring(startIndex+1, endIndex);
            String stringFieldVal = "";
            
            try
            {
                stringFieldVal = BeanUtils.getProperty(dataObj, fieldName);
            }
            catch (Exception e)
            {
                log.warn("Cannot find field '" + fieldName + "' in " + dataObj.getClass().getName() + ".  Removing that field from URL.", e);
            }
            url = url.replace(startIndex, endIndex+1, stringFieldVal);
        }
        
        return url.toString();
    }
    
    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------

    public JComponent getUIComponent()
    {
        return this;
    }

    public void initialize(Properties properties, boolean isViewMode)
    {
        initProps = properties;
        urlFormat = initProps.getProperty("url");
        this.setText(initProps.getProperty("label"));
    }

    public void setCellName(String cellName)
    {
        // ignore
    }

    public void setChangeListener(ChangeListener listener)
    {
        // ignore
    }

    public void setIsDisplayOnly(boolean isDisplayOnly)
    {
        // ignore
    }
    
    //--------------------------------------------------------
    //-- GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value != null && value instanceof FormDataObjIFace)
        {
            dataObj = (FormDataObjIFace)value;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }
}
