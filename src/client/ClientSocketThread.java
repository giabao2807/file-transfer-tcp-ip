package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.xml.crypto.Data;

import data.DataFile;
import data.Send_Type;

public class ClientSocketThread extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// recieve
	InputStream is;
	ISocketListener iSocketListener;

	// Send
	OutputStream os;
	Send_Type sendType = Send_Type.DO_NOT_SEND;
	String message;
	String filename;

	// Data File
	DataFile dataFile;
	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	DataFile m_dtf;

	public ClientSocketThread(ISocketListener iSocketListener) {
		this.iSocketListener = iSocketListener;
		m_dtf = new DataFile();
	}

	public void setSocket(String serverIP, int port) {
		try {
			socket = new Socket(serverIP, port);

			// Connect to server

			System.out.println("Connect: " + socket);

			os = socket.getOutputStream();
			is = socket.getInputStream();

			iSocketListener.showDialog("CONNECTED TO SERVER", "INFOR");

		} catch (Exception e) {
			System.out.println("Can't connect to server");
			iSocketListener.showDialog("Can't connect to Server", "ERROR");
		}
	}

	@Override
	public void run() {
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

	void readData() {
		try {
			System.out.println("Receiving...");
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {

			} else if (obj instanceof DataFile) {

			}
		} catch (Exception e) {
			e.printStackTrace();
			connectServerFail();
			closeSocket();
		}

	}

	void readFile(Object obj) throws Exception {
		DataFile dtf = (DataFile) obj;
		currentSize += 512;

		int percent = (int) (currentSize * 100 / fileSize);
		// System.out.println(currentSize + " : " + fileSize);
		m_dtf.appendByte(dtf.data);
		iSocketListener.setProgess(percent);
	}

	void sendString(String str) {
		System.out.println("SENDING STRING	" + str);
		sendType = Send_Type.SEND_STRING;
		message = str;
	}

	private void connectServerFail() {
		// TODO Auto-generated method stub
		iSocketListener.showDialog("Can't connect to Server", "ERROR");
		isStop = true;
		closeSocket();
	}

	public void closeSocket() {
		isStop = true;
		try {
			this.sendString("STOP");
			if (is != null)
				is.close();
			if (os != null)
				os.close();
			if (socket != null)
				socket.close();

			iSocketListener.showDialog("Closed socket", "INFOR");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	class SendDataThread extends Thread {
		@Override
		public void run() {
			while (!isStop) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (sendType != Send_Type.DO_NOT_SEND)
					sendData();
			}
		}

		private void sendData() {
			if (sendType == Send_Type.SEND_STRING) {
				sendMessage(message);
			} else if (sendType == Send_Type.SEND_FILE) {
				File source = new File(filename);
				InputStream fin;
				try {
					fin = new FileInputStream(source);
					long lengthOfFile = source.length();

					// Send Message: fileName + size
					sendMessage("SEND_FILE" + "--" + filename + "--" + lengthOfFile);
					fin.close();

				} catch (Exception e) {
					// TODO: handle exception
				}
			} else if (sendType == Send_Type.START_SEND_FILE) {
				File source = new File(filename);
				InputStream fin = null;
				long lengthOfFile= source.length();
				
				//SendFile: file data
				
				byte[] buf = new byte[512];
				long total =0;
				int len;
				try {
					fin = new FileInputStream(source);
					while((len=fin.read(buf))!=-1) {
						total += len;
						DataFile dtf = new DataFile();
						dtf.data =buf;
						sendMessage(dtf);
						iSocketListener.setProgess((int)(total*100/lengthOfFile));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Send end file : file name +size
				sendMessage("END_FILE--" + filename + "--" + lengthOfFile);
				
			}
			sendType=Send_Type.DO_NOT_SEND;
		}
	}

}
