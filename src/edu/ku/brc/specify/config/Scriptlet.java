/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.config;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchJRDataSource;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Triple;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: Scriptlet.java,v 1.7 2005/04/04 15:18:41 teodord Exp $
 */
public class Scriptlet extends JRDefaultScriptlet
{
    private static final Logger log = Logger.getLogger(Scriptlet.class);
    
    private static final String SCRPLT_N = "SCRPLT_N";
    private static final String SCRPLT_S = "SCRPLT_S";
    private static final String SCRPLT_E = "SCRPLT_E";
    private static final String SCRPLT_W = "SCRPLT_W";
    
    
    protected UIFieldFormatterIFace catalogFormatter = AppContextMgr.getInstance().getFormatter("CollectionObject", "CatalogNumber");
    
    protected final static String stdFormat = "yyyy-MM-dd";
    protected HashMap<String, SimpleDateFormat> dateFormatHash = new HashMap<String, SimpleDateFormat>();

    /**
     * beforeReportInit.
     */
    public void beforeReportInit() throws JRScriptletException
    {
        //System.out.println("call beforeReportInit");
    }

    /**
     * afterReportInit.
     */
    public void afterReportInit() throws JRScriptletException
    {
        //System.out.println("call afterReportInit");
    }

    /**
     * beforePageInit.
     */
    public void beforePageInit() throws JRScriptletException
    {
        //System.out.println("call   beforePageInit : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }

    /**
     *
     */
    public void afterPageInit() throws JRScriptletException
    {
        //System.out.println("call   afterPageInit  : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }

    /**
     *
     */
    public void beforeColumnInit() throws JRScriptletException
    {
        //System.out.println("call     beforeColumnInit");
    }

    /**
     * afterColumnInit.
     */
    public void afterColumnInit() throws JRScriptletException
    {
        //System.out.println("call     afterColumnInit");
    }

    /**
     * beforeGroupInit.
     */
    public void beforeGroupInit(String groupName) throws JRScriptletException
    {
        /*if (groupName.equals("CityGroup"))
        {
            System.out.println("call       beforeGroupInit : City = " + this.getFieldValue("City"));
        }*/
    }

    /**
     * afterGroupInit.
     */
    public void afterGroupInit(String groupName) throws JRScriptletException
    {
        /*if (groupName.equals("CityGroup"))
        {
            System.out.println("call       afterGroupInit  : City = " + this.getFieldValue("City"));

            String allCities = (String)this.getVariableValue("AllCities");
            String city = (String)this.getFieldValue("City");
            StringBuffer sbuffer = new StringBuffer();

            if (allCities != null)
            {
                sbuffer.append(allCities);
                sbuffer.append(", ");
            }

            sbuffer.append(city);
            this.setVariableValue("AllCities", sbuffer.toString());
        }*/
    }

    /**
     * beforeDetailEval.
     */
    public void beforeDetailEval() throws JRScriptletException
    {
        //System.out.println("        detail");
    }

    /**
     * afterDetailEval.
     */
    public void afterDetailEval() throws JRScriptletException
    {
    }

    /**
     * Formats a String to a float to a String.
     * @param floatStr the string with a Float value
     * @return Formats a String to a float to a String
     * @throws JRScriptletException xxx
     */
    public String formatCatNo(String catalogNumber)
    {
        //log.debug("********* Catalog Formatter["+catalogFormatter+"]["+(catalogFormatter != null ? catalogFormatter.isFromUIFormatter() : "")+"]["+catalogNumber+"]");
        if (catalogFormatter != null && catalogFormatter.isInBoundFormatter())
        {
            return catalogFormatter.formatToUI(catalogNumber).toString();
        }
        return catalogNumber;
    }

    /*
    public String formatCatNo(Float catalogNo) throws JRScriptletException
    {
        if (catalogNo == null)
        {
            return "N/A";
        }
        return String.format("%*.0f", new Object[] {catalogNo});
    }*/

    /**
     * Formats a float to a string.
     * @param floatVar the float variable
     * @return Formats a float to a string
     * @throws JRScriptletException
     */
    public String format(Float floatVar) throws JRScriptletException
    {
        if (floatVar == null) { return ""; }

        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(floatVar.floatValue());
    }

    /**
     * Formats a Float to a string with "N","S","E", "W".
     * @param floatVal the Float value
     * @param isLat whether it is a lat or lon
     * @return Formats a float to a string with "N","S","E", "W"
     */
    public String getDirChar(final Float floatVal, final boolean isLat)
    {
        if (floatVal == null) { return ""; }

        String key;
        if (isLat)
        {
            key = floatVal.floatValue() > 0.0 ? SCRPLT_N : SCRPLT_S;
        } else
        {
            key = floatVal.floatValue() > 0.0 ? SCRPLT_E : SCRPLT_W;
        }
        return UIRegistry.getResourceString(key);
    }
    
    

    /**
     * Formats a BigDecimal to a string with "N","S","E", "W".
     * @param bdValue the float value
     * @param isLat whether it is a lat or lon
     * @return Formats a float to a string with "N","S","E", "W"
     */
    public String getDirChar(final BigDecimal bdValue, final boolean isLat)
    {
        if (bdValue == null) { return ""; }

        String key;
        if (isLat)
        {
            key = bdValue.floatValue() > 0.0 ? SCRPLT_N : SCRPLT_S;
        } else
        {
            key = bdValue.floatValue() > 0.0 ? SCRPLT_E : SCRPLT_W;
        }
        return UIRegistry.getResourceString(key);

    }

    /**
     * Formats a String as a float with "N","S","E", "W".
     * @param floatVal the float value
     * @param isLat whether it is a lat or lon
     * @return Formats a String as a float with "N","S","E", "W"
     */
    public String getDirChar(final String strVal, final boolean isLat)
    {
        if (strVal == null) { return ""; }
        return getDirChar(new Float(Float.parseFloat(strVal)), isLat);
    }

    /**
     * Formats a BigDecimal into a lat/lon with "N","S","E", "W".
     * @param value
     * @param originalLatLongUnit
     * @param isLat
     * @return
     * @throws JRScriptletException
     */
    public String formatLatLon(final BigDecimal value, 
                               final Integer    originalLatLongUnit, 
                               final boolean    isLat) throws JRScriptletException
    {
        if (value != null) 
        { 
            return LatLonConverter.format(value, 
                                   isLat ? LatLonConverter.LATLON.Latitude : LatLonConverter.LATLON.Longitude, 
                                   LatLonConverter.convertIntToFORMAT(originalLatLongUnit), 
                                   LatLonConverter.DEGREES_FORMAT.Symbol, 
                                   LatLonConverter.DECIMAL_SIZES[originalLatLongUnit]);
        }
        return "";
    }

    /**
     * Formats a String with a float value as a degrees.
     * @param floatStr
     * @param isLat indicates whether it is a latitude or a longitude
     * @return Formats a String with a float value as a degrees
     * @throws JRScriptletException XXX
     */
    public String degrees(final String floatStr, final boolean isLat) throws JRScriptletException
    {
        return "Not Implemented!";//degrees(new Float(Float.parseFloat(floatStr)), isLat);
    }

    /**
     * Formats a Lat,Lon into a single string where the values are separated by a comma.
     * @param desc a prefix of a description
     * @param lat the latitude
     * @param lon the longitude
     * @return Formats a Lat,Lon into a single string where the values are separated by a comma
     * @throws JRScriptletException XXX
     */
    public String locality(final Object desc, 
                           final Float lat, 
                           final Float lon, 
                           final int    originalLatLongUnit) throws JRScriptletException
    {
        return localityBD(desc, new BigDecimal(lat), new BigDecimal(lon), originalLatLongUnit);
    }

    /**
     * Formats a Lat,Lon into a single string where the values are separated by a comma.
     * @param desc a prefix of a description
     * @param lat the latitude
     * @param lon the longitude
     * @return Formats a Lat,Lon into a single string where the values are separated by a comma
     * @throws JRScriptletException XXX
     */
    public String localityBD(final Object desc, 
                             final BigDecimal lat, 
                             final BigDecimal lon, 
                             final Integer    originalLatLongUnit) throws JRScriptletException
    {

        StringBuffer strBuf = new StringBuffer();
        if (desc instanceof String)
        {
            strBuf.append(((String) desc));
        } else if (desc instanceof byte[])
        {
            strBuf.append(new String((byte[]) desc));
        }
        strBuf.append(" ");
        strBuf.append(formatLatLon(lat, originalLatLongUnit, true));
        strBuf.append(", ");
        strBuf.append(formatLatLon(lon, originalLatLongUnit, false));
        return strBuf.toString();
    }

    /**
     * Formats the Field Number.
     * @param fieldNumber
     * @return the field number
     */
    public String formatFieldNo(String fieldNumber)
    {
        return fieldNumber == null ? "" : fieldNumber;
    }
    
    /**
     * @param format
     * @return
     */
    public String getCurrentDate(final String format)
    {
        return formatDate(new Date(System.currentTimeMillis()), format);
    }

    /**
     * Creates the category string which is either "LOAN" or "GIFT"
     * @param isGift
     * @return "LOAN" if isGift is null else "GIFT"
     */
    public String loanCategory(final Boolean isGift)
    {
        if (isGift)
        {
            return "GIFT";
        }
        return "LOAN";
    }

    /**
     * Builds the shipped to agent's name string.
     * @param firstName
     * @param lastName
     * @param middleInitial
     */
    public String buildNameString(final String firstName, final String lastName, final String middleInitial)
    {
        String name = StringUtils.isNotEmpty(lastName) ? lastName : "";
        if (StringUtils.isNotEmpty(firstName))
        {
            name += (name.length() > 0 ? ", " : "") + firstName;
        }
        if (StringUtils.isNotEmpty(middleInitial))
        {
            name += (name.length() > 0 ? " " : "") + middleInitial;
        }
        return name;
    }

    /**
     * @param catalogNumber
     * @return string giving type status for determinations for catalogNumber. "" if no type determinations exist.
     * 
     * NOTE: for testing. Not yet suitable for multi-collection dbs.
     * 
     */
    public String getTypeStatus(final String catalogNumber)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //System.out.println(colEvId);
        
        try
        {
        	String result = "";
        	CollectionObject co = session.getData(CollectionObject.class, "catalogNumber", catalogNumber, DataProviderSessionIFace.CompareType.Equals);
        	if (co != null)
        	{
        	
        		List<Determination> list = session.getDataList(Determination.class, "collectionObjectId", co.getId(), DataProviderSessionIFace.CompareType.Equals);
        
        		if (list.size() > 0)
        		{
        			Determination d = list.get(0);
        			if (d.getTypeStatusName() != null)
        			{
        				if (!result.equals(""))
        				{
        					result += ", ";
        				}
        				result += d.getTypeStatusName();
        			}
        		}
        		return result;
        	} else
        	{
        			log.error("Couldn't locate CollectionObject [" + catalogNumber + "]");
        			return result;
        	}
        } finally 
        {
        	session.close();
        }
    }
    
    /**
     * @param catalogNumber
     * @return FullTaxonName + Author for type determinations for catalognumber.
     * 
     * 
     * NOTE: for testing. Not yet suitable for multi-collection dbs.
     * 
     */
    public String getTypeTaxon(final String catalogNumber)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //System.out.println(colEvId);
        
        try
        {
        	String result = "";
        	CollectionObject co = session.getData(CollectionObject.class, "catalogNumber", catalogNumber, DataProviderSessionIFace.CompareType.Equals);
        	if (co != null)
        	{
        	
        		List<Determination> list = session.getDataList(Determination.class, "collectionObjectId", co.getId(), DataProviderSessionIFace.CompareType.Equals);
        
        		if (list.size() > 0)
        		{
        			Determination d = list.get(0);
        			if (d.getTypeStatusName() != null)
        			{
        				Taxon t = null;
        				if (d.getTaxon() != null)
        				{
        					t = session.getData(Taxon.class, "taxonId", d.getTaxon().getId(), DataProviderSessionIFace.CompareType.Equals);
        				}
        				if (t != null)
        				{	
        					if (!result.equals(""))
        					{
        						result += ", ";
        					}
        					result += t.getFullName() + " " + t.getAuthor();
        				} else
        				{
                			log.error("Couldn't locate taxon [" + d.getTaxon() + "]");
        				}
        			}
        		}
        		return result;
        	} else
        	{
        			log.error("Couldn't locate CollectionObject [" + catalogNumber + "]");
        			return result;
        	}
        } finally 
        {
        	session.close();
        }
    }
    
