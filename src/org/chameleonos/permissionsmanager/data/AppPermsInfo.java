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

package org.chameleonos.permissionsmanager.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppPermsInfo implements Comparable {
    public PackageInfo mPkgInfo;
    public List<String> mRevokedPerms;
    public Drawable mIcon;
    public String mAppName;

    public AppPermsInfo(Context context, PackageInfo pi) {
        PackageManager pm = context.getPackageManager();
        mAppName = pm.getApplicationLabel(pi.applicationInfo).toString();
        String[] revokedPerms = pm.getRevokedPermissions(pi.packageName);
        mRevokedPerms = new ArrayList<String>(Arrays.asList(revokedPerms));
        mPkgInfo = pi;
        mIcon = null;
    }

    @Override
    public int compareTo(Object o) {
        return this.mAppName.toString().compareTo(((AppPermsInfo)o).mAppName);
    }
}
