package io.binroot.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.ArrayList;
import java.util.HashMap;

public class GameActivity extends BaseGameActivity {
    private static final String TAG = GameActivity.class.getSimpleName();
    private final static int REQUEST_LEADERBOARD = 1337;
    private final static int REQUEST_ACHIEVEMENTS = 31337;
    private final static String LEADERBOARD_ID = "CgkI6MmA6IQKEAIQAA";

    FrameLayout mSellYellow;
    FrameLayout mBuyYellow;
    Button mSellButton;
    Button mBuyButton;
    Button mPlayButton;
    StockPriceView mStockPriceView;
    TextView mSharesText;
    TextView mMoneyText;
    TextView mCurPriceText;
    private double mMoney = 100;
    private int mShares = 0;
    CardFrontFragment mFrontFragment;
    CardBackFragment mBackFragment;
    Typeface tf1;
    Typeface tf2;
    private boolean mShowingBack = false;
    ArrayList<PointF> mBuyPoints = new ArrayList<PointF>();
    ArrayList<PointF> mSellPoints = new ArrayList<PointF>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d(TAG, "max float: "+Float.MAX_VALUE);

        SharedPreferences sp = getSharedPreferences("vars", getApplicationContext().MODE_PRIVATE);
        mMoney = sp.getFloat("money", 2000000000f);
        mShares = sp.getInt("shares", 0);

        tf1 = Typeface.createFromAsset(this.getAssets(),"fonts/paraaminobenzoic.ttf");
        tf2 = Typeface.createFromAsset(this.getAssets(),"fonts/digital7.ttf");

        mSellYellow = (FrameLayout) findViewById(R.id.sell_yellow);
        mBuyYellow = (FrameLayout) findViewById(R.id.buy_yellow);
        mSellButton = (Button) findViewById(R.id.sell_button);
        mPlayButton = (Button) findViewById(R.id.play_button);
        mBuyButton = (Button) findViewById(R.id.buy_button);
        mSharesText = (TextView) findViewById(R.id.shares_text);
        mMoneyText = (TextView) findViewById(R.id.money_text);
        mCurPriceText = (TextView) findViewById(R.id.cur_price_text);
        mSellButton.setTypeface(tf1);
        mBuyButton.setTypeface(tf1);
        mPlayButton.setTypeface(tf1);
        ((Button)findViewById(R.id.play_button2)).setTypeface(tf1);
        mSharesText.setTypeface(tf2);
        mMoneyText.setTypeface(tf2);
        mCurPriceText.setTypeface(tf2);

        mBuyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buyClicked(null);
                return true;
            }
        });

        mSellButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sellClicked(null);
                return true;
            }
        });

        updateSharesText();
        updateMoneyText();

        mBackFragment = new CardBackFragment();
        mFrontFragment = new CardFrontFragment();

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.stock_container, mFrontFragment)
                    .commit();
        }
    }

    public void setStockPriceView(StockPriceView stockPriceView) {
        mStockPriceView = stockPriceView;
        mStockPriceView.setGameActivity(this);
    }


    private void flipCard() {
        if (mShowingBack) {
            mShowingBack = false;
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .replace(R.id.stock_container, mFrontFragment)
                    .commit();
        } else {
            mShowingBack = true;
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .replace(R.id.stock_container, mBackFragment)
                    .commit();
        }
    }

    @Override
    public void onSignInFailed() {
        Log.d(TAG, "sign in failed");
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "sign in succeeded");
    }

    /**
     * A fragment representing the front of the card.
     */
    public static class CardFrontFragment extends Fragment {

        Button mLeaderboardButton;
        Button mAchievementsButton;
        TextView mBestScore;
        TextView mBestScoreText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_front, container, false);
            mLeaderboardButton = (Button) v.findViewById(R.id.leaderboard_button);
            mLeaderboardButton.setTypeface(((GameActivity)getActivity()).tf1);
            mAchievementsButton = (Button) v.findViewById(R.id.achievements_button);
            mAchievementsButton.setTypeface(((GameActivity)getActivity()).tf1);
            return v;
        }

        public void hideAll() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLeaderboardButton.setVisibility(View.GONE);
                    mAchievementsButton.setVisibility(View.GONE);
                    mBestScore.setVisibility(View.GONE);
                    mBestScoreText.setVisibility(View.GONE);
                }
            }, getResources().getInteger(R.integer.card_flip_time_half));

        }

        @Override
        public void onResume() {
            super.onResume();
            mBestScore = (TextView) getView().findViewById(R.id.text_bestscore);
            mBestScoreText = (TextView) getView().findViewById(R.id.text_bestscore_text);
            SharedPreferences sp = getActivity().getSharedPreferences("vars", MODE_PRIVATE);
            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
            String bestscoreStr = "$" + String.format("%.2f", bestscore);
            mBestScore.setText(bestscoreStr);

            mBestScore.setTypeface(((GameActivity)getActivity()).tf2);
            mBestScoreText.setTypeface(((GameActivity)getActivity()).tf1);

        }
    }

    /**
     * A fragment representing the back of the card.
     */
    public static class CardBackFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_back, container, false);
            GameActivity ga = (GameActivity) getActivity();
            StockPriceView spv = (StockPriceView) v.findViewById(R.id.stock_price);
            ga.setStockPriceView(spv);
            return v;
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sp = getSharedPreferences("vars", getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (mShowingBack) {
            saveCurPrice();
            playSound(R.raw.start);
            mStockPriceView.clearCanvas();
            mStockPriceView.setRunning(false);

            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));

            checkAchievements();
            if (mMoney > bestscore) {
                editor.putString("bestscore", mMoney+"");
                editor.commit();
                try {
                    Games.Leaderboards.submitScore(getApiClient(), LEADERBOARD_ID, (long) (mMoney*1000000));
                } catch (Exception e) {}
            }

            mBuyPoints.clear();
            mSellPoints.clear();
            findViewById(R.id.play_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.buysell_frame).setVisibility(View.GONE);

            TranslateAnimation aRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            aRight.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
            aRight.setInterpolator(new DecelerateInterpolator());
            aRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPlayButton.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            Button playButton2 = (Button) findViewById(R.id.play_button2);
            playButton2.startAnimation(aRight);


            TranslateAnimation aLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            aLeft.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
            aLeft.setInterpolator(new DecelerateInterpolator());
            mPlayButton.startAnimation(aLeft);
            mCurPriceText.setVisibility(View.INVISIBLE);
            flipCard();
        } else {
            super.onBackPressed();
        }
    }

    public void checkAchievements() {
        if (mMoney > 100) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_break_even));
            } catch (Exception e) {}
        }
        if (mMoney >= 200) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_double_the_fun));
            } catch (Exception e) {}
        }
        if (mMoney >= 500) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_finance_guru));
            } catch (Exception e) {}
        }
        if (mMoney >= 1000) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_epic_win));
            } catch (Exception e) {}
        }
        if (mMoney >= 1000000) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_millionaire));
            } catch (Exception e) {}
        }
        if (mMoney >= 1000000000) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_billionaire));
            } catch (Exception e) {}
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        saveCurPrice();
        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("money", (float)mMoney);
        editor.putInt("shares", mShares);
        editor.commit();

        super.onPause();
    }

    public void saveCurPrice() {
        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (mStockPriceView != null) {
            editor.putFloat("curprice", mStockPriceView.getCurStockPrice());
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateMoneyText() {
        String moneyStr = "$" + String.format("%.2f", mMoney);
        mMoneyText.setText(moneyStr);
    }

    private void updateSharesText() {
        String sharesStr = mShares + " " + (mShares == 1 ? "Share" : "Shares");
        mSharesText.setText(sharesStr);
    }

    public void leaderboardClicked(View v) {
        try {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), LEADERBOARD_ID), REQUEST_LEADERBOARD);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Nuts! Couldn't connect to Google Play.", Toast.LENGTH_SHORT).show();
        }
    }

    public void achievementsClicked(View v) {
        try {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Nuts! Couldn't connect to Google Play.", Toast.LENGTH_SHORT).show();
        }
    }


    public void buyClicked(View view) {
        ScaleAnimation aClick = new ScaleAnimation(
                1, 1.1f, 1, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aClick.setDuration(100);
        mBuyButton.startAnimation(aClick);
        float curPrice = mStockPriceView.getCurStockPriceActual();
        float moneySpent = curPrice;
        if (view == null) moneySpent = curPrice * ((int)(mMoney / curPrice));
        if (moneySpent > mMoney || moneySpent == 0) {
            mMoneyText.setTextColor(getResources().getColor(R.color.buydot));
            playSound(R.raw.nobuy);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMoneyText.setTextColor(getResources().getColor(R.color.white));
                }
            }, 500);
            return;
        }
        if (view == null) playSound(R.raw.buyall); else playSound(R.raw.coin);
        mBuyPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mBuyPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mBuyPoints.remove(0);
            }
        }

        int newShares = 1;
        if (view == null) newShares = (int) (mMoney / mStockPriceView.getCurStockPriceActual());

        Log.d(TAG, "money: "+mMoney+", spent: "+moneySpent+ ", outcome: "+(mMoney - moneySpent));
        mMoney -= moneySpent;
        mShares = mShares + newShares;
        updateMoneyText();
        updateSharesText();

        if (mMoney <= 0) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_all_in));
            } catch (Exception e) {}
        }

        setBuyFill();
        setSellFill();
    }

    public void sellClicked(View view) {
        ScaleAnimation aClick = new ScaleAnimation(
                1, 1.1f, 1, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aClick.setDuration(100);
        mSellButton.startAnimation(aClick);
        float moneyGained = (int) mStockPriceView.getCurStockPriceActual();
        if (view == null) moneyGained = (mShares * mStockPriceView.getCurStockPriceActual());
        if (mShares <= 0) {
            mSharesText.setTextColor(getResources().getColor(R.color.buydot));
            playSound(R.raw.nobuy);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSharesText.setTextColor(getResources().getColor(R.color.white));
                }
            }, 500);
            return;
        }
        if (view == null) playSound(R.raw.sellall); else playSound(R.raw.coin2);
        mSellPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mSellPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mSellPoints.remove(0);
            }
        }

        mMoney += moneyGained;
        if (view == null) mShares = 0; else mShares--;
        updateMoneyText();
        updateSharesText();

        setBuyFill();
        setSellFill();

        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
        if (mMoney > bestscore) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("bestscore", mMoney+"");
            editor.commit();
            try {
                Games.Leaderboards.submitScore(getApiClient(), LEADERBOARD_ID, (long) (mMoney*1000000));
            } catch (Exception e) {}
        }

        checkAchievements();
    }

    public void playClicked(final View v) {
        playSound(R.raw.back);
        mFrontFragment.hideAll();
        v.setEnabled(false);
        TranslateAnimation aRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        aRight.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
        aRight.setInterpolator(new DecelerateInterpolator());
        aRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.play_frame).setVisibility(View.GONE);
                findViewById(R.id.buysell_frame).setVisibility(View.VISIBLE);
                AnimationSet animationSet = new AnimationSet(true);
                TranslateAnimation aDown = new TranslateAnimation(
                        TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                        TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0);
                aDown.setDuration(1000);
                AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(500);

                animationSet.addAnimation(aDown);
                animationSet.addAnimation(fadeIn);

                mMoneyText.startAnimation(animationSet);
                mSharesText.startAnimation(animationSet);
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mPlayButton.startAnimation(aRight);
        Button playButton2 = (Button) findViewById(R.id.play_button2);
        TranslateAnimation aLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        aLeft.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
        aLeft.setInterpolator(new DecelerateInterpolator());
        playButton2.startAnimation(aLeft);
        mCurPriceText.setVisibility(View.VISIBLE);
        flipCard();
    }

    boolean ranOnce = false;
    public void onCurStockPriceUpdated(final float curStockPrice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!ranOnce) {
                    setSellFill();
                    setBuyFill();
                    ranOnce = true;
                }
                String curStr = "$" + String.format("%.2f", mStockPriceView.getCurStockPriceActual());
                mCurPriceText.setText(curStr);
            }
        });
    }

    public void setSellFill() {
        ViewGroup.LayoutParams sLayout = mSellYellow.getLayoutParams();
        float oldHeight = mSellYellow.getHeight();
        float newHeight = ((mShares+0.0f)/10.0f) * mSellButton.getHeight();
        Log.d(TAG, "setting height: "+newHeight);
        sLayout.height = (int) newHeight;
        if (sLayout.height < 0) {
            sLayout.height = 0;
        }
        if (sLayout.height > mSellButton.getHeight()) {
            sLayout.height = mSellButton.getHeight();
        }
        mSellYellow.setLayoutParams(sLayout);

        if (!(oldHeight == mSellButton.getHeight() && newHeight >= oldHeight)) {
            ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight / newHeight), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
            a.setDuration(500);
            a.setInterpolator(new BounceInterpolator());
            mSellYellow.startAnimation(a);
        }

        if (mShares > 10) {
            mSellButton.setTextColor(getResources().getColor(R.color.white2));
            mSellButton.setTypeface(mSellButton.getTypeface(), Typeface.BOLD);
            if (mShares >= 20) {
                mSharesText.setTextColor(getResources().getColor(R.color.white2));
                mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.BOLD);
            } else {
                mSharesText.setTextColor(getResources().getColor(R.color.white));
                mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.NORMAL);
            }
        } else {
            mSellButton.setTextColor(getResources().getColor(R.color.buysell));
            mSellButton.setTypeface(mSellButton.getTypeface(), Typeface.NORMAL);
            mSharesText.setTextColor(getResources().getColor(R.color.white));
            mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.NORMAL);
        }
    }

    public void setBuyFill() {
        ViewGroup.LayoutParams bLayout = mBuyYellow.getLayoutParams();
        float oldHeight = mBuyYellow.getHeight();
        float newHeight = (((float)mMoney+0.0f)/100.0f) * mBuyButton.getHeight();
        bLayout.height = (int) newHeight;
        if (bLayout.height > mBuyButton.getHeight()) {
            bLayout.height = mBuyButton.getHeight();
        } else if (bLayout.height < 0) {
            bLayout.height = 0;
        }
        mBuyYellow.setLayoutParams(bLayout);

        if (!(oldHeight == mBuyButton.getHeight() && newHeight >= oldHeight)) {
            ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight / newHeight), 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
            a.setDuration(500);
            a.setInterpolator(new BounceInterpolator());
            mBuyYellow.startAnimation(a);
        }

        if (mMoney > 100) {
            mBuyButton.setTextColor(getResources().getColor(R.color.white2));
            mBuyButton.setTypeface(mBuyButton.getTypeface(), Typeface.BOLD);
            if (mMoney >= 200) {
                mMoneyText.setTextColor(getResources().getColor(R.color.white2));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);
            } else {
                mMoneyText.setTextColor(getResources().getColor(R.color.white));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.NORMAL);
            }
        } else {
            mBuyButton.setTextColor(getResources().getColor(R.color.buysell));
            mBuyButton.setTypeface(mBuyButton.getTypeface(), Typeface.NORMAL);
            mMoneyText.setTextColor(getResources().getColor(R.color.white));
            mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.NORMAL);
        }
    }

    HashMap<Integer, MediaPlayer> mediaMap = new HashMap<Integer, MediaPlayer>();
    public void playSound(int soundId){
        if (mediaMap.containsKey(soundId)) {
            mediaMap.get(soundId).start();
        } else {
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
            mp.start();
            mediaMap.put(soundId, mp);
        }
    }
}
