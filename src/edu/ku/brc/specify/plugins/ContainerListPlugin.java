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
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;

import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.ui.containers.ContainerTreePanel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 7, 2010
 *
 */
public class ContainerListPlugin extends UIPluginBase implements ChangeListener
{
    private static final Logger log = Logger.getLogger(ContainerListPlugin.class);
            
    protected ContainerTreePanel treePanel;
    
    /**
     * 
     */
    public ContainerListPlugin()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        treePanel = new ContainerTreePanel(this, false, null, null);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g",  "f:p:g"), this);
        
        pb.add(treePanel, cc.xy(1, 1));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value instanceof Container)
        {
            Container container = (Container)value;
            if (container.getId() == null)
            {
                final Thread waitThread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        System.out.println("Here1");
                        try
                        {
                            Thread.sleep(500);
                            System.out.println("Here2");
                        } catch (InterruptedException e) {}
                        System.out.println("Here3");
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                chooseContainer();
                            }
                        });
                    }
                });
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        waitThread.start();
                    }
                });
                
                
            }
        }
    }
    
    /**
     * 
     */
    private void chooseContainer()
    {
        ViewBasedSearchDialogIFace srchDlg = getViewbasedFactory().createSearchDialog(null, "ContainerSearch"); //$NON-NLS-1$
        if (srchDlg != null)
        {
            srchDlg.setTitle(title);
            srchDlg.getDialog().setVisible(true);
            if (!srchDlg.isCancelled())
            {
                Container container = (Container)srchDlg.getSelectedObject();
                if (container != null)
                {
                    DataProviderSessionIFace session = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        session.attach(container);
                        treePanel.set(container, null);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
                        log.error(ex);
                        
                    } finally
                    {
                        if (session != null)
                        {
                            session.close();
                        }
                    }
                    
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
        super.setParent(parent);
        
        if (parent != null)
        {
            fvo.getRsController().getPanel().setVisible(false);
            fvo.getMVParent().getSeparator().setVisible(false);
            
            if (parent.getSaveComponent() != null)
            {
                parent.getSaveComponent().setVisible(false);
            }
        }
        treePanel.setFVO(parent);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"collectionObject"};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#shutdown()
     */
    @Override
    public void shutdown()
    {
        treePanel.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
        ChangeEvent ce = e;
        if (ce == null)
        {
            ce = new ChangeEvent(this);
        }
        notifyChangeListeners(e);
    }

}
