/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mobile.gauges.ui;

import static android.view.animation.Animation.INFINITE;
import static com.github.mobile.gauges.ui.ToastUtil.toastOnUiThread;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.gauges.R.anim;
import com.github.mobile.gauges.R.id;
import com.github.mobile.gauges.R.layout;
import com.github.mobile.gauges.R.menu;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.List;

import roboguice.fragment.RoboListFragment;

/**
 * List loading fragment for a specific type
 *
 * @param <E>
 */
public abstract class ListLoadingFragment<E> extends RoboListFragment implements LoaderCallbacks<List<E>> {

    private MenuItem refreshItem;
    private ViewHoldingListAdapter listAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.gauges, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.refresh:
            refreshItem = item;
            refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        final Activity activity = getActivity();
        if(activity == null)
            return;

        /* Attach a rotating ImageView to the refresh item as an ActionView */
        View iv = activity.getLayoutInflater().inflate(layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(activity, anim.clockwise_refresh);
        rotation.setRepeatCount(INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);

        getLoaderManager().restartLoader(0, null, this);
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        setList(items);

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);

        if (refreshItem != null && refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }

    /**
     * Allows you to update the list's items without using setListAdapter(), which makes the list jump back to the top.
     */
    private void setList(List<E> items) {
        if (listAdapter == null) {
            setListAdapter(listAdapter = adapterFor(items));
        } else {
            listAdapter.setList(items);
        }
    }

    /**
     * Create adapter for list of items
     *
     * @param items
     * @return list adapter
     */
    protected abstract ViewHoldingListAdapter<E> adapterFor(List<E> items);

    @Override
    public void onLoaderReset(Loader<List<E>> listLoader) {
    }

    /**
     * Show message via a {@link Toast}
     * <p>
     * This method ensures the {@link Toast} is displayed on the UI thread and so it may be called from any thread
     *
     * @param message
     */
    protected void showError(final int message) {
        final Activity activity = getActivity();
        if (activity != null)
            toastOnUiThread(activity, getString(message));
    }
}
