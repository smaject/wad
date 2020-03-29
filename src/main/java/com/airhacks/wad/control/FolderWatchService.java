
package com.airhacks.wad.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

/**
 * @author airhacks.com
 */
public interface FolderWatchService {

    long POLLING_INTERVALL = 500;

    String POM = "pom.xml";

    static void listenForChanges(Path baseDir, Runnable listener, boolean isMainModule) throws IOException {
        Path dir = Paths.get(baseDir.toString(), "src/main");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        System.out.println("Register listener for dir: " + dir);
        if (!isMainModule) {
            CompletableFuture.runAsync(() -> checkForChanges(scheduler, dir, listener));
        } else {
            checkForChanges(scheduler, dir, listener);
        }
    }

    static void checkForChanges(ScheduledExecutorService scheduler, Path dir, Runnable changeListener) {
        long initialStamp = getProjectModificationId(dir);
        long currentStamp;
        while (true) {
            try {
                final long previous = initialStamp;
                currentStamp = scheduler.
                        schedule(() -> detectModification(dir), POLLING_INTERVALL, TimeUnit.MILLISECONDS).
                        get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new IllegalStateException("Scheduler error", ex);
            }
            if (initialStamp != currentStamp) {
                changeListener.run();
                initialStamp = currentStamp;
            }
        }
    }

    static long getPomModificationStamp() {
        return getFileSize(Paths.get(POM));
    }

    static long detectModification(Path dir) {
        return getProjectModificationId(dir);
    }

    static long getProjectModificationId(Path dir) {
        try {
            long modificationId = Files.walk(dir).
                    filter(Files::isRegularFile).
                    mapToLong(FolderWatchService::getFileSize).
                    sum();
            modificationId += getPomModificationStamp();
            return modificationId;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot list files", ex);
        }
    }

    static long getFileSize(Path p) {
        try {
            return Files.size(p);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot obtain FileTime", ex);
        }
    }


}
