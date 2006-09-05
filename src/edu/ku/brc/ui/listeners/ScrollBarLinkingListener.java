package edu.ku.brc.ui.listeners;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;

/**
 * Provides a generic way to link the movement of a set of JScrollbars.
 *
 * @code_status Complete
 * @author jstewart
 */
public class ScrollBarLinkingListener implements AdjustmentListener
{
	/** A collections of scrollbars being monitored. */
	private List<JScrollBar> scrollBars;
	
	/** An on/off toggle for the linker. */
	private boolean enabled;
	
	/**
	 * Constructs an instance with an empty set of scrollbars to monitor.
	 *
	 */
	public ScrollBarLinkingListener()
	{
		scrollBars = new Vector<JScrollBar>();
		enabled = true;
	}
	
	/**
	 * Adds the given scrollbar to the set to be monitored.
	 *
	 * @param scrollBar a scrollbar
	 */
	public synchronized void addScrollBar( JScrollBar scrollBar )
	{
		scrollBars.add(scrollBar);
		scrollBar.addAdjustmentListener(this);
	}

	/**
	 * Removes the given scrollbar from the set to be monitored.
	 *
	 * @param scrollBar a scrollbar
	 */
	public synchronized void removeScrollBar( JScrollBar scrollBar )
	{
		scrollBar.removeAdjustmentListener(this);
		scrollBars.remove(scrollBar);
	}

	/**
	 * Returns whether or not the linker is currently enabled.
	 *
	 * @return true is enabled, false otherwise
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Turns the linker on or off, depending on the parameter value supplied.
	 *
	 * @param enabled whether or not the linker should be on
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * Updates all scroll bar models to value "equal" values.
	 *
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 * @param e the triggering adjustment event
	 */
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		// think of the model for each bar being the range
		// minimum <==> max-extent
		if( !enabled )
		{
			return;
		}
		
		BoundedRangeModel model = ((JScrollBar)e.getSource()).getModel();
		int value  = model.getValue();
		int extent = model.getExtent();
		int max    = model.getMaximum();
		
		double percent = (double)value / (double)(max-extent);

		synchronized( this )
		{
			for( JScrollBar sb: scrollBars )
			{
				BoundedRangeModel model2 = sb.getModel();
				int extent2 = model2.getExtent();
				int max2    = model2.getMaximum();
				enabled = false;
				model2.setValue((int)(percent * (max2-extent2)));
				enabled = true;
			}
		}
	}

//	public static void main(String[] args)
//	{
//		JLabel l1 = new JLabel("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//		JLabel l2 = new JLabel("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//		JLabel l3 = new JLabel("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//		JLabel l4 = new JLabel("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//		JLabel l5 = new JLabel("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//		
//		l1.setSize(l1.getWidth(),100);
//		l2.setSize(l2.getWidth(),100);
//		l3.setSize(l3.getWidth(),100);
//		l4.setSize(l4.getWidth(),100);
//		l5.setSize(l5.getWidth(),100);
//		
//		JScrollPane sp1 = new JScrollPane(l1);
//		JScrollPane sp2 = new JScrollPane(l2);
//		JScrollPane sp3 = new JScrollPane(l3);
//		JScrollPane sp4 = new JScrollPane(l4);
//		JScrollPane sp5 = new JScrollPane(l5);
//		
//		JScrollBar sb1 = sp1.getHorizontalScrollBar();
//		JScrollBar sb2 = sp2.getHorizontalScrollBar();
//		JScrollBar sb3 = sp3.getHorizontalScrollBar();
//		JScrollBar sb4 = sp4.getHorizontalScrollBar();
//		JScrollBar sb5 = sp5.getHorizontalScrollBar();
//		
//		ScrollBarLinkingListener l = new ScrollBarLinkingListener();
//		l.addScrollBar(sb1);
//		l.addScrollBar(sb2);
//		l.addScrollBar(sb3);
//		l.addScrollBar(sb4);
//		l.addScrollBar(sb5);
//		
//		JPanel p = new JPanel();
//		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));
//		p.add(sp1);
//		p.add(sp2);
//		p.add(sp3);
//		p.add(sp4);
//		p.add(sp5);
//
//		JFrame f = new JFrame();
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.add(p);
//		f.setSize(500,500);
//		f.setVisible(true);
//	}
}
