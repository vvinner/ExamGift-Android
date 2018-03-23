package com.porster.gift.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.porster.gift.model.GiftModel;
import com.porster.gift.utils.ApiUtils;
import com.porster.gift.utils.AppConstants;
import com.porster.gift.utils.LogCat;
import com.porster.gift.utils.PinYinUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Porster on 17/2/28.
 */
public class DataManager {
    private final String FXXK_DMG="Fxxk.dmg";
    private final String CACHE_GIFTS="CACHE_GIFTS.AIR";


    private static DataManager ourInstance = new DataManager();

    public static DataManager getInstance() {
        return ourInstance;
    }

    private DataManager() {
        mGiftModels=new ArrayList<>();
    }

    public ArrayList<GiftModel> getGiftModels() {
        return mGiftModels;
    }

    private ArrayList<GiftModel> mGiftModels;
    private OnAnalysisStateListener mOnAnalysisStateListener;

    public void setOnAnalysisStateListener(OnAnalysisStateListener onAnalysisStateListener) {
        mOnAnalysisStateListener = onAnalysisStateListener;
    }

    //保存数据
    public void saveList(final Context mCtx, final String key, final Object list){

                //将数据保存到本地
                FileOutputStream fos= null;
                try {

                    fos = mCtx.openFileOutput(key, Context.MODE_PRIVATE);

                    ObjectOutputStream oos=new ObjectOutputStream(fos){
                        @Override
                        protected void writeStreamHeader() throws IOException {
//                            super.writeStreamHeader();
                        }
                    };

                    oos.writeObject(list);

                    oos.flush();
                    oos.close();

                    fastEncrypt(mCtx,key);

                } catch (IOException e) {
                    e.printStackTrace();
                }
    }
    public void saveListAsync(final Context mCtx, final String key, final Object list){
        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                saveList(mCtx,key,list);
                return null;
            }
        });
    }

    //保存数据
    public Object readList(final Context mCtx, final String key) throws Exception{
        return queryCacheList(mCtx,key);
    }
    public void readListAsync(final Context mCtx, final String key, final OnReadListener mResult) {
        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {

                    Object list = readList(mCtx, key);
                    return  list;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ArrayList<GiftModel>();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                mResult.onSuccess(o);
            }
        });
    }
    private Object queryCacheList(final Context mCtx, final String key) throws IOException,Exception{
        fastDecode(mCtx,key);

        FileInputStream fis=mCtx.openFileInput(key);

        ObjectInputStream ois=new ObjectInputStream(fis){
            @Override
            protected void readStreamHeader() throws IOException {
//                super.readStreamHeader();
            }
        };

        Object cache=ois.readObject();

        fastEncrypt(mCtx,key);

        if(cache!=null){
            return cache;
        }
        return null;
    }


    public void init(final Context mCtx){
        /*
            1、将加密好的文件从AS中读取保存到内部存储中
            2、读取内部存储中的文件进行解密
            3、读取解密后的文件数据到List中,并删除解密后的文件
         */


        //复制到内存
        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {

            long startTime;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(mOnAnalysisStateListener!=null){
                    mOnAnalysisStateListener.onStart();
                }
                startTime=System.currentTimeMillis();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(mOnAnalysisStateListener!=null){
                    mOnAnalysisStateListener.onEnd();
                }
                long s=(System.currentTimeMillis()-startTime)/1000;
                LogCat.i(AppConstants.TAG,"总共耗时"+s+"秒");
                LogCat.i(AppConstants.TAG,"解析出"+getGiftModels().size()+"道题目");

//                for (GiftModel giftModel : getGiftModels()) {
//                    LogCat.i(AppConstants.TAG,giftModel.toString());
//                }
            }

            @Override
            protected Object doInBackground(Object... params) {
                File fs=new File(mCtx.getFilesDir(),CACHE_GIFTS);
                if(fs.exists()){

                    try {
                        mGiftModels= (ArrayList<GiftModel>) readList(mCtx, CACHE_GIFTS);
                        LogCat.i(AppConstants.TAG,"从本地读取完成");
                    } catch (Exception e) {
                        LogCat.i(AppConstants.TAG,"从本地读取异常,即将重试"+fs.delete());
                        init(mCtx);
                        e.printStackTrace();
                    }
                    return null;
                }

                AssetManager mAM=mCtx.getAssets();
                try {
                    //复制到内部存储
                    InputStream in=mAM.open(FXXK_DMG);

                    FileOutputStream out=mCtx.openFileOutput(FXXK_DMG,Context.MODE_PRIVATE);

                    byte[] buf=new byte[1024];

                    int len;
                    while ((len=in.read(buf))>0){
                        out.write(buf,0,len);
                    }
                    out.close();
                    in.close();

                    //解密文件
                    File f=fastEncrypt(mCtx,FXXK_DMG,true);

                    //读取内容
                    FileInputStream fin=mCtx.openFileInput(FXXK_DMG);

                    InputStreamReader isr=new InputStreamReader(fin,"utf-8");

                    BufferedReader br=new BufferedReader(isr);

                    String line;
                    while ((line=br.readLine())!=null){
                        GiftModel giftModel=new GiftModel();
                        LogCat.i(AppConstants.TAG,line);

                        String id=line.substring(0,line.indexOf("、"));
                        String content=line.substring(line.indexOf("、")+1,line.indexOf("|"));
                        String ansers=line.substring(line.indexOf("|")+1,line.length()-1);
                        String right=ansers.substring(0,ansers.indexOf("|"));

                        giftModel.id=id;

                        //分离图片与内容
                        if(content.startsWith("image")){
                            //如果image第2位是数字,那么就截取后2位
                            //如果不是,则截取后1位
                            if(isNumeric(content.substring(6,7))){
                                giftModel.content=content.substring(7).trim();
                                giftModel.picName=content.substring(0,7);
                            }else{
                                giftModel.content=content.substring(6).trim();
                                giftModel.picName=content.substring(0,6);
                            }
                        }else{
                            giftModel.content=content.trim();
                        }
                        giftModel.ansers=ansers;
                        giftModel.rightAnswer =right;
                        giftModel.title=line;

                        //解析拼音耗时3S
                        giftModel.pinyin= PinYinUtils.getFirstSpell(giftModel.content);

                        mGiftModels.add(giftModel);
                    }
                    br.close();
                    isr.close();
                    fin.close();

                    saveList(mCtx,CACHE_GIFTS,mGiftModels);

                    LogCat.i(AppConstants.TAG,"读取完成"+(f.delete()?"删除成功":"删除失败"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        //读取到数据
    }
    public File fastDecode(Context mCtx, String fileName) throws IOException{
        return fastEncrypt(mCtx,fileName,true);
    }
    public File fastEncrypt(Context mCtx, String fileName) throws IOException{
        return fastEncrypt(mCtx,fileName,false);
    }
    /***
     * 快速加解密数据
     * @param isDecode  true:解密:false:加密
     * @return
     * @throws IOException
     */
    public File fastEncrypt(Context mCtx, String fileName, boolean isDecode) throws IOException{
        File f=new File(mCtx.getFilesDir(),fileName);

        RandomAccessFile raf=new RandomAccessFile(f,"rw");

        FileChannel fc=raf.getChannel();

        MappedByteBuffer mbb=fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());


        for (int i = 0; i < fc.size(); i+=10) {

            byte origin=mbb.get(i);

            if(isDecode){
                mbb.put(i, (byte) (origin-1));
            }else{
                mbb.put(i, (byte) (origin+1));
            }

        }

        mbb.force();
        mbb.clear();
        fc.close();
        raf.close();
        return f;
    }
    private File encrypt2(Context mCtx,String fileName,boolean isDecode) throws IOException{
        File f=new File(mCtx.getFilesDir(),fileName);

        RandomAccessFile raf=new RandomAccessFile(f,"rw");

        FileChannel fc=raf.getChannel();

        MappedByteBuffer mbb=fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());


        for (int i = 0; i < fc.size(); i++) {

            byte origin=mbb.get(i);

            if(isDecode){
                mbb.put(i, (byte) (origin-1));
            }else{
                mbb.put(i, (byte) (origin+1));
            }

        }

        mbb.force();
        mbb.clear();
        fc.close();
        raf.close();
        return f;
    }
    public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**解析进度*/
    public interface OnAnalysisStateListener{
        void onStart();
        void onEnd();
    }
    /**解析进度*/
    public interface OnReadListener{
        void onSuccess(Object list);
    }

    public SparseArray<String> getTeacherPoint(){
        SparseArray<String> k=new SparseArray<>();
        String points[]={
                "建邺[65，94]","建邺[43，20]","江南野外[78，41]","江南野外[78，104]",
                "长安[290，48]","长安[136,20]","大唐国境[323，114]","大唐国境[304，291]",
                "大唐国境[182，293]","大唐国境[86，241]","长安[500，72]","长安[516，185]",
                "化生寺[23,15]","化生寺[70，62]","长安[328,266]","大唐官府[56，26]",
                "大唐官府[95，76]","大唐官府[143，54]","长安城[368，209]","长安城[300,187]",
        };
        for (int i = 0; i < points.length; i++) {
            k.put(i,points[i]);
        }
        return k;
    }
}
