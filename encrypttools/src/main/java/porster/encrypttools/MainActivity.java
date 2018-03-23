package porster.encrypttools;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.encrypt_file).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.encrypt_file:
                encrypt();
                Log.i(TAG,"加密完成");
                break;
        }
    }
    private void encrypt(){
        String SD= Environment.getExternalStorageDirectory().getPath()+"/AAA/kj.txt";
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
}
