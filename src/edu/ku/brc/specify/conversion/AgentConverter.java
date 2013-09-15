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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.buildSelectFieldList;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldNamesFromSchema;
import static edu.ku.brc.ui.UIRegistry.showError;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.AddressOfRecord;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 11, 2009
 *
 */
public class AgentConverter
{
    
    /*private enum ParseAgentType {LastNameOnlyLF,  // Last Name Field only with names in Last Name then First Name order
                                 LastNameOnlyFL,  // Last Name Field only with names in First Name then Last Name order
                                 LastThenFirstLF, // The first Last Name is in the First Name Field the rest of the data is in the Last Name field and the order is Last Name, Comma first Name
    }*/
    protected static final Logger                           log                    = Logger.getLogger(AgentConverter.class);

    protected IdMapperMgr                                   idMapperMgr = null;

    protected Connection                                    oldDBConn;
    protected Connection                                    newDBConn;
    protected static boolean                                shouldCreateMapTables  = true;
    protected GenericDBConversion                           conv;
    
    protected Statement                                     gStmt;
    protected Statement                                     updateStmtNewDB;
    
    protected HashMap<Integer, AgentInfo>                   agentHash  = new HashMap<Integer, AgentInfo>();
    protected Pair<String, String>                          namePair   = new Pair<String, String>();
    protected Triple<String, String, String>                nameTriple = new Triple<String, String, String>();

    protected TableWriter                                   tblWriter;
    
    // For Name Parsing
    protected List<AgentNameInfo>                           names    = new Vector<AgentNameInfo>();
    protected Stack<AgentNameInfo>                          recycler = new Stack<AgentNameInfo>();

    /**
     * @param conv
     * @param idMapperMgr
     * @param shouldCreateMapTables
     * @throws SQLException 
     */
    public AgentConverter(final GenericDBConversion conv,
                          final IdMapperMgr idMapperMgr,
                          final boolean shouldCreateMapTables) throws SQLException
    {
        super();
        this.conv        = conv;
        this.idMapperMgr = idMapperMgr;
        this.oldDBConn   = conv.getOldDBConn();
        this.newDBConn   = conv.getNewDBConn();
        
        this.gStmt           = oldDBConn.createStatement();
        this.updateStmtNewDB = newDBConn.createStatement();
        
        tblWriter = conv.getConvLogger().getWriter("AgentConv.html", "Agents");
    }

    public AgentConverter()
    {
        
    }
    
