/*
 * Created on 26 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.application;

/**
 * A "Border" that adds a picture as background to some component.
 * From example code by Dr. Laslo Jamf.
 *
 */

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
 
public class BackgroundBorder implements Border {
    private final BufferedImage image;
 
    public BackgroundBorder(BufferedImage image) {
        this.image = image;
    }
 
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        x += (width-image.getWidth())/2;
        y += (height-image.getHeight())/2;
        ((Graphics2D) g).drawRenderedImage(image, AffineTransform.getTranslateInstance(x,y));
    }
 
    public Insets getBorderInsets(Component c) {
        return new Insets(0,0,0,0);
    }
 
    public boolean isBorderOpaque() {
        return true;
    }
 
    //demo
    public static void main(String[] args) throws IOException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JTextArea area = new JTextArea();
        area.setForeground(Color.GREEN);
        area.setOpaque(false);
        area.read(new StringReader("CentredBackgroundBorder.java"), null);
        String url = "file:/home/rosmord/Images/pap.png";
        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setOpaque(false);
        sp.setViewportBorder(new BackgroundBorder(ImageIO.read(new URL(url))));
 
        JFrame f = new JFrame("BackgroundBorderExample");
        f.getContentPane().add(sp);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600,400);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
