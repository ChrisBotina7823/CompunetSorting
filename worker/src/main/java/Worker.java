import Demo.*;
import com.zeroc.Ice.*;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Worker implements WorkerI {
    @Override
    public String[] doTask(String[] tasks, Current current) {
        Arrays.sort(tasks);
        return tasks;
    }



    public static void main(String[] args) {
        try(Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("WorkerIAdapter", "default -p 500");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter worker name:");
            String workerName = scanner.nextLine();
            ObjectPrx obj = adapter.add(new Worker(), Util.stringToIdentity(workerName));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }



    @Override
    public void mergeFiles(String file1, String file2, String outputFile, Current current) {
        try (
            BufferedReader reader1 = new BufferedReader(new FileReader(file1));
            BufferedReader reader2 = new BufferedReader(new FileReader(file2));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
    
            while (line1 != null || line2 != null) {
                if (line2 == null || (line1 != null && line1.compareTo(line2) <= 0)) {
                    writer.write(line1);
                    writer.newLine();
                    line1 = reader1.readLine();
                } else {
                    writer.write(line2);
                    writer.newLine();
                    line2 = reader2.readLine();
                }
            }
            try {
                Files.deleteIfExists(Paths.get(file1));
                Files.deleteIfExists(Paths.get(file2));
            } catch(IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}