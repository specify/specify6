/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 14, 2007
 *
 */
public class SpecifyQueryAdjusterForDomain extends QueryAdjusterForDomain
{
    
    private static final String SPECIFYUSERID  = "SPECIFYUSERID";
    private static final String DIVISIONID     = "DIVISIONID";
    private static final String DSPLNID       = "DSPLNID";
    private static final String COLMEMID       = "COLMEMID";
    private static final String COLLID         = "COLLID";
    //private static final String COLMEMIDGRP    = "COLMEMIDGRP";
    
    private static final String TAXTREEDEFID   = "TAXTREEDEFID";
    private static final String STORTREEDEFID  = "STORTREEDEFID";
    private static final String LITHOTREEDEFID = "LITHOTREEDEFID";
    private static final String GTPTREEDEFID   = "GTPTREEDEFID";
    private static final String GEOTREEDEFID   = "GEOTREEDEFID";

    public SpecifyQueryAdjusterForDomain()
    {
        // no op
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#getSpecialColumns(edu.ku.brc.dbsupport.DBTableInfo, boolean)
     */
    @Override
    public String getSpecialColumns(final DBTableInfo tableInfo, final boolean isHQL, final String tblAlias)
    {
        if (tableInfo != null)
        {
            String sql = tblAlias == null ? "" : tblAlias + ".";
            if (tableInfo.getFieldByName("collectionMemberId") != null)
            {
                sql += (isHQL ? "collectionMemberId" : "CollectionMemberID") + " = " + COLMEMID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == Agent.getClassTableId() ||
                       tableInfo.getTableId() == DeterminationStatus.getClassTableId())
            {
                sql += (isHQL ? "discipline" : "DisciplineID") + " = " + DSPLNID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == Geography.getClassTableId())
            {
                sql += (isHQL ? "definition" : "GeographyTreeDefID") + " = " + GEOTREEDEFID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == GeologicTimePeriodTreeDef.getClassTableId())
            {
                sql += (isHQL ? "definition" : "GeologicTimePeriodTreeDefID") + " = " + GTPTREEDEFID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == LithoStrat.getClassTableId())
            {
                sql += (isHQL ? "definition" : "LithoStratTreeDefID") + " = " + LITHOTREEDEFID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == Storage.getClassTableId())
            {
                sql += (isHQL ? "definition" : "StorageTreeDefID") + " = " + STORTREEDEFID;
                return adjustSQL(sql);
                
            } else if (tableInfo.getTableId() == PrepType.getClassTableId())
            {
                sql += (isHQL ? "collection" : "CollectionID") + " = " + COLLID;
                return adjustSQL(sql);
          
            } else if (tableInfo.getTableId() == Taxon.getClassTableId())
            {
                sql += (isHQL ? "definition" : "TaxonTreeDefID") + " = " + TAXTREEDEFID;
                return adjustSQL(sql);
                
            }
        }
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#adjustSQL(java.lang.String, boolean)
     */
    @Override
    public String adjustSQL(final String sql)
    {
        // SpecifyUser should NEVER be null nor the Id !
        SpecifyUser user = SpecifyUser.getCurrentUser();
        if (user != null)
        {
            Integer id = user.getId();
            if (id != null)
            {
                String adjSQL = sql;
                if (StringUtils.contains(adjSQL, SPECIFYUSERID))
                {
                    adjSQL = StringUtils.replace(adjSQL, SPECIFYUSERID, Integer.toString(id));
                }
                
                if (StringUtils.contains(adjSQL, DIVISIONID))
                {
                    Integer divId = Agent.getUserAgent().getDivision() != null ? Agent.getUserAgent().getDivision().getDivisionId() : null;
                    if (divId != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, DIVISIONID, Integer.toString(divId));
                    }
                }
                
                //System.out.println(adjSQL);
                if (StringUtils.contains(adjSQL, COLMEMID))
                {
                    Collection collection = Collection.getCurrentCollection();
                    if (collection != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, COLMEMID, Integer.toString(collection.getCollectionId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, COLLID))
                {
                    Collection collection = Collection.getCurrentCollection();
                    if (collection != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, COLLID, Integer.toString(collection.getCollectionId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, DSPLNID))
                {
                    Discipline discipline = Discipline.getCurrentDiscipline();
                    if (discipline != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, DSPLNID, Integer.toString(discipline.getDisciplineId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, TAXTREEDEFID))
                {
                    TaxonTreeDef taxonTreeDef = TaxonTreeDef.getCurrentTaxonTreeDef();
                    if (taxonTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, TAXTREEDEFID, Integer.toString(taxonTreeDef.getTaxonTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, GTPTREEDEFID))
                {
                    GeologicTimePeriodTreeDef gtpTreeDef = GeologicTimePeriodTreeDef.getCurrentGeologicTimePeriodTreeDef();
                    if (gtpTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, GTPTREEDEFID, Integer.toString(gtpTreeDef.getGeologicTimePeriodTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, STORTREEDEFID))
                {
                    StorageTreeDef locTreeDef = StorageTreeDef.getCurrentStorageTreeDef();
                    if (locTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, STORTREEDEFID, Integer.toString(locTreeDef.getStorageTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, LITHOTREEDEFID))
                {
                    LithoStratTreeDef lithoTreeDef = LithoStratTreeDef.getCurrentLithoStratTreeDef();
                    if (lithoTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, LITHOTREEDEFID, Integer.toString(lithoTreeDef.getLithoStratTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, GEOTREEDEFID))
                {
                    GeographyTreeDef lithoTreeDef = GeographyTreeDef.getCurrentGeographyTreeDef();
                    if (lithoTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, GEOTREEDEFID, Integer.toString(lithoTreeDef.getGeographyTreeDefId()));
                    }
                }
                
                return adjSQL;
                
            }
            throw new RuntimeException("The SpecifyUser cannot be null!");
        }
        return super.adjustSQL(sql);
    }
}
