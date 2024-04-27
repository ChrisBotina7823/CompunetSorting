public class PrinterI implements Demo.Printer
{
    public void printString(Demo.Message s, com.zeroc.Ice.Current current)
    {
	try{
	    System.out.println(s.msg);
	    Thread.sleep(5000);
	}catch(Exception e){
	}
    }
}
