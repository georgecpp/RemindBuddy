package com.steelparrot.remindbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class TaskFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";


    private Task mTask;
    private TextView mTaskDate;
    private TextView mTaskTime;
    private TextView mTaskTitle;
    private TextView mTaskDescription;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task, container, false);
        mTaskDate = (TextView) v.findViewById(R.id.task_date);
        mTaskTime = (TextView) v.findViewById(R.id.task_time);
        mTaskTitle = (TextView) v.findViewById(R.id.task_title);
        mTaskDescription = (TextView) v.findViewById(R.id.task_description);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        //
    }

    @Override
    public void onPause() {
        super.onPause();

        TaskHandler.get(getActivity()).updateTask(mTask);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
