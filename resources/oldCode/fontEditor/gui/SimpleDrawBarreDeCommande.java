package jsesh.fontEditor.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JToolBar;

import jsesh.fontEditor.model.DeplacerOutil;

public class SimpleDrawBarreDeCommande
	extends JToolBar
	implements ActionListener {

	/** 
	 * ButtonGroup pour gérer l'exclusion mutuelle des boutons
	 * Dans un ButtonGroup, un seul bouton peut être sélectionné à
	 * un instant donné.
	 * */
	private ButtonGroup buttons;
	
	private SimpleDrawViewControl control;
	
	public SimpleDrawBarreDeCommande(SimpleDrawViewControl c) {
		// La barre d'outils est verticale :
		super(VERTICAL);
		buttons= new ButtonGroup();
		control= c;
			
		// Ajout des boutons :
		ajouterBouton("Déplacer", new DeplacerOutil());
		ajouterBouton("Créer cercle", new CreateurCercleOutil());
		
	}
	
	private void ajouterBouton(String titre, Tool outil) {
		ToolButton b = new ToolButton(titre, outil);
		b.addActionListener(this);
		add(b);
		buttons.add(b);
	}

	/**
	 * Sélectionne l'tool correct.
	 * @param e
	 */
	
	public void actionPerformed(ActionEvent e) {
		control.setOutil(((ToolButton) e.getSource()).getTool());
	}
}
