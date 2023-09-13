package process;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Main {
    
    private Map<String,String> translationMap = new HashMap<>();
    private Set<String> processedStrings = new TreeSet<>();

    String translations[] = {
        "arabic_translation_base.txt",
        "corr1.txt",
        "corr2.txt"
        //,"arabic_corr1.txt"
    };

    private void loadMap() throws IOException {
        for (String fname: translations) {
            List<String> lines = Files.readAllLines(Path.of(fname), StandardCharsets.UTF_8);
            for (String line: lines) {
                String [] elts = line.split("\t", -1);
                if (elts.length != 2) {
                    System.err.println("Problem with line "+ line);                                
                } else {
                    String english = elts[0];
                    String arabic = elts[1];
                    translationMap.put(english.toLowerCase(), arabic);
                }
            }
        }
    }

    public void processFile(Path inFile, Path outFile) throws IOException {
        List<String> lines = Files.readAllLines(inFile, StandardCharsets.UTF_8);
        try (BufferedWriter out = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            for (String line: lines) {
                boolean keepLine = true;
                if (line.startsWith("#") || line.trim().isBlank()) {
                    // Do nothing...
                } else {
                    String [] content = line.split("=",2);
                    if (content.length == 2) {
                        String key = content[0];
                        String value= content[1];
                        if (
                            key.endsWith(".accelerator")
                            || key.endsWith(".accelerator.default")
                            || key.endsWith(".mnemonic.default")
                            || key.endsWith(".mnemonic")                            
                            || key.endsWith(".icon")
                            || key.endsWith(".mnemonic.mac")
                            || key.endsWith(".AcceleratorKey")   
                            || key.endsWith(".AcceleratorKey[mac]")   
                            || key.endsWith(".iconMdC")                                                        
                            || key.endsWith(".IconMdC")      
                            || key.startsWith("accelerator")
                            || key.endsWith("accelerator.mac")
                        ) {
                            continue;
                        }
                        if (translationMap.containsKey(value.toLowerCase())) {
                            out.write(key);
                            out.write('=');
                            out.write(translationMap.get(value.toLowerCase()));
                            out.write("\n");
                            processedStrings.add(value.toLowerCase());
                            keepLine = false;
                        } else {
                            System.err.println("NOT FOUND "+ line);
                        }
                    } // else nothing... keep line
                }
                if (keepLine) {
                    out.write(line);
                    out.write("\n");
                }
            }
        }
    }   
    public void run() throws IOException {
        loadMap();
        String JSESH = "../jseshLabels/src/main/resources/jsesh/resources/labels.properties";
        String JHOTDRAW = "../jhotdrawfw/src/main/java/org/jhotdraw_7_6/app/Labels.properties";
        // jsesh
        processFile(Path.of(JSESH), Path.of("jsesh.txt"));
        // jhotdraw
        processFile(Path.of(JHOTDRAW), Path.of("jhotdraw.txt"));
        // what is left ?
        Set<String> strings = new HashSet<>(translationMap.keySet()).stream().map(s -> s.toLowerCase()).collect(Collectors.toSet());
        strings.removeAll(processedStrings);
        System.err.println("Strings left:");
        for (String s : strings) {
            System.err.println(s);
        }
    }

    public static void main(String[] args) throws IOException {
        new Main().run();    
    }
}
