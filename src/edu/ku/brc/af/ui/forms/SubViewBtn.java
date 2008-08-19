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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 6, 2007
 *
 */
public class SubViewBtn extends JPanel implements GetSetValueIFace
{
    public enum DATA_TYPE {IS_SET, IS_SINGLE, IS_THIS, IS_SINGLESET_ITEM}
    
    protected static final Logger log = Logger.getLogger(SubViewBtn.class);
    
    protected FormCellSubViewIFace  subviewDef;
    protected ViewIFace             view;
    protected DATA_TYPE             dataType;
    protected ViewBasedDisplayIFace frame      = null;
    protected MultiView             multiView  = null;
    protected MultiView             mvParent   = null;
    protected String                frameTitle = "";
    protected String                cellName;
    protected int                   options;
    protected String                baseLabel;
    protected Class<?>              classToCreate = null;
    protected String                helpContext   = null;
    protected boolean               isEditing;
    
    protected JButton               subViewBtn;
    protected JLabel                label;
    protected ImageIcon             icon;
    
    protected Object                dataObj;
    protected Object                newDataObj;
    protected Class<?>              classObj;
    protected FormDataObjIFace      parentObj;
    
    // Security
    private PermissionSettings      perm = null;

    /**
     * @param subviewDef
     * @param isCollection
     * @param props
     */
    public SubViewBtn(final MultiView            mvParent,
                      final FormCellSubViewIFace subviewDef,
                      final ViewIFace            view,
                      final DATA_TYPE            dataType,
                      final int                  options,
                      final Properties           props,
                      final Class<?>             classToCreate,
                      final AltViewIFace.CreationMode mode)
    {
        this.mvParent      = mvParent;
        this.subviewDef    = subviewDef;
        this.dataType      = dataType;
        this.view          = view;
        this.options       = options;
        this.classToCreate = classToCreate;
        
        isEditing = mode == AltViewIFace.CreationMode.EDIT;
        
        // 02/12/08 - rods - Removing the "IS_NEW_OBJECT" of the parent object because it doesn't
        // matter to the popup form it creates. The form takes care of everything.
        this.options &= ~MultiView.IS_NEW_OBJECT;
        
        //log.debug("Editing "+MultiView.isOptionOn(options, MultiView.IS_EDITTING));
        //log.debug("IsNew "+MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT));

        cellName     = subviewDef.getName();
        frameTitle   = props.getProperty("title");
        String  align   =  props.getProperty("align", "left");
        String iconName =  props.getProperty("icon", null);
        
        icon = null;
        baseLabel = props.getProperty("label");
        if (baseLabel == null)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(classToCreate != null ? classToCreate.getName() : view.getClassName());
            if (tableInfo != null)
            {
                baseLabel = tableInfo.getTitle();
                if (StringUtils.isNotEmpty(iconName))
                {
                    icon = IconManager.getIcon(iconName, IconManager.IconSize.NonStd);
                } else
                {
                    icon = IconManager.getIcon(tableInfo.getName(), IconManager.IconSize.Std24);   
                }
                if (frameTitle == null)
                {
                    frameTitle = baseLabel;
                }
            }
        }
        
