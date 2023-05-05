package app.insightfuleye.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.utilities.Base64Utils;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;

public class ImagesDAO {
    public String TAG = ImagesDAO.class.getSimpleName();

    public boolean insertObsImageDatabase(String uuid, String encounteruuid, String conceptUuid) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("encounteruuid", encounteruuid);
            contentValues.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("conceptuuid", conceptUuid);
            contentValues.put("voided", "0");
            contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_obs", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isInserted;
    }

    public boolean updateObs(String uuid) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            values.put("sync", "TRUE");
            updatedCount = db.update("tbl_obs", values, selection, new String[]{uuid});
            //If no value is not found, then update fails so insert instead.
            if (updatedCount == 0) {
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }

        return true;
    }


    public void removeAzureSynced(String imageName) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        File file= new File(AppConstants.IMAGE_PATH + imageName);
        try {
            String query = "Select * from tbl_azure_img_uploads where imageName = \'" + imageName + "\'";
            Cursor cursor = localdb.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                //while (cursor.moveToNext()) {
                    //String dbImageName = cursor.getString(cursor.getColumnIndexOrThrow("imageName"));
                    //Log.d("FileErase", dbImageName);
                    //Log.d("File erase", imageName);
                    localdb.execSQL("DELETE from tbl_azure_img_uploads where imageName = \'" + imageName + "\'");
                    localdb.setTransactionSuccessful();
//                    List<azureResults> imageQueue = new ArrayList<>();
//                    try {
//                        imageQueue = getAzureImageQueue();
//                        Log.e(TAG, imageQueue.toString());
//                    } catch (DAOException e) {
//                        FirebaseCrashlytics.getInstance().recordException(e);
//                    }

                //if (file.exists()) {
                //      file.delete();
                //  }
                //}
            }
        }
        catch (SQLException e){
            throw new DAOException(e);
        }
        finally {
            localdb.endTransaction();
        }
    }

    public void removeAzureFromVisit(String visitUuid) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        //File file= new File(AppConstants.IMAGE_PATH + imageName);
        try {
            String query = "Select * from tbl_azure_img_uploads where visitId = \'" + visitUuid + "\'";
            Cursor cursor = localdb.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    localdb.execSQL("DELETE from tbl_azure_img_uploads where visitId = \'" + visitUuid + "\'");
                }
                localdb.setTransactionSuccessful();
            }
        }
        catch (SQLException e){
            throw new DAOException(e);
        }
        finally {
            localdb.endTransaction();
        }
    }


    public ArrayList getImageUuid(String encounterUuid, String conceptuuid) throws DAOException {
        Logger.logD(TAG, "encounter uuid for image " + encounterUuid);
        ArrayList<String> uuidList = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE", new String[]{encounterUuid, conceptuuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return uuidList;
    }



    public List<String> isImageListObsExists(String encounterUuid, String conceptUuid) throws DAOException {
        List<String> imagesList = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE order by modified_date", new String[]{encounterUuid, conceptUuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    imagesList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
        }

        return imagesList;
    }


    public boolean isLocalImageUuidExists(String imageuuid) throws DAOException {
        boolean isLocalImageExists = false;
        File imagesPath = new File(AppConstants.IMAGE_PATH);
        String imageName = imageuuid + ".jpg";
        if (new File(imagesPath + "/" + imageName).exists()) {
            isLocalImageExists = true;
        }
        return isLocalImageExists;
    }



    public ArrayList<azureResults> getAzureImageQueue() throws DAOException {
        //get unsynced images from local storage
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ArrayList<azureResults> azureResultList = new ArrayList<>();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_azure_img_uploads", null);
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    azureResults ImageQueue= new azureResults();
                    ImageQueue.setImagePath(idCursor.getString(idCursor.getColumnIndexOrThrow("imageName")));
                    ImageQueue.setLeftRight(idCursor.getString(idCursor.getColumnIndexOrThrow("type")));
                    ImageQueue.setVisitId(idCursor.getString(idCursor.getColumnIndexOrThrow("visitId")));
                    ImageQueue.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patientId")));
                    azureResultList.add(ImageQueue);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return azureResultList;
    }

}

