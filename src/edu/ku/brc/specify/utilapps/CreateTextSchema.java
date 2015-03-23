/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    private static Locale currLang  = new Locale("en");
    
    private static byte SCHEMATYPE = SpLocaleContainer.CORE_SCHEMA;
    
    protected static String schemaOutputHTMLName = "SpecifySchema%s.html";
    protected static String NBSP                 = "&nbsp;";
    protected static String VERSION              =  "<!-- Version -->";
    
    protected static DBTableIdMgr tableMgr;
    

    protected String basePath = "src/edu/ku/brc/specify/utilapps/";
    
    protected PrintWriter po   = null;
    
    /**
     * 
     */
    public CreateTextSchema()
    {
        System.setProperty(AppContextMgr.factoryName, "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        
        if (SCHEMATYPE == SpLocaleContainer.CORE_SCHEMA)
        {
            tableMgr = DBTableIdMgr.getInstance();
        } else
        {
            tableMgr = new DBTableIdMgr(false);
            tableMgr.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml"))); //$NON-NLS-1$
        }
    }
    
    /**
     * @param field
     * @param includeIndexCol
     */
    protected void processRel(final DBRelationshipInfo field, final boolean includeIndexCol)
    {
        String  clsName = field.getClassName();
        int     inx     = clsName.lastIndexOf('.');
        clsName = inx > -1 ? clsName.substring(inx+1) : clsName;
        clsName = clsName.toLowerCase();
        
        DBTableInfo toTable =tableMgr.getInfoByTableName(clsName);
        if (toTable != null)
        {
            clsName = toTable.getTitle();
        }
                
        po.write("<tr>\n");
        po.write("<td align=\"center\" colspan=\"2\">\n");
        po.write(field.getType().toString());
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\""+(includeIndexCol ? 2 : 1)+"\">\n");
        po.write(field.getTitle());
        po.write("</td>\n");
        po.write("<td align=\"center\" colspan=\"1\">\n");
        po.write(clsName);
        po.write("</td>\n");
        po.write("</tr>\n");
    }
    
    /**
     * @param field
     * @param isID
     */
    protected void processField(final DBFieldInfo field, final boolean isID, final boolean includeIndexCol)
    {
        String  type = field.getType();
        int     inx  = type.lastIndexOf('.');
        type = inx > -1 ? type.substring(inx+1) : type;
        
        po.write("<tr>\n");
        po.write("<td align=\"center\">\n");
        po.write(field.getTitle());
        
        po.write("</td>\n");
        po.write("<td align=\"center\">\n");
        po.write(type.equals("text") ? "Memo" : type);
        po.write("</td>\n");
        po.write("        <td align=\"center\">\n");
        po.write(field.getLength() > -1 ? Integer.toString(field.getLength()) : NBSP);
        po.write("        </td>\n");
        
        if (includeIndexCol)
        {
            po.write("        <td align=\"center\">\n");
            po.write(isID ? field.getTitle() : field.getName());
            po.write("        </td>\n");
        }
        if (field.getDescription() != null && field.getDescription().equals(field.getName()))
        {
            field.setDescription(null);
        }
        po.write("<td align=\"left\">\n");
        po.write(isID ? UIRegistry.getResourceString("PrimaryKey") : getStr(field.getDescription()));
        po.write("</td>\n");
        po.write("</tr>\n");

    }
    
    /**
     * @param tables
     */
    protected void makeIndex(final Vector<DBTableInfo> tables)
    {
        for (DBTableInfo tn : tables)
        {
            DBTableInfo tblInfo   =tableMgr.getInfoByTableName(tn.getName());
            po.write("<LI> <a href=\"#"+tn.getName()+"\">"+tblInfo.getTitle()+"</a></LI>\n");
        }
    }
    
    /**
     * @param tables
     */
    protected void makeTableDesc(final Vector<DBTableInfo> tables)
    {
        for (DBTableInfo tn : tables)
        {
            DBTableInfo tblInfo   =tableMgr.getInfoByTableName(tn.getName());
            po.write("<LI> <a href=\"#"+tn.getName()+"\">"+tblInfo.getTitle()+"</a></LI>\n");
        }
    }
    
    /**
     * @param tables
     */
    @SuppressWarnings("unchecked")
    protected void makeTableIndexes(final Vector<DBTableInfo> tables)
    {
        
        po.write("    <tr>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("IndexName")+"</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("ColumnName")+"</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("Table")+"</td>\n");
        po.write("    </tr>\n");
        
        for (DBTableInfo tn : tables)
        {
            System.out.println("Indexing "+tn.getName());
            
            Hashtable<String, String> colToTitle = new Hashtable<String, String>();
            for (DBFieldInfo fi : tn.getFields())
            {
                colToTitle.put(fi.getColumn(), StringUtils.isNotEmpty(fi.getTitle()) ? fi.getTitle() : fi.getColumn());
            }

            if (tn.getTableIndexMap() != null)
            {
                Vector<String> keys = new Vector<String>(tn.getTableIndexMap().keySet());
                Collections.sort(keys);
                for (String key : keys)
                {
                    String title = colToTitle.get(key);
                    if (StringUtils.isNotEmpty(title))
                    {
                        title = key;
                    }
                    
                    po.write("<tr>\n");
                    po.write("<td align=\"center\">");
                    po.write(tn.getTableIndexMap().get(key));
                    po.write("</td>\n");
                    po.write("<td align=\"center\">");
                    po.write(key);
                    po.write("</td>\n");
                    po.write("<td align=\"center\">");
                    //if (!tableName.equals(prevTbl))
                    //{
                        po.write("    <a href=\"#"+tn.getName()+"\">");
                        po.write(tn.getTitle());
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
    }
    
    private String getStr(final String str)
    {
        return StringUtils.isNotEmpty(str) ? str : NBSP;
    }
    
    /**
     * @param tables
     */
    protected void processDescs(final Vector<DBTableInfo> tables)
    {
        
        po.write("    <tr>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("Table")+"</td>\n");
        po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("Description")+"</td>\n");
        po.write("    </tr>\n");
        
        for (DBTableInfo tn : tables)
        {
            po.write("<tr>\n");
            po.write("<td nowrap=\"true\" align=\"left\">");
            po.write("    <a href=\"#"+tn.getName()+"\">");
            po.write(tn.getTitle());
            po.write("</a>");
            po.write("</td>\n");
            po.write("<td align=\"left\">");
            po.write(getStr(tn.getDescription()));
            po.write("</td>\n");
            po.write("</tr>\n");

        }
        
    }
    
    /**
     * @param tables
     */
    @SuppressWarnings({ "unchecked" })
    protected void processTables(final Vector<DBTableInfo> tables, final boolean includeIndexCol)
    {
        for (DBTableInfo tn : tables)
        {
            System.out.println("Processing "+tn.getName());
            
            po.write("<a name=\""+tn.getName()+"\"></a>\n");
            po.write("<table class=\"tbl\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\"\n");
            po.write("    width=\"75%\">\n");
    
            po.write("    <tr>\n");
            po.write("        <td colspan=\"5\" class=\"hdbig\">\n");
            po.write("            "+tn.getTitle()+"\n");
    
            if (StringUtils.isNotEmpty(tn.getDescription()))
            {
                po.write("            <br />\n");
                po.write("            <div class=\"desc\">\n");
                po.write("                "+tn.getDescription()+"\n");
                po.write("            </div>\n");
            }
            po.write("        </td>\n");
            po.write("    </tr>\n");
            po.write("    <tr>\n");
            po.write("        <td class=\"hd\">"+UIRegistry.getResourceString("Field")+"</td>\n");
            po.write("        <td class=\"hd\">"+UIRegistry.getResourceString("Type")+"</td>\n");
            po.write("        <td class=\"hd\">"+UIRegistry.getResourceString("Length")+"</td>\n");
            
            if (includeIndexCol)
            {
                po.write("        <td class=\"hd\">"+UIRegistry.getResourceString("IndexName")+"</td>\n");
            }
            po.write("        <td class=\"hd\">"+UIRegistry.getResourceString("Descriptions")+"</td>\n");
            po.write("    </tr>\n");
    
            //DBFieldInfo primaryIndexField = tn.getFieldByColumnName(tn.getIdColumnName());
            DBFieldInfo primaryField = new DBFieldInfo(tn, tn.getIdColumnName(), tn.getIdColumnName(), tn.getIdType(), -1, true, false, true, true, false, null);
            processField(primaryField, true, includeIndexCol);
    
            for (DBFieldInfo fn : tn.getFields())
            {
                processField(fn, false, includeIndexCol);
            }
            po.write("    <tr>\n");
            po.write("        <td colspan=\"5\" class=\"subhead\">"+UIRegistry.getResourceString("Relationships")+"</td>\n");
            po.write("    </tr>\n");
            po.write("    <tr>\n");
            po.write("        <td class=\"hd\" colspan=\"2\">"+UIRegistry.getResourceString("Type")+"</td>\n");
            po.write("        <td class=\"hd\" colspan=\""+(includeIndexCol ? 2 : 1)+"\">"+UIRegistry.getResourceString("Name")+"</td>\n");
            po.write("        <td class=\"hd\" colspan=\"1\">"+UIRegistry.getResourceString("ToTable")+"</td>\n");
            po.write("    </tr>\n");
    
            for (DBRelationshipInfo rn : tn.getRelationships())
            {
                processRel(rn, includeIndexCol);
            }
    
            po.write("</table>\n");
            po.write("<br />\n");
            po.write("<br />\n");
        }
    }
    
    private String adjustFileNameForLocale(final String fileName)
    {
        if (currLang.getLanguage().equals("en"))
        {
            return String.format(fileName, "");
        } 
        
        return String.format(fileName, "_" + currLang.getLanguage());
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void process(final String versionNum)
    {
        try
        {
            File oFile = new File(adjustFileNameForLocale(schemaOutputHTMLName));
            po = new PrintWriter(oFile, "UTF8");
            
            System.out.println("Opening "+XMLHelper.getConfigDirPath(DatamodelHelper.getOutputFileName()));
            System.out.println("Writing "+oFile.getAbsolutePath());
            
            
            Vector<DBTableInfo> tables =tableMgr.getTables();
            Collections.sort(tables);
            
            SchemaLocalizerXMLHelper schemaXMLHelper = new SchemaLocalizerXMLHelper(SCHEMATYPE, tableMgr);
            schemaXMLHelper.load(true);
            schemaXMLHelper.setTitlesIntoSchema();
            
            //SchemaI18NService.getInstance().loadWithLocale(SCHEMATYPE, disciplineId,tableMgr, Locale.getDefault());
            
            List<String> lines = FileUtils.readLines(new File(basePath+ adjustFileNameForLocale("SpecifySchemaTemplate%s.html")));
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
            
            System.out.println(oFile.getAbsolutePath());
            
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
                
                Locale.setDefault(currLang);
                
                try
                {
                    ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
                    
                } catch (MissingResourceException ex)
                {
                    Locale.setDefault(Locale.ENGLISH);
                    UIRegistry.setResourceLocale(Locale.ENGLISH);
                }
                
                CreateTextSchema cts = new CreateTextSchema();
                cts.process(schemaVersion);
                
                //File file = XMLHelper.getConfigDir(specifyDescFileName);
                //file.delete();
    
                JOptionPane.showMessageDialog(null, "Done");
          }
        });
    }
}
