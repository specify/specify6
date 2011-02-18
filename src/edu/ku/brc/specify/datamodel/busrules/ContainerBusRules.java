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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.plugins.ContainerListPlugin;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 16, 2010
 *
 */
public class ContainerBusRules extends BaseBusRules
{
    private JButton containerTreeBtn = null;
    
    /**
     * 
     */
    public ContainerBusRules()
    {
        super(ContainerBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.isEditing())
        {
            Component comp = formViewObj.getControlByName("ContainerTreeBtn");
            if (comp instanceof JButton)
            {
                containerTreeBtn = (JButton)comp;
                containerTreeBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                launch();
                            }
                        });
                    }
                });
            }
        }
    }
    
    private void launch()
    {
        if (formViewObj != null)
        {
            Container container = (Container)formViewObj.getDataObj();
            if (container != null && container.getId() != null)
            {
               // SubPaneMgr.getInstance().closeCurrent();
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    // Just in case the Discipline and Division aren't loaded
                    // that should happen.
                    session.refresh(container);
                    container.forceLoad();

                    
                } catch (Exception ex)
                {
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionBusRules.class, ex);
                    ex.printStackTrace();
                    UsageTracker.incrNetworkUsageCount();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
                ContainerListPlugin plugin = new ContainerListPlugin();
                plugin.initialize(null, !formViewObj.isEditing());
                
                plugin.setValue(container, null);
                
                CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Container Tree", true, CustomDialog.OK_BTN, plugin);
                dlg.setOkLabel(UIRegistry.getResourceString("CLOSE"));
                
                dlg.createUI();
                dlg.pack();
                
                Dimension size = dlg.getPreferredSize();
                size.width = 600;
                dlg.setSize(size);
                UIHelper.centerWindow(dlg);
                dlg.setVisible(true);
                
                plugin.shutdown();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown()
    {
        super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
    }

    
}
