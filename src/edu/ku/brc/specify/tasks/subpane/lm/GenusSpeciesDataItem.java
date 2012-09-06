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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 5, 2012
 *
 */
public class GenusSpeciesDataItem implements OccurrenceSetIFace
{
    private String title;
    private String occurrenceId;
    private String taxa;
    
    /**
     * @param title
     * @param genusSpecies
     */
    public GenusSpeciesDataItem(String title, String occurrenceId, String taxa)
    {
        super();
        this.title = title;
        this.occurrenceId = occurrenceId;
        this.taxa = taxa;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.GenusSpeciesItem#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.GenusSpeciesItem#getGenusSpecies()
     */
    @Override
    public String getOccurrenceId()
    {
        return occurrenceId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.OccurrenceSetIFace#getTaxa()
     */
    @Override
    public String getTaxa()
    {
        return taxa;
    }
}