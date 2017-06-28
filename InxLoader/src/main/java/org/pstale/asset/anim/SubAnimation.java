package org.pstale.asset.anim;

/**
 * �������Ӽ�
 * @author yanmaoyuan
 *
 */
public class SubAnimation {
	public int id;// ���
	public int type;// ��������

	public float startTime;// ��ʼʱ�� * 160
	public float endTime;// ����ʱ�� * 160
	public float length;// ��ʱ�� * 160

	public boolean repeat;// �Ƿ��ظ�
	public int animIndex;// ��Ӧ����������
	

	/**
	 * ���췽��
	 * @param id
	 * @param type
	 * @param startTime
	 * @param endTime
	 * @param length
	 * @param repeat
	 * @param animIndex
	 */
	public SubAnimation(int id, int type, float startTime, float endTime,
			float length, boolean repeat, int animIndex) {
		super();
		this.id = id;
		this.type = type;
		this.startTime = startTime;
		this.endTime = endTime;
		this.length = length;
		this.repeat = repeat;
		this.animIndex = animIndex;
	}

	public String toString() {
		return String.format("[%s]SubAnimInx=%d Type=%d �ظ�=%b ʱ��=%.2f", getName(), animIndex, type, repeat, length);
	}
	
	public String getName() {
		return id + " " + getAnimationSetNameById(type);
	}
	
	public static String getAnimationSetNameById(int id) {
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
}