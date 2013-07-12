/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.HelpMgr;

/**
 * CustomDialog is designed to enable developers to "customize" a dialog's contents and 
 * have the Dialog's buttons automatically created with any label on them.
 * The dialog can have up to four different button: OK, Cancel, Apply, and Help.
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
 * IMPORTANT: The setVisible method will register and unregister the dialog with the UIRegistry window stack.
 * 
 * @code_status Complete
 * 
 * @author rods
 * 
 */
@SuppressWarnings("serial")
public class CustomDialog extends JDialog
{
    // Static Data Members
    public static final int NONE_BTN           = 0;
    public static final int OK_BTN             = 1;
    public static final int CANCEL_BTN         = 2;
    public static final int HELP_BTN           = 4;
    public static final int APPLY_BTN          = 8;
    
    public static final int OKCANCEL           = OK_BTN | CANCEL_BTN;
    public static final int OKHELP             = OK_BTN | HELP_BTN;
    public static final int OKCANCELHELP       = OK_BTN | CANCEL_BTN | HELP_BTN;
    public static final int OKCANCELAPPLY      = OK_BTN | CANCEL_BTN | APPLY_BTN;
    public static final int OKCANCELAPPLYHELP  = OK_BTN | CANCEL_BTN | APPLY_BTN | HELP_BTN;
    public static final int CANCELHELP         = CANCEL_BTN | HELP_BTN;
    
    protected static  ImageIcon appIcon = null;
    
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

    protected boolean           isCancelled      = true;
    protected boolean           closeOnApplyClk  = false;
    protected boolean           closeOnHelpClk   = false;
    protected int               btnPressed       = NONE_BTN;
    protected boolean           isCreated        = false;
    
    protected JPanel            mainPanel;

    // Needed for delayed building of Dialog
    protected int               whichBtns        = OK_BTN;
    protected int               defaultBtn       = OK_BTN;
    protected String            helpContext      = null;
    protected Component         contentPanel     = null;
    protected JComponent        extraBtn         = null;
    
    // Custom Titlebar
    protected GradiantLabel     titleBarLabel    = null;
    protected Color             borderColor      = null;
    
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param contentPanel the contentpane
     * @throws HeadlessException
     */
    public CustomDialog(final Frame     frame, 
                        final String    title, 
                        final boolean   isModal,
                        final Component contentPanel) throws HeadlessException
    {
        this(frame, title, isModal, OK_BTN | CANCEL_BTN, contentPanel);
    }

    /**
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param whichBtns which button to use for the dialog
     * @param contentPanel the contentPanel
     * @throws HeadlessException
     */
    public CustomDialog(final Frame     frame, 
                        final String    title, 
                        final boolean   isModal,
                        final int       whichBtns,
                        final Component contentPanel) throws HeadlessException
    {
        super(frame, title, isModal);
        
        this.whichBtns    = whichBtns;
        this.contentPanel = contentPanel;
        
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
    }

    /**
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param whichBtns which button to use for the dialog
     * @param contentPanel the contentPanel
     * @throws HeadlessException
     */
    public CustomDialog(final Frame     frame, 
                        final String    title, 
                        final boolean   isModal,
                        final int       whichBtns,
                        final Component contentPanel,
                        final int defaultBtn) throws HeadlessException
    {
        super(frame, title, isModal);
        
        this.whichBtns    = whichBtns;
        this.contentPanel = contentPanel;
        this.defaultBtn = defaultBtn;
        
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
    }

