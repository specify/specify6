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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class is used for auto registering collections.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 17, 2008
 *
 */
public class RegisterSpecify
{
    private static final String EXTRA_CHECK = "extra.check";

    private enum RegisterType { Institution, Division, Discipline, Collection }
    
    private static RegisterSpecify instance   = new RegisterSpecify();
    
    private boolean hasConnection = false;
    private Boolean isFirstReg    = null;
    
    
    /**
     * Constructor.
     */
    private RegisterSpecify()
    {
        super();
    }
    
    /**
     * @return the instance
     */
    public static RegisterSpecify getInstance()
    {
        return instance;
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
                                        getLocalizedMessage("SpReg.DO_REG", typeTitle, typeName),  //$NON-NLS-1$
                                        getResourceString("SpReg.DO_REG_TITLE"),  //$NON-NLS-1$
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        return userChoice == JOptionPane.YES_OPTION;
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if auto update checking is turned on
     */
    public static boolean hasInstitutionRegistered()
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        boolean isReg =  inst != null && StringUtils.isNotEmpty(inst.getRegNumber());
        
        if (getInstance().isFirstReg == null)
        {
            getInstance().isFirstReg = isReg;
        }
        return isReg;
    }
    
    /**
     * @return
     */
    public static boolean isAnonymous()
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        return inst != null ? inst.getIsAnonymous() : false;
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public static boolean hasDivisionRegistered()
    {
        Division division = AppContextMgr.getInstance().getClassObject(Division.class);
        return division != null && StringUtils.isNotEmpty(division.getRegNumber());
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public static boolean hasDisciplineRegistered()
    {
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        return discipline != null && StringUtils.isNotEmpty(discipline.getRegNumber());
    }
    
    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if update checking is turned on
     */
    public static boolean hasCollectionRegistered()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        return collection != null && StringUtils.isNotEmpty(collection.getRegNumber());
    }
    
    /**
     * @return the hasConnection
     */
    public static boolean hasConnection()
    {
        return getInstance().hasConnection;
    }

    /**
     * 
     */
    @SuppressWarnings({ "unchecked" })
    private <T> T update(final Class<?> cls, final Object dataObjArg)
    {
        Object dataObj = dataObjArg;
        DataModelObjBase.save(dataObj);
        
        ((DataModelObjBase)dataObj).forceLoad();
        
        dataObj = DataModelObjBase.getDataObj(cls, ((DataModelObjBase)dataObj).getId());
        if (dataObj != null)
        {
            AppContextMgr.getInstance().setClassObject(cls, dataObj);
        }
        return (T)dataObj;
    }
    
