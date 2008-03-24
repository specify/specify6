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

/**
 * A generic comparator used on various formatter and aggregator lists
 * 
 * @author rpereira
 *
 * @code_status Alpha
 *
 * Created Date: Mar 24, 2008
 *
 */

package edu.ku.brc.util;

import java.util.Comparator;

public class ComparatorByStringRepresentation<T> implements Comparator<T>
{
    public int compare(T o1, T o2)
    {
        return o1.toString().compareTo(o2.toString());
    }
}
