/* Filename:    $RCSfile: LabelsPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.core.subpane;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.Taskable;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.view.*;
import net.sf.jasperreports.view.JRViewer;

/**
 * This class will display Label previews and may eventually hold a labels editor
 * 
 * @author rods
 * 
 * 
 */
public class LabelsPane extends BaseSubPane
{
    // Static Data Members
    private static Log log = LogFactory.getLog(LabelsPane.class);
    
    /**
     * 
     *
     */
    public LabelsPane(final String name, 
                      final Taskable task)
    {
        super(name, task);
        
        JLabel label = new JLabel("Labels Overview", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        /*
        try
        {
            String fileName = XMLHelper.getConfigDirPath("andys_label.jrxml");
            String compiledFileName = JasperCompileManager.compileReportToFile(fileName);
            //String compiledFileName = JasperCompileManager.compileReportToFile("/Dev/prototypes/Hyla3/reports_labels/lichens_label.jrxml");
            //String compiledFileName = "/Dev/prototypes/Hyla3/Unnamed.jasper";
            String filledReportName = JasperFillManager.fillReportToFile(compiledFileName, null, DBConnection.getInstance().getConnection());
            //String filledReportName = "/Dev/prototypes/Hyla3/Unnamed.jrprint";
            
            JRViewer jasperViewer = new JRViewer(filledReportName, false);
            add(jasperViewer, BorderLayout.CENTER);
            
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        */
        
    }
    public void doReport(String[] aArgs)
    {
        boolean design = false;
        if (design)
        {
            //FrameDesigner designer = new FrameDesigner(new File(aArgs[1]), false);
            //designer.show();
            
        } else
        {
            try {
                //File file = new File(aArgs[1]);
                //System.out.println("File:["+aArgs[1]+"] "+(file.exists() ? "exists" : "does not exist."));
                
                /* XXX
                String compiledFileName = JasperCompileManager.compileReportToFile("/Dev/prototypes/Hyla3/reports_labels/andys_label2.jrxml");//aArgs[1]);
                //String compiledFileName = JasperCompileManager.compileReportToFile("/Dev/prototypes/Hyla3/reports_labels/lichens_label.jrxml");//aArgs[1]);
                //String compiledFileName = "/Dev/prototypes/Hyla3/Unnamed.jasper";
                String filledReportName = JasperFillManager.fillReportToFile(compiledFileName, null, this.mSession.connection());
                //String filledReportName = "/Dev/prototypes/Hyla3/Unnamed.jrprint";
                
                JRViewer jasperViewer = new JRViewer(filledReportName, false);
                add(jasperViewer, BorderLayout.CENTER);
                XXX */
                
            } catch (Exception ex)
            {
                System.out.println("Report file["+aArgs[1]+"]");
                ex.printStackTrace();
            }
        }
    }
   
}