    /**
     * 
     */
    public static Institution setHasBeenAsked()
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        inst.setHasBeenAsked(true);
        inst = getInstance().update(Institution.class, inst);
        return inst;
    }
    
    /**
     * 
     */
    public static Institution setIsAnonymous(final boolean isAnonymous)
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        inst.setIsAnonymous(isAnonymous);
        inst = getInstance().update(Institution.class, inst);
        return inst;
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private static void setInstitutionHasAutoRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
        
        Institution inst = acMgr.getClassObject(Institution.class);
        inst.setRegNumber(regNumber);
        inst = getInstance().update(Institution.class, inst);
        
        getInstance().isFirstReg = true;
        
        getInstance().doStartRegister(RegisterType.Division, isAnonymous, false);
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
        update(Division.class, division);
        
        doStartRegister(RegisterType.Discipline, isAnonymous, false);
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
        update(Discipline.class, discipline);
        
        doStartRegister(RegisterType.Collection, isAnonymous, false);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private void setCollectionHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        collection.setRegNumber(regNumber);
        update(Collection.class, collection);
        
        if (!isFirstReg && !isAnonymous())
        {
            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SpReg.REG_TITLE", "SpReg.REG_OK");
        }
    }
    
    /**
     * @param regType
     * @param isAnonymous
     * @param isForISANumber
     */
    private void doStartRegister(final RegisterType regType, 
                                 final boolean      isAnonymous,
                                 final boolean      isForISANumber)
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
                    return doRegisterInternal(regType, isAnonymous, isForISANumber);
                }
                catch (ConnectionException e)
                {
                    // do nothing
                    return null;
                    
                } catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RegisterSpecify.class, e);
                    // if any exceptions occur, return them so the finished() method can have them
                    return e;
                }
            }
            
            @Override
            public void finished()
            {
                Object retVal = getValue();
                if (retVal != null)
                {
                    // if an exception occurred during update check...
                    if (retVal instanceof String)
                    {
                        if (!isForISANumber)
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
                            
                        } else
                        {
                            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                            
                            String isaTitle = getResourceString("SpReg.ISA_TITLE");

                            collection = update(Collection.class, collection);
                            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, isaTitle, "SpReg.ISA_ACCEPTED", collection.getIsaNumber());
                            return;
                        }
                    }
                } else if (isForISANumber && RegisterSpecify.this.hasConnection)
                {
                    UIRegistry.showLocalizedError("SpReg.ISA_ERROR");
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
    private String doRegisterInternal(final RegisterType regType, 
                                      final boolean isAnonymous,
                                      final boolean isForISANumber) throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", RegisterSpecify.class.getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getRegisterURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // get the POST parameters
        NameValuePair[] postParams = createPostParameters(regType, isAnonymous, isForISANumber);
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
        }
        catch (Exception e)
        {
            hasConnection = false;
            //UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RegisterSpecify.class, e);
            e.printStackTrace();
            throw new ConnectionException(e);
        }
        
        // get the server response
        String responseString = postMethod.getResponseBodyAsString();
        
        if (StringUtils.isNotEmpty(responseString))
        {
            //System.err.println(responseString);
            
            String[] tokens = StringUtils.split(responseString);
            if (tokens.length == 2 && tokens[0].equals("1"))
            {
                return tokens[1];
                
            } else if (isForISANumber && tokens.length == 1 && tokens[0].equals("1"))
            {
                return tokens[0];
            }
        }
        
        return null;
    }
    
    /**
     * Gets the URL of the version checking / usage tracking server.
     * 
     * @return the URL string
     */
    public static String getRegisterURL()
    {
        String baseURL = getResourceString("SpReg.REGISTER_URL"); //$NON-NLS-1$
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
    private NameValuePair[] createPostParameters(final RegisterType regType, 
                                                          final boolean isAnonymous,
                                                          final boolean isForISANumber)
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
        
        if (isForISANumber)
        {
            postParams.add(new NameValuePair("reg_isa",  "true")); //$NON-NLS-1$
            postParams.add(new NameValuePair("reg_number",  collection.getRegNumber())); //$NON-NLS-1$
        }
        
        switch (regType)
        {
            case Institution:
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Institution_name",  fixParam(inst.getName()))); //$NON-NLS-1$
                }
                break;
                
            case Division:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Division_name",      fixParam(division.getName()))); //$NON-NLS-1$
                }
                break;
                
            case Discipline:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Division_number",    fixParam(division.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Discipline_type",    fixParam(discipline.getType()))); //$NON-NLS-1$
                }
                break;
                
            case Collection:
                postParams.add(new NameValuePair("Institution_number", fixParam(inst.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Division_number",    fixParam(division.getRegNumber()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Discipline_number",  fixParam(discipline.getRegNumber()))); //$NON-NLS-1$
                if (!isAnonymous)
                {
                    postParams.add(new NameValuePair("Discipline_type", fixParam(discipline.getType()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("Collection_name", fixParam(collection.getCollectionName()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("ISA_Number",       fixParam(collection.getIsaNumber()))); //$NON-NLS-1$
                }
                break;
        } // switch
                
        if (!isAnonymous)
        {
            SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            //postParams.add(new NameValuePair("User_name",  fixParam(user.getName()))); //$NON-NLS-1$
            postParams.add(new NameValuePair("User_email", fixParam(user.getEmail()))); //$NON-NLS-1$
            
            Address addr = inst.getAddress();
            if (addr != null)
            {
                postParams.add(new NameValuePair("Address", fixParam(addr.getIdentityTitle()))); //$NON-NLS-1$
                postParams.add(new NameValuePair("Phone", fixParam(addr.getPhone1()))); //$NON-NLS-1$
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
     * Registers the Institution and makes it be not Anonymous anymore.
     */
    public static void register(final boolean forceRegistration)
    {
        getInstance().registerInternal(forceRegistration);
    }

    /**
     * Registers the Institution and makes it be not Anonymous anymore. (Non-static version)
     */
    private void registerInternal(final boolean forceRegistration)
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        if (forceRegistration && !spUser.getUserType().equals(SpecifyUserTypes.UserType.Manager.toString()))
        {
            if (forceRegistration)
            {
                UIRegistry.showLocalizedMsg(null, "SpReg.MUSTBECM");
            }
            return;
        }
        
        Institution inst         = AppContextMgr.getInstance().getClassObject(Institution.class);
        Collection  collection   = AppContextMgr.getInstance().getClassObject(Collection.class);
        
        Boolean     hasBeenAsked = inst.getHasBeenAsked();
        boolean     isAnonymous  = isAnonymous();
        
        if (!hasInstitutionRegistered() || (forceRegistration && isAnonymous))
        {
            if (forceRegistration)
            {
                if (!isAnonymous) // this really shouldn't happen
                {
                    UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SpReg.REGISTER", "SpReg.ITMS_REGED");
                    return;
                }
                
                setIsAnonymous(false);
                
                // if it failt to get the registration numbers the first time, try again.
                if (!hasInstitutionRegistered())
                {
                    doStartRegister(RegisterType.Institution, false, false); // will register everything
                }
                localPrefs.putBoolean(EXTRA_CHECK, true);
                UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SpReg.REGISTER", "SpReg.ITMS_REGED");
                
            } else if (!hasBeenAsked)
            {
                boolean okToReg  = askToReg(DBTableIdMgr.getInstance().getTitleForId(Collection.getClassTableId()), collection.getCollectionName());
                setHasBeenAsked();
                
                if (okToReg)
                {
                    setIsAnonymous(false);
                    doStartRegister(RegisterType.Institution, false, false); // will register everything
                    
                } else
                {
                    setIsAnonymous(true);
                    doStartRegister(RegisterType.Institution, true, false);
                    localPrefs.putBoolean(EXTRA_CHECK, false);
                }
            }
            
        } else 
        {
            // Institution has been registered.
            // so it is OK to register unregistered Disciplines and Collections
            if (!hasDisciplineRegistered())
            {
                doStartRegister(RegisterType.Division, isAnonymous, false);
                
            } else if (!hasDivisionRegistered())
            {
                doStartRegister(RegisterType.Discipline, isAnonymous, false);
                
            } else if (!hasCollectionRegistered())
            {
                doStartRegister(RegisterType.Collection, isAnonymous, false);
                
            } else if (forceRegistration)
            {
                showRegisteredNumbers(isAnonymous);
            }
        }
    }
    
    /**
     * @param isAnonymous
     */
    private void showRegisteredNumbers(final boolean isAnonymous)
    {
        if (!hasInstitutionRegistered() || isAnonymous)
        {
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "10px,p,10px"));
            
            pb.add(UIHelper.createI18NLabel("SpReg.ITMS_NOT_REGED"), cc.xy(1,2));
            pb.setDefaultDialogBorder();
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), getResourceString("SpReg.REG_TITLE"), true, CustomDialog.OKCANCELHELP, pb.getPanel());
            dlg.setCancelLabel(getResourceString("CLOSE"));
            dlg.setOkLabel(getResourceString("SpReg.REGISTER"));
            
            dlg.setVisible(true);
            
            if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
            {
                setIsAnonymous(false);
                doStartRegister(RegisterType.Institution, false, false); // will register everything 
            }
        } else
        {
            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SpReg.REGISTER", "SpReg.ITMS_REGED");
        }
    }

    /**
     * @return the ISA number or null
     */
    public static String getISANumber()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        return collection.getIsaNumber();
    }

    /**
     * Registers the ISA number.
     */
    public static void registerISA()
    {
        SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        if (!spUser.getUserType().equals(SpecifyUserTypes.UserType.Manager.toString()))
        {
            UIRegistry.showLocalizedMsg("", "SpReg.MUSTBECM");
            return;
        }
        
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        String     isaNumber  = collection.getIsaNumber();
        String     isaTitle   = getResourceString("SpReg.ISA_TITLE");
        
        if (StringUtils.isNotEmpty(isaNumber))
        {
            String msg = UIRegistry.getLocalizedMessage("SpReg.ISA_NUM", isaNumber);
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), msg, isaTitle, JOptionPane.INFORMATION_MESSAGE);
            
        } else
        {
            final JTextField textField = UIHelper.createTextField(30);
            isaNumber = textField.getText();
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,10px,p"));
            pb.add(UIHelper.createI18NFormLabel("SpReg.ISA_ENT"), cc.xy(1, 1));
            pb.add(textField, cc.xy(3, 1));
            pb.add(UIHelper.createI18NLabel("SpReg.ISA_EXPL"), cc.xyw(1, 3, 3));
            pb.setDefaultDialogBorder();
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), isaTitle, true, pb.getPanel());
            dlg.createUI();
            final JButton okBtn = dlg.getOkBtn();
            okBtn.setEnabled(false);
            
            textField.getDocument().addDocumentListener(new DocumentAdaptor() {
                @Override
                protected void changed(DocumentEvent e)
                {
                    if (StringUtils.isNotEmpty(textField.getText()) != okBtn.isEnabled())
                    {
                        okBtn.setEnabled(!okBtn.isEnabled());
                    }
                }
            });
            
            dlg.setVisible(true);
            isaNumber = textField.getText();
            if (!dlg.isCancelled() && StringUtils.isNotEmpty(isaNumber))
            {
                setIsAnonymous(false);
                
                collection.setIsaNumber(isaNumber);
                
                getInstance().doStartRegister(RegisterType.Collection, false, true);
                
                AppPreferences.getLocalPrefs().putBoolean(EXTRA_CHECK, true);
            }
        }
    }
    
    /**
     *
     */
    public static class ConnectionException extends IOException
    {
        public ConnectionException(@SuppressWarnings("unused") Throwable e)
        {
            super();
        }
    }
    
    /*
    public static void main(String[] args)
    {
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setParameter("http.useragent", RegisterSpecify.class.getName()); //$NON-NLS-1$
            
            // get the URL of the website to check, with usage info appended, if allowed
            String versionCheckURL = getRegisterURL();
            
            PostMethod postMethod = new PostMethod(versionCheckURL);
            
            // get the POST parameters
            NameValuePair[] postParams = new NameValuePair[2];
            postParams[0] = new NameValuePair("reg_number", "XXX");
            postParams[1] = new NameValuePair("test", "ZZZZ");
            postMethod.setRequestBody(postParams);
            
            // connect to the server
            try
            {
                httpClient.executeMethod(postMethod);
            }
            catch (Exception e)
            {
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RegisterSpecify.class, e);
                throw new ConnectionException(e);
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RegisterSpecify.class, ex);
            ex.printStackTrace();
        }
    }*/
}
