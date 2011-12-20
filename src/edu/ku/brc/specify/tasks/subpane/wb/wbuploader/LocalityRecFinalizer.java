/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class LocalityRecFinalizer implements UploadedRecFinalizerIFace
{

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader)
     */
    @Override
    public void finalizeForWrite(DataModelObjBase rec, int recNum, Uploader uploader) throws UploaderException
    {
        //This assumes that rec is a newly uploaded/created record. 
        //XXX Updates -- Will need to be re-worked when record updates are implemented
        Locality loc = (Locality )rec;
        WorkbenchRow wbRow = uploader.getWbSS().getWorkbench().getRow(uploader.getRow());
        loc.setLat1text(wbRow.getLat1Text());
        loc.setLat2text(wbRow.getLat2Text());
        loc.setLong1text(wbRow.getLong1Text());
        loc.setLong2text(wbRow.getLong2Text());
        
        LatLonConverter.FORMAT fmt = new GeoRefConverter().getLatLonFormat(StringUtils.stripToNull(wbRow.getLat1Text()));
        loc.setOriginalLatLongUnit(fmt.ordinal());
        loc.setSrcLatLongUnit((byte )fmt.ordinal());
        
    }

}
