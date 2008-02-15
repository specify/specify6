/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.ui.db;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 8, 2007
 *
 */
public class TextFieldWithQuery extends JPanel implements CustomQueryListener
{
    protected static final Logger log = Logger.getLogger(TextFieldWithQuery.class);
    
    protected int                  popupDlgThreshold = 15;
            
    protected JTextField           textField;
    protected Object               dataObj        = null;
    protected Vector<Integer>      idList         = new Vector<Integer>();
    protected Vector<String>       list           = new Vector<String>();
    protected JPopupMenu           popupMenu      = null;
    protected JButton              dbBtn;
    protected boolean              isPopupShowing = false;
    
    protected boolean              popupFromBtn   = false;
    protected boolean              ignoreFocusLost = false;
    
    protected boolean              addAddItem     = false;
    
    protected DBTableInfo          tableInfo;
    protected String               sql;
    protected String               displayColumns;
    protected String               format;
    protected String[]             keyColumns;
    protected int                  numColumns          = -1;
    protected Object[]             values;
    protected Hashtable<Integer, Object[]> duplicatehash = new Hashtable<Integer, Object[]>();
    
    protected List<ListSelectionListener> listSelectionListeners = new ArrayList<ListSelectionListener>();
    protected PopupMenuListener    popupMenuListener   = null;
    protected Integer              selectedId          = null;
    protected String               currentText         = "";
    protected boolean              hasNewText          = false;
    
    protected boolean              isDoingCount        = false;
    protected Integer              returnCount         = null;
    
    protected QueryWhereClauseProvider queryWhereClauseProvider = null;
    
    /**
     * Constructor.
     */
    public TextFieldWithQuery(final DBTableInfo tableInfo,
                              final String keyColumn,
                              final String displayColumns,
                              final String format)
    {
        super();
        this.tableInfo      = tableInfo;
        this.displayColumns = displayColumns != null ? displayColumns : keyColumn;
        this.format         = format;

        if (StringUtils.contains(keyColumn, ","))
        {
            keyColumns = StringUtils.split(keyColumn, ",");
        } else
        {
            keyColumns = new String[] {keyColumn};
        }
        popupDlgThreshold = AppPreferences.getRemote().getInt("TFQ.POPUPDLD.THRESHOLD", 15);
        
        createUI();
    }
    
