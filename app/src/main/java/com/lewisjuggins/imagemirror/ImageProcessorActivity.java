package com.lewisjuggins.imagemirror;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ImageProcessorActivity extends Activity
{
	private final static String TAG = ImageProcessorActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("a", "Create activity");
		setContentView(R.layout.activity_main);

		new AsyncTask<Void,Void,Void>(){

			@Override protected Void doInBackground(final Void... voids)
			{
				final Intent intent = getIntent();
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

				finish();

				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void flip(final Intent intent, final Uri imageUri)
			throws FileNotFoundException
	{
		InputStream input = null;
		try
		{
			final String filename = FilenameUtils.getBaseName(imageUri.getPath());
			final String ext = FilenameUtils.getExtension(imageUri.getPath()).toLowerCase();
			input = getContentResolver().openInputStream(imageUri);

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			final Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);

			final Matrix m = new Matrix();
			m.preScale(-1, 1);
			final Bitmap src = bitmap;

			final Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);

			final File output = new File(Environment.getExternalStorageDirectory(), "/ImageMirror/");
			output.mkdir();
			final FileOutputStream out = new FileOutputStream(new File(output, filename + "." + ext));

			final Bitmap.CompressFormat format;
			if(intent.getType().equals("image/png"))
			{
				format = Bitmap.CompressFormat.PNG;
			}
			else
			{
				format = Bitmap.CompressFormat.JPEG;
			}

			dst.compress(format, 100, out);

			out.close();

			runOnUiThread(new Runnable()
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
