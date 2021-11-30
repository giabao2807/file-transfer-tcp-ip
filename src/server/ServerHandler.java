package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import utils.Send_Type;

public class ServerHandler {
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
	
	
}
