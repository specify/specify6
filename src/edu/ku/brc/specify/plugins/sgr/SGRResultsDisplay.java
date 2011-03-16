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

import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModelRowInfo;
import edu.ku.brc.specify.plugins.sgr.RawData.DataIndexType;
import edu.ku.brc.specify.toycode.mexconabio.AnalysisBase;
import edu.ku.brc.ui.DateParser;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class SGRResultsDisplay extends BaseResultsDisplay
{
    //private static final Logger  log = Logger.getLogger(SGRResultsDisplay.class);
    private static final HashMap<String, Color> colorHash = new HashMap<String, Color>();
    
    /*
     "id, institution_code, collection_code, collector_num, " +
     "catalogue_number, collector_name, genus, species, " +
     "subspecies, locality,  CONCAT_WS('-', year, month, day), latitude, " +
      "longitude, country, state_province, county, " +
      "continent_ocean, lat_long_precision, max_altitude, min_altitude, " +
      "altitude_precision, min_depth, max_depth, depth_precision," +
      "lat_long_precision
    */
    
    private final String[] colNames = {"Id", "Institution Code", "Collection Code", "Collector Number", 
                                       "Catalog Number", "Collector Name", "Genus", "Species", 
                                       "Subspecies",  "Locality", "Date", "Latitude", 
                                       "Longitude", "Country", "State", "County", 
                                       "Continent Ocean", "Lat Long Prec", "Max altitude", "Min altitude", 
                                       "Alt Precision", "Min Depth", "Max Depth", "Depth Precision", 
                                       "Score"};
    
             //                         0          1                 2                3
    private final String[] wbNames = {"id", "institutioncode", "collectioncode", "fieldnumber", 
            //                             4                  5            6             7 
                                      "catalognumber", "collectorname", "genus1", "species1", 
            //                           8                9               10          11
                                      "subspecies1", "localityname", "startdate", "latitude1", 
            //                            12            13       14      15
                                      "longitude1", "country", "state", "county", 
            //                            16             17           18                19
                                      "continent", "latlonprec",  "maxaltitude", "minaltitude", 
            //                            20             21         22                23
                                      "altprecision", "minDepth", "maxdepth", "depthprecision", 
            //                         24 
                                      null};
//--------------------------------------------------------------------------------------------------------------
//    0              1               2           3           4               5               6
//"fieldnumber", "catalognumber", "genus1", "species1", "subspecies1", "collectorname", "localityname",
//  7              8           9        10     11        12        13      14    
//"latitude1", "longitude1", "year", "month", "day", "country", "state", "county",
// 15           16              17                18                      19          20          21 
//"family1", "maxaltitude", "minaltitude", "institutioncode", "collectioncode", "author", "startdate",

    private final Integer[] colIndex = {3, 4, 6, 7, 
                                        8, 5, 9, 11, 
                                        12, 10, null, null, 
                                        13, 14, 15, 19, 
                                        null, 18, 19, 1, 
                                        null, 10};
    
    private HashMap<String, Integer> wbColToIndexHash = new HashMap<String, Integer>();
    
    private final Class<?> dataClasses[] = {Integer.class, String.class, String.class, String.class, 
                                            String.class, String.class, String.class, String.class, 
                                            String.class, String.class, String.class, String.class, 
                                            String.class, String.class, String.class, String.class, 
                                            String.class, String.class, String.class, String.class, 
                                            String.class, String.class, String.class, String.class,
                                            Integer.class, Integer.class, };
    
    protected List<Integer>  idList     = new Vector<Integer>();
    protected List<Integer>  rowScores  = new Vector<Integer>();
    protected RawData        baseRow;
    protected DataResultsRow dataResRow;
    
    protected AnalysisBase  analysis  = new AnalysisBase();
    
    protected JButton       geoRefToolBtn;

    
    /**
     * @param connection
     */
    public SGRResultsDisplay(final Connection connection)
    {
        super(connection);
        
        analysis.setColorsForJTable();
    }
    
    /**
     * @param dataResRow
     */
    public void setGroupData(final DataResultsRow dataResultRow)
    {
        this.dataResRow = dataResultRow;
        this.baseRow    = dataResultRow.getRawData();
        
        hasData = false;
        
        idList.clear();
        if (dataResRow.getGrpRawData() != null)
        {
            idList.addAll(dataResRow.getGrpRawData().getRawIds());
        }
        
        createAndFillModels();
        
        topTable.setModel(model);
        botTable.setModel(newModel);
        
        initColorGrid(model.getValues());
        
        if (imgView != null)
        {
            if (baseRow.getImgIcon() != null)
            {
                Dimension     size = imgView.getSize();
                BufferedImage bi   = null;
                if (baseRow.getImgIcon().getImage() instanceof BufferedImage)
                {
                    bi = (BufferedImage)baseRow.getImgIcon().getImage();
                } else
                {
                    bi = GraphicsUtils.getBufferedImage(baseRow.getImgIcon());
                }
                
                imgView.setJpg(bi);
                
                if (bi != null)
                {
                    float ratio;
                    float w = bi.getWidth();
                    float h = bi.getHeight();
                    if (w > h)
                    {
                        ratio = size.width > w ? w / (float)size.width : (float)size.width / w;
                    } else
                    {
                        ratio = size.height > h ? h / (float)size.height : (float)size.height / h;
                    }
                    final float ratioF = ratio;
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            imgView.setZoom(ratioF);
                        }
                    });
                    
                }
            } else
            {
                imgView.setJpg((BufferedImage)null);
            }
        }
        
        /*
        Color CADETBLUE = new Color(166, 121, 0);
        Color DARKGREEN = new Color(48, 77, 166);
        Color GREEN     = new Color(166, 0, 166);
        Color YELLOW    = new Color(166, 166, 0);
        
        int rowInx = 0;
        for (Object dataObj : model.getValues())
        {
            Color[]  colorRow = colorGrid.get(rowInx);
            Object[] row      = (Object[])dataObj;
            for (int i=0;i<colorRow.length;i++)
            {
                //System.out.println(i+"  "+row[i]);
                colorRow[i] = null;
            }
            
            for (RawData.DataIndexType dt : RawData.DataIndexType.values())
            {
                if (dt.ordinal() >= colIndex.length)
                {
                    continue;
                }
                
                Integer inx = colIndex[dt.ordinal()];
                if (inx != null)
                {
                    Object rdDataObj  = baseRow.getData(dt);
                    Object mdDataObj  = row[inx];
                    
                    if ((rdDataObj instanceof String || rdDataObj == null) &&
                        (mdDataObj instanceof String || mdDataObj == null))
                    {
                        String rdStr = (String)rdDataObj;
                        String mdStr = (String)mdDataObj;
                        
                        if (StringUtils.isNotEmpty(rdStr) && StringUtils.isNotEmpty(mdStr))
                        {
                            colorRow[inx] = rdStr.equals(mdStr) ? Color.BLACK : GREEN;
                            
                        } else if (StringUtils.isEmpty(rdStr) && StringUtils.isNotEmpty(mdStr))
                        {
                            colorRow[inx] = DARKGREEN;
                            
                        } else if (StringUtils.isNotEmpty(rdStr) && StringUtils.isEmpty(mdStr))
                        {
                            colorRow[inx] = YELLOW;
                        } else
                        {
                            colorRow[inx] = CADETBLUE;
                        }
                    } else if (rdDataObj != null && mdDataObj != null)
                    {
                        colorRow[inx] = rdDataObj.equals(mdDataObj) ? Color.BLACK : GREEN;
                        
                    } else if (rdDataObj == null && mdDataObj != null)
                    {
                        colorRow[inx] = DARKGREEN;
                        
                    } else if (rdDataObj != null && mdDataObj == null)
                    {
                        colorRow[inx] = YELLOW;
                    } else
                    {
                        colorRow[inx] = CADETBLUE;
                    }
                    //System.out.println(dt+"  ord: "+dt.ordinal()+"  inx "+inx+"  rd: "+rdDataObj+"  md: "+mdDataObj+"  clr: "+colorRow[inx]+"  "+row[inx]);
                }
            }
            rowInx++;
        }*/
    }
    
    /**
     * 
     */
    protected void initColorGrid(final Vector<Object[]> vals)
    {
        if (colorGrid == null)
        {
            colorGrid = new Vector<Color[]>();
            for (Object dataObj : vals)
            {
                Object[] row      = (Object[])dataObj;
                Color[]  colorRow = new Color[row.length];
                for (int i=0;i<colorRow.length;i++)
                {
                    colorRow[i] = Color.BLACK;
                }
                colorGrid.add(colorRow);
            }
            
            if (colorGrid != null && colorGrid.size() > 0)
            {
                Color[] colors = new Color[colorGrid.get(0).length];
                for (int i=0;i<colors.length;i++)
                {
                    colors[i] = Color.BLACK;
                }
                colorGrid.insertElementAt(colors, 0);
            }
        }

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseCleanup#createAndFillModels()
     */
    @Override
    protected void createAndFillModels()
    {
        model = new DataObjTableModel(connection, 100, null, false)
        {
            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#buildSQL()
             */
            @Override
            protected String buildSQL()
            {
                String sql = "SELECT id, institution_code, collection_code, collector_num, " +
                		     "catalogue_number, collector_name, genus, species, subspecies, locality, " +
                		     "CONCAT_WS('-', year, SUBSTRING(CONCAT('0' , month), -2), SUBSTRING(CONCAT('0' , day), -2)), " +
                              "latitude, longitude, country, state_province, county, " +
                              "continent_ocean, lat_long_precision, max_altitude, min_altitude, " +
                              "altitude_precision, min_depth, max_depth, depth_precision," +
                              "lat_long_precision " +
                              "FROM raw WHERE ID in (%s)";
                
                StringBuilder sb = new StringBuilder();
                for (Integer id : idList)
                {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(id);
                }
                
                String fullSQL = String.format(sql, sb.toString());
                log.debug(fullSQL);
                            
                tableInfo = new DBTableInfo(100, this.getClass().getName(), "raw2", "id", "r");
                
                for (int i=0;i<colNames.length;i++)
                {
                    DBFieldInfo fi = new DBFieldInfo(tableInfo, colNames[i], dataClasses[i]);
                    fi.setTitle(colNames[i]);
                    colDefItems.add(fi);
                    
                    if (StringUtils.isNotEmpty(wbNames[i]))
                    {
                        wbColToIndexHash.put(wbNames[i], i);
                        //System.out.println("mappingv["+wbNames[i]+"]  i["+i+"]");
                    }
                }
                numColumns = colNames.length;
                
                return idList.size() == 0 ? null : fullSQL;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#addAdditionalRows(java.util.ArrayList, java.util.ArrayList)
             */
            @Override
            protected void addAdditionalRows(final ArrayList<DBInfoBase> colDefItems,
                                             final ArrayList<DataObjTableModelRowInfo> rowInfoList)
            {
                super.addAdditionalRows(colDefItems, rowInfoList);
                if (baseRow == null)
                {
                    return;
                }
                
                initColorGrid(values);
                
                Object[] row = new Object[colDefItems.size()];
                for (String colName : wbNames)
                {
                    if (colName != null)
                    {
                        Object  data      = baseRow.getData(colName);
                        Integer dataIndex = wbColToIndexHash.get(colName);
                        if (dataIndex != null)
                        {
                            row[dataIndex] = data != null ? data : "";
                        }
                        System.out.println("colName["+colName+"]  data["+data+"]  dataIndex["+dataIndex+"]");
                    }
                }
                
                /*System.out.println("=================================================================================");
                for (Object obj : row)
                {
                    System.out.print(obj+",");
                    
                }
                System.out.println();
                System.out.println("---------------------------------------------------------------------------------");
                for (Object[] dataRow : values)
                {
                    for (Object obj : dataRow)
                    {
                        System.out.print(obj+",");
                        
                    }
                    System.out.println();
                }*/
                
                ArrayList<DataObjTableModelRowInfo> oldItems = new ArrayList<DataObjTableModelRowInfo>(rowInfoList);
                for (DataObjTableModelRowInfo ri : oldItems)
                {
                    ri.setMainRecord(false);
                }
                rowInfoList.clear();
                rowInfoList.add(new DataObjTableModelRowInfo(-1, true, true));
                rowInfoList.addAll(oldItems);
                
                values.insertElementAt(row, 0);
                
                if (dataResRow.getGrpSNIBData() != null)
                {
                    addSNIBRows(dataResRow, colDefItems.size(), values, rowInfoList);
                }
                
                System.out.println("---------------- REF ROW ------------------");
                Object[] refRow = new Object[AnalysisBase.NUM_FIELDS];
                for (DataIndexType dit : DataIndexType.values())
                {
                    if (dit.ordinal() < AnalysisBase.NUM_FIELDS)
                    {
                        refRow[dit.ordinal()] = baseRow.getData(dit);
                        System.out.println(dit+" / "+refRow[dit.ordinal()] +"  "+dit.ordinal());
                    }
                }
                System.out.println();
                
                row[row.length-1] = 100;
                
                if (colorGrid.size() > 0)
                {
                    Color[] cr = colorGrid.get(0);
                    for (int i=0;i<cr.length-1;i++)
                    {
                        cr[i] = Color.BLACK;
                    }
                }
                
                Object[] cmpRow = new Object[AnalysisBase.NUM_FIELDS];
                for (int rowInx=1;rowInx<values.size();rowInx++)
                {
                    Object[] valRow = values.get(rowInx);
                    
                    for (int ii=0;ii<cmpRow.length;ii++)
                    {
                        cmpRow[ii] = null;
                    }
                        
                    System.out.println("----- Compare Rows -------------");
                    for (String colName : wbNames)
                    {
                        if (colName != null)
                        {
                            Integer destIndex = RawData.getIndex(colName);
                            Integer srcIndex = wbColToIndexHash.get(colName);
                            //System.out.println(destIndex+" / "+srcIndex);
                            if (destIndex != null && srcIndex != null && 
                                destIndex < AnalysisBase.NUM_FIELDS && srcIndex < valRow.length)
                            {
                                cmpRow[destIndex] = valRow[srcIndex];
                                System.out.println(colName+" / "+valRow[srcIndex]+"  "+destIndex);
                            }
                        }
                    }
                    System.out.println();
                    
                    Integer srcIndex = wbColToIndexHash.get("startdate");
                    String  dateStr  = (String)valRow[srcIndex];
                    if (StringUtils.isNotEmpty(dateStr))
                    {
                        DateParser dp   = new DateParser("yyyy-MM-dd");
                        Date       date = dp.parseDate(dateStr);
                        if (date != null)
                        {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cmpRow[DataIndexType.eYear.ordinal()]  = Integer.toString(cal.get(Calendar.YEAR));
                            cmpRow[DataIndexType.eMonth.ordinal()] = Integer.toString(cal.get(Calendar.MONTH)+1);
                            cmpRow[DataIndexType.eDay.ordinal()]   = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                        } else
                        {
                            log.error("Bad date["+dateStr+"]");
                        }
                    }
                    
                    analysis.clearRowAttrs(); // clears colors
                    
                    int    score    = analysis.score(refRow, cmpRow);
                    double maxScore = analysis.getMaxScore();
                    valRow[valRow.length-1] = (int)(((score  / maxScore) * 100.0) + 0.5);
                    
                    System.out.println(String.format("Score: %d,  Max: %5.2f,  Percent: %d", score, maxScore, valRow[valRow.length-1]));
                    
                    initColorGrid(values);
                    
                    String[] colors   = analysis.getTDColorCodes();
                    Color[]  colorRow = rowInx < colorGrid.size() ? colorGrid.get(rowInx) : new Color[colorGrid.size()];
                    if (colorRow.length > 0)
                    {
                        for (int i=0;i<colorRow.length-1;i++)
                        {
                            colorRow[i] = Color.GRAY;
                        }
                        colorRow[colorRow.length-1] = Color.BLACK;
                    }
                    
                    for (String colNm : wbNames)
                    {
                        if (colNm != null)
                        {
                            boolean isStartDate = colNm.equals("startdate");
                            Integer dIndex = RawData.getIndex(colNm);
                            Integer sIndex = wbColToIndexHash.get(colNm);
                            //System.out.println(colNm+"  dIndex:"+dIndex+"  sIndex:"+sIndex+"  colorRow.length:"+colorRow.length+"  colors.length:"+colors.length+"  "+Color.GRAY.getBlue());
                            
                            if (dIndex != null && srcIndex != null && 
                                (isStartDate || dIndex < colors.length) && sIndex < colorRow.length)
                            {
                                String rgb = isStartDate ? colors[AnalysisBase.DAY_INX] : colors[dIndex];
                                //System.out.println(colNm+"  colorStr:"+rgb+"  dIndex:"+dIndex+"  sIndex:"+sIndex);
                                if (rgb != null)
                                {
                                    Color c = colorHash.get(rgb);
                                    if (c == null)
                                    {
                                        c = UIHelper.parseRGB(rgb);
                                        colorHash.put(rgb, c);
                                    }
                                    colorRow[sIndex] = c;
                                } else
                                {
                                    colorRow[sIndex] = Color.GRAY;
                                }
                            }
                        }
                    }
                    
                    /*int colInx = 0;
                    for (String colorStr : analysis.getTDColorCodes())
                    {
                        System.out.print(colorStr+", ");
                        if (colorStr != null)
                        {
                            int rgb = Integer.parseInt(colorStr, 16);
                            colorRow[colInx] = new Color(rgb);
                        } else
                        {
                            colorRow[colInx] = Color.BLACK;
                        }
                        colInx++;
                    }
                    System.out.println();
                    */
                }
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#doneFillingModels()
             */
            @Override
            protected void doneFillingModels(final Vector<Object[]> values)
            {
                Collections.sort(values, new Comparator<Object[]>()
                {
                    @Override
                    public int compare(Object[] o1, Object[] o2)
                    {
                        if (o1 != null && o2 != null && o1[o1.length-1] instanceof Integer && o2[o2.length-1] instanceof Integer)
                        {
                            Integer i1 = (Integer)o1[o1.length-1];
                            Integer i2 = (Integer)o2[o2.length-1];
                            
                            if (i1 != null && i2 != null)
                            {
                                return i2.compareTo(i1);
                            }
                        }
                        return 0;
                    }
                    
                });
                UIHelper.calcColumnWidths(topTable);
            }
            
            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#processColumns(java.lang.Object[])
             */
            protected void processColumns(final Object[] cmpRow)
            {
                String instCode        = (String)cmpRow[1];
                cmpRow[1] = (StringUtils.isNotEmpty(instCode) && instCode.length() > 8) ? instCode.substring(0, 8) : instCode;
                
                int rowScore = 0;
                if (cmpRow[2] != null) rowScore += 15; // Collector Number
                if (cmpRow[4] != null) rowScore += 10; // Genus
                if (cmpRow[5] != null) rowScore += 12; // Species
                    
                if (cmpRow[17] != null) rowScore += 8; // Country
                if (cmpRow[22] != null) 
                {
                    String ymd = (String)cmpRow[22];
                    if (ymd.length() > 4)
                    {
                        char sep = ymd.charAt(4);
                        if (sep == '-' || sep == '/' || sep == '.')
                        {
                            rowScore += 10;
                        }
                    }
                    if (ymd.length() > 7)
                    {
                        char sep = ymd.charAt(7);
                        if (sep == '-' || sep == '/' || sep == '.')
                        {
                            rowScore += 5;
                        }
                    }
                }
                cmpRow[cmpRow.length-1] = (int)((double)rowScore / 60.0 * 100.0);
                //System.err.println(cmpRow[cmpRow.length-1]);
            }
        };
        model.setFirstColBool(false);
        
        newModel = new DataObjTableModel(connection, 100, model.getItems(), model.getHasDataList(), 
                                         model.getSameValues(), model.getMapInx(), model.getIndexHash());
        newModel.setFirstColBool(false);
    }
    
    private void addSNIBRows(final DataResultsRow dataResRow, 
                             final int numColumns,
                             Vector<Object[]>                          values,
                             final ArrayList<DataObjTableModelRowInfo> rowInfoList)
    {
        String snibSQL = "SELECT IdSNIB, InstitutionAcronym, CollectorNumber, CatalogNumber, CONCAT_WS(',', LastNameFather, FirstName), " +
                         "Genus, Species, Cataegoryinfraspecies, Locality, `Year`, `Month`, `Day`, Latitude, Longitude, " +
                         "Country, State FROM angiospermas WHERE IdSNIB in (%s)";

        
        StringBuilder sb = new StringBuilder();
        for (Integer id : dataResRow.getGrpSNIBData().getRawIds())
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        
        String fullSQL = String.format(snibSQL, sb.toString());
        log.debug(fullSQL);      
        
       for (Object[] row : BasicSQLUtils.query(connection, fullSQL))
       {
           Object[] r = new Object[numColumns];
           int i = 1;
           for (int inx=1;inx<numColumns;inx++)
           {
               if (i < row.length)
               {
                   Object dataObj = row[i];
                   //System.out.println("["+dataObj+"] "+(dataObj != null ? dataObj.getClass().getSimpleName() : ""));
                   
                   if (i == 9)
                   {
                       int yr = (Integer)dataObj;
                       int mn = (Integer)row[++i];
                       int dy = (Integer)row[++i];
                       dataObj = String.format("%4d-%02d-%02d", yr, mn, dy);
                   }
                   
                   if (dataObj != null && !(dataObj instanceof String))
                   {
                       dataObj = dataObj.toString();
                   }
                   System.out.println("["+dataObj+"]");
                   
                   r[inx] = dataObj;
                   if (i == 1) // index '2' is for Collection Catalog Code
                   {
                       inx++;
                       r[inx] = "SNIB";
                   }
                       
               } else
               {
                   r[inx] = null;
               }
               i++;
           }
           r[0] = row[0];
           rowInfoList.add(new DataObjTableModelRowInfo((Integer)r[0], false, false));
           values.add(r);
       }
       
       
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.sgr.BaseResultsDisplay#getDataRow()
     */
    @Override
    protected Object getDataRow()
    {
        Vector<Object[]> values = newModel.getValues();
        if (values != null && values.size() > 0)
        {
            RawData  rawData = GroupHashDAO.getInstance().getRawDataObj();
            Object[] row     = values.get(0);
            for (DataIndexType dt : DataIndexType.values())
            {
                Integer inx = colIndex[dt.ordinal()];
                if (inx != null)
                {
                    rawData.setData(dt, row[inx]);
                }
            }
            return rawData;
        }
        
        return  null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.sgr.BaseResultsDisplay#rowSelected(int)
     */
    @Override
    protected void rowSelected(final int row)
    {
        super.rowSelected(row); // Must be Called
        
        geoRefToolBtn.setEnabled(row > -1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.sgr.BaseResultsDisplay#getNumTools()
     */
    @Override
    protected boolean hasTools()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.sgr.BaseResultsDisplay#getToolsPanel()
     */
    @Override
    protected JPanel getToolsPanel()
    {
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g, p, 4px, p, 4px, p, 4px, p, f:p:g", "p"));
        
        pb.add( createGeoRefTool(),      cc.xy(2, 1));
        pb.add( createImageLabelTool(),  cc.xy(4, 1));
        pb.add( createWeightingConfig(), cc.xy(6, 1));
        pb.add( createColorConfig(),     cc.xy(8, 1));
        
        return pb.getPanel();
    }
    
    /**
     * @return
     */
    private JButton createWeightingConfig()
    {
        return createIconBtn("Weightings", IconManager.IconSize.NonStd, "Weighting Config", false, true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //toggleImageFrameVisible();
            }
        });
    }
    
    /**
     * @return
     */
    private JButton createColorConfig()
    {
        return createIconBtn("ColorPalette", IconManager.IconSize.NonStd, "COlor COnfig", false, true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //toggleImageFrameVisible();
            }
        });
    }
    
    /**
     * @return
     */
    private JButton createImageLabelTool()
    {
        return createIconBtn("CardImage", IconManager.IconSize.NonStd, "Show Image Label", false, true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //toggleImageFrameVisible();
            }
        });
    }
    
    /**
     * @return
     */
    private JButton createGeoRefTool()
    {
        AppPreferences remotePrefs = AppPreferences.getRemote();
        final String tool = remotePrefs.get("georef_tool", "geolocate");
        String iconName = "GEOLocate20"; //tool.equalsIgnoreCase("geolocate") ? "GeoLocate" : "BioGeoMancer";
        String toolTip = tool.equalsIgnoreCase("geolocate") ? "WB_DO_GEOLOCATE_LOOKUP"
                : "WB_DO_BIOGEOMANCER_LOOKUP";
        
        return geoRefToolBtn = createIconBtn(iconName, IconManager.IconSize.NonStd,
                toolTip, false, new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        if (tool.equalsIgnoreCase("geolocate"))
                        {
                            //doGeoRef(new GeoCoordGeoLocateProvider(), "WB.GeoLocateRows");
                        }
                        else
                        {
                            //doGeoRef(new GeoCoordBGMProvider(), "WB.BioGeomancerRows");
                        }
                    }
                });
    }
}
