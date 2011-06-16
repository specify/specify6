/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDefItem;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.ProgressFrame;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jun 17, 2010
 *
 */
public class StratToGTP
{
    protected static final Logger                           log                    = Logger.getLogger(StratToGTP.class);
    
    protected GenericDBConversion                           conversion;

    protected static StringBuilder                          strBuf                 = new StringBuilder("");

    protected static SimpleDateFormat                       dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected static Timestamp                              now                    = new Timestamp(System .currentTimeMillis());
    protected static String                                 nowStr                 = dateTimeFormatter.format(now);

    protected ProgressFrame                                 frame     = null;
    protected boolean                                       hasFrame  = false;
    
    protected Connection                                    oldDBConn = null;
    protected Connection                                    newDBConn = null;
    
    protected IdMapperMgr                                   idMapperMgr;
    protected TableWriter                                   tblWriter;
    
    protected GeologicTimePeriodTreeDefItem                 era;
    protected GeologicTimePeriodTreeDef                     geoLogTmTreeDef;
    protected GeologicTimePeriod                            eraNode;


    protected Hashtable<String, GeologicTimePeriod> geoLogTmHash = new Hashtable<String, GeologicTimePeriod>();

    /**
     * @param oldDBConn
     * @param newDBConn
     * @param oldDBName
     * @param tblWriter
     */
    public StratToGTP(final Connection oldDBConn, 
                      final Connection newDBConn,
                      final String     oldDBName,
                      final TableWriter tblWriter,
                      final GenericDBConversion conv)
    {
        this.oldDBConn  = oldDBConn;
        this.newDBConn  = newDBConn;
        this.tblWriter  = tblWriter;
        this.conversion = conv;
        
        this.idMapperMgr = IdMapperMgr.getInstance();
    }
    
