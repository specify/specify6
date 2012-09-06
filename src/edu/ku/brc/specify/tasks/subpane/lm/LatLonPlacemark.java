/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.lm;

import javax.swing.ImageIcon;

import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 5, 2012
 *
 */
public class LatLonPlacemark implements LatLonPlacemarkIFace
{
    private Pair<Double, Double> pnt;
    private ImageIcon            markerImg;
    /**
     * 
     */
    public LatLonPlacemark(final ImageIcon markerImg, final double lat, final double lon)
    {
        super();
        this.markerImg = markerImg;
        pnt = new Pair<Double, Double>(lat, lon);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getHtmlContent(java.lang.String)
     */
    @Override
    public String getHtmlContent(String textColor)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getLatLon()
     */
    @Override
    public Pair<Double, Double> getLatLon()
    {
        return pnt;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getAltitude()
     */
    @Override
    public Double getAltitude()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getImageIcon()
     */
    @Override
    public ImageIcon getImageIcon()
    {
        return markerImg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#cleanup()
     */
    @Override
    public void cleanup()
    {
    }
}