package example.tctctc.com.tybookreader.bookshelf.common;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import example.tctctc.com.tybookreader.BookApplication;
import example.tctctc.com.tybookreader.R;
import example.tctctc.com.tybookreader.bean.BookBean;
import example.tctctc.com.tybookreader.bean.ReadConfigBean;
import example.tctctc.com.tybookreader.bookshelf.contact.ImportContact;
import example.tctctc.com.tybookreader.common.rx.RxManager;
import example.tctctc.com.tybookreader.utils.SelectManager;
import example.tctctc.com.tybookreader.utils.UiUtils;
import example.tctctc.com.tybookreader.view.SelectView;

import static android.R.attr.x;
import static example.tctctc.com.tybookreader.R.id.directory;
import static example.tctctc.com.tybookreader.R.id.page_turn;
import static example.tctctc.com.tybookreader.R.id.read_progress_bar;

/**
 * Created by tctctc on 2017/3/26.
 * Function:
 */

public class DialogManager2 implements PageManager.PageEvent {

    public static final String TAG = "DialogManager2";

    private static DialogManager2 mDialogManager;
    private Context mContext;
    //阅读配置bean
    private ReadConfigBean mConfigBean;
    private Typeface mTypeface;
    //background
    //亮度bar
    @BindView(R.id.bright_bar)
    SeekBar brightSeekBar;
    //系统亮度textView
    @BindView(R.id.default_light)
    TextView systemBright_tv;

    private SelectManager<SelectView, Integer> mBgSelectManager;

    private SelectManager<SelectView, Integer> mPageSelectManager;

    private SelectManager<SelectView, String> mFontSelectManager;


    @BindView(R.id.bottom_ll)
    LinearLayout mBottomll;
    @BindView(R.id.font_ll)
    LinearLayout mFontll;
    @BindView(R.id.background_ll)
    LinearLayout mBackgroundll;
    @BindView(R.id.page_turn_ll)
    LinearLayout mPageTurnll;

    private View mLastll;


    //bottom区域
    @BindView(R.id.read_progress_bar)
    SeekBar readProgress;
    @BindView(R.id.read_progress_tv)
    TextView mReadProgressTv;

    //font区域
    @BindView(R.id.fontSize_num)
    TextView fontSizeNum;

    private RelativeLayout mLayout;


    private Unbinder mUnbinder;

    private BookBean mBookBean;

    private DecimalFormat mDecimalFormat;


    private PageManager mPageManager;

    private RxManager mRxManager;


    public static DialogManager2 getInstance(Context context, BookBean bookBean, RelativeLayout rootView) {
        if (null == mDialogManager) {
            mDialogManager = new DialogManager2(context, bookBean, rootView);
        }
        return mDialogManager;
    }

    private DialogManager2(Context context, BookBean bookBean, RelativeLayout rootView) {
        mContext = context;
        mBookBean = bookBean;
        mDecimalFormat = new DecimalFormat("#0.0");
        mTypeface = Typeface.createFromAsset(mContext.getAssets(), "font/iconfont.ttf");
        mConfigBean = new ReadConfig(BookApplication.getContext()).getConfigBean();
        mUnbinder = ButterKnife.bind(this, rootView);
        mLayout = rootView;
        mRxManager = new RxManager();
        setBottomDialog();
        setFontDialog(mConfigBean.getFontSize(), mConfigBean.getFontType());
        setBackgroundDialog(mConfigBean.getBackground(), mConfigBean.getBrightness());
        setPageTurnDialog(mConfigBean.getPageTurn());
    }

    @OnClick({directory, R.id.font, R.id.background, page_turn, R.id.set, R.id.next_chapter, R.id.last_chapter})
    public void onBottomClick(View view) {
        switch (view.getId()) {
            //底部一级dialog事件
            case directory:
                showDirectory();
                hideBottomDialog();
                break;
            case R.id.font:
                show(mFontll);
                break;
            case R.id.background:
                show(mBackgroundll);
                break;
            case page_turn:
                show(mPageTurnll);
                break;
            case R.id.set:
//                mBottomll.setVisibility(View.GONE);
                break;
            case R.id.next_chapter:
                mPageManager.nextChapter();
                break;
            case R.id.last_chapter:
                mPageManager.lastChapter();
                break;

        }
    }