    /**
     * @param tblWriter
     */
    public void createGTPTreeDef() throws SQLException
    {
        deleteAllRecordsFromTable("geologictimeperiodtreedef", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable("geologictimeperiodtreedefitem", BasicSQLUtils.myDestinationServerType);
        
        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        geoLogTmTreeDef = createGeologicTimePeriodTreeDef("GeologicTimePeriod");
        localSession.saveOrUpdate(geoLogTmTreeDef);
        
                                      era       = createGeologicTimePeriodTreeDefItem(geoLogTmTreeDef, "Era",           0, false);
        GeologicTimePeriodTreeDefItem superGrp  = createGeologicTimePeriodTreeDefItem(era,             "Period",      100, false);
        GeologicTimePeriodTreeDefItem lithoGrp  = createGeologicTimePeriodTreeDefItem(superGrp,        "Epoch",       200, false);
        GeologicTimePeriodTreeDefItem eml       = createGeologicTimePeriodTreeDefItem(lithoGrp,        "EML (epoch)", 300, false);
        GeologicTimePeriodTreeDefItem age       = createGeologicTimePeriodTreeDefItem(eml,             "Age",         400, false);
                                                  createGeologicTimePeriodTreeDefItem(age,             "EML (age)",   500, false);
        localSession.saveOrUpdate(era);
        
        // setup the root Geography record (planet Earth)
        eraNode = new GeologicTimePeriod();
        eraNode.initialize();
        eraNode.setName("Era");
        eraNode.setFullName("Era");
        eraNode.setNodeNumber(1);
        eraNode.setHighestChildNodeNumber(1);
        eraNode.setRankId(0);
        eraNode.setDefinition(geoLogTmTreeDef);
        eraNode.setDefinitionItem(era);
        era.getTreeEntries().add(eraNode);
        localSession.saveOrUpdate(eraNode);

        HibernateUtil.commitTransaction();

        log.info("Finished inferring GTP tree definition and items");
    }
    
    /**
     * @throws SQLException
     */
    public void convertStratToGTP() throws SQLException
    {
        Statement stmt = null;
        ResultSet rs   = null;
        
        try
        {
            // get a Hibernate session for saving the new records
            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
    
            int count = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM stratigraphy");
            if (count < 1) return;
            
            if (hasFrame)
            {
                setProcess(0, count);
            }
            
            IdTableMapper gtpIdMapper = IdMapperMgr.getInstance().addTableMapper("geologictimeperiod", "GeologicTimePeriodID");
            
            Hashtable<Integer, Integer> ceToNewStratIdHash = new Hashtable<Integer, Integer>();
            
            IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");

            // get all of the old records
            //  Future GTP                           Period        Epoch       EML        Age    EML(age)    Text1   Text2     Remarks
            String sql  = "SELECT s.StratigraphyID, s.SuperGroup, s.Group, s.Formation, s.Member, s.Bed,    s.Text1, s.Text2,  s.Remarks FROM stratigraphy s ORDER BY s.StratigraphyID";
            
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs   = stmt.executeQuery(sql);
    
            int counter = 0;
            // for each old record, convert the record
            while (rs.next())
            {
                if (counter % 500 == 0)
                {
                    if (hasFrame)
                    {
                        setProcess(counter);
    
                    } else
                    {
                        log.info("Converted " + counter + " Stratigraphy records");
                    }
                }
    
                // grab the important data fields from the old record
                int oldStratId = rs.getInt(1);
                String period  = rs.getString(2);
                String epoch   = rs.getString(3);
                String eml     = rs.getString(4);
                String age     = rs.getString(5);
                String emlAge  = rs.getString(6);
                
                String text1      = rs.getString(7);
                String text2      = rs.getString(8);
                String remarks    = rs.getString(9);
                
                if (StringUtils.isNotEmpty(text2) && text2.length() > 128)
                {
                    remarks += "; " + text2;
                    text2    = text2.substring(0, 128);
                }
                
                if (StringUtils.isNotEmpty(eml))
                {
                    if (StringUtils.isNotEmpty(epoch))
                    {
                        epoch += ' ' + eml;
                        
                    } else
                    {
                        epoch = eml;
                    }
                }
                
                if (StringUtils.isEmpty(epoch))
                {
                    epoch = "(Empty)";
                }
                
                // create a new Geography object from the old data
                GeologicTimePeriod newStrat = convertOldStratRecord(period, epoch, eml, age, emlAge, text1, text2, remarks, eraNode, localSession);
    
                counter++;
    
                // Map Old GeologicTimePeriod ID to the new Tree Id
                gtpIdMapper.put(oldStratId, newStrat.getGeologicTimePeriodId());
                
                // Convert Old CEId to new CEId, then map the new CEId -> new StratId
                Integer ceId = ceMapper.get(oldStratId);
                if (ceId != null)
                {
                    ceToNewStratIdHash.put(ceId, newStrat.getGeologicTimePeriodId());
                } else
                {
                    String msg = String.format("No CE mapping for Old StratId %d, when they are a one-to-one.", oldStratId);
                    tblWriter.logError(msg);
                    log.error(msg);
                }
            }
            stmt.close();
    
            if (hasFrame)
            {
                setProcess(counter);
    
            } else
            {
                log.info("Converted " + counter + " Stratigraphy records");
            }
    
            TreeHelper.fixFullnameForNodeAndDescendants(eraNode);
            eraNode.setNodeNumber(1);
            fixNodeNumbersFromRoot(eraNode);
            rs.close();
            
            HibernateUtil.commitTransaction();
            log.info("Converted " + counter + " Stratigraphy records");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        // Now in this Step we Add the PaleoContext to the Collecting Events
        
    }

    
    /**
     * Regenerates all nodeNumber and highestChildNodeNumber field values for all nodes attached to
     * the given root. The nodeNumber field of the given root must already be set.
     * 
     * @param root the top of the tree to be renumbered
     * @return the highest node number value present in the subtree rooted at <code>root</code>
     */
    private static <T extends Treeable<T, ?, ?>> int fixNodeNumbersFromRoot(T root)
    {
        int nextNodeNumber = root.getNodeNumber();
        for (T child : root.getChildren())
        {
            child.setNodeNumber(++nextNodeNumber);
            nextNodeNumber = fixNodeNumbersFromRoot(child);
        }
        root.setHighestChildNodeNumber(nextNodeNumber);
        return nextNodeNumber;
    }

    /**
     * @param treeDef
     * @throws SQLException
     */
    /*@SuppressWarnings("unchecked")
    private GeologicTimePeriod convertGeologicTimePeriodFromCSV(final GeologicTimePeriodTreeDef treeDef, final boolean doSave)
    {
        geoLogTmHash.clear();

        File file = new File("Stratigraphy.csv");
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }

        // empty out any pre-existing records
        deleteAllRecordsFromTable(newDBConn, "geologictimeperiod", BasicSQLUtils.myDestinationServerType);

        // get a Hibernate session for saving the new records
        Session localSession = doSave ? HibernateUtil.getCurrentSession() : null;
        if (localSession != null)
        {
            HibernateUtil.beginTransaction();
        }

        List<String> lines = null;
        try
        {
            lines = FileUtils.readLines(file);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        // setup the root Geography record (planet Earth)
        GeologicTimePeriod earth = new GeologicTimePeriod();
        earth.initialize();
        earth.setName("Earth");
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        for (Object o : treeDef.getTreeDefItems())
        {
            GeologicTimePeriodTreeDefItem defItem = (GeologicTimePeriodTreeDefItem)o;
            if (defItem.getRankId() == 0)
            {
                earth.setDefinitionItem(defItem);
                break;
            }
        }
        GeologicTimePeriodTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);
        if (doSave)
        {
            localSession.save(earth);
        }

        // create an ID mapper for the geography table (mainly for use in converting localities)
        IdTableMapper geoLogTmIdMapper = doSave ? IdMapperMgr.getInstance().addTableMapper("geologictimeperiod", "GeologicTimePeriodID") : null;

        int counter = 0;
        // for each old record, convert the record
        for (String line : lines)
        {
            if (counter == 0)
            {
                counter = 1;
                continue; // skip header line
            }

            if (counter % 500 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " Stratigraphy records");
                }
            }

            String[] columns = StringUtils.splitPreserveAllTokens(line, ',');
            if (columns.length < 7)
            {
                log.error("Skipping[" + line + "]");
                continue;
            }

            // grab the important data fields from the old record
            int oldId = Integer.parseInt(columns[0]);
            String superGroup = columns[2];
            String lithoGroup = columns[3];
            String formation  = columns[4];
            String member     = columns[5];
            String bed        = columns[6];

            // create a new Geography object from the old data 
            GeologicTimePeriod newStrat = convertOldStratRecord(superGroup, lithoGroup, formation, member, bed, earth, localSession);

            counter++;

            // add this new ID to the ID mapper
            if (geoLogTmIdMapper != null)
            {
                geoLogTmIdMapper.put(oldId, newStrat.getGeologicTimePeriodId());
            }

        }

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " Stratigraphy records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

        if (doSave)
        {
            HibernateUtil.commitTransaction();
        }
        log.info("Converted " + counter + " Stratigraphy records");

        // set up Geography foreign key mapping for locality
        if (doSave)
        {
            idMapperMgr.mapForeignKey("Locality", "StratigraphyID", "GeologicTimePeriod", "GeologicTimePeriodID");
        }

        geoLogTmHash.clear();

        return earth;
    }*/

    /**
     * @param nameArg
     * @param parentArg
     * @param sessionArg
     * @return
     */
    private GeologicTimePeriod buildGeologicTimePeriodLevel(final String     nameArg,
                                                            final GeologicTimePeriod parentArg,
                                                            final Session    sessionArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<GeologicTimePeriod> children = parentArg.getChildren();
        for (GeologicTimePeriod child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        GeologicTimePeriod newStrat = new GeologicTimePeriod();
        newStrat.initialize();
        newStrat.setName(name);
        
        newStrat.setParent(parentArg);
        parentArg.addChild(newStrat);
        newStrat.setDefinition(parentArg.getDefinition());
        
        int                   newGeoRank = parentArg.getRankId() + 100;
        GeologicTimePeriodTreeDefItem defItem    = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newStrat.setDefinitionItem(defItem);
        
        newStrat.setRankId(newGeoRank);

        if (sessionArg != null)
        {
            sessionArg.save(newStrat);
        }

        return newStrat;
    }

    /**
     * @param period
     * @param epoch
     * @param formation
     * @param age
     * @param bed
     * @param stratRoot
     * @param localSession
     * @return  period, epoch, eml, age, emlAge
     */
    private GeologicTimePeriod convertOldStratRecord(final String     period,
                                                     final String     epoch,
                                                     final String     eml,
                                                     final String     age,
                                                     final String     emlAge, 
                                                     final String     text1, 
                                                     final String     text2, 
                                                     final String     remarks,
                                                     final GeologicTimePeriod stratRoot,
                                                     final Session    localSession)
    {
        String levelNames[] = { period, epoch, eml, age, emlAge};
        int levelsToBuild = 0;
        for (int i = levelNames.length; i > 0; --i)
        {
            if (StringUtils.isNotEmpty(levelNames[i - 1]))
            {
                levelsToBuild = i;
                break;
            }
        }

        for (int i = 0; i < levelsToBuild; i++)
        {
            if (StringUtils.isEmpty(levelNames[i]))
            {
                levelNames[i] = "(Empty)";
            }
        }

        GeologicTimePeriod prevLevelGeo = stratRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            GeologicTimePeriod newLevelStrat = buildGeologicTimePeriodLevel(levelNames[i], prevLevelGeo, localSession);
            newLevelStrat.setText1(text1);
            newLevelStrat.setText2(text2);
            newLevelStrat.setRemarks(remarks);
            
            if (localSession != null)
            {
                localSession.save(newLevelStrat);
            }
            prevLevelGeo = newLevelStrat;
        }

        return prevLevelGeo;
    }

    //-------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * Create a <code>GeologicTimePeriodTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the GeologicTimePeriod tree def
     */
    private static GeologicTimePeriodTreeDef createGeologicTimePeriodTreeDef(final String name)
    {
        GeologicTimePeriodTreeDef lstd = new GeologicTimePeriodTreeDef();
        lstd.initialize();
        lstd.setName(name);
        lstd.setFullNameDirection(TreeDefIface.FORWARD);
        return lstd;
    }

    /**
     * @param parent
     * @param name
     * @param rankId
     * @param inFullName
     * @return
     */
    private static GeologicTimePeriodTreeDefItem createGeologicTimePeriodTreeDefItem(final GeologicTimePeriodTreeDefItem parent,
                                                                                     final String name,
                                                                                     final int rankId,
                                                                                     final boolean inFullName)
    {
        if (parent != null)
        {
            GeologicTimePeriodTreeDef treeDef = parent.getTreeDef();
            if (treeDef != null)
            {
                GeologicTimePeriodTreeDefItem lstdi = new GeologicTimePeriodTreeDefItem();
                lstdi.initialize();
                lstdi.setName(name);
                lstdi.setRankId(rankId);
                lstdi.setIsInFullName(inFullName);
                lstdi.setIsEnforced(false);

                lstdi.setTreeDef(treeDef);
                treeDef.getTreeDefItems().add(lstdi);
                
                parent.getChildren().add(lstdi);
                lstdi.setParent(parent);
                
                return lstdi;
            }
            throw new RuntimeException("GeologicTimePeriodTreeDef is null!");
        }
        throw new RuntimeException("Parent is null!");
    }

    /**
     * @param treeDef
     * @param name
     * @param rankId
     * @param inFullName
     * @return
     */
    private static GeologicTimePeriodTreeDefItem createGeologicTimePeriodTreeDefItem(final GeologicTimePeriodTreeDef treeDef,
                                                                                     final String name,
                                                                                     final int rankId,
                                                                                     final boolean inFullName)
    {
        if (treeDef != null)
        {
            GeologicTimePeriodTreeDefItem lstdi = new GeologicTimePeriodTreeDefItem();
            lstdi.initialize();
            lstdi.setName(name);
            lstdi.setRankId(rankId);
            lstdi.setIsInFullName(inFullName);
            lstdi.setIsEnforced(false);
            lstdi.setTreeDef(treeDef);
            treeDef.getTreeDefItems().add(lstdi);
            return lstdi;
        }
        throw new RuntimeException("GeologicTimePeriodTreeDef is null!");
    }
    
    //-------------------------------------------------------------------------------------------------------------
    // Convert stratigraphy2 to stratigraphy tree
    //-------------------------------------------------------------------------------------------------------------
    
    /**
     * @param stratTblWriter
     */
    public void convertStrat(final TableWriter stratTblWriter, final boolean isPaleo) throws SQLException
    {
        Transaction trans  = null;
        Session lclSession = null;
        try
        {
            // empty out any pre-existing records
            deleteAllRecordsFromTable(newDBConn, "lithostrat", BasicSQLUtils.myDestinationServerType);
            
            lclSession = HibernateUtil.getNewSession();
            
            List<?>  disciplineeList = lclSession.createQuery("FROM Discipline").list();
            
            for (Object obj : disciplineeList)
            {
                trans = lclSession.beginTransaction();
                
                Discipline discipline = (Discipline)obj;
                LithoStratTreeDef lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
                
                lithoStratTreeDef.getDisciplines().add(discipline);
                discipline.setLithoStratTreeDef(lithoStratTreeDef);
                
                lclSession.saveOrUpdate(lithoStratTreeDef);
                lclSession.saveOrUpdate(discipline);
                
                LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth",         0, false);
                LithoStratTreeDefItem superGroup     = createLithoStratTreeDefItem(earth,        "Suprt Group", 100, false);
                LithoStratTreeDefItem group     = createLithoStratTreeDefItem(superGroup,        "Group",       200, false);
                LithoStratTreeDefItem formation = createLithoStratTreeDefItem(group,             "Formation",   300, false);
                LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation,         "Member",      400, false);
                                                  createLithoStratTreeDefItem(member,            "Unit",        500, false);
                lclSession.saveOrUpdate(earth);
                
                // setup the root Geography record (planet Earth)
                LithoStrat earthNode = new LithoStrat();
                earthNode.initialize();
                earthNode.setName("Earth");
                earthNode.setFullName("Earth");
                earthNode.setNodeNumber(1);
                earthNode.setHighestChildNodeNumber(1);
                earthNode.setRankId(0);
                earthNode.setDefinition(lithoStratTreeDef);
                earthNode.setDefinitionItem(earth);
                earth.getTreeEntries().add(earthNode);
                lclSession.saveOrUpdate(earthNode);
                
                trans.commit();
                
                if (isPaleo)
                {
                    conversion.convertLithoStrat(lithoStratTreeDef, earthNode, stratTblWriter, "stratigraphy2", false);
                }
            }
            
        } catch (Exception ex)
        {
            if (trans != null)
            {
                trans.rollback();
            }
            
            ex.printStackTrace();
        } finally
        {
            lclSession.close();
        }
    }

    //-----------------------------------------------------------------------------------------------------
    // Misc
    //-----------------------------------------------------------------------------------------------------
    
    /**
     * Sets a UI feedback frame.
     * @param frame the frame
     */
    public void setFrame(final ProgressFrame frame)
    {
        this.frame = frame;
        hasFrame = frame != null;

        BasicSQLUtils.setFrame(frame);

        if (idMapperMgr != null)
        {
            idMapperMgr.setFrame(frame);
        }
    }

    public void setOverall(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setOverall(min, max);
        }
    }

    public void setOverall(final int value)
    {
        if (hasFrame)
        {
            frame.setOverall(value);
        }
    }

    public void setProcess(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setProcess(min, max);
        }
    }

    public void setProcess(final int value)
    {
        if (hasFrame)
        {
            frame.setProcess(value);
        }
    }

    public void setDesc(final String text)
    {
        if (hasFrame)
        {
            frame.setDesc(text);
        }
    }

    /**
     * Return the SQL Connection to the Old Database
     * @return the SQL Connection to the Old Database
     */
    public Connection getOldDBConnection()
    {
        return oldDBConn;
    }

}
