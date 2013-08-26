/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.tasks.services;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.util.LatLonConverter.convertToDDDDDD;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.PluginsTask;
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

	/** Keyhole Markup Language namespace declaration. */
	protected static String KML_NAMESPACE_DECL = "http://earth.google.com/kml/2.0";

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
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("kml").addAttribute("xmlns", KML_NAMESPACE_DECL);
        Element kmlDocument = root.addElement("Document");
        if (StringUtils.isNotEmpty(description))
        {
            kmlDocument.addElement("description").addText(description);
        }
        GenericKMLGenerator.generateStyle(kmlDocument, placemarkIconURL, balloonStyleBgColor, balloonStyleTextColor, balloonStyleText);
		
		boolean isDoingCollectingEvents = false;
		DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
    		for( int i = 0; i < dataObjs.size(); ++i )
    		{
                String           label   = labels.get(i);
                FormDataObjIFace dataObj = dataObjs.get(i);
                
                session.attach(dataObj);
                
                if (dataObj instanceof CollectingEvent)
                {
                    generatePlacemark(kmlDocument, (CollectingEvent)dataObj, label);
        			isDoingCollectingEvents = true;
        			
                } else if (dataObj instanceof Locality)
                {
                    generatePlacemark(kmlDocument, (Locality)dataObj, label);
                    
                } else if (dataObj instanceof CollectionObject)
                {
                    generatePlacemark(kmlDocument, (CollectionObject)dataObj, label);
                }
    		}
		} catch (Exception ex)
		{
		    ex.printStackTrace();
		    
		} finally
        {
            if (session != null)
            {
                session.close();
            }
        }
		
		if (isDoingCollectingEvents)
		{
		    /*String kmlStr = generatePathForLocalities();
		    if (kmlStr != null)
            {
                writer.write(kmlStr);
            }*/
		}
		
		FileWriter out = new FileWriter(filename);
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(out, format);
        writer.write(document);
        writer.close();
		out.close();
	}

