/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tools.datamodelgenerator.DatamodelGenerator;
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
    
    
    protected String basePath = "src/edu/ku/brc/specify/utilapps/";
    
    protected PrintWriter po   = null;
    protected String      lang = "en";
    
    /**
     * 
     */
    public CreateTextSchema()
    {
        
    }
    
    protected void processRel(final Element field)
    {
        Element descE    = (Element)field.selectSingleNode("nameDesc");
        String nameDesc  = descE != null ? descE.getStringValue() : "XXX";
        String type      = getAttr(field, "type", "&nbsp;");
        String classname = getAttr(field, "classname", "&nbsp;&nbsp;");
        if (classname.indexOf(".") > -1)
        {
            classname = StringUtils.substringAfterLast(classname, ".");
        }
        po.write("<tr>\n");
        po.write("<td align=\"center\" colspan=\"2\">\n");
        po.write(type);
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\"2\">\n");
        po.write(nameDesc);
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\"1\">\n");
        po.write(classname);
        po.write("</td>\n");
        po.write("</tr>\n");
    }
    
    protected void processField(final Element field, final boolean isID)
    {
        String column = getAttr(field, "column", "&nbsp;");
        //String name = getAttr(field, "name", "&nbsp;");
        String type = getAttr(field, "type", "&nbsp;");
        String length = getAttr(field, "length", "&nbsp;&nbsp;");
        //String updatable = getAttr(field, "updatable", "&nbsp;");
        //String required = getAttr(field, "required", "&nbsp;");
        //String unique = getAttr(field, "unique", "&nbsp;");
        //String indexed = getAttr(field, "indexed", "&nbsp;");
        String indexName = getAttr(field, "indexName", "&nbsp;");
        
        Element descE   = (Element)field.selectSingleNode("desc");
        String desc     = descE != null ? descE.getStringValue() : "&nbsp;";
        
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
        
        po.write("        <td align=\"center\">\n");
        po.write(isID ? nameDesc : indexName);
        po.write("        </td>\n");
        po.write("<td align=\"left\">\n");
        po.write(isID ? "Primary Key" : desc);
        po.write("</td>\n");
        po.write("</tr>\n");

    }
    
    protected void makeIndex(final Vector<DOMNode> tables)
    {
        for (DOMNode tn : tables)
        {
            String tableName = getAttr(tn.node, "table", "");
            String nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            po.write("<LI> <a href=\"#"+tableName+"\">"+nameDesc+"</a></LI>\n");
        }
    }
    
    protected void makeTableDesc(final Vector<DOMNode> tables)
    {
        for (DOMNode tn : tables)
        {
            String tableName = getAttr(tn.node, "table", "");
            String nameDesc = ((Element)tn.node.selectSingleNode("nameDesc")).getStringValue();
            po.write("<LI> <a href=\"#"+tableName+"\">"+nameDesc+"</a></LI>\n");
        }
    }
    
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
                String column = getAttr(field, "column", "&nbsp;");
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
                //    po.write("&nbsp;");
                //}
                po.write("</td>\n");
                po.write("</tr>\n");
            }
            
        }
    }
    
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
            String  desc     = descE != null ? descE.getStringValue() : "&nbsp;";
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
    
    @SuppressWarnings({ "unchecked", "unchecked" })
    protected void processTables(final Vector<DOMNode> tables)
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
            po.write("        <td class=\"hd\">Index Name</td>\n");
            po.write("        <td class=\"hd\">Description</td>\n");
            po.write("    </tr>\n");
    
            processField((Element)tn.node.selectObject("id"), true);
    
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
                processField(fn.node, false);
            }
            po.write("    <tr>\n");
            po.write("        <td colspan=\"5\" class=\"subhead\">Relationships</td>\n");
            po.write("    </tr>\n");
            po.write("    <tr>\n");
            po.write("        <td class=\"hd\" colspan=\"2\">Type</td>\n");
            po.write("        <td class=\"hd\" colspan=\"2\">Name</td>\n");
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
                processRel(rn.node);
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
    protected void process()
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
            
            List<String> lines = (List<String>)FileUtils.readLines(new File(basePath+"SpecifySchemaTemplate.html"));
            for (String line : lines)
            {
                if (StringUtils.contains(line, "<!-- Table Defs -->"))
                {
                    processTables(tables);
                    
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
                }
                po.write(line);
                po.write("\n");
            }
            
            po.flush();
            po.close();
            
        } catch (Exception ex)
        {
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
        
        CreateTextSchema cts = new CreateTextSchema();
        cts.process();
        
        File file = XMLHelper.getConfigDir(specifyDescFileName);
        file.delete();

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
