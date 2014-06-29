package io.binroot.stocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by binroot on 6/27/14.
 */
public class StockPriceView extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "StockPriceView";
    private GameActivity mGameActivity;
    Paint mPriceDotPaint;
    int mPriceDotRadius = 7;
    Paint mPriceLinePaint;
    Paint mSellDotPaint;
    Paint mBuyDotPaint;
    Path mPriceLine;
    GameThread mGameThread;
    private float mCurStockPrice = 300.0f;
    int mCurTime = 0;
    private int mHeight = 0;
    private boolean mClearCanvas = false;

    public StockPriceView(Context context) {
        super(context);
        init();
    }

    public StockPriceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mPriceLinePaint = new Paint();
            mPriceLinePaint.setColor(getResources().getColor(R.color.priceline));
            mPriceLinePaint.setStyle(Paint.Style.STROKE);
            mPriceLinePaint.setStrokeWidth(5);
        mPriceDotPaint = new Paint();
            mPriceDotPaint.setColor(getResources().getColor(R.color.pricedot));
        mBuyDotPaint = new Paint();
            mBuyDotPaint.setColor(getResources().getColor(R.color.buydot));
        mSellDotPaint = new Paint();
        mSellDotPaint.setColor(getResources().getColor(R.color.selldot));
        mPriceDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPriceLine = new Path();

        getHolder().addCallback(this);
    }

    public void setRunning(boolean run) {
        if (mGameThread != null) mGameThread.setRunning(run);
    }

    public void clearCanvas() {
        mClearCanvas = true;
        invalidate();
    }

    public void setGameActivity(GameActivity gameActivity) {
        mGameActivity = gameActivity;
    }

    public float getCurStockPrice() {
        return mCurStockPrice;
    }

    public void setCurStockPrice(float curStockPrice) {
        mCurStockPrice = curStockPrice;
        mGameActivity.onCurStockPriceUpdated(getCurStockPriceActual());
    }

    public float getCurStockPriceActual() {
        return (mHeight - mCurStockPrice)/5.0f;
    }

    private void start() {
        mGameThread.setRunning(true);
        if (!mGameThread.isAlive()) mGameThread.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()
        mHeight = holder.getSurfaceFrame().height();

        SharedPreferences sp = mGameActivity.getSharedPreferences("vars", mGameActivity.getApplicationContext().MODE_PRIVATE);
        float curprice = sp.getFloat("curprice", mHeight-95);
        mCurStockPrice = curprice;

        mGameThread = new GameThread(getHolder(), this, new SmoothMarket(mCurStockPrice, mHeight)); //Start the thread that
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, getResources().getInteger(R.integer.card_flip_time_full));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mGameThread.setRunning(false);                //Tells thread to stop
            mGameThread.join();                           //Removes thread from mem.
        } catch (InterruptedException e) {}
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (mClearCanvas) return;

        c.save();
        c.translate(-mCurTime + c.getWidth() - 20, 0);

        c.drawPath(mPriceLine, mPriceLinePaint);
        c.drawOval(new RectF(mCurTime - mPriceDotRadius, mCurStockPrice - mPriceDotRadius, mCurTime + mPriceDotRadius, mCurStockPrice + mPriceDotRadius), mPriceDotPaint);

        for (PointF p : mGameActivity.mBuyPoints) {
            c.drawOval(new RectF(p.x-mPriceDotRadius, p.y-mPriceDotRadius, p.x+mPriceDotRadius, p.y+mPriceDotRadius), mBuyDotPaint);
        }

        for (PointF p : mGameActivity.mSellPoints) {
            c.drawOval(new RectF(p.x-mPriceDotRadius, p.y-mPriceDotRadius, p.x+mPriceDotRadius, p.y+mPriceDotRadius), mSellDotPaint);
        }

        c.restore();
    }
}
