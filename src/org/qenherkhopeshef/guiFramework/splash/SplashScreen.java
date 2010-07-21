/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 12 d�c. 2004
 *
 */
package org.qenherkhopeshef.guiFramework.splash;

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
public class SplashScreen {

	private String imgPath = "/jseshResources/images/splash.png";

	private Frame frame;
	
	private Image img;

	public SplashScreen(String imgPath) {
		this.imgPath= imgPath;
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
	
	private class PictureFrame extends Frame {
		public void update(Graphics g) {
			paint(g);
		}

		public void paint(Graphics g) {
			g.drawImage(img, 0, 0, frame.getWidth(), frame.getHeight(), frame);
		}
	}

}