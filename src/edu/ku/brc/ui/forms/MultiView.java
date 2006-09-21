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
package edu.ku.brc.ui.forms;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.DataChangeListener;
import edu.ku.brc.ui.validation.DataChangeNotifier;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.UIValidator;
import edu.ku.brc.ui.validation.ValidationListener;

/**
 * A MulitView is a "view" that contains multiple Viewable object that can display the current data object in any of the given views.
 * Typically three views are registered: Form, Table, and Field <BR>
 * <BR>
 * Upon creation the agrument "createWithMode" tells the creation mechanism whether to look for and obey the "View" vs "Edit" modeness.
 * Meaning that if we have a view with subview and they (or some of them) have both a n Edit View and a non-Edit View,
 * all the subview will be cerated as either view or edit form accoring to the parent's mode.

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class MultiView extends JPanel implements ValidationListener, DataChangeListener
{


    // Statics
    private static final Logger log = Logger.getLogger(MultiView.class);

    protected MultiView                    mvParent          = null;
    protected View                         view;
    protected Hashtable<String, Viewable>  viewMapByName   = new Hashtable<String, Viewable>();
    protected Object                       data            = null;
    protected Object                       parentDataObj   = null;
    protected CardLayout                   cardLayout      = new CardLayout();
    protected Viewable                     currentView     = null;
    protected Session                      session         = null;

    protected boolean                      specialEditView = false;
    protected boolean                      editable        = false;
    protected AltView.CreationMode         createWithMode  = AltView.CreationMode.None;
    protected Vector<FormValidator>        formValidators  = new Vector<FormValidator>();
    protected boolean                      dataHasChanged  = false;    

    protected boolean                      createRecordSetController;
    protected boolean                      createViewSwitcher;

    protected List<MultiView>              kids            = new ArrayList<MultiView>();

    protected List<ViewBasedDisplayIFace>  displayFrames   = null;

    // Temp
    protected MultiView                    thisObj           = null;
    protected CarryForwardSetUp            carryForwardSetup = null;

    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (Noe, Edit or View mode)
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @param createRecordSetController indicates that a RecordSet Contoller should be created
     */
    public MultiView(final MultiView mvParent,
                     final View view,
                     final AltView.CreationMode createWithMode,
                     final boolean createRecordSetController,
                     final boolean createViewSwitcher)
    {
        setLayout(cardLayout);

        this.mvParent                  = mvParent;
        this.view                      = view;
        this.createWithMode            = createWithMode;
        this.createRecordSetController = createRecordSetController;
        this.createViewSwitcher        = createViewSwitcher;

        specialEditView = view.isSpecialViewEdit();

        createDefaultViewable(createRecordSetController, createViewSwitcher);

        // Testing
        if (mvParent == null)
        {
            thisObj = this;

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)
                {
                    showContextMenu(e);
                }

                public void mouseReleased(MouseEvent e)
                {
                    showContextMenu(e);

                }
                public void mouseClicked(MouseEvent e)
                {
                    ((FormViewObj)thisObj.currentView).listFieldChanges();
                }
            });

        }
    }

    /**
     * Shows Parent Form's Context Menu.
     * @param e the mouse event
     */
    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Configure Carry Forward"); // I18N
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    carryForwardSetup = new CarryForwardSetUp(thisObj);
                    thisObj.add(carryForwardSetup, "carryforward");
                    cardLayout.show(thisObj, "carryforward");
                }
            });

            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());

        }
    }

    /**
     * Called to indicate acceptence of CarryForward setup.
     * @param accept true accept, false denied
     */
    public void acceptCarryForwardSetup(final boolean accept)
    {
        if (carryForwardSetup != null)
        {
            cardLayout.show(thisObj, currentView.getName());
            remove(carryForwardSetup);
            carryForwardSetup = null;
        }
    }


    /**
     * Returns a Collection of the Viewables
     * @return  a Collection of the Viewables
     */
    public Collection<Viewable> getViewables()
    {
        return viewMapByName.values();
    }

    /**
     * Adds child view.
     * @param mv add child view
     */
    public void addChild(final MultiView mv)
    {
        kids.add(mv);
    }

    /**
     * Asks the Viewable to get the data from the UI and transfer the changes (really all the fields) to
     * the DB object.
     */
    public void getDataFromUI()
    {
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            if (viewable.getValidator() != null && viewable.getValidator().hasChanged()) // XXX Not sure why it must have a validator ???
            {
                viewable.getDataFromUI();
                if (viewable.getValidator() != null && viewable.getValidator().hasChanged())
                {
                    if (HibernateUtil.updateLastEdittedInfo(viewable.getDataObj()))
                    {
                        viewable.setDataIntoUI();
                    }
                }
            }
        }
    }

    /**
     * Asks the Viewable to get the data from the UI and transfer the changes (really all the fields) to
     * the DB object.
     */
    public void setSession(final Session session)
    {
        this.session = session;
        
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            e.nextElement().setSession(session);
        }
        
        for (MultiView mv : kids)
        {
            mv.setSession(session);
        }
    }


    /**
     * Returns the View (the definition).
     * @return the View (the definition)
     */
    public View getView()
    {
        return view;
    }

    /**
     * Returns all the Multiview children.
     * @return all the Multiview children
     */
    public List<MultiView> getKids()
    {
        return kids;
    }

    /**
     * Called to tell current view it is about to be shown or hidden.
     * @param show true - shown, false - hidden
     */
    public void aboutToShow(final boolean show)
    {
        if (currentView != null)
        {
            currentView.aboutToShow(show);
        }
        showDisplayFrames(show);
    }

    /**
     * Tells all the Viewables that have validators that the form is new or old.
     * NOTE New Forms means that it is an empty form and that the controls should
     * not show validation errors until they have had focus if they are a validator that changes on input
     * and not by the OK button or by focus.
     */
    public void setIsNewForm(final boolean isNewForm)
    {
        dataHasChanged = false;
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            if (viewable.getValidator() != null)
            {
                viewable.getValidator().setAllUIValidatorsToNew(isNewForm);
            }
        }
    }
    
    /**
     * Returns true if any of the validators have changed, false if it has no validators or they haven't changed.
     * @return true if any of the validators have changed, false if it has no validators or they haven't changed.
     */
    public boolean hasChanged()
    {
        if (!dataHasChanged)
        {
            log.info("MV ---------- "+hashCode());
            for (FormValidator validator : formValidators)
            {
                log.info("FV1 ---------- "+validator.hashCode());
                if (validator.hasChanged())
                {
                    return true;
                }
            }
            
            for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
            {
                Viewable viewable = e.nextElement();
                FormValidator validator = viewable.getValidator();
                if (validator != null)
                {
                    log.info("FV2 ---------- "+validator.hashCode());
                    if (validator.hasChanged())
                    {
                        return true;
                    }
                }
            }
            
            for (MultiView mv : kids)
            {
                if (mv.hasChanged())
                {
                    return true;
                }
            }
            return false;
        }
        return dataHasChanged;
    }

    /**
     * Creates the Default Viewable for this view (it chooses the "default" ViewDef.
     * @param createRecordSetController indicates that a RecordSet Contoller should be created
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @return return the default Viewable (ViewDef)
     */
    protected Viewable createDefaultViewable(final boolean createRecordSetController,
                                             final boolean createViewSwitcher)
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

        Viewable viewable = ViewFactory.getInstance().buildViewable(view, altView, this, createRecordSetController, createViewSwitcher);
        viewable.setParentDataObj(parentDataObj);

        if (add(viewable, altView.getName()))
        {
            showView(altView.getName());
        }

        return viewable;
    }


    /**
     * Returns the name of the view for the MultiView.
     * @return the name of the view for the MultiView
     */
    public String getViewName()
    {
        return view.getName();
    }

    /**
     * Adds a viewable to the MultiView.
     * @param viewable the viewablew to be added
     * @return true if it was added, false if name conflicts
     */
    protected boolean add(final Viewable viewable, final String name)
    {
        //System.out.println("******** ["+name+"]");
        if (viewMapByName.get(name) != null)
        {
            log.error("Adding a Viewable by a name that is already used["+name+"]");
            return false;

        } else
        {
            viewMapByName.put(name, viewable);
            add(viewable.getUIComponent(), name);
            return true;
        }
    }

    /**
     * Adds a form validator.
     * @param validator
     */
    public void addFormValidator(final FormValidator validator)
    {
        validator.addDataChangeListener(this);
        formValidators.add(validator);

    }

    /**
     * Show the AltView.
     * @param altView show the AltView
     */
    public void showView(final AltView altView)
    {
        showView(altView.getName());
    }

    /**
     * Show the component by name.
     * @param name the registered name of the component
     */
    public void showView(final String name)
    {
        //System.out.println("Show["+name+"]");
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
                View view = AppContextMgr.getInstance().getView(currentView.getView().getViewSetName(), altView.getView().getName());
                if (view != null)
                {
                    log.debug("--------------------------");
                    for (int i=0;i<getComponentCount();i++)
                    {
                        Component comp = getComponent(i);
                        if (comp instanceof Viewable)
                        {
                            log.debug(((Viewable)comp).getName());
                        } else
                        {
                            log.debug(comp);
                        }
                    }
                    log.debug("--------------------------");

                    String altViewName = altView.getName();
                    currentView.aboutToShow(false);
                    editable       = altView.getMode() == AltView.CreationMode.Edit;
                    createWithMode = altView.getMode();
                    viewable = ViewFactory.createFormView(this, view, altViewName, data, createRecordSetController, createViewSwitcher);
                    viewable.setSession(session);
                    if (add(viewable, altViewName))
                    {
                        viewable.aboutToShow(true);
                        cardLayout.show(this, altViewName);
                        log.debug("Added Viewable["+altViewName+"]");
                    }

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
            if (currentView != null)
            {
                currentView.aboutToShow(false);
            }
            viewable.aboutToShow(true);
            cardLayout.show(this, name);
        }

        currentView = viewable;

        if (currentView != null)
        {
            currentView.setParentDataObj(parentDataObj);
        }
    }

    /**
     * Returns the MultiView's mvParent.
     * @return the MultiView's mvParent
     */
    public MultiView getMultiViewParent()
    {
        return mvParent;
    }

    /**
     * Return whether the MultiView is in Edit Mode.
     * @return whether the MultiView is in Edit Mode
     */
    public boolean isEditable()
    {
        return editable;
    }

    /**
     * Return whether the MultiView's CreateMode (may be null, true or false) meaning don't assume it will always be non-null.
     * @return whether the MultiView's CreateMode (may be null, true or false)
     */
    public AltView.CreationMode getCreateWithMode()
    {
        return createWithMode;
    }

    /**
     * Sets the Data Object into the View.
     * @param data the data object
     */
    public void setData(Object data)
    {
        this.data = data;
        currentView.setDataObj(data);
        dataHasChanged = false;
    }

    /**
     * Returns  the data object for this form.
     * @return the data object for this form
     */
    public Object getData()
    {
        if (data instanceof Collection<?>)
        {
            return currentView.getDataObj();
        }
        return data;
    }

    /**
     * Returns whether all the validation of this form and child forms is OK.
     * @return whether all the validation of this form and child forms is OK
     */
    protected boolean isAllValidationOK()
    {
        for (FormValidator validator : formValidators)
        {
            //log.debug("*** "+validator.isFormValid()+"  "+validator.getName());
            //validator.dumpState(false);
            if (!validator.isFormValid())
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Sets the dataObj of the parent, this is need to add new child node from subforms.
     * @param parentDataObj the dataObj of the parent
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
        if (currentView != null)
        {
            currentView.setParentDataObj(parentDataObj);
        }
    }


    /**
     * Returns the dataObj of of the parent.
     * @return the dataObj of of the parent
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }


    /**
     * Returns whether this MulitView is the very top MultiView which typically contains the save UI.
     * @return whether this MulitView is the very top MultiView
     */
    public boolean isRoot()
    {
        return this.mvParent == null;
    }


    /**
     * Registers "display" window for display "sub object" information.
     * @param frame the frame to be added
     */
    public void registerDisplayFrame(final ViewBasedDisplayIFace frame)
    {
        if (displayFrames == null)
        {
            displayFrames  = new ArrayList<ViewBasedDisplayIFace>();
        }
        displayFrames.add(frame);
    }

    /**
     * Unregsters a frame from the MultiView list of sub-frames.
     * @param frame the frame to be unregistered (removed)
     */
    public void unregisterDisplayFrame(final ViewBasedDisplayIFace frame)
    {
        if (displayFrames != null)
        {
            displayFrames.remove(frame);
        }

    }

    /**
     * Shows or hides all the DisplayFrame attached to this MultiView.
     * @param show true - show, false - hide
     */
    public void showDisplayFrames(final boolean show)
    {
        if (displayFrames != null)
        {
            for (ViewBasedDisplayIFace frame : displayFrames)
            {
                frame.showDisplay(show);
            }
        }
    }

    /**
     * Returns the current view.
     * @return the current view
     */
    public Viewable getCurrentView()
    {
        return currentView;
    }

    /**
     * Tells the MultiView the MV that it is being shutdown to be disposed.
     */
    public void shutdown()
    {
        mvParent      = null;
        view          = null;
        data          = null;
        parentDataObj = null;
        currentView   = null;
        session       = null;

        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            e.nextElement().shutdown();
        }

        for (FormValidator fv : formValidators)
        {
            fv.cleanUp();
        }
        formValidators.clear();

        for (MultiView mv : kids)
        {
            mv.shutdown();
        }
        kids.clear();

        if (displayFrames != null)
        {
            for (ViewBasedDisplayIFace vbd : displayFrames)
            {
                vbd.shutdown();
            }
            displayFrames.clear();
        }

        thisObj           = null;
        carryForwardSetup = null;
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
     * @see edu.ku.brc.ui.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.validation.DataChangeNotifier)
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
