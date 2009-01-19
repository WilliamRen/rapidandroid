package org.rapidandroid.data;

import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

//todo: dmyung
//implement when we are ready to write a context

public class RapidSmsContentProvider extends ContentProvider {
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */

	public static final Uri CONTENT_URI = Uri.parse("content://" + RapidSmsDataDefs.AUTHORITY);

	private static final String TAG = "RapidSmsContentProvider";

	private SmsDbHelper mOpenHelper;

	private static final int MESSAGE = 1; 
	private static final int MESSAGE_ID = 2; 
	private static final int MONITOR = 3;
	private static final int MONITOR_ID = 4;
	private static final int MONITOR_MESSAGE_ID = 5;
	
	private static final int FORM = 6;
	private static final int FORM_ID = 7;
	
	private static final int FIELD = 8;
	private static final int FIELD_ID = 9;
	
	private static final int FIELDTYPE = 10;
	private static final int FIELDTYPE_ID = 11;
	
	private static final int FORMDATA_ID = 12;
	//private static final int FORMDATA_ID = 13;
	
	
	
	
	private static final UriMatcher sUriMatcher;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Message.URI_PART, MESSAGE);		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Message.URI_PART+ "/#",MESSAGE_ID);
		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Monitor.URI_PART, MONITOR);
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Monitor.URI_PART + "/#", MONITOR_ID);
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, "messagesbymonitor/#",MONITOR_MESSAGE_ID);
		
		
		//form field data stuffs
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Form.URI_PART, FORM);		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Form.URI_PART+ "/#",FORM_ID);
		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Field.URI_PART, FIELD);		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.Field.URI_PART+ "/#", FIELD_ID);
		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.FieldType.URI_PART, FIELDTYPE);		
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.FieldType.URI_PART+ "/#",FIELDTYPE_ID);
						
		//actual form data
		sUriMatcher.addURI(RapidSmsDataDefs.AUTHORITY, RapidSmsDataDefs.FormData.URI_PART+ "/#",FORMDATA_ID);		
	}

	public RapidSmsContentProvider(Context context, String name,
			CursorFactory factory, int version) {

		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public RapidSmsContentProvider() {
		// TODO Auto-generated constructor stub
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case MESSAGE:
			return RapidSmsDataDefs.Message.CONTENT_TYPE;
		case MESSAGE_ID:
			return RapidSmsDataDefs.Message.CONTENT_ITEM_TYPE;
		case MONITOR:
			return RapidSmsDataDefs.Monitor.CONTENT_TYPE;
		case MONITOR_ID:
			return RapidSmsDataDefs.Monitor.CONTENT_ITEM_TYPE;
		case MONITOR_MESSAGE_ID:
			//this is similar to Monitor, but is filtered
			return RapidSmsDataDefs.Monitor.CONTENT_TYPE;
			
		case FORM:
			return RapidSmsDataDefs.Form.CONTENT_TYPE;
		case FORM_ID:
			return RapidSmsDataDefs.Form.CONTENT_ITEM_TYPE;
		
		case FIELD:
			return RapidSmsDataDefs.Field.CONTENT_TYPE;
		case FIELD_ID:
			return RapidSmsDataDefs.Field.CONTENT_ITEM_TYPE;

		case FIELDTYPE:
			return RapidSmsDataDefs.FieldType.CONTENT_TYPE;
		case FIELDTYPE_ID:
			return RapidSmsDataDefs.FieldType.CONTENT_ITEM_TYPE;

		case FORMDATA_ID:
			return RapidSmsDataDefs.FormData.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			//return sUriMatcher.match(uri)+"";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	 public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
//        if (sUriMatcher.match(uri) != MESSAGE || sUriMatcher.match(uri) != MONITOR) {
//            throw new IllegalArgumentException("Unknown URI " + uri);
//        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        
        switch (sUriMatcher.match(uri)) {
		case MESSAGE:
			return insertMessage(uri, values);
		case MONITOR:			
			return insertMonitor(uri, values);
		case FIELDTYPE:
			return insertFieldType(uri, values);
		case FIELD:
			return insertField(uri, values);			
		case FORM:
			return insertForm(uri, values);			
		case FORMDATA_ID:
			return insertFormData(uri,values);
		//other stuffs not implemented for insertion yet.
			
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}        
	}
	
	
	private Uri insertFormData(Uri uri, ContentValues values) {
		//sanity check, see if the table exists
		String formid = uri.getPathSegments().get(1);
		SQLiteDatabase dbr = mOpenHelper.getReadableDatabase();
		Cursor table_exists = dbr.rawQuery("select count(*) from formdata_"
				+ formid, null);
		if (table_exists.getCount() != 1) {
			throw new SQLException("Failed to insert row into " + uri
					+ " :: table doesn't exist.");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(RapidSmsDataDefs.FormData.TABLE_PREFIX + formid,
				RapidSmsDataDefs.FormData.MESSAGE, values);
		if (rowId > 0) {
			Uri fieldUri = ContentUris.withAppendedId(
					RapidSmsDataDefs.Form.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(fieldUri, null);
			return Uri.parse(uri.toString() + "/" + rowId);
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	private Uri insertForm(Uri uri, ContentValues values) {
		if (values.containsKey(RapidSmsDataDefs.Form.FORMNAME) == false ||			
			values.containsKey(RapidSmsDataDefs.Form.DESCRIPTION) == false ||
			values.containsKey(RapidSmsDataDefs.Form.PARSEMETHOD) == false ||
			values.containsKey(RapidSmsDataDefs.Form._ID) == false) {			
			throw new SQLException("Insufficient arguments for Form insert " + uri);			
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(RapidSmsDataDefs.Form.TABLE,
				RapidSmsDataDefs.Form.FORMNAME, values);
		if (rowId > 0) {
			Uri fieldUri = ContentUris.withAppendedId(RapidSmsDataDefs.Form.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(fieldUri, null);
			return fieldUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}
	
	public void ClearDebug() {
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		db.execSQL("delete from " + RapidSmsDataDefs.FieldType.TABLE);
		db.execSQL("delete from " + RapidSmsDataDefs.Field.TABLE);
		db.execSQL("delete from " + RapidSmsDataDefs.Form.TABLE);
		
		Log.v("dimagi", "wiped the form/field/fieldtype/formdata table for debug purposes");	
	}
	
	public void ClearFormDataDebug() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor formsCursor = db.query("rapidandroid_form", new String[] {"_id"},null,null,null,null, null); 
		//("select prefix from rapidandroid_form");
		
		//iterate and blow away
		formsCursor.moveToFirst();
		do {
			String id = formsCursor.getString(0);
			String dropstatement = "drop table formdata_" + id +";";
			db.execSQL(dropstatement);
		} while (formsCursor.moveToNext());
		
	}
	
	public void generateFormTable(Form form) {
		//dmyung: 1/19/2009
		//For the intial run through this is a bit hacky.
		
		//for each form, create a new sql table create table script
		//do do that get the form prefix and get a foriegn key back to the message table
		//after that, create all the columns
		//do do this we make a switch statement and we will support the SQLite datatypes.
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		StringBuilder sb = new StringBuilder();
		sb.append("create table formdata_");
		sb.append(form.getFormId());
		sb.append(" (");
		sb.append(" \"_id\" integer not null PRIMARY KEY, ");		
		sb.append(" \"message_id\" integer not null references \"message\", ");
		
		org.rapidsms.java.core.model.Field[] fields = form.getFields();
		int fieldcount = fields.length;
		
		boolean last = false;
		for(int i = 0; i < fieldcount; i++) {
			if(i == fieldcount-1) {
				last = true;
			} 
			getFieldDeclaration(fields[i],sb, last);
		}
		
		sb.append(" );");
		
		db.execSQL(sb.toString());
	}

	private void getFieldDeclaration(Field field, StringBuilder sb, boolean last) {

		sb.append(" \"");
		sb.append("col_" + field.getName());
		sb.append("\"");
		if (field.getFieldType().getDataType().equals("integer")) {
			sb.append(" integer NULL");
		} else if (field.getFieldType().getDataType().equals("number")) {
			sb.append(" integer NULL");
		} else if (field.getFieldType().getDataType().equals("boolean")) {
			sb.append(" bool NULL");
		} else if (field.getFieldType().getDataType().equals("word")) {
			sb.append(" varchar(36) NULL");
		} else if (field.getFieldType().getDataType().equals("ratio")) {
			sb.append(" varchar(36) NULL");
		} else if (field.getFieldType().getDataType().equals("datetime")) {
			sb.append(" datetime NULL");
		}
		if(!last) {
			sb.append(", ");
		}
	}
	
	private Uri insertField(Uri uri, ContentValues values) {
		if (values.containsKey(RapidSmsDataDefs.Field.FORM) == false ||			
			values.containsKey(RapidSmsDataDefs.Field.NAME) == false ||
			values.containsKey(RapidSmsDataDefs.Field.FIELDTYPE) == false ||
			values.containsKey(RapidSmsDataDefs.Field.PROMPT) == false ||
			values.containsKey(RapidSmsDataDefs.Field.SEQUENCE) == false ||
			values.containsKey(RapidSmsDataDefs.Field._ID) == false) {			
			throw new SQLException("Insufficient arguments for field insert " + uri);			
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(RapidSmsDataDefs.Field.TABLE,
				RapidSmsDataDefs.Field.NAME, values);
		if (rowId > 0) {
			Uri fieldUri = ContentUris.withAppendedId(RapidSmsDataDefs.Field.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(fieldUri, null);
			return fieldUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri + " error: " + rowId + " ID: " + values.getAsInteger(RapidSmsDataDefs.Field._ID));
		}
	}
	
	
	private Uri insertFieldType(Uri uri, ContentValues values) {
		if (values.containsKey(RapidSmsDataDefs.FieldType._ID) == false ||
			values.containsKey(RapidSmsDataDefs.FieldType.NAME) == false ||			
			values.containsKey(RapidSmsDataDefs.FieldType.REGEX) == false ||
			values.containsKey(RapidSmsDataDefs.FieldType.DATATYPE) == false) {
			
			throw new SQLException("Insufficient arguments for fieldtype insert " + uri);
			
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(RapidSmsDataDefs.FieldType.TABLE,
				RapidSmsDataDefs.FieldType.NAME, values);
		if (rowId > 0) {
			Uri fieldtypeUri = ContentUris.withAppendedId(RapidSmsDataDefs.FieldType.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(fieldtypeUri, null);
			return fieldtypeUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}
	

	/**
	 * @param uri
	 * @param values
	 */
	private Uri insertMessage(Uri uri, ContentValues values) {
		Long now = Long.valueOf(System.currentTimeMillis());

		// Make sure that the fields are all set
		if (values.containsKey(RapidSmsDataDefs.Message.TIME) == false) {
			values.put(RapidSmsDataDefs.Message.TIME, now);
		}

		if (values.containsKey(RapidSmsDataDefs.Message.MESSAGE) == false) {
			throw new SQLException("No message");
		}

		if (values.containsKey(RapidSmsDataDefs.Message.PHONE) == false) {
			throw new SQLException("No message");
		} else {
			ContentValues monitorValues = new ContentValues();
			monitorValues.put(RapidSmsDataDefs.Monitor.PHONE,values.getAsString(RapidSmsDataDefs.Message.PHONE));
			Uri monitorUri = insertMonitor(RapidSmsDataDefs.Monitor.CONTENT_URI,monitorValues);
			//ok, so we insert the monitor into the monitor table.
			//get the URI back and assign the foreign key into the values as part of the message insert
			values.put(RapidSmsDataDefs.Message.MONITOR, monitorUri.getPathSegments().get(1));
		}

		if (values.containsKey(RapidSmsDataDefs.Message.IS_OUTGOING) == false) {
			throw new SQLException("No direction");
		}

		if (values.containsKey(RapidSmsDataDefs.Message.IS_VIRTUAL) == false) {
			values.put(RapidSmsDataDefs.Message.IS_VIRTUAL, false);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		
		long rowId = db.insert(RapidSmsDataDefs.Message.TABLE,
				RapidSmsDataDefs.Message.MESSAGE, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(
					RapidSmsDataDefs.Message.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		else {
			throw new SQLException("Failed to insert row into " + uri);
		}
		
	}

	/**
	 * @param uri
	 * @param values
	 */
	private Uri insertMonitor(Uri uri, ContentValues values) {
		// Make sure that the fields are all set
		if (values.containsKey(RapidSmsDataDefs.Monitor.PHONE) == false) {
			throw new SQLException("No phone");
		}
		
		if (values.containsKey(RapidSmsDataDefs.Monitor.ALIAS) == false) {
			values.put(RapidSmsDataDefs.Monitor.ALIAS, values.getAsString(RapidSmsDataDefs.Monitor.PHONE));
		}

		if (values.containsKey(RapidSmsDataDefs.Monitor.EMAIL) == false) {
			values.put(RapidSmsDataDefs.Monitor.EMAIL, "");
		}
		
		if (values.containsKey(RapidSmsDataDefs.Monitor.FIRST_NAME) == false) {
			values.put(RapidSmsDataDefs.Monitor.FIRST_NAME, "");
		}

		if (values.containsKey(RapidSmsDataDefs.Monitor.LAST_NAME) == false) {
			values.put(RapidSmsDataDefs.Monitor.LAST_NAME, "");
		}

		if (values.containsKey(RapidSmsDataDefs.Monitor.INCOMING_MESSAGES) == false) {
			values.put(RapidSmsDataDefs.Monitor.INCOMING_MESSAGES, 0);
		}
		
		//ok, so parameters are kosher.  let's check to see if this monitor exists or not.
		System.out.println("Attempting insert of monitor: " + uri + " :: phone=" + values.getAsString(RapidSmsDataDefs.Monitor.PHONE));
		Cursor exists = query(uri,null,RapidSmsDataDefs.Monitor.PHONE + "='" + values.getAsString(RapidSmsDataDefs.Monitor.PHONE) + "'", null, null);
		System.out.println("Insert monitor query result: " + exists.getCount());
		
		if(exists.getCount() == 1) {
			//throw new SQLException("Monitor "  + values.getAsString(RapidSmsDataDefs.Monitor.PHONE) + " already exists");
			exists.moveToFirst();
//			String[] names = exists.getColumnNames();
//			for(int q = 0; q < names.length; q++) {
//				System.out.println("\tMonitorInsert: cols: " + q + "->" + names[q]);
//			}
			return ContentUris.withAppendedId(RapidSmsDataDefs.Monitor.CONTENT_URI, exists.getInt(0));
			 
		} 
		exists.close();
		

		SQLiteDatabase dbmon = mOpenHelper.getWritableDatabase();
		long rowIdmon = dbmon.insert(RapidSmsDataDefs.Monitor.TABLE,
				RapidSmsDataDefs.Monitor.EMAIL, values);
		if (rowIdmon > 0) {
			Uri monitorUri = ContentUris.withAppendedId(
					RapidSmsDataDefs.Monitor.CONTENT_URI, rowIdmon);
			getContext().getContentResolver().notifyChange(monitorUri, null);
			return monitorUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String table;
		String finalWhere = "";
			
		
		switch (sUriMatcher.match(uri)) {
		case MESSAGE:
			table = RapidSmsDataDefs.Message.TABLE;
			break;

		case MESSAGE_ID:
			table = RapidSmsDataDefs.Message.TABLE;
			finalWhere = RapidSmsDataDefs.Message._ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			break;
		case MONITOR:
			table = RapidSmsDataDefs.Monitor.TABLE;
			break;

		case MONITOR_ID:
			table = RapidSmsDataDefs.Monitor.TABLE;
			finalWhere = RapidSmsDataDefs.Message._ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			break;
		case MONITOR_MESSAGE_ID:
			table = RapidSmsDataDefs.Message.TABLE;
//			qb.appendWhere(RapidSmsDataDefs.Message.MONITOR + "="
//					+ uri.getPathSegments().get(1));
			
			finalWhere = RapidSmsDataDefs.Message.MONITOR + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if(finalWhere == "") {
			finalWhere = where;
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int result = db.delete(table, finalWhere, whereArgs);

        return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case MESSAGE:
			qb.setTables(RapidSmsDataDefs.Message.TABLE);
			break;

		case MESSAGE_ID:
			qb.setTables(RapidSmsDataDefs.Message.TABLE);
			qb.appendWhere(RapidSmsDataDefs.Message._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case MONITOR:
			qb.setTables(RapidSmsDataDefs.Monitor.TABLE);
			break;

		case MONITOR_ID:
			qb.setTables(RapidSmsDataDefs.Monitor.TABLE);
			qb.appendWhere(RapidSmsDataDefs.Monitor._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case MONITOR_MESSAGE_ID:
			qb.setTables(RapidSmsDataDefs.Message.TABLE);
			qb.appendWhere(RapidSmsDataDefs.Message.MONITOR + "="
					+ uri.getPathSegments().get(1));
			break;
		case FORM:
			qb.setTables(RapidSmsDataDefs.Form.TABLE);			
			break;						
		case FORM_ID:
			qb.setTables(RapidSmsDataDefs.Form.TABLE);
			qb.appendWhere(RapidSmsDataDefs.Form._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case FIELD:
			qb.setTables(RapidSmsDataDefs.Field.TABLE);			
			break;			
		case FIELD_ID:
			qb.setTables(RapidSmsDataDefs.Field.TABLE);
			qb.appendWhere(RapidSmsDataDefs.Field._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case FIELDTYPE:
			qb.setTables(RapidSmsDataDefs.FieldType.TABLE);			
			break;
		case FIELDTYPE_ID:
			qb.setTables(RapidSmsDataDefs.FieldType.TABLE);
			qb.appendWhere(RapidSmsDataDefs.FieldType._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case FORMDATA_ID:
			//todo:  need to set the table to the FieldData + form_prefix
			//this is possible via querying hte forms to get the formname/prefix from the form table definition
			//and appending that to do the qb.setTables
			//qb.setTables(RapidSmsDataDefs.FieldType.TABLE);
			//qb.appendWhere(RapidSmsDataDefs.FieldType._ID + "=" + uri.getPathSegments().get(1));
			throw new IllegalArgumentException(uri + " query handler not implemented.");
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

        // If no sort order is specified use the default
        String orderBy = sortOrder;
        
//        if (TextUtils.isEmpty(sortOrder)) {
//            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
//        } else {
//            orderBy = sortOrder;
//        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		throw new IllegalArgumentException("Update not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public boolean onCreate() {
		mOpenHelper = new SmsDbHelper(getContext());
		return true;
	}

	
	

}