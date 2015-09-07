package org.commonmark.spannable;

import org.commonmark.node.Node;

import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.text.SpannableStringBuilder;

public class SpannableRenderer {
    private final SpannableFactory mFactory;
    private final Integer mListItemMarkerLeftMargin;
    private final Integer mListItemMarkerLeftMarginResId;
    private final Integer mListItemExtraHeight;
    private final Integer mListItemExtraHeightResId;
    private final Integer mListItemLeading;
    private final Integer mListItemLeadingResId;
    private final Integer mListItemBulletRadius;
    private final Integer mListItemBulletRadiusResId;
    private final Integer mHeaderTextSize;
    private final Integer mHeaderTextSizeResId;
    private final Integer mCodeTextSize;
    private final Integer mCodeTextSizeResId;
    private final Integer mCodeBlockPadding;
    private final Integer mCodeBlockPaddingResId;
    private final Integer mParagraphPadding;
    private final Integer mParagraphPaddingResId;
    private final Integer mQuotePadding;
    private final Integer mQuotePaddingResId;
    private final Integer mQuoteStripeWidth;
    private final Integer mQuoteStripeWidthResId;
    private final Integer mCodeBlockColor;
    private final Integer mCodeBlockColorResId;
    private final Integer mQuoteStripeColor;
    private final Integer mQuoteStripeColorResId;

    private SpannableRenderer(Builder builder) {
        mFactory = builder.mFactory;
        mListItemMarkerLeftMargin = builder.mListItemMarkerLeftMargin;
        mListItemMarkerLeftMarginResId = builder.mListItemMarkerLeftMarginResId;
        mListItemExtraHeight = builder.mListItemExtraHeight;
        mListItemExtraHeightResId = builder.mListItemExtraHeightResId;
        mListItemLeading = builder.mListItemLeading;
        mListItemLeadingResId = builder.mListItemLeadingResId;
        mListItemBulletRadius = builder.mListItemBulletRadius;
        mListItemBulletRadiusResId = builder.mListItemBulletRadiusResId;
        mHeaderTextSize = builder.mHeaderTextSize;
        mHeaderTextSizeResId = builder.mHeaderTextSizeResId;
        mCodeTextSize = builder.mCodeTextSize;
        mCodeTextSizeResId = builder.mCodeTextSizeResId;
        mCodeBlockPadding = builder.mCodeBlockPadding;
        mCodeBlockPaddingResId = builder.mCodeBlockPaddingResId;
        mParagraphPadding = builder.mParagraphPadding;
        mParagraphPaddingResId = builder.mParagraphPaddingResId;
        mQuotePadding = builder.mQuotePadding;
        mQuotePaddingResId = builder.mQuotePaddingResId;
        mQuoteStripeWidth = builder.mQuoteStripeWidth;
        mQuoteStripeWidthResId = builder.mQuoteStripeWidthResId;
        mCodeBlockColor = builder.mCodeBlockColor;
        mCodeBlockColorResId = builder.mCodeBlockColorResId;
        mQuoteStripeColor = builder.mQuoteStripeColor;
        mQuoteStripeColorResId = builder.mQuoteStripeColorResId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public CharSequence render(Resources res, Node node) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        render(res, node, ssb);
        return ssb;
    }

    public void render(Resources res, Node node, SpannableStringBuilder ssb) {
        SpannableVisitor visitor = new SpannableVisitor(createWriter(res, ssb));
        node.accept(visitor);
    }

    private SpannableWriter createWriter(Resources res, SpannableStringBuilder ssb) {
        return new SpannableWriter(res, ssb, mFactory,
                                   mListItemMarkerLeftMargin, mListItemMarkerLeftMarginResId,
                                   mListItemExtraHeight, mListItemExtraHeightResId,
                                   mListItemLeading, mListItemLeadingResId,
                                   mListItemBulletRadius, mListItemBulletRadiusResId,
                                   mHeaderTextSize, mHeaderTextSizeResId,
                                   mCodeTextSize, mCodeTextSizeResId,
                                   mCodeBlockPadding, mCodeBlockPaddingResId,
                                   mParagraphPadding, mParagraphPaddingResId,
                                   mQuotePadding, mQuotePaddingResId,
                                   mQuoteStripeWidth, mQuoteStripeWidthResId,
                                   mCodeBlockColor, mCodeBlockColorResId,
                                   mQuoteStripeColor, mQuoteStripeColorResId);
    }

