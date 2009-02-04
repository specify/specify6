package edu.ku.brc.specify.tasks.services;

import static edu.ku.brc.util.LatLonConverter.convertToDDDDDD;

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

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.PluginsTask;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.LatLonConverter.DEGREES_FORMAT;
import edu.ku.brc.util.LatLonConverter.DIRECTION;
import edu.ku.brc.util.services.GenericKMLGenerator;

/**
 * Creates Google Earth KML for the LocalityMapper.
 * 
 * @author jstewart
 * 
 * @code_status Complete
 */
public class CollectingEventLocalityKMLGenerator
{
	/** Logger used to emit any messages from this class. */
	private static final Logger log = Logger.getLogger(CollectingEventLocalityKMLGenerator.class);

	/** Standard XML file type declaration. */
	protected static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	
	/** Keyhole Markup Language namespace declaration. */
	protected static String KML_NAMESPACE_DECL = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";

	/** <code>CollectingEvents</code> to map in the KML output. */
	protected List<FormDataObjIFace> dataObjs;
	
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
	public CollectingEventLocalityKMLGenerator()
	{
		dataObjs = new Vector<FormDataObjIFace>();
		labels   = new Vector<String>();
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
	public void addDataObj(FormDataObjIFace ce, String label )
	{
		dataObjs.add(ce);
		labels.add(label);
	}
    
    /**
     * Clears all data from the generator's data sets.
     */
    public void clear()
    {
        dataObjs.clear();
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
		
		boolean isDoingCollectingEvents = false;
		
		for( int i = 0; i < dataObjs.size(); ++i )
		{
            String           label   = labels.get(i);
            FormDataObjIFace dataObj = dataObjs.get(i);
            
            String kmlStr = null;
            if (dataObj instanceof CollectingEvent)
            {
                kmlStr = generatePlacemark((CollectingEvent)dataObj, label);
    			isDoingCollectingEvents = true;
    			
            } else if (dataObj instanceof Locality)
            {
                kmlStr = generatePlacemark((Locality)dataObj, label);
                
            } else if (dataObj instanceof CollectionObject)
            {
                kmlStr = generatePlacemark((CollectionObject)dataObj, label);
            }
            
            if (kmlStr != null)
            {
                writer.write(kmlStr);
            }
		}
		
		if (isDoingCollectingEvents)
		{
		    String kmlStr = generatePathForLocalities();
		    if (kmlStr != null)
            {
                writer.write(kmlStr);
            }
		}
		
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
	    int cnt = 0;
		StringBuilder sb = new StringBuilder("<Placemark>\n");
		sb.append("<LineString>\n");
		sb.append("<coordinates>");
		for( FormDataObjIFace dataObj : dataObjs )
		{
		    Locality loc = dataObj instanceof CollectingEvent ? ((CollectingEvent)dataObj).getLocality() : (Locality)dataObj;
		    if (loc != null && loc.getLongitude1() != null && loc.getLatitude1() != null)
		    {
    			sb.append(loc.getLongitude1());
    			sb.append(",");
    			sb.append(loc.getLatitude1());
    			sb.append(",");
    			sb.append("0.0\n");
    			cnt++;
		    }
		}
		sb.append("</coordinates>\n");
		sb.append("</LineString>\n");
		sb.append("</Placemark>\n\n\n");

		return cnt > 0 ? sb.toString() : null;
    }

    /**
     * Generates a KML chunk describing the given collecting event.
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected String generatePlacemark(final Locality loc, final String label)
    {
        if (loc == null || 
            loc.getLatitude1() == null || 
            loc.getLongitude1() == null)
        {
            return null;
        }
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();
        
        // TODO Finishing implementing this method with Geography
        
        //Geography  geo = loc.getGeography(); 

        // build the placemark
        StringBuilder sb = new StringBuilder("<Placemark>\n");
        {
            sb.append(GenericKMLGenerator.generateXmlElement("styleUrl", "#custom"));
            sb.append("\n");
        }
        sb.append("<name>");
        if (label != null)
        {
            sb.append(label);
        } else
        {
            sb.append(loc.getLocalityName());
        }
        sb.append("</name>\n");

        // build the fancy HTML popup description
        /*
        sb.append("<description><![CDATA[");
        sb.append("<h3>"+UIRegistry.getResourceString("GE_LOCALITY")+":</h3>\n<ul>\n");
        sb.append("<br/><table>\n");
        
        sb.append("<tr>\n");
        sb.append("<td style=\"color:#"+textColor+"\" >"++"</td>\n");
        sb.append("<td style=\"color:#"+textColor+"\" >"++"</td>\n");
        sb.append("</tr>\n");
        
        sb.append("</table>]]></description>\n");
        */
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
     * Generates a KML chunk describing the given collecting event.
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected String generatePlacemark(final CollectingEvent ce, final String label)
    {
        if (ce == null || 
            ce.getLocality() == null || 
            ce.getLocality().getLatitude1() == null ||
            ce.getLocality().getLongitude1() == null)
        {
            return null;
        }
        
		// get all of the important information
		Locality   loc = ce.getLocality();
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();

		// get event times
		Calendar   start   = ce.getStartDate();
		DateFormat dfStart = DateFormat.getDateInstance();
		
		String startString = "";
		if (start != null)
		{
		    dfStart.setCalendar(start);
		    startString = dfStart.format(start.getTime());
		}
		
		Calendar   end   = ce.getEndDate();
		DateFormat dfEnd = DateFormat.getDateInstance();
		
		String endString = "";
		if (end != null)
		{
		    dfEnd.setCalendar(end);
	        endString = dfEnd.format(end.getTime());
		}

		// get names of collectors
		List<String> agentNames = new Vector<String>();
		for( Collector c: ce.getCollectors() )
		{
            if (StringUtils.isEmpty(c.getAgent().getFirstName()))
            {
                agentNames.add(c.getAgent().getLastName());
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
				if( d.isCurrentDet() )
				{
					Taxon t = d.getPreferredTaxon();
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
        
        if (StringUtils.isNotEmpty(startString))
        {
            sb.append(startString);
            if (!startString.equals(endString))
            {
                sb.append(" - ");
                sb.append(endString);
            }
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
		
        String primaryURL        = AppPreferences.getRemote().get(PluginsTask.GE_BALLOON_PRIMARY_URL, null);
        String primaryURLTitle   = AppPreferences.getRemote().get(PluginsTask.GE_BALLOON_PRIMARY_URL_TITLE, null);
        String secondaryURL      = AppPreferences.getRemote().get(PluginsTask.GE_BALLOON_SECONDARY_URL, null);
        String secondaryURLTitle = AppPreferences.getRemote().get(PluginsTask.GE_BALLOON_SECONDARY_URL_TITLE, null);
        
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
                sb.append(formatter.formatToUI(coHash.get(tax).getCatalogNumber()));                
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
                String primaryURLStr = String.format(primaryURL, tax.first, tax.second);
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
     * @param sb
     * @param title
     * @param value
     */
    private void appendCellTR(final StringBuilder sb, 
                              final Integer tableId,
                              final String  colName,
                              final String  titleArg, 
                              final String  value)
    {
        String title = titleArg;
        if (titleArg == null && tableId != null)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
            if (ti != null)
            {
                if (colName != null)
                {
                    DBFieldInfo fi = ti.getFieldByColumnName(colName);
                    if (fi != null)
                    {
                        title = fi.getTitle();
                    }
                } else
                {
                    title = ti.getTitle();
                }
            }
        }
        if (StringUtils.isNotEmpty(title) &&StringUtils.isNotEmpty(value))
        {
            sb.append("<tr><td style=\"color:#"+textColor+"; text-align:right\">"+title+":</td><td style=\"color:#"+textColor+"\" >"+value+"</td></tr>\n");
        }
    }
    
    /**
     * Generates a KML chunk describing the given collecting event.
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected String generatePlacemark(final CollectionObject colObj, final String label)
    {
        if (colObj == null || 
            colObj.getCollectingEvent() == null || 
            colObj.getCollectingEvent().getLocality() == null)
        {
            return null;
        }
        
        CollectingEvent ce = colObj.getCollectingEvent();
        Locality        loc = ce.getLocality();
        
        if (loc.getLatitude1() == null || loc.getLongitude1() == null)
        {
            return null;
        }
        
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();
        
        //DBTableInfo coti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBTableInfo ceti = DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId());
        
        StringBuilder title        = new StringBuilder(colObj.getIdentityTitle());
        String        taxonStr     = null;
        String        startDateStr = null;
        String        geoStr       = null;
        for (Determination det : colObj.getDeterminations())
        {
            if (det.getIsCurrent())
            {
                Taxon taxon = det.getTaxon();
                if (taxon != null)
                {
                    taxonStr = taxon.getFullName();
                }
            }
        }
        
        if (ce.getStartDate() != null)
        {
            DBFieldInfo           sdFI  = ceti.getFieldByColumnName("StartDate");
            UIFieldFormatterIFace fmtr = sdFI.getFormatter();
            if (fmtr == null)
            {
                fmtr = UIFieldFormatterMgr.getInstance().getFormatter("PartialDate");
            }
            if (fmtr != null)
            {
                Object dateFmtObj = (String)fmtr.formatToUI(ce.getStartDate(), ce.getStartDatePrecision());
                if (dateFmtObj != null)
                {
                    startDateStr = dateFmtObj.toString();
                }
            }
        }
        
        if (loc.getGeography() != null)
        {
            geoStr = loc.getGeography().getFullName();
        }
        
        // build the placemark
        StringBuilder sb = new StringBuilder("<Placemark>\n");
        {
            sb.append(GenericKMLGenerator.generateXmlElement("styleUrl", "#custom"));
            sb.append("\n");
        }
        sb.append("<name>");
        if (label != null)
        {
            sb.append(label);
        } else
        {
            sb.append(title.toString());
        }
        sb.append("</name>\n");

        // build the fancy HTML popup description
        
        sb.append("<description><![CDATA[");
        //sb.append("<h3>"+UIRegistry.getResourceString("GE_LOCALITY")+":</h3>\n<ul><br/>\n");
        sb.append("<table>\n");
        
        appendCellTR(sb, CollectingEvent.getClassTableId(), "StartDate",  "Start Date", startDateStr);
        appendCellTR(sb, Taxon.getClassTableId(),            null,        "Taxon",      taxonStr);
        appendCellTR(sb, Locality.getClassTableId(),        "Latitude1",  "Latitude",   convertToDDDDDD(loc.getLatitude1(), DEGREES_FORMAT.String, DIRECTION.NorthSouth, 4));
        appendCellTR(sb, Locality.getClassTableId(),        "Longitude1", "Longitude",  convertToDDDDDD(loc.getLongitude1(), DEGREES_FORMAT.String, DIRECTION.EastWest, 4));
        appendCellTR(sb, Geography.getClassTableId(),       null,         "Geography",  geoStr);
        
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
