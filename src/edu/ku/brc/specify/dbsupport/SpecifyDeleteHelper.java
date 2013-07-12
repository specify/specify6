/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAuditLog;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.Workbench;
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
    private static String CNT      = "CNT";
    private static String MSG      = "MSG";
    private static String STR2PARM = "%s %s";
    private static String STR3PARM = "%s %s %s";
    
    protected static boolean    debug        = false;
    protected static boolean    debugUpdate  = false;
    
    protected String            delMsgStr;
    protected DBTableIdMgr      tblMgr;
    
    protected boolean           doTrees      = true;

    protected Integer           totalCount   = null;
    protected int               counter      = 0;
    
    protected SwingWorker<?, ?> worker       = null;
    protected JProgressBar      progressBar  = null;
    protected JLabel            titleLbl     = null;
    
    protected Connection        connection   = null;
    
    protected HashSet<Class<?>> classHash = new HashSet<Class<?>>();
    
    /**
     * 
     */
    public SpecifyDeleteHelper()
    {
        super();
        
        delMsgStr = UIRegistry.getResourceString("DELETING");
        tblMgr    = DBTableIdMgr.getInstance();
        
        /* Need for when we test from main()
        DBConnection dbConn = DBConnection.createInstance("com.mysql.jdbc.Driver", 
                                                          "org.hibernate.dialect.MySQLDialect", 
                                                          "wpupdater", 
                                                          "jdbc:mysql://localhost/wbupdater", 
                                                          "root", 
                                                          "root");*/
                                                          
        DBConnection dbConn = DBConnection.getInstance();
        connection = dbConn.createConnection();
        //BasicSQLUtils.setDBConnection(connection);
        
        /*if (false)
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
        }*/
    }
    
    /**
     * @param workerArg
     * @param title (already localized)
     * @return a modal dialog showing the progress
     */
    public JDialog initProgress(final SwingWorker<?, ?> workerArg,
                                final String title)
    {
        this.worker = workerArg;
        
        if (workerArg != null)
        {
            JDialog dialog = new JDialog((Dialog)null, true);
            
            titleLbl    = new JLabel(title);
            progressBar = new JProgressBar(0, 100);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,p,4px,p,f:p:g"));
            
            pb.add(titleLbl,    cc.xy(1, 2));
            pb.add(progressBar, cc.xy(1, 4));
            pb.setDefaultDialogBorder();
            dialog.setContentPane(pb.getPanel());
            
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.pack();
            dialog.setSize(500, 150);
            workerArg.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
            return dialog;
        }
        return null;
    }
    
    /**
     * @param msgKey
     * @param vals
     */
    private void fireMsg(final String msgKey, final Object...vals)
    {
        if (worker != null)
        {
            worker.firePropertyChange(MSG, null, UIRegistry.getLocalizedMessage(msgKey, vals));
        }
    }
    

    /**
     * 
     */
    public void rollback()
    {
        try
        {
            if (connection != null)
            {
                connection.rollback();
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * @param doClose
     */
    public void done(final boolean doClose)
    {
        if (worker != null)
        {
            worker.firePropertyChange(CNT, SwingWorker.StateValue.DONE, SwingWorker.StateValue.DONE);
        }

        if (doClose)
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
    
    /**
     * @param cls
     * @param id
     * @return true if there are no referential integrity, or other, constraints preventing the record from being deleted.
     * 
     * Quick (or not), dirty, easy, loutish approach.
     */
    public boolean isRecordDeletable(final Class<?>  cls, final int id)
    {
    	boolean result;
    	try
    	{
    		try 
    		{
    			delRecordFromTable(cls, id, true);
    			result = true;
    		} catch (SQLException ex)
    		{
    			result = false;
    		}
    	} finally
    	{
    		rollback();
    	}
    	return result;
    }
    
    /**
     * @param cls
     * @param id
     * @param excludeCls
     * @param excludeFromRequirement
     * @return
     * @throws SQLException
     */
    public boolean isRecordRequired(final Class<?> cls, final int id, final Class<?> excludeCls, final int[] excludeFromRequirement) throws SQLException
    {
        StackItem root      = new StackItem(null, null, null);
        DBTableInfo tblInfo = tblMgr.getByShortClassName(cls.getSimpleName());
        
        String sqlStr = "SELECT "+tblInfo.getIdColumnName()+" FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = ";
        
        getSubTables(root, cls, id, sqlStr, null, 0, null, true);

        Statement stmt = connection.createStatement();
        try 
        {
        	if (stmt != null)
        	{
        		for (StackItem si : root.getStack())
        		{
        			if (checkRequiredRecs(si, 0, id, excludeCls, excludeFromRequirement))
        			{
        				return true;
        			}
        		}
        	}
        } finally
        {
        	 stmt.close();	
        }
        
        return false;
    }
    
    
    /**
     * @param rec
     * @return
     */
    public boolean isRecordShared(final Class<?> cls, final Integer id) throws SQLException
    {
    	StackItem root      = new StackItem(null, null, null);
        getSubTables(root, cls, id, null, null, 0, null, true);
        StackItem s = root.getStack().peek();
    	for (StackItem si : s.getStack())
    	{
    		System.out.println(si.getSql() + id);
    		Vector<Integer> ids = getIds(si.getSql() + id, -1);
    		if (ids != null && ids.size() > 1)
    		{
    			return true;
    		}
    		if (ids != null && ids.size() == 1)
    		{
    			if (isRecordShared(si.getTableInfo().getClassObj(), ids.get(0)))
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
     * @param excludeCls
     * @param excludeFromRequirement
     * @return
     * @throws SQLException
     */
    public boolean checkRequiredRecs(final StackItem si, 
    		final int level, 
    		final int id,
    		final Class<?> excludeCls, 
    		final int[] excludeFromRequirement) throws SQLException
    {
    	Vector<Integer> ids = getIds(si.getSql() + id, level);
    	if (si.getTableInfo().getClassObj().equals(excludeCls))
    	{
    		for (int exid : excludeFromRequirement)
    		{
    			int idx = ids.indexOf(exid);
    			if (idx != -1)
    			{
    				ids.remove(idx); //assuming no dups in ids
    			}
    		}
    	}
    	
    	if (ids.size() > 0)
    	{
    		return true;
    	}
    	
//    	for (StackItem s : si.getStack())
//    	{
//    		if (checkRequiredRecs(s, level+1, excludeCls, excludeFromRequirement))
//    		{
//    			return true;
//    		}
//    	}
    	
    	return false;
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
        DBTableInfo tblInfo = tblMgr.getByShortClassName(cls.getSimpleName());
        
        String sqlStr = "SELECT "+tblInfo.getIdColumnName()+" FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = ";
        String delStr = doDeleteId ? "DELETE FROM "+ tblInfo.getName() + " WHERE " +tblInfo.getIdColumnName()+" = " : null;
        
        fireMsg("INITIALIZING");

        getSubTables(root, cls, id, sqlStr, delStr, 0, null, false);
        
        debug = false;
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
                fireMsg(STR2PARM, delMsgStr, si.getTableInfo().getTitle());
                deleteRecords(si, 0, id, stmt, false);
            }
            
            if (debug) System.out.println("Count: "+counter);
            
            if (!debug)
            {
                totalCount = counter;
                counter    = 0;
                
                if (worker != null && totalCount != null)
                {
                    worker.firePropertyChange(CNT, totalCount, (int)( (100.0 * counter) / totalCount));
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
                                     final Hashtable<String, Boolean> inUseHashArg,
                                     final boolean checkIfIsShared/*,
                                     final int excludeId /*check if is required by anything other than this id*/)
    {
        if (classHash.contains(cls))
        {
            return null;
        }
        classHash.add(cls);
        
        if (debug)
        {
            printLevel(level);
            System.out.println(cls.getSimpleName());
        }
        
        DBTableInfo tblInfo = tblMgr.getByShortClassName(cls.getSimpleName());
        
        StackItem child = parent.push(tblInfo, sqlStr, delSqlStr);
        
        Hashtable<String, Boolean> inUseHash = inUseHashArg == null && level == 1 ? new Hashtable<String, Boolean>() : inUseHashArg;
        
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
                                ex.printStackTrace();
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDeleteHelper.class, ex);
                            }
                            if (treeClass == cls)
                            {
                                classHash.remove(cls);
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
            boolean includeSubTable = false;
            if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
            {
                String nm = method.getName();
                boolean isAttachment = nm.indexOf("Attachment") > -1;
                
                boolean doDel = false;
                javax.persistence.OneToMany oneToMany = (javax.persistence.OneToMany)method.getAnnotation(javax.persistence.OneToMany.class);
                for (CascadeType ct : oneToMany.cascade())
                {
                    if (ct == CascadeType.ALL || ct == CascadeType.REMOVE)
                    {
                        doDel = true;
                        break;
                    }
                }
                
                if (!isAttachment && !doDel && method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                {
                    org.hibernate.annotations.Cascade cascade = (org.hibernate.annotations.Cascade)method.getAnnotation(org.hibernate.annotations.Cascade.class);
                    for (org.hibernate.annotations.CascadeType ct : cascade.value())
                    {
                        if (ct == org.hibernate.annotations.CascadeType.ALL ||
                            ct == org.hibernate.annotations.CascadeType.DELETE ||
                            ct == org.hibernate.annotations.CascadeType.REMOVE)
                        {
                            doDel = true;
                            break;
                        }
                    }
                }
                
                
                isOKToDel = !doDel ? isOKToDel(method) : true;
                if (checkIfIsShared && cls.equals(Geography.class) && method.getName().equals("getLocalities")) //Hibernate!?. Looks like the Cascade annotation is wrong.
                {
                	isOKToDel = false;
                }
                includeSubTable = !checkIfIsShared ? isOKToDel : !isOKToDel;
                colName = tblInfo.getIdColumnName();
                
            } else if (!checkIfIsShared && method.isAnnotationPresent(javax.persistence.ManyToOne.class))
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
                includeSubTable = !checkIfIsShared ? isOKToDel : !isOKToDel;
                if (includeSubTable)
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
                    System.out.println(joinColName);
                    
                    
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
                        
                        DBTableInfo ti = tblMgr.getByShortClassName(relInfo.getDataClass().getSimpleName());
                        if (!checkIfIsShared)
                        {
                        	child.push(ti, sql, delSql);
                        }
                        
                    } else
                    {
                        //System.err.println(cls.getName());
                    }

                }
            } 
            
            if (includeSubTable)
            {
                //System.out.println(method.getName()+"  "+method.getReturnType().getSimpleName());
                String             relName = method.getName().substring(3);
                DBRelationshipInfo relInfo = tblInfo.getRelationshipByName(relName);
                if (relInfo != null)
                {
                    DBTableInfo ti = tblMgr.getByClassName(relInfo.getClassName());
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
                                if (!checkIfIsShared)
                                {
                                	getSubTables(child, ti.getClassObj(), id, sql, delSql, level+1, inUseHash, checkIfIsShared);
                                } else
                                {
                                	child.push(ti, sql, delSql);
                                }
                                
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
        
        for (DBTableInfo ti : tblMgr.getTables())
        {
            if (ti != tblInfo)
            {
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    /*System.out.println(ri.getName());*/

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
                            
                            if (inUseHash != null) inUseHash.put(ti.getClassName(), true);
                            //if (!checkIfIsShared)
                            {
                            	getSubTables(child, ti.getClassObj(), id, sql, delSql, level+1, inUseHash, checkIfIsShared);
                            }
                            
                        } else if (ri.getDataClass() == tblInfo.getClassObj() && !hashOK && StringUtils.isEmpty(ri.getOtherSide()))
                        {
                            if (debug) System.out.println("Skipping "+ti.getClassObj().getSimpleName()+" for "+tblInfo.getClassObj().getSimpleName());
                        }
                    } else
                    {
                        //System.err.println(ri.getDataClass().getName());
                    }
                }
            }
        }
        
        if (debug) System.out.println();

        classHash.remove(cls);
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

                                if (StringUtils.contains(si.getDelSql(), "XXX"))
                                {
                                    delSql = StringUtils.replace(si.getDelSql(), "XXX", Integer.toString(itemId));
                                }
                                
                                if (si.getTableInfo().getClassObj() == Discipline.class && StringUtils.contains(delSql, "FROM discipline"))
                                {
                                    deleteDiscipline(delSql, itemId);
                                    
                                } else
                                {
                                    if (si.getTableInfo().getClassObj() == Division.class && StringUtils.contains(delSql, "FROM division"))
                                    {
                                        cleanUpAgentsForDivision(id);
                                    } else if (si.getTableInfo().getName().endsWith("attribute")) 
                                    {
                                    	String preDelSql = "update " + si.getTableInfo().getName().replace("attribute", "")
                                    			+ " set " + si.getTableInfo().getPrimaryKeyName() + " = null where "
                                    			+ si.getTableInfo().getPrimaryKeyName() + " = " + itemId;
                                        if (debugUpdate) System.err.println(preDelSql);
                                        int count = stmt.executeUpdate(preDelSql);
                                        if (debugUpdate) System.err.println("Count: "+count);
                                    	
                                    }
                                    
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
                deleteRecords(stckItm, level+2, id, stmt, true);
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
        
        if (worker != null && totalCount != null)
        {
            worker.firePropertyChange(CNT, totalCount, (int)( (100.0 * counter) / totalCount));
        }
    }
    
    /**
     * @param delSql
     * @param id
     */
    protected void deleteDiscipline(final String delSql, final int id) throws SQLException
    {
        progressBar.setIndeterminate(true);
        
        String dispName = BasicSQLUtils.querySingleObj("SELECT Name FROM discipline WHERE DisciplineID = " + id);
        
        fireMsg(STR3PARM, delMsgStr, dispName, tblMgr.getTitleForId(Discipline.getClassTableId()));
        
        Statement stmt = connection.createStatement();
        
        for (Integer collId : BasicSQLUtils.queryForInts("SELECT CollectionID FROM collection WHERE DisciplineID = "+id))
        {
            String dSQL = "DELETE FROM sptasksemaphore WHERE CollectionID = "+collId;
            int count = stmt.executeUpdate(dSQL);
            if (debugUpdate) System.err.println(count +" - "+dSQL);
            
            String sql = "SELECT RecordSetID FROM recordset WHERE CollectionMemberID = "+collId;
            if (debugUpdate) System.err.println(sql);
            Vector<Integer> ids = getIds(sql, 0);
            for (Integer rsId : ids)
            {
                deleteRecordSet(rsId);
            }
        }
        
        // Cleanup any Semaphores
        String dSQL = "DELETE FROM sptasksemaphore WHERE DisciplineID = "+id;
        int count = stmt.executeUpdate(dSQL);
        if (debugUpdate) System.err.println(count +" - "+dSQL);
        
        Integer txTDId   = null;
        Integer geoTTDId = null;
        Integer lsTDId   = null;
        Integer gtpTDId  = null;
        
        
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
            count = stmt.executeUpdate(delSql);
            if (debugUpdate) System.err.println(count +" - "+delSql);
            
            cleanUpTree(TaxonTreeDef.class,              txTDId,   dispName);    
            cleanUpTree(GeographyTreeDef.class,          geoTTDId, dispName);    
            cleanUpTree(LithoStratTreeDef.class,         lsTDId,   dispName);    
            cleanUpTree(GeologicTimePeriodTreeDef.class, gtpTDId,  dispName);
            
        } else
        {
            // error!
            System.err.println("Error!");
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
                if (debug) System.out.println(tablename+" "+count);
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
                               final int      treeDefId,
                               final String dispName) throws SQLException
    {
        Statement   stmt             = connection.createStatement();
        DBTableInfo tblInfo          = tblMgr.getByShortClassName(treeDefClass.getSimpleName());
        String      className        = treeDefClass.getSimpleName().substring(0, treeDefClass.getSimpleName().length()-7);
        String      tableNameTD      = tblInfo.getName();
        String      primaryKeyTD     = tblInfo.getIdColumnName();
        String      itemTableNameTDI = tableNameTD + "item";

        String delSql;
        
        fireMsg(STR3PARM, delMsgStr, dispName, tblMgr.getByShortClassName(className).getTitle());
        
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
        
        delSql = "DELETE FROM "+className.toLowerCase() + " WHERE "+primaryKeyTD+" = "+treeDefId+" ORDER BY NodeNumber DESC";
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
        
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(SpAuditLog.getClassTableId()));
        
        //stmt.executeUpdate("DELETE FROM spauditlog WHERE CreatedByAgentID =  )\
        sql = "SELECT au.SpAuditLogID FROM spauditlog AS au Inner Join agent AS a ON au.CreatedByAgentID = a.AgentID WHERE a.DivisionID = "+divisionId;
        if (debugUpdate) System.err.println(sql);
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM spauditlog WHERE SpAuditLogID = "+id);
        }
        
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(Agent.getClassTableId()));

        sql = "SELECT AgentID FROM agent WHERE DivisionID = "+divisionId + " AND ParentOrganizationID IS NOT NULL";
        if (debugUpdate) System.err.println(sql);
        ids = getIds(sql, 0);
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        sql = "SELECT a.AgentID FROM agent a INNER JOIN groupperson gp ON a.AgentID = gp.MemberID WHERE a.DivisionID = "+divisionId;
        if (debugUpdate) System.err.println(sql);
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            stmt.executeUpdate("DELETE FROM groupperson WHERE MemberID = "+id);
        }
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        sql = "SELECT AgentID FROM agent WHERE DivisionID = "+divisionId;
        ids = getIds(sql, 0);
        if (debugUpdate) System.err.println(sql);
        cleanUpAgentsForDivision(stmt, divisionId, ids, true);
        
        stmt.close();
        
    }
    
    /**
     * @param stmt
     * @param divisionId
     * @param ids
     * @throws SQLException
     */
    protected void cleanUpAgentsForDivision(final Statement stmt,
                                            final int divisionId, 
                                            final Vector<Integer> ids,
                                            final boolean doDelAgents) throws SQLException
    {
        SpecifyUser spUser    = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        Division    currDiv   = AppContextMgr.getInstance().getClassObject(Division.class);
        Agent       userAgent = null;
        for (Agent agt : spUser.getAgents())
        {
            if (agt.getDivision().getId().equals(currDiv.getId()))
            {
                userAgent = agt;
                break;
            }
        }
        
        for (Integer agentId : ids)
        {
            if (debugUpdate) System.err.println("Agent: "+agentId);
            
            // Check to see if the Agent is has edited and remaining tables
            for (DBTableInfo ti : tblMgr.getTables())
            {
                DBRelationshipInfo ri = ti.getRelationshipByName("createdByAgent");
                if (ri != null)
                {
                    int createdByCount   = BasicSQLUtils.getCountAsInt("SELECT count(*) FROM "+ti.getName()+" WHERE CreatedByAgentID = "+agentId);
                    if (createdByCount > 0)
                    {
                        String sql = String.format("UPDATE %s SET CreatedByAgentID = %d WHERE CreatedByAgentID = %d", ti.getName(), userAgent.getId(), agentId);
                        if (debug) System.err.println(sql);
                        stmt.executeUpdate(sql);
                    }
                    
                    int modifiedByCount = BasicSQLUtils.getCountAsInt("SELECT count(*) FROM "+ti.getName()+" WHERE ModifiedByAgentID = "+agentId);
                    if (modifiedByCount > 0)
                    {
                        String sql = String.format("UPDATE %s SET ModifiedByAgentID=%d WHERE ModifiedByAgentID = %d", ti.getName(), userAgent.getId(), agentId);
                        if (debug) System.err.println(sql);
                        stmt.executeUpdate(sql);
                    }
                }
            }
            
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
    
    /**
     * @param recordSetId
     * @throws SQLException
     */
    protected void deleteRecordSet(final int recordSetId) throws SQLException
    {
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(RecordSet.getClassTableId()));

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
        
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(SpReport.getClassTableId()));
        
        String sql = "SELECT r.SpReportID FROM spreport r INNER JOIN spquery q ON r.SpQueryID = q.SpQueryID WHERE r.SpecifyUserID  = "+specifyUserId;
        if (debugUpdate) System.err.println(sql);
        Vector<Integer> reportIds  = getIds(sql, 0);
        
        for (Integer id : reportIds)
        {
            stmt.executeUpdate("DELETE FROM spreoprts WHERE SpReportID "+id);
        }
        
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(SpQuery.getClassTableId()));

        sql = "SELECT SpQueryID FROM spquery WHERE SpecifyUserID = "+specifyUserId;
        if (debugUpdate) System.err.println(sql);
            
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
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(SpAppResource.getClassTableId()));

        Statement stmt = connection.createStatement();
        String    sql  = "SELECT SpAppResourceDataID FROM spappresourcedata sd INNER JOIN spappresource sr ON sd.SpAppResourceID = sr.SpAppResourceID WHERE sr.SpAppResourceDirID = "+appResDirId;
        if (debugUpdate) System.err.println(sql);
        Vector<Integer> ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            sql = "DELETE FROM spappresourcedata WHERE SpAppResourceDataID = "+id;
            stmt.executeUpdate(sql);
            if (debugUpdate) System.err.println(sql);
        }
        sql = "DELETE FROM spappresource WHERE SpAppResourceDirID = "+appResDirId;
        if (debugUpdate) System.err.println(sql);
        stmt.executeUpdate(sql);
        
        sql  = "SELECT ard.SpAppResourceDataID FROM spappresourcedata ard INNER JOIN spviewsetobj v ON ard.SpViewSetObjID = v.SpViewSetObjID";
        if (debugUpdate) System.err.println(sql);
        ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            sql = "DELETE FROM spappresourcedata WHERE SpAppResourceDataID "+id;
            if (debugUpdate) System.err.println(sql);
            stmt.executeUpdate(sql);
        }
        sql = "DELETE FROM spviewsetobj WHERE SpAppResourceDirID = "+appResDirId;
        if (debugUpdate) System.err.println(sql);
        stmt.executeUpdate(sql);
        
        sql = "DELETE FROM spappresourcedir WHERE SpAppResourceDirID = "+appResDirId;
        if (debugUpdate) System.err.println(sql);
        stmt.executeUpdate(sql);
        
        stmt.close();
    }
    
    /**
     * @param wbId
     * @throws SQLException
     */
    protected void deleteWorkBench(final int wbId) throws SQLException
    {
        fireMsg(STR2PARM, delMsgStr, tblMgr.getTitleForId(Workbench.getClassTableId()));

        Statement stmt = connection.createStatement();
        
        Vector<Integer> templateIds = getIds("SELECT workbench.WorkbenchTemplateID FROM workbench WHERE WorkbenchID = "+wbId, 0);
        
        String sql = "SELECT wdi.WorkbenchDataItemID FROM workbench wb INNER JOIN workbenchrow wbr ON wb.WorkbenchID = wbr.WorkbenchID " +
                     "INNER JOIN workbenchdataitem wdi ON wbr.WorkbenchRowID = wdi.WorkbenchRowID WHERE wb.WorkbenchID = "+wbId;
        Vector<Integer> ids = getIds(sql, 0);
        for (Integer id : ids)
        {
            sql = "DELETE FROM workbenchdataitem WHERE WorkbenchDataItemID = "+id;
            if (debugUpdate) System.err.println(sql);
            stmt.executeUpdate(sql);
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
     * Lists the tables and their record counts in alphabetical order.
     */
    public static void showTableCounts(final String fileName, final boolean filterEmpty)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new File(fileName));
            ArrayList<String> tblNames = new ArrayList<String>(DBTableIdMgr.getInstance().getTables().size());
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                tblNames.add(ti.getName());
            }
            Collections.sort(tblNames);
            
            int total    = 0;
            int tblCount = 0;
            for (String name : tblNames)
            {
                Integer count = BasicSQLUtils.getCount("SELECT count(*) FROM "+name);
                if (count != null && ( !filterEmpty || count > 0))
                {
                    int cnt = count == 0 ? 0 : count;
                    if (debug) System.out.println(String.format("%5d - %s", cnt, name));
                    pw.println(String.format("%5d - %s", cnt, name));
                    total += cnt;
                    tblCount++;
                }
            }
            if (debug) System.out.println(String.format("%5d - %s", total, "Total"));
            pw.println(String.format("%5d - %s", total, "Total"));
            
            if (debug) System.out.println(String.format("%5d - %s", tblCount, "Total Tables"));
            pw.println(String.format("%5d - %s", tblCount, "Total Tables"));
            pw.close();
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     *
     *
     */
    class SwingWorkerCompletionWaiter implements PropertyChangeListener
    {
        private JDialog dialog;

        public SwingWorkerCompletionWaiter(final JDialog dialog)
        {
            this.dialog = dialog;
        }

        public void propertyChange(PropertyChangeEvent event)
        {
            
            if (CNT.equals(event.getPropertyName())) 
            {
                int value = (Integer)event.getNewValue();
                if (value < 100)
                {
                    progressBar.setValue(value);
                } else
                {
                    progressBar.setValue(100);
                }
            } else if (MSG.equals(event.getPropertyName()))
            {
                titleLbl.setText((String)event.getNewValue());
                
            } else if ("state".equals(event.getPropertyName()) &&
                    SwingWorker.StateValue.DONE == event.getNewValue())
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    };
    
    
    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        DBTableIdMgr.getInstance().getByShortClassName(Accession.class.getSimpleName()); // Preload
        
        SpecifyDeleteHelper sdh = new SpecifyDeleteHelper();
        try
        {
            sdh.delRecordFromTable(Division.class, 32768, false);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        //s.getSubTables(Discipline.class);
        //s.getSubTables(Division.class);
    }*/

    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DBTableIdMgr.getInstance().getByShortClassName(CollectingEvent.class.getSimpleName()); // Preload
        
        SpecifyDeleteHelper sdh = new SpecifyDeleteHelper();
        try
        {
//        	if (sdh.isRecordShared(CollectingEvent.class, 1016))
//        	{
//        		System.out.println("CollectingEvent 1016 is shared");
//        	} else
//        	{
//        		System.out.println("CollectingEvent 1016 is NOT shared");
//        	}
//        	if (sdh.isRecordShared(Locality.class, 132))
//        	{
//        		System.out.println("Locality 132 is shared");
//        	} else
//        	{
//        		System.out.println("Locality 132 is NOT shared");
//        	}
        	if (sdh.isRecordShared(Geography.class, 54907))
        	{
        		System.out.println("Geography 54907 is shared");
        	} else
        	{
        		System.out.println("Geography 54907 is NOT shared");
        	}
        		
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        
    }

}
