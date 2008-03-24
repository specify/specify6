/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

/**
 * This is the configuration window for create a new user and new database.
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public abstract class BaseSetupPanel extends JPanel
{
    protected static final boolean DO_DEBUG = false;
    
    protected String             panelName;
    protected KeyAdapter         keyAdapter;
    protected JButton            nextBtn;
    
    public BaseSetupPanel(final String panelName,
                          final JButton nextBtn)
    {
        this.panelName = panelName;
        this.nextBtn   = nextBtn;
        
        keyAdapter = new KeyAdapter() {
          public void keyPressed(KeyEvent e)
          {
              nextBtn.setEnabled(isUIValid());
          }
        };
    }
    
    /**
     * @return the panelName
     */
    public String getPanelName()
    {
        return panelName;
    }
    
    protected String makeName(final String fName)
    {
        return makeName(panelName, fName);
    }
    
    public static String makeName(final String pName, final String fName)
    {
        return pName+"_"+fName;
    }

    protected abstract void getValues(Properties props);
    
    protected abstract void setValues(Properties values);
    
    protected abstract boolean isUIValid();
    
    protected abstract void updateBtnUI();
    
    /**
     * Helper function for creating the UI.
     * @param builder builder
     * @param label the string label
     * @param row the row to place it on
     * @return the create JTextField (or JPasswordField)
     */
    protected JTextField createField(final PanelBuilder builder, final String label, final int row)
    {
        return createField(builder, label, row, false);
    }
    
    /**
     * Helper function for creating the UI.
     * @param builder builder
     * @param label the string label
     * @param row the row to place it on
     * @param isPassword whether to create a password or text field
     * @return the create JTextField (or JPasswordField)
     */
    protected JTextField createField(final PanelBuilder builder, final String label, final int row, final boolean isPassword)
    {
        CellConstraints cc = new CellConstraints();
        
        JTextField txt = isPassword ? createPasswordField(15) : createTextField(15);
        
        builder.add(createLabel(label+":", SwingConstants.RIGHT), cc.xy(1, row));
        builder.add(txt,                                         cc.xy(3, row));
        //txt.addFocusListener(this);
        //txt.addKeyListener(keyAdapter);
        
        txt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {updateBtnUI();}
            public void removeUpdate(DocumentEvent e) {updateBtnUI();}
            public void changedUpdate(DocumentEvent e) {updateBtnUI();}
        });
        return txt;
    }
}

