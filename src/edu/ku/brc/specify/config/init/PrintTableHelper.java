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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import edu.ku.brc.specify.tasks.PageSetupDlg;
import edu.ku.brc.specify.tasks.subpane.LabelsPane;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 5, 2010
 *
 */
public class PrintTableHelper
{
    protected JTable printTable;
    
    /**
     * 
     */
    public PrintTableHelper(final JTable printTable)
    {
        super();
        this.printTable = printTable;
    }

    /**
     * @param model
     * @return
     * @throws Exception
     */
    public DynamicReport buildReport(final TableModel model, final PageSetupDlg pageSetupDlg) throws Exception
    {
        // Find a Sans Serif Font on the System
        String fontName = null;
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (java.awt.Font font : ge.getAllFonts())
        {
            
            String fName = font.getFamily().toLowerCase();
            if (StringUtils.contains(fName, "sansserif") || 
                StringUtils.contains(fName, "arial") || 
                StringUtils.contains(fName, "verdana"))
            {
                fontName = font.getFamily();
                break;
            }
        }
        
        if (fontName == null)
        {
            fontName = Font._FONT_TIMES_NEW_ROMAN;
        }
        
        /**
         * Creates the DynamicReportBuilder and sets the basic options for the report
         */
        FastReportBuilder drb = new FastReportBuilder();
        
        Style columDetail = new Style();
        //columDetail.setBorder(Border.THIN);
        
        Style columDetailWhite = new Style();
        //columDetailWhite.setBorder(Border.THIN);
        columDetailWhite.setBackgroundColor(Color.WHITE);
        columDetailWhite.setFont(new Font(10, fontName, false));
        columDetailWhite.setHorizontalAlign(HorizontalAlign.CENTER);
        columDetailWhite.setBlankWhenNull(true);
        
        Style columDetailWhiteBold = new Style();
        //columDetailWhiteBold.setBorder(Border.THIN);
        columDetailWhiteBold.setBackgroundColor(Color.WHITE);
        
        Style titleStyle = new Style();
        titleStyle.setFont(new Font(12, fontName, true));
        
        // Odd Row Style
        Style oddRowStyle = new Style();
        //oddRowStyle.setBorder(Border.NO_BORDER);
        oddRowStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        
        Color veryLightGrey = new Color(240, 240, 240);
        oddRowStyle.setBackgroundColor(veryLightGrey);
        oddRowStyle.setTransparency(Transparency.OPAQUE);

        // Create Column Headers for the Report
        for (int i=0;i<model.getColumnCount();i++)
        {
            String colName = model.getColumnName(i);
            
            Class<?> dataClass = model.getColumnClass(i);
            if (dataClass == Object.class)
            {
                if (model.getRowCount() > 0)
                {
                    Object data = model.getValueAt(0, i);
                    if (data != null)
                    {
                        dataClass = data.getClass();
                    } else
                    {
                        // Column in first row was null so search down the rows
                        // for a non-empty cell
                        for (int j=1;j<model.getRowCount();j++)
                        {
                            data = model.getValueAt(j, i);
                            if (dataClass != null)
                            {
                                dataClass = data.getClass();
                                break;
                            }
                        }
                        
                        if (dataClass == null)
                        {
                            dataClass = String.class;
                        }
                    }
                }
            }
            
            ColumnBuilder colBldr = ColumnBuilder.getInstance().setColumnProperty(colName, dataClass.getName());
            int bracketInx = colName.indexOf('[');
            if (bracketInx > -1)
            {
                colName = colName.substring(0, bracketInx-1);
            }
            colBldr.setTitle(colName);
            
            colBldr.setStyle(columDetailWhite);
            
            AbstractColumn column = colBldr.build();
            drb.addColumn(column);
            
            Style headerStyle = new Style();
            headerStyle.setFont(new Font(11, fontName, true));
            //headerStyle.setBorder(Border.THIN);
            headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
            headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
            headerStyle.setBackgroundColor(new Color(80, 80, 80));
            headerStyle.setTransparency(Transparency.OPAQUE);
            headerStyle.setTextColor(new Color(255, 255, 255));
            column.setHeaderStyle(headerStyle);
        }
        
        drb.setTitle(pageSetupDlg.getPageTitle());
        drb.setTitleStyle(titleStyle);
        
        drb.setLeftMargin(20);
        drb.setRightMargin(20);
        drb.setTopMargin(10);
        drb.setBottomMargin(10);
        
        drb.setPrintBackgroundOnOddRows(true);
        drb.setOddRowBackgroundStyle(oddRowStyle);
        drb.setColumnsPerPage(new Integer(1));
        drb.setUseFullPageWidth(true);
        drb.setColumnSpace(new Integer(5));
        
        // This next line causes an exception
        // Event with DynamicReport 3.0.12 and JasperReposrts 3.7.3
        //drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGMENT_CENTER);
        
        Page[] pageSizes = new Page[] {Page.Page_Letter_Portrait(), Page.Page_Legal_Portrait(), Page.Page_A4_Portrait(),
                                       Page.Page_Letter_Landscape(), Page.Page_Legal_Landscape(), Page.Page_A4_Landscape()};
        int pageSizeInx = pageSetupDlg.getPageSize() + (pageSetupDlg.isPortrait() ? 0 : 3);
        drb.setPageSizeAndOrientation(pageSizes[pageSizeInx]);
        
        DynamicReport dr = drb.build();
        
        return dr;
    }
    
    /**
     * @param table
     * @param pageTitle
     */
    public void printGrid(final String pageTitle)
    {
        final PageSetupDlg pageSetup = new PageSetupDlg();
        pageSetup.createUI();
        pageSetup.setPageTitle(pageTitle);
        
        pageSetup.setVisible(true);
        if (pageSetup.isCancelled())
        {
            return;
        }
        
        UIRegistry.writeSimpleGlassPaneMsg(getResourceString("JasperReportFilling"), 24);
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            protected JasperPrint jp    = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                if (printTable != null)
                {
                    try
                    {
                        DynamicReport dr = buildReport(printTable.getModel(), pageSetup);
                        JRDataSource  ds = new JRTableModelDataSource(printTable.getModel());
                        jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (jp != null)
                {   
                    reportFinished(jp, pageTitle);
                }
            }
        };
        
        backupWorker.execute();
    }
    
    /**
     * @param print
     * @param pageTitle
     */
    private void reportFinished(final JasperPrint print, final String pageTitle)
    {
        try
        {
            JRViewer jasperViewer = new JRViewer(print);  
            
            JPanel p = new JPanel(new BorderLayout());
            p.add(jasperViewer, BorderLayout.CENTER);
            
            final CustomFrame frame = new CustomFrame(pageTitle, CustomFrame.OK_BTN, p);
            frame.setOkLabel(UIRegistry.getResourceString("CLOSE"));
            //frame.pack();
            //frame.setSize(600, 900);
            UIHelper.centerAndShow(frame);
            frame.setSize(800, 800);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    UIHelper.centerWindow(frame);
                }
            });

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LabelsPane.class, ex);
            ex.printStackTrace();
        }
    }

}
