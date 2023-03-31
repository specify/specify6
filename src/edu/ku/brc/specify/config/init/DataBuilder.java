/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.config.init;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.auth.specify.policy.DatabaseService;
import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.AccessionAuthorization;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentVariant;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.Author;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAgent;
import edu.ku.brc.specify.datamodel.BorrowMaterial;
import edu.ku.brc.specify.datamodel.BorrowReturnMaterial;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectingTrip;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionObjectCitation;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Disposal;
import edu.ku.brc.specify.datamodel.DisposalAgent;
import edu.ku.brc.specify.datamodel.DisposalPreparation;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationCitation;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.GroupPerson;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgent;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityCitation;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.specify.datamodel.OtherIdentifier;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.Project;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonCitation;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.UserGroupScope;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.BaseTask;
import edu.ku.brc.specify.tasks.ExportMappingTask;
import edu.ku.brc.specify.tasks.PermissionOptionPersist;
import edu.ku.brc.util.Pair;

public class DataBuilder
{
    protected static Calendar startCal = Calendar.getInstance();
    protected static Session  session  = null;
    
    /** Maps usertype strings to the name of the default groups */
    protected static Map<String, Pair<String, Byte>> usertypeToDefaultGroup;
    

    /**
     * @return
     */
    public static Session getSession()
    {
        return session;
    }

    /**
     * @param session
     */
    public static void setSession(Session session)
    {
        DataBuilder.session = session;
    }

    public static AccessionAgent createAccessionAgent(Accession accession, Agent agent)
    {
        AccessionAgent accessionAgent = new AccessionAgent();

        Timestamp now = new Timestamp(System.currentTimeMillis());
        accessionAgent.setTimestampCreated(now);
        accessionAgent.setAccession(accession);
        accessionAgent.setAgent(agent);
        persist(accessionAgent);
        return accessionAgent;

    }

    public static AgentVariant createAgentVariant(final Byte varType, final String name, final Agent agentOwner)
    {
        AgentVariant av = new AgentVariant();
        av.initialize();
        av.setVarType(varType);
        av.setName(name);
        av.setAgent(agentOwner);
        agentOwner.getVariants().add(av);
        return av;
    }
    
    public static Institution createInstitution(final String name)
    {
        // Create Discipline
        Institution inst = new Institution();
        inst.initialize();
        inst.setName(name);

        persist(inst);
        return inst;
    }
    
    public static Division createDivision(final Institution inst, 
                                          final String discipline, 
                                          final String name, 
                                          final String abbrev, 
                                          final String title)
    {
        // Create Discipline
        Division division = new Division();
        division.initialize();
        division.setName(name);
        division.setDiscipline(discipline);
        division.setAbbrev(abbrev);
        division.setTitle(title);
        
        inst.addReference(division, "divisions");

        persist(division);
        return division;
    }
    
    public static GroupPerson createGroupPerson(final Agent group, 
                                                final Agent agent, 
                                                final int order,
                                                final Division division)
    {
        GroupPerson groupPerson = new GroupPerson();
        groupPerson.initialize();
        groupPerson.setOrderIndex(order);
        
        groupPerson.setDivision(division);
        
        if (true)
        {
            groupPerson.setMember(agent);
            groupPerson.setGroup(group);
            
            agent.getGroups().add(groupPerson);
            group.getMembers().add(groupPerson);
        } else
        {
            group.addReference(groupPerson, "groups");
            agent.addReference(groupPerson, "members");
        }
        
        /*System.out.println("Agent '"+agent.getLastName()+"' is in groups:");
        for (GroupPerson gp : agent.getGroups())
        {
            System.out.println("  Mem '"+gp.getGroup().getLastName()+"'  "+gp.getOrderIndex());
        }
        System.out.println("------------------------\nGroup '"+group.getLastName()+"'  has Agents:  ");
        for (GroupPerson gp : group.getMembers())
        {
            System.out.println("  Mem '"+gp.getMember().getLastName()+"'  "+gp.getOrderIndex());
        }*/
        return groupPerson;
    }
    
    /**
     * @param title
     * @param firstName
     * @param middleInit
     * @param lastName
     * @param abbreviation
     * @param email
     * @return
     */
    public static Agent createAgent(final String title,
                                    final String firstName,
                                    final String middleInit,
                                    final String lastName,
                                    final String abbreviation,
                                    final String email)
    {
        return createAgent(title, firstName, middleInit, lastName, abbreviation, email, null, null);
    }
    
    /**
     * @param title
     * @param firstName
     * @param middleInit
     * @param lastName
     * @param abbreviation
     * @param email
     * @param division
     * @param discipline
     * @return
     */
    public static Agent createAgent(final String title,
                                    final String firstName,
                                    final String middleInit,
                                    final String lastName,
                                    final String abbreviation,
                                    final String email,
                                    final Division division,
                                    final Discipline discipline)
    {
        // Create Discipline
        Agent agent = new Agent();
        agent.initialize();
        agent.setAgentType((byte) 1);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setMiddleInitial(middleInit);
        agent.setAbbreviation(abbreviation);
        agent.setTitle(title);
        agent.setEmail(email);
        
        if (division == null)
        {
            Division div = AppContextMgr.getInstance().getClassObject(Division.class);
            if (div == null)
            {
                throw new RuntimeException("Division is NULL!");
            }
            agent.setDivision(div);
        } else
        {
            agent.setDivision(division);
        }
        
        persist(agent);
        return agent;
    }
    
    /**
     * @param filename
     * @param mimeType
     * @return
     */
    public static Attachment createAttachment(final String filename,
                                              final String mimeType,
                                              final int tableId)
    {
        Attachment attachment = new Attachment();
        attachment.initialize();
        attachment.setOrigFilename(filename);
        attachment.setMimeType(mimeType);
        attachment.setTableId(tableId);
        persist(attachment);
        return attachment;
    }
    
    public static SpQuery createQuery(final String      name, 
                                      final String      contextName, 
                                      final int         contextTableId,
                                      final SpecifyUser owner,
                                      final Agent       agent)
    {
        SpQuery query = new SpQuery();
        query.initialize();
        query.setName(name);
        query.setContextName(contextName);
        query.setContextTableId((short)contextTableId);
        query.setCreatedByAgent(agent);
        query.setSpecifyUser(owner);
        return query;
        
    }
    
    public static CollectionRelType createCollectionRelType(final String name,
                                                            final Collection leftSideCollection,
                                                            final Collection rightSideCollection)
    {
        CollectionRelType crt = new CollectionRelType();
        crt.initialize();
        crt.setName(name);
        
        crt.setLeftSideCollection(leftSideCollection);
        crt.setRightSideCollection(rightSideCollection);
        rightSideCollection.getRightSideRelTypes().add(crt);
        leftSideCollection.getLeftSideRelTypes().add(crt);
        
        return crt;
    }
    
    public static SpQueryField createQueryField(final SpQuery query,
                                                final Short   position,
                                                final String  fieldName,
                                                final String  fieldAlias,
                                                final Boolean isNot,
                                                final Byte    operStart,
                                                final Byte    operEnd,
                                                final String  startValue,
                                                final String  endValue,
                                                final Byte    sortType,
                                                final Boolean isDisplay,
                                                final String  tableList,
                                                final int contextTableIdent)
    {
        SpQueryField field = new SpQueryField();
        field.initialize();
        field.setPosition(position);
        field.setFieldName(fieldName);
        field.setColumnAlias(fieldAlias); 
        field.setOperStart(operStart);
        field.setOperEnd(operEnd);
        field.setEndValue(endValue);
        field.setIsDisplay(isDisplay);
        field.setIsNot(isNot);
        field.setSortType(sortType);
        field.setStartValue(startValue);
        field.setTableList(tableList);
        field.setContextTableIdent(contextTableIdent);
        field.setIsPrompt(true);
        query.addReference(field, "fields");
        return field;
    }
                                              
    public static AttributeDef createAttributeDef(final AttributeIFace.FieldType type,
                                                  final String name,
                                                  final Discipline discipline,
                                                  final PrepType prepType)
    {
        AttributeDef attrDef = new AttributeDef();
        attrDef.initialize();
        attrDef.setDataType(type.getType());
        attrDef.setFieldName(name);
        attrDef.setPrepType(prepType);
        attrDef.setDiscipline(discipline);
        
        discipline.getAttributeDefs().add(attrDef);
        
        return attrDef;
    }

    /**
     * @param division
     * @param type
     * @param name
     * @param dataType
     * @param taxonTreeDef
     * @param geographyTreeDef
     * @param geologicTimePeriodTreeDef
     * @param lithoStratTreeDef
     * @return
     */
    public static Discipline createDiscipline(final Division         division,
                                              final String           type,
                                              final String           name,
                                              final DataType         dataType,
                                              final TaxonTreeDef     taxonTreeDef,
                                              final GeographyTreeDef geographyTreeDef,
                                              final GeologicTimePeriodTreeDef geologicTimePeriodTreeDef,
                                              final LithoStratTreeDef lithoStratTreeDef)
    {
        Discipline discipline = new Discipline();
        discipline.initialize();
        discipline.setType(type);
        discipline.setName(name);
        discipline.setDataType(dataType);
        discipline.setTaxonTreeDef(taxonTreeDef);
        discipline.setGeographyTreeDef(geographyTreeDef);//meg added to support not-null constraints
        discipline.setGeologicTimePeriodTreeDef(geologicTimePeriodTreeDef);//meg added to support not-null constraints
        discipline.setLithoStratTreeDef(lithoStratTreeDef);
        
        geographyTreeDef.getDisciplines().add(discipline);
        geologicTimePeriodTreeDef.getDisciplines().add(discipline);
        lithoStratTreeDef.getDisciplines().add(discipline);
        
        taxonTreeDef.setDiscipline(discipline);
        
        division.addReference(discipline, "disciplines");

        persist(discipline);
        
        return discipline;
    }

    /**
     * @param prefix
     * @param name
     * @param catalogNumberingScheme
     * @param disciplines
     * @param isEmbeddedCollectingEvent
     * @return
     */
    public static Collection createCollection(final String prefix,
                                              final String name,
                                              final String catalogNumFormatName,
                                              final AutoNumberingScheme catalogNumberingScheme,
                                              final Discipline[] disciplines,
                                              final boolean isEmbeddedCollectingEvent)
    {
        Collection collection = new Collection();
        collection.initialize();
        collection.setCatalogNumFormatName(catalogNumFormatName);
        collection.setCode(prefix);
        collection.setModifiedByAgent(null);
        collection.setCollectionName(name);
        collection.setIsEmbeddedCollectingEvent(isEmbeddedCollectingEvent);
        
        if (catalogNumberingScheme != null)
        {
            collection.getNumberingSchemes().add(catalogNumberingScheme);
            catalogNumberingScheme.getCollections().add(collection);
        }
        
        for (Discipline disp : disciplines)
        {
            collection.setDiscipline(disp);
        }

        persist(collection);
        
        if (catalogNumberingScheme != null)
        {
            persist(catalogNumberingScheme);
        }

        return collection;
    }

    /**
     * @param prefix
     * @param name
     * @param discipline
     * @return
     */
    public static Collection createCollection(final String prefix,
                                              final String name,
                                              final String catalogNumFormatName,
                                              final AutoNumberingScheme numberingScheme,
                                              final Discipline discipline)
    {
        return createCollection(prefix, name, catalogNumFormatName, numberingScheme, discipline, true);
    }

    /**
     * @param prefix
     * @param name
     * @param discipline
     * @return
     */
    public static Collection createCollection(final String prefix,
                                              final String name,
                                              final String catalogNumFormatName,
                                              final AutoNumberingScheme numberingScheme,
                                              final Discipline discipline,
                                              final boolean isEmbeddedCE)
    {
        return createCollection(prefix, name, catalogNumFormatName, numberingScheme, new Discipline[] { discipline }, isEmbeddedCE);
    }

    /**
     * @param locality
     * @param collectors
     * @return
     */
    public static CollectingEvent createCollectingEvent(final Locality locality, 
                                                        final Calendar startDate,
                                                        final String stationFieldNumber,
                                                        final Collector[] collectors)
    {
        CollectingEvent colEv = new CollectingEvent();
        colEv.initialize();

        colEv.setStartDate(startDate);
        colEv.setStationFieldNumber(stationFieldNumber);
        
        HashSet<Collector> collectorSet = new HashSet<Collector>();
        if (collectors != null)
        {
            for (Collector c: collectors)
            {
                c.setCollectingEvent(colEv);
                collectorSet.add(c);
            }
        }
        colEv.setCollectors(collectorSet);
        colEv.setLocality(locality);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        colEv.setTimestampCreated(now);

        persist(colEv);
        persist(locality);
        return colEv;
    }
    
