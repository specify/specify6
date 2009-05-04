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
package edu.ku.brc.specify.extras;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormRow;
import edu.ku.brc.af.ui.forms.persist.FormRowIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace.ViewType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.BiColorBooleanTableCellRenderer;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 2, 2009
 *
 */
public class ViewToSchemaReview
{
    Vector<Object[]> modelList = new Vector<Object[]>();

    /**
     * 
     */
    public ViewToSchemaReview()
    {
        super();
    }

    
    /**
     * 
     */
    public static void checkViews()
    {
        SpecifyAppContextMgr sacm = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        for (ViewIFace view : sacm.getEntirelyAllViews())
        {
            System.err.println(view.getName() + " ----------------------");
            for (AltViewIFace av : view.getAltViews())
            {
                ViewDefIFace vd = av.getViewDef();
                if (vd.getType() == ViewType.form)
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(vd.getClassName());
                    if (ti != null)
                    {
                        FormViewDef fvd = (FormViewDef)vd;
                        for (FormRowIFace row : fvd.getRows())
                        {
                            for (FormCellIFace cell : row.getCells())
                            {
                                if (cell.getType() == FormCellIFace.CellType.panel)
                                {
                                    
                                } else if (cell.getType() == FormCellIFace.CellType.field)
                                {
                                    FormCellFieldIFace fcf       = (FormCellFieldIFace)cell;
                                    String             fieldName = fcf.getName();
                                    if (!fcf.isIgnoreSetGet() && !fieldName.equals("this"))
                                    {
                                        DBFieldInfo fi = ti.getFieldByName(fieldName);
                                        if (fi != null)
                                        {
                                            //System.err.println("Form Field["+fieldName+"] is in schema.");
                                            
                                        } else
                                        {
                                            DBRelationshipInfo ri = ti.getRelationshipByName(fieldName);
                                            if (ri == null)
                                            {
                                                System.err.println("Form Field["+fieldName+"] not in table.");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Object[] createRow(final String tblName, final String fldName, final String fldTitle, final Boolean isOnForm)
    {
        Object[] row = new Object[5];
        row[0] = tblName;
        row[1] = fldName;
        row[2] = fldTitle;
        row[3] = isOnForm;
        row[4] = !isOnForm;
        return row;
    }
    
    /**
     * 
     */
    public void checkSchemaAndViews()
    {
        
        Hashtable<String, Hashtable<String, Boolean>> viewFieldHash = new Hashtable<String, Hashtable<String, Boolean>>();
        
        SpecifyAppContextMgr sacm = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        for (ViewIFace view : sacm.getEntirelyAllViews())
        {
            System.err.println(view.getName() + " ----------------------");
            for (AltViewIFace av : view.getAltViews())
            {
                ViewDefIFace vd = av.getViewDef();
                if (vd.getType() == ViewType.form)
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(vd.getClassName());
                    if (ti != null)
                    {
                        Hashtable<String, Boolean> tiHash = viewFieldHash.get(ti.getName());
                        if (tiHash == null)
                        {
                            tiHash = new Hashtable<String, Boolean>();
                            viewFieldHash.put(ti.getName(),  tiHash);
                        }
                        
                        FormViewDef fvd = (FormViewDef)vd;
                        for (FormRowIFace row : fvd.getRows())
                        {
                            for (FormCellIFace cell : row.getCells())
                            {
                                if (cell.getType() == FormCellIFace.CellType.panel)
                                {
                                    
                                } else if (cell.getType() == FormCellIFace.CellType.field)
                                {
                                    FormCellFieldIFace fcf       = (FormCellFieldIFace)cell;
                                    String             fieldName = fcf.getName();
                                    if (!fcf.isIgnoreSetGet() && !fieldName.equals("this"))
                                    {
                                        DBFieldInfo fi = ti.getFieldByName(fieldName);
                                        if (fi != null)
                                        {
                                            //System.err.println("Form Field["+fieldName+"] is in schema.");
                                            tiHash.put(fieldName, true);
                                        } else
                                        {
                                            DBRelationshipInfo ri = ti.getRelationshipByName(fieldName);
                                            if (ri == null)
                                            {
                                                System.err.println("Form Field["+fieldName+"] not in table.");
                                            } else
                                            {
                                                tiHash.put(fieldName, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            int cnt = 0;
            
            Hashtable<String, Boolean> tiHash = viewFieldHash.get(ti.getName());
            if (tiHash != null)
            {
                System.err.println(ti.getName() + " ----------------------");
                for (DBFieldInfo fi : ti.getFields())
                {
                    String title = cnt == 0 ? ti.getTitle() : "";
                    Boolean isInForm = tiHash.get(fi.getName()) != null;
                    
                    if (fi.isHidden())
                    {
                        if (isInForm)
                        {
                            System.err.println("    ["+fi.getName()+"] is hidden but is on a form.");
                            modelList.add(createRow(title, fi.getName(), fi.getTitle(), isInForm));
                            cnt++;
                        }
                    } else
                    {
                        if (!isInForm)
                        {
                            System.err.println("    ["+fi.getName()+"] is visible but is NOT on a form.");
                            modelList.add(createRow(title, fi.getName(), fi.getTitle(), isInForm));
                            cnt++;
                        }
                    }
                }
                
                for (DBRelationshipInfo ri : ti.getRelationships())
                {
                    String title = cnt == 0 ? ti.getTitle() : "";

                    Boolean isInForm = tiHash.get(ri.getName()) != null;
                    
                    if (ri.isHidden())
                    {
                        if (isInForm)
                        {
                            System.err.println("    ["+ri.getName()+"] is hidden but is on a form.");
                            modelList.add(createRow(title, ri.getName(), ri.getTitle(), isInForm));
                            cnt++;
                        }
                    } else
                    {
                        if (!isInForm)
                        {
                            System.err.println("    ["+ri.getName()+"] is visible but is NOT on a form.");
                            modelList.add(createRow(title,  ri.getName(), ri.getTitle(), isInForm));
                            cnt++;
                        }
                    }  
                }
            }
        }
        
        JTable table = new JTable(new ViewModel());
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(UIHelper.createScrollPane(table), (new CellConstraints()).xy(1, 1));
        pb.setDefaultDialogBorder();
        
        table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
        table.setDefaultRenderer(Boolean.class, new BiColorBooleanTableCellRenderer());
        
        //UIHelper.makeTableHeadersCentered(table, false);
        UIHelper.calcColumnWidths(table, null);
        
        CustomDialog dlg = new CustomDialog(null, "", true, pb.getPanel());
        dlg.setVisible(true);
    }
    
    /**
     * 
     */
    public static void dumpFormFieldList()
    {
        List<ViewIFace> viewList = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getEntirelyAllViews();
        Hashtable<String, ViewIFace> hash = new Hashtable<String, ViewIFace>();
        
        for (ViewIFace view : viewList)
        {
            hash.put(view.getName(), view);
        }
        Vector<String> names = new Vector<String>(hash.keySet());
        Collections.sort(names);
        
        try
        {
            PrintWriter pw = new PrintWriter(new File("FormFields.html"));
            
            pw.println("<HTML><HEAD><TITLE>Form Fields</TITLE><link rel=\"stylesheet\" href=\"http://specify6.specifysoftware.org/schema/specify6.css\" type=\"text/css\"/></HEAD><BODY>");
            pw.println("<center>");
            pw.println("<H2>Forms and Fields</H2>");
            pw.println("<center><table class=\"brdr\" border=\"0\" cellspacing=\"0\">");
            
            int formCnt  = 0;
            int fieldCnt = 0;
            for (String name : names)
            {
                ViewIFace view = hash.get(name);
                boolean hasEdit = false;
                for (AltViewIFace altView : view.getAltViews())
                {
                    if (altView.getMode() != AltViewIFace.CreationMode.EDIT)
                    {
                        hasEdit = true;
                        break;
                    }
                }
    
                //int numViews = view.getAltViews().size();
                for (AltViewIFace altView : view.getAltViews())
                {
                    //AltView av = (AltView)altView;
                    if ((hasEdit && altView.getMode() == AltViewIFace.CreationMode.VIEW))
                    {
                        ViewDefIFace vd = altView.getViewDef();
                       if (vd instanceof FormViewDef)
                       {
                           formCnt++;
                           FormViewDef fvd = (FormViewDef)vd;
                           pw.println("<tr><td class=\"brdrodd\">");
                           pw.println(fvd.getName());
                           pw.println("</td></tr>");
                           int r = 1;
                           for (FormRowIFace fri :fvd.getRows())
                           {
                               FormRow fr = (FormRow)fri;
                               for (FormCellIFace cell : fr.getCells())
                               {
                                   if (StringUtils.isNotEmpty(cell.getName()))
                                   {
                                       if (cell.getType() == FormCellIFace.CellType.field ||
                                           cell.getType() == FormCellIFace.CellType.subview)
                                       {
                                           pw.print("<tr><td ");
                                           pw.print("class=\"");
                                           pw.print(r % 2 == 0 ? "brdrodd" : "brdreven");
                                           pw.print("\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + cell.getName());
                                           pw.println("</td></tr>");
                                           fieldCnt++;
                                       }
                                   }
                               }
                           }
                       }
                    }
                }
            }
            pw.println("</table></center><br>");
            pw.println("Number of Forms: "+formCnt+"<br>");
            pw.println("Number of Fields: "+fieldCnt+"<br>");
            pw.println("</body></html>");
            pw.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    class ViewModel extends AbstractTableModel
    {
        protected String[] header = {"Table", "Field Name", "Field Title", "On Form But Hidden", "Not on Form Visible"};
        
        /**
         * 
         */
        public ViewModel()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex > 2 ? Boolean.class : String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return header[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == 3 || columnIndex == 4;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return header.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return modelList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Object[] row = modelList.get(rowIndex);
            return row[columnIndex];
        }
        
    }

}
