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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.plugins.sgr.RawData.DataIndexType;
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
    private static final String[] DATE_FORMATS = {"yyyy/MM/dd", "MM/dd/yyyy", "yyyy-MM-dd", "MM-dd-yyyy"};
    protected SimpleDateFormat[]  sdFormats    = new SimpleDateFormat[DATE_FORMATS.length];
    
    protected Calendar calender = Calendar.getInstance();

    protected static final String GENUS      = "genus1";
    protected static final String SPECIES    = "species1";
    protected static final String SUBSPECIES = "subspecies1";
    protected static final String COLLNUM    = "fieldNumber";
    protected static final String FIELDNUM   = "stationFieldNumber";
    protected static final String COUNTRY    = "country";
    protected static final String STARTDATE  = "startDate";
    protected static final String LOCALITY   = "localityName";
    protected static final String LATITUDE   = "latitude1";
    protected static final String LONGITUDE  = "longitude1";
    protected static final String STATE      = "state";
    protected static final String COUNTY     = "county";
    
    protected static String[]        COLNAMES   = {GENUS, SPECIES, SUBSPECIES, COLLNUM, FIELDNUM, COUNTRY, STARTDATE, LOCALITY, LATITUDE, LONGITUDE, STATE, COUNTY, };
    protected static DataIndexType[] DATAINXTYP = {DataIndexType.eGenus, DataIndexType.eSpecies, DataIndexType.eSubspecies, DataIndexType.eCollector_num, 
                                                  null, DataIndexType.eCountry, DataIndexType.eYear, DataIndexType.eLocality, DataIndexType.eLatitude, 
                                                  DataIndexType.eLongitude, DataIndexType.eState_province, DataIndexType.eCounty, };

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
       
    /**
     * 
     */
    public SGRPluginImpl()
    {
        super();
        
        for (int i=0;i<sdFormats.length;i++)
        {
            sdFormats[i] = new SimpleDateFormat(DATE_FORMATS[i]);
        }
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
            protected List<RawData>            wbRows = new ArrayList<RawData>();
            protected List<GroupingColObjData> items  = new ArrayList<GroupingColObjData>();
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
                int rowCnt = 0;
                for (WorkbenchRow row : rows)
                {
                    int colNumInx   = collNumIndex != null ? collNumIndex : fieldNumIndex;
                    collectorNumber = row.getData(nameToIndex.get(COLLNUM));
                    fieldNumber     = row.getData(colNumInx);
                    startDate       = row.getData(startDateIndex);
                    genus           = row.getData(nameToIndex.get(GENUS));
                    
                    fieldNumber = StringUtils.isNotEmpty(collectorNumber) && StringUtils.isEmpty(fieldNumber) ? collectorNumber : fieldNumber;
                    
                    if (StringUtils.isNotEmpty(fieldNumber) &&
                        StringUtils.isNotEmpty(startDate) &&
                        StringUtils.isNotEmpty(genus))
                    {
                        
                        String year  = null;
                        String month = null;
                        
                        for (SimpleDateFormat sdf : sdFormats)
                        {
                            try
                            {
                                Date date = sdf.parse(startDate);
                                calender.setTime(date);
                                
                                year  = ((Integer)calender.get(Calendar.YEAR)).toString();
                                month = ((Integer)calender.get(Calendar.MONTH)).toString();
                                
                            } catch (Exception ex) {}
                            
                            if (year != null) break;
                        }
                        
                        if (year != null)
                        {
                            try
                            {
                                GroupingColObjData gcd = GroupHashDAO.getInstance().getGroupingData(collectorNumber, genus, year, month);
                                if (gcd != null)
                                {
                                    System.out.println(gcd.toString());
                                    items.add(gcd);
                                    
                                    RawData rawData = GroupHashDAO.getInstance().getRawDataObj();
                                    loadRawData(row, rawData, rowCnt);
                                    wbRows.add(rawData);
                                }
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
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
                            SGRResultsChooser chooser = new SGRResultsChooser((Frame)UIRegistry.getTopWindow(), wbRows, items);
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
     * @param wbRow
     * @param rowData
     */
    protected void loadRawData(final WorkbenchRow wbRow, 
                               final RawData rawData,
                               final int     rowInx)
    {
        int colInx = 0;
        for (String nm : COLNAMES)
        {
            Integer index = nameToIndex.get(nm);
            if (index == null || DATAINXTYP[index] == null)
            {
                colInx++;
                continue;
            }
            
            if (DataIndexType.eCollector_num == DATAINXTYP[index])
            {
                int collNumInx = collNumIndex != null ? collNumIndex : fieldNumIndex;
                
                Object val = spreadSheet.getValueAt(rowInx, collNumInx);
                rawData.setData(DATAINXTYP[index], val);
                
            } else
            {
                Object val = spreadSheet.getValueAt(rowInx, colInx);
                rawData.setData(DATAINXTYP[index], val);
            }
            colInx++;
        }
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
                    System.out.println("--------------------\n"+rawData.toString());
                    
                    int colInx = 0;
                    for (String nm : COLNAMES)
                    {
                        Integer index = nameToIndex.get(nm);
                        if (index == null)
                        {
                            System.out.println("Error: "+nm+" -> "+index);
                            colInx++;
                            continue;
                        }
                        System.out.println("Good: "+nm+" -> "+index);
                        
                        if (DATAINXTYP[colInx] == null)
                        {
                            System.out.println("Skipping: "+colInx+" -> "+DATAINXTYP[colInx]);
                            colInx++;
                            continue;
                        }
                        
                        System.out.println(nm+" -> "+index+"  "+DATAINXTYP[colInx]);
                        
                        if (startDateIndex != null && DataIndexType.eYear == DATAINXTYP[colInx])
                        {
                            String yStr = (String)rawData.getData(DataIndexType.eYear);
                            String mStr = (String)rawData.getData(DataIndexType.eMonth);
                            String dStr = (String)rawData.getData(DataIndexType.eDay);
                            
                            yStr = StringUtils.isNotEmpty(yStr) ? yStr : "????";
                            mStr = StringUtils.isNotEmpty(mStr) ? mStr : "??";
                            dStr = StringUtils.isNotEmpty(dStr) ? dStr : "??";
                            
                            String val = String.format("%s-%s-%s", yStr, mStr, dStr);
                            if (!val.equals("????-??-??"))
                            {
                                spreadSheet.setValueAt(val, rowInx, index);
                            }
                            
                        } else if (DataIndexType.eCollector_num == DATAINXTYP[colInx])
                        {
                            int collNumInx = collNumIndex != null ? collNumIndex : fieldNumIndex;
                            //System.out.println(String.format("Dest Col: %d   Src Col Inx: %d   Typ: %s  collNumInx: %d", index, colInx,DATAINXTYP[colInx].toString(), collNumInx));
                            
                            Object valObj = rawData.getData(DATAINXTYP[colInx]);
                            if (valObj != null)
                            {
                                String valStr = valObj.toString();
                                if (StringUtils.isNotEmpty(valStr))
                                {
                                    spreadSheet.setValueAt(valStr, rowInx, collNumInx);
                                }
                            }
                        } else
                        {
                            String val = (String)rawData.getData(DATAINXTYP[colInx]);
                            //System.out.println(String.format("Dest Col: %d   Src Col Inx: %d   Typ: %s ", index, colInx, DATAINXTYP[colInx].toString()));
                            if (StringUtils.isNotEmpty(val))
                            {
                                spreadSheet.setValueAt(val, rowInx, index);
                            }
                        }
                        colInx++;
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
                
                indexToFieldInfo.put(viewOrderIndex.intValue(), fieldInfo);
                fieldInfoToIndex.put(fieldInfo, viewOrderIndex.intValue());
                nameToIndex.put(fieldInfo.getName(), viewOrderIndex.intValue());
                System.out.println("Name Mapping: "+fieldInfo.getName()+" => "+viewOrderIndex.intValue());
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
