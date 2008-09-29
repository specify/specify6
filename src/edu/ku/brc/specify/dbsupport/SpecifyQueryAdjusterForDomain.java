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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
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
    private static final String DIVID          = "DIVID";
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
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#getSpecialColumns(edu.ku.brc.dbsupport.DBTableInfo, boolean, boolean, java.lang.String)
     */
    @Override
    public String getSpecialColumns(final DBTableInfo tableInfo, final boolean isHQL, final boolean isLeftJoin, final String tblAlias)
    {
        if (tableInfo != null)
        {
            String  prefix         = tblAlias == null ? "" : tblAlias + ".";
            String  criterion      = null;
            String  fld            = null;
            boolean adjustFldToSQL = true;
            
            if (tableInfo.getFieldByName("collectionMemberId") != null)
            {
                fld = isHQL ? "collectionMemberId" : "CollectionMemberId";
                criterion = COLMEMID;
                

            } else if (tableInfo.getTableId() == Accession.getClassTableId())
            {
                if (prefix.equals(""))
                {
                    prefix = "dv.";
                }
                else
                {
                    prefix = "dv" + prefix;
                }
                fld = isHQL ? "divisionId" : "DivisionID";
                criterion = DIVID;
                
            } else if (tableInfo.getRelationshipByName("discipline") != null)
            {
                if (prefix.equals(""))
                {
                    // rods - 9/29/08 - This was messing up the Geography Count (SQL) but is needed for HQL
                    prefix = isHQL ? "dsp." : "";
                }
                else
                {
                    prefix = "dsp" + prefix;
                }
                fld = isHQL ? "disciplineId" : "DisciplineID";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == DeterminationStatus.getClassTableId())
            {
                fld = isHQL ? "disciplineId" : "DisciplineID";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == Geography.getClassTableId())
            {
                fld = isHQL ? "definition" : "GeographyTreeDefID";
                criterion = GEOTREEDEFID;
                
            } else if (tableInfo.getTableId() == GeologicTimePeriod.getClassTableId())
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
                fld = isHQL ? "discipline" : "DisciplineID";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == Locality.getClassTableId())
            {
                fld = isHQL ? "discipline" : "DisciplineID";
                criterion = DSPLNID;
                
            } else if (tableInfo.getTableId() == Agent.getClassTableId())
            {
                if (StringUtils.isEmpty(prefix))
                {
                    prefix = "ag.";
                }
                criterion = DSPLNID;
                if (isHQL)
                {
                    fld = criterion + " in elements(" + prefix + "disciplines)";
                    adjustFldToSQL = false;
                }
                else
                {
                    //this probably won't actually work without additional
                    //changes to the from clause for the query
                    fld = "agent_discipline.DisciplineID"; 
                    prefix = "";
                    //throw new RuntimeException("Fix me I am probably broken!");
                }
            }
            
            if (criterion != null && fld != null)
            {
                String sql;
                if (adjustFldToSQL)
                {
                    sql = prefix + fld + " = " + criterion;
                }
                else
                {
                    sql = fld;
                }
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
    public String getJoinClause(DBTableInfo tableInfo, boolean isHQL, final String aliasArg, boolean useLeftJoin)
    {
        String alias;
        if (aliasArg == null)
        {
            alias = tableInfo.getAbbrev();
        }
        else
        {
            alias = aliasArg;
        }
        String join;
        if (useLeftJoin)
        {
            join = "left join ";
        }
        else if (isHQL)
        {
            join = "join ";
        }
        else
        {
            join = "inner join ";
        }
        
        if (tableInfo.getTableId() == Agent.getClassTableId())
        {
            if (isHQL)
            {
                return join + alias + ".disciplines as dsp" + (aliasArg == null ? "" : alias);
            }
            if (aliasArg != null)
            {
                throw new RuntimeException("SpecifyQueryAdjuster.getJoinClause does not work for SQL with non-null alias.");
            }
            return join + "agent_discipline ON agent.AgentID = agent_discipline.AgentID";
            
        } else if (tableInfo.getTableId() == Accession.getClassTableId())
        {
            if (isHQL)
            {
                return join + alias + ".division as dv" + (aliasArg == null ? "" : alias);
            }
            if (aliasArg != null)
            {
                throw new RuntimeException("SpecifyQueryAdjuster.getJoinClause does not work for SQL with non-null alias.");
            }
            return join;
            
        } else if (tableInfo.getRelationshipByName("discipline") != null)
        {
            if (isHQL)
            {
                return join + alias +".discipline as dsp" + (aliasArg == null ? "" : alias);
            }
            //if (aliasArg != null)
            //{
                //throw new RuntimeException("SpecifyQueryAdjuster.getJoinClause does not work for SQL with non-null alias.");
            //}
            return join + "discipline as dsp ON "+aliasArg+".DisciplineID = dsp.DisciplineID";
        }
        return super.getJoinClause(tableInfo, isHQL, alias, useLeftJoin);


//        if (tableInfo.getTableId() == Agent.getClassTableId())
//        {
//            if (isHQL)
//            {
//                return "JOIN ag.disciplines as dsp";
//            }
//            return "INNER JOIN agent_discipline ON agent.AgentID = agent_discipline.AgentID";
//            
//        } else if (tableInfo.getRelationshipByName("discipline") != null)
//        {
//            if (isHQL)
//            {
//                return "JOIN "+tableInfo.getAbbrev()+".discipline as dsp";
//            }
//            return "INNER JOIN discipline as dsp ON "+tableInfo.getName()+".DisciplineID = discipline.DisciplineID";
//        }
//        return super.getJoinClause(tableInfo, isHQL);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#adjustSQL(java.lang.String, boolean)
     */
    @Override
    public String adjustSQL(final String sql)
    {
        // SpecifyUser should NEVER be null nor the Id !
        SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
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
                
                if (StringUtils.contains(adjSQL, DIVID))
                {
                    Integer divId = Agent.getUserAgent().getDivision() != null ? Agent.getUserAgent().getDivision().getDivisionId() : null;
                    if (divId != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, DIVID, Integer.toString(divId));
                    }
                }
                
                //System.out.println(adjSQL);
                if (StringUtils.contains(adjSQL, COLMEMID))
                {
                    Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                    if (collection != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, COLMEMID, Integer.toString(collection.getCollectionId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, COLLID))
                {
                    Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                    if (collection != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, COLLID, Integer.toString(collection.getCollectionId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, DSPLNID))
                {
                    Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
                    if (discipline != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, DSPLNID, Integer.toString(discipline.getDisciplineId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, TAXTREEDEFID))
                {
                    TaxonTreeDef taxonTreeDef = AppContextMgr.getInstance().getClassObject(TaxonTreeDef.class);
                    if (taxonTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, TAXTREEDEFID, Integer.toString(taxonTreeDef.getTaxonTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, GTPTREEDEFID))
                {
                    GeologicTimePeriodTreeDef gtpTreeDef = AppContextMgr.getInstance().getClassObject(GeologicTimePeriodTreeDef.class);
                    if (gtpTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, GTPTREEDEFID, Integer.toString(gtpTreeDef.getGeologicTimePeriodTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, STORTREEDEFID))
                {
                    StorageTreeDef locTreeDef = AppContextMgr.getInstance().getClassObject(StorageTreeDef.class);
                    if (locTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, STORTREEDEFID, Integer.toString(locTreeDef.getStorageTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, LITHOTREEDEFID))
                {
                    LithoStratTreeDef lithoTreeDef = AppContextMgr.getInstance().getClassObject(LithoStratTreeDef.class);
                    if (lithoTreeDef != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, LITHOTREEDEFID, Integer.toString(lithoTreeDef.getLithoStratTreeDefId()));
                    }
                }
                
                if (StringUtils.contains(adjSQL, GEOTREEDEFID))
                {
                    GeographyTreeDef lithoTreeDef = AppContextMgr.getInstance().getClassObject(GeographyTreeDef.class);
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
