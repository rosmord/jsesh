/*
 * HieroglyphDatabaseRepository.java
 *
 * Created on 27 sept. 2007, 17:40:08
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.hieroglyphs.data;

import jsesh.hieroglyphs.data.io.SignDescriptionBuilderToHieroglyphDatabaseAdapter;
import jsesh.hieroglyphs.data.io.SignDescriptionReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import jsesh.hieroglyphs.data.io.SignDescriptionBuilder;
import jsesh.hieroglyphs.resources.HieroglyphResources;

import jsesh.resources.ResourcesManager;

import org.xml.sax.SAXException;

/**
 * A static repository for the database about hieroglyphic sources. Aggregates
 * information from JSesh and information from the user.
 *
 * @author rosmord
 */
public class HieroglyphDatabaseRepository {

    private ManuelDeCodage basicManuelDeCodageManager = ManuelDeCodage
            .getInstance();

    private static SimpleHieroglyphDatabase instance = buildInstance();

    private static SimpleHieroglyphDatabase buildInstance() {
        SimpleHieroglyphDatabase database = new SimpleHieroglyphDatabase(ManuelDeCodage.getInstance());
        readFiles(database);
        return database;
    }

    public static HieroglyphDatabaseInterface getHieroglyphDatabase() {
        return instance;
    }

    /**
     * Read the XML files containing the signs descriptions.
     *
     * @throws IOException
     * @throws SAXException
     *
     */
    private static void readFiles(SimpleHieroglyphDatabase instance) {
        try {
            SignDescriptionBuilderToHieroglyphDatabaseAdapter adapter
                    = new SignDescriptionBuilderToHieroglyphDatabaseAdapter(instance);
            // Read standard ressource...
            SignDescriptionReader reader = new SignDescriptionReader(
                    adapter);
            try (InputStream in1 = HieroglyphResources.getSignsDescriptionXML();) {
                // Read "standard" signs description

                if (in1 != null) {
                    reader.readSignDescription(in1);
                } else {
                    throw new RuntimeException("Standard Sign description file not found - it's a bug in JSesh distribution. Please report");
                }
            }

            // We don't want errors in the user-defined file to
            // prevent the software from starting
            // Read user signs descriptions (if any is available)
            File f = getUserSignDefinitionFile();
            if (f.exists()) {

                // First, a dummy run to check the content is ok...
                try (InputStream in2 = new FileInputStream(f)) {
                    SignDescriptionReader dummyReader
                            = new SignDescriptionReader(new DummyAdapter());
                    dummyReader.readSignDescription(in2);
                }
                // If we are here, the reading was ok... Do it for real.
                try (InputStream in = new FileInputStream(f)) {
                    reader.readSignDescription(in);
                }
            }
        } catch (IOException | RuntimeException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getUserSignDefinitionFile() {
        File f = ResourcesManager.getInstance().getUserPrefsDirectory();
        f = new File(f, "signs_definition.xml");
        return f;
    }

    private static class DummyAdapter implements SignDescriptionBuilder {

        @Override
        public void addTransliteration(String currentSign, String transliteration, String use, String type) {
        }

        @Override
        public void addVariant(String currentSign, String baseSign, String isSimilar, String degree) {
        }

        @Override
        public void addPartOf(String currentSign, String baseSign) {
        }

        @Override
        public void addDeterminativeValue(String currentSign, String category) {
        }

        @Override
        public void addDeterminative(String category, String lang, String label) {
        }

        @Override
        public void addTagToSign(String currentSign, String tag) {
        }

        @Override
        public void addTagLabel(String tag, String lang, String label) {
        }

        @Override
        public void addSignDescription(String currentSign, String text, String currentLang) {
        }

        @Override
        public void addPhantom(String currentSign, String baseSign, String existsIn) {
        }

        @Override
        public void setSignAlwaysDisplay(String currentSign) {
        }

        @Override
        public void addTagCategory(String tag) {
        }

    }

}
