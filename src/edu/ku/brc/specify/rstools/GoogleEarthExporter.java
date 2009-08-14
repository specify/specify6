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
package edu.ku.brc.specify.rstools;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.writeTimedSimpleGlassPaneMsg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.RecordSetLoader;
import edu.ku.brc.specify.tasks.services.CollectingEventLocalityKMLGenerator;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.GenericKMLGenerator;

/**
 * An implementation of {@link RecordSetToolsIFace} that produces KML files
 * and opens Google Earth as an external viewer.
 * 
 * @author jstewart
 * @code_status Complete
 */
public class GoogleEarthExporter implements RecordSetToolsIFace
{
    /** Logger for all log messages emitted from this class. */
    private static final Logger log = Logger.getLogger(GoogleEarthExporter.class);
    
    public GoogleEarthExporter()
    {
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.rstools.RecordSetToolsIFace#processRecordSet(edu.ku.brc.dbsupport.RecordSetIFace, java.util.Properties)
	 */
	public void processRecordSet(final RecordSetIFace recordSet, final Properties reqParams)
    {
	    String description = JOptionPane.showInputDialog(getTopWindow(), getResourceString("GE_ENTER_DESC"));
        
        log.info("Exporting RecordSet");
        int dataTableId = recordSet.getDbTableId();

        if (dataTableId == CollectingEvent.getClassTableId())
        {
            exportDataObjects(description, 
                              RecordSetLoader.loadRecordSet(recordSet), 
                              true, 
                              getPlacemarkIcon());
            
        } else if (dataTableId == CollectionObject.getClassTableId())
        {
            exportCollectionObjectRecordSet(description, recordSet);
            
        } else if (dataTableId == Locality.getClassTableId())
        {
            exportLocalityRecordSet(description, recordSet);
            
        } else
        {
            throw new RuntimeException("Only Collection Objects, Colelcting Events and Localities are supported for GoogleEarth export."); // I18N
        }
	}
	
	/**
	 * @return
	 */
	private ImageIcon getPlacemarkIcon()
	{
        // get an icon URL that is specific to the current context
        
        String discipline = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        for ( Pair<String, ImageIcon> pair : IconManager.getListByType("disciplines", IconManager.IconSize.Std32))
        {
            if (pair.first.equals(discipline))
            {
                return pair.second;
            }
        }
        return null;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#exportList(java.util.List, java.util.Properties)
     */
    @SuppressWarnings("unchecked")
    public void processDataList(List<?> data, Properties reqParams) throws Exception
    {
        log.info("Exporting data list");
        if (data == null || data.size() == 0)
        {
            log.warn("Empty or null data list given to GoogleEarthExporter");
            return;
        }
        
        if (data.get(0) instanceof LatLonPlacemarkIFace)
        {
            try
            {
                File tmpFile = File.createTempFile("sp6export", ".kmz");
                tmpFile.deleteOnExit();
                log.info("Writing KML output to " + tmpFile.getAbsolutePath());
                
                ImageIcon imageIcon = getIconFromPrefs();
                List<LatLonPlacemarkIFace> mappedPlacemarks = exportPlacemarkList(reqParams.getProperty("description"), 
                                                                     (List<LatLonPlacemarkIFace>)data, imageIcon, tmpFile);
                if (mappedPlacemarks.size() != data.size())
                {
                    getStatusBar().setErrorMessage(String.format(getResourceString("NOT_ALL_MAPPED"), new Object[] {(data.size() - mappedPlacemarks.size()), data.size()}));
                }
                
                try
                {
                	openExternalViewer(tmpFile);
                }
                catch (Exception e)
                {
                	log.warn("Failed to open external viewer (e.g. Google Earth) for KML file", e);
                    String errorMessage = getResourceString("GOOGLE_EARTH_ERROR");
                    getStatusBar().setErrorMessage(errorMessage,e);
                }
            }
            catch (Exception e)
            {
                log.error("Exception caught while creating KML output or opening Google Earth", e);
                String errorMessage = getResourceString("KML_EXPORT_ERROR");
                getStatusBar().setErrorMessage(errorMessage,e);
            }
        }
    }
    
    /**
     * Converts a RecordSet of CollectionObjects to a list of CollecitnEvents.
     * @param description
     * @param recordSet the RecordSet
     */
    protected void exportCollectionObjectRecordSet(final String description,
                                                   final RecordSetIFace recordSet)
    {
        List<Object> list    = new Vector<Object>();
        List<Object> records = RecordSetLoader.loadCollectionObjectsRecordSet(recordSet);
        for (Object obj : records)
        {
            if (obj instanceof CollectionObject) // it HAS to be
            {
                CollectionObject colObj = (CollectionObject)obj;
                if (colObj.getCollectingEvent() != null &&
                    colObj.getCollectingEvent().getLocality() != null)
                {
                    Locality loc = colObj.getCollectingEvent().getLocality();
                    if (loc.getLatitude1() != null && loc.getLongitude1() != null)
                    {
                        list.add(obj);
                    }
                }
            }
        }
        
        if (list.size() > 0)
        {
            exportDataObjects(description, list, true, getIconFromPrefs());
            
        } else
        {
            writeTimedSimpleGlassPaneMsg(getResourceString("GE_NO_POINTS"), Color.RED);
        }
    }
    
    
    /**
     * Converts a RecordSet of CollectionObjects to a list of CollecitnEvents.
     * @param description
     * @param recordSet the RecordSet
     */
    protected void exportLocalityRecordSet(final String description,
                                           final RecordSetIFace recordSet)
    {
        List<Object> list    = new Vector<Object>();
        List<Object> records = RecordSetLoader.loadRecordSet(recordSet);
        for (Object o : records)
        {
            list.add((Locality)o);
        }
        
        if (list.size() > 0)
        {
            exportDataObjects(description, list, true, getIconFromPrefs());
        } else
        {
            writeTimedSimpleGlassPaneMsg(getResourceString("GE_NO_POINTS"), Color.RED);
        }
    }
    
    /**
     * @return
     */
    @SuppressWarnings("deprecation")
    protected ImageIcon getIconFromPrefs()
    {
        String iconUrl = AppPreferences.getRemote().getProperties().getProperty("google.earth.icon", null);
        if (StringUtils.isNotEmpty(iconUrl))
        {
            try
            {
                return new ImageIcon(new File(iconUrl).toURL());
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GoogleEarthExporter.class, ex);
            }
        }
        return null;
    }
    
    /**
     * Creates a KML file containing the data found in a {@link RecordSet} of {@link CollectingEvent}s and
     * opens the KML file using the default system viewer (most likely Google Earth).
     * 
     * @param description KMZ description
     * @param dataObjList list of {@link CollectingEvent}s
     * @param doKMZ whether to export as KMZ or KML
     * @param imgIconArg the icon to use for placemarks
     */
    protected void exportDataObjects(final String       description,
                                     final List<Object> dataObjList, 
                                     final boolean      doKMZ,
                                     final ImageIcon    imgIconArg) 
    {
        try
        {
            if (dataObjList != null && dataObjList.size() > 0)
            {
                CollectingEventLocalityKMLGenerator kmlGen = new CollectingEventLocalityKMLGenerator();
                kmlGen.setDescription(description);
                
                ImageIcon defaultIcon = IconManager.getImage("DefaultPlacemark");

                // If an icon isn't passed in then get the default icon for the disciplineType
                ImageIcon imageIcon = defaultIcon;
                if (imgIconArg == null)
                {
                    imageIcon = getPlacemarkIcon();
                } else
                {
                    imageIcon = imgIconArg;
                }
                
                File defaultIconFile = null;
                if (imageIcon != null)
                {
                    // set it to a standard name that we will put into the KMZ file
                    kmlGen.setPlacemarkIconURL("files/specify32.png");
                    
                    // Write the image out to the temp directory
                    defaultIconFile = File.createTempFile("sp6-export-icon-", ".png");
                    if (!writeImageIconToFile(imageIcon, defaultIconFile))
                    {
                        // if it fails (could be a bad user defined icon)
                        // check to see if it is the default icon (which should always be good)
                        // and try to write that
                        if (imageIcon != defaultIcon)
                        {
                            if (!writeImageIconToFile(defaultIcon, defaultIconFile))
                            {
                                kmlGen.setPlacemarkIconURL(null); // setting to null means a GE push pin icon
                            }
                        } else
                        {
                            kmlGen.setPlacemarkIconURL(null); // setting to null means a GE push pin icon
                        }
                    }
                        
                } else
                {
                    kmlGen.setPlacemarkIconURL(null);// setting to null means a GE push pin icon
                }
                
                Color  geBGColor = AppPreferences.getRemote().getColor("google.earth.bgcolor", new Color(0, 102, 179));
                String bgColor   = UIHelper.getBGRHexFromColor(geBGColor);
                
                Color  geFGColor = AppPreferences.getRemote().getColor("google.earth.fgcolor", new Color(255, 255, 255));
                String fgColor   = UIHelper.getBGRHexFromColor(geFGColor);

                kmlGen.setBalloonStyleBgColor("AA"+bgColor);
                kmlGen.setBalloonStyleTextColor("FF" + fgColor);
                kmlGen.setBalloonStyleText(getBalloonText(fgColor));
                kmlGen.setTextColor(fgColor);
                
                for (Object obj : dataObjList)
                {
                    if (obj instanceof FormDataObjIFace)
                    {
                        kmlGen.addDataObj((FormDataObjIFace)obj, null);
                    }
                }
                
                File outputFile = File.createTempFile("sp6export", doKMZ ? ".kmz" : ".kml");
                kmlGen.outputToFile(outputFile.getAbsolutePath());
                
                if (doKMZ)
                {
                    // now we have the KML in outputFile
                    // we need to create a KMZ (zip file containing doc.kml and other files)
                    
                    createKMZ(outputFile, defaultIconFile);
                }
                
                if (imageIcon != null && defaultIconFile != null)
                {
                    defaultIconFile.delete();
                }
                
                openExternalViewer(outputFile);
                
            } else
            {
                writeTimedSimpleGlassPaneMsg(getResourceString("GE_NO_POINTS"), Color.RED);
            }

        } catch (Exception ex)
        {
            writeTimedSimpleGlassPaneMsg(getResourceString("GE_EXPORT_PROB"), Color.RED);
            ex.printStackTrace();
        }
    }
    
    /**
     * @param textColor
     * @return
     */
    protected String getBalloonText(final String textColorArg)
    {
        String textColor = UIHelper.fixColorForHTML(textColorArg);
        
        return "<b><font color=\""+textColor+"\" size=\"+3\"><center>$[name]</center></font></b>"
                + "<br/><hr><br/>"
                + "$[description]<br/><br/>"
                + "<center><a href=\"http://www.specifysoftware.org/\"><font color=\""+textColor+"\">http://www.specifysoftware.org</font></a></center>";
    }
    
    /**
     * Generates a KMZ file representing the given list of placemarks.
     * @param description description for the KMZ file
     * @param placemarks a List of placemarks to be mapped
     * @param imgIconArg a user defined icon
     * @param outputFile the file to put the KML into
     * @return a List of placemarks that were mapped (does not include any passed in placemarks that couldn't be mapped for some reason)
     * @throws IOException an error occurred while writing the KML to the file
     */
    protected List<LatLonPlacemarkIFace> exportPlacemarkList(final String     description,
                                                                  final List<LatLonPlacemarkIFace> placemarks, 
                                                                  final ImageIcon  imgIconArg, 
                                                                  final File       outputFile) throws IOException
    {
        ImageIcon defaultIcon = IconManager.getImage("DefaultPlacemark");

        List<LatLonPlacemarkIFace> mappedPlacemarks = new Vector<LatLonPlacemarkIFace>();
        
        GenericKMLGenerator kmlGenerator = new GenericKMLGenerator();
        if (description != null)
        {
            kmlGenerator.setDescription(description);
            
        } else if (placemarks.size() == 1)
        {
            LatLonPlacemarkIFace pm = placemarks.get(0);
            kmlGenerator.setDescription(pm.getTitle());
            if (pm.getImageIcon() != null)
            {
                defaultIcon = pm.getImageIcon();
            }
        }
        
        Color  geBGColor = AppPreferences.getRemote().getColor("google.earth.bgcolor", new Color(0, 102, 179), true);
        String bgColor   = UIHelper.getBGRHexFromColor(geBGColor);
        
        Color  geFGColor = AppPreferences.getRemote().getColor("google.earth.fgcolor", new Color(255, 255, 255), true);
        String fgColor   = UIHelper.getBGRHexFromColor(geFGColor);
        
        // setup all of the general style stuff
        kmlGenerator.setBalloonStyleBgColor("AA"+bgColor);
        kmlGenerator.setBalloonStyleTextColor("FF" + fgColor);
        kmlGenerator.setBalloonStyleText(getBalloonText(fgColor));

        // If an icon isn't passed in then get the default icon for the disciplineType
        ImageIcon imageIcon = defaultIcon;
        if (imgIconArg == null)
        {
            imageIcon = getPlacemarkIcon();
        } else
        {
            imageIcon = imgIconArg;
        }
        
        File defaultIconFile = null;
        if (imageIcon != null)
        {
            // set it to a standard name that we will put into the KMZ file
            kmlGenerator.setPlacemarkIconURL("files/specify32.png");
            
            // Write the image out to the temp directory
            defaultIconFile = File.createTempFile("sp6-export-icon-", ".png");
            if (!writeImageIconToFile(imageIcon, defaultIconFile))
            {
                // if it fails (could be a bad user defined icon)
                // check to see if it is the default icon (which should always be good)
                // and try to write that
                if (imageIcon != defaultIcon)
                {
                    if (!writeImageIconToFile(defaultIcon, defaultIconFile))
                    {
                        kmlGenerator.setPlacemarkIconURL(null); // setting to null means a GE push pin icon
                    }
                } else
                {
                    kmlGenerator.setPlacemarkIconURL(null); // setting to null means a GE push pin icon
                }
            }
                
        } else
        {
            kmlGenerator.setPlacemarkIconURL(null);// setting to null means a GE push pin icon
        }
        
        // add all of the placemarks to the KML generator
        for (LatLonPlacemarkIFace pm : placemarks)
        {
            String name = pm.getTitle();
            name = name == null ? "" : name;
            Pair<Double,Double> geoRef = pm.getLatLon();
            if (geoRef != null)
            {
                mappedPlacemarks.add(pm);
                String htmlDesc = pm.getHtmlContent(kmlGenerator.getBalloonStyleTextColor());
                kmlGenerator.addPlacemark(geoRef, name, htmlDesc);
            }
            else
            {
                log.warn("Placemark returned a null geocoordinate");
            }
        }

        // generate the KML
        kmlGenerator.generateKML(outputFile);
        
        // now we have the KML in outputFile
        // we need to create a KMZ (zip file containing doc.kml and other files)
        
        createKMZ(outputFile, defaultIconFile);
        
        if (defaultIconFile != null)
        {
            defaultIconFile.delete();
        }
        
        return mappedPlacemarks;
    }
    
    /**
     * Takes an ImageIcon (in memory) and writes it out to a file.
     * @param icon the image icon
     * @param output the destination file
     * @return true on success
     * @throws IOException
     */
    protected boolean writeImageIconToFile(final ImageIcon icon, final File output) throws IOException
    {
        if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 && output != null)
        {
            try
            {
                BufferedImage bimage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                if (bimage != null)
                {
                    Graphics g = bimage.createGraphics();
                    if (g != null)
                    {
                        g.drawImage(icon.getImage(), 0, 0, null);
                        g.dispose();
                        ImageIO.write(bimage, "PNG", output);
                        return true;
                    }
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GoogleEarthExporter.class, ex);
                // no need to throw an exception or display it
            }
        }
        return false;
    }
    
