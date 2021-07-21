package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class TaskFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;


    private Task mTask;
    private TextView mTaskDate;
    private EditText mTaskTitle;
    private ImageButton mNewTaskDateButton;
    private TimePicker mTaskTimeOfTheDay;

    private Callbacks mCallbacks;


    public interface Callbacks {
        void onTaskUpdated(Task task);
    }

    public static TaskFragment newInstance(UUID taskID) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK_ID, taskID);

        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID taskID = (UUID) getArguments().getSerializable(ARG_TASK_ID);
        mTask = TaskHandler.get(getActivity()).getTask(taskID);
    }

    private void updateTask() {
        TaskHandler.get(getActivity()).updateTask(mTask);
        mCallbacks.onTaskUpdated(mTask);
    }

    public boolean showNavigationBar(Resources resources)
    {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // for screen height differentiation.
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels + getNavigationBarHeight();
//        int width = displayMetrics.widthPixels;
//        View v;
//        if(height >= 2300) {
//            v = inflater.inflate(R.layout.fragment_task, container, false);
//        }
//        else {
//            v = inflater.inflate(R.layout.fragment_task_smallheight,container,false);
//        }

          View v = inflater.inflate(R.layout.fragment_task, container, false);
          mTaskDate = (TextView) v.findViewById(R.id.task_date);
          mTaskDate.setText(TaskListFragment.getCurrentDate());

          mTaskTitle = (EditText) v.findViewById(R.id.task_title);
          mTaskTitle.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                  // blank
              }

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                  mTask.setTitle(s.toString());
                  updateTask();
              }

              @Override
              public void afterTextChanged(Editable s) {
                  // blank
              }
          });

          mNewTaskDateButton = (ImageButton) v.findViewById(R.id.new_task_date_button);
          mNewTaskDateButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  try {
                      FragmentManager fragmentManager = getFragmentManager();
                      Date date = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).parse(mTaskDate.getText().toString());
                      DatePickerFragment dialog = DatePickerFragment.newInstance(date);
                      dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                      dialog.show(fragmentManager, DIALOG_DATE);
                  } catch (ParseException e) {
                      e.printStackTrace();
                  }
              }
          });

          mTaskTimeOfTheDay = (TimePicker) v.findViewById(R.id.task_timepicker);
          mTaskTimeOfTheDay.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
              @Override
              public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                  try {
                      Calendar calendar = Calendar.getInstance();
                      SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
                      calendar.setTime(sdf.parse(mTaskDate.getText().toString()));
                      calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                      calendar.set(Calendar.MINUTE, minute);

                      String currentTimeSet = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.getTime());
                      mTask.setTime(currentTimeSet);
                      updateTask();

                  } catch (ParseException e) {
                      e.printStackTrace();
                  }

              }
          });


        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode!=Activity.RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            String newDateSet = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date);
            mTaskDate.setText(newDateSet);
            mTask.setDate(newDateSet);
            updateTask();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        TaskHandler.get(getActivity()).updateTask(mTask);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


}
