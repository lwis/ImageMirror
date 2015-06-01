package com.lewisjuggins.imagemirror;

/**
 * Created by Lewis on 01/06/15.
 */
public class Application extends android.app.Application
{
	private static int pendingNotificationsCount = 0;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static int getPendingNotificationsCount()
	{
		return pendingNotificationsCount;
	}

	public static void decreasePendingNotificationsCount()
	{
		pendingNotificationsCount--;
	}

	public static int increasePendingNotificationsCount()
	{
		return pendingNotificationsCount++;
	}
}