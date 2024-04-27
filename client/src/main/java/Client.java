import java.util.Arrays;

import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import Demo.Callback;
import Demo.CallbackPrx;
import Demo.MathServicesPrx;
import Demo.Point;

public class Client implements Callback
{
    static int[][] mathC;
    public static void main(String[] args)throws Exception
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"client.cfg"))
        {
            ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
            ObjectPrx prx = adapter.add(new Client(), Util.stringToIdentity("Client"));
            adapter.activate();
            CallbackPrx proxyC = CallbackPrx.checkedCast(prx);
            com.zeroc.Ice.ObjectPrx baseMath = communicator.propertyToProxy("math.proxy");
            MathServicesPrx mathPrx = MathServicesPrx.checkedCast(baseMath);
           // long res = mathPrx.sum(5, 7);
            //System.out.println("Suma: "+res);

            int[][] math = generateMatix(10000, 3000);
            int[][] mathB = generateMatix(3000, 50000);
            int[][] mathBT = trans(mathB);
            /*
            System.out.println("________");
            print(math);
            System.out.println("________");
            print(mathB);
            System.out.println("________");
            print(mathBT);
            System.out.println("________");
            System.out.println(Arrays.toString(math[0]));
            System.out.println(Arrays.toString(mathBT[0]));
            System.out.println("________");
 */
            mathC = new int[math.length][mathB[0].length];
            System.out.println("Start mul");
            long time = System.currentTimeMillis();
            for (int i = 0; i < mathC.length; i++) {
                for (int j = 0; j < mathC[0].length; j++) {
                    mathPrx.vectorDot(math,mathBT, null, null,proxyC);
                    //Integer result = vectorDot(math[i],mathBT[j], i, j);

                    //mathC[i][j] = result;
                }
            }
 
   //         print(mathC);
            System.out.println("Time: "+(System.currentTimeMillis()-time));

        }
    }

    public static void print(int[][] mat){

        for (int i = 0; i < mat.length; i++) {
            System.out.println(Arrays.toString(mat[i]));
        }
        System.out.println();

    }

    public static int[][] generateMatix(int m, int n){
        int[][] mat = new int[m][n];

        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                mat[i][j] = (int)(Math.random()*20);
            }
        }
        return mat;
    }

    public static int[][] trans(int[][] mat){
        int[][] tras = new int[mat[0].length][mat.length];
        for (int i = 0; i < tras.length; i++) {
            for (int j = 0; j < tras[0].length; j++) {
                tras[i][j] = mat[j][i];
            }
        }
        return tras;
    }

     public static int vectorDot(int[] filaA, int[] colB, int i, int j) {
        int value = 0;
        for (int k = 0; k < colB.length; k++) {
            value += filaA[k]*colB[k];
        }
        return value;
    }

    @Override
    public void reportResult(int[][] result, Point i, Point j, Current current) {
        synchronized(this){
            //mathC[i][j] = result;
        }
    }
}