    public static CollectingTrip createCollectingTrip(final String collectingTripName,
                                                      final String remarks, 
                                                      final CollectingEvent[] events)
    {
        CollectingTrip trip = new CollectingTrip();
        trip.initialize();
        trip.setCollectingTripName(collectingTripName);
        trip.setRemarks(remarks);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        trip.setTimestampCreated(now);

        Calendar startDate = Calendar.getInstance();
        startDate.set(9999, 12, 31);
        Calendar endDate = Calendar.getInstance();
        endDate.set(1, 1, 1);
        String verStartDate = null;
        String verEndDate = null;
        
        for (CollectingEvent e: events)
        {
            if (e.getStartDate().before(startDate))
            {
                startDate = e.getStartDate();
                verStartDate = e.getStartDateVerbatim();
            }
            if (e.getEndDate().after(endDate))
            {
                endDate = e.getEndDate();
                verEndDate = e.getEndDateVerbatim();
            }
            trip.getCollectingEvents().add(e);
        }
        
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setStartDateVerbatim(verStartDate);
        trip.setEndDateVerbatim(verEndDate);
        
        return trip;
    }

    public static CollectingEventAttr createCollectingEventAttr(final CollectingEvent colEv,
                                                                final AttributeDef colObjAttrDef,
                                                                final String strVal,
                                                                final Double dblVal)
    {
        // Create CollectionObjectAttr
        CollectingEventAttr colEvAttr = new CollectingEventAttr();
        colEvAttr.initialize();

        colEvAttr.setDefinition(colObjAttrDef);
        colObjAttrDef.getCollectingEventAttrs().add(colEvAttr);
        
        colEvAttr.setCollectingEvent(colEv);
        if (strVal != null)
        {
            colEvAttr.setStrValue(strVal);
        }
        if (dblVal != null)
        {
            colEvAttr.setDblValue(dblVal);
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        colEvAttr.setTimestampCreated(now);

        colEv.getAttrs().add(colEvAttr);

        persist(colEv);
        return colEvAttr;
    }

    public static CollectionObjectAttr createCollectionObjectAttr(final CollectionObject colObj,
                                                                  final AttributeDef colObjAttrDef,
                                                                  final String strVal,
                                                                  final Double dblVal)
    {
        // Create CollectionObjectAttr
        CollectionObjectAttr colObjAttr = new CollectionObjectAttr();
        colObjAttr.initialize();

        colObjAttr.setDefinition(colObjAttrDef);
        colObjAttr.setCollectionObject(colObj);
        if (strVal != null)
        {
            colObjAttr.setStrValue(strVal);
        }
        if (dblVal != null)
        {
            colObjAttr.setDblValue(dblVal);
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        colObjAttr.setTimestampCreated(now);

        colObj.getAttrs().add(colObjAttr);

        persist(colObj);
        return colObjAttr;
    }

    public static Collector createCollector(final Agent agent, int orderNum)
    {
        Collector collector = new Collector();
        collector.initialize();

        collector.setAgent(agent);
        collector.setModifiedByAgent(null);
        collector.setOrderNumber(orderNum);
        collector.setRemarks("");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        collector.setTimestampCreated(now);

        persist(collector);
        return collector;
    }


    public static CollectionObject createCollectionObject(final String catalogNumber,
                                                          final String fieldNumber,
                                                          final Agent cataloger,
                                                          final Collection collection,
                                                          final int count,
                                                          final CollectingEvent collectingEvent,
                                                          final Calendar catalogedDate,
                                                          @SuppressWarnings("unused") final String lastEditedBy)
    {
        return createCollectionObject(catalogNumber, fieldNumber, cataloger,
                                      collection, count, collectingEvent, catalogedDate, 
                                      UIFieldFormatterIFace.PartialDateEnum.Full, lastEditedBy);
    }
    
    public static CollectionObject createCollectionObject(final String catalogNumber,
                                                          final String fieldNumber,
                                                          final Agent cataloger,
                                                          final Collection collection,
                                                          final int count,
                                                          final CollectingEvent collectingEvent,
                                                          final Calendar catalogedDate,
                                                          final UIFieldFormatterIFace.PartialDateEnum dateType,
                                                          @SuppressWarnings("unused") final String lastEditedBy)
    {
        // Create Collection Object
        CollectionObject colObj = new CollectionObject();
        colObj.initialize();
        
        colObj.setCataloger(cataloger);
        colObj.setCatalogedDate(catalogedDate);
        colObj.setCatalogedDateVerbatim(DateFormat.getInstance().format(catalogedDate.getTime()));
        colObj.setCatalogNumber(catalogNumber);
        colObj.setCatalogedDatePrecision((byte)dateType.ordinal());
        colObj.setCollection(collection);
        colObj.setCollectingEvent(collectingEvent);
        colObj.setCountAmt(count);
        colObj.setFieldNumber(fieldNumber);
        colObj.setModifiedByAgent(cataloger);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        colObj.setTimestampCreated(now);

        if (collectingEvent != null)
        {
            collectingEvent.getCollectionObjects().add(colObj);
        }

        persist(colObj);
        if (collectingEvent != null)
        {
            persist(collectingEvent);
        }
        return colObj;
    }

    public static Determination createDetermination(final CollectionObject collectionObject,
                                                    final Agent determiner,
                                                    final Taxon taxon,
                                                    final boolean isCurrent,
                                                    final Calendar calendar)
    {
        // Create Determination
        Determination determination = new Determination();
        determination.initialize();

        determination.setIsCurrent(isCurrent);
        determination.setCollectionObject(collectionObject);
        determination.setDeterminedDate(calendar);
        determination.setDeterminer(determiner);
        determination.setTaxon(taxon);

        //status.getDeterminations().add(determination);
        collectionObject.getDeterminations().add(determination);
        //taxon.getDeterminations().add(determination);

        persist(collectionObject);
        persist(determination);
        persist(taxon);
        return determination;

    }

    /**
     * Create a <code>GeographyTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the geography tree def
     */
    public static GeographyTreeDef createGeographyTreeDef(final String name, final int treeDir)
    {
        GeographyTreeDef gtd = new GeographyTreeDef();
        gtd.initialize();
        gtd.setName(name);
        gtd.setFullNameDirection(treeDir);

        persist(gtd);
        return gtd;
    }

    /**
     * Creates a <code>GeographyTreeDefItem</code> using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param parent the parent node
     * @param gtd the associated definition object
     * @param name the name of the item
     * @param rankId the rank of the itme
     * @return the new item
     */
    @SuppressWarnings("unchecked")
    public static GeographyTreeDefItem createGeographyTreeDefItem(final GeographyTreeDefItem parent,
                                                                  final GeographyTreeDef gtd,
                                                                  final String name,
                                                                  final int rankId)
    {
        GeographyTreeDefItem gtdi = new GeographyTreeDefItem();
        gtdi.initialize();
        gtdi.setName(name);
        gtdi.setParent(parent);
        gtdi.setRankId(rankId);
        gtdi.setIsInFullName(false);
        gtdi.setIsEnforced(false);
        gtdi.setTreeDef(gtd);
        gtdi.setFullNameSeparator(", ");
        if (gtd != null)
        {
            gtd.getTreeDefItems().add(gtdi);
        }

        persist(gtdi);
        return gtdi;
    }

    /**
     * Creates a new <code>Geography</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param gtd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static Geography createGeography(final GeographyTreeDef gtd, final Geography parent,
    //final String abbrev,
                                            final String name,
                                            //final int highNode,
                                            //final int nodeNum,
                                            final int rankId)
    {
        Geography geography = new Geography();
        geography.initialize();
        geography.setDefinition(gtd);
        geography.setName(name);
        geography.setParent(parent);
        geography.setIsAccepted(true);
        geography.setIsCurrent(true);
        if (parent!=null)
        {
            parent.getChildren().add(geography);
        }
        geography.setRankId(rankId);
        GeographyTreeDefItem defItem = gtd.getDefItemByRank(rankId);
        if (defItem != null)
        {
            geography.setDefinitionItem(defItem);
        }
        gtd.getTreeEntries().add(geography);

        persist(geography);
        return geography;
    }
    
    public static List<Geography> createGeographyChildren(final GeographyTreeDef geoTreeDef,
                                                      final Geography        parent,
                                                      final String[]         childNames,
                                                      final int              rankId)
    {
        List<Geography> geos = new Vector<Geography>();
        for (int i=0;i<childNames.length;i++)
        {
            geos.add(createGeography(geoTreeDef, parent, childNames[i], rankId));
        }
        return geos;
    }


    /**
     * Create a <code>StorageTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the storage tree def
     */
    public static StorageTreeDef createStorageTreeDef(final String name, final int treeDir)
    {
        StorageTreeDef ltd = new StorageTreeDef();
        ltd.initialize();
        ltd.setName(name);
        ltd.setFullNameDirection(treeDir);

        persist(ltd);
        return ltd;
    }

    /**
     * Creates a <code>StorageTreeDefItem</code> using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param parent the parent node
     * @param ltd the associated definition object
     * @param name the name of the item
     * @param rankId the rank of the itme
     * @return the new item
     */
    @SuppressWarnings("unchecked")
    public static StorageTreeDefItem createStorageTreeDefItem(final StorageTreeDefItem parent,
                                                                final StorageTreeDef ltd,
                                                                final String name,
                                                                final int rankId)
    {
        StorageTreeDefItem ltdi = new StorageTreeDefItem();
        ltdi.initialize();
        ltdi.setName(name);
        ltdi.setParent(parent);
        ltdi.setRankId(rankId);
        ltdi.setTreeDef(ltd);
        ltdi.setIsInFullName(false);
        ltdi.setIsEnforced(false);
        ltdi.setFullNameSeparator(", ");
        if (ltd != null)
        {
            ltd.getTreeDefItems().add(ltdi);
        }

        persist(ltdi);
        return ltdi;
    }

    /**
     * Creates a new <code>Storage</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param ltd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static Storage createStorage(final StorageTreeDef ltd, final Storage parent,
    //final String abbrev,
                                          final String name,
                                          //final int highNode,
                                          //final int nodeNum,
                                          final int rankId)
    {
        Storage storage = new Storage();
        storage.initialize();
        storage.setDefinition(ltd);
        storage.setName(name);
        storage.setParent(parent);
        storage.setIsAccepted(true);
        if (parent!=null)
        {
            parent.getChildren().add(storage);
        }
        StorageTreeDefItem defItem = ltd.getDefItemByRank(rankId);
        if (defItem != null)
        {
            storage.setDefinitionItem(defItem);
        }

        storage.setRankId(rankId);
        ltd.getTreeEntries().add(storage);

        persist(storage);
        return storage;
    }

    /**
     * @param name
     * @param type
     * @param tableName
     * @param fieldName
     * @param formatter
     * @param readOnly
     * @param sizeLimit
     * @return
     */
    public static PickList createPickList(final String  name,
                                          final Byte    type,
                                          final String  tableName,
                                          final String  fieldName,
                                          final String  formatter,
                                          final boolean readOnly, 
                                          final int     sizeLimit,
                                          final Boolean isSystem,
                                          final Byte    sortType,
                                          final Collection collectionArg)
    {
        PickList pickList = new PickList();
        pickList.initialize();
        pickList.setName(name);
        pickList.setType(type);
        pickList.setTableName(tableName);
        pickList.setFieldName(fieldName);
        pickList.setFormatter(formatter);
        pickList.setReadOnly(readOnly);
        pickList.setSizeLimit(sizeLimit);
        pickList.setIsSystem(isSystem);
        pickList.setSortType(sortType);
        
        Collection collection = collectionArg != null ? collectionArg : AppContextMgr.getInstance().hasContext() ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;
        if (collection != null)
        {
            pickList.setCollection(collection);
            collection.getPickLists().add(pickList);
        }
        
        persist(pickList);
        return pickList;
    }
    
    /**
     * Create a <code>GeologicTimePeriodTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the geologic time period tree def
     */
    public static GeologicTimePeriodTreeDef createGeologicTimePeriodTreeDef(final String name, final int treeDir)
    {
        GeologicTimePeriodTreeDef gtp = new GeologicTimePeriodTreeDef();
        gtp.initialize();
        gtp.setName(name);
        gtp.setFullNameDirection(treeDir);

        persist(gtp);
        return gtp;
    }

    /**
     * Creates a <code>GeologicTimePeriodTreeDefItem</code> using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param parent the parent node
     * @param gtptd the associated definition object
     * @param name the name of the item
     * @param rankId the rank of the itme
     * @return the new item
     */
    @SuppressWarnings("unchecked")
    public static GeologicTimePeriodTreeDefItem createGeologicTimePeriodTreeDefItem(final GeologicTimePeriodTreeDefItem parent,
                                                                                    final GeologicTimePeriodTreeDef gtptd,
                                                                                    final String name,
                                                                                    final int rankId)
    {
        GeologicTimePeriodTreeDefItem gtdi = new GeologicTimePeriodTreeDefItem();
        gtdi.initialize();
        gtdi.setName(name);
        gtdi.setParent(parent);
        gtdi.setRankId(rankId);
        gtdi.setTreeDef(gtptd);
        gtdi.setIsInFullName(false);
        gtdi.setIsEnforced(false);
         gtdi.setFullNameSeparator(", ");
        if (gtptd != null)
        {
            gtptd.getTreeDefItems().add(gtdi);
        }

        persist(gtdi);
        return gtdi;
    }

    /**
     * Creates a new <code>GeologicTimePeriod</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param gtptd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static GeologicTimePeriod createGeologicTimePeriod(final GeologicTimePeriodTreeDef gtptd,
                                                              final GeologicTimePeriod parent,
                                                              final String name,
                                                              final BigDecimal startMYA,
                                                              final BigDecimal endMYA,
                                                              final int rankId)
    {
        GeologicTimePeriod gtp = new GeologicTimePeriod();
        gtp.initialize();
        gtp.setDefinition(gtptd);
        gtp.setDefinition(gtptd);
        gtp.setName(name);
        gtp.setParent(parent);
        gtp.setIsAccepted(true);
        if (parent!=null)
        {
            parent.getChildren().add(gtp);
        }
        gtp.setStartPeriod(startMYA);
        gtp.setStartUncertainty(BigDecimal.ZERO);
        gtp.setEndPeriod(endMYA);
        gtp.setEndUncertainty(BigDecimal.ZERO);
        GeologicTimePeriodTreeDefItem defItem = gtptd.getDefItemByRank(rankId);
        if (defItem != null)
        {
            gtp.setDefinitionItem(defItem);
        }
        gtp.setRankId(rankId);
        gtptd.getTreeEntries().add(gtp);
        persist(gtp);
        return gtp;
    }

    /**
     * Create a <code>TaxonTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the taxon tree def
     */
    public static TaxonTreeDef createTaxonTreeDef(final String name, final int treeDir)
    {
        TaxonTreeDef ttd = new TaxonTreeDef();
        ttd.initialize();
        ttd.setName(name);
        ttd.setFullNameDirection(treeDir);
        return ttd;
    }

    /**
     * Creates a <code>TaxonTreeDefItem</code> using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param parent the parent node
     * @param ttd the associated definition object
     * @param name the name of the item
     * @param rankId the rank of the itme
     * @return the new item
     */
    @SuppressWarnings("unchecked")
    public static TaxonTreeDefItem createTaxonTreeDefItem(final TaxonTreeDefItem parent,
                                                          final TaxonTreeDef ttd,
                                                          final String name,
                                                          final int rankId)
    {
        TaxonTreeDefItem ttdi = new TaxonTreeDefItem();
        ttdi.initialize();
        ttdi.setName(name);
        ttdi.setParent(parent);
        ttdi.setRankId(rankId);
        ttdi.setIsInFullName(false);
        ttdi.setIsEnforced(false);
        ttdi.setTreeDef(ttd);
        ttdi.setFullNameSeparator(" ");
        if (ttd != null)
        {
            ttd.getTreeDefItems().add(ttdi);
        }

        persist(ttdi);
        return ttdi;
    }
    
    /**
     * Create a <code>LithoStratTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the LithoStrat tree def
     */
    public static LithoStratTreeDef createLithoStratTreeDef(final String name)
    {
        LithoStratTreeDef lstd = new LithoStratTreeDef();
        lstd.initialize();
        lstd.setName(name);
        lstd.setFullNameDirection(TreeDefIface.FORWARD);
        return lstd;
    }

    public static LithoStratTreeDefItem createLithoStratTreeDefItem(final LithoStratTreeDefItem parent,
                                                                    final String name,
                                                                    final int rankId,
                                                                    final boolean inFullName)
    {
        if (parent != null)
        {
            LithoStratTreeDef treeDef = parent.getTreeDef();
            if (treeDef != null)
            {
                LithoStratTreeDefItem lstdi = new LithoStratTreeDefItem();
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
            throw new RuntimeException("LithoStratTreeDef is null!");
        }
        throw new RuntimeException("Parent is null!");
    }

    public static LithoStratTreeDefItem createLithoStratTreeDefItem(final LithoStratTreeDef treeDef,
                                                                    final String name,
                                                                    final int rankId,
                                                                    final boolean inFullName)
    {
        if (treeDef != null)
        {
            LithoStratTreeDefItem lstdi = new LithoStratTreeDefItem();
            lstdi.initialize();
            lstdi.setName(name);
            lstdi.setRankId(rankId);
            lstdi.setIsInFullName(inFullName);
            lstdi.setIsEnforced(false);
            lstdi.setTreeDef(treeDef);
            treeDef.getTreeDefItems().add(lstdi);
            return lstdi;
        }
        throw new RuntimeException("LithoStratTreeDef is null!");
    }



    /**
     * Creates a new <code>Taxon</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param ttd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static Taxon createTaxon(final TaxonTreeDef ttd, final Taxon parent, final String name, final int rankId)
    {
        return createTaxon(ttd, parent, name, null, rankId);
    }

    /**
     * Creates a new <code>Taxon</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param ttd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param commonName the common name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static Taxon createTaxon(final TaxonTreeDef ttd, final Taxon parent, final String name, final String commonName, final int rankId)
    {
        Taxon taxon = new Taxon();
        taxon.initialize();
        taxon.setDefinition(ttd);
        taxon.setName(name);
        taxon.setCommonName(commonName);
        taxon.setParent(parent);
        taxon.setIsAccepted(true);
        if (parent != null)
        {
            parent.getChildren().add(taxon);
        }
        TaxonTreeDefItem defItem = ttd.getDefItemByRank(rankId);
        if (defItem != null)
        {
            taxon.setDefinitionItem(defItem);
        }
        taxon.setRankId(rankId);
        ttd.getTreeEntries().add(taxon);

        persist(taxon);
        return taxon;
    }
    
    public static List<Object> createTaxonChildren(final TaxonTreeDef treeDef,
                                              final Taxon parent,
                                              final String[] childNames,
                                              final String[] commonNames,
                                              final int rankId)
    {
        Vector<Object> kids = new Vector<Object>();
        for (int i = 0; i < childNames.length; i++)
        {
            kids.add(createTaxon(treeDef, parent, childNames[i], commonNames[i], rankId));
        }
        return kids;
    }

    public static Preparation createPreparation(final PrepType prepType,
                                                final Agent preparedBy,
                                                final CollectionObject colObj,
                                                final Storage storage,
                                                final int count,
                                                final Calendar preparedDate)
    {
        Preparation prep = new Preparation();
        prep.initialize();

        prep.setCollectionObject(colObj);
        prep.setCountAmt(count);
        prep.setModifiedByAgent(null);
        prep.setStorage(storage);
        prep.setPreparedByAgent(preparedBy);
        prep.setPreparedDate(preparedDate);
        prep.setPrepType(prepType);
        prep.setRemarks(null);
        prep.setStorageLocation(null);
        prep.setText1(null);
        prep.setText2(null);

        colObj.getPreparations().add(prep);

        persist(prep);
        return prep;
    }

    public static Locality createLocality(final String name, final Geography geo)
    {
        Locality locality = new Locality();
        locality.initialize();

        locality.setLocalityName(name);
        locality.setGeography(geo);

        persist(locality);
        return locality;
    }

    public static PreparationAttr createPreparationAttr(final AttributeDef attrDef,
                                                        final Preparation prep,
                                                        final String strVal,
                                                        final Double dblVal)
    {
        PreparationAttr prepAttr = new PreparationAttr();
        prepAttr.initialize();

        prepAttr.setDefinition(attrDef);
        prepAttr.setPreparation(prep);
        if (strVal != null)
        {
            prepAttr.setStrValue(strVal);

        }
        else if (dblVal != null)
        {
            prepAttr.setDblValue(dblVal);
        }
        prepAttr.setTimestampCreated(new Timestamp(System.currentTimeMillis()));

        prep.getAttrs().add(prepAttr);
        persist(prepAttr);
        return prepAttr;
    }

    public static Address createAddress(final Agent agent,
                                        final String address1,
                                        final String address2,
                                        final String city,
                                        final String state,
                                        final String country,
                                        final String postalCode)
    {
        return createAddress(agent, address1, address2, city, state, country, postalCode, 0);
    }

    public static Address createAddress(final Agent agent,
                                        final String address1,
                                        final String address2,
                                        final String city,
                                        final String state,
                                        final String country,
                                        final String postalCode,
                                        final int    ordinal)
    {
        Address address = new Address();
        address.initialize();
        address.setAgent(agent);
        address.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        address.setAddress(address1);
        address.setAddress2(address2);
        address.setCity(city);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setState(state);
        address.setIsPrimary(true);
        address.setOrdinal(ordinal);
        
        agent.getAddresses().add(address);
        persist(address);
        persist(agent);
        return address;
    }

    public static Permit createPermit(final String permitNumber,
                                      final String type,
                                      final Calendar issuedDate,
                                      final Calendar startDate,
                                      final Calendar endDate,
                                      final Calendar renewalDate)
    {
        Permit permit = new Permit();
        permit.initialize();
        permit.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        permit.setStartDate(startDate);
        permit.setEndDate(endDate);
        permit.setPermitNumber(permitNumber);
        permit.setIssuedDate(issuedDate);
        permit.setRenewalDate(renewalDate);
        permit.setType(type);
        persist(permit);
        return permit;
    }

    //-----------------------------------------------------------

    public static Accession createAccession(final Division division,
                                            final String type,
                                            final String status,
                                            final String number,
                                            final String verbatimDate,
                                            final Calendar dateAccessioned,
                                            final Calendar dateReceived)
    {
        Accession accession = new Accession();
        accession.initialize();
        accession.setDivision(division);
        accession.setAccessionNumber(number);
        accession.setVerbatimDate(verbatimDate);
        accession.setDateAccessioned(dateAccessioned);
        accession.setDateReceived(dateReceived);
        accession.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        accession.setStatus(status);
        accession.setType(type);
        persist(accession);
        return accession;
    }

    public static AccessionAgent createAccessionAgent(final String role,
                                                       final Agent agent,
                                                       final Accession accession,
                                                       final RepositoryAgreement repositoryAgreement)
    {
        AccessionAgent accessionagent = new AccessionAgent();
        accessionagent.initialize();
        accessionagent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        accessionagent.setAccession(accession);
        accessionagent.setRepositoryAgreement(repositoryAgreement);
        accessionagent.setRole(role);
        accessionagent.setAgent(agent);
        persist(accessionagent);
        return accessionagent;
    }

    public static AccessionAuthorization createAccessionAuthorization(final Permit permit,
                                                                        final Accession accession,
                                                                        final RepositoryAgreement repositoryAgreement)
    {
        AccessionAuthorization accessionauthorization = new AccessionAuthorization();
        accessionauthorization.initialize();
        accessionauthorization.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        accessionauthorization.setAccession(accession);
        accessionauthorization.setPermit(permit);
        accessionauthorization.setRepositoryAgreement(repositoryAgreement);
        persist(accessionauthorization);
        return accessionauthorization;
    }

    public static AttributeDef createAttributeDef(final Short tableType,
                                                  final String fieldName,
                                                  final Short dataType,
                                                  final Discipline discipline,
                                                  final PrepType prepType)
    {
        AttributeDef attributedef = new AttributeDef();
        attributedef.initialize();
        attributedef.setDiscipline(discipline);
        attributedef.setPrepType(prepType);
        attributedef.setTableType(tableType);
        attributedef.setFieldName(fieldName);
        attributedef.setDataType(dataType);
        persist(attributedef);
        return attributedef;
    }

    public static Author createAuthor(final Short orderNumber, final ReferenceWork referenceWork, final Agent agent)
    {
        Author author = new Author();
        author.initialize();
        author.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        author.setAgent(agent);
        author.setOrderNumber(orderNumber);
        author.setReferenceWork(referenceWork);
        persist(author);
        return author;
    }

    public static Borrow createBorrow(final String invoiceNumber,
                                      final Calendar receivedDate,
                                      final Calendar originalDueDate,
                                      final Calendar dateClosed,
                                      final Boolean isClosed,
                                      final Calendar currentDueDate)
    {
        Borrow borrow = new Borrow();
        borrow.initialize();
        borrow.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        borrow.setInvoiceNumber(invoiceNumber);
        borrow.setReceivedDate(receivedDate);
        borrow.setOriginalDueDate(originalDueDate);
        borrow.setDateClosed(dateClosed);
        borrow.setCurrentDueDate(currentDueDate);
        borrow.setIsClosed(isClosed);
        persist(borrow);
        return borrow;
    }

    public static BorrowAgent createBorrowAgent(final String role, final Agent agent, final Borrow borrow)
    {
        BorrowAgent borrowagent = new BorrowAgent();
        borrowagent.initialize();
        borrowagent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        borrowagent.setRole(role);
        borrowagent.setAgent(agent);
        borrowagent.setBorrow(borrow);
        persist(borrowagent);
        return borrowagent;
    }

    public static BorrowMaterial createBorrowMaterial(final String materialNumber,
                                                      final String description,
                                                      final Short quantity,
                                                      final String outComments,
                                                      final String inComments,
                                                      final Short quantityResolved,
                                                      final Short quantityReturned,
                                                      final Borrow borrow)
    {
        BorrowMaterial borrowmaterial = new BorrowMaterial();
        borrowmaterial.initialize();
        borrowmaterial.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        borrowmaterial.setQuantity(quantity);
        borrowmaterial.setBorrow(borrow);
        borrowmaterial.setMaterialNumber(materialNumber);
        borrowmaterial.setOutComments(outComments);
        borrowmaterial.setInComments(inComments);
        borrowmaterial.setQuantityResolved(quantityResolved);
        borrowmaterial.setQuantityReturned(quantityReturned);
        borrowmaterial.setDescription(description);
        persist(borrowmaterial);
        return borrowmaterial;
    }

    public static BorrowReturnMaterial createBorrowReturnMaterial(final Calendar returnedDate,
                                                                  final Short quantity,
                                                                  final Agent agent,
                                                                  final BorrowMaterial borrowMaterial)
    {
        BorrowReturnMaterial borrowreturnmaterial = new BorrowReturnMaterial();
        borrowreturnmaterial.initialize();
        borrowreturnmaterial.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        borrowreturnmaterial.setAgent(agent);
        borrowreturnmaterial.setReturnedDate(returnedDate);
        borrowreturnmaterial.setQuantity(quantity);
        borrowreturnmaterial.setBorrowMaterial(borrowMaterial);
        persist(borrowreturnmaterial);
        return borrowreturnmaterial;
    }

//    public static BorrowShipment createBorrowShipment(final Shipment shipment, final Borrow borrow)
//    {
//        BorrowShipment borrowshipment = new BorrowShipment();
//        borrowshipment.initialize();
//        borrowshipment.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
//        borrowshipment.setShipment(shipment);
//        borrowshipment.setBorrow(borrow);
//        persist(borrowshipment);
//        return borrowshipment;
//    }

    public static CollectingEvent createCollectingEvent(final String   stationFieldNumber,
                                                        final String   method,
                                                        final String   verbatimDate,
                                                        final Calendar startDate,
                                                        final Byte    startDatePrecision,
                                                        final String   startDateVerbatim,
                                                        final Calendar endDate,
                                                        final Byte     endDatePrecision,
                                                        final String   endDateVerbatim,
                                                        final Short    startTime,
                                                        final Short    endTime,
                                                        final String   verbatimLocality,
                                                        final Integer  groupPermittedToView,
                                                        final Locality locality)
    {
        CollectingEvent collectingevent = new CollectingEvent();
        collectingevent.initialize();
        collectingevent.setVerbatimDate(verbatimDate);
        collectingevent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collectingevent.setStartDate(startDate);
        collectingevent.setEndDate(endDate);
        collectingevent.setStationFieldNumber(stationFieldNumber);
        collectingevent.setStartDatePrecision(startDatePrecision);
        collectingevent.setStartDateVerbatim(startDateVerbatim);
        collectingevent.setEndDatePrecision(endDatePrecision);
        collectingevent.setEndDateVerbatim(endDateVerbatim);
        collectingevent.setEndTime(endTime);
        collectingevent.setVerbatimLocality(verbatimLocality);
        collectingevent.setLocality(locality);
        collectingevent.setMethod(method);
        collectingevent.setStartTime(startTime);
        persist(collectingevent);
        return collectingevent;
    }

    public static CollectingEventAttr createCollectingEventAttr(final String strValue,
                                                                final Double dblValue,
                                                                final CollectingEvent collectingEvent,
                                                                final AttributeDef definition)
    {
        CollectingEventAttr collectingeventattr = new CollectingEventAttr();
        collectingeventattr.initialize();
        collectingeventattr.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collectingeventattr.setCollectingEvent(collectingEvent);
        collectingeventattr.setStrValue(strValue);
        collectingeventattr.setDblValue(dblValue);
        collectingeventattr.setDefinition(definition);
        persist(collectingeventattr);
        return collectingeventattr;
    }

    public static Discipline createDiscipline(final String name,
                                                          final DataType dataType,
                                                          final GeographyTreeDef geographyTreeDef,
                                                          final GeologicTimePeriodTreeDef geologicTimePeriodTreeDef,
                                                          final TaxonTreeDef taxonTreeDef)
    {
        Discipline discipline = new Discipline();
        discipline.initialize();
        discipline.setDataType(dataType);
        discipline.setGeographyTreeDef(geographyTreeDef);
        discipline.setGeologicTimePeriodTreeDef(geologicTimePeriodTreeDef);
        discipline.setTaxonTreeDef(taxonTreeDef);
        discipline.setType(name);
        persist(discipline);
        return discipline;
    }

    public static CollectionObject createCollectionObject(final String fieldNumber,
                                                          final String description,
                                                          final Integer countAmt,
                                                          final String name,
                                                          final String modifier,
                                                          final Calendar catalogedDate,
                                                          final String catalogedDateVerbatim,
                                                          final String guid,
                                                          //final String altCatalogNumber,
                                                          final Integer groupPermittedToView,
                                                          final Boolean deaccessioned,
                                                          final String catalogNumber,
                                                          final CollectingEvent collectingEvent,
                                                          final Collection collection,
                                                          final Accession accession,
                                                          final Agent cataloger,
                                                          final Container container)
    {
        CollectionObject collectionobject = new CollectionObject();
        collectionobject.initialize();
        collectionobject.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collectionobject.setFieldNumber(fieldNumber);
        collectionobject.setCountAmt(countAmt);
        collectionobject.setModifier(modifier);
        collectionobject.setCatalogedDate(catalogedDate);
        collectionobject.setCatalogedDateVerbatim(catalogedDateVerbatim);
        collectionobject.setGuid(guid);
        //collectionobject.setAltCatalogNumber(altCatalogNumber);
        collectionobject.setDeaccessioned(deaccessioned);
        collectionobject.setCatalogNumber(catalogNumber);
        collectionobject.setCollectingEvent(collectingEvent);
        collectionobject.setCollection(collection);
        collectionobject.setAccession(accession);
        collectionobject.setCataloger(cataloger);
        collectionobject.setContainer(container);
        collectionobject.setName(name);
        collectionobject.setDescription(description);
        persist(collectionobject);
        return collectionobject;
    }

    public static CollectionObjectAttr createCollectionObjectAttr(final String strValue,
                                                                  final Double dblValue,
                                                                  final CollectionObject collectionObject,
                                                                  final AttributeDef definition)
    {
        CollectionObjectAttr collectionobjectattr = new CollectionObjectAttr();
        collectionobjectattr.initialize();
        collectionobjectattr.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collectionobjectattr.setCollectionObject(collectionObject);
        collectionobjectattr.setStrValue(strValue);
        collectionobjectattr.setDblValue(dblValue);
        collectionobjectattr.setDefinition(definition);
        persist(collectionobjectattr);
        return collectionobjectattr;
    }

    public static CollectionObjectCitation createCollectionObjectCitation(final ReferenceWork referenceWork,
                                                                          final CollectionObject collectionObject)
    {
        CollectionObjectCitation collectionobjectcitation = new CollectionObjectCitation();
        collectionobjectcitation.initialize();
        collectionobjectcitation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collectionobjectcitation.setCollectionObject(collectionObject);
        collectionobjectcitation.setReferenceWork(referenceWork);
        persist(collectionobjectcitation);
        return collectionobjectcitation;
    }

    public static Collector createCollector(final Integer orderNumber,
                                             final CollectingEvent collectingEvent,
                                             final Agent agent)
    {
        Collector collector = new Collector();
        collector.initialize();
        collector.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        collector.setCollectingEvent(collectingEvent);
        collector.setAgent(agent);
        collector.setOrderNumber(orderNumber);
        persist(collector);
        return collector;
    }

    public static Container createContainer(final Short type,
                                            final String name,
                                            final String description,
                                            final Integer number,
                                            final CollectionObject colObj,
                                            final Storage storage)
    {
        Container container = new Container();
        container.initialize();
        container.setNumber(number);
        container.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        container.getCollectionObjects().add(colObj);
        container.setName(name);
        container.setStorage(storage);
        container.setDescription(description);
        container.setType(type);
        persist(container);
        return container;
    }

    public static DataType createDataType(final String name)
    {
        DataType datatype = new DataType();
        datatype.initialize();
        datatype.setName(name);
        persist(datatype);
        return datatype;
    }

    public static Disposal createDisposal(final String type,
                                                final String disposalNumber,
                                                final Calendar disposalDate)
    {
        Disposal disposal = new Disposal();
        disposal.initialize();
        disposal.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        disposal.setDisposalNumber(disposalNumber);
        disposal.setDisposalDate(disposalDate);
        disposal.setType(type);
        persist(disposal);
        return disposal;
    }

    public static DisposalAgent createDisposalAgent(final String role,
                                                           final Agent agent,
                                                           final Disposal disposal)
    {
        DisposalAgent disposalagent = new DisposalAgent();
        disposalagent.initialize();
        disposalagent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        disposalagent.setRole(role);
        disposalagent.setAgent(agent);
        disposalagent.setDisposal(disposal);
        persist(disposalagent);
        return disposalagent;
    }

    public static DisposalPreparation createDisposalPreparation(final Short quantity,
                                                                      final Disposal disposal)
    {
        DisposalPreparation disposalpreparation = new DisposalPreparation();
        disposalpreparation.initialize();
        disposalpreparation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        disposalpreparation.setQuantity(quantity.intValue());
        disposalpreparation.setDisposal(disposal);
        persist(disposalpreparation);
        return disposalpreparation;
    }

    public static Determination createDetermination(final boolean isCurrent,
                                                    final String typeStatusName,
                                                    final Calendar determinedDate,
                                                    final String confidence,
                                                    final String method,
                                                    final String featureOrBasis,
                                                    final Taxon taxon,
                                                    final CollectionObject collectionObject,
                                                    final Agent determiner)
    {
        Determination determination = new Determination();
        determination.initialize();
        determination.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        determination.setIsCurrent(isCurrent);
        determination.setCollectionObject(collectionObject);
        determination.setTypeStatusName(typeStatusName);
        determination.setDeterminedDate(determinedDate);
        determination.setConfidence(confidence);
        determination.setFeatureOrBasis(featureOrBasis);
        determination.setTaxon(taxon);
        determination.setDeterminer(determiner);
        determination.setMethod(method);
        persist(determination);
        return determination;
    }

    public static DeterminationCitation createDeterminationCitation(final ReferenceWork referenceWork,
                                                                    final Determination determination)
    {
        DeterminationCitation determinationcitation = new DeterminationCitation();
        determinationcitation.initialize();
        determinationcitation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        determinationcitation.setReferenceWork(referenceWork);
        determinationcitation.setDetermination(determination);
        persist(determinationcitation);
        return determinationcitation;
    }

    /*
     public static ExchangeIn createExchangeIn(final Calendar exchangeDate,
     final Short quantityExchanged,
     final String descriptionOfMaterial,
     final Agent agent,
     final Agent agent)
     {
     ExchangeIn exchangein = new ExchangeIn();
     exchangein.initialize();
     exchangein.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
     exchangein.setAgent(agent);
     exchangein.setAgent(agent);
     exchangein.setExchangeDate(exchangeDate);
     exchangein.setQuantityExchanged(quantityExchanged);
     exchangein.setDescriptionOfMaterial(descriptionOfMaterial);
     if (session != null)
     {
     persist(exchangein);
     }
     return exchangein;
     }

     public static ExchangeOut createExchangeOut(final Calendar exchangeDate,
     final Short quantityExchanged,
     final String descriptionOfMaterial,
     final Agent agent,
     final Agent agent,
     final Shipment shipment)
     {
     ExchangeOut exchangeout = new ExchangeOut();
     exchangeout.initialize();
     exchangeout.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
     exchangeout.setAgent(agent);
     exchangeout.setAgent(agent);
     exchangeout.setExchangeDate(exchangeDate);
     exchangeout.setQuantityExchanged(quantityExchanged);
     exchangeout.setDescriptionOfMaterial(descriptionOfMaterial);
     exchangeout.setShipment(shipment);
     if (session != null)
     {
     persist(exchangeout);
     }
     return exchangeout;
     }*/

    public static GroupPerson createGroupPerson(final Short orderNumber,
                                                 final Agent agentByGroup,
                                                 final Agent agentByMember)
    {
        GroupPerson groupperson = new GroupPerson();
        groupperson.initialize();
        groupperson.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        groupperson.setOrderNumber(orderNumber);
        groupperson.setGroup(agentByGroup);
        groupperson.setMember(agentByMember);
        persist(groupperson);
        return groupperson;
    }

    public static InfoRequest createInfoRequest(final Integer infoRequestID,
                                                final String firstName,
                                                final String lastName,
                                                final String institution,
                                                final String email,
                                                final Calendar requestDate,
                                                final Calendar replyDate,
                                                final RecordSet recordSet,
                                                final Agent agent)
    {
        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        infoRequest.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        infoRequest.setEmail(email);
        infoRequest.setAgent(agent);
        infoRequest.setFirstName(firstName);
        infoRequest.setLastName(lastName);
        infoRequest.setInfoRequestID(infoRequestID);
        infoRequest.setInstitution(institution);
        infoRequest.setRequestDate(requestDate);
        infoRequest.setReplyDate(replyDate);
        infoRequest.addReference(recordSet, "recordSets");
        persist(infoRequest);
        return infoRequest;
    }

    public static Journal createJournal(final String journalName, final String journalAbbreviation)
    {
        Journal journal = new Journal();
        journal.initialize();
        journal.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        journal.setJournalName(journalName);
        journal.setJournalAbbreviation(journalAbbreviation);
        persist(journal);
        return journal;
    }

    public static Loan createLoan(final String loanNumber,
                                  final Calendar loanDate,
                                  final Calendar currentDueDate,
                                  final Calendar originalDueDate,
                                  final Calendar dateClosed,
                                  final Boolean isClosed,
                                  final Shipment shipment)
    {
        Loan loan = new Loan();
        loan.initialize();
        loan.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        //loan.setShipment(shipment);
        loan.addReference(shipment, "shipments");
        loan.setOriginalDueDate(originalDueDate);
        loan.setDateClosed(dateClosed);
        loan.setCurrentDueDate(currentDueDate);
        loan.setLoanNumber(loanNumber);
        loan.setLoanDate(loanDate);
        loan.setIsClosed(isClosed);
        persist(loan);
        return loan;
    }

    public static LoanAgent createLoanAgent(final String role, final Loan loan, final Agent agent)
    {
        LoanAgent loanAgent = new LoanAgent();
        loanAgent.initialize();
        loanAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        loanAgent.setRole(role);
        loanAgent.addReference(agent, "agent");
        //agent.getLoanAgents().add(loanAgent);
        loanAgent.setLoan(loan);
        persist(loanAgent);
        return loanAgent;
    }
//createLoanPreparation((short)quantity, null, null, null, (short)0, (short)0, p, closedLoan);
    public static LoanPreparation createLoanPreparation(final Integer quantity,
                                                              final String descriptionOfMaterial,
                                                              final String outComments,
                                                              final String inComments,
                                                              final Integer quantityResolved,
                                                              final Integer quantityReturned,
                                                              final Preparation preparation,
                                                              final Loan loan)
    {
        LoanPreparation loanpreparation = new LoanPreparation();
        loanpreparation.initialize();
        loanpreparation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        loanpreparation.setDescriptionOfMaterial(descriptionOfMaterial);
        loanpreparation.setQuantity(quantity);
        loanpreparation.setLoan(loan);
        loanpreparation.setPreparation(preparation);
        loanpreparation.setOutComments(outComments);
        loanpreparation.setInComments(inComments);
        loanpreparation.setQuantityResolved(quantityResolved);
        loanpreparation.setQuantityReturned(quantityReturned);
        
        loan.getLoanPreparations().add(loanpreparation);

        persist(loanpreparation);
        return loanpreparation;
    }

    public static LoanReturnPreparation createLoanReturnPreparation(final Calendar returnedDate,
                                                                    final Integer qtyRet,
                                                                    final Integer qtyRes,
                                                                    final LoanPreparation loanPreparation,
                                                                    final DisposalPreparation disposalPreparation,
                                                                    final Agent agent)
    {
        LoanReturnPreparation loanreturnpreparation = new LoanReturnPreparation();
        loanreturnpreparation.initialize();
        loanreturnpreparation.setReceivedBy(agent);
        loanreturnpreparation.setReturnedDate(returnedDate);
        loanreturnpreparation.setQuantityReturned(qtyRet);
        loanreturnpreparation.setQuantityResolved(qtyRes);
        loanreturnpreparation.setLoanPreparation(loanPreparation);
        Set<DisposalPreparation> preps = new HashSet<>();
        preps.add(disposalPreparation);
        loanreturnpreparation.setDisposalPreparations(preps);
        persist(loanreturnpreparation);
        return loanreturnpreparation;
    }

    public static Locality createLocality(final String namedPlace,
                                          final String relationToNamedPlace,
                                          final String localityName,
                                          final String baseMeridian,
                                          final String range,
                                          final String rangeDirection,
                                          final String township,
                                          final String townshipDirection,
                                          final String section,
                                          final String sectionPart,
                                          final String verbatimElevation,
                                          final String originalElevationUnit,
                                          final BigDecimal minElevation,
                                          final BigDecimal maxElevation,
                                          final String elevationMethod,
                                          final BigDecimal elevationAccuracy,
                                          final Integer originalLatLongUnit,
                                          final String latLongType,
                                          final BigDecimal latitude1,
                                          final BigDecimal longitude1,
                                          final BigDecimal latitude2,
                                          final BigDecimal longitude2,
                                          final String latLongMethod,
                                          final BigDecimal latLongAccuracy,
                                          final String datum,
                                          final Integer groupPermittedToView,
                                          final String lat1text,
                                          final String lat2text,
                                          final String long1text,
                                          final String long2text,
                                          final String nationalParkName,
                                          final String islandGroup,
                                          final String island,
                                          final String waterBody,
                                          final String drainage,
                                          final Geography geography)
    {
        Locality locality = new Locality();
        locality.initialize();
        locality.setNamedPlace(namedPlace);
        locality.setRelationToNamedPlace(relationToNamedPlace);
        locality.setLocalityName(localityName);
        locality.setVerbatimElevation(verbatimElevation);
        locality.setOriginalElevationUnit(originalElevationUnit);
        locality.setMinElevation(minElevation);
        locality.setMaxElevation(maxElevation);
        locality.setElevationMethod(elevationMethod);
        locality.setElevationAccuracy(elevationAccuracy);
        locality.setOriginalLatLongUnit(originalLatLongUnit);
        locality.setLatLongType(latLongType);
        locality.setLatitude1(latitude1);
        locality.setLongitude1(longitude1);
        locality.setLatitude2(latitude2);
        locality.setLongitude2(longitude2);
        locality.setLatLongMethod(latLongMethod);
        locality.setLatLongAccuracy(latLongAccuracy);
        locality.setDatum(datum);
        locality.setLat1text(lat1text);
        locality.setLat2text(lat2text);
        locality.setLong1text(long1text);
        locality.setLong2text(long2text);
        locality.setGeography(geography);
        
        LocalityDetail localityDetail = new LocalityDetail();
        localityDetail.initialize();
        
        localityDetail.setBaseMeridian(baseMeridian);
        localityDetail.setRangeDesc(range);
        localityDetail.setRangeDirection(rangeDirection);
        localityDetail.setTownship(township);
        localityDetail.setTownshipDirection(townshipDirection);
        localityDetail.setSection(section);
        localityDetail.setSectionPart(sectionPart);
        localityDetail.setNationalParkName(nationalParkName);
        localityDetail.setIslandGroup(islandGroup);
        localityDetail.setIsland(island);
        localityDetail.setWaterBody(waterBody);
        localityDetail.setDrainage(drainage);

        locality.addReference(localityDetail, "localityDetails");
        
        persist(locality);
        
        return locality;
    }

    public static LocalityCitation createLocalityCitation(final ReferenceWork referenceWork, final Locality locality)
    {
        LocalityCitation localitycitation = new LocalityCitation();
        localitycitation.initialize();
        localitycitation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        localitycitation.setReferenceWork(referenceWork);
        localitycitation.setLocality(locality);
        persist(localitycitation);
        return localitycitation;
    }

    public static OtherIdentifier createOtherIdentifier(final String identifier, final CollectionObject collectionObject)
    {
        OtherIdentifier otheridentifier = new OtherIdentifier();
        otheridentifier.initialize();
        otheridentifier.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        otheridentifier.setCollectionObject(collectionObject);
        otheridentifier.setIdentifier(identifier);
        persist(otheridentifier);
        return otheridentifier;
    }

    public static PrepType createPrepType(final Collection collection,
                                          final String name)
    {
        PrepType preptype = new PrepType();
        preptype.initialize();
        collection.addReference(preptype, "prepTypes");
        preptype.setName(name);
        persist(preptype);
        return preptype;
    }

    public static Preparation createPreparation(final Integer count,
                                                final String storageLocation,
                                                final Calendar preparedDate,
                                                final PrepType prepType,
                                                final CollectionObject collectionObject,
                                                final Agent preparedByAgent,
                                                final Storage storage)
    {
        Preparation preparation = new Preparation();
        preparation.initialize();
        preparation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        preparation.setCountAmt(count);
        preparation.setStorageLocation(storageLocation);
        preparation.setPreparedDate(preparedDate);
        preparation.setPrepType(prepType);
        preparation.setCollectionObject(collectionObject);
        preparation.setPreparedByAgent(preparedByAgent);
        preparation.setStorage(storage);
        persist(preparation);
        return preparation;
    }

    public static PreparationAttr createPreparationAttr(final String strValue,
                                                        final Double dblValue,
                                                        final AttributeDef definition,
                                                        final Preparation preparation)
    {
        PreparationAttr preparationattr = new PreparationAttr();
        preparationattr.initialize();
        preparationattr.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        preparationattr.setStrValue(strValue);
        preparationattr.setDblValue(dblValue);
        preparationattr.setDefinition(definition);
        preparationattr.setPreparation(preparation);
        persist(preparationattr);
        return preparationattr;
    }

    public static Project createProject(final String projectName,
                                        final String projectDescription,
                                        final String url,
                                        final Calendar startDate,
                                        final Calendar endDate,
                                        final Agent agent)
    {
        Project project = new Project();
        project.initialize();
        project.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        project.setUrl(url);
        project.setAgent(agent);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setProjectName(projectName);
        project.setProjectDescription(projectDescription);
        persist(project);
        return project;
    }

    public static RecordSetIFace createRecordSet(final Integer recordSetID, final String name, final SpecifyUser owner)
    {
        RecordSet recordset = new RecordSet();
        recordset.initialize();
        recordset.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        recordset.setRecordSetId(recordSetID);
        recordset.setOwner(owner);
        recordset.setName(name);
        persist(recordset);
        return recordset;
    }

    public static ReferenceWork createReferenceWork(final Byte referenceWorkType,
                                                    final String title,
                                                    final String publisher,
                                                    final String placeOfPublication,
                                                    final String workDate,
                                                    final String volume,
                                                    final String pages,
                                                    final String url,
                                                    final String libraryNumber,
                                                    final Boolean isPublished,
                                                    final Journal journal)
    {
        ReferenceWork referencework = new ReferenceWork();
        referencework.initialize();
        referencework.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        referencework.setUrl(url);
        referencework.setReferenceWorkType(referenceWorkType);
        referencework.setPublisher(publisher);
        referencework.setPlaceOfPublication(placeOfPublication);
        referencework.setWorkDate(workDate);
        referencework.setVolume(volume);
        referencework.setPages(pages);
        referencework.setLibraryNumber(libraryNumber);
        referencework.setIsPublished(isPublished);
        referencework.setJournal(journal);
        referencework.setTitle(title);
        if (journal != null)
        {
            journal.getReferenceWorks().add(referencework);
        }
        persist(referencework);
        return referencework;
    }

    public static RepositoryAgreement createRepositoryAgreement(final Division division,
                                                                final String number,
                                                                final String status,
                                                                final Calendar startDate,
                                                                final Calendar endDate,
                                                                final Calendar dateReceived,
                                                                final Agent originator)
    {
        RepositoryAgreement repositoryagreement = new RepositoryAgreement();
        repositoryagreement.initialize();
        repositoryagreement.setDivision(division);
        repositoryagreement.setRepositoryAgreementNumber(number);
        repositoryagreement.setDateReceived(dateReceived);
        repositoryagreement.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        repositoryagreement.setStartDate(startDate);
        repositoryagreement.setEndDate(endDate);
        repositoryagreement.setOriginator(originator);
        repositoryagreement.setStatus(status);
        persist(repositoryagreement);
        return repositoryagreement;
    }

    public static Shipment createShipment(final Calendar shipmentDate,
                                          final String shipmentNumber,
                                          final String shipmentMethod,
                                          final Short numberOfPackages,
                                          final String weight,
                                          final String insuredForAmount,
                                          final Agent shipper,
                                          final Agent shippedTo,
                                          final Agent agent)
    {
        Shipment shipment = new Shipment();
        shipment.initialize();
        shipment.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        shipment.setShippedBy(agent);
        shipment.setShipmentDate(shipmentDate);
        shipment.setShipmentNumber(shipmentNumber);
        shipment.setShipmentMethod(shipmentMethod);
        shipment.setNumberOfPackages(numberOfPackages);
        shipment.setWeight(weight);
        shipment.setInsuredForAmount(insuredForAmount);
        shipment.setShipper(shipper);
        shipment.setShippedTo(shippedTo);
        persist(shipment);
        return shipment;
    }


    public static SpecifyUser createSpecifyUser(final String    name,
                                                final String    email,
                                                final String    password,
                                                final String    userType)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setEmail(email);
        specifyuser.setPassword(password);
        specifyuser.setName(name);
        specifyuser.setUserType(userType);
        persist(specifyuser);
        return specifyuser;
    }
    
    public static SpecifyUser createSpecifyUser(final String name,
                                                final String email,
                                                final String password,
                                                final Map<String, SpPrincipal> groupMap,
                                                final String userType)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setEmail(email);
        specifyuser.setPassword(password);
        specifyuser.addUserToSpPrincipalGroup(groupMap.get(userType));
        specifyuser.setName(name);
        specifyuser.setUserType(userType);
        persist(specifyuser);
        return specifyuser;
    }
    
    public static SpecifyUser createSpecifyUser(final String name,
                                                final String email,
                                                final String    password,
                                                final List<SpPrincipal> userGroups)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setEmail(email);
        specifyuser.setPassword(password);
        specifyuser.setName(name);
        if (userGroups!=null) 
        {
            for (SpPrincipal group : userGroups)
            {
                specifyuser.addUserToSpPrincipalGroup(group);
            }
        }
        persist(specifyuser);
        return specifyuser;
    }
    
