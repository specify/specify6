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
package edu.ku.brc.specify.tools.datamodelgenerator;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.getConfigDir;
import static edu.ku.brc.helpers.XMLHelper.readFileToDOM4J;

import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 13, 2007
 *
 */
public class DataModelToCSV
{
    protected PrintWriter pw = null;
    protected char sep = '\t';
    
    public DataModelToCSV()
    {
        
    }
    
    protected String getLast(final String str)
    {
        if (str.indexOf('.') > -1)
        {
            return StringUtils.substringAfterLast(str, ".");
        }
        return str;
    }
    
    protected String fixText(final String text)
    {
        return StringUtils.replace(text, "&apos;", "\'");
    }
    
    protected void processId(final Element id)
    {
        // <id column="AccessionID" name="accessionId" type="java.lang.Integer"/>
        Element nameElement = (Element)id.selectSingleNode("nameDesc");
        String name = nameElement != null ? nameElement.getText() : getAttr(id, "column", null);
        
        System.out.println(name);
        
        Element descElement = (Element)id.selectSingleNode("desc");
        String  desc        = descElement != null ? fixText(descElement.getText()) : "";
        pw.print(name);
        pw.print(sep);
        pw.print(desc);
        pw.print(sep);
        pw.print(getLast(getAttr(id, "type", "")));
        pw.print(sep);
        pw.print(getAttr(id, "column", ""));
        pw.println();
    }
     
    protected void processField(final Element field)
    {
        Element nameElement = (Element)field.selectSingleNode("nameDesc");
        String name = nameElement != null ? nameElement.getText() : getAttr(field, "column", null);
        
        System.out.println(name);
        
        Element descElement = (Element)field.selectSingleNode("desc");
        String  desc        = descElement != null ? fixText(descElement.getText()) : "";
        pw.print(name);
        pw.print(sep);
        pw.print(desc);
        pw.print(sep);
        pw.print(getLast(getAttr(field, "type", null)));
        pw.print(sep);
        pw.print(getLast(getAttr(field, "column", null)));
        pw.println();
        
    }
     
    protected void processRelationship(final Element rel)
    {
        Element nameElement = (Element)rel.selectSingleNode("nameDesc");
        String name = nameElement != null ? nameElement.getText() : getLast(getAttr(rel, "classname", null));
        System.out.println(name);
        Element descElement = (Element)rel.selectSingleNode("desc");
        String desc = descElement != null ? fixText(descElement.getText()) : "";
        String type = getAttr(rel, "type", null);
        pw.print(name);
        pw.print(sep);
        pw.print(desc);
        pw.print(sep);
        pw.print(type);
        pw.print(sep);
        pw.print(getAttr(rel, "relationshipname", null));
        pw.println();
    }
     
    
    protected void processTables(final Element table)
    {
        pw.println(""+sep+sep+sep+sep);
        pw.println("TABLE"+sep+sep+sep);
        Element nameElement = (Element)table.selectSingleNode("nameDesc");
        String name = nameElement != null ? nameElement.getText() : StringUtils.substringAfterLast(getAttr(table, "classname", null), ".");
        System.out.println(name);
        Element descElement = (Element)table.selectSingleNode("desc");
        String desc = descElement != null ? fixText(descElement.getText()) : "";
        pw.print(name);
        pw.print(sep);
        pw.print(desc);
        pw.print(sep);
        pw.print(sep);
        pw.println();
        
        pw.println("FIELDS"+sep+sep+sep);
        processId((Element)table.selectSingleNode("id"));
        
        for (Iterator<?> iter = table.selectNodes("field").iterator();iter.hasNext();)
        {
            processField((Element)iter.next());
        }
        
        pw.println("RELATIONSHIPS"+sep+sep+sep);
        for (Iterator<?> iter = table.selectNodes("relationship").iterator();iter.hasNext();)
        {
            processRelationship((Element)iter.next());
        }
    }
    
    public void process()
    {
        try
        {
            pw = new PrintWriter(getConfigDir("specify_datamodel.csv"));
            
            pw.println("Name"+sep+"Description"+sep+"Column"+sep+"Type");

            Element root = readFileToDOM4J(getConfigDir("specify_datamodel.xml"));
            
            for (Iterator<?> iter = root.selectNodes("/database/table").iterator();iter.hasNext();)
            {
                processTables((Element)iter.next());
            }
            pw.close();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataModelToCSV.class, ex);
            ex.printStackTrace();
        } 
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        DataModelToCSV toCSV = new DataModelToCSV();
        toCSV.process();
        System.out.println("Done.");
    }

}
