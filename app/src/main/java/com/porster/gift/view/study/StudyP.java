package com.porster.gift.view.study;

import com.porster.gift.model.GiftModel;

import java.util.ArrayList;

/**
 * Created by Porster on 17/3/31.
 */

public interface StudyP {

    void handleQuestion(ArrayList<GiftModel> models);

    void gotoLastQuestion();

    void cehckNeedShowAdvert();
}
