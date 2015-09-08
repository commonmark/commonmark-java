package org.commonmark.spannable;

import org.commonmark.R;
import org.commonmark.spannable.text.style.CodeBlockSpan;
import org.commonmark.spannable.text.style.CountedSpan;
import org.commonmark.spannable.text.style.HeaderSpan;
import org.commonmark.spannable.text.style.InlineCodeSpan;
import org.commonmark.spannable.text.style.LineSeparatorSpan;
import org.commonmark.spannable.text.style.LinkSpan;
import org.commonmark.spannable.text.style.OrderedListItemSpan;
import org.commonmark.spannable.text.style.QuoteSpan;
import org.commonmark.spannable.text.style.UnorderedListItemSpan;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.ArrayList;

public class SpannableWriter {
    private final ArrayList<Object> mSpans = new ArrayList<>();

    private final SpannableStringBuilder mBuffer;
    private final SpannableFactory mFactory;

    private final int mListItemMarkerLeftMargin;
    private final int mListItemExtraHeight;
    private final int mListItemLeading;
    private final int mListItemBulletRadius;
    private final int mHeaderTextSize;
    private final int mCodeTextSize;
    private final int mCodeBlockPadding;
    private final int mParagraphPadding;
    private final int mQuotePadding;
    private final int mQuoteStripeWidth;
    private final int mCodeBlockColor;
    private final int mQuoteStripeColor;

    private boolean mOrderedList;
    private boolean mUnorderedList;

    private int mCount;

    private char mLastChar = 0;

    public SpannableWriter(Resources res, SpannableStringBuilder out, SpannableFactory factory,
                           Integer listItemMarkerLeftMargin, Integer listItemMarkerLeftMarginResId,
                           Integer listItemExtraHeight, Integer listItemExtraHeightResId,
                           Integer listItemLeading, Integer listItemLeadingResId,
                           Integer listItemBulletRadius, Integer listItemBulletRadiusResId,
                           Integer headerTextSize, Integer headerTextSizeResId,
                           Integer codeTextSize, Integer codeTextSizeResId,
                           Integer codeBlockPadding, Integer codeBlockPaddingResId,
                           Integer paragraphPadding, Integer paragraphPaddingResId,
                           Integer quotePadding, Integer quotePaddingResId,
                           Integer quoteStripeWidth, Integer quoteStripeWidthResId,
                           Integer codeBlockColor, Integer codeBlockColorResId,
                           Integer quoteStripeColor, Integer quoteStripeColorResId) {
        mBuffer = out;
        mFactory = factory;

        mListItemMarkerLeftMargin = getDimen(res, listItemMarkerLeftMargin, listItemMarkerLeftMarginResId,
                                             R.dimen.commonmark_list_item_marker_left_margin);

        mListItemExtraHeight = getDimen(res, listItemExtraHeight, listItemExtraHeightResId,
                                        R.dimen.commonmark_list_item_extra_height);

        mListItemLeading = getDimen(res, listItemLeading, listItemLeadingResId,
                                    R.dimen.commonmark_list_item_leading);

        mListItemBulletRadius = getDimen(res, listItemBulletRadius, listItemBulletRadiusResId,
                                         R.dimen.commonmark_list_item_bullet_radius);

        mHeaderTextSize = getDimen(res, headerTextSize, headerTextSizeResId,
                                   R.dimen.commonmark_header_text_size);

        mCodeTextSize = getDimen(res, codeTextSize, codeTextSizeResId,
                                 R.dimen.commonmark_code_text_size);

        mCodeBlockPadding = getDimen(res, codeBlockPadding, codeBlockPaddingResId,
                                     R.dimen.commonmark_code_block_padding);

        mParagraphPadding = getDimen(res, paragraphPadding, paragraphPaddingResId,
                                     R.dimen.commonmark_paragraph_padding);

        mQuotePadding = getDimen(res, quotePadding, quotePaddingResId,
                                 R.dimen.commonmark_quote_padding);

        mQuoteStripeWidth = getDimen(res, quoteStripeWidth, quoteStripeWidthResId,
                                     R.dimen.commonmark_quote_stripe_width);

        mCodeBlockColor = getColor(res, codeBlockColor, codeBlockColorResId,
                                   R.color.commonmark_code_block_color);

        mQuoteStripeColor = getColor(res, quoteStripeColor, quoteStripeColorResId,
                                     R.color.commonmark_quote_stripe_color);
    }

    public void paragraph() {
        if (mLastChar != 0) {
            if (mLastChar != '\n') {
                mBuffer.append('\n');
            }

            mBuffer.append(getParagraphSeparator());

            mLastChar = 0;
        }
    }

