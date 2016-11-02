package org.pstale.asset.loader;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;

/**
 * ���鳡��������
 * @author yanmaoyuan
 *
 */
public class StageLoader extends ByteReader implements AssetLoader {

	static Logger log = Logger.getLogger(StageLoader.class);
	
	private final static int OBJ_FRAME_SEARCH_MAX = 32;

	class SmdFramePos {
		int startFrame;
		int endFrame;
		int posNum;
		int posCnt;
	}
	
	/**
	 * ����
	 * size = 320
	 * @author yanmaoyuan
	 *
	 */
	private class MATERIAL {
		int InUse;
		int TextureCounter;
		TEXTUREHANDLE[] smTexture = new TEXTUREHANDLE[8];
		int[] TextureStageState = new int[8];
		int[] TextureFormState = new int[8];
		int ReformTexture;

		int MapOpacity; // �� ���ǽ�Ƽ ���� ( TRUE , FALSE )

		// �Ϲ����� �Ӽ�
		int TextureType; // �ؽ��� Ÿ�� ( ��Ƽ�ͽ� / �ִϸ��̼� )
		int BlendType; // �귣�� ���� ( SMMAT_BLEND_XXXX )

		int Shade; // ���̵� ��� ( �뽦�̵� / �׷ν��̵� )
		int TwoSide; // ��� ��� ����
		int SerialNum; // ��Ʈ���� ���� ���� ��ȣ

		Vector3f Diffuse; // Diffuse ����
		float Transparency; // ����
		float SelfIllum; // ��ü �߱�

		int TextureSwap; // �ؽ��� ������
		int MatFrame; // ��������� ( ���� ���⸦ ���߱� ���� )
		int TextureClip; // �����ο� �ؽ��� Ŭ������ ( TRUE �� �ؽ��� Ŭ���� �㰡 )

		// �޽� ���� �Ӽ�
		int UseState; // �뵵 ( ��� �Ӽ� )
		int MeshState; // MESH�� ���� �Ӽ���

		// Mesh ���� ���� ����
		int WindMeshBottom; // �ٶ��ұ� �޽� ���� ���� ��

		// ���ϸ��̼� �ؽ��� �Ӽ�
		TEXTUREHANDLE[] smAnimTexture = new TEXTUREHANDLE[32]; // �ִϸ��̼� �ؽ��� �ڵ� ����Ʈ
		int AnimTexCounter; // �ֹ̳��̼� �ؽ��� ī����
		int FrameMask; // �ִϸ��̼ǿ� ������ ����ũ
		int Shift_FrameSpeed; // ������ ���� �ӵ� ( Ÿ�̸Ӹ� Shift �Ͽ� ��� )
		int AnimationFrame; // ������ ��ȣ ( ������ ��� �����Ӱ� / SMTEX_AUTOANIMATION �� �ڵ� )
	}
	
	class TEXTUREHANDLE {
		String Name;// [64];
		String NameA;// [64];
		int Width, Height;
		int UsedTime;
		int UseCounter;// ��������Ǹ��������ı�־λ����¼���Texture�Ƿ��Ѿ�ʹ�á�
		int MapOpacity; // �Ƿ�͸��( TRUE , FALSE )
		int TexSwapMode; // ( TRUE / FALSE )
		TEXTUREHANDLE TexChild;
	}
	
	class FTPOINT {
	    float u,v;              
	}
	
	/**
	 * size = 24
	 *
	 */
	class VERTEX {
		int x,y,z;// ����
		int nx,ny,nz;// normals ������
	}
	
	/**
	 * size = 36
	 */
	class FACE{
		short[] v= new short[4];// a,b,c,Matrial
	    FTPOINT[] t = new FTPOINT[3];
	    TEXLINK[] lpTexLink;
	}
	/**
	 * size = 28
	 */
	class STAGE_VERTEX {
	    int sum;
	    //smRENDVERTEX *lpRendVertex;
	    float x,y,z;
	    float r, g, b, a;// ����256��������ColorRGBA
	}
	
