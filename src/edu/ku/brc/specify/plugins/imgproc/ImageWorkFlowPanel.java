/* Copyright (C) 2011, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.imgproc;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.Trayable;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.WorkBenchPluginIFace;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 11, 2011
 *
 */
public class ImageWorkFlowPanel extends JPanel implements ImageProcListener, WorkBenchPluginIFace
{
    protected CustomDialog        dlg;
    protected ImageProcessorPanel imgProcPanel;
    protected IconTray<Trayable>  imageTray;
    protected JStatusBar          statusBar;
    protected JList               list;
    protected boolean             firstDisplay = true;
    
    protected JButton             ssBtn       = null;
    protected Workbench           workbench   = null;
    protected SpreadSheet         spreadSheet = null;
    protected WorkbenchPaneSS     wbpss       = null;
    
    /**
     * 
     */
    public ImageWorkFlowPanel()
    {
        super();
        createUI();
    }

    /**
     * 
     */
    public void createUI()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,8px,f:p:g", "p,4px,f:max(150px;p):g,8px,p"), this);
        
        imageTray = new IconTray<Trayable>(IconTray.SINGLE_ROW);
        imageTray.setCellRenderer(new ImageTrayListCellRenderer());
        //imageTray.setFixedCellHeight(120);
        
        imgProcPanel = new ImageProcessorPanel(imageTray, this);
        imgProcPanel.createUI();
        
        statusBar = new JStatusBar();
        
        list = new JList(new DefaultListModel());
        JScrollPane sc = UIHelper.createScrollPane(list);
        
        pb.add(imgProcPanel, cc.xy(1,1));
        pb.add(sc,           cc.xywh(3, 1, 1, 3));
        pb.add(imageTray,    cc.xyw(1,3,1));
        pb.add(statusBar,    cc.xyw(1,5,3));
        
        pb.setDefaultDialogBorder();
        
        dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Image Processor", false, CustomDialog.OKCANCELAPPLY, this)
        {
            @Override
            public JButton getOkBtn()
            {
                imageTray.removeAllItems();
                return super.getOkBtn();
            }

            @Override
            protected void cancelButtonPressed()
            {
                ConfigDlg configDlg = new ConfigDlg(dlg);
                UIHelper.centerAndShow(configDlg);
                if (!configDlg.isCancelled())
                {
                    imgProcPanel.readSetupPrefs();
                }
            }
            
            @Override
            protected void applyButtonPressed()
            {
                imgProcPanel.clearFiles();
            }
        };
        dlg.setOkLabel("Close");
        dlg.setCancelLabel("Config");
        dlg.setApplyLabel("Clear");
        
        imgProcPanel.clearFiles();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.imgproc.ImageProcListener#complete(edu.ku.brc.imgproc.ImageProcListener.ActionType, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void complete(ActionType actionType, Object data)
    {
        if (actionType == ActionType.eBarDecoding)
        {
            if (data instanceof Pair<?, ?>)
            {
                Pair<String, ArrayList<File>> dataPair = (Pair<String, ArrayList<File>>)data;
                ((DefaultListModel)list.getModel()).addElement(dataPair.first);
                
                if (spreadSheet != null)
                {
                    int rowInx = spreadSheet.getRowCount() - 1;
                    spreadSheet.setRowSelectionInterval(rowInx, rowInx);
                    wbpss.addRowAfter();
                    rowInx++;
                    
                    WorkbenchRow row = workbench.getRow(rowInx);
                    if (row != null)
                    {
                        for (File file : dataPair.second)
                        {
                            try
                            {
                                row.addImage(file);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        WorkbenchTemplate wbt = workbench.getWorkbenchTemplate();
                        for (WorkbenchTemplateMappingItem item : wbt.getWorkbenchTemplateMappingItems())
                        {
                            System.out.println("["+item.getFieldName()+"]");
                            if (item.getFieldName().equals("catalogNumber"))
                            {
                                int colInx = item.getViewOrder();
                                System.out.println("colInx["+colInx+"]");
                                if (colInx > -1)
                                {
                                    spreadSheet.setValueAt(dataPair.first, rowInx, colInx);
                                }
                            }
                        }
                        wbpss.refreshImagesForSelectedRow();
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.imgproc.ImageProcListener#statusMsg(java.lang.String)
     */
    @Override
    public void statusMsg(String msg)
    {
        statusBar.setText(msg);
    }
    
    //---------------------------------------------------------------------------------
    // WorkBenchPluginIFace
    //---------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSubPanel(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setSubPanel(final SubPaneIFace parent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#getMissingFieldsForPlugin()
     */
    @Override
    public List<String> getMissingFieldsForPlugin()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setWorkbenchPaneSS(edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS)
     */
    @Override
    public void setWorkbenchPaneSS(final WorkbenchPaneSS wbpss)
    {
        this.wbpss = wbpss;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setSpreadSheet(edu.ku.brc.ui.tmanfe.SpreadSheet)
     */
    @Override
    public void setSpreadSheet(final SpreadSheet ss)
    {
        this.spreadSheet = ss;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setWorkbench(edu.ku.brc.specify.datamodel.Workbench)
     */
    @Override
    public void setWorkbench(final Workbench workbench)
    {
        this.workbench = workbench;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#process(java.util.List)
     */
    @Override
    public boolean process(final List<WorkbenchRow> rows)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#shutdown()
     */
    @Override
    public void shutdown()
    {
        this.workbench   = null;
        this.spreadSheet = null;
        
        this.dlg.setVisible(false);
        this.dlg       = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#setButton(javax.swing.JButton)
     */
    @Override
    public void setButton(final JButton btn)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#getSSButtons()
     */
    @Override
    public Collection<JComponent> getSSButtons()
    {
        if (ssBtn == null)
        {
            ssBtn = UIHelper.createIconBtn("ip_barcode20", null, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    if (firstDisplay)
                    {
                        UIHelper.centerAndShow(dlg);
                        firstDisplay = false;
                    } else
                    {
                        dlg.setVisible(!dlg.isVisible());
                    }
                }
            });
            ssBtn.setEnabled(true);
        }
        ArrayList<JComponent> btns = new ArrayList<JComponent>();
        btns.add(ssBtn);
        return btns;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.WorkBenchPluginIFace#getFormButtons()
     */
    @Override
    public Collection<JComponent> getFormButtons()
    {
        return new ArrayList<JComponent>();
    }

    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
               ImageWorkFlow appObj = new ImageWorkFlow();
               appObj.createUI();
               
            }
        });
        
    }*/
}
