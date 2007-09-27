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

public interface ViewIFace
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
    public abstract BusinessRulesIFace getBusinessRule();

    /**
     * Clean up internal data.
     */
    public abstract void cleanUp();

    public abstract List<AltViewIFace> getAltViews();

    public abstract boolean isUseResourceLabels();

    public abstract String getResourceLabels();

    public abstract String getDesc();

    public abstract String getName();

    public abstract String getObjTitle();

    public abstract String getClassName();

    public abstract String getViewSetName();

    public abstract AltViewIFace.CreationMode getDefaultMode();

    public abstract String getSelectorName();

    public abstract void setDefaultMode(AltViewIFace.CreationMode defaultMode);

    public abstract void setSelectorName(String selectorName);

}