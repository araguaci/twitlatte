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

package com.github.moko256.twitlatte;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.twitlatte.widget.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.github.moko256.latte.client.mastodon.MastodonApiClientKt.CLIENT_TYPE_MASTODON;

/**
 * Created by moko256 on 2017/01/15.
 *
 * @author moko256
 */

public class ShowUserFragmentsPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_TIMELINE = 1;
    private static final int FRAGMENT_LIKE = 2;
    private static final int FRAGMENT_MEDIA = 3;
    private static final int FRAGMENT_FOLLOW = 4;
    private static final int FRAGMENT_FOLLOWER = 5;
    private static final int FRAGMENT_LIST = 6;


    final private List<Integer> list;

    final private int type;
    final private long accountUserId;
    final private Context context;
    private long userId = -1;

    ShowUserFragmentsPagerAdapter(AccessToken accessToken, FragmentManager fm, Context context) {
        super(fm);

        list = new ArrayList<>(4);

        this.type = accessToken.getClientType();
        this.accountUserId = accessToken.getUserId();
        this.context = context;
    }

    public void setUserId(long userId) {
        if (this.userId == -1) {
            this.userId = userId;

            list.add(FRAGMENT_TIMELINE);
            if (type == CLIENT_TYPE_MASTODON) {
                list.add(FRAGMENT_MEDIA);
            }
            list.add(FRAGMENT_FOLLOW);
            list.add(FRAGMENT_FOLLOWER);
            if (!(type == CLIENT_TYPE_MASTODON && userId != accountUserId)) {
                list.add(list.size() - 2, FRAGMENT_LIKE);
                list.add(FRAGMENT_LIST);
            }

            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (list.get(position)) {
            case FRAGMENT_TIMELINE:
                return UserTimelineFragment.Companion.newInstance(userId);
            case FRAGMENT_LIKE:
                return UserLikeFragment.Companion.newInstance(userId);
            case FRAGMENT_MEDIA:
                return MediaTimelineFragment.Companion.newInstance(userId);
            case FRAGMENT_FOLLOW:
                return UserFollowsFragment.Companion.newInstance(userId);
            case FRAGMENT_FOLLOWER:
                return UserFollowersFragment.Companion.newInstance(userId);
            case FRAGMENT_LIST:
                return ListsEntriesFragment.Companion.newInstance(userId);
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getFragment(position);
        if (fragment instanceof ToolbarTitleInterface) {
            return context.getString(((ToolbarTitleInterface) fragment).getTitleResourceId());
        } else {
            return null;
        }
    }
}
