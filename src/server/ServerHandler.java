package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import data.DataFile;
import data.Send_Type;

public class ServerHandler extends Thread {
	private Socket socket;
	private boolean isStop = false;

	// To handle ReceiveFile
	InputStream is;
	private ISocketServerListener iSocketServerListener;

	// To handle SendFile
	OutputStream os;
	Send_Type sendType = Send_Type.DO_NOT_SEND;
	String message;
	String filename;

	FileWorker fileWorker;
	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	DataFile utils;

	public ServerHandler(Socket soc, ISocketServerListener iSocketServerListener) throws IOException {
		this.socket = soc;
		os = soc.getOutputStream();
		is = soc.getInputStream();

		this.iSocketServerListener = iSocketServerListener;
		fileWorker = new FileWorker();

		utils = new DataFile();
	}

	@Override
	public void run() {
		System.out.println("Processing:  " + socket);

		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				connectClientFail();
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Complete processing: " + socket);
		closeSocket();
	}

	void readData() throws Exception {
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
			connectClientFail();
			closeSocket();
		}
	}

	private String readString(Object obj) {
		String str = obj.toString();
		iSocketServerListener.showDialog(str, "STRING INFOR");
		
		if(str.equals("STOP"))
			isStop=true;
		else if (str.equals("VIEW_ALL_FILE")) {
			String[] files = fileWorker.getAllFileName();
			String data= "ALL_FILE";
			for(String file:files) {
				data+= "--" +file;
			}
			this.sendString(data);
		} else if (str.contains("SEARCH_FILE")) {
			String[] searches =str.split("--");
			
			String[] files = fileWorker.searchFile(searches[1]);
			String data = "ALL_FILE";
			
			for(String file:files) {
				data+= "--" + file;
			}
			this.sendString(data);
		} else if (str.contains("DOWNLOAD_FILE")) {
			String[] array = str.split("--");
			sendFile(array[1]);
		} else if (str.contains("START_SEND_FILE")) {
			this.sendType= Send_Type.START_SEND_FILE;
		} else if (str.contains("SEND_FILE")) {
			String[] fileInfor = str.split("--");
			System.out.println(fileInfor[1]);
			
			fileNameReceived =fileWorker.getFileName(fileInfor[1]);
			
			fileSize = Integer.parseInt(fileInfor[2]);
			System.out.println("File Size: " + fileSize);
			currentSize=0;
			utils.clear();
			
			if(fileWorker.checkFile(fileNameReceived)) {
				this.sendString("START_SEND_FILE");
			}
			else {
				this.sendString("ERROR--File TrungTen");
			}
		} else if (str.contains("END_FILE")) {
			utils.saveFile(FileWorker.URL_FOLDER + "/" + fileNameReceived);
		}
		
		return str;
	}

	private void readFile(Object obj) {
		DataFile fileUtils = (DataFile) obj;
		currentSize += 512;

		// count percent progess 50% ...
		int percent = (int) (currentSize * 100 / fileSize);

		utils.appendByte(fileUtils.data);
		iSocketServerListener.showProgessBarPercent(percent);
	}

	public synchronized void sendMessage(Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			// only send text
			if (obj instanceof String) {
				String message = obj.toString();
				oos.writeObject(message);
				oos.flush();
			}
			// send attach file
			else if (obj instanceof DataFile) {
				oos.writeObject(obj);
				oos.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void sendFile(String filename) {
		System.out.println("SENDING FILE	");
		sendType = Send_Type.SEND_FILE;
		this.filename = filename;
	}

	void sendString(String str) {
		System.out.println("SENDING STRING	");
		sendType = Send_Type.SEND_STRING;
		message = str;
	}

	private void connectClientFail() {
		isStop = true;
		closeSocket();
	}

	private void closeSocket() {
		try {
			this.sendString("STOP");
			if (socket != null)
				socket.close();
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			iSocketServerListener.showDialog("Closed Server Socket", "INFOR");
		} catch (Exception e) {
			e.printStackTrace();
			iSocketServerListener.showDialog("Connect Fail", "ERROR");
		}
	}

}
