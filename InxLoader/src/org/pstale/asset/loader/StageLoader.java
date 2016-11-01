package org.pstale.asset.loader;

import java.io.IOException;
import java.io.InputStream;

import org.pstale.asset.base.AbstractLoader;

public class StageLoader extends AbstractLoader {

	private final static int OBJ_FRAME_SEARCH_MAX = 32;

	class SmdFramePos {
		int startFrame;
		int endFrame;
		int posNum;
		int posCnt;
	}
	
	// SMD�ļ�ͷ
	String header;
	int objCounter;
	int matCounter;
	int matFilePoint;
	int firstObjInfoPoint;
	int tmFrameCounter;
	SmdFramePos[] data = new SmdFramePos[32];
	
	int nLight = 0;
	
	@Override
	public Object parse(InputStream inputStream) throws IOException {
		getByteBuffer(inputStream);

		/**
		 * ��ȡ�ļ�ͷ
		 */
		header = getString();
		objCounter = getInt(24);
		matCounter = getInt();
		matFilePoint = getInt();
		firstObjInfoPoint = getInt();
		tmFrameCounter = getInt();
		for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
			data[i] = new SmdFramePos();
			data[i].startFrame = getInt();
			data[i].endFrame = getInt();
			data[i].posNum = getInt();
			data[i].posCnt = getInt();
		}
		
		if ("SMD Stage data Ver 0.72".equals(header)) {// ��ͼ
			loadStage();
			return null;
		} else if ("SMD Model data Ver 0.62".equals(header)){// ģ��
			
			return null;
		} else {
			
			return null;
		}
	}

	boolean loadStage() {
		
		// ��ȡsmSTAGE3D����
		readStage3D();
		
		// ��ȡMaterialGroup
		if (matCounter > 0) {
			loadMaterial();
		}
		
		// ��ȡVertex
		readVertex();
		
		// ��ȡFace
		readFace();
		
		// ��ȡTEX_LINK
		readTexLink();
		
		// ��ȡ�ƹ�
		if ( nLight > 0 ) {
			readLight();
		}
		
		return true;
	}

	private void loadMaterial() {
		// ��ȡMaterialGroup����
		readMaterialGroup();
	}

	private void readStage3D() {
		// TODO Auto-generated method stub
		
	}
	
	/**
class smMATERIAL_GROUP {
	DWORD	Head;
public:
	smMATERIAL *smMaterial;
	DWORD MaterialCount;

	int ReformTexture;		//������ �ؽ��� ����

	int MaxMaterial;

	int LastSearchMaterial;
	char szLastSearchName[64];
}
	 */
	private void readMaterialGroup() {
		getInt();// Head
		getInt();// *smMaterial
		int materialCount = getInt();
		int reformTexture = getInt();
		int maxMaterial = getInt();
		int lastSearchMaterial = getInt();
		String lastSearchName = getString(64);
	}

	private void readVertex() {
		// TODO Auto-generated method stub
		
	}

	private void readFace() {
		// TODO Auto-generated method stub
		
	}

	private void readTexLink() {
		// TODO Auto-generated method stub
		
	}
	
	private void readLight() {
		// TODO Auto-generated method stub
		
	}
}
