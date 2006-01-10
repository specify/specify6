/* Filename:    $RCSfile: ViewFactory.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import java.awt.BorderLayout;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.beanutils.PropertyUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormRow;
import edu.ku.brc.specify.ui.forms.persist.FormView;

/**
 * Creates FormViewObj object that implment the FormViewable interface.
 * 
 * @author rods
 *
 */
public class ViewFactory
{
    // Statics
    //private final static Logger log        = Logger.getLogger(ViewMgr.class);
    private static ViewFactory  instance   = new ViewFactory();

    protected ViewFactory()
    {
        
    }
    
    /**
     * 
     * @return the singleton for the ViewMgr
     */
    public static ViewFactory getInstance()
    {
        return instance;
    }
    

    /**
     * Creates a JGoodies definition from a List of Strings
     * @param list the list of definitions
     * @return Returns a JGoodies definition from a List of Strings
     */
    protected static String getDefAsString(List<String> list)
    {
        StringBuffer defStr = new StringBuffer();
        for (int i=0;i<list.size();i++)
        {
            defStr.append((i > 0 ? "," : "") + list.get(i));
        }
        return defStr.toString();
    }
    
    /**
     * 
     * @param aObj
     */
    public static JPanel createIconPanel(JComponent aComp)
    {
        JPanel  panel = new JPanel(new BorderLayout());
        //panel.setOpaque(true);
        //panel.setBackground(Color.RED);
        JButton btn   = new JButton("...");
        panel.add(btn, BorderLayout.WEST);
        panel.add(aComp, BorderLayout.EAST);
        return panel;
    }
    
    /**
     * Returns the data object for this field in the "main" data object
     * @param dataObj the main data object
     * @param fieldName the field name to be gotten
     * @return return the field data frm the POJO
     */
    public static Object getFieldValue(Object dataObj, String fieldName) 
    {
        Object value = null;
        if (dataObj != null)
        {
            try 
            {
                Method getter = PropertyUtils.getReadMethod(PropertyUtils.getPropertyDescriptor(dataObj, fieldName));
                value = getter.invoke(dataObj, (Object[])null);
                
            } catch (Exception ex) 
            {
                // XXX FIXME
                ex.printStackTrace();
            }
        }
        return value == null ? "" : value;    
    }    
    
    
    /**
     * @param formView
     * @param dataObj
     * @param classObj
     * @param parentView
     * @return
     */
    public static FormViewObj buildFormView(final FormView     formView, 
                                            final Object       dataObj, 
                                            final FormViewObj  parentView)
    {
        if (formView == null) return null;
        
        if (formView.getType() == FormView.ViewType.form)
        {
            return buildFormView((FormFormView)formView, dataObj, parentView);
            
        } else if (formView.getType() == FormView.ViewType.table)
        {
            return null;
            
        } else if (formView.getType() == FormView.ViewType.field)
        {
            return null;
            
        } else
        {
            throw new RuntimeException("Form Type not covered by builder ["+formView.getType()+"]");
        }
        
    }   
    
