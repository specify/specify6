/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.ESTermParser;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsSQL;
import edu.ku.brc.af.core.expresssearch.SearchTermField;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This is a "generic" or more specifically "configurable" search panel class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.
 *
 * NOTE: The second Constructor has not been tested! (It is designed as a single text field only that doesn't need a form view).
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DBObjSearchPanel extends JPanel implements ExpressSearchResultsPaneIFace, PropertyChangeListener
{
    private static final Logger log  = Logger.getLogger(DBObjSearchDialog.class);

    // Form Stuff
    protected ViewIFace      formView   = null;
    protected Viewable       form       = null;
    protected List<String>   fieldIds   = new ArrayList<String>();
    protected List<String>   fieldNames = new ArrayList<String>();
    protected ActionListener doQuery    = null;
    
    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;
    protected String         searchName;
    protected String         fieldName;
    
    // UI
    protected JButton        okBtn;
    protected JTextField     searchText;

    protected JPanel         panel;
    protected JScrollPane    scrollPane;
    protected JTable         table;

    protected JButton        searchBtn;
    protected Color          textBGColor    = null;
    protected Color          badSearchColor = new Color(255,235,235);

    protected Hashtable<String, ExpressResultsTableInfo> tables = new Hashtable<String, ExpressResultsTableInfo>();
    protected ExpressResultsTableInfo          esTableInfo;
    protected ESResultsTablePanelIFace         etrb = null;

    protected List<Integer>                    idList         = null;
    protected String                           sqlStr;

    protected Hashtable<String, Object>        dataMap = new Hashtable<String, Object>();
    
    protected ViewBasedSearchQueryBuilderIFace queryBuilder        = null;
    protected QueryForIdResultsIFace           queryForIdResults   = null;
    protected boolean                          isMultipleSelection = true;

    
    /**
     * Constructs a search dialog from form infor and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param searchName the search name, this is looked up by name in the "search_config.xml" file
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public DBObjSearchPanel(final String  viewSetName, 
                            final String  viewName, 
                            final String  searchName,
                            final String  className,
                            final String  idFieldName,
                            final int     searchBtnPos) throws HeadlessException
    {
        //this((Window)parent, viewSetName, viewName, searchName, className, idFieldName);
        super(new BorderLayout());
        
        this.className   = className;
        this.idFieldName = idFieldName;  
        this.searchName  = searchName;
        
        init();
        
        
        String rowDef;
        if (searchBtnPos == SwingConstants.TOP)
        {
            rowDef = "t:p";
        } else if (searchBtnPos == SwingConstants.BOTTOM)
        {
            rowDef = "b:p"; 
        } else
        {
            rowDef = "p";
        }
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,1dlu,p", rowDef));
        CellConstraints cc = new CellConstraints();

        
        formView = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, dataMap, MultiView.NO_OPTIONS, null);

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        if (form != null)
        {
            fieldIds = new ArrayList<String>();
            form.getFieldIds(fieldIds);
            for (String id : fieldIds)
            {
                Component comp = form.getCompById(id);
                if (comp instanceof JTextField)
                {
                    ((JTextField)comp).addActionListener(doQuery);
                }
            }
            
            form.getFieldNames(fieldNames);
            
            createSearchBtn();
    
            pb.add(form.getUIComponent(), cc.xy(1,1));
    
            pb.add(searchBtn, cc.xy(3,1));
    
            add(pb.getPanel(), BorderLayout.NORTH);
            
            createUI();
            
        } else
        {
            log.error("ViewSet ["+viewSetName + "] View["+viewName + "] could not be created.");
        }
    }

    /**
     * Constructs a search dialog from form infor and from search info.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param searchName the search name, this is looked up by name in the "search_config.xml" file
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public DBObjSearchPanel(final String fieldName,
                            final String className,
                            final String idFieldName) throws HeadlessException
    {
        super(new BorderLayout());
        
        this.className   = className;
        this.idFieldName = idFieldName;  
        this.fieldName   = fieldName;  
        
        init();
        
        searchText = new JTextField(30);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                }
            }
        });
        
        PanelBuilder    pb  = new PanelBuilder(new FormLayout("p,1dlu,p", "p,2dlu,p,2dlu,p"));
        CellConstraints cc = new CellConstraints();

        pb.add(searchText, cc.xy(1,1));
        pb.add(searchBtn, cc.xy(3,1));

        add(pb.getPanel(), BorderLayout.NORTH);

        createUI();
    }
    
    /**
     * @return the searchName
     */
    public String getSearchName()
    {
        return searchName;
    }

    /**
     * Gets the data from the UI whether it is using a form or a single field. 
     */
    protected void getDataFromUI()
    {
        if (form != null)
        {
            form.getDataFromUI();
            
        } else
        {
            dataMap.put(fieldName, searchText.getText().trim());
        }
    }

    /**
     * Common initializer.
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     */
    protected void init()
    {
        doQuery = new ActionListener() 
        {
            public void actionPerformed(final ActionEvent e)
            {
                doStartQuery((JComponent)e.getSource());
            }
        };
        
        if (queryBuilder == null)
        {
            ExpressResultsTableInfo esTblInfo = ExpressSearchConfigCache.getTableInfoByName(searchName);
            if (esTblInfo != null)
            {
               esTableInfo = esTblInfo;
               esTableInfo.setViewSQLOverridden(true);
               
               tables.put(esTableInfo.getTableId(), esTableInfo);
               
               sqlStr = esTableInfo.getViewSql();
    
           } else
           {
               throw new RuntimeException("Couldn't find search name["+searchName+"] in the search_config.xml");
           }
        }
    }
    
    /**
     *  Creates the Search btn and hooks it up.
     */
    protected void createSearchBtn()
    {
        searchBtn  = UIHelper.createButton(getResourceString("SEARCH"));
        searchBtn.addActionListener(doQuery);
    }
    
    /**
     * Creates the Default UI.
     *
     */
    public void createUI()
    {
        panel      = new JPanel(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(300,200));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     }
    
    /**
     * @return the scroll pane for the results.
     */
    public JScrollPane getScrollPane()
    {
        return scrollPane;
    }
    
    /**
     * @return the form
     */
    public Viewable getForm()
    {
        return form;
    }

    /**
     * Sets the okBtn from the dialog.
     * @param okBtn the btn
     */
    public void setOKBtn(final JButton okBtn)
    {
        this.okBtn = okBtn;
        updateUIControls();
   }

    /**
     * Updates the OK button (sets whether it is enabled by checking to see if there is a recordset
     */
    protected void updateUIControls()
    {
        if (okBtn != null)
        {
            okBtn.setEnabled(idList != null && ((idList.size() == 1 && !isMultipleSelection) || (idList.size() > 0 && isMultipleSelection)));
        }
    }

    /**
     * Makes sure the UI is enabled correctly.
     * @param enabled true/false
     */
    protected void setUIEnabled(final boolean enabled)
    {
        form.getFieldIds(fieldIds);
        for (String fieldId : fieldIds)
        {
            Component comp = form.getCompById(fieldId);
            if (comp instanceof JTextField)
            {
                ((JTextField)comp).setEnabled(enabled);
            }
        }
        searchBtn.setEnabled(enabled);
    }
    
    /**
     * @param comp
     */
    protected void doStartQuery(final JComponent comp)
    {
        getDataFromUI();
        
        QueryForIdResultsIFace resultsInfo = null;
        if (queryBuilder != null)
        {
            sqlStr = queryBuilder.buildSQL(dataMap, fieldNames);
            if (StringUtils.isNotEmpty(sqlStr))
            {
                resultsInfo = queryBuilder.createQueryForIdResults();
                resultsInfo.setSQL(sqlStr);
                resultsInfo.setMultipleSelection(isMultipleSelection);
                
            } else
            {
                UIRegistry.getStatusBar().setErrorMessage(getResourceString("ES_SUSPICIOUS_SQL"));
                return;
            }
            
        } else
        {
            QueryAdjusterForDomain qafd   = QueryAdjusterForDomain.getInstance();
            StringBuilder          strBuf = new StringBuilder(256);
            int cnt = 0;
            for (ERTICaptionInfo captionInfo : esTableInfo.getVisibleCaptionInfo())
            {
                Object value  = dataMap.get(captionInfo.getColName());
                
                log.debug("Column Name["+captionInfo.getColName()+"] Value["+value+"]");
                if (value != null)
                {
                    String valStr = value.toString();
                    if (valStr.length() > 0)
                    {
                        if (qafd.isUserInputNotInjectable(valStr))
                        {
                            if (ESTermParser.parse(valStr.toLowerCase(), true))
                            {
                                if (StringUtils.isNotEmpty(valStr))
                                {
                                    List<SearchTermField> fields     = ESTermParser.getFields();
                                    SearchTermField       firstTerm  = fields.get(0);
                                    
                                    if (cnt > 0)
                                    {
                                        strBuf.append(" OR ");
                                    }
                                    
                                    String clause = ESTermParser.createWhereClause(firstTerm, null, captionInfo.getColName());
                                    strBuf.append(clause);
                                    cnt++;
                                }
                            }
                        } else
                        {
                            UIRegistry.getStatusBar().setErrorMessage(getResourceString("ES_SUSPICIOUS_SQL"));
                            return;
                        }
                    }
                }/* else
                {
                    log.debug("DataMap was null for Column Name["+captionInfo.getColName()+"] make sure there is a field of this name in the form.");
                }*/
            }
            
            if (cnt == 0)
            {
                return;  
            }
            
            String fullStrSql = QueryAdjusterForDomain.getInstance().adjustSQL(sqlStr);
            String fullSQL    = fullStrSql.replace("%s", strBuf.toString());
            log.info(fullSQL);
            setUIEnabled(false);
            
            resultsInfo = new QueryForIdResultsSQL(esTableInfo.getId(), null, esTableInfo, 0, "");
            resultsInfo.setSQL(fullSQL);
            resultsInfo.setMultipleSelection(isMultipleSelection);
        }
        
        addSearchResults(resultsInfo);
        
        if (comp instanceof JTextField)
        {
           final JTextField txt = (JTextField)comp;
           int len = txt.getText().length();
           txt.setSelectionEnd(len);
           txt.setSelectionStart(0);
           
           SwingUtilities.invokeLater(new Runnable() {
               public void run()
               {
                   txt.requestFocus();
               }
           });
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addSearchResults(edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace)
     */
    public void addSearchResults(final QueryForIdResultsIFace results)
    {
        idList = null;
        
        updateUIControls();

        remove(panel != null ? panel : scrollPane);
        panel = null;
        
        if (etrb != null)
        {
            etrb.cleanUp();
        }
        
        if (true)
        {
            etrb = new ESResultsTablePanel(this, results, false, true, false);
            etrb.setPropertyChangeListener(this);
            
        } else
        {
            etrb = null; // Instantiate your class here
            etrb.initialize(this, results);
            etrb.setPropertyChangeListener(this);
        }
        
        scrollPane = new JScrollPane(etrb.getUIComponent(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        validate();
        
        setUIEnabled(true);
        repaint();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ExpressSearchResultsPaneIFace#removeTable(edu.ku.brc.af.tasks.subpane.ExpressTableResultsBase)
     */
    public void removeTable(final ESResultsTablePanelIFace etrbTable)
    {
        etrbTable.cleanUp();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void addTable(final ESResultsTablePanelIFace etrBase)
    {
        // It has already been added so don't do anything
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ExpressSearchResultsPaneIFace#revalidateScroll()
     */
    public void revalidateScroll()
    {
        scrollPane.revalidate();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#hasResults()
     */
    public boolean hasResults()
    {
        return etrb.hasResults();
    }

    /**
     * @return
     */
    private Object getSelectedObjects(final boolean doAll)
    {
        List<Object> objList = null;
        String errMsg = null;
        
        if (idList != null && idList.size() > 0)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                for (Integer id : idList)
                {
                    log.debug("getSelectedObject class["+className+"] idFieldName["+idFieldName+"] id["+id+"]");
                    Class<?> classObj = Class.forName(className);
                    List<?> list = session.getDataList(classObj, idFieldName, id, DataProviderSessionIFace.CompareType.Restriction);
                    
                    if (list.size() == 1)
                    {
                        if (doAll)
                        {
                            if (objList == null)
                            {
                                objList = new Vector<Object>();
                            }
                            objList.add(list.get(0));
                            
                        } else
                        {
                            return list.get(0);
                        }
                        
                    } else if (list.size() == 0)
                    {
                        errMsg = "Why could we NOT load the object with id["+id+"] for class["+className+"]in DBObjSearchDialog?";
                    } else
                    {
                        errMsg = "Why would more than one object be found in DBObjSearchDialog? return size["+list.size()+"]";
                    }
                }

                return objList;
                
            } catch (Exception ex)
            {
                errMsg = ex.toString();
                ex.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        
        if (errMsg != null)
        {
            throw new RuntimeException(errMsg);
        }
        return null;
    }
    
    /**
     * @return
     */
    public Object getSelectedObject()
    {
        return getSelectedObjects(false);
    }
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Object> getSelectedObjects()
    {
        return (List<Object>)getSelectedObjects(true);

    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("selection"))
        {
            Integer numRowsSelected = (Integer)evt.getOldValue();
            if (numRowsSelected > 0)
            {
                idList = etrb.getListOfIds(false);
            } else
            {
                idList = null;
            }
            updateUIControls();
            
        } else if (evt.getPropertyName().equals("doubleClick"))
        {
            okBtn.doClick();
            
        } else if (evt.getPropertyName().equals("loaded"))
        {
            // If there is only one returned, then select it and focus the OK defult btn
            if (evt.getNewValue() != null && ((Integer)evt.getNewValue()) == 1)
            {
                ESResultsTablePanel etrbPanel = (ESResultsTablePanel)etrb;
                etrbPanel.getTable().setRowSelectionInterval(0, 0);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        okBtn.requestFocus();
                    }
                });
                
            }
        }
    }

    /**
     * @param builder
     */
    public void registerQueryBuilder(final ViewBasedSearchQueryBuilderIFace builder)
    {
        this.queryBuilder = builder;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isMultipleSelection()
     */
    public boolean isMultipleSelection()
    {
        return isMultipleSelection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    public void setMultipleSelection(boolean isMultiple)
    {
        isMultipleSelection = isMultiple;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#doQueriesSynchronously()
     */
    public boolean doQueriesSynchronously()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#done()
     */
    public void done()
    {
        // not needed
    }
}
