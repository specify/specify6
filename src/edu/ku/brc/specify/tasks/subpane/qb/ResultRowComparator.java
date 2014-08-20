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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Compares result-set rows by comparing individual columns as defined by
 * a list of SortElement objects. 
 */
public class ResultRowComparator implements Comparator<Vector<Object>>
{
    /**
     * Describes the columns to be compared.
     */
    protected final List<SortElement> sortDef; 
    /**
     * Is the first column in the data being sorted a recId column?
     */
    protected final boolean adjustForRecIds;
    
    protected final List<UIFieldFormatterIFace> formatters = new ArrayList<UIFieldFormatterIFace>();
    
    /**
     * @param sortDef
     * @param adjustForRecIds
     */
    public ResultRowComparator(final List<SortElement> sortDef, final boolean adjustForRecIds, final List<ERTICaptionInfoQB> colInfo)
    {
        this.sortDef = sortDef;
        this.adjustForRecIds = adjustForRecIds;
        buildFormatters(colInfo);
    }

    protected void buildFormatters(List<ERTICaptionInfoQB> colInfo) {
        for (int s = 0; s < sortDef.size(); s++) {
        	UIFieldFormatterIFace formatter = colInfo == null ? null : colInfo.get(this.sortDef.get(s).getColumn()).getUiFieldFormatter();
        	if (formatter != null && formatter.isInBoundFormatter()) {
        		formatters.add(formatter);
        	} else {
        		formatters.add(null);
        	}
        }
    }
    /**
     * @param sortDef
     * @param adjustForRecIds
     */
    public ResultRowComparator(final List<SortElement> sortDef)
    {
        this.sortDef = sortDef;
        this.adjustForRecIds = false;
        buildFormatters(null);
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(Vector<Object> o1, Vector<Object> o2) {
		for (int s=0; s < sortDef.size(); s++) {
			int result = doCompare(s, o1, o2);
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}
    
    /**
     * @param s
     * @param o1
     * @param o2
     * @return
     * 
     * Compares the elements in o1 and o2 at the index defined by s in the order defined by s.
     */
    @SuppressWarnings("unchecked")
	protected int doCompare(int sIdx, Vector<Object> o1, Vector<Object> o2) {
		SortElement s = sortDef.get(sIdx);
		int column = adjustForRecIds ? s.getColumn() + 1 : s.getColumn();
		Object obj1 = s.getDirection() == SortElement.ASCENDING ? o1
				.get(column) : o2.get(column);
		Object obj2 = s.getDirection() == SortElement.ASCENDING ? o2
				.get(column) : o1.get(column);
		if (formatters.get(sIdx) != null) {
			obj1 = formatters.get(sIdx).formatFromUI(obj1);
			obj2 = formatters.get(sIdx).formatFromUI(obj2);
		}
		if (obj1 == null && obj2 == null) {
			return 0;
		}
		if (obj1 == null) {
			return -1;
		}
		if (obj2 == null) {
			return 1;
		}

		Class<?> cls = obj1.getClass();
		if (cls.equals(obj2.getClass())) {
			if (Comparable.class.isAssignableFrom(cls)) {
				// return ((Comparable ) obj1).compareTo(obj2);
				return Comparable.class.cast(obj1).compareTo(obj2);
			}
		}

		// default if (somehow) objects are diferrend classes or their class
		// does not implement Comparable:
		return obj1.toString().compareTo(obj2.toString());
	}
    
}
