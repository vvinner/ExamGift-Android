package com.porster.gift.view.study;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.porster.gift.R;
import com.porster.gift.core.BaseActivity;
import com.porster.gift.core.BaseFragment;
import com.porster.gift.core.DataManager;
import com.porster.gift.core.SessionData;
import com.porster.gift.model.AnswerModel;
import com.porster.gift.model.GiftModel;
import com.porster.gift.utils.ApiUtils;
import com.porster.gift.utils.IntentHelper;
import com.porster.gift.utils.LogCat;
import com.porster.gift.utils.Utils;
import com.porster.gift.view.FailFragment;
import com.porster.gift.view.StudyFragment;
import com.porster.gift.view.dev.DevToolsAct;
import com.porster.gift.widget.TitleBar;
import com.porster.gift.widget.XDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import cn.waps.AppConnect;
import cn.waps.AppListener;

/**
 * 练习
 * 按照题目数量创建对应个数的Fragment
 * 0.5%概率底部出现广告
 * Created by Porster on 17/3/8.
 */

public class StudyAct extends BaseActivity implements View.OnClickListener,StudyV{
    public TextView mTitle;
    ViewPager viewpager;
    public ArrayList<GiftModel> mGiftModels;
    private ArrayList<BaseFragment> mFragments;

    /**答错的题目*/
    private LinkedHashSet<GiftModel> mFailModels;

    public StudyPresenter mPresenter;

    /**是否允许滑动*/
    private boolean isNeedScroll;

