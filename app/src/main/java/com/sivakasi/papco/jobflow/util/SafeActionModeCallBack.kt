package com.sivakasi.papco.jobflow.util

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode

/*
This is a utility wrapper class for preventing memory leaks.
Lets assume that we are implementing action mode callback in the fragment (which we have to do)
Now, when we start the action mode by providing it the fragment itself since it implements the callback,
then even after destroying the fragment view, (that is even when actionMode==null) the activity somehow
stores the strong reference to the fragment we provided it and so, the fragment leaks.
To prevent it from leaking, we are using this wrapper class which will clear the callback (fragment instance)
when the action mode is destroyed. Simply this class acts between the fragment and activity by holding and clearing
the fragment from the activity preventing memory leak.
 */

class SafeActionModeCallBack(
    var callback: ActionMode.Callback?
):ActionMode.Callback {

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
        return callback?.onCreateActionMode(actionMode,menu) ?: error("callback is null")
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
        return callback?.onPrepareActionMode(actionMode,menu) ?: error("callback is null")
    }

    override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
        return callback?.onActionItemClicked(actionMode,menuItem) ?: error("callback is null")
    }

    override fun onDestroyActionMode(actionMode: ActionMode?) {
        callback?.onDestroyActionMode(actionMode) ?: error("callback is null")
        callback=null
    }
}