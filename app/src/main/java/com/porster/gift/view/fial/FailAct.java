package com.porster.gift.view.fial;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;

import com.porster.gift.R;
import com.porster.gift.core.DataManager;
import com.porster.gift.core.SessionData;
import com.porster.gift.model.AnswerModel;
import com.porster.gift.model.GiftModel;
import com.porster.gift.utils.IntentHelper;
import com.porster.gift.utils.LogCat;
import com.porster.gift.utils.Utils;
import com.porster.gift.view.FailFragment;
import com.porster.gift.view.setting.HighSetAct;
import com.porster.gift.view.study.StudyAct;

import java.util.ArrayList;
import java.util.List;

/**
 * 错题
 * Created by Porster on 17/5/26.
 */

public class FailAct extends StudyAct{

    /**答错次数*/
    int fail_count;
    /**待移除队列*/
    private List<GiftModel> mPrepareRemove=new ArrayList<>();


    @Override
    public void initUI() {
        super.initUI();

        fail_count= (int) SessionData.getObject(mContext, HighSetAct.SP_REMOVE_COUNT,2);

        GONE($(R.id.study_total_record_layout));

        GONE($(R.id.title_bar_right_txt));

        VISIBLE(setText(R.id.fail_count_tips,"答对"+fail_count+"次后，将移除错题，次数可以在高级设置中设定"));
    }

    @Override
    public void initData() {
        mGiftModels= (ArrayList<GiftModel>) getIntent().getSerializableExtra(IntentHelper.KEY1);
        for (GiftModel giftModel : mGiftModels) {
            giftModel.hasAnswer=false;
            if(giftModel.errorCount==0){//有错误次数的不再重新计数
                giftModel.errorCount=fail_count;
            }
            giftModel.answerResult=false;
            if(!Utils.isEmpty(giftModel.selectAnswer)){
                for (AnswerModel answerModel : giftModel.selectAnswer) {
                    answerModel.isRightAnswer=false;
                    answerModel.isSelected=false;
                }
            }
        }

        mPresenter.handleQuestion(mGiftModels);
        mTitle.setText("1/"+mGiftModels.size());
    }



    @Override
    public void updata(GiftModel model) {
        //答对一次，则errorCount-1
        if(model.answerResult){
            model.errorCount--;
            if(model.errorCount==0){
                //移除错题本
                mPrepareRemove.add(model);
            }
        }
    }

    @Override
    public void onBackPressed() {
        for (GiftModel giftModel : mGiftModels) {
            LogCat.i(giftModel.errorCount+" "+giftModel);
        }
        AsyncTaskCompat.executeParallel(new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                LogCat.w("准备移除"+mPrepareRemove.size()+"道 ，总:"+mGiftModels.size()+"道");
                if(mPrepareRemove.size()>0){//移除已经回答的问题
                    for (int i = mGiftModels.size() - 1; i >= 0; i--) {

                        GiftModel mNow=mGiftModels.get(i);

                        for (GiftModel giftModel : mPrepareRemove) {
                            //找到移除
                            if(TextUtils.equals(mNow.id,giftModel.id)){
                                mGiftModels.remove(i);
                            }
                        }
                    }
                }
                LogCat.w("移除结束，剩余"+mGiftModels.size()+"道");

                DataManager.getInstance().saveList(mContext, FailFragment.CACHE_FAIL_DATA,mGiftModels);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            protected void onPreExecute() {
                showProgressDialog("");
            }
        });

    }
}