    /**
     * @param addressHash
     */
    private void dumpInfo(final String fileName, final HashMap<Integer, AddressInfo> addressHash)
    {
        try
        {
            File file = new File(fileName);
            PrintWriter pw = new PrintWriter(file);
            pw.println("------- AgentInfo Dump-------- ");
            for (AgentInfo agentInfo : agentHash.values())
            {
                pw.println(agentInfo.toString());
                for (Integer agtAdrId : agentInfo.getAddrs().keySet())
                {
                    pw.println("    AgentAddrId: "+agtAdrId + "    AddrId: "+agentInfo.getAddrs().get(agtAdrId));
                }
            }
            
            pw.println("\n------- AddressInfo Dump-------- ");
            for (AddressInfo addrInfo : addressHash.values())
            {
                pw.println(addrInfo.toString());
            }
            pw.println("\n");
            pw.close();
         
            //System.out.println(FileUtils.readFileToString(file));
            
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    /**
     * Specify 5.x points at AgentAdress instead of an Agent. The idea was that to point at an Agent
     * and possibly a differnt address that represents what that person does. This was really
     * confusing so we are changing it to point at an Agent instead.
     * 
     * So that means we need to pull apart these relationships and have all foreign keys that point
     * to an AgentAddress now point at an Agent and we then need to add in the Agents and then add
     * the Address to the Agents.
     * 
     * The AgentAdress, Agent and Address (triple) can have a NULL Address but it cannot have a NULL
     * Agent. If there is a NULL Agent then this method will throw a RuntimeException.
     */
    public boolean convertAgents(final boolean doFixAgents)
    {
        boolean debugAgents = false;

        log.debug("convert Agents");

        BasicSQLUtils.removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);

        // Create the mappers here, but fill them in during the AgentAddress Process
        IdTableMapper agentIDMapper     = idMapperMgr.addTableMapper("agent",        "AgentID");
        IdTableMapper agentAddrIDMapper = idMapperMgr.addTableMapper("agentaddress", "AgentAddressID");

        agentIDMapper.setInitialIndex(4);

        if (shouldCreateMapTables)
        {
            log.info("Mapping Agent Ids");
            agentIDMapper.mapAllIds("SELECT AgentID FROM agent ORDER BY AgentID");
        }
        
        // Just like in the conversion of the CollectionObjects we
        // need to build up our own SELECT clause because the MetaData of columns names returned
        // FROM
        // a query doesn't include the table names for all columns, this is far more predictable
        List<String> oldFieldNames = new ArrayList<String>();

        StringBuilder agtAdrSQL = new StringBuilder("SELECT ");
        List<String> agentAddrFieldNames = getFieldNamesFromSchema(oldDBConn, "agentaddress");
        agtAdrSQL.append(buildSelectFieldList(agentAddrFieldNames, "agentaddress"));
        agtAdrSQL.append(", ");
        GenericDBConversion.addNamesWithTableName(oldFieldNames, agentAddrFieldNames, "agentaddress");

        List<String> agentFieldNames = getFieldNamesFromSchema(oldDBConn, "agent");
        agtAdrSQL.append(buildSelectFieldList(agentFieldNames, "agent"));
        log.debug("MAIN: "+agtAdrSQL);
        agtAdrSQL.append(", ");
        GenericDBConversion.addNamesWithTableName(oldFieldNames, agentFieldNames, "agent");

        List<String> addrFieldNames = getFieldNamesFromSchema(oldDBConn, "address");
        log.debug(agtAdrSQL);
        agtAdrSQL.append(buildSelectFieldList(addrFieldNames, "address"));
        GenericDBConversion.addNamesWithTableName(oldFieldNames, addrFieldNames, "address");

        // Create a Map FROM the full table/fieldname to the index in the resultset (start at 1 not zero)
        HashMap<String, Integer> indexFromNameMap = new HashMap<String, Integer>();

        agtAdrSQL.append(" FROM agent INNER JOIN agentaddress ON agentaddress.AgentID = agent.AgentID INNER JOIN address ON agentaddress.AddressID = address.AddressID Order By agentaddress.AgentAddressID Asc");

        // These represent the New columns of Agent Table
        // So the order of the names are for the new table
        // the names reference the old table
        String[] agentColumns = { "agent.AgentID", "agent.TimestampModified", "agent.AgentType",
                                  "agentaddress.JobTitle", "agent.FirstName", "agent.LastName",
                                  "agent.MiddleInitial", "agent.Title", "agent.Interests", "agent.Abbreviation",
                                  "agentaddress.Email", "agentaddress.URL", "agent.Remarks",
                                  "agent.TimestampCreated",// User/Security changes
                                  "agent.ParentOrganizationID" };

        HashMap<Integer, AddressInfo> addressHash = new HashMap<Integer, AddressInfo>();

        // Create a HashMap to track which IDs have been handled during the conversion process
        try
        {
            log.info("Hashing Address Ids");

            Integer agentCnt = BasicSQLUtils.getCount(oldDBConn, "SELECT COUNT(AddressID) FROM address ORDER BY AddressID");
            
            // So first we hash each AddressID and the value is set to 0 (false)
            Statement stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rsX   = stmtX.executeQuery("SELECT AgentAddressID, AddressID FROM agentaddress ORDER BY AgentAddressID");

            conv.setProcess(0, agentCnt);
            
            int cnt = 0;
            // Needed to add in case AgentAddress table wasn't used.
            while (rsX.next())
            {
                int agentAddrId = rsX.getInt(1);
                int addrId      = rsX.getInt(2);
                addressHash.put(addrId, new AddressInfo(agentAddrId, addrId));

                if (cnt % 100 == 0)
                {
                    conv.setProcess(0, cnt);
                }
                cnt++;
            }
            rsX.close();
            stmtX.close();

            conv.setProcess(0, 0);

            // Next we hash all the Agents and set their values to 0 (false)
            log.info("Hashing Agent Ids");
            stmtX    = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            agentCnt = BasicSQLUtils.getCount(oldDBConn, "SELECT COUNT(*) FROM agent ORDER BY AgentID");
            rsX      = stmtX.executeQuery("SELECT AgentID, AgentType, LastName, Name, FirstName FROM agent ORDER BY AgentID");

            conv.setProcess(0, agentCnt);
            
            cnt = 0;
            while (rsX.next())
            {
                int agentId = rsX.getInt(1);
                agentHash.put(agentId, new AgentInfo(agentId, agentIDMapper.get(agentId), rsX.getByte(2), rsX.getString(3), rsX.getString(4), rsX.getString(5)));
                if (cnt % 100 == 0)
                {
                    conv.setProcess(0, cnt);
                }
                cnt++;
            }

            rsX.close();
            stmtX.close();

            conv.setProcess(0, 0);

            // Now we map all the Agents to their Addresses AND
            // All the Addresses to their Agents.
            //
            // NOTE: A single Address Record May be used by more than one Agent so
            // we will need to Duplicate the Address records later
            //
            log.info("Cross Mapping Agents and Addresses");

            String post = " FROM agentaddress WHERE AddressID IS NOT NULL and AgentID IS NOT NULL";
            agentCnt = BasicSQLUtils.getCount(oldDBConn, "SELECT COUNT(AgentAddressID)" + post);
            
            stmtX    = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            String asSQL = "SELECT AgentAddressID, AgentID" + post;
            log.debug(asSQL);
            rsX = stmtX.executeQuery(asSQL);

            conv.setProcess(0, agentCnt);
            cnt = 0;
            // Needed to add in case AgentAddress table wasn't used.
            while (rsX.next())
            {
                int agentAddrId = rsX.getInt(1);
                int agentId     = rsX.getInt(2);

                // ///////////////////////
                // Add Address to Agent
                // ///////////////////////
                AgentInfo agentInfo = agentHash.get(agentId);
                if (agentInfo == null) 
                { 
                    String msg = "The AgentID [" + agentId + "] in AgentAddress table id[" + agentAddrId + "] desn't exist";
                    log.error(msg);
                    tblWriter.logError(msg);
                } else
                {
                    agentInfo.add(agentAddrId, agentAddrId);    
                }

                if (cnt % 100 == 0)
                {
                    conv.setProcess(0, cnt);
                }
                cnt++;
            }
            rsX.close();
            stmtX.close();
            
            //dumpInfo("beforeInfo.txt", addressHash);

            conv.setProcess(0, 0);

            // It OK if the address is NULL, but the Agent CANNOT be NULL
            log.info("Checking for null Agents");

            agentCnt = BasicSQLUtils.getCount(oldDBConn, "SELECT COUNT(AgentAddressID) FROM agentaddress a where AddressID IS NOT NULL and AgentID is null");
            // If there is a Single Record With a NULL Agent this would be BAD!
            if (agentCnt != null && agentCnt > 0)
            {
                showError("There are "+agentCnt+" AgentAddress Records where the AgentID is null and the AddressId IS NOT NULL!");
            }

            // ////////////////////////////////////////////////////////////////////////////////
            // This does the part of AgentAddress where it has both an Address AND an Agent
            // ////////////////////////////////////////////////////////////////////////////////

            log.info(agtAdrSQL.toString());

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            log.debug("AgentAddress: "+agtAdrSQL.toString());
            
            
            // Create Map of column name to column index number
            int inx = 1;
            for (String fldName : oldFieldNames)
            {
                // log.info("["+fldName+"] "+inx+" ["+rsmd.getColumnName(inx)+"]");
                indexFromNameMap.put(fldName, inx++);
            }

            Statement updateStatement  = newDBConn.createStatement();
            

            // Figure out certain column indexes we will need alter
            int agentIdInx   = indexFromNameMap.get("agent.AgentID");
            int agentTypeInx = indexFromNameMap.get("agent.AgentType");
            int lastEditInx  = indexFromNameMap.get("agent.LastEditedBy");
            int nameInx      = indexFromNameMap.get("agent.Name");
            int lastNameInx  = indexFromNameMap.get("agent.LastName");
            int firstNameInx = indexFromNameMap.get("agent.FirstName");
            
            int recordCnt = 0;
            ResultSet rs = stmt.executeQuery(agtAdrSQL.toString());
            while (rs.next())
            {
                int agentAddressId  = rs.getInt(1);
                int agentId         = rs.getInt(agentIdInx);
                String lastEditedBy = rs.getString(lastEditInx);
                
                AgentInfo agentInfo = agentHash.get(agentId);
                
                // Deal with Agent FirstName, LastName and Name]
                String lastName = rs.getString(lastNameInx);
                String name     = rs.getString(nameInx);
                
                namePair.second  = StringUtils.isNotEmpty(name) && StringUtils.isEmpty(lastName) ? name : lastName;
                namePair.first   = rs.getString(firstNameInx);

                // Now tell the AgentAddress Mapper the New ID to the Old AgentAddressID
                if (shouldCreateMapTables)
                {
                    agentAddrIDMapper.setShowLogErrors(false);
                    if (debugAgents)log.info(String.format("Map - agentAddressId (Old) %d  to Agent -> New ID: %d", agentAddressId, agentInfo.getNewAgentId()));
                    
                    if (agentAddrIDMapper.get(agentAddressId) == null)
                    {
                        agentAddrIDMapper.put(agentAddressId, agentInfo.getNewAgentId());
                    } else
                    {
                        log.debug(String.format("ERROR - agentAddressId %d  Already mapped to  New ID:  %d", agentAddressId, agentInfo.getNewAgentId()));
                    }
                    agentAddrIDMapper.setShowLogErrors(true);
                }

                // Because of the old DB relationships we want to make sure we only add each agent
                // in one time
                // So start by checking the HashMap to see if it has already been added
                if (!agentInfo.wasAdded())
                {
                    agentInfo.setWasAdded(true);
                    //agentInfo.addWrittenAddrOldId(addrInfo.getOldAddrId());
                    
                    BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
                    
                    // It has not been added yet so Add it
                    StringBuilder sqlStr = new StringBuilder();
                    sqlStr.append("INSERT INTO agent ");
                    sqlStr.append("(AgentID, DivisionId, TimestampModified, AgentType, JobTitle, FirstName, LastName, MiddleInitial, ");
                    sqlStr.append("Title, Interests, Abbreviation, Email, URL, Remarks, TimestampCreated, ");
                    sqlStr.append("ParentOrganizationID, CreatedByAgentID, ModifiedByAgentID, Version)");
                    sqlStr.append(" VALUES (");

                    for (int i = 0; i < agentColumns.length; i++)
                    {
                        if (i > 0)
                        {
                            sqlStr.append(",");
                        }

                        if (i == 0)
                        {
                            if (debugAgents)log.info("Adding: "+agentColumns[i]+"  New ID: "+agentInfo.getNewAgentId());
                            sqlStr.append(agentInfo.getNewAgentId());
                            sqlStr.append(",");
                            sqlStr.append(conv.getCurDivisionID());

                        } else if (agentColumns[i].equals("agent.ParentOrganizationID"))
                        {
                            Object obj = rs.getObject(indexFromNameMap.get(agentColumns[i]));
                            if (obj != null)
                            {
                                int oldId = rs.getInt(agentColumns[i]);
                                Integer newID = agentIDMapper.get(oldId);
                                if (newID == null)
                                {
                                    log.error("Couldn't map ParentOrganizationID [" + oldId + "]");
                                }
                                sqlStr.append(BasicSQLUtils.getStrValue(newID));

                            } else
                            {
                                sqlStr.append("NULL");
                            }

                        } else if (agentColumns[i].equals("agent.LastName") || agentColumns[i].equals("LastName"))
                        {
                            
                            int    lastNameLen = 120;
                            String lstName    = namePair.second;
                            lstName = lstName == null ? null : lstName.length() <= lastNameLen ? lstName : lstName.substring(0, lastNameLen);
                            sqlStr.append(BasicSQLUtils.getStrValue(lstName));

                        } else if (agentColumns[i].equals("agent.FirstName") || agentColumns[i].equals("FirstName"))
                        {
                            sqlStr.append(BasicSQLUtils.getStrValue(namePair.first));

                        } else
                        {
                            inx = indexFromNameMap.get(agentColumns[i]);
                            sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
                    sqlStr.append("," + conv.getCreatorAgentIdForAgent(lastEditedBy) + "," + conv.getModifiedByAgentIdForAgent(lastEditedBy) + ",0");
                    sqlStr.append(")");
                    

                    try
                    {
                        if (debugAgents)
                        {
                            log.info(sqlStr.toString());
                        }
                        updateStatement.executeUpdate(sqlStr.toString());
                        
                        Integer newAgentId = BasicSQLUtils.getInsertedId(updateStatement);
                        if (newAgentId == null)
                        {
                            throw new RuntimeException("Couldn't get the Agent's inserted ID");
                        }
                        
                        //conv.addAgentDisciplineJoin(newAgentId, conv.getDisciplineId());

                    } catch (SQLException e)
                    {
                        log.error(sqlStr.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        System.exit(0);
                        throw new RuntimeException(e);
                    }

                }
                
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
                
                if (recordCnt % 250 == 0)
                {
                    log.info("AgentAddress Records: " + recordCnt);
                }
                recordCnt++;
            } // while
            
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "address", BasicSQLUtils.myDestinationServerType);
            
            log.info("AgentAddress Records: " + recordCnt);
            rs.close();
            stmt.close();

            // ////////////////////////////////////////////////////////////////////////////////
            // This does the part of AgentAddress where it has JUST Agent
            // ////////////////////////////////////////////////////////////////////////////////
            log.info("******** Doing AgentAddress JUST Agent");

            int newRecordsAdded = 0;

            StringBuilder justAgentSQL = new StringBuilder();
            justAgentSQL.setLength(0);
            justAgentSQL.append("SELECT ");
            justAgentSQL.append(buildSelectFieldList(agentAddrFieldNames, "agentaddress"));
            justAgentSQL.append(", ");

            getFieldNamesFromSchema(oldDBConn, "agent", agentFieldNames);
            justAgentSQL.append(buildSelectFieldList(agentFieldNames, "agent"));

            justAgentSQL.append(" FROM agent INNER JOIN agentaddress ON agentaddress.AgentID = agent.AgentID WHERE agentaddress.AddressID IS NULL ORDER BY agentaddress.AgentAddressID ASC");

            log.info(justAgentSQL.toString());

            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(justAgentSQL.toString());

            oldFieldNames.clear();
            GenericDBConversion.addNamesWithTableName(oldFieldNames, agentAddrFieldNames, "agentaddress");
            GenericDBConversion.addNamesWithTableName(oldFieldNames, agentFieldNames,     "agent");

            indexFromNameMap.clear();
            inx = 1;
            for (String fldName : oldFieldNames)
            {
                indexFromNameMap.put(fldName, inx++);
            }

            agentIdInx   = indexFromNameMap.get("agent.AgentID");
            lastEditInx  = indexFromNameMap.get("agent.LastEditedBy");
            agentTypeInx = indexFromNameMap.get("agent.AgentType");
            
            recordCnt = 0;
            while (rs.next())
            {
                byte agentType      = rs.getByte(agentTypeInx);
                int agentAddressId  = rs.getInt(1);
                int agentId         = rs.getInt(agentIdInx);
                String lastEditedBy = rs.getString(lastEditInx);

                AgentInfo agentInfo = agentHash.get(agentId);

                // Now tell the AgentAddress Mapper the New ID to the Old AgentAddressID
                if (shouldCreateMapTables)
                {
                    agentAddrIDMapper.put(agentAddressId, agentInfo.getNewAgentId());
                }

                recordCnt++;

                if (!agentInfo.wasAdded())
                {
                    agentInfo.setWasAdded(true);
                    BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
                    
                    // Create Agent
                    StringBuilder sqlStr = new StringBuilder("INSERT INTO agent ");
                    sqlStr.append("(AgentID, DivisionID, TimestampModified, AgentType, JobTitle, FirstName, LastName, MiddleInitial, Title, Interests, ");
                    sqlStr.append("Abbreviation, Email, URL, Remarks, TimestampCreated, ParentOrganizationID, ");
                    sqlStr.append("CreatedByAgentID, ModifiedByAgentID, Version)");
                    sqlStr.append(" VALUES (");
                    for (int i = 0; i < agentColumns.length; i++)
                    {
                        if (i > 0) sqlStr.append(",");
                        
                        if (i == 0)
                        {
                            if (debugAgents) log.info(agentColumns[i]);
                            sqlStr.append(agentInfo.getNewAgentId());
                            sqlStr.append(",");
                            sqlStr.append(conv.getCurDivisionID());

                        } else if (i == lastEditInx)
                        {
                            // Skip the field

                        } else if (agentColumns[i].equals("agent.LastName"))
                        {
                            if (debugAgents) log.info(agentColumns[i]);
                            int    srcColInx = agentType != 1 ? nameInx : lastNameInx;
                            String lName     = BasicSQLUtils.getStrValue(rs.getObject(srcColInx));
                            sqlStr.append(lName);

                        } else
                        {
                            if (debugAgents) log.info(agentColumns[i]);
                            inx = indexFromNameMap.get(agentColumns[i]);
                            sqlStr.append(BasicSQLUtils.getStrValue(rs.getObject(inx)));
                        }
                    }
                    sqlStr.append("," + conv.getCreatorAgentIdForAgent(lastEditedBy) + "," + conv.getModifiedByAgentIdForAgent(lastEditedBy) + ", 0"); // '0' is Version
                    sqlStr.append(")");

                    try
                    {
                        if (debugAgents)
                        {
                            log.info(sqlStr.toString());
                        }
                        updateStatement.executeUpdate(sqlStr.toString());
                        
                        Integer newAgentId = BasicSQLUtils.getInsertedId(updateStatement);
                        if (newAgentId == null)
                        {
                            throw new RuntimeException("Couldn't get the Agent's inserted ID");
                        }
                        
                        newRecordsAdded++;

                    } catch (SQLException e)
                    {
                        log.error(sqlStr.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        throw new RuntimeException(e);
                    }

                }

                if (recordCnt % 250 == 0)
                {
                    log.info("AgentAddress (Agent Only) Records: " + recordCnt);
                }
            } // while
            log.info("AgentAddress (Agent Only) Records: " + recordCnt + "  newRecordsAdded " + newRecordsAdded);

            rs.close();
            
            updateStatement.close();

            conv.setProcess(0, BasicSQLUtils.getNumRecords(oldDBConn, "agent"));
            conv.setDesc("Adding Agents");

            // Now Copy all the Agents that where part of an Agent Address Conversions
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery("SELECT AgentID FROM agent");
            recordCnt = 0;
            while (rs.next())
            {
                Integer agentId = rs.getInt(1);
                AgentInfo agentInfo = agentHash.get(agentId);
                if (agentInfo == null || !agentInfo.wasAdded())
                {
                    copyAgentFromOldToNew(agentId, agentIDMapper);
                }
                recordCnt++;
                if (recordCnt % 50 == 0)
                {
                    conv.setProcess(recordCnt);
                }
            }
            
            conv.setProcess(recordCnt);
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
            
            //------------------------------------------------------------
            // Now Copy all the Agents that where missed
            //------------------------------------------------------------
            conv.setProcess(0);
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs   = stmt.executeQuery("SELECT AgentID FROM agent");
            recordCnt = 0;
            while (rs.next())
            {
                Integer   agentId = rs.getInt(1);
                Integer   newId   = agentIDMapper.get(agentId);
                if (newId != null)
                {
                    Integer isThere = BasicSQLUtils.getCount(newDBConn, "SELECT COUNT(*) FROM agent WHERE AgentID = "+ newId);
                    if (isThere == null || isThere == 0)
                    {
                        copyAgentFromOldToNew(agentId, agentIDMapper);
                    }
                } else
                {
                    tblWriter.logError("Mapping missing for old Agent id["+agentId+"]");
                }
                recordCnt++;
                if (recordCnt % 50 == 0)
                {
                    conv.setProcess(recordCnt);
                }
            }
            conv.setProcess(recordCnt);
            
            if (doFixAgents)
            {
                fixAgentsLFirstLastName();
            }
            
            //----------------------------------------------------------------------------------------------------------------------------------
            // Now loop through the Agents hash and write the addresses. If the address has already been written then it will need to be 
            // duplicate in the second step.
            //----------------------------------------------------------------------------------------------------------------------------------
            StringBuilder sqlStr1 = new StringBuilder("INSERT INTO address ");
            sqlStr1.append("(TimestampModified, Address, Address2, City, State, Country, PostalCode, Remarks, TimestampCreated, ");
            sqlStr1.append("IsPrimary, IsCurrent, Phone1, Phone2, Fax, RoomOrBuilding, PositionHeld, AgentID, CreatedByAgentID, ModifiedByAgentID, Version, Ordinal)");
            sqlStr1.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            
            PreparedStatement pStmt = newDBConn.prepareStatement(sqlStr1.toString());
            
            //                               1                2         3        4        5           6            7              8                9          10      11           12                13            14            15
            String addrSQL = "SELECT a.TimestampModified, a.Address, a.City, a.State, a.Country, a.Postalcode, a.Remarks, a.TimestampCreated, aa.Phone1, aa.Phone2, aa.Fax, aa.RoomOrBuilding , aa.IsCurrent, a.LastEditedBy, aa.JobTitle " +
                    		 "FROM address AS a " +
                             "INNER JOIN agentaddress AS aa ON a.AddressID = aa.AddressID WHERE aa.AgentAddressID = %d";
            
            BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "address", BasicSQLUtils.myDestinationServerType);
            
            int fixCnt = 0;
            for (AgentInfo agentInfo : agentHash.values())
            {
                HashMap<Integer, Integer> addrs = agentInfo.getAddrs();
                
                for (Integer oldAgentAddrId : addrs.keySet())
                {
                    String adrSQL = String.format(addrSQL, oldAgentAddrId);
                    rs = stmt.executeQuery(adrSQL);
                    if (!rs.next()) 
                    {
                        rs.close();
                        continue;
                    }
                    
                    String lastEditedBy = rs.getString(14);
                    String posHeld = rs.getString(15);
                    if (posHeld != null && posHeld.length() > 32)
                    {
                        posHeld = posHeld.substring(0, 32);
                    }
                    
                    String addr1 = rs.getString(2);
                    String addr2 = null;
                    if (addr1 != null && addr1.length() > 255)
                    {
                        addr1 = addr1.substring(0, 255);
                        addr2 = addr1.substring(255);
                    }
                    
                    pStmt.setTimestamp(1, rs.getTimestamp(1));
                    pStmt.setString(2,    addr1);
                    pStmt.setString(3,    addr2);                 // Address 2
                    pStmt.setString(4,    rs.getString(3));
                    pStmt.setString(5,    rs.getString(4));
                    pStmt.setString(6,    rs.getString(5));
                    pStmt.setString(7,    rs.getString(6));
                    pStmt.setString(8,    rs.getString(7));
                    pStmt.setTimestamp(9, rs.getTimestamp(8));
                    pStmt.setBoolean(10,  rs.getByte(13) != 0);
                    pStmt.setBoolean(11,  rs.getByte(13) != 0);
                    pStmt.setString(12,   rs.getString(9));
                    pStmt.setString(13,   rs.getString(10));
                    pStmt.setString(14,   rs.getString(11));
                    pStmt.setString(15,   rs.getString(12));
                    pStmt.setString(16,   posHeld);
                    pStmt.setInt(17,      agentInfo.getNewAgentId());
                    pStmt.setInt(18,      conv.getCreatorAgentIdForAgent(lastEditedBy));
                    pStmt.setInt(19,      conv.getModifiedByAgentIdForAgent(lastEditedBy));
                    pStmt.setInt(20,      0);
                    
                    pStmt.setInt(21, agentInfo.addrOrd);
                    
                    Integer newID = BasicSQLUtils.getInsertedId(pStmt);
                    log.debug(String.format("Saved New Id %d", newID));
                    
                    //agentInfo.addWrittenAddrOldId(addrInfo.getOldAddrId());
                    
                    agentInfo.addrOrd++;
                    
                    rs.close();

                    try
                    {
                        if (debugAgents)
                        {
                            log.info(sqlStr1.toString());
                        }
                        
                        if (pStmt.executeUpdate() != 1)
                        {
                            log.error("Error inserting address.)");
                        }
                        //addrInfo.setWasAdded(true);

                    } catch (SQLException e)
                    {
                        log.error(sqlStr1.toString());
                        log.error("Count: " + recordCnt);
                        e.printStackTrace();
                        log.error(e);
                        throw new RuntimeException(e);
                    }
                }
            }
            log.info(String.format("Added %d new Addresses", fixCnt));
            
            pStmt.close();
            
            //------------------------------------------------------------------
            // Step #2 - Now duplicate the addresses for the agents that had 
            // already been written to the database
            //------------------------------------------------------------------
            
            /*fixCnt = 0;
            for (AgentInfo agentInfo : agentHash.values())
            {
                for (Integer oldAgentAddrId : agentInfo.getUnwrittenOldAddrIds())
                {
                    Integer     oldAddrId = agentInfo.getAddrs().get(oldAgentAddrId);
                    //AddressInfo addrInfo  = addressHash.get(oldAddrId);
                    System.out.println(String.format("%d  %d", oldAgentAddrId, oldAddrId));
                    //duplicateAddress(newDBConn, addrInfo.getOldAddrId(), addrInfo.getNewAddrId(), agentInfo.getNewAgentId());
                }
            }
            log.info(String.format("Duplicated %d new Addresses", fixCnt));
            */
            
            //----------------------------------------------------------------------------------------------------------------------------------
            // Now loop through the Agents hash and write the addresses. If the address has already been written then it will need to be 
            // duplicate in the second step.
            //----------------------------------------------------------------------------------------------------------------------------------
            /*BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "address", BasicSQLUtils.myDestinationServerType);
            
            sqlStr1 = new StringBuilder("INSERT INTO address ");
            sqlStr1.append("(TimestampModified, Address, Address2, City, State, Country, PostalCode, Remarks, TimestampCreated, ");
            sqlStr1.append("IsPrimary, IsCurrent, Phone1, Phone2, Fax, RoomOrBuilding, AgentID, CreatedByAgentID, ModifiedByAgentID, Version, Ordinal)");
            sqlStr1.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            
            pStmt = newDBConn.prepareStatement(sqlStr1.toString());
            
            //                               1                2         3        4        5           6            7              8                9          10      11           12                13            14                 15
            String addrOnlySQL = "SELECT aa.TimestampModified, a.Address, a.City, a.State, a.Country, a.Postalcode, a.Remarks, aa.TimestampCreated, aa.Phone1, aa.Phone2, aa.Fax, aa.RoomOrBuilding , aa.IsCurrent, a.LastEditedBy, aa.AgentID " +
                                 "FROM agentaddress AS aa " +
                                 "LEFT JOIN address AS a ON a.AddressID = aa.AddressID " +
                                 "WHERE a.addressID IS NULL AND aa.AgentID IS NOT NULL";
            
            fixCnt = 0;
            rs = stmt.executeQuery(addrOnlySQL);
            while (rs.next())
            {
                int agentId    = rs.getInt(15);
                int newAgentId = agentIDMapper.get(agentId);
                
                String lastEditedBy = rs.getString(14);
                
                pStmt.setTimestamp(1, rs.getTimestamp(1));
                pStmt.setString(2,    rs.getString(2));
                pStmt.setString(3,    null);                 // Address 2
                pStmt.setString(4,    rs.getString(3));
                pStmt.setString(5,    rs.getString(4));
                pStmt.setString(6,    rs.getString(5));
                pStmt.setString(7,    rs.getString(6));
                pStmt.setString(8,    rs.getString(7));
                pStmt.setTimestamp(9, rs.getTimestamp(8));
                pStmt.setBoolean(10,  rs.getByte(13) != 0);
                pStmt.setBoolean(11,  rs.getByte(13) != 0);
                pStmt.setString(12,   rs.getString(9));
                pStmt.setString(13,   rs.getString(10));
                pStmt.setString(14,   rs.getString(11));
                pStmt.setString(15,   rs.getString(12));
                pStmt.setInt(16,      newAgentId);
                pStmt.setInt(17,      conv.getCreatorAgentIdForAgent(lastEditedBy));
                pStmt.setInt(18,      conv.getModifiedByAgentIdForAgent(lastEditedBy));
                pStmt.setInt(19,      0);
                pStmt.setInt(20,      1);

                try
                {
                    if (debugAgents)
                    {
                        log.info(sqlStr1.toString());
                    }
                    
                    if (pStmt.executeUpdate() != 1)
                    {
                        log.error("Error inserting address.)");
                    } else
                    {
                        fixCnt++;
                    }

                } catch (SQLException e)
                {
                    log.error(sqlStr1.toString());
                    log.error("Count: " + recordCnt);
                    e.printStackTrace();
                    log.error(e);
                    throw new RuntimeException(e);
                }
            }
            rs.close();
            log.info(String.format("Added %d new Addresses", fixCnt));

            pStmt.close();*/
            
            stmt.close();

            //dumpInfo("afterInfo.txt", addressHash);
            
            BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);
            
            return true;

        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
            System.exit(0);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @param name
     * @param np
     * @return
     */
    protected boolean parseName(final String name, final Triple<String, String, String> np)
    {
        np.first  = null;
        np.second = null;
        np.third  = null;
        
        String[] toks = StringUtils.split(name, ' ');
        switch (toks.length)
        {
            case 1 : 
                np.third = toks[0];
                break;
                
            case 2 : 
                np.first = toks[0];
                np.third = toks[1];
                break;
                
            case 3 : 
                np.first  = toks[0];
                np.second = toks[1];
                np.third  = toks[2];
                break;
                
            default:
                return false;
        }
        return true;
    }
    

   
    /**
     * @param newDBConnArg
     * @param oldId
     * @param newId
     * @throws SQLException 
     */
    protected void duplicateAddress(final Connection newDBConnArg,
                                    final Integer oldAddrId,
                                    final Integer newAddrId,
                                    final Integer newAgentId) throws SQLException
    {
        log.info(String.format("Duplicating oldAddrId[%d]    newAddrId[%d] to newAgentId[%d]", oldAddrId, newAddrId, newAgentId));

        String addFieldNames = DisciplineDuplicator.getFieldNameList(newDBConn, "address");
        String insertSQL = String.format("INSERT INTO address (%s) (SELECT %s FROM address WHERE AddressID = %d)", addFieldNames, addFieldNames, newAddrId);
        try
        {
            Statement updateStatement = newDBConnArg.createStatement();
            updateStatement.executeUpdate(insertSQL);
            
            int insertedAddrID = BasicSQLUtils.getInsertedId(updateStatement);
            String sql = String.format("UPDATE address SET AgentID=%d WHERE AddressID = %d", newAgentId, insertedAddrID);
            updateStatement.executeUpdate(sql);
            updateStatement.close();

        } catch (Exception ex)
        {
           ex.printStackTrace();
        }
    }
 
    /**
     * @param oldAgentId
     * @param agentIDMapper
     * @param tblWriter
     */
    protected void copyAgentFromOldToNew(final Integer oldAgentId, 
                                         final IdTableMapper agentIDMapper)
    {
        boolean doDebug = false;
        
        DBTableInfo agentTI        = DBTableIdMgr.getInstance().getByShortClassName("Agent");
        DBFieldInfo lastNameField  = agentTI.getFieldByColumnName("LastName");
        DBFieldInfo firstNameField = agentTI.getFieldByColumnName("FirstName");
        
        StringBuilder sql = new StringBuilder("SELECT ");
        if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
        {
            BasicSQLUtils.removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
        }
        BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);

        List<String> oldAgentFieldNames = getFieldNamesFromSchema(oldDBConn, "agent");
        
        String oldFieldListStr = buildSelectFieldList(oldAgentFieldNames, "agent");
        sql.append(oldFieldListStr);
        sql.append(" FROM agent WHERE AgentID = " + oldAgentId);

        //log.info(oldFieldListStr);

        List<String> newAgentFieldNames = getFieldNamesFromSchema(newDBConn, "agent");
        String newFieldListStr = buildSelectFieldList(newAgentFieldNames, "agent");

        //log.info(newFieldListStr);
        
        int lastNameLen = 120;

        HashMap<String, Integer> oldIndexFromNameMap = new HashMap<String, Integer>();
        int inx = 1;
        for (String fldName : oldAgentFieldNames)
        {
            oldIndexFromNameMap.put(fldName, inx++);
        }

        HashMap<String, Integer> newIndexFromNameMap = new HashMap<String, Integer>();
        inx = 1;
        for (String fldName : newAgentFieldNames)
        {
            newIndexFromNameMap.put(fldName, inx++);
        }

        try
        {
            // So first we hash each AddressID and the value is set to 0 (false)
            Statement stmtX = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rsX   = stmtX.executeQuery(sql.toString());
            
            int agentIDInx   = oldIndexFromNameMap.get("AgentID");
            int agentTypeInx = oldIndexFromNameMap.get("AgentType");
            int nameInx      = oldIndexFromNameMap.get("Name");
            int lastNameInx  = oldIndexFromNameMap.get("LastName");
            int firstNameInx = oldIndexFromNameMap.get("FirstName");

            // log.debug(sql.toString());

            int cnt = 0;
            while (rsX.next())
            {
                int agentId = rsX.getInt(1);

                StringBuilder sqlStr = new StringBuilder();
                sqlStr.append("INSERT INTO agent ");
                sqlStr.append("(" + newFieldListStr);
                sqlStr.append(")");
                sqlStr.append(" VALUES (");

                int fCnt = 0;
                for (String fieldName : newAgentFieldNames)
                {
                    if (fCnt > 0)
                        sqlStr.append(", ");

                    if (StringUtils.contains(fieldName.toLowerCase(), "disciplineid"))
                    {
                        sqlStr.append(conv.getDisciplineId());

                    } else if (StringUtils.contains(fieldName, "FirstName"))
                    {
                        String firstName = rsX.getString(firstNameInx);
                        if (firstName != null && firstName.length() > firstNameField.getLength())
                        {
                            String str = firstName.substring(0, firstNameField.getLength());
                            tblWriter.logError("Agent id: "+rsX.getString(agentIDInx)+" - Concatinating First Name FROM ["+firstName+"] to ["+str+"]");
                            firstName = str;
                        }
                        sqlStr.append(BasicSQLUtils.getStrValue(firstName));
                        
                    } else if (StringUtils.contains(fieldName, "LastName"))
                    {
                        int    oldType   = rsX.getInt(agentTypeInx);
                        int    srcColInx = oldType != 1 ? nameInx : lastNameInx;
                        String lName     = rsX.getString(srcColInx);
                        
                        if (lName == null && oldType != 1)
                        {
                            lName = rsX.getString(lastNameInx);
                        }
                        
                        if (lName != null && lName.length() > lastNameField.getLength())
                        {
                            String str = lName.substring(0, firstNameField.getLength());
                            tblWriter.logError("Agent id: "+rsX.getString(agentIDInx)+" - Concatinating Last Name FROM ["+lName+"] to ["+str+"]");
                            lName = str;
                        }
                        
                        String lstName = lName;
                        lName = lstName == null ? null : lstName.length() <= lastNameLen ? lstName : lstName.substring(0, lastNameLen);
                        
                        sqlStr.append(BasicSQLUtils.getStrValue(lName));
                        
                    } else
                    {
                        String value = "";
                        Integer index;
                        
                        if (fieldName.equals("ModifiedByAgentID"))
                        {
                            index = oldIndexFromNameMap.get("LastEditedBy");
                        } else 
                        {
                            index = oldIndexFromNameMap.get(fieldName);
                        }
                        
                        if (index == null)
                        {
                            // log.debug(fieldName);
                            value = "NULL";

                        } else if (fCnt == 0)
                        {
                            value = agentIDMapper.get(agentId).toString();

                        } else
                        {
                            value = BasicSQLUtils.getStrValue(rsX.getObject(index.intValue()));
                        }

                        BasicSQLUtilsMapValueIFace valueMapper = conv.getColumnValueMapper().get(fieldName);
                        if (valueMapper != null)
                        {
                            value = valueMapper.mapValue(value);
                        }
                        sqlStr.append(value);
                    }
                    fCnt++;
                }
                sqlStr.append(")");
                // log.info(sqlStr.toString());

                Statement updateStatement = newDBConn.createStatement();
                // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                if (doDebug)
                {
                    log.info(sqlStr.toString());
                }
                updateStatement.executeUpdate(sqlStr.toString());
                Integer newAgentId = BasicSQLUtils.getInsertedId(updateStatement);
                if (newAgentId == null)
                {
                    throw new RuntimeException("Couldn't get the Agent's inserted ID");
                }
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;
                
                //conv.addAgentDisciplineJoin(newAgentId, conv.getDisciplineId());

                cnt++;
                BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer(newDBConn, "agent", BasicSQLUtils.myDestinationServerType);

            }
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * 
     */
    protected void dropCollectorsTable()
    {
        try
        {
            gStmt.executeUpdate("DROP TABLE agent_collectors");
            
        } catch (Exception ex)
        {
            // Exception may occur if table doesn't exist
        }
    }
    
    /**
     * 
     */
    protected void createCollectorsTable()
    {
        try
        {
            dropCollectorsTable();

            String sql = "CREATE TABLE `agent_collectors` ("+
                                "`KeyID` int(11) NOT NULL, "+
                                "`OldID` int(11) NOT NULL, "+
                                "`Name` varchar(255) DEFAULT NULL, "+
                                "`Ids` varchar(255) DEFAULT NULL, "+
                                " PRIMARY KEY (`KeyID`), KEY `AgentNameIDX` (`Name`), ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
            log.info("sql query: " + sql);
            gStmt.executeUpdate(sql);

        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * @param name
     * @param ids
     */
    protected void addAgentCollectors(final String name, final int oldId, final int[] ids)
    {
        try
        {
            StringBuilder sb = new StringBuilder("INSERT INTO agent_collectors (Name, OldID, Ids) VALUES('"+name+"', "+oldId+",'");
            int i = 0;
            for (int id : ids)
            {
                if (i > 0) sb.append(",");
                sb.append(id);
                i++;
            }
            sb.append("')");
            
            gStmt.executeUpdate(sb.toString());

        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * @param name
     * @return array of new Ids for the new agents, but the first id is the original old Id
     */
    protected int[] getAgentCollectors(final String name)
    {
        try
        {
            String sql = "SELECT OldID, Ids FROM agent_collectors WHERE Name = '"+name+"'";
            ResultSet rs = gStmt.executeQuery(sql);
            if (rs.next())
            {
                int      oldId  = rs.getInt(1);
                String   idsStr = rs.getString(2);
                String[] toks   = StringUtils.split(idsStr, ",");
                int[]    ids    = new int[toks.length+1];
                
                ids[0] = oldId;
                for (int i=1;i<toks.length+1;i++)
                {
                    ids[i] = Integer.valueOf(toks[i]);
                }
                return ids;
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     */
    public void fixAgentsLFirstLastName()
    {
        try
        {
            Integer numAgents    = BasicSQLUtils.getCount("SELECT COUNT(*) FROM agent WHERE AgentType = " + Agent.PERSON);
            Integer numBadAgents = BasicSQLUtils.getCount("SELECT COUNT(*) FROM agent WHERE FirstName is NULL AND LastName IS NOT NULL AND NOT (LastName LIKE '%&%') AND AgentType = " + Agent.PERSON);
            
            double percent = ((double)numBadAgents / (double)numAgents);
            if (percent > 0.25)
            {
                log.info("Bad Agents > 25% => " + percent);
            }
            
            tblWriter.log("<H4>Person Names not Fixed</H4>");
            tblWriter.startTable();
            tblWriter.logHdr("New Agent ID", "Old Last Name");
            String sql = "SELECT AgentID, LastName FROM agent WHERE FirstName is NULL AND LastName IS NOT NULL AND (LastName LIKE '%&%') AND AgentType = " + Agent.PERSON;
            ResultSet rs = gStmt.executeQuery(sql);
            while (rs.next())
            {
                Integer  id        = rs.getInt(1);
                String   lastName  = rs.getString(2);
                tblWriter.log(id.toString(), lastName);
            }
            rs.close();
            tblWriter.endTable();
            
            tblWriter.log("<BR><H4>Person Names not Fixed</H4>");
            tblWriter.startTable();
            tblWriter.logHdr("Old Last Name", "New First Name", "New Last Name");
            StringBuilder sb = new StringBuilder();
            sql = "SELECT AgentID, LastName FROM agent WHERE FirstName is NULL AND LastName IS NOT NULL AND NOT (LastName LIKE '%&%') AND AgentType = " + Agent.PERSON;
            rs  = gStmt.executeQuery(sql);
            while (rs.next())
            {
                int      id        = rs.getInt(1);
                String   lastName  = rs.getString(2);
                String[] toks   = StringUtils.split(lastName, " ");
                
                if (toks.length > 1)
                {
                    String firstName = toks[0];
                    sb.setLength(0);
                    for (int i=1;i<toks.length;i++)
                    {
                        sb.append(toks[i]);
                        if (i < toks.length-1) sb.append(' ');
                    }
                    
                    tblWriter.log(lastName, firstName, sb.toString());
                    sql = "UPDATE agent SET FirstName=\"" + firstName + "\", LastName=\"" + sb.toString() + "\" WHERE AgentID = " + id;
                    log.debug(sql);
                    updateStmtNewDB.executeUpdate(sql);
                }
                
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
        tblWriter.endTable();
    }
    
    /**
     * @param agentInfo
     * @param namePair
     */
    @SuppressWarnings("unchecked")
    protected void fixupForCollectors(final Division    divisionArg,
                                      final Discipline  disciplineArg)
    {
        //ParseAgentType parseType = ParseAgentType.LastNameOnlyLF;

        
        Session     session = HibernateUtil.getNewSession();
        Transaction trans   = null;
        try
        {
            Division   division       = (Division)session.createQuery("FROM Division WHERE id = " + divisionArg.getId()).list().iterator().next();
            Discipline discipline     = (Discipline)session.createQuery("FROM Discipline WHERE id = " + disciplineArg.getId()).list().iterator().next();
            Agent      createdByAgent = (Agent)session.createQuery("FROM Agent WHERE id = " + conv.getCreatorAgentIdForAgent(null)).list().iterator().next();
            
            tblWriter.log("<H4>Splitting Mutliple Collectors names into Multiple Agents</H4>");
            tblWriter.startTable();
            tblWriter.logHdr("New Agent ID", "Old Last Name", "Description");
            
            Vector<Integer> agentToDelete = new Vector<Integer>();
            
            conv.setProcess(0, agentHash.values().size());
            int cnt = 0;
            
            for (AgentInfo agentInfo : agentHash.values())
            {
                String lastNameText  = agentInfo.getAgentType() != Agent.PERSON ? agentInfo.getName() : agentInfo.getLastName();
                //String firstNameText = agentInfo.getFirstName();

                if ((StringUtils.contains(lastNameText, ",") || StringUtils.contains(lastNameText, ";")) && !StringUtils.contains(lastNameText, "'"))
                {
                    String    sql = "SELECT c.CollectorID, c.CollectingEventID FROM collector c INNER JOIN agent ON c.AgentID = agent.AgentID WHERE agent.AgentID = " + agentInfo.getNewAgentId();
                    ResultSet rs  = gStmt.executeQuery(sql);
                    if (rs.next())
                    {
                        int      highestOrder = 0;
                        Integer  colID        = rs.getInt(1);
                        Integer  ceId         = rs.getInt(2);
                        
                        tblWriter.log(ceId +" / " + colID +" / " + agentInfo.getNewAgentId().toString(), lastNameText, "&nbsp;");
                        
                        sql = "SELECT ce.CollectingEventID, c.CollectorID, c.OrderNumber, c.IsPrimary FROM collector c INNER JOIN collectingevent ce ON c.CollectingEventID = ce.CollectingEventID " + 
                              "WHERE c.CollectorID = " + colID + " ORDER BY c.OrderNumber DESC";
                        ResultSet rs2 = gStmt.executeQuery(sql);
                        if (rs2.next())
                        {
                            highestOrder = rs2.getInt(3);
                        }
                        rs.close();
                        
                        CollectingEvent ce        = (CollectingEvent)session.createQuery("FROM CollectingEvent WHERE id = " + ceId).list().get(0);
                        Agent           origAgent = (Agent)session.createQuery("FROM Agent WHERE id = " + agentInfo.getNewAgentId()).list().get(0);
                        
                        // Now process the multiple Collectors
                        String[] lastNames  = StringUtils.split(lastNameText, ",;");
                        //String[] firstNames = StringUtils.split(firstNameText, ",;");
                        for (int i=0;i<lastNames.length;i++)
                        {
                            if (parseName(lastNames[i], nameTriple))
                            {
                                String firstName = nameTriple.first;
                                String middle    = nameTriple.second;
                                String lastName  = nameTriple.third;
                                
                                List<Agent> agts          = (List<Agent>)session.createQuery("FROM Agent WHERE firstName = " + (firstName != null ? "'" + firstName + "'" : "NULL") 
                                                                         + " AND middleInitial = " + (middle != null ? "'"+middle+"'" : "NULL") 
                                                                         + " AND lastName = " + (lastName != null ? "'" + lastName + "'" : "NULL")).list();
                                Agent       existingAgent = agts != null && agts.size() > 0 ? agts.get(0) : null;
                                
                                trans = session.beginTransaction();
                                if (i == 0)
                                {
                                    if (existingAgent != null)
                                    {
                                        Collector collector = (Collector)session.createQuery("FROM Collector WHERE id = " + colID).list().get(0);
                                        collector.setAgent(existingAgent);
                                        existingAgent.getCollectors().add(collector);
                                        
                                        /*origAgent.getCollectors().clear();
                                        origAgent.getDisciplines().clear();
                                        
                                        for (Agent a : new ArrayList<Agent>(discipline.getAgents()))
                                        {
                                            if (a.getId().equals(origAgent.getId()))
                                            {
                                                discipline.getAgents().remove(a);
                                                break;
                                            }
                                        }
                                        for (Agent a : new ArrayList<Agent>(division.getMembers()))
                                        {
                                            if (a.getId().equals(origAgent.getId()))
                                            {
                                                division.getMembers().remove(a);
                                                break;
                                            }
                                        }
                                        origAgent.setDivision(null);
                                        origAgent.setCreatedByAgent(null);
                                        origAgent.setModifiedByAgent(null);
                                        
                                        //session.delete(origAgent);*/
                                        
                                        tblWriter.log(agentInfo.getNewAgentId().toString(), firstName+", "+lastName, "reusing collector,using existing agent");
                                        
                                        agentToDelete.add(origAgent.getId());
                                        
                                        session.saveOrUpdate(existingAgent);
                                        session.saveOrUpdate(collector);
                                        session.saveOrUpdate(division);
                                        session.saveOrUpdate(discipline);
                                        
                                    } else
                                    {
                                        origAgent.setFirstName(firstName);
                                        origAgent.setLastName(lastName);
                                        origAgent.setAgentType(Agent.PERSON);
                                        
                                        tblWriter.log(agentInfo.getNewAgentId().toString(), firstName+", "+lastName, "resetting agent names - reclaiming");
                                        session.saveOrUpdate(origAgent);
                                    }
                                    
                                } else
                                {
                                    highestOrder++;
                                    
                                    Agent agent;
                                    if (existingAgent == null)
                                    {
                                        agent = new Agent();
                                        agent.initialize();     
                                        agent.setAgentType(Agent.PERSON);
                                        agent.setCreatedByAgent(createdByAgent);
                                        agent.setDivision(division);
                                        agent.setFirstName(nameTriple.first);
                                        agent.setMiddleInitial(nameTriple.second);
                                        agent.setLastName(nameTriple.third);
                                        division.getMembers().add(agent);
                                        
                                        tblWriter.log(agentInfo.getNewAgentId().toString(), firstName+", "+lastName, "new agent, new collector");

                                        
                                    } else
                                    {
                                        agent = existingAgent;
                                        tblWriter.log(agentInfo.getNewAgentId().toString(), firstName+", "+lastName, "reusing, new collector");
                                    }
                                    
                                    Collector collector = new Collector();
                                    collector.initialize();
                                    collector.setCreatedByAgent(createdByAgent);
                                    collector.setOrderNumber(highestOrder);
                                    
                                    ce.getCollectors().add(collector);
                                    collector.setCollectingEvent(ce);
                                    collector.setAgent(agent);
                                    agent.getCollectors().add(collector);
                                    collector.setIsPrimary(false);
                                    
                                    session.saveOrUpdate(agent);
                                    session.saveOrUpdate(collector);
                                    session.saveOrUpdate(division);
                                    session.saveOrUpdate(discipline);
                                    
                                }
                                trans.commit();
                            }
                        }
                    }
                    rs.close();
                }
                conv.setProcess(++cnt);
            }
            tblWriter.endTable();
            
            Collections.sort(agentToDelete);
            
            tblWriter.log("<H4>Removing Original Agents</H4>");
            tblWriter.startTable();
            tblWriter.logHdr("New Agent ID", "LastName", "FirstName");
            for (Integer id : agentToDelete)
            {
                System.out.println("Deleting Agent["+id+"]");
                List<Object[]> rows = BasicSQLUtils.query("SELECT AgentID, LastName, MiddleInitial, FirstName FROM agent WHERE AgentID = " + id);
                Object[] row = rows.get(0);
                tblWriter.log(id.toString(), (row[1] == null ? "&nbsp;" : row[1].toString()) + (row[2] == null ? "" : " "+row[2].toString()), 
                                             row[3] == null ? "&nbsp;" : row[3].toString());
                
                updateStmtNewDB.executeUpdate("DELETE FROM agent WHERE AgentID = " + id);
                
            }
            tblWriter.endTable();
            
        } catch (Exception ex)
        {
            try
            {
                if (trans != null) trans.rollback();
            } catch (Exception ex1) {}
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            session.close();
        }
    }

    
    
    /**
     * 
     */
    public void fixAddressOfRecord()
    {
        String oldAcc = "SELECT ac.AccessionID, aa.AgentID, adr.Address, adr.City, adr.State, adr.Country, adr.Postalcode, adr.Remarks, adr.TimestampModified, adr.TimestampCreated " +
                        "FROM accession AS ac " +
                        "INNER JOIN accessionagents AS aca ON ac.AccessionID = aca.AccessionID " +
                        "INNER JOIN agentaddress AS aa ON aca.AgentAddressID = aa.AgentAddressID " +
                        "INNER JOIN address AS adr ON aa.AddressID = adr.AddressID " +
                        "ORDER BY ac.Number ASC";
        
        String oldCntAcc = "SELECT COUNT(*) " +
                        "FROM accession AS ac " +
                        "INNER JOIN accessionagents AS aca ON ac.AccessionID = aca.AccessionID " +
                        "INNER JOIN agentaddress AS aa ON aca.AgentAddressID = aa.AgentAddressID " +
                        "INNER JOIN address AS adr ON aa.AddressID = adr.AddressID " +
                        "ORDER BY ac.Number ASC";
        
        String oldLoan = "SELECT l.LoanID, aa.AgentID, adr.Address, adr.City, adr.State, adr.Country, adr.Postalcode, adr.Remarks, adr.TimestampModified, adr.TimestampCreated " +
                        "FROM loan AS l " +
                        "INNER JOIN loanagents AS la ON l.LoanID = la.LoanID " +
                        "INNER JOIN agentaddress AS aa ON la.AgentAddressID = aa.AgentAddressID " +
                        "INNER JOIN address AS adr ON aa.AddressID = adr.AddressID " +
                        "WHERE Category = 0 ORDER BY l.LoanNumber ASC";

        String oldCntLoan = "SELECT COUNT(*) FROM loan AS l " +
                            "INNER JOIN loanagents AS la ON l.LoanID = la.LoanID " +
                            "INNER JOIN agentaddress AS aa ON la.AgentAddressID = aa.AgentAddressID " +
                            "INNER JOIN address AS adr ON aa.AddressID = adr.AddressID " +
                            "WHERE Category = 0 ORDER BY l.LoanNumber ASC";
        
        String oldGift    = StringUtils.replace(oldLoan, "0", "1");
        String oldCntGift = StringUtils.replace(oldCntLoan, "0", "1");
        
        conv.setDesc("Fixing Accession Address Of Record");
        doAddressOfRecord(oldCntAcc, oldAcc, "Accession", "accession_AccessionID");
        
        conv.setDesc("Fixing Loan Address Of Record");
        doAddressOfRecord(oldCntLoan, oldLoan, "Loan", "loan_LoanID");
        
        conv.setDesc("Fixing Gift Address Of Record");
        doAddressOfRecord(oldCntGift, oldGift, "Gift", "loan_LoanID");

    }
    
    /**
     * @param cntSQL
     * @param sql
     * @param tableName
     * @param mapperName
     */
    private void doAddressOfRecord(final String cntSQL, 
                                   final String sql, 
                                   final String tableName, 
                                   final String mapperName)
    {
        IdMapperIFace agentMapper = IdMapperMgr.getInstance().get("agent_AgentID");
        
        Session     session = HibernateUtil.getNewSession();
        Transaction trans   = null;
        try
        { 
            conv.setProcess(0, BasicSQLUtils.getCountAsInt(oldDBConn, cntSQL));
        
            IdMapperIFace loanMapper  = IdMapperMgr.getInstance().get(mapperName);

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs   = stmt.executeQuery(sql);

            int cnt = 0;
            while (rs.next())
            {
                int col = 1;
                Integer loanID       = rs.getInt(col++);
                Integer agentID      = rs.getInt(col++); 
                String address       = rs.getString(col++); 
                String city          = rs.getString(col++); 
                String state         = rs.getString(col++); 
                String country       = rs.getString(col++);
                String postalCode    = rs.getString(col++); 
                String remarks       = rs.getString(col++); 
                Timestamp timestampModified = rs.getTimestamp(col++);
                Timestamp timestampCreated  = rs.getTimestamp(col++);
                
                Integer newAgentId = agentMapper.get(agentID);
                
                if (newAgentId != null)
                {
                    List<?> list = session.createQuery("FROM Agent WHERE id = " + newAgentId).list();
                    if (list != null && list.size() == 1)
                    {
                        Agent agent = (Agent)list.get(0);
                        
                        Integer newLoanId = loanMapper.get(loanID);
                        
                        if (newLoanId != null)
                        {
                            list = session.createQuery("FROM "+tableName+" WHERE id = " + newLoanId).list();
                            if (list != null && list.size() == 1)
                            {
                                trans = session.beginTransaction();
                                AddressOfRecord aor = new AddressOfRecord();
                                aor.initialize();
                                aor.setAddress(address);
                                aor.setAgent(agent);
                                aor.setCity(city);
                                aor.setCountry(country);
                                aor.setPostalCode(postalCode);
                                aor.setRemarks(remarks);
                                aor.setState(state);
                                aor.setTimestampCreated(timestampCreated);
                                aor.setTimestampModified(timestampModified);
                               
                                FormDataObjIFace parentObj = (FormDataObjIFace)list.get(0);
                                DataModelObjBase.setDataMember(parentObj, "addressOfRecord", aor);
                                
                                session.saveOrUpdate(parentObj);
                                //session.saveOrUpdate(aor);
                                trans.commit();
                            }
                        }
                    }
                }
                
                if (cnt % 100 == 0)
                {
                    conv.setProcess(0, cnt);
                }
                cnt++;
            }
            rs.close();
            stmt.close();
            
        } catch (Exception ex)
        {
            try
            {
                if (trans != null) trans.rollback();
            } catch (Exception ex1) {}
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            session.close();
        }
    }
    
    
    /**
     * 
     */
    public void fixMissingAddrsFromConv()
    {
        IdTableMapper agentIDMapper     = idMapperMgr.addTableMapper("agent",        "AgentID", false);
        IdTableMapper addrIDMapper      = idMapperMgr.addTableMapper("address",      "AddressID", false);

        String sql = "SELECT ag.AgentID, aa.AgentAddressID, ad.AddressID FROM agent ag INNER JOIN agentaddress aa ON ag.AgentID = aa.AgentID " +
                     "INNER JOIN address ad ON aa.AddressID = ad.AddressID ";
        
        sql = "SELECT AddressID FROM (SELECT aa.AddressID, COUNT(aa.AddressID) as cnt FROM agentaddress aa INNER JOIN address ON aa.AddressID = address.AddressID " +
              "GROUP BY aa.AddressID) T1 WHERE cnt > 1 ";
        
        int numFixed = 0;
        for (Integer oldAddrId : BasicSQLUtils.queryForInts(oldDBConn, sql))
        {
            
            sql = "SELECT a.AgentID FROM address ad INNER JOIN agentaddress aa ON ad.AddressID = aa.AddressID " +
                   "INNER JOIN agent a ON aa.AgentID = a.AgentID WHERE ad.AddressID = " + oldAddrId;
            
            Integer newAddrID  = addrIDMapper.get(oldAddrId);
            
            log.info("-------------------------------- fixMissingAddrsFromConv -------------------------------- ");
            for (Integer oldAgentId : BasicSQLUtils.queryForInts(oldDBConn, sql))
            {
                Integer newAgentID = agentIDMapper.get(oldAgentId);
                
                sql = "SELECT ad.AddressID FROM agent a LEFT JOIN address ad ON a.AgentID = ad.AgentID WHERE a.AgentID = " + newAgentID;
                Integer addrID = BasicSQLUtils.getCount(newDBConn, sql);
                if (addrID == null)
                {
                    //System.out.println("newAgentID: "+newAgentID+"  addrID: "+addrID + "  newAddrID: "+newAddrID);  
                    try
                    {
                        duplicateAddress(newDBConn, addrID, newAddrID, newAgentID);
                        numFixed++;
                        
                    } catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        System.out.println("---------------------------------------------");
        System.out.println("Number of Addresses Duplicated: "+numFixed);
        System.out.println("---------------------------------------------");
    }
    
    protected void parseAndFixMultiLineAddresses()
    {
        String whereStr = " FROM address a WHERE Address like '%\r\n%'";
        String sql = "SELECT COUNT(*)" + whereStr;
        if (BasicSQLUtils.getCountAsInt(sql) < 1)
        {
            return;
        }
        
        sql = "SELECT AddressID, Address" + whereStr;
        
        Statement         stmt  = null;
        //PreparedStatement pStmt = null; 
        try
        {
           // pStmt = newDBConn.prepareStatement("UPDATE address SET Address=?, Address2=?, City=?, State=?, PostalCode=? WHERE AddressID = ?");
            stmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);
            
            HashSet<Integer> hashSet = new HashSet<Integer>();
            while (rs.next())
            {
                String[] toks = StringUtils.split(rs.getString(2), "\r\n");
                hashSet.add(toks.length);
            }
            rs.close();
            
            for (Integer i : (Integer[])hashSet.toArray())
            {
                System.out.println(i);
            }
            System.out.println();
            
        } catch (Exception ex)
        {
            
        }
        
    }
    
    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    class AddressInfo
    {
        Integer                     oldAgentAddrId;
        Integer                     oldAddrId;
        boolean                     isUsed            = false;
        boolean                     wasAdded          = false;

        public AddressInfo(final Integer oldAgentAddrId, 
                           final Integer oldAddrId)
        {
            this.oldAgentAddrId = oldAgentAddrId;
            this.oldAddrId      = oldAddrId;
        }

        public Integer getOldAddrId()
        {
            return oldAddrId;
        }

        public boolean isUsed()
        {
            return isUsed;
        }

        public void setUsed(boolean isUsed)
        {
            this.isUsed = isUsed;
        }

        public boolean wasAdded()
        {
            return wasAdded;
        }

        public void setWasAdded(boolean wasAddedArg)
        {
            this.wasAdded = wasAddedArg;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            IdMapperIFace agentAddrIDMapper = idMapperMgr.get("agentaddress", "AgentAddressID");
            Integer newAddrId =  agentAddrIDMapper.get(oldAgentAddrId);
            return String.format("AddressInfo [oldAddrId=%d, newAddrId=%d,  isUsed=%s,  wasAdded=%s]", oldAddrId, newAddrId, isUsed ? "Y" : "N", wasAdded ? "Y" : "N");
        }
    }
    
    /**
     * @param firstName
     * @param lastName
     * @param innerSep
     * @param nameSep
     * @param isLastNameFieldFirst
     * @param isLastNameFirst
     * @param trimAfterComma
     * @return
     */
    public List<AgentNameInfo> parseName(final String    firstName, 
                                         final String    lastName,
                                         @SuppressWarnings("unused") final char      innerSep,
                                         final char      nameSep,
                                         final boolean   isLastNameFieldFirst,
                                         @SuppressWarnings("unused") final boolean   isLastNameFirst,
                                         final boolean   trimAfterComma)
    {
        recycler.addAll(names);
        names.clear();
        
        String   fullName = isLastNameFieldFirst ? lastName + (firstName != null ? " " + firstName : "") : (firstName != null ? (firstName + " ") : "") + lastName;
        String[] toks     = StringUtils.split(fullName, nameSep);
        
        for (String name : toks)
        {
            if (trimAfterComma)
            {
                int inx = name.indexOf(',');
                if (inx > -1)
                {
                    name = name.substring(0, inx);
                }
            }
            
            /*if (StringUtils.contains(name, nameSep))
            {
                String[] nameToks = StringUtils.split(name, innerSep);
            }*/
            
        }
        return names;
    }

    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    class AgentNameInfo
    {
        protected String firstName;
        protected String lastName;
        protected String middle;
        public AgentNameInfo(String firstName, String lastName, String middle)
        {
            super();
            this.firstName = firstName;
            this.lastName = lastName;
            this.middle = middle;
        }
        /**
         * @return the firstName
         */
        public String getFirstName()
        {
            return firstName;
        }
        /**
         * @return the lastName
         */
        public String getLastName()
        {
            return lastName;
        }
        /**
         * @return the middle
         */
        public String getMiddle()
        {
            return middle;
        }
        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }
        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }
        /**
         * @param middle the middle to set
         */
        public void setMiddle(String middle)
        {
            this.middle = middle;
        }
        
    }
    
    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    class AgentInfo
    {
        Integer                     oldAgentId;
        Integer                     newAgentId;
        Byte                        agentType;
        HashMap<Integer, Integer>   addrs    = new HashMap<Integer, Integer>();
        boolean                     isUsed   = false;
        boolean                     wasAdded = false;
        String                      lastName;
        String                      firstName;
        String                      name;
        int                         addrOrd = 0;
        
        HashSet<Integer>            addrsWritten = new HashSet<Integer>();


        public AgentInfo(Integer oldAgentId, 
                         Integer newAgentId,
                         byte    agentType,
                         String  lastName,
                         String  firstName,
                         String  name)
        {
            super();
            this.oldAgentId = oldAgentId;
            this.newAgentId = newAgentId;
            this.agentType  = agentType;
            this.lastName   = lastName;
            this.firstName  = firstName;
            this.name       = name;
        }

        public HashMap<Integer, Integer> getAddrs()
        {
            return addrs;
        }
        
        public void add(final Integer agtAdrId, final Integer adrId)
        {
            addrs.put(agtAdrId, adrId);
        }

        public Integer getNewAgentId()
        {
            return newAgentId;
        }

        public Integer getOldAgentId()
        {
            return oldAgentId;
        }

        public boolean isUsed()
        {
            return isUsed;
        }

        public void setUsed(boolean isUsed)
        {
            this.isUsed = isUsed;
        }

        public boolean wasAdded()
        {
            return wasAdded;
        }

        public void setWasAdded(boolean wasAddedArg)
        {
            this.wasAdded = wasAddedArg;
        }

        public void addWrittenAddrOldId(final Integer addrId)
        {
            addrsWritten.add(addrId);
        }
        
        public HashSet<Integer> getUnwrittenOldAddrIds()
        {
            HashSet<Integer> unwrittenIds = new HashSet<Integer>();
            for (Integer agentAddrs : addrs.keySet())
            {
                if (!addrsWritten.contains(agentAddrs))
                {
                    unwrittenIds.add(agentAddrs);
                }
            }
            return unwrittenIds;
        }
        
        /**
         * @return the agentType
         */
        public Byte getAgentType()
        {
            return agentType;
        }

        /**
         * @return the lastName
         */
        public String getLastName()
        {
            return lastName;
        }

        /**
         * @return the firstName
         */
        public String getFirstName()
        {
            return firstName;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return String.format("AgentInfo [oldAgentId=%d, newAgentId=%d, lastName=%s, firstName=%s,  isUsed=%s,  wasAdded=%s, addrs=%d]", 
                    oldAgentId != null ? oldAgentId : -1, newAgentId != null ? newAgentId : -1, lastName, firstName, isUsed ? "Y" : "N", wasAdded ? "Y" : "N", addrs.size());
        }
    }
}
