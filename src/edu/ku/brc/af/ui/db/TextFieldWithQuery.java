/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.db;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import sun.awt.CausedFocusEvent;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 8, 2007
 *
 */
@SuppressWarnings("serial")
public class TextFieldWithQuery extends JPanel
{
    protected static final Logger log = Logger.getLogger(TextFieldWithQuery.class);
    
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    
    protected int                              popupDlgThreshold = 15;
            
    protected JTextField                       textField;
    protected Object                           dataObj        = null;
    protected Vector<Integer>                  idList         = new Vector<Integer>();
    protected Vector<String>                   list           = new Vector<String>();
    protected JPopupMenu                       popupMenu      = null;
    protected JButton                          dbBtn;
    protected boolean                          isPopupShowing = false;
    protected AtomicBoolean                    isDoingQuery   = new AtomicBoolean(false);

    protected boolean                          ignoreTab      = false;
    protected boolean                          tabOutSearch   = false;
    protected boolean                          doAdjustQuery  = true;
    
    protected boolean                          doAddAddItem   = false;
    
    protected DBTableInfo                      tableInfo;
    protected DBFieldInfo                      fieldInfo;
    protected String                           sql;
    protected String                           displayColumns;
    protected String                           format;
    protected String                           fieldFormatterName;
    protected UIFieldFormatterIFace            uiFieldFormatter         = null;
    protected String                           sqlTemplate              = null;
    protected ViewBasedSearchQueryBuilderIFace builder                  = null;
    protected String[]                         keyColumns;
    protected int                              numColumns               = -1;
    protected Object[]                         values;
    protected Hashtable<Integer, Object[]>     duplicatehash            = new Hashtable<Integer, Object[]>();
    
    protected List<ListSelectionListener>      listSelectionListeners = new ArrayList<ListSelectionListener>();
    protected PopupMenuListener                popupMenuListener   = null;
    protected Integer                          selectedId          = null;
    protected String                           currentText         = ""; //$NON-NLS-1$
    protected boolean                          hasNewText          = false;
    protected boolean                          wasCleared          = false;
    protected boolean                          ignoreDocChange     = false;
    protected boolean                          isReadOnlyMode      = false;
    
    protected AtomicBoolean                    isDoingCount        = new AtomicBoolean(false);
    protected Integer                          returnCount         = null;
    protected String                           prevEnteredText     = null;
    protected String                           cachedPrevText      = null;
    protected String                           searchedForText     = null;
    protected FontMetrics                      fontMetrics         = null;
    
    protected ExternalQueryProviderIFace      externalQueryProvider = null;

    /**
     * Constructor.
     */
    public TextFieldWithQuery(final DBTableInfo tableInfo,
                              final String      keyColumn,
                              final String      displayColumns,
                              final String      format,
                              final String      fieldFormatterName,
                              final String      sqlTemplate)
    {
        super();
        this.tableInfo          = tableInfo;
        this.fieldInfo          = tableInfo.getFieldByName(keyColumn);
        this.displayColumns     = displayColumns != null ? displayColumns : keyColumn;
        this.format             = format;
        this.fieldFormatterName = fieldFormatterName;
        this.sqlTemplate        = sqlTemplate;
        
        if (fieldInfo != null && fieldInfo.getFormatter() != null)
        {
            uiFieldFormatter = fieldInfo.getFormatter();
            
        } else if (StringUtils.isNotEmpty(fieldFormatterName))
        {
            uiFieldFormatter = UIFieldFormatterMgr.getInstance().getFormatter(fieldFormatterName);
        }

        if (StringUtils.contains(keyColumn, ",")) //$NON-NLS-1$
        {
            keyColumns = StringUtils.split(keyColumn, ","); //$NON-NLS-1$
        } else
        {
            keyColumns = new String[] {keyColumn};
        }
        popupDlgThreshold = AppPreferences.getRemote().getInt("TFQ.POPUPDLD.THRESHOLD", 15); //$NON-NLS-1$
        
        createUI();
    }
    
