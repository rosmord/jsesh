/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.dialogs.JSearchFolderPanel;
import jsesh.jhotdraw.dialogs.JSearchPanel;
import jsesh.jhotdraw.search.CorpusSearchPresenter;
import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.qenherkhopeshef.jhotdrawChanges.ActiveViewAwareApplication;

import javax.swing.*;

/**
 * Search folder for text action.
 *
 * Simple search functionality for JSesh.
 *
 * @author rosmord
 */
public final class FindInFolderAction extends AbstractApplicationAction {

    public static final String ID = "edit.findInFolder";

    private final CorpusSearchPresenter corpusSearchPresenter;
    
    public FindInFolderAction(ActiveViewAwareApplication app, CorpusSearchPresenter corpusSearchPresenter) {
        super(app);
        this.corpusSearchPresenter= corpusSearchPresenter;
        BundleHelper.getInstance().configure(this);        
        corpusSearchPresenter= new CorpusSearchPresenter(app);
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
            corpusSearchPresenter.show();
    }
}


