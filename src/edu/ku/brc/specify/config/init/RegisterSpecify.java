package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * This class is used for auto registering collections.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 17, 2008
 *
 */
public class RegisterSpecify
{
    /**
     * Constructor.
     */
    public RegisterSpecify()
    {
        super();
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if auto update checking is turned on
     */
    public static boolean hasAutoRegistered()
    {
        return AppContextMgr.getInstance().getClassObject(Institution.class).getIsAR();
    }
    
    /**
     * 
     */
    public static void setHasAutoRegistered()
    {
        
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        inst.setIsAR(true);
        Institution.save(inst);
    }
    
    /**
     * 
     */
    public void doRegister()
    {
        // Create a SwingWorker to connect to the server in the background, then show results on the Swing thread
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                // connect to the server, sending usage stats if allowed, and gathering the latest modules version info
                try
                {
                    return doRegisterInternal();
                }
                catch (Exception e)
                {
                    // if any exceptions occur, return them so the finished() method can have them
                    return e;
                }
            }
            
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            @Override
            public void finished()
            {
                Object retVal = getValue();
                if (retVal != null)
                {
                    // if an exception occurred during update check...
                    if (retVal instanceof Boolean && ((Boolean)retVal))
                    {
                        setHasAutoRegistered();
                    }
                }
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    /**
     * @return
     * @throws Exception if an IO error occured or the response couldn't be parsed
     */
    protected boolean doRegisterInternal() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getRegisterURL();
        
        //System.err.println(versionCheckURL);
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters();
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
        }
        catch (Exception e)
        {
            throw new ConnectionException(e);
        }
        
        // get the server response
        String responseString = postMethod.getResponseBodyAsString();
        
        return StringUtils.isNotEmpty(responseString) && responseString.equals("ok");
    }
    
    /**
     * Gets the URL of the version checking / usage tracking server.
     * 
     * @return the URL string
     */
    protected String getRegisterURL()
    {
        String baseURL = getResourceString("SPECIFY.REGISTER"); //$NON-NLS-1$
        return baseURL;
    }
    
    private String fixParam(final String value)
    {
        return value == null ? "" : value;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param sendUsageStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters()
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();

        // get the install ID
        String installID = UsageTracker.getInstallId();
        postParams.add(new NameValuePair("id", installID)); //$NON-NLS-1$

        // get the OS name and version
        postParams.add(new NameValuePair("os_name", System.getProperty("os.name"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("os_version",System.getProperty("os.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_version",System.getProperty("java.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_vendor",System.getProperty("java.vendor"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("user_name",System.getProperty("user.name"))); //$NON-NLS-1$

        
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        postParams.add(new NameValuePair("Institution_name", fixParam(inst.getName()))); //$NON-NLS-1$
        postParams.add(new NameValuePair("Institution_title", fixParam(inst.getTitle()))); //$NON-NLS-1$
        
        Division div = AppContextMgr.getInstance().getClassObject(Division.class);
        postParams.add(new NameValuePair("Division_name", fixParam(div.getName()))); //$NON-NLS-1$
        postParams.add(new NameValuePair("Division_title", fixParam(div.getTitle()))); //$NON-NLS-1$
        
        SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        postParams.add(new NameValuePair("User_name", fixParam(user.getName()))); //$NON-NLS-1$
        postParams.add(new NameValuePair("User_email", fixParam(user.getEmail()))); //$NON-NLS-1$
        
        Address addr = inst.getAddress();
        if (addr != null)
        {
            postParams.add(new NameValuePair("Address", fixParam(addr.getIdentityTitle()))); //$NON-NLS-1$
        }
        
        // create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }
        return paramArray;
    }
    
    public static class ConnectionException extends IOException
    {
        public ConnectionException(@SuppressWarnings("unused") Throwable e)
        {
            super();
        }
    }
}
