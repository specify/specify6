/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.ku.brc.specify.ui.HelpMgr;

/**
 * CustomFrame is designed to enable developers to "customize" a frame's contents and 
 * have the Frame's buttons automatically created with any label on them.
 * The frame can have up to four different button: OK, Cancel, Apply, and Help.
 * 
 * You can use the class two ways. Create one directly and pass in the "contents" pane, or
 * derive your own class and override the "createUI" method. Note: you should/must call super.createUI
 * as the first line (or nearly the first line), this call will create all your buttons. Any changes to the labels
 * of the four btns should be done before the call to createUI.
 * 
 * Also note that the setVisible call will automatically call createUI, but it is OK to call it manually
 * yourself before the setVisible. BUT! if you do you may need to call "pack" before call setVisible.
 * 
 * If you override createUI and want to set your own contents pane internally do it like this:<br>
 * <pre>
 * contentPanel = myNewPanel;
 * mainPanel.add(contentPanel, BorderLayout.CENTER);
 * </pre>
 * 
 * IMPORTANT: The setVisible method will register and unregister the Frame with the UIRegistry window stack.
 * 
 * @code_status Complete
 * 
 * @author rods
 * 
 */
@SuppressWarnings("serial")
public class CustomFrame extends JFrame
{
    // Static Data Members
    public static final int OK_BTN             = 1;
    public static final int CANCEL_BTN         = 2;
    public static final int HELP_BTN           = 4;
    public static final int APPLY_BTN          = 8;
    
    public static final int OKCANCEL           = OK_BTN | CANCEL_BTN;
    public static final int OKHELP             = OK_BTN | HELP_BTN;
    public static final int OKCANCELHELP       = OK_BTN | CANCEL_BTN | HELP_BTN;
    public static final int OKCANCELAPPLY      = OK_BTN | CANCEL_BTN | APPLY_BTN;
    public static final int OKCANCELAPPLYHELP  = OK_BTN | CANCEL_BTN | APPLY_BTN | HELP_BTN;
    
    // Data Members
    protected JButton           okBtn            = null;
    protected JButton           cancelBtn        = null;
    protected JButton           helpBtn          = null;
    protected JButton           applyBtn         = null;
    
    // Button Labels
    protected String            okLabel          = null;
    protected String            cancelLabel      = null;
    protected String            helpLabel        = null;
    protected String            applyLabel       = null;

    protected ImageIcon         icon             = null;
    protected boolean           isCancelled      = true;
    protected int               btnPressed       = CANCEL_BTN;
    
    protected JPanel            mainPanel;

    // Needed for delayed building of Frame
    protected int               whichBtns        = OK_BTN;
    protected String            helpContext      = null;
    protected Component         contentPanel     = null;
    
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the frame
     * @param contentPanel the contentPanel
     * @throws HeadlessException
     */
    public CustomFrame(final String    title, 
                       final Component contentPanel) throws HeadlessException
    {
        this(title, OK_BTN | CANCEL_BTN, contentPanel);
    }

    /**
     * @param frame parent frame
     * @param title the title of the frame
     * @param isModal whether or not it is model
     * @param whichBtns which button to use for the frame
     * @param contentPanel the contentPanel
     * @throws HeadlessException
     */
    public CustomFrame(final String    title, 
                       final int       whichBtns,
                       final Component contentPanel) throws HeadlessException
    {
        super(title);
        
        this.whichBtns = whichBtns;
        this.contentPanel = contentPanel;
    }
    
    /**
     * Sets the title bar to look like the contents have been modified.
     * @param isModified true/false
     */
    public void setWindowModified(final boolean isModified)
    {
        UIHelper.setWindowModified(this, isModified);
    }
    
