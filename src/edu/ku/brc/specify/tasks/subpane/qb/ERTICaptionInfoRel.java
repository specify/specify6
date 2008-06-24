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

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

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
public class ERTICaptionInfoRel extends ERTICaptionInfo
{
    protected final DBRelationshipInfo relationship;
    
    public ERTICaptionInfoRel(String  colName, 
                           String  colLabel, 
                           boolean isVisible, 
                           UIFieldFormatterIFace uiFieldFormatter,
                           int     posIndex,
                           DBRelationshipInfo relationship)
    {
        super(colName, colLabel, isVisible, uiFieldFormatter, posIndex);
        this.relationship = relationship;
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
        return DataObjFieldFormatMgr.getInstance().format(getObject(key), relationship.getDataClass());
    }
    
    /**
     * @param value
     * @return List of related of objects if value is a key, or
     * value cast to a collection if it is not a key.
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
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            return session.getDataList("from " + relationship.getDataClass().getName() + " where "
                    + (relationship.getColName() != null ? relationship.getColName() 
                            : relationship.getOtherSide() + "Id")
                    + " = " + key.toString());
        }
        finally
        {
            session.close();
        }
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
