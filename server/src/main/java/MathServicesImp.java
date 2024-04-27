import com.zeroc.Ice.Current;

import Demo.CallbackPrx;
import Demo.MathServices;
import Demo.Point;

public class MathServicesImp implements MathServices {

    @Override
    public long sum(long l1, long l2, Current current) {
        return l1 + l2;
    }

    @Override
    public void vectorDot(int[][] filaA, int[][] colB, Point i, Point j, CallbackPrx c, Current current) {
        int[][] retMat = new int[j.x-i.x+1][j.y-i.y+1];
        for (int k = i.x; k <= j.x; k++) {
            for (int k2 = i.y; k2 <= j.y; k2++) {
                int value = vectorDot(filaA[k], colB[k2]);
                retMat[k-i.x][k2-i.y] = value;
            }
        }
        c.reportResult(retMat, i, j);
    }

    public int vectorDot(int[] v, int[] v2){
        int value = 0;
        for (int k = 0; k < v.length; k++) {
            value += v[k]*v2[k];
        }   
        return value;
     }
    
}
