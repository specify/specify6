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
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
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
    protected static final Logger log = Logger.getLogger(SpecifyQueryAdjusterForDomain.class);
    
    private static final String SPECIFYUSERID  = "SPECIFYUSERID";
    private static final String DIVISIONID     = "DIVISIONID";
    private static final String DSPLNID        = "DSPLNID";
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
    public String getSpecialColumns(final DBTableInfo tableInfo, final boolean isHQL, final boolean isLeftJoin, final String tblAlias)
    {
        if (tableInfo != null)
        {
            String prefix = tblAlias == null ? "" : tblAlias + ".";
            String criterion = null;
            String fld = null;
            if (tableInfo.getFieldByName("collectionMemberId") != null)
            {
                fld = "collectionMemberId";
                criterion = COLMEMID;
                
            } else if (tableInfo.getTableId() == DeterminationStatus.getClassTableId())
            {
                fld = isHQL ? "discipline" : "DisciplineID";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == Geography.getClassTableId())
            {
                fld = isHQL ? "definition" : "GeographyTreeDefID";
                criterion = GEOTREEDEFID;
                
            } else if (tableInfo.getTableId() == GeologicTimePeriodTreeDef.getClassTableId())
            {
                fld = isHQL ? "definition" : "GeologicTimePeriodTreeDefID"; 
                criterion = GTPTREEDEFID;
                
            } else if (tableInfo.getTableId() == LithoStrat.getClassTableId())
            {
                fld = isHQL ? "definition" : "LithoStratTreeDefID";
                criterion = LITHOTREEDEFID;
                
            } else if (tableInfo.getTableId() == Storage.getClassTableId())
            {
                fld = isHQL ? "definition" : "StorageTreeDefID";
                criterion = STORTREEDEFID;
                
            } else if (tableInfo.getTableId() == PrepType.getClassTableId())
            {
                fld = isHQL ? "collection" : "CollectionID";
                criterion = COLLID;  
                
            } else if (tableInfo.getTableId() == Taxon.getClassTableId())
            {
                fld = isHQL ? "definition" : "TaxonTreeDefID"; 
                criterion = TAXTREEDEFID;
                
            } else if (tableInfo.getTableId() == Locality.getClassTableId())
            {
                fld = "disciplineId";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == Agent.getClassTableId())
            {
                fld = isHQL ? "dsp.disciplineId" : "agent_discpline.DisciplineID"; 
                criterion = DSPLNID;
            }
            
            if (criterion != null && fld != null)
            {
                String sql = prefix + fld + " = " + criterion;
                if (isLeftJoin)
                {
                    if (isHQL)
                    {
                        if (tblAlias != null)
                        {
                            sql = "(" + sql + " or " + tblAlias + " is null)";
                        }
                        else
                        {
                            log.error("can't adjust hql for left join without a tblAlias.");
                        }
                    }
                    else
                    {
                        sql = "(" + sql + " or " + prefix + fld + " is null)"; 
                    }
                }
                return adjustSQL(sql);
            }
        }
        return null;
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#getJoinClause(edu.ku.brc.dbsupport.DBTableInfo, boolean)
     */
    @Override
    public String getJoinClause(DBTableInfo tableInfo, boolean isHQL)
    {
        if (tableInfo.getTableId() == Agent.getClassTableId())
        {
            if (isHQL)
            {
                return "JOIN ag.disciplines as dsp";
            } else
            {
                return "INNER JOIN agent_discpline ON agent.AgentID = agent_discpline.AgentID";
            }
        }
        return super.getJoinClause(tableInfo, isHQL);
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
