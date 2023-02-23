package it.unibo.cs.lam2021.ui.scanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import it.unibo.cs.lam2021.R;

public class DrawSurface extends View {


    private int highlightPaddingVertical;
    private int highlightPaddingHorizontal;
    private int highlightCornerRadius;
    private int focusRadius;

    private Paint paintHighlightFill;
    private Paint paintHighlightStroke;
    private Paint paintFocusFill;
    private Paint paintFocusStroke;

    private Rect highlight;
    private RectF roundRect;
    private Point focus;

    public DrawSurface(Context ctx) {
        super(ctx);
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() { // initialize colors and stuff
        Resources res = getContext().getResources();

        highlightPaddingHorizontal = res.getDimensionPixelOffset(R.dimen.barcode_highlight_padding_horizontal);
        highlightPaddingVertical = res.getDimensionPixelOffset(R.dimen.barcode_highlight_padding_vertical);
        highlightCornerRadius = res.getDimensionPixelOffset(R.dimen.barcode_highlight_corner_radius);
        focusRadius = res.getDimensionPixelOffset(R.dimen.focus_highlight_radius);

        paintHighlightFill = new Paint();
        paintHighlightFill.setColor(getContext().getColor(R.color.highlight_fill));
        paintHighlightFill.setStyle(Paint.Style.FILL);

        paintHighlightStroke = new Paint();
        paintHighlightStroke.setColor(getContext().getColor(R.color.highlight_stroke));
        paintHighlightStroke.setStrokeWidth(res.getDimensionPixelOffset(R.dimen.barcode_highlight_stroke_width));
        paintHighlightStroke.setStyle(Paint.Style.STROKE);

        paintFocusFill = new Paint();
        paintFocusFill.setColor(getContext().getColor(R.color.focus_fill));
        paintFocusFill.setStyle(Paint.Style.FILL);

        paintFocusStroke = new Paint();
        paintFocusStroke.setColor(getContext().getColor(R.color.focus_stroke));
        paintFocusStroke.setStrokeWidth(res.getDimensionPixelOffset(R.dimen.focus_highlight_stroke_width));
        paintFocusStroke.setStyle(Paint.Style.STROKE);
    }

    public void highlight(Rect h) {

        highlight = h;
        roundRect = new RectF(highlight.left - highlightPaddingHorizontal,
                highlight.top - highlightPaddingVertical,
                highlight.right + highlightPaddingHorizontal,
                highlight.bottom + highlightPaddingVertical);
        invalidate();
    }

    public void clear() {
        highlight = null;
        invalidate();
    }

    public void focus(Point p) {
        focus = p;
        invalidate();
    }

    public void clearFocus() {
        focus = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) { // called on every frame
        super.onDraw(canvas);

        if(highlight == null || focus == null)
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if(highlight != null) {
            canvas.drawRoundRect(roundRect, highlightCornerRadius, highlightCornerRadius, paintHighlightStroke);
            canvas.drawRoundRect(roundRect, highlightCornerRadius, highlightCornerRadius, paintHighlightFill);
        }
        if(focus != null) {
            if(highlight != null && highlight.intersects(
                    focus.x - focusRadius,
                    focus.y - focusRadius,
                    focus.x + focusRadius,
                    focus.y + focusRadius)) {
                focus = null;
                return;
                }

            canvas.drawCircle(focus.x, focus.y, focusRadius, paintFocusFill);
            canvas.drawCircle(focus.x, focus.y, focusRadius, paintFocusStroke);
        }
    }
}