        if (frameTitle == null)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(classToCreate != null ? classToCreate.getName() : view.getClassName());
            if (tableInfo != null)
            {
                frameTitle = tableInfo.getTitle();
            }
        }
        
        int x = 2;
        String colDef;
        if (align.equals("center"))
        {
            colDef = "f:p:g,p,2px,p,f:p:g";
            
        } else if (align.equals("right"))
        {
            colDef = "f:p:g, p,2px,p";
            
        } else // defaults to left
        {
            colDef = "p,2px,p,f:p:g";
            x = 1;  
        }
        
        subViewBtn = icon != null ? createButton(icon) : createButton(baseLabel);
        subViewBtn.addActionListener(new ActionListener() {
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                showForm();
            }
        });
        subViewBtn.setEnabled(false);

        
        label  = createLabel("  ");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout(colDef, "p"), this);
        CellConstraints cc = new CellConstraints();
        pb.add(subViewBtn, cc.xy(x,1));
        pb.add(label, cc.xy(x+2,1));
               
        try
        {
            classObj = Class.forName(view.getClassName());

        } catch (ClassNotFoundException ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }
    }
    
    /**
     * Getting the permissions for the data object.
     * @param dObj the data object
     */
    private void ensurePermissions(final Object dObj)
    {
        if (dObj != null)
        {
            if (UIHelper.isSecurityOn())
            {
                if (perm == null)
                {
                    Class<?> cls;
                    if (dObj instanceof Set<?>)
                    {
                        Set<?> set = (Set<?>)dObj;
                        if (set.size() == 0)
                        {
                            return;
                        }
                        cls = set.iterator().next().getClass();
                    } else
                    {
                        cls = dObj.getClass();
                    }
                    DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
                    if (tblInfo != null)
                    {
                        perm = tblInfo.getPermissions();
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
     
        ensurePermissions(dataObj);
        
        boolean isSecurityEnableOK = perm == null || (perm != null && ((isEditing && perm.isViewOnly()) || (!isEditing && !perm.canView())));
        subViewBtn.setEnabled(enabled && isSecurityEnableOK);
        label.setEnabled(enabled && isSecurityEnableOK);
        
        if (!isSecurityEnableOK)
        {
            subViewBtn.setToolTipText("SubForm is Restricted."); // I18N
        }
    }
    
    /**
     * @param pObj
     */
    public void setParentDataObj(final Object pObj)
    {
        this.parentObj = (FormDataObjIFace)pObj;
    }
    
    /**
     * @param helpContext
     */
    public void setHelpContext(final String helpContext)
    {
        this.helpContext = helpContext;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void showForm()
    {
        //boolean isParentNew = parentObj instanceof FormDataObjIFace ? ((FormDataObjIFace)parentObj).getId() == null : false;
        boolean isNewObject = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean isEdit      = MultiView.isOptionOn(options, MultiView.IS_EDITTING) || isNewObject;
        
        String closeBtnTitle = isEdit ? getResourceString("DONE") : getResourceString("CLOSE");
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                subviewDef.getViewSetName(),
                subviewDef.getViewName(),
                null,                      // What is this argument???
                frameTitle,
                closeBtnTitle,
                view.getClassName(),
                cellName,  // idFieldName
                isEdit | isNewObject,
                false,
                cellName,
                mvParent,
                options | MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE,
                CustomDialog.CANCEL_BTN | (StringUtils.isNotEmpty(helpContext) ? CustomDialog.HELP_BTN : 0))
        {

            /* (non-Javadoc)
             * @see edu.ku.brc.ui.db.ViewBasedDisplayDialog#cancelButtonPressed()
             */
            @Override
            protected void cancelButtonPressed()
            {
                FormValidator validator = multiView.getCurrentValidator();
                if (validator != null && validator.getState() != UIValidatable.ErrorType.Valid)
                {
                    FormViewObj fvo = multiView.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        boolean  isNew   = fvo.isNewlyCreatedDataObj();
                        String   msgKey  = isNew ? "MV_INCOMPLETE_DATA_NEW" : "MV_INCOMPLETE_DATA";
                        String   btnKey  = isNew ? "MV_REMOVE_ITEM" : "MV_DISCARD_ITEM";
                        Object[] optionLabels = { getResourceString(btnKey), getResourceString("CANCEL") };
                        int rv = JOptionPane.showOptionDialog(null, 
                                    getResourceString(msgKey),
                                    getResourceString("MV_INCOMPLETE_DATA_TITLE"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, optionLabels, optionLabels[0]);                        
                        if (rv == JOptionPane.NO_OPTION)
                        {
                            return;
                        }
                    }
                }
                super.cancelButtonPressed();
            }
            
        };
        
        dlg.setCancelLabel(closeBtnTitle);
        frame     = dlg;
        multiView = frame.getMultiView();
        
        
        if (parentObj != null && parentObj.getId() != null)
        {
            DataProviderSessionIFace sessionLocal = null;
            try
            {
                DataObjectGettable getter = DataObjectGettableFactory.get(parentObj.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
                sessionLocal = DataProviderFactory.getInstance().createSession();
                // rods - 07/22/08 - Apparently Merge just doesn't work the way it seems it should
                // so instead we will just go get the parent again.
                parentObj = (FormDataObjIFace)sessionLocal.get(parentObj.getDataClass(), parentObj.getId());
                
                Object[] objs = UIHelper.getFieldValues(subviewDef, parentObj, getter);
                dataObj = objs[0];
                multiView.setParentDataObj(parentObj);
                multiView.setData(dataObj);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally
            {
                if (sessionLocal != null)
                {
                    sessionLocal.close();
                }
            }
        } else
        {
            multiView.setParentDataObj(parentObj);
            multiView.setData(dataObj);
        }
        
        multiView.setClassToCreate(classToCreate);
        
        FormValidator formVal = multiView.getCurrentViewAsFormViewObj().getValidator();
        if (formVal != null)
        {
            formVal.setEnabled(true);
            multiView.addCurrentValidator();
        }
        
        dlg.createUI();
        frame.getCancelBtn().setEnabled(true);
        
        frame.showDisplay(true);
        
        if (formVal != null)
        {
            multiView.removeCurrentValidator();
        }
        
        if (multiView != null)
        {
            if (frame.isEditMode())
            {
                FormViewObj fvo = frame.getMultiView().getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
                    //boolean removeCurrItem = fvo.getValidator().getState() != UIValidatable.ErrorType.Valid;

                    switch (dataType)
                    {
                        case IS_SET :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_SINGLE :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_THIS :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_SINGLESET_ITEM :
                            fvo.getDataFromUI();
                            break;
                    }
                } 
                updateBtnText();
                //if (fvo.getValidator() != null)
                //{
                    //fvo.getValidator().setFormValidationState(UIValidatable.ErrorType.Valid);
                   // fvo.getValidator().validateForm();
                    //multiView.validate();
                    //fvo.getValidator().wasValidated(null);
                //}
            }
        } else
        {
            
        }
        frame.dispose();
        frame = null;
    }
    
    /**
     * 
     */
    protected void updateBtnText()
    {
        if (dataObj instanceof Set<?> && icon != null)
        {
            ensurePermissions(dataObj);
            
            String lblStr = null;
            if (UIHelper.isSecurityOn() && (perm != null && perm.hasNoPerm()) || perm == null)
            {
                lblStr = "   ";
            }

            if (!lblStr.equals("   "))
            {
                int size = ((Set<?>)dataObj).size();
                lblStr = " " + String.format("%s", size)+ " ";
            }
            
            label.setText(lblStr);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    //@Override
    public Object getValue()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    //@Override
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        
        // Retrieve lazy object while in the context of a session (just like a subform would do
        if (dataObj != null)
        {
            if (dataObj instanceof FormDataObjIFace)
            {
                ((FormDataObjIFace)dataObj).getIdentityTitle();
                
            } else if (dataObj instanceof Set<?>)
            {
                updateBtnText(); // note: that by calling this, 'size' gets called and that loads the Set (this must be done).
                
                /*
                for (Object data : (Set<?>)dataObj)
                {
                    if (data instanceof FormDataObjIFace)
                    {
                        ((FormDataObjIFace)data).getIdentityTitle();
                    }
                }*/
            }
        }
    }

}
