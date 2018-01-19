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
package org.qenherkhopeshef.jsesh.codeDumper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.utilities.HieroglyphCodesExtractor;

/**
 * Dump the codes from various JSesh files.
 *
 * @author rosmord
 */
public class CodeDump {

    /**
     * Dump the codes from a folder or JSesh file into a destination folder.
     *
     * The code files will contain only the codes (respecting line breaks,
     * though). Each code file will have the same name as the corresponding
     * JSesh file, but with an appended ".txt".
     *
     * @param sourcePath a source folder containing JSesh files...
     * @param destinationFolder a destination folder for the result.
     */
    public void dumpFolders(Path sourcePath, Path destinationFolder) throws IllegalArgumentException, IOException {
        if (!Files.isDirectory(destinationFolder)) {
            throw new IllegalArgumentException(destinationFolder.getFileName() + " should be a folder");
        }
        Files.walk(sourcePath).forEach(p -> {
            if (!Files.isDirectory(p) && matchMdcSuffix(p)) {
                dumpFile(p, destinationFolder);
            }
        });
    }

    public void dumpFile(Path sourceFile, Path destinationFolder) {
        try {
            Path destinationFile = destinationFolder.resolve(sourceFile.getFileName() + ".txt");
            List<List<String>> lines = extractCodes(sourceFile);
            try (BufferedWriter out = Files.newBufferedWriter(destinationFile);) {
                for (List<String> line : lines) {
                    if (!line.isEmpty()) {
                        out.write(String.join(" ", line));
                        out.write("\n");
                    }
                }
            }
        } catch (IOException | MDCSyntaxError e) {
            System.err.println("Ignored file : " + sourceFile.getFileName());
        }
    }

    private List<List<String>> extractCodes(Path sourceFile) throws MDCSyntaxError, IOException {
        MDCDocumentReader documentReader = new MDCDocumentReader();
        MDCDocument doc = documentReader.loadFile(sourceFile.toFile());
        TopItemList topItemList = doc.getHieroglyphicTextModel().getModel();
        HieroglyphCodesExtractor codesExtractor = new HieroglyphCodesExtractor(true);
        List<List<String>> lines = codesExtractor.extractHieroglyphLines(topItemList);
        return lines;
    }

    private boolean matchMdcSuffix(Path p) {
        String[] suffixes = {".gly", ".GLY", ".hie", ".HIE"};
        return Arrays.asList(suffixes).stream().anyMatch(
                suff -> FileSystems.getDefault().getPathMatcher("glob:**" + suff).matches(p)
        );
    }

}
