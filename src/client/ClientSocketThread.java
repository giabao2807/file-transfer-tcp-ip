package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;


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
				connectServerFail();
				e.printStackTrace();
				break;			}

		}
	}

	void readData() {
		try {
			System.out.println("Receiving...");
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);
			} else if (obj instanceof DataFile) {
				readFile(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			connectServerFail();
			closeSocket();
		}

	}
	void readString(Object obj) {
		String str = obj.toString();
		if(str.equals("STOP"))
			isStop=true;
		else if (str.contains("START_SEND_FILE")) {
			this.sendType = Send_Type.START_SEND_FILE;
		} else if (str.contains("SEND_FILE")) {
			String[] fileInfor = str.split("--");
			fileNameReceived = fileInfor[1];
			fileSize = Integer.parseInt(fileInfor[2]);
			
			System.out.println("File Size: " +  fileSize);
			currentSize=0;
			m_dtf.clear();
			this.sendString("START_SEND_FILE");
		} else if (str.contains("END_FILE")) {
			iSocketListener.chooserFileToSave(m_dtf);
		} else if (str.contains("ALL_FILE")) {
			String[] listFile = str.split("--");
			String[] yourArray = Arrays.copyOfRange(listFile,1, listFile.length);
			iSocketListener.updateListFile(yourArray);
		} else if (str.contains("ERROR")) {
			String[] list = str.split("--");
			iSocketListener.showDialog(list[1], "ERROR");
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
	
	void sendFile(String filename) {
		System.out.println("SENDING FILE  " );
		sendType = Send_Type.SEND_FILE;
		this.filename=filename;
		
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
		
		private synchronized void sendMessage(Object obj) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(os);
				
				//only send text
				if(obj instanceof String) {
					String message = obj.toString();
					oos.writeObject(message);
					oos.flush();
				} else if (obj instanceof DataFile) {
					oos.writeObject(obj);
					oos.flush();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

}
