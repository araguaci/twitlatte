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

package com.github.moko256.twitlatte.repository

import android.content.Context
import com.github.moko256.latte.client.base.entity.AccessToken
import com.github.moko256.twitlatte.database.TokenSQLiteOpenHelper

/**
 * Created by moko256 on 2018/03/29.
 *
 * @author moko256
 */
class AccountsRepository(private val context: Context) {

    private val tokens: ArrayList<AccessToken> by lazy {
        val helper = TokenSQLiteOpenHelper(context)
        val list = helper.accessTokens.asList()
        helper.close()
        ArrayList(list)
    }


    fun getAccessTokens(): List<AccessToken> = tokens

    fun get(key: String): AccessToken? =
            tokens.find { accessToken -> accessToken.getKeyString() == key }

    fun getAccessTokensByType(clientType: Int): List<AccessToken> =
            tokens.filter { accessToken -> accessToken.clientType == clientType }

    fun add(accessToken: AccessToken) {
        val helper = TokenSQLiteOpenHelper(context)
        helper.addAccessToken(accessToken)
        helper.close()
        val i = tokens.indexOf(accessToken)
        if (i >= 0) {
            tokens.removeAt(i)
        }
        tokens.add(accessToken)
    }

    fun delete(accessToken: AccessToken?) {
        if (accessToken != null) {
            val helper = TokenSQLiteOpenHelper(context)
            helper.deleteAccessToken(accessToken)
            helper.close()
            tokens.remove(accessToken)
        }
    }

    fun size(): Int = tokens.size
}