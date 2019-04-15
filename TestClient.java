/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */
package rdt;

import static rdt.RDT.SR;

public class TestClient {

    public TestClient() {
    }

    public static void main(String[] args) {
        String hostname = "localhost";
        int dst_port = 4568;
        int local_port = 4569;

        byte[] buf = new byte[RDT.MSS];

        int messageSize = 45;
        int bufsize = 6;
        int numMessages = 10;
        byte[] data = new byte[messageSize];

        RDT rdt = new RDT(hostname, dst_port, local_port, bufsize, bufsize);
        RDT.setMSS(10);
        RDT.setLossRate(0.4);
        RDT.protocol = SR;

        for (int i = 0; i < numMessages; i++) {
            for (int j = 0; j < messageSize; j++) {
                data[j] = (byte) i;
            }
            rdt.send(data, messageSize);
        }

        System.out.println(System.currentTimeMillis() + ": Client has sent all data \n");
        System.out.flush();

        rdt.receive(buf, RDT.MSS);
        rdt.close();
    }
}
