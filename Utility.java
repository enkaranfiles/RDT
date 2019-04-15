/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */
package rdt;

import java.net.*;

public class Utility {

	private static final int MAX_NETWORK_DELAY = 200; // msec

	public static void udp_send (RDTSegment seg, DatagramSocket socket, InetAddress ip, int port, boolean resend) {
		double d = RDT.random.nextDouble();
		if ( d < RDT.lossRate) {
            if (seg.containsData()) {
                System.out.println(System.currentTimeMillis() + ":" + " Segment Lost with the number : " + seg.seqNum);
            }
            else {
                System.out.println(System.currentTimeMillis() + ":" + " ACK Lost with the number : " + seg.ackNum);
            }
			System.out.flush();
	        return;
	    }

		// Prepare UDP payload
		int payloadSize = seg.length + RDTSegment.HDR_SIZE;
		byte[] payload = new byte[payloadSize];
		makePayload(seg, payload);

		// Send over UDP
		// Simulate random network delay
		int delay = RDT.random.nextInt(MAX_NETWORK_DELAY);
		try {
			Thread.sleep(delay);
			socket.send(new DatagramPacket(payload, payloadSize, ip, port));
		} catch (Exception e) {
			System.out.println("udp_send: " + e);
		}

		// Print information about the transmission to stdout
		String segData = dataToString(seg.getData());
		String status;

		if (resend) {
		    status = "Resend: ";
        }
        else {
		    status = "Sent: ";
        }

		if (seg.containsAck()) {
            System.out.println(System.currentTimeMillis() + ": " + status + "SeqNum="
                    + seg.seqNum + " AckNum=" + seg.ackNum + " Delay=" + delay);
        }
        else {
            System.out.println(System.currentTimeMillis() + ": " + status + "Segment <" + seg + "> " + "SeqNum="
                    + seg.seqNum + " AckNum=" + seg.ackNum + " Delay=" + delay + " Checksum=" + seg.checksum + " Data=" + segData + "");
        }

        System.out.flush();
		// end print

		return;
	}

	public static void intToByte(int intValue, byte[] data, int idx) {
		data[idx++] = (byte) ((intValue & 0xFF000000) >> 24);
		data[idx++] = (byte) ((intValue & 0x00FF0000) >> 16);
		data[idx++] = (byte) ((intValue & 0x0000FF00) >> 8);
		data[idx]   = (byte) (intValue & 0x000000FF);	
	}

	public static void shortToByte(short shortValue, byte[] data, int idx) {
		data[idx++] = (byte) ((shortValue & 0xFF00) >> 8);
		data[idx]   = (byte) (shortValue & 0x00FF);	
	}

    public static int byteToInt(byte[] data, int idx) {
        int intValue = 0, intTmp = 0;
		
		if ( ((int) data[idx]) < 0 ) { //leftmost bit (8th bit) is 1
			intTmp = 0x0000007F & ( (int) data[idx]);
			intTmp += 128;  // add the value of the masked bit: 2^7
		} else
			intTmp = 0x000000FF & ((int) data[idx]);
		idx++;
		intValue = intTmp; 
		intValue <<= 8;
				
		if ( ((int) data[idx]) < 0 ) { //leftmost bit (8th bit) is 1
			intTmp = 0x0000007F & ( (int) data[idx]);
			intTmp += 128;  // add the value of the masked bit: 2^7
		} else
			intTmp = 0x000000FF & ((int) data[idx]);
		idx++;
		intValue |= intTmp;
		intValue <<= 8 ; 	
			
		if ( ((int) data[idx]) < 0 ) { //leftmost bit (8th bit) is 1
			intTmp = 0x0000007F & ( (int) data[idx]);
			intTmp += 128;  // add the value of the masked bit: 2^7
		} else
			intTmp = 0x000000FF & ((int) data[idx]);
		idx++;
		intValue |= intTmp;
		intValue <<= 8;
			
		if ( ((int) data[idx]) < 0 ) { //leftmost bit (8th bit) is 1
			intTmp = 0x0000007F & ( (int) data[idx]);
			intTmp += 128;  // add the value of the masked bit: 2^7
		} else
			intTmp = 0x000000FF & ((int) data[idx]);
		intValue |= intTmp;
		//System.out.println(" byteToInt: " + intValue + "  " + intTmp);
		return intValue;
	}

    public static void makePayload(RDTSegment seg, byte[] payload) {
        // Add header
        intToByte(seg.seqNum, payload, seg.SEQ_NUM_OFFSET);
        intToByte(seg.ackNum, payload, seg.ACK_NUM_OFFSET);
        intToByte(seg.flags, payload, seg.FLAGS_OFFSET);
        intToByte(seg.checksum, payload, seg.CHECKSUM_OFFSET);
        intToByte(seg.rcvWin, payload, seg.RCV_WIN_OFFSET);
        intToByte(seg.length, payload, seg.LENGTH_OFFSET);

        byte segData[] = seg.getData();

        for (int i = 0; i < seg.length; i++) {
            payload[i + seg.HDR_SIZE] = segData[i];
        }
    }

    public static String dataToString(byte[] data) {
	    String str = "";
        for (int i = 0; i < data.length; i++) {
            str += data[i];
            if (i > 3) {
                str += "...";
                break;
            }
        }
        return str;
    }
}
