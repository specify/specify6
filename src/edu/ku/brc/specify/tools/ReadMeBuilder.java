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
package edu.ku.brc.specify.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 8, 2009
 *
 */
public class ReadMeBuilder
{

    protected Vector<BugInfo> list = new Vector<BugInfo>();
    
    public ReadMeBuilder()
    {
        super();
    }

    
    public void process()
    {
        JFileChooser chooser    = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (path != null)
            {
                try
                {
                    Element root = XMLHelper.readFileToDOM4J(new File(path));
                    if (root != null)
                    {
                        for (Object bugObj : root.selectNodes("bug"))
                        {
                            Element node = (Element)bugObj;
                            
                            String bugId = ((Node)node.selectObject("bug_id")).getText();
                            list.add(new BugInfo(((Node)node.selectObject("short_desc")).getText(), ((Node)node.selectObject("delta_ts")).getText(), bugId));
                            System.out.println(bugId);
                        }
                    }
                    
                    Collections.sort(list);
                    
                    PrintWriter pw = new PrintWriter(new File("readme.html"));
                    pw.append("</body>\n</html>\n");
                    pw.append("<html>\n");
                    pw.append("<head>\n");
                    pw.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
                    pw.append("<title>Release Notes</title>\n");
                    pw.append("</head>\n");
    
                    pw.append("<body>\n");
                    pw.append("<h3>Release Notes<br />\n");
                    pw.append("  Specify 6.0.xx<br />\n");
                    pw.append("  08 July 2009<br />\n");
                    pw.append("  Specify Software Project</h3>\n");
                    pw.append("<ol>\n");
                    
                    for (BugInfo bi : list)
                    {
                        pw.append("<li>");
                        pw.append(bi.getText());
                        pw.append("&nbsp;&nbsp;");
                        pw.append(bi.getDate());
                        pw.append("&nbsp;&nbsp;(");
                        pw.append(bi.getNum());
                        pw.append(").</li>\n");
                    }
                    pw.append("</ol>\n");
                    for (int i=0;i<4;i++)
                    {
                        pw.append("<BR>\n");
                    }
                    pw.append("</body>\n</html>\n");
                    
                    pw.close();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                UIRegistry.showLocalizedMsg("Done.");
                System.exit(0);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
          public void run()
            {
                ReadMeBuilder rmb = new ReadMeBuilder();
                rmb.process();
            }
        });

    }

    
    class BugInfo implements Comparable<BugInfo>
    {
        protected String text;
        protected String date;
        protected String num;
        
        public BugInfo(String text, String date, String num)
        {
            super();
            this.text = text;
            this.date = date;
            this.num = num;
        }
        /**
         * @return the text
         */
        public String getText()
        {
            return text;
        }
        /**
         * @return the date
         */
        public String getDate()
        {
            return date;
        }
        /**
         * @return the num
         */
        public String getNum()
        {
            return num;
        }
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(BugInfo o)
        {
            int rv = date.compareTo(o.date);
            if (rv == 0)
            {
                rv = num.compareTo(o.num);
            }
            return rv;
        }
    }
}
