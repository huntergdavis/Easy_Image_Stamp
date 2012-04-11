package com.hunterdavis.easyimagestamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

class Panel extends SurfaceView implements SurfaceHolder.Callback {
	private CanvasThread canvasthread;

	int _x = 0;
	int _y = 0;
	Uri selectedUri = null;
	Uri selectedStampUri = null;
	public Boolean surfaceCreated;
	int scaleValue = 50;
	public Bitmap lastGoodBitmap;

	private Vector xvalues;
	private Vector yvalues;
	private Vector scalevalues;
	private Vector stampUris;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			addValues((int) event.getX(), (int) event.getY(), scaleValue);
		}
		return true;

	}

	public void setUri(Uri myUri) {
		selectedUri = myUri;
		// xvalues.clear();
		// yvalues.clear();
		// scalevalues.clear();
		lastGoodBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
	}

	public void clear() {
		selectedUri = null;
		selectedStampUri = null;
		xvalues.clear();
		yvalues.clear();
		scalevalues.clear();
		stampUris.clear();
		lastGoodBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
	}

	public void setStamp(Uri myUri) {
		selectedStampUri = myUri;
	}

	public void setScaleValue(int scale) {
		scaleValue = scale;
	}

	private void addValues(int x, int y, int scale) {
		xvalues.addElement(x);
		yvalues.addElement(y);
		scalevalues.addElement(scale);
		stampUris.addElement(selectedStampUri);
	}

	public void undo() {
		if (xvalues.size() > 0) {

			xvalues.removeElement(xvalues.lastElement());
			yvalues.removeElement(yvalues.lastElement());
			stampUris.removeElement(stampUris.lastElement());
			scalevalues.removeElement(scalevalues.lastElement());
			lastGoodBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Bitmap.Config.ARGB_8888);
		}
	}

	public Panel(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		surfaceCreated = false;
		xvalues = new Vector();
		yvalues = new Vector();
		scalevalues = new Vector();
		stampUris = new Vector();
		lastGoodBitmap = null;

		// crashes
		// lastGoodBitmap = Bitmap.createBitmap( getWidth(), getHeight(),
		// Bitmap.Config.ARGB_8888);

		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void createThread(SurfaceHolder holder) {
		canvasthread = new CanvasThread(getHolder(), this);
		canvasthread.setRunning(true);
		canvasthread.start();
	}

	public void terminateThread() {
		canvasthread.setRunning(false);
		try {
			canvasthread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (surfaceCreated == false) {
			createThread(holder);
			surfaceCreated = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;

	}

	@Override
	public void onDraw(Canvas canvas) {

		Paint paint = new Paint();

		// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
		// R.drawable.kangoo);
		Canvas singleUseCanvas = new Canvas();
		if (lastGoodBitmap == null) {
			lastGoodBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Bitmap.Config.ARGB_8888);
		}

		singleUseCanvas.setBitmap(lastGoodBitmap);
		singleUseCanvas.drawColor(Color.WHITE);

		// canvas.drawBitmap(kangoo, 10, 10, null);
		if (selectedUri != null) {
			double divisorHeightDouble = 400;
			double divisorWidthDouble = 300;
			divisorHeightDouble = canvas.getHeight();
			divisorWidthDouble = canvas.getWidth();

			InputStream photoStream = null;

			Context context = getContext();
			try {
				photoStream = context.getContentResolver().openInputStream(
						selectedUri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int scaleSize = decodeFile(photoStream, (int) divisorHeightDouble,
					(int) divisorWidthDouble);

			try {
				photoStream = context.getContentResolver().openInputStream(
						selectedUri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = scaleSize;

			Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream, null,
					o);
			int h = photoBitmap.getHeight();
			int w = photoBitmap.getWidth();

			if ((w > h)) {
				double ratio = divisorWidthDouble / w;
				w = (int) divisorWidthDouble;
				h = (int) (ratio * h);

				if (h > divisorHeightDouble) {
					double newratio = divisorHeightDouble / h;
					h = (int) divisorHeightDouble;
					w = (int) (w * newratio);
				}

			} else if ((h > w)) {
				double ratio = divisorHeightDouble / h;
				h = (int) divisorHeightDouble;
				w = (int) (ratio * w);

				if (w > divisorWidthDouble) {
					double newratio = divisorWidthDouble / h;
					w = (int) divisorWidthDouble;
					h = (int) (w * newratio);
				}

			}

			Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
			photoBitmap.recycle();
			singleUseCanvas.drawBitmap(scaled, 0, 0, null);
		} else {
			// here we call a canvas operation function to add all the cats
			drawCatsFromVectors(singleUseCanvas);
			canvas.drawBitmap(lastGoodBitmap, 0, 0, null);
			return;
		}

		drawCatsFromVectors(singleUseCanvas);

		// since we drew to the bitmap, display it
		canvas.drawBitmap(lastGoodBitmap, 0, 0, null);

	}

	// decodes image and scales it to reduce memory consumption
	private int decodeFile(InputStream photostream, int h, int w) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(photostream, null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < w || height_tmp / 2 < h)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		return scale;
	}

	public void drawCatsFromVectors(Canvas canvas) {
		// for each cat position
		Bitmap scaled = null;
		Boolean scalme = false;
		for (int i = 0; i < xvalues.size(); i++) {
			scalme = true;
			int localxvalue = (Integer) xvalues.get(i);
			int localyvalue = (Integer) yvalues.get(i);
			int localscalevalue = (Integer) scalevalues.get(i);
			double scalefactor = .01 * (1 + localscalevalue);
			Uri stampUri = (Uri) stampUris.get(i);

			// center the cat on the bitmap
			// get a hold of our cat bitmap
			Bitmap _scratch;
			if (stampUri == null) {
				_scratch = BitmapFactory.decodeResource(getResources(),
						R.drawable.jumping);
			} else {
				_scratch = scaleURIAndReturnBitmap(stampUri);
			}

			// now scale the bitmap using the scale value
			int h = (int) (_scratch.getHeight() * scalefactor);
			int w = (int) (_scratch.getWidth() * scalefactor);

			scaled = Bitmap.createScaledBitmap(_scratch, w, h, true);
			canvas.drawBitmap(scaled, localxvalue - (w / 2), localyvalue
					- (h / 2), null);

		} // end of cats loop
		if (scalme == true) {
			scaled.recycle();
		}

	} // end of drawcatsfromvectors

	public Bitmap scaleURIAndReturnBitmap(Uri uri) {
		InputStream photoStream;
		double divisorDouble = 300;
		try {
			Context context = getContext();
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap photoBitmap;

		photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
		if (photoBitmap == null) {
			return null;
		}
		int h = photoBitmap.getHeight();
		int w = photoBitmap.getWidth();
		if ((w > h) && (w > divisorDouble)) {
			double ratio = divisorDouble / w;
			w = (int) divisorDouble;
			h = (int) (ratio * h);
		} else if ((h > w) && (h > divisorDouble)) {
			double ratio = divisorDouble / h;
			h = (int) divisorDouble;
			w = (int) (ratio * w);
		}
		return Bitmap.createScaledBitmap(photoBitmap, w, h, true);

	}

	public Boolean saveImage(Context context, View v) {
		if (surfaceCreated == false) {

			Toast.makeText(context, "Please Select an Image", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		String newFileName = null;
		
		if (selectedUri == null) {
			Calendar c = Calendar.getInstance();
			String sDate = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-"
					+ (c.get(Calendar.DAY_OF_MONTH) + 1) + "_"
					+ c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE);
			newFileName = "stamps-" + sDate + ".png";

		}
		else
		{
			String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME /* col1 */};
			Cursor c = context.getContentResolver().query(selectedUri, projection,
					null, null, null);
			if (c != null && c.moveToFirst()) {
				String oldFileName = c.getString(0);
				int dotpos = oldFileName.lastIndexOf(".");
				if (dotpos > -1) {
					newFileName = oldFileName.substring(0, dotpos) + "-stamps.png";
				}
			}
		}

		// terminate the running thread and join the data
		terminateThread();

		// now save out the file holmes!
		OutputStream outStream = null;
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		

		if (newFileName != null) {
			File file = new File(extStorageDirectory, newFileName);
			try {
				outStream = new FileOutputStream(file);

				lastGoodBitmap.compress(Bitmap.CompressFormat.PNG, 100,
						outStream);

				try {
					outStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					createThread(getHolder());
					return false;
				}
				try {
					outStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					createThread(getHolder());
					return false;
				}

				Toast.makeText(context, "Saved " + newFileName,
						Toast.LENGTH_LONG).show();
				new SingleMediaScanner(context, file);

			} catch (FileNotFoundException e) {
				// do something if errors out?

				createThread(getHolder());
				return false;
			}
		}

		createThread(getHolder());
		return true;

	}

} // end class