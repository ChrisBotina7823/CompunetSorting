module Demo
{
    sequence<int> ArrInt;
    sequence<ArrInt> MatrInt;

    class Message{
	    string msg;
	    long date;
    }
    class Point{
        int x;
        int y;
    }

    interface Callback{
        void reportResult(MatrInt result, Point i, Point j);
    }
    interface Printer
    {
        void printString(Message msg);
    }
    interface MathServices{
        long sum(long l1, long l2);
        void vectorDot(MatrInt mat, MatrInt matB, Point i, Point j, Callback* callback);
    }

}
