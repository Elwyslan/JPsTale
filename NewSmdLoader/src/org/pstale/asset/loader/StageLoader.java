package org.pstale.asset.loader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.pstale.asset.loader.SmdKey.SMDTYPE;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;
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

	private FILE_HEADER smd_file_header;
	/**
	 * ����
	 */
	private PAT3D BipPattern = null;
	
	/**
	 * size = 64
	 */
	class FMATRIX {
		float _11, _12, _13, _14;
		float _21, _22, _23, _24;
		float _31, _32, _33, _34;
		float _41, _42, _43, _44;
		
		FMATRIX() {
			_11 = 1; _12 = 0; _13 = 0; _14 = 0;
			_21 = 0; _22 = 1; _23 = 0; _24 = 0;
			_31 = 0; _32 = 0; _33 = 1; _34 = 0;
			_41 = 0; _42 = 0; _43 = 0; _44 = 1;
		}
		FMATRIX(boolean init) {
			_11 = getFloat(); _12 = getFloat(); _13 = getFloat(); _14 = getFloat();
			_21 = getFloat(); _22 = getFloat(); _23 = getFloat(); _24 = getFloat();
			_31 = getFloat(); _32 = getFloat(); _33 = getFloat(); _34 = getFloat();
			_41 = getFloat(); _42 = getFloat(); _43 = getFloat(); _44 = getFloat();
		}
	}
	
	/**
	 * size = 64
	 */
	class MATRIX {
		int _11, _12, _13, _14;
		int _21, _22, _23, _24;
		int _31, _32, _33, _34;
		int _41, _42, _43, _44;
		
		MATRIX() {
			_11 = 1; _12 = 0; _13 = 0; _14 = 0;
			_21 = 0; _22 = 1; _23 = 0; _24 = 0;
			_31 = 0; _32 = 0; _33 = 1; _34 = 0;
			_41 = 0; _42 = 0; _43 = 0; _44 = 1;
		}
		
		/**
		 * ������������ֵ��256������Ԫ�س���256�������ʽ��1��
		 * @param init
		 */
		MATRIX(boolean init) {
			_11 = getInt(); _12 = getInt(); _13 = getInt(); _14 = getInt();
			_21 = getInt(); _22 = getInt(); _23 = getInt(); _24 = getInt();
			_31 = getInt(); _32 = getInt(); _33 = getInt(); _34 = getInt();
			_41 = getInt(); _42 = getInt(); _43 = getInt(); _44 = getInt();
		}
	}
	
	/**
	 * size = 20
	 *
	 */
	class TM_ROT {
		int frame;
		Quaternion quad;
		TM_ROT() {
			frame = getInt();
			
			float x, y, z, w;
			x = getFloat();
			y = getFloat();
			z = getFloat();
			w = getFloat();
			quad = new Quaternion(-x, -y, -z, w);
		}
	}

	/**
	 * size = 16
	 */
	class TM_POS {
		int frame;
		Vector3f vec3;
		TM_POS() {
			frame = getInt();
			float x, y, z;
			x = getFloat();
			y = getFloat();
			z = getFloat();
			
			vec3 = new Vector3f(x, y, z);
		}
	}
	
	/**
	 * size = 16
	 */
	class TM_SCALE {
		int frame;
		Vector3f scale;
		TM_SCALE() {
			frame = getInt();
			float x, y, z;
			x = getPTDouble();
			y = getPTDouble();
			z = getPTDouble();
			scale = new Vector3f(x, y, z);
		}
	}
	
	/**
	 * size = 16
	 */
	class FRAME_POS {
		int startFrame;
		int endFrame;
		int posNum;
		int posCnt;
		
		FRAME_POS() {
			startFrame = getInt();
			endFrame = getInt();
			posNum = getInt();
			posCnt = getInt();
		}
	}
	
	/**
	 * SMD�ļ�ͷ
	 * size = 556;
	 */
	class FILE_HEADER{
		String header;// 24�ֽ�
		int objCounter;
		int matCounter;
		int matFilePoint;
		int firstObjInfoPoint;
		int tmFrameCounter;
		FRAME_POS[] TmFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];// 512�ֽ�
		
		/**
		 * ��ȡ�ļ�ͷ
		 */
		FILE_HEADER() {
			header = getString(24);
			objCounter = getInt();
			matCounter = getInt();
			matFilePoint = getInt();
			firstObjInfoPoint = getInt();
			tmFrameCounter = getInt();
			for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
				TmFrame[i] = new FRAME_POS();
			}
			
			assert buffer.position() == 556;
			
			log.debug(header);
		}
	}
	
	/**
	 * size = 40
	 */
	class FILE_OBJINFO {
		/**
		 * ���������
		 */
		String NodeName;// 32�ֽ�
		/**
		 * ���Obj3D�������ļ�����ռ���ֽ�����
		 */
		int Length;
		/**
		 * ���Obj3D�������ļ��е���ʵλ�á�
		 */
		int ObjFilePoint;
		
		FILE_OBJINFO() {
			NodeName = getString(32);
			Length = getInt();
			ObjFilePoint = getInt();
		}
	}
	
	
	/**
	 * ���ļ�ͷ�е�mat>0��˵���в��ʡ�
	 * ��������������Ӧ����һ��������smMATERIAL_GROUP����
	 * size = 88��
	 */
	class MATERIAL_GROUP {
		// DWORD Head
		MATERIAL[] materials;
		int materialCount;
		int reformTexture;
		int maxMaterial;
		int lastSearchMaterial;
		String lastSearchName;
		
		////////////////
		// �����ȡ����������MaterialGroupռ���˶����ڴ棬û��ʵ�����塣
		int size = 0;
		////////////////
		/**
		 * ��ȡsmMATERIAL_GROUP����
		 */
		MATERIAL_GROUP() {
			int start = buffer.position();
			
			getInt();// Head
			getInt();// *smMaterial
			materialCount = getInt();
			reformTexture = getInt();
			maxMaterial = getInt();
			lastSearchMaterial = getInt();
			lastSearchName = getString(64);
			
			size += 88;
			
			assert buffer.position() - start == 88;
		}
		
		/**
		 * �������в���
		 */
		void loadFile() {
			materials = new MATERIAL[materialCount];
			
			for(int i=0; i<materialCount; i++) {
				materials[i] = new MATERIAL();
				size += 320;
				
				if (materials[i].InUse != 0) {
					int strLen = getInt();
					size += 4;
					size += strLen;
					
					materials[i].smTexture = new TEXTURE[materials[i].TextureCounter];
					for(int j=0; j<materials[i].TextureCounter; j++) {
						TEXTURE texture = new TEXTURE();
						materials[i].smTexture[j] = texture;
						texture.Name = getString();
						texture.NameA = getString();
						
						if (texture.NameA.length() > 0) {
							log.debug("TEX MIPMAP:" + texture.NameA);
						}
					}
					
					materials[i].smAnimTexture = new TEXTURE[materials[i].AnimTexCounter];
					for(int j=0; j<materials[i].AnimTexCounter; j++) {
						TEXTURE texture = new TEXTURE();
						materials[i].smAnimTexture[j] = texture;
						texture.Name = getString();
						texture.NameA = getString();
					}
				}
			}
			
			log.debug("Material Size=" + size);
		}
	}
	
	/**
	 * ����
	 * size = 320
	 * @author yanmaoyuan
	 *
	 */
	class MATERIAL {
		/**
		 * �ж�������Ƿ�ʹ�á�
		 * ʵ����smd�ļ��д洢�Ĳ��ʶ��Ǳ��õ��Ĳ��ʣ������ǲ���洢�ġ�
		 * ����ж����������û��ʵ�����塣
		 */
		int InUse;
		/**
		 * �����������
		 */
		int TextureCounter;
		/**
		 * ����ͼƬ�����ơ�
		 * ����STAGE3D��˵����1��������DiffuseMap����2������Ӧ����LightMap��
		 * �����������֪����ʲô�á�
		 */
		TEXTURE[] smTexture = new TEXTURE[8];
		int[] TextureStageState = new int[8];
		int[] TextureFormState = new int[8];
		int ReformTexture;

		/**
		 * �Ƿ�͸�� ( TRUE , FALSE )
		 */
		int MapOpacity;

		/**
		 * �������ͣ���ɫ�򶯻�<pre>
		 * #define SMTEX_TYPE_MULTIMIX		0x0000
		 * #define SMTEX_TYPE_ANIMATION		0x0001</pre>
		 */
		int TextureType;
		
		/**
		 * ��ɫ��ʽ<pre>
		 * #define SMMAT_BLEND_NONE		0x00
		 * #define SMMAT_BLEND_ALPHA		0x01
		 * #define SMMAT_BLEND_COLOR		0x02
		 * #define SMMAT_BLEND_SHADOW		0x03
		 * #define SMMAT_BLEND_LAMP		0x04
		 * #define SMMAT_BLEND_ADDCOLOR	0x05
		 * #define SMMAT_BLEND_INVSHADOW	0x06</pre>
		 */
		int BlendType;// SMMAT_BLEND_XXXX

		/**
		 * TODO δ֪����
		 * TRUE or FALSE
		 */
		int Shade;
		/**
		 * �Ƿ����涼��ʾ TRUE or FALSE
		 */
		int TwoSide; // ��� ��� ����
		int SerialNum; // ��Ʈ���� ���� ���� ��ȣ

		/**
		 * ���ʵ���ɫ
		 */
		FCOLOR Diffuse;
		/**
		 * ͸���ȣ�ȡֵ��Χ(0~1f)�������ʵ�͸���ȴ���0.2�������ģ�Ͳ���Ҫ������ײ��⡣
		 */
		float Transparency;
		/**
		 * ����̶�
		 */
		float SelfIllum;

		int TextureSwap; // �ؽ��� ������
		int MatFrame; // ��������� ( ���� ���⸦ ���߱� ���� )
		int TextureClip; // �����ο� �ؽ��� Ŭ������ ( TRUE �� �ؽ��� Ŭ���� �㰡 )

		/**
		 * ����ASEģ���е�ScriptState
		 */
		int UseState;
		/**
		 * �Ƿ������ײ���<pre>
		 * #define SMMAT_STAT_CHECK_FACE	0x00000001</pre>
		 */
		int MeshState;

		// Mesh ���� ���� ����
		int WindMeshBottom; // �ٶ��ұ� �޽� ���� ���� ��

		// ���ϸ��̼� �ؽ��� �Ӽ�
		TEXTURE[] smAnimTexture = new TEXTURE[32]; // �ִϸ��̼� �ؽ��� �ڵ� ����Ʈ
		/**
		 * �����м���ͼNumTex
		 */
		int AnimTexCounter;
		int FrameMask; // �ִϸ��̼ǿ� ������ ����ũ
		/**
		 * �����л��ٶȡ�
		 */
		int Shift_FrameSpeed;
		/**
		 * SMTEX_AUTOANIMATION
		 */
		int AnimationFrame;
		
		/**
		 * ��ȡMATERIAL���ݽṹ
		 */
		MATERIAL() {
			int start = buffer.position();
			
			InUse = getInt(); // > 0 ��ʾ��ʹ��
			TextureCounter = getInt();// ��������������������Ȼֻ��1�š�
			for(int i=0; i<8; i++) {
				getInt();// *smTexture[8];
			}
			for(int i=0; i<8; i++) {
				TextureStageState[i] = getInt();
			}
			for(int i=0; i<8; i++) {
				TextureFormState[i] = getInt();
			}
			ReformTexture = getInt();

			
			MapOpacity = getInt();

			TextureType = getInt();

			BlendType = getInt();

			Shade = getInt();
			TwoSide = getInt();
			SerialNum = getInt();

			Diffuse = new FCOLOR();
			Transparency = getFloat();
			SelfIllum = getFloat(); //

			TextureSwap = getInt(); //
			MatFrame = getInt(); //
			TextureClip = getInt(); //

			UseState = getInt(); // ScriptState
			MeshState = getInt();

			// Mesh ���� ���� ����
			WindMeshBottom = getInt(); // TODO @see smTexture.cpp �ű��ı��

			// ���ϸ��̼� �ؽ��� �Ӽ�
			for(int i=0; i<32; i++) {
				getInt();// *smAnimTexture[32]
			}
			AnimTexCounter = getInt(); 
			FrameMask = getInt(); // NumTex-1
			Shift_FrameSpeed = getInt();
			
			/**
			 * �Ƿ��Զ����Ŷ���
			 * #define SMTEX_AUTOANIMATION		0x100
			 * Ϊ0ʱ���Զ�����
			 */
			AnimationFrame = getInt();
			
			assert (buffer.position() - start) == 320;
		}
	}
	
	class TEXTURE {
		String Name;// [64];
		String NameA;// [64];
		int Width, Height;
		int UsedTime;
		int UseCounter;// ��������Ǹ��������ı�־λ����¼���Texture�Ƿ��Ѿ�ʹ�á�
		int MapOpacity; // �Ƿ�͸��( TRUE , FALSE )
		int TexSwapMode; // ( TRUE / FALSE )
		TEXTURE TexChild;
	}
	
	/**
	 * MaterialGroup��ʹ�����������¼Diffuse
	 * size = 12��
	 */
	class FCOLOR {
		float r, g, b;
		FCOLOR() {
			r = getFloat();
			g = getFloat();
			b = getFloat();
		}
	}
	// size = 8
	class FTPOINT {
	    float u,v;
	    FTPOINT() {
	    	u = getFloat();
	    	v = getFloat();
	    }
	}
	
	// size = 12
	class POINT3D {
		int x, y ,z;
		
		POINT3D() {
			x = y = z = 0;
		}
		POINT3D(boolean init) {
			x = getInt();
			y = getInt();
			z = getInt();
		}
	}
	
	/**
	 * size = 24
	 *
	 */
	class VERTEX {
		Vector3f v;// ����
		Vector3f n;// normals ������
		
		VERTEX() {
			v = getPTPoint3f();
			n = getPTPoint3f();
		}
	}
	
	/**
	 * size = 36
	 */
	class FACE {
		int[] v= new int[4];// a,b,c,Matrial
	    FTPOINT[] t = new FTPOINT[3];
	    int lpTexLink;
	    TEXLINK TexLink;
	    
	    FACE() {
	    	for(int i=0; i<4; i++) {
	    		v[i] = getUnsignedShort();
	    	}
	    	
	    	for(int i=0; i<3; i++) {
	    		t[i] = new FTPOINT();
	    	}
	    	
	    	lpTexLink = getInt();
	    }
	}
	/**
	 * size = 28
	 */
	class STAGE_VERTEX {
	    int sum;
	    //smRENDVERTEX *lpRendVertex;
	    Vector3f v;
	    ColorRGBA vectorColor;
	    
	    STAGE_VERTEX() {
	    	sum = getInt();
			getInt();// *lpRendVertex

			// Vectex // ����256����ʵ�ʵ�ֵ
			v = getPTPoint3f();
			
			// VectorColor
			// ����256��������ColorRGBA
			float r = getShort() / 256f;
			float g = getShort() / 256f;
			float b = getShort() / 256f;
			float a = getShort() / 256f;
			vectorColor = new ColorRGBA(r, g, b, a);
	    }
	}
	
	/**
	 * size = 28
	 *
	 */
	class STAGE_FACE {
	    int sum;
	    int CalcSum;
	    int v[] = new int[4];//a, b, c, mat_id;
	    int lpTexLink;// ����һ��ָ�룬ָ��TEXLINK�ṹ��
	    TEXLINK TexLink;// ��lpTexLink != 0����TexLinkָ��һ��ʵ�ʵĶ�����

	    float nx, ny, nz, y;// Cross����( Normal )  ( nx , ny , nz , [0,1,0]���� Y ); 
	    
	    STAGE_FACE() {
	    	sum = getInt();
			CalcSum = getInt();
			
			for(int i=0; i<4; i++) {
				v[i] = getUnsignedShort();
			}
			
			lpTexLink = getInt();// ���������ָ�롣smTEX_LINK *lpTexLink
			
			nx = getShort()/32767f;// nx
			ny = getShort()/32767f;// ny
			nz = getShort()/32767f;// nz
			y = getShort()/32767f;// Y ����32767���� 1/8PI����֪���к��á�
	    }
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
		
		TEXLINK() {
			u[0] = getFloat();
			u[1] = getFloat();
			u[2] = getFloat();
			
			v[0] = getFloat();
			v[1] = getFloat();
			v[2] = getFloat();
			
			hTexture = getInt();// *hTexture;
			lpNextTex = getInt();// *NextTex;
		}
	}
	
	/**
	 * size = 22
	 */
	class LIGHT3D {
	    int type;
	    
	    Vector3f location;
	    float range;
	    ColorRGBA color;
	    
	    LIGHT3D() {
	    	type = getInt();
	    	
	    	location = getPTPoint3f();
	    	
			range = getInt() / 256f / 256f;
			
			float r = getUnsignedShort() / 255f;
			float g = getUnsignedShort() / 255f;
			float b = getUnsignedShort() / 255f;
			color = new ColorRGBA(r, g, b, 1f);
	    }
	}
	
	/**
	 * Stage3D���������
	 * �ļ����ݵĵڶ��Σ��洢��һ��������smSTAGE3D���� size = 262260
	 * ���еĹؼ�������nVertex/nFace/nTexLink/nLight��Щ��
	 */
	class STAGE3D {
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
		MATERIAL_GROUP    materialGroup;// sizeof(smMaterialGroup) = 88
		// smSTAGE_OBJECT      *StageObject;
		MATERIAL[]          materials;
		
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
		 * ��ʼ��Stage3D����
		 */
		protected STAGE3D() {
			int start = buffer.position();
			
		    // Head = FALSE;
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
			
			nVertex = getInt();
			nFace = getInt();
			nTexLink = getInt();
			nLight = getInt();
			
			nVertColor = getInt();
			Contrast = getInt();
			Bright = getInt();
			
			// �ƹ�ķ���
			vectLight = new Vector3f();
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
			log.debug("������ֵ�ǵ�ͼ�ı�Ե��x,zƽ��ľ��Ρ����εı߳����Ŵ���256��");
			log.debug(String.format("min(%d, %d) max(%d, %d)", minX, minY, maxX, maxY));
			
			assert buffer.position() - start== 262260;
		}
		
		/**
		 * ������̨����
		 * @return
		 */
		void loadFile() {
			// ��ȡMaterialGroup
			if (smd_file_header.matCounter > 0) {
				// ��ȡMaterialGroup����
				materialGroup = new MATERIAL_GROUP();
				materialGroup.loadFile();
				materials = materialGroup.materials;
			}
			
			// ��ȡVertex
			Vertex = new STAGE_VERTEX[nVertex];
			for(int i=0; i<nVertex; i++) {
				Vertex[i] = new STAGE_VERTEX();
			}
			
			// ��ȡFace
			Face = new STAGE_FACE[nFace];
			for(int i=0; i<nFace; i++) {
				Face[i] = new STAGE_FACE();
			}
			
			// ��ȡTEX_LINK(��ʵ����uv����)
			TexLink = new TEXLINK[nTexLink];
			for(int i=0; i<nTexLink; i++) {
				TexLink[i] = new TEXLINK();
			}
			
			// ��ȡ�ƹ�
			if ( nLight > 0 ) {
				Light = new LIGHT3D[nLight];
				for(int i=0; i<nLight; i++) {
					Light[i] = new LIGHT3D();
				}
			}
			
			// ���½���Face��TexLink֮��Ĺ���
			relinkFaceAndTex();
		}
		
		/**
		 * ���½���TexLink֮�䡢Face��TexLink֮��Ĺ�����
		 * 
		 * TexLink��һ��smTEXLINK���飬˳��洢��lpOldTexLink��¼�����׵�ַ��
		 * ����{@code sizeof(smTEXLINK) = 32}�����ԣ�{@code ������=(ԭ��ַ-lpOldTexLink)/32}
		 */
		void relinkFaceAndTex() {
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
		
		/**
		 * ����STAGE3D����
		 * @return
		 */
		Node buildStage3D() {
			Node rootNode = new Node("STAGE3D:" + key.getName());
			
			Node solidNode = new Node("SMMAT_STAT_CHECK_FACE");// ���������Ҫ������ײ���Ĳ���
			Node otherNode = new Node("SMMAT_STAT_NOT_CHECK_FACE");// ������Ų���Ҫ������ײ���Ĳ���
			rootNode.attachChild(solidNode);
			rootNode.attachChild(otherNode);
			
			int materialCount = materialGroup.materialCount;
			
			// ��������
			for(int mat_id=0; mat_id<materialCount; mat_id++) {
				MATERIAL m = materials[mat_id];
				
				if (m.InUse == 0) {
					continue;
				}
				
				/**
				 * ͳ�Ʋ���Ϊmat_id����һ���ж��ٸ��棬���ڼ�����Ҫ���ɶ��ٸ�������
				 */
				int size = 0;
				for (int i = 0; i < nFace; i++) {
					if (Face[i].v[3] != mat_id)
						continue;
					size++;
				}
				if (size < 1)
					continue;
				
				// ��������
				Mesh mesh = buildStage3DMesh(size, mat_id);
				Geometry geom = new Geometry(key.getName() + "#" + mat_id, mesh);
				
				// ��������
				Material mat = createLightMaterial(materials[mat_id]);
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
						FrameAnimControl control = createFrameAnimControl(materials[mat_id]);
						geom.addControl(control);
					}
				}

			}
			
			if (nLight > 0) {
				rootNode.attachChild(lightNode());
			}
			
			return rootNode;
		}
		
		Node lightNode() {
			
			Node lightNode = new Node("LIGHT3D");// �ƹ�ڵ㣬������е�ģ��ƹ�λ�ã������ڵ��ԡ�
			
			for(int i=0; i<nLight; i++) {
				Sphere mesh = new Sphere(12, 12, Light[i].range);
				FloatBuffer fb = (FloatBuffer)mesh.getBuffer(Type.Position).getData();
				// ������Ŀ
				int cnt = fb.capacity() / 3;
				for(int n=0; n<cnt; n++) {
					float x = fb.get(n*3);
					float y = fb.get(n*3+1);
					float z = fb.get(n*3+2);
					
					x+= Light[i].location.x;
					y+= Light[i].location.y;
					z+= Light[i].location.z;
					
					fb.put(n*3, x);
					fb.put(n*3+1, y);
					fb.put(n*3+2, z);
				}
				//mesh.setBuffer(Type.Position, 3, fb);
				mesh.updateBound();
				
				Geometry geom = new Geometry("Light"+i, mesh);
				
				// ����
				Material mat = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", Light[i].color);
				mat.getAdditionalRenderState().setWireframe(true);
				geom.setMaterial(mat);
				
				lightNode.attachChild(geom);
			}
			
			return lightNode;
		}

		Mesh buildStage3DMesh(int size, int mat_id) {
			
			Vector3f[] position = new Vector3f[size * 3];
			int[] f = new int[size * 3];
			Vector2f[] uv1 = new Vector2f[size * 3];
			Vector2f[] uv2 = new Vector2f[size * 3];

			int index = 0;
			// Prepare MeshData
			for (int i = 0; i < nFace; i++) {
				// Check the MaterialIndex
				if (Face[i].v[3] != mat_id)
					continue;

				// ˳����3������
				for(int vIndex=0; vIndex<3; vIndex++) {
					// ���� VERTEX
					position[index * 3 + vIndex] = Vertex[Face[i].v[vIndex]].v;
	
					// �� FACE
					f[index * 3 + vIndex] = index * 3 + vIndex;
	
					// ����ӳ��
					TEXLINK tl = Face[i].TexLink;
					if(tl != null) {
						// ��1��uv����
						uv1[index * 3 + vIndex] = new Vector2f(tl.u[vIndex], 1f - tl.v[vIndex]);
					} else {
						uv1[index * 3 + vIndex] = new Vector2f();
					}
					
					// ��2��uv����
					if (tl != null && tl.NextTex != null) {
						tl = tl.NextTex;
						uv2[index * 3 + vIndex] = new Vector2f(tl.u[vIndex], 1f - tl.v[vIndex]);
					} else {
						uv2[index * 3 + vIndex] = new Vector2f();
					}
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
	}

	class SMotionStEndInfo {
		int	StartFrame;
		int	EndFrame;
	}
	/**
	 * size = 2236
	 */
	class OBJ3D {
		//DWORD		Head;
		VERTEX[] Vertex;				// ���ؽ�
		FACE[] Face;					// ���̽�
		TEXLINK[] TexLink;				//�ؽ��� ��ǥ ����Ʈ

		OBJ3D[] Physique; // ������Ĺ���

		VERTEX	ZeroVertex;				// ������Ʈ �߾� ���ؽ� ��

		int maxZ,minZ;
		int maxY,minY;
		int maxX,minX;

		int dBound;							// �ٿ�� ����� �� ^2
		int Bound;							// �ٿ�� ����� ��

		int MaxVertex;
		int MaxFace;

		int nVertex;
		int nFace;

		int nTexLink;

		int ColorEffect;					// ����ȿ�� ��� ����
		int ClipStates;					// Ŭ���� ����ũ ( �� Ŭ���κ� ��� ���� ) 

		POINT3D Posi;
		POINT3D CameraPosi;
		POINT3D Angle;
		int[]	Trig = new int[8];

		// �ִϸ��̼� ����
		String NodeName;//[32];		// ������Ʈ�� ��� �̸�
		String NodeParent;//[32];		// �θ� ������Ʈ�� �̸�
		OBJ3D pParent;			// �θ� ������Ʈ ������

		MATRIX	Tm;				// �⺻ TM ���
		MATRIX	TmInvert;		// �����
		FMATRIX	TmResult;		// �ִϸ��̼� ���
		MATRIX	TmRotate;		// �⺻�� ȸ�� ��� 

		MATRIX	mWorld;			// ������ǥ ��ȯ ���
		MATRIX	mLocal;			// ��Į��ǥ ��ȯ ���

		int		lFrame;				// ���� ������

		float	qx,qy,qz,qw;		// ȸ�� ���ʹϾ�
		int		sx,sy,sz;			// ������ ��ǥ
		int		px,py,pz;			// ������ ��ǥ

		TM_ROT[] TmRot;			// �����Ӻ� ȸ�� �ִϸ��̼�
		TM_POS[] TmPos;			// �����Ӻ� ������ �ִϸ��̼�
		TM_SCALE[] TmScale;		// �����Ӻ� ������ �ִϸ��̼�

		FMATRIX[] TmPrevRot; // ֡�Ķ�������

		int TmRotCnt;
		int TmPosCnt;
		int TmScaleCnt;

		//TM ������ ��ġ ( �������� ������ ã�Ⱑ ���� )
		FRAME_POS[] TmRotFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		FRAME_POS[] TmPosFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		FRAME_POS[] TmScaleFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		int TmFrameCnt;									//TM������ ī���� (��ü����)

		////////////////////
		int lpPhysuque;
		int lpOldTexLink;
		////////////////////
		
		OBJ3D() {
			NodeName = null;
			NodeParent = null;
			Tm = new MATRIX();
			pParent = null;
			TmRot = null;
			TmPos = null;
			TmScale = null;
			TmRotCnt = 0;
			TmPosCnt = 0;
			TmScaleCnt = 0;
			TmPrevRot = null;
			Face=null;
			Vertex=null;
			TexLink=null;
			Physique = null;
		}
		
		void readOBJ3D() {
			int start = buffer.position();
			
			getInt();// Head `DCB\0`
			getInt();// smVERTEX	*Vertex;
			getInt();// smFACE		*Face;
			lpOldTexLink = getInt();// smTEXLINK	*TexLink;
			lpPhysuque = getInt();// smOBJ3D		**Physique;
			
			ZeroVertex = new VERTEX();
			
			maxZ = getInt();minZ = getInt();
			maxY = getInt();minY = getInt();
			maxX = getInt();minX = getInt();

			dBound = getInt();
			Bound = getInt();

			MaxVertex = getInt();
			MaxFace = getInt();

			nVertex = getInt();
			nFace = getInt();

			nTexLink = getInt();

			ColorEffect = getInt();
			ClipStates = getInt();

			Posi = new POINT3D(true);
			CameraPosi = new POINT3D(true);
			Angle = new POINT3D(true);
			Trig = new int[8];
			for(int i=0; i<8; i++) {
				Trig[i] = getInt();
			}

			// �ִϸ��̼� ����
			NodeName = getString(32);
			NodeParent = getString(32);
			getInt();// OBJ3D *pParent;

			Tm = new MATRIX(true);
			TmInvert = new MATRIX(true);
			TmResult = new FMATRIX(true);
			TmRotate = new MATRIX(true);

			mWorld = new MATRIX(true);
			mLocal = new MATRIX(true);

			lFrame = getInt();

			qx = getFloat();qy = getFloat();qz = getFloat();qw = getFloat();
			sx = getInt();sy = getInt();sz = getInt();
			px = getInt();py = getInt();pz = getInt();
			
			getInt();// smTM_ROT	*TmRot;
			getInt();// smTM_POS	*TmPos;
			getInt();// smTM_SCALE	*TmScale;
			getInt();// smFMATRIX	*TmPrevRot;
			
			TmRotCnt = getInt();
			TmPosCnt = getInt();
			TmScaleCnt = getInt();
			
			for(int i=0; i<OBJ_FRAME_SEARCH_MAX; i++) {
				TmRotFrame[i] = new FRAME_POS();
			}
			for(int i=0; i<OBJ_FRAME_SEARCH_MAX; i++) {
				TmPosFrame[i] = new FRAME_POS();
			}
			for(int i=0; i<OBJ_FRAME_SEARCH_MAX; i++) {
				TmScaleFrame[i] = new FRAME_POS();
			}
			TmFrameCnt = getInt();
			
			assert buffer.position() - start == 2236;
		}
		
		void loadFile(PAT3D PatPhysique) {
			readOBJ3D();
			
			Vertex = new VERTEX[ nVertex ];
			for(int i=0; i<nVertex; i++) {
				Vertex[i] = new VERTEX();
			}

			Face = new FACE[ nFace ];
			for(int i=0; i<nFace; i++) {
				Face[i] = new FACE();
			}

			TexLink = new TEXLINK[ nTexLink ];
			for(int i=0; i<nTexLink; i++) {
				TexLink[i] = new TEXLINK();
			}

			TmRot = new TM_ROT[ TmRotCnt ];
			for(int i=0; i<TmRotCnt; i++) {
				TmRot[i] = new TM_ROT();
			}

			TmPos = new TM_POS[ TmPosCnt ];
			for(int i=0; i<TmPosCnt; i++) {
				TmPos[i] = new TM_POS();
			}

			TmScale = new TM_SCALE[TmScaleCnt];
			for(int i=0; i<TmScaleCnt; i++) {
				TmScale[i] = new TM_SCALE();
			}

			TmPrevRot	= new FMATRIX[ TmRotCnt ];	
			for(int i=0; i<TmRotCnt; i++) {
				TmPrevRot[i] = new FMATRIX(true);
			}
			
			relinkFaceAndTex();
			
			if ( lpPhysuque != 0 && PatPhysique != null ) {
				
				Physique = new OBJ3D [ nVertex ];
				
				String[] names = new String[nVertex];
				for(int i=0; i<nVertex; i++) {
					names[i] = getString(32);
				}

				for(int i=0; i<nVertex ; i++ ) {
					Physique[i] = PatPhysique.getObjectFromName( names[i] );
				}

			}
			
			
		}
		
		void relinkFaceAndTex() {
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

		Geometry buildOBJ3D() {
			// ��������
			Mesh mesh = buildOBJ3DMesh();
			Geometry geom = new Geometry(NodeName, mesh);
			
			return geom;
		}

		Mesh buildOBJ3DMesh() {
			
			Vector3f[] position = new Vector3f[nFace * 3];
			int[] f = new int[nFace * 3];
			Vector2f[] uv1 = new Vector2f[nFace * 3];

			int index = 0;
			// Prepare MeshData
			for (int i = 0; i < nFace; i++) {

				// ���� VERTEX
				position[index * 3 + 0] = Vertex[Face[i].v[0]].v;
				position[index * 3 + 1] = Vertex[Face[i].v[1]].v;
				position[index * 3 + 2] = Vertex[Face[i].v[2]].v;

				// �� FACE
				if (i < nFace) {
					f[index * 3 + 0] = index * 3 + 0;
					f[index * 3 + 1] = index * 3 + 1;
					f[index * 3 + 2] = index * 3 + 2;
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
				
				index++;
			}

			Mesh mesh = new Mesh();
			mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(position));
			mesh.setBuffer(Type.Index, 3, f);
			// DiffuseMap UV
			mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv1));

			mesh.setStatic();
			mesh.updateBound();
			mesh.updateCounts();
			
			return mesh;
		}
	}
	
	/**
	 * size = 1228
	 */
	class PAT3D {
		//DWORD	Head;
		OBJ3D[] obj3d = new OBJ3D[128];
		byte[] TmSort = new byte[128];

		PAT3D TmParent;

		MATERIAL_GROUP smMaterialGroup;		//��Ʈ���� �׷�

		int MaxFrame;
		int Frame;

		int SizeWidth , SizeHeight;					// ���� ���� �� �ִ�ġ 

		int nObj3d;
		//LPDIRECT3DTEXTURE2 *hD3DTexture;

		POINT3D Posi;
		POINT3D Angle;
		POINT3D CameraPosi;

		int dBound;
		int Bound;

		FRAME_POS[] TmFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		int TmFrameCnt;

		int TmLastFrame;
		POINT3D TmLastAngle;
	
		PAT3D() {
			
		}

		PAT3D(boolean init) {
			int start = buffer.position();
			
			getInt();// Head
			for(int i=0; i<128; i++) {
				getInt();
			}
			buffer.get(TmSort);
			
			getInt();//smPAT3D		*TmParent;

			getInt();//smMATERIAL_GROUP	*smMaterialGroup;		//��Ʈ���� �׷�

			MaxFrame = getInt();
			Frame = getInt();

			SizeWidth = getInt(); SizeHeight = getInt();

			nObj3d = getInt();
			getInt();//LPDIRECT3DTEXTURE2 *hD3DTexture;

			Posi = new POINT3D(true);
			Angle = new POINT3D(true);
			CameraPosi = new POINT3D(true);

			dBound = getInt();
			Bound = getInt();

			for(int i=0; i<OBJ_FRAME_SEARCH_MAX; i++) {
				TmFrame[i] = new FRAME_POS();
			}
			TmFrameCnt = getInt();

			TmLastFrame = getInt();
			TmLastAngle = new POINT3D(true);
		
			assert buffer.position() - start == 1228;
		}
		
		void init() {
			nObj3d = 0;
			//hD3DTexture = 0;
			TmParent = null;

			MaxFrame = 0;
			Frame = 0;

			SizeWidth = 0;
			SizeHeight = 0;

			Bound = 0;
			dBound = 0;

			TmFrameCnt = 0;

			TmLastFrame = -1;
			
			TmLastAngle = new POINT3D();
			TmLastAngle.x = -1;
			TmLastAngle.y = -1;
			TmLastAngle.z = -1;

			for( int i=0;i<128;i++) {
				TmSort[i]=(byte)i;
			}

			smMaterialGroup = null;
		}
		void loadFile(String NodeName) {
			log.debug("ģ���ļ�:" + key.getName());
			
			OBJ3D obj;
			PAT3D BipPat;
			FILE_HEADER	FileHeader = smd_file_header;

			init();
			
			BipPat = BipPattern;
			
			// ��ȡObj3D������Ϣ
			FILE_OBJINFO[] FileObjInfo = new FILE_OBJINFO [ FileHeader.objCounter ];
			for(int i=0; i<FileHeader.objCounter; i++) {
				FileObjInfo[i] = new FILE_OBJINFO();
			}
			
			// ��¼�ļ�ͷ�еĶ�����֡��������ÿ֡�����ݡ�
			TmFrameCnt = FileHeader.tmFrameCounter;
			for(int i=0; i<32; i++) {
				TmFrame[i] = FileHeader.TmFrame[i];
			}
			
			// ��ȡ����
			// �����ļ�(.smb)�в��������ʣ���˿���û����һ�����ݡ�
			if ( FileHeader.matCounter > 0) {
				smMaterialGroup = new MATERIAL_GROUP();
				smMaterialGroup.loadFile();
			}
			
			if ( NodeName != null ) {
				log.debug("NodeName != null && NodeName == " + NodeName);
				// ����ָ�����Ƶ�3D����
				for(int i=0;i<FileHeader.objCounter;i++) {
					if ( NodeName.equals( FileObjInfo[i].NodeName ) ) {
						obj = new OBJ3D();
						if ( obj != null) {
							buffer.position(FileObjInfo[i].ObjFilePoint);
							obj.loadFile( BipPat );
							addObject( obj );
						}
						break;
					}
				}
			} else {
				// ��ȡȫ��3D����
				for(int i=0;i<FileHeader.objCounter;i++) {
					obj = new OBJ3D();
					if ( obj != null ) {
						obj.loadFile( BipPat );
						addObject( obj );
					}
				}
				linkObject();
			}

			TmParent = BipPat;
		}
		
		boolean addObject(OBJ3D obj ) {
			// ������������������128��
			if ( nObj3d < 128 ) {
				obj3d[ nObj3d ] = obj;
				nObj3d ++;

				// ͳ�ƶ���֡��
				int frame = 0;
				if ( obj.TmRotCnt>0 && obj.TmRot != null) 
					frame = obj.TmRot[ obj.TmRotCnt-1 ].frame;
				if ( obj.TmPosCnt>0 && obj.TmPos != null ) 
					frame = obj.TmPos[ obj.TmPosCnt-1 ].frame;
				if ( MaxFrame<frame ) 
					MaxFrame = frame;

				//ũ�� ���� ����
				if ( SizeWidth < obj.maxX ) SizeWidth = obj.maxX;
				if ( SizeWidth < obj.maxZ ) SizeWidth = obj.maxZ;
				if ( SizeHeight < obj.maxY ) SizeHeight = obj.maxY;

				//�ٿ�� ����� ��
				if ( Bound<obj.Bound ) {
					Bound = obj.Bound;
					dBound = obj.dBound;
				}

				return true;
			}

			return false;
		}
		
		/**
		 * ��������֮��ĸ��ӹ�ϵ��
		 */
		void linkObject() {
			for(int i=0; i<nObj3d ; i++ ) {
				if ( obj3d[i].NodeParent != null) {
					for(int k=0; k<nObj3d; k++ ) {
						if (  obj3d[i].NodeParent.equals( obj3d[k].NodeName )) {
							obj3d[i].pParent = obj3d[k];
							break;
						}
					}
				} else {
					log.debug("j = 0");
				}
			}

			int NodeCnt =0;

			// ����
			for(int i=0;i<128;i++) {
				TmSort[i]=0;
			}

			// ���ȼ�¼���ڵ�
			for(int i=0;i<nObj3d; i++ ) {
				if ( obj3d[i].pParent == null ) 
					TmSort[NodeCnt++] = (byte)i;
			}

			// �θ� �޷��ִ� �ڽ��� ã�� ������� ����
			for(int j=0;j<nObj3d; j++ ) {
				for(int i=0; i<nObj3d; i++ ) {
					if ( obj3d[i].pParent !=null && obj3d[TmSort[j]]==obj3d[i].pParent ) {
						TmSort[NodeCnt++] = (byte)i;
					}
				}
			}
		}
		
		/**
		 * ���ݽ�����ƣ���ѯObj3D����
		 * @param name
		 * @return
		 */
		OBJ3D getObjectFromName(String name) {
			for(int i=0; i<nObj3d; i++) {
				if(obj3d[i].NodeName.equals(name)) {
					return obj3d[i];
				}
			}
			return null;
		}
		
		/**
		 * ���ɹ���
		 */
		void buildSkeleton() {
			/*
			for(int i=0; i<nObj3d; i++) {
				MATRIX TmInvert = obj3d[i].TmInvert;
				log.debug(obj3d[i].NodeName + " Invert:");
				log.debug(TmInvert._11 + ", " + TmInvert._12 + ", " + TmInvert._13 + ", " + TmInvert._14);
				log.debug(TmInvert._21 + ", " + TmInvert._22 + ", " + TmInvert._23 + ", " + TmInvert._24);
				log.debug(TmInvert._31 + ", " + TmInvert._32 + ", " + TmInvert._33 + ", " + TmInvert._34);
				log.debug(TmInvert._41 + ", " + TmInvert._42 + ", " + TmInvert._43 + ", " + TmInvert._44);
		
			}
			*/
			
			HashMap<String, Bone> boneMap = new HashMap<String, Bone>();
			Bone[] bones = new Bone[nObj3d];
			for (int i = 0; i < nObj3d; i++) {
				byte n = TmSort[i];
				OBJ3D obj = obj3d[n];

				Bone bone = new Bone(obj.NodeName);
				boneMap.put(obj.NodeName, bone);
				bone.setBindTransforms(obj.TmPos[0].vec3, obj.TmRot[0].quad, obj.TmScale[0].scale);
				bones[i] = bone;

				// I AM YOUR FATHER!!!
				if (obj.NodeParent != null) {
					Bone parent = boneMap.get(obj.NodeParent);
					if (parent != null)
						parent.addChild(bone);
				}

			}

			Skeleton ske = new Skeleton(bones);

			AnimControl ac = new AnimControl(ske);

			SkeletonControl sc = new SkeletonControl(ske);
		}
		
		/**
		 * ��˳���ȡ��3��int����TmInvert����ת�ã����һ��GL����ϵ�Ķ��㡣
		 * @param res1
		 * @param res2
		 * @param res3
		 * @param tm
		 */
		Vector3f mult(long res1, long res2, long res3, MATRIX tm ) {
			// reverting..
			long v1 = -(
					(res2 * tm._33 * tm._21 - res2 * tm._23 * tm._31 - res1 * tm._33 * tm._22
					+ res1 * tm._23 * tm._32 - res3 * tm._21 * tm._32 + res3 * tm._31 * tm._22
					+ tm._43 * tm._21 * tm._32 - tm._43 * tm._31 * tm._22 - tm._33 * tm._21 * tm._42
					+ tm._33 * tm._41 * tm._22 + tm._23 * tm._31 * tm._42 - tm._23 * tm._41 * tm._32) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12 + tm._21 * tm._32 * tm._13
					- tm._33 * tm._21 * tm._12 - tm._11 * tm._23 * tm._32 - tm._31 * tm._22 * tm._13);
			long v2 = (
					(res2 * tm._11 * tm._33 - res1 * tm._33 * tm._12 - res3 * tm._11 * tm._32
					+ res3 * tm._31 * tm._12 - res2 * tm._31 * tm._13 + res1 * tm._32 * tm._13
					+ tm._11 * tm._43 * tm._32 - tm._43 * tm._31 * tm._12 - tm._11 * tm._33 * tm._42
					+ tm._33 * tm._41 * tm._12 + tm._31 * tm._42 * tm._13 - tm._41 * tm._32 * tm._13) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12 + tm._21 * tm._32 * tm._13
					- tm._33 * tm._21 * tm._12 - tm._11 * tm._23 * tm._32 - tm._31 * tm._22 * tm._13);
			long v3 = -(
					(res2 * tm._11 * tm._23 - res1 * tm._23 * tm._12 - res3 * tm._11 * tm._22
					+ res3 * tm._21 * tm._12 - res2 * tm._21 * tm._13 + res1 * tm._22 * tm._13
					+ tm._11 * tm._43 * tm._22 - tm._43 * tm._21 * tm._12 - tm._11 * tm._23 * tm._42
					+ tm._23 * tm._41 * tm._12 + tm._21 * tm._42 * tm._13 - tm._41 * tm._22 * tm._13) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12 + tm._21 * tm._32 * tm._13
					- tm._33 * tm._21 * tm._12 - tm._11 * tm._23 * tm._32 - tm._31 * tm._22 * tm._13);

			float x = (float) v1 / 256.0f;
			float y = (float) v2 / 256.0f;
			float z = (float) v3 / 256.0f;
			return new Vector3f(x, y, z);
		}
		Node buildPAT3D() {
			Node rootNode = new Node("STAGEOBJ:" + key.getName());
			
			for(int i=0; i<nObj3d; i++) {
				if (obj3d[i].nFace > 0) {
					Geometry geom = obj3d[i].buildOBJ3D();
					// ��������
					int mat_id = obj3d[i].Face[0].v[3];
					MATERIAL m = smMaterialGroup.materials[mat_id];
					Material mat = createLightMaterial(m);
					geom.setMaterial(mat);
					rootNode.attachChild(geom);
				}
			}
			
			
			// ���ɹ���
			if (BipPattern != null) {
				BipPattern.buildSkeleton();
			}
			
			return rootNode;
		}
		
		
	}
	
	public AssetManager manager = null;
	public AssetKey<?> key = null;
	
	public Material defaultMaterial;
	
	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		key = assetInfo.getKey();
		manager = assetInfo.getManager();
		
		// ȷ���û�ʹ����SmdKey
		if (!(key instanceof SmdKey)) {
			log.error("�û�δʹ��SmdKey������ģ��:" + key.getName());
			throw new RuntimeException("��ʹ��SmdKey�����ؾ����smdģ�͡�");
		}
		
		/**
		 * ����������ʼ��Ϊnull
		 */
		BipPattern = null;
		
		/**
		 * ���û�ʹ����SmdKey���͸���type�������������ַ�ʽ������ģ�͡�
		 */
		SmdKey smdkey = (SmdKey) key;
		SMDTYPE type = smdkey.type;
		switch (type) {
		case STAGE3D:{// ����ͼ
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			STAGE3D stage3D = new STAGE3D();
			stage3D.loadFile();
			return stage3D.buildStage3D();
		}
		case STAGE_OBJ_BIP: {// ��̨���壬�ж���
			// ���ļ�����׺��Ϊsmb
			String smb = key.getName();
			smb = smb.replaceAll("smd", "smb");
			BipPattern = (PAT3D)manager.loadAsset(new SmdKey(smb, SMDTYPE.BONE));
		}
		case STAGE_OBJ:{// ��̨���壬�޶���
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			PAT3D pat = new PAT3D();
			pat.loadFile(null);
			return pat.buildPAT3D();
		}
		case PAT3D:
			return null;
		case MODEL:
			return null;
		case BONE: {
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			PAT3D bone = new PAT3D();
			bone.loadFile(null);
			return bone;
		}
		default:
			return null;
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
		
		// TODO ��texture.Name���뻺���У������μ��ء�
		
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
		mat.setColor("Diffuse", new ColorRGBA(m.Diffuse.r, m.Diffuse.g, m.Diffuse.b, 1));
		//mat.setBoolean("UseMaterialColors", true);
		
		RenderState rs = mat.getAdditionalRenderState();
		
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
	 * AminTexCounter����0˵�����ֲ�����������һ��Control����ʱ���»��档
	 * @param m
	 * @return
	 */
	private FrameAnimControl createFrameAnimControl(MATERIAL m) {
		FrameAnimControl control = new FrameAnimControl(m.AnimTexCounter, m.Shift_FrameSpeed);
		for(int i=0; i<m.AnimTexCounter; i++) {
			Texture tex = createTexture(m.smAnimTexture[i].Name);
			control.textures[i] = tex;
		}
		return control;
	}
	
	/**
	 * ֡����������
	 * ����Ĳ��ֶ�����ͨ��ͼƬ�ֲ���ʵ�ֵġ�
	 * @author yanmaoyuan
	 *
	 */
	class FrameAnimControl extends AbstractControl {
		/**
		 * ������������
		 */
		private Texture[] textures;
		/**
		 * ����֡��
		 */
		private final int numTex;
		/**
		 * ֡�л���ʱ��������λΪ�롣
		 */
		private final float internal;
		
		/**
		 * 
		 * @param numTex
		 * @param shiftFrameSpeed ֡���л��ٶȣ�Ϊ2��n�η�����λ�Ǻ��롣
		 */
		public FrameAnimControl(int numTex, int shiftFrameSpeed) {
			this.numTex = numTex;
			this.textures = new Texture[numTex];
			this.internal = (1 << shiftFrameSpeed) / 1000f;
		}
		
		private float time = 0;
		private int index = 0;
		
		@Override
		protected void controlUpdate(float tpf) {
			time += tpf;
			if (time > internal) {
				time -= internal;

				// �л�ͼƬ
				index++;
				if (index == numTex) {
					index = 0;
				}
				
				Material mat = ((Geometry) spatial).getMaterial();
				mat.setTexture("DiffuseMap", textures[index]);
			}
		}
		
		@Override
		protected void controlRender(RenderManager rm, ViewPort vp) {}

	}

}
