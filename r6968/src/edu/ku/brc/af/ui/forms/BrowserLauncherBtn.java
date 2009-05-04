/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class BrowserLauncherBtn extends JButton implements GetSetValueIFace
{
    protected String url     = null;
    protected Object dataObj = null;
    protected ActionListener action = null;
    
    public BrowserLauncherBtn(final String text)
    {
        super(text);
        setControlSize(this);
        setEnabled(false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value != null)
        {
            url = value.toString();
            this.setText(url);
            dataObj = value;
            
            if (action != null)
            {
                this.removeActionListener(action);
            }
            
            if (StringUtils.isNotEmpty(url))
            {
                action = new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        buildAndOpenURL();
                    }
                };
                this.addActionListener(action);
                setEnabled(true);
            } else
            {
                setEnabled(false);
            }
        } else
        {
            setEnabled(false);
        }
    }
    
    protected void buildAndOpenURL()
    {
        StringBuilder urlBuilder = new StringBuilder(url);

        if (url.startsWith("["))
        {
            // replace the URL prefix ([prefix]) at the start with the value from the DB 
            AppPreferences remotePrefs = AppPreferences.getRemote();
            
            int endingBracketIndex = urlBuilder.indexOf("]");
            String urlPrefix = urlBuilder.substring(1, endingBracketIndex);
            String urlPrefixValue = remotePrefs.get("URL_Prefix." + urlPrefix, null);
            
            if (urlPrefixValue == null)
            {
                String errorMsg = String.format(getResourceString("WLLB_CANNOT_BUILD_URL"), new Object[] {urlPrefix});
                UIRegistry.getStatusBar().setErrorMessage(errorMsg);
                return;
            }
            urlBuilder.replace(0, endingBracketIndex+1, urlPrefixValue);
        }
        
        try
        {
            AttachmentUtils.openURI(new URI(urlBuilder.toString()));
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BrowserLauncherBtn.class, e);
            String errorMsg = String.format(getResourceString("ERROR_CANT_OPEN_URL"), new Object[] {urlBuilder.toString()});
            UIRegistry.getStatusBar().setErrorMessage(errorMsg, e);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }
}
