package edu.ku.brc.specify.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.hibernate.Session;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.AccessionAuthorizations;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.Authors;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAgents;
import edu.ku.brc.specify.datamodel.BorrowMaterial;
import edu.ku.brc.specify.datamodel.BorrowReturnMaterial;
import edu.ku.brc.specify.datamodel.BorrowShipments;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectingTrip;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionObjectCitation;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.ContainerItem;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Deaccession;
import edu.ku.brc.specify.datamodel.DeaccessionAgents;
import edu.ku.brc.specify.datamodel.DeaccessionCollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationCitation;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.GroupPersons;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgents;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityCitation;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.OtherIdentifier;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.Project;
import edu.ku.brc.specify.datamodel.ProjectCollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Stratigraphy;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonCitation;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;

public class DataBuilder
{
    protected static Calendar startCal = Calendar.getInstance();
    protected static Session  session  = null;

    public static Session getSession()
    {
        return session;
    }

    public static void setSession(Session session)
    {
        ObjCreatorHelper.session = session;
    }

    public static AccessionAgents createAccessionAgent(Accession accession, Agent agent)
    {
        AccessionAgents accessionAgent = new AccessionAgents();

        accessionAgent.setTimestampCreated(new Date());
        accessionAgent.setTimestampModified(new Date());
        accessionAgent.setAccession(accession);
        accessionAgent.setAgent(agent);
        persist(accessionAgent);
        return accessionAgent;

    }

    public static Agent createAgent(final String title,
                                    final String firstName,
                                    final String middleInit,
                                    final String lastName,
                                    final String abbreviation)
    {
        // Create Collection Object Definition
        Agent agent = new Agent();
        agent.initialize();
        agent.setAgentType((byte) 1);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setMiddleInitial(middleInit);
        agent.setAbbreviation(abbreviation);
        agent.setTitle(title);

        persist(agent);
        return agent;
    }

    public static AttributeDef createAttributeDef(final AttributeIFace.FieldType type,
                                                  final String name,
                                                  final PrepType prepType)
    {
        AttributeDef attrDef = new AttributeDef();
        attrDef.setDataType(type.getType());
        attrDef.setFieldName(name);
        attrDef.setPrepType(prepType);
        return attrDef;
    }

    public static CollectionObjDef createCollectionObjDef(final String name,
                                                          final String disciplineName,
                                                          final DataType dataType,
                                                          final SpecifyUser user,
                                                          final TaxonTreeDef taxonTreeDef)
    {
        CollectionObjDef colObjDef = new CollectionObjDef();
        colObjDef.initialize();
        colObjDef.setName(name);
        colObjDef.setDiscipline(disciplineName);
        colObjDef.setDataType(dataType);
        colObjDef.setSpecifyUser(user);
        colObjDef.setTaxonTreeDef(taxonTreeDef);

        taxonTreeDef.setCollObjDef(colObjDef);

        persist(colObjDef);
        return colObjDef;
    }

    public static CatalogSeries createCatalogSeries(final String prefix,
                                                    final String name,
                                                    final CollectionObjDef[] colObjDefs)
    {
        CatalogSeries catalogSeries = new CatalogSeries();
        catalogSeries.initialize();
        catalogSeries.setCatalogSeriesPrefix(prefix);
        catalogSeries.setLastEditedBy(null);
        catalogSeries.setRemarks("These are the remarks");
        catalogSeries.setSeriesName(name);

        for (CollectionObjDef cod: colObjDefs)
        {
            catalogSeries.addCollectionObjDefItems(cod);
        }

        persist(catalogSeries);
        return catalogSeries;
    }

    public static CatalogSeries createCatalogSeries(final String prefix,
                                                    final String name,
                                                    final CollectionObjDef colObjDef)
    {
        return createCatalogSeries(prefix, name, new CollectionObjDef[] { colObjDef });
    }

    public static CollectingEvent createCollectingEvent(final Locality locality, final Collectors[] collectors)
    {
        CollectingEvent colEv = new CollectingEvent();
        colEv.initialize();

        HashSet<Collectors> collectorsSet = new HashSet<Collectors>();
        if (collectors != null)
        {
            for (Collectors c: collectors)
            {
                collectorsSet.add(c);
                c.setCollectingEvent(colEv);
            }
        }
        colEv.setCollectors(collectorsSet);
        colEv.setLocality(locality);
        colEv.setTimestampCreated(new Date());
        colEv.setTimestampModified(new Date());

        persist(colEv);
        persist(locality);
        return colEv;
    }
    
    public static CollectingTrip createCollectingTrip(final String remarks, final CollectingEvent[] events)
    {
        CollectingTrip trip = new CollectingTrip();
        trip.initialize();
        trip.setRemarks(remarks);
        Date now = new Date();
        trip.setTimestampCreated(now);
        trip.setTimestampModified(now);

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
        colEvAttr.setCollectingEvent(colEv);
        if (strVal != null)
        {
            colEvAttr.setStrValue(strVal);
        }
        if (dblVal != null)
        {
            colEvAttr.setDblValue(dblVal);
        }
        colEvAttr.setTimestampCreated(new Date());
        colEvAttr.setTimestampModified(new Date());

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
        colObjAttr.setTimestampCreated(new Date());
        colObjAttr.setTimestampModified(new Date());

        colObj.getAttrs().add(colObjAttr);

        persist(colObj);
        return colObjAttr;
    }

