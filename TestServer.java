/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */
package rdt;

import static rdt.RDT.SR;

public class TestServer {

    public TestServer() {
    }

    public static void main(String[] args) throws Exception {
        String hostname = "localhost";
        int dst_port = 4569;
        int local_port = 4568;

        byte[] buf = new byte[500];
        System.out.println("Server is waiting to receive ... ");

        int bufsize = 6;
        RDT rdt = new RDT(hostname, dst_port, local_port, bufsize, bufsize);
        RDT.setMSS(90);
        RDT.setLossRate(0.2);
        RDT.protocol = SR;

        while (true) {
            int size = rdt.receive(buf, RDT.MSS);
            System.out.flush();
        }
    }
}

