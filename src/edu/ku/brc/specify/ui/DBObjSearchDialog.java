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
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.ExpressSearchResults;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResults;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;

/**
 * This is a "generic" or more specifically "configurable" search dialog class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.
 *
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DBObjSearchDialog extends JDialog implements ActionListener, ExpressSearchResultsPaneIFace, ViewBasedSearchDialogIFace
{
    private static final Logger log  = Logger.getLogger(DBObjSearchDialog.class);

    // Form Stuff
    protected View           formView = null;
    protected Viewable       form     = null;
    protected List<String>   fieldIds;
    
    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;
    protected String         searchName;
    
    // UI
    protected boolean        isCancelled    = true;
    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JTextField     searchText;

    protected JPanel         contentPanel;
    protected JScrollPane    scrollPane;
    protected JTable         table;

    protected JButton        searchBtn;
    protected Color          textBGColor    = null;
    protected Color          badSearchColor = new Color(255,235,235);

    protected Hashtable<String, ExpressResultsTableInfo> tables = new Hashtable<String, ExpressResultsTableInfo>();
    protected ExpressResultsTableInfo  tableInfo;
    protected ExpressTableResultsBase  etrb = null;

    protected List<Long>     idList         = null;
    protected String         sqlStr;

    protected Hashtable<String, Object> dataMap = new Hashtable<String, Object>();

    /**
     * Constructs a search dialog from form infor and from search info
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param searchName the search name, this is looked up by name in the "search_config.xml" file
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public DBObjSearchDialog(final Frame  parentFrame,
                             final String viewSetName, 
                             final String viewName, 
                             final String searchName,
                             final String title,
                             final String className,
                             final String idFieldName) throws HeadlessException
    {
        super(parentFrame, title, true);
        
        this.className   = className;
        this.idFieldName = idFieldName;  
        this.searchName  = searchName;  


        ExpressResultsTableInfo tblInfo = ExpressSearchTask.getTableInfoByName(searchName);
        if (tblInfo != null)
        {
           tableInfo = tblInfo;
           tableInfo.setViewSQLOverridden(true);
           
           tables.put(tableInfo.getTableId(), tableInfo);
           
           sqlStr = tableInfo.getViewSql();

           createUI(viewSetName, viewName, title);
           
           setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
           setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
           
       } else
       {
           throw new RuntimeException("Couldn't find search name["+searchName+"] in the search_config.xml");
       }

    }

    /**
     * Creates the Default UI.
     *
     */
    protected void createUI(final String viewSetName, 
                            final String viewName, 
                            final String title)
    {
        searchText = new JTextField(30);
        searchBtn  = new JButton(getResourceString("Search"));
        ActionListener doQuery = new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                contentPanel.removeAll();

                form.getDataFromUI();

                StringBuilder strBuf = new StringBuilder(256);
                int cnt = 0;
                String[] columnNames = tableInfo.getColNames();
                for (String colName : columnNames)
                {
                    Object value  = dataMap.get(colName);
                    log.debug("Column Name["+colName+"] Value["+value+"]");
                    if (value != null)
                    {
                        String valStr = value.toString();
                        if (valStr.length() > 0)
                        {
                            if (cnt > 0)
                            {
                                strBuf.append(" OR ");
                            }
                            strBuf.append(" lower("+colName+") like '#$#"+valStr+"#$#'");
                            cnt++;
                        }
                    } else
                    {
                        log.debug("DataMap was null for Column Name["+colName+"] make sure there is a field of this name in the form.");
                    }
                }
                
                String fullSQL = sqlStr.replace("%s", strBuf.toString());
                log.info(fullSQL);
                fullSQL = fullSQL.replace("#$#", "%");
                log.info(fullSQL);
                tableInfo.setViewSql(fullSQL);
                setUIEnabled(false);
                
                ExpressSearchResults results = new ExpressSearchResults(tableInfo.getId(), null, tableInfo);
                addSearchResults(results, null);
            }
        };

        searchBtn.addActionListener(doQuery);
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

        formView = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, dataMap, MultiView.NO_OPTIONS);
            add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,1dlu,p", "p,2dlu,p,2dlu,p"));
        CellConstraints cc         = new CellConstraints();

        //builder.addSeparator(getResourceString("AgentSearchTitle"), cc.xywh(1,1,3,1));
        builder.add(form.getUIComponent(), cc.xy(1,1));
        builder.add(searchBtn, cc.xy(3,1));

        panel.add(builder.getPanel(), BorderLayout.NORTH);
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2,false));


        scrollPane = new JScrollPane(contentPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setPreferredSize(new Dimension(300,200));

        // Bottom Button UI
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));

        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        updateUI();
    }

    /**
     * Updates the OK button (sets whether it is enabled by checking to see if there is a recordset
     */
    protected void updateUI()
    {
        okBtn.setEnabled(idList != null && idList.size() == 1);
        
    }

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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addSearchResults(edu.ku.brc.af.core.ExpressSearchResults, org.apache.lucene.search.Hits)
     */
    public void addSearchResults(final ExpressSearchResults results, final Hits hits)
    {
        idList = null;
        
        updateUI();

        if (etrb != null)
        {
            contentPanel.remove(etrb);
            etrb.cleanUp();
        }
        contentPanel.add(etrb = new ExpressTableResults(this, results, false));
        
        table = etrb.getTable();
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
                {
                    if (etrb != null && !e.getValueIsAdjusting())
                    {
                        idList = etrb.getListOfIds(false);

                    } else
                    {
                        idList = null;
                    }
                    updateUI();
                }});
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    okBtn.doClick(); //emulate button click
                }
            }
        });
        setUIEnabled(true);
        repaint();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ExpressSearchResultsPaneIFace#removeTable(edu.ku.brc.af.tasks.subpane.ExpressTableResultsBase)
     */
    public void removeTable(final ExpressTableResultsBase etrbTable)
    {
        etrbTable.cleanUp();
        
        contentPanel.remove(etrbTable);
        contentPanel.invalidate();
        contentPanel.doLayout();
        contentPanel.repaint();

        scrollPane.revalidate();
        scrollPane.doLayout();
        scrollPane.repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void addTable(final ExpressTableResultsBase etrBase)
    {
        // It has already been added so don't do anything
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ExpressSearchResultsPaneIFace#revalidateScroll()
     */
    public void revalidateScroll()
    {
        contentPanel.invalidate();
        scrollPane.revalidate();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
        isCancelled = e.getSource() == cancelBtn;
        setVisible(false);
    }
    
    //------------------------------------------------------------
    //-- ViewBasedDisplayIFace Interface
    //------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#getDialog()
     */
    public JDialog getDialog()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#isCancelled()
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.ViewBasedSearchDialogIFace#getSelectedObject()
     */
    public Object getSelectedObject()
    {
        if (!isCancelled && idList != null && idList.size() > 0)
        {
            Long id = idList.get(0);
            try
            {
                log.debug("getSelectedObject class["+className+"] idFieldName["+idFieldName+"] id["+id+"]");
                
                Class<?> classObj = Class.forName(className);
                
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List<?> list = session.getDataList(classObj, idFieldName, id);
                session.close();
                
                if (list.size() == 1)
                {
                    return list.get(0);
                }
                // else
                throw new RuntimeException("Why would more than one object be found in DBObjSearchDialog?");
            } catch (Exception ex)
            {
                log.error(ex);
            }

        }
        return null;
    }


}
