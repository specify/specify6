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

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.ToolsTask;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.GenericKMLGenerator;

/**
 * Creates Google Earth KML for the LocalityMapper.
 * 
 * @author jstewart
 * 
 * @code_status Complete
 */
public class CollectingEventKMLGenerator
{
	/** Logger used to emit any messages from this class. */
	private static final Logger log = Logger.getLogger(CollectingEventKMLGenerator.class);

	/** Standard XML file type declaration. */
	protected static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	/** Keyhole Markup Language namespace declaration. */
	protected static String KML_NAMESPACE_DECL = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";

	/** <code>CollectingEvents</code> to map in the KML output. */
	protected List<CollectingEvent> events;
	
	/** Labels to apply to the events mapped. */
	protected List<String> labels;

	/** A mapping from a species name to a URL with a species image. */
	protected Hashtable<String, String> speciesToImageMapper;
	
	protected String textColor = "000000";
    
    /** A URL to an image file to be used as the placemark icon. */
    protected String placemarkIconURL;
    
    /** The background color of the placemark balloons. */
    protected String balloonStyleBgColor;
    
    /** The text color for the placemark balloons. */
    protected String balloonStyleTextColor;
    
    /** The format description for the placemark balloons. */
    protected String balloonStyleText;
    
    /** The description of the KML/KMZ File */
    protected String description;
    
