package io.binroot.stocks;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by binroot on 6/27/14.
 */
class GameThread extends Thread {
    private SurfaceHolder mSurfaceHolder;
    private StockPriceView mPanel;
    private boolean mRun = false;
    private ArrayList<PointF> mPoints = new ArrayList<PointF>();
    private boolean mFirst = true;
    private StockMarketEmulator mMarket;

    public GameThread(SurfaceHolder surfaceHolder, StockPriceView panel, StockMarketEmulator market) {
        mSurfaceHolder = surfaceHolder;
        mPanel = panel;
        mMarket = market;
    }

    public void setRunning(boolean run) { //Allow us to stop the thread
        mRun = run;
    }

    @Override
    public void run() {
        Canvas c;
        while (mRun) {     //When setRunning(false) occurs, _run is
            c = null;      //set to false and loop ends, stopping thread
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    mPanel.mCurTime+=10;
                    mPanel.setCurStockPrice(mMarket.next());
                    mPoints.add(new PointF(mPanel.mCurTime, mPanel.getCurStockPrice()));
                    if (mPoints.size() > 100) {
                        for (int i = 0; i < 50; i++) mPoints.remove(0);
                        generatePath();
                    }
                    if (mFirst) mPanel.mPriceLine.moveTo(mPanel.mCurTime, mPanel.getCurStockPrice());
                    else mPanel.mPriceLine.lineTo(mPanel.mCurTime, mPanel.getCurStockPrice());
                    mFirst = false;
                    mPanel.postInvalidate();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    try {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    } catch(Exception e) {}
                }
            }
        }
    }

    private void generatePath() {
        mPanel.mPriceLine = new Path();
        boolean first = true;
        for (PointF p : mPoints) {
            if (first) {
                mPanel.mPriceLine.moveTo(p.x, p.y);
            } else {
                mPanel.mPriceLine.lineTo(p.x, p.y);
            }
            first = false;
        }
    }
}
