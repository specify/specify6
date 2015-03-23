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
package edu.ku.brc.services.biogeomancer;

/**
 * This class is nothing more than a C-style struct holding a bunch of string
 * values.  The information encapsulated in this struct is a representation of
 * the a BioGeomancer Classic query result.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class BioGeomancerResultStruct
{
    // results values
    public String country;
    public String adm1;
    public String adm2;
    public String featureName;
    public String featureType;
    public String gazetteer;
    public String coordinates;
    public String offset;
    public String boundingBox;
    public String locality;
}
