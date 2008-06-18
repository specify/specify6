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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class RelQRI extends FieldQRI
{
    protected DBRelationshipInfo relationshipInfo;
    
    /**
     * @param parent
     * @param ri
     */
    public RelQRI(final TableQRI parent, final DBRelationshipInfo ri)
    {
        super(parent, null);

        this.relationshipInfo = ri;

        try
        {
            iconName = Class.forName(ri.getClassName()).getSimpleName();
            title = ri.getTitle();
            if (StringUtils.isEmpty(title))
            {
                title = UIHelper.makeNamePretty(iconName);
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            iconName = "BlankIcon";
            title = "????";
        }
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#setIsInUse(java.lang.Boolean)
     */
    @Override
    public void setIsInUse(Boolean isInUse)
    {
        // TODO Auto-generated method stub
        super.setIsInUse(isInUse);
        table.setIsInUse(isInUse);
    }

    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        if (fi != null)
        {
            return super.getFieldName();
        }
        if (relationshipInfo != null)
        {
            return relationshipInfo.getName();
        }
        return table.getTableInfo().getName();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getTableInfo()
     */
    @Override
    public DBTableInfo getTableInfo()
    {
        return table.getTableInfo();    
    }

    protected String deCapitalize(final String toDecap)
    {
        return toDecap.substring(0, 1).toLowerCase().concat(toDecap.substring(1));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getSQLFldSpec(edu.ku.brc.specify.tasks.subpane.qb.TableAbbreviator)
     */
    @Override
    public String getSQLFldSpec(TableAbbreviator ta, final boolean forWhereClause)
    {
        if (relationshipInfo.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany) /*What about ManyToMany?? And some OneToOnes???*/)
        {
            return ta.getAbbreviation(table.getTableTree().getParent()) + "." + relationshipInfo.getOtherSide() + "Id";
        }
        //else ManyToOnes.   Is this OK for all OneToOnes too?
        return ta.getAbbreviation(table.getTableTree()) + "." + deCapitalize(table.getTableInfo().getClassObj().getSimpleName()) + "Id";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#hasChildren()
     */
    @Override
    public boolean hasChildren()
    {
        return true;
    }


    /**
     * @return the relationshipInfo
     */
    public DBRelationshipInfo getRelationshipInfo()
    {
        return relationshipInfo;
    }
    
    /**
     * @param lbl
     * @return lbl with extra info about formatting or aggregation removed
     */
    public static String stripDescriptiveStuff(final String lbl)
    {
        if (lbl.endsWith(" " + UIRegistry.getResourceString("QB_AGGREGATED")))
        {
           return lbl.substring(0, lbl.length() - UIRegistry.getResourceString("QB_AGGREGATED").length() - 1);
        
        }   
        //else
        if (lbl.endsWith(" " + UIRegistry.getResourceString("QB_FORMATTED")))
        {
            return lbl.substring(0, lbl.length() - UIRegistry.getResourceString("QB_FORMATTED").length() - 1);
        }
        //else
        return lbl;
    }
}