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


    long createdRecordsCount = 0;

    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {
        System.out.println("trying to insert locations");
        System.out.println(locationDTOS.size());

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (int i = 0; i < locationDTOS.size(); i++) {
                System.out.println("createLocation");
                boolean isCreated= createLocation(locationDTOS.get(i), db);
                Log.d("LocationCreated", String.valueOf(isCreated));
            }
        } catch (SQLException e){
            isInserted=false;
            throw new DAOException(e.getMessage(), e);
        }finally {
            db.endTransaction();
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
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "TRUE");
            Log.d("VALUES:","VALUES: "+values);

            createdRecordsCount = db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
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
