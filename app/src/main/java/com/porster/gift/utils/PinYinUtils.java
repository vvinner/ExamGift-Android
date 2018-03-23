package com.porster.gift.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音
 * Created by Porster on 17/3/2.
 */

public class PinYinUtils {
        /**
         * 获取汉字串拼音首字母，英文字符不变
         * @param chinese 汉字串
         * @return 汉语拼音首字母
         */
        public static String getFirstSpell(String chinese) {
            StringBuilder pybf = new StringBuilder();
            char[] arr = chinese.toCharArray();

            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            for (char anArr : arr) {
                if (anArr > 128) {
                    try {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(anArr, defaultFormat);
                        if (temp != null) {
                            pybf.append(temp[0].charAt(0));
                        }
                        else{
                            pybf.append("_");
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    pybf.append(anArr);
                }
            }
//            return pybf.toString().replaceAll("\\W", "").trim();
            return pybf.toString().trim();
        }

//        /**
//         * 获取汉字串拼音，英文字符不变
//         * @param chinese 汉字串
//         * @return 汉语拼音
//         */
//        public static String getFullSpell(String chinese) {
//            StringBuilder pybf = new StringBuilder();
//            char[] arr = chinese.toCharArray();
//            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//            for (int i = 0; i < arr.length; i++) {
//                if (arr[i] > 128) {
//                    try {
//                        pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);
//                    } catch (BadHanyuPinyinOutputFormatCombination e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    pybf.append(arr[i]);
//                }
//            }
//            return pybf.toString();
//        }
}
