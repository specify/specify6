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
package edu.ku.brc.specify.config;

import java.util.HashSet;
import java.util.prefs.BackingStoreException;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 14, 2010
 *
 */
public class FixDBAfterLogin
{

    /**
     * 
     */
    public FixDBAfterLogin()
    {
        super();
    }
    
    /**
     * 
     */
    public void checkMultipleLocalities()
    {
         int cnt = BasicSQLUtils.getCountAsInt("select count(localitydetailid) - count(distinct localityid) from localitydetail");
         if (cnt > 0)
		{
			cnt = BasicSQLUtils
					.getCountAsInt("select count(collectionobjectid) from collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid inner join  (select localityid from localitydetail group by localityid having count(localitydetailid) > 1) badlocs on badlocs.localityid = ce.localityid");
			String str = String.format("Multiple Locality Detail Records - Count: %d", cnt);
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
		}
         
         cnt = BasicSQLUtils.getCountAsInt("select count(geocoorddetailid) - count(distinct localityid) from geocoorddetail");
         if (cnt > 0)
         {
        	 cnt = BasicSQLUtils.getCountAsInt("select count(collectionobjectid) from collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid inner join  (select localityid from geocoorddetail group by localityid having count(geocoorddetailid) > 1) badlocs on badlocs.localityid = ce.localityid");
 		     String str = String.format("Multiple GeoCoord Detail Records - Count: %d", cnt);
 		     edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
         }
    }
    
    /**
     * fixes bad version and timestamps for recordsets created by Uploader. 
     */
    public void fixUploaderRecordsets()
    {
    	BasicSQLUtils.update( "update recordset set TimestampCreated = now(), Version = 0 where Type = 1 and Version is null");
        AppPreferences.getGlobalPrefs().putBoolean("FixUploaderRecordsets", true);
    }
    
}
