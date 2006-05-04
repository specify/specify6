/* Filename:    $RCSfile: SimpleDescPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.tasks.subpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.tasks.services.LocalityMapper;

/**
 * A default pane for display a simple label telling what it is suppose to do
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class LocalityMapperSubPane extends BaseSubPane
{
    //private static Log log = LogFactory.getLog(SimpleDescPane.class);
    
    protected LocalityMapper localityMapper = new LocalityMapper();
    protected JLabel         imageLabel     = new JLabel("Loading Image...");
    protected MapGetter      mapGetter;
    
    /**
     * 
     *
     */
    public LocalityMapperSubPane(final String name, 
                                 final Taskable task,
                                 final List<Locality> list)
    {
        super(name, task);
        
        setBackground(Color.WHITE);
        
        for (Locality locality : list)
        {
            localityMapper.addLocalityAndLabel(locality, null);
        }
        
        add(imageLabel, BorderLayout.CENTER);
        
        mapGetter = new MapGetter(localityMapper);
        mapGetter.start();
       
    }
    
    protected void setLabel(final Icon imageIcon)
    {
        imageLabel.setText(null);
        imageLabel.setIcon(imageIcon);
    }

    protected void setLabel(final String msg)
    {
        imageLabel.setIcon(null);
        imageLabel.setText(msg);
    }

 
    //------------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------------
    
    public class MapGetter implements Runnable
    {
        protected Thread thread;
        protected LocalityMapper localityMapper;
        
        /**
         * Constructs a an object to execute an SQL staement and then notify the listener
         * @param listener the listener
         * @param sqlStr the SQL statement to be executed.
         */
        public MapGetter(final LocalityMapper localityMapper)
        {
            this.localityMapper = localityMapper;
        }
        
        public void start()
        {
            thread = new Thread(this);
            thread.start();
        }

        /**
         * Stops the thread making the call
         *
         */
        public synchronized void stop()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
            thread = null;
            notifyAll();
        }
        
        public void run()
        {
            try
            {
                setLabel(localityMapper.getMap());
                
            } catch (Exception ex)
            {
                setLabel("Was unable to get the Map.");
            }
            
        }
    }
}
