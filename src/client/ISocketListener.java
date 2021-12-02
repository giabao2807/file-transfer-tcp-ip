package client;

import utils.FileUtils;

public interface ISocketListener {
	void updateListFile(String[] listFile);
	
	void setProgess(int n);
	
	void showDialog(String str,String type);
	
	String chooserFileToSave(FileUtils dataFile);
}
