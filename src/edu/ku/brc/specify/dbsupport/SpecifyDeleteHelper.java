/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AddressOfRecord;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 3, 2008
 *
 */
public class SpecifyDeleteHelper
{
    protected boolean    debug             = false;
    protected boolean    debugUpdate       = false;
    protected boolean    doTrees           = false;

    protected Integer    totalCount        = null;
    protected int        counter           = 0;
    protected JStatusBar statusBar         = null;
    
    protected Connection connection;
    
    /**
     * 
     */
    public SpecifyDeleteHelper(final boolean doUpdateStatusBar)
    {
        super();
        
        if (doUpdateStatusBar)
        {
            statusBar = UIRegistry.getStatusBar();
        }
        
        DBConnection dbConn = DBConnection.createInstance("com.mysql.jdbc.Driver", 
                                                          "org.hibernate.dialect.MySQLDialect", 
                                                          "testfish", 
                                                          "jdbc:mysql://localhost/testfish", 
                                                          "Specify", 
                                                          "Specify");
        connection = dbConn.createConnection();

        if (false)
        {
            try
            {
                delRecordFromTable(Discipline.class, 3, true);
                
                //delRecordFromTable(Division.class, 2, false);
                //cleanUpAgentsForDivision(2);
                
                checkTables();
                
            } catch (SQLException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
                ex.printStackTrace();
                try
                {
                    connection.rollback();
                } catch (SQLException ex2)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex2);
                    ex.printStackTrace();
                }
                
            } finally
            {
                try
                {
                    if (connection != null)
                    {
                        connection.close();
                    }
                } catch (SQLException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 
     */
    public void done()
    {
        if (totalCount != null)
        {
            statusBar.setValue(FormViewObj.STATUSBAR_NAME, totalCount);
        }
        
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
            ex.printStackTrace();
        }
    }

    
    /**
     * @param cls
     * @param id
     * @return
     * @throws SQLException
     */
    public boolean delRecordFromTable(final Class<?>    cls, 
                                      final int         id,
                                      final boolean     doDeleteId) throws SQLException
    {
        StackItem root      = new StackItem(null, null, null);
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
        
        String sqlStr = "SELECT "+tblInfo.getIdColumnName()+" FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = ";
        String delStr = doDeleteId ? "DELETE FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = " : null;
        
        getSubTables(root, cls, id, sqlStr, delStr, 0, null);
        
        if (debug)
        {
            System.out.println("\n------------------------------------------\n");
            dumpStack(root, 0);
        }
        
        boolean isAutoCommit = connection.getAutoCommit();
        
        connection.setAutoCommit(false);

        Statement stmt = connection.createStatement();
        if (stmt != null)
        {
            for (StackItem si : root.getStack())
            {
                deleteRecords(si, 0, id, stmt, false);
            }
            
            if (debug) System.out.println("Count: "+counter);
            
            if (!debug)
            {
                totalCount = counter;
                counter    = 0;
                
                if (statusBar != null && totalCount != null)
                {
                    statusBar.setProgressRange(FormViewObj.STATUSBAR_NAME, 0, totalCount, 0);
                }
                
                for (StackItem si : root.getStack())
                {
                    deleteRecords(si, 0, id, stmt, true);
                }
            }
            stmt.close();
                
            connection.commit();
            
            connection.setAutoCommit(isAutoCommit);
            
            return true;            
        }
        return false;
    }
    
    /**
     * @param level
     */
    protected void printLevel(final int level)
    {
        for (int i=0;i<level;i++)
        {
            System.out.print("  ");
        }
    }
    
    /**
     * @param parent
     * @param cls
     * @param sqlStr
     * @param delSqlStr
     * @param level
     */
    protected StackItem getSubTables(final StackItem parent,
                                     final Class<?>  cls, 
                                     final int       id,
                                     final String    sqlStr,
                                     final String    delSqlStr,
                                     final int       level,
                                     final Hashtable<String, Boolean> inUseHashArg)
    {
        if (debug)
        {
            printLevel(level);
            System.out.println(cls.getSimpleName());
        }
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
        
        StackItem child = parent.push(tblInfo, sqlStr, delSqlStr);
        
        Hashtable<String, Boolean> inUseHash = inUseHashArg == null && level == 1 ? new Hashtable<String, Boolean>() : inUseHashArg;
        
        if (tblInfo.getTableId() == 4)
        {
            int x = 0;
            x++;
        }
        
        for (Method method : cls.getMethods())
        {
            String methodName = method.getName();
            //System.out.println(methodName);
            
            // Skip if it is a not a getter
            if (!methodName.startsWith("get"))
            {
                continue;
            }
            
            // Skip if it is a not a ManyToOne
            if (method.isAnnotationPresent(javax.persistence.Transient.class))
            {
                continue;
            }

            if (methodName.equals("getPickListItems"))
            {
                int x = 0;
                x++;
            }

            if (StringUtils.contains(methodName, "TaxonCitation"))
            {
                int x = 0;
                x++;
            }
            
            if (methodName.endsWith("TreeDef"))
            {
                if (doTrees)
                {
                    String className = methodName.substring(3, methodName.length()-7);
                    
                    String tableNameTD      = className.toLowerCase() + "treedef";
                    String primaryKeyTD     = className+"TreeDefID";
                    String itemTableNameTDI = className.toLowerCase() + "treedefitem";
    
                    String sql;
                    String delSql;
                    
                    try
                    {
                        sql = "SELECT "+primaryKeyTD+" FROM "+tblInfo.getName() + " WHERE "+tblInfo.getIdColumnName()+" = "+id;
                        if (debugUpdate) System.err.println(sql);
                        
                        Vector<Integer> ids = getIds(sql, level);
                        if (ids != null && ids.size() > 0)
                        {
                            Class<?> treeClass = null;
                            try
                            {
                                treeClass = Class.forName("edu.ku.brc.specify.datamodel."+className);
                            } catch (Exception ex)
                            {
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
                                ex.printStackTrace();
                            }
                            if (treeClass == cls)
                            {
                                return null;
                            }
                            
                            if (treeClass == Taxon.class)
                            {
                                String tmpSql = "SELECT tc.TaxonCitationID FROM taxoncitation tc INNER JOIN taxon tx ON tc.TaxonID = tx.TaxonID INNER JOIN taxontreedef ttd ON tx.TaxonTreeDefID = ttd.TaxonTreeDefID = "+ids.get(0);
                                delSql = "DELETE FROM taxoncitation WHERE TaxonCitationID = ";
                                child.pushPPS(new StackItem(null, tmpSql, delSql, false, true));
                            }
                            
                            delSql = "DELETE FROM "+className.toLowerCase() + " WHERE "+primaryKeyTD+" = "+ids.get(0)+" ORDER BY AcceptedID DESC, ParentID DESC";
                            child.pushPPS(new StackItem(null, sql, delSql, false, false));
                            
                            delSql = "DELETE FROM "+itemTableNameTDI + " WHERE "+primaryKeyTD+" = "+ids.get(0)+" ORDER BY RankID DESC";
                            child.pushPPS(new StackItem(null, sql, delSql, false, false));
                            
                            delSql = "DELETE FROM "+tableNameTD + " WHERE "+primaryKeyTD+" = "+ids.get(0);
                            child.pushPPS(new StackItem(null, sql, delSql, false, false));
                        }
                        
                    } catch (SQLException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
                        ex.printStackTrace();
                    }
                }
                continue;
            }

            String  colName   = null;
            boolean isOKToDel = false;
            if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
            {
                boolean doDel = false;
                javax.persistence.OneToMany oneToMany = (javax.persistence.OneToMany)method.getAnnotation(javax.persistence.OneToMany.class);
                for (CascadeType ct : oneToMany.cascade())
                {
                    if (ct == CascadeType.ALL || ct == CascadeType.REMOVE)
                    {
                        //doDel = true;
                    }
                }
                isOKToDel = !doDel ? isOKToDel(method) : true;
                colName = tblInfo.getIdColumnName();
                
            } else if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
            {
                boolean doDel = false;
                javax.persistence.ManyToOne oneToMany = (javax.persistence.ManyToOne)method.getAnnotation(javax.persistence.ManyToOne.class);
                for (CascadeType ct : oneToMany.cascade())
                {
                    if (ct == CascadeType.ALL || ct == CascadeType.REMOVE)
                    {
                        doDel = true;
                    }
                }
                isOKToDel = !doDel ? isOKToDel(method) : true;
                if (isOKToDel)
                {
                    javax.persistence.JoinColumn joinCol = (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class);
                    if (joinCol != null)
                    {
                        colName = joinCol.name();
                    }
                }
                
            } else if (method.isAnnotationPresent(javax.persistence.ManyToMany.class))
            {
                javax.persistence.JoinTable joinTable = (javax.persistence.JoinTable)method.getAnnotation(javax.persistence.JoinTable.class);
                if (joinTable != null)
                {
                    String joinTableName = joinTable.name();
                    String joinColName   = null;
                    for (JoinColumn jc : joinTable.joinColumns())
                    {
                        joinColName = jc.name();
                        break;
                    }
                    
                    DBRelationshipInfo relInfo = null;
                    for (DBRelationshipInfo ri : tblInfo.getRelationships())
                    {
                        if (ri.getJoinTable() != null && ri.getJoinTable().equals(joinTableName))
                        {
                            relInfo = ri;
                            break;
                        }
                    }
                    //System.out.println(joinColName);
                    
                    
                    if (cls != Agent.class)
                    {
                        if (inUseHash != null) inUseHash.put(relInfo.getClassName(), true);
                        
                        String sql    = "SELECT "+joinColName+ " FROM " + joinTableName + " WHERE " + joinColName + " = ";
                        String delSql = "DELETE FROM "+joinTableName+ " WHERE " + joinColName + " = ";
                        
                        if (debug)
                        {
                            printLevel(level);
                            System.out.println(sql);
                        }
                        
                        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(relInfo.getDataClass().getSimpleName());
                        child.push(ti, sql, delSql);
                        
                    } else
                    {
                        //System.err.println(cls.getName());
                    }

                }
            }
            
            if (isOKToDel)
            {
                //System.out.println(method.getName()+"  "+method.getReturnType().getSimpleName());
                String             relName = method.getName().substring(3);
                DBRelationshipInfo relInfo = tblInfo.getRelationshipByName(relName);
                if (relInfo != null)
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(relInfo.getClassName());
                    if (ti != null)
                    {
                        String sql;
                        if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
                        {
                            String otherColName = colName;
                            DBRelationshipInfo ri = ti.getRelationshipByName(relInfo.getOtherSide());
                            if (ri != null)
                            {
                                otherColName = ri.getColName();
                            }
                            sql = "SELECT "+ti.getAbbrev()+"."+ti.getIdColumnName()+ " FROM "+ti.getName() + " "+ti.getAbbrev()+" INNER JOIN "+tblInfo.getName()+" "+
                                   tblInfo.getAbbrev()+" ON "+ti.getAbbrev()+"."+otherColName+" = "+tblInfo.getAbbrev()+"."+tblInfo.getIdColumnName()+
                                   "  WHERE "+tblInfo.getAbbrev()+"."+colName+" = ";
                        } else
                        {
                            sql = "SELECT "+ti.getAbbrev()+"."+ti.getIdColumnName()+ " FROM "+ti.getName() + " "+ti.getAbbrev()+" INNER JOIN "+tblInfo.getName()+" "+
                                  tblInfo.getAbbrev()+" ON "+ti.getAbbrev()+"."+ti.getIdColumnName()+" = "+tblInfo.getAbbrev()+"."+colName+
                                  "  WHERE "+tblInfo.getAbbrev()+"."+tblInfo.getIdColumnName()+" = ";
                        }
                        String delSql = "DELETE FROM "+ti.getName() + " WHERE "+ti.getIdColumnName()+" = ";
                        
                        if (debug)
                        {
                            printLevel(level);
                            System.out.println(sql);
                            printLevel(level);
                            System.out.println(delSql);
                        }
                        
                        if (relInfo.getDataClass() != Agent.class)
                        {
                            if (inUseHash != null) inUseHash.put(relInfo.getClassName(), true);
                            
                            if (ti.getClassObj() != cls || (doTrees && !Treeable.class.isAssignableFrom(cls)))
                            {
                                getSubTables(child, ti.getClassObj(), id, sql, delSql, level+1, inUseHash);
                                
                            } else if (debug)
                            {
                                System.err.println("Skipping "+ti.getClassObj().getSimpleName());
                            }
                        } else
                        {
                            //System.err.println(relInfo.getDataClass().getName());
                        }
                        
                    } else
                    {
                        String shortClassName = relInfo.getDataClass().getSimpleName();
                        String sql    = "SELECT "+shortClassName+"ID FROM "+shortClassName.toLowerCase() + " WHERE "+tblInfo.getClassObj().getSimpleName()+"ID = ";
                        String delSql = "DELETE FROM "+shortClassName.toLowerCase() + " WHERE "+shortClassName+"ID = ";
                        if (debug)
                        {
                            printLevel(level);
                            System.out.println(sql);
                            printLevel(level);
                            System.out.println(delSql);
                        }
                        child.push(tblInfo, sql, delSql); // NOTE: the tblInfo is for the parent!
                    }
                }
            }
        }
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            if (ti.getTableId() == 75 && tblInfo.getTableId() == 4)
            {
                int x = 0;
                x++;
            }
            if (ti != tblInfo)
            {
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    /*System.out.println(ri.getName());
                    if (ri.getDataClass() == Taxon.class)
                    {
                        int x = 0;
                        x++;
                    }*/

                    if (ri.getDataClass() != Agent.class)
                    {
                        boolean hashOK = inUseHash == null || inUseHash.get(ti.getClassName()) == null;
                        if (ri.getDataClass() == tblInfo.getClassObj() && hashOK && StringUtils.isEmpty(ri.getOtherSide()))
                        {
                            String sql    = "SELECT "+ti.getIdColumnName()+ " FROM "+ti.getName() + "  WHERE " + ri.getColName() + " = ";
                            String delSql = "DELETE FROM "+ti.getName() + "  WHERE " + ti.getIdColumnName() + " = ";
                            
                            if (debug)
                            {
                                printLevel(level);

                                System.out.println("Missed "+ti.getClassName()+" for "+tblInfo.getClassObj());
                                printLevel(level);
                                System.out.println(sql);
                            }
                            
                            if (ti.getTableId() == Borrow.getClassTableId() ||
                                ti.getTableId() == AddressOfRecord.getClassTableId())
                            {
                                int x= 0;
                                x++;
                            }
                            if (inUseHash != null) inUseHash.put(ti.getClassName(), true);
                            getSubTables(child, ti.getClassObj(), id, sql, delSql, level+1, inUseHash);
                            
                        } else if (ri.getDataClass() == tblInfo.getClassObj() && !hashOK && StringUtils.isEmpty(ri.getOtherSide()))
                        {
                            System.out.println("Skipping "+ti.getClassObj().getSimpleName()+" for "+tblInfo.getClassObj().getSimpleName());
                            int x = 0;
                            x++;
                        }
                    } else
                    {
                        //System.err.println(ri.getDataClass().getName());
                    }
                }
            }
        }
        
        if (debug) System.out.println();
        
        if (cls == Discipline.class)
        {
            String sql    = "SELECT DisciplineID FROM agent_discipline WHERE DisciplineID = ";
            String delSql = "DELETE FROM agent_discipline WHERE DisciplineID = ";
            child.push(tblInfo, sql, delSql); // NOTE: tblInfo is of parent!
        }

        if (cls == Division.class)
        {
            String sql    = "SELECT DisciplineID FROM agent_discipline WHERE DisciplineID = ";
            String delSql = "DELETE FROM agent_discipline WHERE DisciplineID = ";
            child.push(tblInfo, sql, delSql); // NOTE: tblInfo is of parent!
        }

        return child;
    }
    
    /**
     * @param method
     * @return
     */
    protected boolean isOKToDel(final Method method)
    {
        org.hibernate.annotations.Cascade hibCascade = (org.hibernate.annotations.Cascade)method.getAnnotation(org.hibernate.annotations.Cascade.class);
        if (hibCascade != null)
        {
            boolean isAllOrDel = false;
            for (org.hibernate.annotations.CascadeType ct : hibCascade.value())
            {
                if (ct == org.hibernate.annotations.CascadeType.ALL || 
                    ct == org.hibernate.annotations.CascadeType.DELETE)
                {
                    isAllOrDel = true;
                } else if (isAllOrDel && ct == org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @param si
     * @param level
     */
    protected void dumpStack(final StackItem si, final int level)
    {
        printLevel(level);
        System.out.print(" -- ");
        System.out.println(si.getTableInfo() == null ? "Root" : si.getTableInfo().getName()+" -- ");
        
        for (StackItem s : si.getStack())
        {
            dumpStack(s, level+1);
        }
        
        if (si.getSql() != null)
        {
            printLevel(level);
            System.out.println(si.getSql());
        }
    }
    
    /**
     * @param stmt
     * @param sqlArg
     * @param id
     * @param level
     * @return
     * @throws SQLException
     */
    protected Vector<Integer> getIds(final String    sqlArg,
                                     final int       level) throws SQLException
    {
        int             cnt = 0;
        Vector<Integer> ids = null;
        if (sqlArg != null)
        {
            ids = new Vector<Integer>();
            
            //if (debugUpdate) System.err.println(sqlArg);
            
            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery(sqlArg);
            while (rs.next())
            {
                int rowId = rs.getInt(1);
                ids.add(rowId);
                
                /*if (debugUpdate)
                {
                    printLevel(level);
                    System.out.println("Deleting ID: "+rowId);
                }*/
                cnt++;
            }
            rs.close();
            stmt.close();
            counter += cnt;
        }
        return ids;
    }
    
    /**
     * @param si
     * @param level
     * @param id
     * @param stmt
     * @throws SQLException
     */
    protected void deleteRecords(final StackItem si, 
                                 final int       level,
                                 final int       id, 
                                 final Statement stmt,
                                 final boolean   doDeletes) throws SQLException
    {
        
        if (!doTrees && (si.getTableInfo() == null || Treeable.class.isAssignableFrom(si.getTableInfo().getClassObj())))
        {
            return;
        }
        
        if (debugUpdate)
        {  
            printLevel(level);
            System.out.print(" -- ");
            System.out.println(si.getTableInfo() == null ? "Root" : si.getTableInfo().getName()+" -- ");
        }
        
        int             cnt = 0;
        Vector<Integer> ids = null;
        if (si.getSql() != null && (si.isBuildingSQL() || si.isBuildingDelSQL()))
        {
            ids = new Vector<Integer>();
            String sql = si.isBuildingSQL() ? si.getSql() + Integer.toString(id) : si.getSql();

            if (debugUpdate) System.err.println(sql);
             
            ResultSet rs  = stmt.executeQuery(sql);
            while (rs.next())
            {
                int rowId = rs.getInt(1);
                ids.add(rowId);
                
                if (debugUpdate)
                {
                    printLevel(level);
                    System.out.println("Adding ID: "+rowId+"  "+(si.getTableInfo() != null ? si.getTableInfo().getName() : "N/A"));
                }
                
                Statement statement = connection.createStatement();
                for (StackItem s : si.getStack())
                {
                   deleteRecords(s, level+1, rowId, statement, doDeletes);
                }

                statement.close();
                cnt++;
            }
            rs.close();
            counter += cnt;
            
            if (debugUpdate)
            {
                //printLevel(level);
                System.err.println("Items returned: "+cnt);
            }
        }

        
        if (doDeletes)
        {
            if (si.isBuildingDelSQL())
            {
                if (ids != null)
                {
                    if (cnt > 0)
                    {
                        for (Integer itemId : ids)
                        {
                            if (si.getDelSql() != null)
                            {
                                String delSql = si.getDelSql() + itemId;

                                //System.err.println(delSql);
                                /*if (delSql.indexOf("FROM discipline") > -1)
                                {
                                    System.err.println(delSql);
                                    int x= 0;
                                    x++;
                                }*/
                                
                                if (StringUtils.contains(si.getDelSql(), "XXX"))
                                {
                                    delSql = StringUtils.replace(si.getDelSql(), "XXX", Integer.toString(itemId));
                                }
                                
                                if (si.getTableInfo().getClassObj() == Discipline.class && StringUtils.contains(delSql, "FROM discipline"))
                                {
                                    deleteDiscipline(delSql, itemId);
                                    
                                } else
                                {
                                    if (debugUpdate) System.err.println(delSql);
                                    int count = stmt.executeUpdate(delSql);
                                    if (debugUpdate) System.err.println("Count: "+count);
                                }

                            }
                        }
                    }
                } else if (si.getDelSql() != null)
                {
                    String delSql = si.getDelSql() + id;
                    if (debugUpdate) System.err.println(delSql);
                    
                    int count = stmt.executeUpdate(delSql);
                    if (debugUpdate) System.err.println("Count: "+count);
                }
            } else if (si.getDelSql() != null)
            {
                String delSql = si.getDelSql();
                if (debugUpdate) System.err.println("*****: "+delSql);
                
                int count = stmt.executeUpdate(delSql);
                if (debugUpdate) System.err.println("Count: "+count); 
            }
                
            for (StackItem stckItm : si.getPostProcStack())
            {
                debugUpdate = true;
                deleteRecords(stckItm, level+2, id, stmt, true);
                debugUpdate = false;
            }
        }
        
        if (debugUpdate)
        {
            printLevel(level);
            System.out.println("Records to delete: "+cnt);
        }
            
        if (totalCount != null && debugUpdate)
        {
            System.err.println(counter+" / "+totalCount);
        }
        
        if (statusBar != null)
        {
            statusBar.setValue(FormViewObj.STATUSBAR_NAME, counter);
        }
    }
    
    /**
     * @param delSql
     * @param id
     */
    protected void deleteDiscipline(final String delSql, final int id) throws SQLException
    {
        Integer txTDId   = null;
        Integer geoTTDId = null;
        Integer lsTDId   = null;
        Integer gtpTDId  = null;
        
        Statement stmt = connection.createStatement();
        String    tmpSql = "SELECT TaxonTreeDefID, GeographyTreeDefID, LithoStratTreeDefID, GeologicTimePeriodTreeDefID FROM discipline WHERE DisciplineID = " + id;
        ResultSet tmpRS  = stmt.executeQuery(tmpSql);
        if (tmpRS.next())
        {
            txTDId   = tmpRS.getInt(1);   
            geoTTDId = tmpRS.getInt(2);    
            lsTDId   = tmpRS.getInt(3);    
            gtpTDId  = tmpRS.getInt(4);    
        }
        tmpRS.close();
        
        if (txTDId != null && txTDId != null && txTDId != null && txTDId != null)
        {
            stmt.executeUpdate(delSql);
            
            cleanUpTree(TaxonTreeDef.class,              txTDId);    
            cleanUpTree(GeographyTreeDef.class,          geoTTDId);    
            cleanUpTree(LithoStratTreeDef.class,         lsTDId);    
            cleanUpTree(GeologicTimePeriodTreeDef.class, gtpTDId);
        } else
        {
            // error!
        }
        
        stmt.close();
    }
    
    /**
     * @throws SQLException
     */
    protected void checkTables() throws SQLException
    {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("show tables");
        while (rs.next())
        {
            String tablename = rs.getString(1);
            int count = BasicSQLUtils.getNumRecords("select count(*) FROM "+tablename, connection);
            if (count > 0)
            {
                System.out.println(tablename+" "+count);
            }
        }
        rs.close();
        statement.close();
            
    }
    
    /**
     * @param divisionId
     * @throws SQLException
     */
    protected void cleanUpTree(final Class<?> treeDefClass, 
                               final int      treeDefId) throws SQLException
    {
        Statement   stmt             = connection.createStatement();
        DBTableInfo tblInfo          = DBTableIdMgr.getInstance().getByShortClassName(treeDefClass.getSimpleName());
        String      className        = treeDefClass.getSimpleName().substring(0, treeDefClass.getSimpleName().length()-7);
        String      tableNameTD      = tblInfo.getName();
        String      primaryKeyTD     = tblInfo.getIdColumnName();
        String      itemTableNameTDI = tableNameTD + "item";

        String delSql;
        
        if (treeDefClass == TaxonTreeDef.class)
        {
            String tmpSql = "SELECT tc.TaxonCitationID FROM taxoncitation tc INNER JOIN taxon tx ON tc.TaxonID = tx.TaxonID INNER JOIN taxontreedef ttd ON tx.TaxonTreeDefID = ttd.TaxonTreeDefID = "+treeDefId;
            if (debugUpdate) System.err.println(tmpSql);
            
            Vector<Integer> ids = getIds(tmpSql, 0);
            for (Integer id : ids)
            {
                delSql = "DELETE FROM taxoncitation WHERE TaxonCitationID = "+id;
                int count =stmt.executeUpdate(delSql);
                if (debugUpdate) System.err.println(count +" - "+delSql);
            }
        }
        
        delSql = "DELETE FROM "+className.toLowerCase() + " WHERE "+primaryKeyTD+" = "+treeDefId+" ORDER BY AcceptedID DESC, ParentID DESC";
        if (debugUpdate) System.err.println(delSql);
        stmt.executeUpdate(delSql);
        
        delSql = "DELETE FROM "+itemTableNameTDI + " WHERE "+primaryKeyTD+" = "+treeDefId+" ORDER BY RankID DESC";
        if (debugUpdate) System.err.println(delSql);
        stmt.executeUpdate(delSql);
        
        delSql = "DELETE FROM "+tableNameTD + " WHERE "+primaryKeyTD+" = "+treeDefId;
        if (debugUpdate) System.err.println(delSql);
        stmt.executeUpdate(delSql);
        
        stmt.close();
    }
    
    /**
     * @param divisionId
     * @throws SQLException
     */
    protected void cleanUpAgentsForDivision(final int divisionId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        
        String sql;
        Vector<Integer> ids;
        
        sql = "SELECT AgentID FROM agent WHERE DivisionID = "+divisionId + " AND ParentOrganizationID IS NOT NULL";
        System.err.println(sql);
        ids = getIds(sql, 0);
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        sql = "SELECT agent.AgentID FROM agent INNER JOIN groupperson ON agent.AgentID = groupperson.MemberID WHERE DivisionID = "+divisionId;
        System.err.println(sql);
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM groupperson WHERE MemberID = "+id);
        }
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        sql = "SELECT AgentID FROM agent WHERE DivisionID = "+divisionId;
        ids = getIds(sql, 0);
        System.err.println(sql);
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        stmt.close();
        
    }
    
    /**
     * @param stmt
     * @param divisionId
     * @param ids
     * @throws SQLException
     */
    protected void cleanUpAgentsForDivision(Statement stmt,
                                            final int divisionId, 
                                            final Vector<Integer> ids,
                                            final boolean doDelAgents) throws SQLException
    {
        for (Integer agentId : ids)
        {
            System.err.println("Agent: "+agentId);
            cleanUpSpecifyUser(agentId);
            
            
            boolean isOKToDel = true;
            // Check to see if the Agent is has edited and remaining tables
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                DBFieldInfo fi = ti.getFieldByColumnName("CreatedByAgentID");
                if (fi != null)
                {
                    ResultSet rs   = stmt.executeQuery("SELECT cpount(*) FROM "+ti.getName()+" WHERE CreatedByAgentID = "+agentId+" OR ModifiedByAgentID = "+agentId);
                    if (rs.next())
                    {
                        int count = rs.getInt(1);
                        if (count > 0)
                        {
                            System.err.println(ti.getName()+"  uses agent "+agentId);
                            isOKToDel = false;
                        }
                    }
                    rs.close();
                }
            }
            
            if (isOKToDel)
            {
                stmt.executeUpdate("DELETE FROM accessionagent WHERE AgentID = "+agentId);
                stmt.executeUpdate("DELETE FROM permit WHERE IssuedToID = "+agentId+" OR IssuedByID = "+agentId);
                stmt.executeUpdate("DELETE FROM agentvariant WHERE AgentID = "+agentId);
                stmt.executeUpdate("DELETE FROM agentgeography WHERE AgentID = "+agentId);
                stmt.executeUpdate("DELETE FROM agentspecialty WHERE AgentID = "+agentId);
                stmt.executeUpdate("DELETE FROM address WHERE AgentID = "+agentId);
                if (doDelAgents)
                {
                    stmt.executeUpdate("DELETE FROM agent WHERE AgentID = "+ agentId);
                }
            }
        }
    }
    
    
    /**
     * @param agentId
     * @throws SQLException
     */
    protected void cleanUpSpecifyUser(final int agentId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        ResultSet rs   = stmt.executeQuery("SELECT a.SpecifyUserID FROM agent a INNER JOIN specifyuser spu ON a.SpecifyUserID = spu.SpecifyUserID WHERE a.AgentId = "+agentId);
        while (rs.next())
        {
            int specifyUserID = rs.getInt(1);
            Vector<Integer> ids = getIds("SELECT SpAppResourceDirID FROM spappresourcedir WHERE SpecifyUserID = "+specifyUserID, 0);
            for (Integer id : ids)
            {
                deleteAppResourceDir(id);
            }
            
            ids = getIds("SELECT RecordSetID FROM recordset WHERE SpecifyUserID = "+specifyUserID, 0);
            for (Integer id : ids)
            {
                deleteRecordSet(id);
            }
            
            deleteReportsAndQueries(specifyUserID);
            
            ids = getIds("SELECT WorkbenchID FROM workbench WHERE SpecifyUserID = "+specifyUserID, 0);
            for (Integer id : ids)
            {
                deleteWorkBench(id);
            }
        }
        rs.close();
        stmt.close();
    }
    
    /**
     * @param recordSetId
     * @throws SQLException
     */
    protected void deleteRecordSet(final int recordSetId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("DELETE FROM recordsetitem WHERE RecordSetID = "+recordSetId);
        stmt.executeUpdate("DELETE FROM recordset WHERE RecordSetID = "+recordSetId);
        stmt.close();
    }

    /**
     * @param appResDirId
     * @throws SQLException
     */
    protected void deleteReportsAndQueries(final int specifyUserId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        //stmt.executeUpdate("SELECT FROM spreport WHERE SpecityUserID = "+specifyUserId);
        //stmt.executeUpdate("SELECT FROM spreport WHERE SpecityUserID = "+specifyUserId);
        
        String sql = "SELECT r.SpReportID FROM spreport r INNER JOIN spquery q ON r.SpQueryID = q.SpQueryID WHERE r.SpecifyUserID  = "+specifyUserId;
        System.err.println(sql);
        Vector<Integer> reportIds  = getIds(sql, 0);
        
        for (Integer id : reportIds)
        {
            stmt.executeUpdate("DELETE FROM spreoprts WHERE SpReportID "+id);
        }
        
        sql = "SELECT SpQueryID FROM spquery WHERE SpecifyUserID = "+specifyUserId;
        System.err.println(sql);
            
        Vector<Integer> queryIds  = getIds(sql, 0);
        for (Integer id : queryIds)
        {
            stmt.executeUpdate("DELETE FROM spqueryfield WHERE SpQueryID = "+id);
            stmt.executeUpdate("DELETE FROM spquery WHERE SpQueryID = "+id);
        }
    }

    /**
     * @param appResDirId
     * @throws SQLException
     */
    protected void deleteAppResourceDir(final int appResDirId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        String    sql  = "SELECT SpAppResourceDataID FROM spappresourcedata sd INNER JOIN spappresource sr ON sd.SpAppResourceID = sr.SpAppResourceID WHERE sr.SpAppResourceDirID = "+appResDirId;
        Vector<Integer> ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM spappresourcedata WHERE SpAppResourceDataID = "+id);
        }
        sql = "DELETE FROM spappresource WHERE SpAppResourceDirID = "+appResDirId;
        stmt.executeUpdate(sql);
        
        sql  = "SELECT ard.SpAppResourceDataID FROM spappresourcedata ard INNER JOIN spviewsetobj v ON ard.SpViewSetObjID = v.SpViewSetObjID";
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM spappresourcedata WHERE S�DataID "+id);
        }
        sql = "DELETE FROM spviewsetobj WHERE SpAppResourceDirID = "+appResDirId;
        stmt.executeUpdate(sql);
        
        sql = "DELETE FROM spappresourcedir WHERE SpAppResourceDirID = "+appResDirId;
        stmt.executeUpdate(sql);
        
        stmt.close();
    }
    
    /**
     * @param wbId
     * @throws SQLException
     */
    protected void deleteWorkBench(final int wbId) throws SQLException
    {
        Statement stmt = connection.createStatement();
        
        Vector<Integer> templateIds = getIds("SELECT workbench.WorkbenchTemplateID FROM workbench WHERE WorkbenchID = "+wbId, 0);
        
        String sql = "SELECT wdi.WorkbenchDataItemID FROM workbench wb INNER JOIN workbenchrow wbr ON wb.WorkbenchID = wbr.WorkbenchID " +
                     "INNER JOIN workbenchdataitem wdi ON wbr.WorkbenchRowID = wdi.WorkbenchRowID WHERE wb.WorkbenchID = "+wbId;
        Vector<Integer> ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM workbenchdataitem WHERE WorkbenchDataItemID = "+id);
        }
        
        sql = "SELECT wri.WorkbenchRowImageID FROM workbench wb INNER JOIN workbenchrow wbr ON wb.WorkbenchID = wbr.WorkbenchID " +
              "INNER JOIN workbenchrowimage wri ON wbr.WorkbenchRowID = wri.WorkbenchRowID WHERE wb.WorkbenchID = "+wbId;
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM workbenchrowimage WHERE WorkbenchRowImageID = "+id);
        }
        
        sql = "SELECT wbr.WorkbenchRowID FROM workbench wb INNER JOIN workbenchrow wbr ON wb.WorkbenchID = wbr.WorkbenchID WHERE wbr.WorkbenchID = "+wbId;
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM workbenchrow WHERE WorkbenchRowID = "+id);
        }
        stmt.executeUpdate("DELETE FROM workbench WHERE WorkbenchID = "+wbId);
        
        for (Integer id : templateIds)
        {
            stmt.executeUpdate("DELETE FROM workbenchtemplatemappingitem WHERE WorkbenchTemplateID = "+id);
            stmt.executeUpdate("DELETE FROM workbenchtemplate WHERE WorkbenchTemplateID = "+id);
        }
        stmt.close();
    }

    
    //-------------------------------------------------------------------
    class StackItem
    {
        protected DBTableInfo      tableInfo;
        protected Stack<StackItem> stack          = new Stack<StackItem>();
        protected Stack<StackItem> postProcStack  = new Stack<StackItem>();
        protected String           sql;
        protected String           delSql;
        protected boolean          isBuildingSQL;
        protected boolean          isBuildingDelSQL;
        
        /**
         * @param tableInfo
         * @param sql
         */
        public StackItem(final DBTableInfo tableInfo, 
                         final String sql, 
                         final String delSql,
                         final boolean isBuildingSQL,
                         final boolean isBuildingDelSQL)
        {
            super();
            this.tableInfo     = tableInfo;
            this.sql           = sql;
            this.delSql        = delSql;
            this.isBuildingSQL = isBuildingSQL;
            this.isBuildingDelSQL = isBuildingDelSQL;
        }
        
        /**
         * @param tableInfo
         * @param sql
         */
        public StackItem(final DBTableInfo tableInfo, 
                         final String sql, 
                         final String delSql,
                         final boolean isBuildingAllSQL)
        {
            this(tableInfo, sql, delSql, isBuildingAllSQL, isBuildingAllSQL);
        }
        
        
        public StackItem(final DBTableInfo tableInfo, 
                         final String sql, 
                         final String delSql)
        {
            this(tableInfo, sql, delSql, true);
        }
        
        public StackItem push(final DBTableInfo ti, final String sqlArg, final String delSqlArg)
        {
            StackItem si = new StackItem(ti, sqlArg, delSqlArg);
            stack.push(si);
            return si;
        }
        
        public void removeChild(final StackItem child)
        {
            stack.remove(child);
        }
        
        public StackItem pushPPS(final StackItem si)
        {
            postProcStack.push(si);
            return si;
        }
        
        /**
         * @return the stack
         */
        public Stack<StackItem> getStack()
        {
            return stack;
        }

        /**
         * @return the tableInfo
         */
        public DBTableInfo getTableInfo()
        {
            return tableInfo;
        }

        /**
         * @return the sql
         */
        public String getSql()
        {
            return sql;
        }

        /**
         * @return the delSql
         */
        public String getDelSql()
        {
            return delSql;
        }

        /**
         * @return the postProcStack
         */
        public Stack<StackItem> getPostProcStack()
        {
            return postProcStack;
        }

        /**
         * @return the isBuildingSQL
         */
        public boolean isBuildingSQL()
        {
            return isBuildingSQL;
        }

        /**
         * @return the isBuildingDelSQL
         */
        public boolean isBuildingDelSQL()
        {
            return isBuildingDelSQL;
        }

        /**
         * @param isBuildingSQL the isBuildingSQL to set
         */
        public void setBuildingSQL(boolean isBuildingSQL)
        {
            this.isBuildingSQL = isBuildingSQL;
        }

        /**
         * @param isBuildingSQL the isBuildingSQL to set
         */
        public void setBuildingDelSQL(boolean isBuildingDelSQL)
        {
            this.isBuildingDelSQL = isBuildingDelSQL;
        }
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DBTableIdMgr.getInstance().getByShortClassName(Accession.class.getSimpleName()); // Preload
        
        
        new SpecifyDeleteHelper(false);
        
        //s.getSubTables(Discipline.class);
        //s.getSubTables(Division.class);
    }

}
