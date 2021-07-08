package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class TaskListFragment extends Fragment {

    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;

    private FloatingActionButton mAddTaskFAB;
    private TextView mCurrentDateTextView;
    private ImageButton mDateNextButton;
    private ImageButton mDatePrevButton;
    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;
    private Callbacks mCallbacks;
    private String mCurrentDate;


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
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // TO IMPLEMENT.
                int position = viewHolder.getAdapterPosition();
                Task task = mAdapter.mTasks.get(position);
                TaskHandler.get(getActivity()).deleteTask(task);
                updateUI();
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
        setTaskRecyclerViewItemTouchListener();


        mAddTaskFAB = (FloatingActionButton) view.findViewById(R.id.add_task_fab);
        mAddTaskFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // String currentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
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
        private Task mTask;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.task_title);
            mDatetimeTextView = (TextView) itemView.findViewById(R.id.task_datetime);

        }

        public void bind(Task task) {
            mTask = task;
            mTitleTextView.setText(mTask.getTitle());
            mDatetimeTextView.setText(mTask.getTime());
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mDateChangedReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
        updateUI();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mDateChangedReceiver);
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
