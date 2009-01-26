package org.rapidandroid.content.translation;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import org.rapidandroid.data.RapidSmsDataDefs;
import org.rapidsms.java.core.model.Message;
import org.rapidsms.java.core.model.Monitor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.method.DateTimeKeyListener;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 21, 2009
 * 
 *          Helper class to simplify the insertion and querying of raw SMS
 *          messages from the content provider
 * 
 */

public class MessageTranslator {
	
	private static HashMap<Integer,Monitor> mMonitorHash;
	
	public static void updateMonitorHash(Context context) {
		mMonitorHash = new HashMap<Integer,Monitor>();
		Cursor monitorCursor = context.getContentResolver().query(RapidSmsDataDefs.Monitor.CONTENT_URI, null,null,null,null);
		if(monitorCursor.getCount() == 0) {
			return;
		}
		
		monitorCursor.moveToFirst();
		
		do {
			//(int id, String firstName, String lastName, String alias,
			//String phone, String email, int incomingMessages) {
			Monitor newMonitor = new Monitor(monitorCursor.getInt(Monitor.COL_ID), 
					monitorCursor.getString(Monitor.COL_FIRSTNAME), 
					monitorCursor.getString(Monitor.COL_LASTNAME),
					monitorCursor.getString(Monitor.COL_ALIAS),
					monitorCursor.getString(Monitor.COL_PHONE),
					monitorCursor.getString(Monitor.COL_EMAIL),
					monitorCursor.getInt(Monitor.COL_MESSAGECOUNT));
			mMonitorHash.put(Integer.valueOf(newMonitor.getID()), newMonitor);			
		}while (monitorCursor.moveToNext());
		monitorCursor.close();
	}
	
	
	public static Message GetMessage(Context context, int messageID) {
		updateMonitorHash(context);
		
		Uri getMessageUri = Uri.parse(RapidSmsDataDefs.Message.CONTENT_URI_STRING + messageID);
		
		Cursor msgCursor = context.getContentResolver().query(getMessageUri, null,null,null,null);
		msgCursor.moveToFirst();
		if(msgCursor.getCount() != 1) {
			return null;
		} else {
			try {
				Message newMessage = new Message(msgCursor.getInt(Message.COL_ID),
												msgCursor.getString(Message.COL_MESSAGE), 
												DateFormat.getDateTimeInstance().parse(msgCursor.getString(Message.COL_TIME)),									
												mMonitorHash.get(Integer.valueOf(msgCursor.getInt(Message.COL_MONITOR))));
				msgCursor.close();
				return newMessage;
			} catch (Exception ex) {
				Message newMessage = new Message(msgCursor.getInt(Message.COL_ID),
						msgCursor.getString(Message.COL_MESSAGE), 
						new Date(),									
						mMonitorHash.get(Integer.valueOf(msgCursor.getInt(Message.COL_MONITOR))));;
				msgCursor.close();
						return newMessage;
			}			
		}
	}
	
	public static Message[] GetMessages(Context context, int[] messages) {
		
		updateMonitorHash(context);
		
		
		Uri getMessageUri = RapidSmsDataDefs.Message.CONTENT_URI;
		String whereclause = "_id in (";
		int length = messages.length;
		for(int i = 0; i < length; i++) {
			whereclause += messages[i];
			if(i < length-1) {
				whereclause += ",";
			}
		}
		whereclause += ")";
		
		Cursor msgCursor = context.getContentResolver().query(getMessageUri,null,whereclause,null,"time DESC");
		int retlen = msgCursor.getCount();
		Message[] ret = new Message[retlen];
		
		msgCursor.moveToFirst();
		for(int i = 0; i < retlen; i++) {
			//public Message(int id, String message, Date timestamp, Monitor mMonitorString) {
		
			
			try {
				Message newMessage = new Message(msgCursor.getInt(Message.COL_ID),
												msgCursor.getString(Message.COL_MESSAGE),
												DateFormat.getInstance().parse(msgCursor.getString(Message.COL_TIME)),
												mMonitorHash.get(Integer.valueOf(msgCursor.getInt(Message.COL_MONITOR))));
				ret[i] = newMessage;
			} catch (Exception ex) {
				//unable to parse datetime format
				
			}			
			msgCursor.moveToNext();
		}
		msgCursor.close();
		return ret;
	}

	
}
