/* Copyright (C) 2015, University of Kansas Center for Research
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.credential.UsernameCredential;
import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalSQLService;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 *  SpDBLoginModule is a LoginModule as part of the JAAS framework.
 *  It authenticates a given username/password credential against
 *  a JDBC datasource.
 *  
 * @author megkumin
 * @code_status Beta
 * Created Date: Jul 17, 2007
 *
 */
public class SpDBLoginModule implements LoginModule
{
    protected static final Logger log = Logger.getLogger(SpDBLoginModule.class);
    
    // initial state
    private CallbackHandler       callbackHandler;
    private Subject               subject;

    // variable options
    private String                username;
    private String                password;
    private String                url;
    private String                driverClass;
    private String                dbUserName;
    private String                dbPwd;

    // the authentication status
    boolean                       authenticated;

    /**
     * Creates a login module that will authenticate against a datasource
     */
    public SpDBLoginModule()
    {
        authenticated = false;
    }

    /**
     * Initialize this <code>SpDBLoginModule</code>.

     * @param subject - the Subject to be authenticated
     * @param callbackHandler - a CallbackHandler for communicating with the end user (prompts for credentials)
     * @param sharedState - shared LoginModule state
     * @param options - options specified in the login Configuration for this particular LoginModule
     *
     * (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(final Subject subjectArg, 
                           final CallbackHandler callbackHandlerArg, 
                           final Map<String, ?> sharedState, 
                           final Map<String, ?> options)
    {
        this.callbackHandler = callbackHandlerArg;
        this.subject         = subjectArg;
    }

    /* 
     * Verify the username and password against the relevant JDBC datasource.
     * @return true always, since this LoginModule should not be ignored.
     * 
     * @exception FailedLoginException - if the authentication fails.
     * @exception LoginException - if this <code>LoginModule</code> is unable to perform the authentication.

     * (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException
    {
        if (callbackHandler == null)
        {
            throw new LoginException("Error: no CallbackHandler provided");
        }
        try
        {
            // Setup default callback handlers.
            Callback[] callbacks = new Callback[] { 
                    new NameCallback("Username: "),
                    new PasswordCallback("Password: ", false),
                    new TextInputCallback("Url:"),
                    new TextInputCallback("Driver:"),
                    new TextInputCallback("DBUserName:"),
                    new TextInputCallback("DBPwd:")
            };
            callbackHandler.handle(callbacks);
            
            username    = ((NameCallback) callbacks[0]).getName();
            password    = new String(((PasswordCallback) callbacks[1]).getPassword());
            url         = new String (((TextInputCallback) callbacks[2]).getText());
            driverClass = new String (((TextInputCallback) callbacks[3]).getText());
            dbUserName  = new String (((TextInputCallback) callbacks[4]).getText());
            dbPwd       = new String (((TextInputCallback) callbacks[5]).getText());

            authenticated = SecurityMgr.getInstance().authenticateDB(username, password, driverClass, url, dbUserName, dbPwd);
            
            for (int i = 0; i < callbacks.length; i++)
            {
                callbacks[i] = null;
            }

            if (!authenticated)
            {
                // throw new LoginException("Authentication failed: Password does not match");
                //XXX do we abort or just return false or throw Login exception?
                //i found conflicting recommendations.
                abort();
            }
            return authenticated;
        } 
        catch (LoginException ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpDBLoginModule.class, ex);
            log.error("LoginException" + ex.getMessage());
            authenticated = false;
            ex.printStackTrace();
        } 
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpDBLoginModule.class, ex);
            log.error("Exception" + ex.getMessage());
            ex.printStackTrace();
            throw new LoginException(ex.getMessage());
        }    
        return false;
    }

    /**
     * This method is called if the LoginContext's overall authentication succeeded (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules succeeded).
     * 
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private
     * state saved by the login method), then this method associates a Principal with the Subject 
     * located in the LoginModule. If this LoginModule's own authentication attempted failed, then
     * this method removes any state that was originally saved.
     * 
     * @exception LoginException -  if the commit fails
     * @return true if this LoginModule's own login and commit attempts succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException
    {
        if (!authenticated)
        {
            log.debug("user was not authenticated");
            return false;
        }
        if (subject.isReadOnly()) { throw new LoginException("Subject is Readonly"); }
        try
        {
            Set<SpPrincipal> groups = UserPrincipalSQLService.getUsersGroupsByUsername(username);
            for (Iterator<SpPrincipal> itr = groups.iterator(); itr.hasNext();)
            {
                SpPrincipal ug = itr.next();
                String s = "class " + ug.getGroupSubClass();

                // I'm not adding the SpPrincipal anymore, 
                // just the principals instanciated from subclasses of BasicPrincipal
                // subject.getPrincipals().add(ug);
                
                if (s != null)
                {
                    if (s.equals(AdminPrincipal.class.toString()))
                    {
                        AdminPrincipal adminUser = new AdminPrincipal(ug.getId(), username);
                        subject.getPrincipals().add(adminUser);
                    }
                    else if (s.equals(GroupPrincipal.class.toString()))
                    {
                        GroupPrincipal groupPrincipal = new GroupPrincipal(ug.getId(), username);                        
                        subject.getPrincipals().add(groupPrincipal);
                    }
                    else if (s.equals(UserPrincipal.class.toString()))
                    {
                        UserPrincipal userPrincipal = new UserPrincipal(ug.getId(), username);
                        subject.getPrincipals().add(userPrincipal);
                    }
                }
            }
            UsernameCredential cred = new UsernameCredential(username);
            subject.getPublicCredentials().add(cred);           
            return true;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpDBLoginModule.class, ex);
            log.error("Specify exception occurred" + ex.toString());
            ex.printStackTrace();
            throw new LoginException(ex.getMessage());
        }         
    }


    /*
     * Logout the user.
     * 
     * This method removes the Principal that was added by the commit method.
     * 
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private
     * state saved by the login method), then this method associates a Principal with the Subject
     * located in the LoginModule. If this LoginModule's own authentication attempted failed, then
     * this method removes any state that was originally saved.
     * 
     * @exception LoginException - if the commit fails @return true if this LoginModule's own login
     * and commit attempts succeeded, or false otherwise.
     * 
     * (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException
    {
        if (subject.isReadOnly()) { return false; }
        
        // remove the principals the login module added
        Iterator<SpPrincipal> it = subject.getPrincipals(SpPrincipal.class).iterator();
        while (it.hasNext())
        {
            SpPrincipal p = it.next();
            subject.getPrincipals().remove(p);
        }
        
        // remove the credentials the login module added
        it = subject.getPublicCredentials(SpPrincipal.class).iterator();
        while (it.hasNext())
        {
            SpPrincipal c = it.next();
            subject.getPrincipals().remove(c);
        }
        cleanState();
        return (true); 
    }

    /**
     *
     * This method is called if the LoginContext's overall authentication failed. (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not succeed). If this LoginModule's 
     * own authentication attempt succeeded (checked by retrieving the private state saved by the login 
     * and commit methods), then this method cleans up any state that was originally saved.
     * @exception LoginException if the abort fails.
     * @return false if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     */
    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException
    {
        //Clean out state
        cleanState();
        logout();
        return true;
    }
    
    public void cleanState()
    {
        username = null;
        password = null;
        url = null;
        driverClass = null;
        authenticated = false;
    } 
}
