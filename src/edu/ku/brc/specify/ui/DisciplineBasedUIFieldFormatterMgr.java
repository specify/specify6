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
package edu.ku.brc.specify.ui;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;

/**
 * This class can be used to load a UIFieldFormatterMgr for a specific Discipline.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Apr 28, 2011
 *
 */
public class DisciplineBasedUIFieldFormatterMgr extends SpecifyUIFieldFormatterMgr
{
    private Discipline discipline;
    private int        disciplineID;
    
    /**
     * Construct with the Discipline for the UIFieldFormatter that is needed.
     * @param disciplineID record ID of Discipline
     */
    public DisciplineBasedUIFieldFormatterMgr(final int disciplineID)
    {
        super();
        this.disciplineID = disciplineID;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr#getDiscipline(edu.ku.brc.af.core.AppContextMgr)
     */
    @Override
    protected Discipline getDiscipline(final AppContextMgr contextMgr)
    {
        if (discipline == null)
        {
            DataProviderSessionIFace pSession = null;
            try
            {
                pSession = DataProviderFactory.getInstance().createSession();
                discipline = pSession.get(Discipline.class, disciplineID);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally
            {
                pSession.close();
            }
        }
        return discipline;
    }

    /**
     * @return the discipline
     */
    public Discipline getDiscipline()
    {
        return discipline;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr#shutdown()
     */
    @Override
    public void shutdown()
    {
        discipline = null;
    }
    
}