    /**
     * Builds the locality string.
     * @param geoName - the geography place name (country, state)
     * @param localityName - the locality name
     * @param latitude - latitude
     * @param longitude - longitude
     */
    public String buildLocalityString(final String geoName,
                                      final String localityName,
                                      final String latitude,
                                      final String longitude)
    {
        String locality = "";

        if (geoName != null && geoName.length() >= 1)
        {
            locality += geoName;
        }

        if (localityName != null && localityName.length() >= 1)
        {
            locality += ", " + localityName;
        }

        if (latitude != null && latitude.length() >= 1)
        {
            String temp1[] = latitude.split("deg");
            locality += ", " + temp1[0] + (temp1.length > 1 ? temp1[1] : "");
        }

        if (longitude != null && longitude.length() >= 1)
        {
            String temp2[] = longitude.split("deg");
            locality += ", " + temp2[0] + (temp2.length > 1 ? temp2[1] : "");
        }

        return locality;
    }

    /**
     * Create a string representing the difference between two dates.
     * @param startDate the start date
     * @param endDate the end date
     */
    public String dateDifference(java.sql.Date startDate, java.sql.Date endDate)
    {
        String loanLength = UIRegistry.getResourceString("NA");
        
        if (startDate != null && endDate != null)
        {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            int monthCount = 0;
            while (startCal.before(endCal))
            {
                startCal.add(Calendar.MONTH, 1);
                monthCount++;
            }

            loanLength = String.format(UIRegistry.getResourceString("SCRPLT_MON_LEN"), monthCount);
        }
        return loanLength;
    }
    
