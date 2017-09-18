/*
 * Copyright (c) 2017 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package dpreference;


import android.content.Context;

/**
 * Created by wangyida on 15-4-9.
 */
public class DPreference {

    Context mContext;

    /**
     * preference file name
     */
    String mName;

    private DPreference() {
    }

    public DPreference(Context context, String name) {
        this.mContext = context;
        this.mName = name;
    }

    public String getString(final String key, final String defaultValue) {
        return PrefAccessor.getString(mContext, mName, key, defaultValue);
    }

    public void putString(final String key, final String value) {
        PrefAccessor.setString(mContext, mName, key, value);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return PrefAccessor.getBoolean(mContext, mName, key, defaultValue);
    }

    public void putBoolean(final String key, final boolean value) {
        PrefAccessor.setBoolean(mContext, mName, key, value);
    }

    public void putInt(final String key, final int value) {
        PrefAccessor.setInt(mContext, mName, key, value);
    }

    public int getInt(final String key, final int defaultValue) {
        return PrefAccessor.getInt(mContext, mName, key, defaultValue);
    }

    public void putLong(final String key, final long value) {
        PrefAccessor.setLong(mContext, mName, key, value);
    }

    public long getLong(final String key, final long defaultValue) {
        return PrefAccessor.getLong(mContext, mName, key, defaultValue);
    }

    public void removePreference(final String key) {
        PrefAccessor.remove(mContext, mName, key);
    }
}