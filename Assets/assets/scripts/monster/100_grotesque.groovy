monster = [
	ID:'100_grotesque',
	Name:'亚特兰斯巨人',
	Clazz:0,// 1 BOSS
	Brood:0x00,// 0 Normal; 0x90(144) UNDEAD; 0x91 MUTANT; 0x92 DEMON; 0x93 MECHANIC;
	// Common Status
	Level:100,
	ActiveHour:0,
	RespawnGroup:[1, 1],

	Attributes:[
		Life:4860,
		// Attack Status
		Atk:[100, 120],
		AtkSpeed:7,
		Range:80,
		Rating:1300,

		// Defence Status
		Flee:740,
		Absorb:20,
		Block:5,
		DamageStunPers:100,

		// Resistance
		Earth:100,
		Fire:40,
		Ice:40,// Water
		Lighting:20,// Wind
		Poison:0,
	],

	// AI
	AI:[
		Nature:0x82,// 0x80 NATURAL; 0x81 GOOD; 0x82 EVIL
		IQ:9,
		Real_Sight:370,
		Talks:[],

		// Move Behavier
		Move_Speed:1,
		MoveRange:64,

		// Skill Behavier
		SpAttackPercetage:15,
		SkillDamage:[130, 150],
		SkillDistance:80,
		SkillRange:0,
		SkillRating:18,
		SkillCurse:0,

		// Heal Behavier
		PotionPercent:0,
		PotionCount:0,
	],

	Looks:[
		ClassCode:11,
		ArrowPosi:[-10, 150],
		ModelSize:0.0,
		UseEventModel:false,
		SizeLevel:3,
		Model:'char/monster/grotesque/grotesque.INI',
		Sound:0x00001520,
	],

	// Drops
	AllSeeItem:false,
	Exp:59000,
	Quantity:1,
	drops:[
		[probability:1510, code:"NULL"/* Drops nothing */],
		[probability:3200, code:"GG101", value:[330, 420]/* Gold */],
		[probability:1266, code:"PL104"/* 顶级恢复生命药水 */],
		[probability:1266, code:"PS104"/* 顶级恢复耐力药水 */],
		[probability:1266, code:"PM104"/* 顶级恢复魔法药水 */],
		[probability:47, code:"DA113"/* 虎刹魔铠 */],
		[probability:47, code:"DA213"/* 幽绿之眼 */],
		[probability:47, code:"WA111"/* 天阙斧 */],
		[probability:47, code:"WC111"/* 利维坦 */],
		[probability:47, code:"WH112"/* 碎星锤 */],
		[probability:47, code:"WM112"/* 审判之杖 */],
		[probability:47, code:"WP112"/* 傲天枪 */],
		[probability:47, code:"WS113"/* 猛犸巨弩 */],
		[probability:47, code:"WS214"/* 金刚伏魔剑 */],
		[probability:47, code:"WT112"/* 鸩尾标 */],
		[probability:47, code:"DB111"/* 圣靴 */],
		[probability:47, code:"DS111"/* 苍穹之盾 */],
		[probability:47, code:"OM112"/* 菱晶石 */],
		[probability:47, code:"DG111"/* 黄铜护手 */],
		[probability:47, code:"OA211"/* 玄铁臂环 */],
		[probability:47, code:"OR113"/* 灵魂之戒 */],
		[probability:47, code:"OA113"/* 生命之链 */],
		[probability:20, code:"DA114"/* 星晨宝铠 */],
		[probability:20, code:"DA214"/* 绯红之眼 */],
		[probability:20, code:"WA112"/* 奥丁斧 */],
		[probability:20, code:"WC112"/* 飞龙爪 */],
		[probability:20, code:"WH113"/* 破日锤 */],
		[probability:20, code:"WM113"/* 魔蜓杖 */],
		[probability:20, code:"WP113"/* 冥河战镰 */],
		[probability:20, code:"WS114"/* 爱神之翼 */],
		[probability:20, code:"WS215"/* 诅咒之剑 */],
		[probability:20, code:"WT113"/* 魔龙标 */],
		[probability:20, code:"DB112"/* 破棘之靴 */],
		[probability:20, code:"DS112"/* 暗黑盾 */],
		[probability:20, code:"OM113"/* 西法路 */],
		[probability:20, code:"DG112"/* 巨灵护手 */],
		[probability:20, code:"OA212"/* 紫焰臂环 */],
		[probability:20, code:"OR114"/* 帝王之戒 */],
		[probability:20, code:"OA114"/* 神之庇护 */],
		[probability:20, code:"OS107"/* 水晶石 */],
		[probability:20, code:"OS107"/* 水晶石 */],
		[probability:20, code:"GP109"/* 神秘水晶 */],
		[probability:9, code:"DA115"/* 泰坦战铠 */],
		[probability:9, code:"DA215"/* 文章法袍 */],
		[probability:9, code:"WA113"/* 蝶花霹雳斧 */],
		[probability:9, code:"WC113"/* 魔星爪 */],
		[probability:9, code:"WH114"/* 鬼眼锤 */],
		[probability:9, code:"WM114"/* 混沌之杖 */],
		[probability:9, code:"WP114"/* 龙翼枪 */],
		[probability:9, code:"WS115"/* 精灵之翼 */],
		[probability:9, code:"WS216"/* 破军 */],
		[probability:9, code:"WT114"/* 追月标 */],
		[probability:9, code:"DB113"/* 遁地靴 */],
		[probability:9, code:"DS113"/* 龙纹盾 */],
		[probability:9, code:"OM114"/* 堕天 */],
		[probability:9, code:"DG113"/* 鲲鹏护手 */],
		[probability:9, code:"OA213"/* 璇彩臂环 */],
		[probability:9, code:"OR115"/* 守护之戒 */],
		[probability:9, code:"OA115"/* 暗印护符 */],
		[probability:9, code:"OS107"/* 水晶石 */],
		[probability:9, code:"OS108"/* 虎翼石 */],
		[probability:9, code:"GP109"/* 神秘水晶 */],
		[probability:9, code:"GP110"/* 守护圣徒水晶 */],
		[probability:2, code:"DA116"/* 暗黑铠 */],
		[probability:2, code:"DA216"/* 祝福法袍 */],
		[probability:2, code:"WA114"/* 战神之刃 */],
		[probability:2, code:"WC114"/* 天狼爪 */],
		[probability:2, code:"WH115"/* 雷公槌 */],
		[probability:2, code:"WM115"/* 亡灵刺 */],
		[probability:2, code:"WP115"/* 狂暴之枪 */],
		[probability:2, code:"WS116"/* 血精灵 */],
		[probability:2, code:"WS217"/* 鬼切 */],
		[probability:2, code:"WT115"/* 惊鸿 */],
		[probability:2, code:"DB114"/* 鹏翅之靴 */],
		[probability:2, code:"DS114"/* 泰坦之盾 */],
		[probability:2, code:"OM115"/* 炫彩水晶 */],
		[probability:2, code:"DG114"/* 金刚护手 */],
		[probability:2, code:"OA214"/* 金刚臂环 */],
		[probability:2, code:"OR115"/* 守护之戒 */],
		[probability:2, code:"OA115"/* 暗印护符 */],
		[probability:2, code:"OS108"/* 虎翼石 */],
		[probability:2, code:"OS108"/* 虎翼石 */],
		[probability:2, code:"GP109"/* 神秘水晶 */],
		[probability:2, code:"GP110"/* 守护圣徒水晶 */],
		[probability:2, code:"SE101"/* 造化石 */],
		[probability:0, code:"DA117"/* 远古圣铠 */],
		[probability:0, code:"DA217"/* 天使法袍 */],
		[probability:0, code:"WA115"/* 泰坦斧 */],
		[probability:0, code:"WC115"/* 魔玉爪 */],
		[probability:0, code:"WH116"/* 轰天锤 */],
		[probability:0, code:"WM116"/* 诸神的黄昏 */],
		[probability:0, code:"WP116"/* 虹月 */],
		[probability:0, code:"WS117"/* 破鹫 */],
		[probability:0, code:"WS218"/* 天裂 */],
		[probability:0, code:"WT116"/* 玛雅神标 */],
		[probability:0, code:"DB115"/* 时空之靴 */],
		[probability:0, code:"DS115"/* 亢龙之盾 */],
		[probability:0, code:"OM116"/* 龙之护身 */],
		[probability:0, code:"DG115"/* 赤龙护手 */],
		[probability:0, code:"OA215"/* 赤龙臂环 */],
		[probability:0, code:"OR116"/* 雅典娜之吻 */],
		[probability:0, code:"OA116"/* 苍穹之链 */],
		[probability:0, code:"OS108"/* 虎翼石 */],
		[probability:0, code:"OS108"/* 虎翼石 */],
		[probability:0, code:"GP109"/* 神秘水晶 */],
		[probability:0, code:"GP110"/* 守护圣徒水晶 */],
		[probability:0, code:"SE101"/* 造化石 */]
	],
	drops_more:[
	]
]