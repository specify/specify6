/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.util.Pair;

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
    protected static final Logger                            log              = Logger.getLogger(ERTICaptionInfoRel.class);

    
    protected final DBRelationshipInfo relationship;
    /**
     * hql to retrieve list of related objects for one-to-many relationships.
     */
    protected final String listHql;
    
    /*
     * lookupTbl and lookupSize implement a really quick and dirty cache for recently formatted many-to-one related records.
     * If there is much repetition of related data, the cache can really speed up results display. And even when there are
     * far more adds to the cache than finds, performance does not seem to worsen.
     * 
     * NOTE: This cache has no effect on the performance of one-to-many relationships which even slower than many-to-ones.
     * 
     * Still need to check this on some large, real datasets to accurately judge how effective it is.
     */
    protected LinkedList<Pair<Integer, Object>> lookupTbl = null;
    protected static final int lookupSize = 20;
    protected boolean useCache = true;
    
    //for debugging...
    private int finds = 0; 
    private int adds = 0;
    private static boolean debugging = false;
    
    public ERTICaptionInfoRel(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex,
                           String colStringId,
                           DBRelationshipInfo relationship)
    {
        super(colName, colLabel, isVisible, uiFieldFormatter, posIndex, colStringId, null);
        this.relationship = relationship;
        if (relationship.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
        {
            DBTableInfo otherSideTbl = DBTableIdMgr.getInstance().getByClassName(relationship.getClassName());
            DBRelationshipInfo otherSideRel = otherSideTbl.getRelationshipByName(relationship.getOtherSide());
            String otherSideCol = otherSideRel.getColName();
            otherSideCol = otherSideCol.substring(0, 1).toLowerCase().concat(otherSideCol.substring(1));
            otherSideCol = otherSideCol.substring(0, otherSideCol.length()-2) + "Id";
            listHql = "from " + relationship.getDataClass().getName() + " where " + otherSideCol + " = ";
        }
        else
        {
            listHql = null;
            if (useCache)
            {
                lookupTbl = new LinkedList<Pair<Integer, Object>>();
            }
        }
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
        if (relationship.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
        {
            return DataObjFieldFormatMgr.getInstance().aggregate(getList(key), relationship.getDataClass());
        }
        Object value = useCache ? lookupKey((Integer )key) : null;
        if (value == null)
        {
            value = DataObjFieldFormatMgr.getInstance().format(getObject(key), relationship.getDataClass());
            if (useCache && key != null)
            {
                addKey((Integer )key, value);
            }
        }
        return value;
    }
    
    private void logDaBug(Object bug)
    {
        if (debugging)
        {
            log.debug(bug);
        }
    }
    /**
     * @param key
     * @return
     */
    protected Object lookupKey(final Integer key)
    {
        if (key != null)
        {
            for (Pair<Integer, Object> entry : lookupTbl)
            {
                if (entry.getFirst().equals(key))
                {
                    logDaBug(this.getColLabel() + " -- " + ++finds + ": Found: " + key);
                    return entry.getSecond();
                }
            }
        }
        return null;
    }

    /**
     * @param key
     * @param value
     */
    protected void addKey(final Integer key, final Object value)
    {
        if (lookupTbl.size() == lookupSize)
        {
            lookupTbl.remove();
        }
        logDaBug(this.getColLabel() + " -- " + ++adds + ": added: " + key);
        lookupTbl.add(new Pair<Integer, Object>(key, value));
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
                return session.getDataList(listHql + key);
            }
            finally
            {
                session.close();
            }
        }
        return null;
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
