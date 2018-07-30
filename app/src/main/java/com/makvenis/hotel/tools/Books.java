package com.makvenis.hotel.tools;

/**
 * 对应数据库的books编号
 */

public class Books {

    /* 获取餐桌信息 */
    public static final String PATH_NUM_BOOK="book=6";

    /* 修改餐桌状态 */
    public static final String PATH_ZHUANGTAI_BOOK="7";

    /* 修改用户头像 */
    public static final String PATH_UPLOAD_USER_PHOTO_BOOK="8";
    /* 用户结账买单清单 */
    public static final String PATH_PAY_BOOK="9";
    /* 更新远程数据库的android_commodity状态=0，表示该用户已经付款 */
    public static final String PATH_UPLOAD_STATE_BOOK="10";
    /* 修改座位表的状态为带清理状态=2 表示待清理桌面 */
    public static final String PATH_UPLOAD_PATHNUM_STATE_BOOK="11";
    /* 获取待结账的桌位 */
    public static final String PATH_SELECT_WAITING_CASH_BOOK="12";
    /*  */
    public static final String PATH_SELECT_INFORMATION_BOOK="13";
    /* 查询当前文章的评论信息 */
    public static final String PATH_SELECT_EVALUATION_BOOK="14";

}
