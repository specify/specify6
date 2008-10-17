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

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
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
public abstract class BaseSetupPanel extends JPanel implements SetupPanelIFace
{
    protected static final boolean DO_DEBUG = true;
    
    protected String             panelName;
    protected KeyAdapter         keyAdapter;
    protected JButton            nextBtn;
    protected Font               bold      = (new JLabel()).getFont().deriveFont(Font.BOLD);

    /**
     * @param panelName
     * @param nextBtn
     */
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getPanelName()
     */
    public String getPanelName()
    {
        return panelName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getValues(java.util.Properties)
     */
    public abstract void getValues(Properties props);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#setValues(java.util.Properties)
     */
    public abstract void setValues(Properties values);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    public abstract boolean isUIValid();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#updateBtnUI()
     */
    public abstract void updateBtnUI();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingNext()
     */
    @Override
    public void doingNext()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingPrev()
     */
    @Override
    public void doingPrev()
    {
    }

    /**
     * Helper function for creating the UI.
     * @param builder builder
     * @param label the string label
     * @param row the row to place it on
     * @return the create JTextField (or JPasswordField)
     */
    protected JTextField createField(final PanelBuilder builder, final String label, final boolean isReq, final int row)
    {
        return createField(builder, label, isReq, row, false);
    }
    
    /**
     * Helper function for creating the UI.
     * @param builder builder
     * @param label the string label
     * @param row the row to place it on
     * @param isPassword whether to create a password or text field
     * @return the create JTextField (or JPasswordField)
     */
    protected JTextField createField(final PanelBuilder builder, 
                                     final String       label, 
                                     final boolean      isRequired,
                                     final int          row, 
                                     final boolean      isPassword)
    {
        CellConstraints cc = new CellConstraints();
        
        JTextField txt = isPassword ? createPasswordField(15) : createTextField(15);
        
        
        JLabel lbl = createI18NFormLabel(label, SwingConstants.RIGHT);
        if (isRequired)
        {
            lbl.setFont(bold);
        }
        builder.add(lbl, cc.xy(1, row));
        builder.add(txt, cc.xy(3, row));
        if (isRequired)
        {
            txt.setBackground(new Color(215, 230, 253));
        }
        //txt.addFocusListener(this);
        //txt.addKeyListener(keyAdapter);
        
        txt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {updateBtnUI();}
            public void removeUpdate(DocumentEvent e) {updateBtnUI();}
            public void changedUpdate(DocumentEvent e) {updateBtnUI();}
        });
        return txt;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }
    
    
}

