import Demo.*;
import com.zeroc.Ice.*;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
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