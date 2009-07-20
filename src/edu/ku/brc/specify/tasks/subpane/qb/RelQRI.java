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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RelQRI.class, ex);
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

    /**
     * @return the DataObjDataFieldFormatter for the related table.
     */
    public DataObjDataFieldFormatIFace getDataObjFormatter()
    {
        DataObjSwitchFormatter sf = DataObjFieldFormatMgr.getInstance().getDataFormatter(getTableInfo().getShortClassName());
        if (sf != null && sf.isSingle())
        {
            return sf.getSingle();
        }
        return null;
    }
    
    protected String deCapitalize(final String toDecap)
    {
        return toDecap.substring(0, 1).toLowerCase().concat(toDecap.substring(1));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.FieldQRI#getSQLFldSpec(edu.ku.brc.specify.tasks.subpane.qb.TableAbbreviator)
     */
    @Override
    public String getSQLFldSpec(TableAbbreviator ta, final boolean forWhereClause, 
    		final boolean forSchemaExport)
    {
        if (relationshipInfo.getType().equals(DBRelationshipInfo.RelationshipType.OneToMany)
                || relationshipInfo.getType().equals(DBRelationshipInfo.RelationshipType.ZeroOrOne) /*What about ManyToMany?? And some OneToOnes???*/)
        {
            //XXX Formatter.getSingleField() checks for ZeroOrOne rels

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
        
        //If the formatter only uses one field, just retrieve that field with hql.
        //XXX Formatter.getSingleField() checks for OneToOne rels
        if (relationshipInfo.getType() == RelationshipType.ManyToOne)
        {
            DataObjDataFieldFormatIFace formatter = getDataObjFormatter();
            if (formatter != null)
            {
                String formatField = formatter.getSingleField();
                if (formatField != null)
                {
                    return ta.getAbbreviation(table.getTableTree()) + "." + formatField;
                }
            }
        }
        
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
        if (relationshipInfo.isHidden())
        {
            return true;
        }
        
        //else check if related table is hidden
        return DBTableIdMgr.getInstance().getInfoByTableName(relationshipInfo.getDataClass().getSimpleName().toLowerCase()).isHidden();
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
