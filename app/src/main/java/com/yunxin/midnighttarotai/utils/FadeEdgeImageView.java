package com.yunxin.midnighttarotai.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class FadeEdgeImageView extends AppCompatImageView {
    private Paint fadePaint;
    private RadialGradient gradient;

    public FadeEdgeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FadeEdgeImageView(Context context) {
        super(context);
        init();
    }

    public FadeEdgeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gradient = new RadialGradient(
                w * 0.5f,
                h * 0.5f,
                Math.max(w, h) * 0.7f,
                new int[]{
                        Color.WHITE,
                        Color.WHITE,
                        Color.TRANSPARENT
                },
                new float[]{
                        0f,
                        0.6f,
                        1f
                },
                Shader.TileMode.CLAMP
        );
        fadePaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 创建离屏缓存
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);

        // 绘制原图
        super.onDraw(canvas);

        // 应用渐变遮罩
        canvas.drawPaint(fadePaint);

        // 恢复画布
        canvas.restoreToCount(saveCount);
    }
}