	/**
	 * Constructs a new KML generator object.
	 */
	public CollectingEventKMLGenerator()
	{
		events = new Vector<CollectingEvent>();
		labels = new Vector<String>();
	}


    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * @param textColor the textColor to set
     */
    public void setTextColor(String textColor)
    {
        this.textColor = textColor;
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
     * Clears all data from the generator's data sets.
     */
    public void clear()
    {
        events.clear();
        labels.clear();
    }

	/**
	 * Sets the speciesToImageMapper.
	 *
	 * @param speciesToImageMapper the mapping <code>Hashtable</code>
	 */
	public void setSpeciesToImageMapper(final Hashtable<String, String> speciesToImageMapper)
	{
		this.speciesToImageMapper = speciesToImageMapper;
	}

	/**
	 * Write the KML out to a file.
	 *
	 * @param filename the name of the output file
	 * @throws IOException a file I/O exception occurred
	 */
	public void outputToFile(final String filename) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write(XML_DECLARATION);
		writer.write(KML_NAMESPACE_DECL);
		writer.write("<Document>\n");
        if (StringUtils.isNotEmpty(description))
        {
            writer.append("<description><![CDATA[");
            writer.append(description);
            writer.append("]]></description>");
        }
		writer.write(GenericKMLGenerator.generateStyle(placemarkIconURL, balloonStyleBgColor, balloonStyleTextColor, balloonStyleText));
		
		for( int i = 0; i < events.size(); ++i )
		{
			CollectingEvent ce    = events.get(i);
			String          label = labels.get(i);
			writer.write(generatePlacemark(ce, label));
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
	protected String generatePlacemark(final CollectingEvent ce, final String label)
	{
		// get all of the important information

		// get storage information
		Locality   loc = ce.getLocality();
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();

		// get event times
		Calendar   start       = ce.getStartDate();
		DateFormat dfStart     = DateFormat.getDateInstance();
		
		dfStart.setCalendar(start);
		String     startString = dfStart.format(start.getTime());
		
		Calendar   end       = ce.getEndDate();
		DateFormat dfEnd     = DateFormat.getDateInstance();
		
		dfEnd.setCalendar(end);
		String     endString = dfEnd.format(end.getTime());

		// get names of collectors
		List<String> agentNames = new Vector<String>();
		for( Collector c: ce.getCollectors() )
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
		Hashtable<Pair<String, String>, CollectionObject> coHash = new Hashtable<Pair<String,String>, CollectionObject>();
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
			Pair<String, String> genusSpeciesPair = new Pair<String,String>(genus,species);
			genusSpecies.add(genusSpeciesPair);
			coHash.put(genusSpeciesPair, co);
		}
		
		DBTableInfo           colObjTableInfo = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
		DBFieldInfo           catalogNumberFI = colObjTableInfo.getFieldByColumnName("CatalogNumber");
		UIFieldFormatterIFace formatter       = catalogNumberFI.getFormatter();

		// build the placemark
		StringBuilder sb = new StringBuilder("<Placemark>\n");
        //if (placemarkIconURL != null)
        {
            sb.append(GenericKMLGenerator.generateXmlElement("styleUrl", "#custom"));
            sb.append("\n");
        }
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
		sb.append("<h3>"+UIRegistry.getResourceString("GE_COLLECTOR")+":</h3>\n<ul>\n");
		for( String agent: agentNames )
		{
			sb.append("<li>");
			sb.append(agent);
			sb.append("</li>\n");
		}
		sb.append("</ul>\n");
		sb.append("<br/><h3>"+UIRegistry.getResourceString("GE_COLLECTION_OBJECTS")+":</h3>\n<table>\n");
		
        String primaryURL        = AppPreferences.getRemote().get(ToolsTask.GE_BALLOON_PRIMARY_URL, null);
        String primaryURLTitle   = AppPreferences.getRemote().get(ToolsTask.GE_BALLOON_PRIMARY_URL_TITLE, null);
        String secondaryURL      = AppPreferences.getRemote().get(ToolsTask.GE_BALLOON_SECONDARY_URL, null);
        String secondaryURLTitle = AppPreferences.getRemote().get(ToolsTask.GE_BALLOON_SECONDARY_URL_TITLE, null);
        
        sb.append("<tr>");
        sb.append("<th><center>");
        sb.append(UIRegistry.getResourceString("GE_CATALOG_NUMBER"));
        sb.append("</center></th>\n");
        sb.append("<th>");
        sb.append(UIRegistry.getResourceString("GE_TAXONOMY"));
        sb.append("</b></th>\n");
        if (StringUtils.isNotEmpty(primaryURL))
        {
            sb.append("<th style=\"color:#"+textColor+"\"><center>"+UIRegistry.getResourceString("GE_PRIMARY")+"</center></th>\n");
        }
        
        if (StringUtils.isNotEmpty(secondaryURL))
        {
            sb.append("<th style=\"color:#"+textColor+"\"><center>"+UIRegistry.getResourceString("GE_SECONDARY")+"</center></th>\n");
        }
        sb.append("</tr>\n");
        
		for( Pair<String, String> tax: genusSpecies )
		{
			sb.append("<tr>\n");
            sb.append("<td><center>");
            if (formatter != null)
            {
                sb.append(formatter.formatInBound(coHash.get(tax).getCatalogNumber()));                
            } else
            {
                sb.append(coHash.get(tax).getCatalogNumber());    
            }
            sb.append("</center></td>\n");
            
			// simple name text
			String taxonomicName = tax.first + " " + tax.second;
			sb.append("<td><i>");
			sb.append(taxonomicName);
			sb.append("</i></td>\n");

			String linkTextColor = (textColor.startsWith("F") ? "WHITE" : "BLACK");
			
            if (StringUtils.isNotEmpty(primaryURL))
            {
                String primaryURLStr   = String.format(primaryURL, tax.first, tax.second);
                sb.append("<td><a style=\"color:"+linkTextColor+"\" href=\""+primaryURLStr+"\"><center>");
                sb.append(primaryURLTitle);
                sb.append("</a></center></td>\n");
            }
            
            if (StringUtils.isNotEmpty(secondaryURL))
            {
                String secondaryURLStr = String.format(secondaryURL, tax.first, tax.second);
                sb.append("<td><a style=\"color:"+linkTextColor+"\" href=\""+secondaryURLStr+"\"><center>");
                sb.append(secondaryURLTitle);
                sb.append("</a></center></td>\n");
            }
            
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

    /**
     * @param placemarkIconURL the placemarkIconURL to set
     */
    public void setPlacemarkIconURL(String placemarkIconURL)
    {
        this.placemarkIconURL = placemarkIconURL;
    }

    /**
     * @param balloonStyleBgColor the balloonStyleBgColor to set
     */
    public void setBalloonStyleBgColor(String balloonStyleBgColor)
    {
        this.balloonStyleBgColor = balloonStyleBgColor;
    }

    /**
     * @param balloonStyleTextColor the balloonStyleTextColor to set
     */
    public void setBalloonStyleTextColor(String balloonStyleTextColor)
    {
        this.balloonStyleTextColor = balloonStyleTextColor;
    }

    /**
     * @param balloonStyleText the balloonStyleText to set
     */
    public void setBalloonStyleText(String balloonStyleText)
    {
        this.balloonStyleText = balloonStyleText;
    }

}
