/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools.datamodelgenerator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class Display
{
    protected String view; 
    protected String dataobjformatter; 
    protected String uiformatter;
    protected String searchdlg;
    protected String newobjdlg;
    
    public Display(String view, String dataobjformatter, String uiformatter, String searchdlg, String newobjdlg)
    {
        super();
        this.view = view;
        this.dataobjformatter = dataobjformatter;
        this.uiformatter = uiformatter;
        this.searchdlg = searchdlg;
        this.newobjdlg = newobjdlg;
    }

    public String getDataobjformatter()
    {
        return dataobjformatter;
    }

    public String getNewobjdlg()
    {
        return newobjdlg;
    }

    public String getSearchdlg()
    {
        return searchdlg;
    }

    public String getUiformatter()
    {
        return uiformatter;
    }

    public String getView()
    {
        return view;
    }
    
}
