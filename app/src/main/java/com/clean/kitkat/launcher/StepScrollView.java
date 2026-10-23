package com.clean.kitkat.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class StepScrollView extends HorizontalScrollView {

    private int slotSize;
    private int visibleSlots = 5;
    private int cardCount;

    public StepScrollView(Context context) {
        super(context);
        setSmoothScrollingEnabled(false);
        setClipChildren(true);
        setClipToPadding(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public StepScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSmoothScrollingEnabled(false);
        setClipChildren(true);
        setClipToPadding(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public StepScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSmoothScrollingEnabled(false);
        setClipChildren(true);
        setClipToPadding(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    public void setVisibleSlots(int visibleSlots) {
        this.visibleSlots = visibleSlots;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public void snapToCard(int cardIndex) {
        if (slotSize <= 0 || getChildCount() == 0) {
            return;
        }
        int maxSlot = Math.max(0, cardCount - visibleSlots);
        int currentSlot = Math.max(0, Math.min(getScrollX() / slotSize, maxSlot));
        int targetSlot = currentSlot;
        if (cardIndex < targetSlot) {
            targetSlot = cardIndex;
        } else if (cardIndex > targetSlot + visibleSlots - 1) {
            targetSlot = cardIndex - visibleSlots + 1;
        }
        targetSlot = Math.max(0, Math.min(targetSlot, maxSlot));
        scrollTo(targetSlot * slotSize, 0);
    }
}
