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
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tools.datamodelgenerator.DatamodelGenerator;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.DatamodelHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 7, 2008
 *
 */
public class CreateTextSchema
{
    protected static String specifyDescFileName  = "specify_desc_datamodel.xml";
    protected static String schemaOutputHTMLName = "SpecifySchema.html";
    protected static String NBSP                 = "&nbsp;";
    protected static String VERSION              =  "<!-- Version -->";
    

    protected String basePath = "src/edu/ku/brc/specify/utilapps/";
    
    protected PrintWriter po   = null;
    protected String      lang = "en";
    
    /**
     * 
     */
    public CreateTextSchema()
    {
        
    }
    
    protected void processRel(final Element field, final boolean includeIndexCol)
    {
        Element descE    = (Element)field.selectSingleNode("nameDesc");
        String nameDesc  = descE != null ? descE.getStringValue() : "XXX";
        String type      = getAttr(field, "type", NBSP);
        String classname = getAttr(field, "classname", "&nbsp;&nbsp;");
        if (classname.indexOf(".") > -1)
        {
            classname = StringUtils.substringAfterLast(classname, ".");
        }
        po.write("<tr>\n");
        po.write("<td align=\"center\" colspan=\"2\">\n");
        po.write(type);
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\""+(includeIndexCol ? 2 : 1)+"\">\n");
        po.write(nameDesc);
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\"1\">\n");
        po.write(classname);
        po.write("</td>\n");
        po.write("</tr>\n");
    }
    
    /**
     * @param field
     * @param isID
     */
    protected void processField(final Element field, final boolean isID, final boolean includeIndexCol)
    {
        String column = getAttr(field, "column", NBSP);
        //String name = getAttr(field, "name", NBSP);
        String type = getAttr(field, "type", NBSP);
        String length = getAttr(field, "length", "&nbsp;&nbsp;");
        //String updatable = getAttr(field, "updatable", NBSP);
        //String required = getAttr(field, "required", NBSP);
        //String unique = getAttr(field, "unique", NBSP);
        //String indexed = getAttr(field, "indexed", NBSP);
        String indexName = getAttr(field, "indexName", NBSP);
        
        Element descE   = (Element)field.selectSingleNode("desc");
        String desc     = descE != null ? descE.getStringValue() : NBSP;
        
        descE   = (Element)field.selectSingleNode("nameDesc");
        String nameDesc     = descE != null ? descE.getStringValue() : column;
        
        if (type.indexOf(".") > -1)
        {
            type = StringUtils.substringAfterLast(type, ".");
        }

        /*
         * column="Text3" name="text3" type="java.lang.String" length="300" updatable="true" required="false" unique="false" indexed="false"
         */
        
        po.write("<tr>\n");
        po.write("<td align=\"center\">\n");
        po.write(nameDesc);
        
        po.write("</td>\n");
        po.write("<td align=\"center\">\n");
        po.write(type.equals("text") ? "Memo" : type);
        po.write("</td>\n");
        po.write("        <td align=\"center\">\n");
        po.write(length);
        po.write("        </td>\n");
        
        if (includeIndexCol)
        {
            po.write("        <td align=\"center\">\n");
            po.write(isID ? nameDesc : indexName);
            po.write("        </td>\n");
        }
        po.write("<td align=\"left\">\n");
        po.write(isID ? "Primary Key" : desc);
        po.write("</td>\n");
        po.write("</tr>\n");

    }
    
    /**
     * @param tables
     */
    protected void makeIndex(final Vector<DOMNode> tables)
    {
        for (DOMNode tn : tables)
        {
            String tableName = getAttr(tn.node, "table", "");
            String nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            po.write("<LI> <a href=\"#"+tableName+"\">"+nameDesc+"</a></LI>\n");
        }
    }
    
    /**
     * @param tables
     */
    protected void makeTableDesc(final Vector<DOMNode> tables)
    {
        for (DOMNode tn : tables)
        {
            String tableName = getAttr(tn.node, "table", "");
            String nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            po.write("<LI> <a href=\"#"+tableName+"\">"+nameDesc+"</a></LI>\n");
        }
    }
    
    /**
     * @param tables
     */
    @SuppressWarnings("unchecked")
    protected void makeTableIndexes(final Vector<DOMNode> tables)
    {
        
        po.write("    <tr>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">Index Name</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">Column Name</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">Table</td>\n");
        po.write("    </tr>\n");
        
        for (DOMNode tn : tables)
        {
            String nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            String tableName = getAttr(tn.node, "table", "");
            System.out.println("Indexing "+tableName);
            
            Hashtable<String, String> colToTitle = new Hashtable<String, String>();
            for (Element field : (List<Element>)tn.node.selectNodes("field"))
            {
                String column = getAttr(field, "column", NBSP);
                Element descE   = (Element)field.selectSingleNode("nameDesc");
                colToTitle.put(column, descE != null ? descE.getStringValue() : column);
            }

            Vector<DOMNode> tableIndexes = new Vector<DOMNode>();
            for (Element field : (List<Element>)tn.node.selectNodes("tableindex"))
            {
                DOMNode fn = new DOMNode();
                fn.node     = field;
                fn.domName = getAttr(tn.node, "columnNames", "");
                tableIndexes.add(fn);
            }
            Collections.sort(tableIndexes);

            //String prevTbl = "";
            for (DOMNode fn : tableIndexes)
            {
                String columnNames = getAttr(fn.node, "columnNames", "");
                
                String title = colToTitle.get(columnNames);
                if (StringUtils.isNotEmpty(title))
                {
                    title = columnNames;
                }
                
                String indexName = getAttr(fn.node, "indexName", "");
                
                po.write("<tr>\n");
                po.write("<td align=\"center\">");
                po.write(indexName);
                po.write("</td>\n");
                po.write("<td align=\"center\">");
                po.write(columnNames);
                po.write("</td>\n");
                po.write("<td align=\"center\">");
                //if (!tableName.equals(prevTbl))
                //{
                    po.write("    <a href=\"#"+tableName+"\">");
                    po.write(nameDesc);
                    po.write("</a>");
                    //prevTbl = tableName;
                //} else
                //{
                //    po.write(NBSP);
                //}
                po.write("</td>\n");
                po.write("</tr>\n");
            }
            
        }
    }
    
    /**
     * @param tables
     */
    protected void processDescs(final Vector<DOMNode> tables)
    {
        
        po.write("    <tr>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">Table</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">Description</td>\n");
        po.write("    </tr>\n");
        
        for (DOMNode tn : tables)
        {
            String tableName = getAttr(tn.node, "table", "");
            
            Element descE    = (Element)tn.node.selectSingleNode("desc");
            String  desc     = descE != null ? descE.getStringValue() : NBSP;
            String  nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            
            po.write("<tr>\n");
            po.write("<td nowrap=\"true\" align=\"left\">");
            po.write("    <a href=\"#"+tableName+"\">");
            po.write(nameDesc);
            po.write("</a>");
            po.write("</td>\n");
            po.write("<td align=\"left\">");
            po.write(desc);
            po.write("</td>\n");
            po.write("</tr>\n");

        }
        
    }
    
    /**
     * @param tables
     */
    @SuppressWarnings({ "unchecked" })
    protected void processTables(final Vector<DOMNode> tables, final boolean includeIndexCol)
    {
        for (DOMNode tn : tables)
        {
            //String classname = getAttr(tn.node, "classname", "");
            String tableName = getAttr(tn.node, "table", "");
            
            System.out.println("Processing "+tableName);
            
            Element descE    = (Element)tn.node.selectSingleNode("desc");
            String  desc     = descE != null ? descE.getStringValue() : "";
            String  nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            
            
            po.write("<a name=\""+tableName+"\"></a>\n");
            po.write("<table class=\"tbl\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\"\n");
            po.write("    width=\"75%\">\n");
    
            po.write("    <tr>\n");
            po.write("        <td colspan=\"5\" class=\"hdbig\">\n");
            po.write("            "+nameDesc+"\n");
    
            if (StringUtils.isNotEmpty(desc))
            {
                po.write("            <br />\n");
                po.write("            <div class=\"desc\">\n");
                po.write("                "+desc+"\n");
                po.write("            </div>\n");
            }
            po.write("        </td>\n");
            po.write("    </tr>\n");
            po.write("    <tr>\n");
            po.write("        <td class=\"hd\">Field</td>\n");
            po.write("        <td class=\"hd\">Type</td>\n");
            po.write("        <td class=\"hd\">Length</td>\n");
            
            if (includeIndexCol)
            {
                po.write("        <td class=\"hd\">Index Name</td>\n");
            }
            po.write("        <td class=\"hd\">Description</td>\n");
            po.write("    </tr>\n");
    
            processField((Element)tn.node.selectObject("id"), true, includeIndexCol);
    
            Vector<DOMNode> fields = new Vector<DOMNode>();
            for (Element field : (List<Element>)tn.node.selectNodes("field"))
            {
                DOMNode fn = new DOMNode();
                fn.node = field;
                fn.domName = ((Element)field.selectSingleNode("nameDesc")).getStringValue();
                fields.add(fn);
            }
            Collections.sort(fields);
            for (DOMNode fn : fields)
            {
                processField(fn.node, false, includeIndexCol);
            }
            po.write("    <tr>\n");
            po.write("        <td colspan=\"5\" class=\"subhead\">Relationships</td>\n");
            po.write("    </tr>\n");
            po.write("    <tr>\n");
            po.write("        <td class=\"hd\" colspan=\"2\">Type</td>\n");
            po.write("        <td class=\"hd\" colspan=\""+(includeIndexCol ? 2 : 1)+"\">Name</td>\n");
            po.write("        <td class=\"hd\" colspan=\"1\">To Table</td>\n");
            po.write("    </tr>\n");
    
            Vector<DOMNode> rels = new Vector<DOMNode>();
            for (Element rel : (List<Element>)tn.node.selectNodes("relationship"))
            {
                DOMNode rn = new DOMNode();
                rn.node = rel;
                rn.domName = ((Element)rel.selectSingleNode("nameDesc")).getStringValue();
                rels.add(rn);
            }
            Collections.sort(rels);
            for (DOMNode rn : rels)
            {
                processRel(rn.node, includeIndexCol);
            }
    
            po.write("</table>\n");
            po.write("<br />\n");
            po.write("<br />\n");
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void process(final String versionNum)
    {
        try
        {
            po = new PrintWriter(new File(schemaOutputHTMLName));
            
            System.out.println("Opening "+XMLHelper.getConfigDirPath(DatamodelHelper.getOutputFileName()));
            
            Element root = XMLHelper.readDOMFromConfigDir(DatamodelHelper.getOutputFileName());
            if (root == null)
            {
                System.err.println("File ["+XMLHelper.getConfigDirPath(DatamodelHelper.getOutputFileName())+"] couldn't be read.");
            }
            Vector<DOMNode> tables = new Vector<DOMNode>();
            for (Element table : (List<Element>)root.selectNodes("/database/table"))
            {
                DOMNode tn = new DOMNode();
                tn.node = table;
                tn.domName = ((Element)table.selectSingleNode("nameDesc")).getStringValue();
                tables.add(tn);
            }
            Collections.sort(tables);
            
            List<String> lines = FileUtils.readLines(new File(basePath+"SpecifySchemaTemplate.html"));
            for (String line : lines)
            {
                if (StringUtils.contains(line, "<!-- Table Defs -->"))
                {
                    processTables(tables, false);
                    
                } else if (StringUtils.contains(line, "<!-- Table Contents -->"))
                {
                    makeIndex(tables);
                    
                } else if (StringUtils.contains(line, "<!-- Table Descs -->"))
                {
                    processDescs(tables);
                    
                } else if (StringUtils.contains(line, "<!-- Date -->"))
                {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                    line = StringUtils.replace(line, "<!-- Date -->", sdf.format(cal.getTime()));
                    
                } else if (StringUtils.contains(line, "<!-- Indexes -->"))
                {
                    makeTableIndexes(tables);
                    
                } else if (StringUtils.contains(line, VERSION))
                {
                    line = StringUtils.replace(line, VERSION, versionNum);
                }
                po.write(line);
                po.write("\n");
            }
            
            po.flush();
            po.close();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CreateTextSchema.class, ex);
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DatamodelGenerator datamodelWriter = new DatamodelGenerator(true);
        datamodelWriter.process(specifyDescFileName);
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
          public void run()
          {
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                }
                
                String schemaVersion = JOptionPane.showInputDialog("Enter Schema Version:"); 
                
                CreateTextSchema cts = new CreateTextSchema();
                cts.process(schemaVersion);
                
                File file = XMLHelper.getConfigDir(specifyDescFileName);
                file.delete();
    
                JOptionPane.showMessageDialog(null, "Done");
          }
        });
    }
    
    //------------------------------------------------------------------
    //
    //------------------------------------------------------------------
    private class DOMNode implements Comparable<DOMNode>
    {
        public Element node;
        public String  domName;
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(DOMNode t)
        {
            return domName.compareTo(t.domName);
        }
        
    }

}