    /**
     * @param date
     * @param format
     * @return
     */
    public String formatDate(final Date date, final String format)
    {
        String           fmtStr = StringUtils.isNotEmpty(format) ? format : stdFormat;
        SimpleDateFormat sdf    = dateFormatHash.get(fmtStr);
        if (sdf == null)
        {
            sdf = new SimpleDateFormat(fmtStr);
            dateFormatHash.put(format, sdf);
        }
        return sdf.format(date);
    }

    /**
     * @param sqlDate
     * @param format
     * @return
     */
    public String formatDate(final java.sql.Date sqlDate, final String format)
    {
        return formatDate((Date)sqlDate, format);
    }

    /**
     * Returns a list of collectors
     * @param colEvId
     * @return
     */
    public String getCollectors(final Integer colEvId)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //System.out.println(colEvId);

        //DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getInstance().lookupByClassName(CollectingEvent.class.getName());
        String collectorsStr = UIRegistry.getResourceString("NA");
        List<?> list = session.getDataList(CollectingEvent.class, "collectingEventId", colEvId);
        if (list.size() > 0)
        {
            CollectingEvent ce = (CollectingEvent) list.get(0);
            Set<Collector> collectors = ce.getCollectors();
            if (collectors.size() > 0)
            {
                collectorsStr = DataObjFieldFormatMgr.getInstance().aggregate(collectors, Collector.class);
            } else
            {
                collectorsStr = UIRegistry.getResourceString("SCRPLT_NO_COLTRS");
            }

        } else
        {
            log.error("Couldn't locate CollecingEventID [" + colEvId + "]");
        }

