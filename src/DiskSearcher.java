import java.io.File;

public class DiskSearcher {

    static final int MILESTONES_QUEUE_CAPACITY = 60;
    static final int DIRECTORY_QUEUE_CAPACITY = 60;
    static final int RESULTS_QUEUE_CAPACITY = 60;

    public static void main(String[] args) throws InterruptedException {

        long startOfRunTime = System.currentTimeMillis();

        boolean isAudit = Boolean.parseBoolean(args[0]);
        String prefix = args[1];
        File root = new File(args[2]);
        File destination = new File(args[3]);
        int numOfSearchers = Integer.parseInt(args[4]);
        int numOfCopiers = Integer.parseInt(args[5]);
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<>(MILESTONES_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
        int id = 1;

        Thread scouter = new Thread(new Scouter(id, directoryQueue, root, milestonesQueue, isAudit));
        scouter.start();
        id++;

        Thread[] searchers = new Thread[numOfSearchers];
        for (int i = 0; i < numOfSearchers; i++) {
            searchers[i] = new Thread(new Searcher(id, prefix, directoryQueue, resultsQueue, milestonesQueue, isAudit));
            searchers[i].start();
            id++;
        }

        Thread[] copiers = new Thread[numOfCopiers];
        for (int i = 0; i < numOfCopiers; i++) {
            copiers[i] = new Thread(new Copier(id, destination, resultsQueue, milestonesQueue, isAudit));
            copiers[i].start();
            id++;
        }

        scouter.join();

        for (Thread searchersThread : searchers) {
            searchersThread.join();
        }

        for (Thread copiersThread : copiers) {
            copiersThread.join();
        }

        if (milestonesQueue != null) {
            String milestones = milestonesQueue.dequeue();
            while (milestones != null) {
                System.out.println(milestones);
                milestones = milestonesQueue.dequeue();
            }
        }

        long endOfRunTime = System.currentTimeMillis();
        System.out.println("total runtime: " + (endOfRunTime - startOfRunTime) + " milliseconds");
    }
}
