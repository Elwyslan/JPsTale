package org.pstale.entity;

import java.util.ArrayList;
import java.util.List;

import org.pstale.components.Gate;
import org.pstale.components.StgMonster;

public class Field {

	List<Gate> gates;
	
	// 怪物列表
	List<StgMonster> monsterList = new ArrayList<StgMonster>();// 最多50种

}
