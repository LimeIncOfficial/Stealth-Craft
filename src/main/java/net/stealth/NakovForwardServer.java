import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;
 
public class NakovForwardServer
{
    private static final boolean ENABLE_LOGGING = true;
    public static final String SETTINGS_FILE_NAME = "NakovForwardServer.properties";
 
    private ServerDescription[] mServersList = null;
    private int mListeningTcpPort = 2001;
    private boolean mUseLoadBalancingAlgorithm = true;
    private long mCheckAliveIntervalMs = 5*1000;
 
    /**
     * ServerDescription descripts a server (server hostname/IP, server port,
     * is the server alive at last check, how many clients are connected to it, etc.)
     */
    class ServerDescription
    {
        public String host;
        public int port;
        public int clientsConectedCount = 0;
        public boolean isAlive = true;
        public ServerDescription(String host, int port)
        {
           this.host = host;
           this.port = port;
        }
    }
 
    /**
     * @return an array of ServerDescription - all destination servers.
     */
    public ServerDescription[] getServersList()
    {
        return mServersList;
    }
 
    /**
     * @return the time interval (in milliseconds) through which all dead servers
     * should be re-checked if they are alive (a server is alive if accepts
     * client connections on the specified port, otherwise is dead).
     */
    public long getCheckAliveIntervalMs()
    {
        return mCheckAliveIntervalMs;
    }
 
    /**
     * @return true if load balancing algorithm is enabled.
     */
    public boolean isLoadBalancingEnabled()
    {
        return mUseLoadBalancingAlgorithm;
    }
 
    /**
     * Reads the Nakov Forward Server configuration file "NakovForwardServer.properties"
     * and load user preferences. This method is called once during the server startup.
     */
    public void readSettings()
    throws Exception
    {
        // Read properties file in a Property object
        Properties props = new Properties();
        props.load(new FileInputStream(SETTINGS_FILE_NAME));
 
        // Read and parse the server list
        String serversProperty = props.getProperty("Servers");
        if (serversProperty == null )
           throw new Exception("The server list can not be empty.");
        try {
           ArrayList servers = new ArrayList();
           StringTokenizer stServers = new StringTokenizer(serversProperty,",");
           while (stServers.hasMoreTokens()) {
               String serverAndPort = stServers.nextToken().trim();
               StringTokenizer stServerPort = new StringTokenizer(serverAndPort,": ");
               String host = stServerPort.nextToken();
               int port = Integer.parseInt(stServerPort.nextToken());
               servers.add(new ServerDescription(host,port));
           }
           mServersList = (ServerDescription[]) servers.toArray(new ServerDescription[] {});
        } catch (Exception e) {
           throw new Exception("Invalid server list format : " + serversProperty);
        }
        if (mServersList.length == 0)
           throw new Exception("The server list can not be empty.");
 
        // Read server's listening port number
        try {
           mListeningTcpPort = Integer.parseInt(props.getProperty("ListeningPort"));
        } catch (Exception e) {
           log("Server listening port not specified. Using default port : " + mListeningTcpPort);
        }
 
        // Read load balancing property
        try {
           String loadBalancing = props.getProperty("LoadBalancing").toLowerCase();
            mUseLoadBalancingAlgorithm = (loadBalancing.equals("yes") ||
				loadBalancing.equals("true") || loadBalancing.equals("1") ||
				loadBalancing.equals("enable") || loadBalancing.equals("enabled"));
        } catch (Exception e) {
           log("LoadBalancing property is not specified. Using default value : " + mUseLoadBalancingAlgorithm);
        }
 
        // Read the check alive interval
        try {
           mCheckAliveIntervalMs = Integer.parseInt(props.getProperty("CheckAliveInterval"));
        } catch (Exception e) {
           log("Check alive interval is not specified. Using default value : " + mCheckAliveIntervalMs + " ms.");
        }
 
    }
 
    /**
     * Starts a thread that re-checks all dead threads if they are alive
     * through mCheckAliveIntervalMs millisoconds
     */
    private void startCheckAliveThread()
    {
        CheckAliveThread checkAliveThread = new CheckAliveThread(this);
        checkAliveThread.setDaemon(true);
        checkAliveThread.start();
    }
 
    /**
     * Starts the forward server - binds on a given port and starts serving
     */
    public void startForwardServer()
    throws Exception
    {
        // Bind server on given TCP port
        ServerSocket serverSocket;
        try {
           serverSocket = new ServerSocket(mListeningTcpPort);
        } catch (IOException ioe) {
           throw new IOException("Unable to bind to port " + mListeningTcpPort);
        }
 
        log("Nakov Forward Server started on TCP port " + mListeningTcpPort + ".");
        log("All TCP connections to " + InetAddress.getLocalHost().getHostAddress() + 
			":" + mListeningTcpPort + " will be forwarded to the following servers:");
        for (int i=0; i<mServersList.length; i++) {
           log("  " + mServersList[i].host +  ":" + mServersList[i].port);
        }
        log("Load balancing algorithm is " + (mUseLoadBalancingAlgorithm ? "ENABLED." : "DISABLED."));
 
        // Accept client connections and process them until stopped
        while(true) {
           try {
               Socket clientSocket = serverSocket.accept();
               String clientHostPort = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
               log("Accepted client from " + clientHostPort);
               ForwardServerClientThread forwardThread = new ForwardServerClientThread(this, clientSocket);
               forwardThread.start();
           } catch (Exception e) {
               throw new Exception("Unexpected error.\n" + e.toString());
           }
        }
    }
 
    /**
     * Prints given log message on the standart output if logging is enabled,
     * otherwise ignores it
     */
    public void log(String aMessage)
    {
        if (ENABLE_LOGGING)
           System.out.println(aMessage);
    }
 
    /**
     * Program entry point. Reads settings, starts check-alive thread and
     * the forward server
     */
    public static void main(String[] aArgs)
    {
        NakovForwardServer srv = new NakovForwardServer();
        try {
           srv.readSettings();
           srv.startCheckAliveThread();
           srv.startForwardServer();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
 
}