/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.CardLayout;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpExportSchemaItemMapping;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpQueryField.OperatorType;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.DateConverter;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
@SuppressWarnings("serial")
public class QueryFieldPanel extends JPanel implements ActionListener 
{
    protected static final Logger log = Logger.getLogger(QueryFieldPanel.class);
    
    protected String           noMappingStr = getResourceString("WB_NO_MAPPING");

    protected QueryFieldPanelContainerIFace    ownerQuery;
    protected String           columnDefStr;
    protected ImageIcon        blankIcon = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    
    protected boolean						hasFocus		= false;
	protected Color							bgColor			= null;
	protected JLabel						fieldLabel;
	protected boolean						labelQualified	= false;
	protected JButton						closeBtn;
	protected JLabel						schemaItemLabel;
	protected JLabel						iconLabel;
	protected ImageIcon						icon;
	protected IconManager.IconSize			iconSize = IconManager.IconSize.Std24;
	protected JCheckBox						isNotCheckbox;
	protected JComboBox						operatorCBX;
	protected JComponent					criteria;
	protected MultiStateIconButon			sortCheckbox;
	protected JCheckBox						isDisplayedCkbx;
	protected JCheckBox						isPromptCkbx;
	protected JCheckBox						isEnforcedCkbx;

	protected JComponent[]					comps;

	protected FieldQRI						fieldQRI;
	protected SpQueryField					queryField		= null;
	protected SpExportSchemaItem			schemaItem      = null;
	protected boolean						conditionForSchema = false;
	protected PickListDBAdapterIFace		pickList		= null;

	protected FormValidator					validator;

	protected QueryFieldPanel				thisItem;

	protected String[]						labelStrs;
	protected SpQueryField.OperatorType[]	comparators;

	protected DateConverter					dateConverter	= null;

	protected boolean						selected		= false;
    

    /**
     * @author timbo
     *
     * @code_status Alpha
     * 
     * Deals with pairs of criteria entries.
     *
     */

    private class CriteriaPair extends JPanel
    {
        protected JTextField text1;
        protected JTextField text2;
        protected JLabel connectorText;
        protected JPanel rangePanel;
        protected JTextField text;
        protected boolean showingPair = false;
        
        /**
         * Constructor
         */
        public CriteriaPair(final KeyListener listener)
        {
            super();
            buildUI(listener);
        }
        
        /**
         * Creates the UI components.
         */
        protected void buildUI(final KeyListener listener)
        {        	
        	text1 = createTextField("1");
        	text1.addKeyListener(listener);
            text2 = createTextField("2");
            text2.addKeyListener(listener);
            connectorText = new JLabel(" " + getResourceString("AND") + " ");
            
            rangePanel = new JPanel();
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g, f:p, f:p:g", "f:p"), rangePanel);
            CellConstraints cc = new CellConstraints();
            pb.add(text1, cc.xy(1, 1));
            pb.add(connectorText, cc.xy(2, 1));
            pb.add(text2, cc.xy(3, 1));
            rangePanel.validate();
            
            setLayout(new CardLayout());
            text = createTextField("3");
            text.addKeyListener(listener);
            add("text", text);
            add("rangePanel", rangePanel);
            validate();
        }   
        
        /**
         * @param entry
         * @param op
         * 
         * Sets the criteria text, modifying the layout if necessary.
         */
        public void setCriteriaText(final String entry, final String entry2, final OperatorType op)
        {
            if (op != null && op.equals(OperatorType.BETWEEN))
            {
                //Currently between operator is not allowed for string fields, so assume no quotes 
                //and no commas but the one separating the two limits.
                //Also assuming (more or less) valid entry. 
                setShowingPair(true);
                String[] entries; 
                if (StringUtils.isBlank(entry2))
                {
                	entries = entry.split(",");
                } else
                {
                	entries = new String[2];
                	entries[0] = entry;
                	entries[1] = entry2;
                }
                if (entries.length > 0)
                {
                	text1.setText(entries[0]);
                	if (entries.length > 1)
                	{
                		text2.setText(entries[1]);
                	}
                	else
                	{
                		text2.setText(null);
                	}
                }
                else
                {
                	text1.setText(null);
                }
            }
            else
            {
                setShowingPair(false);
                text.setText(entry);
            }
            
        }
        
