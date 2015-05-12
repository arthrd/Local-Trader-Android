/*
 * Copyright (c) 2015 ThanksMister LLC
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

package com.thanksmister.bitcoin.localtrader.ui.advertisements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thanksmister.bitcoin.localtrader.BaseActivity;
import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.Advertisement;
import com.thanksmister.bitcoin.localtrader.data.api.model.TradeType;
import com.thanksmister.bitcoin.localtrader.data.database.DbManager;
import com.thanksmister.bitcoin.localtrader.data.database.MethodItem;
import com.thanksmister.bitcoin.localtrader.data.services.DataService;
import com.thanksmister.bitcoin.localtrader.ui.search.TradeRequestActivity;
import com.thanksmister.bitcoin.localtrader.utils.Dates;
import com.thanksmister.bitcoin.localtrader.utils.Strings;
import com.thanksmister.bitcoin.localtrader.utils.TradeUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subscriptions.Subscriptions;

import static rx.android.app.AppObservable.bindActivity;

public class AdvertiserActivity extends BaseActivity
{
    public static final String EXTRA_AD_ID = "com.thanksmister.extras.EXTRA_AD_ID";

    @Inject
    DataService dataService;
    
    @Inject
    DbManager dbManager;

    @InjectView(android.R.id.progress)
    View progress;

    @InjectView(R.id.advertiserContent)
    ScrollView content;

    @InjectView(android.R.id.empty)
    View empty;
    
    @InjectView(R.id.buttonLayout)
    View buttonLayout;
    
    @InjectView(R.id.priceLayout)
    View priceLayout;

    @InjectView(R.id.buttonLayoutDivider)
    View buttonLayoutDivider;

    @InjectView(R.id.priceLayoutDivider)
    View priceLayoutDivider;

    @InjectView(R.id.retryTextView)
    TextView emptyTextView;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.tradePrice)
    TextView tradePrice;

    @InjectView(R.id.traderName)
    TextView traderName;

    @InjectView(R.id.tradeLimit)
    TextView tradeLimit;

    @InjectView(R.id.tradeTerms)
    TextView tradeTerms;

    @InjectView(R.id.tradeFeedback)
    TextView tradeFeedback;

    @InjectView(R.id.tradeCount)
    TextView tradeCount;

    @Optional
    @InjectView(R.id.dateText)
    TextView dateText;

    @InjectView(R.id.noteTextAdvertiser)
    TextView noteTextAdvertiser;

    @InjectView(R.id.lastSeenIcon)
    View lastSeenIcon;
    
    @OnClick(R.id.requestButton)
    public void requestButtonClicked()
    {
        showTradeRequest();
    }
    
    private String adId;
    private Observable<List<MethodItem>> methodObservable;
    private Observable<Advertisement> advertisementObservable;
    private Subscription subscription = Subscriptions.empty();
    private AdvertisementData advertisementData;

    private class AdvertisementData {
        public Advertisement advertisement;
        public MethodItem method;
    }

    public static Intent createStartIntent(Context context, String adId)
    {
        Intent intent = new Intent(context, AdvertiserActivity.class);
        intent.putExtra(EXTRA_AD_ID, adId);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.view_advertiser);

        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            adId = getIntent().getStringExtra(EXTRA_AD_ID);
        } else {
            adId = savedInstanceState.getString(EXTRA_AD_ID);
        }
        
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            setToolBarMenu(toolbar);
        }
    
        methodObservable = bindActivity(this, dbManager.methodQuery().cache());
        advertisementObservable = bindActivity(this, dataService.getAdvertisement(adId).cache());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        
        outState.putSerializable(EXTRA_AD_ID, adId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(toolbar != null)
            toolbar.inflateMenu(R.menu.searchresults);

        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        subscribeData();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        subscription.unsubscribe();
    }
    
    public void setToolBarMenu(Toolbar toolbar)
    {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {

                switch (menuItem.getItemId()) {
                    case R.id.action_profile:
                        showProfile();
                        return true;
                    case R.id.action_location:
                        showAdvertisementOnMap();
                        return true;
                    case R.id.action_website:
                        showPublicAdvertisement();
                        return true;
                }
                return false;
            }
        });
    }

    public void showError(String message)
    {
        progress.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
        emptyTextView.setText(message);
    }
    
    public void showProgress()
    {
        progress.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }
    public void hideProgress()
    {
        progress.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    protected void subscribeData()
    {
        subscription = Observable.combineLatest(methodObservable, advertisementObservable, new Func2<List<MethodItem>, Advertisement, AdvertisementData>()
        {
            @Override
            public AdvertisementData call(List<MethodItem> methods, Advertisement advertisement)
            {
                MethodItem method = TradeUtils.getMethodForAdvertisement(advertisement, methods);

                advertisementData = new AdvertisementData();
                advertisementData.method = method;
                advertisementData.advertisement = advertisement;

                return advertisementData;
            }
        }).subscribe(new Action1<AdvertisementData>()
        {
            @Override
            public void call(AdvertisementData advertisementData)
            {
                hideProgress();

                if(TradeUtils.isOnlineTrade(advertisementData.advertisement)) {
                    setAdvertisement(advertisementData.advertisement, advertisementData.method);
                } else {
                    setAdvertisement(advertisementData.advertisement, null);
                }
            }
        }, new Action1<Throwable>()
        {
            @Override
            public void call(Throwable throwable)
            {
                showError("Unable to retrieve advertisement data.");
            }
        });
    }

    public void setAdvertisement(Advertisement advertisement, MethodItem method)
    {
        setHeader(advertisement.trade_type);
        
        String location = (TradeUtils.isLocalTrade(advertisement))? advertisement.location:advertisement.location + " (" + advertisement.distance + " km)";
        String provider = TradeUtils.getPaymentMethod(advertisement, method);
        
        switch (advertisement.trade_type) {
            case ONLINE_SELL:
                noteTextAdvertiser.setText(Html.fromHtml(getString(R.string.advertiser_notes_text_online, "sell", advertisement.currency, provider)));
                break;
            case LOCAL_SELL:
                noteTextAdvertiser.setText(Html.fromHtml(getString(R.string.advertiser_notes_text_locally, "sell", advertisement.currency, location)));
                break;
            case ONLINE_BUY:
                noteTextAdvertiser.setText(Html.fromHtml(getString(R.string.advertiser_notes_text_online, "buy your", advertisement.currency, provider)));
                break;
            case LOCAL_BUY:
                noteTextAdvertiser.setText(Html.fromHtml(getString(R.string.advertiser_notes_text_locally, "buy your", advertisement.currency, location)));
                break;
        }

        if(advertisement.isATM()) {
            priceLayout.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.GONE);
            priceLayoutDivider.setVisibility(View.GONE);
            buttonLayoutDivider.setVisibility(View.GONE);
            tradePrice.setText("ATM");
            noteTextAdvertiser.setText(Html.fromHtml(getString(R.string.advertiser_notes_text_atm, advertisement.currency, location)));
        } else {
            tradePrice.setText(getString(R.string.trade_price, advertisement.temp_price, advertisement.currency));
        }
        
        traderName.setText(advertisement.profile.username);
        
        if(advertisement.isATM()) {
            tradeLimit.setText("");
        } else if(advertisement.min_amount == null) {
            tradeLimit.setText("");
        } else if(advertisement.max_amount == null) {
            tradeLimit.setText(getString(R.string.trade_limit_min, advertisement.min_amount, advertisement.currency));
        } else { // no maximum set
            tradeLimit.setText(getString(R.string.trade_limit, advertisement.min_amount, advertisement.max_amount_available, advertisement.currency));
        }

        if(!Strings.isBlank(advertisement.message)){
            tradeTerms.setText(Html.fromHtml(advertisement.message.trim()));
            tradeTerms.setMovementMethod(LinkMovementMethod.getInstance());
        }

        tradeFeedback.setText(advertisement.profile.feedback_score);
        tradeCount.setText(advertisement.profile.trade_count);
        lastSeenIcon.setBackgroundResource(TradeUtils.determineLastSeenIcon(advertisement.profile.last_online));
        String date = Dates.parseLocalDateStringAbbreviatedTime(advertisement.profile.last_online);
        dateText.setText("Last Seen - " + date);
    }

    public void setHeader(TradeType tradeType)
    {
        String header = "";
        switch (tradeType) {
            case ONLINE_SELL:
            case ONLINE_BUY:
                header = (tradeType == TradeType.ONLINE_SELL) ? "Online Seller" : "Online Buyer";
                break;
            case LOCAL_SELL:
            case LOCAL_BUY:
                header = (tradeType == TradeType.LOCAL_SELL) ? "Local Seller" : "Local Buyer";
                break;
        }

        toolbar.setTitle(header);
    }
    
    public void showTradeRequest()
    {
        if(advertisementData == null) return;
        Advertisement advertisement = advertisementData.advertisement;
        Intent intent = TradeRequestActivity.createStartIntent(this, advertisement.ad_id, advertisement.temp_price, advertisement.min_amount, advertisement.max_amount, advertisement.currency, advertisement.profile.username);
        startActivity(intent);
    }
    
    public void showPublicAdvertisement()
    {
        if(advertisementData == null) return;
        Advertisement advertisement = advertisementData.advertisement;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(advertisement.actions.public_view));
        startActivity(browserIntent);
    }
    
    public void showProfile()
    {
        if(advertisementData == null) return;
        Advertisement advertisement = advertisementData.advertisement;
        String url = "https://localbitcoins.com/accounts/profile/" + advertisement.profile.username + "/";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
    
    public void showAdvertisementOnMap()
    {
        if(advertisementData == null) return;
        Advertisement advertisement = advertisementData.advertisement;
        String geoUri = "";
        if(advertisement.trade_type == TradeType.LOCAL_BUY || advertisement.trade_type == TradeType.LOCAL_SELL) {
            geoUri = "http://maps.google.com/maps?q=loc:" + advertisement.lat + "," + advertisement.lon + " (" + advertisement.location + ")";
        } else {
            geoUri = "geo:0,0?q=" + advertisement.location;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        startActivity(intent);
    }
}