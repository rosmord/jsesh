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
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.swingUtils.GraphicsUtils;

/**
 * A simple, JDK 1.5 compatible, splash screen.
 * TODO : either use JDK 1.6, or cleanup/check media tracking problems on this one.
 * @author Serge Rosmorduc
 */
public class SplashScreen {

	private String imgPath;
	private PictureFrame frame;
	private Image img;
	private ProgressionDisplay progressionDisplay = null;
	private int progressionLevel = -1;
	private List<SplashMessageText> messages = new ArrayList<SplashMessageText>();

	public SplashScreen(String imgPath) {
		this.imgPath = imgPath;
		buildImage();
	}

	public SplashScreen() {
		this("/org/qenherkhopeshef/guiFramework/splash/default.png");
	}

	public void addMessage(SplashMessageText message) {
		this.messages.add(message);
	}

	/**
	 * Ask for a certain display to be used for the advancement.
	 * 
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
		// buildImage();
		
		frame = new PictureFrame();		
		center();
		// Dummy draw. Ensure fonts needed for the messages have been read.
		paintPicture(new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB));
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
		URL imgURL = getClass().getResource(imgPath);
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
		if (frame != null)
			frame.dispose();
	}

	private void paintPicture(Image buff) {
		Graphics2D g2d = (Graphics2D) buff.getGraphics();
		g2d.drawImage(img, 0, 0, buff.getWidth(null), buff.getHeight(null),
				null);
		if (progressionDisplay != null && progressionLevel > 0) {
			progressionDisplay.drawAdvancement(g2d, buff.getWidth(null),
					buff.getHeight(null), getProgressionLevel());
		}
		if (!messages.isEmpty()) {
			GraphicsUtils.antialias(g2d);
			for (SplashMessageText m : messages) {
				m.paint(g2d);
			}
		}
		g2d.dispose();
	}
	
	/**
	 * A frame displaying a picture and an advancement clock.
	 * 
	 * @author rosmord
	 * 
	 */
	@SuppressWarnings("serial")
	private class PictureFrame extends JFrame {

		// A number of attempts have been done here to speed up the first
		// rendering of the splash screen, but
		// in vain.

		VolatileImage buff = null;

		public PictureFrame() {
			setUndecorated(true);
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(img, 0);// (we should wait for the picture to be loaded.)
			try {
				tracker.waitForID(0);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			buff = preparePicture();
			setSize(img.getWidth(null), img.getHeight(null));
			toFront();
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					closeSplash();
				}
			});
		}

		public void update(Graphics g) {
			paint(g);
		}

		public void paint(Graphics g) {
			if (buff == null || buff.contentsLost())
				buff = preparePicture();
			// g.drawImage(buff, 0, 0, frame.getWidth(), frame.getHeight(),
			// frame);
			g.drawImage(buff, 0, 0, null);
			buff = preparePicture();
		}

		/**
		 * @return
		 */
		private VolatileImage preparePicture() {
			VolatileImage buff;
			buff = this.createVolatileImage(this.getWidth(), this.getHeight());
			if (buff != null) {
				paintPicture(buff);
			}
			return buff;
		}
	}

	/**
	 * Sets the progression level.
	 * <p>
	 * nothing is displayed if it is negative.
	 * 
	 * <p>
	 * The actual interpretation depends on the ProgressionDisplay used. A bar
	 * would mean a percentage. A clock would mean degrees.
	 * 
	 * @param n
	 */
	public synchronized void setProgression(int n) {
		this.progressionLevel = n;
		// Repaint can be called from anywhere.
		if (frame != null) {
			frame.repaint();
		}
	}

	public synchronized int getProgressionLevel() {
		return progressionLevel;
	}

	/**
	 * Demo...
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		SplashScreen splash = new SplashScreen();
		splash.setClockDisplay();
		splash.display();
		for (int i = 0; i < 100; i++) {
			splash.setProgression(i);
			Thread.sleep(100);
		}
		splash.closeSplash();
	}
}