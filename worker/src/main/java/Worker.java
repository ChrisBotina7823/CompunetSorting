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
            // Crear un ObjectAdapter para cada worker con un puerto único
            System.out.println("Ingrese el nombre del worker:");
            Scanner scanner = new Scanner(System.in);
            String workerName = scanner.nextLine();
            System.out.println("Ingrese el número de puerto para este worker:");
            int portNumber = scanner.nextInt();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(workerName + "Adapter", "default -p " + portNumber);

            // Agregar el worker al adaptador y activarlo
            ObjectPrx obj = adapter.add(new Worker(), Util.stringToIdentity(workerName));
            adapter.activate();

            // Esperar a que el comunicador se cierre
            communicator.waitForShutdown();
        }
    }

}