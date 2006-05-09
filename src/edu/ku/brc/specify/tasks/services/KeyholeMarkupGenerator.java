package edu.ku.brc.specify.tasks.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.util.Pair;

public class KeyholeMarkupGenerator
{
	private static Log log = LogFactory.getLog(KeyholeMarkupGenerator.class);

	protected static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	protected static String KML_NAMESPACE_DECL = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
	
	protected List<CollectingEvent> events;
	protected List<String> labels;
	
	public KeyholeMarkupGenerator()
	{
		events = new Vector<CollectingEvent>();
		labels = new Vector<String>();
	}
	
	public void addCollectingEvent( CollectingEvent ce, String label )
	{
		events.add(ce);
		labels.add(label);
	}
	
	public void outputToFile( String filename ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(XML_DECLARATION);
		writer.write(KML_NAMESPACE_DECL);
		writer.write("<Document>");
		for( int i = 0; i < events.size(); ++i )
		{
			CollectingEvent ce = events.get(i);
			String label = labels.get(i);
			writer.write(generatePlacemark(ce,label));
		}
		writer.write(generatePathForLocalities());
		writer.write("</Document>\n");
		writer.write("</kml>\n");
		writer.flush();
		writer.close();
	}
	
	protected String generatePathForLocalities()
	{
		StringBuilder sb = new StringBuilder("<Placemark>\n");
		sb.append("<LineString>\n");
		sb.append("<coordinates>");
		for( CollectingEvent ce: events )
		{
			Locality loc = ce.getLocality();
			sb.append(loc.getLongitude1());
			sb.append(",");
			sb.append(loc.getLatitude1());
			sb.append(",");
			sb.append("0.0\n");
		}
		sb.append("</coordinates>\n");
		sb.append("</LineString>\n");
		sb.append("</Placemark>\n\n\n");

		return sb.toString();
	}
	
