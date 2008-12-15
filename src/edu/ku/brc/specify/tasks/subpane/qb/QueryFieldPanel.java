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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
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

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DateConverter;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    protected JComponent       criteria;
    //protected JComboBox        criteriaList; 
    protected MultiStateIconButon sortCheckbox;
    protected JCheckBox        isDisplayedCkbx;
    protected JCheckBox        isPromptCkbx;
    protected JCheckBox        isEnforcedCkbx; 
    
    protected FieldQRI         fieldQRI;
    protected SpQueryField     queryField = null;
    protected PickListDBAdapterIFace pickList = null;
    
    protected FormValidator    validator;
    
    protected QueryFieldPanel  thisItem;
    
    protected String[] labelStrs;
    protected SpQueryField.OperatorType[] comparators;

    protected DateConverter                             dateConverter = null;
    
    protected boolean selected = false;
    

    
    protected PickListDBAdapterIFace buildPickList()
    {
        if (fieldQRI != null && fieldQRI.getTableInfo() != null && fieldQRI.getFieldInfo() != null) 
        {
            //XXX unfortunately this doesn't work because currently picklist defs are only setup via form view defs
            if (StringUtils.isNotEmpty(fieldQRI.getFieldInfo().getPickListName()))
            {
                //pickList = ((edu.ku.brc.specify.ui.db.PickListDBAdapterFactory)PickListDBAdapterFactory.getInstance()).create(fieldQRI.getFieldInfo().getPickListName(), false);
                return PickListDBAdapterFactory.getInstance().create(fieldQRI.getFieldInfo().getPickListName(), false);
            }
            //else
            return RecordTypeCodeBuilder.getTypeCode(fieldQRI.getFieldInfo());
        }
        return null;
    }
    
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
                    UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), getResourceString("QB_ALWAYS_ENFORCE"), " ", " " };
        }
        
        this.fieldQRI      = fieldQRI;
        if (fieldQRI != null && (fieldQRI.getDataClass().equals(Calendar.class) || fieldQRI.getDataClass().equals(java.sql.Timestamp.class)))
        {
            dateConverter = new DateConverter();
        }
        
        pickList = buildPickList();
        
        this.columnDefStr  = columnDefStr;
        
        thisItem = this;
        
        validator = new FormValidator(null);
        if (saveBtn != null)
        {
            validator.addEnableItem(saveBtn, FormValidator.EnableType.ValidAndChangedItems);
        }
        validator.setEnabled(true);
        
        boolean createAsHeader = StringUtils.isEmpty(columnDefStr);
        
        int[] widths = buildControlLayout(iconSize, createAsHeader, saveBtn);
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
        updateQueryField(queryField);
    }
    
    protected String getCriteriaText(final boolean useValues)
    {
        if (criteria instanceof JTextField)
        {
            return ((JTextField)criteria).getText();
        }
        if (criteria instanceof PickListCriteriaCombo)
        {
            return ((PickListCriteriaCombo)criteria).getText(useValues);
        }
        
        throw new RuntimeException("Unrecognized criteria component: " + criteria.getClass());
    }
    
    public void updateQueryField(final SpQueryField qField)
    {
        if (qField != null && !ownerQuery.isPromptMode())
        {
            qField.setIsDisplay(isDisplayedCkbx.isSelected());
            qField.setIsPrompt(isPromptCkbx.isSelected());
            qField.setAlwaysFilter(isEnforcedCkbx.isSelected());
            qField.setIsNot(isNotCheckbox.isSelected());
            if (validator.hasChanged() && qField.getSpQueryFieldId() != null)
            {
                FormHelper.updateLastEdittedInfo(qField);
            }
            
            qField.setSortType((byte)sortCheckbox.getState());
            qField.setOperStart((byte)operatorCBX.getSelectedIndex());
            qField.setStartValue(getCriteriaText(false));
            String lbl = this.getLabel();
            if (fieldQRI instanceof RelQRI)
            {
                lbl = RelQRI.stripDescriptiveStuff(lbl);    
            }
            qField.setContextTableIdent(fieldQRI.getTableInfo().getTableId());
            qField.setColumnAliasTitle(lbl);
            qField.setIsRelFld(fieldQRI instanceof RelQRI);
            
            Vector<Integer> idList = new Vector<Integer>();
            TableQRI parent = fieldQRI.getTable();
            while (parent != null)
            {
                idList.add(parent.getTableInfo().getTableId());
                parent = parent.getTableTree().getParent().getTableQRI();
            }
            
            String tablesIds = fieldQRI.getTableTree().getPathFromRoot();
            log.debug(tablesIds);
            qField.setTableList(tablesIds);
            qField.setStringId(getStringId());
            
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

    protected void setCriteriaText(final String text)
    {
        if (criteria instanceof JTextField)
        {
            ((JTextField)criteria).setText(text);
        }
        else if (criteria instanceof PickListCriteriaCombo)
        {
            ((PickListCriteriaCombo)criteria).setSelections(text);
        }
        else
        {
            throw new RuntimeException("Unrecognized criteria component: " + criteria.getClass());
        }
        
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
                setCriteriaText(queryField.getStartValue());
                sortCheckbox.setState(queryField.getSortType());
                if (!ownerQuery.isPromptMode())
                {
                    isDisplayedCkbx.setSelected(queryField.getIsDisplay());
                    isPromptCkbx.setSelected(queryField.getIsPrompt() == null ? true : queryField.getIsPrompt());
                    isEnforcedCkbx.setSelected(queryField.getAlwaysFilter() == null ? true : queryField.getAlwaysFilter());
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
    protected SpQueryField.OperatorType[] getComparatorList(final FieldQRI field)
    {
        if (fieldQRI ==  null)
        {
            return new SpQueryField.OperatorType[]{};
        }
        if (pickList != null)
        {
            return new SpQueryField.OperatorType[] {
                    SpQueryField.OperatorType.EQUALS,
                    SpQueryField.OperatorType.IN,
                    SpQueryField.OperatorType.EMPTY};
        }
        if (fieldQRI instanceof TreeLevelQRI)
        {
            return new SpQueryField.OperatorType[] {
                SpQueryField.OperatorType.EQUALS,
                SpQueryField.OperatorType.IN,
                SpQueryField.OperatorType.EMPTY};
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
    protected SpQueryField.OperatorType[] getComparatorListForClass(final Class<?> classObj)
    {
        if (classObj.equals(String.class))
        {
            return new SpQueryField.OperatorType[] {SpQueryField.OperatorType.LIKE,
                    SpQueryField.OperatorType.EQUALS,
                    SpQueryField.OperatorType.IN,
                    SpQueryField.OperatorType.EMPTY};
        }
        else if (classObj.equals(Boolean.class))
        {
            return new SpQueryField.OperatorType[] {SpQueryField.OperatorType.DONTCARE,
                    SpQueryField.OperatorType.TRUE,
                    SpQueryField.OperatorType.FALSE,
                    SpQueryField.OperatorType.EMPTY};
        }
        else if (classObj.equals(java.sql.Timestamp.class))
        {
            return new SpQueryField.OperatorType[] {SpQueryField.OperatorType.GREATERTHAN,
                    SpQueryField.OperatorType.LESSTHAN,
                    SpQueryField.OperatorType.BETWEEN,
                    SpQueryField.OperatorType.EMPTY};
        }
        // else
        return new SpQueryField.OperatorType[] {SpQueryField.OperatorType.EQUALS,
                SpQueryField.OperatorType.GREATERTHAN,
                SpQueryField.OperatorType.LESSTHAN,
                SpQueryField.OperatorType.GREATERTHANEQUALS,
                SpQueryField.OperatorType.LESSTHANEQUALS,
                SpQueryField.OperatorType.BETWEEN,
                SpQueryField.OperatorType.IN,
                SpQueryField.OperatorType.EMPTY};
    }
    
    /**
     * @return
    */
    public SortElement getOrderSpec(int pos)
    {
        Byte sortType;
        if (ownerQuery.isPromptMode())
        {
            sortType = (byte)sortCheckbox.getState();
        }
        else
        {
            sortType = queryField.getSortType();
        }
        if (sortType.equals(SpQueryField.SORT_NONE)) { return null; }
        
        int direction = sortType.equals(SpQueryField.SORT_ASC) ? SortElement.ASCENDING : SortElement.DESCENDING;
        return new SortElement(pos, direction);
    }
    
    protected boolean hasCriteria()
    {
        if (fieldQRI.getDataClass().equals(Boolean.class))
        {
            return !operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.DONTCARE);
        }
        return StringUtils.isNotEmpty(getCriteriaText(true).trim());
    }
    
    /**
     * @param criteriaEntry - String of comma-delimited entries
     * @return Array of formatted criteria
     * @throws ParseException
     */
    protected Object[] parseCriteria(final String criteriaEntry) throws ParseException
    {
        String[] raw;
        
        if (operatorCBX.getSelectedItem() == SpQueryField.OperatorType.BETWEEN 
                || operatorCBX.getSelectedItem() == SpQueryField.OperatorType.IN)
        {
            raw = criteriaEntry.split(",");
        }
        else
        {
            raw = new String[1];
            raw[0] = criteriaEntry;
        }
        
        
        if (operatorCBX.getSelectedItem() == SpQueryField.OperatorType.BETWEEN)
        {
            if (raw.length != 2)
            {
                throw new ParseException(getLabel() + " - " + UIRegistry.getResourceString("QB_INVALID_CRITERIA"), -1);
            }
        }
        else if (operatorCBX.getSelectedItem() != SpQueryField.OperatorType.IN)
        {
            if (raw.length != 1)
            {
                throw new ParseException(getLabel() + " - " + UIRegistry.getResourceString("QB_INVALID_CRITERIA"), -1);
            }
        }
        
        UIFieldFormatterIFace formatter = fieldQRI.getFormatter();
        Object[] result = new String[raw.length];
        for (int e=0; e<raw.length; e++)
        {
            try
            {
                result[e] = formatter != null ? formatter.formatFromUI(raw[e].trim()) : raw[e].trim();
            }
            catch (Exception ex)
            {
                throw new ParseException(getLabel() + " - " 
                        + String.format(UIRegistry.getResourceString("QB_PARSE_ERROR"), ex.getLocalizedMessage()), -1);
            }
        }
        return result;
    }
    
    
    /**
     * @param escapee
     * @param escaper
     * WRONG:@return escapee with occurrences of escapee preceded by '\'. Pike's Peak -> Pike\'s Peak
     * @return escapee with occurrences of escapee doubled. Pike's Peak -> Pike''s Peak
     * 
     * This actually only works for "'". 
     * Hibernate (but not MySQL) complains when % and \' are both contained in a criteriummmm
     * 
     * Too bad if the escaper is already escaped.
     */
    protected Object escape(final Object escapee, final char escaper)
    {
        //XXX may be MySQL -specific?
        if (escaper == ' ')
        {
            return escapee;
        }
        if (!(escapee instanceof String))
        {
            throw new RuntimeException("Escapee is not a String!");
        }
        String escapeeStr = (String)escapee;
        StringBuilder result = new StringBuilder();
        for (int c = 0; c < escapeeStr.length(); c++)
        {
            if (escapeeStr.charAt(c) == escaper)
            {
                result.append(escaper);
            }
            result.append(escapeeStr.charAt(c));
        }
        return result.toString();
    }
    /**
     * @param criteriaObjs
     * @param operatorStr
     * @param quote - if true then items will be surrounded with single quotes.
     * @return comma-delimited list of items in criteriaObjs.
     */
    protected String concatCriteria(final Object[] criteriaObjs, final String operatorStr, final boolean quote)
    {
        //XXX '%' as wildcard may be db vendor -specific??
        
        char quoteStr = quote ? '\'' : ' ';
        String result = quoteStr + escape(criteriaObjs[0], quoteStr).toString() + quoteStr;
        if (SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.LIKE.getOrdinal())
        {
            //for Specify 5 compatibility...?
            //replaced unescaped '*' with '%'
            if (result.contains("*"))
            {
                //grrrrrrrr
                StringBuilder newResult = new StringBuilder();
                int skip = -1;
                for (int s = 0; s < result.length(); s++)
                {
                  
                    if (skip != s && result.charAt(s) == '\\')
                    {
                        skip = s+1;
                    }
                    if (skip != s && result.charAt(s) == '*')
                    {
                        newResult.append('%');
                    }
                    else
                    {
                        newResult.append(result.charAt(s));
                    }
                    if (skip == s)
                    {
                        skip = -1;
                    }
                }
                result = newResult.toString();
            }
            
            boolean unEscapedWildcard = false;
            boolean skip = false;
            int s = 0;
            while (!unEscapedWildcard && s < result.length())
            {
                if (skip)
                {
                    skip = false;
                }
                else if (result.charAt(s) == '\\')
                {
                    skip = true;
                }
                else if (result.charAt(s) == '%')
                {
                    unEscapedWildcard = true;
                }
                s++;
            }
            
            if (!unEscapedWildcard)
            {
                //if user didn't purposely include a wildcard then add them
                result = quoteStr + "%" + result.substring(1, result.length()-1) + "%" + quoteStr;
            }

        }
        else if (SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.BETWEEN.getOrdinal())
        {
            result += " and " + quoteStr + escape(criteriaObjs[1], quoteStr) + quoteStr;
        }
        else if (SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.IN.getOrdinal())
        {
            for (int p = 1; p < criteriaObjs.length; p++)
            {
                result += ", " + quoteStr + escape(criteriaObjs[p], quoteStr) + quoteStr; 
            }
            result = "(" + result + ")";
        }
        return result;
    }
    /**
     * @return
     */
    public String getCriteriaFormula(final TableAbbreviator ta,
                                     final List<Pair<String, Object>> paramList)
            throws ParseException
    {
        if (operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.EMPTY))
        {
            return fieldQRI.getSQLFldSpec(ta, true) + (isNotCheckbox.isSelected() ? " is not " : " is ") + "null";
        }
        
        if (hasCriteria())
        {
            Object[] criteriaStrs = parseCriteria(getCriteriaText(true).trim());
            String criteriaFormula = "";
            String operStr = operatorCBX.getSelectedItem().toString();
            if (!(criteriaStrs[0] instanceof String))
            {
                //XXX - If the field has a formatter and it returned non-String data
                // then assume all parsing and conversion has been accomplished??
                //(hopefully this will never occur)
                log.info(fieldQRI.getFieldInfo() + ": formatter returned non-string data.");
                criteriaFormula = concatCriteria(criteriaStrs, operStr, false);
            }
            else
            {
                if (fieldQRI.getDataClass().equals(Boolean.class))
                {
                    if (operStr.equals(SpQueryField.OperatorType
                            .getString(SpQueryField.OperatorType.TRUE.getOrdinal())))
                    {
                        criteriaFormula = "true";
                    }
                    else
                    {
                        criteriaFormula = "false";
                    }
                    operStr = "=";
                }
                else if (fieldQRI.getDataClass().equals(String.class))
                {
                    criteriaFormula = concatCriteria(criteriaStrs, operStr, true);
                }
                else if (fieldQRI.getDataClass().equals(Calendar.class) || fieldQRI.getDataClass().equals(java.sql.Timestamp.class))
                {
                    for (int p = 0; p < criteriaStrs.length; p++)
                    {
                        String paramName = "spparam" + p;
                        try
                        {
                            Object arg = dateConverter.convert((String)criteriaStrs[p]);
                            if (fieldQRI.getDataClass().equals(java.sql.Timestamp.class))
                            {
                                arg = new java.sql.Timestamp(((Calendar)arg).getTimeInMillis());
                            }
                            paramList.add(new Pair<String, Object>(paramName, arg));
                        }
                        catch (ParseException ex)
                        {
                            throw new ParseException(getLabel()
                                    + " - "
                                    + String.format(UIRegistry.getResourceString("QB_PARSE_ERROR"),
                                            ex.getLocalizedMessage()), -1);
                        }
                        if (p > 0)
                        {
                            if (operatorCBX.getSelectedItem() == SpQueryField.OperatorType.BETWEEN)
                            {
                                criteriaFormula += " and ";
                            }
                            else
                            {
                                criteriaFormula += ", ";
                            }
                        }
                        criteriaFormula += ":" + paramName;
                    }
                    if (SpQueryField.OperatorType.getOrdForName(operStr) == SpQueryField.OperatorType.IN
                            .getOrdinal())
                    {
                        criteriaFormula = "(" + criteriaFormula + ")";
                    }
                }
                else if (Number.class.isAssignableFrom(fieldQRI.getDataClass()))
                {
                    Constructor<?> tester;
                    try
                    {
                        tester = fieldQRI.getDataClass().getConstructor(String.class);
                        for (int s = 0; s < criteriaStrs.length; s++)
                        {
                            tester.newInstance((String)criteriaStrs[s]);
                        }
                    }
                    catch (NoSuchMethodException ex)
                    {
                        // this will never happen. trust me.
                        throw new RuntimeException(ex);
                    }
                    catch (InvocationTargetException ex)
                    {
                        if (ex.getTargetException() instanceof NumberFormatException)
                        {
                            String msg = ex.getTargetException().getLocalizedMessage();
                            if (StringUtils.isBlank(msg))
                            {
                                msg = ex.getTargetException().getClass().getSimpleName();
                               
                            }
                            throw new ParseException(getLabel()
                                    + " - "
                                    + String.format(UIRegistry.getResourceString("QB_PARSE_ERROR"), msg), -1);
                        }
                        throw new RuntimeException(ex);
                    }
                    catch (IllegalAccessException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    catch (InstantiationException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    catch (NumberFormatException ex)
                    {
                        String msg = ex.getLocalizedMessage();
                        if (StringUtils.isBlank(msg))
                        {
                            msg = ex.getClass().getSimpleName();
                        }
                        throw new ParseException(getLabel()
                                + " - "
                                + String.format(UIRegistry.getResourceString("QB_PARSE_ERROR"), msg), -1);
                    }
                    criteriaFormula = concatCriteria(criteriaStrs, operStr, false);
                }
            }
            if (criteriaFormula.length() > 0)
            {
                if (fieldQRI instanceof TreeLevelQRI)
                {
                    return ((TreeLevelQRI)fieldQRI).getNodeNumberCriteria(criteriaFormula, ta, operStr, isNotCheckbox.isSelected());
                }
                    
                StringBuilder str = new StringBuilder();

                if (operStr.equals("="))
                {
                    str.append(fieldQRI.getSQLFldSpec(ta, true) + " ");
                    str.append(isNotCheckbox.isSelected() ? "!" : "");
                    str.append(operStr);

                }
                else
                {
                    str.append(isNotCheckbox.isSelected() ? "NOT " : "");
                    str.append(fieldQRI.getSQLFldSpec(ta, true) + " ");
                    str.append(operStr);
                }
                str.append(" ");
                str.append(criteriaFormula);
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
    
    protected PickListCriteriaCombo createPickList(final Component saveBtn)
    {
        PickListCriteriaCombo result = new PickListCriteriaCombo(pickList);
        if (!ownerQuery.isPromptMode())
        {
            result.addActionListener(new ActionListener() {

                /* (non-Javadoc)
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getID() == 1001/*ComboBoxChanged*/)
                    {
                        saveBtn.setEnabled(true);
                    }
                }
            
            });
        }
        return result;
    }
    
    protected JCheckBox createCheckBox(final String id)
    {
        ValCheckBox checkbox = new ValCheckBox("", false, false);
        DataChangeNotifier dcn = validator.createDataChangeNotifer(id, checkbox, null);
        checkbox.addActionListener(dcn);
        return checkbox;
    }
    
    protected JComboBox createComboBox(SpQueryField.OperatorType[] items)
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
                                       final boolean returnWidths, final Component saveBtn)
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
        boolean isBool = fieldQRI != null && fieldQRI.getDataClass().equals(Boolean.class);
        if (!isBool)
        {
            operatorCBX.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        criteria.setVisible(!operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.EMPTY));
                    }
                }
            });
        }
        if (pickList == null)
        {
            criteria = createTextField();
        }
        else
        {
            criteria = createPickList(saveBtn);
            if (!ownerQuery.isPromptMode())
            {
                ((PickListCriteriaCombo) criteria).setCurrentOp((SpQueryField.OperatorType) operatorCBX.getModel().getElementAt(0));
            }
            operatorCBX.addItemListener(new ItemListener()
            {

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
                 */
                //@Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        // System.out.println("setting curront op");
                        ((PickListCriteriaCombo) criteria)
                                .setCurrentOp((SpQueryField.OperatorType) operatorCBX
                                        .getSelectedItem());
                    }
                }

            });
        }
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
            isEnforcedCkbx = createCheckBox("isEnforcedCkbx");
            isEnforcedCkbx.addFocusListener(focusListener);
            closeBtn = new JLabel(IconManager.getIcon("Close"));
        }
        else
        {
            isDisplayedCkbx = null;
            this.isPromptCkbx = null;
            isEnforcedCkbx = null;
            this.closeBtn = null;
        }

        // 0 1 2 3 4 5 6 7 8 9
        JComponent[] comps = { iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, isDisplayedCkbx, isPromptCkbx, isEnforcedCkbx, closeBtn, null };

        StringBuilder sb = new StringBuilder();
        if (columnDefStr == null)
        {
            for (int i = 0; i < comps.length; i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (i == 2 || i == 3 || i == 6 || i == 7 || i == 8)
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
            icon = IconManager.getIcon(fieldQRI.getTableInfo().getName(), iconSize);
            setIcon(icon);
        }
        if (!ownerQuery.isPromptMode())
        {
            isDisplayedCkbx.setSelected(true);
            isPromptCkbx.setSelected(!(fieldQRI instanceof RelQRI));
            isEnforcedCkbx.setSelected(false);
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
        criteria.setVisible(!isRel && !isBool);
        if (!isRel)
        {
            this.sortCheckbox.setVisible(true);
        }
        else
        {
            this.sortCheckbox.setVisible(((RelQRI )fieldQRI).getRelationshipInfo().getType() != RelationshipType.OneToMany);
        }
        
        if (!ownerQuery.isPromptMode())
        {
            isDisplayedCkbx.setVisible(!isRel);
            isPromptCkbx.setVisible(!isRel);
            isEnforcedCkbx.setVisible(!isRel);
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

    /**
     * @param otherLabels
     * @param unQualify if true then label is un-qualified.
     * @return a label for the field that is 'qualified' to distinguish it from other labels with the same title.
     */
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
                    if (p.getFieldTitle().equals(getFieldTitle()))
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
            String newLabel = getFieldTitle();
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
            fieldLabel.setText(getFieldTitle());
        }
        return fieldLabel.getText();
    }

    /**
     * @return the title of the field.
     */
    protected String getFieldTitle()
    {
        if (fieldQRI != null)
        {
            return fieldQRI.getTitle();
        }
        return null;
    }
    
    
    
    /**
     * @param parent
     * @param checkParent
     * @return
     */
    protected String getQualifiedLabel(final TableTree parent, final boolean checkParent)
    {
        TableTree reParent = parent;
        if (checkParent && reParent.getTableInfo().getClassObj().equals(Agent.class)
                && (StringUtils.isEmpty(reParent.getField()) || reParent.getName().equalsIgnoreCase(reParent.getField())) 
                && reParent.getParent().getTableQRI() != null)
        // agent (and what others??) generally offers no informative distinguishing info
        {
            reParent = reParent.getParent();
        }
        return reParent.getTableQRI().getTitle() + "/" + getFieldTitle();
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
    
    /**
     * @return true if this field's criteria should ALWAYS be applied.
     */
    public boolean isEnforced()
    {
        if (isEnforcedCkbx != null)
        {
            return isEnforcedCkbx.isSelected();
        }
        if (queryField != null)
        {
            return queryField.getAlwaysFilter();
        }
        return false;
    }
    
    /**
     * @return a string identifier unique to this field within the query that is independent of the field's title.
     */
    public String getStringId()
    {
        return fieldQRI.getStringId();
    }

    /**
     * @return the pickList
     */
    public PickListDBAdapterIFace getPickList()
    {
        return pickList;
    }
    
}