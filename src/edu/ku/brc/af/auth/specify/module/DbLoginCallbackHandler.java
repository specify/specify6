/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
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
public class DbLoginCallbackHandler  implements CallbackHandler 
{
    protected static final Logger log    = Logger.getLogger(DbLoginCallbackHandler.class);
    protected String              user   = "";
    protected String              pass   = "";
    protected String              url    = "";
    protected String              driver = "";
    
    /**
     * Creates a callback handler for connection to a database
     */
    public DbLoginCallbackHandler(String user, String pass, String url, String driver)
    {
        this.user = user;
        this.pass = pass;
        this.url = url;
        this.driver = driver;
        log.debug("DbLoginCallbackHandler() created");
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
        log.debug("handle");
        try
        {
            for (int i = 0; i < callbacks.length; i++) {

                if (callbacks[i] instanceof NameCallback) 
                {
                    log.debug("handle - [LoginPanelCallback]");
                    log.debug("handle - got user:" + user);
                    ((NameCallback)callbacks[i]).setName(user);
                } 
                else if (callbacks[i] instanceof PasswordCallback) 
                {
                    log.debug("handle - [PasswordCallback]");
                    log.debug("handle - got password:" + pass);
                    ((PasswordCallback)callbacks[i]).setPassword(pass.toCharArray());
                } 
                else if (callbacks[i] instanceof TextInputCallback) 
                {
                    log.debug("handle - [TextInputCallback]");
                    if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("Url:"))
                    {
                        log.debug("handle - got url:" + url);
                        ((TextInputCallback)callbacks[i]).setText(url);
                    } else if ((((TextInputCallback)callbacks[i]).getPrompt()).equals("Driver:"))
                    {
                        log.debug("handle - got driver:" + driver);
                        ((TextInputCallback)callbacks[i]).setText(driver);
                    }
                }
                else
                {
                    throw (new UnsupportedCallbackException(callbacks[i],
                            "Callback class not supported"));
                }
            }
        } catch (Exception e)
        {
            log.error("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
