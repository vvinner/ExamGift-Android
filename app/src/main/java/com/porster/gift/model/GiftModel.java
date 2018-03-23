package com.porster.gift.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Porster on 17/2/28.
 */

public class GiftModel implements Serializable{
    public String title;
    /**内容*/
    public String content;
    /**题目ID*/
    public String id;
    /**正确答案*/
    public String rightAnswer;
    /**答案*/
    public String ansers;
    /**拼音*/
    public String pinyin;
    /**图片名称*/
    public String picName;

    /**关键字下标起始*/
    public int subIndexStart;
    /**关键字下标末尾*/
    public int subIndexEnd;

    /**是否已经回答过*/
    public boolean hasAnswer;

    /**答题结果true：答对；false：答错*/
    public boolean answerResult;

    /**错误次数,为0则移除*/
    public int errorCount;

    /**答案集合*/
    public List<AnswerModel> selectAnswer;


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof GiftModel){
            GiftModel giftModel= (GiftModel) o;
            return TextUtils.equals(giftModel.id,id);
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "GiftModel{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", rightAnswer='" + rightAnswer + '\'' +
                ", ansers='" + ansers + '\'' +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }
}
