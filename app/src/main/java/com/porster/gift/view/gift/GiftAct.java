package com.porster.gift.view.gift;

import com.porster.gift.view.study.StudyAct;

/**
 * 科举
 * 随机抽取20道题目
 * 计时，每答一道题目增加时间量(时间量=2个考官之间的最短距离时间)
 * 天王令、地王令各使用一次
 * Created by Porster on 17/5/26.
 */

public class GiftAct extends StudyAct{



    @Override
    public void initUI() {
        super.initUI();

    }

    @Override
    public void initData() {

        mPresenter.handleQuestion(mGiftModels);
        mTitle.setText("1/"+mGiftModels.size());
    }




    @Override
    public void onBackPressed() {

    }
}
