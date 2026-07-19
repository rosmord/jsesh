package jsesh.utils.io;

import java.io.File;
import java.util.Optional;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventPublisher;
import org.qenherkhopeshef.observable.ObservableEventSupport;

/**
 * An observable reference to a directory.
 */
public class DirectoryHolder implements ObservableEventPublisher<DirectoryHolder.DirectoryEvent> {
    
    private File directory;
    private final ObservableEventSupport<DirectoryEvent> eventSupport = 
        new ObservableEventSupport<>();

    /**
     * Gets the directory (may not be available).
     * @return
     */
    public Optional<File> optDirectory() {
        return Optional.ofNullable(directory);        
    }

    /**
     * Sets the directory.
     * If an actual file is provided, it must exist and be a directory, else a directoryException is raised.
     * @param optDirectory a optional for a directory.
     */
    public void directory(Optional<File> optDirectory) {
        if (optDirectory.isPresent()) {
            File f = optDirectory.get();
            if (!f.exists() || !f.isDirectory()) {
                throw new DirectoryException("Not a valid directory: " + f);
            }
            File oldDirectory = directory;
            directory = f;
            // We notify in all cases,
            // as we may use this to update font lists.
            eventSupport.fireEvent(new DirectoryEvent(oldDirectory, directory));
        } else {
            File oldDirectory = directory;
            directory = null;
            if (oldDirectory != null) {
                eventSupport.fireEvent(new DirectoryEvent(oldDirectory, null));
            }
        }
        // do nothing yet.
    }

    /**
     * A (bit hackish) method to notify clients that something has changed.
     */
    public void forceRefresh() {
        eventSupport.fireEvent(new DirectoryEvent(directory, directory));
    }

    public static final class DirectoryException extends RuntimeException {
        public DirectoryException(String message) {
            super(message);
        }
    }


    public static final record DirectoryEvent(File oldDirectory, File newDirectory) {        
    }


    @Override
    public void addListener(ObservableEventListener<DirectoryEvent> listener) {
        eventSupport.addListener(listener);
    }

    @Override
    public void removeListener(ObservableEventListener<DirectoryEvent> listener) {
        eventSupport.removeListener(listener);
    }
}
