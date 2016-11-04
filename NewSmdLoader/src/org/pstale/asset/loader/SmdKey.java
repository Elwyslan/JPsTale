package org.pstale.asset.loader;

import org.pstale.asset.loader.StageLoader.PAT3D;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetProcessor;
import com.jme3.asset.CloneableAssetProcessor;
import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefAssetCache;

public class SmdKey extends AssetKey<Object> {

	public enum SMDTYPE {
		/**
		 * �������͵�smd�ļ������˵�ͼ�����ݡ�
		 */
		STAGE3D,
		/**
		 * �����ļ����洢��PAT3D�ṹ���еĹ����������������κβ��ʡ�
		 * ��׺��Ϊsmb
		 */
		BONE,
		/**
		 * �����ļ����ڴ洢�˽�ɫ�����NPC����̨��������ݣ����������񡢲��ʵ����ݡ�
		 */
		PAT3D,
		/**
		 * �����ļ�����һ�����ƣ����ǰ���������
		 * ���ļ���Ϊ Field/iron/i2-bip04_ani.smd
		 * �����ļ�Ϊ Field/iron/i2-bip04_ani.smb
		 * 2���ļ�ֻ�к�׺����ͬ��Ҫ�ȼ���BONE��Ȼ���ټ���PAT3D������ȷ�󶨹�����
		 */
		PAT3D_BIP,
		/**
		 * ���ǽ�ɫ�����NPC�ȴ��и��Ӷ���ģ�͵��ļ����͡��Ƚ���INX�ļ���ö���������Ȼ���ټ��ؾ����ģ�͡�
		 */
		INX;
	}
	
	SMDTYPE type;
	PAT3D bone;

	public SmdKey(String name, SMDTYPE type) {
		super(name);
		this.type = type;
	}
	
    public PAT3D getBone() {
		return bone;
	}

	public void setBone(PAT3D bone) {
		this.bone = bone;
	}

	@Override
    public Class<? extends AssetCache> getCacheType(){
        return WeakRefAssetCache.class;
    }
    
    @Override
    public Class<? extends AssetProcessor> getProcessorType(){
        return CloneableAssetProcessor.class;
    }
	
}