        /**
         * @return a String representation of the entered criteria, parseable by the QueryBuilder.
         */
        public String getCriteriaText()
        {
            //Assuming isValidPairEntry() is true
            if (showingPair)
            {
                return text1.getText() + "," + text2.getText();
            }
            
            return text.getText();
        }
        
        /**
         * @return true unless the entered criteria is really messed up.
         */
        public boolean isValidPairEntry()
        {
            if (showingPair)
            {
                return (StringUtils.isBlank(text1.getText()) && StringUtils.isBlank(text2.getText()))
                    || (!StringUtils.isBlank(text1.getText()) && !StringUtils.isBlank(text2.getText()));
            }
            
            return true;
        }   
        
        /**
         * @return showingPair.
         */
        public boolean isShowingPair()
        {
            return showingPair;
        }
        
        /**
         * @param showingPair
         * 
         * Switches the UI from single to double criteria controls based on value of showingPair.
         */
        public void setShowingPair(final boolean showingPair)
        {
            if (this.showingPair != showingPair)
            {
                this.showingPair = showingPair;
                if (showingPair)
                {
                    ((CardLayout )getLayout()).last(this);
                }
                else
                {
                    ((CardLayout )getLayout()).first(this);
                }
                
                //clear old entries, for now.
                text.setText(null);
                text1.setText(null);
                text2.setText(null);
                
                validate();
            }
        }
    }
    
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
     * @param ownerQuery
     * @param fieldQRI
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     * @param schemaItem
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
                           final FieldQRI      fieldQRI, 
                           final String        columnDefStr,
                           final Component       saveBtn,
                           final SpQueryField  queryField,
                           final SpExportSchemaItem schemaItem)
    {
    	this(ownerQuery, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField, schemaItem, false);
    }

    /**
     * @param ownerQuery
     * @param fieldQRI
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     * @param schemaItem
     * @param conditionForSchema
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
            final FieldQRI      fieldQRI, 
            final String        columnDefStr,
            final Component       saveBtn,
            final SpQueryField  queryField,
            final SpExportSchemaItem schemaItem,
            final boolean conditionForSchema)
    {
    	this(ownerQuery, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField, schemaItem, conditionForSchema);
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
                           final SpQueryField  queryField,
                           final SpExportSchemaItem schemaItem,
                           final boolean conditionForSchema)
    {        
        this.ownerQuery = ownerQuery;
        this.conditionForSchema = conditionForSchema;
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
            if (schemaItem == null)
            {
            	labelStrs = new String[]{ " ",
                    /*UIRegistry.getResourceString("QB_FIELD")*/" ", UIRegistry.getResourceString("QB_NOT"),
                    UIRegistry.getResourceString("QB_OPERATOR"),
                    UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"),
                    UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), getResourceString("QB_ALWAYS_ENFORCE"), " ", " " };
            }
            else
            {
            	labelStrs = new String[]{ UIRegistry.getResourceString("QB_SCHEMAITEM"), " ",
                        /*UIRegistry.getResourceString("QB_FIELD")*/" ", UIRegistry.getResourceString("QB_NOT"),
                        UIRegistry.getResourceString("QB_OPERATOR"),
                        UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"),
                        /*UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), getResourceString("QB_ALWAYS_ENFORCE"), */" ", " " };
            }
        }
        this.iconSize = iconSize;
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
        
        this.schemaItem = schemaItem;
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
    
    /**
     * @param useValues
     * @return the text contained in the criteria control.
     */
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
        if (criteria instanceof CriteriaPair)
        {
            return ((CriteriaPair )criteria).getCriteriaText();
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
            
            String tablesIds = fieldQRI.getTableTree().getPathFromRootAsString();
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

    /**
     * @param text the criteria
     * @param text2 the end criteria in case of BETWEEN op
     * @param op the operator
     */
    protected void setCriteriaText(final String text, final String text2, final OperatorType op)
    {
        if (op != null && op.equals(OperatorType.BETWEEN) && !(criteria instanceof CriteriaPair))
        {
            //Probably nothing bad will result but, log situation, in case.
            log.error("operator is 'BETWEEN' but criteria control is not CriteriaPair");
        }
        if (criteria instanceof JTextField)
        {
            ((JTextField)criteria).setText(text);
        }
        else if (criteria instanceof PickListCriteriaCombo)
        {
            ((PickListCriteriaCombo)criteria).setSelections(text);
        }
        else if (criteria instanceof CriteriaPair)
        {
            ((CriteriaPair )criteria).setCriteriaText(text, text2, op);
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
                try
                {
                	operatorCBX.setSelectedIndex(queryField.getOperStart());
                } catch(IllegalArgumentException ex)
                {
                	log.error("unable to set operator index for " + queryField.getStringId() + ": " + ex);
                	operatorCBX.setSelectedIndex(0);
                }
                setCriteriaText(queryField.getStartValue(), queryField.getEndValue(), (OperatorType )operatorCBX.getSelectedItem());
                sortCheckbox.setState(queryField.getSortType());
                sortCheckbox.setEnabled(queryField.getIsDisplay());
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
                this.queryField.setStringId(fieldQRI.getStringId());
            }
            validator.validateForm();
            validator.wasValidated(null);
        }
    }
    
    /**
     * @param fqri
     * @param qf
     * 
     * Sets new field and updates UI to display properties for new field.
     */
    public void setField(final FieldQRI fqri, final SpQueryField qf)
    {
        fieldQRI = fqri;
        if (fieldQRI != null && (fieldQRI.getDataClass().equals(Calendar.class) || fieldQRI.getDataClass().equals(java.sql.Timestamp.class)))
        {
            dateConverter = new DateConverter();
        }
 
        if (fieldQRI != null)
        {
            icon = IconManager.getIcon(fieldQRI.getTableInfo().getName(), iconSize);
            setIcon(icon);
        }
 
        pickList = buildPickList();
    	comparators = getComparatorList(fieldQRI);
        String fieldLabelText = fieldQRI != null ? fieldQRI.getTitle() : null;
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
        fieldLabel.setText(fieldLabelText);
        boolean isBool = fieldQRI != null && fieldQRI.getDataClass().equals(Boolean.class);
		boolean isRel = fieldQRI != null && fieldQRI instanceof RelQRI;
			
		operatorCBX.setModel(new DefaultComboBoxModel(comparators));
		//XXX need to set up criteria to support 'between' if necessary
		
		for (int c = 1; c < comps.length; c++)
		{
			if (comps[c] != null)
			{
				comps[c].setVisible(fieldQRI != null);
			}
		}
		
		isNotCheckbox.setVisible(fieldQRI != null && !isRel);
		operatorCBX.setVisible(fieldQRI != null && !isRel);
		criteria.setVisible(fieldQRI != null && !isRel && !isBool);
		if (!isRel)
		{
			this.sortCheckbox.setVisible(fieldQRI != null);
		} else
		{
			this.sortCheckbox
					.setVisible(((RelQRI) fieldQRI).getRelationshipInfo()
							.getType() != RelationshipType.OneToMany);
		}

		if (schemaItem == null)
		{
			if (conditionForSchema)
			{
				schemaItemLabel.setText(UIRegistry.getResourceString("QueryFieldPanel.UnmappedCondition"));
				sortCheckbox.setVisible(false);
				isDisplayedCkbx.setVisible(false);
				isPromptCkbx.setVisible(false);
				isEnforcedCkbx.setVisible(false);
			}
			if (!ownerQuery.isPromptMode())
			{
				isDisplayedCkbx.setVisible(fieldQRI != null && !isRel);
				isPromptCkbx.setVisible(fieldQRI != null && !isRel);
				isEnforcedCkbx.setVisible(fieldQRI != null && !isRel);
			}
		}
		else
		{
			isDisplayedCkbx.setVisible(false);
			isPromptCkbx.setVisible(false);
			isEnforcedCkbx.setVisible(false);
		}
    	setQueryField(qf);
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
                SpQueryField.OperatorType.LIKE,
                SpQueryField.OperatorType.IN,
                SpQueryField.OperatorType.EMPTY};
        }
        //CatalogNumber needs special treatment - works better as a number.
        //And other fields? Not sure how to tell. Maybe the formatter?????
        if (field.getFieldInfo() != null && field.getFieldInfo().getName().equalsIgnoreCase("catalognumber") 
                && field.getTableInfo().getClassObj().equals(CollectionObject.class))
        {
            if (field.getFieldInfo().getFormatter() != null && field.getFieldInfo().getFormatter().isNumeric())
            {
            	return getComparatorListForClass(Number.class);
            }
            OperatorType[] stringCmps = getComparatorListForClass(String.class);
            OperatorType[] result = new OperatorType[stringCmps.length + 3];
            int c = 0;
            for (OperatorType ot : stringCmps)
            {
            	result[c++] = ot;
            }
            result[c++] = SpQueryField.OperatorType.GREATERTHAN;
            result[c++] = SpQueryField.OperatorType.LESSTHAN;
            result[c++] = SpQueryField.OperatorType.BETWEEN;
            return result;
        }
        //else
        return getComparatorListForClass(field.getDataClass());
    }
    
    /**
     * @param classObj
     * @return
     */
    public static SpQueryField.OperatorType[] getComparatorListForClass(final Class<?> classObj)
    {
        if (classObj != null)
		{
			if (classObj.equals(String.class))
			{
				return new SpQueryField.OperatorType[] {
						SpQueryField.OperatorType.CONTAINS,
						SpQueryField.OperatorType.LIKE,
						SpQueryField.OperatorType.EQUALS,
						SpQueryField.OperatorType.IN,
						SpQueryField.OperatorType.EMPTY };
			}
			if (classObj.equals(Boolean.class))
			{
				return new SpQueryField.OperatorType[] {
						SpQueryField.OperatorType.DONTCARE,
						SpQueryField.OperatorType.TRUE,
						SpQueryField.OperatorType.FALSE,
						SpQueryField.OperatorType.EMPTY };
			}
			if (classObj.equals(java.sql.Timestamp.class))
			{
				return new SpQueryField.OperatorType[] {
						SpQueryField.OperatorType.EQUALS,
						SpQueryField.OperatorType.GREATERTHAN,
						SpQueryField.OperatorType.LESSTHAN,
						SpQueryField.OperatorType.BETWEEN,
						SpQueryField.OperatorType.EMPTY };
			}
		}
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
    
    /**
     * @return true if a condition has been specified for the field.
     */
    protected boolean hasCriteria()
    {
        if (fieldQRI.getDataClass().equals(Boolean.class))
        {
            return !operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.DONTCARE);
        }
        if (operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.EMPTY))
        {
        	return true;
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
        if (SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.LIKE.getOrdinal()
                || SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.CONTAINS.getOrdinal())
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
            
            if (SpQueryField.OperatorType.getOrdForName(operatorStr) == SpQueryField.OperatorType.CONTAINS.getOrdinal())
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
            //return fieldQRI.getSQLFldSpec(ta, true, this.schemaItem != null) + (isNotCheckbox.isSelected() ? " is not " : " is ") + "null";
        	return fieldQRI.getNullCondition(ta, schemaItem != null, isNotCheckbox.isSelected());
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
                		String paramName = "spparam" + paramList.size();
                        try
                        {
                            if (fieldQRI instanceof DateAccessorQRI)
                            {
                            	new Integer((String )criteriaStrs[p]);
                            }
                            else
                            {
                            	Object arg = dateConverter.convert((String)criteriaStrs[p]);
                            	if (fieldQRI.getDataClass().equals(java.sql.Timestamp.class))
                            	{
                            		arg = new java.sql.Timestamp(((Calendar)arg).getTimeInMillis());
                            	}
                            	paramList.add(new Pair<String, Object>(paramName, arg));
                            }
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
                        if (fieldQRI instanceof DateAccessorQRI)
                        {
                        	criteriaFormula += (String )criteriaStrs[p];
                        }
                        else
                        {
                        	criteriaFormula += ":" + paramName;
                        }
                    }
                    if (SpQueryField.OperatorType.getOrdForName(operStr) == SpQueryField.OperatorType.IN
                            .getOrdinal())
                    {
                        criteriaFormula = "(" + criteriaFormula + ")";
                    }
                }
                else if (Number.class.isAssignableFrom(fieldQRI.getDataClass()) )
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryFieldPanel.class, ex);
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryFieldPanel.class, ex);
                        throw new RuntimeException(ex);
                    }
                    catch (InstantiationException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryFieldPanel.class, ex);
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
            if (operStr.equals(SpQueryField.OperatorType
                            .getString(SpQueryField.OperatorType.CONTAINS.getOrdinal())))
            {
                operStr = "Like";
            }
                            
            if (criteriaFormula.length() > 0 || fieldQRI instanceof TreeLevelQRI)
            {
                if (fieldQRI instanceof TreeLevelQRI)
                {
                    try
                    {
                    	return ((TreeLevelQRI)fieldQRI).getNodeNumberCriteria(criteriaFormula, ta, operStr, isNotCheckbox.isSelected());
                    }
                    catch (ParseException pe)
                    {
                        throw new ParseException(getLabel()
                                + " - "
                                + String.format(UIRegistry.getResourceString("QB_PARSE_ERROR"),
                                        pe.getLocalizedMessage()), -1);
                   	
                    }
                }
                    
                StringBuilder str = new StringBuilder();

                str.append(isNotCheckbox.isSelected() ? "(NOT " : "");
                str.append(fieldQRI.getSQLFldSpec(ta, true, schemaItem != null) + " ");
                str.append(operStr);
                str.append(" ");
                str.append(criteriaFormula);
                if (isNotCheckbox.isSelected()) 
                {
                    if (!operStr.equals(SpQueryField.OperatorType
                            .getString(SpQueryField.OperatorType.EMPTY.getOrdinal())))
                    {
                        str.append(" or " + fieldQRI.getSQLFldSpec(ta, true, schemaItem != null) + " is null");
                    }
                    str.append(")");
                }
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
    
    protected JTextField createTextField(final String id)
    {
        ValTextField textField = new ValTextField();
        textField.setRequired(false);
        validator.hookupTextField(textField, id, false, UIValidator.Type.Changed, "", true);
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

        KeyListener enterListener = new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				//nuthin
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				//nuthin
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				if (arg0.getKeyChar() == KeyEvent.VK_ENTER && ownerQuery != null)
				{
					ownerQuery.doSearch();
				}
				
			}
        	
        };
        
        comparators = getComparatorList(fieldQRI);
        //XXX need to build schemaItem for header panel too...
        if (schemaItem != null)
        {
        	schemaItemLabel = createLabel(schemaItem.getFieldName());
        	schemaItemLabel.setHorizontalAlignment(SwingConstants.CENTER);
        	schemaItemLabel.setText(schemaItem.getFieldName());
        }
        else if (conditionForSchema)
        {
        	String caption = fieldQRI == null ? 
        			UIRegistry.getResourceString("QueryFieldPanel.AddUnmappedCondition") :
        			UIRegistry.getResourceString("QueryFieldPanel.UnmappedCondition");	
        	schemaItemLabel = createLabel(caption);
        	schemaItemLabel.setHorizontalAlignment(SwingConstants.CENTER);
        	schemaItemLabel.setText(UIRegistry.getResourceString(caption)); 
        }
        else
        {
        	this.schemaItemLabel = null;
        }
        
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
        fieldLabel.addKeyListener(enterListener);
        isNotCheckbox = createCheckBox("isNotCheckbox");
        isNotCheckbox.addFocusListener(focusListener);
        isNotCheckbox.addKeyListener(enterListener);
        operatorCBX = createComboBox(comparators);
        operatorCBX.addFocusListener(focusListener);
        operatorCBX.addKeyListener(enterListener);
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
            boolean hasBetweenOp = false;
            for (int o=0; o < comparators.length; o++)
            {
                if (comparators[o].equals(OperatorType.BETWEEN))
                {
                    hasBetweenOp = true;
                    break;
                }
            }
            if (hasBetweenOp)
            {
                criteria = new CriteriaPair(enterListener);
                operatorCBX.addActionListener(this);
            }
            else
            {
                criteria = createTextField("1");
                criteria.addKeyListener(enterListener);
            }
        }
        else
        {
            criteria = createPickList(saveBtn);
            if (!ownerQuery.isPromptMode())
            {
                ((PickListCriteriaCombo) criteria).setCurrentOp((SpQueryField.OperatorType) operatorCBX.getModel().getElementAt(0));
            }
            criteria.addKeyListener(enterListener);
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
        //criteria.addFocusListener(focusListener);
        
        sortCheckbox = new MultiStateIconButon(new ImageIcon[] {
                IconManager.getImage("GrayDot", IconManager.IconSize.Std16),
                IconManager.getImage("UpArrow", IconManager.IconSize.Std16),
                IconManager.getImage("DownArrow", IconManager.IconSize.Std16) });
        DataChangeNotifier dcn = validator.hookupComponent(sortCheckbox, "scb",
                UIValidator.Type.Changed, "", true);
        sortCheckbox.addFocusListener(focusListener);
        sortCheckbox.addActionListener(dcn);
        sortCheckbox.addKeyListener(enterListener);
        if (!this.ownerQuery.isPromptMode())
        {
            isDisplayedCkbx = createCheckBox("isDisplayedCkbx");
            isDisplayedCkbx.addFocusListener(focusListener);
            isDisplayedCkbx.addKeyListener(enterListener);
            isDisplayedCkbx.addActionListener(new ActionListener() {

				/* (non-Javadoc)
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					SwingUtilities.invokeLater(new Runnable() {

						/* (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run()
						{
							sortCheckbox.setEnabled(isDisplayedCkbx.isSelected());
						}
					});
				}
            });
            isPromptCkbx = createCheckBox("isPromptCkbx");
            isPromptCkbx.addFocusListener(focusListener);
            isPromptCkbx.addKeyListener(enterListener);
            isEnforcedCkbx = createCheckBox("isEnforcedCkbx");
            isEnforcedCkbx.addFocusListener(focusListener);
            isEnforcedCkbx.addKeyListener(enterListener);
            closeBtn = createIconBtn("Close", "QB_REMOVE_FLD", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
            		boolean clearIt = schemaItem != null;
                	ownerQuery.removeQueryFieldItem((QueryFieldPanel) ((JComponent) ae.getSource())
                        .getParent());
                	if (clearIt)
                	{
                 		setField(null, null);
                	}
                }
            });
            closeBtn.setEnabled(true);
            closeBtn.setFocusable(false);
            closeBtn.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    ((JButton)e.getSource()).setIcon(IconManager.getIcon("CloseHover"));
                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    ((JButton)e.getSource()).setIcon(IconManager.getIcon("Close"));
                    super.mouseExited(e);
                }
                
            });
        }
        else
        {
            isDisplayedCkbx = null;
            this.isPromptCkbx = null;
            isEnforcedCkbx = null;
            this.closeBtn = null;
        }

        JComponent[] qComps = {iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, isDisplayedCkbx, isPromptCkbx, isEnforcedCkbx, closeBtn, null };
        JComponent[] sComps = { schemaItemLabel, iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, closeBtn, null };
        // 0 1 2 3 4 5 6 7 8 9
        if (schemaItem == null && !conditionForSchema)
        {
        	comps = qComps;
        }
        else
        {
        	comps = sComps;
        }
        
        StringBuilder sb = new StringBuilder();
        if (columnDefStr == null)
        {
            for (int i = 0; i < comps.length; i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (isCenteredComp(i))
                    sb.append("c:");
                if (i != 0 || schemaItem == null)
                {
                	sb.append("p");
                }
                else
                {
                	sb.append("300px");
                }
                if (isGrowComp(i))
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
        }

        if (fieldQRI == null && !returnWidths)
        {
        	for (int c = 1; c < comps.length; c++)
        	{
        		if (comps[c] != null)
        		{
        			comps[c].setVisible(false);
        		}
        	}
        }
        else
        {
			// for now
			boolean isRel = fieldQRI != null && fieldQRI instanceof RelQRI;
			isNotCheckbox.setVisible(!isRel);
			operatorCBX.setVisible(!isRel);
			criteria.setVisible(!isRel && !isBool);
			if (!isRel)
			{
				this.sortCheckbox.setVisible(true);
			} else
			{
				this.sortCheckbox
						.setVisible(((RelQRI) fieldQRI).getRelationshipInfo()
								.getType() != RelationshipType.OneToMany);
			}

			if (!ownerQuery.isPromptMode())
			{
				isDisplayedCkbx.setVisible(!isRel);
				isPromptCkbx.setVisible(!isRel);
				isEnforcedCkbx.setVisible(!isRel);
			}
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
            if (this.schemaItemLabel == null)
            {
            	widths[0] = iconSize.size();
            	widths[1] = 200;
            }
            else
            {
            	widths[1] = iconSize.size();
            	widths[2] = 200;
            }
        }
        return widths;
    }
    
    /**
     * @param compIdx
     * @return true if comps[compIdx] should be centered
     */
    protected boolean isCenteredComp(int compIdx)
    {
        if (schemaItemLabel == null)
        {
        	return compIdx == 1 || compIdx == 2 || compIdx == 5 || compIdx == 6 || compIdx == 7;
        }
        else
        {
        	return compIdx == 2 || compIdx == 3 || compIdx == 6 || compIdx == 7 || compIdx == 8;
        }

    }
    
    /**
     * @param compIdx
     * @return true if comps[compIdx] should grow.
     */
    protected boolean isGrowComp(int compIdx)
    {
    	return schemaItemLabel == null ? compIdx == 4 : compIdx == 5;
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
            if (isCenteredComp(i)) sb.append("c:");
            sb.append("max(");
            sb.append(labelWidths[i]);
            sb.append(";p)");
            if (isGrowComp(i)) sb.append(":g");
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
        if (fieldQRI != null)
        {
        	return fieldQRI.getFieldInfo();
        }
        return null;
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
        return !conditionForSchema && 
        	(ownerQuery.isPromptMode() || isDisplayedCkbx.isSelected());
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
            } while (parent != null && labels.indexOf(newLabel) != -1 && !parent.getName().equals("root"));
            
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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(operatorCBX))
        {
            OperatorType op = (OperatorType )operatorCBX.getSelectedItem();
            if (op != null && op.equals(OperatorType.BETWEEN))
            {
                ((CriteriaPair )criteria).setShowingPair(true);
            }
            else
            {
                if (criteria instanceof CriteriaPair)
                {
                    ((CriteriaPair )criteria).setShowingPair(false);
                }
            }
        }
    }

	/**
	 * @return the schemaItem
	 */
	public SpExportSchemaItem getSchemaItem()
	{
		return schemaItem;
	}

	/**
	 * @return the itemMapping
	 */
	public SpExportSchemaItemMapping getItemMapping()
	{
		return queryField != null ? queryField.getMapping() : null;
	}

	/**
	 * @return the conditionForSchema
	 */
	public boolean isConditionForSchema()
	{
		return conditionForSchema;
	}
    
}
