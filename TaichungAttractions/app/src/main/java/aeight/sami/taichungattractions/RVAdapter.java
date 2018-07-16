package aeight.sami.taichungattractions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2018/7/13.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {
    private final static String TAG = "RVAdapter";
    private List<Location> data;
    private static Context context;

    public RVAdapter(Context context, List<Location> mData){
        this.context = context;
        data = new ArrayList<>();
        for(int i = 0; i < mData.size(); i++)
            this.data.add(mData.get(i));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_textview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position % 2 == 0)
            holder.textName.setTextColor(0xFF000000);
        else
            holder.textName.setTextColor(0xFFAAAAAA);
        holder.textName.setText(data.get(position).getName());
        holder.textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v instanceof TextView){
                    ((MainActivity)context).openDataDialog(position);
                    Log.d(TAG, "開啟" + data.get(position).getName() + "詳細資訊dialog");
                }
            }
        });
        holder.textName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        holder.textName.setTextColor(context.getColor(R.color.colorAccent));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL://避免捲動時沒有檢測到ACTION_DOWN而沒有恢復顏色
                        holder.textName.setTextColor(context.getColor(R.color.colorNormal));
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textName;
        public ViewHolder(View itemView) {
            super(itemView);
            textName = (TextView)itemView.findViewById(R.id.textView_name);
        }
    }

}
