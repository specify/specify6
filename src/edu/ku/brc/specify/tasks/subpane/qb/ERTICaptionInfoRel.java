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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjAggregator;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *This class is sort of a 'self-aggregating' or 'self-formatting' version of ERTICaptionInfo.
 *The values in the column represented are keys or foreign keys which, along with the relationship
 *property,  are used to obtain objects which are aggregated and formatted.
 *
 */
public class ERTICaptionInfoRel extends ERTICaptionInfoQB
{
    private static final Logger log = Logger.getLogger(ERTICaptionInfoRel.class);
   
    protected final DBRelationshipInfo relationship;
    /**
     * hql to retrieve list of related objects for one-to-many relationships.
     */
    protected final String listHql;
    
    protected final boolean useCache;
    protected final LookupsCache lookupCache;
    protected DataProviderSessionIFace session = null;
    protected final String processor; //the name of the formatter or aggregator for the column
    
    public ERTICaptionInfoRel(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex,
                           String colStringId,
                           DBRelationshipInfo relationship,
                           boolean useCache,
                           Integer cacheSize)
    {
        super(colName, colLabel, isVisible, uiFieldFormatter, posIndex, colStringId, null, null);
        //Don't like having a 'permanent' (until finalize()) session
        //maybe a done() method could be added that the owner of the cols could call... 
        //session = DataProviderFactory.getInstance().createSession();
        this.relationship = relationship;
        
        this.useCache = useCache;
        //this.useCache = false; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (this.useCache)
        {
            lookupCache = cacheSize == null ? new LookupsCache() : new LookupsCache(cacheSize);
        }
        else
        {
            lookupCache = null;
        }
        
        DBTableInfo otherSideTbl = DBTableIdMgr.getInstance().getByClassName(relationship.getClassName());
        if (relationship.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
        {
        	//processor = otherSideTbl.getAggregatorName();
            
            DBRelationshipInfo otherSideRel = otherSideTbl.getRelationshipByName(relationship.getOtherSide());
            String otherSideCol = otherSideRel.getColName();
            otherSideCol = otherSideCol.substring(0, 1).toLowerCase().concat(otherSideCol.substring(1));
            otherSideCol = otherSideCol.substring(0, otherSideCol.length()-2) + "Id";
            List<DataObjAggregator> aggs = DataObjFieldFormatMgr.getInstance().getAggregatorList(relationship.getDataClass());
            String orderByFld = null;
            String aggregator = null;
            for (DataObjAggregator agg : aggs)
            {
            	if (agg.isDefault())
            	{
            		orderByFld = agg.getOrderFieldName();
            		aggregator = agg.getName();
            		break;
            	}
            }
            processor = aggregator;
            listHql = "from " + relationship.getDataClass().getName() + " where " + otherSideCol + " = &id"
             + (StringUtils.isNotEmpty(orderByFld) ? " order by " + orderByFld : "");
        }
        else
        {
        	List<DataObjSwitchFormatter> forms = DataObjFieldFormatMgr.getInstance().getFormatterList(relationship.getDataClass());
            String formatter = null; 
        	for (DataObjSwitchFormatter form : forms)
        	{
        		if (form.isDefault()) 
        		{
        			formatter = form.getName();
        			break;
        		}
        	}
            processor = formatter;
        	listHql = null;
        }
        if (processor == null) 
        {
        	log.error("couldn't find formatter or aggregator for " + otherSideTbl.getName());
        }
    }
    
    /**
     * @return
     */
    public DBRelationshipInfo getRelationship()
    {
        return relationship;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getAggClass()
     */
    @Override
    public Class<?> getAggClass()
    {
        // all agg stuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getAggregatorName()
     */
    @Override
    public String getAggregatorName()
    {
        // all agg stuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getColClass()
     */
    @Override
    public Class<?> getColClass()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getDataObjFormatter()
     */
    @Override
    public DataObjSwitchFormatter getDataObjFormatter()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getSubClass()
     */
    @Override
    public Class<?> getSubClass()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getSubClassFieldName()
     */
    @Override
    public String getSubClassFieldName()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#getUiFieldFormatter()
     */
    @Override
    public UIFieldFormatterIFace getUiFieldFormatter()
    {
        // all agg/formatstuff is handled within this class
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.ERTICaptionInfo#processValue(java.lang.Object)
     */
    @Override
    public Object processValue(Object key)
    {
        if (processor != null)
        {
        	Object value = useCache ? lookupCache.lookupKey((Integer )key) : null;
        	if (value == null)
        	{
        		if (relationship.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
        		{
        			//value = DataObjFieldFormatMgr.getInstance().aggregate(getList(key), relationship.getDataClass());
        			value = DataObjFieldFormatMgr.getInstance().aggregate(getList(key), processor);
        		}
        		else
        		{
        			//value = DataObjFieldFormatMgr.getInstance().format(getObject(key), relationship.getDataClass());
        			value = DataObjFieldFormatMgr.getInstance().format(getObject(key), processor);
        		}
        		if (useCache && key != null)
        		{
        			lookupCache.addKey((Integer )key, value);
        		}
        	}
        	return value;
        } else
        {
        	return null;
        }
    }
        
    /**
     * @param value
     * @return List of related of objects if value is a key, or value cast to a collection if it is
     *         not a key.
     */
    protected Collection<?> getList(final Object value)
    {
        if (value instanceof Integer)
        {
            return getListFromKey(value);
        }
        return (Collection<?>)value;
    }
    
    /**
     * @param key
     * @return set of related objects for key.
     */
    protected Collection<?> getListFromKey(final Object key)
    {
        if (key != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                
            	return session.getDataList(getListHql(key));
            }
            finally
            {
            	session.close();
            }
        }
        return null;
    }
    
    
    /* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable 
	{
		super.finalize();
		if (session != null)
		{
			session.close();
		}
	}

	/**
     * @return
     */
    public String getListHql(final Object key)
    {
        //return listHql + (key != null ? key.toString() : "");
        return listHql.replace("&id", key != null ? key.toString() : "");
    }

    /**
     * @param value
     * @return related object if value is a key, or value itself if it is not a key.
     */
    protected Object getObject(final Object value)
    {
        if (value instanceof Integer)
        {
            //assume it's a key..
            return getObjectFromKey(value);
        }
        //else
        //assume its a DataModelObjBase
        return value;
    }
    
    /**
     * @param key
     * @return
     */
    protected Object getObjectFromKey(final Object key)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            return session.get(relationship.getDataClass(), (Integer)key);
        }
        finally
        {
            session.close();
        }
    }
}
