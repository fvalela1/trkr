package com.example.francescovalela.trkr.data.local.methodofpayment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.example.francescovalela.trkr.data.local.MethodOfPaymentDataSource;
import com.example.francescovalela.trkr.logExpense.models.MethodOfPayment;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Created by flavazelli on 2017-02-14.
 */

public class MethodOfPaymentLocalDataSource implements MethodOfPaymentDataSource  {
    private MethodOfPaymentDbHelper mDbHelper;

    private static MethodOfPaymentLocalDataSource INSTANCE;

    //private to prevent instantiation
    private MethodOfPaymentLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        mDbHelper = new MethodOfPaymentDbHelper(context);
    }

    //constructor
    public static MethodOfPaymentLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MethodOfPaymentLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public void getMethodOfPayments(@NonNull MethodOfPaymentDataSource.LoadMethodOfPaymentsCallback callback) {
        List<MethodOfPayment> methodOfPayments = new ArrayList<MethodOfPayment>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                MethodOfPaymentContract.MethodOfPaymentEntry._ID,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_DATE,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_NICKNAME,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_TYPEID
        };

        Cursor c = db.query(MethodOfPaymentContract.MethodOfPaymentEntry.TABLE_NAME, projection, null, null, null,null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(MethodOfPaymentContract.MethodOfPaymentEntry._ID));
                long date = c.getLong(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_DATE));
                String nickname = c.getString(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_NICKNAME));
                int typeId = c.getInt(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_TYPEID));
                MethodOfPayment type = new MethodOfPayment(nickname,typeId,date);
                methodOfPayments.add(type);
            }
        }

        if (c != null) {
            c.close();
        }

        db.close();

        // will go in if table is empty/new
        if (methodOfPayments.isEmpty()) {
            callback.onDataNotAvailable();
        } else {
            callback.onMethodOfPaymentsLoaded(methodOfPayments);
        }
    }

    @Override
    public void getMethodOfPayment(@NonNull String methodOdfPaymentId, @NonNull MethodOfPaymentDataSource.GetMethodOfPaymentCallback callback) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                MethodOfPaymentContract.MethodOfPaymentEntry._ID,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_DATE,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_NICKNAME,
                MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_TYPEID
        };

        // because ID is primary key, it's all we need to search
        String selection = MethodOfPaymentContract.MethodOfPaymentEntry._ID + " LIKE ?";
        String[] selectionArgs = { methodOdfPaymentId };


        Cursor c = db.query(MethodOfPaymentContract.MethodOfPaymentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        MethodOfPayment type = null;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();

            int id = c.getInt(c.getColumnIndex(MethodOfPaymentContract.MethodOfPaymentEntry._ID));
            String nickname = c.getString(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_NICKNAME));
            long date = c.getLong(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_DATE));
            int typeId = c.getInt(c.getColumnIndexOrThrow(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_TYPEID));
            type = new MethodOfPayment(nickname,typeId,date);
        }

        if (c != null) {
            c.close();
        }

        db.close();

        // will go in if expense is populated
        if (type != null) {
            callback.onMethodOfPaymentLoaded(type);
        } else {
            callback.onDataNotAvailable();
        }

    }

    @Override
    public void saveMethodOfPayment(@NonNull MethodOfPayment methodOfPayment) {
        checkNotNull(methodOfPayment);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_DATE, methodOfPayment.getDate().getTime());
        contentValues.put(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_NICKNAME, methodOfPayment.getNickname());
        //contentValues.put(MethodOfPaymentContract.MethodOfPaymentEntry.COLUMN_NAME_TYPEID, methodOfPayment.get());

        db.insert(MethodOfPaymentContract.MethodOfPaymentEntry.TABLE_NAME, null, contentValues);
        db.close();
    }

    @Override
    public void deleteAllMethodOfPayments() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(MethodOfPaymentContract.MethodOfPaymentEntry.TABLE_NAME, null, null);

        db.close();
    }

    @Override
    public void deleteMethodOfPayment(@NonNull String typeId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = MethodOfPaymentContract.MethodOfPaymentEntry._ID + " LIKE ?";
        String[] arguments = {typeId};

        db.delete(MethodOfPaymentContract.MethodOfPaymentEntry.TABLE_NAME, selection, arguments);

        db.close();
    }
}
