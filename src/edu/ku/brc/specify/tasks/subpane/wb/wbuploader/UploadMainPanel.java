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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.TableNameRenderer;
import edu.ku.brc.ui.IconManager;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadMainPanel extends JPanel
{
    private static final Logger log = Logger.getLogger(UploadMainPanel.class);

    //action commands for user actions
    public final static String VALIDATE_CONTENT = "VALIDATE_CONTENT";
    public final static String DO_UPLOAD = "DO_UPLOAD";
    public final static String VIEW_UPLOAD = "VIEW_UPLOAD";
    public final static String VIEW_SETTINGS = "VIEW_SETTINGS";
    public final static String CLOSE_UI = "CLOSE_UI";
    public final static String CANCEL_OPERATION = "CANCEL_OPERATION";
    public final static String TBL_CLICK = "TBL_CLICK";
    public final static String TBL_DBL_CLICK = "TBL_DBL_CLICK";
    public final static String MSG_CLICK = "MSG_CLICK";
    public final static String UNDO_UPLOAD = "UNDO_UPLOAD";
    public final static String PRINT_INVALID = "PRINT_INVALID";
    
    protected JLabel uploadTblLbl;
    protected JList uploadTbls;
    protected JTable uploadTblTbl;
    protected JPanel uploadTblPanel;
    protected JLabel currOpLbl;
    protected JProgressBar currOpProgress;
    protected JButton validateContentBtn;
    protected JButton doUploadBtn;
    protected JButton viewSettingsBtn;
    protected JButton viewUploadBtn;
    protected JButton closeBtn;
    protected JButton cancelBtn;
    protected JButton undoBtn;
    protected JButton printBtn;
    protected JPanel msgPane;
    protected JLabel msgLbl;
    protected JList msgList;
    protected JList validationErrorList;
    protected JPanel validationErrorPanel;
    protected JScrollPane msgListSB;
    
    /**
     * The object listening to this form. Currently an Uploader object.
     */
    protected ActionListener listener = null;
    
    
    public UploadMainPanel()
    {
        buildUI();
    }
    
    /**
     * Shows the validation error msg list.
     */
    protected void showValidationErrors()
    {
        CellConstraints cc = new CellConstraints();
        msgPane.remove(msgListSB);
        msgPane.add(validationErrorPanel, cc.xy(1, 1));
        msgPane.add(msgListSB, cc.xy(1, 2));
        msgPane.validate();
    }
    
    /**
     * Hides the validation error msg list.
     */
    protected void hideValidationErrors()
    {
        CellConstraints cc = new CellConstraints();
        msgPane.remove(validationErrorPanel);
        msgPane.remove(msgListSB);
        msgPane.add(msgListSB, cc.xywh(1, 1, 1, 2));
        msgPane.validate();
    }
    
    /**
     * @return true if validation error msg list is displayed.
     */
    protected boolean isValidationErrorsVisible()
    {
        return validationErrorPanel.getParent() == msgPane;
    }
    
    /**
     * Displays the list view of affected tables.
     */
    public void showUploadTblList()
    {
        ((CardLayout)uploadTblPanel.getLayout()).first(uploadTblPanel);
    }
    
    /**
     * Displays the table view of affected tables.
     */
    public void showUploadTblTbl()
    {
        ((CardLayout)uploadTblPanel.getLayout()).last(uploadTblPanel);
    }
    
    /**
     * @param uts - the tables that could be affected by the upload.
     * 
     * Sets up the ui controls that display the affected tables.
     */
    public void addAffectedTables(Iterator<UploadInfoRenderable> uts)
    {
        DefaultListModel tbls = new DefaultListModel();
        Vector<Vector<Object>> cells = new Vector<Vector<Object>>();
        JList tableList = getUploadTbls();
        TableNameRenderer nameRender = new TableNameRenderer(IconManager.IconSize.Std24);
        nameRender.setUseIcon("PlaceHolder");
        tableList.setCellRenderer(nameRender);
    
        while (uts.hasNext())
        {
            UploadInfoRenderable ut = uts.next();
            tbls.addElement(ut);
            Vector<Object> row = new Vector<Object>(2);
            row.add(ut);
            row.add(ut);
            cells.add(row);            
        }
    
        tableList.setModel(tbls);
        Vector<String> heads = new Vector<String>(2);
        heads.add(getResourceString("ERD_TABLE")); 
        heads.add(getResourceString("WB_UPLOAD_RECORDS_ADDED"));
        getUploadTblTbl().setModel(new DefaultTableModel(cells, heads));
        getUploadTblTbl().getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer()
        {
                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value,
                                                               boolean isSelected,
                                                               boolean hasFocus,
                                                               int row,
                                                               int column)
                {
                    super
                            .getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                    column);

                    UploadInfoRenderable ti = (UploadInfoRenderable) value;
                    if (column == 0)
                    {
                        ImageIcon icon = IconManager.getIcon(ti.getIconName(),
                                IconManager.IconSize.Std16);
                        setIcon(icon != null ? icon : IconManager.getIcon("Blank",
                                IconManager.IconSize.Std16));
                    }
                    else
                    {
                        setIcon(null);
                    }
                    if (isSelected)
                    {
                        setOpaque(true);
                        setBackground(table.getSelectionBackground());
                        setForeground(table.getSelectionForeground());

                    }
                    else
                    {
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                    }
                    setText(ti.getTitle());
                    return this;
                }
            });
            getUploadTblTbl().getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer()
            {
              @Override
              public Component getTableCellRendererComponent(JTable table,
                                                             Object value,
                                                             boolean isSelected,
                                                             boolean hasFocus,
                                                             int row,
                                                             int column)
              {
                  super
                          .getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                  column);
  
                  UploadInfoRenderable ti = (UploadInfoRenderable) value;
                  setIcon(null);
                  if (isSelected)
                  {
                      setOpaque(true);
                      setBackground(table.getSelectionBackground());
                      setForeground(table.getSelectionForeground());
  
                  }
                  else
                  {
                      setBackground(table.getBackground());
                      setForeground(table.getForeground());
                  }
  
                  if (ti.getCreatedCnt() != null)
                  {
                      if (getHorizontalAlignment() != SwingConstants.CENTER)
                      {
                          setHorizontalAlignment(SwingConstants.CENTER);
                      }
                      setText(ti.getCreatedCnt().toString());
                  }
                  return this;
              }
          });
        }

    
    public void buildUI()
    {
        CellConstraints cc = new CellConstraints();
        setLayout(new FormLayout("3dlu:none, fill:50dlu:grow(0.30), 20dlu:none, fill:50dlu:grow(0.70), 5dlu:none, r:max(50dlu;pref), 3dlu:none", 
                "2dlu:none, fill:m:none, 4dlu:none, t:m:none, 2dlu:none, fill:75dlu:grow, 5dlu:none"));
        
        JLabel title = new JLabel(getResourceString("WB_UPLOAD_FORM_TITLE"));
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, cc.xywh(2,2,5,1));
        
        JPanel pPane = new JPanel(new FormLayout("fill:pref:grow, fill:pref:none", "center:m:grow"));
        currOpProgress = new JProgressBar();
        pPane.add(currOpProgress, cc.xy(1, 1));
        cancelBtn = new JButton(getResourceString("Cancel")); 
        cancelBtn.setActionCommand(CANCEL_OPERATION);
        pPane.add(cancelBtn, cc.xy(2, 1));
        add(pPane, cc.xywh(4, 2, 3, 1));

        add(new JSeparator(SwingConstants.HORIZONTAL), cc.xywh(2,3,5,1));
        
        uploadTblLbl = new JLabel(getResourceString("WB_UPLOAD_AFFECTED_TBLS_LIST"));
        add(uploadTblLbl, cc.xy(2, 4));
        
        
        uploadTblPanel = new JPanel(new CardLayout());
        uploadTbls = new JList();
        JScrollPane sp = new JScrollPane(uploadTbls, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        uploadTblPanel.add(sp, "list");
        uploadTblTbl = new JTable()
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        uploadTblTbl.setCellSelectionEnabled(false);
        sp = new JScrollPane(uploadTblTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        uploadTblPanel.add(sp, "table");
        
        add(uploadTblPanel, cc.xy(2, 6));
        
        msgPane = new JPanel(new FormLayout("fill:m:grow", "fill:pref:grow, fill:m:grow"));
        
        msgLbl  = new JLabel(getResourceString("WB_UPLOAD_MSG_LIST"));
        add(msgLbl, cc.xy(4, 4));
        
        msgList = new JList(new DefaultListModel());
        msgListSB = new JScrollPane(msgList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        msgPane.add(msgListSB, cc.xywh(1, 1, 1, 2));
        
        validationErrorPanel = new JPanel(new BorderLayout());
        validationErrorList = new JList(new DefaultListModel());
        validationErrorPanel.add(new JScrollPane(validationErrorList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), 
                BorderLayout.CENTER);
        printBtn = new JButton(getResourceString("WB_UPLOAD_PRINT_MESSAGES_BTN")); 
        printBtn.setActionCommand(PRINT_INVALID);
        JPanel pbtnPane = new JPanel(new FormLayout("fill:m:grow, right:max(50dlu;pref)", "c:m"));
        pbtnPane.add(printBtn, cc.xy(2, 1));
        validationErrorPanel.add(pbtnPane, BorderLayout.SOUTH);
        
        add(msgPane, cc.xy(4, 6));
                
        JPanel btnPane = new JPanel(new FormLayout("f:max(50dlu;pref)", "c:m, c:m, c:m, c:m, c:m, c:m"));
        validateContentBtn = new JButton(getResourceString("WB_UPLOAD_VALIDATE_CONTENT_BTN"));
        validateContentBtn.setActionCommand(VALIDATE_CONTENT);
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
        btnPane.add(validateContentBtn, cc.xy(1, 1));
        btnPane.add(doUploadBtn, cc.xy(1,2));
        btnPane.add(viewUploadBtn, cc.xy(1, 3));
        btnPane.add(viewSettingsBtn, cc.xy(1, 4));
        btnPane.add(undoBtn, cc.xy(1, 5));
        btnPane.add(closeBtn, cc.xy(1, 6));
        add(btnPane, cc.xy(6, 6));
        
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
        
        msgList.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
               msgClick(false);
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

        validationErrorList.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
               msgClick(true);
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
    protected void msgClick(final boolean isValidationErrorMsg)
    {
        if (listener != null)
        {
            if (isValidationErrorMsg)
            {
                listener.actionPerformed(new ActionEvent(validationErrorList, 0, MSG_CLICK));
            }
            else
            {
                listener.actionPerformed(new ActionEvent(msgList, 0, MSG_CLICK));
            }
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
     * @return the msgList
     */
    public JList getMsgList()
    {
        return msgList;
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
     * @return the validateContentBtn
     */
    public JButton getValidateContentBtn()
    {
        return validateContentBtn;
    }
    /**
     * @return the msgLbl
     */
    public JLabel getMsgLbl()
    {
        return msgLbl;
    }

    /**
     * @return the msgPane
     */
    public JPanel getMsgPane()
    {
        return msgPane;
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
        setBtnListener(validateContentBtn, listener);
        setBtnListener(doUploadBtn, listener);
        setBtnListener(viewSettingsBtn, listener);
        setBtnListener(viewUploadBtn, listener);
        setBtnListener(closeBtn, listener);
        setBtnListener(cancelBtn, listener);
        setBtnListener(undoBtn, listener);
        setBtnListener(printBtn, listener);
    }
    
    public void addMsg(UploadMessage msg)
    {
        boolean isValErrMsg = false;
        JList list;
        if (msg instanceof UploadTableInvalidValue)
        {
            list = this.validationErrorList;
            isValErrMsg = true;
            
        }
        else
        {
            list = this.msgList;
        }
        ((DefaultListModel)list.getModel()).addElement(msg);
        if (isValErrMsg && !isValidationErrorsVisible())
        {
            this.showValidationErrors();
        }
        list.ensureIndexIsVisible(list.getModel().getSize()-1);
    }
    
    /**
     * 
     * @param toClear
     *
     * Removes messages whose class is contained in toClear from the message lists.
     * Hides the ValidationError list if it is empty.
     */
    public void clearMsgs(final Class<?>[] toClear)
    {
        DefaultListModel model1 = (DefaultListModel)msgList.getModel();
        DefaultListModel model2 = (DefaultListModel)this.validationErrorList.getModel();
        for (int c=0; c<toClear.length; c++)
        {
            for (int i = model1.getSize()-1; i >= 0; i--)
            {
                if (model1.getElementAt(i).getClass().equals(toClear[c]))
                {
                    model1.remove(i);
                }
            }
            for (int i = model2.getSize()-1; i >= 0; i--)
            {
                if (model2.getElementAt(i).getClass().equals(toClear[c]))
                {
                    model2.remove(i);
                }
            }
        }
        if (model2.getSize() == 0)
        {
            this.hideValidationErrors();
        }
    }
    
    public void updateObjectsCreated()
    {
        showObjectsCreated(false);
    }
    
    public void clearObjectsCreated()
    {
        showObjectsCreated(true);
    }

    /**
     * @param clear
     * 
     * Updates display of 'Affected Tables'.
     */
    protected void showObjectsCreated(boolean clear)
    {
        //This is weird, but because Table and List view are using the same
        //UploadInfoRenderable objects, both views will get refreshed by
        //using the List view model. Even though, in current version of the interface, 
        //"objects created" are only being shown in the table view.
        DefaultListModel model = (DefaultListModel)uploadTbls.getModel();
        for (int i = model.getSize()-1; i >= 0; i--)
        {
            if (clear)
            {
                ((UploadInfoRenderable)model.getElementAt(i)).reset();
            }
            ((UploadInfoRenderable)model.getElementAt(i)).refresh();
        }
        uploadTbls.repaint();
        this.uploadTblTbl.repaint();
    }

    
    public class TesterThingy implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals(UploadMainPanel.DO_UPLOAD))
            {
                System.out.println(UploadMainPanel.DO_UPLOAD);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.VIEW_UPLOAD))
            {
                System.out.println(UploadMainPanel.VIEW_UPLOAD);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.VIEW_SETTINGS))
            {
                System.out.println(UploadMainPanel.VIEW_SETTINGS);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.CLOSE_UI))
            {
                System.out.println(UploadMainPanel.CLOSE_UI);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.CANCEL_OPERATION))
            {
                System.out.println(UploadMainPanel.CANCEL_OPERATION);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.TBL_DBL_CLICK))
            {
                System.out.println(UploadMainPanel.TBL_DBL_CLICK);
            }
            else if (e.getActionCommand().equals(UploadMainPanel.MSG_CLICK))
            {
                System.out.println(UploadMainPanel.MSG_CLICK);
            }
       }
    }
    
    public static void main(final String[] args)
    {
        final UploadMainPanel tf = new UploadMainPanel();
        tf.buildUI();
        DefaultListModel tbls = new DefaultListModel();
        tbls.addElement("CollectingEvent");
        tbls.addElement("Collection Object");
        tbls.addElement("Taxon");
        tf.getUploadTbls().setModel(tbls);
        
        DefaultListModel invalids = new DefaultListModel();
        invalids.addElement("good");
        invalids.addElement("dog");
        DefaultListModel msgs = new DefaultListModel();
        msgs.addElement("happy");
        msgs.addElement("man");
        tf.getMsgList().setModel(msgs);
        tf.getValidationErrorList().setModel(invalids);
        tf.getMsgPane().setVisible(invalids.size() > 0);
        
        tf.setActionListener(tf.new TesterThingy());
        
        tf.setVisible(true);

        JMenuBar menuBar = new JMenuBar();

      //Build the first menu.
      JMenu menu = new JMenu("A Menu");
      menuBar.add(menu);

      //a group of JMenuItems
      JMenuItem menuItem = new JMenuItem("flipper",
                               KeyEvent.VK_F);
      menuItem.addActionListener(new ActionListener()
      {
          public void actionPerformed(ActionEvent e)
          {
              if (tf.isValidationErrorsVisible())
              {
                  tf.hideValidationErrors();
              }
              else
              {
                  tf.showValidationErrors();
              }
          }
      });
      menu.add(menuItem);
      
      menuItem = new JMenuItem("list");
      menuItem.addActionListener(new ActionListener()
      {
          public void actionPerformed(ActionEvent e)
          {
              tf.showUploadTblList();
          }
      });
      menu.add(menuItem);
      
      menuItem = new JMenuItem("table");
      menuItem.addActionListener(new ActionListener()
      {
          public void actionPerformed(ActionEvent e)
          {
              tf.showUploadTblTbl();
          }
      });
      menu.add(menuItem);

      JFrame frm = new JFrame();
        frm.setPreferredSize(new Dimension(600,400));
        frm.setContentPane(tf);
        frm.setJMenuBar(menuBar);
        frm.pack();
        frm.setVisible(true);
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
     * @return the printBtn
     */
    public JButton getPrintBtn()
    {
        return printBtn;
    }

    /**
     * @return the validationErrorList
     */
    public JList getValidationErrorList()
    {
        return validationErrorList;
    }

    /**
     * @return the validationErrorPanel
     */
    public JPanel getValidationErrorPanel()
    {
        return validationErrorPanel;
    }

    /**
     * @return the uploadTblTbl
     */
    public JTable getUploadTblTbl()
    {
        return uploadTblTbl;
    }
}