    /**
     * @param formView
     * @param dataObj
     * @param classObj
     * @param parentView
     * @return
     */
    public static FormViewObj buildFormView(final FormFormView formView, 
                                            final Object       dataObj, 
                                            final FormViewObj  parentView)
    {
        if (formView == null) return null;
        
        try 
        {
            Class classObj = dataObj != null ? dataObj.getClass() : null;
            if (classObj == null)
            {
                classObj = Class.forName(formView.getClassName());
            }
            
            FormViewObj formViewObj = new FormViewObj(parentView, formView);
            
            // Figure columns
            List<String> colList = formView.getColumnDef();
            String   colsDefStr  = getDefAsString(colList);
            int      numCols     = colList.size();

            List<String> rowList = formView.getRowDef();
            String   rowsDefStr  = getDefAsString(rowList);

            FormLayout      formLayout = new FormLayout(colsDefStr, rowsDefStr);
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
            boolean firstOne = false;
            int rowInx = 1;
            for (FormRow row : formView.getRows())
            {
                int colInx = 1;
                for (FormCell cell : row.getCells()) 
                {
                    String fieldName = cell.getName();
                                           
                    if (cell.getType() == FormCell.CellType.field)
                    {
                        String labelStr      = cell.getLabel(); // XXX Don't forget about localization
                        
                        String format = cell.getFormat();
                        String uiType = cell.getUiType();
                        
                        int colspan   = cell.getColspan();
                        int rowspan   = cell.getRowspan();
                        
                        colspan = (colspan > 0) ? colspan : 1;
                        if (labelStr == null)
                        {
                            labelStr = fieldName;
                        }
                        JLabel label = new JLabel(labelStr + ":", JLabel.RIGHT);
                        JComponent compToAdd = label;
                        if (firstOne) 
                        {
                            compToAdd = createIconPanel(label);
                            firstOne = false;
                        }
                        if (compToAdd != null)
                        {
                            builder.add(compToAdd, cc.xy(colInx, rowInx));
                            colInx += 2;
                        }
                        
                        Object value = null;
                        /** XXX Don't forget the Attrs!
                        if (!aIsAttrs && _dataGetter != null)
                        {
                            value = _dataGetter.getFieldValue(aObj, fieldName);
                        }
                        */
                        if (formView.getDataGettable() != null)
                        {
                            value = formView.getDataGettable().getFieldValue(dataObj, fieldName);
                        }
                        
                        JComponent comp = null;
                        if (uiType == null || uiType.length() == 0)
                        {
                            uiType = "text";
                        }
                        
                        if (uiType.equals("text"))
                        {
                            comp = new JTextField(value != null ? value.toString() : "", cell.getCols());
                        
                        } else if (uiType.equals("img")) {
                            //ImageDisplay imgDisp = new ImageDisplay();
                            comp = null;//imgDisp;
                            
                        } else if (uiType.equals("url")) {
                            JButton btn = new JButton("Show");
                            comp = btn;
                            
                        } else if (uiType.equals("combobox")) {
                            comp = new JComboBox(); 
                            
                        } else if (uiType.equals("checkbox")) {
                            comp = new JCheckBox("", value.toString().equals("true")); 
                            
                        } else if (uiType.equals("textarea")) {
                            JTextArea ta = new JTextArea(value.toString(), cell.getRows(), cell.getCols());
                            
                            JScrollPane sp = new JScrollPane(ta);
                            //sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            //sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                            
                            comp = sp;
                        } else
                        {
                            throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
                        }
                        /* XXX hashtable
                        if (aHashtable != null)
                        {
                            aHashtable.put(fieldName, comp);
                            
                        }
                        if (aFormatHashtable != null && format != null && format.length() > 0)
                        {
                            aFormatHashtable.put(fieldName, format);
                            
                        }
                        */
                        if (rowspan == -1)
                        {
                            builder.add(comp, colspan == 1 ? cc.xy(colInx, rowInx) : cc.xyw(colInx, rowInx, colspan));
                        } else 
                        {
                            rowspan = (rowspan > 0) ? rowspan : 1; 
                            builder.add(comp, cc.xywh(colInx, rowInx, colspan, rowspan));
                        }
                        colInx += 2;


                    } else if (cell.getType() == FormCell.CellType.separator) 
                    {
                        String labelStr = cell.getLabel(); // XXX Don't forget about localization
                        
                        builder.addSeparator(labelStr == null ? "" : labelStr, cc.xyw(colInx, rowInx, numCols));
                        
                    } else if (cell.getType() == FormCell.CellType.subview) 
                    {
                        FormCellSubView cellSubView = (FormCellSubView)cell;
                        
                        int    subViewId     = cellSubView.getId();
                        
                        FormView subFormView = ViewMgr.getView(cellSubView.getViewSetName(), subViewId);                       
                        if (subFormView != null)
                        {
                            FormViewObj subView = buildFormView(subFormView, null, formViewObj);
                            builder.add(subView.getUIComponent(), cc.xyw(colInx, rowInx, numCols));
                        
                        } else 
                        {
                            System.err.println("buildFormView - Could find subview's with id["+subViewId+"]");
                        }
                        
                    }
                }                
                rowInx += 2;
            }
            
            if (parentView == null)
            {
                builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            }
            
            formViewObj.setComp(builder.getPanel());
            
            return formViewObj;

        } catch (Exception e)
        {
            System.err.println("buildPanel - Outer id["+formView.getId()+"]  Name["+formView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    } 
    
    /**
     * Creates a FormView
     * @param view the definition of the form view to be created
     * @return return a new FormView
     */
    public static FormViewable createView(FormView formView)
    {
        try
        {
            if (formView.getType() == FormView.ViewType.form)
            {
                FormViewObj formViewObj = buildFormView((FormFormView)formView, null, null);
                return formViewObj; 
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    

}