    /**
     * Create the UI for the frame.
     */
    public void createUI()
    {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        if (contentPanel != null)
        {
            mainPanel.add(contentPanel, BorderLayout.CENTER);
        }

        // Bottom Button UI
        okBtn = createButton(StringUtils.isNotEmpty(okLabel) ? okLabel : getResourceString("OK"));
        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                okButtonPressed();
            }
        });
        getRootPane().setDefaultButton(okBtn);
        
        
        if ((whichBtns & CANCEL_BTN) == CANCEL_BTN)
        {
            cancelBtn = createButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel : getResourceString("CANCEL"));
            cancelBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    cancelButtonPressed();
                }
            });
        }
        
        if ((whichBtns & HELP_BTN) == HELP_BTN)
        {
            helpBtn = createButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel : getResourceString("HELP"));
            if (StringUtils.isNotEmpty(helpContext))
            {
                HelpMgr.registerComponent(helpBtn, helpContext);
            } else
            {
                helpBtn.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        helpButtonPressed();
                    }
                }); 
            }
        }
        
        if ((whichBtns & APPLY_BTN) == APPLY_BTN)
        {
            applyBtn = createButton(StringUtils.isNotEmpty(applyLabel) ? applyLabel : getResourceString("Apply"));
            applyBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    applyButtonPressed();
                }
            });
        }
        
        JPanel bb;
        if (whichBtns == OK_BTN)
        {
            bb = ButtonBarFactory.buildOKBar(okBtn);
            
        } else if (whichBtns == OKCANCEL)
        {
            bb = ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn);
            
        } else if (whichBtns == OKCANCELAPPLY)
        {
            bb = ButtonBarFactory.buildOKCancelApplyBar(okBtn, cancelBtn, applyBtn);
            
        } else if (whichBtns == OKHELP)
        {
            bb = ButtonBarFactory.buildOKHelpBar(okBtn, helpBtn);
            
        } else if (whichBtns == OKCANCELHELP)
        {
            bb = ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn);
            
        } else if (whichBtns == OKCANCELAPPLYHELP)
        {
            bb = ButtonBarFactory.buildOKCancelApplyHelpBar(okBtn, cancelBtn, applyBtn, helpBtn);
            
        } else
        {
            bb = ButtonBarFactory.buildOKBar(okBtn);
        }

        mainPanel.add(bb, BorderLayout.SOUTH);


        setContentPane(mainPanel);
        
        pack();
        
        setLocationRelativeTo(this.getOwner());

    }
    
    /**
     * Performs cancel action.
     */
    protected void cancelButtonPressed()
    {
        isCancelled = true;
        btnPressed  = CANCEL_BTN;
        setVisible(false);
    }

    /**
     * Performs ok action.
     */
    protected void okButtonPressed()
    {
        isCancelled = false;
        btnPressed  = OK_BTN;
        setVisible(false);
    }

    /**
     * Performs help action.
     */
    protected void helpButtonPressed()
    {
        isCancelled = false;
        btnPressed  = HELP_BTN;
    }

    /**
     * Performs apply action.
     */
    protected void applyButtonPressed()
    {
        isCancelled = false;
        btnPressed  = HELP_BTN;
    }
    
    /**
     * Returns whether it was cancelled.
     * @return whether it was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    public void setOkLabel(final String text)
    {
        this.okLabel = text;
    }

    public void setCancelLabel(final String text)
    {
        this.cancelLabel = text;
    }

    public int getBtnPressed()
    {
        return btnPressed;
    }

    public void setApplyLabel(String applyLabel)
    {
        this.applyLabel = applyLabel;
    }

    public void setHelpContext(String helpContext)
    {
        this.helpContext = helpContext;
    }

    public void setHelpLabel(String helpLabel)
    {
        this.helpLabel = helpLabel;
    }

    /**
     * @return the applyBtn
     */
    public JButton getApplyBtn()
    {
        return applyBtn;
    }

    /**
     * @return the cancelBtn
     */
    public JButton getCancelBtn()
    {
        return cancelBtn;
    }

    /**
     * @return the helpBtn
     */
    public JButton getHelpBtn()
    {
        return helpBtn;
    }

    /**
     * @return the okBtn
     */
    public JButton getOkBtn()
    {
        return okBtn;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible)
    {
        if (visible)
        {
            UIRegistry.pushWindow(this);
        } else
        {
            UIRegistry.popWindow(this);
        }
        
        if (okBtn == null && visible)
        {
            createUI();
        }
        UIHelper.centerWindow(this);
        super.setVisible(visible);
    }

}
