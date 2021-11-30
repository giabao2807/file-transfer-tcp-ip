package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
	public byte[] data;
	public int size;
	public static int MAX_SIZE = 1024000*100;//1 MB
	
	public void clear() {
		data = new byte[0];
		size=0;
	}
	
	public FileUtils() {
		data = new byte[0];
		size =0;
	}
	
	public FileUtils(String filename) throws IOException {
		File file = new File(filename);
		data =Files.readAllBytes(Paths.get(filename));
		System.out.println(data);
	}
	
	@Override
	public String toString() {
		return size + " ; " + data.toString();
	}
	
	public void appendByte(byte[] array) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(data);
			outputStream.write(array);
			data = outputStream.toByteArray();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void saveFile(String fileToReceived) {
		Path path = Paths.get(fileToReceived);
		try {
			Files.write(path, data);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
