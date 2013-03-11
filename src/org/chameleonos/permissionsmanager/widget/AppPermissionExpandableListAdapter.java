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

package org.chameleonos.permissionsmanager.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import org.chameleonos.permissionsmanager.R;
import org.chameleonos.permissionsmanager.data.AppPermsInfo;

import java.util.List;

/**
 * @author Clark Scheff
 */
public class AppPermissionExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<AppPermsInfo> mInstalledPackages;
    private PackageManager mPm;

    /**
     *
     */
    public AppPermissionExpandableListAdapter() {
    }

    public AppPermissionExpandableListAdapter(Context context, List<AppPermsInfo> installedPackages) {
        mContext = context;
        mInstalledPackages = installedPackages;
        mPm = context.getPackageManager();
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getChild(int, int)
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getChildId(int, int)
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_child_item, null, false);
        }
        PermissionView pv = (PermissionView) convertView.findViewById(R.id.perm);
        AppPermsInfo api = mInstalledPackages.get(groupPosition);
        String permName = api.mPkgInfo.requestedPermissions[childPosition];
        pv.setText(permName.substring(permName.lastIndexOf('.') + 1));
        pv.setChecked(api.mRevokedPerms.contains(permName));

        return convertView;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        String[] perms = mInstalledPackages.get(groupPosition).mPkgInfo.requestedPermissions;
        return perms == null ? 0 : perms.length;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getGroup(int)
     */
    @Override
    public Object getGroup(int groupPosition) {
        return mInstalledPackages.get(groupPosition);
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getGroupCount()
     */
    @Override
    public int getGroupCount() {
        return mInstalledPackages.size();
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getGroupId(int)
     */
    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_group_item, null, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.app);
        AppPermsInfo api = mInstalledPackages.get(groupPosition);
        tv.setText(api.mAppName);
        if (api.mIcon == null) {
            try {
                api.mIcon = mPm.getApplicationIcon(mInstalledPackages.get(groupPosition).mPkgInfo.packageName);
            } catch (NameNotFoundException e) {
                api.mIcon = mPm.getDefaultActivityIcon();
            }
        }
        tv.setCompoundDrawablesWithIntrinsicBounds(api.mIcon, null, null, null);
        tv.setCompoundDrawablePadding(10);

        return convertView;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
