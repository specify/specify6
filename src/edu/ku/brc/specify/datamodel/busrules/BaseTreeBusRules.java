/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValComboBoxFromQuery;

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
                                       extends BaseBusRules
{
    private static final Logger log = Logger.getLogger(BaseTreeBusRules.class);
    
    protected boolean isAcceptedItemListenerInstalled = false;


    /**
     * Constructor.
     * 
     * @param dataClasses a var args list of classes that this business rules implementation handles
     */
    public BaseTreeBusRules(Class<?>... dataClasses)
    {
        super(dataClasses);
    }
    
    public abstract String[] getRelatedTableAndColumnNames();
    
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
            QueryIFace query = session.createQuery(queryStr);
            query.setParameter("highChild", node.getHighestChildNodeNumber());
            query.setParameter("nodeNum", node.getNodeNumber());
            childIDs = (List<Integer>)query.list();
            
        } catch (Exception ex)
        {
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
    
    @SuppressWarnings("unchecked")
    protected void parentChanged(final FormViewObj form, 
                                 final ValComboBoxFromQuery parentComboBox, 
                                 final ValComboBox rankComboBox)
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
        
        T formNode = (T)objInForm;
        
        // set the contents of this combobox based on the value chosen as the parent
        adjustRankComboBoxModel(parentComboBox, rankComboBox, formNode);
        
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
                form.getValidator().validateForm();
            }
        });

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
        if (parent != null)
        {
            formNode.setDefinition(parent.getDefinition());
        }
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
        log.debug("nodeInForm = " + nodeInForm.getName());
        if (nodeInForm == null)
        {
            return;
        }
        
        DefaultComboBoxModel model = (DefaultComboBoxModel)rankComboBox.getModel();
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
        if (formViewObj.getAltView().getMode() != CreationMode.EDIT)
        {
            // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
            //log.debug("form is not in edit mode: no special listeners will be attached");
            return;
        }

        // This is a little weak and chessey, but it gets the job done.
        // Becase both the Tree and Definition want/need to share Business Rules.
        String viewName = formViewObj.getView().getName();
        if (StringUtils.contains(viewName, "TreeDef"))
        {
            return;
        }
        
        final T nodeInForm = (T)formViewObj.getDataObj();

        GetSetValueIFace  parentField  = (GetSetValueIFace)formViewObj.getControlByName("parent");
        final ValComboBox rankComboBox = (ValComboBox)formViewObj.getControlByName("definitionItem");

        if (parentField instanceof ValComboBoxFromQuery)
        {
            final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery)parentField;
            if (parentCBX != null && rankComboBox != null)
            {
                parentCBX.registerQueryBuilder(new SearchQueryBuilder<T>(nodeInForm));
                
                parentCBX.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (e == null || !e.getValueIsAdjusting())
                        {
                            parentChanged(formViewObj, parentCBX, rankComboBox);
                        }
                    }
                });
            }
        }
        
        if (nodeInForm != null && nodeInForm.getDefinitionItem() != null)
        {
            //log.debug("node in form already has a set rank: forcing a call to adjustRankComboBoxModel()");
            UIValidator.setIgnoreAllValidation(this, true);
            adjustRankComboBoxModel(parentField, rankComboBox, nodeInForm);
            UIValidator.setIgnoreAllValidation(this, false);
        }
        
        // TODO: the form system MUST require the accepted parent widget to be present if the isAccepted checkbox is present
        final JCheckBox            acceptedCheckBox     = (JCheckBox)formViewObj.getControlByName("isAccepted");
        final ValComboBoxFromQuery acceptedParentWidget = (ValComboBoxFromQuery)formViewObj.getControlByName("acceptedParent");
        if (acceptedCheckBox != null && acceptedParentWidget != null)
        {
            acceptedParentWidget.setEnabled(!acceptedCheckBox.isSelected());
            if (acceptedCheckBox.isSelected())
            {
                acceptedParentWidget.setValue(null, null);
            }
            
            if (!isAcceptedItemListenerInstalled)
            {
                isAcceptedItemListenerInstalled = true;
                acceptedCheckBox.addItemListener(new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        if (acceptedCheckBox.isSelected())
                        {
                            acceptedParentWidget.setValue(null, null);
                        }
                    }
                });
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return false;
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
    protected void updateFullNamesIfNecessary(T node, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {        
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
           
            if (node.getFullName() == null)
            {
                // set it's fullname
                String fullname = TreeHelper.generateFullname(node);
                node.setFullName(fullname);
            }
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

            if (added && node.getDefinition().getDoNodeNumberUpdates() && node.getDefinition().getNodeNumbersAreUpToDate())
            {
                log.info("Saved tree node was added.  Updating node numbers appropriately.");
                TreeDataService dataServ = TreeDataServiceFactory.createService();
                success = dataServ.updateNodeNumbersAfterNodeAddition(node, session);
            }
            else if (added)
            {
                node.getDefinition().setNodeNumbersAreUpToDate(false);
            }
        }
        
        return success;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterDeleteCommit(Object dataObj)
    {
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

            if (node.getDefinition().getDoNodeNumberUpdates() && node.getDefinition().getNodeNumbersAreUpToDate())
            {
                log.info("A tree node was deleted.  Updating node numbers appropriately.");
                TreeDataService<T,D,I> dataServ = TreeDataServiceFactory.createService();
                //apparently a refresh() is necessary. node can hold obsolete values otherwise.
                //Possibly needs to be done for all business rules??
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.refresh(node);
                    dataServ.updateNodeNumbersAfterNodeDeletion(node,session);
                    
                } catch (Exception ex)
                {
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
            }
            else
            {
                node.getDefinition().setNodeNumbersAreUpToDate(false);
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

}
