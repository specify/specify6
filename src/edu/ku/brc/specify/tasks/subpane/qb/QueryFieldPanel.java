/* Copyright (C) 2013, University of Kansas Center for Research
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
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpQueryField.OperatorType;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.ui.CatalogNumberFormatter;
import edu.ku.brc.specify.ui.CatalogNumberUIFieldFormatter;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.specify.ui.db.PickListTableAdapter;
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
    
//    private static final String operatorSavingFixDateStr = "2012-12-05"; 
//    private static final Date operatorSavingFixDate = DateFormat.getDateInstance(DateFormat.LONG, Locale.US).parse("2012-12-05");
    
    protected String           noMappingStr = getResourceString("WB_NO_MAPPING");

    protected QueryFieldPanelContainerIFace    ownerQuery;
    protected String           columnDefStr;
    protected ImageIcon        blankIcon = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    
    protected boolean						hasFocus		= false;
	protected Color							bgColor			= null;
	protected JLabel						fieldLabel;
	protected boolean						labelQualified	= false;
	protected JButton						closeBtn;
	protected JComboBox						schemaItemCBX;
	protected JLabel						iconLabel;
	protected ImageIcon						icon;
	protected IconManager.IconSize			iconSize = IconManager.IconSize.Std24;
	protected JCheckBox						isNotCheckbox;
	protected JComboBox		                operatorCBX;
	protected JComponent					criteria;
	protected MultiStateIconButon			sortCheckbox;
	protected JCheckBox						isDisplayedCkbx;
	protected JCheckBox						isPromptCkbx;
	protected JCheckBox						isEnforcedCkbx;

	protected JComponent[]					comps;

	protected FieldQRI						fieldQRI;
	protected SpQueryField					queryField		= null;
	protected SpExportSchemaMapping	        schemaMapping   = null;
	protected SpExportSchemaItem			schemaItem      = null;
	protected String	 					schemaItemName  = null;
	protected boolean						autoMapped      = false;
	
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

        /* (non-Javadoc)
    	 * @see javax.swing.JComponent#getPreferredSize()
    	 */
    	@Override
    	public Dimension getPreferredSize() 
    	{
    		Dimension result = super.getPreferredSize();
    		result.setSize(Math.min((sortCheckbox.getX() - 4) - (operatorCBX.getX() + operatorCBX.getWidth() + 4), result.getWidth()), result.getHeight());
    		return result;
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
        
//        /**
//         * @return true unless the entered criteria is really messed up.
//         */
//        public boolean isValidPairEntry()
//        {
//            if (showingPair)
//            {
//                return (StringUtils.isBlank(text1.getText()) && StringUtils.isBlank(text2.getText()))
//                    || (!StringUtils.isBlank(text1.getText()) && !StringUtils.isBlank(text2.getText()));
//            }
//            
//            return true;
//        }   
//        
//        /**
//         * @return showingPair.
//         */
//        public boolean isShowingPair()
//        {
//            return showingPair;
//        }
        
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
        if (fieldQRI instanceof RelQRI)
        {
        	PickListDBAdapterIFace pl = PickListDBAdapterFactory.getInstance().create(fieldQRI.getTableInfo().getName(), false);
        	if (pl instanceof PickListTableAdapter)
        	{
        		return pl;
        	}
        	return null;

        }
    	if (fieldQRI != null && fieldQRI.getTableInfo() != null && fieldQRI.getFieldInfo() != null) 
        {
            
    		PickListDBAdapterIFace typeCodeList = RecordTypeCodeBuilder.getTypeCode(fieldQRI.getFieldInfo());
    		if (typeCodeList != null)
    		{
    			return typeCodeList;
    		}
    		//XXX unfortunately this doesn't work because currently picklist defs are only setup via form view defs
            if (StringUtils.isNotEmpty(fieldQRI.getFieldInfo().getPickListName()))
            {
                //pickList = ((edu.ku.brc.specify.ui.db.PickListDBAdapterFactory)PickListDBAdapterFactory.getInstance()).create(fieldQRI.getFieldInfo().getPickListName(), false);
                return PickListDBAdapterFactory.getInstance().create(fieldQRI.getFieldInfo().getPickListName(), false);
            }
            //else
            //return RecordTypeCodeBuilder.getTypeCode(fieldQRI.getFieldInfo());
        }
        return null;
    }
    


    /**
     * @param ownerQuery
     * @param fieldQRI
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     * @param schemaMapping
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
            final FieldQRI      fieldQRI, 
            final String        columnDefStr,
            final Component       saveBtn,
            final SpQueryField  queryField,
            final SpExportSchemaMapping schemaMapping)
    {
    	this(ownerQuery, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField, schemaMapping, null);
    }
 
    /**
     * @param ownerQuery
     * @param fieldQRI
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
                           final FieldQRI      fieldQRI, 
                           final String        columnDefStr,
                           final Component       saveBtn,
                           final SpQueryField  queryField)
    {
    	this(ownerQuery, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField, null, null);
    }

    /**
     * @param ownerQuery
     * @param fieldQRI
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     * @param schemaMapping
     * @param schemaItem
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
            final FieldQRI      fieldQRI, 
            final String        columnDefStr,
            final Component       saveBtn,
            final SpQueryField  queryField,
            final SpExportSchemaMapping schemaMapping,
            final SpExportSchemaItem schemaItem)
    {
    	this(ownerQuery, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField, schemaMapping, schemaItem);
    }

    /**
     * @param ownerQuery
     * @param fieldQRI
     * @param iconSize
     * @param columnDefStr
     * @param saveBtn
     * @param queryField
     * @param schemaMapping
     * @param schemaItem
     */
    public QueryFieldPanel(final QueryFieldPanelContainerIFace ownerQuery,
                           final FieldQRI      fieldQRI, 
                           final IconManager.IconSize iconSize,
                           final String        columnDefStr,
                           final Component       saveBtn,
                           final SpQueryField  queryField,
                           final SpExportSchemaMapping schemaMapping,
                           final SpExportSchemaItem schemaItem)
    {        
        this.ownerQuery = ownerQuery;
        this.schemaMapping = schemaMapping;
        this.schemaItem = schemaItem;
        boolean isForSchema = this.schemaMapping != null;
        if (this.ownerQuery.isPromptMode())
        {
            if (!isForSchema)
            {
            	labelStrs = new String[]{ " ",
                    UIRegistry.getResourceString("QB_FIELD"), UIRegistry.getResourceString("QB_NOT"),
                    UIRegistry.getResourceString("QB_OPERATOR"),
                    UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"),
                    //UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), 
                    //" ", " " 
                    };
            } else
            {
            	labelStrs = new String[]{UIRegistry.getResourceString("QB_SCHEMAITEM"), " ",
                        UIRegistry.getResourceString("QB_FIELD"), UIRegistry.getResourceString("QB_NOT"),
                        UIRegistry.getResourceString("QB_OPERATOR"),
                        UIRegistry.getResourceString("QB_CRITERIA"), UIRegistry.getResourceString("QB_SORT"), UIRegistry.getResourceString("QB_ALLOW_NULL"),
                        //UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_PROMPT"), 
                        //" ", " " 
                        };
            }
        }
        else
        {
            if (!isForSchema)
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
                        UIRegistry.getResourceString("QB_DISPLAY"), getResourceString("QB_ALLOW_NULL"), " ", " " };
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
    public String getCriteriaText(final boolean useValues)
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
        boolean isForSchema = schemaMapping != null;
    	if (qField != null && !ownerQuery.isPromptMode() && !ownerQuery.isForSchemaExport())
        {
            qField.setIsDisplay(isDisplayedCkbx.isSelected());
            qField.setIsPrompt(isPromptCkbx.isSelected());
            if (isForSchema)
            {
            	qField.setAllowNulls(isEnforcedCkbx.isSelected());
            } else
            {
            	qField.setAlwaysFilter(isEnforcedCkbx.isSelected());
            }
            qField.setIsNot(isNotCheckbox.isSelected());
            if (validator.hasChanged() && qField.getSpQueryFieldId() != null)
            {
                FormHelper.updateLastEdittedInfo(qField);
            }
            
            qField.setSortType((byte)sortCheckbox.getState());
            
            qField.setOperStart(((SpQueryField.OperatorType)operatorCBX.getSelectedItem()).getOrdinal());
           
            qField.setStartValue(getCriteriaText(false));
            String lbl = this.getLabel();
            if (fieldQRI instanceof RelQRI)
            {
                lbl = RelQRI.stripDescriptiveStuff(lbl);    
            }
            qField.setContextTableIdent(fieldQRI.getTableInfo().getTableId());
            qField.setColumnAliasTitle(lbl, fieldQRI instanceof TreeLevelQRI);
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
            
            if (schemaItemCBX != null)
            {
            	SpExportSchemaItemMapping mapping = qField.getMapping();
            	if (mapping != null)
            	{
            		schemaItem = (SpExportSchemaItem )schemaItemCBX.getSelectedItem();
            		if (schemaItem.getId() == null)
            		{
            			mapping.setExportSchemaItem(null);
            			String exportName = schemaItemCBX.getEditor().getItem().toString();
            			if (exportName == null || exportName.equals(getResourceString("QueryBldrPane.UnmappedSchemaItemName")))            			
            			{
            				mapping.setExportedFieldName(getDefaultExportedFieldName());
            			} else
            			{
            				mapping.setExportedFieldName(exportName);
            			}
            		} else
            		{
            			mapping.setExportSchemaItem(schemaItem);
            		}
            	}
            }
        } else
        {
            log.error("QueryField is null or ownerQuery is prompt only. Unable to update database object.");
        }
    }
    
    /**
     * @return customized fieldname/column header for schema mapping items that are not mapped to a concept.
     */
    protected String getExportedFieldName()
    {
    	SpExportSchemaItemMapping mi = this.getItemMapping();
    	if (mi == null || mi.getExportedFieldName() == null)
    	{
    		return getDefaultExportedFieldName();
    	} else
    	{
    		return mi.getExportedFieldName();
    	}
    }
    
    protected String getDefaultExportedFieldName()
    {
    	return queryField.getColumnAlias();
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
    
//    private SpQueryField.OperatorType getOperatorType(final SpQueryField fld)
//    {
//    	Timestamp fldTimeStamp = fld.getTimeStampModified();
//    	
//    }
    
    /**
     * @param queryField
     */
    public void setQueryFieldForAutomapping(SpQueryField queryField)
    {
    	this.queryField = queryField;
    }
    
    /**
     * @param queryField the queryField to set
     */
    private void setQueryField(SpQueryField queryField)
    {
        this.queryField = queryField;
        boolean isForSchema = schemaMapping != null;
        //if (!ownerQuery.isPromptMode()) {
        	if (queryField != null)
        	{
        		if (queryField.getSpQueryFieldId() != null)
        		{
        			isNotCheckbox.setSelected(queryField.getIsNot());
        			try
        			{
        				OperatorType o = OperatorType.values()[queryField.getOperStart()];
        				operatorCBX.setSelectedItem(o);
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
        				if (isForSchema)
        				{
        					isEnforcedCkbx.setSelected(queryField.getAllowNulls() == null ? false : queryField.getAllowNulls());
        				} else
        				{
        					isEnforcedCkbx.setSelected(queryField.getAlwaysFilter() == null ? true : queryField.getAlwaysFilter());
        				}
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
        //}
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
		boolean isTreeLevel = fieldQRI instanceof TreeLevelQRI;
		
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
		if (schemaMapping != null)
		{
			this.sortCheckbox.setVisible(!(isTreeLevel || isRel));
		}
		else
		{
			if (!isRel)
			{
				this.sortCheckbox.setVisible(fieldQRI != null);
			} else
			{
				this.sortCheckbox
					.setVisible(((RelQRI) fieldQRI).getRelationshipInfo()
							.getType() != RelationshipType.OneToMany);
			}
		}

		if (schemaMapping != null)
		{
				isPromptCkbx.setVisible(false);
		} else if (!ownerQuery.isPromptMode())
		{
				isDisplayedCkbx.setVisible(fieldQRI != null && !isRel);
				isPromptCkbx.setVisible(fieldQRI != null && !isRel);
				isEnforcedCkbx.setVisible(fieldQRI != null && !isRel);
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
        if (field ==  null)
        {
            return new SpQueryField.OperatorType[]{};
        }
        return getComparatorList(field instanceof TreeLevelQRI, pickList != null, field.getFieldInfo(),
        		field.getDataClass());
    }    

    /**
     * @return the format name.
     */
    protected String getFormatName()
    {
    	return getQueryField() != null ? getQueryField().getFormatName() : null;
    }
    
    /**
     * @param field
     * @return list of comparators appropriate for field.
     */
    public static SpQueryField.OperatorType[] getComparatorList(boolean isTreeLevel, boolean isPickList, 
    		DBFieldInfo fieldInfo, Class<?> dataClass)
    {
        if (isPickList)
        {
            return new SpQueryField.OperatorType[] {
                    SpQueryField.OperatorType.EQUALS,
                    SpQueryField.OperatorType.IN,
                    SpQueryField.OperatorType.EMPTY};
        }
        //CatalogNumber needs special treatment - works better as a number.
        //And other fields? Not sure how to tell. Maybe the formatter?????
        if (fieldInfo != null && fieldInfo.getName().equalsIgnoreCase("catalognumber") 
                && fieldInfo.getTableInfo().getClassObj().equals(CollectionObject.class))
        {
            if (fieldInfo.getFormatter() != null && fieldInfo.getFormatter().isNumeric())
            {
            	return getComparatorListForClass(Number.class);
            }
            OperatorType[] stringCmps = getComparatorListForClass(String.class);
            OperatorType[] result = new OperatorType[stringCmps.length + 2];
            int c = 0;
            for (OperatorType ot : stringCmps)
            {
            	result[c++] = ot;
            }
            result[c++] = SpQueryField.OperatorType.GREATERTHAN;
            result[c++] = SpQueryField.OperatorType.LESSTHAN;
            //result[c++] = SpQueryField.OperatorType.BETWEEN;
            return result;
        }
        //else
        return getComparatorListForClass(dataClass);
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
						SpQueryField.OperatorType.BETWEEN,
						SpQueryField.OperatorType.EMPTY };
			}
			if (classObj.equals(Boolean.class))
			{
				return new SpQueryField.OperatorType[] {
						SpQueryField.OperatorType.DONTCARE,
						SpQueryField.OperatorType.TRUE,
						SpQueryField.OperatorType.FALSE,
						SpQueryField.OperatorType.TRUEORNULL,
						SpQueryField.OperatorType.FALSEORNULL,
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
        return StringUtils.isNotEmpty(getCriteriaText(false).trim());
    }
 
    
   /**
     * @param criteriaEntry - String of comma-delimited entries
     * @return Array of formatted criteria
     * @throws ParseException
     */
    protected Object[] parseCriteria(final String origCriteriaEntry) throws ParseException
    {
        String[] raw;
        UIFieldFormatterIFace formatter = fieldQRI.getFormatter();
        boolean isNumericCatNum = (formatter instanceof CatalogNumberUIFieldFormatter && ((CatalogNumberUIFieldFormatter )formatter).isNumeric());
        String criteriaEntry = origCriteriaEntry;
        if (isNumericCatNum) {
        	criteriaEntry = CatalogNumberFormatter.preParseNumericCatalogNumbersWithSeries(origCriteriaEntry, formatter);
        }
        if (operatorCBX.getSelectedItem() == SpQueryField.OperatorType.BETWEEN 
                || operatorCBX.getSelectedItem() == SpQueryField.OperatorType.IN 
                || formatter instanceof CatalogNumberUIFieldFormatter) //',' in numeric catnums cause stack traces, and they are invalid in string catnums so don't allow them)
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
        
        Object[] result = new Object[raw.length];
        for (int e=0; e<raw.length; e++)
        {
            try
            {
                if (isNumericCatNum) {
                	Pair<String, String> series = getSerStartEnd(raw[e].trim());
                	formatter.formatFromUI(series.getFirst());
                	formatter.formatFromUI(series.getSecond());
                	if (!series.getSecond().equals(raw[e].trim())) {
                		result[e] = series;
                	} else {
                		result[e] = series.getFirst();
                	}
                } else {
                	result[e] = formatter != null ? formatter.formatFromUI(raw[e].trim()) : raw[e].trim();
                }
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
     * @param ser
     * @return
     * @throws Exception
     */
    protected Pair<String, String> getSerStartEnd(String ser) throws Exception {
    	String start = null;
    	String end = null;
    	if (!ser.contains("-")) {
    		start = ser;
    		end = ser;
    	} else if (ser.endsWith("-")) {
    		throw new Exception("Bad series format.");
    	} else {
    		String[] parts = ser.split("-");
    		if (parts.length != 2) {
    			throw new Exception("Bad series format.");
    		} 
    		start = parts[0];
    		if (parts[1].length() >= parts[0].length()) {
    			end = parts[1];
    		} else {
    			end = start.substring(0, start.length() - parts[1].length()) + parts[1];
    		}
    	}
    	return new Pair<String, String>(start, end);
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
    @SuppressWarnings("unchecked")
    protected String concatCriteria(final Object[] criteriaObjs, final String operatorStr, final boolean quote, final TableAbbreviator ta)
    {
        //XXX '%' as wildcard may be db vendor -specific??
        
        char quoteStr = quote ? '\'' : ' ';
        String result = quoteStr + escape(criteriaObjs[0], quoteStr).toString() + quoteStr;
        if (SpQueryField.OperatorType.getOrdForOp(operatorStr) == SpQueryField.OperatorType.LIKE.getOrdinal()
                || SpQueryField.OperatorType.getOrdForOp(operatorStr) == SpQueryField.OperatorType.CONTAINS.getOrdinal())
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
            
            if (SpQueryField.OperatorType.getOrdForOp(operatorStr) == SpQueryField.OperatorType.CONTAINS.getOrdinal())
            {
                //if user didn't purposely include a wildcard then add them
                result = quoteStr + "%" + result.substring(1, result.length()-1) + "%" + quoteStr;
            }

        }
        else if (SpQueryField.OperatorType.getOrdForOp(operatorStr) == SpQueryField.OperatorType.BETWEEN.getOrdinal())
        {
            result += " and " + quoteStr + escape(criteriaObjs[1], quoteStr) + quoteStr;
        }
        else if (SpQueryField.OperatorType.getOrdForOp(operatorStr) == SpQueryField.OperatorType.IN.getOrdinal())
        {
            result = "";
            List<Pair<String,String>> sers = new ArrayList<Pair<String, String>>();
            for (int p = 0; p < criteriaObjs.length; p++) {
            	if (criteriaObjs[p] instanceof String) {
            		if (!"".equals(result)) {
            			result += ",";
            		}
            		result += "" + quoteStr + escape(criteriaObjs[p], quoteStr) + quoteStr;
            	} else {
            		Pair<String, String> ser = (Pair<String, String>)criteriaObjs[p];
            		ser.setFirst("" + quoteStr + escape(ser.getFirst(), quoteStr) + quoteStr);
            		ser.setSecond("" + quoteStr + escape(ser.getSecond(), quoteStr) + quoteStr);
            		sers.add(ser);
            	}
            }
            if (!"".equals(result)) {
            	result = fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()) + " " + operatorStr + "(" + result + ")";
            }
            for (Pair<String, String> ser : sers) {
            	if (!"".equals(result)) {
            		result += " OR ";
            	}
            	result += fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()) + " BETWEEN " + ser.getFirst() + " AND " + ser.getSecond();
            }
            result = "(" + result + ")";
        }
        return result;
    }
    
    /**
     * @return
     */
    public boolean isNegated()
    {
    	return isNotCheckbox != null && isNotCheckbox.isSelected();
    }

    /**
     * @return true if criteria entries should be handled as numeric cat nums for hql/sql
     * 
     * NOTE: "where catalogNumber = 1000" works in hql even though catalogNumber is a string field.
     * This MAY be because MySQL will automatically convert string/numeric types when necessary.
     * If we switch to another sql dbms, catalogNumbers may have to be treated as strings.
     * 
     */
    protected boolean isNumericCatalogNumber() 
    {
    	UIFieldFormatterIFace formatter = fieldQRI.getFormatter();
    	return formatter instanceof CatalogNumberUIFieldFormatter && ((CatalogNumberUIFieldFormatter )formatter).isNumeric();    	
    }

    /**
     * @return true is the the 'Empty' operator criteria operator is selected
     */
    public boolean isEmptyCriterion()
    {
    	return operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.EMPTY);
    }
    
    /**
     * @param op
     * @return
     */
    protected String getOperatorQLText()
    {
    	return SpQueryField.OperatorType.getOp(((SpQueryField.OperatorType)operatorCBX.getSelectedItem()).getOrdinal());
    }

    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getCriteriaFormula(final TableAbbreviator ta,
                                     final List<Pair<String, Object>> paramList)
            throws ParseException
    {
        if (operatorCBX.getSelectedItem().equals(SpQueryField.OperatorType.EMPTY))
        {
            String nullCond = fieldQRI.getNullCondition(ta, schemaItem != null, isNotCheckbox.isSelected(), getFormatName());
            if (fieldQRI.getDataClass().equals(String.class)) {
                String fieldSpec = fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()); 
                return "(" + nullCond + " or " + fieldSpec + " = '')";
            }
        	return nullCond;
        }

        boolean seriesPresent = false;

        if (hasCriteria())
        {
            boolean addNullConjunction = false;
            boolean nullPick = criteria instanceof PickListCriteriaCombo && ((PickListCriteriaCombo)criteria).nullItemIsPicked();
        	Object[] criteriaStrs = parseCriteria(getCriteriaText(true).trim());
            String criteriaFormula = "";
            //String operStr = operatorCBX.getSelectedItem().toString();
            String operStr = getOperatorQLText();
            if (!(criteriaStrs[0] instanceof String) && !(criteriaStrs[0] instanceof Pair))
            {
                //XXX - If the field has a formatter and it returned non-String data
                // then assume all parsing and conversion has been accomplished??
                //(hopefully this will never occur)
                log.info(fieldQRI.getFieldInfo() + ": formatter returned non-string data.");
                criteriaFormula = concatCriteria(criteriaStrs, operStr, false, ta);
            }
            else
            {
                if (fieldQRI.getDataClass().equals(Boolean.class))
                {
                    if (operStr.equals(SpQueryField.OperatorType
                            .getOp(SpQueryField.OperatorType.TRUE.getOrdinal())) ||
                            operStr.equals(SpQueryField.OperatorType
                                    .getOp(SpQueryField.OperatorType.TRUEORNULL.getOrdinal())) )
                    {
                        criteriaFormula = "true";
                    }
                    else 
                    {
                        criteriaFormula = "false";
                    }
                    addNullConjunction = operStr.equals(SpQueryField.OperatorType
                                    .getOp(SpQueryField.OperatorType.FALSEORNULL.getOrdinal())) ||
                            operStr.equals(SpQueryField.OperatorType
                                    .getOp(SpQueryField.OperatorType.TRUEORNULL.getOrdinal()));
                    operStr = "=";
                }
                else if (fieldQRI.getDataClass().equals(String.class) && !isNumericCatalogNumber())
                {
                    criteriaFormula = concatCriteria(criteriaStrs, operStr, !(pickList instanceof PickListTableAdapter), ta);
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
                    if (SpQueryField.OperatorType.getOrdForOp(operStr) == SpQueryField.OperatorType.IN
                            .getOrdinal())
                    {
                        criteriaFormula = "(" + criteriaFormula + ")";
                    }
                }
                else if (Number.class.isAssignableFrom(fieldQRI.getDataClass()) || isNumericCatalogNumber())
                {
                    Constructor<?> tester;
                    try
                    {
                        tester = isNumericCatalogNumber() ? Integer.class.getConstructor(String.class)
                        		: fieldQRI.getDataClass().getConstructor(String.class);
                        for (int s = 0; s < criteriaStrs.length; s++)
                        {
                            Object critter = criteriaStrs[s];
                            List<String> strs = new ArrayList<String>(2);
                            if (critter instanceof String) {
                            	strs.add(critter.toString());
                            } else {
                            	seriesPresent = true;
                            	strs.add(((Pair<String, String>)critter).getFirst());
                            	strs.add(((Pair<String, String>)critter).getSecond());
                            }
                            List<String> newStrs = new ArrayList<String>(2);
                            for (String str : strs) {
                            	tester.newInstance(str);
                            
                            	//remove leading zeroes
                            	String newString = str;
                            	boolean isZeroes = false;
                            	while (newString.startsWith("0"))
                            	{
                            		newString = newString.substring(1);
                            		isZeroes = true;
                            	}
                            	if (isZeroes && StringUtils.isBlank(newString))
                            	{
                            		newString = "0";
                            	}
                            	newStrs.add(newString);
                            }
                            if (newStrs.size() == 2) {
                            	((Pair<String, String>)criteriaStrs[s]).setFirst(newStrs.get(0));
                            	((Pair<String, String>)criteriaStrs[s]).setSecond(newStrs.get(1));
                            } else {
                            	criteriaStrs[s] = newStrs.get(0);
                            }
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
                    criteriaFormula = concatCriteria(criteriaStrs, operStr, false, ta);
                }
            }
            if (operStr.equals(SpQueryField.OperatorType
                            .getOp(SpQueryField.OperatorType.CONTAINS.getOrdinal())))
            {
                operStr = "Like";
            }
                            
            if (criteriaFormula.length() > 0 || nullPick || fieldQRI instanceof TreeLevelQRI)
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
                if (!seriesPresent) {
                	str.append(fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()) + " ");
                }
                if (nullPick && "=".equals(operStr))
                {
                	str.append(" is null ");
                } else if (!seriesPresent) {
                	str.append(operStr);
                }
                str.append(" ");
                if (!(nullPick && "=".equals(operStr)))
                {
                	str.append(criteriaFormula);
                }
                if (isNotCheckbox.isSelected()) 
                {
                    if (!operStr.equals(SpQueryField.OperatorType
                            .getOp(SpQueryField.OperatorType.EMPTY.getOrdinal())))
                    {
                        str.append(" or " + fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()) + " is null");
                    }
                    str.append(")");
                }
                String result =  str.toString();
                if (addNullConjunction 
                		|| (StringUtils.isNotBlank(result) && isEnforcedCkbx != null && isEnforcedCkbx.isSelected() && schemaMapping != null)
                		|| (nullPick && !"=".equals(operStr)))
                {
                	//Currently, when the null value is picked with the IN condition, a '' entry is included in the IN list
                	//This is not technically correct, but probably will never matter, and possibly produce more desirable 
                	//results then the technically correct criteria 
                	result = "(" + result + " or " + fieldQRI.getSQLFldSpec(ta, true, schemaItem != null, getFormatName()) + " is null)";
                }                
                return result;
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
                        if (saveBtn != null)
                        {
                        	saveBtn.setEnabled(true);
                        }
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
        if (schemaMapping != null)
        {
        	schemaItemCBX = edu.ku.brc.ui.UIHelper.createComboBox();
        	schemaItemCBX.addItem("empty"); //to work around validator blow up for empty lists.
            DataChangeNotifier dcnsi = validator.hookupComponent(schemaItemCBX, "sicbx",
                    UIValidator.Type.Changed, "", true);
            schemaItemCBX.addActionListener(dcnsi);
        	
            schemaItemCBX.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (!QueryFieldPanel.this.ownerQuery.isUpdatingAvailableConcepts())
                    {
                    	if (e.getStateChange() == ItemEvent.SELECTED)
                    	{
                    		if (e.getItem() instanceof SpExportSchemaItem)
                    		{
                    			QueryFieldPanel.this.schemaItem = (SpExportSchemaItem )e.getItem();
                    		} else
                    		{
                    			SpExportSchemaItemMapping m = QueryFieldPanel.this.getItemMapping(); 
                    			SpExportSchemaItem si = QueryFieldPanel.this.schemaItem;
                    			String item = e.getItem().toString();
                    			if (StringUtils.isNotBlank(item) && ownerQuery.isAvailableExportFieldName(QueryFieldPanel.this, item))
                    			{
                    				if (m != null)
                    				{
                    					m.setExportedFieldName(e.getItem().toString());
                    				}
                    				if (si != null)
                    				{
                    					si.setFieldName(e.getItem().toString());
                    				}
                    			} else
                    			{
                    				if (StringUtils.isBlank(item))
                    				{
                    					UIRegistry.displayErrorDlgLocalized("QueryFieldPanel.ExportFieldNameInvalid", item);
                    				} else
                    				{
                    					UIRegistry.displayErrorDlgLocalized("QueryFieldPanel.ExportFieldNameAlreadyUsed", item);
                    				}
                    				schemaItemCBX.setSelectedIndex(0);
                    			}
                    		}
                    		ownerQuery.updateAvailableConcepts();
                    	}
                    }
                }
            });
        } else
        {
        	schemaItemCBX = null;
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
            isEnforcedCkbx = createCheckBox("isEnforcedCkbx");
            dcn = validator.hookupComponent(isEnforcedCkbx, "iecb",
                    UIValidator.Type.Changed, "", true);
            isEnforcedCkbx.addActionListener(dcn);
            isEnforcedCkbx.addFocusListener(focusListener);
            isEnforcedCkbx.addKeyListener(enterListener);
        }
        if (!this.ownerQuery.isPromptMode())
        {
            isDisplayedCkbx = createCheckBox("isDisplayedCkbx");
            dcn = validator.hookupComponent(isDisplayedCkbx, "idcb",
                    UIValidator.Type.Changed, "", true);
            isDisplayedCkbx.addFocusListener(focusListener);
            isDisplayedCkbx.addKeyListener(enterListener);
            isDisplayedCkbx.addActionListener(dcn);
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
            dcn = validator.hookupComponent(isPromptCkbx, "ipcb",
                    UIValidator.Type.Changed, "", true);
            isPromptCkbx.addActionListener(dcn);
            isPromptCkbx.addFocusListener(focusListener);
            isPromptCkbx.addKeyListener(enterListener);
            closeBtn = edu.ku.brc.ui.UIHelper.createIconBtn("Close", "QB_REMOVE_FLD", new ActionListener()
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
            this.isEnforcedCkbx = null;            
            this.closeBtn = null;
    	}

        JComponent[] qComps = {iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, isDisplayedCkbx, isPromptCkbx, isEnforcedCkbx, closeBtn, null };
        JComponent[] sComps = { schemaItemCBX, iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria,
                sortCheckbox, isDisplayedCkbx, isEnforcedCkbx, closeBtn, null };
        // 0 1 2 3 4 5 6 7 8 9
        if (schemaMapping == null)
        {
        	comps = qComps;
        }
        else
        {
        	comps = sComps;
        }
        
        StringBuilder sb = new StringBuilder();
        Integer[] qFixedCmps = {300};
        Integer[] sFixedCmps = {268, 300};
        Integer[] fixedCmps;
        if (schemaMapping != null) 
        {
        	fixedCmps = sFixedCmps;
        } else
        {
        	fixedCmps = qFixedCmps;
        }
        if (columnDefStr == null)
        {
            for (int i = 0; i < comps.length; i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (isCenteredComp(i))
                    sb.append("c:");
                if (i >= fixedCmps.length)
                {
                	sb.append("p");
                }
                else
                {
                	sb.append(fixedCmps[i] + "px");
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
			boolean isTreeLevel = fieldQRI instanceof TreeLevelQRI;
			isNotCheckbox.setVisible(!isRel || pickList != null);
			operatorCBX.setVisible(!isRel || pickList != null);
			criteria.setVisible((!isRel && !isBool) || pickList != null);
			if (schemaMapping != null)
			{
				this.sortCheckbox.setVisible(!(isTreeLevel || isRel));
			}
			else
			{
				if (!isRel)
				{
					this.sortCheckbox.setVisible(true);
				} else
				{
					this.sortCheckbox
						.setVisible(((RelQRI) fieldQRI).getRelationshipInfo()
								.getType() != RelationshipType.OneToMany);
				}
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
            if (this.schemaMapping == null)
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
        if (schemaMapping == null)
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
    	return schemaMapping == null ? compIdx == 4 : compIdx == 5;
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
    
    /**
     * @return true if field is displayed in results
     */
    public boolean isForDisplay()
    {
        if (ownerQuery.isPromptMode()) {
        	if (queryField != null) {
        		return queryField.getIsDisplay();
        	} else {
        		return true;
        	}
        } else {
        	return isDisplayedCkbx.isSelected();
        }
    }
    
    /**
     * @return the field label text
     */
    public String getLabel()
    {
        return this.fieldLabel.getText();
    }

    /**
     * @return the schemaItem combo box
     */
    public JComboBox getSchemaItemCBX()
    {
    	return this.schemaItemCBX;
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
     * @return true if nulls are allowed. i.e: if 'or is null' should be appended to the field's criteria.
     */
    public boolean isAllowNulls()
    {
    	//XXX until/if allowNulls is added to general queries, the isEnforcedCkbx is being used to 
    	//access it
    	if (isEnforcedCkbx != null) 
        {
            return isEnforcedCkbx.isSelected();
        }
        if (queryField != null)
        {
            return queryField.getAllowNulls();
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
	 * @return the autoMapped
	 */
	public boolean isAutoMapped() 
	{
		return autoMapped;
	}


	/**
	 * @param autoMapped the autoMapped to set
	 */
	public void setAutoMapped(boolean autoMapped) 
	{
		this.autoMapped = autoMapped;
	}
    
	
}
