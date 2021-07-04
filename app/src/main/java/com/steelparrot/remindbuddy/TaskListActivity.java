package com.steelparrot.remindbuddy;

import androidx.fragment.app.Fragment;

// to do: implement TaskListFragment in order to get runnable app.
public class TaskListActivity extends SingleFragmentActivity implements TaskListFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new TaskListFragment();
    }

    @Override
    public void onTaskSelected(Task task) {

    }
}