package cz.nelasoft.opencms.mobile;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class StyleableSpannableStringBuilder extends SpannableStringBuilder {

	public StyleableSpannableStringBuilder appendWithStyle(CharSequence text, CharacterStyle... c) {
		super.append(text);
		int startPos = length() - text.length();
		for (CharacterStyle cs : c) {
			setSpan(cs, startPos, length(), 0);
		}
		return this;
	}

	public StyleableSpannableStringBuilder appendBold(CharSequence text) {
		return appendWithStyle(text, new StyleSpan(Typeface.BOLD));
	}

	public StyleableSpannableStringBuilder appendColor(CharSequence text, int color) {
		return appendWithStyle(text, new ForegroundColorSpan(color));
	}

	public StyleableSpannableStringBuilder appendColorBold(CharSequence text, int color) {
		return appendWithStyle(text, new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(color));
	}

}
