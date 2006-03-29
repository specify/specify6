/* Filename:    $RCSfile: MultiView.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
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
package edu.ku.brc.specify.ui.forms;

import java.awt.CardLayout;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.forms.persist.FormAltView;
import edu.ku.brc.specify.ui.forms.persist.FormView;

/**
 * A MulitView is a "view" that contains multiple FormViewable object that can display the current data object in any of the given views.
 * Typically three views are registered: Form, Table, and Field 
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class MultiView extends JPanel
{
    // Statics
    private final static Logger log = Logger.getLogger(MultiView.class);

    protected Hashtable<String, FormViewObj>  viewMapByName   = new Hashtable<String, FormViewObj>();
    protected Object                          data            = null;
    protected CardLayout                      cardLayout;
    protected FormViewObj                     currentView     = null;
    /**
     * 
     *
     */
    public MultiView()
    { 
        setLayout(cardLayout = new CardLayout());
    }
    
    /**
     * Registers a view
     * @param view the view to be added
     */
    public void addView(final FormViewObj view)
    {
        view.setMultiView(this);
        viewMapByName.put(view.getName(), view);
        
        add(view.getUIComponent(), Integer.toString(view.getId()));
        /* Can't get the non-edit view to resize initially correctly
        view.getUIComponent().invalidate();
        view.getUIComponent().validate();
        view.getUIComponent().doLayout();
        invalidate();
        validate();
        doLayout();
        */
        showView(view.getName());
    }
    
    /**
     * Show the component by name
     * @param name the registered name of the component
     */
    public void showView(final String name)
    {
        // This needs to always map from the incoming name to the ID for that view
        // so first look it up by name
        FormViewObj fvo = viewMapByName.get(name);
        
        // If it isn't in the map then it needs to be created
        // all the view are created when needed.
        if (fvo == null)
        {
            List<FormAltView> list = currentView.getFormView().getAltViews();
            int inx = 0;
            for (FormAltView fv : list)
            {
                if (name.equals(fv.getLabel()))
                {
                    break;
                }
                inx++;
            }
            if (inx < list.size())
            {
                FormAltView flv = list.get(inx);
                FormView fv = ViewMgr.getView(currentView.getFormView().getViewSetName(), flv.getId());
                if (fv != null)
                {
                    FormViewable form = ViewFactory.createView(fv);
                    form.setDataObj(currentView.getDataList() != null ? currentView.getDataList() : currentView.getDataObj());
                    form.setDataIntoUI();
                    addView((FormViewObj)form);
                    /* Can't get the non-edit view to resize initially correctly
                    form.getUIComponent().setPreferredSize(currentView.getUIComponent().getPreferredSize());
                    form.getUIComponent().setBounds(currentView.getUIComponent().getBounds());
                    form.getUIComponent().invalidate();
                    form.getUIComponent().doLayout();
                    form.getUIComponent().validate();
                    */
                } else
                {
                    log.error("Unable to load form ViewSetName ["+currentView.getFormView().getViewSetName()+"] id["+flv.getId()+"]"); 
                }
            } else
            {
                log.error("Couldn't find Alt View ["+name+"]in AltView List");
            }

        } else
        {
            fvo.aboutToShow();
            cardLayout.show(this, Integer.toString(fvo.getId()));
        }
        
        currentView = fvo;
    }
    
    /**
     * Sets the Data Object into the View
     * @param aData the data object
     */
    public void setData(Object data)
    {
        this.data = data;
    }
}
