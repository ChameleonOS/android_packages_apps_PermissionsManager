/*
 * Copyright (C) 2013 The ChameleonOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chameleonos.permissionsmanager.activity;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Switch;
import org.chameleonos.permissionsmanager.R;
import org.chameleonos.permissionsmanager.data.AppPermsInfo;
import org.chameleonos.permissionsmanager.widget.AppPermissionExpandableListAdapter;
import org.chameleonos.permissionsmanager.widget.PermissionView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionManagerActivity extends ExpandableListActivity
        implements CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemLongClickListener, ExpandableListView.OnGroupClickListener {
    private Context mContext;
    private Handler mHandler = new Handler();
    private PackageManager mPm;
    private Switch mEnabled;
    private int mLastExpandedGroup = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        mContext = this;
        mPm = getPackageManager();
        getInstalledAppsList();
        ExpandableListView elv = getExpandableListView();
        elv.setGroupIndicator(null);
        elv.setDividerHeight(0);
        elv.setOnItemLongClickListener(this);
        elv.setOnGroupClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manager, menu);
        MenuItem item = menu.getItem(0);
        mEnabled = (Switch) item.getActionView();
        mEnabled.setChecked(Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.ENABLE_PERMISSIONS_MANAGEMENT, 0) == 1);
        mEnabled.setOnCheckedChangeListener(this);
        return true;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        SharedPreferences prefs = getPreferences(0);
        if (!prefs.getBoolean("show_disclaimer", false)) {
            showDisclaimer();
        }
    }

    @Override
    public void onGroupExpand(final int groupPosition) {
        if (groupPosition != mLastExpandedGroup)
            getExpandableListView().collapseGroup(mLastExpandedGroup);

        super.onGroupExpand(groupPosition);
        mLastExpandedGroup = groupPosition;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        // Implement this method to scroll to the correct position as this doesn't
        // happen automatically if we override onGroupExpand() as above
        parent.smoothScrollToPosition(groupPosition);

        // Need default behaviour here otherwise group does not get expanded/collapsed
        // on click
        if (parent.isGroupExpanded(groupPosition)) {
            parent.collapseGroup(groupPosition);
        } else {
            parent.expandGroup(groupPosition);
        }

        return true;
    }

    private void showDisclaimer() {
        (new AlertDialog.Builder(mContext))
                .setTitle(R.string.dlg_disclaimer_title)
                .setMessage(R.string.dlg_disclaimer_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(R.string.dlg_disclaimer_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = getPreferences(0).edit();
                        editor.putBoolean("show_disclaimer", true).commit();
                    }
                })
                .setNegativeButton(R.string.dlg_disclaimer_tldr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showTldr();
                            }
                        });
                    }
                })
                .show();
    }

    private void showTldr() {
        (new AlertDialog.Builder(mContext))
                .setTitle(R.string.dlg_disclaimer_title)
                .setMessage(R.string.dlg_tldr_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(R.string.dlg_disclaimer_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = getPreferences(0).edit();
                        editor.putBoolean("show_disclaimer", true).commit();
                    }
                })
                .setNegativeButton(R.string.dlg_tldr_disagree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    private void getInstalledAppsList() {
        mHandler.post(new Runnable() {
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> allPackages = pm
                        .getInstalledPackages(PackageManager.GET_PERMISSIONS);
                List<AppPermsInfo> installedPackages = new ArrayList<AppPermsInfo>();
                for (int i = 0; i < allPackages.size(); i++) {
                    PackageInfo pi = allPackages.get(i);
                    if (!isThisASystemPackage(pi)) {
                        if (pi.requestedPermissions != null && pi.requestedPermissions.length > 0)
                            installedPackages.add(new AppPermsInfo(mContext, pi));
                    }
                }
                Collections.sort(installedPackages);
                AppPermissionExpandableListAdapter adapter = new AppPermissionExpandableListAdapter(
                        mContext, installedPackages);
                PermissionManagerActivity.this.setListAdapter(adapter);
            }
        });
    }

    private static boolean isThisASystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View child, int groupPosition,
                                int childPosition, long id) {
        PermissionView pv = (PermissionView) child.findViewById(R.id.perm);
        AppPermsInfo api = (AppPermsInfo) getExpandableListAdapter().getGroup(groupPosition);
        pv.toggle();
        if (pv.isChecked()) {
            api.mRevokedPerms.add(api.mPkgInfo.requestedPermissions[childPosition]);
            String[] revoked = new String[api.mRevokedPerms.size()];
            api.mRevokedPerms.toArray(revoked);
            mPm.setRevokedPermissions(api.mPkgInfo.packageName, revoked);
        } else {
            api.mRevokedPerms.remove(api.mPkgInfo.requestedPermissions[childPosition]);
            String[] revoked = new String[api.mRevokedPerms.size()];
            api.mRevokedPerms.toArray(revoked);
            mPm.setRevokedPermissions(api.mPkgInfo.packageName, revoked);
        }
        pv.invalidate();
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean newValue) {
        if (compoundButton == mEnabled) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.ENABLE_PERMISSIONS_MANAGEMENT,
                    newValue == true ? 1 : 0);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                                   int position, long id) {
        long pos = getExpandableListView().getExpandableListPosition(position);
        if (ExpandableListView.getPackedPositionType(pos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPosition = ExpandableListView.getPackedPositionGroup(pos);
            int childPosition = ExpandableListView.getPackedPositionChild(pos);
            AppPermsInfo api = (AppPermsInfo) getExpandableListAdapter().getGroup(groupPosition);
            if (api != null) {
                showPermInfo(api.mPkgInfo.requestedPermissions[childPosition]);
            }
            return true;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Simple little dialog to show a description of the permission identified by permName
     *
     * @param permName Permission to show full description of
     */
    private void showPermInfo(String permName) {
        PermissionInfo pi = null;
        try {
            pi = mPm.getPermissionInfo(permName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
        CharSequence description = pi.loadDescription(mPm);
        if (TextUtils.isEmpty(description))
            return;
        permName = permName.substring(permName.lastIndexOf('.') + 1);
        (new AlertDialog.Builder(mContext)).setTitle(permName)
                .setMessage(description)
                .setPositiveButton(R.string.dlg_perm_info_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }
}