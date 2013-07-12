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
package edu.ku.brc.specify.toycode;

import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 11, 2008
 *
 */
public class BugParse
{
    protected Hashtable<Integer, Hashtable<Integer, Integer>> hashNew = new Hashtable<Integer, Hashtable<Integer,Integer>>();
    protected Hashtable<Integer, Hashtable<Integer, Integer>> hashRes = new Hashtable<Integer, Hashtable<Integer,Integer>>();
    protected Hashtable<Integer, Hashtable<Integer, Integer>> hashDif = new Hashtable<Integer, Hashtable<Integer,Integer>>();
    
    protected Hashtable<Integer, Bug> bugHash = new Hashtable<Integer, Bug>();
    
    public BugParse()
    {
        parse("20060901_.txt");
        parse("20070901_.txt");
        parse("20080301_.txt");
        parse("20080701_.txt");
        parse("Now_.txt");
        
        String eng = "meg";
        boolean doPriority = false;
        int cnt = 0;
        Vector<Integer> keys = new Vector<Integer>(bugHash.keySet());
        Collections.sort(keys);
        for (Bug bug : bugHash.values())
        {
            if ((eng == null || bug.getEngineer().startsWith(eng)) && (!doPriority || bug.getPriority().equals("P1") || bug.getPriority().equals("P1")))
            {
                process(hashNew, bug.getOpenYear(), bug.getOpenMon());
                if (bug.isClosed)
                {
                    process(hashRes, bug.getModYear(), bug.getModMon());
                }
                System.out.println(bug.getNumber()+"  "+bug.getOpenDate()+"  "+bug.getModDate()+"  "+bug.isClosed());
                cnt++;
            }
        }
        
        System.err.println(cnt);
        StringBuilder yearStr = new StringBuilder();
        StringBuilder dataStr = new StringBuilder();

        for (int year=2005;year<2009;year++)
        {
            int start = year == 2005 ? 7 : 1;
            int end   = year == 2008 ? 11 : 13;
            
            Hashtable<Integer, Integer> yearHashNew = hashNew.get(year);
            for (int mon=start;mon<end;mon++)
            {
                Integer monNew = yearHashNew != null ? yearHashNew.get(mon) : null;
                if (monNew == null)
                {
                    monNew = 0;
                }
                if (yearStr.length() > 0) yearStr.append(',');
                yearStr.append(mon+"/"+Integer.toString(year-2000));
                
                if (dataStr.length() > 0) dataStr.append(',');
                dataStr.append(monNew);
            }
        }
        
        List<String> dataLines = new Vector<String>();
        dataLines.add(yearStr.toString());
        dataLines.add(dataStr.toString());
        
        System.out.println(yearStr.toString());
        System.out.println(dataStr.toString());
        
        yearStr = new StringBuilder();
        dataStr = new StringBuilder();
        StringBuilder dataStrDif = new StringBuilder();
        int diffTotal = 0;
        
        for (int year=2005;year<2009;year++)
        {
            int start = year == 2005 ? 7 : 1;
            int end   = year == 2008 ? 11 : 13;
            
            Hashtable<Integer, Integer> yearHashNew = hashNew.get(year);
            Hashtable<Integer, Integer> yearHashRes = hashRes.get(year);
            
            for (int mon=start;mon<end;mon++)
            {
                Integer monNew = yearHashNew != null ? yearHashNew.get(mon) : null;
                if (monNew == null)
                {
                    monNew = 0;
                }
                Integer monRes = yearHashRes != null ? yearHashRes.get(mon) : null;
                if (monRes == null)
                {
                    monRes = 0;
                }
                
                if (yearStr.length() > 0) yearStr.append(',');
                yearStr.append(mon+"/"+Integer.toString(year-2000));
                
                if (dataStr.length() > 0) dataStr.append(',');
                dataStr.append(monRes);
                
                diffTotal += monNew - monRes;
                System.out.println("> "+diffTotal+"  "+year+"  "+mon+" "+monNew+"  "+monRes);
                if (dataStrDif.length() > 0) dataStrDif.append(',');
                dataStrDif.append(diffTotal);
                
                //System.out.println(year+"  "+mon+"  "+monNew);
            }
        }
        System.out.println(dataStr.toString());
        System.out.println(dataStrDif.toString());
        
        dataLines.add(dataStr.toString());
        dataLines.add(dataStrDif.toString());
        
        createChart(dataLines, eng == null ? "All" : eng);
    }
    
    /**
     * @param ds
     * @param vals
     * @param col
     * @param cat
     */
    protected static void fill(DefaultCategoryDataset ds, double[] vals, String[] col, String cat)
    {
        double total = 0.0;
        int i = 0;
        for (double d : vals)
        {
            total += d;
            ds.addValue(d, col[i++], cat);
        }
    }
    
