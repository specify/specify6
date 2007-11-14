/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValCheckBox;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValTextField;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class QueryFieldPanel extends JPanel
{
    protected static final Logger log = Logger.getLogger(QueryFieldPanel.class);
    
    protected String           noMappingStr = getResourceString("WB_NO_MAPPING");

    protected QueryBldrPane    queryBldrPane;
    protected String           columnDefStr;
    protected ImageIcon        blankIcon = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    
    protected boolean          hasFocus      = false;
    protected Color            bgColor       = null;
    protected JLabel           fieldLabel;
    protected JLabel           closeBtn;
    protected JLabel           iconLabel;
    protected ImageIcon        icon;
    protected JCheckBox        isNotCheckbox;
    protected JComboBox        operatorCBX;
    protected JTextField       criteria;
    protected MultiStateIconButon sortCheckbox;
    protected JCheckBox        isDisplayedCkbx;
    
    protected FieldQRI         fieldQRI;
    protected SpQueryField     queryField = null;
    protected DBFieldInfo      field      = null;
    
    protected FormValidator    validator;
    
    protected QueryFieldPanel  thisItem;
    
    protected String[] labelStrs   = {" ", "Field", "Not", "Operator", "Criteria", "Sort", "Display", " ", " "};
    protected String[] comparators;
    
    /**
     * Constructor.
     * @param fieldName the field Name
     * @param icon the icon to use once it is mapped
     */
    public QueryFieldPanel(final QueryBldrPane queryBldrPane,
                           final FieldQRI      fieldQRI, 
                           final IconManager.IconSize iconSize,
                           final String        columnDefStr,
                           final JButton       saveBtn,
                           final SpQueryField  queryField)
    {
        this.queryBldrPane = queryBldrPane;
        this.fieldQRI      = fieldQRI;
        this.field         = fieldQRI != null ? fieldQRI.getFieldInfo() : null;
        this.columnDefStr  = columnDefStr;
        
        thisItem = this;
        
        validator = new FormValidator(null);
        validator.addEnableItem(saveBtn);
        validator.setEnabled(true);
        
        boolean createAsHeader = StringUtils.isEmpty(columnDefStr);
        
        int[] widths = buildControlLayout(iconSize, createAsHeader);
        if (createAsHeader)
        {
            removeAll();
            buildLabelLayout(widths);
            queryBldrPane.setColumnDefStr(this.columnDefStr);
        }
        
        setQueryField(queryField);
    }
    
    public void updateQueryField()
    {
        if (queryField != null)
        {
//            queryField.setEndOper(oper);
//            queryField.setEndValue(endValue);
//            queryField.setFieldName(fieldName);
//          queryField.setOperEnd(operEnd);
            
            queryField.setIsDisplay(isDisplayedCkbx.isSelected());
            queryField.setIsNot(isNotCheckbox.isSelected());
            if (validator.hasChanged() && queryField.getSpQueryFieldId() != null)
            {
                FormHelper.updateLastEdittedInfo(queryField);
            }
            
            queryField.setSortType((byte)sortCheckbox.getState());
            queryField.setOperStart((byte)operatorCBX.getSelectedIndex());
            queryField.setStartValue(criteria.getText());
            
            Vector<Integer> idList = new Vector<Integer>();
            BaseQRI         parent  = fieldQRI.getParent();
            
            while (parent != null)
            {
                if (parent instanceof TableQRI)
                {
                    idList.add(((TableQRI)parent).getTableInfo().getTableId());
                }
                parent = parent.getParent();
            }
            
            StringBuilder tablesIds = new StringBuilder();
            for (int i=idList.size()-1;i>=0;i--)
            {
                if (tablesIds.length() > 0) tablesIds.append(',');
                tablesIds.append(idList.get(i));
            }
            /*do
            {
                if (tablesIds.length() > 0) tablesIds.append(',');
                tablesIds.append(idStack.pop());
                
            } while (idStack.size() > 0);*/
            
            log.debug(tablesIds.toString());
            queryField.setTableList(tablesIds.toString());
            
        } else
        {
            log.error("QueryField is null!");
        }
    }
    
    /**
     * @return the queryField
     */
    public SpQueryField getQueryField()
    {
        return queryField;
    }

    /**
     * @param queryField the queryField to set
     */
    private void setQueryField(SpQueryField queryField)
    {
        this.queryField = queryField;
        
        if (queryField != null)
        {
            if (queryField.getSpQueryFieldId() != null)
            {
                isNotCheckbox.setSelected(queryField.getIsNot());
                operatorCBX.setSelectedIndex(queryField.getOperStart());
                criteria.setText(queryField.getStartValue());
                sortCheckbox.setState(queryField.getSortType());
                isDisplayedCkbx.setSelected(queryField.getIsDisplay());
                validator.setHasChanged(false);
                
            } else
            {
                validator.reset(true); // tells it it is a new data object
                validator.setHasChanged(true);
    
            }
            validator.validateForm();
            validator.wasValidated(null);
        }
    }
    
    /**
     * 
     */
    public void resetValidator()
    {
        validator.reset(true);
    }

    /**
     * @param classObj
     * @return
     */
    protected String[] getComparatorListForClass(final Class<?> classObj)
    {
        if (classObj == String.class)
        {
            return new String[] {"Like", "="};
        }
        // else
        return new String[] {"=", ">", "<", ">=", "<="};
    }
    
    /**
     * @return
     */
    public String getCriteriaFormula()
    {
        String criteriaStr = criteria.getText();
        if (StringUtils.isNotEmpty(criteriaStr))
        {
            StringBuilder str  = new StringBuilder();
            String operStr     = operatorCBX.getSelectedItem().toString();
            
            //System.out.println(fieldQRI.getFieldInfo().getDataClass().getSimpleName());
            if (fieldQRI.getFieldInfo().getDataClass() == String.class)
            {
                if (operStr.equals("Like"))
                {
                    //criteriaStr = "'%" + criteriaStr + "%'";
                    criteriaStr = "'" + criteriaStr + "'";
                } else
                {
                    criteriaStr = "'" + criteriaStr + "'";
                }
            }
            if (criteriaStr.length() > 0)
            {
                TableTree parentTree = fieldQRI.getParent().getTableTree();
                str.append(parentTree.getAbbrev() + '.');
                str.append(getFieldInfo().getName());
                str.append(' ');
                if (operStr.equals("="))
                {
                    str.append(isNotCheckbox.isSelected() ? "!" : "");
                    str.append(operStr);
                    
                } else
                {
                    str.append(isNotCheckbox.isSelected() ? "NOT" : "");
                    str.append(' ');
                    str.append(operStr);
                }
                str.append(' ');
                str.append(criteriaStr);
                return str.toString();
            }
        }
        return null;
    }
    
    /**
     * @return the fieldQRI
     */
    public FieldQRI getFieldQRI()
    {
        return fieldQRI;
    }
    
    protected JTextField createTextField()
    {
        ValTextField textField = new ValTextField();
        textField.setRequired(false);
        validator.hookupTextField(textField, "1", false, UIValidator.Type.Changed, "", true);
        return textField;
    }
    
    protected JCheckBox createCheckBox(final String id)
    {
        ValCheckBox checkbox = new ValCheckBox("", false, false);
        DataChangeNotifier dcn = validator.createDataChangeNotifer(id, checkbox, null);
        checkbox.addActionListener(dcn);
        return checkbox;
    }
    
    protected JComboBox createComboBox(String[] items)
    {
        ValComboBox cbx = new ValComboBox(items, false);
        DataChangeNotifier dcn = validator.hookupComponent(cbx, "cbx",  UIValidator.Type.Changed, "", true);
        cbx.getComboBox().addActionListener(dcn);
        return cbx.getComboBox();
    }

    /**
     * @param iconSize
     * @param returnWidths
     * @return
     */
    protected int[] buildControlLayout(final IconManager.IconSize iconSize, final boolean returnWidths)
    {
        comparators = new String[SpQueryField.OperatorType.values().length];
        int inx = 0;
        for (SpQueryField.OperatorType op : SpQueryField.OperatorType.values())
        {
            comparators[inx++] = SpQueryField.OperatorType.getString(op.getOrdinal());
        }
        
        iconLabel     = new JLabel(icon);
        fieldLabel    = new JLabel(field.getTitle());
        isNotCheckbox = createCheckBox("isNotCheckbox");
        operatorCBX   = createComboBox(comparators);
        criteria      = createTextField();
        sortCheckbox  = new MultiStateIconButon(new ImageIcon[] {
                            IconManager.getImage("GrayDot",   IconManager.IconSize.Std16),
                            IconManager.getImage("UpArrow",   IconManager.IconSize.Std16),
                            IconManager.getImage("DownArrow", IconManager.IconSize.Std16)});
        //sortCheckbox.setMargin(new Insets(2,2,2,2));
        //sortCheckbox.setBorder(BorderFactory.createLineBorder(new Color(225,225,225)));
        isDisplayedCkbx = createCheckBox("isDisplayedCkbx");
        closeBtn        = new JLabel(IconManager.getIcon("Close"));
        
        //                       0           1           2              3           4           5             6              7
        JComponent[] comps = {iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria, sortCheckbox, isDisplayedCkbx, closeBtn, null};

        StringBuilder sb = new StringBuilder();
        if (columnDefStr == null)
        {
            for (int i=0;i<comps.length;i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (i == 2 || i == 3 || i == 6) sb.append("c:");
                sb.append("p");
                if (i == 4) sb.append(":g");
                sb.append(",4px");
            }
        } else
        {
            sb.append(columnDefStr);
        }
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout(sb.toString(), "p"), this);
        CellConstraints cc      = new CellConstraints();

        int col = 1;
        for (JComponent comp : comps)
        {
            if (comp != null)
            {
                builder.add(comp, cc.xy(col, 1));
            }
            col += 2;
        }

        icon = IconManager.getIcon(field.getTableInfo().getTitle(), iconSize);
        setIcon(icon);
        isDisplayedCkbx.setSelected(true);
        
        closeBtn.addMouseListener(new MouseAdapter() 
        {
            public void mousePressed(MouseEvent e) 
            {
                queryBldrPane.removeQueryFieldItem((QueryFieldPanel)((JComponent)e.getSource()).getParent());
            }
        });
        
        validate();
        doLayout();
        
        int[] widths = null;
        if (returnWidths)
        {
            widths = new int[comps.length];
            for (int i=0;i<comps.length;i++)
            {
                widths[i] = comps[i] != null ? comps[i].getSize().width : 0;
            }
            widths[0] = iconSize.size();
            widths[1] = 200;
        }
        return widths;
    }
    
    /**
     * @param iconSize
     * @param widths
     */
    protected void buildLabelLayout(final int[] widths)
    {
        
        StringBuilder sb     = new StringBuilder();
        JLabel[] labels      = new JLabel[labelStrs.length];
        int[]    labelWidths = new int[labelStrs.length];
        for (int i=0;i<labels.length;i++)
        {
            labels[i] = new JLabel(labelStrs[i], SwingConstants.CENTER);
            labelWidths[i] = Math.max(widths[i], labels[i].getPreferredSize().width);
            System.out.println(labelStrs[i]+"  "+labelWidths[i]);
        }
        
        for (int i=0;i<labels.length;i++)
        {
            sb.append(i == 0 ? "" : ",");
            if (i == 2 || i == 3 || i == 6) sb.append("c:");
            sb.append("max(");
            sb.append(labelWidths[i]);
            sb.append(";p)");
            if (i == 4) sb.append(":g");
            sb.append(",4px");
        }

        System.out.println(sb.toString());
        columnDefStr = sb.toString();
        
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout(sb.toString(), "p"), this);
        CellConstraints cc      = new CellConstraints();


        int x = 1;
        for (JLabel label : labels)
        {
            builder.add(label, cc.xy(x, 1));
            x += 2;
        }          
    }
    
    /**
     * Split apart the name keying on upper case
     * @param nameToFix the name of the field
     * @return the split apart name
     */
    protected String fixName(final String nameToFix)
    {
        StringBuilder s = new StringBuilder();
        for (int i=0;i<nameToFix.length();i++)
        {
            if (i == 0) 
            {
                s.append(Character.toUpperCase(nameToFix.charAt(i)));
            } else
            {
                char c = nameToFix.charAt(i);
                if (Character.isUpperCase(c))
                {
                    s.append(' ');
                }
                s.append(c);
            }
        }
        return s.toString();  
    }


    /**
     * @param icon
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon == null ? blankIcon : icon;
        iconLabel.setIcon(this.icon);
    }

    /**
     * @return the TableInfo object
     */
    public DBFieldInfo getFieldInfo()
    {
        return field;
    }

    /**
     * Returns the field name.
     * @return the field name.
     */
    public String getFieldName()
    {
        return fieldLabel.getText();
    }
    
    public boolean isForDisplay()
    {
        return isDisplayedCkbx.isSelected();
    }
}