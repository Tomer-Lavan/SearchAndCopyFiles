import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public class Copier implements Runnable {

    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> auditingQueue;
    int id;
    File destination;
    boolean isAudit;

   public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> auditingQueue, boolean isAudit) {

       this.id = id;
       this.destination = destination;
       this.resultsQueue = resultsQueue;
       this.auditingQueue = auditingQueue;
       this.isAudit = isAudit;
       if (isAudit) {
           auditingQueue.registerProducer();
       }
   }

    @Override
    public void run() {
        File source = resultsQueue.dequeue();
        while (source != null) {
            try {
                Files.copy(source.toPath(), Path.of(destination.toPath().toString() + "\\" + source.getName()), StandardCopyOption.REPLACE_EXISTING);
                if (isAudit) {
                    this.auditingQueue.enqueue("Copier from thread id " + this.id + ": file named " + source.getName() + " was copied");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            source = resultsQueue.dequeue();
        }
        if (isAudit) {
            auditingQueue.unregisterProducer();
        }
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
        Searcher searcher = new Searcher(2, "gla", d, r,q,true);
        searcher.run();
        File dest = new File("C:\\Users\\tomer\\Desktop\\Tomer\\Tomer Idc\\Ex2");
        Copier copier = new Copier(3,dest, r, q, true );
        copier.run();
    }
}
