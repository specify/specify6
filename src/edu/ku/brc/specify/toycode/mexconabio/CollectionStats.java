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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
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

    private List<CollStatInfo>            institutions = new ArrayList<CollStatInfo>();
    private HashMap<String, CollStatInfo> instHashMap  = new HashMap<String, CollStatInfo>();
    
    /**
     * 
     */
    public CollectionStats()
    {
        super();
        //discoverInstCodesAndtotals();
    }
    
    public void discoverInstCodesAndtotals()
    {
        System.out.println("Getting Institutions");
        String sql = "SELECT * FROM (SELECT institution_code, COUNT(*) AS cnt FROM raw GROUP BY institution_code) T1 WHERE cnt > 1 ORDER BY cnt DESC";
        for (Object[] row : BasicSQLUtils.query(dbSrcConn, sql))
        {
            CollStatInfo csi = new CollStatInfo();
            csi.setTitle(row[0].toString());
            csi.setTotalNumRecords(((Long)row[1]).intValue());
            instHashMap.put(csi.getTitle(), csi);
            institutions.add(csi);
        }
        System.out.println("Done Getting Institutions");
    }
    
    
    @SuppressWarnings("unchecked")
    public List<CollStatInfo> loadInstCodesAndtotals()
    {
        XStream xstream = new XStream();
        CollStatInfo.config(xstream);
        
        String xml = "";
        try
        {
            xml = FileUtils.readFileToString(new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatinfo.xml")));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        List<CollStatInfo> list = (List<CollStatInfo>)xstream.fromXML(xml);
        for (CollStatInfo csi : list)
        {
            instHashMap.put(csi.getTitle(), csi);
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
    protected boolean generateDateChart(final CollStatInfo csi, final List<CollStatSQLDefs> statTypes)
    {
        System.out.println(csi.getTitle());
        
        int total = csi.getValue(StatType.eTotalNumRecords);
        
        Vector<Pair<String, Integer>> pairs = new Vector<Pair<String, Integer>>();
        
        int goodCnt = 0;
        for (CollStatSQLDefs csqd : statTypes)
        {
            if (csqd.getType() == StatType.eTotalNumRecords) continue;
            
            double dVal = (double)csi.getValue(csqd.getType());
            int    val = (int)(((dVal / (double)total) * 100.0) + 0.5);
            
            if (val > 0)
            {
                goodCnt++;
            }
            
            //System.out.println(csqd.getName()+"  "+val+ "  " + csi.getValue(csqd.getType())+" / "+ total);
            
            pairs.add(new Pair<String, Integer>(csqd.getName(), val));
        }
         
        //System.out.println("goodCnt: "+goodCnt);
        if (goodCnt > 0)
        {
            createChart(csi, pairs, "Percent", "Statistic");
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
        
        List<CollStatSQLDefs> statTypes = getStatSQL();
        
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
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                //Integer cnt1 = o1.getTotalNumRecords();
                //Integer cnt2 = o2.getTotalNumRecords();
                //return cnt2.compareTo(cnt1);
            }
        });
        
        CollStatInfo clsi = institutions.get(0);
        
        
        //tblWriter.logHdr(titles);
        
        
        int i = 0;
        for (CollStatInfo csi : institutions)
        {
            if (i == 0)
            {
                startLogging("reports", "charts", clsi.hashCode()+".html", clsi.getTitle(), false);
                tblWriter.startTable();

            } else
            {
                tblWriter.endTable();
                startNewDocument(csi.hashCode()+".html", csi.getTitle(), false);
                tblWriter.startTable();
            }
            
            if (generateDateChart(csi, statTypes))
            {
                tblWriter.setHasLines();
                tblWriter.print("<TR><TD>");
                tblWriter.print(String.format("<img src=\"%s\">", csi.getChartFileName()));
                tblWriter.println("<BR><BR><BR><BR></TD></TR>");
            }
            
            i++;
            
            /*if (i % 25 == 0)
            {
                tblWriter.endTable();
                startNewDocument("institutions"+i+".html", "Institutions " + i, false);
                tblWriter.setHasLines();
            }*/
            
            //if (i == 75) break;
        }
        tblWriter.endTable();
        //tblWriter.println("</BODY></HTML>");
        endLogging(true);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(int type, int options)
    {
        loadInstCodesAndtotals();
        
        for (CollStatSQLDefs csqd : getStatSQL())
        {
            if (csqd.getType() != StatType.eMissingCountries) continue;
            
            System.out.println(csqd.getType().toString());
            for (Object[] row : BasicSQLUtils.query(dbSrcConn, csqd.getSQL()))
            {
                String  instCode = (String)row[0];
                Long count       = (Long)row[1];
                
                CollStatInfo csi = instHashMap.get(instCode);
                if (csi != null)
                {
                    csi.setValue(csqd.getType(), count.intValue());
                } else
                {
                    System.err.println("Couldn't find institution["+instCode+"]");
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
        
        XStream xstream = new XStream();
        CollStatInfo.config(xstream);
        File file = new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatinfo.xml"));
        System.out.println(file.getAbsolutePath());
        try
        {
            FileUtils.writeStringToFile(file, xstream.toXML(institutions));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
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
            xml = FileUtils.readFileToString(new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatsqldefs.xml")));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return (List<CollStatSQLDefs>)xstream.fromXML(xml);
        
        /*
        XStream xstream = new XStream(
                new XppDriver() 
                {
                    @Override
                    public HierarchicalStreamWriter createWriter(Writer out) 
                    {
                        return new PrettyPrintWriter(out) 
                        {
                            @Override
                            protected void writeText(QuickWriter writer, String text) 
                            {
                                writer.write("<![CDATA[");
                                writer.write(text);
                                writer.write("]]>");
                            }
                        };
                    }
                }
            );
        CollStatSQLDefs.config(xstream);
        
        ArrayList<CollStatSQLDefs> array = new ArrayList<CollStatSQLDefs>();
        for (StatType st : StatType.values())
        {
            CollStatSQLDefs csi = new CollStatSQLDefs();
            csi.setType(st);
            csi.setName(st.toString());
            csi.setSQL("SELECT * FROM (SELECT institution_code, COUNT(*) AS cnt FROM raw GROUP BY institution_code) T1 WHERE cnt > 1 ORDER BY cnt DESC");
            array.add(csi);
        }
        
        File file = new File(XMLHelper.getConfigDirPath("../src/edu/ku/brc/specify/toycode/mexconabio/collstatsqldefs.xml"));
        System.out.println(file.getAbsolutePath());
        //if (file.exists())
        //{
        //    //return (Hashtable<String, Skin>)xstream.fromXML(XMLHelper.getContents(skinsFile));
        //}
        try
        {
            FileUtils.writeStringToFile(file, xstream.toXML(array));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return array;*/
    }
    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        CollectionStats cs = new CollectionStats();
        
        //cs.createDBConnection("localhost", "3306", "plants", "root", "root");
        cs.createSrcDBConnection("localhost", "3306", "plants_ref", "root", "root");
        //cs.discoverInstCodesAndtotals();
        //cs.process(0, 0);
        cs.createCharts();
        cs.cleanup();
    }
}
