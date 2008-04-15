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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
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
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
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

    protected QueryFieldPanelContainerIFace    ownerQuery;
    protected String           columnDefStr;
    protected ImageIcon        blankIcon = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    
    protected boolean          hasFocus      = false;
    protected Color            bgColor       = null;
    protected JLabel           fieldLabel;
    protected boolean          labelQualified = false; 
    protected JLabel           closeBtn;
    protected JLabel           iconLabel;
    protected ImageIcon        icon;
    protected JCheckBox        isNotCheckbox;
    protected JComboBox        operatorCBX;
    protected JTextField       criteria;
    protected JComboBox        criteriaList; 
    protected MultiStateIconButon sortCheckbox;
    protected JCheckBox        isDisplayedCkbx;
    protected JCheckBox        isPromptCkbx;
    
    protected FieldQRI         fieldQRI;
    protected SpQueryField     queryField = null;
    
    protected FormValidator    validator;
    
    protected QueryFieldPanel  thisItem;
    
    protected String[] labelStrs;
    protected String[] comparators;
    
//    public static final DataFlavor    QUERY_FLD_PANE_FLAVOR = new DataFlavor(DroppableTaskPane.class, "QueryFldPane");
//    protected List<DataFlavor>        flavors         = new ArrayList<DataFlavor>(); 
//    GhostMouseInputAdapter            mouseDropAdapter; 
//    protected BufferedImage           shadowBuffer        = null;
//    protected BufferedImage           buffer              = null;
//    protected boolean                 generateImgBuf      = true;    
//    protected static final int SHADOW_SIZE = 10;
//    protected boolean isOver = false;
    protected boolean selected = false;
    