    /**是否做过了题目*/
    private boolean mDataChanged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_study);
        initUI();
        initData();
    }

    public void initUI() {
        mPresenter=new StudyPresenter(mContext,this);

        TitleBar mTitleBar=addActionBar("");
        mTitle=mTitleBar.getTitle();
        mTitleBar.getRight("跳页").setOnClickListener(this);
        mTitleBar.getLeft().setOnClickListener(this);

        $(R.id.study_auto_answer,this);

        //创建ViewPager
        viewpager=$(R.id.viewpager);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                mPresenter.cehckNeedShowAdvert();
                mTitle.setText((position+1)+"/"+mGiftModels.size());

                //检查该页面是否可以滑到下一个页面：当前界面的hasAnswer=true
                checkHasScrollToNextPage(position);

                if(DevToolsAct.DEV_AUTO_ANSWER){
                    viewpager.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            devModel();
                        }
                    },200);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewpager.setOnTouchListener(new View.OnTouchListener() {
            //控制是否能滑动到下一页
            private float downX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isNeedScroll){
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX=event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(event.getX()>downX){
                            downX=event.getX();
                        }
                        if(event.getX()<downX){
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }
    public void initData(){
        showProgressDialog("");

        DataManager.getInstance().readListAsync(mContext, StudyFragment.CACHE_HISTORY_STUDY, new DataManager.OnReadListener() {
            @Override
            public void onSuccess(Object mlist) {

                ArrayList<GiftModel> list= (ArrayList<GiftModel>) mlist;

                dismissProgressDialog();
                boolean hasHistory=!Utils.isEmpty(list);
                if(!hasHistory){
                    LogCat.i("未找到历史答题记录,重新创建");
                    mGiftModels=new ArrayList<>();

                    for (GiftModel giftModel : DataManager.getInstance().getGiftModels()) {
                        GiftModel bean=new GiftModel();
                        bean.id=giftModel.id;
                        bean.ansers=giftModel.ansers;
                        bean.content=giftModel.content;
                        bean.picName=giftModel.picName;
                        bean.title=giftModel.title;

                        final List<AnswerModel> models=new ArrayList<>();
                        //解析出答案
                        for (String s : giftModel.ansers.split("\\|")) {
                            AnswerModel answerModel=new AnswerModel();
                            answerModel.title=s;
                            models.add(answerModel);
                        }
                        bean.selectAnswer=models;
                        bean.rightAnswer=models.get(0).title;


                        mGiftModels.add(bean);
                    }
                    //打乱题目顺序
                    Collections.shuffle(mGiftModels);

                }else{
                    mGiftModels=list;
                }

                mPresenter.handleQuestion(mGiftModels);
                //跳转到上次页面
                if(hasHistory){
                    mPresenter.gotoLastQuestion();
                }else{
                    mTitle.setText("1/"+mGiftModels.size());
                }
                //计算答题统计
                updata(null);

                startDevModel(DevToolsAct.DEV_AUTO_ANSWER);

                //加载错题
                ArrayList<GiftModel> mLocFailModels = null;
                try {
                    mLocFailModels= (ArrayList<GiftModel>) DataManager.getInstance().readList(mContext, FailFragment.CACHE_FAIL_DATA);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                if(mLocFailModels==null){
                    mFailModels=new LinkedHashSet<>();
                }else{
                    mFailModels=new LinkedHashSet<GiftModel>(mLocFailModels);
                }
                LogCat.i("错题记录="+mFailModels.size()+"条");
            }
        });
    }


    /**
     * 检查该页面是否可以滑到下一个页面：当前界面的hasAnswer=true
     * @param position  当前页面
     */
    public void checkHasScrollToNextPage(Integer position){
        if(position==null){
            position=viewpager.getCurrentItem();
        }
        if(position<mGiftModels.size()){
            position=Math.max(position,0);
            isNeedScroll = mGiftModels.get(position).hasAnswer;
            LogCat.i("第"+(position+1)+"已经回答过可以滑动"+isNeedScroll);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.study_auto_answer:
                Button study_auto_answer= (Button) v;
                if(study_auto_answer.getText().toString().contains("停止")){
                    mAutoAnswer=false;
                    study_auto_answer.setText("开始自动答题");
                }else{
                    mAutoAnswer=true;
                    study_auto_answer.setText("停止自动答题");
                    startDevModel(DevToolsAct.DEV_AUTO_ANSWER);
                }
                break;
            case R.id.title_bar_left_txt:
                onBackPressed();
                break;
            case R.id.title_bar_right_txt:
                SkipPageFragment skipPageFragment=new SkipPageFragment();
                Bundle k=new Bundle();
                k.putInt(IntentHelper.KEY1,viewpager.getCurrentItem()+1);

                //当前已答题的进度
                int mNowAnswerCount = 0;
                for (int i = 0; i < mGiftModels.size(); i++) {
                    GiftModel bean=mGiftModels.get(i);
                    if(!bean.hasAnswer){//发现答过的题,则停止
                        mNowAnswerCount=i+1;
                        break;
                    }
                }
//                mNowAnswerCount=1163;
                k.putInt(IntentHelper.KEY2,mNowAnswerCount);
                skipPageFragment.setArguments(k);
                skipPageFragment.show(getFragmentManager(),"");
                break;
        }
    }

    @Override
    public void showQuestionFragment(final ArrayList<BaseFragment> baseFragments) {
        this.mFragments=baseFragments;
        viewpager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return baseFragments.get(position);
            }

            @Override
            public int getCount() {
                return baseFragments.size();
            }
        });

    }

    @Override
    public void gotoLastQuestion(int index) {
        viewpager.setCurrentItem(index);
        mTitle.setText((index+1)+"/"+mGiftModels.size());
        checkHasScrollToNextPage(index);
    }
    @Override
    public void switchAdvert(boolean show) {

        String value=AppConnect.getInstance(this).getConfig("showStudyAD","false");

        if(TextUtils.equals(value,"true")&&show){
            MobclickAgent.onEvent(mContext,"GiftShowAD");
            AppConnect.getInstance(this).showPopAd(this,new AppListener(){
                @Override
                public void onPopClose() {
                }
                @Override
                public void onPopNoData() {
                }
            });
        }
    }

    /**
     * 更新答题统计
     * @param model
     */
    public void updata(GiftModel model){
        TextView total_ok=$(R.id.total_ok);
        TextView total_no=$(R.id.total_no);
        TextView total_persent=$(R.id.total_persent);

        int rightCount=0;
        int failCount=0;
        if (model != null) {
            mDataChanged=true;
            try {
                rightCount=Integer.parseInt(total_ok.getText().toString());
                failCount=Integer.parseInt(total_no.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            Boolean answerResult=model.answerResult;
            if(answerResult){
                rightCount++;
            }else{
                failCount++;
            }
            //计入错题本
            if(!model.answerResult){
                mFailModels.add(model);
            }
        }else{
            //计算出答题统计
            for (GiftModel giftModel : mGiftModels) {
                if(giftModel.hasAnswer){
                    if(giftModel.answerResult){
                        rightCount++;
                    }else{
                        failCount++;
                    }
                }else{
                    break;
                }
            }
        }
        if((rightCount+failCount)==0){
            total_persent.setText("0%");
        }else{
            float persent=rightCount*1.0f/(rightCount+failCount)*100f;
            String persentStr=(persent+"");
            int index=persentStr.indexOf(".");
            if(index!=-1){
                persentStr=persentStr.substring(0,index);
            }
            total_persent.setText(persentStr+"%");
        }
        total_ok.setText(rightCount+"");
        total_no.setText(failCount+"");

        //答完了
        if(rightCount+failCount==mGiftModels.size()){
            XDialog.showRadioDialog(mContext, "你完了成所有题目\n正确" + rightCount + " 错误" + failCount + "\n正确率" + total_persent.getText().toString(), new XDialog.DialogClickListener() {
                @Override
                public void confirm() {
                    //清空记录
                    onBackClick(0,new ArrayList<GiftModel>());
                }
                @Override
                public void cancel() {

                }
            });
        }

    }
    public void next(long millis){
        viewpager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewpager.setCurrentItem(viewpager.getCurrentItem()+1,true);
            }
        },millis);
    }
    private void onBackClick(final int nowPage, final ArrayList<GiftModel> nowRecord){
        if(!mDataChanged){
            finish();
            return;
        }
        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {
            @Override
            protected void onPreExecute() {
                SessionData.setObject(mContext, StudyFragment.SP_STUDY_LAST_CURRENT,nowPage);
                showProgressDialog(nowPage==0?"":"保存进度...");
            }

            @Override
            protected Object doInBackground(Object... params) {
                for (GiftModel failModel : mFailModels) {
                    LogCat.i("错题记录="+failModel);
                }
                DataManager.getInstance().saveList(mContext,FailFragment.CACHE_FAIL_DATA,new ArrayList<>(mFailModels));

                DataManager.getInstance().saveList(mContext,StudyFragment.CACHE_HISTORY_STUDY,nowRecord);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                dismissProgressDialog();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        onBackClick(viewpager.getCurrentItem(),mGiftModels);
    }

    private boolean mAutoAnswer;
    @Override
    public void devModel() {
        if(!mAutoAnswer){
            return;
        }
        QuestionFragment fragment= (QuestionFragment) mFragments.get(viewpager.getCurrentItem());
        fragment.devModel();
    }

    @Override
    public void startDevModel(boolean needStart) {
        super.startDevModel(needStart);
        if(needStart){
            mAutoAnswer=true;
            VISIBLE($(R.id.study_auto_answer));
        }
    }
}
