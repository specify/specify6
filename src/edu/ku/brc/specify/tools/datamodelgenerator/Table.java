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
package edu.ku.brc.specify.tools.datamodelgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.tools.schemalocale.LocalizedStrIFace;

/**
 * Create table data.
 *
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Table implements Comparable<Table>
{
    private String                   name;
    private String                   table;
    private String                   lazy;
    private String                   tableId;
    private boolean                  searchable;
    private String                   businessRule;
    private String                   abbrv;
    private List<Field>              fields        = new ArrayList<Field>();
    private List<Id>                 ids           = new ArrayList<Id>();
    private List<Relationship>       relationships = new ArrayList<Relationship>();
    private Display                  display;
    private List<FieldAlias>         fieldAliases = new ArrayList<FieldAlias>();

    
    // These are needed for the DataMode Tools for generating schema diagrams etc
    private LocalizedStrIFace        desc;
    private LocalizedStrIFace        nameDesc;
    
    // Transient
    private Vector<TableIndex>       indexes           = new Vector<TableIndex>();
    private HashMap<String, Boolean> likeManyToOneHash = null;
    
    
    /**
     * @param name
     * @param table
     * @param lazy
     * @param tableId
     * @param display
     * @param fieldAliases
     * @param searchable
     * @param businessRule
     * @param abbrv
     */
    public Table(String name, 
                 String table, 
                 String lazy,
                 String tableId, 
                 Display display,
                 List<FieldAlias> fieldAliases,
                 boolean searchable, 
                 String businessRule,
                 String abbrv)
    {
        this.name      = name;
        this.table     = table;
        this.lazy      = lazy;
        this.tableId   = tableId;
        this.display   = display;
        this.fieldAliases = fieldAliases;
        this.searchable = searchable;
        this.businessRule = businessRule;
        this.abbrv     = abbrv;
    }

    /**
     * @return
     * String
     */
    public String getLazy()
    {
        return lazy;
    }

    /**
     * @param lazy
     * void
     */
    public void setLazy(String lazy)
    {
        this.lazy = lazy;
    }

    /**
     * @return
     * String
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return
     * String
     */
    public String getTableId()
    {
        return tableId;
    }

    /**
     * Returns the name portion before the ".".
     * @param nameArg the name to be shortened
     * @return the shortened name
     */
    protected String createShortName(String nameArg)
    {
        int inx = nameArg.lastIndexOf('.');
        if (inx != -1)
        {
            return nameArg.substring(inx + 1, nameArg.length());
        }
        return nameArg;
    }


    /**
     * @param name
     * void
     */
    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * @return
     * String
     */
    public String getTable()
    {
        return table;
    }

    /**
     * @param table
     * void
     */
    public void setTable(String table)
    {
        this.table = table;
    }

    /**
     * @param tableId
     * void
     */
    public void setTableId(String tableId)
    {
        this.tableId = tableId;
    }

    public void addField(Field aField)
    {
        fields.add(aField);
    }

    /**
     * @param rel
     * void
     */
    public void addRelationship(Relationship rel)
    {
        relationships.add(rel);
    }


    /**
     * @param anId
     * void
     */
    public void addId(Id anId)
    {
        ids.add(anId);
    }


    /**
     * @return
     * Collection<Field>
     */
    public Collection<Field> getFields()
    {
        Collections.sort(fields);
        
        return fields;
    }


    /**
     * @return
     * Collection<Id>
     */
    public Collection<Id> getIds()
    {
        return ids;
    }

    /**
     * @return
     * Collection<Relationship>
     */
    public Collection<Relationship> getRelationships()
    {
        Collections.sort(relationships);
        return relationships;
    }

    public Display getDisplay()
    {
        return display;
    }

    public boolean isSearchable()
    {
        return searchable;
    }
    
    public boolean getSearchable()
    {
        return searchable;
    }
    
    public String getBusinessRule()
    {
        return businessRule;
    }
    
    /**
     * @return the indexes
     */
    public Vector<TableIndex> getIndexes()
    {
        return indexes;
    }

    /**
     * @param indexes the indexes to set
     */
    public void setIndexes(Vector<TableIndex> indexes)
    {
        this.indexes = indexes;
    }
    
    /**
     * @return the desc
     */
    public LocalizedStrIFace getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(LocalizedStrIFace desc)
    {
        this.desc = desc;
    }

    public void updateIndexFields()
    {
        Collections.sort(fields);
        
        for (TableIndex ti : indexes)
        {
            for (String columnName : ti.getColumnNames())
            {
                for (Field f : fields)
                {
                    if (f.getColumn().equals(columnName))
                    {
                        f.setIndexName(ti.getIndexName());
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * @return the abbrv
     */
    public String getAbbrv()
    {
        return abbrv;
    }

    /**
     * @param abbrv the abbrv to set
     */
    public void setAbbrv(String abbrv)
    {
        this.abbrv = abbrv;
    }

    /**
     * @param nameDesc the nameDesc to set
     */
    public void setNameDesc(LocalizedStrIFace nameDesc)
    {
        this.nameDesc = nameDesc;
    }

    /**
     * @return the nameDesc
     */
    public LocalizedStrIFace getNameDesc()
    {
        return nameDesc;
    }

    /**
     * @return the fieldAliases
     */
    public List<FieldAlias> getFieldAliases()
    {
        return fieldAliases;
    }

    /**
     * @param fieldAliases the fieldAliases to set
     */
    public void setFieldAliases(List<FieldAlias> fieldAliases)
    {
        this.fieldAliases = fieldAliases;
    }
    
    /**
     * @param relName
     * @return
     */
    public boolean getIsLikeManyToOne(final String relName)
    {
        if (likeManyToOneHash != null)
        {
            Boolean isLike = likeManyToOneHash.get(relName);
            return isLike != null ? isLike : false;
        }
        return false;
    }
    
    /**
     * @param likeManyToOneHash the likeManyToOneHash to set
     */
    public void setLikeManyToOneHash(HashMap<String, Boolean> likeManyToOneHash)
    {
        this.likeManyToOneHash = likeManyToOneHash;
    }

    // Comparable
    public int compareTo(Table obj)
    {
        if (obj == null)
        {
            return 0;
        }
        return name.compareTo(obj.name);
    }
}
