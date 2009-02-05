package edu.ku.brc.dbsupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.EntityMode;
import org.hibernate.Session;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/*
 * @code_status ?
 * 
 * @author Andrew Ozor
 * 
 */
// TODO find a better way to stip the working set
public class ImportExportDB
{
    protected Session session;
    protected String  importFolderPath;

    public static void main(String[] args)
    {
        Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
        UIHelper.tryLogin("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", //$NON-NLS-1$ //$NON-NLS-2$
                "testfish", "jdbc:mysql://localhost/testfish", //$NON-NLS-1$ //$NON-NLS-2$
                usernamePassword.first, 
                usernamePassword.second); 

        Session testSession = HibernateUtil.getCurrentSession();
        String workingFolder = "Specify6ImportExport"; //$NON-NLS-1$
        File wf = new File(workingFolder);
        if (!wf.exists())
        {
            wf.mkdirs();
        }
        //String dbTable = "Division"; //$NON-NLS-1$

        ImportExportDB impexp = new ImportExportDB(testSession, workingFolder + File.separator);

        try
        {
            // record set
            // impexp.importSingleDBObject(dbTable, 1, false);
            // impexp.printRecordSet(dbTable);
            // impexp.printSingleRecordXML(dbTable, 3);
            // impexp.writeRecordSet(dbTable, 1);
            // impexp.writeSingleRecordXML(dbTable, 1);
            // impexp.exportTables();
            // List<String> temp = new ArrayList();
            // impexp.getImmediateParentTables(dbTable,temp , false);
            // impexp.getRequiredFields("Geography");

            // import
            System.out.println("importing..."); //$NON-NLS-1$
            // impexp.importTable(dbTable);
            // impexp.importTable("LoanAgent");
            // print results
            System.out.println("printing..."); //$NON-NLS-1$
            //impexp.printXML(dbTable);
            // impexp.printXML("Geography");
            // impexp.printXML("LoanAgent");
            // impexp.printXML("CollectionObject");
            // impexp.printXML("LoanReturnPreparation");
            // impexp.printXML("CollectionObject");
            // impexp.printXML("Collector");

            // export
            // impexp.writeSingleRecordXML(dbTable,1);
            //impexp.exportTables();
            
            impexp.writeXMLfile("PrepType"); //$NON-NLS-1$

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        } finally
        {
            impexp.close();
            System.out.println("...done"); //$NON-NLS-1$
        }
    }

    /**
     * Consturctor
     * @param session the current session
     */
    public ImportExportDB(Session session)
    {
        this.session = session;
    }

    public ImportExportDB(Session session, String importFolderPath)
    {
        this.session = session;
        this.importFolderPath = importFolderPath;
    }