    public static final class Builder {
        private SpannableFactory mFactory;
        private Integer mListItemMarkerLeftMargin;
        private Integer mListItemMarkerLeftMarginResId;
        private Integer mListItemExtraHeight;
        private Integer mListItemExtraHeightResId;
        private Integer mListItemLeading;
        private Integer mListItemLeadingResId;
        private Integer mListItemBulletRadius;
        private Integer mListItemBulletRadiusResId;
        private Integer mHeaderTextSize;
        private Integer mHeaderTextSizeResId;
        private Integer mCodeTextSize;
        private Integer mCodeTextSizeResId;
        private Integer mCodeBlockPadding;
        private Integer mCodeBlockPaddingResId;
        private Integer mParagraphPadding;
        private Integer mParagraphPaddingResId;
        private Integer mQuotePadding;
        private Integer mQuotePaddingResId;
        private Integer mQuoteStripeWidth;
        private Integer mQuoteStripeWidthResId;
        private Integer mCodeBlockColor;
        private Integer mCodeBlockColorResId;
        private Integer mQuoteStripeColor;
        private Integer mQuoteStripeColorResId;

        private Builder() {
        }

        public Builder factory(SpannableFactory factory) {
            mFactory = factory;
            return this;
        }

        public Builder listItemMarkerLeftMargin(int margin) {
            mListItemMarkerLeftMargin = margin;
            return this;
        }

        public Builder listItemMarkerLeftMarginResId(@DimenRes int resId) {
            mListItemMarkerLeftMarginResId = resId;
            return this;
        }

        public Builder listItemExtraHeight(int extraHeight) {
            mListItemExtraHeight = extraHeight;
            return this;
        }

        public Builder listItemExtraHeightResId(@DimenRes int resId) {
            mListItemExtraHeightResId = resId;
            return this;
        }

        public Builder listItemLeading(int leading) {
            mListItemLeading = leading;
            return this;
        }

        public Builder listItemLeadingResId(@DimenRes int resId) {
            mListItemLeadingResId = resId;
            return this;
        }

        public Builder listItemBulletRadius(int radius) {
            mListItemBulletRadius = radius;
            return this;
        }

        public Builder listItemBulletRadiusResId(@DimenRes int resId) {
            mListItemBulletRadiusResId = resId;
            return this;
        }

        public Builder headerTextSize(int textSize) {
            mHeaderTextSize = textSize;
            return this;
        }

        public Builder headerTextSizeResId(@DimenRes int resId) {
            mHeaderTextSizeResId = resId;
            return this;
        }

        public Builder codeTextSize(int textSize) {
            mCodeTextSize = textSize;
            return this;
        }

        public Builder codeTextSizeResId(@DimenRes int resId) {
            mCodeTextSizeResId = resId;
            return this;
        }

        public Builder codeBlockPadding(int padding) {
            mCodeBlockPadding = padding;
            return this;
        }

        public Builder codeBlockPaddingResId(@DimenRes int resId) {
            mCodeBlockPaddingResId = resId;
            return this;
        }

        public Builder paragraphPadding(int padding) {
            mParagraphPadding = padding;
            return this;
        }

        public Builder paragraphPaddingResId(@DimenRes int resId) {
            mParagraphPaddingResId = resId;
            return this;
        }

        public Builder quotePadding(int padding) {
            mQuotePadding = padding;
            return this;
        }

        public Builder quotePaddingResId(@DimenRes int resId) {
            mQuotePaddingResId = resId;
            return this;
        }

        public Builder quoteStripeWidth(int width) {
            mQuoteStripeWidth = width;
            return this;
        }

        public Builder quoteStripeWidthResId(@DimenRes int resId) {
            mQuoteStripeWidthResId = resId;
            return this;
        }

        public Builder codeBlockColor(@ColorInt int color) {
            mCodeBlockColor = color;
            return this;
        }

        public Builder codeBlockColorResId(@ColorRes int resId) {
            mCodeBlockColorResId = resId;
            return this;
        }

        public Builder quoteStripeColor(@ColorInt int color) {
            mQuoteStripeColor = color;
            return this;
        }

        public Builder quoteStripeColorResId(@ColorRes int resId) {
            mQuoteStripeColorResId = resId;
            return this;
        }

        public SpannableRenderer build() {
            return new SpannableRenderer(this);
        }
    }
}
