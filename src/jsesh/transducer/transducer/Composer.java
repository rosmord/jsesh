/*
 * Created on 10 avr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jsesh.transducer.transducer;

/**
 * Knows how to compose a grounded transducer and a rule transducer.
 * @author rosmord
 */
public class Composer {
	// Algo :
	// Tant qu'il y a des noeuds à traiter,
	// Etendre les noeuds
	
	// etendre un noeud :
	// 
	
	// Noeuds "medians" des règles
	// a) étendre les epsilons de l'entrée
	// b) si la partie "règle" est epsilon
	// 		* pour un noeud final, générer le noeud de la fin de règle
	//		* pour un noeud non final, générer le noeud suivant
	// c) sinon
	// 		voir tous les arcs de l'entrée
	//			s'ils matchent, créer le noeud d'arrivée et l'arc
	
	// Noeuds "de choix"
	// a) Epsilons arcs de l'entrée
	// b) pour toutes les règles ayant un epsilon arc pour 
	//    premier noeud, créer le noeud correspondant
	// c) créer les noeuds pour le reste.
}