    /**
     * Imports a table and related tables into a database
     * @param dbTable the class name of the table
     */
    public void importTable(String dbTable)
    {
        try
        {
            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);
            if (path == null)
            {
                System.err.println("error opening file"); //$NON-NLS-1$
            } else
            {
                HibernateUtil.beginTransaction();

                // dynamicXMLImport(dbImport, dbTable);
                long t = 0;
                // sequentialXMLImportRecordSet(dbImport, dbTable, dbTable, t);
                sequentialDatabaseImport(dbImport, dbTable, dbTable, t, false);
                // sequentialXMLImport(dbImport, dbTable, dbTable, t);//the parent is itself
                // iterativeXMLImport(dbImport, dbTable, dbTable, t);//the parent is itself
                HibernateUtil.commitTransaction();
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            try
            {
                ex.printStackTrace();
                HibernateUtil.rollbackTransaction();
            } catch (Exception ex1)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex1);
                ex1.printStackTrace();
            }
        }
    }

    // import a single dbobject
    // dbTable - table name
    // id - the id of the dbOjbect to be imported
    // recursion - also add children?
    public void importSingleDBObject(String dbTable, int id, boolean recursion)
    {
        try
        {

            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();

            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);
            Element importMe = (Element) dbImport.selectSingleNode("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                    + primaryKey + " = \"" + id + "\"]"); //$NON-NLS-1$ //$NON-NLS-2$

            if (path == null)
            {
                System.err.println("error opening file"); //$NON-NLS-1$
            } else if (importMe == null)
            {
                System.err.println("record with id:" + id + " does not exsist in " //$NON-NLS-1$ //$NON-NLS-2$
                        + importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            } else
            {
                HibernateUtil.beginTransaction();
                long t = 0;
                singleXMLImport(importMe, dbTable, dbTable, t, id, recursion);// the parent is
                                                                                // itself
                HibernateUtil.commitTransaction();
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
    }

    // return a built database object that is ready to be imported
    // dbTable - table name
    // id - the id of the dbOjbect to be imported
    // recursion - also add children?
    public Object buildSingleDBObject(String dbTable, int id, boolean recursion)
    {
        Object dbObject = new Object();
        try
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();

            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);
            Element importMe = (Element) dbImport.selectSingleNode("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                    + primaryKey + " = \"" + id + "\"]"); //$NON-NLS-1$ //$NON-NLS-2$

            if (path == null)
            {
                System.err.println("error opening file"); //$NON-NLS-1$
            } else
            {
                long t = 0;
                dbObject = buildSingleDBObjectFromXML(importMe, dbTable, dbTable, t, id, recursion);// the
                                                                                                    // parent
                                                                                                    // is
                                                                                                    // itself
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return dbObject;
    }

    // return a built database object that is ready to be imported
    // used for entire database insert
    public Object buildSingleDataBaseObject(String dbTable, int id, boolean recursion)
    {
        Object dbObject = new Object();
        try
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();

            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);
            Element importMe = (Element) dbImport.selectSingleNode("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                    + primaryKey + " = \"" + id + "\"]"); //$NON-NLS-1$ //$NON-NLS-2$

            if (path == null)
            {
                System.err.println("error opening file"); //$NON-NLS-1$
            } else
            {
                long t = 0;
                dbObject = buildSingleDataBaseObjectFromXML(importMe, dbTable, dbTable, t, id,
                        recursion);// the parent is itself
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return dbObject;
    }

    // for testing
    public Map buildSingleDataBaseObjectTemp(String dbTable, int id, boolean recursion)
    {
        Map dbObject = new HashMap();
        try
        {
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();

            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);
            Element importMe = (Element) dbImport.selectSingleNode("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                    + primaryKey + " = \"" + id + "\"]"); //$NON-NLS-1$ //$NON-NLS-2$

            if (path == null)
            {
                System.err.println("error opening file"); //$NON-NLS-1$
            } else
            {
                long t = 0;
                dbObject = buildSingleDataBaseObjectFromXMLTemp(importMe, dbTable, dbTable, t, id,
                        recursion);// the parent is itself
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return dbObject;
    }

    /**
     * Imports a table from a given xml file
     * @param dbImport the opend xml file elememt
     * @param dbTable the class name of the table
     */
    protected void dynamicXMLImport(Element dbImport, String dbTable)
    {
        try
        {
            String id = new String();
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());

            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            String lowerdbTable = lowerFirstChar(dbTable);
            // List attributes = dbImport.selectNodes("//@"+lowerdbTable+"Id");
            List ids = dbImport.selectNodes("//" + lowerdbTable + "Id");// assume this is dbs id //$NON-NLS-1$ //$NON-NLS-2$
                                                                        // name
            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                {
                    // keep this id to compare against it's collection
                    Element idElement = (Element) ids.get(k);
                    // id = attribute.getText();
                    id = idElement.getStringValue();
                    // make the agent and the element
                    Object agent = parentInfo.getClassObj().newInstance();
                    Map<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        Object value = findType(element, dbTable, agent, " ");// the parent is //$NON-NLS-1$
                                                                                // itself, just a
                                                                                // dummy variable
                        // if(value!=null && value != "collection")
                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);
                        }
                        // ignore many-to-many for now
                        else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {// RECURSE
                            // is it a collection, add all associated records
                            // get assoicated ids
                            // List collectingevent_ids =
                            // element.selectNodes("//"+dbTable+"[@"+lowerdbTable+"Id='"+id+"']/"+element.getName()+"/"+lowerElement);
                            // TODO shouldl not assume things are in order
                            List collectingevent_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                    + id + "]/" + element.getName() + "/*");// +upperElement); //$NON-NLS-1$ //$NON-NLS-2$

                            if (!collectingevent_ids.isEmpty())
                            {
                                // add all the assoctions to aDbElement
                                // get child dbName
                                String childDbName = getDbName(collectingevent_ids);
                                // make a parent object
                                BeanUtils.populate(agent, agentMap);

                                // Collection collection = xmlImportRecursion(locality,
                                // upperElement, collectingevent_ids, parent, lowerdbTable);
                                Set collection = xmlImportRecursion(agentMap, childDbName,
                                        collectingevent_ids, agent, lowerdbTable);
                                if (collection != null)
                                {
                                    agentMap.put(element.getName(), collection);
                                } else
                                {
                                    System.err.println("error on the collection " //$NON-NLS-1$
                                            + element.getName() + " with parent " + dbTable); //$NON-NLS-1$
                                }
                            }
                        } else
                        // else, dont add it
                        {
                            // if it is the tables id, just ignore. otherwise print out error
                            if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);

                    this.session.saveOrUpdate(agent);
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            // the last par tof the string conatins the class
            // if(err.startsWith("org.hibernate.PropertyValueException")){
            try
            {
                // try again
                // flush
                // do reference work
                // importTable("ReferenceWork");
                // the import aagain
            } catch (Exception ex1)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex1);
                ex1.printStackTrace();
            }
            // }else{
            ex.printStackTrace();
            // }
        }
    }

    protected void sequentialXMLImport(Element dbImport,
                                       String dbTable,
                                       String parentName,
                                       long parentId)
    {
        try
        {
            String id = new String();
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());

            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            String lowerdbTable = lowerFirstChar(dbTable);
            // TODO: should not assume that is the id name, use getPrimaryKeyName
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();
            // List ids = dbImport.selectNodes("//"+lowerdbTable+"Id");//assume this is dbs id name
            List ids = dbImport.selectNodes("//" + primaryKey); //$NON-NLS-1$

            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                { //
                    Vector collectionIds = new Vector(20);
                    Vector<String> collectionNames = new Vector(20);
                    // keep this id to compare against it's collection
                    Element idElement = (Element) ids.get(k);
                    // id = attribute.getText();
                    id = idElement.getStringValue();
                    // make the agent and the element
                    Object agent = parentInfo.getClassObj().newInstance();
                    Map<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        Object value = findTypeSequential(element, dbTable, parentId, parentName);// the
                                                                                                    // parent
                                                                                                    // is
                                                                                                    // itself,
                                                                                                    // just
                                                                                                    // a
                                                                                                    // dummy
                                                                                                    // variable
                        // if(value!=null && value != "collection")
                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);

                        }
                        // ignore many-to-many for now
                        else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {// RECURSE
                            // get assoicated ids
                            // List temp_collection_ids =
                            // element.selectNodes("//"+dbTable+"["+id+"]/"+element.getName()+"/*");//+upperElement);
                            List temp_collection_ids = element
                                    .selectNodes("//" + dbTable + "[" + primaryKey + " = \"" + id //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                            + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
                            // get collection info and still dont add it
                            if (!temp_collection_ids.isEmpty())
                            {
                                // get child dbName
                                String childDbName = getDbName(temp_collection_ids);
                                collectionNames.addElement(childDbName);
                                for (int index = 0; index < temp_collection_ids.size(); index++)
                                {
                                    collectionIds.addElement(temp_collection_ids.get(index));
                                }
                            }
                        } else
                        // else, dont add it
                        {
                            // if it is an id, just ignore. otherwise print out error
                            if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);

                    this.session.save(agent);
                    // session.lock(agent, LockMode.NONE);

                    // if there was a collection, then recurse
                    if (!collectionIds.isEmpty())
                    {
                        long newParentId = new Long(session.getIdentifier(agent).toString())
                                .longValue();

                        sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable,
                                newParentId);
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            // String err = ex.toString();
            // the last par tof the string conatins the class
            // if(err.startsWith("org.hibernate.PropertyValueException")){
            try
            {
                // try again
                // flush
                // do reference work
                // importTable("ReferenceWork");
                // the import aagain
            } catch (Exception ex1)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex1);
                ex1.printStackTrace();
            }
            // }else{
            ex.printStackTrace();
            // }
        }
    }

    protected void sequentialXMLImportRecursion(Vector collectionNames,
                                                Vector collectionIds,
                                                String parentName,
                                                long parentId)
    {
        int idIndex = 0;
        try
        {
            // for each collection
            for (int z = 0; z < collectionNames.size(); z++)
            {
                // get table to work on
                String dbTable = collectionNames.get(z).toString();
                String lowerdbTable = lowerFirstChar(dbTable);
                DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(
                        dbTable.toLowerCase());
                String primaryKey = info.getPrimaryKeyName();

                // open xml file
                File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
                Element dbImport = XMLHelper.readFileToDOM4J(path);

                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                        dbTable.toLowerCase());

                // loop for each id that we need in this table
                for (int k = 0; k < collectionIds.size() && idIndex < collectionIds.size(); k++)
                {
                    Vector newCollectionIds = new Vector(20);
                    Vector newCollectionNames = new Vector(20);
                    // the only way to get the value out of collectionIds
                    Element temp_id = (Element) collectionIds.get(idIndex);

                    // is this the right element to work on
                    // if so use else; otherwise stop the loop
                    if (temp_id.getName().equals(dbTable))
                    {
                        idIndex++;
                        String id = temp_id.getText();
                        // select the node
                        // TODO shouldl not assume things are in order
                        // Element collectingevent =
                        // (Element)dbImport.selectSingleNode("//"+dbTable+"["+id+"]");//temp_id.getText()+"]");
                        Element collectingevent = (Element) dbImport.selectSingleNode("//" //$NON-NLS-1$
                                + dbTable + "[" + primaryKey + " = \"" + id + "\"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Iterator iter = collectingevent.elementIterator();

                        // make the element and the agent
                        Map<String, Object> agentMap = new HashMap<String, Object>();
                        Object agent = tableInfo.getClassObj().newInstance();
                        do
                        {
                            Element element = (Element) iter.next();

                            Object value2 = findTypeSequential(element, dbTable, parentId,
                                    parentName);
                            if (value2 != null && value2 != "OneToMany" && value2 != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                            {
                                agentMap.put(element.getName(), value2);
                            } else if (value2 == "ManyToMany") //$NON-NLS-1$
                            {
                                Set<Object> parentSet = new HashSet<Object>();
                                Object parentObject = genericDBObject2(parentName, parentId);
                                parentSet.add(parentObject);
                                agentMap.put(element.getName(), parentSet);
                            } else if (value2 == "OneToMany") //$NON-NLS-1$
                            {
                                // RECURSE
                                // get assoicated ids
                                // TODO shouldl not assume things are in order
                                // List temp_collection_ids =
                                // element.selectNodes("//"+dbTable+"["+id+"]/"+element.getName()+"/*");//+upperElement);
                                List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                        + primaryKey + " = \"" + id + "\"]/" + element.getName() //$NON-NLS-1$ //$NON-NLS-2$
                                        + "/*"); //$NON-NLS-1$
                                // get collection info and still dont add it
                                if (!temp_collection_ids.isEmpty())
                                {
                                    // get child dbName
                                    String childDbName = getDbName(temp_collection_ids);
                                    newCollectionNames.addElement(childDbName);
                                    for (int index = 0; index < temp_collection_ids.size(); index++)
                                    {
                                        newCollectionIds.addElement(temp_collection_ids.get(index));
                                    }
                                }

                            } else
                            {
                                // if it is an id, just ignore. otherwise print out error
                                if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                                {
                                    System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                            + " to the element " + dbTable); //$NON-NLS-1$
                                }
                            }
                        } while (iter.hasNext());

                        // add to the set
                        BeanUtils.populate(agent, agentMap);

                        this.session.saveOrUpdate(agent);
                        // if there was a collection, then recurse
                        if (!newCollectionIds.isEmpty())
                        {
                            long newParentId = new Long(session.getIdentifier(agent).toString())
                                    .longValue();

                            sequentialXMLImportRecursion(newCollectionNames, newCollectionIds,
                                    dbTable, newParentId);
                        }
                    } else
                    {// stop the loop
                        k = collectionIds.size();
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            // the last par tof the string conatins the class
            // if(err.startsWith("org.hibernate.PropertyValueException")){
            try
            {
                // try again
                // flush
                // do reference work
                // importTable("ReferenceWork");
                // the import aagain
            } catch (Exception ex1)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex1);
                ex1.printStackTrace();
            }
            // }else{
            ex.printStackTrace();
            // }
        }
    }

    // import a record by its id, as it is in the XML file
    protected void singleXMLImport(Element dbImport,
                                   String dbTable,
                                   String parentName,
                                   long parentId,
                                   int id,
                                   boolean recursion)
    {
        try
        {
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            String lowerdbTable = lowerFirstChar(dbTable);
            String primaryKey = parentInfo.getPrimaryKeyName();

            Vector collectionIds = new Vector(20);
            Vector collectionNames = new Vector(20);
            // make the agent and the element
            Object agent = parentInfo.getClassObj().newInstance();
            Map<String, Object> agentMap = new HashMap<String, Object>();
            Element dbElement = dbImport;
            Iterator i = dbElement.elementIterator();
            do
            {// do for each element in the record
                Element element = (Element) i.next();

                Object value = findTypeSequential(element, dbTable, parentId, parentName);// the
                                                                                            // parent
                                                                                            // is
                                                                                            // itself,
                                                                                            // just
                                                                                            // a
                                                                                            // dummy
                                                                                            // variable
                // if(value!=null && value != "collection")
                if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {
                    agentMap.put(element.getName(), value);
                }
                // ignore many-to-many for now
                else if (recursion && (value == "OneToMany" || value == "ManyToMany")) //$NON-NLS-1$ //$NON-NLS-2$
                {// RECURSE
                    // get assoicated ids
                    List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                            + primaryKey + " = \"" + id + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    // get collection info and still dont add it
                    if (!temp_collection_ids.isEmpty())
                    {
                        // get child dbName
                        String childDbName = getDbName(temp_collection_ids);
                        collectionNames.addElement(childDbName);
                        for (int index = 0; index < temp_collection_ids.size(); index++)
                        {
                            collectionIds.addElement(temp_collection_ids.get(index));
                        }
                    }
                } else
                // else, dont add it
                {
                    // if it is an id, just ignore. otherwise print out error
                    if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                    {
                        System.err.println("did not add " + element.getName() + " to the element " //$NON-NLS-1$ //$NON-NLS-2$
                                + dbTable);
                    }
                }
            } while (i.hasNext());

            // populate and save
            BeanUtils.populate(agent, agentMap);

            this.session.save(agent);

            // if there was a collection, then recurse
            if (!collectionIds.isEmpty())
            {
                long newParentId = new Long(session.getIdentifier(agent).toString()).longValue();

                sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable, newParentId);
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
    }

    // return a DBobject that is ready to be imported, also makes and saves new parents
    protected Object buildSingleDBObjectFromXML(Element dbImport,
                                                String dbTable,
                                                String parentName,
                                                long parentId,
                                                int id,
                                                boolean recursion)
    {
        Object dbObject = new Object();
        try
        {
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            String primaryKey = parentInfo.getPrimaryKeyName();

            Vector collectionIds = new Vector(20);
            Vector collectionNames = new Vector(20);
            // make the agent and the element
            Object agent = parentInfo.getClassObj().newInstance();
            Map<String, Object> agentMap = new HashMap<String, Object>();
            Element dbElement = dbImport;
            Iterator i = dbElement.elementIterator();
            do
            {// do for each element in the record
                Element element = (Element) i.next();

                // Object value = findTypeSequential(element, dbTable, parentId, parentName );//the
                // parent is itself, just a dummy variable
                Object value = findTypeRecordSet(element, dbTable, parentId, parentName);// the
                                                                                            // parent
                                                                                            // is
                                                                                            // itself,
                                                                                            // just
                                                                                            // a
                                                                                            // dummy
                                                                                            // variable
                if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {
                    agentMap.put(element.getName(), value);
                }
                // ignore many-to-many for now
                else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {// RECURSE
                    if (recursion)
                    {
                        // get assoicated ids
                        List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                + primaryKey + " = \"" + id + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        // get collection info and still dont add it
                        if (!temp_collection_ids.isEmpty())
                        {
                            // get child dbName
                            String childDbName = getDbName(temp_collection_ids);
                            collectionNames.addElement(childDbName);
                            for (int index = 0; index < temp_collection_ids.size(); index++)
                            {
                                collectionIds.addElement(temp_collection_ids.get(index));
                            }
                        }
                    }
                } else
                // else, dont add it
                {
                    // if it is an id, just ignore. otherwise print out error
                    if (!element.getName().equals(primaryKey))
                    {
                        System.err.println("did not add " + element.getName() + " to the element " //$NON-NLS-1$ //$NON-NLS-2$
                                + dbTable);
                    }
                }
            } while (i.hasNext());

            // populate
            BeanUtils.populate(agent, agentMap);
            // save it then gets is id (assigned by Hibernate)
            this.session.save(agent);

            // if there was a collection, then recurse
            if (!collectionIds.isEmpty())
            {
                long newParentId = new Long(session.getIdentifier(agent).toString()).longValue();

                sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable, newParentId);
            }
            dbObject = agent;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }

        return dbObject;
    }

    // return a DBobject that is ready to be imported, also makes and saves new parents
    protected Object buildSingleDataBaseObjectFromXML(Element dbImport,
                                                      String dbTable,
                                                      String parentName,
                                                      long parentId,
                                                      int id,
                                                      boolean recursion)
    {
        Object dbObject = new Object();
        try
        {
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            String lowerdbTable = lowerFirstChar(dbTable);
            String primaryKey = parentInfo.getPrimaryKeyName();

            Vector collectionIds = new Vector(20);
            Vector collectionNames = new Vector(20);
            // make the agent and the element
            Object agent = parentInfo.getClassObj().newInstance();
            Map<String, Object> agentMap = new HashMap<String, Object>();
            Element dbElement = dbImport;
            Iterator i = dbElement.elementIterator();
            do
            {// do for each element in the record
                Element element = (Element) i.next();

                // Object value = findTypeSequential(element, dbTable, parentId, parentName );//the
                // parent is itself, just a dummy variable
                Object value = findTypeDataBaseParent(element, dbTable, parentId, parentName);// the
                                                                                                // parent
                                                                                                // is
                                                                                                // itself,
                                                                                                // just
                                                                                                // a
                                                                                                // dummy
                                                                                                // variable
                if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {
                    agentMap.put(element.getName(), value);
                }
                // ignore many-to-many for now
                else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {// RECURSE
                    if (recursion)
                    {
                        // get assoicated ids
                        List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                + primaryKey + " = \"" + id + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        // get collection info and still dont add it
                        if (!temp_collection_ids.isEmpty())
                        {
                            // get child dbName
                            String childDbName = getDbName(temp_collection_ids);
                            collectionNames.addElement(childDbName);
                            for (int index = 0; index < temp_collection_ids.size(); index++)
                            {
                                collectionIds.addElement(temp_collection_ids.get(index));
                            }
                        }
                    }
                } else
                // else, dont add it
                {
                    // if it is an id, just ignore. otherwise print out error
                    if (!element.getName().equals(primaryKey))
                    {
                        System.err.println("did not add " + element.getName() + " to the element " //$NON-NLS-1$ //$NON-NLS-2$
                                + dbTable);
                    }
                }
            } while (i.hasNext());

            // populate
            BeanUtils.populate(agent, agentMap);
            // save it then gets its id (assigned by Hibernate)
            this.session.save(agent);

            // if there was a collection, then recurse
            if (!collectionIds.isEmpty())
            {
                long newParentId = new Long(session.getIdentifier(agent).toString()).longValue();

                sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable, newParentId);
            }
            dbObject = agent;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }

        return dbObject;
    }

    // return a DBobject that is ready to be imported, also makes and saves new parents
    protected Map buildSingleDataBaseObjectFromXMLTemp(Element dbImport,
                                                       String dbTable,
                                                       String parentName,
                                                       long parentId,
                                                       int id,
                                                       boolean recursion)
    {
        Object dbObject = new Object();
        Map<String, Object> agentMap = new HashMap<String, Object>();
        try
        {
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            String lowerdbTable = lowerFirstChar(dbTable);
            String primaryKey = parentInfo.getPrimaryKeyName();

            Vector collectionIds = new Vector(20);
            Vector collectionNames = new Vector(20);
            // make the agent and the element
            Object agent = parentInfo.getClassObj().newInstance();

            Element dbElement = dbImport;
            Iterator i = dbElement.elementIterator();
            do
            {// do for each element in the record
                Element element = (Element) i.next();

                // Object value = findTypeSequential(element, dbTable, parentId, parentName );//the
                // parent is itself, just a dummy variable
                Object value = findTypeDataBaseParent(element, dbTable, parentId, parentName);// the
                                                                                                // parent
                                                                                                // is
                                                                                                // itself,
                                                                                                // just
                                                                                                // a
                                                                                                // dummy
                                                                                                // variable
                if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {
                    agentMap.put(element.getName(), value);
                }
                // ignore many-to-many for now
                else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                {// RECURSE
                    if (recursion)
                    {
                        // get assoicated ids
                        List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                + primaryKey + " = \"" + id + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        // get collection info and still dont add it
                        if (!temp_collection_ids.isEmpty())
                        {
                            // get child dbName
                            String childDbName = getDbName(temp_collection_ids);
                            collectionNames.addElement(childDbName);
                            for (int index = 0; index < temp_collection_ids.size(); index++)
                            {
                                collectionIds.addElement(temp_collection_ids.get(index));
                            }
                        }
                    }
                } else
                // else, dont add it
                {
                    // if it is an id, just ignore. otherwise print out error
                    if (!element.getName().equals(primaryKey))
                    {
                        System.err.println("did not add " + element.getName() + " to the element " //$NON-NLS-1$ //$NON-NLS-2$
                                + dbTable);
                    }
                }
            } while (i.hasNext());

            // populate
            // BeanUtils.populate(agent, agentMap);
            // save it then gets its id (assigned by Hibernate)
            // this.session.save(agent);

            // if there was a collection, then recurse
            if (!collectionIds.isEmpty())
            {
                long newParentId = new Long(session.getIdentifier(agent).toString()).longValue();

                sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable, newParentId);
            }
            dbObject = agent;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }

        return agentMap;
    }

    protected void sequentialXMLImportRecordSet(Element dbImport,
                                                String dbTable,
                                                String parentName,
                                                long parentId)
    {
        try
        {
            String id = new String();
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            // get the records
            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            String lowerdbTable = lowerFirstChar(dbTable);
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();
            List ids = dbImport.selectNodes("//" + primaryKey); //$NON-NLS-1$

            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                {
                    Vector collectionIds = new Vector(20);
                    Vector collectionNames = new Vector(20);
                    // keep this id to compare against it's collection
                    Element idElement = (Element) ids.get(k);
                    id = idElement.getStringValue();
                    // make the agent and the element
                    Object agent = parentInfo.getClassObj().newInstance();
                    Map<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        Object value = findTypeRecordSet(element, dbTable, parentId, parentName);// the
                                                                                                    // parent
                                                                                                    // is
                                                                                                    // itself,
                                                                                                    // just
                                                                                                    // a
                                                                                                    // dummy
                                                                                                    // variable
                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);
                        }
                        // ignore many-to-many for now
                        else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {// RECURSE
                            // get assoicated ids
                            List temp_collection_ids = element
                                    .selectNodes("//" + dbTable + "[" + primaryKey + " = \"" + id //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                            + "\"]/" + element.getName() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
                            // get collection info and still dont add it
                            if (!temp_collection_ids.isEmpty())
                            {
                                // get child dbName
                                String childDbName = getDbName(temp_collection_ids);
                                collectionNames.addElement(childDbName);
                                for (int index = 0; index < temp_collection_ids.size(); index++)
                                {
                                    collectionIds.addElement(temp_collection_ids.get(index));
                                }
                            }
                        } else
                        // else, dont add it
                        {
                            // if it is an id, just ignore. otherwise print out error
                            if (!element.getName().equals(primaryKey))
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);

                    this.session.save(agent);

                    // if there was a collection, then recurse
                    if (!collectionIds.isEmpty())
                    {
                        long newParentId = new Long(session.getIdentifier(agent).toString())
                                .longValue();
                        // import all children
                        sequentialXMLImportRecursion(collectionNames, collectionIds, dbTable,
                                newParentId);
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
    }

    protected void parentXMLImport(Element dbImport,
                                   String dbTable,
                                   String parentName,
                                   long parentId)
    {
        // get the immediate parents
        List<String> immediateParents = new ArrayList<String>();
        immediateParents = getImmediateParentTables(dbTable, immediateParents, false);

        // get all parents
        List<String> parents = new ArrayList<String>();
        parents = getParentTables(dbTable, parents, false);

        // ignore agent for now
        Element dbElement = (Element) dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
        Iterator i = dbElement.elementIterator();
        do
        {// do for each element in the record
            Element element = (Element) i.next();
            String elementName = element.getName().toString();
            // if there is an immediate parent
            if (immediateParents.contains(elementName))
            {
                // check if three is a value
                if (element.getText().equals("") || element.getText().equals(null)) //$NON-NLS-1$
                {
                    // remove from list
                    immediateParents.remove(elementName);
                } else
                {
                    // check if we can load it
                    String className = immediateParents.get(immediateParents.indexOf(elementName));
                    int num = new Integer(className).intValue();
                    // make a new one
                    importSingleDBObject(className, num, false);
                }
            }

        } while (i.hasNext());
    }

    protected void iterativeXMLImport(Element dbImport,
                                      String dbTable,
                                      String parentName,
                                      long parentId)
    {
        try
        {
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());

            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            String lowerdbTable = lowerFirstChar(dbTable);
            // List ids = dbImport.selectNodes("//"+lowerdbTable+"Id");//assume this is dbs id name
            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                {
                    // keep this id to compare against it's collection
                    // Element idElement = (Element)ids.get(k);
                    // make the agent and the element
                    Object agent = parentInfo.getClassObj().newInstance();
                    Map<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        Object value = findTypeSequential(element, dbTable, parentId, parentName);// the
                                                                                                    // parent
                                                                                                    // is
                                                                                                    // itself,
                                                                                                    // just
                                                                                                    // a
                                                                                                    // dummy
                                                                                                    // variable
                        // if(value!=null && value != "collection")
                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);
                        } else
                        // else, dont add it
                        {
                            // if it is an id, just ignore. otherwise print out error
                            if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);

                    this.session.save(agent);
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
    }

    // TODO: only assumes one record in xm.fixl
    /**
     * Imports a table from a given xml file
     * @param dbImport the opend xml file elememt
     * @param dbTable the class name of the table
     */
    protected Object dynamicXMLImportRecReturn(Element dbImport, String dbTable)
    {
        Object agent = new Object();
        try
        {
            String id = new String();
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());

            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            String lowerdbTable = lowerFirstChar(dbTable);
            // List attributes = dbImport.selectNodes("//@"+lowerdbTable+"Id");
            // TODO shouldl not assume this is the dbprimary key, use dbtablemgr
            List ids = dbImport.selectNodes("//" + lowerdbTable + "Id");// assume this is dbs id //$NON-NLS-1$ //$NON-NLS-2$
                                                                        // name
            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                {
                    // keep this id to compare against it's collection
                    Element idElement = (Element) ids.get(k);
                    // id = attribute.getText();
                    id = idElement.getStringValue();
                    // make the agent and the element
                    agent = parentInfo.getClassObj().newInstance();
                    Map<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        Object value = findType(element, dbTable, agent, " ");// the parent is //$NON-NLS-1$
                                                                                // itself, just a
                                                                                // dummy variable

                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);

                        }
                        // ignore many-to-many for now
                        else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {// RECURSE
                            // is it a collection, add all associated records
                            // get assoicated ids
                            // TODO shouldl not assume things are in order
                            List collectingevent_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                    + id + "]/" + element.getName() + "/*");// +upperElement); //$NON-NLS-1$ //$NON-NLS-2$

                            if (!collectingevent_ids.isEmpty())
                            {
                                // add all the assoctions to aDbElement
                                // get child dbName
                                String childDbName = getDbName(collectingevent_ids);
                                // make a parent object
                                BeanUtils.populate(agent, agentMap);

                                // Collection collection = xmlImportRecursion(locality,
                                // upperElement, collectingevent_ids, parent, lowerdbTable);
                                Set collection = xmlImportRecursion(agentMap, childDbName,
                                        collectingevent_ids, agent, lowerdbTable);
                                if (collection != null)
                                {
                                    agentMap.put(element.getName(), collection);
                                } else
                                {
                                    System.err.println("error on the collection " //$NON-NLS-1$
                                            + element.getName() + " with parent " + dbTable); //$NON-NLS-1$
                                }
                            }
                        } else
                        // else, dont add it
                        {
                            // if it is an id, just ignore. otherwise print out error
                            if (!element.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);
                    this.session.saveOrUpdate(agent);

                }
            }

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return agent;
    }

    /**
     * recurses over many-to-many or one-to-many record sets
     * @param parentMap the map of the parent record
     * @param dbTable the class name of the table
     * @param ids the list of associated ids
     * @param parentObject the parent record
     * @param parentName the parentName
     * @return a set, or null if not found
     */
    protected Set xmlImportRecursion(Map parentMap,
                                     String dbTable,
                                     List ids,
                                     Object parentObject,
                                     String parentName)
    {
        try
        {
            HashSet<Object> set = new HashSet<Object>();
            String lowerdbTable = lowerFirstChar(dbTable);
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());

            // open the new file
            File path = new File(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            Element dbImport = XMLHelper.readFileToDOM4J(path);

            // add each collectingevent to locality
            for (int j = 0; j < ids.size(); j++)
            {
                // the only way to get the value out of collectingevent_ids
                Element temp_id = (Element) ids.get(j);
                String id2 = temp_id.getText();
                // select the node
                Element collectingevent = (Element) dbImport.selectSingleNode("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                        + id2 + "]");// temp_id.getText()+"]"); //$NON-NLS-1$
                Iterator iter = collectingevent.elementIterator();

                // make the element and the agent
                Map<String, Object> collectingEventMap = new HashMap<String, Object>();
                Object agent = parentInfo.getClassObj().newInstance();
                do
                {
                    Element secondelement = (Element) iter.next();

                    Object value2 = findType(secondelement, dbTable, parentObject, parentName);
                    if (value2 != null && value2 != "OneToMany" && value2 != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        collectingEventMap.put(secondelement.getName(), value2);
                    } else if (value2 == "ManyToMany") //$NON-NLS-1$
                    {
                        HashSet<Object> parentSet = new HashSet<Object>();
                        parentSet.add(parentObject);
                        collectingEventMap.put(secondelement.getName(), parentSet);
                    } else if (value2 == "OneToMany") //$NON-NLS-1$
                    {
                        // RECURSE
                        // is it a collection, add all associated records
                        // get assoicated ids
                        List associated_ids = secondelement.selectNodes("//" + dbTable + "[" + id2 //$NON-NLS-1$ //$NON-NLS-2$
                                + "]/" + secondelement.getName() + "/*");// +upperElement); //$NON-NLS-1$ //$NON-NLS-2$
                        if (!associated_ids.isEmpty())
                        {
                            // add all the assoctions to aDbElement
                            // get child database name
                            String childDbName = getDbName(associated_ids);
                            // make a parent object
                            BeanUtils.populate(agent, collectingEventMap);

                            Set collection = xmlImportRecursion(collectingEventMap, childDbName,
                                    associated_ids, agent, lowerdbTable);

                            if (collection != null)
                            {
                                collectingEventMap.put(secondelement.getName(), collection);
                            } else
                            {
                                System.err.println("error on the collection " //$NON-NLS-1$
                                        + secondelement.getName() + " with parent " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } else
                    {
                        // if it is an id, just ignore. otherwise print out error
                        if (!secondelement.getName().equals(lowerdbTable + "Id")) //$NON-NLS-1$
                        {
                            System.err.println("did not add " + secondelement.getName() //$NON-NLS-1$
                                    + " to the element " + dbTable); //$NON-NLS-1$
                        }
                    }
                } while (iter.hasNext());

                // add to the set
                BeanUtils.populate(agent, collectingEventMap);
                set.add(agent);
            }

            // return the set;
            return set;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
            return null;
        }

    }

    protected void sequentialDatabaseImport(Element dbImport,
                                            String dbTable,
                                            String parentName,
                                            long parentId,
                                            boolean recursion)
    {
        try
        {
            String id = new String();
            DBTableInfo parentInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                    dbTable.toLowerCase());
            // get the records
            List records = dbImport.selectNodes("//" + dbTable); //$NON-NLS-1$
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
            String primaryKey = info.getPrimaryKeyName();
            List ids = dbImport.selectNodes("//" + primaryKey); //$NON-NLS-1$

            if (records.size() < 1)
            {
                System.err.println("Cannot import. Given database type:" + dbTable //$NON-NLS-1$
                        + " does not exsist in import file"); //$NON-NLS-1$
            } else
            {
                // loop for each record
                for (int k = 0; k < records.size(); k++)
                {
                    Vector collectionIds = new Vector(20);
                    Vector collectionNames = new Vector(20);
                    // keep this id to compare against it's collection
                    Element idElement = (Element) ids.get(k);
                    id = idElement.getStringValue();
                    // make the agent and the element
                    Object agent = parentInfo.getClassObj().newInstance();
                    HashMap<String, Object> agentMap = new HashMap<String, Object>();
                    Element dbElement = (Element) records.get(k);
                    Iterator i = dbElement.elementIterator();
                    do
                    {// do for each element in the record
                        Element element = (Element) i.next();

                        // Object value = findTypeSequential(element, dbTable, parentId, parentName
                        // );//the parent is itself, just a dummy variable
                        Object value = findTypeDataBase(element, dbTable, parentId, parentName);// the
                                                                                                // parent
                                                                                                // is
                                                                                                // itself,
                                                                                                // just
                                                                                                // a
                                                                                                // dummy
                                                                                                // variable
                        if (value != null && value != "OneToMany" && value != "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {
                            agentMap.put(element.getName(), value);
                        }
                        // ignore many-to-many for now
                        else if (value == "OneToMany" || value == "ManyToMany") //$NON-NLS-1$ //$NON-NLS-2$
                        {// RECURSE
                            if (recursion)
                            {
                                // get assoicated ids
                                List temp_collection_ids = element.selectNodes("//" + dbTable + "[" //$NON-NLS-1$ //$NON-NLS-2$
                                        + primaryKey + " = \"" + id + "\"]/" + element.getName() //$NON-NLS-1$ //$NON-NLS-2$
                                        + "/*"); //$NON-NLS-1$
                                // get collection info and still dont add it
                                if (!temp_collection_ids.isEmpty())
                                {
                                    // get child dbName
                                    String childDbName = getDbName(temp_collection_ids);
                                    collectionNames.addElement(childDbName);
                                    for (int index = 0; index < temp_collection_ids.size(); index++)
                                    {
                                        collectionIds.addElement(temp_collection_ids.get(index));
                                    }
                                }
                            }
                        } else
                        // else, dont add it
                        {
                            // if it is an id, just ignore. otherwise print out error
                            if (!element.getName().equals(primaryKey))
                            {
                                System.err.println("did not add " + element.getName() //$NON-NLS-1$
                                        + " to the element " + dbTable); //$NON-NLS-1$
                            }
                        }
                    } while (i.hasNext());

                    // populate and save
                    BeanUtils.populate(agent, agentMap);

                    this.session.save(agent);

                    // if there was a collection, then recurse
                    /*
                     * if(!collectionIds.isEmpty()) { long newParentId = new
                     * Long(session.getIdentifier(agent).toString()).longValue(); //import all
                     * children sequentialXMLImportRecursion(collectionNames, collectionIds,
                     * dbTable, newParentId); }
                     */
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
    }

    /**
     * set the value of an element to the correct type
     * @param compareMe the element
     * @param dbTable the class name of the table
     * @param parentObject the parent record
     * @param parentName the parentName
     * @return an object, "ManyToMany", "OneToMany", or null if it could not find the type
     */
    protected Object findType(Element compareMe,
                              String dbTable,
                              Object parentObject,
                              String parentName)
    {
        try
        { // get the fieldinfo
            String lowerdbTable = dbTable.toLowerCase();
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
            // find element with compareme.getName()
            DBFieldInfo fieldInfo = info.getFieldByName(compareMe.getName());
            // if the element is an id, ignore it
            if (!compareMe.getName().equals(lowerFirstChar(dbTable) + "Id")) //$NON-NLS-1$
            {
                // if it is a normal field
                if (fieldInfo != null)
                {
                    String type = fieldInfo.getType();
                    // check the type
                    if (type.equals("java.lang.String") || type.equals("text")) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        return compareMe.getStringValue();

                    } else if (type.equals("java.util.Date")) //$NON-NLS-1$
                    {
                        Date dtTmp = new SimpleDateFormat("yy-MM-dd H:mm:ss").parse(compareMe //$NON-NLS-1$
                                .getStringValue());
                        return dtTmp;

                    } else if (type.equals("java.sql.Timestamp")) //$NON-NLS-1$
                    {
                        Timestamp tmstmp = Timestamp.valueOf(compareMe.getStringValue());
                        return tmstmp;
                    } else if (type.equals("java.lang.Integer")) //$NON-NLS-1$
                    {
                        int num = new Integer(compareMe.getStringValue()).intValue();
                        return num;

                    } else if (type.equals("java.lang.Boolean")) //$NON-NLS-1$
                    {
                        Boolean bool = Boolean.valueOf(compareMe.getStringValue());
                        return bool;
                    } else if (type.equals("java.math.BigDecimal")) //$NON-NLS-1$
                    {
                        BigDecimal num = new BigDecimal(compareMe.getStringValue());
                        return num;
                    } else if (type.equals("java.lang.Double")) //$NON-NLS-1$
                    {
                        double num = new Double(compareMe.getStringValue()).doubleValue();
                        return num;
                    } else if (type.equals("java.lang.Float")) //$NON-NLS-1$
                    {
                        float num = new Float(compareMe.getStringValue()).floatValue();
                        return num;
                    } else if (type.equals("java.lang.Long")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();
                        return num;
                    } else if (type.equals("java.lang.Short")) //$NON-NLS-1$
                    {
                        short num = new Short(compareMe.getStringValue()).shortValue();
                        return num;
                    } else if (type.equals("java.lang.Byte")) //$NON-NLS-1$
                    {
                        byte num = new Byte(compareMe.getStringValue()).byteValue();
                        return num;
                    } else if (type.equals("java.util.Calendar")) //$NON-NLS-1$
                    {
                        Calendar date = dateString2Calendar(compareMe.getStringValue());
                        return date;
                    }
                } else
                // check if it is a many-to-one
                {
                    DBRelationshipInfo tablerel = info.getRelationshipByName(compareMe.getName());
                    if (tablerel != null && tablerel.getType().name() == "ManyToOne") //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();

                        if (compareMe.getName().equals(parentName))
                        {
                            return parentObject;
                        }
                        // else check if the xml fiel is there,
                        // if so run it
                        // otherwise make a generic thing
                        // some things cant be generic
                        // also check the order of how buildsampledb does it ^.^
                        // a generic map
                        // get the classtype
                        /*
                         * if(compareMe.getName() == "deaccessionPreparation"){ String className =
                         * tablerel.getClassName().substring(29).toLowerCase();//strip working
                         * set Object tableObject2 = genericDBObject2(className, num); return
                         * tableObject2; }
                         */// else{
                        // if(compareMe.getName() == "referenceWork"){File path = new
                        // File(importFolderPath+"ReferenceWork"+".xml");
                        try
                        {// if a generic exsists
                            File path = new File(importFolderPath
                                    + capFirstChar(compareMe.getName()) + "Generic.xml"); //$NON-NLS-1$
                            Element dbImport2 = XMLHelper.readFileToDOM4J(path);
                            Object tableObject = dynamicXMLImportRecReturn(dbImport2,
                                    capFirstChar(compareMe.getName()));
                            return tableObject;
                        } catch (FileNotFoundException e)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, e);
                            // normal
                            String className = tablerel.getClassName().substring(29)
                                    .toLowerCase();// strip working set
                            Object tableObject = genericDBObject2(className, num);
                            return tableObject;
                        }
                        // }else{
                        /*
                         * Element dbImport = XMLHelper.readFileToDOM4J(path); Object
                         * tableObject = dynamicXMLImportRecReturn(dbImport,
                         * capFirstChar(compareMe.getName())); return tableObject;
                         */
                        // }
                        // check if its a collection (one-to-many)
                    } else if ((tablerel != null && tablerel.getType().name() == "OneToMany") //$NON-NLS-1$
                            || (tablerel != null && tablerel.getType().name() == "ManyToMany")) //$NON-NLS-1$
                    {
                        // if many-to-many
                        if (compareMe.getName().equals(parentName + "s")) { return "ManyToMany"; } //$NON-NLS-1$ //$NON-NLS-2$
                        // else one-to-many
                        return "OneToMany"; //$NON-NLS-1$
                    } else
                    {
                        System.err.println("could not import element: " + compareMe.getName() //$NON-NLS-1$
                                + ", with data:" + compareMe.getData()); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return null;
    }

    // this method loads parents from the data base
    protected Object findTypeSequential(Element compareMe,
                                        String dbTable,
                                        long parentId,
                                        String parentName)
    {
        try
        { // get the fieldinfo
            String lowerdbTable = dbTable.toLowerCase();
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
            /*
             * List<DBTableIdMgr.FieldInfo> list = info.getFields();
             * //System.err.println(compareMe.getName()); for( DBTableIdMgr.FieldInfo i: list){
             * System.out.println(i.getName()); }
             */
            // find element with compareme.getName()
            DBFieldInfo fieldInfo = info.getFieldByName(compareMe.getName());
            // if the element is an id, ignore it
            if (!compareMe.getName().equals(lowerFirstChar(dbTable) + "Id")) //$NON-NLS-1$
            {
                // if it is a normal field
                if (fieldInfo != null)
                {
                    String type = fieldInfo.getType();
                    // check the type
                    if (type.equals("java.lang.String") || type.equals("text")) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        return compareMe.getStringValue();

                    } else if (type.equals("java.util.Date")) //$NON-NLS-1$
                    {
                        Date dtTmp = new SimpleDateFormat("yy-MM-dd H:mm:ss").parse(compareMe //$NON-NLS-1$
                                .getStringValue());
                        return dtTmp;

                    } else if (type.equals("java.sql.Timestamp")) //$NON-NLS-1$
                    {
                        Timestamp tmstmp = Timestamp.valueOf(compareMe.getStringValue());
                        return tmstmp;
                    } else if (type.equals("java.lang.Integer")) //$NON-NLS-1$
                    {
                        int num = new Integer(compareMe.getStringValue()).intValue();
                        return num;

                    } else if (type.equals("java.lang.Boolean")) //$NON-NLS-1$
                    {
                        Boolean bool = Boolean.valueOf(compareMe.getStringValue());
                        return bool;
                    } else if (type.equals("java.math.BigDecimal")) //$NON-NLS-1$
                    {
                        BigDecimal num = new BigDecimal(compareMe.getStringValue());
                        return num;
                    } else if (type.equals("java.lang.Double")) //$NON-NLS-1$
                    {
                        double num = new Double(compareMe.getStringValue()).doubleValue();
                        return num;
                    } else if (type.equals("java.lang.Float")) //$NON-NLS-1$
                    {
                        float num = new Float(compareMe.getStringValue()).floatValue();
                        return num;
                    } else if (type.equals("java.lang.Long")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();
                        return num;
                    } else if (type.equals("java.lang.Short")) //$NON-NLS-1$
                    {
                        short num = new Short(compareMe.getStringValue()).shortValue();
                        return num;
                    } else if (type.equals("java.lang.Byte")) //$NON-NLS-1$
                    {
                        byte num = new Byte(compareMe.getStringValue()).byteValue();
                        return num;
                    } else if (type.equals("java.util.Calendar")) //$NON-NLS-1$
                    {
                        Calendar date = dateString2Calendar(compareMe.getStringValue());
                        return date;
                    }
                } else
                // check if it is a many-to-one
                {
                    DBRelationshipInfo tablerel = info.getRelationshipByName(compareMe.getName());
                    if (tablerel != null && tablerel.getType().name() == "ManyToOne") //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();

                        if (compareMe.getName().equals(lowerFirstChar(parentName)))// if they
                                                                                    // equal, load
                                                                                    // the parent
                                                                                    // from the
                                                                                    // database
                        {
                            String className = tablerel.getClassName().substring(29).toLowerCase();// strip
                                                                                                    // working
                                                                                                    // set
                            Object tableObject = genericDBObject2(className, parentId);
                            return tableObject;
                        }
                        // else check if the xml file is there,
                        // if so run it
                        // otherwise make a generic thing
                        // some things cant be generic
                        // also check the order of how buildsampledb does it ^.^
                        // a generic map
                        // get the classtype
                        /*
                         * if(compareMe.getName() == "deaccessionPreparation"){ String className =
                         * tablerel.getClassName().substring(29).toLowerCase();//strip working
                         * set Object tableObject2 = genericDBObject2(className, num); return
                         * tableObject2; }
                         */// else{
                        // if(compareMe.getName() == "referenceWork"){File path = new
                        // File(importFolderPath+"ReferenceWork"+".xml");
                        try
                        {// if a generic exsists
                            File path = new File(importFolderPath
                                    + capFirstChar(compareMe.getName()) + "Generic.xml"); //$NON-NLS-1$
                            Element dbImport2 = XMLHelper.readFileToDOM4J(path);
                            Object tableObject = dynamicXMLImportRecReturn(dbImport2,
                                    capFirstChar(compareMe.getName()));
                            return tableObject;
                            
                        } catch (FileNotFoundException e)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, e);
                            // normal
                            String className = tablerel.getClassName().substring(29)
                                    .toLowerCase();// strip working set
                            Object tableObject = genericDBObject2(className, num);
                            return tableObject;
                        }
                        // }else{
                        /*
                         * Element dbImport = XMLHelper.readFileToDOM4J(path); Object
                         * tableObject = dynamicXMLImportRecReturn(dbImport,
                         * capFirstChar(compareMe.getName())); return tableObject;
                         */
                        // }
                        // check if its a collection (one-to-many)
                    } else if ((tablerel != null && tablerel.getType().name() == "OneToMany") //$NON-NLS-1$
                            || (tablerel != null && tablerel.getType().name() == "ManyToMany")) //$NON-NLS-1$
                    {
                        // if many-to-many
                        if (compareMe.getName().equals(parentName + "s")) { return "ManyToMany"; } //$NON-NLS-1$ //$NON-NLS-2$
                        // else one-to-many
                        return "OneToMany"; //$NON-NLS-1$
                    } else
                    {
                        System.err.println("could not import element: " + compareMe.getName() //$NON-NLS-1$
                                + ", with data:" + compareMe.getData()); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return null;
    }

    // this method adds parents to the database instead of loading them
    protected Object findTypeRecordSet(Element compareMe,
                                       String dbTable,
                                       long parentId,
                                       String parentName)
    {
        try
        { // get the fieldinfo
            String lowerdbTable = dbTable.toLowerCase();
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);

            // find element with compareme.getName()
            DBFieldInfo fieldInfo = info.getFieldByName(compareMe.getName());
            // if the element is an id, ignore it
            // TODO: shouldl check for primary key
            if (!compareMe.getName().equals(lowerFirstChar(dbTable) + "Id")) //$NON-NLS-1$
            {
                // if it is a normal field
                if (fieldInfo != null)
                {
                    String type = fieldInfo.getType();
                    // check the type
                    if (type.equals("java.lang.String") || type.equals("text")) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        return compareMe.getStringValue();

                    } else if (type.equals("java.util.Date")) //$NON-NLS-1$
                    {
                        Date dtTmp = new SimpleDateFormat("yy-MM-dd H:mm:ss").parse(compareMe //$NON-NLS-1$
                                .getStringValue());
                        return dtTmp;

                    } else if (type.equals("java.sql.Timestamp")) //$NON-NLS-1$
                    {
                        Timestamp tmstmp = Timestamp.valueOf(compareMe.getStringValue());
                        return tmstmp;
                    } else if (type.equals("java.lang.Integer")) //$NON-NLS-1$
                    {
                        int num = new Integer(compareMe.getStringValue()).intValue();
                        return num;

                    } else if (type.equals("java.lang.Boolean")) //$NON-NLS-1$
                    {
                        Boolean bool = Boolean.valueOf(compareMe.getStringValue());
                        return bool;
                    } else if (type.equals("java.math.BigDecimal")) //$NON-NLS-1$
                    {
                        BigDecimal num = new BigDecimal(compareMe.getStringValue());
                        return num;
                    } else if (type.equals("java.lang.Double")) //$NON-NLS-1$
                    {
                        double num = new Double(compareMe.getStringValue()).doubleValue();
                        return num;
                    } else if (type.equals("java.lang.Float")) //$NON-NLS-1$
                    {
                        float num = new Float(compareMe.getStringValue()).floatValue();
                        return num;
                    } else if (type.equals("java.lang.Long")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();
                        return num;
                    } else if (type.equals("java.lang.Short")) //$NON-NLS-1$
                    {
                        short num = new Short(compareMe.getStringValue()).shortValue();
                        return num;
                    } else if (type.equals("java.lang.Byte")) //$NON-NLS-1$
                    {
                        byte num = new Byte(compareMe.getStringValue()).byteValue();
                        return num;
                    } else if (type.equals("java.util.Calendar")) //$NON-NLS-1$
                    {
                        Calendar date = dateString2Calendar(compareMe.getStringValue());
                        return date;
                    }
                } else
                // check if it is a many-to-one
                {
                    DBRelationshipInfo tablerel = info.getRelationshipByName(compareMe.getName());
                    // check for many to one, and make sure it has a value
                    if (tablerel != null && tablerel.getType().name() == "ManyToOne" //$NON-NLS-1$
                            && !compareMe.getStringValue().equals("")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();

                        String className = tablerel.getClassName().substring(29);// strip working
                                                                                    // set
                        // TODO: remove this condition for agent
                        if (className.equals("Agent")) //$NON-NLS-1$
                        {
                            className = className.toLowerCase();
                            Object tableObject = genericDBObject2(className, num);
                            return tableObject;
                        }
                        Object tableObject = getParentDBObject(className, num);
                        return tableObject;
                        // check if its a collection (one-to-many)
                    } else if ((tablerel != null && tablerel.getType().name() == "OneToMany") //$NON-NLS-1$
                            || (tablerel != null && tablerel.getType().name() == "ManyToMany")) //$NON-NLS-1$
                    {
                        // if many-to-many
                        if (compareMe.getName().equals(parentName + "s")) { return "ManyToMany"; } //$NON-NLS-1$ //$NON-NLS-2$
                        // else one-to-many
                        return "OneToMany"; //$NON-NLS-1$
                    } else
                    {
                        System.err.println("could not import element: " + compareMe.getName() //$NON-NLS-1$
                                + ", with data:" + compareMe.getData()); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            if (ex.toString().startsWith("java.lang.NumberFormatException")) { return null; } //$NON-NLS-1$
            // else
            ex.printStackTrace();
        }
        return null;
    }

    // this method adds parents to the database instead of loading them
    protected Object findTypeDataBase(Element compareMe,
                                      String dbTable,
                                      long parentId,
                                      String parentName)
    {
        try
        { // get the fieldinfo
            String lowerdbTable = dbTable.toLowerCase();
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);

            // find element with compareme.getName()
            DBFieldInfo fieldInfo = info.getFieldByName(compareMe.getName());
            // if the element is an id, ignore it
            // TODO: shouldl check for primary key
            if (!compareMe.getName().equals(lowerFirstChar(dbTable) + "Id")) //$NON-NLS-1$
            {
                // if it is a normal field
                if (fieldInfo != null)
                {
                    String type = fieldInfo.getType();
                    // check the type
                    if (type.equals("java.lang.String") || type.equals("text")) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        return compareMe.getStringValue();

                    } else if (type.equals("java.util.Date")) //$NON-NLS-1$
                    {
                        Date dtTmp = new SimpleDateFormat("yy-MM-dd H:mm:ss").parse(compareMe //$NON-NLS-1$
                                .getStringValue());
                        return dtTmp;

                    } else if (type.equals("java.sql.Timestamp")) //$NON-NLS-1$
                    {
                        Timestamp tmstmp = Timestamp.valueOf(compareMe.getStringValue());
                        return tmstmp;
                    } else if (type.equals("java.lang.Integer")) //$NON-NLS-1$
                    {
                        int num = new Integer(compareMe.getStringValue()).intValue();
                        return num;

                    } else if (type.equals("java.lang.Boolean")) //$NON-NLS-1$
                    {
                        Boolean bool = Boolean.valueOf(compareMe.getStringValue());
                        return bool;
                    } else if (type.equals("java.math.BigDecimal")) //$NON-NLS-1$
                    {
                        BigDecimal num = new BigDecimal(compareMe.getStringValue());
                        return num;
                    } else if (type.equals("java.lang.Double")) //$NON-NLS-1$
                    {
                        double num = new Double(compareMe.getStringValue()).doubleValue();
                        return num;
                    } else if (type.equals("java.lang.Float")) //$NON-NLS-1$
                    {
                        float num = new Float(compareMe.getStringValue()).floatValue();
                        return num;
                    } else if (type.equals("java.lang.Long")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();
                        return num;
                    } else if (type.equals("java.lang.Short")) //$NON-NLS-1$
                    {
                        short num = new Short(compareMe.getStringValue()).shortValue();
                        return num;
                    } else if (type.equals("java.lang.Byte")) //$NON-NLS-1$
                    {
                        byte num = new Byte(compareMe.getStringValue()).byteValue();
                        return num;
                    } else if (type.equals("java.util.Calendar")) //$NON-NLS-1$
                    {
                        Calendar date = dateString2Calendar(compareMe.getStringValue());
                        return date;
                    }
                } else
                // check if it is a many-to-one
                {
                    DBRelationshipInfo tablerel = info.getRelationshipByName(compareMe.getName());
                    // check for many to one, and make sure it has a value
                    if (tablerel != null && tablerel.getType().name() == "ManyToOne" //$NON-NLS-1$
                            && !compareMe.getStringValue().equals("")) //$NON-NLS-1$
                    {
                        long num = new Long(compareMe.getStringValue()).longValue();

                        String className = tablerel.getClassName().substring(29);// strip working
                                                                                    // set
                        // TODO: remove this condition for agent
                        if (className.equals("Agent")) //$NON-NLS-1$
                        {
                            className = className.toLowerCase();
                            Object tableObject = genericDBObject2(className, num);
                            return tableObject;
                        } else
                        {
                            Object tableObject = loadOrCreateParentDataBaseObject(className, num);
                            return tableObject;
                        }
                        // check if its a collection (one-to-many)
                    } else if ((tablerel != null && tablerel.getType().name() == "OneToMany") //$NON-NLS-1$
                            || (tablerel != null && tablerel.getType().name() == "ManyToMany")) //$NON-NLS-1$
                    {
                        // if many-to-many
                        if (compareMe.getName().equals(parentName + "s")) { return "ManyToMany"; } //$NON-NLS-1$ //$NON-NLS-2$
                        // else one-to-many
                        return "OneToMany"; //$NON-NLS-1$
                    } else
                    {
                        System.err.println("could not import element: " + compareMe.getName() //$NON-NLS-1$
                                + ", with data:" + compareMe.getData()); //$NON-NLS-1$
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            if (ex.toString().startsWith("java.lang.NumberFormatException")) { return null; } //$NON-NLS-1$
            // else
            ex.printStackTrace();
        }
        return null;
    }

    // TODO: replace initial check for id
    // this method adds parents to the database instead of loading them
    protected Object findTypeDataBaseParent(Element compareMe,
                                            String dbTable,
                                            long parentId,
                                            String parentName)
    {
        try
        { // get the fieldinfo
            String lowerdbTable = dbTable.toLowerCase();
            DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
            String primaryKey = info.getPrimaryKeyName();

            // find element with compareme.getName()
            DBFieldInfo fieldInfo = info.getFieldByName(compareMe.getName());
            // if the element is an id, ignore it

            // if( !compareMe.getName().equals( lowerFirstChar(dbTable) + "Id") )
            // {
            // if(!primaryKey.equals(compareMe.getName()))
            // {
            // if it is a normal field
            if (fieldInfo != null || primaryKey.equals(compareMe.getName()))
            // if(fieldInfo != null )
            {
                String type = new String();
                if (fieldInfo == null)
                {// it is an id
                    type = "java.lang.Integer"; //$NON-NLS-1$
                    System.out.println(compareMe.getStringValue());
                } else
                {
                    type = fieldInfo.getType();
                }

                // check the type
                if (type.equals("java.lang.String") || type.equals("text")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    return compareMe.getStringValue();

                } else if (type.equals("java.util.Date")) //$NON-NLS-1$
                {
                    Date dtTmp = new SimpleDateFormat("yy-MM-dd H:mm:ss").parse(compareMe //$NON-NLS-1$
                            .getStringValue());
                    return dtTmp;

                } else if (type.equals("java.sql.Timestamp")) //$NON-NLS-1$
                {
                    Timestamp tmstmp = Timestamp.valueOf(compareMe.getStringValue());
                    return tmstmp;
                } else if (type.equals("java.lang.Integer")) //$NON-NLS-1$
                {
                    int num = new Integer(compareMe.getStringValue()).intValue();
                    return num;

                } else if (type.equals("java.lang.Boolean")) //$NON-NLS-1$
                {
                    Boolean bool = Boolean.valueOf(compareMe.getStringValue());
                    return bool;
                } else if (type.equals("java.math.BigDecimal")) //$NON-NLS-1$
                {
                    BigDecimal num = new BigDecimal(compareMe.getStringValue());
                    return num;
                } else if (type.equals("java.lang.Double")) //$NON-NLS-1$
                {
                    double num = new Double(compareMe.getStringValue()).doubleValue();
                    return num;
                } else if (type.equals("java.lang.Float")) //$NON-NLS-1$
                {
                    float num = new Float(compareMe.getStringValue()).floatValue();
                    return num;
                } else if (type.equals("java.lang.Long")) //$NON-NLS-1$
                {
                    long num = new Long(compareMe.getStringValue()).longValue();
                    return num;
                } else if (type.equals("java.lang.Short")) //$NON-NLS-1$
                {
                    short num = new Short(compareMe.getStringValue()).shortValue();
                    return num;
                } else if (type.equals("java.lang.Byte")) //$NON-NLS-1$
                {
                    byte num = new Byte(compareMe.getStringValue()).byteValue();
                    return num;
                } else if (type.equals("java.util.Calendar")) //$NON-NLS-1$
                {
                    Calendar date = dateString2Calendar(compareMe.getStringValue());
                    return date;
                }
            } else
            // check if it is a many-to-one
            {
                DBRelationshipInfo tablerel = info.getRelationshipByName(compareMe.getName());
                // check for many to one, and make sure it has a value
                if (tablerel != null && tablerel.getType().name() == "ManyToOne" //$NON-NLS-1$
                        && !compareMe.getStringValue().equals("")) //$NON-NLS-1$
                {
                    long num = new Long(compareMe.getStringValue()).longValue();

                    String className = tablerel.getClassName().substring(29);// strip working set
                    // TODO: remove this condition for agent
                    if (className.equals("Agent")) //$NON-NLS-1$
                    {
                        className = className.toLowerCase();
                        Object tableObject = genericDBObject2(className, num);
                        return tableObject;
                    } else
                    {
                        Object tableObject = loadOrCreateParentDataBaseObject(className, num);
                        return tableObject;
                    }
                    // check if its a collection (one-to-many)
                } else if ((tablerel != null && tablerel.getType().name() == "OneToMany") //$NON-NLS-1$
                        || (tablerel != null && tablerel.getType().name() == "ManyToMany")) //$NON-NLS-1$
                {
                    // if many-to-many
                    if (compareMe.getName().equals(parentName + "s")) { return "ManyToMany"; } //$NON-NLS-1$ //$NON-NLS-2$
                    // else one-to-many
                    return "OneToMany"; //$NON-NLS-1$
                } else
                {
                    System.err.println("could not import element: " + compareMe.getName() //$NON-NLS-1$
                            + ", with data:" + compareMe.getData()); //$NON-NLS-1$
                }
            }
            // }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            if (ex.toString().startsWith("java.lang.NumberFormatException")) { return null; } //$NON-NLS-1$
            // else
            ex.printStackTrace();
        }
        return null;
    }

    // TODO: delete
    // if the databas object already exisits, then it will add to the exsisting otherwise
    // it will make a new generic database object
    protected Object genericDBObject(String className, long id)
    {

        // use the classtype to get the tableinfo
        DBTableInfo manyToOneInfo = DBTableIdMgr.getInstance().getInfoByTableName(className);
        // make an object from the tableinfo
        Object tableObject = new Object();
        try
        {
            tableObject = manyToOneInfo.getClassObj().newInstance();
            // make a map for the object
            Map relationMap = new HashMap();
            Timestamp tmstmp = Timestamp.valueOf("9999-08-22 10:09:05"); //$NON-NLS-1$
            relationMap.put(className.concat("Id"), id); //$NON-NLS-1$
            relationMap.put("timestampCreated", tmstmp); //$NON-NLS-1$
            relationMap.put("timestampModified", tmstmp); //$NON-NLS-1$
            // populate the object
            BeanUtils.populate(tableObject, relationMap);

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);

            ex.printStackTrace();
        }
        // return the object
        return tableObject;
    }

    // add to exsisting database, or use a Generic definition and make a new object
    protected Object genericDBObject2(String className, long id)
    {
        int num = (int) id;
        // use the classtype to get the tableinfo
        DBTableInfo manyToOneInfo = DBTableIdMgr.getInstance().getInfoByTableName(className);
        // make an object from the tableinfo
        Object tableObject2 = new Object();
        Object temp = new Object();
        try
        {
            tableObject2 = manyToOneInfo.getClassObj().newInstance();

            temp = session.load(tableObject2.getClass(), num);

            // just retun the object
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);

            if (ex.toString().startsWith("org.hibernate.NonUniqueObjectException")) //$NON-NLS-1$
            {

                ex.printStackTrace();
                // return tableObject2;
            } else if (ex.toString().startsWith("org.hibernate.ObjectNotFoundException")) //$NON-NLS-1$
            {// just return
                tableObject2 = genericDBObject(className, id);
                return tableObject2;
            } else
            {
                ex.printStackTrace();
            }
        }
        // return the object
        return temp;
    }

    // return the new parent object
    protected Object getParentDBObject(String className, long id)
    {
        int num = (int) id;
        // make an object from the tableinfo
        Object dbObject = new Object();
        try
        {
            dbObject = buildSingleDBObject(className, num, false);
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        // return the object
        return dbObject;
    }

    // return the new parent object, but first try and load it from the db
    protected Object loadOrCreateParentDataBaseObject(String className, long id)
    {
        int num = (int) id;
        // try and load first
        // use the classtype to get the tableinfo
        String name = className.toLowerCase();
        DBTableInfo manyToOneInfo = DBTableIdMgr.getInstance().getInfoByTableName(name);
        String primaryKey = manyToOneInfo.getPrimaryKeyName();

        // make an object from the tableinfo
        Object dbObject = new Object();
        Object temp = new Object();

        try
        {
            temp = manyToOneInfo.getClassObj().newInstance();

            dbObject = session.load(temp.getClass(), num);
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            if (ex.toString().startsWith("org.hibernate.ObjectNotFoundException")) //$NON-NLS-1$
            {// create new parent
                dbObject = buildSingleDataBaseObject(className, num, false);
                // load it
                long newParentId = new Long(session.getIdentifier(dbObject).toString()).longValue();
                int newParentIdInt = (int) newParentId;
                session.evict(dbObject);
                // if the new id equals the expected id, we got lucky.
                // otherwise, change the newly inserted dbobjects id to what the child expects
                if (!(num == newParentId))
                {
                    try
                    {
                        // get the requiredFields using the classname, then make a query string from
                        // them
                        String requiredFields = constructSQLString(getRequiredFields(className));
                        String query = "INSERT INTO " + className.toLowerCase() + " (" + primaryKey //$NON-NLS-1$ //$NON-NLS-2$
                                + ") VALUES (" + newParentIdInt + ") ON DUPLICATE KEY UPDATE " //$NON-NLS-1$ //$NON-NLS-2$
                                + primaryKey + "=" + num; //$NON-NLS-1$

                        if (StringUtils.isNotEmpty(requiredFields))
                        {
                            // replace the new id with the exppected one
                            session.createSQLQuery(query).executeUpdate();
                        } else
                        {
                            // replace the new id with the expected one
                            query = query.concat(requiredFields);
                            session.createSQLQuery(query).executeUpdate();
                        }

                        Object loaded = session.load(temp.getClass(), num);

                        return loaded;
                    } catch (Exception ex2)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex2);
                        ex2.printStackTrace();
                    }
                }
            } else
            {
                ex.printStackTrace();
            }
        }
        // return the object
        return dbObject;
    }

    // TODO: make more effiecnt by not adding the parents that don't exsist
    // also ignore agent for now
    public void writeRecordSet(String dbTable, int id)
    {
        List<String> children = new ArrayList();
        List<String> parents = new ArrayList();
        List<String> parentsChildren = new ArrayList();

        // get the children and remove the duplicates
        children = getChildTables(dbTable, children);// removeDuplicates(getChildTables(dbTable,
                                                        // children));
        // get parents
        parents = getParentTables(dbTable, parents, false);
        // get children of the parents
        for (int i = 0; i < parents.size(); i++)
        {
            String db = parents.get(i).toString();
            if (!db.equals("Agent")) //$NON-NLS-1$
                parentsChildren = getChildTables(db, parentsChildren);
        }
        parentsChildren = removeDuplicates(parentsChildren);

        // write files
        // write all children
        for (int i = 0; i < children.size(); i++)
            writeXMLfile(children.get(i));

        // write all parents
        for (int i = 0; i < parents.size(); i++)
        {
            // make sure the file has not already been written
            if (!children.contains(parents.get(i)))
            {
                writeXMLfile(parents.get(i));
            }
        }

        // write all the children of the parents
        for (int i = 0; i < parentsChildren.size(); i++)
        {
            String pChild = parentsChildren.get(i);
            // make sure the file has not already been written
            if (!children.contains(pChild) || !parents.contains(pChild))
            {
                writeXMLfile(pChild);
            }
        }

        writeSingleRecordXML(dbTable, id);
    }

    // get all assocated child tables for the record
    // ignore agent
    private List<String> getChildTables(String dbTable, List<String> children)
    {
        String lowerdbTable = dbTable.toLowerCase();
        DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
        // get all assocaited tables
        try
        {
            List<DBRelationshipInfo> relatedTables = info.getRelationships();
            // iterate over all related tables
            Iterator i = relatedTables.iterator();
            do
            {// do for associated table
                DBRelationshipInfo table = (DBRelationshipInfo) i.next();
                String type = table.getType().name();
                String childName = table.getClassName().substring(29);
                // add the children
                if (!childName.equals("Agent") && !children.contains(childName) //$NON-NLS-1$
                        && (type.equals("OneToMany") || type.equals("OneToOne"))) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    children.add(childName);
                    // recurse and strip the working set(there should be a better way
                    children = getChildTables(childName, children);
                }
            } while (i.hasNext());
        } catch (java.lang.NullPointerException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            System.err.println("table: " + dbTable + " does not exsist"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return children;
    }

    // foundAgent default is false
    // TODO: optimize - dont allows multiple tables to be added
    private List<String> getParentTables(String dbTable, List<String> parents, Boolean foundAgent)
    {
        String lowerdbTable = dbTable.toLowerCase();
        DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
        try
        {
            // get all assocaited tables
            List<DBRelationshipInfo> relatedTables = info.getRelationships();
            // iterate over all related tables
            Iterator i = relatedTables.iterator();
            do
            {// do for associated table
                DBRelationshipInfo table = (DBRelationshipInfo) i.next();
                String type = table.getType().name();

                if (type.equals("ManyToOne") || type.equals("ManyToMany")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    // System.out.println(table.getClassName()+" "+table.getType());
                    String parentName = table.getClassName().substring(29);
                    // only want to add one agent
                    if (parentName.equals("agent") && foundAgent == false //$NON-NLS-1$
                            && !parents.contains(parentName))
                    {
                        foundAgent = true;
                        parents.add(parentName);
                        // recurse
                        parents = getParentTables(parentName, parents, foundAgent);
                    } else if (!parentName.equals("agent") && !parents.contains(parentName)) //$NON-NLS-1$
                    {
                        parents.add(parentName);
                        // recurse
                        parents = getParentTables(parentName, parents, foundAgent);
                    }
                }
            } while (i.hasNext());
        } catch (java.lang.NullPointerException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            System.err.println("table:" + dbTable + " does not exsist"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return parents;
    }

    // foundAgent default is false
    // TODO: optimize - dont allows multiple tables to be added
    private List<String> getImmediateParentTables(String dbTable,
                                                  List<String> parents,
                                                  Boolean foundAgent)
    {
        String lowerdbTable = dbTable.toLowerCase();
        DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(lowerdbTable);
        try
        {
            // get all assocaited tables
            List<DBRelationshipInfo> relatedTables = info.getRelationships();
            // iterate over all related tables
            Iterator i = relatedTables.iterator();
            do
            {// do for associated table
                DBRelationshipInfo table = (DBRelationshipInfo) i.next();
                String type = table.getType().name();

                if (type.equals("ManyToOne") || type.equals("ManyToMany")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    // System.out.println(table.getClassName()+" "+table.getType());
                    String parentName = table.getClassName().substring(29);
                    // only want to add one agent
                    if (parentName.equals("agent") && foundAgent == false //$NON-NLS-1$
                            && !parents.contains(parentName))
                    {
                        foundAgent = true;
                        parents.add(parentName);
                    } else if (!parentName.equals("agent") && !parents.contains(parentName)) //$NON-NLS-1$
                    {
                        parents.add(parentName);
                    }
                }
            } while (i.hasNext());
        } catch (java.lang.NullPointerException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            System.err.println("table: " + dbTable + " does not exsist"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return parents;
    }

    /**
     * print all the records of a table.
     * @param dbTable the class name of the table
     */
    public void printXML(String dbTable)
    {
        Session dom4jSession = session.getSession(EntityMode.DOM4J);
        String query = "from " + dbTable; //$NON-NLS-1$

        List userXML = dom4jSession.createQuery(query).list();
        try
        {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(System.out, format);

            for (int i = 0; i < userXML.size(); i++)
            {
                Element writeMe = (Element) userXML.get(i);
                writer.write(writeMe);
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        System.out.println();
    }

    /**
     * print all associated tables of this table
     * @param dbTable the class name of the table
     */
    public void printRecordSet(String dbTable)
    {
        List<String> children = new ArrayList<String>();
        List<String> parents = new ArrayList<String>();
        // get the children and remove the duplicates
        children = removeDuplicates(getChildTables(dbTable, children));
        parents = getParentTables(dbTable, parents, false);

        // write files
        System.out.println("----Parents------------"); //$NON-NLS-1$
        for (int i = 0; i < parents.size(); i++)
        {
            printXML(parents.get(i));
            System.out.println("-------------------\n"); //$NON-NLS-1$
        }
        System.out.println("----Children------------"); //$NON-NLS-1$
        for (int i = 0; i < children.size(); i++)
        {
            printXML(children.get(i));
            System.out.println("-------------------\n"); //$NON-NLS-1$
        }
    }

    /**
     * export all the tables in a database
     * 
     * @return creates xml files with name of the tables
     */
    // TODO: don't skip any tables
    public void exportTables()
    {
        Vector<DBTableInfo> allTables = DBTableIdMgr.getInstance().getTables();

        for (int i = 0; i < allTables.size(); i++)
        {
            String table = allTables.get(i).getClassName().substring(29);
            if (table.equals("DataType") || table.equals("Discipline") || table.equals("Division") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    || table.equals("GeographyTreeDef") || table.equals("GeographyTreeDefItem") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("GeologicTimePeriodTreeDef") //$NON-NLS-1$
                    || table.equals("GeologicTimePeriodTreeDefItem") || table.equals("Institution") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("LithoStratTreeDef") || table.equals("LithoStratTreeDefItem") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("SpecifyUser") || table.equals("SpLocaleBase") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("SpLocaleContainer") || table.equals("SpLocaleContainerItem") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("StorageTreeDef") || table.equals("StorageTreeDefItem") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("Taxon") || table.equals("TaxonTreeDef") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("TaxonTreeDefItem") || table.equals("TreatmentEvent") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("UserPermission") || table.equals("Workbench") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("WorkbenchRow") || table.equals("WorkbenchTemplate") //$NON-NLS-1$ //$NON-NLS-2$
                    || table.equals("WorkbenchTemplateMappingItem") //$NON-NLS-1$

            )
            {
                System.out.println("skipped: " + table); //$NON-NLS-1$
            } else
            {
                writeXMLfile(table);
            }
        }
        System.out.println("...done"); //$NON-NLS-1$
    }

    /**
     * print a single record
     * @param dbTable the class name of the table
     * @param id the id number of the record
     */
    public void printSingleRecordXML(String dbTable, int id)
    {
        Session dom4jSession = session.getSession(EntityMode.DOM4J);
        // load the object by using its primary key
        DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
        String primaryKey = info.getPrimaryKeyName();
        String query = "from " + dbTable + " where " + primaryKey + " = " + id; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        List userXML = dom4jSession.createQuery(query).list();

        try
        {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(System.out, format);

            for (int i = 0; i < userXML.size(); i++)
            {
                Element writeMe = (Element) userXML.get(i);
                writer.write(writeMe);
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        System.out.println();
    }

    /**
     * write a single record
     * @param dbTable the class name of the table
     * @param id the id number of the record
     */
    public void writeSingleRecordXML(String dbTable, int id)
    {
        FileOutputStream fout;

        Session dom4jSession = session.getSession(EntityMode.DOM4J);
        // load the object by using its primary key
        DBTableInfo info = DBTableIdMgr.getInstance().getInfoByTableName(dbTable.toLowerCase());
        String primaryKey = info.getPrimaryKeyName();
        String query = "from " + dbTable + " where " + primaryKey + " = " + id; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        List userXML = dom4jSession.createQuery(query).list();

        try
        {
            fout = new FileOutputStream(importFolderPath + dbTable + ".xml"); //$NON-NLS-1$
            PrintStream p = new PrintStream(fout);
            p.print("<root>"); //$NON-NLS-1$
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(fout, format);

            for (int i = 0; i < userXML.size(); i++)
            {
                Element writeMe = (Element) userXML.get(i);
                writer.write(writeMe);
            }
            p.println("\n</root>"); //$NON-NLS-1$
            p.close();
            fout.close();
            writer.close();
            System.out.println("Wrote: " + dbTable + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        System.out.println();
    }

    /**
     * write all the records of the given table.
     * @param dbTable the class name of the table
     * @return creates an xml file with name of the table
     */
    public void writeXMLfile(String dataBase)
    {
        FileOutputStream fout;

        Session dom4jSession = session.getSession(EntityMode.DOM4J);
        String query = "from " + dataBase + " where id = 1"; //$NON-NLS-1$ //$NON-NLS-2$
        
        System.out.println(query);
        
        List userXML = dom4jSession.createQuery(query).list();
        try
        {
            fout = new FileOutputStream(importFolderPath + dataBase + ".xml"); //$NON-NLS-1$
            PrintStream p = new PrintStream(fout);
            p.print("<root>"); //$NON-NLS-1$
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(fout, format);

            for (int i = 0; i < userXML.size(); i++)
            {
                Element writeMe = (Element) userXML.get(i);
                writer.write(writeMe);
            }
            p.println("\n</root>"); //$NON-NLS-1$
            p.close();
            fout.close();
            writer.close();
            System.out.println("Wrote: " + dataBase + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }

    }

    void close()
    {
        if (session.isOpen())
        {
            session.close();
        }
    }

    // capitalize the first character of the string
    public String capFirstChar(String text)
    {
        text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return text;
    }

    // lowercase the first character of the string
    public String lowerFirstChar(String text)
    {
        text = text.substring(0, 1).toLowerCase() + text.substring(1);
        return text;
    }

    // remove the last character of the string
    public String removeLastChar(String text)
    {
        text = text.substring(0, text.length() - 1);
        return text;
    }

    // get the database name from a list of elements
    // assume the list is not empty
    public String getDbName(List dbElements)
    {
        Element temp = (Element) dbElements.get(0);
        String dbName = temp.getName();
        return dbName;
    }

    public String getDbName(Vector dbElements)
    {
        Element temp = (Element) dbElements.get(0);
        String dbName = temp.getName();
        return dbName;
    }

    // taken from
    // http://64.233.167.104/search?q=cache:Iu0nrHp8jOIJ:forums.devx.com/showthread.php%3Ft%3D143327+java+string+to+calendar&hl=en&ct=clnk&cd=1&gl=us
    protected Calendar dateString2Calendar(String newstring)
    {
        SimpleDateFormat df = new SimpleDateFormat("dd MMMMMMMMM yyyy"); //$NON-NLS-1$
        Calendar cal = Calendar.getInstance();
        try
        {
            Date date = df.parse(newstring);
            cal.setTime(date);
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImportExportDB.class, ex);
            ex.printStackTrace();
        }
        return cal;
    }

    private List removeDuplicates(List list)
    {
        HashSet<List> set = new HashSet<List>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    public List<String> getRequiredFields(String className)
    {
        DBTableInfo dbTableInfo = DBTableIdMgr.getInstance().getInfoByTableName(
                className.toLowerCase());
        List<DBFieldInfo> fields = dbTableInfo.getFields();
        List<DBRelationshipInfo> relFields = dbTableInfo.getRelationships();
        ArrayList<String> requiredFields = new ArrayList<String>();

        for (int i = 0; i < fields.size(); i++)
        {
            if (fields.get(i).isRequired())
            {
                requiredFields.add(fields.get(i).getName());
            }
        }
        for (int i = 0; i < relFields.size(); i++)
        {
            if (relFields.get(i).isRequired())
            {
                String name = relFields.get(i).getClassName().substring(29) + "ID";//strip working set //$NON-NLS-1$
                //requiredFields.add(relFields.get(i).getName());   
                requiredFields.add(name);
            }
        }

        return requiredFields;
    }

    //a helper function to construct a query that changes a dbobjects id
    private String constructSQLString(List<String> requiredFields)
    {
        if (requiredFields.isEmpty() || requiredFields == null)
        {
            return null;

        } else
        {
            String query = new String();
            for (int i = 0; i < requiredFields.size(); i++)
            {
                String field = requiredFields.get(i);
                query = query.concat(", " + field + "=" + field); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return query;
        }
    }
}