    /**
     * @param lines
     */
    protected void createChart(final List<String> lines, final String engineer)
    {
        
        int[] mins = new int[lines.size()-1];
        for (int index=1;index<lines.size();index++)
        {
            String line = lines.get(index);
            String[] values =  StringUtils.splitPreserveAllTokens(line, ",");
            int inx = 0;
            while (inx < values.length && values[inx].equals("0"))
            {
                inx++;
            }
            mins[index-1] = inx < values.length ? inx : Integer.MAX_VALUE;
            System.err.println(mins[index-1]);
        }
        
        int startInx = Integer.MAX_VALUE;
        for (int min : mins)
        {
            startInx = Math.min(startInx, min);
            System.out.println(min+"  "+startInx);
        }
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String[] headers = StringUtils.split(lines.get(0), ",");
        int len = headers.length - startInx;
        System.out.println(headers.length+"  "+len);
        List<double[]> valArray = new Vector<double[]>();
        
        for (int i=1;i<lines.size();i++)
        {
            String[] values =  StringUtils.splitPreserveAllTokens(lines.get(i), ",");
            double[] vals   = new double[len];
            int inx = 0;
            double prev = -1;
            for (int j=startInx;j<headers.length;j++)
            {
                if (StringUtils.isNotEmpty(values[j]))
                {
                    prev = Double.parseDouble(values[j]);
                    vals[inx++] = prev;
                } else
                {
                    vals[inx++] = 0.0;
                }
            }
            valArray.add(vals);
        }

        double[] vals = valArray.get(0);
        for (int i=0;i<vals.length;i++)
        {
            dataset.addValue(vals[i], "Bugs", headers[i+startInx]);
        }
        vals = valArray.get(1);
        for (int i=0;i<vals.length;i++)
        {
            dataset.addValue(vals[i], "Resolved", headers[i+startInx]);
        }
        
        vals = valArray.get(2);
        for (int i=0;i<vals.length;i++)
        {
            dataset.addValue(vals[i], "Open", headers[i+startInx]);
        }
        
        JFreeChart chart = ChartFactory.createLineChart("Bugs - "+engineer, "Time", "Bugs", 
                    dataset, PlotOrientation.VERTICAL, 
                    true, true, false);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        //plot.setBackgroundPaint(Color.lightGray);
        //plot.setRangeGridlinePaint(Color.white);

        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAxisLineVisible(true);
        
        CategoryAxis catAxis = plot.getDomainAxis();
        catAxis.setAxisLineVisible(true);
        catAxis.setTickMarksVisible(true);
        
        ChartFrame frame = new ChartFrame("", chart, false);
        frame.setBackground(Color.WHITE);
        frame.setSize(500,500);
        frame.setVisible(true);
    }
    
    /**
     * @param hash
     * @param year
     * @param mon
     */
    @SuppressWarnings("unchecked")
    protected void process(final Hashtable<Integer, Hashtable<Integer, Integer>> hash, final int year, final int mon)
    {
        Hashtable<Integer, Integer> yearHash = hash.get(year);
        if (yearHash == null)
        {
            yearHash = new Hashtable<Integer, Integer>();
            hash.put(year, yearHash);
        }
        
        Hashtable<Integer, Integer> yearHashDif = hashDif.get(year);
        if (yearHashDif == null)
        {
            yearHashDif = new Hashtable<Integer, Integer>();
            hashDif.put(year, yearHashDif);
        }
        
        Integer monTotal = yearHash.get(mon);
        int tot = monTotal == null ? 0 : monTotal;
        tot++;
        yearHash.put(mon, tot);
        yearHashDif.put(mon, 0);
    }
    
    /**
     * @param fileName
     */
    @SuppressWarnings("unchecked")
    protected void parse(final String fileName)
    {
        try
        {
            List<String> lines = FileUtils.readLines(new File("/Users/rod/Downloads/"+fileName));
            for (int i=0;i<lines.size();i++)
            {
                String line = lines.get(i);
                if (StringUtils.contains(line, "show_bug.cgi?id"))
                {
                    String line2 = lines.get(++i);
                    
                    String[] tokens1 = StringUtils.split(line);
                    String[] tokens2 = StringUtils.split(line2);
                    
                    int number = Integer.parseInt(tokens1[0]);
                    Bug bug = new Bug(number, tokens1[2], tokens1[3], tokens2[0], tokens1[4], tokens1[5]);
                    bug.setClosed(!(tokens2[1].equals("ASSI") || tokens2[1].equals("NEW")));
                    bugHash.put(number, bug);
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BugParse.class, ex);
            ex.printStackTrace();
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        new BugParse();
    }
    
    class Bug 
    {
        protected int    number;
        protected String openDate;
        protected String modDate;
        protected String engineer;
        protected String severity;
        protected String priority;
        protected boolean isClosed;
        
        /**
         * @param openDate
         * @param modDate
         * @param engineer
         * @param severity
         */
        public Bug(int number, String openDate, String modDate, String engineer, String severity, String priority)
        {
            super();
            this.number = number;
            this.openDate = openDate;
            this.modDate = modDate;
            this.engineer = engineer;
            this.severity = severity;
            this.priority = priority;
        }
        
        protected int getOpenYear()
        {
            //System.out.println("["+openDate+"]");
            if (openDate.length() != 10) openDate = "2008-10-10";
            return Integer.parseInt(openDate.substring(0, 4));
        }
        
        protected int getOpenMon()
        {
            return Integer.parseInt(openDate.substring(5, 7));
        }
        
        protected int getModYear()
        {
            if (modDate.length() != 10) modDate = "2008-10-10";
            return Integer.parseInt(modDate.substring(0, 4));
        }
        
        protected int getModMon()
        {
            return Integer.parseInt(modDate.substring(5, 7));
        }
        
        /**
         * @return the number
         */
        public int getNumber()
        {
            return number;
        }

        /**
         * @return the openDate
         */
        public String getOpenDate()
        {
            return openDate;
        }
        /**
         * @return the modDate
         */
        public String getModDate()
        {
            return modDate;
        }
        /**
         * @return the engineer
         */
        public String getEngineer()
        {
            return engineer;
        }
        /**
         * @return the severity
         */
        public String getSeverity()
        {
            return severity;
        }

        /**
         * @return the isClosed
         */
        public boolean isClosed()
        {
            return isClosed;
        }

        /**
         * @param isClosed the isClosed to set
         */
        public void setClosed(boolean isClosed)
        {
            this.isClosed = isClosed;
        }

        /**
         * @return the priority
         */
        public String getPriority()
        {
            return priority;
        }
        
        
    }

}
