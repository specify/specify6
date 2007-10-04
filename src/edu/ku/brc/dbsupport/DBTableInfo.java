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
/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 3, 2007
 *
 */
public class DBTableInfo extends DBInfoBase
{
    protected static final Logger log = Logger.getLogger(DBTableInfo.class);
    protected int      tableId;
    protected String   className;
    protected String   primaryKeyName;
    protected Class<?> classObj;
    protected boolean  isSearchable       = false;
    protected String   businessRule;
    
    // ID Fields
    protected String idColumnName;
    protected String idFieldName;
    protected String idType;
    
    // Display Items
    protected String defaultFormName;
    protected String uiFormatter;
    protected String dataObjFormatter;
    protected String searchDialog;
    protected String newObjDialog;
    
    protected Set<DBRelationshipInfo> relationships;
    protected List<DBFieldInfo>       fields;

    public DBTableInfo(final int    tableId, 
                       final String className, 
                       final String tableName, 
                       final String primaryKeyName)
    {
        super(tableName);
        
        this.tableId = tableId;
        this.className = className;
        this.primaryKeyName = primaryKeyName;
        try
        {
            this.classObj = Class.forName(className);
            //Class.
        } catch (ClassNotFoundException e)
        {
            log.error("Trying to find class: " + className + " but class was not found");
            //e.printStackTrace();
        }
        relationships = new HashSet<DBRelationshipInfo>();
        fields        = new Vector<DBFieldInfo>();
    }
    
    public void cleanUp()
    {
        relationships.clear();
        fields.clear();
    }

    public String getShortClassName()
    {
        // TODO: replace calls to this with a call to classObj.getSimpleName();
        
        int inx = className.lastIndexOf('.');
        return inx == -1 ? className : className.substring(inx + 1);
    }

    public int getTableId()
    {
        return tableId;
    }

    public String getClassName()
    {
        return className;
    }

    public String getPrimaryKeyName()
    {
        return primaryKeyName;
    }

    public Class<?> getClassObj()
    {
        return classObj;
    }

    public String getDefaultFormName()
    {
        return defaultFormName;
    }
    
    public void setDefaultFormName(String defaultFormName)
    {
        this.defaultFormName = defaultFormName;
    }

    public Set<DBRelationshipInfo> getRelationships()
    {
        return relationships;
    }

    public ImageIcon getIcon(IconManager.IconSize size)
    {
        return IconManager.getIcon(getShortClassName(), size);
    }
    
    public String getDataObjFormatter()
    {
        return dataObjFormatter;
    }

    public void setDataObjFormatter(String dataObjFormatter)
    {
        this.dataObjFormatter = dataObjFormatter;
    }

    public String getUiFormatter()
    {
        return uiFormatter;
    }

    public void setUiFormatter(String uiFormatter)
    {
        this.uiFormatter = uiFormatter;
    }

    public String getNewObjDialog()
    {
        return newObjDialog;
    }

    public void setNewObjDialog(String newObjDialog)
    {
        this.newObjDialog = newObjDialog;
    }

    public String getEditObjDialog()
    {
        return getNewObjDialog();
    }

    public void setEditObjDialog(String editObjDialog)
    {
        setNewObjDialog(editObjDialog);
    }

    public String getSearchDialog()
    {
        return searchDialog;
    }

    public void setSearchDialog(String searchDialog)
    {
        this.searchDialog = searchDialog;
    }

    public String getIdColumnName()
    {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName)
    {
        this.idColumnName = idColumnName;
    }

    public String getIdFieldName()
    {
        return idFieldName;
    }

    public void setIdFieldName(String idFieldName)
    {
        this.idFieldName = idFieldName;
    }

    public String getIdType()
    {
        return idType;
    }

    public void setIdType(String idType)
    {
        this.idType = idType;
    }

    public boolean isSearchable()
    {
        return isSearchable;
    }

    public void setIsSearchable(boolean isSearchable)
    {
        this.isSearchable = isSearchable;
    }

    public String getBusinessRule()
    {
        return businessRule;
    }

    public void setBusinessRule(String busniessRule)
    {
        this.businessRule = busniessRule;
    }

    /**
     * Assumes all fields have names and returns a DBFieldInfo object by name
     * @param name the name of the field
     * @return the DBFieldInfo
     */
    public DBFieldInfo getFieldByName(final String nameArg)
    {
        for (DBFieldInfo fldInfo : fields)
        {
            if (fldInfo.getName().equals(nameArg))
            {
                return fldInfo;
            }
        }
        return null;
    }

    public DBRelationshipInfo getRelationshipByName(final String nameArg)
    {
        for (DBRelationshipInfo tr: relationships)
        {
            String relName = tr.getName();
            if (relName != null && relName.equals(nameArg))
            {
                return tr;
            }
        }
        return null;
    }

    public DBRelationshipInfo.RelationshipType getRelType(final String fieldName)
    {
        for (Iterator<DBRelationshipInfo> iter = relationships.iterator();iter.hasNext();)
        {
            DBRelationshipInfo tblRel = iter.next();
            if (tblRel.getName().equals(fieldName))
            {
                return tblRel.getType();
            }
        }
        return null;
    }
    
    public void addField(final DBFieldInfo fieldInfo)
    {
        fields.add(fieldInfo);
    }
    
    public List<DBFieldInfo> getFields()
    {
        return fields;
    }

}