//	/**
//	 * Builds an XML chunk describing the path from the first locality to the last.
//	 *
//	 * @return an XML string
//	 */
//	protected String generatePathForLocalities()
//	{
//	    int cnt = 0;
//		StringBuilder sb = new StringBuilder("<Placemark>\n");
//        sb.append(GenericKMLGenerator.generateXmlElement("styleUrl", "#custom"));
//        sb.append("\n");
//        sb.append("<LineString>\n");
//		sb.append("<coordinates>");
//		for( FormDataObjIFace dataObj : dataObjs )
//		{
//		    Locality loc = dataObj instanceof CollectingEvent ? ((CollectingEvent)dataObj).getLocality() : (Locality)dataObj;
//		    if (loc != null && loc.getLongitude1() != null && loc.getLatitude1() != null)
//		    {
//    			sb.append(loc.getLongitude1());
//    			sb.append(",");
//    			sb.append(loc.getLatitude1());
//    			sb.append(",");
//    			sb.append("0.0\n");
//    			cnt++;
//		    }
//		}
//		sb.append("</coordinates>\n");
//		sb.append("</LineString>\n");
//		sb.append("</Placemark>\n\n\n");
//
//		return cnt > 0 ? sb.toString() : null;
//    }

    /**
     * Generates a KML chunk describing the given collecting event.
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected void generatePlacemark(Element kmlDocument, final Locality loc, final String label)
    {
        if (loc == null || 
            loc.getLatitude1() == null || 
            loc.getLongitude1() == null)
        {
            return;
        }
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();
        
        // TODO Finishing implementing this method with Geography
        
        //Geography  geo = loc.getGeography(); 

        // build the placemark
        Element placemark = kmlDocument.addElement("Placemark");
        placemark.addElement("styleUrl").addText("#custom");
        placemark.addElement("name").addText(
                label != null ? label : loc.getLocalityName());

        // build the fancy HTML popup description
        placemark.addElement("description").addCDATA(generateHtmlDesc(loc));


        GenericKMLGenerator.buildPointAndLookAt(placemark,
                new Pair<Double, Double>(lat.doubleValue(), lon.doubleValue()));
    }


    /**
     * Generates a KML chunk describing the given collecting event.
     * @param kmlDocument 
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected void generatePlacemark(Element kmlDocument, final CollectingEvent ce, final String label)
    {
        if (ce == null || 
            ce.getLocality() == null || 
            ce.getLocality().getLatitude1() == null ||
            ce.getLocality().getLongitude1() == null)
        {
            return;
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


		// build the placemark
        Element placemark = kmlDocument.addElement("Placemark");
        placemark.addElement("styleUrl").addText("#custom");
		
		StringBuilder name = new StringBuilder();
        if(label != null)
        {
            name.append(label);
        }
        
        if (StringUtils.isNotEmpty(startString))
        {
            name.append(startString);
            if (StringUtils.isNotEmpty(endString) && !startString.equals(endString))
            {
                name.append(" - ");
                name.append(endString);
            }
        }
        
        placemark.addElement("name").addText(name.toString());
		// build the fancy HTML popup description
        
        placemark.addElement("description").addCDATA(generateHtmlDesc(ce));
        
        GenericKMLGenerator.buildPointAndLookAt(placemark, 
                new Pair<Double, Double>(lat.doubleValue(), lon.doubleValue()));
    }
        
    protected String generateHtmlDesc(CollectingEvent ce) {
        // get names of collectors
        List<String> agentNames = new Vector<String>();
        for( Collector c : ce.getCollectors() )
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
        for( CollectionObject co : ce.getCollectionObjects() )
        {
            String genus = null;
            String species = null;
            for (Determination d: co.getDeterminations())
            {
                if (d.isCurrentDet())
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


        Element desc = DocumentHelper.createElement("div");
		//sb.append("<center><h3>");
		//sb.append(startString);
		//sb.append(" - ");
		//sb.append(endString);
		//sb.append("</h3></center><br/>");
        desc.addElement("h3").addText(getResourceString("GE_COLLECTOR"));
		
		Element ul = desc.addElement("ul");
		for( String agent: agentNames )
		{
		    ul.addElement("li").addText(agent);
		}
		
		if (genusSpecies.size() > 0)
		{
		    desc.addElement("br");
		    desc.addElement("h3").addText(getResourceString("GE_COLLECTION_OBJECTS"));
    		
    		Element table = desc.addElement("table");
    		
    		AppPreferences remotePrefs = AppPreferences.getRemote();
    		
            String primaryURL        = remotePrefs.get(PluginsTask.GE_BALLOON_PRIMARY_URL, null);
            String primaryURLTitle   = remotePrefs.get(PluginsTask.GE_BALLOON_PRIMARY_URL_TITLE, null);
            String secondaryURL      = remotePrefs.get(PluginsTask.GE_BALLOON_SECONDARY_URL, null);
            String secondaryURLTitle = remotePrefs.get(PluginsTask.GE_BALLOON_SECONDARY_URL_TITLE, null);
            
            Element tr = table.addElement("tr");
            
            tr.addElement("th").addElement("center")
                .addText(getResourceString("GE_CATALOG_NUMBER"));
            
            tr.addElement("th").addText(getResourceString("GE_TAXONOMY"));

            if (StringUtils.isNotEmpty(primaryURL))
            {
                tr.addElement("th").addAttribute("style", "color:#" + textColor)
                    .addElement("center").addText(getResourceString("GE_PRIMARY"));
            }
            
            if (StringUtils.isNotEmpty(secondaryURL))
            {
                tr.addElement("th").addAttribute("style", "color:#" + textColor)
                    .addElement("center").addText(getResourceString("GE_SECONDARY"));
            }
            
    		for( Pair<String, String> tax : genusSpecies )
    		{
    		    tr = table.addElement("tr");
    		    tr.addElement("td").addElement("center")
    		        .addText( 
    		            (formatter != null) ?
    		                formatter.formatToUI(coHash.get(tax).getCatalogNumber()).toString()
    		                :
    		                coHash.get(tax).getCatalogNumber()
    		        );     
                
    			// simple name text
    			String taxonomicName = (tax.first != null ? tax.first : "") + " " + (tax.second != null ? tax.second : "");
    			
    			tr.addElement("td").addElement("i").addText(taxonomicName);
    
    			String linkTextColor = (textColor.startsWith("F") ? "WHITE" : "BLACK");
    			
                if (StringUtils.isNotEmpty(primaryURL))
                {
                    String primaryURLStr = String.format(primaryURL, tax.first, tax.second);
                    tr.addElement("td").addElement("center").addElement("a")
                        .addAttribute("style", "color:" + linkTextColor)
                        .addAttribute("href", primaryURLStr)
                        .addText(primaryURLTitle);
                }
                
                if (StringUtils.isNotEmpty(secondaryURL))
                {
                    String secondaryURLStr = String.format(secondaryURL, tax.first, tax.second);
                    tr.addElement("td").addElement("center").addElement("a")
                    .addAttribute("style", "color:" + linkTextColor)
                    .addAttribute("href", secondaryURLStr)
                    .addText(secondaryURLTitle);
                }
                
    			if( speciesToImageMapper != null )
    			{
    				String imgSrc = speciesToImageMapper.get(taxonomicName);
                    //System.out.println("["+taxonomicName+"]["+imgSrc+"]");
    				if( imgSrc != null )
    				{
    				    tr.addElement("td").addElement("img")
    				        .addAttribute("src", imgSrc);
    				}
    				else
    				{
    				    tr.addElement("td");
    				}
    			}
    		}
		}
		return XMLtoString(desc);
    }

    private String XMLtoString(Element el)
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        Writer writer = new StringWriter();
        try
        {
            new XMLWriter(writer, format).write(el);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return writer.toString();
    }
    
    private String generateHtmlDesc(Locality loc)
    {
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();

        Element desc = DocumentHelper.createElement("div");
//        desc.addElement("h3").addText(getResourceString("GE_LOCALITY"));
        
        Element table = desc.addElement("table");
        Element tr = table.addElement("tr");
        tr.addElement("td").addAttribute("style", "color:#"+textColor+"; text-align:right")
            .addText(getResourceString("Latitude")+":");
        tr.addElement("td").addAttribute("style", "color:#"+textColor)
            .addText("" + lat.doubleValue());
        
        tr = table.addElement("tr");
        tr.addElement("td").addAttribute("style", "color:#"+textColor+"; text-align:right")
            .addText(getResourceString("Longitude")+":");
        tr.addElement("td").addAttribute("style", "color:#"+textColor)
            .addText("" + lon.doubleValue());
        
        if (loc.getGeography() != null)
        {
            tr = table.addElement("tr");
            tr.addElement("td").addAttribute("style", "color:#"+textColor+"; text-align:right")
                .addText(getResourceString("Geography")+":");
            tr.addElement("td").addAttribute("style", "color:#"+textColor)
                .addText(loc.getGeography().getFullName());
        }
        
        return XMLtoString(desc);
    }
    
    private String generateHtmlDesc(CollectionObject colObj)
    {
        CollectingEvent ce = colObj.getCollectingEvent();
        Locality        loc = ce.getLocality();
       
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
                Object dateFmtObj = fmtr.formatToUI(ce.getStartDate(), ce.getStartDatePrecision());
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
        
//        sb.append(
//                DocumentHelper.createElement("h3")
//                    .addText(getResourceString("GE_LOCALITY")+":").asXML());
        
        Element table = DocumentHelper.createElement("table");
   
  
        appendCellTR(table, CollectingEvent.getClassTableId(), "StartDate",  "Start Date", startDateStr);
        appendCellTR(table, Taxon.getClassTableId(),            null,        "Taxon",      taxonStr);
        appendCellTR(table, Locality.getClassTableId(),        "Latitude1",  "Latitude",   convertToDDDDDD(loc.getLatitude1(), DEGREES_FORMAT.String, DIRECTION.NorthSouth, 4));
        appendCellTR(table, Locality.getClassTableId(),        "Longitude1", "Longitude",  convertToDDDDDD(loc.getLongitude1(), DEGREES_FORMAT.String, DIRECTION.EastWest, 4));
        appendCellTR(table, Geography.getClassTableId(),       null,         "Geography",  geoStr);

        return XMLtoString(table);
    }


    /**
     * @param table
     * @param title
     * @param value
     */
    private void appendCellTR(final Element table, 
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
            Element tr = table.addElement("tr");
            tr.addElement("td").addAttribute("style", "color:#"+textColor+"; text-align:right")
                .addText(title + ":");
            tr.addElement("td").addAttribute("style", "color:#"+textColor)
                .addText(value);
        }
    }
    
    /**
     * Generates a KML chunk describing the given collecting event.
     *
     * @param ce the event
     * @param label the label for the event
     * @return the KML string
     */
    protected void generatePlacemark(Element kmlDocument, final CollectionObject colObj, final String label)
    {
        if (colObj == null || 
            colObj.getCollectingEvent() == null || 
            colObj.getCollectingEvent().getLocality() == null)
        {
            return;
        }
        
        CollectingEvent ce = colObj.getCollectingEvent();
        Locality        loc = ce.getLocality();
        
        if (loc.getLatitude1() == null || loc.getLongitude1() == null)
        {
            return;
        }
        
        BigDecimal lat = loc.getLatitude1();
        BigDecimal lon = loc.getLongitude1();
        
        //DBTableInfo coti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBTableInfo ceti = DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId());
        
        StringBuilder title        = new StringBuilder(colObj.getIdentityTitle());

        // build the placemark
        Element placemark = kmlDocument.addElement("Placemark");
        placemark.addElement("styleUrl").addText("#custom");
        placemark.addElement("name").addText(
                label != null ? label : title.toString());
        
        placemark.addElement("description").addCDATA(generateHtmlDesc(colObj));
       
        GenericKMLGenerator.buildPointAndLookAt(placemark,
                new Pair<Double, Double>(lat.doubleValue(), lon.doubleValue()));
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
