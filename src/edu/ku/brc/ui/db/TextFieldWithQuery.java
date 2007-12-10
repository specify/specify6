/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.db;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 8, 2007
 *
 */
public class TextFieldWithQuery extends JPanel implements SQLExecutionListener
{
    protected static final Logger log = Logger.getLogger(TextFieldWithQuery.class);
            
    protected JTextField           textField;
    protected Object               dataObj        = null;
    protected Vector<Integer>      idList         = new Vector<Integer>();
    protected Vector<String>       list           = new Vector<String>();
    protected JPopupMenu           popupMenu      = null;
    protected JButton              dbBtn;
    protected boolean              isPopupShowing = false;
    
    protected boolean              popupFromBtn   = false;
    
    protected boolean              addAddItem     = false;
    
    protected String               sql;
    protected String               tableName;
    protected String               displayColumns;
    protected String               idColumn;
    protected String               format;
    protected String               keyColumn;
    protected int                  numColumns          = -1;
    protected Object[]             values;
    
    protected List<ListSelectionListener> listSelectionListeners = new ArrayList<ListSelectionListener>();
    protected PopupMenuListener    popupMenuListener   = null;
    protected Integer              selectedId          = null;
    protected String               currentText         = "";
    protected boolean              hasNewText          = false;
    
    /**
     * Constructor
     */
    public TextFieldWithQuery(final String sql,
                              final String format)
    {
        this(null, null, null, null, format);
        this.sql = sql;
    }

    /**
     * Constructor
     */
    public TextFieldWithQuery(final String tableName,
                              final String idColumn,
                              final String keyColumn,
                              final String format)
    {
        this(tableName, idColumn, keyColumn, null, format);
        
    }

    /**
     * Constructor.
     */
    public TextFieldWithQuery(final String tableName,
                              final String idColumn,
                              final String keyColumn,
                              final String displayColumns,
                              final String format)
    {
        super();
        this.tableName      = tableName;
        this.keyColumn      = keyColumn;
        this.idColumn       = idColumn;
        this.displayColumns = displayColumns != null ? displayColumns : keyColumn;
        this.format         = format;

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
        dbBtn     = new JButton(IconManager.getIcon("DownArrow", IconManager.IconSize.NonStd));
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
                log.debug("**************************************************** focusGained");
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
                log.debug("**************************************************** focusLost");
                if (selectedId == null)
                {
                    log.debug("xxxxxxxxxxxxxxxxxxxxx *************** focusLost");
                    textField.setText("");
                }
                super.focusLost(arg0);
            }
            
        });
        
        dbBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                //log.debug("isPopupShowing "+isPopupShowing);
                if (!popupFromBtn)
                {
                    fillBox(currentText);
                    popupFromBtn = true;
                    
                } else
                {
                    popupMenu.setVisible(false);
                    popupFromBtn = false;
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
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
        log.debug(ev.getKeyCode() +"  "+ KeyEvent.VK_TAB);
        if (ev.getKeyCode() == KeyEvent.VK_TAB || ev.getKeyCode() == KeyEvent.VK_SHIFT)
        {
            return;
        }
        
        currentText = textField.getText();
        log.debug("hasNewText "+hasNewText+"  "+currentText.length());
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
                    if (listSelectionListeners != null)
                    {
                        for (ListSelectionListener l : listSelectionListeners)
                        {
                            l.valueChanged(null);
                        }
                    }
                    log.debug("setting hasNewText to true");
                    hasNewText  = true;
                }
            } else
            {
                showPopup(); // add only
                return;
            }
        } else
        {
            hasNewText  = true;
            log.debug("setting hasNewText to true");
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
        log.debug("setting hasNewText to true");
        
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
                    isPopupShowing = false;
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                    isPopupShowing = false;
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
            final Point     location = getLocation();
            final Dimension size     = getSize();
            
            popupFromBtn = false;
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    popupMenu.show(TextFieldWithQuery.this, location.x, location.y+size.height);
                    popupMenu.requestFocus();
                }
            });
        }

    }
    
    /**
     * Builds the SQL to be used to do the search.
     * @param newEntryStr the string value to be searched
     * @return the full sql string.
     */
    protected String buildSQL(final String newEntryStr)
    {
        if (sql == null)
        {
            // XXX MYSQL
            return "select distinct " + displayColumns + "," + idColumn  + " from " + tableName + " where lower(" + keyColumn +
                             ") like '"+ newEntryStr.toLowerCase() +"%' order by " + keyColumn + " asc";
        }
        return sql;
    }


    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public synchronized void exectionDone(SQLExecutionProcessor process, ResultSet resultSet)
    {
        try
        {
            if (resultSet != null && resultSet.next())
            {
                if (numColumns == -1)
                {
                    numColumns = resultSet.getMetaData().getColumnCount()-1;
                    values = new Object[numColumns];
                }
    
                do
                {
                    if (numColumns == 1)
                    {
                        idList.addElement(resultSet.getInt(2));
                        list.addElement(resultSet.getString(1));
    
                    } else
                    {
                        try
                        {
                            idList.addElement(resultSet.getInt(numColumns+1));
                            for (int i=0;i<numColumns;i++)
                            {
                                Object val = resultSet.getObject(i+1);
                                values[i] = val != null ? val : "";
                            }
                            Formatter formatter = new Formatter();
                            formatter.format(format, values);
                            list.addElement(formatter.toString());
    
                        } catch (java.util.IllegalFormatConversionException ex)
                        {
                            list.addElement(values[0] != null ? values[0].toString() : "(No Value)");
                        }
                        
                    }
    
                } while(resultSet.next());
                
                if (idList.size() > 0)
                {
                    showPopup();
                } else
                {
                    textField.setText("");
                }
            } else
            {
                textField.setText("");
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public synchronized void executionError(SQLExecutionProcessor process, Exception ex)
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
            
            String queryString = buildSQL(newEntryStr);
            
            SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, queryString);
            sqlProc.start();
            
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
    public void setSelectedId(Integer selectedId)
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
    
}
