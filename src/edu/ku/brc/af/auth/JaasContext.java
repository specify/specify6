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
package edu.ku.brc.af.auth;

import java.security.Policy;

import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.module.DbLoginCallbackHandler;
import edu.ku.brc.af.auth.specify.module.SpDBConfiguration;
import edu.ku.brc.af.auth.specify.policy.DatabasePolicy;

/**
 * @author megkumin
 *
 * @code_status Beta
 *
 * Created Date: Jul 19, 2007
 *
 */
public class JaasContext
{
    private static final Logger log    = Logger.getLogger(JaasContext.class);
    public static String        url    = ""; //$NON-NLS-1$
    public static String        driver = ""; //$NON-NLS-1$
    
    public static Subject       globalSubject = null;

    /**
     * 
     */
    public JaasContext()
    {
        
    }
    
    /**
     * Composite policy will allow us to piggy back our own policy definition onto of the 
     * default system policy.
     * 
     * We have a database backed policy definition, so rather than granting our permissions 
     * through the file system, ours are stored within the database
     */
    public void createDatabaseBackedPolicyDefinition()
    {
        Policy.setPolicy(new DatabasePolicy());
        
        // XXX Temporary fix for Speeding up the app 
        //SecurityManager securityMgr = new SecurityManager();
        //System.setSecurityManager(securityMgr);
    }
    
    /**
     * Methods
     * @param user - the username credential supplied by the user
     * @param pass - the password credential supplied by the user
     * @param urlArg - the url of the database
     * @param driverArg -  the driver for the database
     * @param dbUserName -  Database UserName
     * @param dbPwd -  the Database password
     */
    public boolean jaasLogin(final String user, 
                             final String pass, 
                             final String urlArg, 
                             final String driverArg, 
                             final String dbUserName, 
                             final String dbPwd)
    {
        globalSubject        = null;
        
        JaasContext.url      = urlArg;
        JaasContext.driver   = driverArg;
        boolean loginSuccess = false;
        try
        {
        	Configuration.setConfiguration(SpDBConfiguration.getInstance());
            createDatabaseBackedPolicyDefinition();
            DbLoginCallbackHandler spcbh        = new DbLoginCallbackHandler(user, pass, urlArg, driverArg, dbUserName, dbPwd);
            LoginContext           loginContext = new LoginContext("SpLogin", spcbh); //$NON-NLS-1$
            loginContext.login();
            loginSuccess  = true;
            globalSubject = loginContext.getSubject();
            
        } catch (LoginException lex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JaasContext.class, lex);
            log.error("jaasLogin() - " + lex.getClass().getName() + ": " + lex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            log.error("jaasLogin() - user failed to login using through jaas framework"); //$NON-NLS-1$
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JaasContext.class, ex);
            log.error("jaasLogin() - " + ex.getClass().getName() + ": " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return loginSuccess;
    }

    /**
     * @return the globalSubject
     */
    public static Subject getGlobalSubject()
    {
        return globalSubject;
    }
}
