/* Copyright (C) 2021, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Objects;

class DwcExtensionInfo {
    private String shortName;
    private String dwcRowType;
    private boolean extension;

    //for now...
    public static String AUDOBON_CORE_ROWTYPE = "http://rs.tdwg.org/ac/terms/Multimedia";
    public static String DWC_OCCURRENCE_ROWTYPE = "http://rs.tdwg.org/dwc/terms/Occurrence";

    /**
     *
     * @param dwcRowType
     */
    public DwcExtensionInfo(String dwcRowType, boolean extension) {
        this.dwcRowType = dwcRowType == null ? DWC_OCCURRENCE_ROWTYPE : dwcRowType;
        this.shortName = getShortNameForRowType(this.dwcRowType);
        this.extension = extension;
    }

    /**
     *
     * @return
     */
    public String getShortName() {
        return shortName;
    }

    /**
     *
     * @return
     */
    public String getDwcRowType() {
        return dwcRowType;
    }

    /**
     *
     * @return
     */
    public boolean isExtension() {
        return extension;
    }

    /**
     *
     * @param dwcRowType
     */
    public static String getShortNameForRowType(String dwcRowType) {
        //look up in resource, db table, web ???
        if (dwcRowType.equals(AUDOBON_CORE_ROWTYPE)) {
            return "Audobon Core";
        } else if (dwcRowType.equals(DWC_OCCURRENCE_ROWTYPE)) {
            return "Occurrence";
        } else {
            return dwcRowType;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DwcExtensionInfo that = (DwcExtensionInfo) o;
        return isExtension() == that.isExtension() &&
                Objects.equals(getShortName(), that.getShortName()) &&
                Objects.equals(getDwcRowType(), that.getDwcRowType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getShortName(), getDwcRowType(), isExtension());
    }
}
