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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.validation.FormValidator;

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
    
    protected JButton               subViewBtn;
    protected JLabel                label;
    protected ImageIcon             icon;
    
    protected Object                dataObj;
    protected Object                newDataObj;
    protected Class<?>              classObj;
    protected Object                parentObj;
    
    
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
                      final Class<?>             classToCreate)
    {
        this.mvParent      = mvParent;
        this.subviewDef    = subviewDef;
        this.dataType      = dataType;
        this.view          = view;
        this.options       = options;
        this.classToCreate = classToCreate;
        
        // 02/12/08 - rods - Removing the "IS_NEW_OBJECT" of the parent object because it doesn't
        // matter to the popup form it creates. he form takes care o everything.
        this.options &= ~MultiView.IS_NEW_OBJECT;
        
        //log.debug("Editing "+MultiView.isOptionOn(options, MultiView.IS_EDITTING));
        //log.debug("IsNew "+MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT));

        cellName     = subviewDef.getName();
        frameTitle   = props.getProperty("title");
        String align =  props.getProperty("align", "left");
        String iconName =  props.getProperty("icon", null);
        
        icon = null;
        baseLabel = props.getProperty("label");
        if (baseLabel == null)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(classToCreate != null ? classToCreate.getName() : view.getClassName());
            if (tableInfo != null)
            {
                baseLabel = tableInfo.getTitle();
                icon = IconManager.getIcon(StringUtils.isNotEmpty(iconName) ? iconName : tableInfo.getName(), IconManager.IconSize.Std24);
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
        
        if (false)
        {
            subViewBtn = UIHelper.createIconBtn("Attach", IconManager.IconSize.Std16, "", false, new ActionListener() {
                //@Override
                public void actionPerformed(ActionEvent e)
                {
                    showForm();
                }
            });
        } else
        {
            subViewBtn = new JButton(IconManager.getIcon("Attach", IconManager.IconSize.Std16));
            subViewBtn.addActionListener(new ActionListener() {
                //@Override
                public void actionPerformed(ActionEvent e)
                {
                    showForm();
                }
            });
        }
        subViewBtn.setEnabled(true);
        
        label              = icon != null ? new JLabel(icon) : new JLabel(baseLabel);
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
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        subViewBtn.setEnabled(enabled);
        label.setEnabled(enabled);
    }
    
    /**
     * @param pObj
     */
    public void setParentDataObj(final Object pObj)
    {
        this.parentObj = pObj;
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
    protected void showForm()
    {
        //boolean isParentNew = parentObj instanceof FormDataObjIFace ? ((FormDataObjIFace)parentObj).getId() == null : false;
        boolean isNewObject = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean isEditing   = MultiView.isOptionOn(options, MultiView.IS_EDITTING) || isNewObject;
        
        String closeBtnTitle = isEditing ? getResourceString("Done") : getResourceString("Close");
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                subviewDef.getViewSetName(),
                subviewDef.getViewName(),
                null,                      // What is this argument???
                frameTitle,
                closeBtnTitle,
                view.getClassName(),
                cellName,  // idFieldName
                isEditing | isNewObject,
                false,
                cellName,
                mvParent,
                options | MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE,
                CustomDialog.OK_BTN | (StringUtils.isNotEmpty(helpContext) ? CustomDialog.HELP_BTN : 0));
        
        /*if (isNewObject)
        {
            FormDataObjIFace formDataObj = FormHelper.createAndNewDataObj(view.getClassName());
            formDataObj.initialize();
            newDataObj = formDataObj;
        }*/
        
        frame = dlg;
        multiView = frame.getMultiView();
        multiView.setParentDataObj(mvParent.getData());
        multiView.setData(dataObj);
        multiView.setClassToCreate(classToCreate);
        
        FormValidator formVal = multiView.getCurrentViewAsFormViewObj().getValidator();
        if (formVal != null)
        {
            formVal.setEnabled(true);
            multiView.addCurrentValidator();
        }
        
        dlg.createUI();
        frame.getOkBtn().setEnabled(true);
        
        frame.showDisplay(true);
        
        if (formVal != null)
        {
            mvParent.removeCurrentValidator();
        }
        
        if (multiView != null && frame.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            if (frame.isEditMode())
            {
                FormViewObj fvo = frame.getMultiView().getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
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
        if (dataObj instanceof Set<?>)
        {
            int    size   = ((Set<?>)dataObj).size();
            if (icon != null)
            {
                label.setText(String.format("(%s) ...", size));
            } else
            {
                String format = UIRegistry.getResourceString("SUBVIEW_BTN_TITLE_FORMAT");
                if (StringUtils.isEmpty(format))
                {
                    format = "%s (%s) ...";
                }
                label.setText(String.format(format, baseLabel, size));
            }
            
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