	/**
	 * size = 28
	 *
	 */
	class STAGE_FACE {
	    int sum;
	    int CalcSum;
	    int a, b, c, mat_id;
	    int lpTexLink;// ����һ��ָ�룬ָ��TEXLINK�ṹ��
	    TEXLINK TexLink;// ��lpTexLink != 0����TexLinkָ��һ��ʵ�ʵĶ�����

	    float nx, ny, nz, y;// Cross����( Normal )  ( nx , ny , nz , [0,1,0]���� Y ); 
	}
	
	/**
	 * size = 32
	 *
	 */
	class TEXLINK {
		float[] u = new float[3];
		float[] v = new float[3];
		int hTexture;
		int lpNextTex;// ����һ��ָ�룬ָ��TEXLINK�ṹ��
		TEXLINK NextTex;// ��lpNextTex != 0����NextTexָ��һ��ʵ�ʵĶ���
	}
	
	/**
	 * size = 22
	 */
	class LIGHT3D {
	    int type;
	    float x,y,z;
	    float Range;
	    float r,g,b;
	}
	
	/**
	 * SMD�ļ�ͷ
	 * ռ�ļ�ǰ size = 556;
	 */
	String header;// 24�ֽ�
	int objCounter;
	int matCounter;
	int matFilePoint;
	int firstObjInfoPoint;
	int tmFrameCounter;
	SmdFramePos[] data = new SmdFramePos[32];// 512�ֽ�
	
	/**
	 * Stage3D���������
	 * �ļ����ݵĵڶ��Σ��洢��һ��������smSTAGE3D���� size = 262260
	 * ���еĹؼ�������nVertex/nFace/nTexLink/nLight��Щ��
	 */
	// DWORD Head; ���õ�ͷ�ļ�ָ�룬4�ֽ�
	int[][] StageArea;// WORD *StageArea[MAP_SIZE][MAP_SIZE];256 * 256��ָ�룬��262144�ֽ�
	Vector3f[] AreaList;// POINT *AreaList; һ��ָ�룬������һ������
	int AreaListCnt;
	
	int MemMode;
	
	int SumCount;
	int CalcSumCount;
	
	STAGE_VERTEX[] Vertex;
	STAGE_FACE[] Face;
	TEXLINK[] TexLink;
	LIGHT3D[] Light;
	// smMATERIAL_GROUP    *smMaterialGroup;// sizeof(smMaterialGroup) = 88
	// smSTAGE_OBJECT      *StageObject;
	MATERIAL[]          smMaterial;
	
	int nVertex = 0;// offset = 88 +  = 262752
	int nFace = 0;
	int nTexLink = 0;//UvVertexNum
	int nLight = 0;
	int nVertColor = 0;
	
	int Contrast = 0;
	int Bright = 0;
	
	Vector3f vectLight;
	
	// WORD    *lpwAreaBuff;
	int     wAreaSize;
	// RECT    StageMapRect;// top bottom right left 4������
	
	//////////////////
	// �������������¼TexLink���ļ��еĵ�ַ
	int lpOldTexLink;
	//////////////////
	
	/**
	 * ���ļ�ͷ�е�mat>0��˵���в��ʡ�
	 * ��������������Ӧ����һ��������smMATERIAL_GROUP����size = 88��
	 */
	// DWORD Head
	// smMaterial* materials
	int materialCount;
	int reformTexture;
	int maxMaterial;
	int lastSearchMaterial;
	String lastSearchName;
	
	public AssetManager manager = null;
	public AssetKey<?> key = null;
	
	public Material defaultMaterial;
	
	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		key = assetInfo.getKey();
		manager = assetInfo.getManager();
		
		/**
		 * �����ļ�
		 */
		getByteBuffer(assetInfo.openStream());
		
		/**
		 * ��ȡ�ļ�ͷ
		 */
		readHead();
		
