import java.io.File;
import java.util.Stack;

public class Scouter implements Runnable{
    SynchronizedQueue<File> directoryQueue;
    File root;
    int id;
    SynchronizedQueue<String> auditingQueue;
    boolean isAudit;

    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root, SynchronizedQueue<String> auditingQueue, boolean isAudit) {
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.id = id;
        this.auditingQueue = auditingQueue;
        this.isAudit = isAudit;
        directoryQueue.registerProducer();
        auditingQueue.registerProducer();
    }

    @Override
    public void run() {
        File[] directories = null;
        if (this.isAudit) {
            this.auditingQueue.enqueue("General, program has started the search");
        }
        else {
            auditingQueue.unregisterProducer();
            this.auditingQueue = null;
        }
        Stack<File> stack = new Stack<File>();
        stack.push(this.root);
        while (!stack.isEmpty()){
            File curRoot = stack.pop();
            directories = curRoot.listFiles();
            if (directories != null) {
                for (File directory : directories) {
                    if (directory.isDirectory()) {
                        stack.push(directory);
                        this.directoryQueue.enqueue(directory);
                        if (this.isAudit) {
                            this.auditingQueue.enqueue("Scouter on thread id " + this.id + ": directory named " + directory.getName() + " was scouted");
                        }
                    }
                }
            }
        }
        directoryQueue.unregisterProducer();
        if (this.isAudit) {
            auditingQueue.unregisterProducer();
        }
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\tomer\\IdeaProjects\\HW3OS\\DOCS");
        SynchronizedQueue<String> q = new SynchronizedQueue<String>(200);
        SynchronizedQueue<File> d = new SynchronizedQueue<File>(200);
        Scouter scouter = new Scouter(1, d, file, q,true);
        scouter.run();
        String s;
        while ((s = q.dequeue()) != null)  {
            System.out.println(s);
        }
        File dir;
        while ((dir = d.dequeue()) != null)  {
            System.out.println(dir.getName());
        }
    }
}
