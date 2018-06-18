/*
 * Copyright (c) 2018 ThanksMister LLC
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
 *
 */

package com.thanksmister.bitcoin.localtrader

import android.content.ContentResolver
import android.os.Bundle

import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.thanksmister.bitcoin.localtrader.di.DaggerApplicationComponent
import com.thanksmister.bitcoin.localtrader.network.services.SyncUtils
import com.thanksmister.bitcoin.localtrader.network.services.SyncUtils.CONTENT_AUTHORITY
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

import io.fabric.sdk.android.Fabric
import timber.log.Timber
import com.crashlytics.android.answers.Answers



class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this);
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build())
        } else {
            Fabric.with(this, Crashlytics())
            Fabric.with(this, Answers())
            Timber.plant(CrashlyticsTree())
        }

        // set up sync
        ContentResolver.setIsSyncable(SyncUtils.getSyncAccount(this), CONTENT_AUTHORITY, 1)
        ContentResolver.setSyncAutomatically(SyncUtils.getSyncAccount(this), CONTENT_AUTHORITY, true)
        ContentResolver.addPeriodicSync(SyncUtils.getSyncAccount(this), CONTENT_AUTHORITY, Bundle.EMPTY, SyncUtils.SYNC_FREQUENCY)
    }
}
