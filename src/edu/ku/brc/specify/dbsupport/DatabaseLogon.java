package edu.ku.brc.specify.dbsupport;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.validation.ValComboBox;
import edu.ku.brc.specify.ui.validation.ValPasswordField;
import edu.ku.brc.specify.ui.validation.ValTextField;

public class DatabaseLogon extends JDialog implements ActionListener, FocusListener
{
    private static final Logger log  = Logger.getLogger(DatabaseLogon.class);
    
    // Form Stuff
    protected MultiView      multiView;
    protected View           formView;
    protected Viewable       form;
    protected List<String>   fieldNames;

    private final ValTextField     username;
    private final ValPasswordField password;
    
    private final ValComboBox      databases;
    private final ValComboBox      servers;

    private Vector<String>        databaseNames = new Vector<String>();
    private Vector<String>        serverNames = new Vector<String>();
    private JDialog               thisDlg;

    private String[]    serverNameStrs     = {
            "jdbc:mysql://localhost/", 
            "jdbc:inetdae7://129.237.201.110/",
            "jdbc:mysql://129.237.201.110/",
            "jdbc:microsoft:sqlserver://129.237.201.110/" };                   // "jdbc:inetdae7://129.237.201.110/"};

    //private String      hostName      = "";
    //private String      databaseName  = "";
    //private String      userName      = "rods";
    //private String      password      = "rods";

    /**
     *
     */
    public DatabaseLogon()
    {
        thisDlg = this;
        
        createUI("SystemSetup", "DatabaseLogon", "Specify Login");
        
        username  = (ValTextField)form.getCompById("username");
        password  = (ValPasswordField)form.getCompById("password");
        
        databases = (ValComboBox)form.getCompById("databases");
        servers   = (ValComboBox)form.getCompById("servers");
        
        Collections.addAll(serverNames, serverNameStrs);
        
        servers.setModel(new DefaultComboBoxModel(serverNames));
        servers.getComboBox().setSelectedIndex(0);


        JButton closeBtn = (JButton)form.getCompById("cancel");
        JButton loginBtn = (JButton)form.getCompById("login");
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);
        
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                thisDlg.dispose();
            }
         });
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doLogin();
            }
         });
        
        ((JButton)form.getCompById("getDBs")).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                fillDatabaseList();
            }
         });
        
        pack();
    }
    
    /**
     * 
     */
    protected void fillDatabaseList()
    {
        //DBConnection.setDriver((String)servers.getComboBox().getSelectedItem());
        //DBConnection.setUsernamePassword(username.getText(), password.getText());
        

        

    }
    
    /**
     * 
     */
    protected void doLogin()
    {
        
    }
    
    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
        formView = ViewMgr.getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView   = new MultiView(null, formView, AltView.CreationMode.Edit, false, true);
            form = multiView.getCurrentView();//ViewFactory.createFormView(null, formView, null, null);
            form.getValidator().validateForm();
            
            //add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        /*
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(multiView, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new NavBoxLayoutManager(0,2));

        
        JButton closeBtn = new JButton(getResourceString("Close"));
        closeBtn.addActionListener(this);
        getRootPane().setDefaultButton(closeBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { closeBtn });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);
*/
        setContentPane(multiView);
        pack();
    }

    /**
     * Called when the user clicks the button or presses Enter in a text field.
     */
    public void actionPerformed(ActionEvent e)
    {
        /*
        _logger.debug("actionperformaed");
        if ("Ok".equals(e.getActionCommand()))
        {
            setDatabaseName((String) dbPrompt.getSelectedItem());
            setUserName(userPrompt.getText());
            setPassword(pwdPrompt.getText());
            setHostName((String) hostPrompt.getSelectedItem());
            this.dispose();

        } else if ("Cancel".equals(e.getActionCommand()))
        {
            System.exit(0);
        }
*/
    }

    private void setHostName(String s)
    {
        //this.hostName = s;

    }

    private void setDatabaseName(String s)
    {
        //this.databaseName = s;

    }

    private void setUserName(String s)
    {
        //this.userName = s;

    }

    private void setPassword(String s)
    {
        //this.password = s;

    }

    public String getHostName()
    {
        return null;//this.hostName;

    }

    public String getDatabaseName()
    {
        return null;//this.databaseName;

    }

    public String getUserName()
    {
        return null;//this.userName;

    }

    public String getPassword()
    {
        return null;//this.password;

    }

    /**
     * Called when one of the fields gets the focus so that
     * we can select the focused field.
     */
    public void focusGained(FocusEvent e)
    {
        log.debug("focus ganined");
        //Component c = e.getComponent();

        //((JTextField) c).selectAll();

    }

    //Needed for FocusListener interface.
    public void focusLost(FocusEvent e)
    {
        log.debug("focuse lost");
    } //ignore
}
