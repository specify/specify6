/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.toycode.mexconabio;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.toycode.mexconabio.CollStatSQLDefs.StatType;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2010
 *
 */
public class CollectionStats extends AnalysisBase
{
    private boolean                            loadInstsFromDB = false; 
    private Vector<CollStatInfo>               institutions    = new Vector<CollStatInfo>();
    private HashMap<Integer, CollStatInfo>     instHashMap     = new HashMap<Integer, CollStatInfo>();
    private HashMap<TableWriter, CollStatInfo> tblInstHash     = new HashMap<TableWriter, CollStatInfo>();
    private TableWriter                        sortByTblWriter = null;
    private PreparedStatement                  pLMStmt         = null;
    
    /**
     * 
     */
    public CollectionStats()
    {
        super();
        //discoverInstCodesAndtotals();
        
        convLogger = new IndexedConvLogger();
    }
    
    /**
     * 
     */
    public void discoverInstCodesAndTotals()
    {
        System.out.println("Getting Institutions");
        String sql = "SELECT * FROM (SELECT data_provider_id, COUNT(*) AS cnt FROM raw GROUP BY data_provider_id) T1 WHERE cnt > 1 ORDER BY cnt DESC";
        for (Object[] row : BasicSQLUtils.query(dbSrcConn, sql))
        {
            CollStatInfo csi = new CollStatInfo();
            csi.setProviderId((Integer)row[0]);
            csi.setTotalNumRecords(((Long)row[1]).intValue());
            
            if (StringUtils.isEmpty(csi.getInstName()))
            {
                csi.setInstName(getProviderNameFromInstCode(csi.getProviderId()));
            }
            instHashMap.put(csi.getProviderId(), csi);
            institutions.add(csi);
        }
        System.out.println("Done Getting Institutions");
    }
    
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CollStatInfo> loadInstCodesAndtotals()
    {
        XStream xstream = new XStream();
        CollStatInfo.config(xstream);
        
        String xml = "";
        try
        {
            xml = FileUtils.readFileToString(new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatinfo.xml")), "UTF8");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        List<CollStatInfo> list = (List<CollStatInfo>)xstream.fromXML(xml);
        for (CollStatInfo csi : list)
        {
            instHashMap.put(csi.getProviderId(), csi);
            institutions.add(csi);
        }
        return list;
    }
    
    /**
     * @param response
     * @param type
     * @param alphaList
     * @param title
     * @param xTitle
     * @param yTitle
     */
    protected void createChart(final CollStatInfo csi,
                               final Vector<Pair<String, Integer>> list,
                               final String              xTitle,
                               final String              yTitle)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        for (Pair<String, Integer> p : list)
        {
            dataset.addValue(p.second, p.first, ""); 
        }
        
        //if (StringUtils.isEmpty(csi.getInstName()))
        //{
        //    csi.setInstName(getProviderNameFromInstCode(csi.getProviderId()));
        //}
        
        JFreeChart chart = ChartFactory.createBarChart( 
                    csi.getTitle(),
                    xTitle,
                    yTitle,
                    dataset, 
                    PlotOrientation.VERTICAL, 
                    true, true, false 
                    ); 
        
        //chart.getCategoryPlot().setRenderer(new CustomColorBarChartRenderer());

        chart.setBackgroundPaint(new Color(228, 243, 255));
        
        try
        {
            Integer          hashCode = csi.hashCode();
            csi.setChartFileName(hashCode+".png");
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("reports/charts/" + csi.getChartFileName())));
            ChartUtilities.writeChartAsPNG(dos, chart, 700, 600);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param response
     */
    protected boolean generateChart(final CollStatInfo csi, final HashMap<StatType, CollStatSQLDefs> statTypeHash)
    {
        System.out.println(csi.getTitle());
        
        int total = csi.getValue(StatType.eTotalNumRecords);
        
        Vector<Pair<String, Integer>> pairs = new Vector<Pair<String, Integer>>();
        
        double totalPercent = 0.0;
        int    cnt          = 0;
        int    goodCnt      = 0;
        for (StatType type : StatType.values())
        {
            if (type == StatType.eTotalNumRecords) continue;
            
            CollStatSQLDefs csqd = statTypeHash.get(type);
            double dVal = (double)csi.getValue(type);
            int    val = (int)(((dVal / (double)total) * 100.0));
            
            if (val > 0)
            {
                goodCnt++;
            }
            
            //System.out.println(csqd.getName()+"  "+val+ "  " + csi.getValue(csqd.getType())+" / "+ total);
            
            pairs.add(new Pair<String, Integer>(csqd.getName(), val));
            
            if (type.ordinal() < StatType.eHasYearOnly.ordinal())
            {
                totalPercent += val;
                cnt++;
            }
        }
        totalPercent = Math.max(totalPercent, 0.0);
        pairs.add(new Pair<String, Integer>("Average", (int)totalPercent/cnt));
         
        //System.out.println("goodCnt: "+goodCnt);
        if (goodCnt > 0)
        {
            createChart(csi, pairs, "Statistic", "Percent");
            return true;
        }
        return false;
    }
    
    /**
     * 
     */
    public void createCharts()
    {
        loadInstCodesAndtotals();
        
        //for (CollStatInfo csi : institutions)
        //{
        //    csi.setInstName(getProviderNameFromInstCode(csi.getProviderId()));
        //}
        
        List<CollStatSQLDefs> statTypes = getStatSQL();
        HashMap<StatType, CollStatSQLDefs> statTypeHash = new HashMap<StatType, CollStatSQLDefs>();
        for (CollStatSQLDefs cs : statTypes)
        {
            statTypeHash.put(cs.getType(), cs);
        }
        
        CollStatInfo totals = new CollStatInfo(" Totals");
        for (CollStatInfo csi : institutions)
        {
            for (CollStatSQLDefs csqd : statTypes)
            {
                StatType type = csqd.getType();
                int totVal = totals.getValue(type) + csi.getValue(type);
                totals.setValue(type, totVal);
            }
        }
        
        try
        {
            FileUtils.deleteDirectory(new File("reports/charts/"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        Collections.sort(institutions, new Comparator<CollStatInfo>()
        {
            @Override
            public int compare(CollStatInfo o1, CollStatInfo o2)
            {
                return o1.getInstName().compareToIgnoreCase(o2.getInstName());
                //Integer cnt1 = o1.getTotalNumRecords();
                //Integer cnt2 = o2.getTotalNumRecords();
                //return cnt2.compareTo(cnt1);
            }
        });
        
        institutions.insertElementAt(totals, 0);
        CollStatInfo clsi = totals;
        
        //tblWriter.logHdr(titles);
        
        
        int i = 0;
        for (CollStatInfo csi : institutions)
        {
            if (StringUtils.isEmpty(csi.getInstName()))
            {
                csi.setInstName(getProviderNameFromInstCode(csi.getProviderId()));
            }
            
            String title = csi.getTitle() + " - " + csi.getTotalNumRecords();
            
            if (i == 0)
            {
                startLogging("reports", "charts", clsi.hashCode()+".html", title, false);
                tblWriter.startTable();
                tblInstHash.put(tblWriter, csi);

            } else
            {
                tblWriter.endTable();
                startNewDocument(csi.hashCode()+".html", title, false);
                tblInstHash.put(tblWriter, csi);
                tblWriter.startTable();
            }
            
            if (generateChart(csi, statTypeHash))
            {
                int total = csi.getValue(StatType.eTotalNumRecords);
                
                tblWriter.setHasLines();
                tblWriter.print("<TR><TD>");
                tblWriter.print(String.format("<img src=\"%s\">", csi.getChartFileName()));
                tblWriter.println("<BR><BR><BR><BR></TD><TD>");
                tblWriter.startTable();
                tblWriter.logHdr("Stat", "Percent");
                
                int    rowCnt       = 0;
                int    cnt          = 0;
                double totalPercent = 0.0;
                for (StatType type : StatType.values())
                {
                    if (type == StatType.eTotalNumRecords) continue;
                    
                    CollStatSQLDefs csqd = statTypeHash.get(type);
                    
                    double dVal = (double)csi.getValue(type);
                    double val = (((dVal / (double)total) * 100.0));
                    
                    if (type.ordinal() < StatType.eHasYearOnly.ordinal())
                    {
                        tblWriter.print(String.format("<TR class=\"%s\">", (rowCnt % 2 == 0 ? "od" : "ev")));
                        totalPercent += val;
                        cnt++;
                    } else
                    {
                        tblWriter.print(String.format("<TR>", csqd.getName()));
                    }
                    tblWriter.println(String.format("<TD>%s</TD><TD style=\"text-align:right\">%6.2f</TD></TR>", csqd.getName(), val));
                    rowCnt++;
                }
                totalPercent = Math.max(totalPercent, 0.0);
                double avePercent = (totalPercent / (double)cnt);
                tblWriter.println(String.format("<TR><TD>Average</TD><TD style=\"text-align:right\">%6.2f</TD></TR>", avePercent));
                csi.setAveragePercent(avePercent);
                
                tblWriter.endTable();
                tblWriter.println("</TD></TR>");
            }
            
            i++;
            
            /*if (i % 25 == 0)
            {
                tblWriter.endTable();
                startNewDocument("institutions"+i+".html", "Institutions " + i, false);
                tblWriter.setHasLines();
            }*/
            
            //if (i == 100) break;
        }
        tblWriter.endTable();
        
        Vector<CollStatInfo> sortedByAvesList = new Vector<CollStatInfo>(institutions);
        Collections.sort(sortedByAvesList, new Comparator<CollStatInfo>()
        {
            @Override
            public int compare(CollStatInfo o1, CollStatInfo o2)
            {
                Double i1 = o1.getAveragePercent();
                Double i2 = o2.getAveragePercent();
                int rv = i2.compareTo(i1);
                if (rv == 0)
                {
                    Integer cnt1 = o1.getTotalNumRecords();
                    Integer cnt2 = o2.getTotalNumRecords();
                    return cnt2.compareTo(cnt1); 
                }
                return rv;
            }
        });
        
        Integer rank    = 0;
        String  average = "";
        startNewDocument("SortedByAverages.html", " Sorted By Averages", false);
        sortByTblWriter = tblWriter;
        tblWriter.startTable();
        tblWriter.logHdr("Rank", "Institution", "Num of Records", "Percentage");
        for (CollStatInfo csi : sortedByAvesList)
        {
            String aveStr = String.format("%8.2f", csi.getAveragePercent());
            Integer cnt   = csi.getTotalNumRecords();
            if (!aveStr.equals(average))
            {
                rank++;
                average = aveStr;
            }
            
            String fileName = StringUtils.replace(csi.getChartFileName(), "png", "html");
            String link = String.format("<a href=\"%s\">%s</>", fileName, csi.getTitle());
            tblWriter.log(rank.toString(), link, cnt.toString(), aveStr);
        }
        tblWriter.endTable();
        
        //tblWriter.println("</BODY></HTML>");
        endLogging(true);
        
        //saveInstitutions();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(int type, int options)
    {
        if (loadInstsFromDB)
        {
            discoverInstCodesAndTotals();
        } else
        {
            loadInstCodesAndtotals();
        }
        
        for (CollStatSQLDefs csqd : getStatSQL())
        {
            if (csqd.getType() == StatType.eTotalNumRecords) continue;
            
            System.out.println(csqd.getType().toString());
            for (Object[] row : BasicSQLUtils.query(dbSrcConn, csqd.getSQL()))
            {
                //String  instCode = (String)row[0];
                Integer providerId = (Integer)row[0];
                Long    count      = (Long)row[1];
                
                CollStatInfo csi = instHashMap.get(providerId);
                if (csi != null)
                {
                    csi.setValue(csqd.getType(), count.intValue());
                } else
                {
                    System.err.println("Couldn't find institution["+providerId+"]");
                }
            }
        }
        
        for (CollStatInfo csi : institutions)
        {
            System.out.println("\n-------------------" + csi.getTitle()+ "-------------------");
            for (CollStatSQLDefs csqd : getStatSQL())
            {
                System.out.println(String.format("%15s %10d", csqd.getType().toString(), csi.getValue(csqd.getType())));
            }
        }
        
        saveInstitutions();
    }
    
    /**
     * 
     */
    private void saveInstitutions()
    {
        XStream xstream = new XStream();
        CollStatInfo.config(xstream);
        File file = new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatinfo.xml"));
        System.out.println(file.getAbsolutePath());
        try
        {
            Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(file), "UTF8"));
            xstream.toXML(institutions, writer);
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /*private XStream createXStreamWriter() throws UnsupportedEncodingException, FileNotFoundException
    {
        Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream("outfilename"), "UTF8"));
        
        XStream xstream = new PrettyPrintWriter(writer);
        return xstream;
    }*/
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CollStatSQLDefs> getStatSQL()
    {
        XStream xstream = new XStream();
        CollStatSQLDefs.config(xstream);
        
        String xml = "";
        try
        {
            xml = FileUtils.readFileToString(new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatsqldefs.xml")), "UTF8");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return (List<CollStatSQLDefs>)xstream.fromXML(xml);
    }
    
    /**
     * @param instCode
     * @return
     */
    private String getProviderNameFromInstCode(final int providerId)
    {
        if (dbLMConn != null)
        {
            if (pLMStmt == null)
            {
                String sql = "SELECT name FROM data_provider WHERE id = ?";
                try
                {
                    pLMStmt = dbLMConn.prepareStatement(sql);
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            
            try
            {
                pLMStmt.setInt(1, providerId);
                ResultSet rs = pLMStmt.executeQuery();
                if (rs.next())
                {
                    return rs.getString(1);
                }
                rs.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return "N/A";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#cleanup()
     */
    @Override
    public void cleanup()
    {
        if (pLMStmt != null)
        {
            try
            {
                pLMStmt.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        super.cleanup();
    }

    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        CollectionStats cs = new CollectionStats();
        
        //cs.createDBConnection("localhost", "3306", "plants", "root", "root");
        cs.createLMDBConnection("lm2gbdb.nhm.ku.edu", "3399", "gbc20100726", "rods", "specify4us");
        cs.createSrcDBConnection("localhost", "3306", "plants", "root", "root");
        //cs.createSrcDBConnection("conabio.nhm.ku.edu", "3306", "plants", "rs", "");
        //cs.process(0, 0);
        cs.createCharts();
        cs.cleanup();
    }
    
    //------------------------------------------------------------------------------------------
    //-- Classes
    //------------------------------------------------------------------------------------------
    class IndexedConvLogger extends ConversionLogger
    {
        
        /**
         * 
         */
        public IndexedConvLogger()
        {
            super();
        }

        /**
         * @param indexWriter
         * @param orderList
         */
        protected void writeIndex(final TableWriter indexWriter, final Vector<TableWriter> orderList)
        {
            orderList.remove(sortByTblWriter);
            orderList.insertElementAt(sortByTblWriter, 1);
            
            HashSet<String> alphaSet = new HashSet<String>();
            for (TableWriter tblWr : orderList)
            {
                String titleLetter = tblWr.getTitle().substring(0,1).toUpperCase().trim();
                if (!titleLetter.isEmpty())
                {
                    alphaSet.add(titleLetter);
                }
            }
            Vector<String> alphaList = new Vector<String>(alphaSet);
            Collections.sort(alphaList);
            
            int i = 0;
            for (String alpha : alphaList)
            {
                if (StringUtils.isAlphanumeric(alpha))
                {
                    if (i > 0) indexWriter.append(", ");
                    indexWriter.print("<a href=\"#"+alpha+"\">"+alpha+"</a>");
                }
                i++;
            }
            indexWriter.println("<BR><BR>");
            
            String alphaAnchor = "";
            indexWriter.startTable();
            for (TableWriter tblWr : orderList)
            {
                System.out.println(tblWr.getTitle());
             
                String alpha = tblWr.getTitle().substring(0,1).toUpperCase();
                if (!alpha.equals(" ") && !alpha.equals(alphaAnchor))
                {
                    indexWriter.print("<a name=\""+alpha+"\"></a><H3>"+alpha+"</H3>");
                    alphaAnchor = alpha;
                }
                
                try
                {
                    String title = tblWr == sortByTblWriter ? "Rankings" : tblInstHash.get(tblWr).getTitle();
                    indexWriter.log("<A href=\""+ FilenameUtils.getName(tblWr.getFileName())+"\">"+title+"</A>");
                    tblWr.close();
                   
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            indexWriter.endTable();
        }
    }
}