        session.close();

        return collectorsStr;
    }

    /**
     * Converts Integer object to int nul -> 0.
     * @param val the value
     * @return an int value
     */
    protected int convertInt(final Integer val)
    {
        return val == null ? 0 : val.intValue();
    }

    /**
     * Returns the count minus quantityReturned minus quantityResolved to see if any are available.
     * @param countArg the count of preps
     * @param QuantityReturnedArg the quant returned
     * @param QuantityResolvedArg the ones remaining
     * @return
     */
    public Integer calcLoanQuantity(final Integer countArg,
                                    final Integer QuantityReturnedArg,
                                    final Integer QuantityResolvedArg)
    {
        int count = convertInt(countArg);
        int quantityReturned = convertInt(QuantityReturnedArg);
        int quantityResolved = convertInt(QuantityResolvedArg);
        return count - quantityReturned - quantityResolved;
    }

    /**
     * Creates a formated label from a given datasource
     * @param dataSource the WorkbenchJRDataSource
     * @return label string value
     */
    public String formatDetermination(Object dataSource)
    {

        String label = new String();
        String data = new String();
        String styleInfo = new String();

        if (dataSource instanceof WorkbenchJRDataSource)
        {
            WorkbenchJRDataSource rowDataSource = (WorkbenchJRDataSource) dataSource;
            String isCurrent1 = rowDataSource.getFieldValue("isCurrent1").toString();
            String isCurrent2 = rowDataSource.getFieldValue("isCurrent2").toString();

            //assume 1 if isCurrent has no value
            if ((isCurrent1.equals("true")) || ( (isCurrent1.equals("")) && (isCurrent2.equals("")) ))
            {

                Vector<String> labelNames = isCurrent1Labels();

                //create label
                for (Enumeration<?> e = labelNames.elements(); e.hasMoreElements();)
                {
                    data = rowDataSource.getFieldValue((String) e.nextElement()).toString();

                    try
                    {

                        if (StringUtils.isNotEmpty(data))
                        {
                            styleInfo = (String) e.nextElement();
                            //if there is specific style info
                            if (styleInfo.startsWith("<style"))
                            {
                                label = label.concat(styleInfo + StringEscapeUtils.escapeHtml(data) + " </style>");
                            } else
                            //no style
                            {
                                label = label.concat(styleInfo + data + " ");
                            }
                        }

                    } catch (NoSuchElementException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Scriptlet.class, ex);
                        log.error(ex);
                        return label;
                    }

                }

            } else if (isCurrent2.equals("true"))//use isCurrent 2 values
            {

                Vector<String> labelNames = isCurrent2Labels();

                //create label
                for (Enumeration<?> e = labelNames.elements(); e.hasMoreElements();)
                {
                    data = rowDataSource.getFieldValue((String) e.nextElement()).toString();

                    try
                    {

                        if (StringUtils.isNotEmpty(data))
                        {
                            styleInfo = (String) e.nextElement();
                            //if there is specific style info
                            if (styleInfo.startsWith("<style"))
                            {
                                label = label.concat(styleInfo + data + " </style>");
                            } else
                            //no style
                            {
                                label = label.concat(styleInfo + data + " ");
                            }
                        }

                    } catch (NoSuchElementException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Scriptlet.class, ex);
                        log.error(ex);
                        return label;
                    }

                }
            }
        }
        //else
        return label;
    }

    //create the order and style information of the label.
    //add the determiner name first, followed by its style information.
    public Vector<String> isCurrent1Labels()
    {
        Vector<String> labelNames = new Vector<String>();
        labelNames.add("genus1");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("speciesQualifier1");
        labelNames.add("");
        labelNames.add("species1");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("speciesAuthorFirstName1");
        labelNames.add("");
        labelNames.add("speciesAuthorLastName1");
        labelNames.add("");
        labelNames.add("subspeciesQualifier1");
        labelNames.add("");
        labelNames.add("subspecies1");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("infraAuthorFirstName1");
        labelNames.add("");
        labelNames.add("infraAuthorLastName1");
        labelNames.add("");
        labelNames.add("varietyQualifier1");
        labelNames.add("var.");
        labelNames.add("variety1");
        labelNames.add("<style isItalic=\"true\">");

        return labelNames;
    }

    // create the order and style information of the label.
    //add the determiner name first, followed by its style information.
    public Vector<String> isCurrent2Labels()
    {
        Vector<String> labelNames = new Vector<String>();
        labelNames.add("genus2");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("speciesQualifier2");
        labelNames.add("");
        labelNames.add("species2");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("speciesAuthorFirstName2");
        labelNames.add("");
        labelNames.add("speciesAuthorLastName2");
        labelNames.add("");
        labelNames.add("subspeciesQualifier2");
        labelNames.add("");
        labelNames.add("subspecies2");
        labelNames.add("<style isItalic=\"true\">");
        labelNames.add("infraAuthorFirstName2");
        labelNames.add("");
        labelNames.add("infraAuthorLastName2");
        labelNames.add("");
        labelNames.add("varietyQualifier2");
        labelNames.add("var.");
        labelNames.add("variety2");
        labelNames.add("<style isItalic=\"true\">");

        return labelNames;
    }
    
    protected static final int Genus                  = 1;
    protected static final int SpeciesQualifier       = 2;
    protected static final int Species                = 4;
    protected static final int SpeciesAuthorFirstName = 8;
    protected static final int SpeciesAuthorLastName  = 16;
    protected static final int SubspeciesQualifier    = 32;
    protected static final int Subspecies             = 64;
    protected static final int InfraAuthorFirstName   = 128;
    protected static final int InfraAuthorLastName    = 256;
    protected static final int VarietyQualifier       = 512;
    protected static final int Variety                = 1024;
    
    protected int mask;
    protected TaxonInfo info = new TaxonInfo();
    protected Triple<String, String, String>   cit   = new Triple<String, String, String>();
    protected Statement stmt = null;
    
    protected boolean isOn(final int opt)
    {
        return (mask & opt) == opt;
    }
    
    protected void setOn(final int opt)
    {
        mask |= opt;
    }
    
    public String formatTaxonWithAuthors(final String genus,
                                         final String speciesQualifier,
                                         final String species,
                                         final String speciesAuthorFirstName,
                                         final String speciesAuthorLastName,
                                         final String subspeciesQualifier,
                                         final String subspecies,
                                         final String infraAuthorFirstName,
                                         final String infraAuthorLastName,
                                         final String varietyQualifier,
                                         final String variety)
    {
        //  1 - Genus
        //  2 - speciesQualifier
        //  3 - species
        //  4 - speciesAuthorFirstName
        //  5 - speciesAuthorLastName
        //  6 - subspeciesQualifier
        //  7 - subspecies
        //  8 - infraAuthorFirstName
        //  9 - infraAuthorLastName
        // 10 - varietyQualifier
        // 11 - variety
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(genus); // 1 Genus
        sb.append(" ");
        
        if (isOn(SpeciesQualifier))
        {
            sb.append("<style isItalic=\"true\">");
            sb.append(speciesQualifier); // 2
            sb.append("</style>");
            sb.append(" ");
        }
        sb.append(species); // 3
        
        if (isOn(SpeciesAuthorFirstName) || isOn(SpeciesAuthorLastName))
        {
            sb.append(" ");
            sb.append("<style isItalic=\"true\">(");
            if (isOn(SpeciesAuthorFirstName))
            {
                sb.append(speciesAuthorFirstName); // 4
                if (isOn(SpeciesAuthorLastName))
                {
                    sb.append(" ");  
                }
            }
            if (isOn(SpeciesAuthorLastName))
            {
                sb.append(speciesAuthorLastName); // 5
            }
            sb.append(")</style>");
        }

        if (isOn(SubspeciesQualifier))
        {
            sb.append(" ");
            sb.append(subspeciesQualifier); // 6
        }
        if (isOn(Subspecies))
        {
            sb.append(" subsp. ");
            sb.append(subspecies); // 7
        }
        
        if (isOn(InfraAuthorFirstName) || isOn(InfraAuthorLastName))
        {
            sb.append(" ");
            sb.append("<style isItalic=\"true\">(");
            if (isOn(InfraAuthorFirstName))
            {
                sb.append(infraAuthorFirstName); // 4
                if (isOn(InfraAuthorLastName))
                {
                    sb.append(" ");  
                }
            }
            if (isOn(InfraAuthorLastName))
            {
                sb.append(infraAuthorLastName); // 5
            }
            sb.append(")</style>");
        }
        
        if (isOn(VarietyQualifier))
        {
            sb.append(" ");  
            sb.append(varietyQualifier);  // 10
        }
        
        if (isOn(Variety))
        {
            sb.append(" var. ");
            sb.append(variety);  // 11
        }
        
        //System.err.println("----------------------------------------------------------------------------");
        //System.err.println(sb.toString());
        
        return sb.toString();
    }
    
    /**
     * @param taxonId
     * @return
     */
    protected TaxonInfo getTaxonInfo(final int taxonId)
    {
        if (stmt == null)
        {
            try
            {
                stmt = DBConnection.getInstance().getConnection().createStatement();
            } catch (SQLException ex)
            {
                log.debug(ex);
            }
        }
        
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery("SELECT Name, RankID, ParentID, Author FROM taxon WHERE TaxonID = "+taxonId);
            if (rs.next())
            {
                info.set(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getString(4));
            }
            
        } catch (SQLException ex)
        {
            log.debug(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                } catch (SQLException ex) {}
            }
        }
        return info;
    }
    
    /**
     * @param taxonId
     * @return
     */
    protected Triple<String, String, String> getAuthor(final int taxonId)
    {
        if (stmt == null)
        {
            try
            {
                stmt = DBConnection.getInstance().getConnection().createStatement();
            } catch (SQLException ex)
            {
                log.debug(ex);
            }
        }
        
        ResultSet rs = null;
        try
        {
            String sql = "SELECT r.Title, r.WorkDate, a.FirstName, a.MiddleInitial, a.LastName " +
                         "FROM taxoncitation ct INNER JOIN referencework r ON ct.ReferenceWorkID = r.ReferenceWorkID " +    
                         "LEFT JOIN author au ON r.ReferenceWorkID = au.ReferenceWorkID " +
                         "INNER JOIN agent a ON au.AgentID = a.AgentID WHERE ct.TaxonID = " + taxonId + " ORDER BY au.OrderNumber ASC";
                        
            rs = stmt.executeQuery(sql);
            cit.third = "";
            while (rs.next())
            {
                cit.first  = rs.getString(1);
                cit.second = rs.getString(2);
                
                //String first = rs.getString(3);
                //String mid   = rs.getString(4);
                String last  = rs.getString(5);
                if (StringUtils.isNotEmpty(cit.third)) cit.third += ", ";
                cit.third += last;
            }
            
        } catch (SQLException ex)
        {
            log.debug(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                } catch (SQLException ex) {}
            }
        }
        return cit;
    }
    
    /**
     * @param taxonId
     * @return
     */
    protected Triple<String, String, String> getDeterminationQualifiers(final int detId)
    {
        if (stmt == null)
        {
            try
            {
                stmt = DBConnection.getInstance().getConnection().createStatement();
                
            } catch (SQLException ex)
            {
                log.debug(ex);
            }
        }
        
        ResultSet rs = null;
        try
        {
            String sql = "SELECT Qualifier, SubSpQualifier, VarQualifier FROM determination WHERE DeterminationID = " + detId;
            rs = stmt.executeQuery(sql);
            cit.third = "";
            while (rs.next())
            {
                info.spQualifer    = rs.getString(1);
                info.subSpQualifer = rs.getString(2);
                info.varQualifer   = rs.getString(3);
            }
            
        } catch (SQLException ex)
        {
            log.debug(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                } catch (SQLException ex) {}
            }
        }
        return cit;
    }
    
    /**
     * @param taxonIdArg
     * @return
     */
    public String getTaxonNameWithAuthors(final Integer taxonIdArg, final Integer detIdArg)
    {
        int taxonId = taxonIdArg;
        
        String genus                  = null;
        String speciesQualifier       = null;
        String species                = null;
        String speciesAuthorFirstName = null;
        String speciesAuthorLastName  = null;
        String subspeciesQualifier    = null;
        String subspecies             = null;
        String infraAuthorFirstName   = null;
        String infraAuthorLastName    = null;
        String varietyQualifier       = null;
        String variety                = null;
        
        mask = 0;
        
        info.clear();
        
        if (detIdArg != null)
        {
            getDeterminationQualifiers(detIdArg);
            
            if (StringUtils.isNotEmpty(info.spQualifer))
            {
                speciesQualifier = info.spQualifer;
                setOn(SpeciesQualifier);
            }
            if (StringUtils.isNotEmpty(info.subSpQualifer))
            {
                subspeciesQualifier = info.subSpQualifer;
                setOn(SubspeciesQualifier);
            }
            if (StringUtils.isNotEmpty(info.varQualifer))
            {
                varietyQualifier = info.varQualifer;
                setOn(VarietyQualifier);
            }
        }
        
        getTaxonInfo(taxonId);
        if (info.rankId == TaxonTreeDef.VARIETY)
        {
            variety     = info.name;
            taxonId     = info.parentId;
            info.rankId = TaxonTreeDef.SUBSPECIES;
            setOn(Variety);
        }
        
        if (info.rankId == TaxonTreeDef.SUBSPECIES)
        {
            getTaxonInfo(taxonId);
            if (StringUtils.isNotEmpty(info.author))
            {
                cit.third = info.author;
            } else
            {
                getAuthor(taxonId);
            }
            if (cit.third != null)
            {
                infraAuthorLastName = cit.third;
                setOn(InfraAuthorLastName);
            }
            
            subspecies  = info.name;
            taxonId     = info.parentId;
            info.rankId = TaxonTreeDef.SPECIES;
            setOn(Subspecies);
        }
        
        if (info.rankId == TaxonTreeDef.SPECIES)
        {
            getTaxonInfo(taxonId);
            if (StringUtils.isNotEmpty(info.author))
            {
                cit.third = info.author;
            } else
            {
                getAuthor(taxonId);
            }
            if (cit.third != null)
            {
                speciesAuthorLastName = cit.third;
                setOn(SpeciesAuthorLastName);
            }
            
            species     = info.name;
            taxonId     = info.parentId;
            info.rankId = TaxonTreeDef.GENUS;
            setOn(Species);
        }
        
        if (info.rankId == TaxonTreeDef.GENUS)
        {
            getTaxonInfo(taxonId);
            
            genus       = info.name;
            taxonId     = info.parentId;
            info.rankId = TaxonTreeDef.SUBSPECIES;
            setOn(Genus);
        }
        
        if (stmt != null)
        {
            try
            {
                stmt.close();
                stmt = null;
                
            } catch (SQLException ex) {}
        }
        
        return formatTaxonWithAuthors(genus,
                                      speciesQualifier,
                                      species,
                                      speciesAuthorFirstName,
                                      speciesAuthorLastName,
                                      subspeciesQualifier,
                                      subspecies,
                                      infraAuthorFirstName,
                                      infraAuthorLastName,
                                      varietyQualifier,
                                      variety);
    }
    
    class TaxonInfo
    {
        String  name;
        Integer rankId;
        Integer parentId;
        String  author;
        
        // Qualifiers
        String spQualifer;
        String subSpQualifer;
        String varQualifer;
        
        /**
         * 
         */
        public TaxonInfo()
        {
            super();
            // TODO Auto-generated constructor stub
        }

        /**
         * @param name
         * @param rankId
         * @param parentId
         * @param author
         */
        public TaxonInfo(String name, Integer rankId, Integer parentId, String author)
        {
            super();
            this.name = name;
            this.rankId = rankId;
            this.parentId = parentId;
            this.author = author;
        }
        
        public void set(String name, Integer rankId, Integer parentId, String author)
        {
            this.name = name;
            this.rankId = rankId;
            this.parentId = parentId;
            this.author = author;
        }
        
        public void clear()
        {
            this.name = null;
            this.rankId = null;
            this.parentId = null;
            this.author = null;
            
            // Qualifiers
            spQualifer = null;
            subSpQualifer = null;
            varQualifer = null;
        }
    }

}
