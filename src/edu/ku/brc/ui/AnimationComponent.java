package edu.ku.brc.ui;

import java.awt.Graphics;

import javax.swing.JComponent;

import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.Envelope.EndBehavior;
import org.jdesktop.animation.timing.Envelope.RepeatBehavior;

/**
 * 
 * @code_status Unknown (auto-generated)
 * @author jstewart
 */
public abstract class AnimationComponent extends JComponent implements TimingTarget
{
	protected Cycle cycle;
	protected Envelope envelope;
	protected TimingController timingController;
	protected int begin;
	protected double repeatCount;
	protected Envelope.RepeatBehavior repeat;
	protected Envelope.EndBehavior end;
	protected float percent;
	protected long cycleElapsedTime;
	protected long totalElapsedTime;
	protected boolean animationInProgress;

	public AnimationComponent(double repeatCount,
	                          int begin,
	                          Envelope.RepeatBehavior repeatBehavior,
	                          Envelope.EndBehavior endBehavior,
	                          int duration,
	                          int resolution)
	{
		super();
		this.percent = 0;
		this.animationInProgress = false;
		
		this.begin = begin;
		this.end = endBehavior;
		this.repeat = repeatBehavior;
		this.repeatCount = repeatCount;
		this.envelope = new Envelope(this.repeatCount,this.begin,this.repeat,this.end);

		this.cycle = new Cycle(duration,resolution);
		
		this.timingController = new TimingController(cycle,envelope,this);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Cycle#getDuration()
	 */
	public int getDuration()
	{
		return cycle.getDuration();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Cycle#getResolution()
	 */
	public int getResolution()
	{
		return cycle.getResolution();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Cycle#setDuration(int)
	 */
	public void setDuration(int arg0)
	{
		cycle.setDuration(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Cycle#setResolution(int)
	 */
	public void setResolution(int arg0)
	{
		cycle.setResolution(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Envelope#getBegin()
	 */
	public int getBegin()
	{
		return envelope.getBegin();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Envelope#getEndBehavior()
	 */
	public EndBehavior getEndBehavior()
	{
		return envelope.getEndBehavior();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Envelope#getRepeatBehavior()
	 */
	public RepeatBehavior getRepeatBehavior()
	{
		return envelope.getRepeatBehavior();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.Envelope#getRepeatCount()
	 */
	public double getRepeatCount()
	{
		return envelope.getRepeatCount();
	}
	
	public void setBegin( int begin )
	{
		this.begin = begin;
		newEnv();
	}
	
	public void setRepeatCount( double count )
	{
		this.repeatCount = count;
		newEnv();
	}
	
	public void setRepeatBehavior( Envelope.RepeatBehavior repeat )
	{
		this.repeat = repeat;
		newEnv();
	}
	
	public void setEndBehavior( Envelope.EndBehavior end )
	{
		this.end = end;
		newEnv();
	}
	
	protected void newEnv()
	{
		envelope = new Envelope(this.repeatCount,this.begin,this.repeat,this.end);
		timingController.setEnvelope(envelope);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#addTarget(org.jdesktop.animation.timing.TimingTarget)
	 */
	public void addTarget(TimingTarget arg0)
	{
		timingController.addTarget(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#getAcceleration()
	 */
	public float getAcceleration()
	{
		return timingController.getAcceleration();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#getDeceleration()
	 */
	public float getDeceleration()
	{
		return timingController.getDeceleration();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#setAcceleration(float)
	 */
	public void setAcceleration(float arg0)
	{
		timingController.setAcceleration(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#setDeceleration(float)
	 */
	public void setDeceleration(float arg0)
	{
		timingController.setDeceleration(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#start()
	 */
	public void start()
	{
		timingController.start();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingController#stop()
	 */
	public void stop()
	{
		timingController.stop();
	}

	public boolean isRunning()
	{
		return timingController.isRunning();
	}

	public void timingEvent(long cycElapsedTime, long totElapsedTime, float percentage)
	{
		this.cycleElapsedTime = cycElapsedTime;
		this.totalElapsedTime = totElapsedTime;
		this.percent = percentage;
		this.repaint();
	}

	public void begin()
	{
		this.percent = 0;
		this.animationInProgress = true;
	}

	public void end()
	{
		this.percent = 1;
		this.animationInProgress = false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		doPaintComponent(g);
	}
	
	protected abstract void doPaintComponent(Graphics g);
}
