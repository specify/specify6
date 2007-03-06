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
package edu.ku.brc.specify.tasks.subpane.wb;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewSet;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 21, 2007
 *
 */
public class WorkbenchFormPane extends FormPane
{

    /**
     * Create a Form Pane for the Workbench.
     * @param name the name of the pane
     * @param task the owning task
     * @param desc a description of the pane
     */
    public WorkbenchFormPane(String name, Taskable task, String desc)
    {
        super(name, task, desc);
    }

    /**
     * Create a Form Pane for the Workbench.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param viewSetName the viewSet name
     * @param viewName the view name
     * @param mode the mode (edit or View)
     * @param data the data to be placed in the form
     * @param options the MultiView options
     */
    public WorkbenchFormPane(String name, Taskable task, String viewSetName, String viewName,
            String mode, Object data, int options)
    {
        super(name, task, viewSetName, viewName, mode, data, options);
    }

    /**
     * Create a Form Pane for the Workbench.
     * @param name the name of the pane
     * @param task the owning task
     * @param view the view to use to create the form
     * @param mode the mode (edit or View)
     * @param data the data to be placed in the form
     * @param options the MultiView options
     */
    public WorkbenchFormPane(String name, Taskable task, View view, String mode, Object data, int options)
    {
        super(name, task, view, mode, data, options);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.FormPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        // Remove the dynamic Views and ViewDefs from the ViewSet Manager
        ViewSetMgr viewSetMgr = SpecifyAppContextMgr.getInstance().getBackstopViewSetMgr();
        if (viewSetMgr != null)
        {
            ViewSet viewSet = viewSetMgr.getViewSet("Dynamic");
            if (viewSet != null)
            {
                View view = multiView.getView();
                if (view != null)
                {
                    viewSet.removeTransientView(view);
                    
                    for (AltView altView : view.getAltViews())
                    {
                        viewSet.removeTransientViewDef(altView.getViewDef()); 
                    }
                }
            }
        }
        return super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.FormPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
    }

}
