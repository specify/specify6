package edu.ku.brc.specify.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.AccessionAuthorizations;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAddress;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.Author;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAgent;
import edu.ku.brc.specify.datamodel.BorrowMaterial;
import edu.ku.brc.specify.datamodel.BorrowReturnMaterial;
import edu.ku.brc.specify.datamodel.BorrowShipment;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionObjectCitation;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.ContainerItem;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Deaccession;
import edu.ku.brc.specify.datamodel.DeaccessionAgent;
import edu.ku.brc.specify.datamodel.DeaccessionCollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationCitation;
import edu.ku.brc.specify.datamodel.ExchangeIn;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.ExternalResource;
import edu.ku.brc.specify.datamodel.ExternalResourceAttr;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.GroupPerson;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgent;
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

public class ObjCreatorHelper
{
    protected static Calendar startCal = Calendar.getInstance();
    protected static Session  session = null;



    public static Session getSession()
    {
        return session;
    }

    public static void setSession(Session session)
    {
        ObjCreatorHelper.session = session;
    }

    public static AccessionAgent createAccessionAgent()
    {
        AccessionAgent accessionAgent = new AccessionAgent();

        accessionAgent.setTimestampCreated(new Date());
        accessionAgent.setTimestampModified(new Date());
        return accessionAgent;

    }

    public static AgentAddress createAgentAddress(final Agent agent,
                                                  final String phone,
                                                  final String jobTitle,
                                                  final Address address)
    {
        AgentAddress agentAddress = new AgentAddress();
        agentAddress.initialize();
        agentAddress.setAddress(address);
        agentAddress.setAgent(agent);
        agentAddress.setIsCurrent(true);
        agentAddress.setJobTitle(jobTitle);
        agentAddress.setPhone1(phone);
        agentAddress.setTypeOfAgentAddressed((short)1);
        agentAddress.setUrl(null);
        agentAddress.setTimestampModified(new Date());

        return agentAddress;
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
        agent.setAgentType((byte)1);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setMiddleInitial(middleInit);
        agent.setAbbreviation(abbreviation);
        agent.setTitle(title);

        if (session != null)
        {
            session.saveOrUpdate(agent);
        }
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
        if (session != null)
        {
            session.saveOrUpdate(attrDef);
        }
        return attrDef;
    }

    public static CollectionObjDef createCollectionObjDef(final String name,
                                                          final DataType dataType,
                                                          final SpecifyUser user,
                                                          final TaxonTreeDef taxonTreeDef)
    {
        CollectionObjDef colObjDef = new CollectionObjDef();
        colObjDef.initialize();
        colObjDef.setName(name);
        colObjDef.setDataType(dataType);
        colObjDef.setSpecifyUser(user);
        colObjDef.setTaxonTreeDef(taxonTreeDef);

        taxonTreeDef.setCollObjDef(colObjDef);

        if (session != null)
        {
            //session.persist(taxonTreeDef);
            session.persist(colObjDef);
        }
        return colObjDef;
    }

    public static CatalogSeries createCatalogSeries(final String prefix, final String name)
    {
        CatalogSeries catalogSeries = new CatalogSeries();
        catalogSeries.initialize();
        catalogSeries.setCatalogSeriesPrefix(prefix);
        catalogSeries.setLastEditedBy(null);
        catalogSeries.setRemarks("These are the remarks");
        catalogSeries.setSeriesName(name);
        catalogSeries.setTimestampCreated(new Date());
        catalogSeries.setTimestampModified(new Date());
        if (session != null)
        {
            session.saveOrUpdate(catalogSeries);
        }
        return catalogSeries;
    }

