package com.porster.gift;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.porster.gift.utils.ApiUtils;
import com.porster.gift.utils.AppConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Porster on 17/2/7.
 */

public class TestEncodeAct extends Activity{
    String myCache="AAA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ApiUtils.execute(new AsyncTask<Object, Integer, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                encrypt();


//                readKJ();


                //读取视频文件加密完后删除本地的再保存新的格式
//                String SD=Environment.getExternalStorageDirectory().getPath();
//                File mCache=new File(SD+File.separator+myCache);
//                if(!mCache.exists()){
//                    boolean b=mCache.mkdirs();
//                }
//                String mp4=mCache.getAbsolutePath()+File.separator+"h221.hck";
////
//                copy2(new File(mp4),new File(mCache.getAbsolutePath()+File.separator+"h221.hck2"));
//

//                CustomFileCipherUtil.encrypt(mp4, new CustomFileCipherUtil.CipherListener() {
//                    @Override
//                    public void onProgress(long current, long total) {
//                        Log.i(AJKConstant.TAG,current+"_"+total);
//                    }
//                });


//                FileInputStream fis;
//                try {
//
//                    fis= new FileInputStream(new File(mp4));
////                    String fileName=mp4.substring(mp4.lastIndexOf("/"));
//
//                    FileOutputStream fos=new FileOutputStream(mCache.getAbsolutePath()+File.separator+"mov_bbb");
//
//                    byte buffer[] = new byte[AJKConstant.BUFF_SIZE];
//                    int realLength;
//                    while ((realLength = fis.read(buffer)) > 0) {
//                        fos.write(buffer, 0, realLength);
//                    }
//                    fis.close();
//                    fos.close();
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
            }
        });
    }
    private void encrypt(){
        String SD=Environment.getExternalStorageDirectory().getPath()+"/AAA/kj.txt";
        try {
            File f=new File(SD);

                RandomAccessFile raf=new RandomAccessFile(f,"rw");

            FileChannel fc=raf.getChannel();

            MappedByteBuffer mbb=fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());


            for (int i = 0; i < fc.size(); i+=10) {

                byte origin=mbb.get(i);

//                if(isDecode){
//                    mbb.put(i, (byte) (origin-1));
//                }else{
                    mbb.put(i, (byte) (origin+1));
//                }

            }

            mbb.force();
            mbb.clear();
            fc.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copy2(File f,File t){
        long s=System.currentTimeMillis();
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(f);
            outStream = new FileOutputStream(t);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
            in.close();
            out.close();
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            long time=System.currentTimeMillis()-s;
            Log.i(AppConstants.TAG,time/1000+"");
        }
    }
    public void readKJ(){
        try {
            FileInputStream fis=new FileInputStream(
                    Environment.getExternalStorageDirectory().getPath()+File.separator+myCache+File.separator+"kj2.txt");

            InputStreamReader reader=new InputStreamReader(fis,"utf-8");

            BufferedReader b=new BufferedReader(reader);

            String line;

//            OutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath()+File.separator+myCache+File.separator+"kj3.txt"));
//            OutputStreamWriter o=new OutputStreamWriter(out,"utf-8");
            StringBuilder sb=new StringBuilder();
            int position=0;
            while ((line=b.readLine())!=null){
                position++;
//                for (int i = 400; i < 920; i++) {
//                    String index=i+"";
//                    if(line.contains(index)){
////                        Log.w(AJKConstant.TAG,index);
////                        line=line.replace(index,"").trim();
//                    }
//                }
//                o.append(position+"、").append(line).append("\n");;
                sb.append(line);
//                Log.i(AJKConstant.TAG,line);

            }


//            String[] alldata=sb.toString().trim().split("#");
//
//            Log.i(AJKConstant.TAG,alldata.length+"");
//
//            int lastP=0;
//            for (String s : alldata) {
//                int nowP=Integer.parseInt(s.split("、")[0].trim());
//                if(nowP-lastP>1){
//                    Log.w(AJKConstant.TAG,s);
//                }
//                Log.i(AJKConstant.TAG,s);
//
//
//                lastP=Integer.parseInt(s.split("、")[0].trim());
//            }




            fis.close();
            reader.close();
            b.close();
//            o.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**自定义实现简单的文件加密解密工具
     * Created by zhangshuo on 2016/6/28.
     */
    public static class CustomFileCipherUtil {

        /**
         * 加密后的文件的后缀
         */
        public static final String CIPHER_TEXT_SUFFIX = ".7z";

        /**
         * 加解密时以32K个字节为单位进行加解密计算
         */
        private static final int CIPHER_BUFFER_LENGHT = 32 * 1024;

        /**
         * 加密，这里主要是演示加密的原理，没有用什么实际的加密算法
         *
         * @param filePath 明文文件绝对路径
         * @return
         */
        public static boolean encrypt(String filePath, CipherListener listener) {

            try {
                File f = new File(filePath);
                RandomAccessFile raf = new RandomAccessFile(f, "rw");

                FileChannel channel=raf.getChannel();

                MappedByteBuffer buffer=channel.map(FileChannel.MapMode.READ_WRITE,0,1);
//                buffer.put(0, (byte) 1);
                buffer.put(0, (byte) 0);
                buffer.force();
                buffer.clear();
                channel.close();
                raf.close();
//                buffer=channel.map(FileChannel.MapMode.READ_WRITE,0,1024);
//                Log.i(AJKConstant.TAG,buffer.get()+"");
//                Log.i(AJKConstant.TAG,buffer.limit()+"");
//                Log.i(AJKConstant.TAG,buffer.get(100)+"");

            } catch (Exception e) {
                e.printStackTrace();
            }

//
//            try {
//                long startTime = System.currentTimeMillis();
//                File f = new File(filePath);
//                RandomAccessFile raf = new RandomAccessFile(f, "rw");
//                long totalLenght = raf.length();
//                FileChannel channel = raf.getChannel();
//                long multiples = totalLenght / CIPHER_BUFFER_LENGHT;
//                long remainder = totalLenght % CIPHER_BUFFER_LENGHT;
//                multiples=1;
//                remainder=1;
//
//                MappedByteBuffer buffer = null;
//                byte tmp;
//                byte rawByte;
//
//                //先对整除部分加密
//                for(int i = 0; i < multiples; i++){
//                    buffer = channel.map(
//                            FileChannel.MapMode.READ_WRITE, i * CIPHER_BUFFER_LENGHT, (i + 1) * CIPHER_BUFFER_LENGHT);
//                    //此处的加密方法很简单，只是简单的异或计算
//                    for (int j = 0; j < CIPHER_BUFFER_LENGHT; ++j) {
//                        rawByte = buffer.get(j);
//                        tmp = (byte) (rawByte ^ j);
//                        buffer.put(j, tmp);
//
//                        if(null != listener){
//                            listener.onProgress(i * CIPHER_BUFFER_LENGHT + j, totalLenght);
//                        }
//                    }
//                    buffer.force();
//                    buffer.clear();
//                }
//
//                //对余数部分加密
//                buffer = channel.map(
//                        FileChannel.MapMode.READ_WRITE, multiples * CIPHER_BUFFER_LENGHT, multiples * CIPHER_BUFFER_LENGHT + remainder);
//
//                for (int j = 0; j < remainder; ++j) {
//                    rawByte = buffer.get(j);
//                    tmp = (byte) (rawByte ^ j);
//                    buffer.put(j, tmp);
//
//                    if(null != listener){
//                        listener.onProgress(multiples * CIPHER_BUFFER_LENGHT + j, totalLenght);
//                    }
//                }
//                buffer.force();
//                buffer.clear();
//
//                channel.close();
//                raf.close();
//
//                //对加密后的文件重命名，增加.cipher后缀
//            f.renameTo(new File(f.getPath() + CIPHER_TEXT_SUFFIX));
//                Log.d("加密用时：", (System.currentTimeMillis() - startTime) /1000 + "s");
//                return true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
            return true;
        }


        /**
         * 解密，这里主要是演示加密的原理，没有用什么实际的加密算法
         *
         * @param filePath 密文文件绝对路径，文件需要以.cipher结尾才会认为其实可解密密文
         * @return
         */
        public static boolean decrypt(String filePath, CipherListener listener) {
            try {
                long startTime = System.currentTimeMillis();
                File f = new File(filePath);
//            if(!f.getPath().toLowerCase().endsWith(CIPHER_TEXT_SUFFIX)){
//                //后缀不同，认为是不可解密的密文
//                return false;
//            }

                RandomAccessFile raf = new RandomAccessFile(f, "rw");
                long totalLenght = raf.length();
                FileChannel channel = raf.getChannel();

                long multiples = totalLenght / CIPHER_BUFFER_LENGHT;
                long remainder = totalLenght % CIPHER_BUFFER_LENGHT;
                multiples=1;
                remainder=1;

                MappedByteBuffer buffer = null;
                byte tmp;
                byte rawByte;

                //先对整除部分解密
                for(int i = 0; i < multiples; i++){
                    buffer = channel.map(
                            FileChannel.MapMode.READ_WRITE, i * CIPHER_BUFFER_LENGHT, (i + 1) * CIPHER_BUFFER_LENGHT);

                    //此处的解密方法很简单，只是简单的异或计算
                    for (int j = 0; j < CIPHER_BUFFER_LENGHT; ++j) {
                        rawByte = buffer.get(j);
                        tmp = (byte) (rawByte ^ j);
                        buffer.put(j, tmp);

                        if(null != listener){
                            listener.onProgress(i * CIPHER_BUFFER_LENGHT + j, totalLenght);
                        }
                    }
                    buffer.force();
                    buffer.clear();
                }

                //对余数部分解密
                buffer = channel.map(
                        FileChannel.MapMode.READ_WRITE, multiples * CIPHER_BUFFER_LENGHT, multiples * CIPHER_BUFFER_LENGHT + remainder);

                for (int j = 0; j < remainder; ++j) {
                    rawByte = buffer.get(j);
                    tmp = (byte) (rawByte ^ j);
                    buffer.put(j, tmp);

                    if(null != listener){
                        listener.onProgress(multiples * CIPHER_BUFFER_LENGHT + j, totalLenght);
                    }
                }
                buffer.force();
                buffer.clear();

                channel.close();
                raf.close();

                //对加密后的文件重命名，增加.cipher后缀
            f.renameTo(new File(f.getPath().substring(f.getPath().toLowerCase().indexOf(CIPHER_TEXT_SUFFIX))));

                Log.d("解密用时：", (System.currentTimeMillis() - startTime) / 1000 + "s");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 用于加解密进度的监听器
         */
        public interface CipherListener{
            void onProgress(long current, long total);
        }
    }
}
