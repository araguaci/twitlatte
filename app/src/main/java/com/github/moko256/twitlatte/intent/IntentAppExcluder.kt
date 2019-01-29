/*
 * Copyright 2015-2019 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte.intent

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * Created by moko256 on 2018/12/14.
 *
 * @author moko256
 */

fun Intent.excludeOwnApp(context: Context, packageManager: PackageManager): Intent = run {
    val intents = packageManager
            .queryIntentActivities(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PackageManager.MATCH_ALL
                    } else {
                        PackageManager.MATCH_DEFAULT_ONLY
                    }
            )
            .map { it.activityInfo.packageName }
            .filter {
                it != context.packageName
            }.map {
                Intent(this).setPackage(it)
            }.toMutableList()

    when {
        intents.isEmpty() -> {
            Intent.createChooser(Intent(), "Open")
        }
        intents.size == 1 -> {
            intents[0]
        }
        else -> {
            Intent.createChooser(intents.removeAt(0), "Open")
                    .putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        }
    }
}