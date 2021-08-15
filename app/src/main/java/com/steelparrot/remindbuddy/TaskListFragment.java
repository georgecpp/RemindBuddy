package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class TaskListFragment extends Fragment {

    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TASK_COMPLETED = 1;


    private FloatingActionButton mAddTaskFAB;
    private TextView mCurrentDateTextView;
    private ImageButton mDateNextButton;
    private ImageButton mDatePrevButton;
    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;
    private Callbacks mCallbacks;
    private static String mCurrentDate;



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
                Snackbar.make(mTaskRecyclerView, task.getTitle()+", Deleted.", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TaskHandler.get(getActivity()).addTask(task);
                                mAdapter.mTasks.add(position, task);
                                mAdapter.notifyItemInserted(position);
                            }
                        }).show();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

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
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mDateChangedReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle extras = Objects.requireNonNull(getActivity()).getIntent().getExtras();
        if(extras!=null) {
            UUID taskId = UUID.fromString(extras.getSerializable("TaskId").toString());
            Task mTask = TaskHandler.get(getContext()).getTask(taskId);
            mTask.setCompleted(true);
            TaskHandler.get(getContext()).updateTask(mTask);
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


}
