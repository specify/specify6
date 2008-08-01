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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;

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
        
    public void addField(final DBFieldInfo fieldInfo)
    {
        fields.add(new FieldQRI(this, fieldInfo));
    }
    
    public void addField(final FieldQRI fieldQRI)
    {
        fieldQRI.setTable(this);
        fields.add(fieldQRI);
    }
    
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
        if (this.getTableTree().getParent() != null)
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
            if (relationship == null)
            {
                log.error("Unable to determine relationship for " + this.getTitle());
            }
        }
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