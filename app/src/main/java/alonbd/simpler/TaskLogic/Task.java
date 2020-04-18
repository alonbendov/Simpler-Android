package alonbd.simpler.TaskLogic;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import alonbd.simpler.BackgroundAndroid.TasksManager;
import alonbd.simpler.R;
import alonbd.simpler.UI.RecyclerViewAdapter;

public class Task implements Serializable {
    private Trigger mTrigger;
    private ArrayList<Action> mActions;
    private String mName;
    private boolean mOnceOnly;
    private Date mDate;
    private static RecyclerViewAdapter sRecyclerViewAdapter;

    public Task(Trigger mTrigger, String mName, boolean mOnceOnly, ArrayList<Action> mActions) {
        mDate = new Date();
        this.mName = mName;
        this.mTrigger = mTrigger;
        this.mActions = mActions;
        this.mOnceOnly = mOnceOnly;
    }

    public static void removeRecyclerViewAdapter() {
        sRecyclerViewAdapter = null;
    }

    public View getDescriptiveView(Context context) {
        View root = View.inflate(context, R.layout.layout_view_task, null);
        ((TextView) root.findViewById(R.id.name_tv)).setText(mName);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        ((TextView) root.findViewById(R.id.date_time_tv)).setText(dateFormat.format(mDate));
        ((LinearLayout) root.findViewById(R.id.trigger_ll)).addView(mTrigger.getDescriptiveView(context, mOnceOnly));

        LinearLayout actionsLL = root.findViewById(R.id.actions_ll);
        for(Action action : mActions) {
            CardView cv = (CardView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.layout_view_action_card, actionsLL, false);
            cv.addView(action.getDescriptiveView(context));
            actionsLL.addView(cv);
        }
        return root;

    }

    public static void setRecyclerViewAdapter(RecyclerViewAdapter mRecyclerViewAdapter) {
        Task.sRecyclerViewAdapter = mRecyclerViewAdapter;
    }

    public void start(Context context) {
        if(isReady()) {
            for(Action action :
                    mActions) {
                action.onExecute(context);
            }
            mTrigger.setUsed();
            TasksManager.getInstance(context).saveData();
            if(sRecyclerViewAdapter != null) {
                sRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    public String getName() {return mName;}

    public boolean isOnceOnly() {
        return mOnceOnly;
    }

    public boolean isTriggerUsed() {
        return mTrigger.isUsed();
    }

    public void setTriggerNotUsed() {
        mTrigger.setReady();
    }

    public boolean isReady() {return !(mTrigger.isUsed() && isOnceOnly());}

    public Class getTriggerClass() {
        return mTrigger.getClass();
    }

    public boolean triggerMatchIntent(Intent intent) {return mTrigger.matchIntent(intent);}

    public boolean triggerMatchLocation(Location location) {return mTrigger.matchLocation(location);}

    public static class DefaultDateComparator implements Comparator<Task> {
        private boolean mDefaultOrder;

        public DefaultDateComparator(boolean mDefaultOrder) {
            this.mDefaultOrder = mDefaultOrder;
        }

        @Override
        public int compare(Task o1, Task o2) {
            return o1.mDate.compareTo(o2.mDate) * (mDefaultOrder ? 1 : -1);
        }
    }

    public static class NameComparator implements Comparator<Task> {
        private boolean mDefaultOrder;

        public NameComparator(boolean mDefaultOrder) {
            this.mDefaultOrder = mDefaultOrder;
        }

        @Override
        public int compare(Task o1, Task o2) {
            return o1.mName.compareTo(o2.getName()) * (mDefaultOrder ? 1 : -1);
        }
    }

    public static class TriggerComparator implements Comparator<Task> {
        private boolean mDefaultOrder;

        public TriggerComparator(boolean mDefaultOrder) {
            this.mDefaultOrder = mDefaultOrder;
        }

        @Override
        public int compare(Task o1, Task o2) {
            return o1.getTriggerClass().getSimpleName().compareTo(o2.getTriggerClass().getSimpleName()) * (mDefaultOrder ? 1 : -1);
        }
    }

    public static class StatusComparator implements Comparator<Task> {
        private boolean mDefaultOrder;

        public StatusComparator(boolean mDefaultOrder) {
            this.mDefaultOrder = mDefaultOrder;
        }

        private static int getStatusNumber(Task t) {
            if(!t.isOnceOnly()) return 3;
            if(t.isReady()) return 2;
            return 1;
        }

        @Override
        public int compare(Task o1, Task o2) {
            return getStatusNumber(o1) - getStatusNumber(o2) * (mDefaultOrder ? 1 : -1);
        }
    }
}

