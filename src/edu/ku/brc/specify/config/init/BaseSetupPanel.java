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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This is the configuration window for create a new user and new database.
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 17, 2008
 *
 */
public abstract class BaseSetupPanel extends JPanel implements SetupPanelIFace
{
    protected static final boolean DO_DEBUG = false;
    
    protected String             panelName;
    protected String             helpContext;
    protected KeyAdapter         keyAdapter;
    protected JButton            nextBtn;
    protected JButton            prevBtn;
    protected boolean            makeStretchy = false;
    protected Font               bold         = (new JLabel()).getFont().deriveFont(Font.BOLD);
    protected Properties         properties   = null;

    protected JProgressBar       progressBar  = null;


    /**
     * @param panelName
     * @param helpContext
     * @param nextBtn
     */
    public BaseSetupPanel(final String panelName,
                          final String helpContext,
                          final JButton nextBtn,
                          final JButton prevBtn)
    {
        this(panelName, helpContext, nextBtn, prevBtn, false);
    }
    
    /**
     * @param panelName
     * @param helpContext
     * @param nextBtn
     * @param makeStretchy
     */
    public BaseSetupPanel(final String panelName,
                          final String helpContext,
                          final JButton nextBtn,
                          final JButton prevBtn,
                          final boolean makeStretchy)
    {
        this.panelName    = panelName;
        this.helpContext  = helpContext;
        this.nextBtn      = nextBtn;
        this.prevBtn      = prevBtn;
        this.makeStretchy = makeStretchy;
        
        this.keyAdapter = new KeyAdapter() 
        {
          public void keyPressed(KeyEvent e)
          {
              nextBtn.setEnabled(isUIValid());
          }
        };
    }
    
    /**
     * @return
     */
    protected JProgressBar getProgressBar()
    {
        if (progressBar == null)
        {
            progressBar = new JProgressBar();
        }
        return progressBar;
    }
    
    /**
     * @return the helpContext
     */
    @Override
    public String getHelpContext()
    {
        return helpContext;
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
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        properties = values;
    }
    
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#enablePreviousBtn()
     */
    @Override
    public boolean enablePreviousBtn()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#aboutToLeave(java.beans.PropertyChangeListener)
     */
    @Override
    public void aboutToLeave()
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
        
        final JTextField txt = isPassword ? createPasswordField(15) : createTextField(15);
        
        JLabel lbl = createI18NFormLabel(label, SwingConstants.RIGHT);
        if (isRequired)
        {
            lbl.setFont(bold);
        }
        builder.add(lbl, cc.xy(1, row));
        builder.add(txt, cc.xyw(3, row, makeStretchy ? 2 : 1));
        if (isRequired)
        {
            txt.setBackground(new Color(215, 230, 253)); // required Color
        }
        //txt.addFocusListener(this);
        //txt.addKeyListener(keyAdapter);
        
        txt.getDocument().addDocumentListener(createDocChangeAdaptor(txt));
        return txt;
    }
    
    /**
     * @return
     */
    public DocumentAdaptor createDocChangeAdaptor(final JTextField tf)
    {
        return new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e) { textChanged(tf); updateBtnUI(); }
        };
    }
    
    /**
     * @param txt
     */
    protected void textChanged(final JTextField txt)
    {
        
    }

    /**
     * Helper function for creating the UI.
     * @param builder builder
     * @param label the string label
     * @param row the row to place it on
     * @return the create JCheckBox
     */
    protected JCheckBox createCheckBox(final PanelBuilder builder, 
                                       final String       label, 
                                       final int          row)
    {
        CellConstraints cc = new CellConstraints();
        
        JCheckBox checkbox = UIHelper.createCheckBox(UIRegistry.getResourceString(label));
        
        JLabel lbl = UIHelper.createI18NLabel(" ", SwingConstants.RIGHT);
        builder.add(lbl, cc.xy(1, row));
        builder.add(checkbox, cc.xyw(3, row, makeStretchy ? 2 : 1));
        return checkbox;
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

