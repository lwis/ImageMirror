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
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ImageProcessorActivity extends Activity
{
	private final static String TAG = ImageProcessorActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("a", "Start activity");

		final Intent intent = new Intent(this, ImageProcessorService.class);
		intent.putExtra("intent", getIntent());
		startService(intent);

		finish();
	}
}
