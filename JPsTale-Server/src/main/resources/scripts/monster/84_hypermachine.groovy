monster = [
	ID:'84_hypermachine',
	Name:'杀戮机械',
	Clazz:0,// 1 BOSS
	Brood:0x00,// 0 Normal; 0x90(144) UNDEAD; 0x91 MUTANT; 0x92 DEMON; 0x93 MECHANIC;
	// Common Status
	Level:84,
	ActiveHour:0,
	RespawnGroup:[1, 1],

	Attributes:[
		Life:3000,
		// Attack Status
		Atk:[50, 60],
		AtkSpeed:8,
		Range:200,
		Rating:750,

		// Defence Status
		Flee:740,
		Absorb:20,
		Block:10,
		DamageStunPers:100,

		// Resistance
		Earth:100,
		Fire:60,
		Ice:10,// Water
		Lighting:0,// Wind
		Poison:100,
	],

	// AI
	AI:[
		Nature:0x82,// 0x80 NATURAL; 0x81 GOOD; 0x82 EVIL
		IQ:9,
		Real_Sight:400,
		Talks:[],

		// Move Behavier
		Move_Speed:1,
		MoveRange:64,

		// Skill Behavier
		SpAttackPercetage:15,
		SkillDamage:[61, 70],
		SkillDistance:80,
		SkillRange:55,
		SkillRating:18,
		SkillCurse:0,

		// Heal Behavier
		PotionPercent:0,
		PotionCount:0,
	],

	Looks:[
		ClassCode:88,
		ArrowPosi:[-10, 150],
		ModelSize:0.0,
		UseEventModel:false,
		SizeLevel:-1,
		Model:'char/monster/Hyper/hyper.ini',
		Sound:0x00001530,
	],

	// Drops
	AllSeeItem:false,
	Exp:41000,
	Quantity:1,
	drops:[
		[probability:1040, code:"NULL"/* Drops nothing */],
		[probability:4400, code:"GG101", value:[265, 320]/* Gold */],
		[probability:600, code:"PL104"/* 顶级恢复生命药水 */],
		[probability:600, code:"PS104"/* 顶级恢复耐力药水 */],
		[probability:600, code:"PM104"/* 顶级恢复魔法药水 */],
		[probability:600, code:"PL104"/* 顶级恢复生命药水 */],
		[probability:600, code:"PM104"/* 顶级恢复魔法药水 */],
		[probability:52, code:"DA110"/* 百裂铠 */],
		[probability:52, code:"DA210"/* 信徒披风 */],
		[probability:52, code:"WA109"/* 破山斧 */],
		[probability:52, code:"WC109"/* 兽之斧刃 */],
		[probability:52, code:"WH110"/* 轩辕巨锤 */],
		[probability:52, code:"WM110"/* 圣者杖 */],
		[probability:52, code:"WP110"/* 白银之枪 */],
		[probability:52, code:"WS111"/* 龙骨战弓 */],
		[probability:52, code:"WS211"/* 斩马刀 */],
		[probability:52, code:"WT110"/* 飞云标 */],
		[probability:52, code:"DB109"/* 大地靴 */],
		[probability:52, code:"DS109"/* 圣盾 */],
		[probability:52, code:"OM110"/* 蓝色星辰 */],
		[probability:52, code:"DG109"/* 神力护手 */],
		[probability:52, code:"OA209"/* 飞翼臂环 */],
		[probability:52, code:"OR111"/* 封印之戒 */],
		[probability:52, code:"OA111"/* 圣者之链 */],
		[probability:21, code:"DA111"/* 重装机铠 */],
		[probability:21, code:"DA211"/* 大法师袍 */],
		[probability:21, code:"WA110"/* 定神斧 */],
		[probability:21, code:"WC110"/* 九头刺蛇爪 */],
		[probability:21, code:"WH111"/* 赤冥之锤 */],
		[probability:21, code:"WM111"/* 王者杖 */],
		[probability:21, code:"WP111"/* 屠龙枪 */],
		[probability:21, code:"WS112"/* 人马之辉 */],
		[probability:21, code:"WS212"/* 嗜血屠魔剑 */],
		[probability:21, code:"WT111"/* 神标 */],
		[probability:21, code:"DB110"/* 地火战靴 */],
		[probability:21, code:"DS110"/* 宙斯盾 */],
		[probability:21, code:"OM111"/* 淬火乌晶 */],
		[probability:21, code:"DG110"/* 火云护手 */],
		[probability:21, code:"OA210"/* 百川流水臂环 */],
		[probability:21, code:"OR112"/* 王者戒指 */],
		[probability:21, code:"OA112"/* 魔龙之心 */],
		[probability:21, code:"OS107"/* 水晶石 */],
		[probability:21, code:"EC102"/* 回城卷 */],
		[probability:10, code:"DA112"/* 战神宝铠 */],
		[probability:10, code:"DA212"/* 红莲战袍 */],
		[probability:10, code:"WA111"/* 天阙斧 */],
		[probability:10, code:"WC111"/* 利维坦 */],
		[probability:10, code:"WH112"/* 碎星锤 */],
		[probability:10, code:"WM112"/* 审判之杖 */],
		[probability:10, code:"WP112"/* 傲天枪 */],
		[probability:10, code:"WS113"/* 猛犸巨弩 */],
		[probability:10, code:"WS213"/* 双截刃 */],
		[probability:10, code:"WT112"/* 鸩尾标 */],
		[probability:10, code:"DB111"/* 圣靴 */],
		[probability:10, code:"DS111"/* 苍穹之盾 */],
		[probability:10, code:"OM112"/* 菱晶石 */],
		[probability:10, code:"DG111"/* 黄铜护手 */],
		[probability:10, code:"OA211"/* 玄铁臂环 */],
		[probability:10, code:"OR113"/* 灵魂之戒 */],
		[probability:10, code:"OA113"/* 生命之链 */],
		[probability:10, code:"EC102"/* 回城卷 */],
		[probability:10, code:"GP110"/* 守护圣徒水晶 */],
		[probability:2, code:"DA113"/* 虎刹魔铠 */],
		[probability:2, code:"DA213"/* 幽绿之眼 */],
		[probability:2, code:"WA112"/* 奥丁斧 */],
		[probability:2, code:"WC112"/* 飞龙爪 */],
		[probability:2, code:"WH113"/* 破日锤 */],
		[probability:2, code:"WM113"/* 魔蜓杖 */],
		[probability:2, code:"WP113"/* 冥河战镰 */],
		[probability:2, code:"WS114"/* 爱神之翼 */],
		[probability:2, code:"WS214"/* 金刚伏魔剑 */],
		[probability:2, code:"WT113"/* 魔龙标 */],
		[probability:2, code:"DB112"/* 破棘之靴 */],
		[probability:2, code:"DS112"/* 暗黑盾 */],
		[probability:2, code:"OM113"/* 西法路 */],
		[probability:2, code:"DG112"/* 巨灵护手 */],
		[probability:2, code:"OA212"/* 紫焰臂环 */],
		[probability:2, code:"OR113"/* 灵魂之戒 */],
		[probability:2, code:"OA113"/* 生命之链 */],
		[probability:2, code:"OS107"/* 水晶石 */],
		[probability:2, code:"OS108"/* 虎翼石 */],
		[probability:2, code:"EC102"/* 回城卷 */],
		[probability:2, code:"GP113"/* 铁甲狂魔水晶 */]
	],
	drops_more:[
		[probability:40, code:"DA113"/* 虎刹魔铠 */]	]
]
