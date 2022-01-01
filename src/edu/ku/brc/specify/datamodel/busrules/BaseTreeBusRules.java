/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionMember;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.TreeDefItemStandardEntry;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace;
import edu.ku.brc.specify.dbsupport.TreeDefStatusMgr;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod 
 *
 * (original author was JDS)
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 * @param <T>
 * @param <D>
 * @param <I>
 */
public abstract class BaseTreeBusRules<T extends Treeable<T,D,I>,
                                       D extends TreeDefIface<T,D,I>,
                                       I extends TreeDefItemIface<T,D,I>>
                                       extends AttachmentOwnerBaseBusRules
{
    public static final boolean ALLOW_CONCURRENT_FORM_ACCESS = true;
	public static final long FORM_SAVE_LOCK_MAX_DURATION_IN_MILLIS = 60000;
	private static final Logger log = Logger.getLogger(BaseTreeBusRules.class);
    
    private boolean processedRules = false;
    
    /**
     * Constructor.
     * 
     * @param dataClasses a var args list of classes that this business rules implementation handles
     */
    public BaseTreeBusRules(Class<?>... dataClasses)
    {
        super(dataClasses);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        GetSetValueIFace  parentField  = (GetSetValueIFace)formViewObj.getControlByName("parent");
        Component comp = formViewObj.getControlByName("definitionItem");
        if (comp instanceof ValComboBox)
        {
            final ValComboBox rankComboBox = (ValComboBox)comp;
    
            final JCheckBox            acceptedCheckBox     = (JCheckBox)formViewObj.getControlByName("isAccepted");
            Component apComp = formViewObj.getControlByName("acceptedParent");
            final ValComboBoxFromQuery acceptedParentWidget = apComp instanceof ValComboBoxFromQuery ?
            		(ValComboBoxFromQuery )apComp : null;
            
            
            if (parentField instanceof ValComboBoxFromQuery)
            {
                final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery)parentField;
                if (parentCBX != null && rankComboBox != null)
                {
                    parentCBX.addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            if (e == null || !e.getValueIsAdjusting())
                            {
                                parentChanged(formViewObj, parentCBX, rankComboBox, acceptedCheckBox, acceptedParentWidget);
                            }
                        }
                    });
                    rankComboBox.getComboBox().addActionListener(new ActionListener(){
                    	@Override
                    	public void actionPerformed(ActionEvent e)
                    	{
                    		rankChanged(formViewObj, parentCBX, rankComboBox, acceptedCheckBox, acceptedParentWidget);
                    	}
                    });
                }
            }
    
            
            if (acceptedCheckBox != null && acceptedParentWidget != null)
            {
                acceptedCheckBox.addItemListener(new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        if (acceptedCheckBox.isSelected())
                        {
                            acceptedParentWidget.setValue(null, null);
                            acceptedParentWidget.setChanged(true); // This should be done automatically
                            acceptedParentWidget.setEnabled(false);
                        }
                        else
                        {
                            acceptedParentWidget.setEnabled(true);
                        }
                    }
                });
            }
        }
    }

    /**
     * @return list of foreign key relationships for purposes of checking
     * if a record can be deleted. 
     * The list contains two entries for each relationship. The first entry
     * is the related table name. The second is the name of the foreign key field in the related table.
     */
    public abstract String[] getRelatedTableAndColumnNames();
    
    /**
    * @return list of ass foreign key relationships. 
    * The list contains two entries for each relationship. The first entry
    * is the related table name. The second is the name of the foreign key field in the related table.
    */
    public String[] getAllRelatedTableAndColumnNames()
    {
    	return getRelatedTableAndColumnNames();
    }
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
	 */
    @SuppressWarnings("unchecked")
	@Override
	public boolean okToEnableDelete(Object dataObj)
	{
        // This is a little weak and chessey, but it gets the job done.
        // Becase both the Tree and Definition want/need to share Business Rules.
        String viewName = formViewObj.getView().getName();
        if (StringUtils.contains(viewName, "TreeDef"))
        {
            final I treeDefItem = (I)dataObj;
            if (treeDefItem != null && treeDefItem.getTreeDef() != null)
            {
            	return treeDefItem.getTreeDef().isRequiredLevel(treeDefItem.getRankId());
            }
        }
        return super.okToEnableDelete(dataObj);
	}

	/**
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean okToDeleteNode(T node)
    {
        if (node.getDefinition() != null && !node.getDefinition().getNodeNumbersAreUpToDate() && !node.getDefinition().isUploadInProgress())
        {
            //Scary. If nodes are not up to date, tree rules may not work.
            //The application should prevent edits to items/trees whose tree numbers are not up to date except while uploading
            //workbenches.
            throw new RuntimeException(node.getDefinition().getName() + " has out of date node numbers.");
        }
        
        if (node.getDefinition() != null && node.getDefinition().isUploadInProgress())
        {
            //don't think this will ever get called during an upload/upload-undo, but just in case.
            return true;
        }
        
        Integer id = node.getTreeId();
        if (id == null)
        {
            return true;
        }
        String[] relationships = getRelatedTableAndColumnNames();

        // if the given node can't be deleted, return false
        if (!super.okToDelete(relationships, node.getTreeId()))
        {
            return false;
        }

        // now check the children

        // get a list of all descendent IDs
        DataProviderSessionIFace session = null;
        List<Integer> childIDs = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            String queryStr = "SELECT n.id FROM " + node.getClass().getName() + " n WHERE n.nodeNumber <= :highChild AND n.nodeNumber > :nodeNum ORDER BY n.rankId DESC";
            QueryIFace query = session.createQuery(queryStr, false);
            query.setParameter("highChild", node.getHighestChildNodeNumber());
            query.setParameter("nodeNum", node.getNodeNumber());
            childIDs = (List<Integer>)query.list();
            
        } catch (Exception ex)
        {
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeBusRules.class, ex);
            // Error Dialog
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        // if there are no descendent nodes, return true
        if (childIDs != null && childIDs.size() == 0)
        {
            return true;
        }

        // break the descendent checks up into chunks or queries
        
        // This is an arbitrary number.  Trial and error will determine a good value.  This determines
        // the number of IDs that wind up in the "IN" clause of the query run inside okToDelete().
        int chunkSize = 250;
        int lastRecordChecked = -1;

        boolean childrenDeletable = true;
        while (lastRecordChecked  + 1 < childIDs.size() && childrenDeletable)
        {
            int startOfChunk = lastRecordChecked + 1;
            int endOfChunk = Math.min(lastRecordChecked+1+chunkSize, childIDs.size());

            // grabs selected subset, exclusive of the last index
            List<Integer> chunk = childIDs.subList(startOfChunk, endOfChunk);
            
            Integer[] idChunk = chunk.toArray(new Integer[1]);
            childrenDeletable = super.okToDelete(relationships, idChunk);
            
            lastRecordChecked = endOfChunk - 1;
        }
        return childrenDeletable;
    }
    
    
    @Override
	protected String getExtraWhereColumns(DBTableInfo tableInfo) {
		String result = super.getExtraWhereColumns(tableInfo);
		if (CollectionMember.class.isAssignableFrom(tableInfo.getClassObj()))
		{
			Vector<Object> cols = BasicSQLUtils.querySingleCol("select distinct CollectionID from collection "
					+ "where DisciplineID = " + AppContextMgr.getInstance().getClassObject(Discipline.class).getId());
			if (cols != null)
			{
				String colList = "";
				for (Object col : cols)
				{
					if (!"".equals(colList))
					{
						colList += ",";
					}
					colList += col;
				}
				if (!"".equals(colList))
				{
					result = "((" + result + ") or " + tableInfo.getAbbrev() + ".CollectionMemberID in(" + colList + "))";
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
    protected void rankChanged(final FormViewObj form,
            final ValComboBoxFromQuery parentComboBox, 
            final ValComboBox rankComboBox,
            final JCheckBox acceptedCheckBox,
            final ValComboBoxFromQuery acceptedParentWidget)
    {
        if (form.getAltView().getMode() != CreationMode.EDIT)
        {
            return;
        }
        
        //log.debug("form was validated: calling adjustRankComboBoxModel()");
        
        Object objInForm = form.getDataObj();
        //log.debug("form data object = " + objInForm);
        if (objInForm == null)
        {
            return;
        }
        
        final T formNode = (T)objInForm;
        T parent = null;
        if (parentComboBox.getValue() instanceof String)
        {
            // the data is still in the VIEW mode for some reason
            log.debug("Form is in mode (" + form.getAltView().getMode() + ") but the parent data is a String");
            parentComboBox.getValue();
            parent = formNode.getParent();
        }
        else
        {
            parent = (T)parentComboBox.getValue();
        }
        
        final T theParent = parent;
        I rankObj = (I )rankComboBox.getValue();
        final int rank = rankObj == null ? -2 : rankObj.getRankId();
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
            	boolean canSynonymize = false;
            	if (canAccessSynonymy(formNode, rank))
                {
                    canSynonymize = formNode.getDefinition() != null && formNode.getDefinition()
                    	.getSynonymizedLevel() <= rank
                    	&& formNode.getDescendantCount() == 0;

                }
            	if (acceptedCheckBox != null && acceptedParentWidget != null)
            	{
            		acceptedCheckBox.setEnabled(canSynonymize && theParent != null);
            		if (acceptedCheckBox.isSelected() && acceptedCheckBox.isEnabled())
            		{
            			acceptedParentWidget.setValue(null, null);
            			acceptedParentWidget.setChanged(true); // This should be done automatically
            			acceptedParentWidget.setEnabled(false);
            		}
            	}
                form.getValidator().validateForm();
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    protected void parentChanged(final FormViewObj form, 
            					 final ValComboBoxFromQuery parentComboBox, 
                                 final ValComboBox rankComboBox,
                                 final JCheckBox acceptedCheckBox,
                                 final ValComboBoxFromQuery acceptedParentWidget)
    {
        if (form.getAltView().getMode() != CreationMode.EDIT)
        {
            return;
        }
        
        //log.debug("form was validated: calling adjustRankComboBoxModel()");
        
        Object objInForm = form.getDataObj();
        //log.debug("form data object = " + objInForm);
        if (objInForm == null)
        {
            return;
        }
        
        final T formNode = (T)objInForm;
        
        // set the contents of this combobox based on the value chosen as the parent
        adjustRankComboBoxModel(parentComboBox, rankComboBox, formNode);

        T parent = null;
        if (parentComboBox.getValue() instanceof String)
        {
            // the data is still in the VIEW mode for some reason
            log.debug("Form is in mode (" + form.getAltView().getMode() + ") but the parent data is a String");
            parentComboBox.getValue();
            parent = formNode.getParent();
        }
        else
        {
            parent = (T)parentComboBox.getValue();
        }
        
        // set the tree def for the object being edited by using the parent node's tree def
        // set the parent too??? (lookups for the AcceptedParent QueryComboBox need this) 
        if (parent != null)
        {
            formNode.setDefinition(parent.getDefinition());
            formNode.setParent(parent);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                boolean rnkEnabled = rankComboBox.getComboBox().getModel().getSize() > 0;
                rankComboBox.setEnabled(rnkEnabled);
                JLabel label = form.getLabelFor(rankComboBox);
                if (label != null)
                {
                    label.setEnabled(rnkEnabled);
                }
                if (rankComboBox.hasFocus() && !rnkEnabled)
                {
                    parentComboBox.requestFocus();
                }
                rankChanged(formViewObj, parentComboBox, rankComboBox, acceptedCheckBox, acceptedParentWidget);
                form.getValidator().validateForm();
            }
        });

    }
    
    /**
     * @param parentField
     * @param rankComboBox
     * @param nodeInForm
     */
    @SuppressWarnings("unchecked")
    protected void adjustRankComboBoxModel(final GetSetValueIFace parentField, 
                                           final ValComboBox rankComboBox, 
                                           final T nodeInForm)
    {
        log.debug("Adjusting the model for the 'rank' combo box in a tree node form");
        if (nodeInForm == null)
        {
            return;
        }
        log.debug("nodeInForm = " + nodeInForm.getName());
        
        DefaultComboBoxModel<I> model = (DefaultComboBoxModel<I>)rankComboBox.getModel();
        model.removeAllElements();

        // this is the highest rank the edited item can possibly be
        I topItem = null;
        // this is the lowest rank the edited item can possibly be
        I bottomItem = null;

        Object value = parentField.getValue();
        T parent = null;
        if (value instanceof String)
        {
            // this happens when the combobox is in view mode, which means it's really a textfield
            // in that case, the parent of the node in the form will do, since the user can't change the parents
            parent = nodeInForm.getParent();
        }
        else
        {
            parent = (T)parentField.getValue();
        }
        
        if (parent == null)
        {
            return;
        }

        // grab all the def items from just below the parent's item all the way to the next enforced level
        // or to the level of the highest ranked child
        topItem = parent.getDefinitionItem().getChild();
        log.debug("highest valid tree level: " + topItem);
        
        if (topItem == null)
        {
            // this only happens if a parent was chosen that cannot have children b/c it is at the
            // lowest defined level in the tree
            log.warn("Chosen node cannot be a parent node.  It is at the lowest defined level of the tree.");
            return;
        }

        // find the child with the highest rank and set that child's def item as the bottom of the range
        if (!nodeInForm.getChildren().isEmpty())
        {
            for (T child: nodeInForm.getChildren())
            {
                if (bottomItem==null || child.getRankId()>bottomItem.getRankId())
                {
                    bottomItem = child.getDefinitionItem().getParent();
                }
            }
        }
        
        log.debug("lowest valid tree level:  " + bottomItem);

        I item = topItem;
        boolean done = false;
        while (!done)
        {
            model.addElement(item);

            if (item.getChild()==null || item.getIsEnforced()==Boolean.TRUE || (bottomItem != null && item.getRankId().intValue()==bottomItem.getRankId().intValue()) )
            {
                done = true;
            }
            item = item.getChild();
        }
        
        if (nodeInForm.getDefinitionItem() != null)
        {
            I defItem = nodeInForm.getDefinitionItem();
            for (int i = 0; i < model.getSize(); ++i)
            {
                I modelItem = (I)model.getElementAt(i);
                if (modelItem.getRankId().equals(defItem.getRankId()))
                {
                    log.debug("setting rank selected value to " + modelItem);
                    model.setSelectedItem(modelItem);
                }
            }
//            if (model.getIndexOf(defItem) != -1)
//            {
//                model.setSelectedItem(defItem);
//            }
        }
        else if (model.getSize() == 1)
        {
            Object defItem = model.getElementAt(0);
            log.debug("setting rank selected value to the only available option: " + defItem);
            model.setSelectedItem(defItem);
        }
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterFillForm(final Object dataObj)
    {
        // This is a little weak and cheesey, but it gets the job done.
        // Because both the Tree and Definition want/need to share Business Rules.
        String viewName = formViewObj.getView().getName();
        if (StringUtils.contains(viewName, "TreeDef"))
        {
            if (formViewObj.getAltView().getMode() != CreationMode.EDIT)
            {
                // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
                //log.debug("form is not in edit mode: no special listeners will be attached");
                return;
            }
            
            if (!StringUtils.contains(viewName, "TreeDefItem"))
            {
            	return;
            }
            
            final I nodeInForm = (I)formViewObj.getDataObj();
            //disable FullName -related fields if TreeDefItem is used by nodes in the tree
        	//NOTE: Can remove the edit restriction. Tree rebuilds now update fullname fields. Need to add tree rebuild after fullname def edits.
            if (nodeInForm != null && nodeInForm.getTreeDef() != null)
            {
//            	boolean canNOTEditFullNameFlds = nodeInForm.hasTreeEntries();
//            	if (canNOTEditFullNameFlds)
//            	{
//            		ValTextField ftCtrl = (ValTextField )formViewObj.getControlByName("textAfter");
//            		if (ftCtrl != null)
//            		{
//            			ftCtrl.setEnabled(false);
//            		}
//            		ftCtrl = (ValTextField )formViewObj.getControlByName("textBefore");
//            		if (ftCtrl != null)
//            		{
//            			ftCtrl.setEnabled(false);
//            		}
//            		ftCtrl = (ValTextField )formViewObj.getControlByName("fullNameSeparator");
//            		if (ftCtrl != null)
//            		{
//            			ftCtrl.setEnabled(false);
//            		}
//            		ValCheckBox ftBox = (ValCheckBox )formViewObj.getControlByName("isInFullName");
//            		if (ftBox != null)
//            		{
//            			ftBox.setEnabled(false);
//            		}
//            	}
            
            	if (!viewName.endsWith("TreeDefItem"))
            	{
            		return;
            	}
            	
            	//disabling editing of name and rank for standard levels.
                List<TreeDefItemStandardEntry> stds = nodeInForm.getTreeDef().getStandardLevels();
                TreeDefItemStandardEntry stdLevel = null;
                for (TreeDefItemStandardEntry std : stds)
                {
                   //if (std.getTitle().equals(nodeInForm.getName()) && std.getRank() == nodeInForm.getRankId())
                   if (std.getRank() == nodeInForm.getRankId())
                    {
                        stdLevel = std;
                        break;
                    }
                }
                if (stdLevel != null)
                {
                    ValTextField nameCtrl = (ValTextField )formViewObj.getControlByName("name");
                    Component rankCtrl = formViewObj.getControlByName("rankId");
                    if (nameCtrl != null)
                    {
                        nameCtrl.setEnabled(false);
                    }
                    if (rankCtrl != null)
                    {
                        rankCtrl.setEnabled(false);
                    }
                    if (nodeInForm.getTreeDef().isRequiredLevel(stdLevel.getRank()))
                    {
                        Component enforcedCtrl = formViewObj.getControlByName("isEnforced");
                        if (enforcedCtrl != null)
                        {
                        	enforcedCtrl.setEnabled(false);
                        }
                    }
                }
            }
            return;
        }
        
        final T nodeInForm = (T) formViewObj.getDataObj();
        
        if (formViewObj.getAltView().getMode() != CreationMode.EDIT) 
        {
            if (nodeInForm != null)
            {
                //XXX this MAY be necessary due to a bug with TextFieldFromPickListTable??
                // TextFieldFromPickListTable.setValue() does nothing because of a null adapter member.
                Component comp = formViewObj.getControlByName("definitionItem");
                if (comp instanceof JTextField)
                {
                    ((JTextField )comp).setText(nodeInForm.getDefinitionItem().getName());
                }
            }
        }
        else
        {
            processedRules = false;
        	GetSetValueIFace parentField = (GetSetValueIFace) formViewObj
                    .getControlByName("parent");
            Component comp = formViewObj.getControlByName("definitionItem");
            if (comp instanceof ValComboBox)
            {
                final ValComboBox rankComboBox = (ValComboBox) comp;

                if (parentField instanceof ValComboBoxFromQuery)
                {
                    final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery) parentField;
                    if (parentCBX != null && rankComboBox != null && nodeInForm != null)
                    {
                    	parentCBX.registerQueryBuilder(new TreeableSearchQueryBuilder(nodeInForm,
                                rankComboBox, TreeableSearchQueryBuilder.PARENT));
                    }
                }

                if (nodeInForm != null && nodeInForm.getDefinitionItem() != null)
                {
                    // log.debug("node in form already has a set rank: forcing a call to
                    // adjustRankComboBoxModel()");
                    UIValidator.setIgnoreAllValidation(this, true);
                    adjustRankComboBoxModel(parentField, rankComboBox, nodeInForm);
                    UIValidator.setIgnoreAllValidation(this, false);
                }

                // TODO: the form system MUST require the accepted parent widget to be present if
                // the
                // isAccepted checkbox is present
                final JCheckBox acceptedCheckBox = (JCheckBox) formViewObj
                        .getControlByName("isAccepted");
                final ValComboBoxFromQuery acceptedParentWidget = (ValComboBoxFromQuery) formViewObj
                        .getControlByName("acceptedParent");
                if (canAccessSynonymy(nodeInForm))
				{
					if (acceptedCheckBox != null
							&& acceptedParentWidget != null)
					{
						if (acceptedCheckBox.isSelected() && nodeInForm != null
								&& nodeInForm.getDefinition() != null)
						{
							// disable if necessary
							boolean canSynonymize = nodeInForm.getDefinition()
									.getSynonymizedLevel() <= nodeInForm
									.getRankId()
									&& nodeInForm.getDescendantCount() == 0;
							acceptedCheckBox.setEnabled(canSynonymize);
						}
						acceptedParentWidget.setEnabled(!acceptedCheckBox
								.isSelected()
								&& acceptedCheckBox.isEnabled());
						if (acceptedCheckBox.isSelected())
						{
							acceptedParentWidget.setValue(null, null);
						}

						if (nodeInForm != null && acceptedParentWidget != null
								&& rankComboBox != null)
						{
							acceptedParentWidget
									.registerQueryBuilder(new TreeableSearchQueryBuilder(
											nodeInForm,
											rankComboBox,
											TreeableSearchQueryBuilder.ACCEPTED_PARENT));
						}
					}
				}
                else
                {
                    if (acceptedCheckBox != null)
                    {
                        acceptedCheckBox.setEnabled(false);
                    }
                    if (acceptedParentWidget != null)
                    {
                        acceptedParentWidget.setEnabled(false);
                    }
                }

                if (parentField instanceof ValComboBoxFromQuery)
                {
                    parentChanged(formViewObj, (ValComboBoxFromQuery) parentField, rankComboBox,
                    		acceptedCheckBox, acceptedParentWidget);
                }
            }
        }
    }

    /**
     * @param tableInfo
     * 
     * @return Select (i.e. everything before where clause) of sqlTemplate
     */
    protected String getSqlSelectTemplate(final DBTableInfo tableInfo)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select %s1 FROM "); //$NON-NLS-1$
        sb.append(tableInfo.getClassName());
        sb.append(" as "); //$NON-NLS-1$
        sb.append(tableInfo.getAbbrev());
        
        String joinSnipet = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, true, null, false); //arg 2: false means SQL
        if (joinSnipet != null)
        {
            sb.append(' ');
            sb.append(joinSnipet);
        }
        sb.append(' ');
        return sb.toString();
    }
    
    /**
     * @param dataObj
     * 
     * return true if acceptedParent and accepted fields should be enabled on data forms.
     */
    @SuppressWarnings("unchecked")
    protected boolean canAccessSynonymy(final T dataObj)
    {
        if (dataObj == null)
        {
            return false; //??
        }
        
        if (dataObj.getChildren().size() > 0)
        {
            return false;
        }
        
        TreeDefItemIface<?,?,?> defItem = dataObj.getDefinitionItem();
        if (defItem == null)
        {
            return false; //??? 
        }
        TreeDefIface<?,?,?> def = dataObj.getDefinition();
        if (def == null)
        {
            def = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass((Class<? extends Treeable<?,?,?>> )dataObj.getClass()); 
        }
        
        if (!def.isSynonymySupported())
        {
        	return false;
        }
        
        return defItem.getRankId() >= def.getSynonymizedLevel();
    }
    
    /**
     * @param dataObj
     * @param rank
     * @return true if the rank is synonymizable according to the relevant TreeDefinition
     * 
     * For use when dataObj's rank has not yet been assigned or updated.
     */
    @SuppressWarnings("unchecked")
    protected boolean canAccessSynonymy(final T dataObj, final int rank)
    {
        if (dataObj == null)
        {
            return false; //??
        }
        
        if (dataObj.getChildren().size() > 0)
        {
            return false;
        }
        TreeDefIface<?,?,?> def =  ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass((Class<? extends Treeable<?,?,?>> )dataObj.getClass()); 
        
        if (!def.isSynonymySupported())
        {
        	return false;
        }
        
        return rank >= def.getSynonymizedLevel();
    }
    /**
     * Updates the fullname field of any nodes effected by changes to <code>node</code> that are about
     * to be saved to the DB.
     * 
     * @param node
     * @param session
     * @param nameChanged
     * @param parentChanged
     * @param rankChanged
     */
    @SuppressWarnings("unchecked")
    protected void updateFullNamesIfNecessary(T node, DataProviderSessionIFace session)
    {        
    	if (!(node.getDefinition().getDoNodeNumberUpdates() && node.getDefinition().getNodeNumbersAreUpToDate())) {
        	return;
        }
    	
        if (node.getTreeId() == null)
        {
            // this is a new node
            // it shouldn't need updating since we set the fullname at creation time
            return;
        }
        
        boolean updateNodeFullName = false;
        boolean updateDescFullNames = false;

        // we need a way to determine if the name changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        T fromDB = (T)tmpSession.get(node.getClass(), node.getTreeId());
        tmpSession.close();
        
        if (fromDB == null)
        {
            // this node is new and hasn't yet been flushed to the DB, so we don't need to worry about updating fullnames
            //return;
            fromDB = node;
        }

        T       origParent    = fromDB.getParent();
        boolean parentChanged = false;
        T       currentParent = node.getParent();
        if ((currentParent == null && origParent != null) || (currentParent != null && origParent == null))
        {
            // I can't imagine how this would ever happen, but just in case
            parentChanged = true;
        }
        if (currentParent != null && origParent != null && !currentParent.getTreeId().equals(origParent.getTreeId()))
        {
            // the parent ID changed
            parentChanged = true;
        }
        
        boolean higherLevelsIncluded = false;
        if (parentChanged)
        {
            higherLevelsIncluded = higherLevelsIncludedInFullname(node);
            higherLevelsIncluded |= higherLevelsIncludedInFullname(fromDB);
        }
        
        if (parentChanged && higherLevelsIncluded)
        {
            updateNodeFullName = true;
            updateDescFullNames = true;
        }
        
        boolean nameChanged = !(fromDB.getName().equals(node.getName()));
        boolean rankChanged = !(fromDB.getRankId().equals(node.getRankId()));
        if (rankChanged || nameChanged)
        {
            updateNodeFullName = true;
            if (booleanValue(fromDB.getDefinitionItem().getIsInFullName(), false) == true)
            {
                updateDescFullNames = true;
            }
            if (booleanValue(node.getDefinitionItem().getIsInFullName(), false) == true)
            {
                updateDescFullNames = true;
            }
        } else if (fromDB == node)
        {
            updateNodeFullName = true;
        }

        if (updateNodeFullName)
        {
            if (updateDescFullNames)
            {
                // this could take a long time
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
            else
            {
                // this should be really fast
                String fullname = TreeHelper.generateFullname(node);
                node.setFullName(fullname);
            }
        }
    }

    protected boolean higherLevelsIncludedInFullname(T node)
    {
        boolean higherLevelsIncluded = false;
        // this doesn't necessarily mean the fullname has to be changed
        // if no higher levels are included in the fullname, then nothing needs updating
        // so, let's see if higher levels factor into the fullname
        T l = node.getParent();
        while (l != null)
        {
            if ((l.getDefinitionItem().getIsInFullName() != null) && 
                    (l.getDefinitionItem().getIsInFullName().booleanValue() == true))
            {
                higherLevelsIncluded = true;
                break;
            }
            l = l.getParent();
        }
        
        return higherLevelsIncluded;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Treeable)
        {
           // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;
            
            if (!node.getDefinition().getNodeNumbersAreUpToDate() && !node.getDefinition().isUploadInProgress())
            {
                //Scary. If nodes are not up to date, tree rules may not work (actually this one is OK. (for now)). 
                //The application should prevent edits to items/trees whose tree numbers are not up to date except while uploading
                //workbenches.
                throw new RuntimeException(node.getDefinition().getName() + " has out of date node numbers.");
            }
           
            // set it's fullname
            String fullname = TreeHelper.generateFullname(node);
            node.setFullName(fullname);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean beforeSaveCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        
        // PLEASE NOTE!
        // If any changes are made to this check to make sure no one (Like GeologicTimePeriod) is overriding this method
        // and make the appropriate changes there also.
        if (!super.beforeSaveCommit(dataObj, session))
        {
            return false;
        }
        
        boolean success = true;
        
        // compare the dataObj values to the nodeBeforeSave values to determine if a node was moved or added
        if (dataObj instanceof Treeable)
        {
            // NOTE: the instanceof check can't check against 'T' since T isn't a class
            //       this has a SMALL amount of risk to it
            T node = (T)dataObj;

            if (!node.getDefinition().getNodeNumbersAreUpToDate() && !node.getDefinition().isUploadInProgress())
            {
                //Scary. If nodes are not up to date, tree rules may not work.
                //The application should prevent edits to items/trees whose tree numbers are not up to date except while uploading
                //workbenches.
                throw new RuntimeException(node.getDefinition().getName() + " has out of date node numbers.");
            }

            // if the node doesn't have any assigned node number, it must be new
            boolean added = (node.getNodeNumber() == null);

            if (node.getDefinition().getDoNodeNumberUpdates() && node.getDefinition().getNodeNumbersAreUpToDate())
            {
                log.info("Saved tree node was added.  Updating node numbers appropriately.");
                TreeDataService<T,D,I> dataServ = TreeDataServiceFactory.createService();
                if (added)
                {
                	success = dataServ.updateNodeNumbersAfterNodeAddition(node, session);
                }
                else
                {
                	success = dataServ.updateNodeNumbersAfterNodeEdit(node, session);
                }
            }
            else 
            {
                node.getDefinition().setNodeNumbersAreUpToDate(false);
            }
        }
        
        return success;
    }
    
    
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
    /*
     * NOTE: If this method is overridden, freeLocks() MUST be called when result is false
     * !!
     * 
     */
	@Override
	public boolean beforeDeleteCommit(Object dataObj,
			DataProviderSessionIFace session) throws Exception
	{
		if (!super.beforeDeleteCommit(dataObj, session))
		{
			return false;
		}
		if (dataObj != null && (formViewObj == null || !StringUtils.contains(formViewObj.getView().getName(), "TreeDef")) && 
				BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS && viewable != null)
		{
			return getRequiredLocks(dataObj);
		}
		else
		{
			return true;
		}
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterDeleteCommit(Object dataObj)
    {
        try
		{
			if (dataObj instanceof Treeable)
			{
				// NOTE: the instanceof check can't check against 'T' since T
				// isn't a class
				// this has a SMALL amount of risk to it
				T node = (T) dataObj;

				if (!node.getDefinition().getNodeNumbersAreUpToDate()
						&& !node.getDefinition().isUploadInProgress())
				{
					// Scary. If nodes are not up to date, tree rules may not
					// work.
					// The application should prevent edits to items/trees whose
					// tree numbers are not up to date except while uploading
					// workbenches.
					throw new RuntimeException(node.getDefinition().getName()
							+ " has out of date node numbers.");
				}

				if (node.getDefinition().getDoNodeNumberUpdates()
						&& node.getDefinition().getNodeNumbersAreUpToDate())
				{
					log
							.info("A tree node was deleted.  Updating node numbers appropriately.");
					TreeDataService<T, D, I> dataServ = TreeDataServiceFactory
							.createService();
					// apparently a refresh() is necessary. node can hold
					// obsolete values otherwise.
					// Possibly needs to be done for all business rules??
					DataProviderSessionIFace session = null;
					try
					{
						session = DataProviderFactory.getInstance()
								.createSession();
						// rods - 07/28/08 commented out because the node is
						// already deleted
						// session.refresh(node);
						dataServ.updateNodeNumbersAfterNodeDeletion(node,
								session);

					} catch (Exception ex)
					{
						edu.ku.brc.exceptions.ExceptionTracker.getInstance()
								.capture(BaseTreeBusRules.class, ex);
						ex.printStackTrace();

					} finally
					{
						if (session != null)
						{
							session.close();
						}
					}

				} else
				{
					node.getDefinition().setNodeNumbersAreUpToDate(false);
				}
			}
		} finally
		{
			if (BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS && viewable != null)
			{
				this.freeLocks();
			}
		}
    }

    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link TreeDefItemIface}.  The real work of this method is to
     * update the 'fullname' field of all {@link Treeable} objects effected by the changes
     * to the passed in {@link TreeDefItemIface}.
     *
     * @param defItem the {@link TreeDefItemIface} being saved
     */
    @SuppressWarnings("unchecked")
    protected void beforeSaveTreeDefItem(I defItem)
    {
        // we need a way to determine if the 'isInFullname' value changed
        // load a fresh copy from the DB and get the values needed for comparison
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        I fromDB = (I)tmpSession.load(defItem.getClass(), defItem.getTreeDefItemId());
        tmpSession.close();

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(defItem);

        boolean changeThisLevel = false;
        boolean changeAllDescendants = false;
        
        boolean fromDBIsInFullname = makeNotNull(fromDB.getIsInFullName());
        boolean currentIsInFullname = makeNotNull(defItem.getIsInFullName());
        if (fromDBIsInFullname != currentIsInFullname)
        {
            changeAllDescendants = true;
        }
        
        // look for changes in the 'textBefore', 'textAfter' or 'fullNameSeparator' fields
        String fromDbBeforeText = makeNotNull(fromDB.getTextBefore());
        String fromDbAfterText = makeNotNull(fromDB.getTextAfter());
        String fromDbSeparator = makeNotNull(fromDB.getFullNameSeparator());
        
        String before = makeNotNull(defItem.getTextBefore());
        String after = makeNotNull(defItem.getTextAfter());
        String separator = makeNotNull(defItem.getFullNameSeparator());
        
        boolean textFieldChanged = false;
        boolean beforeChanged = !before.equals(fromDbBeforeText);
        boolean afterChanged = !after.equals(fromDbAfterText);
        boolean sepChanged = !separator.equals(fromDbSeparator);
        if (beforeChanged || afterChanged || sepChanged)
        {
            textFieldChanged = true;
        }
        
        if (textFieldChanged)
        {
            if (currentIsInFullname)
            {
                changeAllDescendants = true;
            }
            changeThisLevel = true;
        }
        
        if (changeThisLevel && !changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                String generated = TreeHelper.generateFullname(node);
                node.setFullName(generated);
            }
        }
        else if (changeThisLevel && changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                TreeHelper.fixFullnameForNodeAndDescendants(node);
            }
        }
        else if (!changeThisLevel && changeAllDescendants)
        {
            Set<T> levelNodes = defItem.getTreeEntries();
            for (T node: levelNodes)
            {
                // grab all child nodes and go from there
                for (T child: node.getChildren())
                {
                    TreeHelper.fixFullnameForNodeAndDescendants(child);
                }
            }
        }
        // else don't change anything
        
        session.close();
    }
    
    protected boolean booleanValue(Boolean bool, boolean defaultIfNull)
    {
        if (bool != null)
        {
            return bool.booleanValue();
        }
        return defaultIfNull;
    }
    
    /**
     * Converts a null string into an empty string.  If the provided String is not
     * null, it is returned unchanged.
     * 
     * @param s a string
     * @return the string or " ", if null
     */
    private String makeNotNull(String s)
    {
        return (s == null) ? "" : s;
    }
    
    /**
     * Returns the provided {@link Boolean}, or <code>false</code> if null
     * 
     * @param b the {@link Boolean} to convert to non-null
     * @return the provided {@link Boolean}, or <code>false</code> if null
     */
    private boolean makeNotNull(Boolean b)
    {
        return (b == null) ? false : b.booleanValue();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public Object beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeDelete(dataObj, session);
        if (dataObj instanceof Treeable<?,?,?>)
        {
            Treeable<?, ?, ?> node = (Treeable<?,?,?> )dataObj;
            if (node.getAcceptedParent() != null)
            {
                node.getAcceptedParent().getAcceptedChildren().remove(node);
                node.setAcceptedParent(null);
            }
        }
        return dataObj;
    }

    
    /**
     * @param parentDataObj
     * @param dataObj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean parentHasChildWithSameName(final Object parentDataObj, final Object dataObj)
    {
        if (dataObj instanceof Treeable<?,?,?>)
        {
        	Treeable<T, D, I> node = (Treeable<T,D,I> )dataObj;
        	Treeable<T, D, I> parent = parentDataObj == null ? node.getParent() : (Treeable<T, D, I> )parentDataObj; 
        	if (parent != null)
        	{
                //XXX the sql below will only work if all Treeable tables use fields named 'isAccepted' and 'name' to store
        		//the name and isAccepted properties.
        		String tblName = DBTableIdMgr.getInstance().getInfoById(node.getTableId()).getName();
        		String sql = "SELECT count(*) FROM " + tblName + " where isAccepted "
        			+ "and name = " + BasicSQLUtils.getEscapedSQLStrExpr(node.getName());
        		if (parent.getTreeId() != null)
        		{
        			sql +=  " and parentid = " + parent.getTreeId();
        		}
                if (node.getTreeId() != null)
                {
                	sql += " and " + tblName + "id != " + node.getTreeId();
                }
                return BasicSQLUtils.getNumRecords(sql) > 0;
        	}
    	}
    	return false;
    }
    
    /**
     * @param parentDataObj
     * @param dataObj
     * @param isExistingObject
     * @return
     */
    @SuppressWarnings("unchecked")
    public STATUS checkForSiblingWithSameName(final Object parentDataObj, final Object dataObj,
    		final boolean isExistingObject)
    {
		STATUS result = STATUS.OK;
		if (parentHasChildWithSameName(parentDataObj, dataObj)) 
		{
            String parentName;
            if (parentDataObj == null) 
            {
            	parentName = ((Treeable<T,D,I> )dataObj).getParent().getFullName(); 
            }
            else
            {
            	parentName = ((Treeable<T,D,I> )parentDataObj).getFullName();
            }
			boolean saveIt = UIRegistry.displayConfirm(
							UIRegistry.getResourceString("BaseTreeBusRules.IDENTICALLY_NAMED_SIBLING_TITLE"), 
							String.format(UIRegistry.getResourceString("BaseTreeBusRules.IDENTICALLY_NAMED_SIBLING_MSG"), 
				            		parentName, ((Treeable<T,D,I> )dataObj).getName()),							
				            		UIRegistry.getResourceString("SAVE"), 
				            		UIRegistry.getResourceString("CANCEL"), 
				            		JOptionPane.QUESTION_MESSAGE);
			if (!saveIt) 
			{
				//Adding to reasonList prevents blank "Issue of Concern" popup -
				//but causes annoying second "duplicate child" nag.
				reasonList
						.add(UIRegistry
								.getResourceString("BaseTreeBusRules.IDENTICALLY_NAMED_SIBLING")); // XXX
																									// i18n
				result = STATUS.Error;
			}
		}
		return result;
    }

    /**
     * @param dataObj
     * @return OK if required data is present.
     * 
     * Checks for requirements that can't be defined in the database schema.
     */
    protected STATUS checkForRequiredFields(Object dataObj)
    {
		if (dataObj instanceof Treeable<?,?,?>)
		{
			STATUS result = STATUS.OK;
			Treeable<?,?,?> obj = (Treeable<?,?,?> )dataObj;
			if (obj.getParent() == null )
			{
				if (obj.getDefinitionItem() != null && obj.getDefinitionItem().getParent() == null)
				{
					//it's the root, null parent is OK.
					return result;
				}
				result = STATUS.Error;
				DBTableInfo info = DBTableIdMgr.getInstance().getInfoById(obj.getTableId());
				DBFieldInfo fld = info.getFieldByColumnName("Parent");
				String fldTitle = fld != null ? fld.getTitle() : UIRegistry.getResourceString("PARENT");
				reasonList.add(String.format(UIRegistry.getResourceString("GENERIC_FIELD_MISSING"), fldTitle));
			}
			//check that non-accepted node has an 'AcceptedParent'
			if (obj.getIsAccepted() == null || !obj.getIsAccepted() && obj.getAcceptedParent() == null)
			{
				result = STATUS.Error;
				DBTableInfo info = DBTableIdMgr.getInstance().getInfoById(obj.getTableId());
				DBFieldInfo fld = info.getFieldByColumnName("AcceptedParent");
				String fldTitle = fld != null ? fld.getTitle() : UIRegistry.getResourceString("ACCEPTED");
				reasonList.add(String.format(UIRegistry.getResourceString("GENERIC_FIELD_MISSING"), fldTitle));
			}
	    	return result;
		}
		return STATUS.None; //???
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
	 */
	@Override
	public STATUS processBusinessRules(Object parentDataObj, Object dataObj,
			boolean isExistingObject) 
	{
		reasonList.clear();
		STATUS result = STATUS.OK;
		if (!processedRules && dataObj instanceof Treeable<?, ?, ?>)
		{	
			result = checkForSiblingWithSameName(parentDataObj, dataObj, isExistingObject);
			if (result == STATUS.OK)
			{
				result = checkForRequiredFields(dataObj);
			}
			if (result == STATUS.OK)
			{
				processedRules = true;
			}
		}
		return result;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
    /*
     * NOTE: If this method is overridden, freeLocks() MUST be called when result is false
     * !!
     * 
     */
	@Override
	public boolean isOkToSave(Object dataObj, DataProviderSessionIFace session)
	{
		boolean result = super.isOkToSave(dataObj, session);
		if (result && dataObj != null && !StringUtils.contains(formViewObj.getView().getName(), "TreeDef") 
				&& BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS)
		{
			if (!getRequiredLocks(dataObj))
			{
				result = false;
				reasonList.add(getUnableToLockMsg());
			}
		}
		return result;
	}

	/**
	 * @return true if locks were aquired.
	 * 
	 * Locks necessary tables prior to a save.
	 * Only used when ALLOW_CONCURRENT_FORM_ACCESS is true.
	 */
	protected boolean getRequiredLocks(Object dataObj)
	{
		TreeDefIface<?,?,?> treeDef = ((Treeable<?,?,?>)dataObj).getDefinition();
		boolean result = !TreeDefStatusMgr.isRenumberingNodes(treeDef) && TreeDefStatusMgr.isNodeNumbersAreUpToDate(treeDef); 
		if (!result) {
			try {
				Thread.sleep(1500);
				result = !TreeDefStatusMgr.isRenumberingNodes(treeDef) && TreeDefStatusMgr.isNodeNumbersAreUpToDate(treeDef);
			} catch (Exception e) {
				result = false;
			}
		} 
		if (result) {
			TaskSemaphoreMgr.USER_ACTION r = TaskSemaphoreMgr.lock(getFormSaveLockTitle(), getFormSaveLockName(), "save", 
				TaskSemaphoreMgr.SCOPE.Discipline, false, new TaskSemaphoreMgrCallerIFace(){

					/* (non-Javadoc)
					 * @see edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace#resolveConflict(edu.ku.brc.specify.datamodel.SpTaskSemaphore, boolean, java.lang.String)
					 */
					@Override
					public USER_ACTION resolveConflict(
							SpTaskSemaphore semaphore,
							boolean previouslyLocked, String prevLockBy)
					{
						if (System.currentTimeMillis() - semaphore.getLockedTime().getTime() > FORM_SAVE_LOCK_MAX_DURATION_IN_MILLIS) {
							//something is clearly wrong with the lock. Ignore it and re-use it. It will be cleared when save succeeds.
							log.warn("automatically overriding expired " + getFormSaveLockTitle() + " lock set by " + 
									prevLockBy + " at " + DateFormat.getDateTimeInstance().format(semaphore.getLockedTime()));
							return USER_ACTION.OK;
						} else {
							return USER_ACTION.Error;
						}
					}
			
			}, false);
			result = r == TaskSemaphoreMgr.USER_ACTION.OK;
		}
		return result;
	}
	
	/**
	 * @return the class for the generic parameter <T>
	 */
	protected abstract Class<?> getNodeClass();
	
	/**
	 * @return the title for the form save lock.
	 */
	protected String getFormSaveLockTitle()
	{
		return String.format(UIRegistry.getResourceString("BaseTreeBusRules.SaveLockTitle"), getNodeClass().getSimpleName());
	}
	
	/**
	 * @return the name for the form save lock.
	 */
	protected String getFormSaveLockName()
	{
		return getNodeClass().getSimpleName() + "Save";
	}
	
	/**
	 * @return localized message to display in case of failure to lock for saving.
	 */
	protected String getUnableToLockMsg()
	{
		return UIRegistry.getResourceString("BaseTreeBusRules.UnableToLockForSave");
	}
	
	/**
	 * Free locks acquired for saving.
	 */
	protected void freeLocks()
	{
		TaskSemaphoreMgr.unlock(getFormSaveLockTitle(), getFormSaveLockName(), TaskSemaphoreMgr.SCOPE.Discipline);
	}
	
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public boolean afterSaveCommit(Object dataObj,
			DataProviderSessionIFace session)
	{
		boolean result = false;
		if (!super.afterSaveCommit(dataObj, session))
		{
			result = false;
		}
		if (BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS && viewable != null)
		{
			freeLocks();
		}
		return result;
	}

	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveFailure(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	public void afterSaveFailure(Object dataObj,
			DataProviderSessionIFace session)
	{
		super.afterSaveFailure(dataObj, session);
		if (BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS && viewable != null)
		{
			freeLocks();
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
	 */
	@Override
	public STATUS processBusinessRules(Object dataObj) {
		STATUS result = STATUS.OK;
		if (!processedRules)
		{
			result = super.processBusinessRules(dataObj);
			if (result == STATUS.OK)
			{
				result = checkForSiblingWithSameName(null, dataObj, false);
			}
			if (result == STATUS.OK)
			{
				result = checkForRequiredFields(dataObj);
			}
		}
		else
		{
			processedRules = false;
		}
		return result;
	}

    
    
}
