package org.pstale.asset.anim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.LoopMode;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * ����Ķ���������
 * @author yanmaoyuan
 *
 */
public class MotionControl extends AbstractControl {

	private HashMap<Integer, SubAnimation> subAnimSet;
	private boolean isRunning = false;
	private int runningAnimId = -1;
	private float animTime = 0;
	
	private SubAnimation cur;
	
	private AnimControl animControl = null;
	private AnimChannel channel = null;

	/**
	 * ���췽��
	 * @param subAnimSet
	 */
	public MotionControl(HashMap<Integer, SubAnimation> subAnimSet) {
		this.subAnimSet = subAnimSet;
	}
	
	/**
	 * ����cloneForSpatial
	 */
	private MotionControl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * ��õ�ǰ�������еĶ�����������
	 * @return
	 */
	public int GetRunningSubAnimationIndex() {
		if (subAnimSet == null) {
			return -1;
		}
		if (runningAnimId < 0) {
			return -1;
		}
		return subAnimSet.get(runningAnimId).animIndex;
	}
	
	/**
	 * ����ϣ��ִ�еĶ������
	 * @param id
	 */
	public void SetAnimation(int id) {
		if (animControl == null) {
			return;
		}
		if (subAnimSet == null) {
			return;
		}

		if (id < 0) {
			return;
		}
		
		if (subAnimSet.containsKey(id) == true) {
			runningAnimId = id;
			cur = subAnimSet.get(id);
			
			// ��鶯���Ƿ����
			String name = cur.animIndex + "";
			Animation animation = animControl.getAnim(name);
			if (animation != null) {
				// ��Ҫ���еĶ���
				channel.setTime(cur.startTime);
				channel.setAnim(name);
				channel.setLoopMode(LoopMode.DontLoop);
				
				System.err.printf("Anim:%s StartTime:%.2f Repeat:%b AllTime:%.2f\n", name, cur.startTime, cur.repeat, animation.getLength());
				animTime = 0f;
				isRunning = true;
				return;
			}
			
		}

		runningAnimId = -1;
		System.err.println("Error. the assigned AnimationIndex(" + id + ") was not found in AnimationSet List");
	}

	public void RunAnimation() {
		// Toggle it
		if (isRunning == false) {
			isRunning = true;
		} else {
			isRunning = false;
		}
	}

	@Override
    public void setSpatial(Spatial spatial) {
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been added to a Spatial");
        }   
        this.spatial = spatial;
        
        if (spatial != null) {
			animControl = spatial.getControl(AnimControl.class);
			if (animControl != null) {
				channel = animControl.createChannel();
			}
        }
    }
	
	@Override
	protected void controlUpdate(float tpf) {
		if (subAnimSet == null)
			return;

		if (runningAnimId < 0)
			return;

		if (isRunning == false)
			return;
		
		if (cur == null)
			return;
		
		// Animate the Model
		if (animTime < cur.length) {
			// add elapsed time
			if (isRunning == true)
				animTime += tpf;
		} else {
			animTime -=cur.length;
			if (!cur.repeat) {
				isRunning = false;
				cur = null;
				channel.reset(false);// ֹͣ����
			} else {
				channel.setTime(cur.startTime + animTime);
			}
		}

	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {}

    /**
     *  Default implementation of cloneForSpatial() that
     *  simply clones the control and sets the spatial.
     *  <pre>
     *  AbstractControl c = clone();
     *  c.spatial = null;
     *  c.setSpatial(spatial);
     *  </pre>
     *
     *  Controls that wish to be persisted must be Cloneable.
     */
    @Override
    public Control cloneForSpatial(Spatial spatial) {
    	MotionControl c = new MotionControl();
        c.spatial = null; // to keep setSpatial() from throwing an exception
        c.setSpatial(spatial);
        c.isRunning = isRunning;
        c.animTime = animTime;
        c.subAnimSet = subAnimSet;
        c.runningAnimId = runningAnimId;
        
        HashMap<Integer, SubAnimation> subAnimSet = new HashMap<Integer, SubAnimation>();
        subAnimSet.putAll(this.subAnimSet);
        c.subAnimSet = subAnimSet;
        
        return c;
    }

	public Collection<String> getAnimationNames() {
		ArrayList<String> names = new ArrayList<String>();
		
		int count = subAnimSet.size();
		for(int i=0; i<count; i++) {
			names.add(subAnimSet.get(i).getName());
		}
		return names;
	}
}
