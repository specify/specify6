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
package edu.ku.brc.specify.plugins.sgr;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.plugins.sgr.RawData.DataIndexType;
import edu.ku.brc.ui.DateParser;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.WorkBenchPluginIFace;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2010
 *
 */
public class SGRPluginImpl implements WorkBenchPluginIFace
{
    private static final ArrayList<DateParser> sdFormats  = new ArrayList<DateParser>();
    private static final Calendar            calender     = Calendar.getInstance();
    protected static String[]                dateArray    = new String[3];

    protected static final String GENUS      = "genus1";
    protected static final String SPECIES    = "species1";
    protected static final String SUBSPECIES = "subspecies1";
    protected static final String COLLNUM    = "fieldnumber";
    protected static final String FIELDNUM   = "stationfieldnumber";
    protected static final String COUNTRY    = "country";
    protected static final String STARTDATE  = "startdate";
    protected static final String LOCALITY   = "localityname";
    protected static final String LATITUDE   = "latitude1";
    protected static final String LONGITUDE  = "longitude1";
    protected static final String STATE      = "state";
    protected static final String COUNTY     = "county";
    protected static final String FIRSTNAME = "firstname1";
    protected static final String LASTNAME  = "lastname1";
    
    protected static final String COLFIRSTNAME = "collectorfirstname1";
    protected static final String COLLASTNAME  = "collectorlastname1";
    protected static final String COLMIDNAME   = "collectormiddlename1";
    
    protected static String[] COLNAMES   = {GENUS, SPECIES, SUBSPECIES, COLLNUM, FIELDNUM, COUNTRY, STARTDATE, LOCALITY, LATITUDE, LONGITUDE, STATE, COUNTY, LASTNAME, FIRSTNAME, };

    protected Integer                       collNumIndex;
    protected Integer                       fieldNumIndex;
    protected Integer                       startDateIndex;
    
    protected SubPaneIFace                  subPane;
    protected SpreadSheet                   spreadSheet;
    protected Workbench                     workbench;
    protected WorkbenchTemplate             template;
    
    protected HashMap<Integer, DBFieldInfo> indexToFieldInfo = new HashMap<Integer, DBFieldInfo>();
    protected HashMap<DBFieldInfo, Integer> fieldInfoToIndex = new HashMap<DBFieldInfo, Integer>();
    protected HashMap<String, Integer>      nameToIndex      = new HashMap<String, Integer>();
    
    static {
        String[] defaultFormatters = {"yyyy/MM/dd",  "yyyy/M/dd",  "yyyy/MM/d",  "yyyy/M/d", 
                "MM/dd/yyyy",  "M/dd/yyyy",  "MM/d/yyyy",  "M/d/yyyy",
                "yyyy-MM-dd",  "yyyy-M-dd",  "yyyy-MM-d",  "yyyy-M-d", 
                "MM-dd-yyyy",  "M-dd-yyyy",  "MM-d-yyyy",  "M-d-yyyy",
                "yyyy.MM.dd",  "yyyy.M.dd",  "yyyy.MM.d",  "yyyy.M.d", 
                "MM.dd.yyyy",  "M.dd.yyyy",  "MM.d.yyyy",  "M.d.yyyy"};
          
        for (String defaultFormat : defaultFormatters)
        {
            sdFormats.add(new DateParser(defaultFormat));
        }
    }
       