    public static Collectors createCollector(final Agent agent, int orderNum)
    {
        Collectors collector = new Collectors();
        collector.initialize();

        collector.setAgent(agent);
        collector.setLastEditedBy(null);
        collector.setOrderNumber(orderNum);
        collector.setRemarks("");
        collector.setTimestampCreated(new Date());
        collector.setTimestampModified(new Date());

        persist(collector);
        return collector;
    }

    public static CollectionObject createCollectionObject(final float catalogNumber,
                                                          final String fieldNumber,
                                                          final Accession accession,
                                                          final Agent cataloger,
                                                          final CatalogSeries catalogSeries,
                                                          final CollectionObjDef colObjDef,
                                                          final int countAmt,
                                                          final CollectingEvent collectingEvent)
    {
        startCal.clear();
        startCal.set(2006, 0, 1);

        // Create Collection Object
        CollectionObject colObj = new CollectionObject();
        colObj.initialize();
        colObj.setAccession(accession);
        colObj.setCataloger(cataloger);
        colObj.setCatalogedDate(startCal);
        colObj.setCatalogedDateVerbatim("Sometime this year");
        colObj.setCatalogNumber(catalogNumber);
        colObj.setCatalogSeries(catalogSeries);
        colObj.setCollectionObjDef(colObjDef);
        colObj.setCollectingEvent(collectingEvent);
        colObj.setContainer(null);
        colObj.setContainerItem(null);
        colObj.setCountAmt(countAmt);
        colObj.setDeaccessioned(false);
        colObj.setDescription("This is the description");

        colObj.setFieldNumber(fieldNumber);
        colObj.setGuid("This is the GUID");
        colObj.setLastEditedBy("rods");
        colObj.setModifier("modifier");
        colObj.setName("The Name!!!!!!");
        colObj.setRemarks("These are the remarks");
        colObj.setYesNo1(false);
        colObj.setYesNo2(true);

        colObj.setTimestampCreated(new Date());
        colObj.setTimestampModified(new Date());

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

    public static DeterminationStatus createDeterminationStatus(final String name, final String remarks)
    {
        DeterminationStatus status = new DeterminationStatus();
        status.initialize();
        status.setName(name);
        status.setRemarks(remarks);

        persist(status);
        return status;
    }

    public static Determination createDetermination(final CollectionObject collectionObject,
                                                    final Agent determiner,
                                                    final Taxon taxon,
                                                    final DeterminationStatus status,
                                                    final Calendar calendar)
    {
        startCal.clear();
        startCal.set(2006, 0, 2);

        // Create Determination
        Determination determination = new Determination();
        determination.initialize();

        determination.setStatus(status);
        determination.setCollectionObject(collectionObject);
        determination.setDeterminedDate(calendar == null ? startCal : calendar);
        determination.setDeterminer(determiner);
        determination.setTaxon(taxon);

        status.getDeterminations().add(determination);
        collectionObject.getDeterminations().add(determination);
        taxon.getDeterminations().add(determination);

        persist(collectionObject);
        persist(determination);
        persist(status);
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
    public static GeographyTreeDef createGeographyTreeDef(final String name)
    {
        GeographyTreeDef gtd = new GeographyTreeDef();
        gtd.initialize();
        gtd.setName(name);

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
        gtdi.setTreeDef(gtd);
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
     * Create a <code>LocationTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the location tree def
     */
    public static LocationTreeDef createLocationTreeDef(final String name)
    {
        LocationTreeDef ltd = new LocationTreeDef();
        ltd.initialize();
        ltd.setName(name);

        persist(ltd);
        return ltd;
    }

    /**
     * Creates a <code>LocationTreeDefItem</code> using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param parent the parent node
     * @param ltd the associated definition object
     * @param name the name of the item
     * @param rankId the rank of the itme
     * @return the new item
     */
    @SuppressWarnings("unchecked")
    public static LocationTreeDefItem createLocationTreeDefItem(final LocationTreeDefItem parent,
                                                                final LocationTreeDef ltd,
                                                                final String name,
                                                                final int rankId)
    {
        LocationTreeDefItem ltdi = new LocationTreeDefItem();
        ltdi.initialize();
        ltdi.setName(name);
        ltdi.setParent(parent);
        ltdi.setRankId(rankId);
        ltdi.setTreeDef(ltd);
        if (ltd != null)
        {
            ltd.getTreeDefItems().add(ltdi);
        }

        persist(ltdi);
        return ltdi;
    }

    /**
     * Creates a new <code>Location</code> object using the given values.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param ltd the associated definition
     * @param parent the parent node
     * @param name the name of the node
     * @param rankId the rank of the node
     * @return the new node
     */
    @SuppressWarnings("unchecked")
    public static Location createLocation(final LocationTreeDef ltd, final Location parent,
    //final String abbrev,
                                          final String name,
                                          //final int highNode,
                                          //final int nodeNum,
                                          final int rankId)
    {
        Location location = new Location();
        location.initialize();
        location.setDefinition(ltd);
        location.setName(name);
        location.setParent(parent);
        LocationTreeDefItem defItem = ltd.getDefItemByRank(rankId);
        if (defItem != null)
        {
            location.setDefinitionItem(defItem);
        }

        location.setRankId(rankId);
        ltd.getTreeEntries().add(location);

        persist(location);
        return location;
    }

    /**
     * Create a <code>GeologicTimePeriodTreeDef</code> with the given name.  The object is also
     * persisted with a call to {@link #persist(Object)}.
     * 
     * @param name tree def name
     * @return the geologic time period tree def
     */
    public static GeologicTimePeriodTreeDef createGeologicTimePeriodTreeDef(final String name)
    {
        GeologicTimePeriodTreeDef gtp = new GeologicTimePeriodTreeDef();
        gtp.initialize();
        gtp.setName(name);

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
                                                              final float startMYA,
                                                              final float endMYA,
                                                              final int rankId)
    {
        GeologicTimePeriod gtp = new GeologicTimePeriod();
        gtp.initialize();
        gtp.setDefinition(gtptd);
        gtp.setDefinition(gtptd);
        gtp.setName(name);
        gtp.setParent(parent);
        gtp.setStart(startMYA);
        gtp.setStartUncertainty(0.0f);
        gtp.setEnd(endMYA);
        gtp.setEndUncertainty(0.0f);
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
    public static TaxonTreeDef createTaxonTreeDef(final String name)
    {
        TaxonTreeDef ttd = new TaxonTreeDef();
        ttd.initialize();
        ttd.setName(name);
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
        ttdi.setTreeDef(ttd);
        if (ttd != null)
        {
            ttd.getTreeDefItems().add(ttdi);
        }

        persist(ttdi);
        return ttdi;
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
        Taxon taxon = new Taxon();
        taxon.initialize();
        taxon.setDefinition(ttd);
        taxon.setName(name);
        taxon.setParent(parent);
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
                                              final int rankId)
    {
        Vector<Object> kids = new Vector<Object>();
        for (int i = 0; i < childNames.length; i++)
        {
            kids.add(createTaxon(treeDef, parent, childNames[i], rankId));
        }
        return kids;
    }

    public static Preparation createPreparation(final PrepType prepType,
                                                final Agent preparedBy,
                                                final CollectionObject colObj,
                                                final Location location,
                                                final int count)
    {
        Preparation prep = new Preparation();
        prep.initialize();

        prep.setCollectionObject(colObj);
        prep.setCount(count);
        prep.setLastEditedBy("");
        prep.setLocation(location);
        prep.setPreparedByAgent(preparedBy);
        prep.setPreparedDate(Calendar.getInstance());
        prep.setPrepType(prepType);
        prep.setRemarks("These are the remarks");
        prep.setStorageLocation("This is the textual storage location");
        prep.setText1("Thi is text1");
        prep.setText2("This is text2");
        prep.setTimestampModified(new Date());

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
        locality.setTimestampModified(new Date());

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
        prepAttr.setTimestampCreated(new Date());
        prepAttr.setTimestampModified(new Date());

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
        Address address = new Address();
        address.initialize();
        address.setAgent(agent);
        address.setTimestampCreated(new Date());
        address.setTimestampModified(new Date());
        address.setAddress(address1);
        address.setAddress2(address2);
        address.setCity(city);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setState(state);

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
        permit.setTimestampCreated(new Date());
        permit.setTimestampModified(new Date());
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

    public static Accession createAccession(final String type,
                                            final String status,
                                            final String number,
                                            final String verbatimDate,
                                            final Calendar dateAccessioned,
                                            final Calendar dateReceived)
    {
        Accession accession = new Accession();
        accession.initialize();
        accession.setNumber(number);
        accession.setVerbatimDate(verbatimDate);
        accession.setDateAccessioned(dateAccessioned);
        accession.setDateReceived(dateReceived);
        accession.setTimestampCreated(new Date());
        accession.setTimestampModified(new Date());
        accession.setStatus(status);
        accession.setType(type);
        persist(accession);
        return accession;
    }

    public static AccessionAgents createAccessionAgent(final String role,
                                                       final Agent agent,
                                                       final Accession accession,
                                                       final RepositoryAgreement repositoryAgreement)
    {
        AccessionAgents accessionagent = new AccessionAgents();
        accessionagent.initialize();
        accessionagent.setTimestampCreated(new Date());
        accessionagent.setTimestampModified(new Date());
        accessionagent.setAccession(accession);
        accessionagent.setRepositoryAgreement(repositoryAgreement);
        accessionagent.setRole(role);
        accessionagent.setAgent(agent);
        persist(accessionagent);
        return accessionagent;
    }

    public static AccessionAuthorizations createAccessionAuthorizations(final Permit permit,
                                                                        final Accession accession,
                                                                        final RepositoryAgreement repositoryAgreement)
    {
        AccessionAuthorizations accessionauthorizations = new AccessionAuthorizations();
        accessionauthorizations.initialize();
        accessionauthorizations.setTimestampCreated(new Date());
        accessionauthorizations.setTimestampModified(new Date());
        accessionauthorizations.setAccession(accession);
        accessionauthorizations.setPermit(permit);
        accessionauthorizations.setRepositoryAgreement(repositoryAgreement);
        persist(accessionauthorizations);
        return accessionauthorizations;
    }

    public static Agent createAgent(final Byte agentType,
                                    final String firstName,
                                    final String lastName,
                                    final String middleInitial,
                                    final String title,
                                    final String interests,
                                    final String abbreviation,
                                    final String name,
                                    final Agent organization)
    {
        Agent agent = new Agent();
        agent.initialize();
        agent.setTimestampCreated(new Date());
        agent.setTimestampModified(new Date());
        agent.setOrganization(organization);
        agent.setAgentType(agentType);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setMiddleInitial(middleInitial);
        agent.setInterests(interests);
        agent.setAbbreviation(abbreviation);
        agent.setName(name);
        agent.setTitle(title);
        persist(agent);
        return agent;
    }

    public static AttributeDef createAttributeDef(final Short tableType,
                                                  final String fieldName,
                                                  final Short dataType,
                                                  final CollectionObjDef collectionObjDef,
                                                  final PrepType prepType)
    {
        AttributeDef attributedef = new AttributeDef();
        attributedef.initialize();
        attributedef.setCollectionObjDef(collectionObjDef);
        attributedef.setPrepType(prepType);
        attributedef.setTableType(tableType);
        attributedef.setFieldName(fieldName);
        attributedef.setDataType(dataType);
        persist(attributedef);
        return attributedef;
    }

    public static Authors createAuthor(final Short orderNumber, final ReferenceWork referenceWork, final Agent agent)
    {
        Authors author = new Authors();
        author.initialize();
        author.setTimestampCreated(new Date());
        author.setTimestampModified(new Date());
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
                                      final Short closed,
                                      final Calendar currentDueDate)
    {
        Borrow borrow = new Borrow();
        borrow.initialize();
        borrow.setTimestampCreated(new Date());
        borrow.setTimestampModified(new Date());
        borrow.setInvoiceNumber(invoiceNumber);
        borrow.setReceivedDate(receivedDate);
        borrow.setOriginalDueDate(originalDueDate);
        borrow.setDateClosed(dateClosed);
        borrow.setCurrentDueDate(currentDueDate);
        borrow.setClosed(closed);
        persist(borrow);
        return borrow;
    }

    public static BorrowAgents createBorrowAgent(final String role, final Agent agent, final Borrow borrow)
    {
        BorrowAgents borrowagent = new BorrowAgents();
        borrowagent.initialize();
        borrowagent.setTimestampCreated(new Date());
        borrowagent.setTimestampModified(new Date());
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
        borrowmaterial.setTimestampCreated(new Date());
        borrowmaterial.setTimestampModified(new Date());
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
        borrowreturnmaterial.setTimestampCreated(new Date());
        borrowreturnmaterial.setTimestampModified(new Date());
        borrowreturnmaterial.setAgent(agent);
        borrowreturnmaterial.setReturnedDate(returnedDate);
        borrowreturnmaterial.setQuantity(quantity);
        borrowreturnmaterial.setBorrowMaterial(borrowMaterial);
        persist(borrowreturnmaterial);
        return borrowreturnmaterial;
    }

    public static BorrowShipments createBorrowShipment(final Shipment shipment, final Borrow borrow)
    {
        BorrowShipments borrowshipment = new BorrowShipments();
        borrowshipment.initialize();
        borrowshipment.setTimestampCreated(new Date());
        borrowshipment.setTimestampModified(new Date());
        borrowshipment.setShipment(shipment);
        borrowshipment.setBorrow(borrow);
        persist(borrowshipment);
        return borrowshipment;
    }

    public static CatalogSeries createCatalogSeries(final Boolean isTissueSeries,
                                                    final String seriesName,
                                                    final String catalogSeriesPrefix,
                                                    final CatalogSeries tissue)
    {
        CatalogSeries catalogseries = new CatalogSeries();
        catalogseries.initialize();
        catalogseries.setTimestampCreated(new Date());
        catalogseries.setTimestampModified(new Date());
        catalogseries.setIsTissueSeries(isTissueSeries);
        catalogseries.setSeriesName(seriesName);
        catalogseries.setCatalogSeriesPrefix(catalogSeriesPrefix);
        catalogseries.setTissue(tissue);
        persist(catalogseries);
        return catalogseries;
    }

    public static CollectingEvent createCollectingEvent(final String stationFieldNumber,
                                                        final String method,
                                                        final String verbatimDate,
                                                        final Calendar startDate,
                                                        final Short startDatePrecision,
                                                        final String startDateVerbatim,
                                                        final Calendar endDate,
                                                        final Short endDatePrecision,
                                                        final String endDateVerbatim,
                                                        final Short startTime,
                                                        final Short endTime,
                                                        final String verbatimLocality,
                                                        final Integer groupPermittedToView,
                                                        final Locality locality,
                                                        final Stratigraphy stratigraphy)
    {
        CollectingEvent collectingevent = new CollectingEvent();
        collectingevent.initialize();
        collectingevent.setVerbatimDate(verbatimDate);
        collectingevent.setTimestampCreated(new Date());
        collectingevent.setTimestampModified(new Date());
        collectingevent.setGroupPermittedToView(groupPermittedToView);
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
        collectingevent.setStratigraphy(stratigraphy);
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
        collectingeventattr.setTimestampCreated(new Date());
        collectingeventattr.setTimestampModified(new Date());
        collectingeventattr.setCollectingEvent(collectingEvent);
        collectingeventattr.setStrValue(strValue);
        collectingeventattr.setDblValue(dblValue);
        collectingeventattr.setDefinition(definition);
        persist(collectingeventattr);
        return collectingeventattr;
    }

    public static CollectionObjDef createCollectionObjDef(final String name,
                                                          final DataType dataType,
                                                          final SpecifyUser user,
                                                          final GeographyTreeDef geographyTreeDef,
                                                          final GeologicTimePeriodTreeDef geologicTimePeriodTreeDef,
                                                          final LocationTreeDef locationTreeDef,
                                                          final TaxonTreeDef taxonTreeDef)
    {
        CollectionObjDef collectionobjdef = new CollectionObjDef();
        collectionobjdef.initialize();
        collectionobjdef.setDataType(dataType);
        collectionobjdef.setSpecifyUser(user);
        collectionobjdef.setGeographyTreeDef(geographyTreeDef);
        collectionobjdef.setGeologicTimePeriodTreeDef(geologicTimePeriodTreeDef);
        collectionobjdef.setLocationTreeDef(locationTreeDef);
        collectionobjdef.setTaxonTreeDef(taxonTreeDef);
        collectionobjdef.setName(name);
        persist(collectionobjdef);
        return collectionobjdef;
    }

    public static CollectionObject createCollectionObject(final String fieldNumber,
                                                          final String description,
                                                          final Integer countAmt,
                                                          final String name,
                                                          final String modifier,
                                                          final Calendar catalogedDate,
                                                          final String catalogedDateVerbatim,
                                                          final String guid,
                                                          final String altCatalogNumber,
                                                          final Integer groupPermittedToView,
                                                          final Boolean deaccessioned,
                                                          final Float catalogNumber,
                                                          final CollectingEvent collectingEvent,
                                                          final ContainerItem containerItem,
                                                          final CollectionObjDef collectionObjDef,
                                                          final CatalogSeries catalogSeries,
                                                          final Accession accession,
                                                          final Agent cataloger,
                                                          final Container container)
    {
        CollectionObject collectionobject = new CollectionObject();
        collectionobject.initialize();
        collectionobject.setTimestampCreated(new Date());
        collectionobject.setTimestampModified(new Date());
        collectionobject.setFieldNumber(fieldNumber);
        collectionobject.setCountAmt(countAmt);
        collectionobject.setModifier(modifier);
        collectionobject.setCatalogedDate(catalogedDate);
        collectionobject.setCatalogedDateVerbatim(catalogedDateVerbatim);
        collectionobject.setGuid(guid);
        collectionobject.setAltCatalogNumber(altCatalogNumber);
        collectionobject.setGroupPermittedToView(groupPermittedToView);
        collectionobject.setDeaccessioned(deaccessioned);
        collectionobject.setCatalogNumber(catalogNumber);
        collectionobject.setCollectingEvent(collectingEvent);
        collectionobject.setContainerItem(containerItem);
        collectionobject.setCollectionObjDef(collectionObjDef);
        collectionobject.setCatalogSeries(catalogSeries);
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
        collectionobjectattr.setTimestampCreated(new Date());
        collectionobjectattr.setTimestampModified(new Date());
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
        collectionobjectcitation.setTimestampCreated(new Date());
        collectionobjectcitation.setTimestampModified(new Date());
        collectionobjectcitation.setCollectionObject(collectionObject);
        collectionobjectcitation.setReferenceWork(referenceWork);
        persist(collectionobjectcitation);
        return collectionobjectcitation;
    }

    public static Collectors createCollector(final Integer orderNumber,
                                             final CollectingEvent collectingEvent,
                                             final Agent agent)
    {
        Collectors collector = new Collectors();
        collector.initialize();
        collector.setTimestampCreated(new Date());
        collector.setTimestampModified(new Date());
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
                                            final Location location)
    {
        Container container = new Container();
        container.initialize();
        container.setNumber(number);
        container.setTimestampCreated(new Date());
        container.setTimestampModified(new Date());
        container.setContainer(colObj);
        container.setName(name);
        container.setLocation(location);
        container.setDescription(description);
        container.setType(type);
        persist(container);
        return container;
    }

    public static ContainerItem createContainerItem(final Container container)
    {
        ContainerItem containeritem = new ContainerItem();
        containeritem.initialize();
        containeritem.setTimestampCreated(new Date());
        containeritem.setTimestampModified(new Date());
        containeritem.setContainer(container);
        persist(containeritem);
        return containeritem;
    }

    public static DataType createDataType(final String name)
    {
        DataType datatype = new DataType();
        datatype.initialize();
        datatype.setName(name);
        persist(datatype);
        return datatype;
    }

    public static Deaccession createDeaccession(final String type,
                                                final String deaccessionNumber,
                                                final Calendar deaccessionDate)
    {
        Deaccession deaccession = new Deaccession();
        deaccession.initialize();
        deaccession.setTimestampCreated(new Date());
        deaccession.setTimestampModified(new Date());
        deaccession.setDeaccessionNumber(deaccessionNumber);
        deaccession.setDeaccessionDate(deaccessionDate);
        deaccession.setType(type);
        persist(deaccession);
        return deaccession;
    }

    public static DeaccessionAgents createDeaccessionAgent(final String role,
                                                           final Agent agent,
                                                           final Deaccession deaccession)
    {
        DeaccessionAgents deaccessionagent = new DeaccessionAgents();
        deaccessionagent.initialize();
        deaccessionagent.setTimestampCreated(new Date());
        deaccessionagent.setTimestampModified(new Date());
        deaccessionagent.setRole(role);
        deaccessionagent.setAgent(agent);
        deaccessionagent.setDeaccession(deaccession);
        persist(deaccessionagent);
        return deaccessionagent;
    }

    public static DeaccessionCollectionObject createDeaccessionCollectionObject(final Short quantity,
                                                                                final CollectionObject collectionObjectCatalog,
                                                                                final Deaccession deaccession)
    {
        DeaccessionCollectionObject deaccessioncollectionobject = new DeaccessionCollectionObject();
        deaccessioncollectionobject.initialize();
        deaccessioncollectionobject.setTimestampCreated(new Date());
        deaccessioncollectionobject.setTimestampModified(new Date());
        deaccessioncollectionobject.setQuantity(quantity);
        deaccessioncollectionobject.setDeaccession(deaccession);
        deaccessioncollectionobject.setCollectionObjectCatalog(collectionObjectCatalog);
        persist(deaccessioncollectionobject);
        return deaccessioncollectionobject;
    }

    public static Determination createDetermination(final DeterminationStatus status,
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
        determination.setTimestampCreated(new Date());
        determination.setTimestampModified(new Date());
        determination.setStatus(status);
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
        determinationcitation.setTimestampCreated(new Date());
        determinationcitation.setTimestampModified(new Date());
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
     exchangein.setTimestampCreated(new Date());
     exchangein.setTimestampModified(new Date());
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
     exchangeout.setTimestampCreated(new Date());
     exchangeout.setTimestampModified(new Date());
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

    public static GroupPersons createGroupPerson(final Short orderNumber,
                                                 final Agent agentByGroup,
                                                 final Agent agentByMember)
    {
        GroupPersons groupperson = new GroupPersons();
        groupperson.initialize();
        groupperson.setTimestampCreated(new Date());
        groupperson.setTimestampModified(new Date());
        groupperson.setOrderNumber(orderNumber);
        groupperson.setAgentByGroup(agentByGroup);
        groupperson.setAgentByMember(agentByMember);
        persist(groupperson);
        return groupperson;
    }

    public static InfoRequest createInfoRequest(final Long infoRequestID,
                                                final String firstName,
                                                final String lastName,
                                                final String institution,
                                                final String email,
                                                final Calendar requestDate,
                                                final Calendar replyDate,
                                                final RecordSetIFace recordSet,
                                                final Agent agent)
    {
        InfoRequest inforequest = new InfoRequest();
        inforequest.initialize();
        inforequest.setTimestampCreated(new Date());
        inforequest.setTimestampModified(new Date());
        inforequest.setEmail(email);
        inforequest.setAgent(agent);
        inforequest.setFirstName(firstName);
        inforequest.setLastName(lastName);
        inforequest.setInfoRequestID(infoRequestID);
        inforequest.setInstitution(institution);
        inforequest.setRequestDate(requestDate);
        inforequest.setReplyDate(replyDate);
        inforequest.setRecordSet(recordSet);
        persist(inforequest);
        return inforequest;
    }

    public static Journal createJournal(final String journalName, final String journalAbbreviation)
    {
        Journal journal = new Journal();
        journal.initialize();
        journal.setTimestampCreated(new Date());
        journal.setTimestampModified(new Date());
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
                                  final Byte category,
                                  final Short closed,
                                  final Shipment shipment)
    {
        Loan loan = new Loan();
        loan.initialize();
        loan.setTimestampCreated(new Date());
        loan.setTimestampModified(new Date());
        loan.setShipment(shipment);
        loan.setOriginalDueDate(originalDueDate);
        loan.setDateClosed(dateClosed);
        loan.setCurrentDueDate(currentDueDate);
        loan.setLoanNumber(loanNumber);
        loan.setLoanDate(loanDate);
        loan.setCategory(category);
        loan.setClosed(closed);
        persist(loan);
        return loan;
    }

    public static LoanAgents createLoanAgent(final String role, final Loan loan, final Agent agent)
    {
        LoanAgents loanagent = new LoanAgents();
        loanagent.initialize();
        loanagent.setTimestampCreated(new Date());
        loanagent.setTimestampModified(new Date());
        loanagent.setRole(role);
        loanagent.setAgent(agent);
        loanagent.setLoan(loan);
        persist(loanagent);
        return loanagent;
    }

    public static LoanPhysicalObject createLoanPhysicalObject(final Short quantity,
                                                              final String descriptionOfMaterial,
                                                              final String outComments,
                                                              final String inComments,
                                                              final Short quantityResolved,
                                                              final Short quantityReturned,
                                                              final Preparation preparation,
                                                              final Loan loan)
    {
        LoanPhysicalObject loanphysicalobject = new LoanPhysicalObject();
        loanphysicalobject.initialize();
        loanphysicalobject.setTimestampCreated(new Date());
        loanphysicalobject.setTimestampModified(new Date());
        loanphysicalobject.setDescriptionOfMaterial(descriptionOfMaterial);
        loanphysicalobject.setQuantity(quantity);
        loanphysicalobject.setLoan(loan);
        loanphysicalobject.setPreparation(preparation);
        loanphysicalobject.setOutComments(outComments);
        loanphysicalobject.setInComments(inComments);
        loanphysicalobject.setQuantityResolved(quantityResolved);
        loanphysicalobject.setQuantityReturned(quantityReturned);
        persist(loanphysicalobject);
        return loanphysicalobject;
    }

    public static LoanReturnPhysicalObject createLoanReturnPhysicalObject(final Calendar returnedDate,
                                                                          final Short quantity,
                                                                          final LoanPhysicalObject loanPhysicalObject,
                                                                          final DeaccessionCollectionObject deaccessionCollectionObject,
                                                                          final Agent agent)
    {
        LoanReturnPhysicalObject loanreturnphysicalobject = new LoanReturnPhysicalObject();
        loanreturnphysicalobject.initialize();
        loanreturnphysicalobject.setTimestampCreated(new Date());
        loanreturnphysicalobject.setTimestampModified(new Date());
        loanreturnphysicalobject.setAgent(agent);
        loanreturnphysicalobject.setReturnedDate(returnedDate);
        loanreturnphysicalobject.setQuantity(quantity);
        loanreturnphysicalobject.setLoanPhysicalObject(loanPhysicalObject);
        loanreturnphysicalobject.setDeaccessionCollectionObject(deaccessionCollectionObject);
        persist(loanreturnphysicalobject);
        return loanreturnphysicalobject;
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
                                          final Double minElevation,
                                          final Double maxElevation,
                                          final String elevationMethod,
                                          final Double elevationAccuracy,
                                          final Integer originalLatLongUnit,
                                          final String latLongType,
                                          final Double latitude1,
                                          final Double longitude1,
                                          final Double latitude2,
                                          final Double longitude2,
                                          final String latLongMethod,
                                          final Double latLongAccuracy,
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
        locality.setTimestampCreated(new Date());
        locality.setTimestampModified(new Date());
        locality.setGroupPermittedToView(groupPermittedToView);
        locality.setNamedPlace(namedPlace);
        locality.setRelationToNamedPlace(relationToNamedPlace);
        locality.setLocalityName(localityName);
        locality.setBaseMeridian(baseMeridian);
        locality.setRange(range);
        locality.setRangeDirection(rangeDirection);
        locality.setTownship(township);
        locality.setTownshipDirection(townshipDirection);
        locality.setSection(section);
        locality.setSectionPart(sectionPart);
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
        locality.setNationalParkName(nationalParkName);
        locality.setIslandGroup(islandGroup);
        locality.setIsland(island);
        locality.setWaterBody(waterBody);
        locality.setDrainage(drainage);
        locality.setGeography(geography);
        persist(locality);
        return locality;
    }

    public static LocalityCitation createLocalityCitation(final ReferenceWork referenceWork, final Locality locality)
    {
        LocalityCitation localitycitation = new LocalityCitation();
        localitycitation.initialize();
        localitycitation.setTimestampCreated(new Date());
        localitycitation.setTimestampModified(new Date());
        localitycitation.setReferenceWork(referenceWork);
        localitycitation.setLocality(locality);
        persist(localitycitation);
        return localitycitation;
    }

    public static OtherIdentifier createOtherIdentifier(final String identifier, final CollectionObject collectionObject)
    {
        OtherIdentifier otheridentifier = new OtherIdentifier();
        otheridentifier.initialize();
        otheridentifier.setTimestampCreated(new Date());
        otheridentifier.setTimestampModified(new Date());
        otheridentifier.setCollectionObject(collectionObject);
        otheridentifier.setIdentifier(identifier);
        persist(otheridentifier);
        return otheridentifier;
    }

    public static PrepType createPrepType(final String name)
    {
        PrepType preptype = new PrepType();
        preptype.initialize();
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
                                                final Location location)
    {
        Preparation preparation = new Preparation();
        preparation.initialize();
        preparation.setTimestampCreated(new Date());
        preparation.setTimestampModified(new Date());
        preparation.setCount(count);
        preparation.setStorageLocation(storageLocation);
        preparation.setPreparedDate(preparedDate);
        preparation.setPrepType(prepType);
        preparation.setCollectionObject(collectionObject);
        preparation.setPreparedByAgent(preparedByAgent);
        preparation.setLocation(location);
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
        preparationattr.setTimestampCreated(new Date());
        preparationattr.setTimestampModified(new Date());
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
        project.setTimestampCreated(new Date());
        project.setTimestampModified(new Date());
        project.setUrl(url);
        project.setAgent(agent);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setProjectName(projectName);
        project.setProjectDescription(projectDescription);
        persist(project);
        return project;
    }

    public static ProjectCollectionObject createProjectCollectionObject(final CollectionObject collectionObject,
                                                                        final Project project)
    {
        ProjectCollectionObject projectcollectionobject = new ProjectCollectionObject();
        projectcollectionobject.initialize();
        projectcollectionobject.setTimestampCreated(new Date());
        projectcollectionobject.setTimestampModified(new Date());
        projectcollectionobject.setCollectionObject(collectionObject);
        projectcollectionobject.setProject(project);
        persist(projectcollectionobject);
        return projectcollectionobject;
    }

    public static RecordSetIFace createRecordSet(final Long recordSetID, final String name, final SpecifyUser owner)
    {
        RecordSet recordset = new RecordSet();
        recordset.initialize();
        recordset.setTimestampCreated(new Date());
        recordset.setTimestampModified(new Date());
        recordset.setRecordSetId(recordSetID);
        recordset.setOwner(owner);
        recordset.setName(name);
        persist(recordset);
        return recordset;
    }

    public static RecordSetItemIFace createRecordSetItem()
    {
        RecordSetItem recordsetitem = new RecordSetItem();
        recordsetitem.initialize();
        persist(recordsetitem);
        return recordsetitem;
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
                                                    final Short published,
                                                    final Journal journal)
    {
        ReferenceWork referencework = new ReferenceWork();
        referencework.initialize();
        referencework.setTimestampCreated(new Date());
        referencework.setTimestampModified(new Date());
        referencework.setUrl(url);
        referencework.setReferenceWorkType(referenceWorkType);
        referencework.setPublisher(publisher);
        referencework.setPlaceOfPublication(placeOfPublication);
        referencework.setWorkDate(workDate);
        referencework.setVolume(volume);
        referencework.setPages(pages);
        referencework.setLibraryNumber(libraryNumber);
        referencework.setPublished(published);
        referencework.setJournal(journal);
        referencework.setTitle(title);
        persist(referencework);
        return referencework;
    }

    public static RepositoryAgreement createRepositoryAgreement(final String number,
                                                                final String status,
                                                                final Calendar startDate,
                                                                final Calendar endDate,
                                                                final Calendar dateReceived,
                                                                final Agent originator)
    {
        RepositoryAgreement repositoryagreement = new RepositoryAgreement();
        repositoryagreement.initialize();
        repositoryagreement.setNumber(number);
        repositoryagreement.setDateReceived(dateReceived);
        repositoryagreement.setTimestampCreated(new Date());
        repositoryagreement.setTimestampModified(new Date());
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
                                          final Agent agentAddressByShipper,
                                          final Agent agentAddressByShippedTo,
                                          final Agent agent)
    {
        Shipment shipment = new Shipment();
        shipment.initialize();
        shipment.setTimestampCreated(new Date());
        shipment.setTimestampModified(new Date());
        shipment.setAgent(agent);
        shipment.setShipmentDate(shipmentDate);
        shipment.setShipmentNumber(shipmentNumber);
        shipment.setShipmentMethod(shipmentMethod);
        shipment.setNumberOfPackages(numberOfPackages);
        shipment.setWeight(weight);
        shipment.setInsuredForAmount(insuredForAmount);
        shipment.setAgentByShipper(agentAddressByShipper);
        shipment.setAgentByShippedTo(agentAddressByShippedTo);
        persist(shipment);
        return shipment;
    }

    public static SpecifyUser createSpecifyUser(final String name,
                                                final String email,
                                                final Short privLevel,
                                                final UserGroup userGroup,
                                                final String userType)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setEmail(email);
        specifyuser.setPrivLevel(privLevel);
        specifyuser.setUserGroup(userGroup);
        specifyuser.setName(name);
        specifyuser.setUserType(userType);
        persist(specifyuser);
        return specifyuser;
    }

    public static Stratigraphy createStratigraphy(final String superGroup,
                                                  final String lithoGroup,
                                                  final String formation,
                                                  final String member,
                                                  final String bed,
                                                  final CollectingEvent collectingEvent)
    {
        Stratigraphy stratigraphy = new Stratigraphy();
        stratigraphy.initialize();
        stratigraphy.setTimestampCreated(new Date());
        stratigraphy.setTimestampModified(new Date());
        stratigraphy.setCollectingEvent(collectingEvent);
        stratigraphy.setSuperGroup(superGroup);
        stratigraphy.setLithoGroup(lithoGroup);
        stratigraphy.setFormation(formation);
        stratigraphy.setMember(member);
        stratigraphy.setBed(bed);
        persist(stratigraphy);
        return stratigraphy;
    }

    public static TaxonCitation createTaxonCitation(final ReferenceWork referenceWork, final Taxon taxon)
    {
        TaxonCitation taxoncitation = new TaxonCitation();
        taxoncitation.initialize();
        taxoncitation.setTimestampCreated(new Date());
        taxoncitation.setTimestampModified(new Date());
        taxoncitation.setTaxon(taxon);
        taxoncitation.setReferenceWork(referenceWork);
        persist(taxoncitation);
        return taxoncitation;
    }

    public static UserGroup createUserGroup(final String name)
    {
        UserGroup usergroup = new UserGroup();
        usergroup.initialize();
        usergroup.setName(name);
        persist(usergroup);
        return usergroup;
    }

    public static Workbench createWorkbench(final String name, final String remarks, final String exportInstName,
    //final Integer formId,
                                            final WorkbenchTemplate workbenchTemplate)
    {
        Workbench workbench = new Workbench();
        workbench.initialize();
        workbench.setName(name);
        workbench.setRemarks(remarks);
        workbench.setExportInstitutionName(exportInstName);
        //workbench.setFormid(formId);
        workbench.setTimestampCreated(new Date());
        workbench.setTimestampModified(new Date());
        workbench.setWorkbenchItems(new HashSet<WorkbenchDataItem>());
        workbench.setWorkbenchTemplates(workbenchTemplate);

        persist(workbench);

        return workbench;
    }

    public static WorkbenchDataItem createWorkbenchDataItem(final String rowNumber,
                                                            final String columnNumber,
                                                            final String cellData,
                                                            final Workbench workbench)
    {
        WorkbenchDataItem wbdi = new WorkbenchDataItem();
        wbdi.initialize();

        wbdi.setRowNumber(rowNumber);
        wbdi.setColumnNumber(columnNumber);
        //wbdi.setRowOfData(rowData);
        wbdi.setCellData(cellData);
        wbdi.setOwner(workbench);

        persist(wbdi);

        return wbdi;
    }

    public static WorkbenchTemplate createWorkbenchTemplate(final String name, final String remarks)
    {
        WorkbenchTemplate wbt = new WorkbenchTemplate();
        wbt.initialize();

        wbt.setName(name);
        wbt.setRemarks(remarks);

        persist(wbt);

        return wbt;
    }

    public static WorkbenchTemplateMappingItem createMappingItem(final String tableName,
                                                                 final Integer tableId,
                                                                 final String fieldName,
                                                                 final String caption,
                                                                 final String dataType,
                                                                 final Integer viewOrder,
                                                                 final WorkbenchTemplate template)
    {
        WorkbenchTemplateMappingItem wtmi = new WorkbenchTemplateMappingItem();
        wtmi.initialize();

        wtmi.setCaption(caption);
        wtmi.setDatatype(dataType);
        wtmi.setFieldname(fieldName);
        wtmi.setTablename(tableName);
        wtmi.setVieworder(viewOrder);
        wtmi.setWorkbenchTemplates(template);
        wtmi.setTableid(tableId);

        persist(wtmi);

        return wtmi;
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

}