    //底部二级dialog:Font->大小，类型
    @OnClick({R.id.sub_fontSize, R.id.add_fontSize, R.id.fontType2, R.id.fontType3, R.id.fontType1, R.id.font_default_type})
    public void onFontClick(View view) {
        switch (view.getId()) {
            //字体减小
            case R.id.sub_fontSize:
                if (mConfigBean.getFontSize() <= ReadConfig.FONT_SIZE_MIN) {
                    return;
                } else {
                    mConfigBean.setFontSize(mConfigBean.getFontSize() - 1);
                    mPageManager.changeFontSize(mConfigBean.getFontSize());
                    fontSizeNum.setText(mConfigBean.getFontSize() + "");
                }
                break;
            //字体增大
            case R.id.add_fontSize:
                if (mConfigBean.getFontSize() >= ReadConfig.FONT_SIZE_MAX) {
                    return;
                } else {
                    mConfigBean.setFontSize(mConfigBean.getFontSize() + 1);
                    mPageManager.changeFontSize(mConfigBean.getFontSize());
                    fontSizeNum.setText(mConfigBean.getFontSize() + "");
                }
                break;
            //字体类型一
            case R.id.fontType1:
            case R.id.fontType2:
            case R.id.fontType3:
            case R.id.font_default_type:
                String fontType = mFontSelectManager.select((SelectView) view);
                if (fontType != null)
                    setFontType(fontType);
                break;
        }
    }

    @OnClick({R.id.default_light, R.id.pager_bg, R.id.gray_bg, R.id.green_bg, R.id.yellow_bg})
    public void onBackgroundClick(View view) {
        switch (view.getId()) {
            case R.id.default_light:
                defaultBright();
                break;
            case R.id.pager_bg:
            case R.id.gray_bg:
            case R.id.green_bg:
            case R.id.yellow_bg:
                Integer bgId = mBgSelectManager.select((SelectView) view);
                if (bgId != null) {
                    Log.d("aaa", bgId + "");
                    mConfigBean.setBackground(bgId);
                    mPageManager.changeBackground(mConfigBean.getBackground());
                }
                break;
        }
    }

    @OnClick({R.id.turn_no, R.id.turn_cover, R.id.turn_pager, R.id.turn_slide})
    public void onPageClick(View view) {
        switch (view.getId()) {
            case R.id.turn_no:
            case R.id.turn_cover:
            case R.id.turn_pager:
            case R.id.turn_slide:
                Integer type = mPageSelectManager.select((SelectView) view);
                if (type != null)
                    mPageManager.changePageTurn(type);
                break;
        }
    }

