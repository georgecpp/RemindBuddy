package com.steelparrot.remindbuddy;

import android.content.Intent;

import androidx.fragment.app.Fragment;

// to do: implement TaskListFragment in order to get runnable app.
public class TaskListActivity extends SingleFragmentActivity implements TaskListFragment.Callbacks, TaskFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new TaskListFragment();
    }

    @Override
    public void onTaskSelected(Task task) {
        Intent intent = TaskPagerActivity.newIntent(this, task.getID());
        startActivity(intent);
    }

    @Override
    public void onTaskUpdated(Task task) {
        // TO IMPLEMENT WHEN EDIT TASK IN EDITOR FRAGMENT.
    }
}