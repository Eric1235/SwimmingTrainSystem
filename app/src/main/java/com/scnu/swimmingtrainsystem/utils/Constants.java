package com.scnu.swimmingtrainsystem.utils;

/**
 * 保存系统所需常量类
 * 
 * @author LittleByte
 * 修改：李新坤
 */
public class Constants {

	public static String HOSTURL = "http://104.160.34.110:8080/SWIMYUE33/httpPost.action?action_flag=";

	/**
	 * 登录app
	 */
	public static String LOGIN_URL = HOSTURL + "login";

	/**
	 * 	注册
	 */
	public static String REGIST = HOSTURL + "regist";

	/**
	 * 添加运动员
	 */
	public static String ADD_ATHLETE_URL = HOSTURL + "addAthlete";

	/**
	 * 获取运动员列表
	 */
	public static String GET_ATHLETE_LIST = HOSTURL + "getAthletes";

	/**
	 * 修改运动员
	 */
	public static String MODIFY_ATHLETE_URL = HOSTURL + "modifyAthlete";

	/**
	 * 删除运动员
	 */
	public static String DELETE_ATHLETE_URL = HOSTURL + "deleteAthlete";

	/**
	 * 添加普通计时成绩
	 */
	public static String ADD_SCORE = HOSTURL + "addScores";


	/**
	 * 上传其他成绩
	 */
	public static String ADD_OTHER_SCORE = HOSTURL + "addOtherScores";

	/**
	 * 获取成绩
	 */
	public static String GET_SCORE = HOSTURL + "getScores";

	/**
	 * 获取plan
	 */
	public static String GET_PLAN = HOSTURL + "getPlanMsg";


	/**
	 * 修改密码
	 */
	public static String MODIFY_PASSWORD = HOSTURL + "modifyPass";

	/**
	 * 获取密码
	 */
	public static String GET_PASSWORD = HOSTURL + "getPassword";

	/**
	 * 获取全部成绩的日期
	 */
	public static String GET_SCORE_DATE_LIST = HOSTURL + "getScoreDateList";


	/**
	 * 记录登录后的用户id
	 */
	public static final String USER_ID = "uid";

	/**
	 * 登录成功
	 */
	public static final String LOGIN_SUCCEED = "loginsucceed";

	public static final String WATCHISRESET = "WATCHISRESET";

	/**
	 * 运动员，泳姿哈希表
	 */
	public static String ATHLETE_STROKE_MAP = "stroke";

	/**
	 * 超时时间设置
	 */
	public static final int SOCKET_TIMEOUT = 5000;

	/**
	 * 成绩类型 1:普通成绩
	 */
	public static final int NORMALSCORE = 1;
	/**
	 * 成绩类型 2:三次计频成绩
	 */
	public static final int FrequenceSCORE = 0;
	/**
	 * 成绩类型 3:冲刺成绩
	 */
	public static final int SPRINTSCORE = 1;

	/**
	 * SharePreference所用的loginInfo的关键字
	 */
	public static final String LOGININFO = "loginInfo";

	/**
	 * 保存是第几次计时，提醒用户是第几次计时之中
	 */
	public static final String CURRENT_SWIM_TIME = "current";

	/**
	 * 保存所选的计划ID
	 */
	public static final String PLAN_ID = "planID";

	/**
	 * 保存开始计时的日期
	 */
	public static final String TEST_DATE = "testDate";

	/**
	 * 保存手动匹配计时按名次排行的运动员名字,方便除第一趟计时外不用再次拖动运动员进行排行
	 */
	public static final String DRAG_NAME_LIST = "dragList";

	/**
	 * 保存排行的运动员id
	 */
	public static final String DRAG_NAME_LIST_IDS = "DRAG_NAME_IDS";

	/**
	 * 保存当前登录的用户id
	 */
	public static final String CURRENT_USER_ID = "CurrentUser";

	/**
	 * 保存当前是否可以连接服务器的状态
	 */
	public static final String IS_CONNECT_SERVER = "isConnect";

	/**
	 * 上次登陆的user_id
	 */
	public static final String LAST_LOGIN_USER_ID = "lastLoginUser";

	public static final String COMPLETE_NUMBER = "completeNumber";

	/**
	 * 该用户是否是第一次登陆应用
	 */
	public static final String IS_THIS_USER_FIRST_LOGIN = "isThisUserFirstLogin";

	/**
	 * Log信息所需tag--com.scnu.swimmingtrainingsystem
	 */
	public static final String TAG = "com.scnu.swimmingtrainingsystem";

	/**
	 * 取消
	 */
	public static final String CANCLE_STRING = "取消";

	/**
	 * 确定
	 */
	public static final String OK_STRING = "确定";

	/**
	 * 添加成功
	 */
	public static final String ADD_SUCCESS_STRING = "添加成功";

	public static final String SELECTED_POOL = "selectedPool";
	
	public static final String SELECTED_STROKE = "selectedStroke";

	public static final String SWIM_DISTANCE = "swimDistance";

	public static final String FISRTOPENATHLETE = "fisrtOpenAthlete";

	public static final String FISRTOPENPLAN = "fisrtOpenPlan";

	public static final String FISRTSTARTTIMING = "fisrtStartTiming";

	public static final String CURRENT_DISTANCE = "currentDistance";

	public static final String SCORESJSON = "scoresJson";

	public static final String ATHLETEJSON = "athleteJson";

	public static final String ATHLETEIDJSON = "ATHLETEIDJSON";

	/**
	 * 使用说明标题
	 */
	public static final String[] TITLES = new String[] {"1、运动员管理", "2、计时器功能", "3、小功能模块", "4、成绩查询模块" };

	/**
	 * 使用说明内容
	 */
	public static final String[][] CONTENTS = new String[][] {
			{ "•首次使用本软件，先要进入运动员管理界面，如果已经服务器已经有运动员，就会自动把运动员同步到本地<br>" +
					"•运动员管理可以增加运动员，可以修改运动员信息，也可以删除运动员。要慎重使用删除运动员这个功能，运动员一旦被删除" +
					"，是不能找回" },
			{ "•要使用本模块，首先就需要录入运动员信息，然后在计时器模块中，在每次计时之前需要选择运动员，并且需要设置泳池大小，游泳的目标距离、游泳计时的间隔距离以及本轮"
					+ "计时的备注，计时是否间歇，方便区分每次计时成绩。 <br>•在计时页面中，点击计时表盘即可开始，再次点击之后会记录下当前时间 <br>•本趟计时完"
					+ "成之后会跳转到调整页面，对运动员顺序进行调整和根据自己所需删除一些计时时间和运动员，也可直接跳过调整在计完本轮所有成绩时再做总的调整。 "
					+ "<br>•对成绩向左滑或向右滑可以删除时间，对运动员向上或者向下拖动可以移动运动员顺序，而长按成绩可弹出复制该成绩的对话框<br>•点击右上角的恢复按钮可恢复最初数据" },
			{ "•本模块有三次计频和冲刺计时功能，可自由进行切换，并且支持将结果保存并上传到服务器<br>•对成绩向左滑或向右滑可以删除时间，对运动员向上或者向下拖动可以移动运动员顺序，而长按成绩可弹出复制该成绩的对话框" },
			{ "•查询成绩的时候，所有的条件都可以为‘所有’选项<br>•点击联网查询，会返回符合条件的成绩的日期列表<br>•点击列表就可以查询到符合条件的成绩" } };

	public static final String INTERVAL = "interval";

}
