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

import static edu.ku.brc.specify.ui.validation.UIValidator.parseValidationType;

import java.awt.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellCommand;
import edu.ku.brc.specify.ui.forms.persist.FormCellField;
import edu.ku.brc.specify.ui.forms.persist.FormCellLabel;
import edu.ku.brc.specify.ui.forms.persist.FormCellSeparator;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormRow;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.*;
import edu.ku.brc.specify.prefs.*;

/**
 * Creates FormViewObj object that implment the FormViewable interface.
 * 
 * @author rods
 *
 */
public class ViewFactory
{
    // Statics
    private static Log log = LogFactory.getLog(ViewFactory.class);
    private static final ViewFactory  instance = new ViewFactory();
    
    private Font  boldLabelFont = null;
    
    // Data Members 
    protected static SimpleDateFormat scrDateFormat = null;
    
    protected ViewFactory()
    {
        JLabel label = new JLabel();
        Font font = label.getFont();
        boldLabelFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        
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
     * 
     * @param aObj
     */
    public JPanel createIconPanel(JComponent aComp)
    {
        JPanel  panel = new JPanel(new BorderLayout());
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
    public Object getFieldValue(Object dataObj, String fieldName) 
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
     * Creates an array of string from a comman separated string
     * @param initStr the comman separated string
     * @return the array of strings or null
     */
    protected String[] getStringArray(final String initStr)
    {
        if (initStr.length() > 0)
        {
            StringTokenizer st = new StringTokenizer(initStr, ",");
            if (st.countTokens() > 0)
            {
                String[] strs = new String[st.countTokens()];
                int cnt = 0;
                while (st.hasMoreTokens())
                {
                    strs[cnt++] = st.nextToken().trim();
                }
                return strs;
            }
            
        }
        return null;
    }
    
    /**
     * @param formView
     * @param dataObj
     * @param classObj
     * @param parentView
     * @return
     */
    public FormViewObj buildFormView(final FormView     formView, 
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
     * Helper for creating a field
     * @param validator the validator
     * @param value the value of the control
     * @param name the name
     * @param cols the number of columns
     * @param isRequired whether it is required
     * @param valType the validation type
     * @param valRule the validation rule
     * @return the JTextField
     */
    protected JTextField createTextField(final FormValidator validator, 
                                         final Object  value, 
                                         final String  name,
                                         final int     cols,
                                         final boolean isRequired,
                                         final String  valType,
                                         final String  valRule)
    {
        JTextField txtField;
        if (validator != null && (isRequired || valRule.length() > 0))
        {
            
            txtField = validator.createTextField(name, cols, isRequired,
                                                 parseValidationType(valType),  // "OK" if error parsing
                                                 valRule);
        } else
        {
            txtField = new JTextField(cols);
        }    
        txtField.setText(value != null ? value.toString() : "");
        return txtField;
    }

    
    /**
     * @param formView
     * @param dataObj
     * @param classObj
     * @param parentView
     * @return
     */
    public FormViewObj buildFormView(final FormFormView formView, 
                                     final Object       dataObj, 
                                     final FormViewObj  parentView)
    {
        if (formView == null) return null;
        
        try 
        {
            Class classObj = dataObj != null ? dataObj.getClass() : null;
            if (classObj == null)
            {
                try
                {
                    classObj = Class.forName(formView.getClassName());
                    
                } catch (ClassNotFoundException ex)
                {
                    log.error(ex);
                }
            }
            
            Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
            
            FormViewObj     formViewObj    = new FormViewObj(parentView, formView, dataObj);
            ValidatedJPanel validatedPanel = null;
            FormValidator   validator      = null;
            
            Object currDataObj = formViewObj.getCurrentDataObj();
            
            if (formView.isValidated())
            {
                validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
            }
            
            // Figure columns
            FormLayout      formLayout = new FormLayout(formView.getColumnDef(), formView.getRowDef());
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
            int rowInx    = 1;
            int curMaxRow = 1;
            
            for (FormRow row : formView.getRows())
            {
                int colInx = 1;
                
                if (rowInx < curMaxRow)
                {
                    //rowInx = curMaxRow;
                }
                for (FormCell cell : row.getCells()) 
                {
                    JComponent compToAdd = null;    
                    JComponent compToReg = null;    
                    
                    String     fieldName = cell.getName();                 
                    int        colspan   = cell.getColspan();
                    int        rowspan   = cell.getRowspan();
                    
                    boolean    addToValidator = true;
                    boolean    addControl     = true;
                    
                    Object value = null;
                    
                    /** XXX Don't forget the Attrs!
                    if (!aIsAttrs && _dataGetter != null)
                    {
                        value = _dataGetter.getFieldValue(aObj, fieldName);
                    }
                    */
                    
                    if (cell.getType() != FormCell.CellType.field && 
                        cell.getType() != FormCell.CellType.label && 
                        cell.getType() != FormCell.CellType.separator && 
                        cell.getType() != FormCell.CellType.command && 
                        formView.getDataGettable() != null)
                    {
                        value = formView.getDataGettable().getFieldValue(currDataObj, fieldName);
                    }


                    if (cell.getType() == FormCell.CellType.label)
                    {
                        FormCellLabel cellLabel = (FormCellLabel)cell;

                        String lblStr = cellLabel.getLabel();
                        if (false)
                        {
                            builder.addLabel(lblStr.length() > 0 ? lblStr + ":" : "  ", cc.xywh(colInx, rowInx, colspan, rowspan));
                            compToAdd      = null;
                            addToValidator = false;
                            addControl     = false;
                            colInx += colspan + 1;
                            
                        } else
                        {
                            JLabel        lbl       = new JLabel(lblStr.length() > 0 ? lblStr + ":" : "  ", JLabel.RIGHT);
                            //lbl.setFont(boldLabelFont);
                            labelsForHash.put(cellLabel.getLabelFor(), lbl);
                            
                            compToAdd      =  lbl;
                            addToValidator = false;
                            addControl     = false;
                        }
                           
                    } else if (cell.getType() == FormCell.CellType.field)
                    {
                        FormCellField cellField = (FormCellField)cell;
                        
                        /* ZZZ Removing Setting of Value
                         
                        String format = cellField.getFormat();
                        if (format != null && format.length() > 0)
                        {
                            String[] fields = StringUtils.split(fieldName, ",");
                            Object[] values = new Object[fields.length];
                            for (int i=0;i<values.length;i++)
                            {
                                values[i] = formView.getDataGettable().getFieldValue(currDataObj, fields[i]);
                            }
                            if (values.length == 1 && values[0] instanceof java.util.Date)
                            {
                                //value = fastDateFormat.format((java.util.Date)values[0]);
                                
                                value = scrDateFormat.format((java.util.Date)values[0]);
                            } else
                            {
                                Formatter formatter = new Formatter();
                                formatter.format(format, (Object[])values);
                                value = formatter.toString();
                            }
                        } else 
                        {
                            value = currDataObj == null ? "" : formView.getDataGettable().getFieldValue(currDataObj, fieldName);
                            //if (value != null)
                            //{
                            //    System.out.println("*** "+value.getClass().toString());
                            //}
                            if (value instanceof Date)
                            {
                                value = scrDateFormat.format(value);
                            }
                        }
                        */
                        
                        String uiType = cellField.getUiType();
                        
                        
                        if (uiType == null || uiType.length() == 0)
                        {
                            uiType = "text";
                        }
                        
                        if (uiType.equals("text"))
                        {
                            compToAdd = createTextField(validator, 
                                                        value,
                                                        cellField.getName(), 
                                                        cellField.getCols(), 
                                                        cellField.isRequired(),
                                                        cellField.getValidationType(),  
                                                        cellField.getValidationRule());
                            addToValidator = validator == null;
                            
                        
                        } else if (uiType.equals("label")) 
                        {
                            compToAdd = new JLabel("", JLabel.LEFT);
                            
                        } else if (uiType.equals("image")) 
                        {
                            int w = 150;
                            int h = 150;
                            String str = cellField.getInitialize();
                            if (str != null && str.length() > 0)
                            {
                                int inx = str.indexOf("size=");
                                if (inx > -1)
                                {
                                    String[] wh = StringUtils.split(str.substring(inx+5), ",");
                                    if (wh.length == 2)
                                    {
                                        try
                                        {
                                            w = Integer.parseInt(wh[0]);
                                            h = Integer.parseInt(wh[1]);
                                            
                                        } catch (Exception ex)
                                        {
                                            log.error("Initialize string for Image is incorrect ["+str+"]");
                                        }
                                    }
                                }
                            }
                            ImageDisplay imgDisp = new ImageDisplay(w, h, true);
                            compToAdd = imgDisp;
                            
                            addToValidator = false;
                            
                        } else if (uiType.equals("url")) 
                        {
                            JButton btn = new JButton("Show");
                            compToAdd = btn;
                            addToValidator = false;
                            
                        } else if (uiType.equals("combobox")) 
                        {
                            JComboBox cbx;
                            
                            //String valStr = value.toString();
                            String[] initArray = getStringArray(cellField.getInitialize());
                            /*int inx = -1;
                            for (int i =0;i<initArray.length;i++)
                            {
                                if (initArray[i].equals(valStr))
                                {
                                    inx = i;
                                    break;
                                }
                            }*/
                            
                            if (validator != null)
                            {
                                cbx = validator.createComboBox(cellField.getName(), initArray);
                                addToValidator = false;
                                
                            } else
                            { 
                                cbx = new JComboBox(initArray);
                            }
                            //cbx.setSelectedIndex(inx);
                            compToAdd = cbx;
                            
                        } else if (uiType.equals("checkbox")) 
                        {
                            compToAdd = new JCheckBox(cellField.getLabel()); 
                            
                        } else if (uiType.equals("password")) 
                        {
                            JTextField txt;
                            if (validator != null && (cellField.isRequired() || cellField.getValidationRule().length() > 0))
                            {
                                
                                txt = validator.createPasswordField(cellField.getName(), 
                                                                    cellField.getCols(), 
                                                                    cellField.isRequired(),
                                                                    cellField.isEncrypted(),
                                                                    parseValidationType(cellField.getValidationType()),  // "OK" if error parsing
                                                                    cellField.getValidationRule());
                                addToValidator = false;
                            } else
                            {
                                txt = new JTextField(cellField.getCols());
                            }
                            txt.setText(value != null ? value.toString() : "");
                            compToAdd = txt;
                            
                        } else if (uiType.equals("textarea")) 
                        {
                            JTextArea ta = new JTextArea("", cellField.getRows(), cellField.getCols());
                            
                            //System.out.println(ta.getPreferredScrollableViewportSize());
                            System.out.println(ta.getPreferredSize());
                            
                            ta.setLineWrap(true);
                            ta.setWrapStyleWord(true);
                            
                            JScrollPane scrollPane = new JScrollPane(ta);
                            System.out.println(scrollPane.getPreferredSize());

                            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                            
                            //String str = scrollPane.getPreferredSize().width+"px,p";
                            //builder.add(compToAdd, cc.xywh(colInx, rowInx, colspan, rowspan, str));
                            
                            compToReg = ta;
                            compToAdd = scrollPane;
                            
                        } else if (uiType.equals("browse")) 
                        {
                            
                            JTextField textField = createTextField(validator, 
                                                                   value,
                                                                   cellField.getName(), 
                                                                   cellField.getCols(), 
                                                                   cellField.isRequired(),
                                                                   cellField.getValidationType(),  
                                                                   cellField.getValidationRule());

                            BrowseBtnPanel bbp = new BrowseBtnPanel(textField);
                            /*if (validator != null)
                            {
                                validator.addUIComp(cell.getName(), bbp);
                            }
                            builder.add(bbp, cc.xywh(colInx, rowInx, colspan, rowspan));
                            formViewObj.addControl(cell.getName(), textField);
                          
                            addToValidator = false;
                            */
                            compToAdd = bbp;
                            
                        } else if (uiType.equals("colorchooser")) 
                        {
                            ColorWrapper cw;
                            try
                            {
                                cw = new ColorWrapper((String)value);
                            } catch (RuntimeException ex)
                            {
                                cw = new ColorWrapper(Color.BLACK);
                            }
                            ColorChooser colorChooser = new ColorChooser(cw.getColor());
                            
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), colorChooser, null);
                                colorChooser.addPropertyChangeListener("setValue", dcn);
                            }
                            compToAdd = colorChooser;
                                
                            
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


                    } else if (cell.getType() == FormCell.CellType.command) 
                    {
                        FormCellCommand cellCmd = (FormCellCommand)cell;
                        JButton btn  = new JButton(cellCmd.getLabel());
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), value)));
                        addToValidator = false;
                        compToAdd = btn;
                        
                    } else if (cell.getType() == FormCell.CellType.separator) 
                    {
                        compToAdd = null;
                        Component sep = builder.addSeparator(((FormCellSeparator)cell).getLabel(), cc.xyw(colInx, rowInx, cell.getColspan()));
                        if (cell.getName().length() > 0)
                        {
                            formViewObj.addControl(cell, sep);
                        }
                        curMaxRow = rowInx;
                        colInx += 2;
                        
                    } else if (cell.getType() == FormCell.CellType.subview) 
                    {
                        FormCellSubView cellSubView = (FormCellSubView)cell;
                        
                        int    subViewId     = cellSubView.getId();
                        
                        FormView subFormView = ViewMgr.getView(cellSubView.getViewSetName(), subViewId);                       
                        if (subFormView != null)
                        {
                            FormViewObj subView = buildFormView(subFormView, null, formViewObj);
                            
                            
                            //JPanel panel = (JPanel)subView.getUIComponent();
                            //panel.setBackground(Color.BLUE);
                            //panel.getComponent(0).setBackground(Color.RED);
                            //panel.getComponent(0).getComponent(0).setBackground(Color.YELLOW);
                            
                            builder.add(subView.getUIComponent(), cc.xywh(colInx, rowInx, cellSubView.getColspan(), 1, "fill,fill"));
                            formViewObj.addSubView(cell, subView);
                            curMaxRow = rowInx;
                            
                        } else 
                        {
                            System.err.println("buildFormView - Could find subview's with name["+cellSubView.getViewSetName()+"] id["+subViewId+"]");
                        }
                        compToAdd = null;
                        colInx += 2;
                    }
                    
                    if (compToAdd != null)
                    {
                        //System.out.println(colInx+"  "+rowInx+"  "+colspan+"  "+rowspan+"  "+compToAdd.getClass().toString());
                        
                        
                        builder.add(compToAdd, cc.xywh(colInx, rowInx, colspan, rowspan));

                        curMaxRow = Math.max(curMaxRow, rowspan+rowInx);
                        
                        if (addControl)
                        {
                            formViewObj.addControl(cell, compToReg == null ? compToAdd : compToReg);
                        }
                        
                        if (validator != null && addToValidator)
                        {

                            validator.addUIComp(cell.getName(), compToReg == null ? compToAdd : compToReg);
                        }
                        colInx += colspan + 1;
                     }
                    
                }                
                rowInx += 2;
            }
            
            if (parentView == null)
            {
                builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            }
            
            if (formView.isValidated())
            {
                validatedPanel.addPanel(builder.getPanel());
                
                // Here we add all the components whether they are used or not
                // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
                Map<String, Component> mapping = formViewObj.getControlMapping();
                for (String name : mapping.keySet())
                {
                    validatedPanel.addValidationComp(name, mapping.get(name));
                }               
                Map<String, String> enableRules = formView.getEnableRules();
                
                // Load up validation Rules
                FormValidator fv = validatedPanel.getFormValidator();
                formViewObj.setValidator(fv);
                
                for (String name : enableRules.keySet())
                {
                    fv.addEnableRule(name, enableRules.get(name));
                }
                
                // Load up labels and associate them with there component
                for (String nameFor : labelsForHash.keySet())
                {
                    fv.addUILabel(nameFor, labelsForHash.get(nameFor));
                }
                
                
                formViewObj.setFormComp(validatedPanel);
            } else
            {
                formViewObj.setFormComp(builder.getPanel());
            }
            
            return formViewObj;

        } catch (Exception e)
        {
            System.err.println("buildPanel - Outer id["+formView.getId()+"]  Name["+formView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    } 
    
    /**
     * @param formView
     * @param dataObj
     * @param classObj
     * @param parentView
     * @return
     */
    /*public FormViewObj buildFormViewNew(final FormFormView formView, 
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
            
            Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
            
            FormViewObj     formViewObj    = new FormViewObj(parentView, formView, dataObj);
            ValidatedJPanel validatedPanel = null;
            FormValidator   validator      = null;
            
            Object currDataObj = formViewObj.getCurrentDataObj();
            
            if (formView.isValidated())
            {
                validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
            }
            
            // Figure columns
            PanelBuilder    builder    = new PanelBuilder(new FormLayout(formView.getColumnDef(), formView.getRowDef()));
            CellConstraints cc         = new CellConstraints();
            
            int rowInx = 1;
            for (FormRow row : formView.getRows())
            {
                int colInx = 1;
                for (FormCell cell : row.getCells()) 
                {
                    JComponent compToAdd = null;    
                    
                    String     fieldName = cell.getName();                 
                    int        colspan   = cell.getColspan();
                    int        rowspan   = cell.getRowspan();
                    
                    boolean    addToValidator = true;
                    
                    if (cell.getType() == FormCell.CellType.label)
                    {
                        FormCellLabel cellLabel = (FormCellLabel)cell;
                        String        lblStr    = cellLabel.getLabel();
                        JLabel        lbl        = new JLabel(lblStr.length() > 0 ? lblStr + ":" : lblStr, JLabel.RIGHT);
                        labelsForHash.put(cellLabel.getLabelFor(), lbl);
                        
                        compToAdd      = lbl;
                        addToValidator = false;
                       
                    } else if (cell.getType() == FormCell.CellType.field)
                    {
                        FormCellField cellField = (FormCellField)cell;
                        String uiType = cellField.getUiType();
                        
                        
                        if (uiType == null || uiType.length() == 0)
                        {
                            uiType = "text";
                        }
                        
                        if (uiType.equals("text"))
                        {
                            compToAdd = createTextField(validator, 
                                                        "",
                                                        cellField.getName(), 
                                                        cellField.getCols(), 
                                                        cellField.isRequired(),
                                                        cellField.getValidationType(),  
                                                        cellField.getValidationRule());
                            addToValidator = validator == null;
                            
                        
                        } else if (uiType.equals("label")) 
                        {
                            compToAdd = new JLabel("", JLabel.LEFT);
                            
                        } else if (uiType.equals("img")) 
                        {
                            //ImageDisplay imgDisp = new ImageDisplay();
                            compToAdd = null;//imgDisp;
                            addToValidator = false;
                            
                        } else if (uiType.equals("url")) 
                        {
                            JButton btn = new JButton("Show");
                            compToAdd = btn;
                            addToValidator = false;
                            
                        } else if (uiType.equals("combobox")) 
                        {
                            JComboBox cbx;
                            
                            String valStr = formView.getDataGettable() != null ? formView.getDataGettable().getFieldValue(currDataObj, fieldName).toString() : "";
                            String[] initArray = getStringArray(cellField.getInitialize());
                            int inx = -1;
                            for (int i =0;i<initArray.length;i++)
                            {
                                if (initArray[i].equals(valStr))
                                {
                                    inx = i;
                                    break;
                                }
                            }
                            
                            if (validator != null)
                            {
                                cbx = validator.createComboBox(cellField.getName(), initArray);
                                addToValidator = false;
                                
                            } else
                            { 
                                cbx = new JComboBox(initArray);
                             }
                            cbx.setSelectedIndex(inx);
                            compToAdd = cbx;
                            
                        } else if (uiType.equals("checkbox")) 
                        {
                            compToAdd = new JCheckBox(cellField.getName()); 
                            
                        } else if (uiType.equals("password")) 
                        {
                            JTextField txt;
                            if (validator != null && (cellField.isRequired() || cellField.getValidationRule().length() > 0))
                            {
                                
                                txt = validator.createPasswordField(cellField.getName(), 
                                                                    cellField.getCols(), 
                                                                    cellField.isRequired(),
                                                                    cellField.isEncrypted(),
                                                                    parseValidationType(cellField.getValidationType()),  // "OK" if error parsing
                                                                    cellField.getValidationRule());
                                addToValidator = false;
                            } else
                            {
                                txt = new JTextField(cellField.getCols());
                            }
                            compToAdd = txt;     
                            
                        } else if (uiType.equals("textarea")) 
                        {
                            JTextArea ta = new JTextArea("", cellField.getRows(), cellField.getCols());
                            
                            System.out.println(ta.getPreferredScrollableViewportSize());
                            System.out.println(ta.getPreferredSize());
                            
                            ta.setLineWrap(true);
                            ta.setWrapStyleWord(true);
                            JScrollPane scrollPane = new JScrollPane(ta);
                            //sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            //sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                            
                            compToAdd = scrollPane;
                            
                        } else if (uiType.equals("browse")) 
                        {
                            
                            JTextField textField = createTextField(validator, 
                                                                   "",
                                                                   cellField.getName(), 
                                                                   cellField.getCols(), 
                                                                   cellField.isRequired(),
                                                                   cellField.getValidationType(),  
                                                                   cellField.getValidationRule());

                            BrowseBtnPanel bbp = new BrowseBtnPanel(textField);
                            compToAdd = bbp;
                            
                        } else if (uiType.equals("colorchooser")) 
                        {
                            ColorWrapper cw           = new ColorWrapper(Color.WHITE); // set a default (arbitrary color)
                            ColorChooser colorChooser = new ColorChooser(cw.getColor());
                            
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), colorChooser, null);
                                colorChooser.addPropertyChangeListener("setValue", dcn);
                            }
                            compToAdd = colorChooser;
                                
                            
                        } else
                        {
                            throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
                        }


                    } else if (cell.getType() == FormCell.CellType.command) 
                    {
                        Object value = formView.getDataGettable() != null ? formView.getDataGettable().getFieldValue(currDataObj, fieldName) : null;
                        FormCellCommand cellCmd = (FormCellCommand)cell;
                        JButton btn  = new JButton(cellCmd.getLabel());
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), value)));
                        addToValidator = false;
                        compToAdd = btn;
                        
                    } else if (cell.getType() == FormCell.CellType.separator) 
                    {
                        compToAdd = null;
                        Component sep = builder.addSeparator(((FormCellSeparator)cell).getLabel(), cc.xyw(colInx, rowInx, cell.getColspan()));
                        if (cell.getName().length() > 0)
                        {
                            formViewObj.addControl(cell, sep);
                        }
                        colInx += 2;
                        
                    } else if (cell.getType() == FormCell.CellType.subview) 
                    {
                        FormCellSubView cellSubView = (FormCellSubView)cell;
                        
                        int    subViewId     = cellSubView.getId();
                        
                        FormView subFormView = ViewMgr.getView(cellSubView.getViewSetName(), subViewId);                       
                        if (subFormView != null)
                        {
                            FormViewObj subView = buildFormView(subFormView, null, null);//formViewObj);
                            builder.add(subView.getUIComponent(), cc.xyw(colInx, rowInx, cellSubView.getColspan()));
                            formViewObj.addControl(cell, subView.getUIComponent());
                        
                        } else 
                        {
                            System.err.println("buildFormView - Could find subview's with id["+subViewId+"]");
                        }
                        compToAdd = null;
                        colInx += 2;
                    }
                    
                    if (compToAdd != null)
                    {
                        builder.add(compToAdd, cc.xywh(colInx, rowInx, colspan, rowspan));
                        if (!(compToAdd instanceof JLabel))
                        {
                            formViewObj.addControl(cell, compToAdd);
                        }
                        
                        if (validator != null && addToValidator)
                        {
                            validator.addUIComp(cell.getName(), compToAdd);
                        }
                        colInx += 2;
                     }
                    
                }                
                rowInx += 2;
            }
            
            if (parentView == null)
            {
                builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            }
            
            if (formView.isValidated())
            {
                validatedPanel.addPanel(builder.getPanel());
                
                // Here we add all the components whether they are used or not
                // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
                Map<String, Component> mapping = formViewObj.getControlMapping();
                for (String name : mapping.keySet())
                {
                    validatedPanel.addValidationComp(name, mapping.get(name));
                }               
                Map<String, String> enableRules = formView.getEnableRules();
                
                // Load up validation Rules
                FormValidator fv = validatedPanel.getFormValidator();
                formViewObj.setValidator(fv);
                
                for (String name : enableRules.keySet())
                {
                    fv.addEnableRule(name, enableRules.get(name));
                }
                
                // Load up labels and associate them with there component
                for (String nameFor : labelsForHash.keySet())
                {
                    fv.addUILabel(nameFor, labelsForHash.get(nameFor));
                }
                
                
                formViewObj.setFormComp(validatedPanel);
            } else
            {
                formViewObj.setFormComp(builder.getPanel());
            }
            
            return formViewObj;

        } catch (Exception e)
        {
            System.err.println("buildPanel - Outer id["+formView.getId()+"]  Name["+formView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    } */
    
    /**
     * Creates a FormView
     * @param view the definition of the form view to be created
     * @return return a new FormView
     */
    public static FormViewable createView(FormView formView)
    {
        if (scrDateFormat == null)
        {
            scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }
        //try
        //{
            if (formView.getType() == FormView.ViewType.form)
            {
                FormViewObj formViewObj = instance.buildFormView((FormFormView)formView, null, null);
                return formViewObj; 
            }
            
        //} catch (Exception ex)
        //{
        //    ex.printStackTrace();
        //}
        return null;
    }
    
    /**
     * Creates a FormView with a data object to fill it in
     * @param view the definition of the form view to be created
     * @param data the data to fill the form
     * @return return a new FormView
     */
    public static FormViewable createView(FormView formView, Object data)
    {
        //try
       // {
            if (formView.getType() == FormView.ViewType.form)
            {
                return instance.buildFormView((FormFormView)formView, data, null); 
            }
            
        //} catch (Exception ex)
        //{
        //    ex.printStackTrace();
        //}
        return null;
    }

    
}
