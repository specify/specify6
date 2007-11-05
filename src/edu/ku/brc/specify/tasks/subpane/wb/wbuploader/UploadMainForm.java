/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.crypto.interfaces.PBEKey;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadMainForm extends JFrame
{
    private static final Logger log = Logger.getLogger(UploadMainForm.class);

    //action commands for user actions
    public static String DO_UPLOAD = "DO_UPLOAD";
    public static String VIEW_UPLOAD = "VIEW_UPLOAD";
    public static String VIEW_SETTINGS = "VIEW_SETTINGS";
    public static String CLOSE_UI = "CLOSE_UI";
    public static String CANCEL_OPERATION = "CANCEL_OPERATION";
    public static String TBL_CLICK = "TBL_CLICK";
    public static String TBL_DBL_CLICK = "TBL_DBL_CLICK";
    public static String INVALID_VAL_CLICK = "INVALID_VAL_CLICK";
    public static String UNDO_UPLOAD = "UNDO_UPLOAD";
    public static String PRINT_INVALID = "PRINT_INVALID";
    
    protected JLabel uploadTblLbl;
    protected JList uploadTbls;
    protected JLabel currOpLbl;
    protected JProgressBar currOpProgress;
    protected JButton doUploadBtn;
    protected JButton viewSettingsBtn;
    protected JButton viewUploadBtn;
    protected JButton closeBtn;
    protected JButton cancelBtn;
    protected JButton undoBtn;
    protected JButton printInvalidBtn;
    protected JPanel invalidValPane;
    protected JLabel invalidValLbl;
    protected JList invalidVals;
    
    /**
     * The object listening to this form. Currently an Uploader object.
     */
    protected ActionListener listener = null;
    
    /**
     * Builds the form.
     */
    public void buildUIOld()
    {
        Box mainPane = new Box(BoxLayout.Y_AXIS);
        
        JPanel uploadTblPane = new JPanel(new BorderLayout());
        uploadTblLbl = new JLabel(getResourceString("WB_UPLOAD_AFFECTED_TBLS_LIST"));
        uploadTblLbl.setFont(uploadTblLbl.getFont().deriveFont(Font.BOLD));
        uploadTblPane.add(uploadTblLbl, BorderLayout.NORTH);
        uploadTbls = new JList();
        uploadTbls.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() == 2)
                {
                    uploadTblDblClick();
                }
                uploadTblClick();
            }

            public void mouseEntered(MouseEvent me)
            {
                // no comment.
            }

            public void mouseExited(MouseEvent me)
            {
                // no comment.
            }

            public void mousePressed(MouseEvent me)
            {
                // no comment.
            }

            public void mouseReleased(MouseEvent me)
            {
                // no comment.
            }
        });
        
        JScrollPane sp = new JScrollPane(uploadTbls, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        uploadTblPane.add(sp, BorderLayout.CENTER);
        mainPane.add(uploadTblPane);
        mainPane.add(Box.createVerticalStrut(10));
        
        invalidValPane = new JPanel(new BorderLayout());
        invalidValLbl = new JLabel(getResourceString("WB_UPLOAD_INVALID_DATA_LIST"));
        invalidValPane.add(invalidValLbl, BorderLayout.NORTH);
        invalidVals = new JList();
        invalidVals.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
               invalidValClick();
            }

            public void mouseEntered(MouseEvent me)
            {
                // no comment.
            }

            public void mouseExited(MouseEvent me)
            {
                // no comment.
            }

            public void mousePressed(MouseEvent me)
            {
                // no comment.
            }

            public void mouseReleased(MouseEvent me)
            {
                // no comment.
            }
        });
        
        sp = new JScrollPane(invalidVals, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        invalidValPane.add(sp, BorderLayout.CENTER);
        printInvalidBtn = new JButton(getResourceString("WB_UPLOAD_PRINT_INVALID_BTN")); 
        printInvalidBtn.setActionCommand(PRINT_INVALID);
        invalidValPane.add(printInvalidBtn, BorderLayout.SOUTH);
        mainPane.add(invalidValPane);
        
        mainPane.add(Box.createVerticalStrut(20));
        
        JPanel progPane = new JPanel();
        progPane.setLayout(new BoxLayout(progPane, BoxLayout.Y_AXIS));
        JPanel progSubPane = new JPanel(new FlowLayout());
        currOpLbl = new JLabel("");
        progSubPane.add(currOpLbl);
        cancelBtn = new JButton(getResourceString("Cancel")); 
        cancelBtn.setActionCommand(CANCEL_OPERATION);
        progSubPane.add(cancelBtn);
        progPane.add(progSubPane);
        currOpProgress = new JProgressBar();
        progPane.add(currOpProgress);
        mainPane.add(progPane);
        mainPane.add(Box.createVerticalStrut(20));
        
        FlowLayout btnPaneLayout = new FlowLayout(FlowLayout.RIGHT);
        JPanel btnPane = new JPanel(btnPaneLayout);
        viewSettingsBtn = new JButton(getResourceString("WB_UPLOAD_SETTINGS_BTN")); 
        viewSettingsBtn.setActionCommand(VIEW_SETTINGS);
        doUploadBtn = new JButton(getResourceString("WB_UPLOAD_BTN"));
        doUploadBtn.setActionCommand(DO_UPLOAD);
        viewUploadBtn = new JButton(getResourceString("WB_UPLOAD_VIEW_BTN"));
        viewUploadBtn.setActionCommand(VIEW_UPLOAD);
        closeBtn = new JButton(getResourceString("Close")); 
        closeBtn.setActionCommand(CLOSE_UI);
        undoBtn = new JButton(getResourceString("WB_UPLOAD_UNDO_BTN")); 
        undoBtn.setActionCommand(UNDO_UPLOAD);
        btnPane.add(doUploadBtn);
        btnPane.add(viewUploadBtn);
        btnPane.add(viewSettingsBtn);
        btnPane.add(undoBtn);
        btnPane.add(closeBtn);
        mainPane.add(btnPane);
        
        add(mainPane);
        pack();
    }
    
    public void buildUI()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p:g", "p,2px,p, 10px, p, 2px,p, 4px,p"));
        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        uploadTblLbl = new JLabel(getResourceString("WB_UPLOAD_AFFECTED_TBLS_LIST"));
        //uploadTblLbl.setFont(uploadTblLbl.getFont().deriveFont(Font.BOLD));
        pb.add(uploadTblLbl, cc.xy(1, 1));
        
        uploadTbls = new JList();
        JScrollPane sp = new JScrollPane(uploadTbls, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(sp, cc.xy(1, 3));
        
        // Invalid Pane
        
        invalidValPane = new JPanel(new BorderLayout());
        PanelBuilder invalidValPanePB = new PanelBuilder(new FormLayout("p:g", "p,2px,p,4px,p"), invalidValPane);
        
        invalidValLbl  = new JLabel(getResourceString("WB_UPLOAD_INVALID_DATA_LIST"));
        invalidValPanePB.add(invalidValLbl, cc.xy(1, 1));
        
        invalidVals = new JList();
        sp = new JScrollPane(invalidVals, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        invalidValPanePB.add(sp, cc.xy(1, 3));

        PanelBuilder rppb = new PanelBuilder(new FormLayout("f:p:g, p", "p"));
        printInvalidBtn = new JButton(getResourceString("WB_UPLOAD_PRINT_INVALID_BTN")); 
        printInvalidBtn.setActionCommand(PRINT_INVALID);
        rppb.add(printInvalidBtn, cc.xy(2, 1));
        invalidValPanePB.add(rppb.getPanel(), cc.xy(1, 5));
        
        pb.add(invalidValPane, cc.xy(1, 5));
        
        // Progress Pane
        
        JPanel progPane = new JPanel();
        progPane.setLayout(new BoxLayout(progPane, BoxLayout.Y_AXIS));
        
        JPanel progSubPane = new JPanel(new FlowLayout());
        currOpLbl = new JLabel("");
        progSubPane.add(currOpLbl);
        cancelBtn = new JButton(getResourceString("Cancel")); 
        cancelBtn.setActionCommand(CANCEL_OPERATION);
        progSubPane.add(cancelBtn);
        progPane.add(progSubPane);
        currOpProgress = new JProgressBar();
        progPane.add(currOpProgress);
        
        pb.add(progPane, cc.xy(1, 7));
        
        FlowLayout btnPaneLayout = new FlowLayout(FlowLayout.RIGHT);
        JPanel btnPane  = new JPanel(btnPaneLayout);
        viewSettingsBtn = new JButton(getResourceString("WB_UPLOAD_SETTINGS_BTN")); 
        viewSettingsBtn.setActionCommand(VIEW_SETTINGS);
        doUploadBtn     = new JButton(getResourceString("WB_UPLOAD_BTN"));
        doUploadBtn.setActionCommand(DO_UPLOAD);
        viewUploadBtn   = new JButton(getResourceString("WB_UPLOAD_VIEW_BTN"));
        viewUploadBtn.setActionCommand(VIEW_UPLOAD);
        closeBtn        = new JButton(getResourceString("Close")); 
        closeBtn.setActionCommand(CLOSE_UI);
        undoBtn         = new JButton(getResourceString("WB_UPLOAD_UNDO_BTN")); 
        undoBtn.setActionCommand(UNDO_UPLOAD);
        btnPane.add(doUploadBtn);
        btnPane.add(viewUploadBtn);
        btnPane.add(viewSettingsBtn);
        btnPane.add(undoBtn);
        btnPane.add(closeBtn);
        pb.add(btnPane, cc.xy(1, 9));
        
        uploadTbls.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() == 2)
                {
                    uploadTblDblClick();
                }
                uploadTblClick();
            }

            public void mouseEntered(MouseEvent me)
            {
                // no comment.
            }

            public void mouseExited(MouseEvent me)
            {
                // no comment.
            }

            public void mousePressed(MouseEvent me)
            {
                // no comment.
            }

            public void mouseReleased(MouseEvent me)
            {
                // no comment.
            }
        });
        
        invalidVals.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
               invalidValClick();
            }

            public void mouseEntered(MouseEvent me)
            {
                // no comment.
            }

            public void mouseExited(MouseEvent me)
            {
                // no comment.
            }

            public void mousePressed(MouseEvent me)
            {
                // no comment.
            }

            public void mouseReleased(MouseEvent me)
            {
                // no comment.
            }
        });
        

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setContentPane(pb.getPanel());
        pack();
        
        setSize(600,550);
        setTitle("WorkBench Upload Validation"); // I18N
    }

    
    /**
     * Relays user action to listener.
     */
    protected void uploadTblDblClick()
    {
        if (listener != null)
        {
            listener.actionPerformed(new ActionEvent(uploadTbls, 0, TBL_DBL_CLICK));
         }
    }
    
    /**
     * Relays user action to listener.
     */
    protected void uploadTblClick()
    {
        if (listener != null)
        {
            listener.actionPerformed(new ActionEvent(uploadTbls, 0, TBL_CLICK));
         }
    }
    
    /**
     * Relays user action to listener.
     */
    protected void invalidValClick()
    {
        if (listener != null)
        {
            listener.actionPerformed(new ActionEvent(invalidVals, 0, INVALID_VAL_CLICK));
        }
    }
    /**
     * @return the cancelBtn
     */
    public JButton getCancelBtn()
    {
        return cancelBtn;
    }

    /**
     * @return the closeBtn
     */
    public JButton getCloseBtn()
    {
        return closeBtn;
    }

    /**
     * @return the currOpLbl
     */
    public JLabel getCurrOpLbl()
    {
        return currOpLbl;
    }

    /**
     * @return the currOpProgress
     */
    public JProgressBar getCurrOpProgress()
    {
        return currOpProgress;
    }

    /**
     * @return the doUploadBtn
     */
    public JButton getDoUploadBtn()
    {
        return doUploadBtn;
    }

    /**
     * @return the invalidVals
     */
    public JList getInvalidVals()
    {
        return invalidVals;
    }

    /**
     * @return the uploadTblLbl
     */
    public JLabel getUploadTblLbl()
    {
        return uploadTblLbl;
    }

    /**
     * @return the uploadTbls
     */
    public JList getUploadTbls()
    {
        return uploadTbls;
    }

    /**
     * @return the viewSettingsBtn
     */
    public JButton getViewSettingsBtn()
    {
        return viewSettingsBtn;
    }

    /**
     * @return the viewUploadBtn
     */
    public JButton getViewUploadBtn()
    {
        return viewUploadBtn;
    }

    /**
     * @return the invalidValLbl
     */
    public JLabel getInvalidValLbl()
    {
        return invalidValLbl;
    }

    /**
     * @return the invalidValPane
     */
    public JPanel getInvalidValPane()
    {
        return invalidValPane;
    }

    /**
     * @param btn
     * @param listener
     * 
     * Adds listener as an ActionListener for btn.
     */
    protected void setBtnListener(JButton btn, ActionListener listener)
    {
        if (btn != null)
        {
            btn.addActionListener(listener);
        }
        else
        {
            log.error("button object is null.");
        }
    }
    
    /**
     * @param listener
     * 
     * Sets the listener and adds listener as ActionListener for buttons.
     */
    public void setActionListener(ActionListener listener)
    {
        this.listener = listener;
        setBtnListener(doUploadBtn, listener);
        setBtnListener(viewSettingsBtn, listener);
        setBtnListener(viewUploadBtn, listener);
        setBtnListener(closeBtn, listener);
        setBtnListener(cancelBtn, listener);
        setBtnListener(undoBtn, listener);
        setBtnListener(printInvalidBtn, listener);
    }
    
    public class TesterThingy implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals(UploadMainForm.DO_UPLOAD))
            {
                System.out.println(UploadMainForm.DO_UPLOAD);
            }
            else if (e.getActionCommand().equals(UploadMainForm.VIEW_UPLOAD))
            {
                System.out.println(UploadMainForm.VIEW_UPLOAD);
            }
            else if (e.getActionCommand().equals(UploadMainForm.VIEW_SETTINGS))
            {
                System.out.println(UploadMainForm.VIEW_SETTINGS);
            }
            else if (e.getActionCommand().equals(UploadMainForm.CLOSE_UI))
            {
                System.out.println(UploadMainForm.CLOSE_UI);
            }
            else if (e.getActionCommand().equals(UploadMainForm.CANCEL_OPERATION))
            {
                System.out.println(UploadMainForm.CANCEL_OPERATION);
            }
            else if (e.getActionCommand().equals(UploadMainForm.TBL_DBL_CLICK))
            {
                System.out.println(UploadMainForm.TBL_DBL_CLICK);
            }
            else if (e.getActionCommand().equals(UploadMainForm.INVALID_VAL_CLICK))
            {
                System.out.println(UploadMainForm.INVALID_VAL_CLICK);
            }
       }
    }
    
    public static void main(final String[] args)
    {
        UploadMainForm tf = new UploadMainForm();
        tf.buildUI();
        DefaultListModel tbls = new DefaultListModel();
        tbls.addElement("CollectingEvent");
        tbls.addElement("Collection Object");
        tbls.addElement("Taxon");
        tf.getUploadTbls().setModel(tbls);
        
        DefaultListModel invalids = new DefaultListModel();
        invalids.addElement("bad");
        invalids.addElement("wrong");
        invalids.addElement("shame");
        tf.getInvalidVals().setModel(invalids);
        
        tf.getInvalidValPane().setVisible(invalids.size() > 0);
        
        tf.setActionListener(tf.new TesterThingy());
        
        tf.setVisible(true);
    }

    /**
     * @return the listener
     */
    public ActionListener getListener()
    {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(ActionListener listener)
    {
        this.listener = listener;
    }

    /**
     * @return the undoBtn
     */
    public JButton getUndoBtn()
    {
        return undoBtn;
    }

    /**
     * @return the printInvalidBtn
     */
    public JButton getPrintInvalidBtn()
    {
        return printInvalidBtn;
    }
}
