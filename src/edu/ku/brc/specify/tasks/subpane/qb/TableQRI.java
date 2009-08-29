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

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.tasks.subpane.qb.DateAccessorQRI.DATEPART;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class TableQRI extends ExpandableQRI
{
    protected static final Logger log = Logger.getLogger(TableQRI.class);
    
    protected DBRelationshipInfo relationship = null;
    protected boolean            relChecked = false;


    /**
     * @param tableTree
     */
    public TableQRI(final TableTree tableTree)
    {
        super(tableTree);
        determineRel(); //probably not necessary
    }
        
    /**
     * @param fieldInfo the field to add
     */
    public void addField(final DBFieldInfo fieldInfo)
    {
        fields.add(new FieldQRI(this, fieldInfo));
        if (Calendar.class.isAssignableFrom(fieldInfo.getDataClass()))
        {
        	if (addDateAccessors(fieldInfo))
        	{
        		fields.add(new DateAccessorQRI(this, fieldInfo, DATEPART.NumericDay));
        		fields.add(new DateAccessorQRI(this, fieldInfo, DATEPART.NumericMonth));
        		fields.add(new DateAccessorQRI(this, fieldInfo, DATEPART.NumericYear));
        	}
        }        		
    }
    
    /**
     * @param fieldInfo
     * @return true if Day,Month,Year accessors should be added for the (assumed) date
     * field represented by fieldInfo.
     * 
     */
    protected boolean addDateAccessors(final DBFieldInfo fieldInfo)
    {
    	return fieldInfo.getTableInfo().getClassObj().equals(CollectingEvent.class)
    	  || fieldInfo.getTableInfo().getClassObj().equals(Determination.class);
    }
    
    /**
     * @param fieldQRI the field to add
     */
    public void addField(final FieldQRI fieldQRI)
    {
        fieldQRI.setTable(this);
        fields.add(fieldQRI);
    }
    
    /**
     * @param fieldQRI the field whose clone to add
     * @throws CloneNotSupportedException
     */
    public void addFieldClone(final FieldQRI fieldQRI) throws CloneNotSupportedException
    {
        FieldQRI newField = (FieldQRI)fieldQRI.clone();
        newField.setTable(this);
        fields.add(newField);
    }
    
    /**
     * @return relationship re
     */
    protected void determineRel()
    {
        Class<?> classObj = this.getTableTree().getTableInfo().getClassObj();
        List<DBRelationshipInfo> rels = new LinkedList<DBRelationshipInfo>();
        if (getTableTree().getParent() != null && getTableTree().getParent().getTableInfo() != null)
        {
            for (DBRelationshipInfo rel : this.getTableTree().getParent().getTableInfo()
                    .getRelationships())
            {
                if (rel.getDataClass().equals(classObj) && isRelevantRel(rel, classObj))
                {
                    rels.add(rel);
                }
            }
            if (rels.size() == 1) 
            { 
                relationship = rels.get(0); 
                return;
            }
            if (rels.size() > 1)
            {
                if (tableTree.getField() != null)
                {
                    for (DBRelationshipInfo rel : rels)
                    {
                        if (rel.getName().equalsIgnoreCase(tableTree.getField()))
                        {
                            relationship = rel;
                            return;
                        }
                    }
                }
            }
            if (relationship == null && isLocToCollectingEventsLink())
            {
            	relationship = buildLocToCollectingEventsRel();
            }
            if (relationship == null)
            {
            	log.error("Unable to determine relationship for " + this.getTableTree().getField() + " <-> " 
            			+ getTableTree().getParent().getField());
            }
        }
    }

    /**
     * @return one-to-many relationship for Locality->CollectingEvents
     * 
     * The Locality->CollectingEvents relationship was removed from the Hibernate schema for the db due to performance problems 
     * (during data entry, I believe). This method creates a 'description' of the relationship for use by the query builder.
     * 
     * HOWEVER, in order for this strategy to work, the QueryBuilder would have to use SQL, because HQL does not support 
     * "...JOIN CollectingEvent ce ON ce.LocalityId = loc0.localityId" syntax. So for now, the Locality-CollectingEvent relationship
     * is commented out of the QueryBuilder config file (querybuilder.xml).
     */
    protected DBRelationshipInfo buildLocToCollectingEventsRel()
    {
    	return new DBRelationshipInfo("collectingEvents", DBRelationshipInfo.RelationshipType.OneToMany,
    			CollectingEvent.class.getName(), null, "locality", null, false, false, false);
    }
    
    /**
     * @return true if this object represents CollectingEvents associated with Locality.
     */
    protected boolean isLocToCollectingEventsLink()
    {
    	return tableTree.getParent() != null 
    		&& tableTree.getParent().getTableInfo().getClassObj().equals(Locality.class)
    		&& getTableTree().getTableInfo().getClassObj().equals(CollectingEvent.class) 
    		&& tableTree.getField().equals("collectingEvents");
    }
    
    /**
     * @param rel
     * @param classObj
     * @return false if rel represents a 'system' relationship.
     */
    protected boolean isRelevantRel(final DBRelationshipInfo rel, final Class<?> classObj)
    {
        if (classObj.equals(edu.ku.brc.specify.datamodel.Agent.class))
        {
            if (rel.getColName() == null)
            {
                return true;
            }
            if (!rel.getColName().equalsIgnoreCase("modifiedbyagentid") && !rel.getColName().equalsIgnoreCase("createdbyagentid"))
            {
                return !tableTree.getField().equals("modifiedByAgent") && !tableTree.getField().equals("createdByAgent");
            }
            return (!rel.getColName().equalsIgnoreCase("modifiedbyagentid") || tableTree.getField().equals("modifiedByAgent")) && 
                    (!rel.getColName().equalsIgnoreCase("createdbyagentid") || tableTree.getField().equals("createdByAgent"));
        }
        return true;
    }

    /**
     * @return the relationship
     */
    public DBRelationshipInfo getRelationship()
    {
        if (relationship == null && !relChecked)
        {
            determineRel();
            relChecked = true;
        }
        return relationship;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#getTitle()
     */
    @Override
    public String getTitle()
    {
        if (relationship == null)
        {
            return super.getTitle();
        }
        return relationship.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#setTableTree(edu.ku.brc.specify.tasks.subpane.qb.TableTree)
     */
    @Override
    public void setTableTree(TableTree tableTree)
    {
        super.setTableTree(tableTree);
        determineRel();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#hasMultiChildren()
     */
    @Override
    public boolean hasMultiChildren()
    {
        return relationship != null && 
               (relationship.getType() == DBRelationshipInfo.RelationshipType.OneToMany ||
                relationship.getType() == DBRelationshipInfo.RelationshipType.ManyToMany);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TableQRI result = (TableQRI)super.clone();
        result.fields = new Vector<FieldQRI>(fields.size());
        for (FieldQRI f : fields)
        {
            result.addFieldClone(f);
        }
        return result;
    }
}
