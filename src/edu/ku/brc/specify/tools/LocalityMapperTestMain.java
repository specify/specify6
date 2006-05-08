package edu.ku.brc.specify.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.tasks.services.LocalityMapper;
import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.util.Pair;

public class LocalityMapperTestMain implements MapperListener
{
    private static Log log = LogFactory.getLog(LocalityMapperTestMain.class);

    protected Vector<Locality> localities;
    protected Vector<String> labels;
    protected LocalityMapper cem;
    
	public LocalityMapperTestMain(Vector<Locality> localities,
								  Vector<String> labels)
	{
		this.localities = localities;
		this.labels = labels;
		cem = new LocalityMapper(localities,labels);
		cem.setPreferredMapWidth(1000);
		cem.setLabelColor(Color.GREEN);
		cem.setDotSize(12);
		cem.setDotColor(Color.BLUE);
		cem.setShowArrows(true);
		cem.setArrowColor(Color.RED);
		cem.setShowLabels(true);
		cem.setCurrentLocColor(Color.ORANGE);
	}
	
	public void getMap()
	{
		cem.getMap(this);
	}
	
	public void mapReceived(final Icon map)
	{
		if( !SwingUtilities.isEventDispatchThread() )
		{
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						mapReceived(map);
					}
				});
		}
		JFrame f = new JFrame();
		final JLabel l = new JLabel(map);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		f.add(l);
		l.addMouseMotionListener(new MouseMotionListener()
				{
					public void mouseDragged(MouseEvent e)
					{}
					public void mouseMoved(MouseEvent e)
					{
						Pair<Double,Double> latLong = cem.getLatLongForPointOnMapIcon(e.getX(), e.getY());
						if( latLong != null )
							log.info(latLong.first + " : " + latLong.second);
					}
				});
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1000,1000);
		f.setVisible(true);
		
		Timer pickNewCurrent = new Timer(5000,new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Random r = new Random();
						int index = r.nextInt(localities.size());
						cem.setCurrentLoc(localities.elementAt(index));
						l.repaint();
					}
				});
		pickNewCurrent.setRepeats(true);
		pickNewCurrent.start();

	}

	public void exceptionOccurred(Exception e)
	{
		log.error("Exception occurred while grabbing map", e);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void main(String[] args)
	{
		double[] locationArray = {
				42.2893,  -87.7387,
				41.6916,  -87.0678,
				43.8696,  -86.6329,
				45.3410,  -86.3533,
				43.3995,  -87.7512,
				43.2249,  -62.8771,
				14.1280,  -77.6264,
				21.6814, -110.9789
		};
		
		String[] labelArray = {
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8"
		};
		
		Vector<Locality> localities = new Vector<Locality>(locationArray.length/2);
		Vector<String> labels = new Vector<String>(locationArray.length/2);
		for( int i = 0; i < locationArray.length; i+=2 )
		{
			Locality l = new Locality();
			l.initialize();
			l.setLatitude1(locationArray[i]);
			l.setLongitude1(locationArray[i+1]);
			localities.add(l);
			labels.add(labelArray[i/2]);
		}
		
		LocalityMapperTestMain main = new LocalityMapperTestMain(localities,labels);
		main.getMap();
		JFrame f = new JFrame();
		f.setVisible(true);
		f.setVisible(false);
	}
}
