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
package edu.ku.brc.af.ui.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;

/**
 * This class enables the contents of a recordset to be shown as a table. 
 * Meaning you pass in a recordset and it finds the default form and then loads it as a 
 * table showing the resiltion of the recordset items.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 21, 2006
 *
 */
public class RecordSetTableViewObj extends TableViewObj
{
    protected static final Logger log = Logger.getLogger(RecordSetTableViewObj.class);
    
    // UI stuff
    protected JPanel  southPanel;
    //protected JButton editButton;
    //protected JButton newButton;
    //protected JButton deleteButton;
    
    protected boolean dataTypeError;
    protected boolean shouldCloseSession = false;
    
    protected FormValidator            validator;
    protected DataProviderSessionIFace tempSession = null;


    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     * @param formValidator
     * @param options
     * @param cellName the name of the cell when it is a subview
     * @param dataClass the class of the data that is put into the form
     * @param bgColor
     */
    public RecordSetTableViewObj(final ViewIFace     view,
                                 final AltViewIFace  altView,
                                 final MultiView     mvParent,
                                 final FormValidator formValidator,
                                 final int           options,
                                 final String        cellName,
                                 final Class<?>      dataClass,
                                 final Color         bgColor)
    {
        super(view, altView, mvParent, formValidator, options, cellName, dataClass, bgColor);
        
        // we need a form validator that always says it's valid
        validator = new FormValidator(null)
        {
            @Override
            public boolean isFormValid()
            {
                return true;
            }
        };
    }
    
    /**
     * Build the table now that we have all the information we need for the columns.
     */
    protected void buildTable()
    {
        super.buildTable();
        
        //Dimension size = table.getPreferredSize();
        //size.height = 200;
        //table.setPreferredSize(size);
    }
    
    protected void initMainComp()
    {

        mainComp = new RestrictablePanel();
        mainComp.setLayout(new BorderLayout());
        if (mvParent == null)
        {
            mainComp.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        /*
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:1px:g,p,1px,p,1px,p,1px,p,1px,p", "p"));
        CellConstraints cc  = new CellConstraints();
        
        builder.add(editButton, cc.xy(2,1));
        builder.add(newButton, cc.xy(4,1));
        builder.add(deleteButton, cc.xy(6,1));
        builder.add(validationInfoBtn, cc.xy(8,1));
        builder.add(switcherUI, cc.xy(10,1));
        southPanel = builder.getPanel();
*/
        //mainComp.add(formPane,BorderLayout.CENTER);
        mainComp.add(southPanel,BorderLayout.SOUTH);
    }

 
    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getName()
     */
    public String getName()
    {
        return "RecordSetTable View";
    }

    /**
     * @param recordSet
     */
    private void processRecordSet(final RecordSetIFace recordSet)
    {
        isLoading = true;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                table.tableChanged(new TableModelEvent(model));
                table.repaint();
            }
        });
        
        DBTableIdMgr.getInstance().getInClause(recordSet);
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
        
        DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
        
        if (tempSession == null)
        {
            tempSession = DataProviderFactory.getInstance().createSession();
        }
        
        String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
        if (StringUtils.isNotBlank(sqlStr))
        {
            dataObjList.addAll(tempSession.getDataList(sqlStr));
            
            for (Object obj : dataObjList)
            {
                FormDataObjIFace fdi = (FormDataObjIFace)obj;
                fdi.forceLoad();
            }
        }
        
        if (tempSession != null)
        {
            tempSession.close();
            tempSession = null;
        }
        
        isLoading = false;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                table.repaint();
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setDataObj(Object dataObj)
    {
        if (dataObjList == null)
        {
            dataObjList = new Vector<Object>();
        } else
        {
            dataObjList.clear(); 
        }
        
        
        if (dataObj instanceof RecordSetIFace)
        {
            this.dataObj = dataObj;
            
            RecordSetIFace recordSet = (RecordSetIFace)dataObj;
            processRecordSet(recordSet);
  
        } else if (dataObj instanceof Set<?>)
        {
            this.dataObj = null;
            
            Set<?> dataSet = (Set<?>)dataObj;
            if (dataSet.size() > 0)
            {
                Object firstDataObj = dataSet.iterator().next();
                if (firstDataObj instanceof RecordSetIFace)
                {
                    this.dataObj = firstDataObj;
                    processRecordSet((RecordSetIFace)firstDataObj);
                }
            }
            
        } else
        {
            this.dataObj = null;
        }
        
        if (table != null)
        {
            table.tableChanged(new TableModelEvent(model));
        }
        
        if (session == null)
        {
            session = tempSession;
        }
        
        setDataIntoUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = null;//session;
        
        /*if (dataObj instanceof RecordSetIFace)
        {
            RecordSetIFace recordSet = (RecordSetIFace)dataObj;
            
            DBTableIdMgr.getInstance().getInClause(recordSet);
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
            
            DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
            
            if (tempSession == null)
            {
                tempSession = DataProviderFactory.getInstance().createSession();
            }
            
            String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
            if (StringUtils.isNotBlank(sqlStr))
            {
                dataObjList.clear();
                dataObjList.addAll(tempSession.getDataList(sqlStr));
                if (table != null)
                {
                    table.tableChanged(new TableModelEvent(model));
                }
            }
        }*/
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataObj;
    }
}
