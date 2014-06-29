package io.binroot.stocks;

/**
 * Created by binroot on 6/28/14.
 */
public class SmoothMarket implements StockMarketEmulator {
    private float mPrevious = 200;
    private float mCurrent = 100;
    private int mHeight;

    public SmoothMarket(float current, int height) {
        mCurrent = current;
        mHeight = height - 20;
    }


    @Override
    public float next() {
        float newVal = mCurrent;
        if (Math.abs(mPrevious - mCurrent) <= 1) {
            newVal += 4;
        } else {
            newVal -= Math.random()*30-15;
        }
        if (newVal <= 0) {
            newVal += 50;
        } else if (newVal >= mHeight) {
            newVal -= 50;
        }
        mPrevious = mCurrent;
        mCurrent = newVal;
        return newVal;
    }
}
