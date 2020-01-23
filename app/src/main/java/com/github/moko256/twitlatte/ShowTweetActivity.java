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

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.moko256.latte.client.base.entity.Post;
import com.github.moko256.twitlatte.entity.Client;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.base.StatusActionModel;
import com.github.moko256.twitlatte.model.impl.StatusActionModelImpl;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;

import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by moko256 on 2016/03/10.
 * This Activity is to show a tweet.
 *
 * @author moko256
 */
public class ShowTweetActivity extends AppCompatActivity {

    private CompositeDisposable disposables = new CompositeDisposable();
    private StatusActionModel statusActionModel;
    private long statusId;
    private String shareUrl = "";

    private RequestManager requestManager;
    private StatusViewBinder statusViewBinder;

    private Button tweetIsReply;
    private TextView timestampText;
    private TextView viaText;
    private FloatingActionButton replyFab;

    private Client client;

    private boolean isVisible = true;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        statusId = getIntent().getLongExtra("statusId", -1);

        client = GlobalApplicationKt.requireClient(this);
        requestManager = Glide.with(this);
        statusActionModel = new StatusActionModelImpl(
                client.getApiClient(),
                client.getPostCache()
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        tweetIsReply = findViewById(R.id.tweet_show_is_reply_text);
        statusViewBinder = new StatusViewBinder(findViewById(R.id.tweet_show_tweet));
        timestampText = findViewById(R.id.tweet_show_timestamp);
        viaText = findViewById(R.id.tweet_show_via);
        replyFab = findViewById(R.id.tweet_show_fab);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.tweet_show_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary);
        swipeRefreshLayout.setOnRefreshListener(() -> statusActionModel.updateStatus(statusId));

        disposables.addAll(
                statusActionModel.getStatusObservable().subscribe(id -> {
                    Post post = client.getPostCache().getPost(statusId);
                    if (post != null) {
                        if (!isVisible) {
                            isVisible = true;
                            swipeRefreshLayout.getChildAt(0).setVisibility(VISIBLE);
                        }
                        updateView(post);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }),

                statusActionModel.getDidActionObservable().subscribe(
                        it -> Toast.makeText(
                                this,
                                TwitterStringUtils.getDidActionStringRes(
                                        client.getAccessToken().getClientType(), it
                                ),
                                Toast.LENGTH_SHORT
                        ).show()
                ),

                statusActionModel.getErrorObservable().subscribe(error -> {
                    error.printStackTrace();
                    Toast.makeText(
                            this,
                            error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    swipeRefreshLayout.setRefreshing(false);
                    if (client.getPostCache().getPost(statusId) == null) {
                        finish();
                    }
                })
        );

        Post status = client.getPostCache().getPost(statusId);

        if (status != null) {
            updateView(status);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            isVisible = false;
            swipeRefreshLayout.getChildAt(0).setVisibility(GONE);
            statusActionModel.updateStatus(statusId);
        }
    }

    @Override
    protected void onDestroy() {
        disposables.dispose();
        super.onDestroy();
        disposables = null;
        if (statusViewBinder != null) {
            statusViewBinder.clear();
        }
        statusViewBinder = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_tweet_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_quote:
                startActivity(PostActivity.getIntent(
                        this,
                        shareUrl + " "
                ));
                break;
            case R.id.action_share:
                startActivity(Intent.createChooser(
                        new Intent()
                                .setAction(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, shareUrl),
                        getString(R.string.share)));
                break;
            case R.id.action_open_in_browser:
                AppCustomTabsKt.launchChromeCustomTabs(this, shareUrl, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static Intent getIntent(Context context, long statusId) {
        return new Intent(context, ShowTweetActivity.class).putExtra("statusId", statusId);
    }

    private void updateView(Post item) {
        shareUrl = item.getStatus().getUrl();
        long replyTweetId = item.getStatus().getInReplyToStatusId();
        if (replyTweetId != -1) {
            tweetIsReply.setVisibility(VISIBLE);
            tweetIsReply.setOnClickListener(v -> startActivity(
                    GlobalApplicationKt.setAccountKeyForActivity(
                            getIntent(this, replyTweetId),
                            this
                    )
            ));
        } else {
            tweetIsReply.setVisibility(GONE);
        }

        statusViewBinder.getTweetSpoilerText().setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.did_copy, Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("spoiler_text", item.getStatus().getSpoilerText()));
            return true;
        });

        statusViewBinder.getTweetContext().setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.did_copy, Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("post_context", item.getStatus().getText()));
            return true;
        });

        statusViewBinder.getUserImage().setOnClickListener(v -> {
            ActivityOptionsCompat animation = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            this,
                            v,
                            "icon_image"
                    );
            startActivity(
                    GlobalApplicationKt.setAccountKeyForActivity(
                            ShowUserActivity.getIntent(this, item.getUser().getId()),
                            this
                    ),
                    animation.toBundle()
            );
        });

        statusViewBinder.setOnQuotedStatusClicked(v -> startActivity(
                GlobalApplicationKt.setAccountKeyForActivity(
                        ShowTweetActivity.getIntent(this, item.getQuotedRepeatingStatus().getId()),
                        this
                )
        ));

        statusViewBinder.setOnCardClicked(
                v -> AppCustomTabsKt.launchChromeCustomTabs(
                        this,
                        item.getStatus().getCard().getUrl(),
                        false
                )
        );

        statusViewBinder.getLikeButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                statusActionModel.createFavorite(item.getId());
            } else {
                statusActionModel.removeFavorite(item.getId());
            }
            return Unit.INSTANCE;
        });

        statusViewBinder.getRepeatButton().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                statusActionModel.createRepeat(item.getId());
            } else {
                statusActionModel.removeRepeat(item.getId());
            }
            return Unit.INSTANCE;
        });

        View.OnClickListener replyOnClickListener = v -> startActivity(PostActivity.getIntent(
                this,
                item.getStatus().getId(),
                TwitterStringUtils.convertToReplyTopString(
                        client.getUserCache().get(client.getAccessToken().getUserId()).getScreenName(),
                        item.getUser().getScreenName(),
                        item.getStatus().getMentions()
                ).toString()
        ));
        statusViewBinder.getReplyButton().setOnClickListener(replyOnClickListener);
        replyFab.setOnClickListener(replyOnClickListener);

        statusViewBinder.setStatus(
                client,
                requestManager,
                item.getRepeatedUser(),
                item.getRepeat(),
                item.getUser(),
                item.getStatus(),
                item.getQuotedRepeatingUser(),
                item.getQuotedRepeatingStatus()
        );

        statusViewBinder.getSendVote().setOnClickListener(v ->
                statusActionModel
                        .sendVote(
                                statusId,
                                item.getStatus().getPoll().getId(),
                                statusViewBinder.getPollAdapter().getSelections()
                        )
        );

        timestampText.setText(
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                        .format(item.getStatus().getCreatedAt())
        );

        if (item.getStatus().getSourceName() != null) {
            viaText.setText(TwitterStringUtils.appendLinkAtViaText(item.getStatus().getSourceName(), item.getStatus().getSourceWebsite()));
            viaText.setMovementMethod(new LinkMovementMethod());
        } else {
            viaText.setVisibility(GONE);
        }
    }
}