    /**
     * 
     */
    public void createUI()
    {
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setLayout(new BorderLayout());
        
        textField = new JTextField(10);
        ImageIcon img = IconManager.getIcon("DropDownArrow", IconManager.IconSize.NonStd);
        dbBtn     = UIHelper.isMacOS() ? new MacGradiantBtn(img) : new JButton(img);
        dbBtn.setFocusable(false);
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:d:g,p", "f:p:g"), this);
        CellConstraints cc = new CellConstraints();
        
        pb.add(textField, cc.xy(1,1));
        pb.add(dbBtn, cc.xy(2, 1));
        
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e)
            {
                cbxKeyReleased(e);
                super.keyReleased(e);
            }
        });
        
        textField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent arg0)
            {
                int len = textField.getText().length();
                if (len > 0)
                {
                    textField.setCaretPosition(len);
                    textField.selectAll();
                }
                
                super.focusGained(arg0);
            }

            @Override
            public void focusLost(FocusEvent arg0)
            {
                if (selectedId == null && !ignoreFocusLost)
                {
                    textField.setText("");
                    selectedId = null;
                    
                    ListSelectionEvent lse = new ListSelectionEvent(TextFieldWithQuery.this, 0, 0, false);
                    for (ListSelectionListener l : listSelectionListeners)
                    {
                        l.valueChanged(lse);
                    }
                }
                super.focusLost(arg0);
            }
            
        });
        
        dbBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                if (!popupFromBtn)
                {
                    fillBox(currentText);
                    popupFromBtn = true;
                    
                } else
                {
                    if (popupMenu != null)
                    {
                        popupMenu.setVisible(false);
                    }
                    popupFromBtn = false;
                }
            }
        });
    }
    
    /**
     * @param queryWhereClauseProvider
     */
    public void setQueryWhereClauseProvider(QueryWhereClauseProvider queryWhereClauseProvider)
    {
        this.queryWhereClauseProvider = queryWhereClauseProvider;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        textField.setEnabled(enabled);
        dbBtn.setEnabled(enabled);
    }
    
    /**
     * @param popupMenuListener the popupMenuListener to set
     */
    public void setPopupMenuListener(PopupMenuListener popupMenuListener)
    {
        this.popupMenuListener = popupMenuListener;
    }

    /**
     * @param listChangeListener the listChangeListener to set
     */
    public void addListSelectionListener(ListSelectionListener l)
    {
        this.listSelectionListeners.add(l);
    }

    /**
     * @param listChangeListener the listChangeListener to set
     */
    public void removeListSelectionListener(ListSelectionListener l)
    {
        this.listSelectionListeners.remove(l);
    }

    /**
     * @param addAddItem the addAddItem to set
     */
    public void setAddAddItem(boolean addAddItem)
    {
        this.addAddItem = addAddItem;
    }

    /**
     * Processes the KeyEvent.
     * @param ev event
     */
    protected void cbxKeyReleased(KeyEvent ev)
    {
        //log.debug(ev.getKeyCode() +"  "+ KeyEvent.VK_TAB);
        if (ev.getKeyCode() == KeyEvent.VK_TAB || 
            ev.getKeyCode() == KeyEvent.VK_SHIFT || 
            ev.getKeyCode() == KeyEvent.VK_LEFT || 
            ev.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            return;
        }
        
        currentText = textField.getText();
        //log.debug("hasNewText "+hasNewText+"  "+currentText.length());
        if (currentText.length() == 0 || !hasNewText)
        {
            if (ev.getKeyCode() != JAutoCompComboBox.SEARCH_KEY &&
                    ev.getKeyCode() != KeyEvent.VK_DOWN )
            {
                if (ev.getKeyCode() != KeyEvent.VK_ENTER)
                {
                    idList.clear();
                    list.clear();
                    selectedId = null;
                    
                    // 02/09/08 - This should be done here - rods
                    /*if (listSelectionListeners != null)
                    {
                        for (ListSelectionListener l : listSelectionListeners)
                        {
                            l.valueChanged(null);
                        }
                    }*/
                    log.debug("setting hasNewText to true");
                    hasNewText  = true;
                }
            } else
            {
                popupFromBtn = false;
                showPopup(); // add only
                return;
            }
        } else
        {
            hasNewText  = true;
            //log.debug("setting hasNewText to true");
        }

        if (ev.getKeyCode() == JAutoCompComboBox.SEARCH_KEY ||
            ev.getKeyCode() == KeyEvent.VK_DOWN)
        {
            fillBox(textField.getText());
        }
    }
    
    public void setText(final String text)
    {
        textField.setText(text);
    }
    
    /**
     * @param mi
     */
    protected void itemSelected(final JMenuItem mi)
    {
        hasNewText = false;
        //log.debug("setting hasNewText to true");
        
        String selectedStr = mi.getText();
        int inx = popupMenu.getComponentIndex(mi);
        if (inx > -1)
        {
            if (!addAddItem || inx > 0)
            {
                selectedId = idList.get(addAddItem ? inx-1 : inx);
                textField.setText(selectedStr);
            }
            
            if (listSelectionListeners != null)
            {
                ListSelectionEvent lse = new ListSelectionEvent(mi, 0, 0, false);
                for (ListSelectionListener l : listSelectionListeners)
                {
                    l.valueChanged(lse);
                }
            }
        }
    }
    
    /**
     * 
     */
    protected void showPopup()
    {
        if (hasNewText || currentText.length() == 0)
        {
            ActionListener al = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    itemSelected((JMenuItem)e.getSource());
                }
            };
            
            popupMenu = new JPopupMenu();
            if (popupMenuListener != null)
            {
                popupMenu.addPopupMenuListener(popupMenuListener);
            }
            
            popupMenu.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuCanceled(PopupMenuEvent e)
                {
                    isPopupShowing  = false;
                    ignoreFocusLost = false;
                    //popupFromBtn    = false;
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                    isPopupShowing  = false;
                    ignoreFocusLost = false;
                    //popupFromBtn    = false;
                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                    isPopupShowing = true;
                }
            });
            
            if (addAddItem)
            {
                JMenuItem mi = new JMenuItem(UIRegistry.getResourceString("TFWQ_ADD_LABEL"));
                popupMenu.add(mi);
                mi.addActionListener(al); 
            }
            
            for (String str : list)
            {
                JMenuItem mi = new JMenuItem(str);
                popupMenu.add(mi);
                mi.addActionListener(al);
            }
        }
        
        if (popupMenu != null)
        {
            UIHelper.addSpecialKeyListenerForPopup(popupMenu);
            
            final Point     location = getLocation();
            final Dimension size     = getSize();
            
            popupFromBtn = false;
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    ignoreFocusLost = true;
                    popupMenu.show(TextFieldWithQuery.this, location.x, location.y+size.height);
                    popupMenu.requestFocus();
                }
            });
        }

    }
    
    /**
     * Builds the SQL to be used to do the search.
     * @param newEntryStr the string value to be searched
     * @param isForCount do query for count of returns
     * @return the full sql string.
     */
    protected String buildSQL(final String newEntryStr, final boolean isForCount)
    {
        if (sql == null)
        {
            StringBuilder sb = new StringBuilder();
            if (QueryAdjusterForDomain.getInstance().isUerInputNotInjectable(newEntryStr))
            {
                sb.append("SELECT ");
                if (isForCount)
                {
                    sb.append("count(");                
                    sb.append(tableInfo.getIdFieldName());                
                    sb.append(")");
                    
                } else
                {
                    sb.append(displayColumns);
                    sb.append(",");
                    sb.append(tableInfo.getIdFieldName());                
                }
    
                sb.append(" FROM ");
                sb.append(tableInfo.getClassName());
                sb.append(" WHERE ");
                
                String specialCols = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, true);
                if (StringUtils.isNotEmpty(specialCols))
                {
                    sb.append(specialCols);
                    sb.append(" AND ");
                }
                
                if (keyColumns.length > 1)
                {
                    sb.append("(");
                }

                int cnt = 0;
                for (String keyCol : keyColumns)
                {
                    if (cnt > 0) sb.append(" OR ");
                    sb.append(" LOWER(");
                    sb.append(keyCol);
                    sb.append(") LIKE '");
                    sb.append(newEntryStr.toLowerCase());
                    sb.append("%' ");
                    cnt++;
                }
                
                if (keyColumns.length > 1)
                {
                    sb.append(")");
                }

                sb.append(" ORDER BY ");

                cnt = 0;
                for (String keyCol : keyColumns)
                {
                    if (cnt > 0) sb.append(", ");
                    sb.append(keyCol);
                    sb.append(" ASC");
                    cnt++;
                }
            }
            return sb.toString();
        }
        return sql;
    }
    
    /**
     * Process the results from the search
     * @param customQuery the query
     */
    public void processResults(final CustomQueryIFace customQuery)
    {
        List<?> dataObjList =  customQuery.getDataObjects();
        if (dataObjList == null || dataObjList.size() == 0)
        {
            textField.setText("");
            return;
        }
        
        boolean isFirst = true;
        duplicatehash.clear();
        for (Object obj : dataObjList)
        {
            Object[] array = (Object[])obj;
            
            if (isFirst)
            {
                numColumns = array.length - 1;
                values     = new Object[numColumns];
                isFirst = false;
            }
            
            Integer id = (Integer)array[numColumns];
            idList.addElement(id);
            
            if (duplicatehash.get(id) == null)
            {
                duplicatehash.put(id, array);
                
                if (numColumns == 1)
                {
                    list.addElement(array[0].toString());
                    
                } else
                {
                    try
                    {
                        for (int i=0;i<numColumns;i++)
                        {
                            Object val = array[i];
                            values[i] = val != null ? val : "";
                        }
                        Formatter formatter = new Formatter();
                        formatter.format(format, values);
                        list.addElement(formatter.toString());

                    } catch (java.util.IllegalFormatConversionException ex)
                    {
                        ex.printStackTrace();
                        
                        list.addElement(values[0] != null ? values[0].toString() : "(No Value)");
                    }

                }
            }
        }
        
        if (idList.size() > 0 && returnCount != null)
        {
            if (returnCount > popupDlgThreshold)
            {
                showDialog();
                
            } else
            {
                showPopup();
            }
            
        } else
        {
            textField.setText("");
        }
        
        duplicatehash.clear();
    }
    
    /**
     * 
     */
    protected void showDialog()
    {
        DefaultListModel model = new DefaultListModel();
        if (addAddItem)
        {
            model.addElement(UIRegistry.getResourceString("TFWQ_ADD_LABEL"));
        }
        for (String val : list)
        {
            model.addElement(val);
        }
        
        final JList listBox = new JList(model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(UIRegistry.getResourceString("TFWQ_CHOOSE_LABEL"), SwingConstants.CENTER), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(listBox, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(sp, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        // Had to do inner class in order to get it to select an item
        // before being shown
        class PopUpDialog extends CustomDialog
        {
            protected JList pListBox;
            
            public PopUpDialog(final Frame     frame, 
                               final boolean   isModal,
                               final Component contentPanel,
                               JList pListBoxArg) throws HeadlessException
            {
                super(frame, UIRegistry.getResourceString("TFWQ_CHOOSE_TITLE"), isModal, contentPanel);
                
                this.pListBox = pListBoxArg;
                
                pListBox.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            if (okBtn != null && pListBox != null)
                            {
                                okBtn.setEnabled(listBox.getSelectedIndex() != -1);
                            }
                        }
                    }
                });
                pListBox.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        super.mouseClicked(e);
                        
                        if (e.getClickCount() == 2)
                        {
                            okBtn.setEnabled(true);
                            okBtn.doClick();
                        }
                    }
                });
            }

            @Override
            public void setVisible(boolean visible)
            {
                if (visible)
                {
                    listBox.setSelectedIndex(addAddItem ? 1 : 0);
                }
                super.setVisible(visible);
            }
        }
        
        CustomDialog dlg = new PopUpDialog((Frame)UIRegistry.getTopWindow(), true, panel, listBox);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            int     inx        = listBox.getSelectedIndex();
            boolean isDoingAdd = inx == 0 && addAddItem;
            
            inx = addAddItem ? inx-1 : inx;

            if (!isDoingAdd)
            {
                selectedId = idList.get(inx);
                textField.setText(list.get(inx));
            }
            
            if (listSelectionListeners != null)
            {
                ListSelectionEvent lse = new ListSelectionEvent(listBox, 0, 0, false);
                for (ListSelectionListener l : listSelectionListeners)
                {
                    l.valueChanged(lse);
                }
            }
        } else
        {
            textField.setText("");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void exectionDone(final CustomQueryIFace customQuery)
    {
        if (isDoingCount)
        {
            List<?> dataObjList = customQuery.getDataObjects();
            if (dataObjList != null && dataObjList.size() > 0)
            {
                returnCount = (Integer)dataObjList.get(0);
                System.out.println(returnCount);
            }
            isDoingCount = false;
            
            list.clear();
            idList.clear();
            
            JPAQuery jpaQuery = new JPAQuery(buildSQL(((JPAQuery)customQuery).getData().toString(), false), this);
            isDoingCount = false;
            jpaQuery.start();
            
        } else
        {
            processResults(customQuery);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void executionError(final CustomQueryIFace customQuery)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Fill the the drop down with the list from the query
     */
    protected void fillBox(final String newEntryStr)
    {
        if (hasNewText)
        {
            list.clear();
            idList.clear();
            
            returnCount  = null;
            isDoingCount = true;
                       
            JPAQuery jpaQuery = new JPAQuery(buildSQL(newEntryStr, true), this);
            jpaQuery.setUnique(true);
            jpaQuery.setData(newEntryStr);
            jpaQuery.start();
            
        } else
        {
            showPopup();
        }
    }
    
    /**
     * @return the list
     */
    public List<String> getList()
    {
        return list;
    }

    /**
     * @return the id of the selected item
     */
    public Integer getSelectedId()
    {
        return selectedId;
    }

    /**
     * @param selectedId the selectedId to set
     */
    public void setSelectedId(final Integer selectedId)
    {
        this.selectedId = selectedId;
    }

    public void requestFocus()
    {
        textField.requestFocus();
    }
    
    public void clearSelection()
    {
        list.clear();
        selectedId = null;
    }
    
    public boolean hasItem()
    {
        return selectedId != null;
    }
    
    public void clearSearch()
    {
        textField.setText("");
    }
    
    public JTextField getTextField()
    {
        return textField;
    }
    
    public interface QueryWhereClauseProvider
    {
        public String getExtraWehereCaluse();
    }
    
    //--------------------------------------------------------------------------
    // Special class for the right visual appearence on the Mac.
    //--------------------------------------------------------------------------
    class MacGradiantBtn extends JButton
    {
        protected ImageIcon imgIcon;
        protected boolean   isPressed = false;
        
        protected Color top1 = new Color(184, 217, 250);
        protected Color top2  = new Color(120, 180, 241);
        
        protected Color bot1 = new Color(74, 155, 236);
        protected Color bot2 = new Color(179, 248, 255);
        
        protected Color topDarker1;
        protected Color topDarker2;
        protected Color botDarker1;
        protected Color botDarker2;
        
        
        
        /**
         * @param imgIcon the icon image for the dropdown
         */
        public MacGradiantBtn(ImageIcon imgIcon)
        {
            super(imgIcon);
            this.imgIcon = imgIcon;
            
            topDarker1 = UIHelper.changeColorBrightness(top1, 0.95);
            topDarker2 = UIHelper.changeColorBrightness(top2, 0.95);
            botDarker1 = UIHelper.changeColorBrightness(bot1, 0.95);
            botDarker2 = UIHelper.changeColorBrightness(bot2, 0.95);
            
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e)
                {
                    super.mousePressed(e);
                    isPressed = true;
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    super.mouseReleased(e);
                    isPressed = false;
                }
                
            });
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        public void paint(Graphics g)
        {
            super.paint(g);
            
            if (isEnabled())
            {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = this.getWidth() - 6;
                int h = this.getHeight() - 6;
                
                int x = 3;
                int y = 3;
          
                drawButtonBody(g2, x, y, w, (h/2)+4,   isPressed ? topDarker1 : top1, isPressed ? topDarker2 : top2);
                drawButtonBody(g2, x, y+(h/2), w, h/2, isPressed ? botDarker1 : bot1, isPressed ? botDarker2 : bot2);
                
                x = (this.getWidth() - imgIcon.getIconWidth()) / 2;
                y = (this.getHeight() - imgIcon.getIconHeight()) / 2;
                g.drawImage(imgIcon.getImage(), x, y, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
            }
            
        }
        
        /**
         * Draws the button body.
         * @param g2 the graphics to be painted into
         * @param w the width of the control
         * @param h the height of the control
         * @param color the of the background
         */
        protected void drawButtonBody(Graphics2D g2, int x, int y, int w, int h, Color color, Color color2) 
        {
            // draw the button body
            GradientPaint bg = new GradientPaint(new Point(x,y), color,
                                                 new Point(x,y+h), color2);
            g2.setPaint(bg);
            g2.fillRoundRect(x, y, w, h, 6, 6);
        }
    }
    
}
