package com.example.radek.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.radek.criminalintent.db.CrimeBaseHelper;
import com.example.radek.criminalintent.db.CrimeCursorWrapper;
import com.example.radek.criminalintent.db.CrimeDbSchema;
import com.example.radek.criminalintent.db.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by radek on 09/01/16.
 */
public class CrimeLab {

    private static CrimeLab INSTANCE = null;

    private Context context;
    private SQLiteDatabase db;

    private CrimeLab(Context context) {
        this.context = context.getApplicationContext();
        db = new CrimeBaseHelper(context).getWritableDatabase();

//        for (int i = 0; i < 10000; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0); // every other one
//            crimes.add(crime);
//        }
    }

    public static CrimeLab get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new CrimeLab(context);
        }

        return INSTANCE;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?", new String[] {id.toString()});

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }
    
    public File getPhotoFile(Crime crime) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }

        return new File(externalFilesDir, crime.getPhotoFileName());
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);
        db.insert(CrimeTable.NAME, null, values);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        db.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    public void deleteCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        db.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});
    }

    public static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = db.query(
                CrimeTable.NAME,
                null, // null = all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new CrimeCursorWrapper(cursor);
    }
}
