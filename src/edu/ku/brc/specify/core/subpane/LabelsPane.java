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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.*;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.*;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.datamodel.*;


/**
 * This class will display Label previews and may eventually hold a labels editor
 * 
 * @author rods
 * 
 * 
 */
public class LabelsPane extends BaseSubPane implements AsynchronousFilllListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(LabelsPane.class);
    
    // Data Members
    protected AsynchronousFillHandle asyncFillHandler = null;
    protected JLabel                 label            = null; 
    protected JasperCompilerRunnable compiler         = null;
    
    protected RecordSet              recordSet        = null;
    
    /**
     * 
     *
     */
    public LabelsPane(final String name, 
                      final Taskable task)
    {
        super(name, task);
        
        //label = new JLabel("Labels Overview", SwingConstants.CENTER);
        //add(label, BorderLayout.CENTER);
    }
    
    /**
     * Set the text to the label (create the label if it doesn't exist)
     * @param msg the message to be displayed
     */
    protected void setLabelText(final String msg)
    {
        if (label == null)
        {
            removeAll();
            label = new JLabel("", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        label.setText(msg);
        invalidate();
        doLayout();
        repaint();
    }
    
    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     */
    public void createReport(final String fileName, final RecordSet recordSet)
    {
        this.recordSet = recordSet;
        progressLabel.setText(getResourceString("JasperReportCompiling"));
        compiler = new JasperCompilerRunnable(this, XMLHelper.getConfigDirPath(fileName));
        compiler.start();
    }
    
    /**
     * The compiling of the report is complete
     * @param report the completeed report, or null if there was a compiling error
     */
    protected void compileComplete(final JasperReport report)
    {
        if (report != null)
        {
            try
            {
                // 28594
                String itemnum = "";
                
                if ( recordSet == null)
                {
                    itemnum = JOptionPane.showInputDialog(this, "Please Enter a Catalog Item");
                    if (itemnum == null)
                    {
                        itemnum = "28594";
                    }
                    itemnum = "= " + itemnum;
                } else
                {
                    StringBuffer strBuf = new StringBuffer(" in (");
                    Set set = recordSet.getItems();
                    int i = 0;
                    for (Iterator iter=set.iterator();iter.hasNext();)
                    {
                        RecordSetItem rsi = (RecordSetItem)iter.next();
                        if (i > 0)
                        {
                            strBuf.append(",");
                        }
                        strBuf.append(Integer.toString(rsi.getRecordId()));
                        i++;
                    }
                    strBuf.append(")");
                    itemnum = strBuf.toString();
                }
                
                Map<Object, Object> parameters = new HashMap<Object, Object>();
                //parameters.put("itemnum", Integer.parseInt(itemnum));
                parameters.put("itemnum", itemnum);
                
                System.out.println("["+itemnum+"]");

                progressLabel.setText(getResourceString("JasperReportFilling"));
                asyncFillHandler = AsynchronousFillHandle.createHandle(report, parameters, DBConnection.getInstance().getConnection());
                asyncFillHandler.addListener(this);
                asyncFillHandler.startFill();
                
            } catch (JRException ex)
            {
                setLabelText(getResourceString("JasperReportCreatingViewer"));
                log.error(ex);
                ex.printStackTrace();                
            }
        } else
        {
            setLabelText(getResourceString("JasperReportCompileError"));
        }
        compiler = null;        
    }
    
    //------------------------------------------------------------
    // AsynchronousFilllListener
    //------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportCancelled()
     */
    public void reportCancelled()
    {

    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFillError(java.lang.Throwable)
     */
    public void reportFillError(java.lang.Throwable t)
    {
        removeAll();
        setLabelText(getResourceString("JasperReportFillError"));
        log.error(t);
        t.printStackTrace();
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFinished(net.sf.jasperreports.engine.JasperPrint)
     */
    public void reportFinished(JasperPrint jasperPrint)
    {
        try
        {
            removeAll();
            label = null;
            
            JRViewer jasperViewer = new JRViewer(jasperPrint);
            add(jasperViewer, BorderLayout.CENTER);
            
            UICacheManager.forceTopFrameRepaint();
           
            
        } catch (Exception ex)
        {
            setLabelText(getResourceString("JasperReportCreatingViewer"));
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------
    public class JasperCompilerRunnable implements Runnable
    {
        protected Thread               thread;
        protected LabelsPane           listener;
        protected String               fileName;
        
        /**
         * Constructs a an object to execute an SQL staement and then notify the listener
         * @param listener the listener
         * @param sqlStr the SQL statement to be executed.
         */
        public JasperCompilerRunnable(final LabelsPane listener, final String fileName)
        {
            this.listener = listener;
            this.fileName = fileName;
        }
        
 
        /**
         * Starts the thread to make the SQL call
         *
         */
        public void start()
        {
            thread = new Thread(this);
            thread.start();
        }
        
        /**
         * Stops the thread making the call
         *
         */
        public synchronized void stop()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
            thread = null;
            notifyAll();
        }
        
        /**
         * Creates a connection, makes the call and returns the results
         */
        public void run()
        {
            try
            {
                JasperReport report = JasperCompileManager.compileReport(fileName);
                listener.compileComplete(report);
                
            } catch (Exception ex)
            {
                log.error(ex);
                listener.compileComplete(null);
            } 
        }
    }
   
}