	protected String generatePlacemark( CollectingEvent ce, String label )
	{
		// get all of the important information
		
		// get location information
		Locality loc = ce.getLocality();
		Double lat = loc.getLatitude1();
		Double lon = loc.getLongitude1();

		// get event times
		Calendar start = ce.getStartDate();
		DateFormat dfStart = DateFormat.getDateInstance();
		dfStart.setCalendar(start);
		String startString = dfStart.format(start.getTime());
		Calendar end   = ce.getEndDate();
		DateFormat dfEnd = DateFormat.getDateInstance();
		dfEnd.setCalendar(end);
		String endString = dfEnd.format(end.getTime());
		
		// get names of collectors
		List<String> agentNames = new Vector<String>();
		for( Collectors c: ce.getCollectors() )
		{
			agentNames.add(c.getAgent().getName());
		}
		
		// get taxonomy of collection object
		Vector<Pair<String,String>> genusSpecies = new Vector<Pair<String,String>>();
		for( CollectionObject co: ce.getCollectionObjects() )
		{
			String genus = null;
			String species = null;
			for( Determination d: co.getDeterminations() )
			{
				if( d.getIsCurrent().booleanValue() )
				{
					Taxon t = d.getTaxon();
					species = t.getName();
					genus = t.getParent().getName();
					break;
				}
			}
			genusSpecies.add(new Pair<String,String>(genus,species));
		}
		
		// build the placemark
		StringBuilder sb = new StringBuilder("<Placemark>\n");
		sb.append("<name>");
		sb.append(label);
		sb.append("</name>\n");
		
		// build the fancy HTML popup description
		sb.append("<description><![CDATA[");
		sb.append("<center><h3>");
		sb.append(startString);
		sb.append(" - ");
		sb.append(endString);
		sb.append("</h3></center>");
		sb.append("<br/><h3>Collectors:</h3>\n<ul>\n");
		for( String agent: agentNames )
		{
			sb.append("<li>");
			sb.append(agent);
			sb.append("</li>\n");
		}
		sb.append("</ul>\n");
		sb.append("<br/><h3>Collection objects:</h3>\n<ul>\n");
		for( Pair<String,String> tax: genusSpecies )
		{
			sb.append("<li>");

			// simple name text
			sb.append("<i>");
			sb.append(tax.second);
			sb.append(" ");
			sb.append(tax.first);
			sb.append("</i>");

			sb.append("    <a href=\"http://www.fishbase.org/Summary/speciesSummary.php?genusname=");
			sb.append(tax.first);
			sb.append("&speciesname=");
			sb.append(tax.second);
			sb.append("\">");
			sb.append("fb</a>");

			sb.append("    <a href=\"http://animaldiversity.ummz.umich.edu/site/accounts/information/");
			sb.append(tax.first);
			sb.append("_");
			sb.append(tax.second);
			sb.append("\">");
			sb.append("ad</a>");

			sb.append("</li>\n");
		}
		sb.append("]]></description>\n");
		sb.append("<LookAt>\n");
		sb.append("<latitude>");
		sb.append(lat);
		sb.append("</latitude>\n");
		sb.append("<longitude>");
		sb.append(lon);
		sb.append("</longitude>\n");
		sb.append("<range>300000.00</range>\n");
		sb.append("</LookAt>\n");
		sb.append("<Point>\n");
		sb.append("<coordinates>");
		sb.append(lon);
		sb.append(",");
		sb.append(lat);
		sb.append("</coordinates>\n");
		sb.append("</Point>\n");
		sb.append("</Placemark>\n\n\n");
		
		log.debug("Generated placemark:\n " + sb.toString() );
		return sb.toString();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		KeyholeMarkupGenerator kmlGen = new KeyholeMarkupGenerator();
		
		Session s = HibernateUtil.getCurrentSession();
		Query q = s.createQuery("from collectingevent in class CollectingEvent where collectingevent.collectingEventId in (1,2,3,4,5,6)");
		List events = q.list();
		for( int i = 0; i < events.size(); i++ )
		{
			CollectingEvent ce = (CollectingEvent)events.get(i);
			kmlGen.addCollectingEvent(ce, Integer.toString(i));
		}
		
		
//		double[] locationArray = {
//				39.0657, -95.4181,
//				37.8156, -98.7656,
//				37.3069, -98.3925,
////				29.9233, -99.0597,
//				37.2778, -98.5738,
//				38.0367, -97.8029,
//				38.6725, -96.4566,
//				38.9493, -95.1403,
////				29.9650, -99.2344,
////				37.3455, -95.5311
//		};
//		
//		String[] taxa = {
//				"Polyodon", "spathula",
//				"Lepisosteus", "oculatus",
//				"Ctenopharyngodon", "idella",
//				"Ictalurus", "furcatus",
//				"Ictalurus", "punctatus",
//				"Oncorhynchus", "mykiss",
//				"Lepomis", "macrochirus",
//				"Micropterus", "salmoides",
//				"Pomoxis", "nigromaculatus",
//				"Sander", "vitreus"
//		};
//		
//		String[] agentNames = {
//				"Andy Bentley",
//				"Rod Spears",
//				"Joshua Stewart",
//				"UPS Guy",
//				"Freud"
//		};
//
//		Agent[] agents = new Agent[agentNames.length];
//		
//		Calendar c = Calendar.getInstance();
//		Random r = new Random();
//		
//		for( int i = 0; i < agentNames.length; ++i )
//		{
//			Agent a = new Agent();
//			a.initialize();
//			a.setName(agentNames[i]);
//			agents[i] = a;
//		}
//		
//		CollectionObject[] objects = new CollectionObject[taxa.length/2];
//		
//		for( int i = 0; i < taxa.length; i+=2 )
//		{
//			Taxon genus = new Taxon();
//			genus.initialize();
//			genus.setName(taxa[i]);
//			Taxon species = new Taxon();
//			species.initialize();
//			species.setName(taxa[i+1]);
//			species.setParent(genus);
//			Determination d = new Determination();
//			d.initialize();
//			d.setTaxon(species);
//			d.setIsCurrent(true);
//			CollectionObject o = new CollectionObject();
//			o.initialize();
//			o.getDeterminations().add(d);
//			objects[i/2] = o;
//		}
//		
//		for( int i = 0; i < locationArray.length; i+=2 )
//		{
//			CollectingEvent ce = new CollectingEvent();
//			ce.initialize();
//			
//			// set the times
//			ce.setStartDate(c);
//			ce.setEndDate(c);
//			
//			// set the location
//			Locality loc = new Locality();
//			loc.initialize();
//			loc.setLatitude1(locationArray[i]);
//			loc.setLongitude1(locationArray[i+1]);
//			ce.setLocality(loc);
//			
//			// set the agents
//			for( int j = 0; j < agents.length; ++j )
//			{
//				if( r.nextBoolean() )
//				{
//					Collectors coll = new Collectors();
//					coll.initialize();
//					coll.setAgent(agents[j]);
//					ce.getCollectors().add(coll);
//				}
//			}
//			
//			// setup the collection objects
//			ce.getCollectionObjects().add(objects[i/2]);
//			
//			kmlGen.addCollectingEvent(ce,Integer.toString(i/2));
//		}
		
		kmlGen.outputToFile("C:\\Documents and Settings\\jstewart\\Desktop\\kmloutput.kml");
	}
}
