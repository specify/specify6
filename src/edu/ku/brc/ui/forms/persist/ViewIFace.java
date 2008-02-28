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
package edu.ku.brc.ui.forms.persist;

import java.util.List;

import edu.ku.brc.ui.forms.BusinessRulesIFace;

/**
 * Represents a View in the form the syste. A View is is made of of AltView views, enabling it to switch between editing
 * viewing, tables, icon views etc.
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 30, 2007
 *
 */
public interface ViewIFace  extends Comparable<ViewIFace>
{

    /**
     * Adds an alternative view.
     * @param altView the alternate view
     * @return the altView that was passed in
     */
    public abstract AltViewIFace addAltView(final AltViewIFace altView);

    /**
     * Find the default AltViewIFace and creates it.
     * @param altViewType look for a default view for this type of view
     * @return the default altView
     */
    public abstract AltViewIFace getDefaultAltView(final AltViewIFace.CreationMode creationMode,
                                                   final String altViewType);

    /**
     * Find the default AltViewIFace and creates it.
     * @return the default altView
     */
    public abstract AltViewIFace getDefaultAltView();

    /**
     * Find the default AltViewIFace for a mode and creates it.
     * @param creationMode the mode to be looked up
     * @param defAltViewType default Alt View Type
     * @return @return the default altView
     */
    public abstract AltViewIFace getDefaultAltViewWithMode(final AltViewIFace.CreationMode creationMode,
                                                           final String defAltViewType);

    /**
     * Finds a AltViewIFace by name, if the name is null then it returs the default AltViewIFace.
     * @param nameStr the name of the altView
     * @return the altView
     */
    public abstract AltViewIFace getAltView(final String nameStr);

    /**
     * Returns whether it is a special view, meaning a view with just to AltViews where one is edit and the other is view.
     * @return whether it is a special view, meaning a view with just to AltViews where one is edit and the other is view.
     */
    public abstract boolean isSpecialViewAndEdit();

    /**
     * Creates an instance of the BusinessRuleIFace object for processing.
     * @return an instance of the BusinessRuleIFace object for processing.
     */
    public abstract BusinessRulesIFace createBusinessRule();
    
    /**
     * @return the businessRulesClassName
     */
    public String getBusinessRulesClassName();
    
    /**
     * @return whether the default business rules should be used. This enables
     * a form to not have any business rules when there are default BRs. For example,
     * search forms do not want to default to having BRs.
     */
    public boolean useDefaultBusinessRules();

    /**
     * @param businessRulesClassName the businessRulesClassName to set
     */
    public void setBusinessRulesClassName(String businessRulesClassName);

    /**
     * Clean up internal data.
     */
    public abstract void cleanUp();

    /**
     * @return the list of Alternate View for the View
     */
    public abstract List<AltViewIFace> getAltViews();

    /**
     * @return the human readable explanation for the View
     */
    public abstract String getDesc();

    /**
     * @return the name of the view (must be unique)
     */
    public abstract String getName();

    /**
     * @return the human readable title for the View
     */
    public abstract String getObjTitle();

    /**
     * @return the Java Class name for the data that goe sin the form
     */
    public abstract String getClassName();

    /**
     * @return the name of the ViewSet this View is a part of
     */
    public abstract String getViewSetName();

    /**
     * @return The default creation mode for the View. For example, whether it should be created in edit or view mode.
     */
    public abstract AltViewIFace.CreationMode getDefaultMode();

    /**
     * @return The name of the field that is used to select between AltView
     */
    public abstract String getSelectorName();

    /**
     * @param defaultMode the default creation mode
     */
    public abstract void setDefaultMode(AltViewIFace.CreationMode defaultMode);

    /**
     * @param selectorName the name of the field that is used to select between AltView
     */
    public abstract void setSelectorName(String selectorName);
    
    /**
     * @return whether the view is for internal use or a subform view (only) or an external view.
     */
    public abstract boolean isInternal();
    
    /**
     * Comparator.
     * @param obj the obj to compare
     * @return 0,1,-1
     */
    public abstract int compareTo(ViewIFace obj);
    
    /**
     * Appends its XML.
     * @param sb the buffer
     */
    public abstract void toXML(final StringBuilder sb);

}