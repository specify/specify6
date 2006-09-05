package edu.ku.brc.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

// commented out (along with included code) to eliminate the project dependancy on filters JAR
//import com.jhlabs.image.TwirlFilter;

/**
 * Creates a JPanel to display an animated swirling of an image.
 *  
 * @code_status Complete
 * @author jstewart
 */
@SuppressWarnings("serial")
public class ImageSwirlPanel extends JPanel implements TimingTarget
{
    /** The original image. */
    protected BufferedImage original;
    /** The original image's width. */
    protected int origWidth;
    /** The original image's height. */
    protected int origHeight;
    /** The animation controller. */
    protected TimingController timingController;
    /** Frames of the animation. */
    protected BufferedImage[] frames;
    /** The duration of the animation (in ms). */
    protected int duration;
    /** The current frame of the animation. */
    protected int frame;

    /**
     * Creates a new panel to display a swirl animation using the given image file,
     * frame count, and duration.
     *
     * @param imageFilename the original image file
     * @param frameCount the number of frames to render
     * @param duration the duration of the animation (in ms)
     */
    public ImageSwirlPanel( String imageFilename, int frameCount, int duration )
    {
        ImageIcon imageIcon = new ImageIcon(imageFilename);
        init(imageIcon,frameCount,duration);
    }

    /**
     * Creates a new panel to display a swirl animation using the given image file,
     * frame count, and duration.
     *
     * @param image the original image
     * @param frameCount the number of frames to render
     * @param duration the duration of the animation (in ms)
     */
    public ImageSwirlPanel( Image image, int frameCount, int duration )
    {
        ImageIcon imageIcon = new ImageIcon(image);
        init(imageIcon,frameCount,duration);
    }

    /**
     * Does all of the real work of creating the frames and setting up the
     * animation controller.
     *
     * @param icon the original image as an icon
     * @param frameCount the number of frames
     * @param duration the duration of the animation
     */
    private void init(ImageIcon icon, int frameCount, int dur)
    {
        frame = 0;//frameCount - 1;
        frames = new BufferedImage[frameCount];
        this.duration = dur;

        origWidth = icon.getIconWidth();
        origHeight = icon.getIconHeight();
        original = new BufferedImage(origWidth,origHeight,BufferedImage.TYPE_INT_ARGB);

        // draw the original image into the BufferedImage
        Graphics2D g = original.createGraphics();
        g.drawImage(icon.getImage(), 0, 0, origWidth, origHeight, null);
        g.dispose();

        frames[0] = original;
        for( int i = 1; i < frames.length; ++i ) {
            frames[i] = new BufferedImage(origWidth,origHeight,BufferedImage.TYPE_INT_ARGB);
        }

        /* Commented out to eliminate the project dependancy for the filters JAR file
        // initialize the filter
        TwirlFilter filter = new TwirlFilter();
        filter.setRadius(origWidth/2);
        float angle = 4*(float)Math.PI/frames.length;
        filter.setAngle(angle);

        // create the individual frames
        for( int i = 1; i < frames.length; ++i ) {
            //filter.setAngle(angle*i);
            //filter.filter(original,frames[i]);
            filter.setAngle(angle);
            filter.filter(frames[i-1],frames[i]);
        }
        */

        //setup the animation Cycle
        int resolution = 0;
        Cycle cycle = new Cycle(duration,resolution);

        // setup the animation Envelope
        double repeatCount = 1;
        int start = 0;
        Envelope.RepeatBehavior repeatBehavior = Envelope.RepeatBehavior.REVERSE;
        Envelope.EndBehavior endBehavior = Envelope.EndBehavior.HOLD;
        Envelope env = new Envelope(repeatCount,start,repeatBehavior,endBehavior);

        // setup the TimingController (the animation controller)
        timingController = new TimingController(cycle,env,this);
    }

    /**
     * Starts the animation.
     * 
     * @see TimingController#start()
     */
    public void startAnimation()
    {
        timingController.start();
    }

    /**
	 * Returns the current frame number.
	 *
	 * @see #setFrame(int)
	 * @return the frame number
	 */
	public int getFrame()
	{
		return frame;
	}

	/**
	 * Sets the current frame number.
	 *
	 * @see #getFrame()
	 * @param frame the frame number
	 */
	public void setFrame(int frame)
	{
		this.frame = frame;
	}

	/**
     * Paints the current frame of the swirling image.
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int i = frames.length - 1 - frame;
        g.drawImage(frames[i], 0,0, getBackground(), this);
    }

    /**
     * Does nothing.  This is called by the TimingController before
     * the animation begins.
     *
     * @see org.jdesktop.animation.timing.TimingTarget#begin()
     */
    public void begin()
    {
    	// do nothing
    }

    /**
     * Does nothing.  This is called by the TimingController after
     * the animation ends.
     *
     * @see org.jdesktop.animation.timing.TimingTarget#end()
     */
    public void end() 
    {
    	// do nothing
    }

    /**
     * Calculates and sets the frame number of the current frame based on the value
     * of <code>percent</code> and the frame count passed to the constructor.
     *
     * @see org.jdesktop.animation.timing.TimingTarget#timingEvent(long, long, float)
     * @param cycleElapsedTime elapsed time in this cycle of the animation
     * @param totalElapsedTime total elapsed animation time
     * @param percent the percentage of the current cycle that has elapsed
     */
    public void timingEvent(long cycleElapsedTime, long totalElapsedTime, float percent)
    {
        frame = Math.min((int)(frames.length * percent),frames.length-1);
        repaint();
    }

    /**
     * Overrides {@link javax.swing.JComponent#getPreferredSize()} to always
     * return the height and width of <code>original</code>.
     *
     * @see javax.swing.JComponent#getPreferredSize()
     * @return the size of <code>original</code>
     */
    @Override
	public Dimension getPreferredSize()
    {
        return new Dimension(origWidth, origHeight);
    }
}