		/**
		 * ���û�ʹ����SmdKey���͸���type�������������ַ�ʽ������ģ�͡�
		 */
		if (key instanceof SmdKey) {
			switch (((SmdKey) key).type) {
			case STAGE3D:
				return loadStage();
			case STAGE_OBJ:
			case PAT3D:
			case MODEL:
			case BONE:
			}
			
			return null;
		} else {
			/**
			 * ���û�û��ʹ��SmdKey���͸����ļ�ͷ���жϡ�
			 * ���Լ���Stage��Model
			 */
			if ("SMD Stage data Ver 0.72".equals(header)) {// ��ͼ
				return loadStage();
			} else if ("SMD Model data Ver 0.62".equals(header)){// ģ��
				
				return null;
			} else {
				
				return null;
			}
		}
	}
	
	/**
	 * ��ʼ��Stage3D����
	 */
	private void initStage() {
		// �ƹ�ķ���
		vectLight = new Vector3f(1f, -1f, 0.5f).normalizeLocal();
		
		Bright      = 160;//DEFAULT_BRIGHT (smType.h)
	    Contrast    = 300;//DEFAULT_CONTRAST (smType.h)
	    
	    // Head = FALSE;
	    MemMode = 0;
	    SumCount = 0;
	    CalcSumCount = 0;
	    
	    nLight   = 0;
	    nTexLink = 0;
	    nFace    = 0;
	    nVertex  = 0;
	    
	    nVertColor  = 0;

	    // ::ZeroMemory( StageArea, sizeof(StageArea) );

	    AreaList        = null;
	    Vertex          = null;
	    Face            = null;
	    TexLink         = null;
	    Light           = null;
	    //smMaterialGroup = null;
	    //StageObject     = null;
	    smMaterial      = null;
	    //lpwAreaBuff     = null;
	    
	    //////////////////////
	    lpOldTexLink    = 0;
	    //////////////////////
	}
	
	/**
	 * ������̨����
	 * @return
	 */
	private Node loadStage() {
		/***********
		 * ��ȡSMD�ļ�
		 */
		// ��ʼ��smSTAGE3D����
		initStage();
		
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
		
		// ��ȡTEX_LINK(��ʵ����uv����)
		readTexLink();
		
		// ��ȡ�ƹ�
		if ( nLight > 0 ) {
			readLight();
		}
		
		// ���½���Face��TexLink֮��Ĺ���
		relinkFaceAndTex();

		/*************
		 * ����jme3����
		 */
		
		return buildStage3D();
	}
	
	/**
	 * ��ȡ�ļ�ͷ
	 */
	private void readHead() {
		header = getString(24);
		objCounter = getInt();
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
		
		assert buffer.position() == 556;
	}
	
	/**
	 * ��ȡsmSTAGE3D����
	 */
	private void readStage3D() {
		getInt();// Head
		buffer.get(new byte[262144]);//*StageArea[MAP_SIZE][MAP_SIZE]; 4 * 256 * 256 = 262144
		getInt();// *AreaList;
		AreaListCnt = getInt();
		MemMode = getInt();
		SumCount = getInt();
		CalcSumCount = getInt();
		
		getInt();// *Vertex
		getInt();// *Face
		lpOldTexLink = getInt();// *TexLink
		getInt();// *smLight
		getInt();// *smMaterialGroup
		getInt();// *StageObject
		getInt();// *smMaterial
		
		assert buffer.position() == 262752;
		
		nVertex = getInt();
		nFace = getInt();
		nTexLink = getInt();
		nLight = getInt();
		
		log.debug(String.format("V=%d F=%d T=%d L=%d", nVertex, nFace, nTexLink, nLight));
		
		nVertColor = getInt();
		Contrast = getInt();
		Bright = getInt();
		
		vectLight.x = getInt();
		vectLight.y = getInt();
		vectLight.z = getInt();
		
		getInt();// *lpwAreaBuff
		wAreaSize = getInt();
		
		// sizeof(RECT) == 16
		int minX = getInt();
		int minY = getInt();
		int maxX = getInt();
		int maxY = getInt();
		log.info("������ֵ�ǵ�ͼ�ı�Ե��x,zƽ��ľ��Ρ����εı߳����Ŵ���256��");
		log.info(String.format("min(%d, %d) max(%d, %d)", minX, minY, maxX, maxY));
		
		assert buffer.position() == 262816;
	}
	
	/**
	 * ���ز�������
	 */
	private void loadMaterial() {
		int size = 0;
		// ��ȡMaterialGroup����
		readMaterialGroup();
		size += 88;
		
		smMaterial = new MATERIAL[materialCount];
		
		for(int i=0; i<materialCount; i++) {
			smMaterial[i] = readMaterial();
			size += 320;
			
			if (smMaterial[i].InUse != 0) {
				int strLen = getInt();
				size += 4;
				size += strLen;
				
				smMaterial[i].smTexture = new TEXTUREHANDLE[smMaterial[i].TextureCounter];
				for(int j=0; j<smMaterial[i].TextureCounter; j++) {
					TEXTUREHANDLE texture = new TEXTUREHANDLE();
					smMaterial[i].smTexture[j] = texture;
					texture.Name = getString();
					texture.NameA = getString();
					
					// TODO ��texture.Name���뻺���У������μ��ء�
					
					if (texture.NameA.length() > 0) {
						log.info("TEX MIPMAP:" + texture.NameA);
					}
				}
				
				smMaterial[i].smAnimTexture = new TEXTUREHANDLE[smMaterial[i].AnimTexCounter];
				for(int j=0; j<smMaterial[i].AnimTexCounter; j++) {
					TEXTUREHANDLE texture = new TEXTUREHANDLE();
					smMaterial[i].smAnimTexture[j] = texture;
					texture.Name = getString();
					texture.NameA = getString();
					
					// TODO ��texture.Name���뻺���У������μ��ء�

					if (texture.NameA.length() > 0) {
						log.info("Anim MIPMAP:" + texture.NameA);
					}
				}
			}
		}
		
		log.debug("Material Size=" + size);
	}
	
	/**
	 * ��ȡsmMATERIAL_GROUP����
	 */
	private void readMaterialGroup() {
		getInt();// Head
		getInt();// *smMaterial
		materialCount = getInt();
		reformTexture = getInt();
		maxMaterial = getInt();
		lastSearchMaterial = getInt();
		lastSearchName = getString(64);
		
		assert buffer.position() == 262904;
	}
	
	/**
	 * ��ȡMATERIAL���ݽṹ
	 */
	private MATERIAL readMaterial() {
		int start = buffer.position();
		
		MATERIAL mat = new MATERIAL();
		
		mat.InUse = getInt(); // > 0 ��ʾ��ʹ��
		mat.TextureCounter = getInt();// ��������������������Ȼֻ��1�š�
		for(int i=0; i<8; i++) {
			getInt();// *smTexture[8];
		}
		for(int i=0; i<8; i++) {
			mat.TextureStageState[i] = getInt();
		}
		for(int i=0; i<8; i++) {
			mat.TextureFormState[i] = getInt();
		}
		mat.ReformTexture = getInt();

		/**
		 * ͸��
		 */
		mat.MapOpacity = getInt(); // TRUE or FALSE

		/**
		 * ��������
		 * #define SMTEX_TYPE_MULTIMIX		0x0000
		 * #define SMTEX_TYPE_ANIMATION		0x0001
		 */
		mat.TextureType = getInt();
		/**
		 * ��ɫ��ʽ
		 * #define SMMAT_BLEND_NONE			0x00
		 * #define SMMAT_BLEND_ALPHA		0x01
		 * #define SMMAT_BLEND_COLOR		0x02
		 * #define SMMAT_BLEND_SHADOW		0x03
		 * #define SMMAT_BLEND_LAMP			0x04
		 * #define SMMAT_BLEND_ADDCOLOR		0x05
		 * #define SMMAT_BLEND_INVSHADOW	0x06
		 */
		mat.BlendType = getInt(); // SMMAT_BLEND_XXXX

		mat.Shade = getInt(); // TRUE or FALSE
		mat.TwoSide = getInt(); // TRUE or FALSE
		mat.SerialNum = getInt(); // ��Ʈ���� ���� ���� ��ȣ

		mat.Diffuse = getVector3f(); // Diffuse ����
		mat.Transparency = getFloat(); //
		mat.SelfIllum = getFloat(); //

		mat.TextureSwap = getInt(); //
		mat.MatFrame = getInt(); //
		mat.TextureClip = getInt(); //

		// �޽� ���� �Ӽ�
		mat.UseState = getInt(); // ScriptState
		/**
		 * �Ƿ������ײ���
		 * #define SMMAT_STAT_CHECK_FACE	0x00000001
		 */
		mat.MeshState = getInt();

		// Mesh ���� ���� ����
		mat.WindMeshBottom = getInt(); // TODO @see smTexture.cpp �ű��ı��

		// ���ϸ��̼� �ؽ��� �Ӽ�
		for(int i=0; i<32; i++) {
			getInt();// *smAnimTexture[32]
		}
		mat.AnimTexCounter = getInt(); // �����м���ͼNumTex
		mat.FrameMask = getInt(); // NumTex-1
		mat.Shift_FrameSpeed = getInt(); // �����л��ٶȣ�Ĭ����6
		
		/**
		 * �Ƿ��Զ����Ŷ���
		 * #define SMTEX_AUTOANIMATION		0x100
		 * Ϊ0ʱ���Զ�����
		 */
		mat.AnimationFrame = getInt();
		
		assert (buffer.position() - start) == 320;
		
		return mat;
	}

	/**
	 * STAGE_VERTEX
	 * size = 28
	 */
	private void readVertex() {
		Vertex = new STAGE_VERTEX[nVertex];
		for(int i=0; i<nVertex; i++) {
			STAGE_VERTEX vert = new STAGE_VERTEX();
			Vertex[i] = vert;
			
			vert.sum = getInt();
			getInt();// *lpRendVertex

			// Vectex // ����256����ʵ�ʵ�ֵ
			vert.x = getInt() / 256f;
			vert.y = getInt() / 256f;
			vert.z = getInt() / 256f;
			
			// VectorColor
			vert.r = getShort() / 256f;
			vert.g = getShort() / 256f;
			vert.b = getShort() / 256f;
			vert.a = getShort() / 256f;
		}
	}

	/**
	 * ��ȡSTAGE_FACE
	 * size = 28
	 */
	private void readFace() {
		Face = new STAGE_FACE[nFace];
		for(int i=0; i<nFace; i++) {
			STAGE_FACE face = new STAGE_FACE();
			Face[i] = face;
			
			face.sum = getInt();
			face.CalcSum = getInt();
			
			face.a = getUnsignedShort();
			face.b = getUnsignedShort();
			face.c = getUnsignedShort();
			face.mat_id = getUnsignedShort();// ���ʵ�������
			
			face.lpTexLink = getInt();// ���������ָ�롣smTEX_LINK *lpTexLink
			
			face.nx = getShort()/32767f;// nx
			face.ny = getShort()/32767f;// ny
			face.nz = getShort()/32767f;// nz
			face.y = getShort()/32767f;// Y ����32767���� 1/8PI����֪���к��á�
		}
	}

	/**
	 * ��ȡTEXLINK
	 * size=32
	 */
	private void readTexLink() {
		TexLink = new TEXLINK[nTexLink];
		for(int i=0; i<nTexLink; i++) {
			TEXLINK tex = new TEXLINK();
			TexLink[i] = tex;
			
			tex.u[0] = getFloat();
			tex.u[1] = getFloat();
			tex.u[2] = getFloat();
			
			tex.v[0] = getFloat();
			tex.v[1] = getFloat();
			tex.v[2] = getFloat();
			
			tex.hTexture = getInt();// *hTexture;
			tex.lpNextTex = getInt();// *NextTex;
		}
	}
	
	/**
	 * ��ȡsmLIGHT3D
	 * size = 28
	 */
	private void readLight() {
		Light = new LIGHT3D[nLight];
		for(int i=0; i<nLight; i++) {
			LIGHT3D light = new LIGHT3D();
			Light[i] = light;
			
			light.type = getInt();
			light.x = getInt() / 256f;
			light.y = getInt() / 256f;
			light.z = getInt() / 256f;
			light.Range = getInt() / 64f / 256f;
			
			light.r = getUnsignedShort() / 255f;
			light.g = getUnsignedShort() / 255f;
			light.b = getUnsignedShort() / 255f;
		}
	}
	
	/**
	 * ���½���TexLink֮�䡢Face��TexLink֮��Ĺ�����
	 * 
	 * TexLink��һ��smTEXLINK���飬˳��洢��lpOldTexLink��¼�����׵�ַ��
	 * ����{@code sizeof(smTEXLINK) = 32}�����ԣ�{@code ������=(ԭ��ַ-lpOldTexLink)/32}
	 */
	private void relinkFaceAndTex() {
		// ���½���TexLink�����еĹ���
		for(int i=0; i<nTexLink; i++) {
			if ( TexLink[i].lpNextTex != 0) {
	            int index = (TexLink[i].lpNextTex - lpOldTexLink) / 32;
	            TexLink[i].NextTex = TexLink[index];
	        }
		}
		
		// ���½���Face��TexLink֮��Ĺ���
		for(int i=0; i<nFace; i++) {
	        if ( Face[i].lpTexLink != 0) {
	            int index = (Face[i].lpTexLink - lpOldTexLink) / 32;
	            Face[i].TexLink = TexLink[index];
	        }
	    }
	}
	
	
	
	/*******************************************************
	 * ����Ĵ������ڸ��ݾ�������ݽṹ����JME3���������ʡ�����ȶ���
	 *******************************************************/
	
	/**
	 * �ı��ļ��ĺ�׺��
	 * @param line
	 * @return
	 */
	private String changeName(String line) {
		line = line.replaceAll("\\\\", "/");
		int index = line.lastIndexOf("/");
		if (index != -1) {
			line = line.substring(index + 1);
		}
		
		return line;
	}
	/**
	 * ��������
	 * 
	 * @param name
	 */
	private Texture createTexture(String name) {
		name = changeName(name);
		Texture texture = null;
		try {
			texture = manager.loadTexture(key.getFolder() + name);
			texture.setWrap(WrapMode.Repeat);
		} catch (Exception ex) {
			log.warn("Cannot load texture image " + name, ex);
			texture = manager.loadTexture("Common/Textures/MissingTexture.png");
			texture.setWrap(WrapMode.EdgeClamp);
		}
		return texture;
	}
	
	/**
	 * ��������
	 * @param m
	 * @return
	 */
	private Material createLightMaterial(MATERIAL m) {
		Material mat = new Material(manager, "Common/MatDefs/Light/Lighting.j3md");
		mat.setColor("Diffuse", new ColorRGBA(m.Diffuse.x, m.Diffuse.y, m.Diffuse.z, 1));
		//mat.setBoolean("UseMaterialColors", true);
		
		RenderState rs = mat.getAdditionalRenderState();
		
		// TODO ������Щ�汻����ˣ�����ǿ��ʹ���ǿɼ���Ӧ�ø���MATERIAL�еĲ�����������
		rs.setFaceCullMode(FaceCullMode.Off);
		
		if(m.TextureCounter == 0) {
			rs.setFaceCullMode(FaceCullMode.FrontAndBack);
		}
		
		if (m.TwoSide == 1) {
			rs.setFaceCullMode(FaceCullMode.Off);
		}
		
		if (m.MapOpacity != 0) {
			mat.setFloat("AlphaDiscardThreshold", 0.01f);
		}

		// ������ͼ
		if (m.TextureCounter > 0) {
			mat.setTexture("DiffuseMap", createTexture(m.smTexture[0].Name));
		}
		if (m.TextureCounter > 1) {
			mat.setBoolean("SeparateTexCoord", true);
			mat.setTexture("LightMap", createTexture(m.smTexture[1].Name));
		}

		/**
			#define SMMAT_BLEND_NONE		0x00
			#define SMMAT_BLEND_ALPHA		0x01
			#define SMMAT_BLEND_COLOR		0x02
			#define SMMAT_BLEND_SHADOW		0x03
			#define SMMAT_BLEND_LAMP		0x04
			#define SMMAT_BLEND_ADDCOLOR	0x05
			#define SMMAT_BLEND_INVSHADOW	0x06
		 */
		switch (m.BlendType) {
		case 0:// SMMAT_BLEND_NONE
			rs.setBlendMode(BlendMode.Off);
			break;
		case 1:// SMMAT_BLEND_ALPHA
			rs.setBlendMode(BlendMode.Alpha);
			break;
		case 2:// SMMAT_BLEND_COLOR
			rs.setBlendMode(BlendMode.Color);
			break;
		case 3:// SMMAT_BLEND_SHADOW
			break;
		case 4:// SMMAT_BLEND_LAMP
			break;
		case 5:// SMMAT_BLEND_ADDCOLOR
			rs.setBlendMode(BlendMode.Additive);
			break;
		case 6:
			break;
		default:
			log.info("Unknown BlendType=" + m.BlendType);
		};
		
		// TODO ������smRender3d.cpp
		if (m.Transparency <= 0.2f) {
			rs.setDepthWrite(true);
		}
		return mat;
	}
	
	/**
	 * ����STAGE3D����
	 * @return
	 */
	private Node buildStage3D() {
		Node solidNode = new Node("SMMAT_STAT_CHECK_FACE");// ���������Ҫ������ײ���Ĳ���
		Node otherNode = new Node("SMMAT_STAT_NOT_CHECK_FACE");// ������Ų���Ҫ������ײ���Ĳ���
		
		Node rootNode = new Node("STAGE3D:" + key.getName());
		rootNode.attachChild(solidNode);
		rootNode.attachChild(otherNode);
		
		// ��������
		for(int mat_id=0; mat_id<materialCount; mat_id++) {
			MATERIAL m = smMaterial[mat_id];
			/**
			 * �ж�������Ƿ�ʹ�á�
			 * ʵ����smd�ļ��д洢�Ĳ��ʶ��Ǳ��õ��Ĳ��ʣ������ǲ���洢�ġ�
			 * �������жϲ�û��ʵ�����塣
			 */
			if (m.InUse == 0) {
				continue;
			}
			
			/**
			 * ͳ�Ʋ���Ϊmat_id����һ���ж��ٸ��棬���ڼ�����Ҫ���ɶ��ٸ�������
			 */
			int size = 0;
			for (int i = 0; i < nFace; i++) {
				if (Face[i].mat_id != mat_id)
					continue;
				size++;
			}
			if (size < 1)
				continue;
			
			// ��������
			Mesh mesh = buildStage3DMesh(size, mat_id);
			Geometry geom = new Geometry(key.getName() + "#" + mat_id, mesh);
			
			// ��������
			Material mat = createLightMaterial(smMaterial[mat_id]);
			geom.setMaterial(mat);
			
			// ͸����
			if (m.MapOpacity != 0) {
				geom.setQueueBucket(Bucket.Translucent);
			}
			
			if (m.MeshState == 0) {
				otherNode.attachChild(geom);
				log.debug("ID:" + mat_id + " MeshState=" + m.MeshState);// ͸����
			} else {
				solidNode.attachChild(geom);
			}
			
			// ��smTexture.cpp�п�֪��ֻ��Transparency==0���������Ҫ�����ײ����
			if (m.Transparency != 0) {
				otherNode.attachChild(geom);
				log.debug("Transparency=" + m.Transparency);// ͸����
			}
			
			if (m.ReformTexture > 0) {
				log.debug("ReformTexture=" + m.ReformTexture);// ��Ҫ�����ܵ�ͼƬ��Ŀ
			}
			if (m.SelfIllum > 0.0f) {
				log.debug("SelfIllum=" + m.SelfIllum);// �Է���
			}
			if (m.UseState != 0) {//ScriptState
				log.debug("UseState=" + m.UseState);// �нű�����
			}
			
			if (m.TextureType == 0) {
				// SMTEX_TYPE_MULTIMIX		0x0000
			} else {
				// SMTEX_TYPE_ANIMATION		0x0001
				
				// ����Ҳ��Ĭ����ʾ2��
				mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
				
				// �ж������
				if (m.AnimTexCounter > 0) {
					FrameAnimControl control = createFrameAnimControl(smMaterial[mat_id]);
					geom.addControl(control);
				}
			}

		}
		
		return rootNode;
	}

	private Mesh buildStage3DMesh(int size, int mat_id) {
		
		Vector3f[] position = new Vector3f[size * 3];
		int[] f = new int[size * 3];
		Vector2f[] uv1 = new Vector2f[size * 3];
		Vector2f[] uv2 = new Vector2f[size * 3];

		int index = 0;
		// Prepare MeshData
		for (int i = 0; i < nFace; i++) {
			// Check the MaterialIndex
			if (Face[i].mat_id != mat_id)
				continue;

			// ���� VERTEX
			position[index * 3 + 0] = new Vector3f(Vertex[Face[i].a].x, Vertex[Face[i].a].y, Vertex[Face[i].a].z);
			position[index * 3 + 1] = new Vector3f(Vertex[Face[i].b].x, Vertex[Face[i].b].y, Vertex[Face[i].b].z);
			position[index * 3 + 2] = new Vector3f(Vertex[Face[i].c].x, Vertex[Face[i].c].y, Vertex[Face[i].c].z);

			// �� FACE
			if (i < nFace) {
				f[index * 3 + 0] = index * 3 + 0;
				f[index * 3 + 1] = index * 3 + 1;
				f[index * 3 + 2] = index * 3 + 2;
			}

			// ԭ��ͼ�����ж����ͼ�����ʹ�ö��UV����
			for(int k=0; k<smMaterial[mat_id].TextureCounter; k++) {
				
			}
			// ����ӳ��
			TEXLINK tl = Face[i].TexLink;
			if(tl != null) {
				// ��1��uv����
				uv1[index * 3 + 0] = new Vector2f(tl.u[0], 1f - tl.v[0]);
				uv1[index * 3 + 1] = new Vector2f(tl.u[1], 1f - tl.v[1]);
				uv1[index * 3 + 2] = new Vector2f(tl.u[2], 1f - tl.v[2]);
			} else {
				uv1[index * 3 + 0] = new Vector2f();
				uv1[index * 3 + 1] = new Vector2f();
				uv1[index * 3 + 2] = new Vector2f();
			}
			
			// ��2��uv����
			if (tl != null && tl.NextTex != null) {
				tl = tl.NextTex;
				
				uv2[index * 3 + 0] = new Vector2f(tl.u[0], 1f - tl.v[0]);
				uv2[index * 3 + 1] = new Vector2f(tl.u[1], 1f - tl.v[1]);
				uv2[index * 3 + 2] = new Vector2f(tl.u[2], 1f - tl.v[2]);
			} else {
				uv2[index * 3 + 0] = new Vector2f();
				uv2[index * 3 + 1] = new Vector2f();
				uv2[index * 3 + 2] = new Vector2f();
			}
			
			index++;
		}

		Mesh mesh = new Mesh();
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(position));
		mesh.setBuffer(Type.Index, 3, f);
		// DiffuseMap UV
		mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv1));
		// LightMap UV
		mesh.setBuffer(Type.TexCoord2, 2, BufferUtils.createFloatBuffer(uv2));

		mesh.setStatic();
		mesh.updateBound();
		mesh.updateCounts();
		
		return mesh;
	}
	
	/**
	 * AminTexCounter����0˵�����ֲ�����������һ��Control����ʱ���»��档
	 * @param m
	 * @return
	 */
	private FrameAnimControl createFrameAnimControl(MATERIAL m) {
		FrameAnimControl control = new FrameAnimControl(m.AnimTexCounter);
		
		for(int i=0; i<m.AnimTexCounter; i++) {
			Texture tex = createTexture(m.smAnimTexture[i].Name);
			control.animTexture.add(tex);
		}
		return control;
	}
}
