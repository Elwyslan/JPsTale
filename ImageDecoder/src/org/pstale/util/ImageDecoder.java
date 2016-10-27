package org.pstale.util;

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
}
