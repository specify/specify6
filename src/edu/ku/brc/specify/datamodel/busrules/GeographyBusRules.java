/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Geography} or {@link GeographyTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class GeographyBusRules extends BaseTreeBusRules<Geography, GeographyTreeDef, GeographyTreeDefItem>
{
    private static final Logger log = Logger.getLogger("edu.ku.brc.specify.datamodel.busrules");
    
    /**
     * Constructor.
     */
    public GeographyBusRules()
    {
        super(Geography.class,GeographyTreeDefItem.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("GEOGRAPHY_DELETED", ((Geography)dataObj).getName());
    }

    @Override
    public boolean hasNoConnections(Geography geo)
    {
        Integer id = geo.getTreeId();
        if (id == null)
        {
            return true;
        }
        
        boolean noLocs = super.okToDelete("locality",  "GeographyID", id);
        boolean noSyns = super.okToDelete("geography", "AcceptedID",  id);

        boolean noConns = noLocs && noSyns;
        
        return noConns;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Geography)
        {
            return super.okToDeleteNode((Geography)dataObj);
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        log.debug("enter");
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Geography)
        {
            Geography geo = (Geography)dataObj;
            beforeSaveGeography(geo);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(geo, session);

            log.debug("exit");
            return;
        }
        
        if (dataObj instanceof GeographyTreeDefItem)
        {
            beforeSaveGeographyTreeDefItem((GeographyTreeDefItem)dataObj);
            log.debug("exit");
            return;
        }
        log.debug("exit");
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Geography}.  The real work of this method is to
     * update the 'fullname' field of all {@link Geography} objects effected by the changes
     * to the passed in {@link Geography}.
     * 
     * @param geo the {@link Geography} being saved
     */
    protected void beforeSaveGeography(Geography geo)
    {
        // nothing specific to Geography
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link GeographyTreeDefItem}.  The real work of this method is to
     * update the 'fullname' field of all {@link Geography} objects effected by the changes
     * to the passed in {@link GeographyTreeDefItem}.
     *
     * @param defItem the {@link GeographyTreeDefItem} being saved
     */
    protected void beforeSaveGeographyTreeDefItem(GeographyTreeDefItem defItem)
    {
        // This is a LONG process for some trees.  I wouldn't recommend doing it.  Can
        // we set these options before shipping the DB, then not let them change it ever again?
        // Or perhaps they can't change it if there are records at this level.
        
        log.warn("TODO: need to make a decision here");
        return;

        //super.beforeSaveTreeDefItem(defItem);
    }
}
