package org.pstale.asset.loader;

import org.pstale.asset.anim.DrzAnimation;

import com.jme3.asset.ModelKey;

/**
 * ������InxLoader��SmbLoader֮�䴫�Ρ�
 * @author yanmaoyuan
 *
 */
public class SmbKey extends ModelKey {

	DrzAnimation drzAnimation;
	
	public SmbKey(String name, DrzAnimation anim) {
		super(name);
		this.drzAnimation = anim;
	}

	public DrzAnimation getDrzAnimation() {
		return drzAnimation;
	}
	
}
