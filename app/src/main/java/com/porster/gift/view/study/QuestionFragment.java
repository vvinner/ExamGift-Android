package com.porster.gift.view.study;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.porster.gift.R;
import com.porster.gift.core.BaseActivity;
import com.porster.gift.core.BaseFragment;
import com.porster.gift.model.AnswerModel;
import com.porster.gift.model.GiftModel;
import com.porster.gift.utils.ApiUtils;
import com.porster.gift.utils.IntentHelper;
import com.porster.gift.utils.LogCat;
import com.porster.gift.utils.ViewHolder;
import com.porster.gift.utils.ViewUtil;
import com.porster.gift.view.dev.DevToolsAct;
import com.porster.gift.widget.recycler.BaseAdapter;
import com.porster.gift.widget.recycler.OnListItemPartClickListener;
import com.porster.gift.widget.recycler.RecyclerSpaceDivider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Porster on 17/3/31.
 */

public class QuestionFragment extends BaseFragment{
    private RecyclerView mRecycler;
    private SelectAdapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(mainView==null){
            mainView=inflater.inflate(R.layout.fragment_question,container,false);

            mRecycler=$(R.id.q_select);
            mRecycler.setLayoutManager(new GridLayoutManager(mContext,2));
            mRecycler.addItemDecoration(new RecyclerSpaceDivider(ViewUtil.dip2px(mContext,10),2));

            mAdapter=new SelectAdapter(mContext);
            mRecycler.setAdapter(mAdapter);

            mGiftModel= (GiftModel) getArguments().getSerializable(IntentHelper.KEY1);
            initUI();
        }
        return mainView;
    }

    @Override
    public void onDestroyView() {
        if(mainView!=null){
            ((ViewGroup)mainView.getParent()).removeView(mainView);
        }
        super.onDestroyView();
    }

    private GiftModel mGiftModel;
    public void initUI() {
        assert mGiftModel != null;
        setText(R.id.q_title,mGiftModel.id+"、"+mGiftModel.content);

        if(!TextUtils.isEmpty(mGiftModel.picName)){
            loadImg();
        }
        final List<AnswerModel> models=mGiftModel.selectAnswer;
        Collections.shuffle(models);

        mAdapter.clear();
        mAdapter.addItems(models);

        mAdapter.setItemClick(new OnListItemPartClickListener() {
            @Override
            public void onListItemPartClick(View view, Object obj, int state) {
                AnswerModel bean= (AnswerModel) obj;

                mGiftModel.hasAnswer=true;

                bean.isSelected=true;

                mGiftModel.answerResult=TextUtils.equals(mGiftModel.rightAnswer,bean.title);

                if(!mGiftModel.answerResult){
                    ((BaseActivity) getActivity()).showToast("回答错误");

                    if(getActivity() instanceof StudyAct){
                        ((StudyAct) getActivity()).checkHasScrollToNextPage(null);
                    }

                }else{
                    if(getActivity() instanceof StudyAct){
                        ((StudyAct) getActivity()).next(DevToolsAct.DEV_AUTO_ANSWER?100:500);
                    }
                }
                //回答完后,标记处正确答案
                for (AnswerModel model : mAdapter.getList()) {
                    if(TextUtils.equals(model.title,mGiftModel.rightAnswer)){
                        LogCat.i(model.title+"是正确答案");
                        model.isRightAnswer=true;
                    }
                }

                mAdapter.notifyDataSetChanged();

                if(getActivity() instanceof StudyAct){
                    ((StudyAct) getActivity()).updata(mGiftModel);
                }
            }
        });

    }
    private void loadImg(){
        LogCat.i("图标编号:"+mGiftModel.picName);
        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    return BitmapFactory.decodeStream(getActivity().getAssets().open("kj/"+mGiftModel.picName+".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Bitmap bmp= (Bitmap) o;
                if(bmp!=null){
                    ImageView q_img=$(R.id.q_img);
                    if(bmp.getWidth()>200){//针对图片
                        int imgW=ViewUtil.getScreenWidth(mContext)/2;
                        q_img.getLayoutParams().width= imgW;

                        float hw=Float.parseFloat(bmp.getHeight()+"")/bmp.getWidth();
                        q_img.getLayoutParams().height= (int) (imgW*hw);
                    }else if(bmp.getWidth()<50){//针对表情包子
                        int wh=ViewUtil.dip2px(mContext,30);
                        q_img.getLayoutParams().width=wh;
                        q_img.getLayoutParams().height=wh;
                    }
                    q_img.setImageBitmap(bmp);
                }
            }
        });
    }
    private class SelectAdapter extends BaseAdapter<AnswerModel>{

        private int width;

         SelectAdapter(Context mContext) {
            super(mContext);
//             width=(ViewUtil.getScreenWidth(mContext)-ViewUtil.dip2px(mContext,10)*3)/2;
             width=ViewUtil.getScreenWidth(mContext)/2;
        }

        @Override
        public int getLayoutId() {
            return R.layout.fragment_question_item;
        }

        @Override
        public void bindView(View view, int position, AnswerModel bean) {
            setText(view,R.id.s_name,bean.title);
            ImageView s_no= ViewHolder.get(view,R.id.s_no);

            if(bean.isRightAnswer){
                s_no.setVisibility(View.VISIBLE);
                s_no.setImageResource(R.drawable.ok);
            }else{
                if(bean.isSelected){
                    s_no.setVisibility(View.VISIBLE);
                    s_no.setImageResource(R.drawable.no);
                }else{
                    s_no.setVisibility(View.GONE);
                }
            }

            view.setEnabled(!mGiftModel.hasAnswer);
            view.setSelected(bean.isSelected);

            setOnClick(view,bean,position);

            if(width!=view.getLayoutParams().width){
                view.getLayoutParams().width=width;
            }
        }
    }

    @Override
    public void devModel() {
        if(DevToolsAct.DEV_AUTO_ANSWER){

            int index=0;
            for (int i = 0; i < mGiftModel.selectAnswer.size(); i++) {
                AnswerModel bean=mGiftModel.selectAnswer.get(i);
                if(TextUtils.equals(mGiftModel.rightAnswer,bean.title)){
                    index=i;
                    break;
                }
            }
            View v=mRecycler.getChildAt(index);
            v.performClick();
        }
    }
}
