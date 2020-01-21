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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.moko256.latte.client.base.entity.AccessToken;
import com.github.moko256.latte.client.base.entity.User;
import com.github.moko256.twitlatte.api.MediaUrlConverterGeneratorKt;
import com.github.moko256.twitlatte.text.TwitterStringUtils;
import com.github.moko256.twitlatte.view.DpToPxKt;

import java.util.Collections;
import java.util.List;

/**
 * Created by moko256 on 2017/10/26.
 *
 * @author moko256
 */

public class SelectAccountsAdapter extends RecyclerView.Adapter<SelectAccountsAdapter.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 0;
    private static final int VIEW_TYPE_IMAGE_SELECTED = 1;
    private static final int VIEW_TYPE_ADD = 2;
    private static final int VIEW_TYPE_REMOVE = 3;

    private final Context context;

    private List<User> users = Collections.emptyList();
    private List<AccessToken> accessTokens = Collections.emptyList();

    public OnImageClickListener onSelectionChangedListener;
    public View.OnClickListener onAddButtonClickListener;
    public View.OnClickListener onRemoveButtonClickListener;

    private int selectionPosition = -1;
    private final int selectionColor;

    public SelectAccountsAdapter(Context context) {
        this.context = context;

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, typedValue, true);
        selectionColor = typedValue.resourceId;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < accessTokens.size()) ?
                (position == selectionPosition) ?
                        VIEW_TYPE_IMAGE_SELECTED :
                        VIEW_TYPE_IMAGE :
                (position == accessTokens.size()) ?
                        VIEW_TYPE_ADD :
                        VIEW_TYPE_REMOVE;
    }

    @NonNull
    @Override
    public SelectAccountsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_IMAGE || viewType == VIEW_TYPE_IMAGE_SELECTED ?
                R.layout.layout_select_accounts_image_child :
                R.layout.layout_select_accounts_resource_image;
        return new ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectAccountsAdapter.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_IMAGE_SELECTED:
            case VIEW_TYPE_IMAGE: {
                User user = users.get(position);
                AccessToken accessToken = accessTokens.get(position);

                if (user != null) {
                    String uri =
                            MediaUrlConverterGeneratorKt.generateMediaUrlConverter(
                                    accessToken.getClientType()
                            ).convertProfileIconUriBySize(
                                    user,
                                    DpToPxKt.dpToPx(context, 40)
                            );

                    holder.title.setText(TwitterStringUtils.plusAtMark(user.getScreenName(), accessToken.getUrl()));
                    Glide
                            .with(holder.itemView)
                            .load(uri)
                            .circleCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.image);
                } else {
                    holder.title.setText(TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl()));
                }

                if (type == VIEW_TYPE_IMAGE_SELECTED) {
                    holder.itemView.setBackgroundResource(selectionColor);
                } else {
                    holder.itemView.setBackground(null);
                    holder.itemView.setOnClickListener(v -> {
                        if (onSelectionChangedListener != null) {
                            onSelectionChangedListener.onClick(accessToken);
                        }
                    });
                }
                break;
            }
            case VIEW_TYPE_ADD: {
                holder.image.setImageResource(R.drawable.ic_add_white_24dp);
                holder.title.setText(R.string.login_with_another_account);
                holder.itemView.setOnClickListener(onAddButtonClickListener);
                break;
            }
            case VIEW_TYPE_REMOVE: {
                holder.image.setImageResource(R.drawable.ic_remove_black_24dp);
                holder.title.setText(R.string.do_logout);
                holder.itemView.setOnClickListener(onRemoveButtonClickListener);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return accessTokens.size() + 2;
    }

    public void updateAccounts(List<User> userList, List<AccessToken> accessTokenList, AccessToken current) {
        users = userList;
        accessTokens = accessTokenList;
        selectionPosition = accessTokenList.indexOf(current);
        notifyDataSetChanged();
    }

    public void updateSelectedPosition(AccessToken key) {
        int old = selectionPosition;
        selectionPosition = accessTokens.indexOf(key);
        notifyItemChanged(old);
        notifyItemChanged(selectionPosition);
    }

    final static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.layout_images_adapter_image);
            title = itemView.findViewById(R.id.layout_images_adapter_title);
        }
    }

    public interface OnImageClickListener {
        void onClick(AccessToken accessToken);
    }
}
