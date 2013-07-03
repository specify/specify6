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
package edu.ku.brc.specify.dbsupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.SpecifyUserTypes.UserType;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.ExchangeIn;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
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
    //private static final String CNT_TBL_SQL = "SELECT COUNT(*) FROM ";
    
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
    
    private boolean permsOKForGlobalSearch = false;
    
    private int divisionCnt   = 0;
    private int disciplineCnt = 0;
    private int collectionCnt = 0;

    /**
     * 
     */
    public SpecifyQueryAdjusterForDomain()
    {
        permsOKForGlobalSearch = !AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(UserType.Manager);
        
        // This kicks Global Search into gear if there is just one, but only for that one
        //divisionCnt   = BasicSQLUtils.getCountAsInt(CNT_TBL_SQL+"division");
        //disciplineCnt = BasicSQLUtils.getCountAsInt(CNT_TBL_SQL+"discipline");
        //collectionCnt = BasicSQLUtils.getCountAsInt(CNT_TBL_SQL+"collection");
        
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
                

            } else if (tableInfo.getTableId() == Agent.getClassTableId() ||
                       tableInfo.getTableId() == Accession.getClassTableId() ||
                       tableInfo.getTableId() == RepositoryAgreement.getClassTableId() ||
                       tableInfo.getTableId() == ExchangeIn.getClassTableId() ||
                       tableInfo.getTableId() == ExchangeOut.getClassTableId() ||
                       tableInfo.getTableId() == ConservDescription.getClassTableId())
            {
                if (prefix.equals(""))
                {
                    prefix = isHQL ? "dv." : "";
                }
                else
                {
                    prefix = isHQL ? ("dv" + prefix) : prefix;
                }
                fld = isHQL ? "divisionId" : "DivisionID";
                criterion = DIVID;
                
            } else if (tableInfo.getRelationshipByName("discipline") != null)
            {
                DBRelationshipInfo ri = tableInfo.getRelationshipByName("discipline");
                if (ri.getType() != DBRelationshipInfo.RelationshipType.OneToOne)
                {
                    if (prefix.equals(""))
                    {
                        // rods - 9/29/08 - This was messing up the Geography Count (SQL) but is needed for HQL
                        prefix = isHQL ? "dsp." : "";
                    }
                    else
                    {
                        prefix = isHQL ? ("dsp" + prefix) : prefix;
                    }
                    fld = isHQL ? "disciplineId" : "DisciplineID";
                    criterion = DSPLNID;
                }
                
//            } else if (tableInfo.getTableId() == DeterminationStatus.getClassTableId())
//            {
//                fld = isHQL ? "disciplineId" : "DisciplineID";
//                criterion = DSPLNID;
//                
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
                
            } else if (tableInfo.getTableId() == Locality.getClassTableId())
            {
                fld = isHQL ? "discipline" : "DisciplineID";
                criterion = DSPLNID;
                
            } /*else if (tableInfo.getTableId() == Agent.getClassTableId())
            {
                if (StringUtils.isEmpty(prefix))
                {
                    prefix = "ag.";
                }
                criterion = DIVID;
                if (isHQL)
                {
                    fld = criterion + " = " + prefix + "division.id";
                    adjustFldToSQL = false;
                }
                else
                {
                    //this probably won't actually work without additional
                    //changes to the from clause for the query
                    fld = criterion + " = " + prefix + "D";
                    prefix = "";
                    //throw new RuntimeException("Fix me I am probably broken!");
                }
            }*/
            
            if (criterion != null && fld != null)
            {
                String sql;
                if (adjustFldToSQL)
                {
                    sql = "(" + prefix + fld + " = " + criterion + ")";
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
        
        if (tableInfo.getTableId() == Agent.getClassTableId() ||
            tableInfo.getTableId() == Accession.getClassTableId() ||
            tableInfo.getTableId() == RepositoryAgreement.getClassTableId() ||
            tableInfo.getTableId() == ExchangeIn.getClassTableId() ||
            tableInfo.getTableId() == ExchangeOut.getClassTableId() ||
            tableInfo.getTableId() == ConservDescription.getClassTableId())
        {
            if (isHQL)
            {
                return join + alias + ".division as dv" + (aliasArg == null ? "" : alias);
            }
            if (aliasArg != null)
            {
                return "";
                //throw new RuntimeException("SpecifyQueryAdjuster.getJoinClause does not work for SQL with non-null alias.");
            }
            return join;
            
        } else if (tableInfo.getRelationshipByName("discipline") != null)
        {
            DBRelationshipInfo ri = tableInfo.getRelationshipByName("discipline");
            if (ri.getType() != DBRelationshipInfo.RelationshipType.OneToOne)
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
        }
        return super.getJoinClause(tableInfo, isHQL, alias, useLeftJoin);
    }
    
    private int checkAhead(final String lowerSQL, final int startInx, final String key)
    {
        int bInx = lowerSQL.lastIndexOf(key, startInx);
        int diff = startInx - bInx;
        if (diff == key.length()+1)
        {
            return diff;
        }
        return 0;
    }
    
    /**
     * @param lowerSQL
     * @param endInx
     * @param key
     * @return
     */
    private int checkAfter(final String lowerSQL, final int endInx, final String key)
    {
        int bInx = lowerSQL.indexOf(key, endInx);
        if (bInx > -1)
        {
            int diff = bInx - endInx;
            if (diff == 2)
            {
                return key.length()+1;
            }
        }
        return 0;
    }
    
    /**
     * @param source
     * @return
     */
    public static String itrim(String source) 
    {
        //return source.replaceAll("\\b\\s{2,}\\b", " ");
        return source.replaceAll("\\s+", " ");   
    }

    /**
     * @param specialSQL
     * @param key
     * @return
     */
    private String removeSpecialFilter(final String specialSQL, final String key)
    {
        String lowerSQL = specialSQL.toLowerCase();
        
        String frontStr = "";
        String whereStr = "";
        
        int whereInx = lowerSQL.indexOf("where");
        if (whereInx == -1)
        {
            whereInx = 0;
            whereStr = specialSQL;
        } else
        {
            whereInx += 6;
            frontStr = specialSQL.substring(0, whereInx);
            whereStr = itrim(specialSQL.substring(whereInx, specialSQL.length()));//.replaceAll("\n", ""));
            lowerSQL = itrim(lowerSQL.substring(whereInx, specialSQL.length()));//.replaceAll("\n", ""));
        }
        
        
        //System.out.println("["+lowerSQL+"]\n["+whereStr+"]");
        
        int inx = whereStr.indexOf(key);
        if (inx > -1)
        {
            int endInx = inx + key.length();
            if (endInx < whereStr.length() && lowerSQL.charAt(endInx) == ')')
            {
                int startInx = lowerSQL.lastIndexOf('(', inx);
                if (startInx > -1) // here we have source.replaceAll("\\b\\s{2,}\\b", " ");the bounds of the '(' to ')'
                {
                    // Now check to see what is ahead of it
                    int bInx = checkAhead(lowerSQL, startInx, "and");
                    if (bInx > 0)
                    {
                        startInx -= bInx;
                    } else 
                    {
                        bInx = checkAhead(lowerSQL, startInx, "or");
                        if (bInx > 0)
                        {
                            startInx -= bInx;
                        } else
                        {
                            // check after
                            int eInx = checkAfter(lowerSQL, endInx, "and");
                            if (eInx > 0)
                            {
                                endInx += eInx;
                            } else
                            {
                                endInx += checkAfter(lowerSQL, endInx, "or");
                            }
                        }
                    } 
                    String segment = whereStr.substring(startInx, endInx+1);
                    String newStr = frontStr + StringUtils.remove(whereStr, segment);
                    
                    //System.out.println("["+specialSQL+"]\n["+newStr+"]");
                    return newStr;
                }
            }
            //System.out.println("Skipped1["+specialSQL+"]");
        }/* else
        {
            System.out.println("Skipped2["+specialSQL+"]");
        }*/
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain#adjustSQL(java.lang.String, boolean)
     */
    @Override
    public String adjustSQL(final String sql)
    {
        AppPreferences locPrefs = AppPreferences.getLocalPrefs();
        boolean doGlobalSearch = permsOKForGlobalSearch && locPrefs.getBoolean("GLOBAL_SEARCH_AVAIL", false) && locPrefs.getBoolean("GLOBAL_SEARCH", false);
        //divisionCnt++;
        //disciplineCnt++;
        //collectionCnt++;
        
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
                    String adjustedSQL = null;
                    if (doGlobalSearch || divisionCnt == 1)
                    {
                        adjustedSQL = removeSpecialFilter(adjSQL, DIVID);
                    }
                    
                    if (adjustedSQL == null)
                    {
                        Integer  divId    = null;
                        Division division = AppContextMgr.getInstance().getClassObject(Division.class);
                        if (division != null)
                        {
                            divId = division.getId();
                        } else
                        {
                            divId = Agent.getUserAgent().getDivision() != null ? Agent.getUserAgent().getDivision().getDivisionId() : null;
                        }
                        
                        if (divId != null)
                        {
                            adjSQL = StringUtils.replace(adjSQL, DIVID, Integer.toString(divId));
                        }
                    } else
                    {
                        adjSQL = adjustedSQL;
                    }
                }
                
                //System.out.println(adjSQL);
                if (StringUtils.contains(adjSQL, COLMEMID))
                {
                    String adjustedSQL = null;
                    if (doGlobalSearch || collectionCnt == 1)
                    {
                        adjustedSQL = removeSpecialFilter(adjSQL, COLMEMID);
                    }
                    
                    if (adjustedSQL == null)
                    {
                        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                        if (collection != null)
                        {
                            adjSQL = StringUtils.replace(adjSQL, COLMEMID, Integer.toString(collection.getCollectionId()));
                        }
                    } else
                    {
                        adjSQL = adjustedSQL;
                    }
                }
                
                if (StringUtils.contains(adjSQL, COLLID))
                {
                    String adjustedSQL = null;
                    if (doGlobalSearch || collectionCnt == 1)
                    {
                        adjustedSQL = removeSpecialFilter(adjSQL, COLLID);
                    }
                    
                    if (adjustedSQL == null)
                    {
                        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                        if (collection != null)
                        {
                            adjSQL = StringUtils.replace(adjSQL, COLLID, Integer.toString(collection.getCollectionId()));
                        }
                    } else
                    {
                        adjSQL = adjustedSQL;
                    }
                }
                
                if (StringUtils.contains(adjSQL, DSPLNID))
                {
                    String adjustedSQL = null;
                    if (doGlobalSearch || disciplineCnt == 1)
                    {
                        adjustedSQL = removeSpecialFilter(adjSQL, DSPLNID);
                    }
                    
                    if (adjustedSQL == null)
                    {
                        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
                        if (discipline != null)
                        {
                            adjSQL = StringUtils.replace(adjSQL, DSPLNID, Integer.toString(discipline.getDisciplineId()));
                        }
                    } else
                    {
                        adjSQL = adjustedSQL;
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
                    } else
                    {
                        return null;
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
                    } else
                    {
                        return null;
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
            throw new RuntimeException("The SpecifyUser ID cannot be null!");
            
        } else
        {
            throw new RuntimeException("The SpecifyUser cannot be null!");
        }
    }

    /**
     * @return the permsOKForGlobalSearch
     */
    public boolean isPermsOKForGlobalSearch()
    {
        return permsOKForGlobalSearch;
    }
    
    /*
    public static void main(String[] args)
    {
        String str = "1  2   3    4444     555      666666   4444";
        System.err.println(str+"-"+itrim(str));
        
        str = "a  bbb  cc     ff  ggggggg     rr   d";
        System.err.println(str+"-"+itrim(str));
        
        str = ")   OR  (isClosed";
        System.err.println("["+str+"]["+itrim(str)+"]");
        
        SpecifyQueryAdjusterForDomain qa = new SpecifyQueryAdjusterForDomain(true);
        //qa.removeSpecialFilter("SELECT count(loanId) as OpenLoanCount FROM Loan l INNER JOIN l.discipline dsp WHERE (isClosed = 0 OR isClosed is null) AND (dsp.disciplineId = DSPLNID)", DSPLNID);
        //qa.removeSpecialFilter("SELECT count(loanId) as OpenLoanCount FROM Loan l INNER JOIN l.discipline dsp WHERE (isClosed = 0 OR isClosed is null) OR (dsp.disciplineId = DSPLNID)", DSPLNID);
        //qa.removeSpecialFilter("SELECT count(loanId) as OpenLoanCount FROM Loan l INNER JOIN l.discipline dsp WHERE (dsp.disciplineId = DSPLNID)  AND   (isClosed = 0 OR isClosed is null)", DSPLNID);
        qa.removeSpecialFilter("SELECT count(loanId) as OpenLoanCount FROM Loan l INNER JOIN l.discipline dsp WHERE (dsp.disciplineId = DSPLNID)   OR  (isClosed = 0 OR isClosed is null)", DSPLNID);
        qa.removeSpecialFilter("SELECT count(loanId) as OpenLoanCount FROM Loan l INNER JOIN l.discipline dsp WHERE (dsp.disciplineId = DSPLNID)   OR\n  (isClosed = 0 OR isClosed is null)", DSPLNID);
    }
    */
} 
