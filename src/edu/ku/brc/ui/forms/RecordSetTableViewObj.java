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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableRelationship;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.IconTray;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.RecordSetTableViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.validation.FormValidator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 19, 2006
 *
 */
public class RecordSetTableViewObj implements Viewable
{
    protected static final Logger log = Logger.getLogger(RecordSetTableViewObj.class);
    
    // Data Members
    protected DataProviderSessionIFace      session;
    protected MultiView                     mvParent;
    protected View                          view;
    protected AltView                       altView;
    protected RecordSetTableViewDef         viewDef;
    protected String                        cellName;
    protected Vector<AltView>               altViewsList;
    protected int                           viewOptions;
    
    protected FormDataObjIFace              parentDataObj;
    protected Set<Object>                   dataSet;
    protected String                        dataClassName;
    protected String                        dataSetFieldName;
    
    protected FormPane                      formPane;

    // UI stuff
    protected IconTray                      iconTray;
    protected JPanel mainComp;
    protected JPanel southPanel;
    protected JButton editButton;
    protected JButton newButton;
    protected JButton deleteButton;
    protected DropDownButtonStateful        switcherUI;
    protected JButton                       validationInfoBtn;
    
    protected boolean dataTypeError;
    
    protected FormValidator validator;
    
    protected BusinessRulesIFace            businessRules;

    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     */
    public RecordSetTableViewObj(final View view, final AltView altView, final MultiView mvParent, final int options)
    {
        this.view          = view;
        this.altView       = altView;
        this.mvParent      = mvParent;
        this.viewOptions   = options;
        this.viewDef       = (RecordSetTableViewDef)altView.getViewDef();
        this.dataTypeError = false;
        this.businessRules = view.getBusinessRule();
        
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
        String name = "Hello";
        
        formPane = new FormPane(DataProviderFactory.getInstance().createSession(),
                                name, 
                                null,  // task
                                viewDef.getViewSetName(), 
                                viewDef.getViewName(), 
                                "view", 
                                null, 
                                MultiView.VIEW_SWITCHER); // not new data object

            
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
        mainComp.add(formPane,BorderLayout.CENTER);
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
     * @see edu.ku.brc.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1;//tableViewDef.getId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return viewDef.getType();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getUIComponent()
     */
    public synchronized Component getUIComponent()
    {
        if (mainComp==null)
        {
            initMainComp();
        }
        return mainComp;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getCompById(java.lang.String)
     */
    public Component getCompById(final String id)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return validator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(Object dataObj)
    {
        if (dataObj instanceof Set)
        {
            dataSet = (Set)dataObj;
            
        } else
        {
            if (dataSet == null)
            {
                dataSet = new HashSet<Object>();
            } else
            {
                dataSet.clear();
            }
            
            if (dataObj instanceof List)
            {
                dataSet.addAll((List)dataObj);
                
            } else
            {
                // single object
                dataSet.add(dataObj);
            }
        }
        
        setDataIntoUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
//        if (!(parentDataObj instanceof FormDataObjIFace))
//        {
//            dataTypeError = true;
//            return;
//        }
        this.parentDataObj = (FormDataObjIFace)parentDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUI()
     */
    public synchronized void setDataIntoUI()
    {
        if (mainComp == null)
        {
            initMainComp();
        }

        iconTray.removeAllItems();
        
        for (Object o: dataSet)
        {
            if (!(o instanceof FormDataObjIFace))
            {
                log.error("Icon view data set contains non-FormDataObjIFace objects.  Item being ignored.");
                mainComp.removeAll();
                mainComp.add( new JLabel(getResourceString("Error")));
                dataTypeError = true;
                return;
            }
            
            iconTray.addItem((FormDataObjIFace)o);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        if (dataTypeError)
        {
            return;
        }
        
        dataSet.clear();
        Set<FormDataObjIFace> iconTrayItems = iconTray.getItems();
        for (FormDataObjIFace fdo: iconTrayItems)
        {
            dataSet.add(fdo);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        // do nothing
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(boolean show)
    {
        if (switcherUI != null)
        {
            switcherUI.setCurrentIndex(altViewsList.indexOf(altView));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getView()
     */
    public View getView()
    {
        return view;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getViewDef()
     */
    public FormViewDef getViewDef()
    {
        return (FormViewDef)altView.getViewDef();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getAltView()
     */
    public AltView getAltView()
    {
        return altView;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = session;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setHasNewData(boolean)
     */
    public void setHasNewData(final boolean isNewForm)
    {
        // this gives you the opportunity to adjust your UI
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
        this.dataSetFieldName = cellName;
        
        if (parentDataObj == null)
        {
            this.dataClassName = viewDef.getClassName();
        }
        else
        {
            TableInfo parentTI = DBTableIdMgr.lookupByClassName(parentDataObj.getClass().getName());
            TableRelationship rel = parentTI.getRelationshipByName(cellName);
            this.dataClassName = rel.getClassName();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(JButton saveBtnArg)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        mvParent      = null;
        mainComp      = null;
    }
}
