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
package jsesh.search;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.MDCPosition;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * A Search query in a full corpus (folder-based).
 * <p>
 * Instances should not be reused for other queries.</p>
 * As performing such a query can be quite time-consuming, the query performance
 * is represented by an object, which can be run in a SwingWorker.
 * <p>
 * TODO: modify the system so that we can get intermediary results.
 * <p>
 * The search itself is mono-threaded; the result will probably be dispatched in
 * a multi-thread environment, like a SwingWorker.</p>
 * <p>
 * Note that everything there is </p>
 */
public class CorpusSearch {

    private final Path searchRoot;
    private final MdCSearchQuery query;
    private final String[] extensions;
    private final Iterator<Path> fileIterator;
    private final List<CorpusSearchHit> result = new ArrayList<>();

    /**
     * Create a search in a certain folder (which will be recursively search).
     *
     * @param searchRoot the folder to search.
     * @param query the query to perform.
     */
    public CorpusSearch(Path searchRoot, MdCSearchQuery query) {
        try {
            this.searchRoot = searchRoot;
            this.query = query;
            extensions = new String[]{
                ".gly", ".GLY", ".hie", ".HIE"
            };
            fileIterator = Files
                    .walk(searchRoot)
                    .filter(this::isJSeshPath)
                    .iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MdCSearchQuery getQuery() {
        return query;
    }

    public Path getSearchRoot() {
        return searchRoot;
    }

    private boolean isJSeshPath(Path p) {
        Path filePath = p.getFileName();
        String name = filePath.toString();
        for (String ext : extensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNext() {
        return fileIterator.hasNext();
    }

    /**
     * Next search step.
     *
     * <p>
     * Prerequisite : hashNext() must be true.</p>
     * <p>
     * Postcondition: full result is updated.</p>
     *
     * @return the matches found for the current file.
     */
    public List<CorpusSearchHit> searchNext() {
        List<CorpusSearchHit> hits;
        Path file = fileIterator.next();

        hits = new ArrayList<>();
        try {
            MDCDocumentReader reader = new MDCDocumentReader();
            MDCDocument mdcDocument = reader.loadFile(file.toFile());
            List<MDCPosition> positions = query.doSearch(mdcDocument.getHieroglyphicTextModel().getModel());
            // see information about MDCPosition for more details.
            for (MDCPosition pos : positions) {
                hits.add(new CorpusSearchHit(file, pos.getIndex()));
            }
            result.addAll(hits);
        } catch (MDCSyntaxError | IOException e) {
            System.err.println("Error in file " + file.toString());
        }

        return hits;
    }

    /**
     * Returns the full search result (once all files have been searched).
     *
     * @return
     */
    public List<CorpusSearchHit> getResult() {
        return result;
    }
}