    /**
     * 
     */
    public void createUI()
    {
        //setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setLayout(new BorderLayout());
        setOpaque(false);
        
        textField = new JTextField(10);
        // we are going to handle focus traversal manually
        // so that we can do searches when 'tab' is pressed
        textField.setFocusTraversalKeysEnabled(false);
        setControlSize(textField);

        ImageIcon img = IconManager.getIcon("DropDownArrow", IconManager.IconSize.NonStd); //$NON-NLS-1$
        dbBtn     = UIHelper.isMacOS() ? new MacGradiantBtn(img) : new JButton(img);
        dbBtn.setFocusable(false);
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:d:g,p", "f:p:g"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc = new CellConstraints();
        
        pb.add(textField, cc.xy(1,1));
        pb.add(dbBtn, cc.xy(2, 1));


        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (ignoreTab && e.getKeyCode() == KeyEvent.VK_TAB) {
                    ignoreTab = false;
                } else {
                    cbxKeyReleased(e);
                }
                super.keyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (popupMenu != null && popupMenu.isVisible()) {
                    popupMenu.setVisible(false);
                }
                super.keyReleased(e);
            }
        });
        
        textField.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e) {
                prevEnteredText = textField.getText();
                boolean oldWasCleared = wasCleared;
                if (!ignoreDocChange) {
                    wasCleared = wasCleared || selectedId != null;

                    idList.clear();
                    list.clear();
                    selectedId = null;
                    if (oldWasCleared != wasCleared && wasCleared) {
                        notifyListenersOfChange(StringUtils.isEmpty(prevEnteredText) ? null : TextFieldWithQuery.this);
                    }
                }
            }
        });

        StringBuilder sb = new StringBuilder();
        for (String k : keyColumns)
        {
            String title = k;
            DBFieldInfo fi = tableInfo.getFieldByName(k);
            if (fi != null)
            {
                title = fi.getTitle();
            }
            if (sb.length() > 0) sb.append(", ");
            sb.append(title);
        }
        textField.setToolTipText(UIRegistry.getFormattedResStr("TFWQ_SEARCHES_FLDS", sb.toString()));


        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (e instanceof CausedFocusEvent) {
                    switch (((CausedFocusEvent) e).getCause()) {
                        case TRAVERSAL:
                        case TRAVERSAL_BACKWARD:
                        case TRAVERSAL_FORWARD:
                        case TRAVERSAL_UP:
                        case TRAVERSAL_DOWN:
                            ignoreTab = true;
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                int len = textField.getText().length();
                if (len < 1)
                {

                    setText(""); //$NON-NLS-1$

                    ///////////////////////////////////////////////////////////////////////////////////
                    // We only want to generate a change event if it once had a value and then it is
                    // cleared and the user tabs to a new control. - rods 02/28/08
                    ///////////////////////////////////////////////////////////////////////////////////
                    if (wasCleared || selectedId != null)
                    {
                        notifyListenersOfChange(TextFieldWithQuery.this);
                    }
                }
                textField.setCaretPosition(0);
            }
        });

        dbBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                if (popupMenu != null && popupMenu.isVisible())
                {
                    popupMenu.setVisible(false);
                }
                
                log.debug("currentText: "+currentText);
                doQuery(currentText, currentText, 0);
            }
        });
    }
    

    public void setReadOnlyMode()
    {
        this.isReadOnlyMode = true;
        ViewFactory.changeTextFieldUIForDisplay(textField, false);
        dbBtn.setVisible(false);
    }

    public boolean isPopupShowing() {
        return isPopupShowing;
    }



    /**
     * @return the prevEnteredText
     */
    public String getPrevEnteredText()
    {
        if (StringUtils.isEmpty(prevEnteredText) && StringUtils.isNotEmpty(cachedPrevText))
        {
            return cachedPrevText;
        }
        return prevEnteredText;
    }

    /**
     * @param prevEnteredText the prevEnteredText to set
     */
    public void setPrevEnteredText(String prevEnteredText)
    {
        this.prevEnteredText = prevEnteredText;
        this.cachedPrevText  = prevEnteredText;
    }


    /**
     * @return the format
     */
    public String getFormat()
    {
        return format;
    }


    /**
     * @return the uiFieldFormatter
     */
    public UIFieldFormatterIFace getUiFieldFormatter()
    {
        return uiFieldFormatter;
    }

    /**
     * @param externalQueryProvider
     */
    public void setExternalQueryProvider(ExternalQueryProviderIFace externalQueryProvider)
    {
        this.externalQueryProvider = externalQueryProvider;
    }

    /**
     * @param sqlTemplate the sqlTemplate to set
     */
    public void setSqlTemplate(String sqlTemplate)
    {
        this.sqlTemplate = sqlTemplate;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
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
     * @param l the listChangeListener to set
     */
    public void addListSelectionListener(ListSelectionListener l)
    {
        this.listSelectionListeners.add(l);
    }

    /**
     * @param l the listChangeListener to set
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
        this.doAddAddItem = addAddItem;
    }

    /**
     * Processes the KeyEvent.
     * @param ev event
     */
    protected void cbxKeyReleased(KeyEvent ev)
    {
        if (isReadOnlyMode)
        {
            return;
        }
        if (ev.getKeyCode() == KeyEvent.VK_SHIFT ||
            ev.getKeyCode() == KeyEvent.VK_LEFT || 
            ev.getKeyCode() == KeyEvent.VK_RIGHT || 
            ev.getKeyCode() == KeyEvent.VK_CONTROL || 
            ev.getKeyCode() == KeyEvent.VK_META)
        {
            return;
        }
        
        currentText = textField.getText();
        if (uiFieldFormatter != null)
        {
            currentText = uiFieldFormatter.formatFromUI(currentText).toString();
        } 
        if (currentText.length() == 0 || !hasNewText)
        {
            if (ev.getKeyCode() == KeyEvent.VK_TAB) {
                if (ev.isShiftDown()) {
                    textField.transferFocusBackward();
                } else {
                    textField.transferFocus();
                }
                return;
            }

            if (ev.getKeyCode() == JAutoCompComboBox.SEARCH_KEY ||
                    ev.getKeyCode() == KeyEvent.VK_DOWN)
            {
                        showPopup(0); // add only
                        return;
            }

            if (ev.getKeyCode() != KeyEvent.VK_ENTER)
            {
                // Add variable to track whether it once had a value and now it does not rods - 02/28/08

                idList.clear();
                list.clear();
                selectedId = null;

                // 02/09/08 - This should not be done here - rods
                // The reason is, that we may have added something only to remove
                // before leaving the control. So we should never send the notification
                // just because we delete the contents. (see wasCleared above)

                /*if (listSelectionListeners != null)
                {
                    notifyListenersOfChange(TextFieldWithQuery.this);
                }*/
                //log.debug("setting hasNewText to true"); //$NON-NLS-1$
                hasNewText  = true;
            }
        } else
        {
            hasNewText  = true;
            //log.debug("setting hasNewText to true");
        }
        

        if (ev.getKeyCode() == JAutoCompComboBox.SEARCH_KEY ||
                ev.getKeyCode() == KeyEvent.VK_TAB ||
                ev.getKeyCode() == KeyEvent.VK_DOWN)
        {
            String origText = textField.getText();
            String text     = origText;
            if (uiFieldFormatter != null && !uiFieldFormatter.isNumeric())
            {
                text = uiFieldFormatter.formatFromUI(text).toString();
            }
            text = StringUtils.replace(text, "'", "\'");
            text = StringUtils.replace(text, "\"", "\\\"");

            // direction of focus change 1: forward 0: none -1: backwards
            int focusChange = (ev.isShiftDown() ? -1 : 1) *
                    (ev.getKeyCode() == KeyEvent.VK_TAB ? 1 : 0);
            doQuery(text, origText, focusChange);
        }
    }
    
    /**
     * @param text
     */
    public void setText(final String text)
    {
        ignoreDocChange = true;
        if (uiFieldFormatter != null && StringUtils.isNotEmpty(text))
        {
            textField.setText(uiFieldFormatter.formatToUI(text).toString());
        } else
        {
            // 10/2/08 - rods - Not sure why the the if was put here.
            //if (!textField.getText().isEmpty())
            {
                textField.setText(text);
            }
        }
        ignoreDocChange = false;
    }
    
    /**
     * @param mi
     * @param advanceFocus
     * @param idListClosure
     */
    protected void itemSelected(final JMenuItem mi, final int advanceFocus, Vector<Integer> idListClosure)
    {
        hasNewText = false;
        //log.debug("setting hasNewText to true");
        if (advanceFocus != 0) SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                transferFocus(advanceFocus);
            }
        });

        String selectedStr = mi.getText();
        int inx = popupMenu.getComponentIndex(mi);
        if (inx > -1)
        {
            if (idListClosure.size() > 0 && (!doAddAddItem || inx > 0))
            {
                selectedId =  idListClosure.get(doAddAddItem ? inx-1 : inx);
                setText(selectedStr);
            }
            
            if (listSelectionListeners != null)
            {
                String value = StringUtils.isEmpty(prevEnteredText) ? cachedPrevText : prevEnteredText;
                notifyListenersOfChange(mi.getText().equals(UIRegistry.getResourceString("TFWQ_ADD_LABEL")) ? new AddItemEvent(value) : mi);
            }
        }
    }

    public static class AddItemEvent {
        public final String value;
        public AddItemEvent(String value) { this.value = value; }
    }
    
    /**
     *
     * @param advanceFocus
     */
    protected void showPopup(final int advanceFocus)
    {
        final Vector<Integer> idListClosure = (Vector<Integer>) idList.clone();
        if (!isEnabled())
        {
            return;
        }
        
        if (hasNewText || currentText.length() == 0)
        {
            ActionListener al = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    itemSelected((JMenuItem)e.getSource(), advanceFocus, idListClosure);
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

                    cachedPrevText = null;
                    
                    if (selectedId == null)
                    {
                        setText(""); //$NON-NLS-1$
                    }
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                    isPopupShowing  = false;

                    cachedPrevText = prevEnteredText;
                    
                    if (selectedId == null)
                    {
                        setText(""); //$NON-NLS-1$
                    }
                    //textField.requestFocus();
                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                    cachedPrevText = null;
                    isPopupShowing = true;
                }
            });
            
            if (doAddAddItem)
            {
                JMenuItem mi = new JMenuItem(UIRegistry.getResourceString("TFWQ_ADD_LABEL")); //$NON-NLS-1$
                setControlSize(mi);

                popupMenu.add(mi);
                mi.addActionListener(al); 
            }
            
            for (String str : list)
            {
                String label = str;
                if (uiFieldFormatter != null)
                {
                    label = uiFieldFormatter.formatToUI(label).toString();
                }
                JMenuItem mi = new JMenuItem(label);
                setControlSize(mi);

                popupMenu.add(mi);
                mi.addActionListener(al);
            }
        }
        
        if (popupMenu != null)
        {
            if (list.size() > 0 || doAddAddItem)
            {
                UIHelper.addSpecialKeyListenerForPopup(popupMenu);
                
                final Point     location = getLocation();
                final Dimension size     = getSize();
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        popupMenu.setInvoker(TextFieldWithQuery.this);
                        popupMenu.show(TextFieldWithQuery.this, location.x, location.y+size.height);
                        Dimension popupSize = popupMenu.getPreferredSize();
                        popupMenu.setPopupSize(Math.max(size.width, popupSize.width), popupSize.height);
                        popupMenu.requestFocus();
                    }
                });
            } else
            {
                popupMenu = null;
            }
        }

    }
    
    /**
     * Adds the field name with the table abbrev if it doesn't already have one.
     * @param abbrev the table abbrev
     * @param fld the field name
     * @param selectSB the StringBuilder to add it to 
     */
    protected void addTblAbbrev(final String abbrev, final String fld, final StringBuilder selectSB)
    {
        if (!StringUtils.contains(fld, '.'))
        {
            selectSB.append(tableInfo.getAbbrev());    
            selectSB.append("."); //$NON-NLS-1$
        }
        selectSB.append(fld.trim()); 
    }
    
    /**
     * @param doAdjustQuery
     */
    public void setDoAdjustQuery(boolean doAdjustQuery)
    {
        this.doAdjustQuery = doAdjustQuery;
    }

    /**
     * Builds the SQL to be used to do the search.
     * @param newEntryStr the string value to be searched
     * @param isForCount do query for count of returns
     * @return the full sql string.
     */
    protected String buildSQL(final String newEntryStr, final boolean isForCount)
    {
        if (externalQueryProvider != null)
        {
            String fullSQLStr = externalQueryProvider.getFullSQL(newEntryStr, isForCount);
            if (StringUtils.isNotEmpty(fullSQLStr))
            {
                return fullSQLStr;
            }
        }
        
        StringBuilder whereSB = new StringBuilder();
        if (keyColumns.length > 1)
        {
            whereSB.append("("); //$NON-NLS-1$
        }

        int cnt = 0;
        for (String keyCol : keyColumns)
        {
            String [] colParts = keyCol.split("\\.");
            String abbrev = colParts.length > 1 ? colParts[0] : tableInfo.getAbbrev();
            String fld = colParts.length > 1 ? colParts[1] : keyCol;
        	if (cnt > 0) whereSB.append(" OR "); //$NON-NLS-1$
            whereSB.append(" LOWER("); //$NON-NLS-1$
            whereSB.append(abbrev + "." + fld);
            whereSB.append(") LIKE '"); //$NON-NLS-1$
            //The following condition is added specifically for catalog number lookups. Probably.
            if (uiFieldFormatter != null && uiFieldFormatter.isNumeric())
            {
                whereSB.append("%"); //$NON-NLS-1$
            }
            whereSB.append(newEntryStr.toLowerCase());
            whereSB.append("%' "); //$NON-NLS-1$
            cnt++;
        }
        
        if (keyColumns.length > 1)
        {
            whereSB.append(")"); //$NON-NLS-1$
        }
        
        if (externalQueryProvider != null)
        {
            String extraWhereClause = externalQueryProvider.getExtraWhereClause();
            if (StringUtils.isNotEmpty(extraWhereClause))
            {
                whereSB.append(' ');
                whereSB.append(extraWhereClause);
            }
        }

        if (StringUtils.isNotEmpty(sqlTemplate))
        {
            StringBuilder selectSB = new StringBuilder();
            if (isForCount)
            {
                selectSB.append("count(");  //$NON-NLS-1$
                addTblAbbrev(tableInfo.getAbbrev(), tableInfo.getIdFieldName(), selectSB);
                selectSB.append(")"); //$NON-NLS-1$
                
            } else
            {
                if (StringUtils.contains(displayColumns, ','))
                {
                    int fCnt = 0;
                    for (String fld : StringUtils.split(displayColumns, ','))
                    {
                        if (fCnt > 0) selectSB.append(", "); //$NON-NLS-1$
                        addTblAbbrev(tableInfo.getAbbrev(), fld, selectSB);
                        fCnt++;
                    }
                } else
                {
                    addTblAbbrev(tableInfo.getAbbrev(), displayColumns, selectSB);
                }
                selectSB.append(", "); //$NON-NLS-1$
                addTblAbbrev(tableInfo.getAbbrev(), tableInfo.getIdFieldName(), selectSB);
            }
            //System.err.println( selectSB.toString());
            
            sql = StringUtils.replace(sqlTemplate, "%s1", selectSB.toString()); //$NON-NLS-1$
            sql = StringUtils.replace(sql, "%s2", whereSB.toString()); //$NON-NLS-1$
            if (doAdjustQuery)
            {
                sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            }
            
            //log.debug(sql);
            System.err.println(sql);
            
            return sql;
        }
        
        if (sql == null)
        {
            StringBuilder sb = new StringBuilder();
            if (QueryAdjusterForDomain.getInstance().isUserInputNotInjectable(newEntryStr))
            {
                sb.append("SELECT "); //$NON-NLS-1$
                if (isForCount)
                {
                    sb.append("count(");                 //$NON-NLS-1$
                    sb.append(tableInfo.getAbbrev() + "." + tableInfo.getIdFieldName());
                    sb.append(")"); //$NON-NLS-1$
                    
                } else
                {
                    sb.append(tableInfo.getAbbrev() + "." + displayColumns);
                    sb.append(","); //$NON-NLS-1$
                    sb.append(tableInfo.getAbbrev() + "." + tableInfo.getIdFieldName());                
                }
    
                sb.append(" FROM "); //$NON-NLS-1$
                sb.append(tableInfo.getClassName());
                sb.append(" as "); //$NON-NLS-1$
                sb.append(tableInfo.getAbbrev());
                
                String joinSnipet = doAdjustQuery ? QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, true, null, false) : null; //arg 2: false means SQL
                if (joinSnipet != null)
                {
                    sb.append(' ');
                    sb.append(joinSnipet);
                    sb.append(' ');
                }
                
                sb.append(" WHERE "); //$NON-NLS-1$
                
                //System.err.println(sb.toString());
                
                String specialCols = doAdjustQuery ? QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, true) : null;//, false, isForCount ? null : tableInfo.getAbbrev());
                if (StringUtils.isNotEmpty(specialCols))
                {
                    if (whereSB.length() > 0) whereSB.append(" AND "); //$NON-NLS-1$
                    whereSB.append(specialCols);
                    //System.err.println(whereSB.toString());
                }
                
                if (externalQueryProvider != null)
                {
                    String extraWhereClause = externalQueryProvider.getExtraWhereClause();
                    if (StringUtils.isNotEmpty(extraWhereClause))
                    {
                        whereSB.append(' ');
                        whereSB.append(extraWhereClause);
                    }
                }
                
                whereSB.append(" ORDER BY "); //$NON-NLS-1$
                whereSB.append(displayColumns); //$NON-NLS-1$
                whereSB.append(" ASC"); //$NON-NLS-1$
                
//                cnt = 0;
//                for (String keyCol : keyColumns)
//                {
//                    if (cnt > 0) whereSB.append(", "); //$NON-NLS-1$
//                    whereSB.append(keyCol);
//                    whereSB.append(" ASC"); //$NON-NLS-1$
//                    cnt++;
//                }

                sb.append(whereSB.toString());
                
            }
            //log.debug("* "+sql);
            return sb.toString();
        }
        return sql;
    }
    
    /**
     * Process the results from the search
     * @param customQuery the query
     * @param advanceFocus
     */
    private void processResults(final CustomQueryIFace customQuery, int advanceFocus)
    {
        searchedForText = prevEnteredText;
        
        List<?> dataObjList =  customQuery.getDataObjects();
        if (dataObjList == null || dataObjList.size() == 0)
        {
            if (doAddAddItem)
            {
                showPopup(advanceFocus);
            }
            
        } else
        {
            Dimension dim  = getSize();
            if (fontMetrics == null)
            {
                Font font = getFont();
                BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                fontMetrics = bi.getGraphics().getFontMetrics(font);
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
                    isFirst    = false;
                }
                
                Integer id = (Integer)array[numColumns];
                idList.addElement(id);
                
                if (duplicatehash.get(id) == null)
                {
                    duplicatehash.put(id, array);
                    
                    if (numColumns == 1)
                    {
                        Object value = array[0].toString();
                        if (uiFieldFormatter != null)
                        {
                            value = uiFieldFormatter.formatToUI(value);
                            
                        } else if (StringUtils.isNotEmpty(format))
                        {
                            Object oldVal = builder == null ? null : value;
                        	value = UIHelper.getFormattedValue(format, value);
                        	if (builder != null && value == null && oldVal != null) {
                        		//customized qbx format interfering with builder's selected field(s).
                        		value = oldVal;
                        	}
                        }
                    list.addElement(value != null ? value.toString() : "xxx");
                        
                    } else
                    {
                        try
                        {
                            for (int i=0;i<numColumns;i++)
                            {
                                Object val = array[i];
                                if (val instanceof Calendar)
                                {
                                    val = scrDateFormat.format((Calendar)val);
                                } else if (val instanceof Date)
                                {
                                    val = scrDateFormat.format((Date)val);
                                }
                                if (val instanceof FormDataObjIFace)
                                {
                                    val = ((FormDataObjIFace)val).getIdentityTitle();
                                }
                                values[i] = val != null ? val : null; //$NON-NLS-1$
                            }
                            
                            String valStr = (String)UIHelper.getFormattedValue(format, values);
                            
                            if (returnCount <= popupDlgThreshold && fontMetrics.stringWidth(valStr) > dim.width)
                            {
                                int len = valStr.length() - 5;
                                while (len > 25)
                                {
                                    valStr = valStr.substring(0, len);
                                    if (fontMetrics.stringWidth(valStr) < dim.width)
                                    {
                                        valStr = valStr + "...";
                                        break;
                                    }
                                    len -= 5;
                                }
                            }
                            
                            // Minor hack for Bug 5824 for names with no first name
                            // In the future we may want to do a strip of spaces form the end first
                            if (valStr.endsWith(", "))
                            {
                                valStr = valStr.substring(0, valStr.length()-2);
                            }
                            list.addElement(valStr);
    
                        } catch (java.util.IllegalFormatConversionException ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TextFieldWithQuery.class, ex);
                            ex.printStackTrace();
                            
                            list.addElement(values[0] != null ? values[0].toString() : "(No Value)"); //$NON-NLS-1$
                        }
    
                    }
                }
            }
            
            if (idList.size() > 0 && returnCount != null)
            {
                if (tabOutSearch && idList.size() == 1)
                {
                    selectedId = idList.elementAt(0);
                    setText(list.get(0));
                    notifyListenersOfChange(textField);
                        
                } else if (returnCount > popupDlgThreshold)
                {
                    showDialog(advanceFocus);
                    
                } else
                {
                    showPopup(advanceFocus);
                }
                
            } else
            {
                setText(""); //$NON-NLS-1$
            }
            
            duplicatehash.clear();
        }
    }
    
    /**
     * @return where it has at least one Id
     */
    public boolean hasId()
    {
        return selectedId != null;
    }
    
    /**
     * @param source
     */
    private void notifyListenersOfChange(final Object source)
    {
        if (listSelectionListeners != null)
        {
            ListSelectionEvent lse = source == null ? null : new ListSelectionEvent(source, 0, 0, false);
            for (ListSelectionListener l : listSelectionListeners)
            {
                l.valueChanged(lse);
            }
        }
    }
    
    /**
     *
     * @param advanceFocus
     */
    protected void showDialog(final int advanceFocus)
    {
        final Vector<Integer> idListLocal = (Vector<Integer>) idList.clone();
        final Vector<String> listLocal = (Vector<String>) list.clone();

        final String enteredText = StringUtils.isEmpty(prevEnteredText) ? cachedPrevText : prevEnteredText;


        DefaultListModel<String> model = new DefaultListModel<String>();
        if (doAddAddItem)
        {
            model.addElement(UIRegistry.getResourceString("TFWQ_ADD_LABEL")); //$NON-NLS-1$
        }
        
        for (String val : list)
        {
            model.addElement(val);
        }
        
        final JList<String> listBox = new JList<String>(model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createLabel(UIRegistry.getResourceString("TFWQ_CHOOSE_LABEL"), SwingConstants.CENTER), BorderLayout.NORTH); //$NON-NLS-1$
        panel.add(UIHelper.createScrollPane(listBox, true), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        // Had to do inner class in order to get it to select an item
        // before being shown
        class PopUpDialog extends CustomDialog
        {
            protected JList<String> pListBox;
            
            public PopUpDialog(final Frame     frame, 
                               final boolean   isModal,
                               final Component contentPanel,
                               JList<String> pListBoxArg) throws HeadlessException
            {
                super(frame, UIRegistry.getResourceString("TFWQ_CHOOSE_TITLE"), isModal, contentPanel); //$NON-NLS-1$
                this.pListBox = pListBoxArg;
                initialize();
            }
            
            public PopUpDialog(final Dialog     dialog, 
                               final boolean   isModal,
                               final Component contentPanel,
                               JList<String> pListBoxArg) throws HeadlessException
            {
                super(dialog, UIRegistry.getResourceString("TFWQ_CHOOSE_TITLE"), isModal, OK_BTN | CANCEL_BTN, contentPanel); //$NON-NLS-1$
                this.pListBox = pListBoxArg;
                initialize();
            }
            
            /**
             * 
             */
            protected void initialize()
            {
                
                
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
            public void setVisible(final boolean visible)
            {
                if (visible)
                {
                    listBox.setSelectedIndex(doAddAddItem ? 1 : 0);
                }
                super.setVisible(visible);
            }
        }
        
        hasNewText   = false;
        
        Window mostRecent = UIRegistry.getMostRecentWindow();
        CustomDialog dlg;
        if (mostRecent instanceof Dialog)
        {
            dlg = new PopUpDialog((Dialog)UIRegistry.getMostRecentWindow(), true, panel, listBox);
        } else
        {
            dlg = new PopUpDialog((Frame)UIRegistry.getMostRecentWindow(), true, panel, listBox);
        }
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            int     inx        = listBox.getSelectedIndex();
            boolean isDoingAdd = inx == 0 && doAddAddItem;
            
            inx = doAddAddItem ? inx-1 : inx;

            if (!isDoingAdd && inx < idListLocal.size())
            {
                selectedId = idListLocal.get(inx);
                setText(listLocal.get(inx));
            }

            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    transferFocus(advanceFocus);
                    notifyListenersOfChange(isDoingAdd ? new AddItemEvent(enteredText) : listBox);
                }
            });
           
            
        } else
        {
            setText(""); //$NON-NLS-1$
        }
    }

    protected void transferFocus(int direction) {
        if (direction > 0) textField.transferFocus();
        if (direction < 0) textField.transferFocusBackward();
    }
    
    protected void queryDone(final CustomQueryIFace customQuery, final int advanceFocus)
    {
        if (isDoingCount.get())
        {
            List<?> dataObjList = customQuery.getDataObjects();
            if (dataObjList != null && dataObjList.size() > 0)
            {
                returnCount = (Integer)dataObjList.get(0);
            }
            
            if (returnCount != null && returnCount == 0)
            {
                processResults(new EmptyCustomQuery(customQuery), advanceFocus);
                isDoingQuery.set(false);
                return;
            }
            
            list.clear();
            idList.clear();
            
            String sqlStr = null;
            if (builder != null)
            {
                sqlStr = builder.buildSQL(((JPAQuery)customQuery).getData().toString(), false);
            }
            
            if (sqlStr == null)
            {
                sqlStr = buildSQL(((JPAQuery)customQuery).getData().toString(), false);
            }
            
            //log.debug(sqlStr);
            JPAQuery jpaQuery = new JPAQuery(sqlStr, new CustomQueryListener() {
                @Override
                public void exectionDone(CustomQueryIFace customQuery) {
                    queryDone(customQuery, advanceFocus);
                }

                @Override
                public void executionError(CustomQueryIFace customQuery) {
                    isDoingQuery.set(false);
                }
            });
            isDoingCount.set(false);
            jpaQuery.start();
            
        } else
        {
            processResults(customQuery, advanceFocus);
            isDoingQuery.set(false);
        }
    }


    /**
     * Fill the the drop down with the list from the query
     */
    protected void doQuery(final String newEntryStrArg, final String origTextStr, final int focusChange)
    {
        UIRegistry.getStatusBar().setText("");
        if (StringUtils.isEmpty(newEntryStrArg))
        {
            return;
        }
       
        ESTermParser parser = ESTermParser.getInstance();
        
        if (parser.parse(newEntryStrArg, true))
        {
            String newEntryStr = parser.getFields().get(0).getTermLowerCase();
            prevEnteredText = origTextStr;
    
            if (!isDoingQuery.get())
            {
                if (hasNewText)
                {
                    
                    isDoingQuery.set(true);
                    isDoingCount.set(true);
                    
                    list.clear();
                    idList.clear();
                    
                    returnCount  = null;
                    
                    String newSql = null;
                    if (builder != null)
                    {
                        newSql = builder.buildSQL(newEntryStr, true);
                    }
                    
                    if (newSql == null)
                    {
                        newSql = buildSQL(newEntryStr, true);
                    }
                    
                    if (StringUtils.isBlank(newSql))
                    {
                    	Toolkit.getDefaultToolkit().beep();
                    	UIRegistry.displayLocalizedStatusBarError("TFWQ_InvalidEntry", newEntryStr);
                    	isDoingQuery.set(false);
                    	isDoingCount.set(false);
                    }
                    else
                    {
                    	JPAQuery jpaQuery = new JPAQuery(newSql, new CustomQueryListener() {
                            @Override
                            public void exectionDone(CustomQueryIFace customQuery) {
                                queryDone(customQuery, focusChange);
                            }

                            @Override
                            public void executionError(CustomQueryIFace customQuery) {
                                isDoingQuery.set(false);
                            }
                        });
                    	jpaQuery.setUnique(true);
                    	jpaQuery.setData(newEntryStr);
                    	jpaQuery.start();
                    }
                } else if (returnCount != null && returnCount > popupDlgThreshold)
                {
                    showDialog(focusChange);
                    
                } else
                {
                    showPopup(focusChange);
                }
            }
        } else
        {
            showPopup(focusChange); // add only
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
        this.wasCleared = true;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        textField.requestFocus();
    }
    
    public void clearSelection()
    {
        list.clear();
        idList.clear();
        selectedId  = null;
        hasNewText  = false;
        wasCleared  = true;
        currentText = "";
    }
    
    public boolean hasItem()
    {
        return selectedId != null;
    }
    
    public void clearSearch()
    {
        setText(""); //$NON-NLS-1$
    }
    
    public JTextField getTextField()
    {
        return textField;
    }

    //--------------------------------------------------------------------------
    // ExternalQueryProviderIFace
    //--------------------------------------------------------------------------

    public interface ExternalQueryProviderIFace
    {
        /**
         * @return just the additional where clause must start with "AND/OR".
         */
        public String getExtraWhereClause();
        
        /**
         * @return the full SQL to be used.
         */
        public String getFullSQL(final String newEntryStr, final boolean isForCount);
    }
    
    //--------------------------------------------------------------------------
    // Special class for the right visual appearance on the Mac.
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
        @Override
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

    /**
     * @return the builder
     */
    public ViewBasedSearchQueryBuilderIFace getBuilder()
    {
        return builder;
    }

    /**
     * @param builder the builder to set
     */
    public void setBuilder(ViewBasedSearchQueryBuilderIFace builder)
    {
        this.builder = builder;
    }
    
    //--------------------------------------------------------------------------
    // Class for short cutting search if count returns zero.
    //--------------------------------------------------------------------------
    class EmptyCustomQuery implements CustomQueryIFace
    {
        private CustomQueryIFace cqi;
        
        /**
         * 
         */
        public EmptyCustomQuery(CustomQueryIFace cqi)
        {
            super();
            this.cqi = cqi;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#cancel()
         */
        @Override
        public void cancel()
        {
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute()
         */
        @Override
        public boolean execute()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#execute(edu.ku.brc.dbsupport.CustomQueryListener)
         */
        @Override
        public void execute(CustomQueryListener cql)
        {
            
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#getDataObjects()
         */
        @Override
        public List<?> getDataObjects()
        {
            return new ArrayList<Object>();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#getName()
         */
        @Override
        public String getName()
        {
            return cqi.getName();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#getQueryDefinition()
         */
        @Override
        public List<QueryResultsContainerIFace> getQueryDefinition()
        {
            return cqi.getQueryDefinition();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#getResults()
         */
        @Override
        public List<QueryResultsDataObj> getResults()
        {
            return new ArrayList<QueryResultsDataObj>();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#getTableIds()
         */
        @Override
        public List<Integer> getTableIds()
        {
            return cqi.getTableIds();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#isCancelled()
         */
        @Override
        public boolean isCancelled()
        {
            return cqi.isCancelled();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.CustomQueryIFace#isInError()
         */
        @Override
        public boolean isInError()
        {
            return cqi.isInError();
        }
    	/* (non-Javadoc)
    	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#getMaxResults()
    	 */
    	@Override
    	public int getMaxResults() 
    	{
    		return 0;
    	}

    	/* (non-Javadoc)
    	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#setMaxResults(int)
    	 */
    	@Override
    	public void setMaxResults(int maxResults) 
    	{
    		// ignore
    	}
        
    }
}
