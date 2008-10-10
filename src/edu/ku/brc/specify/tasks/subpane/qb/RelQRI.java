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

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
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
        if (relationshipInfo.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany)
                || relationshipInfo.getType().equals(DBRelationshipInfo.RelationshipType.ZeroOrOne) /*What about ManyToMany?? And some OneToOnes???*/)
        {
            String name;
            if (StringUtils.isEmpty(relationshipInfo.getColName()) /*It should always be empty*/)
            {
                name = table.getTableTree().getParent().getTableInfo().getIdFieldName();
            }
            else //something is probably wrong but try this...
            {
                name = deCapitalize(relationshipInfo.getColName());
                if (name.endsWith("ID"))
                {
                    name = name.substring(0, name.length()-2) + "Id";
                }
            }
            return ta.getAbbreviation(table.getTableTree().getParent()) + "." + name;
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
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#isFieldHidden()
     */
    @Override
    public boolean isFieldHidden()
    {
        return relationshipInfo.isHidden();
    }


    //XXX - handling of the extra relationship text seems pretty scattered around. Should be re-worked. 
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
        if (lbl.endsWith("_" + UIRegistry.getResourceString("QB_AGGREGATED")))
        {
           return lbl.substring(0, lbl.length() - UIRegistry.getResourceString("QB_AGGREGATED").length() - 1);
        
        }   
        //else
        if (lbl.endsWith("_" + UIRegistry.getResourceString("QB_FORMATTED")))
        {
            return lbl.substring(0, lbl.length() - UIRegistry.getResourceString("QB_FORMATTED").length() - 1);
        }
        return lbl;
    }
}