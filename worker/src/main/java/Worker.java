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

    
}