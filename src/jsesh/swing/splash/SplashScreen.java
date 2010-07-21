/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 12 d�c. 2004
 *
 */
package jsesh.swing.splash;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/**
 * @author Serge Rosmorduc
 */
public class SplashScreen extends Frame  {

    private Image img;

    
    public SplashScreen() {
        super();
        setUndecorated(true);
        buildImage();
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(img, 0);
        setSize(img.getWidth(this), img.getHeight(this));
        center();
        toFront();
        addMouseListener(new MouseAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            public void mouseClicked(MouseEvent e) {
                closeSplash();
            }
        });
    }
    
    /**
     * Displays this screen in a thread-clean way ?
     *
     */
    public void display() {
        Runnable r= new Runnable() {
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {
                setVisible(true);
            }
        };
            SwingUtilities.invokeLater(r);
    
                
    }

    /**
     * 
     */
    private void buildImage() {
        URL imgURL = getClass().getResource("/jseshResources/images/splash.png");
        img = new ImageIcon(imgURL).getImage(); 
    }

    private void center() {
        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2,
                (screen.height - frame.height) / 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Container#update(java.awt.Graphics)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Container#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }

    public void closeSplash() {
        img.flush();
        dispose();
    }

}