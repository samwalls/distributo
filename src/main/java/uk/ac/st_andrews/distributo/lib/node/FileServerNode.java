package uk.ac.st_andrews.distributo.lib.node;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class FileServerNode implements Runnable {

    private MulticastSocket dataSocket;
    private ServerSocket controlSocket;

    private int dataPort, controlPort;
    private InetAddress dataGroup;

    private File file;

    private Thread dataThread, controlThread;

    private volatile boolean servingData, servingControl;

    private List<InetAddress> clients;

    public FileServerNode(String groupHostname, int dataPort, int controlPort, String file) throws IOException {
        dataGroup = InetAddress.getByName(groupHostname);
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        dataSocket = new MulticastSocket();
        //check that the file exists etc.
        File f = new File(file);
        if (!f.exists())
            throw new IOException("file \"" + file + "\" doesn't exist");
        if (f.isDirectory())
            throw new IOException("file \"" + file + "\" is a directory, currently not supported");
        this.file = f;
        //create our threads
        Thread dataThread = new Thread(() -> {
            try {
                serveData();
            } catch (FileServerException e) {
                System.err.println(e.getMessage());
            }
        });
        Thread controlThread = new Thread(() -> {
            try {
                serveControl();
            } catch (FileServerException e) {
                System.err.println(e.getMessage());
            }
        });
        servingData = false;
    }

    /**
     * Stop serving data if the server already is.
     */
    public void stopServingData() {
        servingData = false;
    }

    public void stopServingControl() {
        stopServingData();
        servingControl = false;
    }

    @Override
    public void run() {
        controlThread.start();
        dataThread.start();
        try {
            controlThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Begin serving data to clients.
     * @throws FileServerException
     */
    private void serveData() throws FileServerException {
        servingData = true;
        byte[] msg = "hello borg".getBytes();
        DatagramPacket p = new DatagramPacket(msg, msg.length, dataGroup, dataPort);
        while (servingData) try {
            System.out.printf("joining group: %s on %s\n", dataGroup.toString(), dataPort);
            dataSocket.joinGroup(dataGroup);
            System.out.printf("sending \"%s\"\n", new String(msg));
            for (int i = 0; i < 10; i++)
                dataSocket.send(p);
            dataSocket.leaveGroup(dataGroup);
            dataSocket.close();
        } catch (IOException e) {
            throw new FileServerException(e.getMessage());
        }
    }

    /**
     * Begin serving control information for clients.
     * @throws FileServerException
     */
    private void serveControl() throws FileServerException {
        try {
            controlSocket = new ServerSocket(controlPort);
        } catch (IOException e) {
            throw new FileServerException(e.getMessage());
        }
        while(servingControl) try {
            Socket client = controlSocket.accept();
            new Thread(new ControlServerHandler(client, this)).start();
        } catch (IOException e) {
            servingControl = false;
        }
    }
}
