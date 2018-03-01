/* Copyright (C) 2017, University of Kansas Center for Research
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

    private Boolean isUpdateUpload = null;

    /**
     *
     * @param uploader
     * @return
     */
    private boolean isUpdateUpload(final Uploader uploader) {
        return isUpdateUpload == null ? isUpdateUpload = uploader.isUpdateUpload() : isUpdateUpload;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int, edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader)
     */
    @Override
    public boolean finalizeForWrite(final DataModelObjBase rec, int recNum, final Uploader uploader) throws UploaderException
    {
        //This assumes that rec is a newly uploaded/created record. 
        //XXX Updates -- Will need to be re-worked when record updates are implemented
        Locality loc = (Locality )rec;
        String origLat1Text = loc.getLat1text();
        String origLat2Text = loc.getLat2text();
        String origLong1Text = loc.getLong1text();
        String origLong2Text = loc.getLong2text();
        String origLatLongType = loc.getLatLongType();
        Integer origOrigLatLongUnit = loc.getOriginalLatLongUnit();
        Byte origSrcLatLongUnit = loc.getSrcLatLongUnit();
        WorkbenchRow wbRow = uploader.getWb().getRow(uploader.getRow());
        if (!isUpdateUpload(uploader) || wbRow.getGeoCoordFlds().size() > 0) {
            loc.setLat1text(wbRow.getLat1Text());
            loc.setLat2text(wbRow.getLat2Text());
            loc.setLong1text(wbRow.getLong1Text());
            loc.setLong2text(wbRow.getLong2Text());
            if (loc.getLatitude1() != null && loc.getLatLongType() == null) {
                if (loc.getLatitude2() == null) {
                    //seems there's no formal definition of the allowed values for latlongtype??
                    loc.setLatLongType("Point");
                } else {
                    //hmmm...assume Line
                    loc.setLatLongType("Line");
                }
            }
            LatLonConverter.FORMAT fmt = new GeoRefConverter().getLatLonFormat(StringUtils.stripToNull(wbRow.getLat1Text()));
            if (!isUpdateUpload) {
                loc.setSrcLatLongUnit((byte) fmt.ordinal());
            }
            loc.setOriginalLatLongUnit(fmt.ordinal());
        }
        return changed(origLat1Text, loc.getLat1text())
                || changed(origLat2Text, loc.getLat2text())
                || changed(origLong1Text, loc.getLong2text())
                || changed(origLong2Text, loc.getLong2text())
                || changed(origLatLongType, loc.getLatLongType())
                || changed(origOrigLatLongUnit, loc.getOriginalLatLongUnit())
                || changed(origSrcLatLongUnit, loc.getSrcLatLongUnit());
    }

    /**
     *
     * @param v1
     * @param v2
     * @return
     */
    private boolean changed(final Object v1, final Object v2) {
       return ((v1 == null) ^ (v2 == null))
               || (v1 != null && !v1.equals(v2));
    }
}
