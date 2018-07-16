package aeight.sami.taichungattractions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private OpenDataThread thread;
    private TextView empty_textView;
    private RecyclerView recyclerView;
    private Handler handler;
    private RecyclerView.Adapter<RVAdapter.ViewHolder> mAdapter;
    private LinearLayoutManager layoutManager;
    private Context context;
    private ArrayList<Location> openData = new ArrayList();
    private ArrayList<Location> outputData = new ArrayList();
    private int dataNum = 20;
    private File jsonFile;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();//初始化
        thread = new OpenDataThread(swipeRefreshLayout);
        thread.start();//上網抓取json資料
        Log.d(TAG, "讀取快取");
        readFile();//讀取快取
        checkEmptyData();//檢查資料是否為空
    }
    private void init(){
        context = this;
        empty_textView = (TextView) findViewById(R.id.empty_textView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new RVDecoration(this, RVDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        handler = new Handler();
        File cacheDir = getCacheDir();
        jsonFile = new File(cacheDir.getAbsolutePath(), "data.plist");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorBar));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                android.R.color.holo_blue_bright,
                android.R.color.black
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                OpenDataThread temp = new OpenDataThread(swipeRefreshLayout);
                temp.start();
                Log.d(TAG, "刷新資料");
                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void checkEmptyData(){
        if(openData.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            empty_textView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Empty data");
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            empty_textView.setVisibility(View.GONE);
        }
    }
    private void readFile(){
        if(jsonFile.exists()){
            openData.clear();
            char[] buffer = new char[1];
            StringBuilder sb = new StringBuilder();
            try{
                FileReader fr = new FileReader(jsonFile);
                while(fr.read(buffer) != -1)
                    sb.append(new String(buffer));
                String str = sb.toString();
                JSONObject obj = new JSONObject(str);
                JSONObject obj1 = obj.getJSONObject("ROOT");
                JSONArray array = obj1.getJSONArray("RECORD");
                //景點ID、狀態、名稱、簡述、介紹(html)、鄉鎮市區、地址、東經、北緯、電話、大眾運輸、門票資訊、行車資訊、停車資訊、旅遊叮嚀
                Log.d(TAG, "Open data資料個數：" + String.valueOf(array.length()));
                for(int i = 0; i < array.length(); i++){
                    JSONObject temp = array.getJSONObject(i);
                    openData.add(new Location(temp.getString("狀態"), temp.getString("名稱"), temp.getString("簡述"),
                            temp.getString("鄉鎮市區") + temp.getString("地址"), temp.getString("電話")));
                    Log.d(TAG, array.getJSONObject(i).getString("名稱"));
                }
                setRecyclerViewData();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    private void setRecyclerViewData(){
//        outputData.clear();
//        for(int i = 0; i < dataNum; i++)
//            outputData.add(openData.get(i));
//        Log.d(TAG, "新增" + outputData.size() + "筆資料");
        try{
            mAdapter = new RVAdapter(context, openData);
            recyclerView.setAdapter(mAdapter);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
//    private void loadMoreData(){
//        int temp = outputData.size();
//        for(int i = 0; i < dataNum; i++){
//            outputData.add(openData.get(temp + i));
//            mAdapter.notifyDataSetChanged();
//        }
//
//        Log.d(TAG, "目前加載data個數" + outputData.size());
//    }
    public void openDataDialog(int position){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.detail_view, null);
        TextView address = (TextView) layout.findViewById(R.id.address_textView);
        TextView tel = (TextView) layout.findViewById(R.id.tel_textView);
        TextView intro = (TextView) layout.findViewById(R.id.intro_textView);
        intro.setMovementMethod(ScrollingMovementMethod.getInstance());
        address.setText("地址：" + openData.get(position).getAddress());
        tel.setText("電話：" + openData.get(position).getTelNum());
        intro.setText(openData.get(position).getIntro());
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(openData.get(position).getName());
        builder.setView(layout);
        builder.setNeutralButton("關閉", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private class OpenDataThread extends Thread {
        private SwipeRefreshLayout layout;
        OpenDataThread(){}
        OpenDataThread(SwipeRefreshLayout layout){
            this.layout = layout;
        }
        @Override
        public void run() {
            super.run();
            catchData();
        }
        public void catchData(){
            String strUrl = "http://datacenter.taichung.gov.tw/swagger/OpenData/a438f78d-b66b-4fed-8c9f-7567fd1e56f1";//台中景點json
            OkHttpClient client = null;
            client = new OkHttpClient();

            Request request = new Request.Builder().url(strUrl).build();
            try {
                Response response = client.newCall(request).execute();
                String str = response.body().string();
                Log.d(TAG, "獲取資料" + str);
                //寫入快取
                FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(str);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                Log.d(TAG, "寫入" + jsonFile.getAbsolutePath().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "網路抓取openData");
                        readFile();
                        Toast.makeText(context, getResources().getText(R.string.refresh_success_text), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, getResources().getText(R.string.refresh_fail_text), Toast.LENGTH_SHORT).show();
                    }
                });
            } finally {
                if(layout != null)
                    layout.setRefreshing(false);
            }
        }
    }
}
