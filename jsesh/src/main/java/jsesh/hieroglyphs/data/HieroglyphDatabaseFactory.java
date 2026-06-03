/*
 * HieroglyphDatabaseRepository.java
 *
 * Created on 27 sept. 2007, 17:40:08
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.hieroglyphs.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import jsesh.hieroglyphs.data.io.SignDescriptionBuilder;
import jsesh.hieroglyphs.data.io.SignDescriptionBuilderToHieroglyphDatabaseAdapter;
import jsesh.hieroglyphs.data.io.SignDescriptionReader;
import jsesh.hieroglyphs.resources.HieroglyphResources;
import jsesh.resources.ResourcesManager;

/**
 * A convenient factory to initialize a hieroglyph database.
 * 
 * @author rosmord
 */
public class HieroglyphDatabaseFactory {

    private HieroglyphDatabaseFactory() {}


    /**
     * Build a hieroglyph database, including user-defined descriptions.
     * 
     * It will include the codes from the given hieroglyphCodesSource,  the descriptions from the embedded XML file, and eventually from the user-defined XML file.
     * @param hieroglyphCodesSource
     * @return
     */
    public static SimpleHieroglyphDatabase buildWithUserDefinitions(HieroglyphCodesSource hieroglyphCodesSource) {
        SimpleHieroglyphDatabase database = new SimpleHieroglyphDatabase(hieroglyphCodesSource);
        addEmbeddedDescriptionsTo(database);
        addUserDescriptionsTo(database);
        return database;
    }

     /**
     * Build a hieroglyph database without user-defined descriptions.
     * 
     * It will include the codes from the given hieroglyphCodesSource and the default descriptions.
     * @param hieroglyphCodesSource
     * @return
     */
    public static SimpleHieroglyphDatabase buildPlainDefault(HieroglyphCodesSource hieroglyphCodesSource) {
        SimpleHieroglyphDatabase database = new SimpleHieroglyphDatabase(hieroglyphCodesSource);
        addEmbeddedDescriptionsTo(database);
        addUserDescriptionsTo(database);
        return database;
    }


    /**
     * adds the user-defined signs descriptions to an existing database.
     * @param database
     */
    private static void addUserDescriptionsTo(SimpleHieroglyphDatabase database) {
        try {
            SignDescriptionBuilderToHieroglyphDatabaseAdapter adapter = new SignDescriptionBuilderToHieroglyphDatabaseAdapter(
                    database);
             SignDescriptionReader reader = new SignDescriptionReader(
                    adapter);
            // We don't want errors in the user-defined file to
            // prevent the software from starting
            // Read user signs descriptions (if any is available)
            File f = getUserSignDefinitionFile();
            if (f.exists()) {

                // First, a dummy run to check the content is ok...
                // Note that this dummy run is more or less useless.
                // We stop JSesh if there is a problem anyway
                // (the reason is that we don't know what to do with
                // the broken file).

                try (InputStream in2 = new FileInputStream(f)) {
                    SignDescriptionReader dummyReader = new SignDescriptionReader(new DummyAdapter());
                    dummyReader.readSignDescription(in2);
                }
                // If we are here, the reading was ok... Do it for real.
                try (InputStream in = new FileInputStream(f)) {
                    reader.readSignDescription(in);
                }
            }
        } catch (IOException | RuntimeException | SAXException e) {
            throw new RuntimeException("Your sign definition file " + getUserSignDefinitionFile().getAbsolutePath()
                    + " is corrupt. Move it to another place.", e);
        }
    }

    /**
     * Read the XML files containing the JSesh officiel signs descriptions.
     *
     * @throws IOException
     * @throws SAXException
     *
     */
    private static void addEmbeddedDescriptionsTo(SimpleHieroglyphDatabase database) {
        try {
            SignDescriptionBuilderToHieroglyphDatabaseAdapter adapter = new SignDescriptionBuilderToHieroglyphDatabaseAdapter(
                    database);
            // Read standard ressource...
            SignDescriptionReader reader = new SignDescriptionReader(
                    adapter);
            try (InputStream in1 = HieroglyphResources.getSignsDescriptionXML();) {
                // Read "standard" signs description

                if (in1 != null) {
                    reader.readSignDescription(in1);
                } else {
                    throw new RuntimeException(
                            "Standard Sign description file not found - it's a bug in JSesh distribution. Please report");
                }
            }
        } catch (IOException | RuntimeException | SAXException e) {
            throw new RuntimeException("Your sign definition file " + getUserSignDefinitionFile().getAbsolutePath()
                    + " is corrupt. Move it to another place.", e);
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
