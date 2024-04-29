import Demo.*;
import com.zeroc.Ice.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Master {

    public static final int BLOCK_SIZE = 100;

    public static void main(String[] args) throws InterruptedException {
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
                        });
                        taskGroup.clear();
                        fileIndex++;
                    }
                }
                System.out.println("Tasks have been distributed to workers. Waiting for workers to finish...");
            } catch(IOException e) {
                e.printStackTrace();
            }

            // Wait for all tasks to complete
            communicator.waitForShutdown();
            System.out.println("Local Sorting completed.");
        }
    }
}