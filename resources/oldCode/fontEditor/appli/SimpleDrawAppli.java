/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */

package jsesh.fontEditor.appli;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jsesh.fontEditor.gui.SimpleDrawBarreDeCommande;
import jsesh.fontEditor.gui.SimpleDrawView;
import jsesh.fontEditor.gui.SimpleDrawViewControl;
import jsesh.fontEditor.gui.ToolContext;
import jsesh.fontEditor.model.Cercle;
import jsesh.fontEditor.model.GlyphModel;
import jsesh.fontEditor.model.Point;
import jsesh.fontEditor.model.Segment;

/**
 * Application permettant d'afficher un dessin.
 * 
 * @author rosmord
 */

public class SimpleDrawAppli {

	private ToolContext contexte;
	private JFrame frame;
	private GlyphModel dessin;
	private JMenuBar menu;
	private SimpleDrawView vue, petiteVue;
	private SimpleDrawViewControl controle;
	private SimpleDrawBarreDeCommande barre;

	private void creerMenus() {
		menu = new JMenuBar();
		JMenu edit = new JMenu("Edit");
		edit.add(new AbstractAction("undo") {
			public void actionPerformed(ActionEvent e) {
				contexte.undo();
			}
		});
		edit.add(new AbstractAction("redo") {
			public void actionPerformed(ActionEvent e) {
				contexte.redo();
			}
		});
		JMenu voir = new JMenu("Vue");
		voir.add(new AbstractAction("Zoom in") {
			public void actionPerformed(ActionEvent e) {
				vue.setScale(vue.getScale() * 2.0);
			}
		});
		voir.add(new AbstractAction("Zoom out") {
			public void actionPerformed(ActionEvent e) {
				vue.setScale(vue.getScale() / 2.0);
			}
		});
		voir.add(new AbstractAction("Zoom 100%") {
			public void actionPerformed(ActionEvent e) {
				vue.setScale(1.0);
			}
		});
		menu.add(edit);
		menu.add(voir);

		frame.setJMenuBar(menu);
	}

	public SimpleDrawAppli() {
		frame = new JFrame("SimpleDraw 1");
		dessin = new GlyphModel();
		vue = new SimpleDrawView(dessin);
		petiteVue = new SimpleDrawView(dessin);
		contexte = new ToolContext(vue);
		controle = new SimpleDrawViewControl(contexte);
		barre = new SimpleDrawBarreDeCommande(controle);
		creerMenus();

		petiteVue.setSize(100, 100);
		petiteVue.setScale(0.25);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(barre, BorderLayout.WEST);
		frame.getContentPane().add(vue, BorderLayout.CENTER);
		frame.getContentPane().add(petiteVue, BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		SimpleDrawAppli appli = new SimpleDrawAppli();
		appli.dessin.add(new Cercle(new Point(100, 100), 100));
		appli.dessin.add(new Cercle(new Point(10, 10), 10));
		appli.dessin.add(new Cercle(new Point(0.5, 0.5), 15.2));
		appli.dessin.add(new Segment(new Point(10, 0), new Point(100, 40)));
	}
}