    public static CollectingEvent createCollectingEvent(final Locality locality,
                                                        final Collector[] collectors)
    {
        CollectingEvent colEv = new CollectingEvent();
        colEv.initialize();

        startCal.clear();
        startCal.set(2006, 0, 1);
        colEv.setStartDate(startCal);

        startCal.clear();
        startCal.set(2006, 0, 2);
        colEv.setEndDate(startCal);


        HashSet<Collector> collectorsSet = new HashSet<Collector>();
        if (collectors != null)
        {
            for (Collector c : collectors)
            {
                collectorsSet.add(c);
                c.setCollectingEvent(colEv);
            }
        }
        colEv.setCollectors(collectorsSet);
        colEv.setLocality(locality);
        colEv.setTimestampCreated(new Date());
        colEv.setTimestampModified(new Date());

        if (session != null)
        {
            session.saveOrUpdate(colEv);
            session.saveOrUpdate(locality);
        }
        return colEv;
    }

    public static CollectingEventAttr createCollectingEventAttr(final CollectingEvent  colEv,
                                                                final AttributeDef     colObjAttrDef,
                                                                final String           strVal,
                                                                final Double           dblVal)
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

        if (session != null)
        {
            session.saveOrUpdate(colEv);
        }
        return colEvAttr;
    }

    public static CollectionObjectAttr createCollectionObjectAttr(final CollectionObject colObj,
                                                                  final AttributeDef     colObjAttrDef,
                                                                  final String           strVal,
                                                                  final Double           dblVal)
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

        if (session != null)
        {
            session.saveOrUpdate(colObj);
        }
        return colObjAttr;
    }

    public static Collector createCollector(final Agent agent, int orderNum)
    {
        Collector collector = new Collector();
        collector.initialize();

        collector.setAgent(agent);
        collector.setLastEditedBy(null);
        collector.setOrderNumber(orderNum);
        collector.setRemarks("");
        collector.setTimestampCreated(new Date());
        collector.setTimestampModified(new Date());

        if (session != null)
        {
            session.saveOrUpdate(collector);
        }
        return collector;
    }

    /**
     * Creates a CollectionObject
     * @param catalogNumber catalogNumber
     * @param cataloger cataloger
     * @param catalogSeries catalogSeries
     * @param colObjDef catalogSeries
     * @return CollectionObject
     */
    public static CollectionObject createCollectionObject(final float            catalogNumber,
                                                          final String           fieldNumber,
                                                          final Accession        accession,
                                                          final Agent            cataloger,
                                                          final CatalogSeries    catalogSeries,
                                                          final CollectionObjDef colObjDef,
                                                          final int              countAmt,
                                                          final CollectingEvent  collectingEvent)
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

        if (session != null)
        {
            session.saveOrUpdate(colObj);
            if (collectingEvent != null)
            {
                session.saveOrUpdate(collectingEvent);
            }
        }
        return colObj;
    }


    public static Determination createDetermination(final CollectionObject collectionObject,
                                                       final Agent            determiner,
                                                       final Taxon            taxon,
                                                       final boolean          isCurrent,
                                                       final Calendar         calendar)
    {
        startCal.clear();
        startCal.set(2006, 0, 2);

        // Create Determination
        Determination determination = new Determination();
        determination.initialize();

        determination.setIsCurrent(isCurrent);
        determination.setCollectionObject(collectionObject);
        determination.setDeterminedDate(calendar == null ? startCal : calendar);
        determination.setDeterminer(determiner);
        determination.setTaxon(taxon);
        determination.setTimestampCreated(new Date());
        determination.setTimestampModified(new Date());

        collectionObject.getDeterminations().add(determination);

        if (session != null)
        {
            session.saveOrUpdate(collectionObject);
            session.saveOrUpdate(determination);
        }
        return determination;

    }

    public static GeographyTreeDef createGeographyTreeDef(final String name)
    {
        GeographyTreeDef gtd = new GeographyTreeDef();
        gtd.initialize();
        gtd.setName(name);

        saveOrUpdate(gtd);
        return gtd;
    }

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
        if( gtd != null )
        {
        	gtd.getTreeDefItems().add(gtdi);
        }

        saveOrUpdate(gtdi);
        return gtdi;
    }

    @SuppressWarnings("unchecked")
	public static Geography createGeography(final GeographyTreeDef gtd,
                                            final Geography parent,
                                            //final String abbrev,
                                            final String name,
                                            //final int highNode,
                                            //final int nodeNum,
                                            final int rankId)
    {
        Geography geography = new Geography();
        geography.initialize();
        //geography.setAbbrev(abbrev);
        geography.setDefinition(gtd);
        geography.setName(name);
        //geography.setHighestChildNodeNumber(highNode);
        //geography.setNodeNumber(nodeNum);
        geography.setParent(parent);
        geography.setRankId(rankId);
        if (gtd != null)
        {
            gtd.getTreeEntries().add(geography);
        }

        saveOrUpdate(geography);
        return geography;
    }



    public static Locality createLocality(final String name, final Geography geo)
    {
        Locality locality = new Locality();
        locality.initialize();

        locality.setLocalityName(name);
        locality.setGeography(geo);
        locality.setTimestampModified(new Date());

        if (session != null)
        {
            session.saveOrUpdate(locality);
        }
        return locality;
    }

    public static LocationTreeDef createLocationTreeDef(final String name)
    {
        LocationTreeDef ltd = new LocationTreeDef();
        ltd.initialize();
        ltd.setName(name);

        saveOrUpdate(ltd);
        return ltd;
    }

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
        if( ltd != null )
        {
            ltd.getTreeDefItems().add(ltdi);
        }

        saveOrUpdate(ltdi);
        return ltdi;
    }

    @SuppressWarnings("unchecked")
	public static Location createLocation(final LocationTreeDef ltd,
                                          final Location parent,
                                          //final String abbrev,
                                          final String name,
                                          //final int highNode,
                                          //final int nodeNum,
                                          final int rankId)
    {
        Location location = new Location();
        location.initialize();

        //location.setAbbrev(abbrev);
        location.setDefinition(ltd);
        location.setName(name);
        //location.setHighestChildNodeNumber(highNode);
        //location.setNodeNumber(nodeNum);
        location.setParent(parent);
        location.setRankId(rankId);
        if (ltd != null)
        {
            ltd.getTreeEntries().add(location);
        }
        
        saveOrUpdate(location);
        return location;
    }

    public static GeologicTimePeriodTreeDef createGeologicTimePeriodTreeDef(final String name)
    {
        GeologicTimePeriodTreeDef gtp = new GeologicTimePeriodTreeDef();
        gtp.initialize();
        gtp.setName(name);

        saveOrUpdate(gtp);
        return gtp;
    }

    @SuppressWarnings("unchecked")
	public static GeologicTimePeriodTreeDefItem createGeologicTimePeriodTreeDefItem(final GeologicTimePeriodTreeDefItem parent,
                                                                                    final GeologicTimePeriodTreeDef gltptd,
                                                                                    final String name,
                                                                                    final int rankId)
    {
        GeologicTimePeriodTreeDefItem gtdi = new GeologicTimePeriodTreeDefItem();
        gtdi.initialize();
        gtdi.setName(name);
        gtdi.setParent(parent);
        gtdi.setRankId(rankId);
        gtdi.setTreeDef(gltptd);
        if( gltptd != null )
        {
        	gltptd.getTreeDefItems().add(gtdi);
        }

        saveOrUpdate(gtdi);
        return gtdi;
    }

    @SuppressWarnings("unchecked")
	public static GeologicTimePeriod createGeologicTimePeriod(final GeologicTimePeriodTreeDef gtptd,
                                                              final GeologicTimePeriod parent,
                                                              final String name,
                                                              final int rankId)
   {
        GeologicTimePeriod gtp = new GeologicTimePeriod();
        gtp.initialize();
        gtp.setTreeDef(gtptd);
        gtp.setDefinition(gtptd);
        gtp.setName(name);
        gtp.setParent(parent);
        gtp.setRankId(rankId);
        if (gtptd != null)
        {
            gtptd.getTreeEntries().add(gtp);
        }

        saveOrUpdate(gtp);
        return gtp;
    }

    public static Preparation createPreparation(final PrepType         prepType,
                                                final Agent            preparedBy,
                                                final CollectionObject colObj,
                                                final Location         location,
                                                final int              count)
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

        if (session != null)
        {
            session.saveOrUpdate(prep);
        }
        return prep;
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
        if (dblVal != null)
        {
            prepAttr.setDblValue(dblVal);
        }
        prepAttr.setTimestampCreated(new Date());
        prepAttr.setTimestampModified(new Date());

        prep.getAttrs().add(prepAttr);
        if (session != null)
        {
            session.saveOrUpdate(prepAttr);
        }
        return prepAttr;
    }

    public static TaxonTreeDef createTaxonTreeDef(final String name)
    {
        TaxonTreeDef ttd = new TaxonTreeDef();
        ttd.initialize();
        ttd.setName(name);
        return ttd;
    }

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
        if( ttd != null )
        {
        	ttd.getTreeDefItems().add(ttdi);
        }

        saveOrUpdate(ttdi);
        return ttdi;
    }

    @SuppressWarnings("unchecked")
	public static Taxon createTaxon(final TaxonTreeDef ttd,
                                      final Taxon parent,
                                      //final String abbrev,
                                      final String name,
                                      //final int highNode,
                                      //final int nodeNum,
                                      final int rankId)
    {
        Taxon taxon = new Taxon();
        taxon.initialize();
        //taxon.setAbbrev(abbrev);
        taxon.setDefinition(ttd);
        taxon.setName(name);
        //taxon.setHighestChildNodeNumber(highNode);
        //taxon.setNodeNumber(nodeNum);
        taxon.setParent(parent);
        taxon.setRankId(rankId);
        if (ttd != null)
        {
            ttd.getTreeEntries().add(taxon);
        }

        saveOrUpdate(taxon);
        return taxon;
    }


    public static Address createAddress(final AgentAddress agentAddress,
                                        final String address1,
                                        final String address2,
                                        final String city,
                                        final String state,
                                        final String country,
                                        final String postalCode)
    {
        Address address = new Address();
        address.initialize();
        address.getAgentAddresses().add(agentAddress);
        address.setTimestampCreated(new Date());
        address.setTimestampModified(new Date());
        address.setAddress(address1);
        address.setAddress2(address2);
        address.setCity(city);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setState(state);

        agentAddress.setAddress(address);
        if (session != null)
        {
            session.saveOrUpdate(address);
            session.saveOrUpdate(agentAddress);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(permit);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(accession);
        }
        return accession;
    }

    public static AccessionAgent createAccessionAgent(final String role,
                                                      final AgentAddress agentAddress,
                                                      final Accession accession,
                                                      final RepositoryAgreement repositoryAgreement)
    {
        AccessionAgent accessionagent = new AccessionAgent();
        accessionagent.initialize();
        accessionagent.setTimestampCreated(new Date());
        accessionagent.setTimestampModified(new Date());
        accessionagent.setAccession(accession);
        accessionagent.setRepositoryAgreement(repositoryAgreement);
        accessionagent.setRole(role);
        accessionagent.setAgentAddress(agentAddress);
        if (session != null)
        {
          session.saveOrUpdate(accessionagent);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(accessionauthorizations);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(agent);
        }
        return agent;
    }

    public static AgentAddress createAgentAddress(final Short typeOfAgentAddressed,
                                                  final String jobTitle,
                                                  final String phone1,
                                                  final String phone2,
                                                  final String fax,
                                                  final String roomOrBuilding,
                                                  final String email,
                                                  final String url,
                                                  final Boolean isCurrent,
                                                  final Agent organization,
                                                  final Agent agent,
                                                  final Address address)
    {
        AgentAddress agentaddress = new AgentAddress();
        agentaddress.initialize();
        agentaddress.setTimestampCreated(new Date());
        agentaddress.setTimestampModified(new Date());
        agentaddress.setTypeOfAgentAddressed(typeOfAgentAddressed);
        agentaddress.setJobTitle(jobTitle);
        agentaddress.setPhone1(phone1);
        agentaddress.setPhone2(phone2);
        agentaddress.setFax(fax);
        agentaddress.setRoomOrBuilding(roomOrBuilding);
        agentaddress.setEmail(email);
        agentaddress.setUrl(url);
        agentaddress.setIsCurrent(isCurrent);
        agentaddress.setOrganization(organization);
        agentaddress.setAgent(agent);
        agentaddress.setAddress(address);

        if (address != null)
        {
            address.getAgentAddresses().add(agentaddress);
        }
        agent.getAgentAddressesByAgent().add(agentaddress);
        if (session != null)
        {
            session.saveOrUpdate(agent);
            session.saveOrUpdate(agentaddress);
            if (address != null)
            {
                session.saveOrUpdate(address);
            }
        }
        return agentaddress;
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
        if (session != null)
        {
          session.saveOrUpdate(attributedef);
        }
        return attributedef;
    }

    public static Author createAuthor(final Short orderNumber,
                                      final ReferenceWork referenceWork,
                                      final Agent agent)
    {
        Author author = new Author();
        author.initialize();
        author.setTimestampCreated(new Date());
        author.setTimestampModified(new Date());
        author.setAgent(agent);
        author.setOrderNumber(orderNumber);
        author.setReferenceWork(referenceWork);
        if (session != null)
        {
          session.saveOrUpdate(author);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(borrow);
        }
        return borrow;
    }

    public static BorrowAgent createBorrowAgent(final String role,
                                                final AgentAddress agentAddress,
                                                final Borrow borrow)
    {
        BorrowAgent borrowagent = new BorrowAgent();
        borrowagent.initialize();
        borrowagent.setTimestampCreated(new Date());
        borrowagent.setTimestampModified(new Date());
        borrowagent.setRole(role);
        borrowagent.setAgentAddress(agentAddress);
        borrowagent.setBorrow(borrow);
        if (session != null)
        {
          session.saveOrUpdate(borrowagent);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(borrowmaterial);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(borrowreturnmaterial);
        }
        return borrowreturnmaterial;
    }

    public static BorrowShipment createBorrowShipment(final Shipment shipment,
                                                      final Borrow borrow)
    {
        BorrowShipment borrowshipment = new BorrowShipment();
        borrowshipment.initialize();
        borrowshipment.setTimestampCreated(new Date());
        borrowshipment.setTimestampModified(new Date());
        borrowshipment.setShipment(shipment);
        borrowshipment.setBorrow(borrow);
        if (session != null)
        {
          session.saveOrUpdate(borrowshipment);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(catalogseries);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectingevent);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectingeventattr);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectionobjdef);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectionobject);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectionobjectattr);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(collectionobjectcitation);
        }
        return collectionobjectcitation;
    }

    public static Collector createCollector(final Integer orderNumber,
                                            final CollectingEvent collectingEvent,
                                            final Agent agent)
    {
        Collector collector = new Collector();
        collector.initialize();
        collector.setTimestampCreated(new Date());
        collector.setTimestampModified(new Date());
        collector.setCollectingEvent(collectingEvent);
        collector.setAgent(agent);
        collector.setOrderNumber(orderNumber);
        if (session != null)
        {
          session.saveOrUpdate(collector);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(container);
        }
        return container;
    }

    public static ContainerItem createContainerItem(final Container container)
    {
        ContainerItem containeritem = new ContainerItem();
        containeritem.initialize();
        containeritem.setTimestampCreated(new Date());
        containeritem.setTimestampModified(new Date());
        containeritem.setContainer(container);
        if (session != null)
        {
          session.saveOrUpdate(containeritem);
        }
        return containeritem;
    }

    public static DataType createDataType(final String name)
    {
        DataType datatype = new DataType();
        datatype.initialize();
        datatype.setName(name);
        if (session != null)
        {
          session.saveOrUpdate(datatype);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(deaccession);
        }
        return deaccession;
    }

    public static DeaccessionAgent createDeaccessionAgent(final String role,
                                                          final AgentAddress agentAddress,
                                                          final Deaccession deaccession)
    {
        DeaccessionAgent deaccessionagent = new DeaccessionAgent();
        deaccessionagent.initialize();
        deaccessionagent.setTimestampCreated(new Date());
        deaccessionagent.setTimestampModified(new Date());
        deaccessionagent.setRole(role);
        deaccessionagent.setAgentAddress(agentAddress);
        deaccessionagent.setDeaccession(deaccession);
        if (session != null)
        {
          session.saveOrUpdate(deaccessionagent);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(deaccessioncollectionobject);
        }
        return deaccessioncollectionobject;
    }

    public static Determination createDetermination(final Boolean isCurrent,
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
        determination.setIsCurrent(isCurrent);
        determination.setCollectionObject(collectionObject);
        determination.setTypeStatusName(typeStatusName);
        determination.setDeterminedDate(determinedDate);
        determination.setConfidence(confidence);
        determination.setFeatureOrBasis(featureOrBasis);
        determination.setTaxon(taxon);
        determination.setDeterminer(determiner);
        determination.setMethod(method);
        if (session != null)
        {
          session.saveOrUpdate(determination);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(determinationcitation);
        }
        return determinationcitation;
    }

    public static ExchangeIn createExchangeIn(final Calendar exchangeDate,
                                              final Short quantityExchanged,
                                              final String descriptionOfMaterial,
                                              final AgentAddress agentAddress,
                                              final Agent agent)
    {
        ExchangeIn exchangein = new ExchangeIn();
        exchangein.initialize();
        exchangein.setTimestampCreated(new Date());
        exchangein.setTimestampModified(new Date());
        exchangein.setAgentAddress(agentAddress);
        exchangein.setAgent(agent);
        exchangein.setExchangeDate(exchangeDate);
        exchangein.setQuantityExchanged(quantityExchanged);
        exchangein.setDescriptionOfMaterial(descriptionOfMaterial);
        if (session != null)
        {
          session.saveOrUpdate(exchangein);
        }
        return exchangein;
    }

    public static ExchangeOut createExchangeOut(final Calendar exchangeDate,
                                                final Short quantityExchanged,
                                                final String descriptionOfMaterial,
                                                final AgentAddress agentAddress,
                                                final Agent agent,
                                                final Shipment shipment)
    {
        ExchangeOut exchangeout = new ExchangeOut();
        exchangeout.initialize();
        exchangeout.setTimestampCreated(new Date());
        exchangeout.setTimestampModified(new Date());
        exchangeout.setAgentAddress(agentAddress);
        exchangeout.setAgent(agent);
        exchangeout.setExchangeDate(exchangeDate);
        exchangeout.setQuantityExchanged(quantityExchanged);
        exchangeout.setDescriptionOfMaterial(descriptionOfMaterial);
        exchangeout.setShipment(shipment);
        if (session != null)
        {
          session.saveOrUpdate(exchangeout);
        }
        return exchangeout;
    }

    public static ExternalResource createExternalResource(final String mimeType,
                                                          final String fileName,
                                                          final Calendar fileCreatedDate,
                                                          final String externalLocation,
                                                          final Agent createdByAgent)
    {
        ExternalResource externalresource = new ExternalResource();
        externalresource.initialize();
        externalresource.setTimestampCreated(new Date());
        externalresource.setTimestampModified(new Date());
        externalresource.setMimeType(mimeType);
        externalresource.setFileName(fileName);
        externalresource.setFileCreatedDate(fileCreatedDate);
        externalresource.setExternalLocation(externalLocation);
        externalresource.setCreatedByAgent(createdByAgent);
        if (session != null)
        {
          session.saveOrUpdate(externalresource);
        }
        return externalresource;
    }

    public static ExternalResourceAttr createExternalResourceAttr(final String strValue,
                                                                  final Double dblValue,
                                                                  final ExternalResource externalResource,
                                                                  final AttributeDef definition)
    {
        ExternalResourceAttr externalresourceattr = new ExternalResourceAttr();
        externalresourceattr.initialize();
        externalresourceattr.setTimestampCreated(new Date());
        externalresourceattr.setTimestampModified(new Date());
        externalresourceattr.setStrValue(strValue);
        externalresourceattr.setDblValue(dblValue);
        externalresourceattr.setDefinition(definition);
        externalresourceattr.setExternalResource(externalResource);
        if (session != null)
        {
          session.saveOrUpdate(externalresourceattr);
        }
        return externalresourceattr;
    }

    public static GroupPerson createGroupPerson(final Short orderNumber,
                                                final Agent agentByGroup,
                                                final Agent agentByMember)
    {
        GroupPerson groupperson = new GroupPerson();
        groupperson.initialize();
        groupperson.setTimestampCreated(new Date());
        groupperson.setTimestampModified(new Date());
        groupperson.setOrderNumber(orderNumber);
        groupperson.setAgentByGroup(agentByGroup);
        groupperson.setAgentByMember(agentByMember);
        if (session != null)
        {
          session.saveOrUpdate(groupperson);
        }
        return groupperson;
    }

    public static InfoRequest createInfoRequest(final Long infoRequestID,
                                                final String firstName,
                                                final String lastName,
                                                final String institution,
                                                final String email,
                                                final Calendar requestDate,
                                                final Calendar replyDate,
                                                final RecordSet recordSet,
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
        if (session != null)
        {
          session.saveOrUpdate(inforequest);
        }
        return inforequest;
    }

    public static Journal createJournal(final String journalName,
                                        final String journalAbbreviation)
    {
        Journal journal = new Journal();
        journal.initialize();
        journal.setTimestampCreated(new Date());
        journal.setTimestampModified(new Date());
        journal.setJournalName(journalName);
        journal.setJournalAbbreviation(journalAbbreviation);
        if (session != null)
        {
          session.saveOrUpdate(journal);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(loan);
        }
        return loan;
    }

    public static LoanAgent createLoanAgent(final String role,
                                            final Loan loan,
                                            final AgentAddress agentAddress)
    {
        LoanAgent loanagent = new LoanAgent();
        loanagent.initialize();
        loanagent.setTimestampCreated(new Date());
        loanagent.setTimestampModified(new Date());
        loanagent.setRole(role);
        loanagent.setAgentAddress(agentAddress);
        loanagent.setLoan(loan);
        if (session != null)
        {
          session.saveOrUpdate(loanagent);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(loanphysicalobject);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(loanreturnphysicalobject);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(locality);
        }
        return locality;
    }

    public static LocalityCitation createLocalityCitation(final ReferenceWork referenceWork,
                                                          final Locality locality)
    {
        LocalityCitation localitycitation = new LocalityCitation();
        localitycitation.initialize();
        localitycitation.setTimestampCreated(new Date());
        localitycitation.setTimestampModified(new Date());
        localitycitation.setReferenceWork(referenceWork);
        localitycitation.setLocality(locality);
        if (session != null)
        {
          session.saveOrUpdate(localitycitation);
        }
        return localitycitation;
    }

    public static OtherIdentifier createOtherIdentifier(final String identifier,
                                                        final CollectionObject collectionObject)
    {
        OtherIdentifier otheridentifier = new OtherIdentifier();
        otheridentifier.initialize();
        otheridentifier.setTimestampCreated(new Date());
        otheridentifier.setTimestampModified(new Date());
        otheridentifier.setCollectionObject(collectionObject);
        otheridentifier.setIdentifier(identifier);
        if (session != null)
        {
          session.saveOrUpdate(otheridentifier);
        }
        return otheridentifier;
    }


    public static PrepType createPrepType(final String name)
    {
        PrepType preptype = new PrepType();
        preptype.initialize();
        preptype.setName(name);
        if (session != null)
        {
          session.saveOrUpdate(preptype);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(preparation);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(preparationattr);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(project);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(projectcollectionobject);
        }
        return projectcollectionobject;
    }

    public static RecordSet createRecordSet(final Long recordSetID,
                                            final String name,
                                            final SpecifyUser owner)
    {
        RecordSet recordset = new RecordSet();
        recordset.initialize();
        recordset.setTimestampCreated(new Date());
        recordset.setTimestampModified(new Date());
        recordset.setRecordSetID(recordSetID);
        recordset.setOwner(owner);
        recordset.setName(name);
        if (session != null)
        {
          session.saveOrUpdate(recordset);
        }
        return recordset;
    }

    public static RecordSetItem createRecordSetItem()
    {
        RecordSetItem recordsetitem = new RecordSetItem();
        recordsetitem.initialize();
        if (session != null)
        {
          session.saveOrUpdate(recordsetitem);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(referencework);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(repositoryagreement);
        }
        return repositoryagreement;
    }

    public static Shipment createShipment(final Calendar shipmentDate,
                                          final String shipmentNumber,
                                          final String shipmentMethod,
                                          final Short numberOfPackages,
                                          final String weight,
                                          final String insuredForAmount,
                                          final AgentAddress agentAddressByShipper,
                                          final AgentAddress agentAddressByShippedTo,
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
        shipment.setAgentAddressByShipper(agentAddressByShipper);
        shipment.setAgentAddressByShippedTo(agentAddressByShippedTo);
        if (session != null)
        {
          session.saveOrUpdate(shipment);
        }
        return shipment;
    }

    public static SpecifyUser createSpecifyUser(final String name,
                                                final String password,
                                                final Short privLevel,
                                                final UserGroup userGroup)
    {
        SpecifyUser specifyuser = new SpecifyUser();
        specifyuser.initialize();
        specifyuser.setPassword(password);
        specifyuser.setPrivLevel(privLevel);
        specifyuser.setUserGroup(userGroup);
        specifyuser.setName(name);
        if (session != null)
        {
          session.saveOrUpdate(specifyuser);
        }
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
        if (session != null)
        {
          session.saveOrUpdate(stratigraphy);
        }
        return stratigraphy;
    }


    public static TaxonCitation createTaxonCitation(final ReferenceWork referenceWork,
                                                    final Taxon taxon)
    {
        TaxonCitation taxoncitation = new TaxonCitation();
        taxoncitation.initialize();
        taxoncitation.setTimestampCreated(new Date());
        taxoncitation.setTimestampModified(new Date());
        taxoncitation.setTaxon(taxon);
        taxoncitation.setReferenceWork(referenceWork);
        if (session != null)
        {
          session.saveOrUpdate(taxoncitation);
        }
        return taxoncitation;
    }

    public static UserGroup createUserGroup(final String name)
    {
        UserGroup usergroup = new UserGroup();
        usergroup.initialize();
        usergroup.setName(name);
        if (session != null)
        {
          session.saveOrUpdate(usergroup);
        }
        return usergroup;
    }

    public static void saveOrUpdate(Object transientObject)
    {
    	if( session != null )
    	{
    		session.saveOrUpdate(transientObject);
    	}
    }
}