    public void line() {
        if (mLastChar != 0 && mLastChar != '\n') {
            mLastChar = '\n';
            mBuffer.append(mLastChar);
        }
    }

    public void bold() {
        mSpans.add(getSpan(SpannableFactory.TYPE_BOLD));
    }

    public void italic() {
        mSpans.add(getSpan(SpannableFactory.TYPE_ITALIC));
    }

    public void header() {
        mSpans.add(getSpan(SpannableFactory.TYPE_HEADER));
    }

    public void code() {
        mSpans.add(getSpan(SpannableFactory.TYPE_INLINE_CODE));
    }

    public void codeBlock() {
        mSpans.add(getSpan(SpannableFactory.TYPE_CODE_BLOCK));
    }

    public void blockQuote() {
        mSpans.add(getSpan(SpannableFactory.TYPE_QUOTE));
    }

    public void link(String url) {
        mSpans.add(getLinkSpan(url));
    }

    public void orderedList() {
        mOrderedList = true;
        if (mUnorderedList) {
            mUnorderedList = false;
        }
    }

    public void unorderedList() {
        mUnorderedList = true;
        if (mOrderedList) {
            mOrderedList = false;
        }
    }

    public void resetCount() {
        mCount = 0;
    }

    public void listItem() {
        if (mOrderedList) {
            mSpans.add(getSpan(SpannableFactory.TYPE_ORDERED_LIST_ITEM));
        }
        if (mUnorderedList) {
            mSpans.add(getSpan(SpannableFactory.TYPE_UNORDERED_LIST_ITEM));
        }
    }

    public void write(String text) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        int length = text.length();
        for (Object span : mSpans) {
            if (span instanceof CountedSpan) {
                mCount++;
                ((CountedSpan) span).setCount(mCount);
            }
            ssb.setSpan(span, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mSpans.clear();

        if (length != 0) {
            mLastChar = text.charAt(length - 1);
        }

        mBuffer.append(ssb);
    }

    private Object getSpan(int type) {
        if (mFactory != null) {
            Object span = mFactory.getSpan(type);
            if (span != null) {
                return span;
            }
        }

        switch (type) {
            case SpannableFactory.TYPE_BOLD:
                return new StyleSpan(Typeface.BOLD);

            case SpannableFactory.TYPE_ITALIC:
                return new StyleSpan(Typeface.ITALIC);

            case SpannableFactory.TYPE_HEADER:
                return new HeaderSpan(mHeaderTextSize);

            case SpannableFactory.TYPE_QUOTE:
                return new QuoteSpan(mQuoteStripeColor, mQuoteStripeWidth, mQuotePadding);

            case SpannableFactory.TYPE_INLINE_CODE:
                return new InlineCodeSpan(mCodeBlockColor, mCodeTextSize);

            case SpannableFactory.TYPE_CODE_BLOCK:
                return new CodeBlockSpan(mCodeBlockColor, mCodeTextSize, mCodeBlockPadding);

            case SpannableFactory.TYPE_ORDERED_LIST_ITEM:
                return new OrderedListItemSpan(mListItemLeading, mListItemExtraHeight, mListItemMarkerLeftMargin);

            case SpannableFactory.TYPE_UNORDERED_LIST_ITEM:
                return new UnorderedListItemSpan(mListItemLeading, mListItemExtraHeight, mListItemMarkerLeftMargin,
                                                 mListItemBulletRadius);

            case SpannableFactory.TYPE_PARAGRAPH_SEPARATOR:
                return new LineSeparatorSpan(mParagraphPadding);

            default:
                throw new IllegalArgumentException("unsupported spannable type " + type);
        }
    }

    private Object getLinkSpan(String url) {
        if (mFactory != null) {
            Object span = mFactory.getLinkSpan(url);
            if (span != null) {
                return span;
            }
        }

        return new LinkSpan(url);
    }

    private CharSequence getParagraphSeparator() {
        SpannableStringBuilder separator = new SpannableStringBuilder("\n");
        separator.setSpan(getSpan(SpannableFactory.TYPE_PARAGRAPH_SEPARATOR), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return separator;
    }

    private int getDimen(Resources res, Integer value, Integer resId, int defaultResId) {
        if (value != null) {
            return value;
        } else if (resId != null) {
            return res.getDimensionPixelSize(resId);
        } else {
            return res.getDimensionPixelSize(defaultResId);
        }
    }

    private int getColor(Resources res, Integer value, Integer resId, int defaultResId) {
        if (value != null) {
            return value;
        } else if (resId != null) {
            return getColor(res, resId);
        } else {
            return getColor(res, defaultResId);
        }
    }

    @SuppressWarnings("deprecation")
    private int getColor(Resources res, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(resId, null);
        } else {
            return res.getColor(resId);
        }
    }
}
