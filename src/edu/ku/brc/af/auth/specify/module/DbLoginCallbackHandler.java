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
package edu.ku.brc.af.auth.specify.module;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.log4j.Logger;

/**
 * 
 * DbLoginCallbackHandler has constructor that takes 
 * String user, String pass, String url, String driver so its handle() 
 * method can retrieve all of the pertinent login information.
 * 
 * @author megkumin
 * @code_status Beta
 * Created Date: Jul 17, 2007
 * 
 *
 */
public class DbLoginCallbackHandler implements CallbackHandler 
{
    protected static final Logger log    = Logger.getLogger(DbLoginCallbackHandler.class);
    protected String              user   = ""; //$NON-NLS-1$
    protected String              pass   = ""; //$NON-NLS-1$
    protected String              url    = ""; //$NON-NLS-1$
    protected String              driver = ""; //$NON-NLS-1$
    protected String              dbUserName = ""; //$NON-NLS-1$
    protected String              dbPwd      = ""; //$NON-NLS-1$
    
    /**
     * Creates a callback handler for connection to a database
     */
    public DbLoginCallbackHandler(final String user, 
                                  final String pass, 
                                  final String url, 
                                  final String driver,
                                  final String dbUserName,
                                  final String dbPwd)
    {
        this.user = user;
        this.pass = pass;
        this.url = url;
        this.driver = driver;
        this.dbUserName = dbUserName;
        this.dbPwd = dbPwd;
        //log.debug("DbLoginCallbackHandler() created"); //$NON-NLS-1$
    }
    
    /**
     * Handles the specified set of Callbacks. 
     *
     * This class supports NameCallback, PasswordCallback and TextInputCallback.
     *
     * @param   callbacks the callbacks to handle
     * @throws  IOException if an input or output error occurs.
     * @throws  UnsupportedCallbackException if the callback is not an
     * instance of NameCallback, PasswordCallback, or TextInputCallback
     * 
     * (non-Javadoc)
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        log.debug("handle"); //$NON-NLS-1$
        try
        {
            for (int i = 0; i < callbacks.length; i++) {

                if (callbacks[i] instanceof NameCallback) 
                {
                    //log.debug("handle - [LoginPanelCallback]"); //$NON-NLS-1$
                    //log.debug("handle - got user:" + user); //$NON-NLS-1$
                    ((NameCallback)callbacks[i]).setName(user);
                } 
                else if (callbacks[i] instanceof PasswordCallback) 
                {
                    //log.debug("handle - [PasswordCallback]"); //$NON-NLS-1$
                    //log.debug("handle - got password:" + pass); //$NON-NLS-1$
                    ((PasswordCallback)callbacks[i]).setPassword(pass.toCharArray());
                } 
                else if (callbacks[i] instanceof TextInputCallback) 
                {
                    //log.debug("handle - [TextInputCallback]"); //$NON-NLS-1$
                    if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("Url:")) //$NON-NLS-1$
                    {
                        //log.debug("handle - got url:" + url); //$NON-NLS-1$
                        ((TextInputCallback)callbacks[i]).setText(url);
                    } else if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("Driver:")) //$NON-NLS-1$
                    {
                        //log.debug("handle - got driver:" + driver); //$NON-NLS-1$
                        ((TextInputCallback)callbacks[i]).setText(driver);
                        
                    } else if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("DBUserName:")) //$NON-NLS-1$
                    {
                        ((TextInputCallback)callbacks[i]).setText(dbUserName);
                        log.debug("handle - got dbUserName:" + dbUserName); //$NON-NLS-1$
                        
                    } else if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("DBPwd:")) //$NON-NLS-1$
                    {
                        ((TextInputCallback)callbacks[i]).setText(dbPwd);
                        log.debug("handle - got dbPwd:" + dbPwd); //$NON-NLS-1$
                    }
                }
                else
                {
                    throw (new UnsupportedCallbackException(callbacks[i], "Callback class not supported")); //$NON-NLS-1$
                }
            }
        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DbLoginCallbackHandler.class, e);
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
            e.printStackTrace();
        }
    }
}
