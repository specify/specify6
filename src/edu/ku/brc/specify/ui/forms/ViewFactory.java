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
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.BrowseBtnPanel;
import edu.ku.brc.specify.ui.ColorChooser;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandActionWrapper;
import edu.ku.brc.specify.ui.ImageDisplay;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellCommand;
import edu.ku.brc.specify.ui.forms.persist.FormCellField;
import edu.ku.brc.specify.ui.forms.persist.FormCellLabel;
import edu.ku.brc.specify.ui.forms.persist.FormCellPanel;
import edu.ku.brc.specify.ui.forms.persist.FormCellSeparator;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormRow;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.DataChangeNotifier;
import edu.ku.brc.specify.ui.validation.FormValidator;
import edu.ku.brc.specify.ui.validation.ValComboBox;
import edu.ku.brc.specify.ui.validation.ValListBox;
import edu.ku.brc.specify.ui.validation.ValTextArea;
import edu.ku.brc.specify.ui.validation.ValTextField;
import edu.ku.brc.specify.ui.validation.ValidatedJPanel;

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
    
    //private Font  boldLabelFont = null;
    
    // Data Members 
    protected static SimpleDateFormat scrDateFormat = null;
    
    protected ViewFactory()
    {
        //JLabel label = new JLabel();
        //Font font = label.getFont();
        //boldLabelFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        
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
     * @param cellField the field info object
     * @return the JTextField
     */
    protected JTextField createTextField(final FormValidator validator, 
                                         final Object        value, 
                                         final FormCellField cellField)
    {
        String validationRule = cellField.getValidationRule();
        
        JTextField txtField;
        if (validator != null && (cellField.isRequired() || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            
            txtField = validator.createTextField(cellField.getName(), 
                                                 cellField.getCols(), 
                                                 cellField.isRequired(),
                                                 parseValidationType(cellField.getValidationType()),  // "OK" if error parsing
                                                 validationRule, 
                                                 cellField.getPickListName(),
                                                 cellField.isChangeListenerOnly());
        } else
        {
            txtField = new JTextField(cellField.getCols());
        }    
        txtField.setText(value != null ? value.toString() : "");
        
        return txtField;
    }
    
    /**
     * @param formView
     * @param validator
     * @param formViewObj
     * @param builder
     * @param labelsForHash
     * @param cc
     * @param currDataObj
     */
    protected void processRows(final FormFormView    formView, 
                               final FormValidator   validator, 
                               final FormViewObj     formViewObj,
                               final PanelBuilder    builder,
                               final Hashtable<String, JLabel> labelsForHash,
                               final CellConstraints cc, 
                               final Object          currDataObj,
                               final List<FormRow>   formRows)
    {
        int rowInx    = 1;
        int curMaxRow = 1;
        
        for (FormRow row : formRows)
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
                
                int        colspan   = cell.getColspan();
                int        rowspan   = cell.getRowspan();
                
                boolean    addToValidator = true;
                boolean    addControl     = true;

                if (cell.getType() == FormCell.CellType.label)
                {
                    FormCellLabel cellLabel = (FormCellLabel)cell;

                    String lblStr = cellLabel.getLabel();
                    if (false)
                    {
                        builder.addLabel(isNotEmpty(lblStr) ? lblStr + ":" : "  ", cc.xywh(colInx, rowInx, colspan, rowspan));
                        compToAdd      = null;
                        addToValidator = false;
                        addControl     = false;
                        colInx += colspan + 1;
                        
                    } else
                    {
                        JLabel        lbl       = new JLabel(isNotEmpty(lblStr) ? lblStr + ":" : "  ", JLabel.RIGHT);
                        //lbl.setFont(boldLabelFont);
                        labelsForHash.put(cellLabel.getLabelFor(), lbl);
                        
                        compToAdd      =  lbl;
                        addToValidator = false;
                        addControl     = false;
                    }
                       
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
                        compToAdd = createTextField(validator, "", cellField);
                        addToValidator = validator == null;
                    
                    } else if (uiType.equals("label")) 
                    {
                        compToAdd = new JLabel("", JLabel.LEFT);
                        
                    } else if (uiType.equals("image")) 
                    {
                        int w = 150;
                        int h = 150;
                        String str = cellField.getInitialize();
                        if (isNotEmpty(str))
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
                        String[] initArray = split(cellField.getInitialize(), ",");
                        for (int i=0;i<initArray.length;i++)
                        {
                            initArray[i] = initArray[i].trim();
                        }
                        

                        if (validator != null)
                        {
                            compToAdd = validator.createComboBox(cellField.getName(),
                                                             initArray, 
                                                             cellField.isRequired(),
                                                             parseValidationType(cellField.getValidationType()),
                                                             cellField.getValidationRule(),
                                                             cellField.getPickListName());
                        } else
                        {
                            compToAdd = initArray == null ? new ValComboBox() : new ValComboBox(initArray);
                            //DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), cbx, null);
                            //cbx.getModel().addListDataListener(dcn);
                        }

                        
                    } else if (uiType.equals("checkbox")) 
                    {
                        JCheckBox checkbox = new JCheckBox(cellField.getLabel()); 
                        if (validator != null)
                        {
                            DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), checkbox, null);
                            checkbox.addActionListener(dcn);
                        }
                        
                        compToAdd = checkbox;
                        
                    } else if (uiType.equals("password")) 
                    {
                        JTextField txt;
                        if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
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
                            txt = new ValTextField(cellField.getCols());
                            //DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), txt, null);
                            //txt.addActionListener(dcn);
                        }
                        
                        compToAdd = txt;
                        
                    } else if (uiType.equals("textarea")) 
                    {
                        JTextArea ta;
                        if (validator != null)
                        {
                            ta = validator.createTextArea(cellField.getName(), cellField.getRows(), cellField.getCols());
                            addToValidator = false;
                        } else
                        {
                            ta = new ValTextArea("", cellField.getRows(), cellField.getCols());
                        }
                        
                        ta.setLineWrap(true);
                        ta.setWrapStyleWord(true);
                        
                        JScrollPane scrollPane = new JScrollPane(ta);
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        
                        compToReg = ta;
                        compToAdd = scrollPane;
                        
                    } else if (uiType.equals("browse")) 
                    {                      
                        BrowseBtnPanel bbp = new BrowseBtnPanel(createTextField(validator, "", cellField));
                        compToAdd = bbp;
                        
                    } else if (uiType.equals("list")) 
                    {
                        int numRows = 15;
                        String[] initArray = null;
                        
                        String initStr = cellField.getInitialize();
                        if (isNotEmpty(initStr))
                        {
                            String[] initSections = split(initStr, ";");
                            if (initSections[0].indexOf("rows=") > -1)
                            {
                                String[] nameValPair = split(initStr, "=");
                                if (nameValPair.length == 2 && nameValPair[0].equals("rows"))
                                {
                                    numRows = Integer.parseInt(nameValPair[1]);
                                }
                                if (initSections.length == 2)
                                {
                                    initArray = split(initStr, ",");
                                    for (int i=0;i<initArray.length;i++)
                                    {
                                        initArray[i] = initArray[i].trim();
                                    }
                                }
                            } else
                            {
                                initArray = split(initSections[0], ",");
                            }
                        }
                        
                        JList list;
                        if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
                        {
    
                            list = validator.createList(cellField.getName(), 
                                                        initArray,
                                                        numRows,
                                                        cellField.isRequired(),
                                                        parseValidationType(cellField.getValidationType()),  // "OK" if error parsing
                                                        cellField.getValidationRule());
                        } else
                        {
                            list = initArray == null ? new ValListBox() : new ValListBox(initArray);
                            list.setVisibleRowCount(numRows);
                            //DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), list, null);
                            //list.addListSelectionListener(dcn);
                        }
                        
                        JScrollPane scrollPane = new JScrollPane(list);
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                        compToReg = list;
                        compToAdd = scrollPane;
                        
                    } else if (uiType.equals("colorchooser")) 
                    {
                        ColorChooser colorChooser = new ColorChooser(Color.BLACK);
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
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
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
                    
                    int subViewId = cellSubView.getId();
                    
                    FormView subFormView = ViewMgr.getView(cellSubView.getViewSetName(), subViewId);                       
                    if (subFormView != null)
                    {
                        FormViewObj subView = buildFormView(subFormView, null, formViewObj);
                        
                        //JPanel panel = (JPanel)subView.getUIComponent();
                        //panel.setBackground(Color.BLUE);
                        //panel.getComponent(0).setBackground(Color.RED);
                        //panel.getComponent(0).getComponent(0).setBackground(Color.YELLOW);
                        
                        builder.add(subView.getUIComponent(), cc.xywh(colInx, rowInx, cellSubView.getColspan(), 1, "fill,fill"));
                        String classDesc = cellSubView.getClassDesc();
                        if (cell.isIgnoreSetGet() || (classDesc != null && classDesc.length() > 0))
                        {
                            formViewObj.addSubView(cell, subView);
                        }
                        curMaxRow = rowInx;
                        
                    } else 
                    {
                        System.err.println("buildFormView - Could find subview's with name["+cellSubView.getViewSetName()+"] id["+subViewId+"]");
                    }
                    compToAdd = null;
                    colInx += 2;
                    
                } else if (cell.getType() == FormCell.CellType.panel) 
                {
                    FormCellPanel cellPanel = (FormCellPanel)cell;
                    String panelType = cellPanel.getPanelType();
                    
                    if (isEmpty(panelType))
                    {
                        DefaultFormBuilder panelBuilder = new DefaultFormBuilder(new FormLayout(cellPanel.getColDef(), cellPanel.getRowDef()));
                        
                        //Color[] colors = new Color[] {Color.YELLOW, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA};
                        //panelBuilder.getPanel().setBackground(colors[cnt % colors.length]);
                        //cnt++;
                        
                        processRows(formView, validator, formViewObj, panelBuilder, labelsForHash, new CellConstraints(), currDataObj, cellPanel.getRows());
                        
                        compToAdd = panelBuilder.getPanel();
                        
                    } else if (panelType.equalsIgnoreCase("buttonbar"))
                    {
                        
                        JButton[] btns = processRows(formView, validator, formViewObj, cellPanel.getRows());
                        compToAdd      = com.jgoodies.forms.factories.ButtonBarFactory.buildCenteredBar(btns);
                   }

                    addControl     = false;
                    addToValidator = false;
                    
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
        
  
    }
    //public static int cnt = 0;
    
    /**
     * @param formView
     * @param validator
     * @param formViewObj
     * @param builder
     * @param labelsForHash
     * @param cc
     * @param currDataObj
     */
    protected JButton[] processRows(final FormFormView    formView, 
                                    final FormValidator   validator, 
                                    final FormViewObj     formViewObj,
                                    final List<FormRow>   formRows)
    {   
        List<JButton> btns = new ArrayList<JButton>();
        
        for (FormRow row : formRows)
        {
            for (FormCell cell : row.getCells()) 
            {
                if (cell.getType() == FormCell.CellType.command) 
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
                    formViewObj.addControl(cell, btn);
                    btns.add(btn);
                } 
            }                
        }
        
        JButton[] btnsArray = new JButton[btns.size()];
        int i = 0;
        for (JButton b : btns)
        {
            btnsArray[i++] = b;
        }
        btns.clear();
        return btnsArray;
  
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
            String className = formView.getClassName();
            if (classObj == null && className != null && className.length() > 0)
            {
                try
                {
                    classObj = Class.forName(className);
                    
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
                if (dataObj != null)
                {
                    validator.addRuleObjectMapping("dataObj", dataObj);
                }
            }
            
            // Figure columns
            FormLayout      formLayout = new FormLayout(formView.getColumnDef(), formView.getRowDef());
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
            processRows(formView, validator, formViewObj, builder, labelsForHash, cc, currDataObj, formView.getRows());
            
            
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
