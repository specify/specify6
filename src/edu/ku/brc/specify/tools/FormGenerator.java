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
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.SpUIAltView;
import edu.ku.brc.specify.datamodel.SpUICell;
import edu.ku.brc.specify.datamodel.SpUIRow;
import edu.ku.brc.specify.datamodel.SpUIView;
import edu.ku.brc.specify.datamodel.SpUIViewDef;
import edu.ku.brc.specify.datamodel.SpUIViewSet;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 14, 2008
 *
 */
public class FormGenerator
{

    /**
     * 
     */
    public FormGenerator()
    {
        super();
    }
    
    public void generateForms()
    {
        SpViewSetObj viewSetObj = new SpViewSetObj();
        viewSetObj.initialize();
        viewSetObj.setName("All Forms");
        
        SpUIViewSet viewSet = new SpUIViewSet();
        viewSet.initialize();
        viewSet.setName("All Forms");
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            String tName = ti.getShortClassName();
            
            if (tName.startsWith("Sp") || 
                tName.startsWith("User"))
            {
                continue;
            }
            
            SpUIView view = new SpUIView();
            view.initialize();
            view.setBusinessRulesClassName(ti.getBusinessRuleName());
            view.setName(tName);
            view.setSpViewSet(viewSet);
            viewSet.getSpViews().add(view);
            
            SpUIAltView altViewView = new SpUIAltView();
            altViewView.initialize();
            altViewView.setName(tName+" View");
            altViewView.setDefault(true);
            altViewView.setLabel("View");
            altViewView.setMode(AltViewIFace.CreationMode.VIEW);
           
            SpUIAltView altViewEdit = new SpUIAltView();
            altViewEdit.initialize();
            altViewEdit.setName(tName+" Edit");
            altViewEdit.setDefault(true);
            altViewEdit.setLabel("Edit");
            altViewEdit.setMode(AltViewIFace.CreationMode.EDIT);
           
            view.getSpAltViews().add(altViewView);
            view.getSpAltViews().add(altViewEdit);
            
            SpUIViewDef viewDef = new SpUIViewDef();
            viewDef.initialize();
            
            viewDef.setName(tName);
            viewDef.setColDef("100px,2px,p,p:g");
            viewDef.createAutoRowDef("p", "2px");
            viewDef.setDescription("Form For "+tName);
            viewDef.setType(ViewDefIFace.ViewType.form);
            viewDef.setDataClassName(ti.getClassName());
            viewDef.setSettableName("edu.ku.brc.ui.forms.DataSetterForObj");
            viewDef.setGettableName("edu.ku.brc.ui.forms.DataGetterForObj");
            
            altViewView.setViewDef(viewDef);
            altViewEdit.setViewDef(viewDef);
            
            viewSet.getSpViewDefs().add(viewDef);
            viewDef.setSpViewSet(viewSet);
            
            SpUIRow row = new SpUIRow();
            row.initialize();
            viewDef.addRow(row);

            SpUICell cell = new SpUICell();
            cell.initialize();
            row.addCell(cell);
            
            cell.setLabel(ti.getTitle());
            cell.setTypeName("separator");
            
            int id = 1;
            for (DBFieldInfo fi : ti.getFields())
            {
                String fName = fi.getName();
                if (fName.startsWith("text") ||
                    fName.startsWith("yesNo") ||
                    fName.startsWith("number") ||
                    fName.startsWith("version") ||
                    fName.startsWith("collectionMember") ||
                    fName.startsWith("timestampCreated") ||
                    fName.startsWith("timestampModified") ||
                    fName.startsWith("createdByAgent") ||
                    fName.startsWith("modifiedByAgent"))
                {
                    continue;
                }
                      
                row = new SpUIRow();
                row.initialize();
                viewDef.addRow(row);

                // Label
                cell = new SpUICell();
                cell.initialize();
                row.addCell(cell);
                
                cell.setTypeName("label");
                cell.setLabelFor(Integer.toString(id));
                
                // Field
                cell = new SpUICell();
                cell.initialize();
                row.addCell(cell);
                
                cell.setTypeName("field");
                cell.setIdent(Integer.toString(id));
                cell.setName(fName);
                cell.setUiType(FormCellFieldIFace.FieldType.text);

                id++;
            }
            
            for (DBRelationshipInfo ri : ti.getRelationships())
            {
                String rName = StringUtils.substringAfterLast(ri.getClassName(), ".");
                
                row = new SpUIRow();
                row.initialize();
                viewDef.addRow(row);
                
                if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                {
                   // Label
                    cell = new SpUICell();
                    cell.initialize();
                    row.addCell(cell);
                    
                    cell.setTypeName("label");
                    cell.setLabelFor(Integer.toString(id));
                    
                    // Field
                    cell = new SpUICell();
                    cell.initialize();
                    row.addCell(cell);
                    
                    cell.setTypeName("field");
                    cell.setIdent(Integer.toString(id));
                    cell.setName(ri.getName());
                    
                    cell.setUiType(FormCellFieldIFace.FieldType.querycbx);
                    
                    if (rName.equals("Division") && id == 19)
                    {
                        int x = 0;
                        x++;
                        
                    }
                    Properties props = new Properties();
                    props.put("title", rName);
                    props.put("name", rName);
                    cell.setProperties(props);
                    
                } else if (ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
                {
                    // Separator
                    cell = new SpUICell();
                    cell.initialize();
                    row.addCell(cell);
                    
                    cell.setLabel(ri.getTitle());
                    cell.setTypeName("separator");

                    // SubView or QueryCBX
                    cell = new SpUICell();
                    cell.initialize();
                    row.addCell(cell);

                    cell.setIdent(Integer.toString(id));
                    cell.setTypeName("subview");
                    cell.setViewName(rName);
                    cell.setName(ri.getName());
                    
                    if (StringUtils.contains(rName.toLowerCase(), "attachment"))
                    {
                        Properties props = new Properties();
                        props.put("addsearch", "true");
                        props.put("btn", "true");
                        props.put("icon", rName);
                        cell.setProperties(props);
                    }
                }  else if (ri.getType() == DBRelationshipInfo.RelationshipType.OneToOne)
                {
                    System.out.println("Skipping OneToOne:   "+tName+" - "+ri.getName());
                    
                } else if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToMany)
                {
                    System.out.println("Skipping ManyToMany: "+tName+" - "+ri.getName());
                }
                id++;
                
            }
            
            //break;
            
        }
        
        StringBuilder sb = new StringBuilder();
        viewSet.toXML(sb);
        
        try
        {
            FileUtils.writeStringToFile(new File("allforms.views.xml"),sb.toString());
            //System.out.println(sb.toString());
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

}