    //底部一级dialog，目录，字体，背景，翻页，设置
    public void setBottomDialog() {
        readProgress.setMax((int) mBookBean.getLength() - 1);
        readProgress.setProgress(mBookBean.getProgress());
        readProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float perProgress = (float) progress / (float) readProgress.getMax();
                if (mReadProgressTv.getVisibility() != View.VISIBLE)
                    mReadProgressTv.setVisibility(View.VISIBLE);
                mReadProgressTv.setText(mDecimalFormat.format(perProgress * 100) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mReadProgressTv.setVisibility(View.INVISIBLE);
                mPageManager.changeProgress(seekBar.getProgress());
            }
        });

        ((TextView) mBottomll.findViewById(R.id.directory)).setTypeface(mTypeface);
        ((TextView) mBottomll.findViewById(R.id.font)).setTypeface(mTypeface);
        ((TextView) mBottomll.findViewById(R.id.background)).setTypeface(mTypeface);
        ((TextView) mBottomll.findViewById(R.id.page_turn)).setTypeface(mTypeface);
        ((TextView) mBottomll.findViewById(R.id.set)).setTypeface(mTypeface);
    }

    //底部二级dialog，字体->字体大小，类型
    public void setFontDialog(int fontSize, String fontType) {
        fontSizeNum.setText(fontSize + "");

        ((TextView) mFontll.findViewById(R.id.sub_fontSize)).setTypeface(mTypeface);
        ((TextView) mFontll.findViewById(R.id.add_fontSize)).setTypeface(mTypeface);

        Map<SelectView, String> map = new HashMap<>();
        map.put((SelectView) mFontll.findViewById(R.id.font_default_type), ReadConfig.FONT_TYPE_DEFAULT);
        map.put((SelectView) mFontll.findViewById(R.id.fontType1), ReadConfig.FONT_TYPE_1);
        map.put((SelectView) mFontll.findViewById(R.id.fontType2), ReadConfig.FONT_TYPE_2);
        map.put((SelectView) mFontll.findViewById(R.id.fontType3), ReadConfig.FONT_TYPE_3);
        mFontSelectManager = new SelectManager<>(map);
        mFontSelectManager.select(fontType);
    }

    //底部二级dialog，环境->亮度，背景
    public void setBackgroundDialog(int bgId, int brightness) {
        brightSeekBar.setMax(ReadConfig.BRIGHTNESS_MAX);
        if (brightness == -1) {
            systemBright_tv.setBackgroundResource(R.drawable.dialog_button_select_shape);
            try {
                brightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }

        brightSeekBar.setProgress(brightness);
        changeBright(brightness);

        brightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mConfigBean.getBrightness() != -1) {
                    mConfigBean.setBrightness(progress);
                    changeBright(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mConfigBean.getBrightness() == -1) {
                    systemBright_tv.setBackgroundResource(R.drawable.dialog_button_un_select_shape);
                    mConfigBean.setBrightness(seekBar.getProgress());
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Map<SelectView, Integer> map = new HashMap<>();
        map.put((SelectView) mBackgroundll.findViewById(R.id.pager_bg), ReadConfig.BG_PAGER);
        map.put((SelectView) mBackgroundll.findViewById(R.id.gray_bg), ReadConfig.BG_GRAY);
        map.put((SelectView) mBackgroundll.findViewById(R.id.green_bg), ReadConfig.BG_GREEN);
        map.put((SelectView) mBackgroundll.findViewById(R.id.yellow_bg), ReadConfig.BG_YELLOW);
        mBgSelectManager = new SelectManager<>(map);
        mBgSelectManager.select(bgId);
    }

    //底部二级dialog，翻页
    public void setPageTurnDialog(int type) {
        Map<SelectView, Integer> map = new HashMap<>();
        map.put((SelectView) mPageTurnll.findViewById(R.id.turn_no), ReadConfig.TURN_NO);
        map.put((SelectView) mPageTurnll.findViewById(R.id.turn_cover), ReadConfig.TURN_COVER);
        map.put((SelectView) mPageTurnll.findViewById(R.id.turn_pager), ReadConfig.TURN_PAPER);
        map.put((SelectView) mPageTurnll.findViewById(R.id.turn_slide), ReadConfig.TURN_SLIDE);
        mPageSelectManager = new SelectManager<>(map);
        mPageSelectManager.select(type);
    }

    private void showDirectory() {
        mRxManager.post("open drawer", "");
    }

    private void defaultBright() {
        if (mConfigBean.getBrightness() == -1) return;
        int bright = 0;
        try {
            bright = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        brightSeekBar.setProgress(bright);
        mConfigBean.setBrightness(-1);
        systemBright_tv.setBackgroundResource(R.drawable.dialog_button_select_shape);
        changeBright(mConfigBean.getBrightness());
    }

    private void changeBright(int brightness) {
        Window window = ((AppCompatActivity) mContext).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }

    private void setFontType(String fontType) {
        if (fontType.equals(mConfigBean.getFontType())) {
            return;
        } else {
            mConfigBean.setFontType(fontType);
            mPageManager.changeTypeface(mConfigBean.getFontType());
        }
    }

    public void showBottomDialog() {
        show(mBottomll);
    }

    public void hideBottomDialog() {
        if (mLastll != null) {
            hide(mLastll);
            mLastll = null;
        }
    }

    private void show(View showView) {
        showView.setVisibility(View.VISIBLE);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        showView.measure(0, h);
        int height = showView.getMeasuredHeight();
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(mLayout, "tc", height, 0)
                .setDuration(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float cVal = (Float) animation.getAnimatedValue();
                mLayout.setTranslationY(cVal);
            }
        });
        anim.start();
        if (mLastll != null) {
            mLastll.setVisibility(View.GONE);
        }
        mLastll = showView;
    }


    public void hide(final View lastll) {
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        lastll.measure(0, h);
        int height = lastll.getMeasuredHeight();
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(mLayout, "tc", 0, height)
                .setDuration(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float cVal = (Float) animation.getAnimatedValue();
                mLayout.setTranslationY(cVal);
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lastll.setVisibility(View.GONE);
            }
        });

        anim.start();
    }


    public void setPageManager(PageManager pageManager) {
        mPageManager = pageManager;
        mPageManager.setPageEvent(this);
    }

    public void destroy() {
        mUnbinder.unbind();
        mRxManager.clear();
        mPageManager = null;
        mContext = null;
        mDialogManager = null;
        mBgSelectManager = null;
        mPageSelectManager = null;
        mFontSelectManager = null;
    }

    @Override
    public void onProgressChange(int progress) {
        readProgress.setProgress(progress);
    }
}