    public static SpecifyUser createSpecifyUser(final String name,
                                                final String email,
                                                final String    password,
                                                final List<SpPrincipal> userGroups,
                                                final String userType)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setEmail(email);
        specifyuser.setPassword(password);
        specifyuser.setName(name);
        specifyuser.setUserType(userType);
        if (userGroups!=null) 
        {
            for (SpPrincipal group : userGroups)
            {
                specifyuser.addUserToSpPrincipalGroup(group);
            }
        }
        persist(specifyuser);
        return specifyuser;
    }

    public static SpecifyUser createAndAddTesterToCollection(final Session            sessionArg,
                                                             final String             name,
                                                             final String             email,
                                                             final String             pwd,
                                                             final String             title,
                                                             final String             first,
                                                             final String             middle,
                                                             final String             last,
                                                             final String             abbrev,
                                                             final Discipline         discipline, 
                                                             final Division           division, 
                                                             final Collection         collection,
                                                             final Map<String, SpPrincipal> groupMap, 
                                                             final String             userType) 
    {
        // Tester
        Agent testerAgent = createAgent(title, first, middle, last, abbrev, email);
        sessionArg.saveOrUpdate(testerAgent);
        
        testerAgent.setDivision(division);
        SpecifyUser testerUser = createSpecifyUser(name, email, pwd, groupMap, userType);
        sessionArg.saveOrUpdate(testerUser);
        
        SpPrincipal testerUserPrincipal = DataBuilder.createUserPrincipal(testerUser, collection);
        sessionArg.saveOrUpdate(testerUserPrincipal);
        
        testerUser.addUserToSpPrincipalGroup(testerUserPrincipal);
        discipline.addReference(testerAgent, "agents");
        testerUser.addReference(testerAgent, "agents");

        return testerUser;
    }

