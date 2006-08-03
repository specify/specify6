package edu.ku.brc.specify;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.HibernateUtil;
/**
 * This is a helper class that is used for initializing data for testing 
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */
public class InitializeData 
{
    protected static Hashtable prepTypeMapper    = new Hashtable();
    protected static int       attrsId           = 0;
    protected static boolean   classesWereLoaded = false;
    protected static final Logger   log = Logger.getLogger(InitializeData.class);
    public static int getIndex(String[] aOldNames, String aNewName)
    {
        for (int i=0;i<aOldNames.length;i++)
        {
            String fieldName = aOldNames[i].substring(aOldNames[i].indexOf(".")+1, aOldNames[i].length());            
            if (aNewName.equals(fieldName))
            {
                return i;
            }
        }
        return -1;
    }
    
    /*
     * 
     */
    public static void loadAttrs()
    {
        try
        {
            
            //------------------------------
            // Load PrepTypes and Prep Attrs
            //------------------------------
            /*String[] pages = {"Formatting", "Colors", "Application"};
            String[] formattingPrefs = {"date", "java.lang.String"};
            
            PrefGroupDAO prefGroupDAO = new PrefGroupDAO();
            PrefGroup    prefGroup    = new PrefGroup();
            
            prefGroup.setName("Formatting");
            prefGroup.setCreated(new Date());
            HashSet<Preference> set = new HashSet<Preference>();
            prefGroup.setAppPrefsIFace(set);
            
            for (int i=0;i<formattingPrefs.length;i++)
            {
                Preference pref = new Preference();
                pref.setName(formattingPrefs[i++]);
                pref.setValueType(formattingPrefs[i]);
                pref.setValue("");
                pref.setCreated(new Date());
                pref.setPrefGroup(prefGroup);
                
                set.add(pref);
            }
            prefGroupDAO.makePersistent(prefGroup);
            */
            
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
              
            // Clean up after ourselves
            //sessionFactory.close();
             
        } catch (Exception e)
        {
            log.error("loadAttr - ", e);
        }
        
    }
    
    public static void testHibernate() {
        // Create a configuration based on the properties file we've put
        // in the standard place.
        //Configuration config = new Configuration();

        // Tell it about the classes we want mapped, taking advantage of
        // the way we've named their mapping documents.
        /*
        config.addClass(Accession.class);
        config.addClass(CollectingEvent.class);
        config.addClass(CollectionObject.class);
        config.addClass(Geography.class);
        config.addClass(TaxonName.class);
        config.addClass(Locality.class);
        config.addClass(CollectionObjectCatalog.class);
        config.addClass(AccessionAgents.class);
        config.addClass(AccessionAuthorizations.class);
        config.addClass(Address.class);
        config.addClass(Agent.class);
        config.addClass(AgentAddress.class);
        config.addClass(BiologicalObjectAttribute.class);
        config.addClass(BiologicalObjectRelation.class);
        config.addClass(BiologicalObjectRelationType.class);
        config.addClass(Borrow.class);
        config.addClass(BorrowAgents.class);
        config.addClass(BorrowMaterial.class);
        config.addClass(BorrowReturnMaterial.class);
        config.addClass(BorrowShipments.class);
        config.addClass(CatalogSeriesDefinition.class);
        config.addClass(CatalogSeries.class);
        //config.addClass(CollectingEvent.class);
        config.addClass(Collection.class);
        //config.addClass(CollectionObject.class);
        //config.addClass(CollectionObjectCatalog.class);
        config.addClass(CollectionObjectCitation.class);
        config.addClass(CollectionObjectType.class);
        config.addClass(CollectionTaxonomyType.class);
        config.addClass(Deaccession.class);
        config.addClass(DeaccessionAgents.class);
        config.addClass(DeaccessionCollectionObject.class);
        config.addClass(Determination.class);
        config.addClass(DeterminationCitation.class);
        config.addClass(ExchangeIn.class);
        config.addClass(ExchangeOut.class);
        //config.addClass(Geography.class);
        config.addClass(GeologicTimeBoundary.class);
        config.addClass(GeologicTimePeriod.class);
        config.addClass(Habitat.class);
        config.addClass(Image.class);
        config.addClass(ImageAgent.class);
        config.addClass(ImageCollectionObject.class);
        config.addClass(ImageLocality.class);
        config.addClass(Journal.class);
        config.addClass(Loan.class);
        config.addClass(LoanAgents.class);
        config.addClass(LoanPhysicalObject.class);
        config.addClass(LoanReturnPhysicalObject.class);
        //config.addClass(Locality.class);
        config.addClass(LocalityCitation.class);
        config.addClass(Observation.class);
        config.addClass(OtherIdentifier.class);
        config.addClass(Permit.class);
        config.addClass(Preparation.class);
        config.addClass(Project.class);
        config.addClass(ProjectCollectionObject.class);
        config.addClass(ReferenceWork.class);
        config.addClass(Shipment.class);
        config.addClass(Sound.class);
        config.addClass(SoundEventStorage.class);
        config.addClass(TaxonCitation.class);
        //config.addClass(TaxonName.class);
        config.addClass(TaxonomicUnitType.class);
        config.addClass(TaxonomyType.class);
        
     
        // New Parts of the Schema
        config.addClass(CollectionObj.class);
        config.addClass(PrepsObj.class);
        config.addClass(PrepAttrs.class);
        config.addClass(PrepTypes.class);
        config.addClass(HabitatAttrs.class);
        config.addClass(BioAttrs.class);
        config.addClass(AttrributeDef.class);
        config.addClass(Collectors.class);
        config.addClass(Authors.class);
        config.addClass(GroupPersons.class);
        config.addClass(Stratigraphy.class);
        
        */
        // Get the session factory we can use for persistence
        //SessionFactory sessionFactory = config.buildSessionFactory();

        // Ask for a session using the JDBC information we've configured
        //Session session = sessionFactory.openSession();
        
        //String tableName = "edu.ku.brc.specify.datamodel.Locality";
        //String queryString = "SELECT count(*) from " + tableName;
        //log.info("Testing simple query through Hibernate:" + queryString);
        
        //Query q =session.createQuery(queryString);//
        //java.util.List c = q.list();
        //log.info("Found ### of records " + c.get(0) +" from " + tableName);            
        // Clean up after ourselves
        //sessionFactory.close();
        //log.info("Done.");  	
    	
    }
    
    /**
     * Utility method to associate an artist with a catObj
     */
    //private static void addCatalogObjCollectionEvent(CatalogObj catObj, CollectionEvent artist) {
    //    catObj.getCollectionEvent().add(artist);
    //}

    public static void main(String args[]) throws Exception 
    {
        boolean doingHibernate = true;
        if (doingHibernate) 
        {
        	testHibernate();
        } else
        {
            loadAttrs();
        }
    }
}