    /**
     * @param outputFile
     * @param defaultIconFile
     */
    protected void createKMZ(final File outputFile,
                             final File defaultIconFile) throws IOException
    {
        // now we have the KML in outputFile
        // we need to create a KMZ (zip file containing doc.kml and other files)
        
        // create a buffer for reading the files
        byte[] buf = new byte[1024];
        int len;
        
        // create the KMZ file
        File outputKMZ = File.createTempFile("sp6-export-", ".kmz");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputKMZ));
        
        // add the doc.kml file to the ZIP
        FileInputStream in = new FileInputStream(outputFile);
        // add ZIP entry to output stream
        out.putNextEntry(new ZipEntry("doc.kml"));
        // copy the bytes
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        // complete the entry
        out.closeEntry();
        in.close();
        
        // add a "files" directory to the KMZ file
        ZipEntry filesDir = new ZipEntry("files/");
        out.putNextEntry(filesDir);
        out.closeEntry();
        
        if (defaultIconFile != null)
        {
            File iconTmpFile = defaultIconFile;
            if (false)
            {
                // Shrink File
                ImageIcon     icon   = new ImageIcon(defaultIconFile.getAbsolutePath());
                BufferedImage bimage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics      g      = bimage.createGraphics();
                g.drawImage(icon.getImage(), 0, 0, null);
                g.dispose();
                BufferedImage scaledBI = GraphicsUtils.getScaledInstance(bimage, 16, 16, true);
                iconTmpFile = File.createTempFile("sp6-export-icon-scaled", ".png");
                ImageIO.write(scaledBI, "PNG", iconTmpFile);
            }
            
            // add the specify32.png file (default icon file) to the ZIP (in the "files" directory)
            in = new FileInputStream(iconTmpFile);
            // add ZIP entry to output stream
            out.putNextEntry(new ZipEntry("files/specify32.png"));
            // copy the bytes
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            // complete the entry
            out.closeEntry();
            in.close();
        }

        // complete the ZIP file
        out.close();
        
        // now put the KMZ file where the KML output was
        FileUtils.copyFile(outputKMZ, outputFile);
        
        outputKMZ.delete();
    }
    
    /**
     * Opens the system default viewer for the given file.
     * 
     * @param f
     * @throws Exception
     */
    protected void openExternalViewer(File f) throws Exception
    {
        AttachmentUtils.openFile(f);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {CollectingEvent.class, LatLonPlacemarkIFace.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getName()
     */
    public String getName()
    {
        return getResourceString("GoogleEarth");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getIconName()
     */
    public String getIconName()
    {
        return "GoogleEarth";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getDescription()
     */
    public String getDescription()
    {
        return getResourceString("GoogleEarth_Description");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#isVisible()
     */
    public boolean isVisible()
    {
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetToolsIFace#getTableIds()
     */
    public int[] getTableIds()
    {
        return new int[] {1, 2, 10};
    }
}