//    protected Border inactiveBorder = BorderFactory.createEmptyBorder(1,1,1,1);
//    protected Border focusBorder    = BorderFactory.createLineBorder(Color.BLACK, 1);
    /**
     * Constructor.
     * @param fieldName the field Name
     * @param icon the icon to use once it is mapped
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
                           final FieldQRI      fieldQRI, 
                           final IconManager.IconSize iconSize,
                           final String        columnDefStr,
                           final Component       saveBtn,
                           final SpQueryField  queryField)
    {        
        this.ownerQuery = ownerQuery;
        if (this.ownerQuery.isPromptMode())
        {
            labelStrs = new String[]{ " ",
                    UIRegistry.getResourceString("QB_FIELD"), UIRegistry.getResourceString("QB_NOT"),
                    UIRegistry.getResourceString("QB_OPERATOR"),
                    UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"),
                    //UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), 
                    //" ", " " 
                    };
        }
        else
        {
            labelStrs = new String[]{ " ",
                    /*UIRegistry.getResourceString("QB_FIELD")*/" ", UIRegistry.getResourceString("QB_NOT"),
                    UIRegistry.getResourceString("QB_OPERATOR"),
                    UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"),
                    UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), " ", " " };
        }
        this.fieldQRI      = fieldQRI;
        this.columnDefStr  = columnDefStr;
        
        thisItem = this;
        
        validator = new FormValidator(null);
        if (saveBtn != null)
        {
            validator.addEnableItem(saveBtn, FormValidator.EnableType.ValidAndChangedItems);
        }
        validator.setEnabled(true);
        
        boolean createAsHeader = StringUtils.isEmpty(columnDefStr);
        
        int[] widths = buildControlLayout(iconSize, createAsHeader);
        if (createAsHeader)
        {
            removeAll();
            buildLabelLayout(widths);
            ownerQuery.setColumnDefStr(this.columnDefStr);
        }
        
        setQueryField(queryField);
        if (!createAsHeader && getFieldInfo() != null /*this means relationships and tree levels won't get qualified*/)
        {
            setToolTipText(getQualifiedLabel(fieldQRI.getTableTree(), true));
        }
}
    
    public void updateQueryField()
    {
        if (queryField != null && !ownerQuery.isPromptMode())
        {
            queryField.setIsDisplay(isDisplayedCkbx.isSelected());
            queryField.setIsPrompt(isPromptCkbx.isSelected());
            queryField.setIsNot(isNotCheckbox.isSelected());
            if (validator.hasChanged() && queryField.getSpQueryFieldId() != null)
            {
                FormHelper.updateLastEdittedInfo(queryField);
            }
            
            queryField.setSortType((byte)sortCheckbox.getState());
            queryField.setOperStart((byte)operatorCBX.getSelectedIndex());
            queryField.setStartValue(criteria.getText());
            queryField.setColumnAlias(this.getLabel());
            queryField.setContextTableIdent(fieldQRI.getTableInfo().getTableId());
            queryField.setIsRelFld(fieldQRI instanceof RelQRI);
            
            Vector<Integer> idList = new Vector<Integer>();
            TableQRI parent = fieldQRI.getTable();
            while (parent != null)
            {
                idList.add(parent.getTableInfo().getTableId());
                parent = parent.getTableTree().getParent().getTableQRI();
            }
            
            StringBuilder tablesIds = new StringBuilder();
            for (int i=idList.size()-1;i>=0;i--)
            {
                if (tablesIds.length() > 0) tablesIds.append(',');
                tablesIds.append(idList.get(i));
            }
            log.debug(tablesIds.toString());
            queryField.setTableList(tablesIds.toString());
            
        } else
        {
            log.error("QueryField is null or ownerQuery is prompt only. Unable to update database object.");
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
                if (!ownerQuery.isPromptMode())
                {
                    isDisplayedCkbx.setSelected(queryField.getIsDisplay());
                    isPromptCkbx.setSelected(queryField.getIsPrompt() == null ? true : queryField.getIsPrompt());
                }
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
     * @param field
     * @return list of comparators appropriate for field.
     */
    protected String[] getComparatorList(final FieldQRI field)
    {
        if (fieldQRI ==  null)
        {
            return new String[]{"none of the above"};
        }
        //CatalogNumber needs special treatment - works better as a number.
        //And other fields? Not sure how to tell. Maybe the formatter?????
        if (field.getFieldInfo() != null && field.getFieldInfo().getName().equalsIgnoreCase("catalognumber") 
                && field.getTableInfo().getClassObj().equals(CollectionObject.class))
        {
            return getComparatorListForClass(Number.class);
        }
        //else
        return getComparatorListForClass(field.getDataClass());
    }
    
    /**
     * @param classObj
     * @return
     */
    protected String[] getComparatorListForClass(final Class<?> classObj)
    {
        if (classObj.equals(String.class))
        {
            return new String[] {SpQueryField.OperatorType.getString(SpQueryField.OperatorType.LIKE.getOrdinal()),
                    SpQueryField.OperatorType.getString(SpQueryField.OperatorType.EQUALS.getOrdinal())};
        }
        else if (classObj.equals(Boolean.class))
        {
            return new String[] {SpQueryField.OperatorType.getString(SpQueryField.OperatorType.DONTCARE.getOrdinal()),
                    SpQueryField.OperatorType.getString(SpQueryField.OperatorType.TRUE.getOrdinal()),
                    SpQueryField.OperatorType.getString(SpQueryField.OperatorType.FALSE.getOrdinal())};
        }
        // else
        return new String[] {SpQueryField.OperatorType.getString(SpQueryField.OperatorType.EQUALS.getOrdinal()),
                SpQueryField.OperatorType.getString(SpQueryField.OperatorType.GREATERTHAN.getOrdinal()),
                SpQueryField.OperatorType.getString(SpQueryField.OperatorType.LESSTHAN.getOrdinal()),
                SpQueryField.OperatorType.getString(SpQueryField.OperatorType.GREATERTHANEQUALS.getOrdinal()),
                SpQueryField.OperatorType.getString(SpQueryField.OperatorType.LESSTHANEQUALS.getOrdinal())};
    }
    
    /**
     * @return
    */
    public String getOrderSpec(int pos)
    {
        if (queryField.getSortType() == SpQueryField.SORT_NONE) { return null; }
        
        StringBuilder result = new StringBuilder();
        //result.append(String.valueOf(this.queryBldrPane.getFldPosition(this)+1));
        result.append(String.valueOf(pos));
        if (queryField.getSortType() == SpQueryField.SORT_DESC)
        {
            result.append(" DESC");
        }
        return result.toString();
    }
    
    protected boolean hasCriteria()
    {
        if (fieldQRI.getDataClass().equals(Boolean.class))
        {
            return !operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.getString(SpQueryField.OperatorType.DONTCARE.getOrdinal()));
        }
        return StringUtils.isNotEmpty(criteria.getText());
    }
    /**
     * @return
     */
    public String getCriteriaFormula(final TableAbbreviator ta)
    {
        String criteriaStr = criteria.getText();
        
        UIFieldFormatterIFace formatter = fieldQRI.getFormatter();
        if (formatter != null && formatter.isFromUIFormatter())
        {
            // XXX Passing in a string may not always work,
            // We might to convert to the actual type of data for that field.
            // i.e. convert it from a String to an Integer.
            criteriaStr = formatter.formatFromUI(criteriaStr).toString();
        }
            
        if (hasCriteria())
        {
            StringBuilder str  = new StringBuilder();
            String operStr     = operatorCBX.getSelectedItem().toString();
            
            //System.out.println(fieldQRI.getFieldInfo().getDataClass().getSimpleName());
            if (fieldQRI.getDataClass().equals(Boolean.class))
            {
                //kind of a goofy way to handle booleans but works without worrying about disabling/removing isNotCheckbox (and criteria)
                if (operStr.equals(SpQueryField.OperatorType.getString(SpQueryField.OperatorType.TRUE.getOrdinal())))
                {
                    criteriaStr = "true";
                }
                else
                {
                    criteriaStr = "false";
                }
                operStr = "=";
            }
            else if (fieldQRI.getDataClass().equals(String.class))
            {
                criteriaStr =  "'" + criteriaStr + "'";
            }
            else if (fieldQRI.getDataClass().equals(Calendar.class))
            {
                criteriaStr = "'" + criteriaStr + "'";
            }
            if (criteriaStr.length() > 0)
            {
                str.append(fieldQRI.getSQLFldSpec(ta, true) + " ");
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
    protected int[] buildControlLayout(final IconManager.IconSize iconSize,
                                       final boolean returnWidths)
    {

        FocusListener focusListener = new FocusListener()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
             */
            // @Override
            public void focusGained(FocusEvent e)
            {
                //Checking oppositeComponent to work around
                //weird behavior after addBtn is clicked which
                //causes top queryFieldPanel to be selected.
                if (ownerQuery.getAddBtn() != null && e.getOppositeComponent() != ownerQuery.getAddBtn())
                {
                    ownerQuery.selectQFP(QueryFieldPanel.this);
                }

            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
             */
            // @Override
            public void focusLost(FocusEvent e)
            {
                // nada
            }

        };
        comparators = getComparatorList(fieldQRI);
        iconLabel = new JLabel(icon);
        iconLabel.addFocusListener(focusListener);
        String fieldLabelText = fieldQRI != null ? fieldQRI.getTitle() : "WXYZ";
        if (fieldQRI instanceof RelQRI)
        {
            DBRelationshipInfo.RelationshipType relType = ((RelQRI)fieldQRI).getRelationshipInfo().getType();
            if (relType.equals(DBRelationshipInfo.RelationshipType.OneToMany) || 
                    relType.equals(DBRelationshipInfo.RelationshipType.ManyToMany))
            {
                fieldLabelText += " " + UIRegistry.getResourceString("QB_AGGREGATED");
            }
            else
            {
                fieldLabelText += " " + UIRegistry.getResourceString("QB_FORMATTED");
            }
                
        }
        fieldLabel = createLabel(fieldLabelText);
        fieldLabel.addFocusListener(focusListener);
        isNotCheckbox = createCheckBox("isNotCheckbox");
        isNotCheckbox.addFocusListener(focusListener);
        operatorCBX = createComboBox(comparators);
        operatorCBX.addFocusListener(focusListener);
        criteria = createTextField();
        criteria.addFocusListener(focusListener);
        sortCheckbox = new MultiStateIconButon(new ImageIcon[] {
                IconManager.getImage("GrayDot", IconManager.IconSize.Std16),
                IconManager.getImage("UpArrow", IconManager.IconSize.Std16),
                IconManager.getImage("DownArrow", IconManager.IconSize.Std16) });
        DataChangeNotifier dcn = validator.hookupComponent(sortCheckbox, "scb",
                UIValidator.Type.Changed, "", true);
        sortCheckbox.addFocusListener(focusListener);
        sortCheckbox.addActionListener(dcn);
        if (!this.ownerQuery.isPromptMode())
        {
            isDisplayedCkbx = createCheckBox("isDisplayedCkbx");
            isDisplayedCkbx.addFocusListener(focusListener);
            isPromptCkbx = createCheckBox("isPromptCkbx");
            isPromptCkbx.addFocusListener(focusListener);
            closeBtn = new JLabel(IconManager.getIcon("Close"));
        }
        else
        {
            isDisplayedCkbx = null;
            this.isPromptCkbx = null;
            this.closeBtn = null;
        }

        // 0 1 2 3 4 5 6 7 8
        JComponent[] comps = { iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, isDisplayedCkbx, isPromptCkbx, closeBtn, null };

        StringBuilder sb = new StringBuilder();
        if (columnDefStr == null)
        {
            for (int i = 0; i < comps.length; i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (i == 2 || i == 3 || i == 6 || i == 7)
                    sb.append("c:");
                sb.append("p");
                if (i == 4)
                    sb.append(":g");
                sb.append(",4px");
            }
        }
        else
        {
            sb.append(columnDefStr);
        }

        PanelBuilder builder = new PanelBuilder(new FormLayout("3px, " + sb.toString() + ", 3px", "3px, p, 3px"), this);
        CellConstraints cc = new CellConstraints();

        int col = 1;
        for (JComponent comp : comps)
        {
            if (comp != null)
            {
                builder.add(comp, cc.xy(col+1, 2));
            }
            col += 2;
        }

        if (fieldQRI != null)
        {
            icon = IconManager.getIcon(fieldQRI.getTableInfo().getTitle(), iconSize);
            setIcon(icon);
        }
        if (!ownerQuery.isPromptMode())
        {
            isDisplayedCkbx.setSelected(true);
            isPromptCkbx.setSelected(!(fieldQRI instanceof RelQRI));
            closeBtn.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    ownerQuery.removeQueryFieldItem((QueryFieldPanel) ((JComponent) e.getSource())
                            .getParent());
                }
            });
        }

        //for now
        boolean isRel = fieldQRI != null && fieldQRI instanceof RelQRI;
        isNotCheckbox.setVisible(!isRel);
        operatorCBX.setVisible(!isRel);
        criteria.setVisible(!isRel);
        this.sortCheckbox.setVisible(!isRel);
        if (!ownerQuery.isPromptMode())
        {
            isDisplayedCkbx.setVisible(!isRel);
            isPromptCkbx.setVisible(!isRel);
        }
        
        validate();
        doLayout();

        int[] widths = null;
        if (returnWidths)
        {
            widths = new int[comps.length];
            for (int i = 0; i < comps.length; i++)
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
            labels[i] = createLabel(labelStrs[i], SwingConstants.CENTER);
            labelWidths[i] = Math.max(widths[i], labels[i].getPreferredSize().width);
            //System.out.println(labelStrs[i]+"  "+labelWidths[i]);
        }

        for (int i=0;i<labels.length;i++)
        {
            sb.append(i == 0 ? "" : ",");
            if (i == 2 || i == 3 || i == 6 || i == 7) sb.append("c:");
            sb.append("max(");
            sb.append(labelWidths[i]);
            sb.append(";p)");
            if (i == 4) sb.append(":g");
            sb.append(",4px");
        }

        //System.out.println(sb.toString());
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
        return fieldQRI.getFieldInfo();
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
        return ownerQuery.isPromptMode() || isDisplayedCkbx.isSelected();
    }
    
    public String getLabel()
    {
        return this.fieldLabel.getText();
    }

    /**
     * @return the labelQualified
     */
    public boolean isLabelQualified()
    {
        return labelQualified;
    }

    public String qualifyLabel(final List<String> otherLabels, final boolean unQualify)
    {
        boolean needToQualify;
        List<String> labels;
        //the following block is not currently used, but keeping it here in case the current strategy
        //doesn't work out.
        if (otherLabels == null)
        {
            needToQualify = false;
            labels = new ArrayList<String>(ownerQuery.getFields()-1);
            for (int i = 0; i < this.ownerQuery.getFields(); i++)
            {
                QueryFieldPanel p = ownerQuery.getField(i);
                if (this != p)
                {
                    labels.add(p.getLabel());
                    if (p.getFieldInfo().getTitle().equals(getFieldInfo().getTitle()))
                    {
                        needToQualify = true;
                    }
                }
            }
        }
        else
        {
            needToQualify = !unQualify;
            labels = otherLabels;
        }
        
        if (needToQualify)
        {
            String newLabel = getFieldInfo().getTitle();
            TableTree parent = fieldQRI.getTableTree();
            int checkParent = 1;
            do
            {
                newLabel = getQualifiedLabel(parent, checkParent-- > 0);
                parent = parent.getParent();
            } while (parent != null && labels.indexOf(newLabel) != -1);
            
            labelQualified = true;
            fieldLabel.setText(newLabel);
        }
        else
        {
            labelQualified = false;
            fieldLabel.setText(getFieldInfo().getTitle());
        }
        return fieldLabel.getText();
    }

    protected String getQualifiedLabel(final TableTree parent, final boolean checkParent)
    {
        TableTree reParent = parent;
        if (checkParent && reParent.getTableInfo().getClassObj().equals(Agent.class)
                && reParent.getParent().getTableQRI() != null)
        // agent (and what others??) generally offers no informative distinguishing info
        {
            reParent = reParent.getParent();
        }
        return reParent.getTableQRI().getTitle() + "/" + getFieldInfo().getTitle();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        // TODO Auto-generated method stub
        super.paint(g);

        if (selected)
        {
            //this block was copied from RolloverCommand.paintComp()
            
            g.setColor(RolloverCommand.getActiveColor());
            Insets insets = getInsets();
            insets.set(1, 1, 1, 1);
            Dimension size = getSize();
            Graphics2D g2d = (Graphics2D) g;
            g2d
                    .setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(insets.left, insets.top,
                    size.width - insets.right - insets.left, size.height - insets.bottom
                            - insets.top, 10, 10);
            g2d.draw(rr);
            rr = new RoundRectangle2D.Double(insets.left + 1, insets.top + 1, size.width
                    - insets.right - insets.left - 2, size.height - insets.bottom - insets.top - 2,
                    10, 10);
            g2d.draw(rr);
        }
    }

    /**
     * @return the selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}