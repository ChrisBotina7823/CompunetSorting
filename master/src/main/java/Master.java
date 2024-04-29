import Demo.*;
import com.zeroc.Ice.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Master {

    public static final int BLOCK_SIZE = 100;

    public static void main(String[] args) throws InterruptedException, IOException {
        try(Communicator communicator = Util.initialize(args)) {
            List<WorkerIPrx> workers = new ArrayList<>();
            Scanner scanner = new Scanner(System.in);
            String workerName;

            while (true) {
                System.out.println("Enter worker names. Type 'start' to begin sorting:");
                if((workerName = scanner.nextLine()).equals("start")) break;
                ObjectPrx base = communicator.stringToProxy(workerName + ":default -p 500");
                WorkerIPrx worker = WorkerIPrx.checkedCast(base);
                if(worker == null) {
                    throw new Error("Invalid proxy");
                }
                workers.add(worker);
            }

            // Open the txt file
            try(BufferedReader br = new BufferedReader(new FileReader("resources/tasks.txt"))) {
                String task;
                List<String> taskGroup = new ArrayList<>();
                int fileIndex = 0;
                Queue<WorkerIPrx> availableWorkers = new LinkedList<>(workers);

                AtomicInteger sortingFilesCount = new AtomicInteger(0);
                while((task = br.readLine()) != null) {
                    taskGroup.add(task);
                    // If the task group has 10 tasks, send it to a worker
                    if(taskGroup.size() == BLOCK_SIZE) {
                        while(availableWorkers.isEmpty()) {
                            // Wait until a worker is available
                            Thread.sleep(1000);
                        }
                        WorkerIPrx worker = availableWorkers.poll(); // Get the next available worker
                        String[] taskGroupArray = taskGroup.toArray(new String[0]);
                        CompletableFuture<String[]> futureArray = worker.doTaskAsync(taskGroupArray);
                        CompletableFuture<List<String>> future = futureArray.thenApply(Arrays::asList);
                        final int finalFileIndex = fileIndex;
                        sortingFilesCount.incrementAndGet();
                        System.out.println("Sending tasks to worker " + worker.ice_getIdentity().name + " for sorting" + finalFileIndex + ".txt");
                        future.thenAccept(sortedTasks -> {
                            // Write the sorted tasks to a temporary file
                            try(BufferedWriter bw = new BufferedWriter(new FileWriter("resources/temp" + finalFileIndex + ".txt"))) {
                                for(String sortedTask : sortedTasks) {
                                    bw.write(sortedTask);
                                    bw.newLine();
                                }
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            availableWorkers.add(worker); // Add the worker back to the queue
                            sortingFilesCount.decrementAndGet();
                        });
                        taskGroup.clear();
                        fileIndex++;
                    }
                }
                while(sortingFilesCount.get() > 0) {
                    Thread.sleep(1000);
                }
                                
                System.out.println("Tasks have been distributed to workers. Waiting for workers to finish...");
            
                System.out.println("Local Sorting completed.");
                
                // Merge files
                Queue<String> fileQueue = new LinkedList<>();
                for(int i = 0; i < fileIndex; i++) {
                    fileQueue.add("resources/temp" + i + ".txt");
                }
                
                while(fileQueue.size() > 1) {
                    
                    String file1 = fileQueue.poll();
                    String file2 = fileQueue.poll();
                    String newFile = "resources/temp" + fileIndex + ".txt";

                    System.out.println("merging " + file1 + " and " + file2 + " into " + newFile);
                
                    // Wait for an available worker
                    while(availableWorkers.isEmpty()) {
                        Thread.sleep(1000);
                    }
                
                    WorkerIPrx worker = availableWorkers.poll(); // Get the next available worker

                    worker.mergeFilesAsync(file1, file2, newFile).thenAccept(result -> {
                        availableWorkers.add(worker); // Add the worker back to the queue
                        fileQueue.add(newFile); // Add the merged file back to the queue
                    });
                
                    fileIndex++;
                }
                if(fileQueue.size() == 1) {
                    String file1 = fileQueue.poll();
                    String file2 = "resources/temp" + (fileIndex - 1) + ".txt";
                    String newFile = "resources/temp" + fileIndex + ".txt";
                
                    System.out.println("merging " + file1 + " and " + file2 + " into " + newFile);
                
                    // Wait for an available worker
                    while(availableWorkers.isEmpty()) {
                        Thread.sleep(1000);
                    }
                
                    WorkerIPrx worker = availableWorkers.poll(); // Get the next available worker
                
                    worker.mergeFilesAsync(file1, file2, newFile).thenAccept(result -> {
                        availableWorkers.add(worker); // Add the worker back to the queue
                        fileQueue.add(newFile); // Add the merged file back to the queue
                    });                
                }

                for(int i=0; i<fileIndex; i++) {
                    Path tempFilePath = Paths.get("resources/temp" + i + ".txt");
                    try {
                        Files.deleteIfExists(tempFilePath);
                    } catch(IOException e) {
                        System.out.println("Error deleting file: " + tempFilePath);
                    }
                }

                Path sourcePath = Paths.get("resources/temp" + fileIndex + ".txt");
                Path targetPath = Paths.get("resources/final.txt");
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                
            } catch(IOException e) {
                e.printStackTrace();
            }

        }
    }
}
