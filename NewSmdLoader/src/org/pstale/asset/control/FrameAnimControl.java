package org.pstale.asset.control;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.Texture;

/**
 * ֡���������� ����Ĳ��ֶ�����ͨ��ͼƬ�ֲ���ʵ�ֵġ�
 * 
 * @author yanmaoyuan
 * 
 */
public class FrameAnimControl extends AbstractControl implements Savable {
	/**
	 * ������������
	 */
	private Texture[] textures;
	/**
	 * ����֡��
	 */
	private int numTex;
	/**
	 * ֡�л���ʱ��������λΪ�롣
	 */
	private float internal;

	/**
	 * 
	 * @param numTex
	 * @param shiftFrameSpeed
	 *            ֡���л��ٶȣ�Ϊ2��n�η�����λ�Ǻ��롣
	 */
	public FrameAnimControl(Texture[] tex, int numTex, int shiftFrameSpeed) {
		this.textures = tex;
		this.numTex = numTex;
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
			mat.setTexture("ColorMap", textures[index]);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public void write(JmeExporter e) throws IOException {
		OutputCapsule capsule = e.getCapsule(FrameAnimControl.this);
		capsule.write(index, "index", 0);
		capsule.write(time, "time", 0f);
		capsule.write(numTex, "numTex", 1);
		capsule.write(internal, "internal", 0.064f);
		
		for(int i=0; i<numTex; i++) {
			capsule.write(textures[i], "tex"+i, null);
		}
	}

	public void read(JmeImporter e) throws IOException {
		InputCapsule capsule = e.getCapsule(this);
		index = capsule.readInt("index", 0);
		time = capsule.readFloat("time", 0f);
		numTex = capsule.readInt("numTex", 1);
		internal = capsule.readFloat("internal", 0.064f);
		
		textures = new Texture[numTex];
		for(int i=0; i<numTex; i++) {
			textures[i] = (Texture) capsule.readSavable("tex"+i, null);
		}
	}
}