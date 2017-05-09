package com.github.stevenrudenko.geofence.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Observable;
import rx.schedulers.Schedulers;

/**
 * SQLite implementaion of {@link GeofenceStorage}.
 */
public class SqliteGeofenceStorage implements GeofenceStorage {
    /**
     * Opened DB.
     */
    private final BriteDatabase db;
    /**
     * Query to get geofences.
     */
    private final Observable<SqlBrite.Query> select;

    public SqliteGeofenceStorage(Context context) {
        final SqlBrite sqlBrite = new SqlBrite.Builder().build();
        db = sqlBrite.wrapDatabaseHelper(new DbHeper(context.getApplicationContext()), Schedulers.io());
        rx.Observable<SqlBrite.Query> v1Query = db.createQuery(GeofenceDb.TABLE, GeofenceDb.SELECT);
        select = RxJavaInterop.toV2Observable(v1Query);
    }

    @Override
    public Observable<List<Geofence>> getGeofenceUpdates() {
        return select.map(query -> {
            final Cursor cursor = query.run();
            return read(cursor);
        });
    }

    @Override
    public List<Geofence> getGeofences() {
        return read(db.query(GeofenceDb.SELECT));
    }

    @Override
    public void add(Geofence geofence) {
        final ContentValues values = new ContentValues(5);
        values.put(GeofenceDb.UUID, geofence.getUuid());
        values.put(GeofenceDb.LAT, geofence.getPoint().getLat());
        values.put(GeofenceDb.LNG, geofence.getPoint().getLng());
        values.put(GeofenceDb.RADIUS, geofence.getRadius());
        values.put(GeofenceDb.SSID, geofence.getSsid());
        db.insert(GeofenceDb.TABLE, values);
    }

    @Override
    public void remove(Geofence geofence) {
        db.delete(GeofenceDb.TABLE, GeofenceDb.UUID + " = '" + geofence.getUuid() + "'");
    }

    private List<Geofence> read(Cursor cursor) {
        if (cursor == null) {
            return Collections.emptyList();
        }
        try {
            final List<Geofence> geofences = new ArrayList<>(cursor.getCount());
            final int idxUuid = cursor.getColumnIndex(GeofenceDb.UUID);
            final int idxLat = cursor.getColumnIndex(GeofenceDb.LAT);
            final int idxLng = cursor.getColumnIndex(GeofenceDb.LNG);
            final int idxRadius = cursor.getColumnIndex(GeofenceDb.RADIUS);
            final int idxSsid = cursor.getColumnIndex(GeofenceDb.SSID);
            while (cursor.moveToNext()) {
                final String uuid = cursor.getString(idxUuid);
                final double lat = cursor.getDouble(idxLat);
                final double lng = cursor.getDouble(idxLng);
                final int radius = cursor.getInt(idxRadius);
                final String ssid = cursor.getString(idxSsid);
                final Geofence geofence = new Geofence(
                        uuid,
                        new LocationProvider.Location(lat, lng),
                        radius,
                        ssid
                );
                geofences.add(geofence);
            }
            return geofences;
        } finally {
            cursor.close();
        }
    }

    /**
     * Used to create and open SQLite DB.
     */
    private static class DbHeper extends SQLiteOpenHelper {
        /**
         * DB name.
         */
        private static final String NAME = "geofence";
        /**
         * DB version.
         */
        private static final int VERSION = 1;

        DbHeper(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(GeofenceDb.CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    /**
     * Database table structure.
     */
    private static final class GeofenceDb implements BaseColumns {
        static final String TABLE = "geofence";
        static final String UUID = "uuid";
        static final String LAT = "lat";
        static final String LNG = "lng";
        static final String RADIUS = "radius";
        static final String SSID = "ssid";

        static final String SELECT = "SELECT * FROM " + GeofenceDb.TABLE;

        static final String CREATE =
                "CREATE TABLE " + TABLE + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        UUID + " TEXT," +
                        LAT + " REAL," +
                        LNG + " REAL," +
                        RADIUS + " INTEGER," +
                        SSID + " TEXT)";

    }

}
