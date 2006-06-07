package edu.ku.brc.specify.dbsupport;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

public class DatabaseLogon extends JDialog implements ActionListener, FocusListener
{
    final static Logger _logger       = Logger.getLogger(DatabaseLogon.class);

    private final JComboBox     hostPrompt;
    private final JTextField    userPrompt;
    private final JComboBox     dbPrompt;
    private final JTextField    pwdPrompt;

    private Vector<String>        databaseNames = new Vector<String>();

    private String[]    hostNames     = {
            "jdbc:mysql://129.237.201.166/SpecifySchemaChanges.html",
            "jdbc:mysql://localhost/", "jdbc:inetdae7://129.237.201.110/",
            "jdbc:mysql://129.237.201.110/",
            "jdbc:microsoft:sqlserver://129.237.201.110/" };                   // "jdbc:inetdae7://129.237.201.110/"};

    private String      hostName      = "";
    private String      databaseName  = "";
    private String      userName      = "rods";
    private String      password      = "rods";

    /**
     *
     */
    public DatabaseLogon()
    {

        JPanel basePanel = new JPanel();

        String[]     labelStrings = { "Enter a hostname:", "Enter a database: ", "Enter user name: ", "Enter a password: ", };
        JLabel[]     labels = new JLabel[labelStrings.length];
        JComponent[] fields = new JComponent[labelStrings.length];
        int fieldNum = 0;

        hostPrompt = new JComboBox(hostNames);
        hostPrompt.setEditable(true);

        // Create the text field and set it up.
        // dbPrompt = new JTextField(databaseName);
        // dbPrompt.setColumns(20);
        fields[fieldNum++] = hostPrompt;

        // the combo box (add/modify items if you like to)
        dbPrompt = new JComboBox(databaseNames);
        dbPrompt.setEditable(true);
        // Create the text field and set it up.
        // dbPrompt = new JTextField(databaseName);
        // dbPrompt.setColumns(20);
        fields[fieldNum++] = dbPrompt;

        userPrompt = new JTextField(userName);
        userPrompt.setColumns(20);
        fields[fieldNum++] = userPrompt;

        pwdPrompt = new JTextField(password);
        // pwdPrompt.setEchoChar('*');
        pwdPrompt.setColumns(20);
        fields[fieldNum++] = pwdPrompt; // JFormattedTextField()

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        // Associate label/field pairs, add everything,
        // and lay it out.
        for (int i = 0; i < labelStrings.length; i++)
        {
            labels[i] = new JLabel(labelStrings[i], JLabel.TRAILING);
            labels[i].setLabelFor(fields[i]);
            panel.add(labels[i]);
            panel.add(fields[i]);

            // Add listeners to each field.
            JComponent tf = (JComponent) fields[i];
            if (tf instanceof JTextField)
            {
                JTextField jtf = (JTextField) tf;
                jtf.addActionListener(this);
            }
            tf.addFocusListener(this);

        }

        final JButton button = new JButton("Ok");
        final JButton cancelButton = new JButton("Cancel");

        button.setActionCommand("Ok");
        button.addActionListener(this);

        cancelButton.setActionCommand("Ok");
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);
        buttonPanel.add(cancelButton);
        basePanel.setLayout(new GridLayout(0, 1));
        basePanel.add(panel);
        basePanel.add(buttonPanel);
        this.getContentPane().add(basePanel);
        this.setTitle("Enter database credentials");
        this.pack();
        this.setModal(true);
        this.setVisible(true);
        // this.addWindowListener(new java.awt.event.WindowAdapter() {
        // public void windowClosing(java.awt.event.WindowEvent e) {
        // System.exit(0); // Terminate when the last window is closed.

        // }
        // });

    }

    /**
     * Called when the user clicks the button or presses Enter in a text field.
     */
    public void actionPerformed(ActionEvent e)
    {
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

    }

    private void setHostName(String s)
    {
        this.hostName = s;

    }

    private void setDatabaseName(String s)
    {
        this.databaseName = s;

    }

    private void setUserName(String s)
    {
        this.userName = s;

    }

    private void setPassword(String s)
    {
        this.password = s;

    }

    public String getHostName()
    {
        return this.hostName;

    }

    public String getDatabaseName()
    {
        return this.databaseName;

    }

    public String getUserName()
    {
        return this.userName;

    }

    public String getPassword()
    {
        return this.password;

    }

    /**
     * Called when one of the fields gets the focus so that
     * we can select the focused field.
     */
    public void focusGained(FocusEvent e)
    {
        _logger.debug("focus ganined");
        Component c = e.getComponent();

        ((JTextField) c).selectAll();

    }

    //Needed for FocusListener interface.
    public void focusLost(FocusEvent e)
    {
        _logger.debug("focuse lost");
    } //ignore
}
