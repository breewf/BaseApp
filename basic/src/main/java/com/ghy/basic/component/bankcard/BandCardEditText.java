package com.ghy.basic.component.bankcard;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * 格式化银行卡号EditText
 */
public class BandCardEditText extends android.support.v7.widget.AppCompatEditText {

    private boolean shouldStopChange = false;

    private BankCardListener listener;

    public BandCardEditText(Context context) {
        this(context, null);
    }

    public BandCardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BandCardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        format(getText());
        shouldStopChange = false;
        setFocusable(true);
        setEnabled(true);
        setFocusableInTouchMode(true);
        addTextChangedListener(new CardTextWatcher());
    }

    class CardTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            format(editable);
        }
    }

    private void format(Editable editable) {
        if (shouldStopChange) {
            shouldStopChange = false;
            return;
        }

        shouldStopChange = true;

        String WHITE_SPACE = " ";
        String str = editable.toString().trim().replaceAll(WHITE_SPACE, "");
        int len = str.length();
        int courPos;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(str.charAt(i));
            if (i == 3 || i == 7 || i == 11 || i == 15 || i == 19) {
                if (i != len - 1)
                    builder.append(WHITE_SPACE);
            }
        }
        courPos = builder.length();
        setText(builder.toString());
        setSelection(courPos);
        if (listener != null) {
            if (isBankCard()) {
                char[] ss = getBankCardText().toCharArray();
                listener.success(BankInfo.getNameOfBank(ss, 0));
            } else {
                listener.failure();
            }
        }
    }

    /**
     * 获取文本内容（银行卡号，不包含空格）
     *
     * @return
     */
    public String getBankCardText() {
        return getText().toString().trim().replaceAll(" ", "");
    }

    public boolean isBankCard() {
        return checkBankCard(getBankCardText());
    }

    /**
     * 校验银行卡卡号
     */
    public boolean checkBankCard(String cardId) {
        char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        if (bit == 'N') {
            return false;
        }
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     */
    public char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (TextUtils.isEmpty(nonCheckCodeCardId)
                || !nonCheckCodeCardId.matches("\\d+")
                || nonCheckCodeCardId.length() < 16
                || nonCheckCodeCardId.length() > 19) {
            //如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

    public void setBankCardListener(BankCardListener listener) {
        this.listener = listener;
    }

    public interface BankCardListener {
        void success(String name);

        void failure();
    }
}
