public class Server
{
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"server.cfg"))
        {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("services");
            com.zeroc.Ice.Object object = new PrinterI();
            MathServicesImp mathIm = new MathServicesImp();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.add(mathIm, com.zeroc.Ice.Util.stringToIdentity("SimpleMath"));

            adapter.activate();
            communicator.waitForShutdown();
        }
    }
}
