package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class TaskListFragment extends Fragment {

    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TASK_COMPLETED = 1;

    private boolean voiceON_OFF;
    private FloatingActionButton mAddTaskFAB;
    private TextView mCurrentDateTextView;
    private ImageButton mDateNextButton;
    private ImageButton mDatePrevButton;
    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;
    private Callbacks mCallbacks;
    private static String mCurrentDate;
    private MenuItem optionsMenuItem;
    private Toolbar mToolbar;
    private static Serializable onceUsedTaskId = null;



    public void updateCurrentDateUI() {
        mCurrentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date());
        mCurrentDateTextView.setText(mCurrentDate);
    }

    private BroadcastReceiver mDateChangedReceiver = new DateChangedReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
                updateCurrentDateUI();
            }
        }
    };

    public interface Callbacks {
        void onTaskSelected(Task task);
    }

    public void setTaskRecyclerViewItemTouchListener()
    {
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction!=ItemTouchHelper.LEFT) {
                    return;
                }
                int position = viewHolder.getAdapterPosition();
                Task task = mAdapter.mTasks.get(position);
                Snackbar.make(mTaskRecyclerView, task.getTitle()+", deleted.", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TaskHandler.get(getActivity()).addTask(task);
                                startBackAlarmForTask(task);
                                mAdapter.mTasks.add(position, task);
                                mAdapter.notifyItemInserted(position);
                            }
                        }).show();
                if(!task.isCompleted() && task.getNotificationIdAssigned()!=-1) {
                    cancelAlarmForTask(task);
                }
                TaskHandler.get(getActivity()).deleteTask(task);
                updateUI();
            }


            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_white_24)
                        .addSwipeLeftBackgroundColor(Color.rgb(220,0,0))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mTaskRecyclerView);
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_list_menu,menu);
        optionsMenuItem = menu.findItem(R.id.task_list_voice_toggler);
        Context context = getActivity();
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        boolean voiceON_OFF_sharedpref = sharedPreferences.getBoolean(context.getString(R.string.voice_on_off),true);
        if(!voiceON_OFF_sharedpref) {
            optionsMenuItem.setTitle(R.string.task_list_voice_toggle_ON);
            voiceON_OFF = false;
        }
        else {
            optionsMenuItem.setTitle(R.string.task_list_voice_toggle_OFF);
            voiceON_OFF = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_list_voice_toggler:
                voiceON_OFF=!voiceON_OFF;
                if(!voiceON_OFF) {
                    optionsMenuItem.setTitle(R.string.task_list_voice_toggle_ON);
                }
                else {
                    optionsMenuItem.setTitle(R.string.task_list_voice_toggle_OFF);
                }
                Context context = getActivity();
                SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.voice_on_off),voiceON_OFF);
                editor.apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        if(getActivity()!=null) {
            MobileAds.initialize(requireActivity());
            AdView mBannerAdView = (AdView) view.findViewById(R.id.bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mBannerAdView.loadAd(adRequest);
        }
        mToolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mCurrentDateTextView = (TextView) view.findViewById(R.id.current_date_view);
        mCurrentDateTextView.setText(mCurrentDate);
        mCurrentDateTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mCurrentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FragmentManager fragmentManager = getFragmentManager();
                    Date date = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).parse(mCurrentDate);
                    DatePickerFragment dialog = DatePickerFragment.newInstance(date);
                    dialog.setTargetFragment(TaskListFragment.this, REQUEST_DATE);
                    dialog.show(fragmentManager, DIALOG_DATE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        mTaskRecyclerView = (RecyclerView) view.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // FOR SWIPE OPTIONS
        setTaskRecyclerViewItemTouchListener();


        mAddTaskFAB = (FloatingActionButton) view.findViewById(R.id.add_task_fab);
        mAddTaskFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // String currentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
//                int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                Task task = new Task(UUID.randomUUID(),mCurrentDate,currentTime);
                task.setTitle("Unnamed_Activity");
                TaskHandler.get(getActivity()).addTask(task);
                updateUI();
                mCallbacks.onTaskSelected(task);
            }
        });



        mDatePrevButton = (ImageButton) view.findViewById(R.id.date_prev_button);
        mDatePrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date currdate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).parse(mCurrentDate);
                    mCurrentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(addDays(currdate,-1));
                    mCurrentDateTextView.setText(mCurrentDate);
                    updateUI();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        mDateNextButton = (ImageButton) view.findViewById(R.id.date_next_button);
        mDateNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date currdate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).parse(mCurrentDate);
                    mCurrentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(addDays(currdate,1));
                    mCurrentDateTextView.setText(mCurrentDate);
                    updateUI();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode!= Activity.RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCurrentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date);
            mCurrentDateTextView.setText(mCurrentDate);
            updateUI();
        }

    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDatetimeTextView;
        private ImageView mSolvedTaskImageView;
        private Task mTask;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.task_title_item);
            mDatetimeTextView = (TextView) itemView.findViewById(R.id.task_datetime);
            mSolvedTaskImageView = (ImageView) itemView.findViewById(R.id.task_solved);
            mSolvedTaskImageView.setFocusable(true);

        }

        public void bind(Task task) {
            mTask = task;
            mTitleTextView.setText(mTask.getTitle());
            mDatetimeTextView.setText(mTask.getTime());
            if(mTask.isCompleted()) {
                mSolvedTaskImageView.setVisibility(View.VISIBLE);
            }
            else {
                mSolvedTaskImageView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onTaskSelected(mTask);
        }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

        private List<Task> mTasks;

        public TaskAdapter(List<Task> tasks) {
            mTasks = tasks;
        }

        @NonNull
        @Override
        public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TaskHolder(layoutInflater, parent);
        }

        public void setTasks(List<Task> tasks) {
            mTasks = tasks;
        }

        @Override
        public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
            Task task = mTasks.get(position);
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }

    public static String getCurrentDate() {
        return mCurrentDate;
    }

    public void updateUI() {
        TaskHandler taskHandler = TaskHandler.get(getActivity());
        List<Task> tasks = taskHandler.getTasksForToday(mCurrentDate);
        Collections.sort(tasks, new TimeComparator());

        if(mAdapter == null) {
            mAdapter = new TaskAdapter(tasks);
            mTaskRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setTasks(tasks);
            mAdapter.notifyDataSetChanged();
        }

    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date());
        getActivity().registerReceiver(mDateChangedReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
        setHasOptionsMenu(true);
    }

    private class TimeComparator implements Comparator<Task> {
        private DateFormat primaryFormat = new SimpleDateFormat("h:mm a");
        private DateFormat secondaryFormat = new SimpleDateFormat("H:mm");

        @Override
        public int compare(Task task, Task t1) {
            return timeInMillis(task.getTime()) - timeInMillis(t1.getTime());
        }


        public int timeInMillis(String time) {
            return timeInMillis(time, primaryFormat);
        }

        private int timeInMillis(String time, DateFormat format) {
            // you may need more advanced logic here when parsing the time if some times have am/pm and others don't.
            try {
                Date date = format.parse(time);
                return (int) date.getTime();
            } catch (ParseException e) {
                if (format != secondaryFormat) {
                    return timeInMillis(time, secondaryFormat);
                }
            }
            return -1;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mDateChangedReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity()!=null) {
            if(requireActivity().getIntent()!=null) {
                Bundle extras = requireActivity().getIntent().getExtras();
                if(extras!=null ) {
                     if(extras.getSerializable("TaskId")!=null && !extras.getSerializable("TaskId").equals(onceUsedTaskId)) {
                         UUID taskId = UUID.fromString(extras.getSerializable("TaskId").toString());
                        onceUsedTaskId = taskId;
                        Task mTask = TaskHandler.get(getContext()).getTask(taskId);
                        mTask.setCompleted(true);
                        TaskHandler.get(getContext()).updateTask(mTask);
                     }
                }
            }
        }
        updateUI();
    }



    @Override
    public void onPause() {
        // old broadcast unregister
//        getActivity().unregisterReceiver(mDateChangedReceiver);
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void cancelAlarmForTask(Task taskWithNotification) {

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), taskWithNotification.getNotificationIdAssigned(), intent, 0);

        if(alarmManager!=null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void startBackAlarmForTask(Task taskWithNotification) {

        // get the calendar object
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(taskWithNotification.getDate()));
            calendar.set(Calendar.HOUR,getCalendarHourFromTaskTime(taskWithNotification.getTime()));
            calendar.set(Calendar.MINUTE,getCalendarMinuteFromTaskTime(taskWithNotification.getTime()));
            calendar.set(Calendar.AM_PM,getCalendarAM_PM_FromTaskTime(taskWithNotification.getTime()));
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), AlertReceiver.class);
            intent.putExtra("TaskTitle", taskWithNotification.getTitle());
            intent.putExtra("TaskId", taskWithNotification.getID());
            intent.putExtra("NotificationId",taskWithNotification.getNotificationIdAssigned());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), taskWithNotification.getNotificationIdAssigned(), intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int getCalendarHourFromTaskTime(String taskTime) {
        return Integer.parseInt(taskTime.split(":")[0]);
    }

    private int getCalendarMinuteFromTaskTime(String taskTime) {
        return Integer.parseInt(taskTime.split(":")[1].split(" ")[0]);
    }

    private int getCalendarAM_PM_FromTaskTime(String taskTime) {
        if(taskTime.contains("AM")) {
            return Calendar.AM;
        }
        else {
            return Calendar.PM;
        }
    }
}
