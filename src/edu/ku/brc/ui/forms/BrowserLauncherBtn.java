/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.helpers.BrowserLauncher;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 26, 2007
 *
 */
public class BrowserLauncherBtn extends JButton implements GetSetValueIFace
{
    protected String url     = null;
    protected Object dataObj = null;
    protected BrowserLauncherAction action = null;
    
    public BrowserLauncherBtn(final String text)
    {
        super(text);
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
            dataObj = value;
            
            if (action != null)
            {
                this.removeActionListener(action);
            }
            
            if (StringUtils.isNotEmpty(url) && url.startsWith("http"))
            {
                action = new BrowserLauncherAction(url);
                addActionListener(action);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }

    //-----------------------------------------------------------
    // Inner Class
    //-----------------------------------------------------------
    

    public class BrowserLauncherAction implements ActionListener
    {
        protected String url;
        
        public BrowserLauncherAction(final String url)
        {
            this.url = url;
        }
        
        public void actionPerformed(ActionEvent ae)
        {
            BrowserLauncher.openURL(url);
        }
    }
}
