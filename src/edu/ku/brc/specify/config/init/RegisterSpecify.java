package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UIRegistry;

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
    private enum RegisterType { Institution, Division, Discipline, Collection }
    
    private final String REGPREF_NAME = "Registered.HAS_ASKED";
    private final String ANON_REG     = "Registered.ANON";
    
    
    /**
     * Constructor.
     */
    public RegisterSpecify()
    {
        super();
    }
    
    /**
     * @param title
     * @return
     */
    private boolean askToReg(final String typeTitle, final String typeName)
    {
        Object[] options = { getResourceString("YES"),  //$NON-NLS-1$
                             getResourceString("NO")  //$NON-NLS-1$
                            };
        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                        getLocalizedMessage("DO_REG", typeTitle, typeName),  //$NON-NLS-1$
                                        getResourceString("DO_REG_TITLE"),  //$NON-NLS-1$
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        return userChoice == JOptionPane.YES_OPTION;
    }
    
    /**
     * @return whether the any user has been asked to register the institution.
     */
    public boolean hasAskedToRegInstitution()
    {
        return AppPreferences.getRemote().getBoolean(REGPREF_NAME, false);
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if auto update checking is turned on
     */
    public boolean hasInstitutionRegistered()
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        return inst != null && StringUtils.isNotEmpty(inst.getRegNumber());
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public boolean hasDivisionRegistered()
    {
        Division division = AppContextMgr.getInstance().getClassObject(Division.class);
        return division != null && StringUtils.isNotEmpty(division.getRegNumber());
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public boolean hasDisciplineRegistered()
    {
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        return discipline != null && StringUtils.isNotEmpty(discipline.getRegNumber());
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public boolean hasCollectionRegistered()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        return collection != null && StringUtils.isNotEmpty(collection.getRegNumber());
    }
    
    /**
     * 
     */
    public void setHasBeenAsked()
    {
        AppPreferences.getRemote().putBoolean(REGPREF_NAME, true); // the user has been asked
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private void setInstitutionHasAutoRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
        
        Institution inst = acMgr.getClassObject(Institution.class);
        inst.setRegNumber(regNumber);
        Institution.save(inst);
        
        inst = Institution.getDataObj(Institution.class, inst.getId());
        if (inst != null)
        {
            acMgr.setClassObject(Institution.class, inst);
        }
        
        doStartRegister(RegisterType.Division, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private void setDivisionHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
                
        Division division = acMgr.getClassObject(Division.class);
        division.setRegNumber(regNumber);
        Division.save(division);
        
        division = Division.getDataObj(Division.class, division.getId());
        if (division != null)
        {
            acMgr.setClassObject(Division.class, division);
        }
        
        doStartRegister(RegisterType.Discipline, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private void setDisciplineHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
                
        Discipline discipline = acMgr.getClassObject(Discipline.class);
        discipline.setRegNumber(regNumber);
        Discipline.save(discipline);
        
        discipline = Discipline.getDataObj(Discipline.class, discipline.getId());
        if (discipline != null)
        {
            acMgr.setClassObject(Discipline.class, discipline);
        }
        
        doStartRegister(RegisterType.Collection, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private void setCollectionHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        collection.setRegNumber(regNumber);
        Collection.save(collection);
    }
    
    /**
     * 
     */
    protected void doStartRegister(final RegisterType regType, final boolean isAnonymous)
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
                    return doRegisterInternal(regType, isAnonymous);
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
                    if (retVal instanceof String)
                    {
                        String regNumber = (String)retVal;
                        switch (regType)
                        {
                            case Institution : 
                                setInstitutionHasAutoRegistered(regNumber, isAnonymous);
                                break;
                                
                            case Division : 
                                setDivisionHasRegistered(regNumber, isAnonymous);
                                break;
                                    
                            case Discipline : 
                                setDisciplineHasRegistered(regNumber, isAnonymous);
                                break;
                                    
                            case Collection :
                                setCollectionHasRegistered(regNumber, isAnonymous);
                                break;
                        } // switch
                    }
                }
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    /**
     * @return
     * @throws Exception if an IO error occurred or the response couldn't be parsed
     */
    protected String doRegisterInternal(final RegisterType regType, final boolean isAnonymous) throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getRegisterURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // get the POST parameters
        NameValuePair[] postParams = createPostParameters(regType, isAnonymous);
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
        
        if (StringUtils.isNotEmpty(responseString))
        {
            String[] tokens = StringUtils.split(responseString);
            if (tokens.length == 2 && tokens[0].equals("1"))
            {
                return tokens[1];
            }
        }
        
        return null;
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
    
    /**
     * @param value
     * @return
     */
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
    protected NameValuePair[] createPostParameters(final RegisterType regType, final boolean isAnonymous)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();

        // get the install ID
        String installID = UsageTracker.getInstallId();
        postParams.add(new NameValuePair("id", installID)); //$NON-NLS-1$

        // get the OS name and version
        postParams.add(new NameValuePair("reg_type",     regType.toString()));
        postParams.add(new NameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$
        
        //postParams.add(new NameValuePair("user_name",    System.getProperty("user.name"))); //$NON-NLS-1$

        AppContextMgr acMgr      = AppContextMgr.getInstance();
        Institution   inst       = acMgr.getClassObject(Institution.class);
        Division      division   = acMgr.getClassObject(Division.class);
        Discipline    discipline = acMgr.getClassObject(Discipline.class);
        Collection    collection = acMgr.getClassObject(Collection.class);
        
        switch (regType)
        {
            case Institution:
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Institution_name",  fixParam(inst.getName()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("Institution_title", fixParam(inst.getTitle()))); //$NON-NLS-1$
                }
                break;
                
            case Division:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Division_name",      fixParam(division.getName()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("Division_title",     fixParam(division.getTitle()))); //$NON-NLS-1$
                }
                break;
                
            case Discipline:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Division_number",    fixParam(division.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Discipline_name",    fixParam(discipline.getName()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("Discipline_title",   fixParam(discipline.getTitle()))); //$NON-NLS-1$
                }
                break;
                
            case Collection:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Division_number",    fixParam(division.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Discipline_number",  fixParam(discipline.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Collection_name",    fixParam(collection.getCollectionName()))); //$NON-NLS-1$
                }
                break;
        } // switch
                
        if (!isAnonymous)
        {
            SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            postParams.add(new NameValuePair("User_name",  fixParam(user.getName()))); //$NON-NLS-1$
            postParams.add(new NameValuePair("User_email", fixParam(user.getEmail()))); //$NON-NLS-1$
            
            Address addr = inst.getAddress();
            if (addr != null)
            {
                postParams.add(new NameValuePair("Address", fixParam(addr.getIdentityTitle()))); //$NON-NLS-1$
            }
        }
        
        // Create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }
        return paramArray;
    }
    
    
    /**
     * 
     */
    public static void register(final boolean forceRegistration)
    {
        RegisterSpecify reg = new RegisterSpecify();
        
        AppPreferences remotePrefs = AppPreferences.getRemote();
        
        Boolean isAnonymousObj = remotePrefs.getBoolean(reg.ANON_REG, null);
        boolean isAnonymous    = isAnonymousObj != null ? isAnonymousObj : true;
        
        if (!reg.hasInstitutionRegistered() || (forceRegistration && isAnonymous))
        {
            if (forceRegistration)
            {
                remotePrefs.putBoolean(reg.ANON_REG, false);
                reg.doStartRegister(RegisterType.Institution, false); // will register everything 
                
            } else if (!reg.hasAskedToRegInstitution())
            {
                Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                
                boolean okToReg = reg.askToReg(DBTableIdMgr.getInstance().getTitleForId(Institution.getClassTableId()), StringUtils.isNotEmpty(inst.getTitle()) ? inst.getTitle() : inst.getName());
                reg.setHasBeenAsked();
                
                if (okToReg)
                {
                    reg.doStartRegister(RegisterType.Institution, false); // will register everything
                    
                } else if (isAnonymousObj == null)
                {
                    remotePrefs.putBoolean(reg.ANON_REG, true);
                    reg.doStartRegister(RegisterType.Institution, true);
                }
            }
            
        } else 
        {
            // Institution has been registered.
            // so it is OK to register unregistered Disciplines and Collections
            if (!reg.hasDisciplineRegistered())
            {
                reg.doStartRegister(RegisterType.Discipline, isAnonymous);
                
            } else if (!reg.hasDivisionRegistered())
            {
                reg.doStartRegister(RegisterType.Division, isAnonymous);
                
            } else if (!reg.hasCollectionRegistered())
            {
                reg.doStartRegister(RegisterType.Collection, isAnonymous);
            }
        }
    }

    
    public static class ConnectionException extends IOException
    {
        public ConnectionException(@SuppressWarnings("unused") Throwable e)
        {
            super();
        }
    }
}
