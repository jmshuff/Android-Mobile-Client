package app.insightfuleye.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.dto.LocationDTO;
import app.insightfuleye.client.utilities.exception.DAOException;

public class LocationDAO {
    private long createdRecordsCount = 0;



    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        try {
            db.beginTransaction();
            Log.d("Locations to insert", String.valueOf(locationDTOS.size()));
            Log.d("location name 1", locationDTOS.get(0).getName());
            for (LocationDTO location : locationDTOS) {
                boolean isCreated= createLocation(location, db);
                Log.d("LocationCreated", String.valueOf(isCreated));
            }
            db.setTransactionSuccessful();
        } catch (SQLException e){
            isInserted=false;
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        }finally {
            db.endTransaction();
            db.close();
        }
        return isInserted;
    }

    private boolean createLocation(LocationDTO location, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            System.out.println(location.getName());
            values.put("name", location.getName());
            values.put("locationuuid", location.getLocationuuid());
            values.put("modified_date", String.valueOf(AppConstants.dateAndTimeUtils.currentDateTime()));
            values.put("sync", "TRUE");
            Log.d("VALUES:","VALUES: "+values);

            createdRecordsCount = db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            Log.e("createLocation", e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }
    public List<LocationDTO> getLocationsDb() throws DAOException{
        List<LocationDTO> locationDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_location", null);
        Log.d("locationdb", String.valueOf(idCursor.getCount()));

        if (idCursor.getCount()!=0){
            while (idCursor.moveToNext()) {
                LocationDTO locationDTO= new LocationDTO();
                locationDTO.setName(idCursor.getString(idCursor.getColumnIndexOrThrow("name")));
                locationDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                locationDTOList.add(locationDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return locationDTOList;
    }

}
