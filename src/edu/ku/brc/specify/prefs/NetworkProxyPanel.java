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
package edu.ku.brc.specify.prefs;

import java.util.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.helpers.ProxyHelper;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Apr 29, 2007
 *
 */
public class NetworkProxyPanel extends GenericPrefsPanel
{
    //protected String[] TYPE_LABELS = {"", ""};
    //protected ValComboBox    proxyType;
   
    protected AppPreferences localPrefs  = AppPreferences.getLocalPrefs();
    
     
    /**
     * Constructor.
     */
    public NetworkProxyPanel()
    {
        createForm("Preferences", "NetworkProxy");
        
        ValTextField tf = form.getCompById(ProxyHelper.PROXY_HOST);
        if (tf != null) tf.setText(localPrefs.get(ProxyHelper.PROXY_HOST, null));
        
        tf = form.getCompById(ProxyHelper.PROXY_PORT);
        if (tf != null) tf.setText(localPrefs.get(ProxyHelper.PROXY_PORT, null));
        
        tf = form.getCompById(ProxyHelper.PROXY_HOST_HTTPS);
        if (tf != null) tf.setText(localPrefs.get(ProxyHelper.PROXY_HOST_HTTPS, null));
        
        tf = form.getCompById(ProxyHelper.PROXY_PORT_HTTPS);
        if (tf != null) tf.setText(localPrefs.get(ProxyHelper.PROXY_PORT_HTTPS, null));
        
        //proxyType = form.getCompById(ProxyHelper.PROXY_TYPE);
        //proxyType.setModel(new DefaultComboBoxModel(TYPE_LABELS));
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
            
            String proxyHost      = null;
            String proxyPort      = null;
            String proxyHostHttps = null;
            String proxyPortHttps = null;
            
            ValTextField tf = form.getCompById(ProxyHelper.PROXY_HOST);
            if (tf != null && StringUtils.isNotEmpty(tf.getText())) 
            {
                proxyHost = tf.getText();
            }
            
            tf = form.getCompById(ProxyHelper.PROXY_PORT);
            if (tf != null && StringUtils.isNotEmpty(tf.getText()))
            {
                proxyPort = tf.getText();
            }
            
            tf = form.getCompById(ProxyHelper.PROXY_HOST_HTTPS);
            if (tf != null && StringUtils.isNotEmpty(tf.getText())) 
            {
                proxyHostHttps = tf.getText();
            }
            
            tf = form.getCompById(ProxyHelper.PROXY_PORT_HTTPS);
            if (tf != null && StringUtils.isNotEmpty(tf.getText()))
            {
                proxyPortHttps = tf.getText();
            }
            
            ProxyHelper.registerProxy(proxyHost, proxyPort, proxyHostHttps, proxyPortHttps, true);
            
            /*
            int typeInx = proxyType.getComboBox().getSelectedIndex();
            if (StringUtils.isNotEmpty(proxyHost) && StringUtils.isNotEmpty(proxyPort))
            {
                ProxyHelper.registerProxy(proxyHost, proxyPort);
            }
            */
            
            try
            {
                localPrefs.flush();
                
            } catch (BackingStoreException ex) {}
        }
    }
 }
