/*
 * Copyright 2016 The twicalico authors
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

package com.github.moko256.twicalico;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import twitter4j.GeoLocation;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterException;

/**
 * Created by moko256 on 2017/07/05.
 *
 * @author moko256
 */

public class TrendsFragment extends BaseListFragment {
    TrendsAdapter adapter;
    ArrayList<Trend> list;

    CompositeSubscription subscription;

    CachedTrendsSQLiteOpenHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        list=new ArrayList<>();
        subscription = new CompositeSubscription();
        helper = new CachedTrendsSQLiteOpenHelper(getContext());
        if (savedInstanceState == null) {
            ArrayList<Trend> trends = helper.getTrends();
            if (trends.size() > 0){
                list = trends;
                setProgressCircleLoading(false);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=super.onCreateView(inflater, container, savedInstanceState);

        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view)==0){
                    outRect.top=Math.round(getContext().getResources().getDisplayMetrics().density*8f);
                }
            }
        });

        adapter=new TrendsAdapter(getContext(), list);
        setAdapter(adapter);
        if (!isInitializedList()){
            adapter.notifyDataSetChanged();
        }

        return view;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            ArrayList<Trend> l=(ArrayList<Trend>) savedInstanceState.getSerializable("list");
            if(l!=null){
                list.addAll(l);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        subscription = null;
        helper.close();
        helper = null;
        list=null;
    }

    @Override
    protected void onInitializeList() {
        switch (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)){
            case PermissionChecker.PERMISSION_GRANTED:
                subscription.add(
                        getResponseObservable()
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        result-> {
                                            list.clear();
                                            list.addAll(Arrays.asList(result.getTrends()));
                                            adapter.notifyDataSetChanged();
                                        },
                                        e -> {
                                            e.printStackTrace();
                                            Snackbar.make(getSnackBarParentContainer(), getContext().getString(R.string.error_occurred_with_error_code,
                                                    ((TwitterException) e).getErrorCode()), Snackbar.LENGTH_INDEFINITE)
                                                    .setAction(R.string.retry, v -> onInitializeList())
                                                    .show();
                                        },
                                        () -> {
                                            getSwipeRefreshLayout().setRefreshing(false);
                                            setProgressCircleLoading(false);
                                        }
                                )
                );
                break;
            case PermissionChecker.PERMISSION_DENIED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 400);
                }
                break;
            default:
                getSwipeRefreshLayout().setRefreshing(false);
                setProgressCircleLoading(false);
        }
    }

    @Override
    protected void onUpdateList() {
        onInitializeList();
    }

    @Override
    protected void onLoadMoreList() {}

    @Override
    protected boolean isInitializedList() {
        return !list.isEmpty();
    }

    @Override
    protected RecyclerView.LayoutManager initializeRecyclerViewLayoutManager() {
        return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            onInitializeList();
        }
    }

    public Observable<Trends> getResponseObservable() {
        return Observable.create(
                subscriber->{
                    try {
                        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if (location != null) {
                            subscriber.onNext(getTrends(location));
                            subscriber.onCompleted();
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    locationManager.removeUpdates(this);
                                    try {
                                        subscriber.onNext(getTrends(location));
                                    } catch (TwitterException e) {
                                        subscriber.onError(e);
                                    }
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {}

                                @Override
                                public void onProviderEnabled(String s) {}

                                @Override
                                public void onProviderDisabled(String s) {}
                            }, Looper.getMainLooper());
                        }
                    } catch (TwitterException | SecurityException e) {
                        subscriber.onError(e);
                    }
                }
        );
    }

    private Trends getTrends(Location location) throws TwitterException {
        Trends result = GlobalApplication.twitter
                .getPlaceTrends(GlobalApplication.twitter.getClosestTrends(
                        new GeoLocation(location.getLatitude(), location.getLongitude())
                ).get(0).getWoeid());

        helper.setTrends(Arrays.asList(result.getTrends()));
        return result;
    }

}