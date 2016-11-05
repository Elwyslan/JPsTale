package org.pstale.asset.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.pstale.asset.control.FrameAnimControl;
import org.pstale.asset.control.WaterAnimationControl;
import org.pstale.asset.control.WindAnimationControl;
import org.pstale.asset.loader.SmdKey.SMDTYPE;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
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
import com.jme3.util.TempVars;

/**
 * ���鳡��������
 * 
 * @author yanmaoyuan
 * 
 */
public class SmdLoader extends ByteReader implements AssetLoader {

	static Logger log = Logger.getLogger(SmdLoader.class);

	// �Ƿ�ʹ��OPENGL����ϵ
	boolean OPEN_GL_AXIS = true;
	// �Ƿ��ӡ������־
	boolean LOG_ANIMATION = false;

	/**
	 * ��������ɫҪ�ȱ��ģ����һ��㡣
	 */
	AmbientLight ambientLightForAnimation;

	public SmdLoader() {
		ambientLightForAnimation = new AmbientLight();
		ambientLightForAnimation.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1f));
	}

	/**
	 * ����Ķ���ʹ��3DS MAX��Ĭ�����ʣ�ÿ��30Tick��ÿTick��160֡�� Ҳ����ÿ��4800֡��
	 * 
	 * ����smd�ļ���Ҳ����洢��2�������� (1) ÿ��Tick�� (Ĭ��30) (2) ÿTick֡�� (Ĭ��160)
	 * ������������ֵ�����˶������ŵ����ʡ�
	 */
	private float framePerSecond = 4800f;

	private final static int OBJ_FRAME_SEARCH_MAX = 32;

	private FILE_HEADER smd_file_header;

	final static int sMATS_SCRIPT_WIND = 1;
	final static int sMATS_SCRIPT_WINDZ1 = 0x0020;
	final static int sMATS_SCRIPT_WINDZ2 = 0x0040;
	final static int sMATS_SCRIPT_WINDX1 = 0x0080;
	final static int sMATS_SCRIPT_WINDX2 = 0x0100;
	final static int sMATS_SCRIPT_WATER = 0x0200;
	final static int sMATS_SCRIPT_NOTVIEW = 0x0400;
	final static int sMATS_SCRIPT_PASS = 0x0800;
	final static int sMATS_SCRIPT_NOTPASS = 0x1000;
	final static int sMATS_SCRIPT_RENDLATTER = 0x2000;
	final static int sMATS_SCRIPT_BLINK_COLOR = 0x4000;
	final static int sMATS_SCRIPT_CHECK_ICE = 0x8000;
	final static int sMATS_SCRIPT_ORG_WATER = 0x10000;

	/**
	 * size = 64
	 */
	class FMATRIX {
		float _11, _12, _13, _14;
		float _21, _22, _23, _24;
		float _31, _32, _33, _34;
		float _41, _42, _43, _44;

		FMATRIX() {
			_11 = 1;
			_12 = 0;
			_13 = 0;
			_14 = 0;
			_21 = 0;
			_22 = 1;
			_23 = 0;
			_24 = 0;
			_31 = 0;
			_32 = 0;
			_33 = 1;
			_34 = 0;
			_41 = 0;
			_42 = 0;
			_43 = 0;
			_44 = 1;
		}

		FMATRIX(boolean init) {
			_11 = getFloat();
			_12 = getFloat();
			_13 = getFloat();
			_14 = getFloat();
			_21 = getFloat();
			_22 = getFloat();
			_23 = getFloat();
			_24 = getFloat();
			_31 = getFloat();
			_32 = getFloat();
			_33 = getFloat();
			_34 = getFloat();
			_41 = getFloat();
			_42 = getFloat();
			_43 = getFloat();
			_44 = getFloat();
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
			_11 = 1;
			_12 = 0;
			_13 = 0;
			_14 = 0;
			_21 = 0;
			_22 = 1;
			_23 = 0;
			_24 = 0;
			_31 = 0;
			_32 = 0;
			_33 = 1;
			_34 = 0;
			_41 = 0;
			_42 = 0;
			_43 = 0;
			_44 = 1;
		}

		/**
		 * ������������ֵ��256������Ԫ�س���256�������ʽ��1��
		 * 
		 * @param init
		 */
		MATRIX(boolean init) {
			_11 = getInt();
			_12 = getInt();
			_13 = getInt();
			_14 = getInt();
			_21 = getInt();
			_22 = getInt();
			_23 = getInt();
			_24 = getInt();
			_31 = getInt();
			_32 = getInt();
			_33 = getInt();
			_34 = getInt();
			_41 = getInt();
			_42 = getInt();
			_43 = getInt();
			_44 = getInt();
		}
	}

	/**
	 * size = 20
	 * 
	 */
	class TM_ROT {
		int frame;
		float x, y, z, w;

		TM_ROT() {
			frame = getInt();
			x = getFloat();
			y = getFloat();
			z = getFloat();
			w = getFloat();
		}
	}

	/**
	 * size = 16
	 */
	class TM_POS {
		int frame;
		float x, y, z;

		TM_POS() {
			frame = getInt();
			x = getFloat();
			y = getFloat();
			z = getFloat();
		}
	}

	/**
	 * size = 16
	 */
	class TM_SCALE {
		int frame;
		float x, y, z;

		TM_SCALE() {
			frame = getInt();
			x = getPTDouble();
			y = getPTDouble();
			z = getPTDouble();
		}
	}

	class Keyframe {
		Vector3f translation;
		Quaternion rotation;
		Vector3f scale;
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
	 * SMD�ļ�ͷ size = 556;
	 */
	class FILE_HEADER {
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
	 * ���ļ�ͷ�е�mat>0��˵���в��ʡ� ��������������Ӧ����һ��������smMATERIAL_GROUP���� size = 88��
	 */
	class MATERIAL_GROUP {
		// DWORD Head
		MATERIAL[] materials;
		int materialCount;
		int reformTexture;
		int maxMaterial;
		int lastSearchMaterial;
		String lastSearchName;

		// //////////////
		// �����ȡ����������MaterialGroupռ���˶����ڴ棬û��ʵ�����塣
		// int size = 0;
		// //////////////
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

			// size += 88;

			assert buffer.position() - start == 88;
		}

		/**
		 * �������в���
		 */
		void loadFile() {
			materials = new MATERIAL[materialCount];

			for (int i = 0; i < materialCount; i++) {
				materials[i] = new MATERIAL();
				// size += 320;

				if (materials[i].InUse != 0) {
					getInt();// int strLen; ���������¼�˺������в���������ռ���ֽ�����
					// size += 4;
					// size += strLen;

					materials[i].smTexture = new TEXTURE[materials[i].TextureCounter];
					for (int j = 0; j < materials[i].TextureCounter; j++) {
						TEXTURE texture = new TEXTURE();
						materials[i].smTexture[j] = texture;
						texture.Name = getString();
						texture.NameA = getString();

						if (texture.NameA.length() > 1) {
							// TODO ����֪��NameA�������Tex�к���
						}
					}

					materials[i].smAnimTexture = new TEXTURE[materials[i].AnimTexCounter];
					for (int j = 0; j < materials[i].AnimTexCounter; j++) {
						TEXTURE texture = new TEXTURE();
						materials[i].smAnimTexture[j] = texture;
						texture.Name = getString();
						texture.NameA = getString();
					}
				}
			}
		}
	}

	/**
	 * ���� size = 320
	 * 
	 * @author yanmaoyuan
	 * 
	 */
	class MATERIAL {
		/**
		 * �ж�������Ƿ�ʹ�á� ʵ����smd�ļ��д洢�Ĳ��ʶ��Ǳ��õ��Ĳ��ʣ������ǲ���洢�ġ� ����ж����������û��ʵ�����塣
		 */
		int InUse;
		/**
		 * �����������
		 */
		int TextureCounter;
		/**
		 * ����ͼƬ�����ơ� ����STAGE3D��˵����1��������DiffuseMap����2������Ӧ����LightMap��
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
		 * �������ͣ���ɫ�򶯻�
		 * 
		 * <pre>
		 * #define SMTEX_TYPE_MULTIMIX		0x0000
		 * #define SMTEX_TYPE_ANIMATION		0x0001
		 * </pre>
		 */
		int TextureType;

		/**
		 * ��ɫ��ʽ
		 * 
		 * <pre>
		 * #define SMMAT_BLEND_NONE		0x00
		 * #define SMMAT_BLEND_ALPHA		0x01
		 * #define SMMAT_BLEND_COLOR		0x02
		 * #define SMMAT_BLEND_SHADOW		0x03
		 * #define SMMAT_BLEND_LAMP		0x04
		 * #define SMMAT_BLEND_ADDCOLOR	0x05
		 * #define SMMAT_BLEND_INVSHADOW	0x06
		 * </pre>
		 */
		int BlendType;// SMMAT_BLEND_XXXX

		/**
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
		 * ����ASEģ���е�ScriptState sMATS_SCRIPT_WIND sMATS_SCRIPT_WINDX1
		 * sMATS_SCRIPT_WINDX2 sMATS_SCRIPT_WINDZ1 sMATS_SCRIPT_WINDZ2
		 * sMATS_SCRIPT_WATER sMATS_SCRIPT_NOTPASS // ��ײ�����ǲ��ɼ� sMATS_SCRIPT_PASS
		 * // ���Դ���
		 * 
		 * sMATS_SCRIPT_RENDLATTER -> MeshState |= sMATS_SCRIPT_RENDLATTER;
		 * sMATS_SCRIPT_CHECK_ICE -> MeshState |= sMATS_SCRIPT_CHECK_ICE;
		 * sMATS_SCRIPT_ORG_WATER -> MeshState = sMATS_SCRIPT_ORG_WATER;
		 */
		int UseState;
		/**
		 * �Ƿ������ײ���
		 * 
		 * <pre>
		 * #define SMMAT_STAT_CHECK_FACE	0x00000001
		 * </pre>
		 */
		int MeshState;

		int WindMeshBottom;

		/**
		 * ��֡�����������ļ���
		 */
		TEXTURE[] smAnimTexture = new TEXTURE[32];
		/**
		 * �����м���ͼNumTex
		 */
		int AnimTexCounter;
		int FrameMask;// == AnimTexCounter - 1
		/**
		 * �����л��ٶȡ�
		 */
		int Shift_FrameSpeed;
		/**
		 * SMTEX_AUTOANIMATION = 0x0100
		 */
		int AnimationFrame;

		/**
		 * ��ȡMATERIAL���ݽṹ
		 */
		MATERIAL() {
			int start = buffer.position();

			InUse = getInt(); // > 0 ��ʾ��ʹ��
			TextureCounter = getInt();// ��������������������Ȼֻ��1�š�
			for (int i = 0; i < 8; i++) {
				getInt();// *smTexture[8];
			}
			for (int i = 0; i < 8; i++) {
				TextureStageState[i] = getInt();
			}
			for (int i = 0; i < 8; i++) {
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

			WindMeshBottom = getInt();

			// ���ϸ��̼� �ؽ��� �Ӽ�
			for (int i = 0; i < 32; i++) {
				getInt();// *smAnimTexture[32]
			}
			AnimTexCounter = getInt();
			FrameMask = getInt(); // NumTex-1
			Shift_FrameSpeed = getInt();

			/**
			 * �Ƿ��Զ����Ŷ��� #define SMTEX_AUTOANIMATION 0x100 Ϊ0ʱ���Զ�����
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
	 * MaterialGroup��ʹ�����������¼Diffuse size = 12��
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
		float u, v;

		FTPOINT() {
			u = getFloat();
			v = getFloat();
		}
	}

	// size = 12
	class POINT3D {
		int x, y, z;

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
		long x, y, z;
		Vector3f v;// ����
		Vector3f n;// normals ������

		VERTEX() {
			x = getInt();
			y = getInt();
			z = getInt();

			v = new Vector3f(x / 256f, y / 256f, z / 256f);
			n = getPTPoint3f();
		}
	}

	/**
	 * size = 36
	 */
	class FACE {
		int[] v = new int[4];// a,b,c,Matrial
		FTPOINT[] t = new FTPOINT[3];
		int lpTexLink;
		TEXLINK TexLink;

		FACE() {
			for (int i = 0; i < 4; i++) {
				v[i] = getUnsignedShort();
			}

			for (int i = 0; i < 3; i++) {
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
		// smRENDVERTEX *lpRendVertex;
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
		int v[] = new int[4];// a, b, c, mat_id;
		int lpTexLink;// ����һ��ָ�룬ָ��TEXLINK�ṹ��
		TEXLINK TexLink;// ��lpTexLink != 0����TexLinkָ��һ��ʵ�ʵĶ�����

		float nx, ny, nz, y;// Cross����( Normal ) ( nx , ny , nz , [0,1,0]���� Y );

		STAGE_FACE() {
			sum = getInt();
			CalcSum = getInt();

			for (int i = 0; i < 4; i++) {
				v[i] = getUnsignedShort();
			}

			lpTexLink = getInt();// ���������ָ�롣smTEX_LINK *lpTexLink

			nx = getShort() / 32767f;// nx
			ny = getShort() / 32767f;// ny
			nz = getShort() / 32767f;// nz
			y = getShort() / 32767f;// Y ����32767���� 1/8PI����֪���к��á�
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
		/**
		 * <pre>
		 * #define	smLIGHT_TYPE_NIGHT		0x00001
		 * #define	smLIGHT_TYPE_LENS		0x00002
		 * #define	smLIGHT_TYPE_PULSE2	0x00004
		 * #define	SMLIGHT_TYPE_OBJ		0x00008
		 * #define	smLIGHT_TYPE_DYNAMIC	0x80000
		 * </pre>
		 */
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
	 * Stage3D��������� �ļ����ݵĵڶ��Σ��洢��һ��������smSTAGE3D���� size = 262260
	 * ���еĹؼ�������nVertex/nFace/nTexLink/nLight��Щ��
	 */
	class STAGE3D {
		// DWORD Head; ���õ�ͷ�ļ�ָ�룬4�ֽ�
		int[][] StageArea;// WORD *StageArea[MAP_SIZE][MAP_SIZE];256 *
							// 256��ָ�룬��262144�ֽ�
		Vector3f[] AreaList;// POINT *AreaList; һ��ָ�룬������һ������
		int AreaListCnt;

		int MemMode;

		int SumCount;
		int CalcSumCount;

		STAGE_VERTEX[] Vertex;
		STAGE_FACE[] Face;
		TEXLINK[] TexLink;
		LIGHT3D[] Light;
		MATERIAL_GROUP materialGroup;// sizeof(smMaterialGroup) = 88
		// smSTAGE_OBJECT *StageObject;
		MATERIAL[] materials;

		int nVertex = 0;// offset = 88 + = 262752
		int nFace = 0;
		int nTexLink = 0;// UvVertexNum
		int nLight = 0;
		int nVertColor = 0;

		int Contrast = 0;
		int Bright = 0;

		Vector3f vectLight;

		// WORD *lpwAreaBuff;
		int wAreaSize;
		// RECT StageMapRect;// top bottom right left 4������

		// ////////////////
		// �������������¼TexLink���ļ��еĵ�ַ
		int lpOldTexLink;

		// ////////////////

		/**
		 * ��ʼ��Stage3D����
		 */
		protected STAGE3D() {
			int start = buffer.position();

			// Head = FALSE;
			getInt();// Head
			buffer.get(new byte[262144]);// *StageArea[MAP_SIZE][MAP_SIZE]; 4 *
											// 256 * 256 = 262144
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
			log.debug(String.format("min(%d, %d) max(%d, %d)", minX, minY,
					maxX, maxY));

			assert buffer.position() - start == 262260;
		}

		/**
		 * ������̨����
		 * 
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
			for (int i = 0; i < nVertex; i++) {
				Vertex[i] = new STAGE_VERTEX();
			}

			// ��ȡFace
			Face = new STAGE_FACE[nFace];
			for (int i = 0; i < nFace; i++) {
				Face[i] = new STAGE_FACE();
			}

			// ��ȡTEX_LINK(��ʵ����uv����)
			TexLink = new TEXLINK[nTexLink];
			for (int i = 0; i < nTexLink; i++) {
				TexLink[i] = new TEXLINK();
			}

			// ��ȡ�ƹ�
			if (nLight > 0) {
				Light = new LIGHT3D[nLight];
				for (int i = 0; i < nLight; i++) {
					Light[i] = new LIGHT3D();
				}
			}

			// ���½���Face��TexLink֮��Ĺ���
			relinkFaceAndTex();
		}

		/**
		 * ���½���TexLink֮�䡢Face��TexLink֮��Ĺ�����
		 * 
		 * TexLink��һ��smTEXLINK���飬˳��洢��lpOldTexLink��¼�����׵�ַ�� ����
		 * {@code sizeof(smTEXLINK) = 32}�����ԣ�{@code ������=(ԭ��ַ-lpOldTexLink)/32}
		 */
		void relinkFaceAndTex() {
			// ���½���TexLink�����еĹ���
			for (int i = 0; i < nTexLink; i++) {
				if (TexLink[i].lpNextTex != 0) {
					int index = (TexLink[i].lpNextTex - lpOldTexLink) / 32;
					TexLink[i].NextTex = TexLink[index];
				}
			}

			// ���½���Face��TexLink֮��Ĺ���
			for (int i = 0; i < nFace; i++) {
				if (Face[i].lpTexLink != 0) {
					int index = (Face[i].lpTexLink - lpOldTexLink) / 32;
					Face[i].TexLink = TexLink[index];
				}
			}
		}

		/**
		 * ������ײ���� ��͸���ġ���������ײ������ͳͳ�ü�����ֻ����������ײ�����档
		 * 
		 * @return
		 */
		Mesh buildSolidMesh() {
			Mesh mesh = new Mesh();

			int materialCount = materialGroup.materialCount;
			/**
			 * ���ݲ��ʵ�������ɸѡ�μ���ײ�������壬 �������ԵĲ������ó�null����Ϊһ�ֱ�ǡ�
			 */
			MATERIAL m;// ��ʱ����
			for (int mat_id = 0; mat_id < materialCount; mat_id++) {
				m = materials[mat_id];

				if (m.MeshState == 1 && m.Transparency < 0.2f) {
					continue;
				}

				if ((m.UseState & sMATS_SCRIPT_NOTPASS) != 0) {
					// ��Щ��Ҫ�μ���ײ���
					continue;
				}

				if ((m.UseState & 0x07FF) != 0) {
					// ��Щ�汻����Ϊ����ֱ�Ӵ�͸
					materials[mat_id] = null;
					continue;
				}
				
				if ( m.BlendType == 1) {// ALPHA��ɫ
					materials[mat_id] = null;
					continue;
				}

				if (m.MapOpacity != 0 || m.Transparency != 0f) {
					// ͸�����治�μ���ײ���
					materials[mat_id] = null;
					continue;
				}

				if (m.TextureType == 1) {
					// ֡����Ҳ��������ײ��⡣������桢����Ĺ�㡣
					materials[mat_id] = null;
					continue;
				}

			}

			/**
			 * ͳ���ж��ٸ�Ҫ�μ���ײ�����档
			 */
			int loc[] = new int[nVertex];
			for (int i = 0; i < nVertex; i++) {
				loc[i] = -1;
			}

			int fSize = 0;
			for (int i = 0; i < nFace; i++) {
				STAGE_FACE face = Face[i];
				if (materials[face.v[3]] != null) {
					loc[face.v[0]] = face.v[0];
					loc[face.v[1]] = face.v[1];
					loc[face.v[2]] = face.v[2];

					fSize++;
				}
			}

			int vSize = 0;
			for (int i = 0; i < nVertex; i++) {
				if (loc[i] > -1) {
					vSize++;
				}
			}

			// ��¼�µĶ�����
			Vector3f[] v = new Vector3f[vSize];
			vSize = 0;
			for (int i = 0; i < nVertex; i++) {
				if (loc[i] > -1) {
					v[vSize] = Vertex[i].v;
					loc[i] = vSize;
					vSize++;
				}
			}

			// ��¼�µĶ���������
			int[] f = new int[fSize * 3];
			fSize = 0;
			for (int i = 0; i < nFace; i++) {
				STAGE_FACE face = Face[i];
				if (materials[face.v[3]] != null) {
					f[fSize * 3] = loc[face.v[0]];
					f[fSize * 3 + 1] = loc[face.v[1]];
					f[fSize * 3 + 2] = loc[face.v[2]];
					fSize++;
				}
			}

			mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(v));
			mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(f));

			mesh.updateBound();
			mesh.setStatic();

			log.debug("������:" + nFace + " ��ײ����:" + fSize);
			log.debug("�ܵ���:" + nVertex + " ��ײ����:" + vSize);
			return mesh;
		}

		/**
		 * ����STAGE3D����
		 * 
		 * @return
		 */
		Node buildNode() {
			Node rootNode = new Node("STAGE3D:" + key.getName());

			// Ϊ���ñ���ƽ�����Ȼ���ԭ������Ͷ������һ�η�������
			Vector3f[] orginNormal = computeOrginNormals();

			int materialCount = materialGroup.materialCount;

			// ��������
			for (int mat_id = 0; mat_id < materialCount; mat_id++) {
				MATERIAL m = materials[mat_id];

				// �ò���û��ʹ�ã�����Ҫ��ʾ��
				if (m.InUse == 0) {
					continue;
				}
				// û����������Ҫ��ʾ��
				if (m.TextureCounter == 0 && m.AnimTexCounter == 0) {
					continue;
				}
				// ���ɼ��Ĳ��ʣ�����Ҫ��ʾ��
				if ((m.UseState & sMATS_SCRIPT_NOTVIEW) != 0) {
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
				// û����ʹ��������ʣ�������
				if (size < 1) {
					continue;
				}

				// ��������
				Mesh mesh = buildMesh(size, mat_id, orginNormal);
				Geometry geom = new Geometry(key.getName() + "#" + mat_id, mesh);

				// ��������
				Material mat;
				if (m.TextureType == 0) {
					// SMTEX_TYPE_MULTIMIX
					mat = createLightMaterial(materials[mat_id]);
				} else {
					// SMTEX_TYPE_ANIMATION
					mat = createMiscMaterial(materials[mat_id]);
				}
				setRenderState(m, mat);

				// Ӧ�ò���
				geom.setMaterial(mat);

				// �ж������
				if (m.AnimTexCounter > 0) {
					FrameAnimControl control = createFrameAnimControl(materials[mat_id]);
					geom.addControl(control);
				}
				
				// Ӧ�ö���
				if (m.WindMeshBottom != 0 && (m.UseState & sMATS_SCRIPT_BLINK_COLOR) == 0) {
					switch (m.WindMeshBottom & 0x07FF) {
					case sMATS_SCRIPT_WINDX1:
					case sMATS_SCRIPT_WINDX2:
					case sMATS_SCRIPT_WINDZ1:
					case sMATS_SCRIPT_WINDZ2:{
						geom.addControl(new WindAnimationControl(m.WindMeshBottom & 0x07FF));
						break;
					}
					case sMATS_SCRIPT_WATER:{
						// ˮ�治�ܶ���һ����ͼ�����ˡ���
						//geom.addControl(new WaterAnimationControl());
						break;
					}
					}
				}

				rootNode.attachChild(geom);

				// ͸����
				// ֻ�в�͸���������Ҫ�����ײ����
				if (m.MapOpacity != 0 || m.Transparency != 0 || m.BlendType == 1) {
					geom.setQueueBucket(Bucket.Translucent);
				}

				if (m.ReformTexture > 0) {
					log.debug("ReformTexture=" + m.ReformTexture);// ��Ҫ�����ܵ�ͼƬ��Ŀ
				}
				if (m.SelfIllum > 0.0f) {
					log.debug("SelfIllum=" + m.SelfIllum);// �Է���
				}

				if (m.UseState != 0) {// ScriptState
					if ((m.UseState & sMATS_SCRIPT_RENDLATTER) != 0) {
						// MeshState |= sMATS_SCRIPT_RENDLATTER;
					}
					if ((m.UseState & sMATS_SCRIPT_CHECK_ICE) != 0) {
						// MeshState |= sMATS_SCRIPT_CHECK_ICE;
					}
					if ((m.UseState & sMATS_SCRIPT_ORG_WATER) != 0) {
						// MeshState = sMATS_SCRIPT_ORG_WATER;
					}
					if ((m.UseState & sMATS_SCRIPT_BLINK_COLOR) != 0) {
						// m.WindMeshBottom == dwBlinkCode[]{ 9, 10, 11, 12, 13,
						// 14, 15, 16,} 8����ֵ������֮һ
					}
				}

			}

			if (nLight > 0) {
				// TODO ����ƹ�
			}

			return rootNode;
		}

		/**
		 * ����ԭ�е��棬����ÿ������ķ�������
		 * 
		 * @return
		 */
		Vector3f[] computeOrginNormals() {
			TempVars tmp = TempVars.get();

			Vector3f A;// �����εĵ�1����
			Vector3f B;// �����εĵ�2����
			Vector3f C;// �����εĵ�3����

			Vector3f vAB = tmp.vect1;
			Vector3f vAC = tmp.vect2;
			Vector3f n = tmp.vect4;

			// Here we allocate all the memory we need to calculate the normals
			Vector3f[] tempNormals = new Vector3f[nFace];
			Vector3f[] normals = new Vector3f[nVertex];

			for (int i = 0; i < nFace; i++) {
				A = Vertex[Face[i].v[0]].v;
				B = Vertex[Face[i].v[1]].v;
				C = Vertex[Face[i].v[2]].v;

				vAB = B.subtract(A, vAB);
				vAC = C.subtract(A, vAC);
				n = vAB.cross(vAC, n);

				tempNormals[i] = n.normalize();
			}

			Vector3f sum = tmp.vect4;
			int shared = 0;

			for (int i = 0; i < nVertex; i++) {
				// ͳ��ÿ���㱻��Щ�湲�á�
				for (int j = 0; j < nFace; j++) {
					if (Face[j].v[0] == i || Face[j].v[1] == i
							|| Face[j].v[2] == i) {
						sum.addLocal(tempNormals[j]);
						shared++;
					}
				}

				// ���ֵ
				normals[i] = sum.divideLocal((shared)).normalize();

				sum.zero(); // Reset the sum
				shared = 0; // Reset the shared
			}

			tmp.release();
			return normals;
		}

		Mesh buildMesh(int size, int mat_id, Vector3f[] orginNormal) {

			Vector3f[] position = new Vector3f[size * 3];
			int[] f = new int[size * 3];
			Vector3f[] normal = new Vector3f[size * 3];
			Vector2f[] uv1 = new Vector2f[size * 3];
			Vector2f[] uv2 = new Vector2f[size * 3];

			int index = 0;
			// Prepare MeshData
			for (int i = 0; i < nFace; i++) {
				// Check the MaterialIndex
				if (Face[i].v[3] != mat_id)
					continue;

				// ˳����3������
				for (int vIndex = 0; vIndex < 3; vIndex++) {
					// ���� VERTEX
					position[index * 3 + vIndex] = Vertex[Face[i].v[vIndex]].v;
					// ������ Normal
					normal[index * 3 + vIndex] = orginNormal[Face[i].v[vIndex]];

					// �� FACE
					f[index * 3 + vIndex] = index * 3 + vIndex;

					// ����ӳ��
					TEXLINK tl = Face[i].TexLink;
					if (tl != null) {
						// ��1��uv����
						uv1[index * 3 + vIndex] = new Vector2f(tl.u[vIndex],
								1f - tl.v[vIndex]);
					} else {
						uv1[index * 3 + vIndex] = new Vector2f();
					}

					// ��2��uv����
					if (tl != null && tl.NextTex != null) {
						tl = tl.NextTex;
						uv2[index * 3 + vIndex] = new Vector2f(tl.u[vIndex],
								1f - tl.v[vIndex]);
					} else {
						uv2[index * 3 + vIndex] = new Vector2f();
					}
				}

				index++;
			}

			Mesh mesh = new Mesh();
			mesh.setBuffer(Type.Position, 3,
					BufferUtils.createFloatBuffer(position));
			mesh.setBuffer(Type.Index, 3, f);
			// DiffuseMap UV
			mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv1));
			// LightMap UV
			mesh.setBuffer(Type.TexCoord2, 2,
					BufferUtils.createFloatBuffer(uv2));
			// ������
			mesh.setBuffer(Type.Normal, 3,
					BufferUtils.createFloatBuffer(normal));

			mesh.setStatic();
			mesh.updateBound();
			mesh.updateCounts();

			return mesh;
		}
	}

	class SMotionStEndInfo {
		int StartFrame;
		int EndFrame;
	}

	/**
	 * size = 2236
	 */
	class OBJ3D {
		// DWORD Head;
		VERTEX[] Vertex;// ����
		FACE[] Face;// ��
		TEXLINK[] TexLink;// ��������

		OBJ3D[] Physique; // ������Ĺ���

		VERTEX ZeroVertex; // ������Ʈ �߾� ���ؽ� ��

		int maxZ, minZ;
		int maxY, minY;
		int maxX, minX;

		int dBound; // �ٿ�� ����� �� ^2
		int Bound; // �ٿ�� ����� ��

		int MaxVertex;
		int MaxFace;

		int nVertex;
		int nFace;

		int nTexLink;

		int ColorEffect; // ����ȿ�� ��� ����
		int ClipStates; // Ŭ���� ����ũ ( �� Ŭ���κ� ��� ���� )

		POINT3D Posi;
		POINT3D CameraPosi;
		POINT3D Angle;
		int[] Trig = new int[8];

		// �ִϸ��̼� ����
		String NodeName;// [32]; // ������Ʈ�� ��� �̸�
		String NodeParent;// [32]; // �θ� ������Ʈ�� �̸�
		OBJ3D pParent; // �θ� ������Ʈ ������

		MATRIX Tm; // �⺻ TM ���
		MATRIX TmInvert; // �����
		FMATRIX TmResult; // �ִϸ��̼� ���
		MATRIX TmRotate; // �⺻�� ȸ�� ���

		MATRIX mWorld; // ������ǥ ��ȯ ���
		MATRIX mLocal; // ��Į��ǥ ��ȯ ���

		int lFrame;// û��ʵ������

		float qx, qy, qz, qw; // ȸ�� ���ʹϾ�
		float sx, sy, sz; // ������ ��ǥ
		float px, py, pz; // ������ ��ǥ

		TM_ROT[] TmRot; // �����Ӻ� ȸ�� �ִϸ��̼�
		TM_POS[] TmPos; // �����Ӻ� ������ �ִϸ��̼�
		TM_SCALE[] TmScale; // �����Ӻ� ������ �ִϸ��̼�

		FMATRIX[] TmPrevRot; // ֡�Ķ�������

		int TmRotCnt;
		int TmPosCnt;
		int TmScaleCnt;

		// TM ������ ��ġ ( �������� ������ ã�Ⱑ ���� )
		FRAME_POS[] TmRotFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		FRAME_POS[] TmPosFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		FRAME_POS[] TmScaleFrame = new FRAME_POS[OBJ_FRAME_SEARCH_MAX];
		int TmFrameCnt;// �Ƿ��ж��� TRUE or FALSE

		// //////////////////
		int lpPhysuque;
		int lpOldTexLink;

		// //////////////////

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
			Face = null;
			Vertex = null;
			TexLink = null;
			Physique = null;
		}

		void readOBJ3D() {
			int start = buffer.position();

			getInt();// Head `DCB\0`
			getInt();// smVERTEX *Vertex;
			getInt();// smFACE *Face;
			lpOldTexLink = getInt();// smTEXLINK *TexLink;
			lpPhysuque = getInt();// smOBJ3D **Physique;

			ZeroVertex = new VERTEX();

			maxZ = getInt();
			minZ = getInt();
			maxY = getInt();
			minY = getInt();
			maxX = getInt();
			minX = getInt();

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
			for (int i = 0; i < 8; i++) {
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

			qx = getFloat();
			qy = getFloat();
			qz = getFloat();
			qw = getFloat();
			sx = getPTDouble();
			sy = getPTDouble();
			sz = getPTDouble();
			px = getPTDouble();
			py = getPTDouble();
			pz = getPTDouble();

			getInt();// smTM_ROT *TmRot;
			getInt();// smTM_POS *TmPos;
			getInt();// smTM_SCALE *TmScale;
			getInt();// smFMATRIX *TmPrevRot;

			TmRotCnt = getInt();
			TmPosCnt = getInt();
			TmScaleCnt = getInt();

			for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
				TmRotFrame[i] = new FRAME_POS();
			}
			for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
				TmPosFrame[i] = new FRAME_POS();
			}
			for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
				TmScaleFrame[i] = new FRAME_POS();
			}
			TmFrameCnt = getInt();

			assert buffer.position() - start == 2236;
		}

		/**
		 * ��ȡOBJ3D�ļ�����
		 * 
		 * @param PatPhysique
		 */
		void loadFile(PAT3D PatPhysique) {
			// ��ȡOBJ3D���󣬹�2236�ֽ�
			readOBJ3D();

			Vertex = new VERTEX[nVertex];
			for (int i = 0; i < nVertex; i++) {
				Vertex[i] = new VERTEX();
			}

			Face = new FACE[nFace];
			for (int i = 0; i < nFace; i++) {
				Face[i] = new FACE();
			}

			TexLink = new TEXLINK[nTexLink];
			for (int i = 0; i < nTexLink; i++) {
				TexLink[i] = new TEXLINK();
			}

			TmRot = new TM_ROT[TmRotCnt];
			for (int i = 0; i < TmRotCnt; i++) {
				TmRot[i] = new TM_ROT();
			}

			TmPos = new TM_POS[TmPosCnt];
			for (int i = 0; i < TmPosCnt; i++) {
				TmPos[i] = new TM_POS();
			}

			TmScale = new TM_SCALE[TmScaleCnt];
			for (int i = 0; i < TmScaleCnt; i++) {
				TmScale[i] = new TM_SCALE();
			}

			TmPrevRot = new FMATRIX[TmRotCnt];
			for (int i = 0; i < TmRotCnt; i++) {
				TmPrevRot[i] = new FMATRIX(true);
			}

			relinkFaceAndTex();

			// �󶨶�������
			if (lpPhysuque != 0 && PatPhysique != null) {

				Physique = new OBJ3D[nVertex];

				String[] names = new String[nVertex];
				for (int i = 0; i < nVertex; i++) {
					names[i] = getString(32);
				}

				for (int i = 0; i < nVertex; i++) {
					Physique[i] = PatPhysique.getObjectFromName(names[i]);
				}

			}
		}

		void relinkFaceAndTex() {
			// ���½���TexLink�����еĹ���
			for (int i = 0; i < nTexLink; i++) {
				if (TexLink[i].lpNextTex != 0) {
					int index = (TexLink[i].lpNextTex - lpOldTexLink) / 32;
					TexLink[i].NextTex = TexLink[index];
				}
			}

			// ���½���Face��TexLink֮��Ĺ���
			for (int i = 0; i < nFace; i++) {
				if (Face[i].lpTexLink != 0) {
					int index = (Face[i].lpTexLink - lpOldTexLink) / 32;
					Face[i].TexLink = TexLink[index];
				}
			}
		}

		/**
		 * �����������ݡ�
		 * 
		 * @param ske
		 * @return
		 */
		Mesh buildMesh(int mat_id, Skeleton ske) {
			Mesh mesh = new Mesh();

			// ͳ��ʹ��������ʵ�����
			int count = 0;
			for (int i = 0; i < nFace; i++) {
				if (Face[i].v[3] == mat_id) {
					count++;
				}
			}

			// ��������
			Vector3f[] position = new Vector3f[count * 3];
			int[] f = new int[count * 3];
			Vector2f[] uv = new Vector2f[count * 3];
			int index = 0;

			// Prepare MeshData
			for (int i = 0; i < nFace; i++) {
				// ���Ե������
				if (Face[i].v[3] != mat_id) {
					continue;
				}

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
				if (tl != null) {
					// ��1��uv����
					uv[index * 3 + 0] = new Vector2f(tl.u[0], 1f - tl.v[0]);
					uv[index * 3 + 1] = new Vector2f(tl.u[1], 1f - tl.v[1]);
					uv[index * 3 + 2] = new Vector2f(tl.u[2], 1f - tl.v[2]);
				} else {
					uv[index * 3 + 0] = new Vector2f();
					uv[index * 3 + 1] = new Vector2f();
					uv[index * 3 + 2] = new Vector2f();
				}

				index++;
			}

			mesh.setBuffer(Type.Position, 3,
					BufferUtils.createFloatBuffer(position));
			mesh.setBuffer(Type.Index, 3, f);
			mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));

			// ������Ƥ
			if (Physique != null && ske != null) {
				float[] boneIndex = new float[count * 12];
				float[] boneWeight = new float[count * 12];

				index = 0;
				for (int i = 0; i < nFace; i++) {
					// ���������
					if (Face[i].v[3] != mat_id) {
						continue;
					}

					for (int j = 0; j < 3; j++) {
						int v = Face[i].v[j];// �������
						int bi = index * 3 + j;// ��Ӧ���������

						OBJ3D obj3d = Physique[v];
						byte targetBoneIndex = (byte) ske
								.getBoneIndex(obj3d.NodeName);

						boneIndex[bi * 4] = targetBoneIndex;
						boneIndex[bi * 4 + 1] = 0;
						boneIndex[bi * 4 + 2] = 0;
						boneIndex[bi * 4 + 3] = 0;

						boneWeight[bi * 4] = 1;
						boneWeight[bi * 4 + 1] = 0;
						boneWeight[bi * 4 + 2] = 0;
						boneWeight[bi * 4 + 3] = 0;
					}

					index++;
				}

				mesh.setMaxNumWeights(1);
				// apply software skinning
				mesh.setBuffer(Type.BoneIndex, 4, boneIndex);
				mesh.setBuffer(Type.BoneWeight, 4, boneWeight);
				// apply hardware skinning
				mesh.setBuffer(Type.HWBoneIndex, 4, boneIndex);
				mesh.setBuffer(Type.HWBoneWeight, 4, boneWeight);

				mesh.generateBindPose(true);
			}

			mesh.setStatic();
			mesh.updateBound();
			mesh.updateCounts();

			return mesh;
		}

		/**
		 * ��˳���ȡ��3��int����TmInvert����ת�ã����һ��GL����ϵ�Ķ��㡣
		 * 
		 * <pre>
		 * ��������x=(v1, v2, v3, 1)�����TmInvert (_11, _12, _13, _14)
		 *                                      (_21, _22, _23, _24)
		 *                                      (_31, _32, _33, _34)
		 *                                      (_41, _42, _43, _44)��
		 * ʹ��TmInvert������x�������Ա任�󣬵õ�������Ϊa(res1, res2, res3, 1)��
		 *       ����TmInvert * x = a(res1, res2, res3, 1)
		 * ����TmInvert��a��֪����x��
		 *       x = (1/TmInvert) * a
		 * </pre>
		 * 
		 * @param res1
		 * @param res2
		 * @param res3
		 * @param tm
		 */
		Vector3f mult(long res1, long res2, long res3, MATRIX tm) {
			long v1 = -((res2 * tm._33 * tm._21 - res2 * tm._23 * tm._31 - res1
					* tm._33 * tm._22 + res1 * tm._23 * tm._32 - res3 * tm._21
					* tm._32 + res3 * tm._31 * tm._22 + tm._43 * tm._21
					* tm._32 - tm._43 * tm._31 * tm._22 - tm._33 * tm._21
					* tm._42 + tm._33 * tm._41 * tm._22 + tm._23 * tm._31
					* tm._42 - tm._23 * tm._41 * tm._32) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12
							+ tm._21 * tm._32 * tm._13 - tm._33 * tm._21
							* tm._12 - tm._11 * tm._23 * tm._32 - tm._31
							* tm._22 * tm._13);
			long v2 = ((res2 * tm._11 * tm._33 - res1 * tm._33 * tm._12 - res3
					* tm._11 * tm._32 + res3 * tm._31 * tm._12 - res2 * tm._31
					* tm._13 + res1 * tm._32 * tm._13 + tm._11 * tm._43
					* tm._32 - tm._43 * tm._31 * tm._12 - tm._11 * tm._33
					* tm._42 + tm._33 * tm._41 * tm._12 + tm._31 * tm._42
					* tm._13 - tm._41 * tm._32 * tm._13) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12
							+ tm._21 * tm._32 * tm._13 - tm._33 * tm._21
							* tm._12 - tm._11 * tm._23 * tm._32 - tm._31
							* tm._22 * tm._13);
			long v3 = -((res2 * tm._11 * tm._23 - res1 * tm._23 * tm._12 - res3
					* tm._11 * tm._22 + res3 * tm._21 * tm._12 - res2 * tm._21
					* tm._13 + res1 * tm._22 * tm._13 + tm._11 * tm._43
					* tm._22 - tm._43 * tm._21 * tm._12 - tm._11 * tm._23
					* tm._42 + tm._23 * tm._41 * tm._12 + tm._21 * tm._42
					* tm._13 - tm._41 * tm._22 * tm._13) << 8)
					/ (tm._11 * tm._33 * tm._22 + tm._23 * tm._31 * tm._12
							+ tm._21 * tm._32 * tm._13 - tm._33 * tm._21
							* tm._12 - tm._11 * tm._23 * tm._32 - tm._31
							* tm._22 * tm._13);

			float x = (float) v1 / 256.0f;
			float y = (float) v2 / 256.0f;
			float z = (float) v3 / 256.0f;

			if (OPEN_GL_AXIS) {
				return new Vector3f(-y, z, -x);
			} else {
				return new Vector3f(x, y, z);
			}
		}

		void invertPoint() {

			for (int i = 0; i < nVertex; i++) {
				if (Physique != null) {
					Vertex[i].v = mult(Vertex[i].x, Vertex[i].y, Vertex[i].z,
							Physique[i].TmInvert);
				} else {
					Vertex[i].v = mult(Vertex[i].x, Vertex[i].y, Vertex[i].z,
							TmInvert);
				}
			}
		}
	}

	/**
	 * size = 1228
	 */
	class PAT3D {
		// DWORD Head;
		OBJ3D[] obj3d = new OBJ3D[128];
		byte[] TmSort = new byte[128];

		PAT3D TmParent;

		MATERIAL_GROUP smMaterialGroup;// ������

		int MaxFrame;
		int Frame;

		int SizeWidth, SizeHeight; // ���� ���� �� �ִ�ġ

		int nObj3d;
		// LPDIRECT3DTEXTURE2 *hD3DTexture;

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
			for (int i = 0; i < 128; i++) {
				getInt();
			}
			buffer.get(TmSort);

			getInt();// smPAT3D *TmParent;

			getInt();// smMATERIAL_GROUP *smMaterialGroup; //��Ʈ���� �׷�

			MaxFrame = getInt();
			Frame = getInt();

			SizeWidth = getInt();
			SizeHeight = getInt();

			nObj3d = getInt();
			getInt();// LPDIRECT3DTEXTURE2 *hD3DTexture;

			Posi = new POINT3D(true);
			Angle = new POINT3D(true);
			CameraPosi = new POINT3D(true);

			dBound = getInt();
			Bound = getInt();

			for (int i = 0; i < OBJ_FRAME_SEARCH_MAX; i++) {
				TmFrame[i] = new FRAME_POS();
			}
			TmFrameCnt = getInt();

			TmLastFrame = getInt();
			TmLastAngle = new POINT3D(true);

			assert buffer.position() - start == 1228;
		}

		void init() {
			nObj3d = 0;
			// hD3DTexture = 0;
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

			for (int i = 0; i < 128; i++) {
				TmSort[i] = (byte) i;
			}

			smMaterialGroup = null;
		}

		void loadFile(String NodeName, PAT3D BipPat) {
			log.debug("ģ���ļ�:" + key.getName());

			OBJ3D obj;
			FILE_HEADER FileHeader = smd_file_header;

			init();

			// ��ȡObj3D������Ϣ
			FILE_OBJINFO[] FileObjInfo = new FILE_OBJINFO[FileHeader.objCounter];
			for (int i = 0; i < FileHeader.objCounter; i++) {
				FileObjInfo[i] = new FILE_OBJINFO();
			}

			// ��¼�ļ�ͷ�еĶ�����֡��������ÿ֡�����ݡ�
			TmFrameCnt = FileHeader.tmFrameCounter;
			for (int i = 0; i < 32; i++) {
				TmFrame[i] = FileHeader.TmFrame[i];
			}

			// ��ȡ����
			// �����ļ�(.smb)�в��������ʣ���˿���û����һ�����ݡ�
			if (FileHeader.matCounter > 0) {
				smMaterialGroup = new MATERIAL_GROUP();
				smMaterialGroup.loadFile();
			}

			if (NodeName != null) {
				log.debug("NodeName != null && NodeName == " + NodeName);
				// ����ָ�����Ƶ�3D����
				for (int i = 0; i < FileHeader.objCounter; i++) {
					if (NodeName.equals(FileObjInfo[i].NodeName)) {
						obj = new OBJ3D();
						if (obj != null) {
							buffer.position(FileObjInfo[i].ObjFilePoint);
							obj.loadFile(BipPat);
							addObject(obj);
						}
						break;
					}
				}
			} else {
				// ��ȡȫ��3D����
				for (int i = 0; i < FileHeader.objCounter; i++) {
					obj = new OBJ3D();
					if (obj != null) {
						obj.loadFile(BipPat);
						addObject(obj);
					}
				}
				linkObject();
			}

			TmParent = BipPat;
		}

		boolean addObject(OBJ3D obj) {
			// ������������������128��
			if (nObj3d < 128) {
				obj3d[nObj3d] = obj;
				nObj3d++;

				// ͳ�ƶ���֡��
				int frame = 0;
				if (obj.TmRotCnt > 0 && obj.TmRot != null)
					frame = obj.TmRot[obj.TmRotCnt - 1].frame;
				if (obj.TmPosCnt > 0 && obj.TmPos != null)
					frame = obj.TmPos[obj.TmPosCnt - 1].frame;
				if (MaxFrame < frame)
					MaxFrame = frame;

				// ũ�� ���� ����
				if (SizeWidth < obj.maxX)
					SizeWidth = obj.maxX;
				if (SizeWidth < obj.maxZ)
					SizeWidth = obj.maxZ;
				if (SizeHeight < obj.maxY)
					SizeHeight = obj.maxY;

				// �ٿ�� ����� ��
				if (Bound < obj.Bound) {
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
			for (int i = 0; i < nObj3d; i++) {
				if (obj3d[i].NodeParent != null) {
					for (int k = 0; k < nObj3d; k++) {
						if (obj3d[i].NodeParent.equals(obj3d[k].NodeName)) {
							obj3d[i].pParent = obj3d[k];
							break;
						}
					}
				} else {
					log.debug("j = 0");
				}
			}

			int NodeCnt = 0;

			// ����
			for (int i = 0; i < 128; i++) {
				TmSort[i] = 0;
			}

			// ���ȼ�¼���ڵ�
			for (int i = 0; i < nObj3d; i++) {
				if (obj3d[i].pParent == null)
					TmSort[NodeCnt++] = (byte) i;
			}

			// �θ� �޷��ִ� �ڽ��� ã�� ������� ����
			for (int j = 0; j < nObj3d; j++) {
				for (int i = 0; i < nObj3d; i++) {
					if (obj3d[i].pParent != null
							&& obj3d[TmSort[j]] == obj3d[i].pParent) {
						TmSort[NodeCnt++] = (byte) i;
					}
				}
			}
		}

		/**
		 * ���ݽ�����ƣ���ѯObj3D����
		 * 
		 * @param name
		 * @return
		 */
		OBJ3D getObjectFromName(String name) {
			for (int i = 0; i < nObj3d; i++) {
				if (obj3d[i].NodeName.equals(name)) {
					return obj3d[i];
				}
			}
			return null;
		}

		/**
		 * ���ɹ���
		 */
		Skeleton buildSkeleton() {

			HashMap<String, Bone> boneMap = new HashMap<String, Bone>();
			Bone[] bones = new Bone[nObj3d];
			for (int i = 0; i < nObj3d; i++) {
				OBJ3D obj = obj3d[i];

				// ����һ����ͷ
				Bone bone = new Bone(obj.NodeName);
				bones[i] = bone;

				// ���ó�ʼPOSE
				if (OPEN_GL_AXIS) {
					Vector3f translation = new Vector3f(-obj.py, obj.pz,
							-obj.px);
					Quaternion rotation = new Quaternion(-obj.qy, obj.qz,
							-obj.qx, -obj.qw);
					Vector3f scale = new Vector3f(obj.sy, obj.sz, obj.sx);

					bone.setBindTransforms(translation, rotation, scale);
				} else {
					Vector3f translation = new Vector3f(obj.px, obj.py, obj.pz);
					Quaternion rotation = new Quaternion(-obj.qx, -obj.qy,
							-obj.qz, obj.qw);
					Vector3f scale = new Vector3f(obj.sx, obj.sy, obj.sz);

					bone.setBindTransforms(translation, rotation, scale);
				}

				// �������ӹ�ϵ
				boneMap.put(obj.NodeName, bone);
				if (obj.NodeParent != null) {
					Bone parent = boneMap.get(obj.NodeParent);
					if (parent != null)
						parent.addChild(bone);
				}

			}

			// ���ɹǼ�
			return new Skeleton(bones);
		}

		/**
		 * ���ɹ���
		 * 
		 * @param ske
		 */
		Animation buildAnimation(Skeleton ske) {

			// ͳ��֡��
			int maxFrame = 0;
			for (int i = 0; i < nObj3d; i++) {
				OBJ3D obj = obj3d[i];
				if (obj.TmRotCnt > 0) {
					if (obj.TmRot[obj.TmRotCnt - 1].frame > maxFrame) {
						maxFrame = obj.TmRot[obj.TmRotCnt - 1].frame;
					}
				}
				if (obj.TmPosCnt > 0) {
					if (obj.TmPos[obj.TmPosCnt - 1].frame > maxFrame) {
						maxFrame = obj.TmPos[obj.TmPosCnt - 1].frame;
					}
				}
				if (obj.TmScaleCnt > 0) {
					if (obj.TmScale[obj.TmScaleCnt - 1].frame > maxFrame) {
						maxFrame = obj.TmScale[obj.TmScaleCnt - 1].frame;
					}
				}

				if (LOG_ANIMATION) {
					log.debug(obj.NodeName + " ���֡=" + maxFrame);
					log.debug("TmPos:" + obj.TmPosCnt + " TmRot:"
							+ obj.TmRotCnt + " TmScl:" + obj.TmScaleCnt);
				}
			}

			// ���㶯��ʱ��
			float length = (maxFrame) / framePerSecond;

			if (LOG_ANIMATION) {
				log.debug("������ʱ��=" + length);
			}

			Animation anim = new Animation("Anim", length);

			/**
			 * ͳ��ÿ�������Ĺؼ�֡
			 */
			for (int i = 0; i < nObj3d; i++) {
				OBJ3D obj = obj3d[i];

				if (LOG_ANIMATION) {
					log.debug("TmPos:" + obj.TmPosCnt + " TmRot:"
							+ obj.TmRotCnt + " TmScl:" + obj.TmScaleCnt);
				}

				/**
				 * ͳ�ƹؼ�֡��
				 */
				TreeMap<Integer, Keyframe> keyframes = new TreeMap<Integer, Keyframe>();
				for (int j = 0; j < obj.TmPosCnt; j++) {
					TM_POS pos = obj.TmPos[j];
					Keyframe k = getOrMakeKeyframe(keyframes, pos.frame);
					if (OPEN_GL_AXIS) {
						k.translation = new Vector3f(-pos.y, pos.z, -pos.x);
					} else {
						k.translation = new Vector3f(pos.x, pos.y, pos.z);
					}
				}

				for (int j = 0; j < obj.TmRotCnt; j++) {
					TM_ROT rot = obj.TmRot[j];
					Keyframe k = getOrMakeKeyframe(keyframes, rot.frame);
					if (OPEN_GL_AXIS) {
						k.rotation = new Quaternion(-rot.y, rot.z, -rot.x,
								-rot.w);
					} else {
						k.rotation = new Quaternion(rot.x, rot.y, rot.z, rot.w);
					}
				}

				Quaternion ori = new Quaternion(0, 0, 0, 1);
				for (Keyframe k : keyframes.values()) {
					if (k.rotation != null) {
						// ori.multLocal(k.rotation);
						ori = k.rotation.mult(ori);
						k.rotation.set(ori);
					}
				}

				for (int j = 0; j < obj.TmScaleCnt; j++) {
					TM_SCALE scale = obj.TmScale[j];
					Keyframe k = getOrMakeKeyframe(keyframes, scale.frame);
					if (OPEN_GL_AXIS) {
						k.scale = new Vector3f(scale.z, scale.y, scale.x);
					} else {
						k.scale = new Vector3f(scale.x, scale.y, scale.z);
					}
				}

				if (LOG_ANIMATION) {
					log.debug("Track[" + obj.NodeName + "]:");
				}

				/**
				 * ���㶯�����ݡ� ΪBoneTrack׼�����ݡ�
				 */
				int size = keyframes.size();
				if (size == 0) {
					if (LOG_ANIMATION) {
						log.debug("  û�йؼ�֡");
					}
					continue;// ���������һ������
				}

				float[] times = new float[size];
				Vector3f[] translations = new Vector3f[size];
				Quaternion[] rotations = new Quaternion[size];
				Vector3f[] scales = new Vector3f[size];

				/**
				 * ���ھ����е�pose������rotate������scale������������һ����ͬ��
				 * ���keyframe����Щ���Ե�ֵ������null�� ���ĳһ֡ȱ�����������ݣ���ô������һ֡�����ݡ�
				 */
				Keyframe last = null;
				/**
				 * �������������¼�Ѿ��������˵ڼ���Keyframe�� ��n=0ʱ����ʼ��last������ֵ��
				 * ��ѭ����ĩβ�����ǽ�last������ָ��ǰKeyframe����
				 */
				int n = 0;
				for (Integer frame : keyframes.keySet()) {
					// ��ȡ��ǰ֡
					Keyframe current = keyframes.get(frame);

					// ���pose����
					if (current.translation == null) {
						if (n == 0) {
							current.translation = new Vector3f(0, 0, 0);
						} else {// ������һ֡������
							current.translation = new Vector3f(last.translation);
						}
					}

					// ���rotate����
					if (current.rotation == null) {
						if (n == 0) {
							current.rotation = new Quaternion(0, 0, 0, 1);
						} else {
							current.rotation = new Quaternion(last.rotation);
						}
					}

					// ���scale����
					if (current.scale == null) {
						if (n == 0) {
							current.scale = new Vector3f(1, 1, 1);
						} else {
							current.scale = new Vector3f(last.scale);
						}
					}

					times[n] = frame / framePerSecond;
					translations[n] = current.translation;
					rotations[n] = current.rotation.normalizeLocal();
					scales[n] = current.scale;

					if (LOG_ANIMATION) {
						String str = String
								.format("  Frame=%05d time=%.5f pos=%s rot=%s scale=%s",
										frame, times[n], translations[n],
										rotations[n], scales[n]);
						log.debug(str);
					}

					// ��¼��ǰ֡
					last = current;

					n++;
				}

				BoneTrack track = new BoneTrack(ske.getBoneIndex(obj.NodeName));
				track.setKeyframes(times, translations, rotations, scales);
				anim.addTrack(track);
			}

			return anim;
		}

		/**
		 * ����֡�ı������ѯKeyframe���ݣ����ĳ��frame��û�ж�Ӧ��Keyframe���ݣ��ʹ���һ���µġ�
		 * 
		 * @param keyframes
		 * @param frame
		 * @return
		 */
		private Keyframe getOrMakeKeyframe(
				SortedMap<Integer, Keyframe> keyframes, Integer frame) {
			Keyframe k = keyframes.get(frame);
			if (k == null) {
				k = new Keyframe();
				keyframes.put(frame, k);
			}
			return k;
		}

		Node buildNode() {
			Node rootNode = new Node("PAT3D:" + key.getName());

			Skeleton ske = null;
			// ���ɹ���
			if (TmParent != null) {
				ske = TmParent.buildSkeleton();
			}

			for (int i = 0; i < nObj3d; i++) {
				OBJ3D obj = obj3d[i];
				if (obj.nFace > 0) {

					// �����ж���������Ա任�����򶥵�����궼��ԭ�㸽����
					obj.invertPoint();

					// ����ģ�͵Ĳ��ʲ�ͬ��������������񣬷ֱ���Ⱦ��
					for (int mat_id = 0; mat_id < smMaterialGroup.materialCount; mat_id++) {
						// ��������
						Mesh mesh = obj.buildMesh(mat_id, ske);

						// ��������
						MATERIAL m = smMaterialGroup.materials[mat_id];
						Material mat = createLightMaterial(m);

						// ���������岢Ӧ�ò��ʡ�
						Geometry geom = new Geometry(obj3d[i].NodeName + "#"
								+ mat_id, mesh);
						geom.setMaterial(mat);

						// ����λ��
						// TODO ���λ�����ú󲢲�׼ȷ����Ҫ��һ���о���
						Vector3f translation = new Vector3f(-obj.py, obj.pz,
								-obj.px);
						Quaternion rotation = new Quaternion(-obj.qy, obj.qz,
								-obj.qx, -obj.qw);
						Vector3f scale = new Vector3f(obj.sy, obj.sz, obj.sx);
						geom.setLocalTranslation(translation);
						geom.setLocalRotation(rotation);
						geom.setLocalScale(scale);

						rootNode.attachChild(geom);
					}
				}
			}

			// �󶨶���������
			if (ske != null) {
				Animation anim = TmParent.buildAnimation(ske);
				AnimControl ac = new AnimControl(ske);
				ac.addAnim(anim);
				rootNode.addControl(ac);
				rootNode.addControl(new SkeletonControl(ske));
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
		 * ���û�ʹ����SmdKey���͸���type�������������ַ�ʽ������ģ�͡�
		 */
		SmdKey smdkey = (SmdKey) key;
		SMDTYPE type = smdkey.type;
		switch (type) {
		case STAGE3D: {// ����ͼ
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			STAGE3D stage3D = new STAGE3D();
			stage3D.loadFile();
			return stage3D.buildNode();
		}
		case STAGE3D_SOLID: {// ��ͼ����
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			STAGE3D stage3D = new STAGE3D();
			stage3D.loadFile();
			return stage3D.buildSolidMesh();
		}
		case BONE: {
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			PAT3D bone = new PAT3D();
			bone.loadFile(null, null);
			return bone;
		}
		case PAT3D_BIP: {// �ж�������̨����
			// ��׺����Ϊsmb
			String smbFile = key.getName();
			int n = smbFile.lastIndexOf(".");
			String str = smbFile.substring(0, n);
			smbFile = str + ".smb";
			PAT3D bone = (PAT3D) manager.loadAsset(new SmdKey(smbFile,
					SMDTYPE.BONE));

			// �ټ���smd�ļ�
			key = assetInfo.getKey();
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			PAT3D pat = new PAT3D();
			pat.loadFile(null, bone);
			return pat.buildNode();
		}
		case PAT3D: {// ��̨���壬�޶���
			getByteBuffer(assetInfo.openStream());
			smd_file_header = new FILE_HEADER();
			PAT3D pat = new PAT3D();
			pat.loadFile(null, smdkey.getBone());
			return pat.buildNode();
		}
		case INX: {
			String inx = key.getName().toLowerCase();
			// inx�ļ�
			if (inx.endsWith("inx")) {
				// �ļ����Ȳ���
				if (assetInfo.openStream().available() <= 67083) {
					log.warn("Error: can't read inx-file (invalid file content)");
					return null;
				}

				getByteBuffer(assetInfo.openStream());

				return parseInx();
			}

			return null;
		}

		default:
			return null;
		}
	}

	/**************************************************
	 * ����INX�ļ�
	 * 
	 * @return
	 */
	private Object parseInx() {

		String smdFile = getString(64);
		String smbFile = getString(64);

		if (smdFile.length() > 0) {
			smdFile = changeName(smdFile);
		}

		if (smbFile.length() > 0) {
			smbFile = changeName(smbFile);
		}

		DrzAnimation anim;
		String sharedInxFile;
		if (buffer.limit() <= 67084) { // old inx file
			buffer.position(61848);
			sharedInxFile = getString();
			handleShared(sharedInxFile);
			anim = readAnimFromOld();
		} else { // new inx file (KPT)
			buffer.position(88472);
			sharedInxFile = getString();
			handleShared(sharedInxFile);
			anim = readAnimFromNew();
		}

		PAT3D BipPattern = null;
		// Read Animation from smb
		if (smbFile.length() > 0) {
			// ��׺����Ϊsmb
			int n = smbFile.lastIndexOf(".");
			String str = smbFile.substring(0, n);
			smbFile = str + ".smb";

			BipPattern = (PAT3D) manager.loadAsset(new SmdKey(key.getFolder()
					+ smbFile, SMDTYPE.BONE));
		}

		// Read Mesh from smd
		// ��׺����Ϊsmd
		int n = smdFile.lastIndexOf(".");
		String str = smdFile.substring(0, n);
		smdFile = str + ".smd";

		SmdKey smdKey = new SmdKey(key.getFolder() + smdFile, SMDTYPE.PAT3D);
		smdKey.setBone(BipPattern);

		return manager.loadAsset(smdKey);
	}

	/**
	 * ����������
	 */
	private void handleShared(String sharedInxFile) {
		if (sharedInxFile == null || sharedInxFile.length() == 0)
			return;

		// ��׺����Ϊinx
		int n = sharedInxFile.lastIndexOf(".");
		String str = sharedInxFile.substring(0, n);
		sharedInxFile = str + ".inx";

		sharedInxFile = changeName(sharedInxFile);

		// ��ȡ����Ķ���
		File file = new File(sharedInxFile);
		if (file.exists()) {
			try {
				InputStream inputStream = new FileInputStream(file);
				int length = inputStream.available();

				if (length <= 67083) {
					System.err
							.println("Error: can't read inx-file (invalid file content):"
									+ length);
				} else {
					getByteBuffer(inputStream);

					buffer.position(64);
					String smbFile = getString();
					if (smbFile.length() > 0) {
						smbFile = changeName(smbFile);
					}

					// TODO û����ȷʹ��
					log.debug("ʹ���˹���Ĺ�������:" + smbFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			log.warn("Error: " + sharedInxFile + " not exists.");
		}
	}

	class DrzAnimationSet {
		public int AnimationIndex;

		public int AnimationTypeId;

		public double SetStartTime;// ��ʼʱ�� * 160
		public double SetEndTime1;// ����ʱ�� * 160
		public double AnimationDurationTime;// ��ʱ�� * 160

		public int AnimationStartKey;
		public int AnimationEndKey1;
		public int AnimationEndKey2;
		public int AnimationDurationKeys;

		public boolean Repeat;// �Ƿ��ظ�
		public char UnkChar;
		public int SubAnimationIndex;// ��Ӧ����������

		public DrzAnimationSet() {

		}

		public DrzAnimationSet(int _ani_type_id, int _start_key, int _end_key,
				boolean _repeat, char _unk_letter, int _sub_ani_index) {
			AnimationStartKey = _start_key;
			AnimationEndKey1 = _end_key;
			Repeat = _repeat;
			UnkChar = _unk_letter;
			SubAnimationIndex = _sub_ani_index;
			AnimationTypeId = _ani_type_id;
		}

		public String toString() {
			String name = getAnimationSetNameById(AnimationTypeId);
			float length = (float) AnimationDurationTime * 160;
			return String.format(
					"[%d %s]SubAnimInx=%d Type=%d ��ʼ֡=%d ����֡=%d �ظ�=%b ʱ��=%.2f",
					AnimationIndex, name, SubAnimationIndex, AnimationTypeId,
					AnimationStartKey, AnimationEndKey1, Repeat, length);
		}

		public String getName() {
			return AnimationIndex + " "
					+ getAnimationSetNameById(AnimationTypeId);
		}

		public float getLength() {
			return (float) AnimationDurationTime * 160;
		}

		public Animation newJmeAnimation() {
			return new Animation(getName(), getLength());
		}
	}

	class DrzAnimation {
		String mAnimationName;

		HashMap<Integer, DrzAnimationSet> mAnimationSetMap = new HashMap<Integer, DrzAnimationSet>();

		List<DrzInxMeshInfo> meshDefInfo = new ArrayList<DrzInxMeshInfo>();

		int mSubAnimationNum;
	}

	/**
	 * ������������
	 * 
	 * @return
	 */
	private DrzAnimation readAnimFromOld() {
		DrzAnimation animation = new DrzAnimation();

		// Read The Mesh Def
		readMeshDef();

		buffer.position(61836);
		int AnimationCount = getShort() - 10;

		int AnimationOffset = 1596;

		int SubAnimationNum = 0;
		for (int i = 0; i < AnimationCount; i++) {
			buffer.position(AnimationOffset + (i * 120) + 116);
			int temp_max_sub_ani = getUnsignedInt();
			if (temp_max_sub_ani > SubAnimationNum) {
				SubAnimationNum = temp_max_sub_ani;
			}
		}

		// ������������
		if (SubAnimationNum > 0) {

			animation.mSubAnimationNum = SubAnimationNum;

			// ��ʱ����
			int[] tmpInt = new int[2];
			for (int id = 0; id < AnimationCount; id++) {

				/**
				 * ���������ͣ���getAminationSetNameById��֪��ʲô��˼��
				 */
				buffer.position(AnimationOffset + (id * 120));

				int AnimationId = getInt();

				if (AnimationId < 1) // no more Animations
					break;

				DrzAnimationSet animSet = new DrzAnimationSet();

				// Set AnimationSetID
				animSet.AnimationTypeId = AnimationId;

				// ��ʼ֡
				buffer.position(AnimationOffset + (id * 120) + 4);// current
																	// animation
																	// starts at
																	// this
																	// frame
				tmpInt[0] = buffer.get() & 0xFF;
				buffer.position(AnimationOffset + (id * 120) + 6);
				tmpInt[1] = buffer.get() & 0xFF;

				animSet.AnimationStartKey = (tmpInt[1] << 8) + tmpInt[0];

				/**
				 * ����֡
				 */
				buffer.position(AnimationOffset + (id * 120) + 16);// current
																	// animation
																	// end at
																	// this
																	// frame
				tmpInt[0] = buffer.get() & 0xFF;
				buffer.position(AnimationOffset + (id * 120) + 18);
				tmpInt[1] = buffer.get() & 0xFF;

				animSet.AnimationEndKey1 = (tmpInt[1] << 8) + tmpInt[0];

				/**
				 * �����Ƿ��ظ�����
				 */
				buffer.position(AnimationOffset + (id * 120) + 108);

				animSet.Repeat = (getInt() == 1);

				// TODO δ֪�ַ�
				buffer.position(AnimationOffset + (id * 120) + 112);

				animSet.UnkChar = buffer.getChar();

				/**
				 * ��Ӧ������������
				 */
				buffer.position(AnimationOffset + (id * 120) + 116);

				int animIndex = getInt();
				if (animIndex > 0) {
					animIndex--;
				}
				animSet.SubAnimationIndex = animIndex;

				animation.mAnimationSetMap.put(id, animSet);
			}
		}

		return animation;
	}

	/**
	 * ������������
	 * 
	 * @return
	 */
	private DrzAnimation readAnimFromNew() {
		DrzAnimation animation = new DrzAnimation();

		// Read The Mesh Def
		readMeshDef();

		buffer.position(88460);
		int AnimationCount = getShort() - 10;

		int AnimationOffset = 2116;

		int SubAnimationNum = 0;
		for (int i = 0; i < AnimationCount; i++) {
			buffer.position(AnimationOffset + (i * 172) + 168);
			int temp_max_sub_ani = getUnsignedInt();
			if (temp_max_sub_ani > SubAnimationNum) {
				SubAnimationNum = temp_max_sub_ani;
			}
		}

		// ������������
		if (SubAnimationNum > 0) {
			animation.mSubAnimationNum = SubAnimationNum;

			for (int i = 0; i < AnimationCount; i++) {
				buffer.position(AnimationOffset + (i * 172));
				int AnimationId = getInt();

				if (AnimationId < 1) // no more Animations
					break;

				DrzAnimationSet CurrentAnimationSet = new DrzAnimationSet();

				// Set AnimationSetID
				CurrentAnimationSet.AnimationTypeId = AnimationId;

				int[] val = new int[2];

				buffer.position(AnimationOffset + (i * 172) + 4);// current
																	// animation
																	// starts at
																	// this
																	// frame
				val[0] = buffer.get() & 0xFF;
				buffer.position(AnimationOffset + (i * 172) + 6);
				val[1] = buffer.get() & 0xFF;

				CurrentAnimationSet.AnimationStartKey = 160 * ((val[1] << 8) + val[0]);

				buffer.position(AnimationOffset + (i * 172) + 16);// current
																	// animation
																	// end at
																	// this
																	// frame
				val[0] = buffer.get() & 0xFF;
				buffer.position(AnimationOffset + (i * 172) + 18);
				val[1] = buffer.get() & 0xFF;
				CurrentAnimationSet.AnimationEndKey1 = 160 * ((val[1] << 8) + val[0]);

				buffer.position(AnimationOffset + (i * 172) + 24);// secound end
																	// key,
																	// downt
																	// know why
				val[0] = buffer.get() & 0xFF;
				buffer.position(AnimationOffset + (i * 172) + 26);
				val[1] = buffer.get() & 0xFF;
				CurrentAnimationSet.AnimationEndKey2 = 160 * ((val[1] << 8) + val[0]);

				CurrentAnimationSet.Repeat = false;
				buffer.position(AnimationOffset + (i * 172) + 160);
				int iRepeat = getInt();
				if (iRepeat == 1) {
					CurrentAnimationSet.Repeat = true;
				}

				buffer.position(AnimationOffset + (i * 172) + 164);
				CurrentAnimationSet.UnkChar = buffer.getChar();

				buffer.position(AnimationOffset + (i * 172) + 168);
				CurrentAnimationSet.SubAnimationIndex = getInt();
				if (CurrentAnimationSet.SubAnimationIndex > 0) {
					CurrentAnimationSet.SubAnimationIndex--;
				}

				// Add AnimationSet
				CurrentAnimationSet.AnimationIndex = i;
				animation.mAnimationSetMap.put(i, CurrentAnimationSet);
			}
		}

		return animation;
	}

	class DrzInxMeshInfo {
		int type = -1;
		String meshName1;
		String meshName2;
		String meshName3;
		String meshName4;
	}

	/**
	 * ��ȡ������
	 */
	private void readMeshDef() {
		int MeshDefOffset = 192;

		for (int i = 0; i < 28; i++) {
			buffer.position(MeshDefOffset + i * 68);
			int MeshDefNum = getInt();

			if (MeshDefNum > 0) {
				DrzInxMeshInfo subMesh = new DrzInxMeshInfo();
				subMesh.type = 1;
				buffer.position(MeshDefOffset + i * 68 + 4);
				subMesh.meshName1 = getString();
				buffer.position(MeshDefOffset + i * 68 + 20);
				subMesh.meshName2 = getString();
				buffer.position(MeshDefOffset + i * 68 + 36);
				subMesh.meshName3 = getString();
				buffer.position(MeshDefOffset + i * 68 + 52);
				subMesh.meshName4 = getString();

			}
		}
	}

	private String getAnimationSetNameById(int id) {
		String ret = "unknown";

		switch (id) {
		case 64:
			ret = "Idle";
			break;
		case 80:
			ret = "Walk";
			break;
		case 96:
			ret = "Run";
			break;
		case 128:
			ret = "Fall";
			break;
		case 256:
			ret = "Attack";
			break;
		case 272:
			ret = "Damage";
			break;
		case 288:
			ret = "Die";
			break;
		case 304:
			ret = "Sometimes";
			break;
		case 320:
			ret = "Potion";
			break;
		case 336:
			ret = "Technique";
			break;
		case 368:
			ret = "Landing (small)";
			break;
		case 384:
			ret = "Landing (large)";
			break;
		case 512:
			ret = "Standup";
			break;
		case 528:
			ret = "Cry";
			break;
		case 544:
			ret = "Hurray";
			break;
		case 576:
			ret = "Jump";
			break;
		}

		return ret;
	}

	/*******************************************************
	 * ����Ĵ������ڸ��ݾ�������ݽṹ����JME3���������ʡ�����ȶ���
	 *******************************************************/

	/**
	 * �ı��ļ��ĺ�׺��
	 * 
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
			texture = manager
					.loadTexture(new TextureKey(key.getFolder() + name));
			texture.setWrap(WrapMode.Repeat);
		} catch (Exception ex) {
			// log.warn("���ʼ���ʧ��:" + ex.getMessage());
			texture = manager.loadTexture("Common/Textures/MissingTexture.png");
			texture.setWrap(WrapMode.EdgeClamp);
		}
		return texture;
	}

	/**
	 * ��������
	 * 
	 * @param m
	 * @return
	 */
	private Material createLightMaterial(MATERIAL m) {
		Material mat = new Material(manager,
				"Common/MatDefs/Light/Lighting.j3md");
		mat.setColor("Diffuse", new ColorRGBA(m.Diffuse.r, m.Diffuse.g,
				m.Diffuse.b, 1));
		mat.setColor("Ambient", new ColorRGBA(1f, 1f, 1f, 1f));
		mat.setColor("Specular", new ColorRGBA(0, 0, 0, 1));
		// mat.setBoolean("UseMaterialColors", true);

		// ������ͼ
		if (m.TextureCounter > 0) {
			mat.setTexture("DiffuseMap", createTexture(m.smTexture[0].Name));
		}
		if (m.TextureCounter > 1) {
			mat.setBoolean("SeparateTexCoord", true);
			mat.setTexture("LightMap", createTexture(m.smTexture[1].Name));
		}

		return mat;
	}

	/**
	 * ����һ�����Թ�Դ�Ĳ��ʡ� ����ר��
	 * 
	 * @param m
	 * @return
	 */
	private Material createMiscMaterial(MATERIAL m) {
		Material mat = new Material(manager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		// mat.setColor("Color", new ColorRGBA(m.Diffuse.r, m.Diffuse.g,
		// m.Diffuse.b, 1));
		mat.setColor("Color", ColorRGBA.White);

		// ������ͼ
		if (m.TextureCounter > 0) {
			mat.setTexture("ColorMap", createTexture(m.smTexture[0].Name));
		}
		if (m.TextureCounter > 1) {
			mat.setBoolean("SeparateTexCoord", true);
			mat.setTexture("LightMap", createTexture(m.smTexture[1].Name));
		}

		return mat;
	}

	/**
	 * ���ò��ʵ�RenderState
	 * 
	 * @param m
	 * @param mat
	 */
	private void setRenderState(MATERIAL m, Material mat) {
		RenderState rs = mat.getAdditionalRenderState();

		/**
		 * #define SMMAT_BLEND_NONE 0x00 #define SMMAT_BLEND_ALPHA 0x01 #define
		 * SMMAT_BLEND_COLOR 0x02 #define SMMAT_BLEND_SHADOW 0x03 #define
		 * SMMAT_BLEND_LAMP 0x04 #define SMMAT_BLEND_ADDCOLOR 0x05 #define
		 * SMMAT_BLEND_INVSHADOW 0x06
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
		}
		;

		if (m.TwoSide == 1) {
			rs.setFaceCullMode(FaceCullMode.Off);
		}

		if (m.TextureType == 0x0001) {
			// ����Ĭ����ʾ2��
			rs.setFaceCullMode(FaceCullMode.Off);
		}

		// ͸������
		if (m.MapOpacity != 0 || m.Transparency != 0) {
			// ���ֵ���õ���΢��һЩ�������ݡ�����ͼƬ�ı�Ե�ͻ���Ϊ͸���Ȳ��������˵����ء�
			mat.setFloat("AlphaDiscardThreshold", 0.75f);
			// ��Ȼ�Ѿ���ʱ�����ǻ���д���Է����⡣
			// rs.setAlphaTest(true);
			// rs.setAlphaFallOff(0.6f);
			rs.setDepthWrite(true);
			rs.setDepthTest(true);
			rs.setColorWrite(true);

			// ͸�����岻�ü���
			rs.setFaceCullMode(FaceCullMode.Off);
		}
	}

	/**
	 * AminTexCounter����0˵�����ֲ�����������һ��Control����ʱ���»��档
	 * 
	 * @param m
	 * @return
	 */
	private FrameAnimControl createFrameAnimControl(MATERIAL m) {
		Texture[] tex = new Texture[m.AnimTexCounter];
		for (int i = 0; i < m.AnimTexCounter; i++) {
			tex[i] = createTexture(m.smAnimTexture[i].Name);
		}
		FrameAnimControl control = new FrameAnimControl(tex, m.AnimTexCounter, m.Shift_FrameSpeed);
		return control;
	}


}
