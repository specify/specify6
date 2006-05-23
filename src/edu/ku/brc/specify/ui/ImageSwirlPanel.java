package edu.ku.brc.specify.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.KeyFrames;
import org.jdesktop.animation.timing.KeyValues;
import org.jdesktop.animation.timing.ObjectModifier;
import org.jdesktop.animation.timing.PropertyRange;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

import com.jhlabs.image.TwirlFilter;

@SuppressWarnings("serial")
public class ImageSwirlPanel extends JPanel implements TimingTarget {
    protected BufferedImage original;
    protected int origWidth;
    protected int origHeight;
    protected TimingController timingController;
    protected TwirlFilter filter;
    protected BufferedImage[] frames;
    protected int duration;
    protected int index;
    
    public ImageSwirlPanel( String imageFilename, int frameCount, int duration ) {
        ImageIcon imageIcon = new ImageIcon(imageFilename);
        init(imageIcon,frameCount,duration);
    }
    
    public ImageSwirlPanel( Image image, int frameCount, int duration ) {
        ImageIcon imageIcon = new ImageIcon(image);
        init(imageIcon,frameCount,duration);
    }
    
    private void init(ImageIcon icon, int frameCount, int duration) {
        index = frameCount - 1;
        frames = new BufferedImage[frameCount];
        this.duration = duration;
        
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

        // initialize the filter
        filter = initTwirlFilter();
        float angle = 2*(float)Math.PI/frames.length;
        filter.setAngle(angle);

        // create the individual frames
        for( int i = 1; i < frames.length; ++i ) {
            filter.setAngle(angle*i);
            filter.filter(original,frames[i]);
        }
        
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
    
    // run the animation
    public void startAnimation() {
        timingController.start();
    }
    
    // initialize the filter
    protected TwirlFilter initTwirlFilter() {
        TwirlFilter filter = new TwirlFilter();
        filter.setRadius(origWidth);
        return filter;
    }
    
    public void setIndex(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return this.index;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(frames[frames.length - 1 - index], getX(), getY(), getBackground(), this);
    }
    
    public void begin() {
    }
    
    public void end() {
    }
    
    // modify the index appropraitely for the percent of the animation that is complete
    public void timingEvent(long cycleElapsedTime, long totalElapsedTime, float percent) {
        index = Math.min((int)(frames.length * percent),frames.length-1);
        System.out.println(index);
        repaint();
    }
    
    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        final ImageSwirlPanel isp = new ImageSwirlPanel("/Users/jstewart/Desktop/splashfish.png",20,1000);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(600,600);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(isp);
                f.setVisible(true);
                isp.startAnimation();
            }
        });
    }
}
