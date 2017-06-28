package org.pstale.asset.anim;

import com.jme3.animation.Animation;

/**
 * �������Ӽ�
 * @author yanmaoyuan
 *
 */
public class DrzAnimationSet {
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
		float length = (float)AnimationDurationTime * 160;
		return String.format("[%d %s]SubAnimInx=%d Type=%d ��ʼ֡=%d ����֡=%d �ظ�=%b ʱ��=%.2f",
						AnimationIndex, name, SubAnimationIndex, AnimationTypeId, AnimationStartKey,
						AnimationEndKey1, Repeat, length);
	}
	
	public String getName() {
		return AnimationIndex + " " + getAnimationSetNameById(AnimationTypeId);
	}

	public float getLength() {
		return (float)AnimationDurationTime * 160;
	}
	
	public Animation newJmeAnimation() {
		return new Animation(getName(), getLength());
	}
	
	public String getAnimationSetNameById(int id) {
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