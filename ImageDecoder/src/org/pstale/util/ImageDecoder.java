package org.pstale.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ImageDecoder {

	/**
	 * ת��TGA�ļ�
	 * @param buffer ���ļ�ͷ��ʼ�����ݣ�����18�ֽ�
	 * @param readable �Ƿ�ʹ�ļ��ɶ�?
	 */
	public static void convertTGA(byte[] buffer, boolean readable) {
		if (readable) {
			// ����TGA
            buffer[0] = 0x0;
            buffer[1] = 0x0;
        	for(byte i=2; i<18; i++) {
        		buffer[i] -= (byte)(i*i);
        	}
		} else {
			// ����TGA
			buffer[0] = 0x47;
            buffer[1] = 0x38;
        	for(byte i=2; i<18; i++) {
        		buffer[i] += (byte)(i*i);
        	}
		}
	}
	
	/**
	 * ת��BMP�ļ�
	 * @param buffer ���ļ�ͷ��ʼ�����ݣ�����16�ֽ�
	 * @param readable �Ƿ�ʹ�ļ��ɶ�?
	 */
	public static void convertBMP(byte[] buffer, boolean readable) {
		if (readable) {
			// ����BMP
			buffer[0] = 0x42;
			buffer[1] = 0x4D;
			for(byte i=2; i<14; i++) {
				buffer[i] -= (byte)(i*i);
			}
		} else {
			// ����BMP
        	buffer[0] = 0x41;
        	buffer[1] = 0x38;
        	for(byte i=2; i<14; i++) {
        		buffer[i] += (byte)(i*i);
        	}
		}
		
	}
	
	/**
	 * ��ָ���ļ���������bmp��tgaͼƬ���롣
	 * 
	 * @param folder
	 */
	public static void imageDecode(String folder) {
		File dir = new File(folder);

		// �ж��ļ����Ƿ����
		if (dir.exists() && dir.isDirectory()) {

			// ����bmp�ļ�
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					String str = name.toLowerCase();
					return str.endsWith(".bmp");
				}
			});// ��ȡ�ļ��б�
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {

					try {
						byte[] buffer = new byte[16];
						RandomAccessFile raf = new RandomAccessFile(file, "rw");
						raf.seek(0);
						raf.readFully(buffer);

						// ����
						if (buffer[0] == 0x41 && buffer[1] == 0x38) {
							convertBMP(buffer, true);
							raf.seek(0);
							raf.write(buffer);
						}

						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			// ����tga�ļ�
			files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					String str = name.toLowerCase();
					return str.endsWith(".tga");
				}
			});// ��ȡ�ļ��б�
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					try {
						byte[] buffer = new byte[18];
						RandomAccessFile raf = new RandomAccessFile(file, "rw");
						raf.seek(0);
						raf.readFully(buffer);

						// ����
						if (buffer[0] == 0x47 && buffer[1] == 0x38) {
							convertTGA(buffer, true);
							raf.seek(0);
							raf.write(buffer);
						}

						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String root = "D:/Priston Tale/PTCN3550/PTCN3550/char/monster/d_ar";
		imageDecode(root);
	}
}
