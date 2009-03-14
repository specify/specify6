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

package edu.ku.brc.af.ui.forms.formatters;

import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author Ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 13, 2008
 *
 */
public class AvailableFieldsComponent
{
    protected DBTableInfo               tableInfo;
    protected Hashtable<String, Object> fieldsByName = new Hashtable<String, Object>();
    protected DataObjFieldFormatMgr     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr       uiFieldFormatterMgrCache;
    
    protected JTree                     availableFieldsTree;
    
    /**
     * Constructor
     */
    public AvailableFieldsComponent(final DBTableInfo           tableInfo, 
                                    final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                    final UIFieldFormatterMgr   uiFieldFormatterMgrCache)
    {
        this.tableInfo                  = tableInfo;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        
        setupAvailableFieldsTree();
    }
    
    /**
     * Wraps Creates a DBFieldInfoWrapper from information passed in the parameter
     * @param field The field being wrapped
     * @return an instance of DBFieldInfoWrapper class wrapping information about a given data obj field 
     */
    public static DataObjDataFieldWrapper wrapFieldInfo(DataObjDataField field)
    {
        if (field == null)
        {
            return null;
        }
        
        return new DataObjDataFieldWrapper(field);
    }

    /**
     * Recovers DataObjDataField from wrapper
     * @param fieldInfoWrapper object wrapping the field information
     * @return DataObjDataField instance
     */
    public static DataObjDataField unwrap(Object fieldInfoWrapper)
    {
        if (!(fieldInfoWrapper instanceof DataObjDataFieldWrapper))
        {
            return null;
        }
        
        DataObjDataFieldWrapper fiWrapper = (DataObjDataFieldWrapper) fieldInfoWrapper;
        return fiWrapper.getFormatterField();
    }
    
    /**
     * 
     */
    public JTree getTree()
    {
        return availableFieldsTree;
    }

    /**
     * 
     * @param ma
     */
    public void addMouseListener(MouseListener ma)
    {
        availableFieldsTree.addMouseListener(ma);
    }
    
    public Object getHashEntry(String key)
    {
        return fieldsByName.get(key);
    }
    
    public boolean containsKey(String key)
    {
        return fieldsByName.containsKey(key);
    }
    
    /**
     * 
     * @param localTableInfo
     * @return
     */
    protected void setupAvailableFieldsTree()
    {
        fieldsByName.clear();
        // stack to store the path from the initial to the current table
        // used to generate the appropriate name for the fields (
        Stack<DBInfoBase> tableStack = new Stack<DBInfoBase>();
        DefaultMutableTreeNode root = createTreeNodeRecursive(tableStack, tableInfo, 1, 4);
        availableFieldsTree = new JTree(root);
        // only one field can be selected at a time
        availableFieldsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
    
    /**
     * 
     * @param infoBase
     * @param currentLevel
     * @param maxLevel
     * @return
     */
    protected DefaultMutableTreeNode createTreeNodeRecursive(Stack<DBInfoBase> tableStack, 
                                                             final DBInfoBase  infoBase, 
                                                             final int         currentLevel, 
                                                             final int         maxLevel)
    {    
        if (infoBase == null || infoBase.isHidden())
        {
            return null;
        }

        DBTableInfo        currentTableInfo = null;
        DBRelationshipInfo relInfo          = null; 
        
        if (infoBase instanceof DBTableInfo)
        {
            currentTableInfo = (DBTableInfo) infoBase;
            
        } else if (infoBase instanceof DBRelationshipInfo)
        {
            relInfo = (DBRelationshipInfo) infoBase;
            currentTableInfo = DBTableIdMgr.getInstance().getByClassName(relInfo.getClassName());
            
            if (currentTableInfo == null)
            {
                return null;
            }
        } else
        {
            // can't really do anything with DBFieldInfo or any other sub-type
            return null;
        }
        
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(infoBase);
        
        if (currentLevel >= maxLevel)
        {
            return node;
        }
        
        // add fields from this table
        for (DBFieldInfo field : currentTableInfo.getFields()) 
        {
            if (field.isHidden()) 
            {
                break;
            }
            
            DataObjDataField dataField = new DataObjDataField(getFieldNameFromStack(tableStack, field.getName()), 
                    field.getDataClass(), null, "", null, null);
            dataField.setDbInfo(currentTableInfo, field, relInfo);
            DataObjDataFieldWrapper fieldWrapper = new DataObjDataFieldWrapper(dataField);
            node.add(new DefaultMutableTreeNode(fieldWrapper)); // leaf (field)
            fieldsByName.put(fieldWrapper.toString(), fieldWrapper);
            
            // add UI field formatters for this field here
            List<UIFieldFormatterIFace> formatters = uiFieldFormatterMgrCache.getFormatterList(field.getTableInfo().getClassObj(), field.getName());
            for (UIFieldFormatterIFace formatter : formatters)
            {
                dataField = new DataObjDataField(getFieldNameFromStack(tableStack, field.getName()), 
                        field.getDataClass(), null, "", null, formatter.getName());
                dataField.setDbInfo(currentTableInfo, field, relInfo);
                fieldWrapper = new DataObjDataFieldWrapper(dataField);
                node.add(new DefaultMutableTreeNode(fieldWrapper)); // leaf (data obj formatter)
                fieldsByName.put(fieldWrapper.toString(), fieldWrapper);
            }
        }

        // get formatters for this table (only if it is a relationship to another table)
        // XXX: check if the following code is correct: is this the right place to include data obj formatters?
        if (relInfo != null) 
        {
            Class<?> clazz = currentTableInfo.getClassObj();
            List<DataObjSwitchFormatter> formatters;
            formatters = dataObjFieldFormatMgrCache.getFormatterList(clazz);
            for (DataObjSwitchFormatter formatter : formatters)
            {
                DataObjDataField dataField = new DataObjDataField(getFieldNameFromStack(tableStack, relInfo.getName()), 
                        clazz, null, "", formatter.getName(), null);
                dataField.setDbInfo(currentTableInfo, null, relInfo);
                DataObjDataFieldWrapper fieldWrapper = new DataObjDataFieldWrapper(dataField);
                node.add(new DefaultMutableTreeNode(fieldWrapper)); // leaf (data obj formatter)
                fieldsByName.put(fieldWrapper.toString(), fieldWrapper);
            }
        }

        // get fields from relationship tables recursively
        for (DBRelationshipInfo rel : currentTableInfo.getRelationships())
        {
            if (rel.isHidden()) 
            {
                break;
            }
            
            // add sub-tree corresponding to the fields (and relationships) from the current relationship table
            tableStack.add(rel);
            DefaultMutableTreeNode child = createTreeNodeRecursive(tableStack, rel, currentLevel + 1, maxLevel);
            if (child != null)
            {
                node.add(child);
            }
        }
        
        return node;
    }

    private String getFieldNameFromStack(final Stack<DBInfoBase> tableStack, final String fieldName)
    {
        StringBuilder sb = new StringBuilder();
        for (DBInfoBase infoBase : tableStack) 
        {
            sb.append(infoBase.getName() + ".");
        }
        sb.append(fieldName);
        return sb.toString();
    }
}
