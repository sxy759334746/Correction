package com.luckyxmobile.correction.util;

public class ConstantsUtil {

    //SharedPreferences
    public static final String TABLE_SHARED_CORRECTION = "correction_sharedpre_table";
    public static final String TABLE_SHARED_IS_NEWEST_ORDER = "is_newest_order";
    public static final String TABLE_FROM_BOOK_ID = "from_book_id";
    public static final String TABLE_PRINT_PAGE = "print_page";
    public static final String TABLE_SHOW_SMEAR = "show_smear";
    public static final String TABLE_FULL_SCREEN = "is_full_screen";
    public static final String TABLE_SHOW_TAG = "is_show_tag";
    public static final String TABLE_VIEW_SMEAR_BY = "show_smear_by";
    public static final String TABLE_PRINT_SMEAR_CONTENT = "print_smear_content";
    public static final String IF_FROM_FAVORITE = "if_from_favorite";


    //多少秒点击一次 默认1秒
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    public static final int MIN_CLICK_TOW_TIME = 500;

    public static final int REQUEST_PERMISSION = 100;
    public static final int REQUEST_CODE_TAKE_PHOTO = 200;
    public static final int REQUEST_CODE_SELECT_ALBUM = 300;
    public static final int REQUEST_CODE = 400;

    //intent参数
    public static final String WHETHER_FROM_ALBUM = "whether_from_album";
    public static final String WHICH_IMAGE = "which_image";
    public static final String WHICH_ACTIVITY = "which_activity";
    public static final String TOPIC_ID = "topic_id";
    public static final String TOPIC_POSITION = "topic_position";
    public static final String IMAGE_POSITION = "image_position";
    public static final String BOOK_ID = "book_id";
    public static final String WHETHER_EDIT_PHOTO = "whether_edit_photo";
    public static final String TOOLBAR_NAME = "toolbar_name";
    public static final String IMAGE_PATH = "image_path";
    public static final String IS_TOPIC = "is_topic";

    //图片类型
    public static final  String IMAGE_BOOK_COVER = "image_book_cover";
    public static final  String IMAGE_ORIGINAL = "image_original";
    public static final  String IMAGE_RIGHT = "image_right";
    public static final  String IMAGE_ERROR = "image_error";
    public static final  String IMAGE_POINT = "image_point";
    public static final  String IMAGE_REASON = "image_reason";

    //涂抹工具类型
    public static final  String PAINT_RIGHT = "paint_right";
    public static final  String PAINT_ERROR = "paint_error";
    public static final  String PAINT_POINT = "paint_point";
    public static final  String PAINT_REASON = "paint_reason";
    public static final  String PAINT_ERASE = "paint_erase";
    public static final  String PAINT_WHITE_OUT = "paint_white_out";

    //对比度
    public static final String CONTRAST_RADIO_WEAK = "contrast_radio_weak";
    public static final String CONTRAST_RADIO_COMMON = "contrast_radio_common";
    public static final String CONTRAST_RADIO_STRONG = "contrast_radio_strong";

    //橡皮擦宽度
    public static final int ERASE_THIN = 60, ERASE_MEDIUM = 80, ERASE_THICK = 100;

    //画笔宽度
    public static final int PAINT_THIN = 40, PAINT_MEDIUM = 60, PAINT_THICK = 80;

}
