package com.lewisjuggins.imagemirror;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Lewis on 01/06/15.
 */
public class ImageProcessorService extends IntentService
{
	private final static String TAG = ImageProcessorService.class.getName();

	private final Handler mHandler;


	public ImageProcessorService()
	{
		super("ImageProcessorService");
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(final Intent i)
	{
		Log.i(TAG, "Starting service");

		final int notificationNumber = Application.increasePendingNotificationsCount();
		final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("Processing").setContentText("Processing images");
		mBuilder.setSmallIcon(R.drawable.icon);
		mBuilder.setProgress(0, 0, true);
		mNotifyManager.notify(notificationNumber, mBuilder.build());

		final Intent intent = i.getParcelableExtra("intent");
		String action = intent.getAction();
		String type = intent.getType();

		if(Intent.ACTION_SEND.equals(action) && type != null)
		{
			if(type.startsWith("image/"))
			{
				handleSendImage(intent);
			}
		}
		else if(Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null)
		{
			if(type.startsWith("image/"))
			{
				handleSendMultipleImages(intent);
			}
		}

		mNotifyManager.cancel(notificationNumber);
		Application.decreasePendingNotificationsCount();
	}

	private void flip(final Intent intent, final Uri imageUri)
			throws FileNotFoundException
	{
		InputStream input = null;
		try
		{
			final String filename_raw = FilenameUtils.getBaseName(imageUri.getPath());
			final String filename = "ACTUAL".equals(filename_raw) ? Long.toString(new Date().getTime()) : filename_raw;
			final String fileext = FilenameUtils.getExtension(imageUri.getPath()).toLowerCase();
			final String ext = "".equals(fileext) ? "jpg" : fileext;

			input = getContentResolver().openInputStream(imageUri);

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			final Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);

			final Matrix m = new Matrix();
			m.preScale(-1, 1);

			final Bitmap dst = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);

			final File output = new File(Environment.getExternalStorageDirectory(), "/ImageMirror/");
			output.mkdir();
			final FileOutputStream out = new FileOutputStream(new File(output, filename + "." + ("".equals(ext) ? "jpg" : ext)));

			final Bitmap.CompressFormat format;
			if(intent.getType().equals("image/png") && ext.equals("png"))
			{
				format = Bitmap.CompressFormat.PNG;
			}
			else
			{
				format = Bitmap.CompressFormat.JPEG;
			}

			dst.compress(format, 100, out);

			out.close();

			mHandler.post(new Runnable()
			{
				public void run()
				{
					Toast.makeText(getBaseContext(), filename + "." + ext + " mirrored!", Toast.LENGTH_SHORT).show();
				}
			});
		}
		catch(java.io.IOException e)
		{
			Log.e(TAG, "File not found?", e);
		}
		finally
		{
			IOUtils.closeQuietly(input);
		}
	}

	private void handleSendImage(final Intent intent)
	{
		final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if(imageUri != null)
		{
			try
			{
				flip(intent, imageUri);
			}
			catch(FileNotFoundException e)
			{
				Log.e(TAG, "File not found?", e);
			}
		}
	}

	private void handleSendMultipleImages(final Intent intent)
	{
		final ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if(imageUris != null)
		{
			for(Uri imageUri : imageUris)
			{
				try
				{
					flip(intent, imageUri);
				}
				catch(FileNotFoundException e)
				{
					Log.e(TAG, "File not found?", e);
				}
			}
		}
	}
}
