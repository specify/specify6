package edu.ku.brc.specify.tasks.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.util.Pair;

/**
 * Creates Google Earth KML for the LocalityMapper.
 * 
 * @author jstewart
 * @code_status Complete
 */
public class KeyholeMarkupGenerator
{
	/** Logger used to emit any messages from this class. */
	private static final Logger log = Logger.getLogger(KeyholeMarkupGenerator.class);

	/** Standard XML file type declaration. */
	protected static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	/** Keyhole Markup Language namespace declaration. */
	protected static String KML_NAMESPACE_DECL = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";

	/** <code>CollectingEvents</code> to map in the KML output. */
	protected List<CollectingEvent> events;
	/** Labels to apply to the events mapped. */
	protected List<String> labels;

	/** A mapping from a species name to a URL with a species image. */
	protected Hashtable<String,String> speciesToImageMapper;

	/**
	 * Constructs a new KML generator object.
	 */
	public KeyholeMarkupGenerator()
	{
		events = new Vector<CollectingEvent>();
		labels = new Vector<String>();
	}

	/**
	 * Adds a collecting event to the set to be mapped.
	 *
	 * @param ce the event
	 * @param label the label for the event
	 */
	public void addCollectingEvent( CollectingEvent ce, String label )
	{
		events.add(ce);
		labels.add(label);
	}

	/**
	 * Sets the speciesToImageMapper.
	 *
	 * @param speciesToImageMapper the mapping <code>Hashtable</code>
	 */
	public void setSpeciesToImageMapper(Hashtable<String, String> speciesToImageMapper)
	{
		this.speciesToImageMapper = speciesToImageMapper;
	}

	/**
	 * Write the KML out to a file.
	 *
	 * @param filename the name of the output file
	 * @throws IOException a file I/O exception occurred
	 */
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

	/**
	 * Builds an XML chunk describing the path from the first locality to the last.
	 *
	 * @return an XML string
	 */
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

	/**
	 * Generates a KML chunk describing the given collecting event.
	 *
	 * @param ce the event
	 * @param label the label for the event
	 * @return the KML string
	 */
	protected String generatePlacemark( CollectingEvent ce, String label )
	{
		// get all of the important information

		// get location information
		Locality loc = ce.getLocality();
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();

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
            if (StringUtils.isNotEmpty(c.getAgent().getName()))
            {
                agentNames.add(c.getAgent().getName());
            } else
            {
                agentNames.add(c.getAgent().getFirstName()+ " "+ c.getAgent().getLastName());
            }
		}

		// get taxonomy of collection object
		Vector<Pair<String,String>> genusSpecies = new Vector<Pair<String,String>>();
		for( CollectionObject co: ce.getCollectionObjects() )
		{
			String genus = null;
			String species = null;
			for( Determination d: co.getDeterminations() )
			{
				if( d.isCurrent() )
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
        if(label != null)
        {
            sb.append(label);
        }
        sb.append(startString);
        if (!startString.equals(endString))
        {
            sb.append(" - ");
            sb.append(endString);
        }
		sb.append("</name>\n");

		// build the fancy HTML popup description
		sb.append("<description><![CDATA[");
		//sb.append("<center><h3>");
		//sb.append(startString);
		//sb.append(" - ");
		//sb.append(endString);
		//sb.append("</h3></center><br/>");
		sb.append("<h3>Collectors:</h3>\n<ul>\n");
		for( String agent: agentNames )
		{
			sb.append("<li>");
			sb.append(agent);
			sb.append("</li>\n");
		}
		sb.append("</ul>\n");
		sb.append("<br/><h3>Collection objects:</h3>\n<table>\n");
		for( Pair<String,String> tax: genusSpecies )
		{
			sb.append("<tr>\n");

			// simple name text
			String taxonomicName = tax.first + " " + tax.second;
			sb.append("<td><i>");
			sb.append(taxonomicName);
			sb.append("</i></td>\n");

			sb.append("<td><a href=\"http://www.fishbase.org/Summary/speciesSummary.php?genusname=");
			sb.append(tax.first);
			sb.append("&speciesname=");
			sb.append(tax.second);
			sb.append("\">");
			sb.append("fb</a></td>\n");

			sb.append("<td><a href=\"http://animaldiversity.ummz.umich.edu/site/accounts/information/");
			sb.append(tax.first);
			sb.append("_");
			sb.append(tax.second);
			sb.append("\">");
			sb.append("ad</a></td>\n");

			if( speciesToImageMapper != null )
			{
				String imgSrc = speciesToImageMapper.get(taxonomicName);
                //System.out.println("["+taxonomicName+"]["+imgSrc+"]");
				if( imgSrc != null )
				{
					sb.append("<td><img src=\"");
					sb.append(imgSrc);
					sb.append("\"/></td>\n");
				}
				else
				{
					sb.append("<td>&nbsp;</td>\n");
				}
			}

			sb.append("</tr>\n");
		}
		sb.append("</table>]]></description>\n");
		sb.append("<LookAt>\n");
		sb.append("<latitude>");
		sb.append(lat.doubleValue());
		sb.append("</latitude>\n");
		sb.append("<longitude>");
		sb.append(lon.doubleValue());
		sb.append("</longitude>\n");
		sb.append("<range>300000.00</range>\n");
		sb.append("</LookAt>\n");
		sb.append("<Point>\n");
		sb.append("<coordinates>");
		sb.append(lon.doubleValue());
		sb.append(",");
		sb.append(lat.doubleValue());
		sb.append("</coordinates>\n");
		sb.append("</Point>\n");
		sb.append("</Placemark>\n\n\n");

		log.debug("Generated placemark:\n " + sb.toString() );
		return sb.toString();
	}
}
