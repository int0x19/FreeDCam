package com.troop.freecam.manager.Drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by troop on 27.10.13.
 */
public class OverlayView extends View
{
    boolean moving = false;
    int moveX;
    int moveY;
    int leftmargine;
    int topmargine;

    Rect currentviewRectangle;
    Rect completviewRectangle;
    Bitmap orginalImage;
    BitmapDrawable firstImage;
    BitmapDrawable secondImage;
    BitmapDrawable previewImage;
    String TAG = "OverlayView";
    Uri[] uris;

    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //canvas.save();

        Paint paint = new Paint();
        //paint.setColor(Color.WHITE);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));


        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //canvas.drawColor(Color.WHITE);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Bitmap newmap =null;
        Canvas newcanvas = null;
        if (previewImage != null && moving)
        {
            newmap = Bitmap.createBitmap(800, 480, previewImage.getBitmap().getConfig());
            newcanvas = new Canvas(newmap);
            //newcanvas.drawColor(Color.WHITE);
            previewImage.draw(canvas);
            //newcanvas.drawBitmap(previewImage,0,0, null);
        }
        else if (!moving)
        {
            if (newmap == null && newcanvas == null && previewImage != null)
            {

                newmap = Bitmap.createBitmap(800, 480, previewImage.getBitmap().getConfig());
                newcanvas = new Canvas(newmap);
                previewImage.draw(newcanvas);
                //newcanvas.drawBitmap(previewImage,0,0, null);
            }


            if (firstImage != null && newmap != null && newcanvas != null)
            {
                //paint.setShader(new BitmapShader(firstImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

                firstImage.setAlpha(125);
                firstImage.draw(newcanvas);
                //newcanvas.drawBitmap(firstImage, 0,0,paint);

            }
            if (secondImage != null && newmap != null && newcanvas != null)
            {
                //paint.setShader(new BitmapShader(secondImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                secondImage.setAlpha(125);
                secondImage.draw(newcanvas);
            }



        }
        if (newmap != null)
        {
            canvas.drawBitmap(newmap,0,0,null);
            newmap.recycle();
        }
        //canvas.restore();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean toreturn = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN && moving == false)
        {
            moveX = (int)event.getX();
            moveY = (int)event.getY();
            moving = true;
            toreturn = true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE && moving)
        {
            int lastmovex = moveX - (int)event.getX();
            int lastmovey = moveY - (int)event.getY();
            //Log.d(TAG, "moved by: X:" + lastmovex + " Y: " + lastmovey);
            moveX = (int)event.getX();
            moveY = (int)event.getY();
            boolean draw = true;
            if (leftmargine + lastmovex >= 0 && leftmargine + 800 + lastmovex <= completviewRectangle.right)
                leftmargine = leftmargine + lastmovex;
            else
                draw = false;
            if(topmargine + lastmovey >= 0 && topmargine + 480 + lastmovey <= completviewRectangle.bottom)
                topmargine = topmargine + lastmovey;
            else
                draw = false;
            if (previewImage != null)
            {
                previewImage.getBitmap().recycle();
                previewImage = null;
                System.gc();
            }
            if (draw)
            {
                previewImage = new BitmapDrawable(Bitmap.createBitmap(orginalImage, leftmargine, topmargine, 800, 480));
                previewImage.setBounds(0,0,800,480);
                invalidate();
            }
            //Log.d(TAG, "Margines orginal: left: " + leftmargine + "Top: " + topmargine);
            toreturn = true;

        }
        if (event.getAction() == MotionEvent.ACTION_UP && moving == true)
        {
            moving = false;

            if(firstImage !=null)
            {
                firstImage.getBitmap().recycle();
                firstImage = null;
                System.gc();
            }
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            firstImage = new BitmapDrawable(Bitmap.createBitmap(BitmapFactory.decodeFile(uris[0].getPath(), o), leftmargine, topmargine, 800, 480));
            firstImage.setBounds(0,0,800,480);
            if (secondImage != null)
            {
                secondImage.getBitmap().recycle();
                secondImage = null;
                System.gc();
            }
            secondImage = new BitmapDrawable (Bitmap.createBitmap(BitmapFactory.decodeFile(uris[2].getPath(), o), leftmargine, topmargine, 800, 480));
            secondImage.setBounds(0,0,800,480);
            invalidate();
            //Log.d(TAG, "Margines overlay: left: " + leftmargine + "Top: " +topmargine);
            toreturn = false;

        }
        return toreturn;
    }

    private void init()
    {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        orginalImage = BitmapFactory.decodeFile(uris[1].getPath(), o);

        leftmargine = (orginalImage.getWidth() - 800) /2;
        topmargine = (orginalImage.getHeight() - 480) /2;
        completviewRectangle = new Rect(0,0, orginalImage.getWidth(), orginalImage.getHeight());
        previewImage = new BitmapDrawable(Bitmap.createBitmap(BitmapFactory.decodeFile(uris[1].getPath(),o), leftmargine, topmargine, 800, 480));
        firstImage = new BitmapDrawable(Bitmap.createBitmap(BitmapFactory.decodeFile(uris[0].getPath(),o), leftmargine, topmargine, 800, 480));
        secondImage = new BitmapDrawable(Bitmap.createBitmap(BitmapFactory.decodeFile(uris[2].getPath(),o), leftmargine, topmargine, 800, 480));
    }


    public void Load(Uri[] uris)
    {
        this.uris = uris;
        init();
    }

    public void Destroy()
    {
        if (orginalImage != null)
            orginalImage.recycle();
        orginalImage = null;
        if (firstImage != null)
            firstImage.getBitmap().recycle();
        firstImage = null;
        if(secondImage != null)
            secondImage.getBitmap().recycle();
        secondImage = null;
        if (previewImage !=null)
            previewImage.getBitmap().recycle();
    }
}
