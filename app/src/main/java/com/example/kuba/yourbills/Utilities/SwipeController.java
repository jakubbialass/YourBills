package com.example.kuba.yourbills.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.kuba.yourbills.R;

import org.w3c.dom.Text;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.*;

enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

public class SwipeController extends ItemTouchHelper.Callback {

    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private static final float buttonWidth = 250;
    private RectF buttonInstance = null;
    private RectF buttonPaidInstance = null;
    private RecyclerView.ViewHolder currentItemViewHolder = null;
    private float x = 0;
    private float y = 0;
    private Context mContext;
    private SwipeControllerActions buttonsActions;
    private float savedTouchDownDx = 0;
    private float savedTouchDownDy = 0;

    public SwipeController(Context mContext, SwipeControllerActions buttonsActions){
        this.mContext = mContext;
        this.buttonsActions = buttonsActions;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c,
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        x=dX;
        y=dY;
        Log.v("myshift ", Integer.toString((int)x) );
        //Log.v("pokazuje aktualny ", Float.toString(dX));

        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, buttonWidth);
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -buttonWidth*2);
                x=dX;
                y=dY;
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
       // onDraw(c, dX, dY);
        onDraw(c);
        currentItemViewHolder = viewHolder;
        Log.v("siemaneczko", " witam");
        //drawButtons(c, viewHolder);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(final Canvas c,
                                  final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder viewHolder,
                                  final float dX, final float dY,
                                  final int actionState, final boolean isCurrentlyActive) {
       // Log.v("pokazuje aktualny ", Float.toString(dX));
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.v("pokazujeAktualny ", Float.toString(dX));
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if(swipeBack){
                    if (dX < -buttonWidth*2)
                        buttonShowedState = ButtonsState.RIGHT_VISIBLE;
                    else if (dX > buttonWidth)
                        buttonShowedState  = ButtonsState.LEFT_VISIBLE;

                    if (buttonShowedState != ButtonsState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        setItemsClickable(recyclerView, false);
                    }
                }
                return false;
            }
        });
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    @SuppressLint("ClickableViewAccessibility")
    private void setTouchDownListener(final Canvas c,
                                      final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder,
                                      final float dX, final float dY,
                                      final int actionState, final boolean isCurrentlyActive) {
        Log.v("eloszka ", " rr");
        //Log.v("pokazujeAktualny ", Float.toString(dX));
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.v("pokazujeAktualny ", Float.toString(dX));
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("pokazujeAktualny ", Float.toString(event.getX()));
                    savedTouchDownDx = event.getX();
                    savedTouchDownDy = event.getY();
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchUpListener(final Canvas c,
                                    final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder,
                                    final float dX, final float dY,
                                    final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                Log.v("eloszka ", " ee");
                recyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                setItemsClickable(recyclerView, true);
                swipeBack = false;


                if (buttonsActions != null && buttonInstance != null
                        && buttonInstance.contains(event.getX(), event.getY())
                        && buttonInstance.contains(savedTouchDownDx, savedTouchDownDy)) {
                    if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                        buttonsActions.onLeftClicked(viewHolder.getAdapterPosition());
                    }
                    else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        buttonsActions.onRightDeleteClicked(viewHolder.getAdapterPosition());
                    }
                }
                //Log.v("pokazuje " , Boolean.toString(buttonInstance.contains(savedTouchDownDx, savedTouchDownDy)));
                if(buttonsActions != null && buttonPaidInstance != null
                        && buttonPaidInstance.contains(event.getX(), event.getY())
                        && buttonPaidInstance.contains(savedTouchDownDx, savedTouchDownDy)){
                    if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        buttonsActions.onRightPaidClicked(viewHolder.getAdapterPosition());
                    }
                }

                buttonShowedState = ButtonsState.GONE;
                currentItemViewHolder = null;
            }
            return false;
        }
    });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder, float x, float y) {
        //float buttonWidthWithoutPadding = buttonWidth - 20;
        float corners = 16;

        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        //Log.v("myshift ", Integer.toString((int)x) );

        RectF leftButton = new RectF(itemView.getLeft()+5, itemView.getTop()+5, itemView.getLeft() - 20 + x, itemView.getBottom()-5);
        p.setColor(Color.BLUE);
        if(x>=20)
            c.drawRoundRect(leftButton, corners, corners, p);
        if(x>=135)
            drawText("edit", c, leftButton, p);

        RectF rightButton = new RectF(itemView.getRight() + 15 + x/2, itemView.getTop()+5, itemView.getRight(), itemView.getBottom()-5);
        p.setColor(Color.RED);
        if(x<=-20)
            c.drawRoundRect(rightButton, corners, corners, p);
        if(x<=-100)
            drawText("delete", c, rightButton, p);
///////////////////////////////
        RectF rightButtonPaid = new RectF(itemView.getRight() + 20 + x, itemView.getTop()+5, itemView.getRight() + 5 + x/2, itemView.getBottom()-5);
        TextView textView = viewHolder.itemView.findViewById(R.id.bill_days_left);
        if(textView.getText().toString().equals(mContext.getResources().getString(R.string.bill_paid)))
            p.setColor(mContext.getResources().getColor(R.color.unpayButtonColor));
        else
            p.setColor(mContext.getResources().getColor(R.color.payButtonColor));

        if(x<=-20)
            c.drawRoundRect(rightButtonPaid, corners, corners, p);
        if(x<=-100)
            drawText("pay", c, rightButtonPaid, p);
///////////////////////////////
        Log.v("eluwina ", buttonShowedState.toString() );

        buttonInstance = null;
        buttonPaidInstance = null;
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton;
        }
        else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton;
            buttonPaidInstance = rightButtonPaid;
        }

    }

    private void drawText(String icon, Canvas c, RectF button, Paint p) {
        /*float textSize = 60;
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
*/
        Resources res = mContext.getResources();
        if(icon.equals("edit")) {
            Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_mode_edit);
            c.drawBitmap(bitmap, button.centerX() - 60, button.centerY() - 60, p);
        }
        else if(icon.equals("delete")){
            Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_delete_forever);
            c.drawBitmap(bitmap, button.centerX() - 60, button.centerY() - 60, p);
        }
        else if(icon.equals("pay")){
            Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_payment);
            c.drawBitmap(bitmap, button.centerX() - 60, button.centerY() - 60, p);
        }


    }

    public void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            Log.v("witaneczko", " witam");
            drawButtons(c, currentItemViewHolder, x, y);
        }
    }

    public void removeButtons(){
        //Log.v("paanie ", "klik");
        buttonShowedState = ButtonsState.GONE;
        currentItemViewHolder = null;
        buttonInstance = null;
        buttonPaidInstance = null;
    }


}
