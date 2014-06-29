package io.binroot.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private float mMoney = 100.00f;
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
        tf1 = Typeface.createFromAsset(this.getAssets(),"fonts/paraaminobenzoic.ttf");
        tf2 = Typeface.createFromAsset(this.getAssets(),"fonts/digital7.ttf");

        mSellYellow = (FrameLayout) findViewById(R.id.sell_yellow);
        mBuyYellow = (FrameLayout) findViewById(R.id.buy_yellow);
        mSellButton = (Button) findViewById(R.id.sell_button);
        mPlayButton = (Button) findViewById(R.id.play_button);
        mBuyButton = (Button) findViewById(R.id.buy_button);
        mSharesText = (TextView) findViewById(R.id.shares_text);
        mMoneyText = (TextView) findViewById(R.id.money_text);
        mSellButton.setTypeface(tf1);
        mBuyButton.setTypeface(tf1);
        mPlayButton.setTypeface(tf1);
        ((Button)findViewById(R.id.play_button2)).setTypeface(tf1);
        mSharesText.setTypeface(tf2);
        mMoneyText.setTypeface(tf2);

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
                }
            }, getResources().getInteger(R.integer.card_flip_time_half));

        }

        @Override
        public void onResume() {
            super.onResume();
            mBestScore = (TextView) getView().findViewById(R.id.text_bestscore);
            SharedPreferences sp = getActivity().getSharedPreferences("vars", MODE_PRIVATE);
            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
            String bestscoreStr = "$" + String.format("%.2f", bestscore);
            mBestScore.setText("Best Score: " + " " + bestscoreStr);

            mBestScore.setTypeface(((GameActivity)getActivity()).tf2);
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
        if (mShowingBack) {
            playSound(R.raw.start);
            mStockPriceView.clearCanvas();
            mStockPriceView.setRunning(false);

            SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));

            checkAchievements();
            if (mMoney > bestscore) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("bestscore", mMoney+"");
                editor.commit();
                try {
                    Games.Leaderboards.submitScore(getApiClient(), LEADERBOARD_ID, (long) (mMoney*1000000));
                } catch (Exception e) {}
            }

            mBuyPoints.clear();
            mSellPoints.clear();
            mShares = 0;
            mMoney = 100;
            updateMoneyText();
            updateSharesText();
            setBuyFill();
            setSellFill(0);
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
    }

    @Override
    public void onPause() {
        super.onPause();
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
            Toast.makeText(getApplicationContext(), "Nuts! Couldn't connect to Google Play.", Toast.LENGTH_LONG).show();
        }
    }

    public void achievementsClicked(View v) {
        try {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Nuts! Couldn't connect to Google Play.", Toast.LENGTH_LONG).show();
        }
    }

    public void buyClicked(View view) {
        int moneySpent = (int) mStockPriceView.getCurStockPriceActual();
        if (moneySpent > mMoney) {
            // TODO: shake animation
            return;
        }
        playSound(R.raw.coin);
        mBuyPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mBuyPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mBuyPoints.remove(0);
            }
        }

        mMoney -= moneySpent;
        mShares++;
        updateMoneyText();
        updateSharesText();

        if (mMoney <= 0) {
            try {
                Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_all_in));
            } catch (Exception e) {}
        }

        if (mSellYellow.getHeight() < mSellButton.getHeight()) {
            ViewGroup.LayoutParams sLayout = mSellYellow.getLayoutParams();
            sLayout.height = mSellYellow.getHeight() + moneySpent;
            if (sLayout.height > mSellButton.getHeight()) {
                sLayout.height = mSellButton.getHeight();
            }
            mSellYellow.setLayoutParams(sLayout);
        }

        setBuyFill();
    }

    public void sellClicked(View view) {
        int moneyGained = (int) mStockPriceView.getCurStockPriceActual();
        if (mShares <= 0) {
            // TODO: shake UI
            return;
        }
        playSound(R.raw.coin2);
        mSellPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mSellPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mSellPoints.remove(0);
            }
        }

        mMoney += moneyGained;
        mShares--;
        updateMoneyText();
        updateSharesText();

        setBuyFill();

        if (mSellYellow.getHeight() > 0) {
            ViewGroup.LayoutParams sLayout = mSellYellow.getLayoutParams();
            sLayout.height = mSellYellow.getHeight() - moneyGained;
            if (sLayout.height < 0) {
                sLayout.height = 0;
            }
            mSellYellow.setLayoutParams(sLayout);
        }

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

        flipCard();
    }


    public void onCurStockPriceUpdated(float curStockPrice) {
        setSellFill(curStockPrice);
    }

    public void setSellFill(final float money) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ViewGroup.LayoutParams sLayout = mSellYellow.getLayoutParams();
                final float oldHeight = mSellYellow.getHeight();
                float newHeight = 0f;
                if (mShares > 0) newHeight = (money/100.0f) * mSellButton.getHeight();
                sLayout.height = (int) newHeight;
                if (sLayout.height > mSellButton.getHeight()) {
                    sLayout.height = mSellButton.getHeight();
                } else if (sLayout.height < 0) {
                    sLayout.height = 0;
                }
                final float finalNewHeight = newHeight;
                mSellYellow.setLayoutParams(sLayout);
                ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight/ finalNewHeight), 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
                a.setDuration(100);
                mSellYellow.startAnimation(a);
            }
        });
    }

    public void setBuyFill() {
        ViewGroup.LayoutParams bLayout = mBuyYellow.getLayoutParams();
        float oldHeight = mBuyYellow.getHeight();
        float newHeight = (mMoney/100.0f) * mBuyButton.getHeight();
        bLayout.height = (int) newHeight;
        if (bLayout.height > mBuyButton.getHeight()) {
            bLayout.height = mBuyButton.getHeight();
        } else if (bLayout.height < 0) {
            bLayout.height = 0;
        }
        mBuyYellow.setLayoutParams(bLayout);
        ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight/newHeight), 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        a.setDuration(500);
        a.setInterpolator(new BounceInterpolator());
        mBuyYellow.startAnimation(a);

        if (mMoney > 100) {
            mBuyButton.setTextColor(getResources().getColor(R.color.white2));
            mBuyButton.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);
            if (mMoney >= 200) {
                mMoneyText.setTextColor(getResources().getColor(R.color.white2));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);
            } else {
                mMoneyText.setTextColor(getResources().getColor(R.color.white));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);
            }
        } else {
            mBuyButton.setTextColor(getResources().getColor(R.color.buysell));
            mBuyButton.setTypeface(mMoneyText.getTypeface(), Typeface.NORMAL);
            mMoneyText.setTextColor(getResources().getColor(R.color.white));
            mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);
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
