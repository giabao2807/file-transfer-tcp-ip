package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import utils.FileUtils;
import utils.Send_Type;

public class ServerHandler extends Thread {
	private Socket socket;
	private boolean isStop = false;
	
	//To handle ReceiveFile
	InputStream is;
	private  ISocketServerListener iSocketServerListener;
	
	//To handle SendFile
	OutputStream os;
	Send_Type sendType = Send_Type.DO_NOT_SEND;
	String message;
	String filename;
	
	
	FileWorker fileWorker;
	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	FileUtils utils;
	
	public ServerHandler(Socket soc, ISocketServerListener iSocketServerListener) throws IOException {
		this.socket=soc;
		os= soc.getOutputStream();
		is = soc.getInputStream();
		
		this.iSocketServerListener= iSocketServerListener;
		fileWorker= new FileWorker();
		
		utils = new FileUtils();
	}
	
	@Override
	public void run() {
		System.out.println("Processing:  " + socket);
		
		while(!isStop) {
			try {
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		System.out.println("Complete processing: " + socket);
	}
	void readData() throws Exception{
		try {
			System.out.println("Receiving...");
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			
			if(obj instanceof String) {
				readString(obj);
			} else if (obj instanceof FileUtils) {
				readFile(obj);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private String readString (Object obj) {
		String str = obj.toString();
		
		iSocketServerListener.showDialog(str, "STRING INFOR");
		
		return str;
		
	}
	
	private void readFile(Object obj) {
		FileUtils fileUtils = (FileUtils) obj;
		currentSize +=512;
		
		//count percent progess 50% ...
		int percent =(int) (currentSize*100/fileSize);
		
		utils.appendByte(fileUtils.data);
		iSocketServerListener.showProgessBarPercent(percent);
	}
	
	private void connectClientFail() {
		isStop=true;
		closeSocket();
	}
	private void closeSocket() {
		try {
			this.sendString("STOP");
			if(socket != null) socket.close();
			if(os!=null) os.close();
			if(is!=null) is.close();
			iSocketServerListener.showDialog("Closed Server Socket", "INFOR");
		} catch (Exception e) {
			e.printStackTrace();
			iSocketServerListener.showDialog("Connect Fail", "ERROR");
		}
	}
	
	
}
