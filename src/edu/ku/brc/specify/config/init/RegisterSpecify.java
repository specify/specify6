package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

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
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.SwingWorker;
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
 * @code_status Alpha
 *
 * Oct 17, 2008
 *
 */
public class RegisterSpecify
{
    private enum RegisterType { Institution, Division, Discipline, Collection }
    
    private static boolean isFirstReg = false;
    
    
    /**
     * Constructor.
     */
    private RegisterSpecify()
    {
        super();
    }
    
    /**
     * @param title
     * @return
     */
    private static boolean askToReg(final String typeTitle, final String typeName)
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
        return inst != null && StringUtils.isNotEmpty(inst.getRegNumber());
    }
    
    public static boolean isAnonymous()
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        return inst != null ? inst.getIsAnonymous() == null ? false : inst.getIsAnonymous() : false;
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
     * 
     */
    @SuppressWarnings({ "unchecked" })
    private static <T> T update(final Class<?> cls, final Object dataObjArg)
    {
        Object dataObj = dataObjArg;
        DataModelObjBase.save(dataObj);
        
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
        inst = update(Institution.class, inst);
        return inst;
    }
    
    /**
     * 
     */
    public static Institution setIsAnonymous(final boolean isAnonymous)
    {
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        inst.setIsAnonymous(isAnonymous);
        inst = update(Institution.class, inst);
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
        inst = update(Institution.class, inst);
        
        isFirstReg = true;
        
        doStartRegister(RegisterType.Division, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private static void setDivisionHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
                
        Division division = acMgr.getClassObject(Division.class);
        division.setRegNumber(regNumber);
        update(Division.class, division);
        
        doStartRegister(RegisterType.Discipline, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private static void setDisciplineHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        AppContextMgr acMgr = AppContextMgr.getInstance();
                
        Discipline discipline = acMgr.getClassObject(Discipline.class);
        discipline.setRegNumber(regNumber);
        update(Discipline.class, discipline);
        
        doStartRegister(RegisterType.Collection, isAnonymous);
    }
    
    /**
     * @param regNumber
     * @param isAnonymous
     */
    private static void setCollectionHasRegistered(final String regNumber, final boolean isAnonymous)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        collection.setRegNumber(regNumber);
        update(Collection.class, collection);
        
        if (isFirstReg && !isAnonymous())
        {
            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SpReg.REG_TITLE", "SpReg.REG_OK");
        }
    }
    
    /**
     * 
     */
    protected static void doStartRegister(final RegisterType regType, final boolean isAnonymous)
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
    protected static String doRegisterInternal(final RegisterType regType, final boolean isAnonymous) throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", RegisterSpecify.class.getName()); //$NON-NLS-1$
        
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
    protected static String getRegisterURL()
    {
        String baseURL = getResourceString("SpReg.REGISTER"); //$NON-NLS-1$
        return baseURL;
    }
    
    /**
     * @param value
     * @return
     */
    private static String fixParam(final String value)
    {
        return value == null ? "" : value;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param sendUsageStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected static NameValuePair[] createPostParameters(final RegisterType regType, final boolean isAnonymous)
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
                    postParams.add(new NameValuePair("Collection_name", fixParam(collection.getCollectionName()))); //$NON-NLS-1$
                    postParams.add(new NameValuePair("SA_Number",       fixParam(collection.getSaNumber()))); //$NON-NLS-1$
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
        Institution inst         = AppContextMgr.getInstance().getClassObject(Institution.class);
        Collection  collection   = AppContextMgr.getInstance().getClassObject(Collection.class);
        
        Boolean     hasBeenAsked = inst.getHasBeenAsked() != null && inst.getHasBeenAsked();
        boolean     isAnonymous  = isAnonymous();
        
        if (!hasInstitutionRegistered() || (forceRegistration && !isAnonymous))
        {
            if (forceRegistration)
            {
                inst = setIsAnonymous(false);
                doStartRegister(RegisterType.Institution, false); // will register everything 
                
            } else if (!hasBeenAsked)
            {
                boolean okToReg  = askToReg(DBTableIdMgr.getInstance().getTitleForId(Collection.getClassTableId()), collection.getCollectionName());
                setHasBeenAsked();
                
                if (okToReg)
                {
                    setIsAnonymous(false);
                    doStartRegister(RegisterType.Institution, false); // will register everything
                    
                } else
                {
                    setIsAnonymous(true);
                    doStartRegister(RegisterType.Institution, true);
                }
            }
            
        } else 
        {
            // Institution has been registered.
            // so it is OK to register unregistered Disciplines and Collections
            if (!hasDisciplineRegistered())
            {
                doStartRegister(RegisterType.Division, isAnonymous);
                
            } else if (!hasDivisionRegistered())
            {
                doStartRegister(RegisterType.Discipline, isAnonymous);
                
            } else if (!hasCollectionRegistered())
            {
                doStartRegister(RegisterType.Collection, isAnonymous);
                
            } else if (forceRegistration)
            {
                showRegisteredNumbers(isAnonymous);
            }
        }
    }
    
    /**
     * @param isAnonymous
     */
    protected static void showRegisteredNumbers(final boolean isAnonymous)
    {
        String sqlStr = "SELECT inst.Name, inst.Title, inst.RegNumber, dv.Name, dv.Title, dv.RegNumber, ds.Name, ds.Title, ds.RegNumber, cl.CollectionName, cl.RegNumber, cl.SANumber " + 
                        "FROM division dv INNER JOIN institution inst ON dv.InstitutionID = inst.UserGroupScopeId " +
                        "INNER JOIN discipline ds ON ds.DivisionID = dv.UserGroupScopeId " +
                        "INNER JOIN collection cl ON cl.DisciplineID = ds.UserGroupScopeId ORDER BY inst.Name, inst.Title, dv.Name, dv.Title, ds.Name, ds.Title, cl.CollectionName";
        
        Vector<Vector<String>> rows = new Vector<Vector<String>>();
        
        Connection dbConnection = null;
        Statement  dbStatement  = null;
        try
        {
            dbConnection = DBConnection.getInstance().getConnection();
            dbStatement = dbConnection.createStatement();
    
            ResultSet rs = dbStatement.executeQuery(sqlStr);
            while (rs.next())
            {
                Vector<String> row = new Vector<String>();
                
                String str = rs.getString(2);
                row.add(StringUtils.isNotEmpty(str) ? str : rs.getString(1));
                str = rs.getString(3);
                row.add(StringUtils.isNotEmpty(str) ? str : "");
                
                str = rs.getString(5);
                row.add(StringUtils.isNotEmpty(str) ? str : rs.getString(4));
                str = rs.getString(6);
                row.add(StringUtils.isNotEmpty(str) ? str : "");
                
                str = rs.getString(8);
                row.add(StringUtils.isNotEmpty(str) ? str : rs.getString(7));
                str = rs.getString(9);
                row.add(StringUtils.isNotEmpty(str) ? str : "");
                
                row.add(rs.getString(10));
                str = rs.getString(11);
                row.add(StringUtils.isNotEmpty(str) ? str : "");
                str = rs.getString(12);
                row.add(StringUtils.isNotEmpty(str) ? str : "");

                rows.add(row);
            }
            
            dbStatement.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        Object[][] objRows = new Object[rows.size()][9];
        String[]   strs    = new String[9];
        for (int i=0;i<strs.length;i++) strs[i] = "";
        
        int r = 0;
        for (Vector<String> row : rows)
        {
            int c = 0;
            for (String col : row)
            {
                if (!col.equals(strs[c]))
                {
                    strs[c] = col;
                } else
                {
                    col = "";
                }
                objRows[r][c++] = col;
            }
            r++;
        }
        
        String numberTitle = "SpReg.Number";
        DBTableIdMgr dm = DBTableIdMgr.getInstance();
        final JTable table = new JTable(new DefaultTableModel(objRows, new String[] {dm.getTitleForId(Institution.getClassTableId()), numberTitle, 
                                                                                     dm.getTitleForId(Division.getClassTableId()),    numberTitle, 
                                                                                     dm.getTitleForId(Discipline.getClassTableId()),  numberTitle, 
                                                                                     dm.getTitleForId(Collection.getClassTableId()),  numberTitle,
                                                                                     "SA Number"}) {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        
        });
        UIHelper.calcColumnWidths(table);
        UIHelper.makeTableHeadersCentered(table, false);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,6px,p"));
        
        String desc = "<html>" + getResourceString("SpReg.ITMS_REGED") + (isAnonymous ? (" \'<font color=\'red\'>" + getResourceString("SpReg.ANON") + "</font>\'") : "") + ".</html>";
        pb.add(UIHelper.createLabel(desc), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(table), cc.xy(1,3));
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), getResourceString("SpReg.REG_TITLE"), true, CustomDialog.OKCANCELHELP, pb.getPanel()) {
            @Override
            protected void cancelButtonPressed()
            {
                table.selectAll();
                TransferHandler th = table.getTransferHandler();
                if (th != null) 
                {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    th.exportToClipboard(table, cb, TransferHandler.COPY);
                }
                //table.clearSelection();
            }
            
        };
        dlg.setOkLabel(getResourceString("CLOSE"));
        dlg.setCancelLabel(getResourceString("COPY"));
        dlg.setVisible(true);
    }

    /**
     * 
     */
    public static void registerSA()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        String     saNumber   = collection.getSaNumber();
        String     saTitle    = getResourceString("SpReg.SA_TITLE");
        
        if (StringUtils.isNotEmpty(saNumber))
        {
            String msg = UIRegistry.getLocalizedMessage("SpReg.SA_NUM", saNumber);
            JOptionPane.showMessageDialog((Frame)UIRegistry.getTopWindow(), msg, saTitle, JOptionPane.INFORMATION_MESSAGE);
            
        } else
        {
            final JTextField textField = UIHelper.createTextField(30);
            saNumber = textField.getText();
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p"));
            pb.add(UIHelper.createI18NFormLabel("SpReg.SA_ENT"), cc.xy(1, 1));
            pb.add(textField, cc.xy(3, 1));
            pb.setDefaultDialogBorder();
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), saTitle, true, pb.getPanel());
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
            saNumber = textField.getText();
            if (!dlg.isCancelled() && StringUtils.isNotEmpty(saNumber))
            {
                collection.setSaNumber(saNumber);
                collection = update(Collection.class, collection);
                
                setIsAnonymous(false);
                
                UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, saTitle, "SpReg.SA_ACCEPTED", saNumber);
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