    /**
     * @param dlg parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param whichBtns which button to use for the dialog
     * @param contentPanel the contentPanel
     * @throws HeadlessException
     */
    public CustomDialog(final Dialog    dialog, 
                        final String    title, 
                        final boolean   isModal,
                        final int       whichBtns,
                        final Component contentPanel) throws HeadlessException
    {
        super(dialog, title, isModal);
        
        this.whichBtns    = whichBtns;
        this.contentPanel = contentPanel;
        
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
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
     * gets JButton corresponding to defaultBtn value.
     */
    protected JButton findDefaultBtn()
    {
        if (defaultBtn == CANCEL_BTN)
        {
            return cancelBtn;
        }
        if (defaultBtn == HELP_BTN)
        {
            return helpBtn;
        }
        if (defaultBtn == APPLY_BTN)
        {
            return applyBtn;
        }
        return okBtn;
    }
    
    /**
     * @return the main panel.
     */
    protected JPanel createMainPanel()
    {
        return new JPanel(new BorderLayout());
    }
    
    /**
     * @param whichBtns the whichBtns to set
     */
    public void setWhichBtns(int whichBtns)
    {
        this.whichBtns = whichBtns;
    }

    /**
     * create buttons
     */
    protected void createButtons()
    {
        if ((whichBtns & OK_BTN) == OK_BTN)
        {
            okBtn = createButton(StringUtils.isNotEmpty(okLabel) ? okLabel : getResourceString("OK"));
            okBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    okButtonPressed();
                }
            });
        }
        
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
            helpBtn = createButton(StringUtils.isNotEmpty(helpLabel) ? helpLabel : getResourceString("HELP"));
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
    }
    
    /**
     * @return button bar panel
     */
    protected JPanel buildButtonBar()
    {
        JPanel bb = null;
    	if (whichBtns == OK_BTN)
        {
            bb = ButtonBarFactory.buildOKBar(okBtn);
            
        } else if (whichBtns == CANCEL_BTN)
        {
            bb = ButtonBarFactory.buildOKBar(cancelBtn);
            
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
            
        } else if (whichBtns == CANCELHELP)
        {
            bb = ButtonBarFactory.buildOKHelpBar(cancelBtn, helpBtn);
            
        }
    	return bb;
    }
    /**
     * Create the UI for the dialog.
     */
    public void createUI()
    {
        isCreated = true;
        
        /*if (helpContext == null)
        {
            whichBtns &= ~HELP_BTN; // Clear Bit for Help button if there is no HelpContext
        }*/

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        mainPanel = createMainPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 2));
        
        if (titleBarLabel != null)
        {
            mainPanel.add(titleBarLabel, BorderLayout.NORTH);
            mainPanel.setBorder(BorderFactory.createLineBorder(borderColor));
        }

        if (contentPanel != null)
        {
            mainPanel.add(contentPanel, BorderLayout.CENTER);
        }

        // Bottom Button UI
        createButtons();
        getRootPane().setDefaultButton(findDefaultBtn());
        JPanel bb = buildButtonBar();
        
        if (extraBtn != null)
        {
            PanelBuilder    builder = new PanelBuilder(new FormLayout("p,f:p:g", "p"));
            CellConstraints cc      = new CellConstraints();
            builder.add(extraBtn, cc.xy(1,1));
            builder.add(bb,       cc.xy(2,1));
            builder.getPanel().setOpaque(false);
            bb = builder.getPanel();
        }
        
        if (bb != null)
        {
            bb.setOpaque(false);
            
            Component bbComp = bb;
            if (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX) // adjust for intruding resizer on Mac OS X
            {
                PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,15px", "p"));
                CellConstraints cc         = new CellConstraints();
                builder.add(bb, cc.xy(1,1));
                builder.getPanel().setOpaque(false);
                
                bbComp = builder.getPanel();
            }
    
            mainPanel.add(bbComp, BorderLayout.SOUTH);
        }


        setContentPane(mainPanel);
        
        pack();
        
        setLocationRelativeTo(this.getOwner());

    }
    
    /**
     * @param title the title of the virtual titlebar
     */
    public void setCustomTitleBar(final String title)
    {
        setUndecorated(true);
        
        titleBarLabel = new GradiantLabel(title, SwingConstants.CENTER);
        
         borderColor    = SystemColor.windowBorder;
        Color textColor = SystemColor.activeCaptionText;
        
        if (UIHelper.isLinux())
        {
            borderColor = SystemColor.activeCaptionBorder;
            textColor   = SystemColor.activeCaptionText;
            if (borderColor.getRed() == borderColor.getGreen() && borderColor.getGreen() == borderColor.getBlue())
            {
                borderColor = new Color(132, 170, 216);
                textColor   = Color.WHITE;
            }
            
        } else if (UIHelper.isWindows())
        {
            borderColor = (Color)Toolkit.getDefaultToolkit().getDesktopProperty("win.frame.activeCaptionColor");
            textColor   = (Color)Toolkit.getDefaultToolkit().getDesktopProperty("win.frame.captionTextColor");
        }
        
        titleBarLabel.setTextColor(textColor);
        titleBarLabel.setBGBaseColor(borderColor);
        titleBarLabel.setGradiants(UIHelper.makeLighter(borderColor, 0.2),
                                   UIHelper.makeDarker(borderColor, 0.2));
    }
    
    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel()
    {
        if (mainPanel == null)
        {
            createUI();
        }
        return mainPanel;
    }

    public Component getContentPanel()
    {
        return contentPanel;
    }

    public void setContentPanel(Component contentPanel)
    {
        this.contentPanel = contentPanel;
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
     * If helpContext has not been set, performs help action.
     */
    protected void helpButtonPressed()
    {
        if (closeOnHelpClk)
        {
            isCancelled = false;
        }
        btnPressed  = HELP_BTN;
        HelpMgr.getHelpForContext();        
        if (closeOnHelpClk)
        {
            setVisible(false);
        }
    }

    /**
     * Performs apply action.
     */
    protected void applyButtonPressed()
    {
        isCancelled = false;
        btnPressed  = APPLY_BTN;
        if (closeOnApplyClk)
        {
            setVisible(false);
        }
    }

    /**
     * Returns whether it was cancelled.
     * @return whether it was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /**
     * Returns whether it was NOT cancelled.
     * @return whether it was NOT cancelled
     */
    public boolean isNotCancelled()
    {
        return !isCancelled;
    }

    /**
     * @param text
     */
    public void setOkLabel(final String text)
    {
        this.okLabel = text;
        
        if (okBtn != null)
        {
            okBtn.setText(okLabel);
        }
    }

    /**
     * @param text
     */
    public void setCancelLabel(final String text)
    {
        this.cancelLabel = text;

        if (cancelBtn != null)
        {
            cancelBtn.setText(cancelLabel);
        }
    }

    /**
     * @return
     */
    public int getBtnPressed()
    {
        return btnPressed;
    }

    /**
     * @param applyLabel
     */
    public void setApplyLabel(String applyLabel)
    {
        this.applyLabel = applyLabel;
        
        if (applyBtn != null)
        {
            applyBtn.setText(applyLabel);
        }
    }

    /**
     * @param closeOnApplyClk
     */
    public void setCloseOnApplyClk(boolean closeOnApplyClk)
    {
        this.closeOnApplyClk = closeOnApplyClk;
    }
    
    /**
     * @param helpContext
     */
    public void setHelpContext(String helpContext)
    {
        this.helpContext = helpContext;
    }

    /**
     * @param helpLabel
     */
    public void setHelpLabel(String helpLabel)
    {
        this.helpLabel = helpLabel;
        
        if (helpBtn != null)
        {
            helpBtn.setText(helpLabel);
        }
    }

    /**
     * @param closeOnHelpClk
     */
    public void setCloseOnHelpClk(boolean closeOnHelpClk)
    {
        this.closeOnHelpClk = closeOnHelpClk;
    }
    
    /*
     * (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible)
    {
        if (visible)
        {
            UIRegistry.pushWindow(this);
            
            if (!isCreated && visible)
            {
                createUI();
            }
            UIHelper.centerWindow(this);
            
        } else
        {
            UIRegistry.popWindow(this);
        }
        
        super.setVisible(visible);
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
    
    /**
     * Method for derived classes to override.
     */
    public void cleanUp()
    {
        // no op
    }

    /**
     * @return the appIcon
     */
    public static ImageIcon getAppIcon()
    {
        return appIcon;
    }

    /**
     * @param appIcon the appIcon to set
     */
    public static void setAppIcon(ImageIcon appIcon)
    {
        CustomDialog.appIcon = appIcon;
    }

    /**
     * @return the extraBtn
     */
    public JComponent getExtraBtn()
    {
        return extraBtn;
    }

    /**
     * @param extraBtn the extraBtn to set
     */
    public void setExtraBtn(JComponent extraBtn)
    {
        this.extraBtn = extraBtn;
    }
    
//    /**
//     * @param titleKey
//     * @param content
//     * @return
//     */
//    public static CustomDialog createI18NDlg(final String titleKey, final JComponent content)
//    {
//        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
//        pb.add(content, (new CellConstraints().xy(1, 1)));
//        pb.setDefaultDialogBorder();
//        
//        return new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), getResourceString(titleKey), true, pb.getPanel());
//    }
//    
//    /**
//     * @param titleKey
//     * @param model
//     * @return
//     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    public static CustomDialog createI18NDlg(final String titleKey, final JList list)
//    {
////        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g", "p")); //$NON-NLS-1$ //$NON-NLS-2$
////        selectAllBtn   = createI18NButton("SELECTALL"); //$NON-NLS-1$
////        deselectAllBtn = createI18NButton("DESELECTALL"); //$NON-NLS-1$
////        btnPB.add(selectAllBtn,   cc.xy(2, 1));
////        btnPB.add(deselectAllBtn, cc.xy(4, 1));
////        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", (topMsg != null ? "p,2px," : "") + "p,2px,p,2px,p"));
//
//        JScrollPane  sb   = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        PanelBuilder pb   = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
//        pb.add(sb, (new CellConstraints().xy(1, 1)));
//        pb.setDefaultDialogBorder();
//        
//        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), getResourceString(titleKey), true, pb.getPanel());
//        dlg.createUI();
//        dlg.getOkBtn().setEnabled(false);
//        
//        list.addListSelectionListener(new ListSelectionListener()
//        {
//            @Override
//            public void valueChanged(ListSelectionEvent e)
//            {
//                if (!e.getValueIsAdjusting())
//                {
//                    dlg.getOkBtn().setEnabled(!list.isSelectionEmpty());
//                }
//            }
//        });
//        
//        list.addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                if (e.getClickCount() == 2)
//                {
//                    dlg.getOkBtn().doClick();
//                }
//            }
//        });
//        
//        return dlg;
//    }
}
