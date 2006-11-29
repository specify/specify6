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
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.FormValidator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 19, 2006
 *
 */
public class RecordSetTableViewObj extends TableViewObj
{
    protected static final Logger log = Logger.getLogger(RecordSetTableViewObj.class);
    
    // UI stuff
    protected JPanel southPanel;
    protected JButton editButton;
    protected JButton newButton;
    protected JButton deleteButton;
    
    protected boolean dataTypeError;
    
    protected FormValidator validator;


    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     */
    public RecordSetTableViewObj(final View          view,
                                 final AltView       altView,
                                 final MultiView     mvParent,
                                 final FormValidator formValidator,
                                 final int           options)
    {
        super(view, altView, mvParent, formValidator, options);
        
        // we need a form validator that always says it's valid
        validator = new FormValidator(){
            @Override
            public boolean isFormValid()
            {
                return true;
            }
        };
        MultiView root = mvParent;
        while (root.getMultiViewParent() != null)
        {
            root = root.getMultiViewParent();
        }
        validator.setName("RecordSetTableViewObj validator");
        root.addFormValidator(validator);
    }
    
    protected void initMainComp()
    {
        /*
        editButton = createButton("EditForm", getResourceString("EditRecord"));
        newButton = createButton("CreateObj", getResourceString("NewRecord"));
        deleteButton = createButton("CreateObj", getResourceString("DeleteRecord"));

        altViewsList = new Vector<AltView>();
        switcherUI   = FormViewObj.createSwitcher(mvParent, view, altView, altViewsList);
        
        validationInfoBtn = new JButton(IconManager.getImage("ValidationValid"));
        validationInfoBtn.setToolTipText(getResourceString("ShowValidationInfoTT"));
        validationInfoBtn.setMargin(new Insets(1,1,1,1));
        validationInfoBtn.setBorder(BorderFactory.createEmptyBorder());
        validationInfoBtn.setFocusable(false);
        validationInfoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                log.error("validation button clicked: not yet implemented");
            }
        });
        
        iconTray = new IconTray(IconTray.SINGLE_ROW);
        //iconTray = new IconTray(IconTray.MULTIPLE_ROWS);
        
        addActionListenerToEditButton();
        
        if (altView.getMode() == CreationMode.View)
        {
            newButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else
        {
            addActionListenerToNewButton();
            addActionListenerToDeleteButton();
        }
*/
        mainComp = new JPanel();
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
            
            DBTableIdMgr.getInClause(recordSet);
            DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
            
            DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
            
            //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            String sqlStr = DBTableIdMgr.getQueryForTable(recordSet);
            if (StringUtils.isNotBlank(sqlStr))
            {
                dataObjList.addAll(session.getDataList(sqlStr));
                if (table != null)
                {
                    table.tableChanged(new TableModelEvent(model));
                }
            }
  
        } else
        {
            this.dataObj = null;
            // throw exception
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
        this.session = session;
        
        if (dataObj instanceof RecordSetIFace)
        {
            RecordSetIFace recordSet = (RecordSetIFace)dataObj;
            
            DBTableIdMgr.getInClause(recordSet);
            DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
            
            DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
            
            //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            String sqlStr = DBTableIdMgr.getQueryForTable(recordSet);
            if (StringUtils.isNotBlank(sqlStr))
            {
                dataObjList.addAll(session.getDataList(sqlStr));
                if (table != null)
                {
                    table.tableChanged(new TableModelEvent(model));
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataObj;
    }

}
