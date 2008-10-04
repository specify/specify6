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

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Discipline;

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
    protected boolean debug       = false;
    protected boolean debugUpdate = true;
    protected Integer totalCount = null;
    protected int     counter    = 0;
    
    /**
     * 
     */
    public SpecifyDeleteHelper()
    {
        super();
        
        DBConnection dbConn = DBConnection.createInstance("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", "testfish", "jdbc:mysql://localhost/testfish", "Specify", "Specify");
        Connection conn = null;
        conn = dbConn.createConnection();

        try
        {
            delRecordFromTable(conn, Discipline.class, 3);
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            try
            {
                conn.rollback();
            } catch (SQLException ex2)
            {
                ex.printStackTrace();
            }
            
        } finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    
    /**
     * @param conn
     * @param tableInfo
     * @param id
     * @return
     * @throws SQLException
     */
    public boolean delRecordFromTable(final Connection  conn, 
                                      final Class<?>    cls, 
                                      final int         id) throws SQLException
    {
        StackItem root = new StackItem(null, null, null);
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
        
        String sqlStr = "SELECT "+tblInfo.getIdColumnName()+" FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = ";
        String delStr = "DELETE FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = ";
        
        getSubTables(root, cls, sqlStr, delStr, id);
        
        if (debug)
        {
            System.out.println("\n------------------------------------------\n");
            dumpStack(root, 0);
        }
        
        boolean isAutoCommit = conn.getAutoCommit();
        
        conn.setAutoCommit(false);

        Statement stmt = conn.createStatement();
        if (stmt != null)
        {
            for (StackItem si : root.getStack())
            {
                showIds(si, 0, id, conn, stmt, false);
            }
            
            if (debug) System.out.println("Count: "+counter);
            
            if (!debug)
            {
                totalCount = counter;
                counter = 0;
                for (StackItem si : root.getStack())
                {
                    showIds(si, 0, id, conn, stmt, true);
                }
            }
            stmt.close();
                
            conn.commit();
            
            conn.setAutoCommit(isAutoCommit);
            
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
    protected void getSubTables(final StackItem parent,
                                final Class<?>  cls, 
                                final String    sqlStr,
                                final String    delSqlStr,
                                final int       level)
    {
        if (debug)
        {
            printLevel(level);
            System.out.println(cls.getSimpleName());
        }
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
        
        StackItem child = parent.push(tblInfo, sqlStr, delSqlStr);
        
        Hashtable<String, DBTableChildIFace> hash      = new Hashtable<String, DBTableChildIFace>();
        Hashtable<String, DBTableChildIFace> otherHash = new Hashtable<String, DBTableChildIFace>();
        
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

            if (methodName.equals("getGeographyTreeDef") || methodName.equals("getTreeEntries"))
            {
                int x = 0;
                x++;
            }
            
            if (methodName.endsWith("TreeDef"))
            {
                String className      = methodName.substring(3, methodName.length()-7);
                
                //String tableName      = className.toLowerCase() + "treedef";
                //String primaryKey     = className+"ID";

                String tableNameTD      = className.toLowerCase() + "treedef";
                String primaryKeyTD     = className+"TreeDefID";
                
                String itemTableNameTDI  = className.toLowerCase() + "treedefitem";
                //String primaryKeyTDI    = className+"TreeDefItemID";

                String sql;
                String delSql;
                
                
                sql    = "SELECT "+primaryKeyTD+" FROM "+tblInfo.getName() + " WHERE "+tblInfo.getIdColumnName()+" = ";
                delSql = "DELETE FROM "+className.toLowerCase() + " WHERE "+primaryKeyTD+" = XXX order by "+className+"ID desc";
                child.pushPPS(new StackItem(null, sql, delSql));
                
                sql    = "SELECT "+primaryKeyTD+" FROM "+tblInfo.getName() + " WHERE "+tblInfo.getIdColumnName()+" = ";
                delSql = "DELETE FROM "+tableNameTD + " WHERE "+primaryKeyTD+" = ";
                child.pushPPS(new StackItem(null, sql, delSql));
                
                sql    = "SELECT "+primaryKeyTD+" FROM "+tblInfo.getName() + " WHERE "+tblInfo.getIdColumnName()+" = ";
                delSql = "DELETE FROM "+itemTableNameTDI + " WHERE "+primaryKeyTD+" = ";
                child.pushPPS(new StackItem(null, sql, delSql));

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
                    
                    hash.put(relInfo.getClassName(), relInfo);
                    otherHash.put(relInfo.getClassName(), relInfo);
                    
                    String sql    = "SELECT "+joinColName+ " FROM " + joinTableName + " WHERE " + joinColName + " = ";
                    String delSql = "DELETE FROM "+joinTableName+ " WHERE " + joinColName + " = ";
                    
                    if (debug)
                    {
                        printLevel(level);
                        System.out.println(sql);
                    }
                    
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(relInfo.getDataClass().getSimpleName());
                    child.push(ti, sql, delSql);
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
                        String delSQL = "DELETE FROM "+ti.getName() + " WHERE "+ti.getIdColumnName()+" = ";
                        
                        if (debug)
                        {
                            printLevel(level);
                            System.out.println(sql);
                            printLevel(level);
                            System.out.println(delSQL);
                        }
                        
                        hash.put(relInfo.getClassName(), relInfo);
                        otherHash.put(relInfo.getDataClass().getSimpleName(), relInfo);
                        
                        getSubTables(child, ti.getClassObj(), sql, delSQL, level+1);
                        
                    } else
                    {
                        String shortClassName = relInfo.getDataClass().getSimpleName();
                        String sql    = "SELECT "+shortClassName+"ID FROM "+shortClassName.toLowerCase() + " WHERE "+tblInfo.getClassObj().getSimpleName()+"ID = ";
                        String delSql = "DELETE FROM "+shortClassName.toLowerCase() + " WHERE "+shortClassName+"ID = ";
                        child.push(null, sql, delSql);
                    }
                }
            }
        }
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            if (ti != tblInfo)
            {
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    if (ri.getDataClass() == tblInfo.getClassObj() && StringUtils.isEmpty(ri.getOtherSide()))
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
                        
                        getSubTables(child, ti.getClassObj(), sql, delSql, level+1);
                    }
                }
            }
        }
        
        if (debug) System.out.println();
        
        if (cls == Discipline.class)
        {
            String sql    = "SELECT DisciplineID FROM agent_discipline WHERE DisciplineID = ";
            String delSql = "DELETE FROM agent_discipline WHERE DisciplineID = ";
            child.push(null, sql, delSql);
        }

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
     * @param si
     * @param level
     * @param id
     * @param conn
     * @param stmt
     * @throws SQLException
     */
    protected void showIds(final StackItem si, 
                           final int       level,
                           final int       id, 
                           final Connection conn,
                           final Statement stmt,
                           final boolean   doDeletes) throws SQLException
    {
        if (debugUpdate)
        {  
            printLevel(level);
            System.out.print(" -- ");
            System.out.println(si.getTableInfo() == null ? "Root" : si.getTableInfo().getName()+" -- ");
        }
        
        
        int             cnt = 0;
        Vector<Integer> ids = null;
        if (si.getSql() != null)
        {
            ids = new Vector<Integer>();
            String sql = si.getSql() + Integer.toString(id);

            if (debugUpdate) System.err.println(sql);
            
            ResultSet rs  = stmt.executeQuery(sql);
            while (rs.next())
            {
                int rowId = rs.getInt(1);
                ids.add(rowId);
                
                if (debugUpdate)
                {
                    printLevel(level);
                    System.out.println("Deleting ID: "+rowId);
                }
                
                Statement statement = conn.createStatement();
                for (StackItem s : si.getStack())
                {
                   showIds(s, level+1, rowId, conn, statement, doDeletes);
                }
                statement.close();
                cnt++;
            }
            rs.close();
            counter += cnt;
        }
        
        if (doDeletes)
        {
            if (ids != null)
            {
                if (cnt > 0)
                {
                    for (Integer itemId : ids)
                    {
                        String delSql = si.getDelSql() + itemId;
                        if (StringUtils.contains(si.getDelSql(), "XXX"))
                        {
                            delSql = StringUtils.replace(si.getDelSql(), "XXX", Integer.toString(itemId));
                        }
                        if (debugUpdate) System.err.println(delSql);
                        int count = stmt.executeUpdate(delSql);
                        if (debugUpdate) System.err.println("Count: "+count);
                    }
                }
            } else
            {
                String delSql = si.getDelSql() + id;
                if (debugUpdate) System.err.println(delSql);
                
                int count = stmt.executeUpdate(delSql);
                if (debugUpdate) System.err.println("Count: "+count);
            }
            
            for (StackItem stckItm : si.getPostProcStack())
            {
                showIds(stckItm, level+2, id, conn, stmt, true);
            }
        }
        
        if (debugUpdate)
        {
            printLevel(level);
            System.out.println("Records to delete: "+cnt);
        }
            
        if (totalCount != null)
        {
            System.err.println(counter+" / "+totalCount);
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DBTableIdMgr.getInstance().getByShortClassName(Accession.class.getSimpleName()); // Preload
        
        
        new SpecifyDeleteHelper();
        
        //s.getSubTables(Discipline.class);
        //s.getSubTables(Division.class);
    }

    
    class StackItem
    {
        protected DBTableInfo      tableInfo;
        protected Stack<StackItem> stack          = new Stack<StackItem>();
        protected Stack<StackItem> postProcStack  = new Stack<StackItem>();
        protected String           sql;
        protected String           delSql;
        
        /**
         * @param tableInfo
         * @param sql
         */
        public StackItem(final DBTableInfo tableInfo, 
                         final String sql, 
                         final String delSql)
        {
            super();
            this.tableInfo = tableInfo;
            this.sql       = sql;
            this.delSql    = delSql;
        }
        
        public StackItem push(final DBTableInfo ti, final String sqlArg, final String delSqlArg)
        {
            StackItem si = new StackItem(ti, sqlArg, delSqlArg);
            stack.push(si);
            return si;
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

    }
}
