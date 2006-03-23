package edu.ku.brc.specify.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.User;

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

    public static Agent createAgent(final String title,
                                    final String firstName,
                                    final String middleInit, 
                                    final String lastName,
                                    final String abbrev)
    {
        // Create Collection Object Definition
        Agent agent = new Agent();
        agent.setAbbreviation(abbrev);
        agent.setAgentType((byte)0);
        agent.setMiddleInitial(middleInit);
        agent.setFirstName(firstName);
        agent.setLastName(lastName);
        agent.setTitle(title);
        agent.setTimestampCreated(new Date());
        agent.setTimestampModified(new Date());
        
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
    
    public static CollectionObjDef createCollectionObjDef(final String name, final DataType dataType, final User user)
    {
        CollectionObjDef colObjDef = new CollectionObjDef();
        colObjDef.setName(name);
        colObjDef.setDataType(dataType);
        colObjDef.setUser(user);
        colObjDef.setTaxonTreeDef(null);
        colObjDef.setCatalogSeries(new HashSet<Object>());
        colObjDef.setAttributeDefs(new HashSet<Object>());   
        if (session != null)
        {
            session.saveOrUpdate(colObjDef);
        }
        return colObjDef;
    }
    
    public static CatalogSeries createCatalogSeries(final String prefix, final String name)
    {
        CatalogSeries catalogSeries = new CatalogSeries();
        catalogSeries.setCatalogSeriesPrefix(prefix);
        catalogSeries.setCollectionObjDefItems(new HashSet<Object>());
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

        startCal.clear();
        startCal.set(2006, 0, 1);
        colEv.setStartDate(startCal);

        Calendar endCal = Calendar.getInstance();
        startCal.clear();
        startCal.set(2006, 0, 2);
        colEv.setEndDate(startCal);
        colEv.setAttrs(new HashSet<Object>());
        
        HashSet collectorsSet = new HashSet<Object>();
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
                                                          final int              countAmt)
    {
        startCal.clear();
        startCal.set(2006, 0, 1);
        
        // Create Collection Object
        CollectionObject colObj = new CollectionObject();
        colObj.setAccession(accession);
        colObj.setAttrs(new HashSet());
        colObj.setCataloger(cataloger);
        colObj.setCatalogedDate(startCal);
        colObj.setCatalogedDateVerbatim("Sometime this year");
        colObj.setCatalogNumber(catalogNumber);
        colObj.setCatalogSeries(catalogSeries);
        colObj.setCollectionObjectCitations(new HashSet<Object>());
        colObj.setCollectionObjDef(colObjDef);
        colObj.setContainer(null);
        colObj.setContainerItem(null);
        colObj.setCountAmt(countAmt);
        colObj.setDeaccessionCollectionObjects(new HashSet<Object>());
        colObj.setDeaccessioned(false);
        colObj.setDescription("This is the description");
        colObj.setDeterminations(new HashSet());
        colObj.setExternalResources(new HashSet());
        colObj.setFieldNumber(fieldNumber);
        colObj.setGuid("This is the GUID");
        colObj.setLastEditedBy("rods");
        colObj.setModifier("modifier");
        colObj.setName("The Name!!!!!!");
        colObj.setPreparations(new HashSet<Object>());
        colObj.setProjectCollectionObjects(new HashSet<Object>());
        colObj.setRemarks("These are the remarks");
        colObj.setYesNo1(false);
        colObj.setYesNo2(true);

        colObj.setTimestampCreated(new Date());
        colObj.setTimestampModified(new Date());
        
        if (session != null)
        {
            session.saveOrUpdate(colObj);
        }
        return colObj;
    }

    
    public static Determination createDetermination(final CollectionObject collectionObject,
                                                       final Agent            determiner,
                                                       final Taxon            taxon,
                                                       final boolean          isCurrent)
    {
        startCal.clear();
        startCal.set(2006, 0, 2);
        
        // Create Determination
        Determination determination = new Determination();
        determination.setIsCurrent(isCurrent);
        determination.setCollectionObject(collectionObject);
        determination.setDeterminedDate(startCal);
        determination.setDeterminer(determiner);
        determination.setTaxon(taxon);
        determination.setTimestampCreated(new Date());
        determination.setTimestampModified(new Date());

        collectionObject.getDeterminations().add(determination);
        
        if (session != null)
        {
            session.saveOrUpdate(determination);
        }
        return determination;

    }
    
    public static GeographyTreeDef createGeographyTreeDef(final String name)
    {
        GeographyTreeDef gtd = new GeographyTreeDef();
        gtd.setName(name);
        gtd.setTreeDefItems(new HashSet<Object>());
        gtd.setTreeEntries(new HashSet<Object>());
        if (session != null)
        {
            session.saveOrUpdate(gtd);
        }
       return gtd;
    }
    
    public static GeographyTreeDefItem createGeographyTreeDefItem(final GeographyTreeDefItem parent, final GeographyTreeDef gtd, final String name, final int rankId)
    {
        GeographyTreeDefItem gtdi = new GeographyTreeDefItem();
        gtdi.setName(name);
        gtdi.setParent(parent);
        gtdi.setRankId(rankId);
        gtdi.setChildren(new HashSet<Object>());
        gtdi.setTreeDef(gtd);
        gtd.getTreeDefItems().add(gtdi);
        if (session != null)
        {
            session.saveOrUpdate(gtd);
        }
        return gtdi;
    }
    
    public static Geography createGeography(final GeographyTreeDef gtd, 
                                            final Geography parent,
                                            //final String abbrev, 
                                            final String name,
                                            //final int highNode,
                                            //final int nodeNum,
                                            final int rankId)
    {
        Geography geography = new Geography();
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
        if (session != null)
        {
            session.saveOrUpdate(geography);
        }
        return geography;
    }
    

    
    public static Locality createLocality(final String name, final Geography geo)
    {
        Locality locality = new Locality();
        locality.setLocalityName(name);
        locality.setGeography(geo);
        locality.setTimestampCreated(new Date());
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
        ltd.setName(name);
        ltd.setTreeDefItems(new HashSet<Object>());
        ltd.setTreeEntries(new HashSet<Object>());
        if (session != null)
        {
            session.saveOrUpdate(ltd);
        }
        return ltd;
    }
    
    public static LocationTreeDefItem createLocationTreeDefItem(final LocationTreeDefItem parent, final LocationTreeDef ltd, final String name, final int rankId)
    {
        LocationTreeDefItem ltdi = new LocationTreeDefItem();
        ltdi.setName(name);
        ltdi.setParent(parent);
        ltdi.setRankId(rankId);
        ltdi.setChildren(new HashSet<Object>());
        ltdi.setTreeDef(ltd);
        ltd.getTreeDefItems().add(ltdi);
        if (session != null)
        {
            session.saveOrUpdate(ltdi);
        }
        return ltdi;
    }
    
    public static Location createLocation(final LocationTreeDef ltd, 
                                          final Location parent,
                                          //final String abbrev, 
                                          final String name,
                                          //final int highNode,
                                          //final int nodeNum,
                                          final int rankId)
    {
        Location location = new Location();
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
        if (session != null)
        {
            session.saveOrUpdate(location);
        }
        return location;
    }
    
    public static PrepType createPrepType(final String name)
    {
        PrepType prepType = new PrepType();
        prepType.setName(name);
        prepType.setPreparations(new HashSet<Object>());
        prepType.setAttributeDefs(new HashSet<Object>());
        if (session != null)
        {
            session.saveOrUpdate(prepType);
        }
        return prepType;
    }
    
    public static Preparation createPreparation(final PrepType         prepType,
                                                final Agent            preparedBy,
                                                final CollectionObject colObj, 
                                                final Location         location,
                                                final int              count)
    {
        Preparation prep = new Preparation();
        prep.setAttrs(new HashSet<Object>());
        prep.setCollectionObject(colObj);
        prep.setCount(count);
        prep.setExternalResources(new HashSet<Object>());
        prep.setLastEditedBy("");
        prep.setLoanPhysicalObjects(new HashSet<Object>());
        prep.setLocation(location);
        prep.setPreparedByAgent(preparedBy);
        prep.setPreparedDate(Calendar.getInstance());
        prep.setPrepType(prepType);
        prep.setRemarks("These are the remarks");
        prep.setStorageLocation("This is the textual storage location");
        prep.setText1("Thi is text1");
        prep.setText2("This is text2");
        prep.setTimestampCreated(new Date());
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
        ttd.setName(name);
        ttd.setTreeDefItems(new HashSet<Object>());
        ttd.setTreeEntries(new HashSet<Object>());
        if (session != null)
        {
            session.saveOrUpdate(ttd);
        }
       return ttd;
    }
    
    public static TaxonTreeDefItem createTaxonTreeDefItem(final TaxonTreeDefItem parent, final TaxonTreeDef ttd, final String name, final int rankId)
    {
        TaxonTreeDefItem ttdi = new TaxonTreeDefItem();
        ttdi.setName(name);
        ttdi.setParent(parent);
        ttdi.setRankId(rankId);
        ttdi.setChildren(new HashSet<Object>());
        ttdi.setTreeDef(ttd);
        ttd.getTreeDefItems().add(ttdi);
        if (session != null)
        {
            session.saveOrUpdate(ttdi);
        }
        return ttdi;
    }
    
    public static Taxon createTaxon(final TaxonTreeDef ttd, 
                                          final Taxon parent,
                                          //final String abbrev, 
                                          final String name,
                                          //final int highNode,
                                          //final int nodeNum,
                                          final int rankId)
    {
        Taxon location = new Taxon();
        //location.setAbbrev(abbrev);
        location.setDefinition(ttd);
        location.setName(name);
        //location.setHighestChildNodeNumber(highNode);
        //location.setNodeNumber(nodeNum);
        location.setParent(parent);
        location.setRankId(rankId);
        if (ttd != null)
        {
            ttd.getTreeEntries().add(location);
        }
        if (session != null)
        {
            session.saveOrUpdate(location);
        }
        return location;
    }
    

}