//    public static UserPermission createUserPermission(SpecifyUser owner, 
//                                                      Discipline objDef, 
//                                                      boolean adminPrivilege, 
//                                                      boolean dataAccessPrivilege)
//    {
//        UserPermission permission = new UserPermission();
//        permission.setAdminPrivilege(adminPrivilege);
//        permission.setDiscipline(objDef);
//        permission.setDataAccessPrivilege(dataAccessPrivilege);
//        permission.setSpecifyUser(owner);
//        persist(permission);
//        return permission;
//    }

    public static TaxonCitation createTaxonCitation(final ReferenceWork referenceWork, final Taxon taxon)
    {
        TaxonCitation taxoncitation = new TaxonCitation();
        taxoncitation.initialize();
        taxoncitation.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        taxoncitation.setTaxon(taxon);
        taxoncitation.setReferenceWork(referenceWork);
        persist(taxoncitation);
        return taxoncitation;
    }

    public static SpPrincipal createGroup(final String name, 
                                          final String type, 
                                          final int priority, 
                                          final UserGroupScope scope)
    {
        SpPrincipal usergroup = new SpPrincipal();
        usergroup.initialize();
        usergroup.setName(name);
        usergroup.setPriority(priority);
        usergroup.setGroupType(type);
        usergroup.setGroupSubClass(GroupPrincipal.class.getCanonicalName());
        usergroup.setScope(scope);
        return usergroup;    
    }
    
    public static SpPrincipal createAdminGroup(final String name, final UserGroupScope scope)
    {
        SpPrincipal groupPrincipal = new SpPrincipal();
        groupPrincipal.initialize();
        groupPrincipal.setName(name);
        groupPrincipal.setPriority(0);
        groupPrincipal.setScope(scope);
        groupPrincipal.setGroupSubClass(AdminPrincipal.class.getCanonicalName());
        return groupPrincipal;
    }
    
    public static SpPrincipal createUserPrincipal(final SpecifyUser user, final Collection scope)
    {
        SpPrincipal userPrincipal = new SpPrincipal();
        userPrincipal.initialize();
        userPrincipal.setName(user.getName());
        userPrincipal.setPriority(80);
        userPrincipal.setScope(scope);
        userPrincipal.setGroupSubClass(UserPrincipal.class.getCanonicalName());
        user.getSpPrincipals().add(userPrincipal);
        return userPrincipal;   
    }
    
    public static AutoNumberingScheme createAutoNumberingScheme(final String schemeName,
                                                                final String schemeClassName,
                                                                final String formatName,
                                                                final boolean isNumericOnly,
                                                                final int   tableNumber)
    {
        AutoNumberingScheme ans = new AutoNumberingScheme();
        ans.initialize();
        ans.setFormatName(formatName);
        ans.setTableNumber(tableNumber);
        ans.setSchemeName(schemeName);
        ans.setSchemeClassName(schemeClassName);
        ans.setIsNumericOnly(isNumericOnly);
        persist(ans);
        return ans;
    }

    public static SpPermission createPermission(final String name, 
    											final String actions, 
    											final String permClass, 
    											final Set<SpPrincipal> groupSet)
    {
    	SpPermission perm = new SpPermission();
        perm.initialize();
    	perm.setName(name);
    	perm.setActions(actions);
    	perm.setPermissionClass(permClass);
    	perm.setPrincipals(groupSet);
    	persist(perm);
    	return perm;
    }

    /**
     * Load definition of default groups
     */
    private static void loadDefaultGroupDefinitions()
    {
        if (usertypeToDefaultGroup == null)
        {
            usertypeToDefaultGroup = new HashMap<String, Pair<String, Byte>>();
            usertypeToDefaultGroup.put(SpecifyUserTypes.UserType.Manager.toString(),       new Pair<String, Byte>("Managers", (byte)10));
            usertypeToDefaultGroup.put(SpecifyUserTypes.UserType.FullAccess.toString(),    new Pair<String, Byte>("Full Access Users", (byte)20));
            usertypeToDefaultGroup.put(SpecifyUserTypes.UserType.LimitedAccess.toString(), new Pair<String, Byte>("Limited Access Users", (byte)30));
            usertypeToDefaultGroup.put(SpecifyUserTypes.UserType.Guest.toString(),         new Pair<String, Byte>("Guests", (byte)40));
        }
    }

    /**
     * Create default groups under the given scope.
     *
     */
    public static Map<String, SpPrincipal> createStandardGroups(final Session     sessionArg,
                                                                final UserGroupScope scope)
    {
        loadDefaultGroupDefinitions();
        
        Map<String, SpPrincipal> groupMap = new HashMap<String, SpPrincipal>();

        for (String usertype : usertypeToDefaultGroup.keySet()) 
        {
            Pair<String, Byte> grpInfo = usertypeToDefaultGroup.get(usertype);
            SpPrincipal group = createGroup(grpInfo.first, usertype, grpInfo.second, scope);
            sessionArg.saveOrUpdate(group);
            groupMap.put(usertype, group);
        }
        createDefaultPermissions(sessionArg, groupMap);

        return groupMap;
    }

    /**
     * @param groupMap
     */
    public static void createDefaultPermissions(final Session sessionArg, final Map<String, SpPrincipal> groupMap)
    {
        createDefaultPermissions(sessionArg, "dataobjs.xml",   "DO.",    groupMap, null);
        createDefaultPermissions(sessionArg, "prefsperms.xml", "Prefs.", groupMap, null);
        createDefaultPermissions(sessionArg, "tasks.xml",      "Task.",  groupMap, getTasksAdditionalSecOpts());
    }
    
    /**
     * @param hash
     * @param fileName
     */
    public static void writePerms(final Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash,
                                  final String fileName)
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        if (localPrefs != null && localPrefs.getBoolean("perms.write.xml", false))
        {
            XStream xstream = new XStream();
            PermissionOptionPersist.config(xstream);
            try
            {
                FileUtils.writeStringToFile(new File(fileName), xstream.toXML(hash)); //$NON-NLS-1$
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * @param filename
     * @param prefix
     * @param groupMap
     */
    public static void createDefaultPermissions(final Session     sessionArg,
                                                final String      filename,
                                                final String      prefix,
                                                final Map<String, SpPrincipal> groupMap,
                                                final List<SecurityOptionIFace> additionalSecOpts)
    {
        Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPermsFromXML(filename);
        for (String permName : mainHash.keySet())
        {
            String userType = "LimitedAccess";
            Hashtable<String, PermissionOptionPersist> hash = mainHash.get(permName);
            if (hash.get(userType) == null)
            {
                PermissionOptionPersist permOpts = hash.get("Manager");
                PermissionOptionPersist newPermOpts = new PermissionOptionPersist(permOpts.getTaskName(), userType, permOpts.isCanView(), permOpts.isCanModify(), permOpts.isCanDel(), permOpts.isCanAdd());
                hash.put(userType, newPermOpts);
            }
            
            userType = "FullAccess";
            hash = mainHash.get(permName);
            if (hash.get(userType) == null)
            {
                PermissionOptionPersist permOpts = hash.get("Manager");
                PermissionOptionPersist newPermOpts = new PermissionOptionPersist(permOpts.getTaskName(), userType, permOpts.isCanView(), permOpts.isCanModify(), permOpts.isCanDel(), permOpts.isCanAdd());
                hash.put(userType, newPermOpts);
            }
        }
        
        if (additionalSecOpts != null)
        {
            for (SecurityOptionIFace aso : additionalSecOpts)
            {
                Hashtable<String, PermissionOptionPersist> hash = mainHash.get(aso.getPermissionName());
                if (hash == null)
                {
                    hash = new Hashtable<String, PermissionOptionPersist>();
                    mainHash.put(aso.getPermissionName(), hash);
                }
                for (SpecifyUserTypes.UserType userType : SpecifyUserTypes.UserType.values())
                {
                    PermissionIFace asoPerm = aso.getDefaultPermissions(userType.toString());
                    if (asoPerm != null)
                    {
                        PermissionOptionPersist newPermOpts = new PermissionOptionPersist(aso.getPermissionName(), userType.toString(), asoPerm.canView(), asoPerm.canModify(), asoPerm.canDelete(), asoPerm.canAdd());
                        hash.put(userType.toString(), newPermOpts);
                    }
                }
            }
        }
        writePerms(mainHash, filename);

        for (SpPrincipal p : groupMap.values())
        {
            persist(p);
        }

        for (String permName : mainHash.keySet())
        {
            Hashtable<String, PermissionOptionPersist> hash = mainHash.get(permName);
            for (String userType : hash.keySet()) 
            {
                PermissionOptionPersist tp   = hash.get(userType);
                SpPermission            perm = tp.getSpPermission();
                sessionArg.saveOrUpdate(perm);
                
                Set<SpPrincipal> groupSet = new HashSet<SpPrincipal>();
                groupSet.add(groupMap.get(userType));
                perm.setPrincipals(groupSet);
                perm.setName(prefix + permName);
            }
        }
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Create default groups under the given scope.
     *
     */
    public static Map<String, List<Integer>> mergeStandardGroups(final Collection scope)
    {
        loadDefaultGroupDefinitions();
        
        Map<String, List<Integer>> groupMap = new HashMap<String, List<Integer>>();

        for (String usertype : usertypeToDefaultGroup.keySet()) 
        {
            Pair<String, Byte> grpInfo = usertypeToDefaultGroup.get(usertype);
            List<Integer> principleIds = getGroup(grpInfo.first, usertype, scope);
            groupMap.put(usertype, principleIds);
        }
        mergeDefaultPermissions(groupMap);

        return groupMap;
    }
    
    /**
     * @return
     */
    private static List<SecurityOptionIFace> getTasksAdditionalSecOpts()
    {
        List<SecurityOptionIFace> additionalTaskSecOpts = new ArrayList<SecurityOptionIFace>();
        for (Taskable task : TaskMgr.getInstance().getAllTasks())
        {
            List<SecurityOptionIFace> list = task.getAdditionalSecurityOptions();
            if (list != null)
            {
                additionalTaskSecOpts.addAll(list);
            }
        }
        return additionalTaskSecOpts;
    }
    
    /**
     * @param groupMap
     */
    private static void mergeDefaultPermissions(final Map<String, List<Integer>> groupMap)
    {

        mergeDefaultPermissions("dataobjs.xml",   "DO.",    groupMap);
        mergeDefaultPermissions("prefsperms.xml", "Prefs.", groupMap);
        mergeDefaultPermissions("tasks.xml",      "Task.",  groupMap);
    }
    
    /**
     * @param name
     * @param type
     * @param scope
     * @return
     */
    private static List<Integer> getGroup(final String name, 
                                          final String type, 
                                          final Collection scope)
    {   
        ArrayList<Integer> ids = new ArrayList<Integer>();
        String sql = " SELECT SpPrincipalID FROM spprincipal WHERE Name=? AND groupType=? AND GroupSubClass=? AND userGroupScopeID=?"; 
        String grpTypeStr = GroupPrincipal.class.getCanonicalName();
        
        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement pStmt = null;
        try
        {
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, name);
            pStmt.setString(2, type);
            pStmt.setString(3, grpTypeStr);
            pStmt.setInt(4, scope.getId());
            
            ResultSet rs = pStmt.executeQuery();
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
            } catch (SQLException e) {}
        }
        
        return ids;
    }
    
    /**
     * @param filename
     * @param prefix
     * @param groupMap
     */
    public static void mergeDefaultPermissions(final String      filename,
                                               final String      prefix,
                                               final Map<String, List<Integer>> groupMap)
    {
        Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPermsFromXML(filename);
        for (String permName : mainHash.keySet())
        {
            String userType = "LimitedAccess";
            Hashtable<String, PermissionOptionPersist> hash = mainHash.get(permName);
            if (hash.get(userType) == null)
            {
                PermissionOptionPersist permOpts = hash.get("Manager");
                PermissionOptionPersist newPermOpts = new PermissionOptionPersist(permOpts.getTaskName(), userType, permOpts.isCanView(), permOpts.isCanModify(), permOpts.isCanDel(), permOpts.isCanAdd());
                hash.put(userType, newPermOpts);
            }
            
            userType = "FullAccess";
            hash = mainHash.get(permName);
            if (hash.get(userType) == null)
            {
                PermissionOptionPersist permOpts = hash.get("Manager");
                PermissionOptionPersist newPermOpts = new PermissionOptionPersist(permOpts.getTaskName(), userType, permOpts.isCanView(), permOpts.isCanModify(), permOpts.isCanDel(), permOpts.isCanAdd());
                hash.put(userType, newPermOpts);
            }
        }
        
        /*if (additionalSecOpts != null)
        {
            for (SpecifyUserTypes.UserType userType : SpecifyUserTypes.UserType.values())
            {
                System.out.println(userType.toString()+" --------------------------------------");
                for (SecurityOptionIFace aso : additionalSecOpts)
                {
                    PermissionIFace asoPerm = aso.getDefaultPermissions(userType.toString());
                    if (asoPerm != null)
                    {
                        System.out.println("  "+prefix+aso.getPermissionName()+"  "+asoPerm.getOptions());
                    }
                }
            }
        }*/
        
        
        HashMap<SpPermission, List<Integer>> prinPermHash = new HashMap<SpPermission, List<Integer>>();
        for (String permName : mainHash.keySet())
        {
            String fullPermName = prefix + permName;
            
            Hashtable<String, PermissionOptionPersist> hash = mainHash.get(permName);
            for (String userType : hash.keySet()) 
            {
                PermissionOptionPersist tp   = hash.get(userType);
                SpPermission            perm = tp.getSpPermission();
                
                for (Integer id : groupMap.get(userType))
                {
                    String str = "SELECT p.SpPermissionID FROM sppermission AS p Inner Join spprincipal_sppermission AS pp ON p.SpPermissionID = pp.SpPermissionID " +
                    		     "WHERE p.Name = '%s' AND pp.SpPrincipalID = %d";
                    String sql = String.format(str, fullPermName, id);
                    Integer permId = BasicSQLUtils.getCount(sql);
                    if (permId == null)
                    {
                        System.out.println(String.format("Going to create %s for Prin: %d", fullPermName, id));
                        List<Integer> list = prinPermHash.get(perm);
                        if (list == null)
                        {
                            perm.setName(fullPermName);
                            list = new ArrayList<Integer>();
                            prinPermHash.put(perm, list);
                        }
                        list.add(id);
                    }
                }
            }
        }
        
        if (prinPermHash.size() > 0)
        {
            Connection        conn   = null;
            PreparedStatement pstmt1 = null; 
            PreparedStatement pstmt2 = null; 
            try
            {
                conn = DatabaseService.getInstance().getConnection();            
                pstmt1 = conn.prepareStatement("INSERT INTO sppermission (Actions, Name, PermissionClass) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);         //$NON-NLS-1$
                pstmt2 = conn.prepareStatement("INSERT INTO spprincipal_sppermission (SpPermissionID, SpPrincipalID) VALUES (?, ?)"); //$NON-NLS-1$
                for (SpPermission spPerm : prinPermHash.keySet())
                {
                    for (Integer prinId : prinPermHash.get(spPerm))
                    {
                        pstmt1.setString(1, spPerm.getActions());
                        pstmt1.setString(2, spPerm.getName());
                        pstmt1.setString(3, spPerm.getClass().getName());
                        pstmt1.setString(3, BasicSpPermission.class.getCanonicalName());
                        pstmt1.executeUpdate();
                        
                        Integer newPermId = BasicSQLUtils.getInsertedId(pstmt1);
                        pstmt2.setInt(1, newPermId);
                        pstmt2.setInt(2, prinId);
                        pstmt2.executeUpdate();
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            } finally
            {
                try
                {
                    if (pstmt1 != null)  pstmt1.close(); 
                    if (pstmt2 != null)  pstmt2.close(); 
                    if (conn != null)  conn.close();
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                }
            } 
        }
    }
    
    //------------------------------------------------------------------------
    
    public static Workbench createWorkbench(final SpecifyUser user,
                                            final String name, 
                                            final String remarks, 
                                            final String exportInstName,
                                            final WorkbenchTemplate workbenchTemplate)
    {
        Workbench workbench = new Workbench();
        workbench.initialize();
        
        workbench.setName(name);
        workbench.setSpecifyUser(user);
        workbench.setRemarks(remarks);
        workbench.setExportInstitutionName(exportInstName);

        workbench.setWorkbenchTemplate(workbenchTemplate);

        persist(workbench);

        return workbench;
    }

    public static WorkbenchRow createWorkbenchRow(final Workbench workbench,
                                                  final short rowNumber)
    {
        WorkbenchRow workbenchRow = new WorkbenchRow(workbench, rowNumber);
        // workbench.initialize(); // not needed with this constructor

        persist(workbenchRow);

        return workbenchRow;
    }

    public static WorkbenchDataItem createWorkbenchDataItem(final WorkbenchRow workbenchRow,
                                                            final String       cellData,
                                                            final Integer      columnNumber)
    {
        
        WorkbenchDataItem wbdi = workbenchRow.setData(cellData, columnNumber.shortValue(), true);
        
        if (wbdi != null)
        {
            wbdi.setRowNumber(workbenchRow.getRowNumber());
            persist(wbdi);
            
        } else
        {
            //System.err.println("workbenchRow.setData returned a null DataItem cellData["+cellData+"] or columnNumber["+columnNumber+"]");
        }

        return wbdi;
    }

    public static WorkbenchTemplate createWorkbenchTemplate(final SpecifyUser user,
                                                            final String name, 
                                                            final String remarks)
    {
        WorkbenchTemplate wbt = new WorkbenchTemplate();
        wbt.initialize();

        wbt.setSpecifyUser(user);
        wbt.setName(name);
        wbt.setRemarks(remarks);

        user.getWorkbenchTemplates().add(wbt);
        
        persist(wbt);

        return wbt;
    }

    public static WorkbenchTemplateMappingItem createWorkbenchMappingItem(final String tableName,
                                                                          final Integer tableId,
                                                                          final String fieldName,
                                                                          final String caption,
                                                                          final int     dataLength,
                                                                          final Integer viewOrder,
                                                                          final Integer dataColumnIndex,
                                                                          final WorkbenchTemplate template)
    {
        WorkbenchTemplateMappingItem wtmi = new WorkbenchTemplateMappingItem();
        wtmi.initialize();

        wtmi.setCaption(caption);
        wtmi.setFieldName(fieldName);
        wtmi.setTableName(tableName.toLowerCase());
        wtmi.setViewOrder(viewOrder.shortValue());
        wtmi.setDataFieldLength((short)dataLength);
        wtmi.setOrigImportColumnIndex(dataColumnIndex.shortValue());
        wtmi.setWorkbenchTemplate(template);
        wtmi.setSrcTableId(tableId);
        
        template.getWorkbenchTemplateMappingItems().add(wtmi);
        
        persist(wtmi);

        return wtmi;
    }
    
    /**
     * Configures both BldrPickList and BldrPickListItem
     * @param xstream the stream
     * @param doPartial not all the fields
     */
    public static void configXStream(final XStream xstream, 
                                     final boolean doExportImport)
    {
        xstream.alias("picklist",     BldrPickList.class);
        xstream.alias("picklistitem", BldrPickListItem.class);
        
        xstream.omitField(BldrPickList.class, "pickListId");
        //xstream.omitField(BldrPickList.class, "items");
        xstream.omitField(BldrPickList.class, "pickListItems");
        
        xstream.useAttributeFor(BldrPickList.class, "fieldName");
        xstream.useAttributeFor(BldrPickList.class, "sizeLimit");
        xstream.useAttributeFor(BldrPickList.class, "tableName");
        xstream.useAttributeFor(BldrPickList.class, "readOnly");
        xstream.useAttributeFor(BldrPickList.class, "type");
        xstream.useAttributeFor(BldrPickList.class, "formatter");
        xstream.useAttributeFor(BldrPickList.class, "name");
        xstream.useAttributeFor(BldrPickList.class, "isSystem");
        xstream.useAttributeFor(BldrPickList.class, "sortType");
        
        xstream.useAttributeFor(BldrPickListItem.class, "title");
        xstream.useAttributeFor(BldrPickListItem.class, "value");
        xstream.useAttributeFor(BldrPickListItem.class, "ordinal");
        
        xstream.aliasAttribute("readonly",  "readOnly");
        xstream.aliasAttribute("tablename", "tableName");
        xstream.aliasAttribute("fieldname", "fieldName");
        xstream.aliasAttribute("sizelimit", "sizeLimit");
        xstream.aliasAttribute("issystem",  "isSystem");
        xstream.aliasAttribute("sort",      "sortType");
        
        if (doExportImport)
        {
            xstream.useAttributeFor(BldrPickList.class, "filterFieldName");
            xstream.useAttributeFor(BldrPickList.class, "filterValue");
        }
        
        String[] omit = {"timestampCreated", "timestampModified", "version", };
        for (String fld : omit)
        {
            if (doExportImport)
            {
                xstream.useAttributeFor(BldrPickList.class, fld);
                xstream.useAttributeFor(BldrPickListItem.class, fld);

            } else
            {
                xstream.omitField(BldrPickList.class, fld); 
                xstream.omitField(BldrPickListItem.class, fld);
            }
        }
        
        xstream.omitField(BldrPickListItem.class, "pickListItemId");
        xstream.omitField(BldrPickListItem.class, "pickList");
    }

    
    /**
     * @param disciplineDirName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<BldrPickList> getBldrPickLists(final String disciplineDirName)
    {
        return getBldrPickLists(disciplineDirName, null);
    }
    
    /**
     * @param disciplineDirName
     * @param plFile
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<BldrPickList> getBldrPickLists(final String disciplineDirName, 
                                                      final File plFile)
    {
        XStream xstream = new XStream();

        try
        {
            File pickListFile;
            if (plFile == null)
            {
                String dirName = disciplineDirName != null ? disciplineDirName + File.separator : "";
                pickListFile = new File(XMLHelper.getConfigDirPath(dirName + "picklist.xml"));
                configXStream(xstream, false);
            } else
            {
                pickListFile = plFile;
                configXStream(xstream, true);
            }
            
            if (pickListFile.exists())
            {
                //System.out.println(FileUtils.readFileToString(pickListFile));
                List<BldrPickList> list = (List<BldrPickList>)xstream.fromXML(FileUtils.readFileToString(pickListFile));
                
                for (BldrPickList pl : list)
                {
                    if (pl.getSortType() == null)
                    {
                        pl.setSortType(PickListIFace.PL_TITLE_SORT);
                    }
                    
                    if (pl.getSortType().equals(PickListIFace.PL_ORDINAL_SORT))
                    {
                        int order = 0;
                        for (BldrPickListItem item : pl.getItems())
                        {
                            item.setOrdinal(order++);
                        }
                    }
                }
                return list;
            }
            //System.out.println("Couldn't find picklist.xml ["+pickListFile.getCanonicalPath()+"]");
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataBuilder.class, ex);
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param file
     * @param pickLists
     */
    public static void writePickListsAsXML(final File file, List<BldrPickList> pickLists)
    {
        XStream xstream = new XStream();
        configXStream(xstream, true);
        
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            xstream.toXML(pickLists, fos);
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataBuilder.class, ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Helper method for saving when there is a session.
     * @param transientObject the object to be saved.
     * 
     */
    public static void persist(Object transientObject)
    {
        if (session != null)
        {
            session.persist(transientObject);
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public static void buildDarwinCoreSchema(final Discipline disciplineArg)
    {
        
//        XStream xstream = new XStream();
//        xstream.alias("field",     FieldMap.class);
//        
//        xstream.useAttributeFor(FieldMap.class, "name");
//        xstream.useAttributeFor(FieldMap.class, "table");
//        xstream.useAttributeFor(FieldMap.class, "field");
//        xstream.useAttributeFor(FieldMap.class, "formatter");
//        
//        DataProviderSessionIFace localSession = null;
//        
        try
        {
//            localSession = DataProviderFactory.getInstance().createSession();
//            
//            if (false)
//            {
//                localSession.beginTransaction();
//                
//                List<SpExportSchema> schemaList = localSession.getDataList(SpExportSchema.class);
//                if (schemaList != null)
//                {
//                    for (SpExportSchema s : schemaList)
//                    {
//                        for (SpExportSchemaItem item : s.getSpExportSchemaItems())
//                        {
//                            if (item.getSpLocaleContainerItem() != null)
//                            {
//                                item.getSpLocaleContainerItem().removeReference(item, "spExportSchemaItems");
//                            }
//                        }
//                        localSession.delete(s);
//                    }
//                }
//                localSession.commit();
//                localSession.flush();
//            }
//            
//            
//            File           mapFile   = new File(XMLHelper.getConfigDirPath("darwin_core_map.xml"));
//            List<FieldMap> fieldList = (List<FieldMap>)xstream.fromXML(FileUtils.readFileToString(mapFile));
//            Hashtable<String, FieldMap> fieldMap = new Hashtable<String, FieldMap>();
//            for (FieldMap fm : fieldList)
//            {
//                fieldMap.put(fm.getName(), fm);
//            }
//            Element root = null;
//            if (true)
//            {
//                root = XMLHelper.readDOMFromConfigDir("darwin2_core.xsd");
//                
//            } else
//            {
//                String url = "http://www.digir.net/schema/conceptual/darwin/2003/1.0/darwin2.xsd";
//                HTTPGetter getter = new HTTPGetter();
//                byte[] bytes = getter.doHTTPRequest(url);
//                if (getter.getStatus() == HTTPGetter.ErrorCode.NoError)
//                {
//                    String xml = new String(bytes);
//                    //System.out.println(xml);
//                    root = XMLHelper.readStrToDOM4J(xml);
//                }
//            }
//            
//            Discipline disciplineTmp;
//            if (disciplineArg == null)
//            {
//                disciplineTmp = AppContextMgr.getInstance().getClassObject(Discipline.class);
//            } else
//            {
//                disciplineTmp = disciplineArg; 
//            }
//            
//            Discipline discipline = (Discipline)localSession.getData("FROM Discipline WHERE disciplineId = " + disciplineTmp.getId());
//            
//            SpExportSchema schema = new SpExportSchema();
//            schema.initialize();
//            schema.setSchemaName("Darwin Core");
//            //Maybe the version for this should be 1.21 to be consistent with the
//            //VertNet darwin schema? Which Laura says is 1.21. 
//            //The darwin2_core.xsd and darwinCoreVertNet.xsd
//            //files seem to point to the same source.            
//            schema.setSchemaVersion("2.0");
//            
//            discipline.addReference(schema, "spExportSchemas");
//            
//            StringBuilder sb = new StringBuilder();
//            for (Object obj : root.selectNodes("/xsd:schema/xsd:annotation/xsd:documentation"))
//            {
//                Element e = (Element)obj;
//                sb.append(e.getTextTrim());
//            }
//            schema.setDescription(sb.toString().substring(0, Math.min(sb.length(), 255)));
//            
//            localSession.beginTransaction();
//            
//            localSession.saveOrUpdate(discipline);
//            localSession.save(schema);
//            
//            for (Object obj : root.selectNodes("/xsd:schema/xsd:element"))
//            {
//                Element e = (Element)obj;
//                String  name      = XMLHelper.getAttr(e, "name", null);
//                String  type      = XMLHelper.getAttr(e, "type", null);
//                
//                FieldMap fm = fieldMap.get(name);
//                
//                if (name != null && type != null && fm != null && StringUtils.isNotEmpty(fm.getField()))
//                {
//                    SpExportSchemaItem item = new SpExportSchemaItem();
//                    item.initialize();
//                    item.setFieldName(name);
//                    item.setDataType(type.substring(4));
//                    item.setFormatter(fm.getFormatter());
//                    
//                    schema.addReference(item, "spExportSchemaItems");
//                    
//                    //System.out.println(type.substring(4)+"  "+type.substring(4).length());
//                    
//                    localSession.save(item);
//                    
//                    String sql = "FROM SpLocaleContainerItem spi INNER JOIN spi.container spc INNER JOIN spc.discipline dsp WHERE spc.name='%s' AND spi.name='%s' AND dsp.disciplineId = %s";
//                    sql = String.format(sql, fm.getTable(), fm.getField(), discipline.getDisciplineId().toString());
//                    Object[] cols = (Object[])localSession.getData(sql);
//                    if (cols != null)
//                    {
//                        System.out.println(name);
//                        SpLocaleContainerItem spItem = (SpLocaleContainerItem)cols[0];
//                        if (spItem != null)
//                        {
//                            item.setSpLocaleContainerItem(spItem);
//                            spItem.getSpExportSchemaItems().add(item);
//                            
//                            localSession.saveOrUpdate(item);
//                            localSession.saveOrUpdate(spItem);
//                            
//                        } else
//                        {
//                            System.err.println("Couldn't find ["+sql+"]");
//                        }
//                    }
//                }
//                
//                /*if (name != null && type != null)
//                {
//                    System.out.println("    <field name=\""+name+"\" table=\"\" field=\"\" formatter=\"\"/>");
//                    //System.out.println("<field name=\""+name+"\" type=\""+ type.substring(4)+"\" table=\"\" field=\"\"/>");
//                    Object node = e.selectObject("xsd:annotation/xsd:documentation");
//                    if (node instanceof Element)
//                    {
//                        Element descEl = (Element)node;
//                        if (descEl != null)
//                        {
//                            String  desc = descEl.getTextTrim();
//                            //System.out.println(desc+"\n");
//                        }
//                    }
//                }*/
//            }
//            localSession.commit();
//            localSession.flush();
            
            //ExportMappingTask imports don't update the schemaLocale tables
            ExportMappingTask.importSchemaDefinition(new File(XMLHelper.getConfigDirPath("tdwg_dwcterms.xsd")), "DarwinCoreTDWG", "2014-11-08");
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataBuilder.class, ex);
            ex.printStackTrace();
            
        } 
//        finally
//        {
//            if (localSession != null)
//            {
//                localSession.close();
//            }
//        }
    }
    
    // USed for Mapping from Export Schema to the Specify Schema
    class FieldMap 
    {
        protected String name;
        protected String table;
        protected String field;
        protected String formatter;
        
        public FieldMap()
        {
            
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * @return the table
         */
        public String getTable()
        {
            return table;
        }

        /**
         * @param table the table to set
         */
        public void setTable(String table)
        {
            this.table = table;
        }

        /**
         * @return the field
         */
        public String getField()
        {
            return field;
        }

        /**
         * @param field the field to set
         */
        public void setField(String field)
        {
            this.field = field;
        }

        /**
         * @return the formatter
         */
        public String getFormatter()
        {
            return formatter;
        }

        /**
         * @param formatter the formatter to set
         */
        public void setFormatter(String formatter)
        {
            this.formatter = formatter;
        }
        
    }

    /**
     * Creates the administration group under the discipline provided as argument. Also creates
     * one admin user under that group.
     * 
     * @param institution
     * @param username
     * @param email
     * @param password
     * @param userType
     * @return
     */
    public static SpecifyUser createAdminGroupAndUser(final Session     sessionArg,
                                                      final Institution institution, 
                                                      final Collection  collection,
                                                      final String      username,
                                                      final String      email, 
                                                      final String      password, 
                                                      final String      userType) 
    {
        
        SpecifyUser specifyAdminUser = createSpecifyUser(username, email, password, userType);
        sessionArg.saveOrUpdate(specifyAdminUser);
        SpecifyUser spUser = createAdminGroupWithSpUser(sessionArg, institution, collection, specifyAdminUser);
        sessionArg.saveOrUpdate(spUser);
        return spUser;
    }
    
    /**
     * @param institution
     * @param specifyAdminUser
     * @return
     */
    public static SpecifyUser createAdminGroupWithSpUser(final Session sessionArg,
                                                         final Institution institution, 
                                                         final Collection collection, 
                                                         final SpecifyUser specifyAdminUser) 
    {
        SpPrincipal adminGroup = createAdminGroup("Administrator", institution);
        sessionArg.saveOrUpdate(adminGroup);
        specifyAdminUser.addUserToSpPrincipalGroup(adminGroup);
        
        if (collection != null)
        {
            SpPrincipal spPrin = createUserPrincipal(specifyAdminUser, collection);
            sessionArg.saveOrUpdate(spPrin);
        }
        
        return specifyAdminUser;
    }
}
