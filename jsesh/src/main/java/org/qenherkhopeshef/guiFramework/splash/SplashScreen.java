/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 12 d�c. 2004
 *
 */
package org.qenherkhopeshef.guiFramework.splash;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.VolatileImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Serge Rosmorduc
 */
public class SplashScreen {

	private String imgPath;

	private PictureFrame frame;
	
	private Image img;
	
	private ProgressionDisplay progressionDisplay= null;

	private int progressionLevel= -1;
	
	public SplashScreen(String imgPath) {
		this.imgPath= imgPath;
	}

	public SplashScreen() {
		this("/org/qenherkhopeshef/guiFramework/splash/default.png");
	}

	/**
	 * Ask for a certain display to be used for the advancement.
	 * @param progressionDisplay
	 */
	public void setProgressionDisplay(ProgressionDisplay progressionDisplay) {
		this.progressionDisplay = progressionDisplay;
	}
	
	/**
	 * Ask for a clock-like progression display.
	 */
	public void setClockDisplay() {
		setProgressionDisplay(new ClockDisplay());
	}
	
	private void buildFrame() {
		buildImage();
		frame= new PictureFrame();
		frame.setUndecorated(true);
		MediaTracker tracker = new MediaTracker(frame);
		tracker.addImage(img, 0);
		frame.setSize(img.getWidth(frame), img.getHeight(frame));
		center();
		frame.toFront();
		frame.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				closeSplash();
			}
		});
		frame.setVisible(true);
	}
	/**
	 * Displays this screen in a thread-clean way ?
	 * 
	 */
	public void display() {
		Runnable r = new Runnable() {
			public void run() {
				buildFrame();
			}
		};
		SwingUtilities.invokeLater(r);
	}

	/**
     * 
     */
	private void buildImage() {
		URL imgURL = getClass()
				.getResource(imgPath);
		img = new ImageIcon(imgURL).getImage();
	}

	private void center() {
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle rect = frame.getBounds();
		frame.setLocation((screen.width - rect.width) / 2,
				(screen.height - rect.height) / 2);
	}

	


	public void closeSplash() {
		img.flush();
		frame.dispose();
	}
	
	/**
	 * A frame displaying a picture and an advancement clock.
	 * @author rosmord
	 *
	 */
	private class PictureFrame extends JFrame {
				
		public void update(Graphics g) {
			paint(g);
		}

		public void paint(Graphics g) {
			VolatileImage buff = frame.createVolatileImage(frame.getWidth(), frame.getHeight());			
			Graphics2D g2d= buff.createGraphics();
			g2d.drawImage(img, 0, 0, frame.getWidth(), frame.getHeight(),null);
			if (progressionDisplay != null && progressionLevel > 0) {
				progressionDisplay.drawAdvancement(g2d, frame.getWidth(), frame.getHeight(),getProgressionLevel());
			}
			g2d.dispose();
			g.drawImage(buff, 0, 0, frame.getWidth(), frame.getHeight(), frame);			
		}
	}

	/**
	 * Sets the progression level.
	 * <p>nothing is displayed if it is negative.
	 * 
	 * <p> The actual interpretation depends on the ProgressionDisplay used. A bar would mean a percentage.
	 * A clock would mean degrees.
	 * 
	 * @param n
	 */
	public synchronized void setProgression(int n) {
		this.progressionLevel= n;
		// Repaint can be called from anywhere.
		if (frame != null)
			frame.repaint();
	}
	
	public synchronized int getProgressionLevel() {
		return progressionLevel;
	}
	
	/**
	 * Demo...
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		SplashScreen splash= new SplashScreen();
		splash.setClockDisplay();
		splash.display();
		for (int i= 0; i < 100; i++) {
			splash.setProgression(i);
			Thread.sleep(100);
		}
		splash.closeSplash();
	}
}