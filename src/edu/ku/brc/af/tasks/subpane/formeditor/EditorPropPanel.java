/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.ValCheckBox;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValTextArea;
import edu.ku.brc.ui.forms.validation.ValTextField;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 22, 2007
 *
 */
public class EditorPropPanel extends JPanel
{
    private static final Logger  log                = Logger.getLogger(EditorPropPanel.class);
    
    protected Hashtable<String, ViewBasedDisplayPanel> propPanelHash  = new Hashtable<String, ViewBasedDisplayPanel>();

    protected ViewBasedDisplayPanel viewPanel    = null;
    protected FormViewObj           formViewObj  = null;
    protected DBTableInfo           tableInfo;
    
    protected FormCell              currentFC    = null;
    protected Object                data;
    
    protected Hashtable<String, Control>         controlHash;
    protected Hashtable<String, SubControl>      subcontrolHash;
    protected Vector<FormCellField>              fieldsNotUsedByLabels;
    protected FormViewDef                        formViewDef;
    
    protected MultiView                          multiView;

    protected JButton                            saveBtn = null;
    protected JButton                            valBtn  = null;
    
    protected PropertyChangeListener             pcl     = null;
    
    protected Hashtable<String, String>          pickList = new Hashtable<String, String>();
    
    
    /**
     * @param controlHash
     * @param subcontrolHash
     * @param fieldsNotUsedByLabels
     * @param addSaveBtn
     * @param pcl
     */
    public EditorPropPanel(final Hashtable<String, Control>    controlHash, 
                           final Hashtable<String, SubControl> subcontrolHash,
                           final Vector<FormCellField>         fieldsNotUsedByLabels,
                           final boolean                       addSaveBtn,
                           final PropertyChangeListener        pcl)
    {
        setLayout(new BorderLayout());
        
        this.controlHash    = controlHash;
        this.subcontrolHash = subcontrolHash;
        this.fieldsNotUsedByLabels = fieldsNotUsedByLabels;
        
        if (addSaveBtn)
        {
            saveBtn = UIHelper.createButton("Accept");
            saveBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (currentFC instanceof FormCellField)
                    {
                        getDataFromUI((FormCellField)currentFC);
                    } else
                    {
                        getDataFromUI(currentFC);
                    }
                    if (pcl != null)
                    {
                        pcl.propertyChange(new PropertyChangeEvent(EditorPropPanel.this, "accept", data, data)); //$NON-NLS-1$
                    }
                }
            });
        }
    }

    /**
     * @param fieldsNotUsedByLabels
     */
    public void setFieldsNotUsedByLabels(Vector<FormCellField> fieldsNotUsedByLabels)
    {
        this.fieldsNotUsedByLabels = fieldsNotUsedByLabels;
    }
    
    /**
     * @param id
     * @return
     */
    protected String getNameForId(final String id)
    {
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell.getIdent().equals(id))
                {
                    return cell.getName();
                }
            }
        }
        return null;
    }
    
    
    /**
     * @param data
     */
    public void setData(final Object data)
    {
        currentFC   = null;
        this.data        = data;
        
        if (multiView != null)
        {
            multiView.setData(data);
            
            if (data != null)
            {
                if (data instanceof ViewDefIFace)
                {
                    JTextField typeLabel = (JTextField)formViewObj.getControlByName("type"); //$NON-NLS-1$
                    typeLabel.setText(UIHelper.makeNamePretty(data.getClass().getSimpleName()));
                    
                } else if (data instanceof AltView)
                {
                    setDataIntoUI((AltView)data);
                }
            }
        }
    }
    
    /**
     * @param altView
     */
    protected void setDataIntoUI(final AltView altView)
    {
        ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("defaultMode"); //$NON-NLS-1$
        DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
        model.removeAllElements();
        
        int cnt = 0;
        int inx = 0;
        for (AltViewIFace.CreationMode mode : AltViewIFace.CreationMode.values())
        {
            model.addElement(mode);
            if (mode == altView.getMode())
            {
                inx = cnt;
            }
            cnt++;
        }
        cbx.getComboBox().setSelectedIndex(inx);
    }
    
    /**
     * @return
     */
    public Object getData()
    {
        if (multiView != null)
        {
            multiView.getDataFromUI();
            
            if (data != null)
            {
                if (data instanceof AltView)
                {
                    getDataFromUI((AltView)data);
                }
            }
        }
        return data;
    }
    
    /**
     * @param data
     */
    protected void getDataFromUI(final AltView altView)
    {
        ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("defaultMode"); //$NON-NLS-1$
        altView.setMode((AltViewIFace.CreationMode)cbx.getComboBox().getSelectedItem());
    }
    
    /**
     * @param name
     * @return
     */
    protected String getIdForName(final String name)
    {
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (StringUtils.isNotEmpty(cell.getName()) && cell.getName().equals(name))
                {
                    return cell.getIdent();
                }
            }
        }
        return null;
    }
    
    /**
     * 
     */
    protected void addSaveBtn(final MultiView mv)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p" + (saveBtn != null ? ",5px,p" : ""), "p")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        int x = 2;
        
        valBtn = FormViewObj.createValidationIndicator(mv.getCurrentView().getUIComponent(), mv.getCurrentView().getValidator());
        pb.add(valBtn, cc.xy(x, 1));
        x += 2;
       
        if (saveBtn != null)
        {
            pb.add(saveBtn, cc.xy(x,1));
            mv.getCurrentView().getValidator().addEnableItem(saveBtn, FormValidator.EnableType.ValidAndChangedItems);
        }
        
         add(pb.getPanel(), BorderLayout.SOUTH);
    }


    /**
     * @param viewName
     * @param className
     */
    public void loadView(final String viewName, 
                         final String className)
    {
        viewPanel = propPanelHash.get(viewName);
        if (viewPanel == null)
        {
            ViewFactory.setDoFixLabels(false);
            viewPanel = new ViewBasedDisplayPanel(null, "Editor", viewName, null, null, null, true, 0);
            propPanelHash.put(viewName, viewPanel);
            ViewFactory.setDoFixLabels(true);
        }
        
        removeAll();
        
        multiView = viewPanel.getMultiView();
        
        multiView.getCurrentView().getValidator().setEnabled(true);
        multiView.getCurrentView().getValidator().addEnableItem(saveBtn, FormValidator.EnableType.ValidAndChangedItems);
        
        if (viewPanel != null && multiView != null)
        {
            add(viewPanel, BorderLayout.CENTER);
            
            addSaveBtn(multiView);
            
            validate();
            
            formViewObj = (FormViewObj)multiView.getCurrentView();
            formViewObj.setFormEnabled(true);
            tableInfo   = className != null ? DBTableIdMgr.getInstance().getByClassName(className) : null;
            
        } else
        {
            log.error("Couldn't load panel for ["+viewName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        validate();
        repaint();
    }
    
    /**
     * @param fc
     * @param rows
     * @param rowInx
     * @param cols
     * @param colInx
     */
    public void setDataIntoUI(final FormViewDef fvd,
                              final FormCell    fc, 
                              final int         rows,
                              final int         rowDefs,
                              final int         rowInx,
                              final int         cols,
                              final int         colDefs,
                              final int         colInx)
    {
        if (formViewObj == null)
        {
            return;
        }
        
        formViewDef = fvd;
        
        currentFC = fc;
        data      = fc;
        
        setDataIntoBase(fc, rows, rowDefs, rowInx, cols, colDefs, colInx, false);
        
        JTextField typeLabel = (JTextField)formViewObj.getControlByName("type"); //$NON-NLS-1$
        typeLabel.setText(fc.getType().toString());
        
        if (fc instanceof FormCellLabel)
        {
            FormCellLabel fcl = (FormCellLabel)fc;
            
            getIdCombobox(fcl.getLabelFor());
            
            processImageNameCBX(fcl.getIconName());
            
            ValCheckBox chkbx = (ValCheckBox)formViewObj.getControlByName("isRecordObj"); //$NON-NLS-1$
            chkbx.setSelected(fcl.isRecordObj());
            
            ((ValTextField)formViewObj.getControlByName("label")).setText(((FormCellLabel)fc).getLabel()); //$NON-NLS-1$
        }
        
        multiView.validateAll();

    }
    
    /**
     * @param imageName
     * @return
     */
    protected ValComboBox processImageNameCBX(final String imageName)
    {
        List<Pair<String, ImageIcon>> icons = IconManager.getListByType("datamodel", IconManager.IconSize.Std16); //$NON-NLS-1$
        
        ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("imageNameCBX"); //$NON-NLS-1$
        DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
        model.addElement(getResourceString("EditorPropPanel.NONE")); //$NON-NLS-1$
        int inx = 0;
        int cnt = 1;
        for (Pair<String, ImageIcon> iconPair : icons)
        {
            model.addElement(iconPair.first);
            if (StringUtils.isNotEmpty(imageName) && iconPair.first.equals(imageName))
            {
                inx = cnt;
            }
            cnt++;
        }
        cbx.getComboBox().setSelectedIndex(inx);
        return cbx;
    }
    
    /**
     * @param labelFor
     * @return
     */
    protected ValComboBox getIdCombobox(final String labelFor)
    {
        ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("labelForCBX"); //$NON-NLS-1$
    
        DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
        model.addElement(getResourceString("EditorPropPanel.NONE")); //$NON-NLS-1$
        int inx = 0;
        if (StringUtils.isNotEmpty(labelFor))
        {
            model.addElement(getNameForId(labelFor));
            inx = 1;
        }
        
        for (FormCellField fcf : fieldsNotUsedByLabels)
        {
            model.addElement(fcf.getName());
        }
        cbx.getComboBox().setSelectedIndex(inx);
        return cbx;
    }
    
    /**
     * @param fc
     * @param rows
     * @param rowInx
     * @param cols
     * @param colInx
     * @param enableNaming
     */
    protected void setDataIntoBase(final FormCell fc, 
                                   final int      rows,
                                   final int      rowDefs,
                                   final int      rowInx,
                                   final int      cols,
                                   final int      colDefs,
                                   final int      colInx,
                                   final boolean  enableNaming)
    {
        if (formViewObj == null)
        {
            return;
        }
        
        int inx;
        int cnt;
        
        String      fName   = fc.getName();
        ValComboBox nameCBX = (ValComboBox)formViewObj.getControlByName("namecbx"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(fName) || enableNaming)
        {
            DefaultComboBoxModel model = (DefaultComboBoxModel)nameCBX.getModel();
            inx = -1;
            cnt = 0;
            for (DBFieldInfo fi : tableInfo.getFields())
            {
                model.addElement(fi.getTitle());
                //System.out.println("["+fi.getName()+"]["+fName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (fi.getTitle().equals(fName) || fi.getName().equals(fName))
                {
                    inx = cnt;
                }
                cnt++;
            }
            for (DBRelationshipInfo ri : tableInfo.getRelationships())
            {
                if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                {
                    model.addElement(ri.getTitle());
                    if (inx == -1)
                    {
                        //System.out.println("*["+ri.getTitle()+"]["+fName+"]");
                        if (ri.getTitle().equals(fName) || ri.getName().equals(fName))
                        {
                            inx = cnt;
                        }
                    }
                    cnt++;
                }
            }
            nameCBX.getComboBox().setSelectedIndex(inx);
            nameCBX.setEnabled(true);
            
        } else
        {
            nameCBX.setEnabled(false);
        }
        
        if (true)
        {
            int rowDefsLeft = rowDefs - rows;
            
            ValComboBox rowSpanCBX = (ValComboBox)formViewObj.getControlByName("rowspancbx"); //$NON-NLS-1$
            DefaultComboBoxModel model = (DefaultComboBoxModel)rowSpanCBX.getModel();
            if (rowDefsLeft == 0)
            {
                model.addElement("1"); //$NON-NLS-1$
                inx = 0;
                rowSpanCBX.setEnabled(false);
                
            } else
            {
                inx = 0;
                cnt = 0;
                for (int i=1;i<=rowDefsLeft;i++)
                {
                    model.addElement(Integer.toString(i));
                    if (i == fc.getRowspan())
                    {
                        inx = cnt;
                    } 
                }
                rowSpanCBX.setEnabled(rowDefsLeft > 1);
                rowSpanCBX.getComboBox().setSelectedIndex(inx);
            }
    
            ValComboBox colSpanCBX = (ValComboBox)formViewObj.getControlByName("colspancbx"); //$NON-NLS-1$
            model = (DefaultComboBoxModel)colSpanCBX.getModel();
            int colDefsLeft = colDefs - cols;
            if (colDefsLeft == 0)
            {
                model.addElement("1"); //$NON-NLS-1$
                inx = 1;
                colSpanCBX.setEnabled(false);
                
            } else 
            {
                inx = 0;
                cnt = 0;
                for (int i=1;i<=colDefsLeft;i++)
                {
                    model.addElement(Integer.toString(i));
                    if (i == fc.getColspan())
                    {
                        inx = cnt;
                    } 
                }
                colSpanCBX.setEnabled(colDefsLeft > 1);
                colSpanCBX.getComboBox().setSelectedIndex(inx);
            }
        }
        
        ValCheckBox chkbx = (ValCheckBox)formViewObj.getControlByName("ignoreSetGet"); //$NON-NLS-1$
        chkbx.setSelected(fc.isIgnoreSetGet());

        chkbx = (ValCheckBox)formViewObj.getControlByName("changeListenerOnly"); //$NON-NLS-1$
        chkbx.setSelected(fc.isChangeListenerOnly());
    }
    
    /**
     * @param fcf
     */
    public void setDataIntoBase(final FormCellField fcf)
    {
        ValCheckBox chkbx = (ValCheckBox)formViewObj.getControlByName("isRequired"); //$NON-NLS-1$
        chkbx.setSelected(fcf.isRequired());
        
        chkbx = (ValCheckBox)formViewObj.getControlByName("isReadOnly"); //$NON-NLS-1$
        chkbx.setSelected(fcf.isReadOnly());
    }
    
    /**
     * @param fcf
     * @param rows
     * @param rowInx
     * @param cols
     * @param colInx
     */
    public void setDataIntoUI(final FormViewDef   fvd,
                              final FormCellField fcf, 
                              final int           rows,
                              final int           rowDefs,
                              final int           rowInx,
                              final int           cols,
                              final int           colDefs,
                              final int           colInx
                              )
    {
        if (formViewObj == null)
        {
            return;
        }
        
        formViewDef = fvd;
        currentFC   = fcf;
        data        = fcf;

        //viewPanel.getMultiView().setData(fcf);
        int inx;
        int cnt;
        
        
        setDataIntoBase(fcf, rows, rowDefs, rowInx, cols, colDefs, colInx, true);
        setDataIntoBase(fcf);
        
        JTextField typeLabel = (JTextField)formViewObj.getControlByName("type"); //$NON-NLS-1$
        typeLabel.setText(fcf.getUiType().toString());
        
        /*
        ValComboBox typeCBX = (ValComboBox)formViewObj.getControlByName("typecbx");
        DefaultComboBoxModel model = (DefaultComboBoxModel)typeCBX.getModel();
        inx = -1;
        cnt = 0;
        for (String type : subcontrolHash.keySet())
        {
            model.addElement(type);
            if (type.equals(fcf.getUiType().toString()))
            {
                inx = cnt;
            }
            cnt++;
        }
        typeCBX.getComboBox().setSelectedIndex(inx);*/
        
        
        
        if (fcf.getUiType() == FormCellFieldIFace.FieldType.combobox)
        {
            DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
            List<?> pickLists = session.getDataList(PickList.class);
            ValComboBox pickListCBX = (ValComboBox)formViewObj.getControlByName("picklistcbx"); //$NON-NLS-1$
            DefaultComboBoxModel model = (DefaultComboBoxModel)pickListCBX.getModel();
            inx = 0;
            cnt = 1;
            
            String dataPickListName = fcf.getPickListName();
    
            pickList.clear();
            model.addElement(getResourceString("EditorPropPanel.NONE")); //$NON-NLS-1$
            for (Iterator<?> iter=pickLists.iterator();iter.hasNext();)
            {
                PickList pl = (PickList)iter.next();
                String name = pl.getName();
                pickList.put(pl.getName(), pl.getIdentityTitle());
                model.addElement(name);
                if (StringUtils.isNotEmpty(dataPickListName) && dataPickListName.equals(name))
                {
                    inx = cnt;
                } 
                cnt++;
            }
            session.close();
            pickListCBX.getComboBox().setSelectedIndex(inx);
            
            ValTextArea textArea = (ValTextArea)formViewObj.getControlByName("list"); //$NON-NLS-1$
            String list = fcf.getProperty("list"); //$NON-NLS-1$
            if (list != null)
            {
                textArea.setText(list);
            }
        }
        
        multiView.validateAll(); 
    }
    
    /**
     * @param fc
     */
    protected void getDataFromUIBase(final FormCell fc)
    {
        ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("namecbx"); //$NON-NLS-1$
        String fName = (String)cbx.getComboBox().getSelectedItem();
        if (StringUtils.isNotEmpty(fName))
        {
            boolean fnd = false;
            for (DBFieldInfo fi : tableInfo.getFields())
            {
                System.out.println("["+fi.getName()+"]["+fName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (fi.getTitle().equals(fName) || fi.getName().equals(fName))
                {
                    fc.setName(fi.getName());
                    fnd = true;
                }
            }
            
            if (!fnd)
            {
                for (DBRelationshipInfo ri : tableInfo.getRelationships())
                {
                    if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                    {
                        //System.out.println("*["+ri.getTitle()+"]["+fName+"]");
                        if (ri.getTitle().equals(fName) || ri.getName().equals(fName))
                        {
                            fc.setName(ri.getName());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @param fcf
     */
    protected void getDataFromUIBase(final FormCellField fcf)
    {
        ValCheckBox chkbx = (ValCheckBox)formViewObj.getControlByName("isRequired"); //$NON-NLS-1$
        fcf.setRequired(chkbx.isSelected());

        chkbx = (ValCheckBox)formViewObj.getControlByName("isReadOnly"); //$NON-NLS-1$
        fcf.setReadOnly(chkbx.isSelected());

    }
    
    /**
     * @param fc
     */
    protected void getDataFromUI(final FormCell fc)
    {
        getDataFromUIBase(fc);
        
        if (fc instanceof FormCellLabel)
        {
            FormCellLabel fcl = (FormCellLabel)fc;
            
            ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("labelForCBX"); //$NON-NLS-1$
            int inx = cbx.getComboBox().getSelectedIndex();
            if (inx == 0)
            {
                fcl.setLabelFor(""); //$NON-NLS-1$
            } else
            {
                fcl.setLabelFor(getIdForName((String)cbx.getComboBox().getSelectedItem()));
            }
            
            cbx = (ValComboBox)formViewObj.getControlByName("imageNameCBX"); //$NON-NLS-1$
            inx = cbx.getComboBox().getSelectedIndex();
            if (inx == 0)
            {
                fcl.setIconName(""); //$NON-NLS-1$
            } else
            {
                String imgName = (String)cbx.getComboBox().getSelectedItem();
                if (StringUtils.isNotEmpty(imgName))
                {
                    fcl.setIconName(getIdForName(imgName));
                } else
                {
                    fcl.setIconName(getIdForName("")); //$NON-NLS-1$
                }
            }
            ValCheckBox chkbx = (ValCheckBox)formViewObj.getControlByName("isRecordObj"); //$NON-NLS-1$
            fcl.setRecordObj(chkbx.isSelected());
            
            fcl.setLabel(((ValTextField)formViewObj.getControlByName("label")).getText()); //$NON-NLS-1$
        }

    }
    
    /**
     * @param fcf
     */
    protected void getDataFromUI(final FormCellField fcf)
    {
        getDataFromUIBase((FormCell)fcf);
        getDataFromUIBase(fcf);
        
        if (fcf.getUiType() == FormCellFieldIFace.FieldType.combobox)
        {
            ValComboBox cbx = (ValComboBox)formViewObj.getControlByName("picklistcbx"); //$NON-NLS-1$
            int         inx = cbx.getComboBox().getSelectedIndex();
            if (inx == 0)
            {
                fcf.setPickListName(""); //$NON-NLS-1$
            } else
            {
                fcf.setPickListName((String)cbx.getComboBox().getSelectedItem());
            }
            
            ValTextArea textArea = (ValTextArea)formViewObj.getControlByName("list"); //$NON-NLS-1$
            fcf.getProperties().put("list", textArea.getText()); //$NON-NLS-1$
        }

    }
    
}
