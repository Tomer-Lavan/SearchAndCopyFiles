import java.io.File;
import java.io.FilenameFilter;

public class Searcher implements Runnable {

    SynchronizedQueue<File> directoryQueue;
    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> auditingQueue;
    String prefix;
    int id;
    boolean isAudit;

    public Searcher(int id, String prefix, SynchronizedQueue<File> directoryQueue, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> auditingQueue, boolean isAudit) {
        this.directoryQueue = directoryQueue;
        this.auditingQueue = auditingQueue;
        this.resultsQueue = resultsQueue;
        this.prefix = prefix;
        this.id = id;
        this.isAudit = isAudit;
        resultsQueue.registerProducer();
        if (isAudit) {
            auditingQueue.registerProducer();
        }
    }


    @Override
    public void run() {
        File curDir = directoryQueue.dequeue();
        File[] foundFiles = null;
        while (curDir != null)  {
            foundFiles = curDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    //System.out.println("file: " + name + " : " + (name.startsWith(prefix)));
                    //System.out.println(dir.isFile());
                    return name.startsWith(prefix);
                }
            });
            for (File file : foundFiles) {
                //System.out.println(file.getName());
                this.resultsQueue.enqueue(file);
                if (isAudit) {
                    this.auditingQueue.enqueue("Searcher on thread id " + this.id + ": file named " + file.getName() + " was found");
                }
            }
            curDir = directoryQueue.dequeue();
        }
        if (isAudit) {
            auditingQueue.unregisterProducer();
        }
        resultsQueue.unregisterProducer();
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\tomer\\IdeaProjects\\HW3OS\\DOCS");
        SynchronizedQueue<String> q = new SynchronizedQueue<String>(50);
        SynchronizedQueue<File> d = new SynchronizedQueue<File>(50);
        SynchronizedQueue<File> r = new SynchronizedQueue<File>(50);
        Scouter scouter = new Scouter(1, d, file, q,true);
        scouter.run();
        String s;
        File fil;
        Searcher searcher = new Searcher(2, "ind", d, r,q,true);
        searcher.run();
        while ((fil = r.dequeue()) != null)  {
            System.out.println(fil.getName());
        }
    }
}
