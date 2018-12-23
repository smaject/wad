
package com.airhacks.wad.watch.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author airhacks.com
 */
public interface FolderWatchService {

    static long POLLING_INTERVALL = 500;

    public static void listenForChanges(Path dir, Runnable listener) throws IOException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        checkForChanges(scheduler, POLLING_INTERVALL, dir, listener);
    }

    static void checkForChanges(ScheduledExecutorService scheduler, long initialTimeStamp, Path dir, Runnable changeListener) {
        long initialStamp = getFolderModificationId(dir);
        System.out.println("Initial timestamp " + initialStamp);
        boolean changeDetected = false;
        try {
            changeDetected = scheduler.schedule(() -> detectModification(dir, initialStamp), POLLING_INTERVALL, TimeUnit.MILLISECONDS).get();
            System.out.println("Change detected " + changeDetected);
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException("Scheduler error", ex);
        }
        if (changeDetected) {
            changeListener.run();
        }
        checkForChanges(scheduler, getFolderModificationId(dir), dir, changeListener);

    }

    static boolean detectModification(Path dir, long previousStamp) {
        long currentStamp = getFolderModificationId(dir);
        return previousStamp != currentStamp;
    }

    static long getFolderModificationId(Path dir) {
        try {
            return Files.list(dir).
                    mapToLong(FolderWatchService::getFileSize).
                    sum();
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
