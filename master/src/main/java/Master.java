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
            String port;

            while (true) {
                System.out.println("Ingrese nombres de workers. Escriba 'start' para comenzar a ordenar:");
                workerName = scanner.nextLine();

                if (workerName.equals("start")) break;
                System.out.println("Ingrese el puerto del worker:");
                port = scanner.nextLine();
                // Asignar un puerto único a cada worker
                ObjectPrx base = communicator.stringToProxy(workerName + ":default -p " + port);
                WorkerIPrx worker = WorkerIPrx.checkedCast(base);
                if (worker == null) {
                    throw new Error("Proxy inválido");
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
                    if(taskGroup.size() == BLOCK_SIZE || !br.ready()) { // Send the group if it's full or it's the last one
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
                try {
                    mergeFiles(fileIndex);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void mergeFiles(int fileIndex) throws IOException {
        Queue<String> fileQueue = new LinkedList<>();
        for(int i = 0; i < fileIndex; i++) {
            fileQueue.add("resources/temp" + i + ".txt");
        }

        while(fileQueue.size() > 1) {
            String file1 = fileQueue.poll();
            String file2 = fileQueue.poll();
            String newFile = "resources/temp" + fileIndex + ".txt";

            System.out.println("Merging " + file1 + " and " + file2 + " into " + newFile);

            try (
                    BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                    BufferedReader reader2 = new BufferedReader(new FileReader(file2));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))
            ) {
                String line1 = reader1.readLine();
                String line2 = reader2.readLine();

                while (line1 != null || line2 != null) {
                    if (line1 != null && (line2 == null || line1.compareTo(line2) <= 0)) {
                        writer.write(line1);
                        writer.newLine();
                        line1 = reader1.readLine();
                    } else {
                        writer.write(line2);
                        writer.newLine();
                        line2 = reader2.readLine();
                    }
                }
                reader1.close();
                reader2.close();
                Files.deleteIfExists(Paths.get(file1));
                Files.deleteIfExists(Paths.get(file2));    
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileQueue.add(newFile);
            fileIndex++;
        }

        if(fileQueue.size() == 1) {
            Path tempFilePath = Paths.get(fileQueue.poll());
            Path targetPath = Paths.get("resources/final.txt");
            Files.move(tempFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