    /**
     * 
     */
    public SGRPluginImpl()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#process(java.util.List)
     */
    @Override
    public boolean process(final List<WorkbenchRow> rows)
    {
        final String PROG = "PROG";
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        
        SwingWorker<Integer, Integer> dataGetter = new SwingWorker<Integer, Integer>()
        {
            protected Vector<DataResultsRow> items  = new Vector<DataResultsRow>();
            protected String collectorNumber = null;
            protected String fieldNumber     = null;
            protected String startDate       = null;
            protected String genus           = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                Integer genusInx = nameToIndex.get(GENUS);
                
                int rowCnt = 0;
                for (WorkbenchRow row : rows)
                {
                    try
                    {
                        int colNumInx   = collNumIndex != null ? collNumIndex : fieldNumIndex;
                        collectorNumber = row.getData(collNumIndex);
                        fieldNumber     = row.getData(colNumInx);
                        startDate       = row.getData(startDateIndex);
                        genus           = row.getData(genusInx);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DbLoginCallbackHandler.class, ex);
                        //log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
                    }
                    
                    fieldNumber = StringUtils.isNotEmpty(collectorNumber) && StringUtils.isEmpty(fieldNumber) ? collectorNumber : fieldNumber;
                    
                    if (StringUtils.isNotEmpty(fieldNumber) &&
                        StringUtils.isNotEmpty(startDate) &&
                        StringUtils.isNotEmpty(genus))
                    {
                        String[] ymd = parseForDate(startDate); // returns Year, Month as strings
                        if (ymd != null)
                        {
                            String  year  = ymd[0];
                            String  month = ymd[1];
                            
                            if (year != null)
                            {
                                DataResultsRow rowItem = null;
                                try
                                {
                                    GroupingColObjData gcd = GroupHashDAO.getInstance().getGroupingData(collectorNumber, genus, year, month);
                                    if (gcd != null)
                                    {
                                        //System.out.println(gcd.toString());
                                        
                                        RawData rawData = GroupHashDAO.getInstance().getRawDataObj(); // return an empty recycled object
                                        loadRawData(row, rawData);
                                        
                                        rowItem = new DataResultsRow(gcd, rawData);
                                        items.add(rowItem);
                                        
                                        //System.out.println((items.size()-1)+ rawData.toString());
                                        
                                    }
                                    
                                    gcd = GroupHashDAO.getInstance().getSNIBData(collectorNumber, genus, year, month);
                                    if (gcd != null)
                                    {
                                        //System.out.println(gcd.toString());
                                        
                                        RawData rawData = GroupHashDAO.getInstance().getRawDataObj(); // return an empty recycled object
                                        loadSNIBData(row, rawData);
                                        
                                        if (rowItem == null)
                                        {
                                            rowItem = new DataResultsRow(null, gcd, rawData);
                                            items.add(rowItem);
                                            
                                        } else
                                        {
                                            rowItem.setGrpSNIBData(gcd);
                                        }
                                        //System.out.println((items.size()-1)+ rawData.toString());
                                    }

                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    
                    rowCnt++;
                    firePropertyChange(PROG, 0, rowCnt);
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (items.size() > 0)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SGRResultsChooser chooser = new SGRResultsChooser((Frame)UIRegistry.getTopWindow(), items);
                            chooser.createUI();
                            chooser.setVisible(true); // Centers 
                            
                            if (!chooser.isCancelled())
                            {
                                transferData(chooser.getResultsChosen());
                            }
                        }
                    });
                    
                } else if (rows.size() > 1)
                {
                    UIRegistry.showLocalizedError("SGR_NO_RESULTS_MLT");
                } else
                {
                    UIRegistry.showLocalizedError("SGR_NO_RESULTS", fieldNumber, startDate, genus);
                }
                UIRegistry.popResourceBundle();
            }
        };
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("SGR_GET_GRP_DATA"), 24);
        glassPane.setProgress(0);
        
        dataGetter.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (PROG.equals(evt.getPropertyName())) 
                        {
                            Integer value   = (Integer)evt.getNewValue();
                            int     percent = (int)((value / (double)rows.size()) * 100.0);
                            String  msg     = String.format("%d%c", percent, '%');
                            glassPane.setText(msg);
                            glassPane.setProgress(percent);
                        }
                    }
                });
        dataGetter.execute();
        
        return true;
    }
    
    /**
     * @param dateStr
     * @return
     */
    public static String[] parseForDate(final String dateStr)
    {
        dateArray[0] = null;
        dateArray[1] = null;
        dateArray[2] = null;
        
        for (DateParser dateParser : sdFormats)
        {
            Date date = dateParser.parseDate(dateStr);
            if (date != null)
            {
                calender.setTime(date);
                
                Integer yearInt = calender.get(Calendar.YEAR);
                Integer monInt  = calender.get(Calendar.MONTH) + 1;
                Integer dayInt  = calender.get(Calendar.DAY_OF_MONTH);
                
                dateArray[0] = yearInt.toString();
                dateArray[1] = monInt.toString();
                dateArray[2] = dayInt.toString();
                
                return dateArray;
            }
        }
        return null;
    }
    
    /**
     * @param wbRow
     * @param rowData
     */
    protected void loadRawData(final WorkbenchRow wbRow, 
                               final RawData rawData)
    {
        String[] extraCollNameCols = {COLLASTNAME, LASTNAME, COLFIRSTNAME, FIRSTNAME};
        
        String lastName  = null;
        String firstName = null;
        
        int colInx = 0;
        for (DataIndexType dataInxTyp : DataIndexType.values())
        {
            String  columnName = RawData.getColumnName(dataInxTyp);
            Integer dataIndex  = nameToIndex.get(columnName);
            
            //System.out.println("dataIndex: "+dataIndex+"  dataInxTyp: "+dataInxTyp+"  columnName["+columnName+"]");
           
            if (DataIndexType.eCollector_num == dataInxTyp)
            {
                int collNumInx = collNumIndex != null ? collNumIndex : fieldNumIndex;
                
                Object val = wbRow.getData(collNumInx);
                rawData.setData(dataInxTyp, val);
                
            } else if (DataIndexType.eCollector_name == dataInxTyp)
            {
                if (dataIndex != null)
                {
                    Object val = wbRow.getData(dataIndex);
                    rawData.setData(dataInxTyp, val);
                } else
                {
                    int i = 0;
                    for (String key : extraCollNameCols)
                    {
                        Integer inx = nameToIndex.get(key);
                        if (inx != null)
                        {
                            if (i < 2)
                            {
                                lastName = (String)wbRow.getData(inx);
                                if (lastName == null) lastName = "";
                                break;
                            } else
                            {
                                firstName = (String)wbRow.getData(inx);
                                if (firstName == null) firstName = ""; 
                                break;
                            }
                        }
                        i++;
                    }
                }
            } else if (columnName.equals(STARTDATE))
            {
                Object val = wbRow.getData(dataIndex);
                if (StringUtils.isNotEmpty((String)val))
                {
                    
                    String[] ymd = parseForDate((String)val);
                    if (ymd != null)
                    {
                        rawData.setData(DataIndexType.eYear,  ymd[0]);
                        rawData.setData(DataIndexType.eMonth, ymd[1]);
                        rawData.setData(DataIndexType.eDay,   ymd[2]);
                        StringBuilder sb = new StringBuilder();
                        if (StringUtils.isNotEmpty(ymd[0]))
                        {
                            sb.append(ymd[0]);
                            if (StringUtils.isNotEmpty(ymd[1]) && StringUtils.isNumeric(ymd[1]))
                            {
                                sb.append(String.format("-%02d", Integer.parseInt(ymd[1])));
                                if (StringUtils.isNotEmpty(ymd[2]) && StringUtils.isNumeric(ymd[2]))
                                {
                                    sb.append(String.format("-%02d", Integer.parseInt(ymd[2])));
                                }
                            }
                        }
                        rawData.setData(DataIndexType.eStartDate,  sb.toString());
                    } else
                    {
                        System.err.println("Couldn't parse date["+val+"]");
                        rawData.setData(DataIndexType.eStartDate,  (String)val);
                    }

                }
            } else  if (dataIndex != null)
            {
                Object val = wbRow.getData(dataIndex);
                rawData.setData(dataInxTyp, val);
                
            } else
            {
                //System.err.println("No index for Standard Column for ["+columnName+"]");
            }
            colInx++;
        }
        
        String fullName  = null;
        if (lastName != null || firstName != null)
        {
            if (StringUtils.isNotEmpty(lastName) && StringUtils.isNotEmpty(firstName))
            {
                fullName = lastName + ", "+firstName;
                
            } else if (StringUtils.isNotEmpty(lastName))
            {
                fullName = lastName;
            } else
            {
                fullName = firstName;
            }
            rawData.setData(DataIndexType.eCollector_name, fullName);
        }
        
        rawData.setImgIcon(getImage(wbRow));
        
        //System.out.println(rawData.toString());
    }

    /**
     * @param wbRow
     * @param rowData
     */
    protected void loadSNIBData(final WorkbenchRow wbRow, 
                                final RawData rawData)
    {
        
        String[] extraCollNameCols = {COLLASTNAME, LASTNAME, COLFIRSTNAME, FIRSTNAME};
        
        String lastName  = null;
        String firstName = null;
        
        int colInx = 0;
        for (DataIndexType dataInxTyp : DataIndexType.values())
        {
            String  columnName = RawData.getColumnName(dataInxTyp);
            Integer dataIndex  = nameToIndex.get(columnName);
            
            //System.out.println("dataIndex: "+dataIndex+"  dataInxTyp: "+dataInxTyp+"  columnName["+columnName+"]");
           
            if (DataIndexType.eCollector_num == dataInxTyp)
            {
                int collNumInx = collNumIndex != null ? collNumIndex : fieldNumIndex;
                
                Object val = wbRow.getData(collNumInx);
                rawData.setData(dataInxTyp, val);
                
            } else if (DataIndexType.eCollector_name == dataInxTyp)
            {
                if (dataIndex != null)
                {
                    Object val = wbRow.getData(dataIndex);
                    rawData.setData(dataInxTyp, val);
                } else
                {
                    int i = 0;
                    for (String key : extraCollNameCols)
                    {
                        Integer inx = nameToIndex.get(key);
                        if (inx != null)
                        {
                            if (i < 2)
                            {
                                lastName = (String)wbRow.getData(inx);
                                if (lastName == null) lastName = "";
                                break;
                            } else
                            {
                                firstName = (String)wbRow.getData(inx);
                                if (firstName == null) firstName = ""; 
                                break;
                            }
                        }
                        i++;
                    }
                }
            } else if (columnName.equals(STARTDATE))
            {
                Object val = wbRow.getData(dataIndex);
                if (StringUtils.isNotEmpty((String)val))
                {
                    
                    String[] ymd = parseForDate((String)val);
                    if (ymd != null)
                    {
                        rawData.setData(DataIndexType.eYear,  ymd[0]);
                        rawData.setData(DataIndexType.eMonth, ymd[1]);
                        rawData.setData(DataIndexType.eDay,   ymd[2]);
                        StringBuilder sb = new StringBuilder();
                        if (StringUtils.isNotEmpty(ymd[0]))
                        {
                            sb.append(ymd[0]);
                            if (StringUtils.isNotEmpty(ymd[1]) && StringUtils.isNumeric(ymd[1]))
                            {
                                sb.append(String.format("-%02d", Integer.parseInt(ymd[1])));
                                if (StringUtils.isNotEmpty(ymd[2]) && StringUtils.isNumeric(ymd[2]))
                                {
                                    sb.append(String.format("-%02d", Integer.parseInt(ymd[2])));
                                }
                            }
                        }
                        rawData.setData(DataIndexType.eStartDate,  sb.toString());
                    } else
                    {
                        System.err.println("Couldn't parse date["+val+"]");
                        rawData.setData(DataIndexType.eStartDate,  (String)val);
                    }

                }
            } else  if (dataIndex != null)
            {
                Object val = wbRow.getData(dataIndex);
                rawData.setData(dataInxTyp, val);
                
            } else
            {
                //System.err.println("No index for Standard Column for ["+columnName+"]");
            }
            colInx++;
        }
        
        String fullName  = null;
        if (lastName != null || firstName != null)
        {
            if (StringUtils.isNotEmpty(lastName) && StringUtils.isNotEmpty(firstName))
            {
                fullName = lastName + ", "+firstName;
                
            } else if (StringUtils.isNotEmpty(lastName))
            {
                fullName = lastName;
            } else
            {
                fullName = firstName;
            }
            rawData.setData(DataIndexType.eCollector_name, fullName);
        }
        
        rawData.setImgIcon(getImage(wbRow));
        
        //System.out.println(rawData.toString());
    }
    

    /**
     * @param wbRow
     * @return
     */
    protected ImageIcon getImage(final WorkbenchRow wbRow)
    {
        if (wbRow != null)
        {
            if (StringUtils.isNotEmpty(wbRow.getCardImageFullPath()))
            {
                if (wbRow.getCardImage() == null)
                {
                    File imgFile = new File(wbRow.getCardImageFullPath());
                    return imgFile.exists() ? new ImageIcon(GraphicsUtils.readImage(wbRow.getCardImageFullPath())) : null;
                }
                return wbRow.getCardImage();
                
            } else if (wbRow.getWorkbenchRowImages().size() > 0)
            {
                for (WorkbenchRowImage wbi : wbRow.getWorkbenchRowImages())
                {
                    ImageIcon imgIcon = wbi.getFullSizeImage(); 
                    if (imgIcon != null)
                    {
                        return imgIcon;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param rawResList
     */
    protected void transferData(final List<RawData> rawResList)
    {
        if (rawResList.size() > 0)
        {
            int[] selRowsIndexes = spreadSheet.getSelectedRows();
            int rowCnt = 0;
            for (RawData rawData : rawResList)
            {
                if (rawData != null)
                {
                    int rowInx = selRowsIndexes[rowCnt];
                    for (String nm : COLNAMES)
                    {
                        Integer index = nameToIndex.get(nm);
                        if (index == null)
                        {
                            continue;
                        }
                    
                        Object val = rawData.getData(nm);
                        if (val != null)
                        {
                            spreadSheet.setValueAt(val, rowInx, index);
                        }
                    }
                }
                rowCnt++;
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSubPanel(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setSubPanel(SubPaneIFace parent)
    {
        this.subPane = parent;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSpreadSheet(edu.ku.brc.ui.tmanfe.SpreadSheet)
     */
    @Override
    public void setSpreadSheet(SpreadSheet ss)
    {
        this.spreadSheet = ss;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setWorkbench(edu.ku.brc.specify.datamodel.Workbench)
     */
    @Override
    public void setWorkbench(final Workbench workbench)
    {
        this.workbench = workbench;
        if (workbench != null)
        {
            this.template = workbench.getWorkbenchTemplate();
            
            for (WorkbenchTemplateMappingItem wbtmi : this.template.getWorkbenchTemplateMappingItems())
            {
                Short       viewOrderIndex = wbtmi.getViewOrder();
                DBFieldInfo fieldInfo      = wbtmi.getFieldInfo();
                
                String  stdColName = fieldInfo.getName().toLowerCase();
                
                boolean override = false;
                if (stdColName.equalsIgnoreCase(COLLASTNAME) ||
                    stdColName.equalsIgnoreCase(COLFIRSTNAME) ||
                    stdColName.equalsIgnoreCase(LASTNAME) ||
                    stdColName.equalsIgnoreCase(FIRSTNAME) ||
                    stdColName.equalsIgnoreCase(STARTDATE))
                {
                    override = true;
                }
                
                Integer index = RawData.getIndex(stdColName);
                if (index != null || override)
                {
                    indexToFieldInfo.put(viewOrderIndex.intValue(), fieldInfo);
                    fieldInfoToIndex.put(fieldInfo, viewOrderIndex.intValue());
                    nameToIndex.put(fieldInfo.getName().toLowerCase(), viewOrderIndex.intValue());
                    
                    //System.out.println("Name Mapping: "+fieldInfo.getName().toLowerCase()+" => "+viewOrderIndex.intValue());
                    
                } else
                {
                    System.err.println("Name Mapping Error:"+fieldInfo.getName()+" was not a standard column name");
                }
            }
            
            collNumIndex   = nameToIndex.get(COLLNUM);
            fieldNumIndex  = nameToIndex.get(FIELDNUM);
            startDateIndex = nameToIndex.get(STARTDATE);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#getMissingFieldsForPlugin()
     */
    @Override
    public List<String> getMissingFieldsForPlugin()
    {
        ArrayList<String> list = new ArrayList<String>();
        if (collNumIndex == null && fieldNumIndex == null)
        {
            return list;
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#shutdown()
     */
    @Override
    public void shutdown()
    {
        if (GroupHashDAO.getInstance() != null)
        {
            GroupHashDAO.getInstance().cleanUp();
        }
    }

}
