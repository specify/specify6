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
import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.validation.DataChangeListener;
import edu.ku.brc.specify.ui.validation.DataChangeNotifier;
import edu.ku.brc.specify.ui.validation.FormValidator;
import edu.ku.brc.specify.ui.validation.UIValidator;
import edu.ku.brc.specify.ui.validation.ValidationListener;

/**
 * A MulitView is a "view" that contains multiple Viewable object that can display the current data object in any of the given views.
 * Typically three views are registered: Form, Table, and Field <BR>
 * <BR>
 * Upon creation the agrument "createWithMode" tells the creation mechanism whether to look for and obey the "View" vs "Edit" modeness.
 * Meaning that if we have a view with subview and they (or some of them) have both a n Edit View and a non-Edit View,
 * all the subview will be cerated as either view or edit form accoring to the parent's mode.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class MultiView extends JPanel implements ValidationListener, DataChangeListener
{


    // Statics
    private final static Logger log = Logger.getLogger(MultiView.class);

    protected MultiView                    mvParent          = null;
    protected View                         view;
    protected Hashtable<String, Viewable>  viewMapByName   = new Hashtable<String, Viewable>();
    protected Object                       data            = null;
    protected Object                       parentDataObj   = null;       
    protected CardLayout                   cardLayout      = new CardLayout();
    protected Viewable                     currentView     = null;

    protected boolean                      specialEditView = false;
    protected boolean                      editable        = false;
    protected AltView.CreationMode         createWithMode  = AltView.CreationMode.None;
    protected Vector<FormValidator>        formValidators  = new Vector<FormValidator>();

    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (Noe, Edit or View mode)
     */
    public MultiView(final MultiView mvParent, 
                     final View view, 
                     final AltView.CreationMode createWithMode)
    {
        setLayout(cardLayout);

        this.mvParent       = mvParent;
        this.view           = view;
        this.createWithMode = createWithMode;
        this.parentDataObj  = parentDataObj;

        specialEditView = view.isSpecialViewEdit();

        createDefaultViewable();
    }

    /**
     *
     */
    public void aboutToShow()
    {
        if (currentView != null)
        {
            currentView.aboutToShow();
        }
    }

    /**
     * Creates the Default Viewable for this view (it chooses the "default" ViewDef
     * @return return the default Viewable (ViewDef)
     */
    protected Viewable createDefaultViewable()
    {
        AltView  altView;
        if (createWithMode != null)
        {
            altView = view.getDefaultAltViewWithMode(createWithMode);

        } else
        {
            altView = view.getDefaultAltView();
        }

        editable = altView.getMode() == AltView.CreationMode.Edit;

        Viewable viewable = ViewFactory.getInstance().buildViewable(view, altView, this);
        viewable.setParentDataObj(parentDataObj);
        
        add(viewable, altView.getName());
        showView(altView.getName());

        return viewable;
    }


    /**
     * Returns the name of the view for the MultiView
     * @return the name of the view for the MultiView
     */
    public String getViewName()
    {
        return view.getName();
    }

    /**
     * @param viewable
     */
    protected void add(final Viewable viewable, final String name)
    {
        //System.out.println("******** ["+name+"]");
        viewMapByName.put(name, viewable);
        add(viewable.getUIComponent(), name);
    }

    /**
     * @param validator
     */
    public void addFormValidator(final FormValidator validator)
    {
        //validator.addValidationListener(root);
        validator.addDataChangeListener(this);
        formValidators.add(validator);

    }

    /**
     * Show the AltView
     * @param altView show the AltView
     */
    public void showView(final AltView altView)
    {
        showView(altView.getName());
    }

    /**
     * Show the component by name
     * @param name the registered name of the component
     */
    public void showView(final String name)
    {
        System.out.println("Show["+name+"]");
        // This needs to always map from the incoming name to the ID for that view
        // so first look it up by name
        Viewable viewable = viewMapByName.get(name);

        // If it isn't in the map then it needs to be created
        // all the view are created when needed.
        if (viewable == null)
        {
            List<AltView> list = currentView.getView().getAltViews();
            int inx = 0;
            for (AltView altView : list)
            {
                if (name.equals(altView.getName()))
                {
                    break;
                }
                inx++;
            }

            if (inx < list.size())
            {
                AltView altView = list.get(inx);
                View view = ViewMgr.getView(currentView.getView().getViewSetName(), altView.getView().getName());
                if (view != null)
                {
                    editable       = altView.getMode() == AltView.CreationMode.Edit;
                    createWithMode =  altView.getMode();
                    viewable = ViewFactory.createFormView(this, view, altView.getName(), data);
                    add(viewable, altView.getName());
                    viewable.aboutToShow();
                    cardLayout.show(this, altView.getName());


                } else
                {
                    log.error("Unable to load form ViewSetName ["+currentView.getView().getViewSetName()+"] Name["+altView.getName()+"]");
                }
            } else
            {
                log.error("Couldn't find Alt View ["+name+"]in AltView List");
            }

        } else
        {
            viewable.aboutToShow();
            cardLayout.show(this, name);
        }

        currentView = viewable;
        
        if (currentView != null)
        {
            currentView.setParentDataObj(parentDataObj);
        }
    }

    /**
     * Returns the MultiView's mvParent
     * @return the MultiView's mvParent
     */
    public MultiView getMultiViewParent()
    {
        return mvParent;
    }

    /**
     * Return whether the MultiView is in Edit Mode
     * @return whether the MultiView is in Edit Mode
     */
    public boolean isEditable()
    {
        return editable;
    }

    /**
     * Return whether the MultiView's CreateMode (may be null, true or false) meaning don't assume it will always be non-null
     * @return whether the MultiView's CreateMode (may be null, true or false)
     */
    public AltView.CreationMode getCreateWithMode()
    {
        return createWithMode;
    }

    /**
     * Sets the Data Object into the View
     * @param data the data object
     */
    public void setData(Object data)
    {
        this.data = data;
        currentView.setDataObj(data);
        //currentView.setDataIntoUI();
    }

    /**
     * @return
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @return
     */
    protected boolean isAllValidationOK()
    {
        for (FormValidator validator : formValidators)
        {
            if (!validator.isFormValid())
            {
                return false;
            }
        }
        return true;
    }
    

    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
        if (currentView != null)
        {
            currentView.setParentDataObj(parentDataObj);
        }
    }


    public Object getParentDataObj()
    {
        return parentDataObj;
        
    }
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------

   /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        boolean wasOK = isAllValidationOK();
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            viewable.validationWasOK(wasOK);
        }
    }

    //-----------------------------------------------------
    // DataChangeListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.specify.ui.validation.DataChangeNotifier)
     */
    public void dataChanged(final String name, final Component comp, DataChangeNotifier dcn)
    {
        boolean wasOK = isAllValidationOK();
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            viewable.validationWasOK(wasOK);
        }
    }
}
