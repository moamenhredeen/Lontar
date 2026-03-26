package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteExternallyChanged;
import app.orgx.desktop.core.events.NoteCreated;
import app.orgx.desktop.core.events.NoteDeleted;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path rootPath;
    private final EventBus eventBus;
    private volatile boolean running = true;
    private WatchService watchService;

    public FileWatcher(Path rootPath, EventBus eventBus) {
        this.rootPath = rootPath;
        this.eventBus = eventBus;
    }

    public void start() {
        var thread = new Thread(this, "file-watcher");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerTree(rootPath);

            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (ClosedWatchServiceException | InterruptedException e) {
                    break;
                }

                var dir = (Path) key.watchable();
                for (var event : key.pollEvents()) {
                    var kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    var path = dir.resolve(((WatchEvent<Path>) event).context());

                    if (!path.toString().endsWith(".md")) continue;

                    if (kind == ENTRY_CREATE) {
                        eventBus.publish(new NoteCreated(path));
                    } else if (kind == ENTRY_DELETE) {
                        eventBus.publish(new NoteDeleted(path));
                    } else if (kind == ENTRY_MODIFY) {
                        eventBus.publish(new NoteExternallyChanged(path));
                    }
                }

                if (!key.reset()) break;
            }
        } catch (IOException e) {
            // Log and stop
        }
    }

    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
