package edu.ku.brc.specify.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.tasks.services.LocalityMapper;
import edu.ku.brc.util.Pair;

public class LocalityMapperTestMain
{
    private static Log log = LogFactory.getLog(LocalityMapperTestMain.class);

	public LocalityMapperTestMain()
	{
		
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
		
		final LocalityMapper cem = new LocalityMapper(localities,labels);
		
		cem.setPreferredMapWidth(1000);
		
		Icon i;
		try
		{
			cem.setLabelColor(Color.GREEN);
			cem.setDotSize(12);
			cem.setDotColor(Color.BLUE);
			cem.setShowArrows(false);
			cem.setArrowColor(Color.RED);
			cem.setShowLabels(true);
			i = cem.getMap();
		}
		catch( HttpException e )
		{
			log.error("HTTP protocol error occured when grabbing map",e);
			return;
		}
		catch( IOException e )
		{
			log.error("A transport error occured when grabbing map",e);
			return;
		}
		JFrame f = new JFrame();
		final JLabel l = new JLabel(i);
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
		
		Timer t = new Timer(6000,new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						log.info("Adding another locality");
						Locality l2 = new Locality();
						l2.initialize();
						l2.setLatitude1(38.9d);
						l2.setLongitude1(-94.8d);
						cem.addLocalityAndLabel(l2, "11911 S Redbud Ln");

						Locality l1 = new Locality();
						l1.initialize();
						l1.setLatitude1(0d);
						l1.setLongitude1(94.8d);
						cem.addLocalityAndLabel(l1, "???");

						
						l.repaint();
						
						try
						{
							l.setIcon(cem.getMap());
						}
						catch( HttpException e1 )
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						catch( IOException e1 )
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
//						try
//						{
//							l.setIcon(cem.getMap());
//						}
//						catch( HttpException e1 )
//						{
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						catch( IOException e1 )
//						{
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
					}
				});
		t.setRepeats(false);
		t.start();
	}
}
