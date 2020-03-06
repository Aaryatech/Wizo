package com.ats.wizo.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ats.wizo.model.Device;
import com.ats.wizo.model.MoodDevice;
import com.ats.wizo.model.MoodDeviceMapping;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxadmin on 6/1/18.
 */

public class DBHandler extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "wizo.db";
    private static final int DATABASE_VERSION = 6;

    // TABLE SCAN DEVICES MASTER
    private static final String TABLE_SCAN_DEVICES = "m_scan_devices";
    private static final String COLUMN_SCAN_DEVICE_ID = "sc_device_id";
    private static final String COLUMN_SCAN_DEVICE_MAC = "sc_device_mac";

    // TABLE DEVICE MASTER
    private static final String TABLE_DEVICE = "m_device";
    private static final String COLUMN_DEV_ID = "dev_id";
    private static final String COLUMN_DEV_CAPTION = "dev_caption";
    private static final String COLUMN_DEV_IP = "dev_ip";
    private static final String COLUMN_DEV_MAC = "dev_mac";
    private static final String COLUMN_DEV_TYPE = "dev_type";
    private static final String COLUMN_DEV_ROOM_ID = "dev_room_id";
    private static final String COLUMN_DEV_POSITION = "dev_position";
    private static final String COLUMN_DEV_SSID = "devSsid";
    private static final String COLUMN_DEV_IS_USED = "dev_is_used";
    private static final String COLUMN_DEV_OPERATION = "dev_operation";
    private static final String COLUMN_DEV_DETAIL_ID = "dev_detail_id";

    // TABLE ROOM MASTER
    private static final String TABLE_ROOM = "m_room";
    private static final String COLUMN_ROOM_ID = "room_id";
    private static final String COLUMN_ROOM_NAME = "room_name";
    private static final String COLUMN_ROOM_ICON = "room_icon";
    private static final String COLUMN_ROOM_IS_USED = "room_is_used";


    // TABLE MOODS MASTER
    private static final String TABLE_MOOD = "m_mood";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MOOD_ID = "mood_header_id";
    private static final String COLUMN_MOOD_NAME = "mood_name";
    private static final String COLUMN_MOOD_STATUS = "mood_status";


    // TABLE MOOD DEVICES MAPPING
    private static final String TABLE_MOOD_DEVICE = "m_mood_device";
    private static final String COLUMN_MOOD_MAP_ID = "mood_map_id";
    private static final String COLUMN_M_ID = "mood_id";
    private static final String COLUMN_MOOD_DEV_MAC = "mood_dev_mac";
    private static final String COLUMN_MOOD_DEV_TYPE = "mood_dev_type";
    private static final String COLUMN_MOOD_OPERATION = "mood_operation";
    private static final String COLUMN_MOOD_DETAIL_ID = "mood_detail_id";


    // CREATE TABLE STATEMENTS
    private static final String SCAN_DEVICES_TABLE_CREATE =
            "CREATE TABLE " + TABLE_SCAN_DEVICES + " (" +
                    COLUMN_SCAN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SCAN_DEVICE_MAC + " TEXT unique" +
                    ")";

    private static final String DEVICE_TABLE_CREATE =
            "CREATE TABLE " + TABLE_DEVICE + " (" +
                    COLUMN_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DEV_CAPTION + " TEXT, " +
                    COLUMN_DEV_IP + " TEXT, " + COLUMN_DEV_MAC + " TEXT, " +
                    COLUMN_DEV_TYPE + " INTEGER, " + COLUMN_DEV_ROOM_ID + " INTEGER, " +
                    COLUMN_DEV_POSITION + " INTEGER, " + COLUMN_DEV_SSID + " TEXT, " + COLUMN_DEV_IS_USED + " INTEGER, " +
                    COLUMN_DEV_OPERATION + " INTEGER, " +
                    COLUMN_DEV_DETAIL_ID + " INTEGER " +
                    ")";

    private static final String ROOM_TABLE_CREATE =
            "CREATE TABLE " + TABLE_ROOM + " (" +
                    COLUMN_ROOM_ID + " INTEGER , " +
                    COLUMN_ROOM_NAME + " TEXT, " +
                    COLUMN_ROOM_ICON + " TEXT, " +
                    COLUMN_ROOM_IS_USED + " INTEGER " +
                    ")";


    //

    private static final String MOOD_MASTER_TABLE_CREATE =
            "CREATE TABLE " + TABLE_MOOD + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MOOD_ID + " INTEGER , " +

                    COLUMN_MOOD_NAME + " TEXT , " + COLUMN_MOOD_STATUS + " INTEGER " +
                    ")";

    private static final String MOOD_DEVICE_TABLE_CREATE =
            "CREATE TABLE " + TABLE_MOOD_DEVICE + " (" +
                    COLUMN_MOOD_MAP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_M_ID + " INTEGER, " +
                    COLUMN_MOOD_DEV_MAC + " TEXT, " +
                    COLUMN_MOOD_DEV_TYPE + " INTEGER, " +
                    COLUMN_MOOD_OPERATION + " INTEGER, " +
                    COLUMN_MOOD_DETAIL_ID + " INTEGER " +
                    ")";


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCAN_DEVICES_TABLE_CREATE);
        db.execSQL(DEVICE_TABLE_CREATE);
        db.execSQL(ROOM_TABLE_CREATE);
        db.execSQL(MOOD_MASTER_TABLE_CREATE);
        db.execSQL(MOOD_DEVICE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCAN_DEVICES);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
//
//        // Create tables again
//        onCreate(db);

        Log.e("DBHandler ", "on upgrade ");

        if (oldVersion == 1) {


            Log.e("upgrading  ", "From version 1");
            db.execSQL(MOOD_MASTER_TABLE_CREATE);
            db.execSQL(MOOD_DEVICE_TABLE_CREATE);

        }
    }


    // ALL CRUD OPERATIONS

    // Adding New Scan Device
    public void addNewMac(String mac) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SCAN_DEVICE_MAC, mac);

        // Inserting Row
        //db.insert(TABLE_SCAN_DEVICES, null, values);
        db.insertWithOnConflict(TABLE_SCAN_DEVICES, null, values, SQLiteDatabase.CONFLICT_REPLACE);


        db.close(); // Closing database connection
    }

    // Getting Scan Devices List
    public List<String> getAllScanDevices() {
        List<String> scanDeviceList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_SCAN_DEVICES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                scanDeviceList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }


        cursor.close();
        db.close();
        return scanDeviceList;
    }


    // Dump New Room Entry From Server
    public void addNewRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_ID, room.getRoomId());
        values.put(COLUMN_ROOM_NAME, room.getRoomName());
        values.put(COLUMN_ROOM_ICON, room.getRoomIcon());
        values.put(COLUMN_ROOM_IS_USED, room.getRoomIsUsed());

        // Inserting Row
        db.insert(TABLE_ROOM, null, values);
        db.close(); // Closing database connection
    }

    public void updateRoomCaption(String caption, int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_ROOM_NAME, caption);
        db.update(TABLE_ROOM, args, COLUMN_ROOM_ID + "=" + id, null);

    }


    public void deleteRoom(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ROOM, COLUMN_ROOM_ID + "=" + id, null);

    }

    // Get All Rooms List
    public List<Room> getAllRooms() {
        List<Room> roomList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ROOM + " WHERE " + COLUMN_ROOM_IS_USED + " = 1 ORDER BY " + COLUMN_ROOM_ID;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Room room = new Room();
                room.setRoomId(Integer.parseInt(cursor.getString(0)));
                room.setRoomName(cursor.getString(1));
                room.setRoomIcon(cursor.getString(2));
                room.setRoomIsUsed(cursor.getInt(3));

                roomList.add(room);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return roomList;
    }


    // Add New Device
    public void addNewDevice(Device device) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DEV_CAPTION, device.getDevCaption());
        values.put(COLUMN_DEV_IP, device.getDevIp());
        values.put(COLUMN_DEV_MAC, device.getDevMac());
        values.put(COLUMN_DEV_TYPE, device.getDevType());
        values.put(COLUMN_DEV_ROOM_ID, device.getDevRoomId());
        values.put(COLUMN_DEV_POSITION, device.getDevPosition());
        values.put(COLUMN_DEV_SSID, device.getDevSsid());
        values.put(COLUMN_DEV_IS_USED, device.getDevIsUsed());
        values.put(COLUMN_DEV_OPERATION, device.getOperation());
        values.put(COLUMN_DEV_DETAIL_ID, device.getDetailId());

        // Inserting Row
        db.insert(TABLE_DEVICE, null, values);
        db.close(); // Closing database connection
    }

    public void updateDeviceData(String devIp, int roomId, int devId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_DEV_IP, devIp);
        args.put(COLUMN_DEV_ROOM_ID, roomId);
        db.update(TABLE_DEVICE, args, COLUMN_DEV_ID + "=" + devId, null);

    }

    // Get All Device By Room Id
    public List<Device> getAllDevicesByRoomId(int roomId) {
        List<Device> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_ROOM_ID + " = " + roomId + " AND " + COLUMN_DEV_IS_USED + " = 1 ORDER BY " + COLUMN_DEV_TYPE ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.e("DB HANDLER-","************************************* "+deviceList);
        return deviceList;
    }


    public boolean checkIsDeviceExist(int detailId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_MOOD_DEVICE + " where " + COLUMN_MOOD_DETAIL_ID + " = " + detailId;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


    public MoodDeviceMapping getMoodDeviceById(String detailId) {

        MoodDeviceMapping moodDevice = new MoodDeviceMapping();

        SQLiteDatabase db = this.getWritableDatabase();

        String Query = "Select * from " + TABLE_MOOD_DEVICE + " where " + COLUMN_MOOD_DETAIL_ID + " = " + detailId;

        Cursor cursor = db.rawQuery(Query, new String[]{detailId});

        if (cursor.moveToFirst()) {
            do {

                moodDevice.setMoodId(Integer.parseInt(cursor.getString(1)));
                moodDevice.setMoodDevMac(cursor.getString(2));
                moodDevice.setMoodDevType(Integer.parseInt(cursor.getString(3)));
                moodDevice.setMoodOperation(Integer.parseInt(cursor.getString(4)));
                moodDevice.setMoodDetailId(Integer.parseInt(cursor.getString(5)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return moodDevice;
    }


    public List<Device> getAllDevicesByDevMacAndDevType(String devMac, int devType) {

        List<Device> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_MAC + " = '" + devMac + "' AND " + COLUMN_DEV_TYPE + " = " + devType + " AND " + COLUMN_DEV_IS_USED + " = 1 ORDER BY " + COLUMN_DEV_ID + " AND " + COLUMN_DEV_POSITION;

        // String selectQuery = "SELECT  d.*, md." + COLUMN_MOOD_OPERATION + ", md." + COLUMN_MOOD_DETAIL_ID + " FROM " + TABLE_DEVICE + " d, " + TABLE_MOOD_DEVICE + " md WHERE md." + COLUMN_M_ID + "=143 AND md." + COLUMN_MOOD_DEV_TYPE + "=d." + COLUMN_DEV_TYPE + " AND d." + COLUMN_DEV_IS_USED + "=1 ORDER BY d." + COLUMN_DEV_ID + " AND d." + COLUMN_DEV_POSITION;


        //       SELECT d.*,md.operation,md.mood_detail_id from m_mood_detail md, m_device d where md.mood_header_id=143  AND md.dev_mac=d.dev_mac AND md.dev_type=d.dev_type AND d.dev_is_used=1 ORDER BY d.dev_id AND d.dev_position

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return deviceList;
    }

    public List<Device> getAllDevicesByDevMac(String devMac) {

        List<Device> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_MAC + " = '" + devMac + "' AND " + COLUMN_DEV_IS_USED + " = 1 ORDER BY " + COLUMN_DEV_ID + " AND " + COLUMN_DEV_POSITION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return deviceList;
    }


    // Get All Device
    public List<Device> getAllDevices() {
        List<Device> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            do {
                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                deviceList.add(device);

            } while (cursor.moveToNext());

        }

        cursor.close();
        db.close();
        return deviceList;
    }


    // Get All Device
    public List<MoodDevice> getAllDevicesForMood() {
        List<MoodDevice> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MoodDevice device = new MoodDevice();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                device.setSelected(false);

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return deviceList;
    }


    // Get All Device By Device Type
    public List<Device> getParentDevicesByRoomId(int roomId) {
        List<Device> deviceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_TYPE + " = 0 OR " + COLUMN_DEV_TYPE + " = 678 OR " + COLUMN_DEV_TYPE + " = 1 AND " + COLUMN_DEV_ROOM_ID + " = " + roomId + " AND " + COLUMN_DEV_IS_USED + " = 1 ORDER BY "+ COLUMN_DEV_ID + " AND " + COLUMN_DEV_POSITION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(9));
                device.setDetailId(cursor.getInt(10));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.e("PARENT DEVICE DB"," -****************************************** "+deviceList);

        return deviceList;
    }

    // Update Device Caption


    public void updateDeviceCaption(String caption, Device device) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_DEV_CAPTION, caption);
        db.update(TABLE_DEVICE, args, COLUMN_DEV_ID + "=" + device.getDevId(), null);

    }

    public void updateDeviceIP(String newIP, String mac) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_DEV_IP, newIP);
        db.update(TABLE_DEVICE, args, COLUMN_DEV_MAC + "=" + "'" + mac + "'", null);

    }


    // Get Topics for subscription
    public List<String> getAllTopics() {
        List<String> macList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  DISTINCT(" + COLUMN_DEV_MAC + ") FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_IS_USED + " = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                macList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return macList;
    }

    // Get Topics for subscription
    public List<String> getAllHomeRouters() {
        List<String> macList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  DISTINCT(" + COLUMN_DEV_SSID + ") FROM " + TABLE_DEVICE + " WHERE " + COLUMN_DEV_IS_USED + " = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                macList.add(cursor.getString(0));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return macList;
    }

    // MOOD MASTER

    // Dump New Entry
    public void addNewMood(MoodMaster master) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(COLUMN_MOOD_ID, master.getMoodId());
        values.put(COLUMN_MOOD_NAME, master.getMoodName());
        values.put(COLUMN_MOOD_STATUS, master.getMoodStatus());


        // Inserting Row
        db.insert(TABLE_MOOD, null, values);
        db.close(); // Closing database connection
    }


    // Mapping Mood - Device
    public void addNewDeviceToMood(MoodDeviceMapping mapping) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(COLUMN_M_ID, mapping.getMoodId());
        values.put(COLUMN_MOOD_DEV_MAC, mapping.getMoodDevMac());
        values.put(COLUMN_MOOD_DEV_TYPE, mapping.getMoodDevType());
        values.put(COLUMN_MOOD_OPERATION, mapping.getMoodOperation());
        values.put(COLUMN_MOOD_DETAIL_ID, mapping.getMoodDetailId());


        // Inserting Row
        db.insert(TABLE_MOOD_DEVICE, null, values);
        db.close(); // Closing database connection

        Log.e("SAVE MOOD TO DB ", "-----------*****************************************************------------------- " + mapping);


    }


    public void updateMoodStatus(int id, int status) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_MOOD_STATUS, status);
        db.update(TABLE_MOOD, args, COLUMN_MOOD_ID + "=" + id, null);

    }

    public void updateMoodOperation(int id, int operation) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_MOOD_OPERATION, operation);
        db.update(TABLE_MOOD_DEVICE, args, COLUMN_MOOD_DETAIL_ID + "=" + id, null);

        Log.e("SQLITE - 682 ", "--------------------************************************* updateMoodOperation - id : " + id + "             operation : " + operation);

    }


    public List<MoodMaster> getAllMoods() {
        List<MoodMaster> list = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_MOOD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                MoodMaster master = new MoodMaster();

                master.setMoodId(Integer.parseInt(cursor.getString(1)));
                master.setMoodName(cursor.getString(2));
                master.setMoodStatus(Integer.parseInt(cursor.getString(3)));

                list.add(master);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public List<MoodMaster> getMoodByName(String name) {
        List<MoodMaster> list = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MOOD + " WEHRE " + COLUMN_MOOD_NAME + "=" + name;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM m_mood WHERE mood_name = ?; ", new String[]{name});

        if (cursor.moveToFirst()) {
            do {

                MoodMaster master = new MoodMaster();

                master.setMoodId(Integer.parseInt(cursor.getString(1)));
                master.setMoodName(cursor.getString(2));
                master.setMoodStatus(Integer.parseInt(cursor.getString(3)));

                list.add(master);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public MoodMaster getMoodById(String id) {

        MoodMaster master = new MoodMaster();

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM m_mood WHERE id = ?; ", new String[]{id});

        if (cursor.moveToFirst()) {
            do {

                master.setMoodId(Integer.parseInt(cursor.getString(1)));
                master.setMoodName(cursor.getString(2));
                master.setMoodStatus(Integer.parseInt(cursor.getString(3)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return master;
    }

    // Get All Device By Mood Id
    public List<Device> getAllDevicesByMoodId(int moodId) {
        List<Device> deviceList = new ArrayList<>();
        // Select All Query


        String selectQuery = "SELECT  * FROM " + TABLE_MOOD_DEVICE + " WHERE " + COLUMN_M_ID + " = " + moodId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                String devMac = cursor.getString(2);
                int devType = Integer.parseInt(cursor.getString(3));

                Log.e("MOOD DEVICES", "*--------------------- ************ ----------------- " + cursor.getString(2) + "            OPERATION :  " + cursor.getString(4) + "               DETAIL ID : " + cursor.getString(5));

                Log.e("DBHandler ", " dev mac " + devMac);

                deviceList.addAll(getAllDevicesByDevMacAndDevType(devMac, devType));


            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();


        return deviceList;
    }


    public List<Device> getDevicesByMoodId(int moodId) {

        List<Device> deviceList = new ArrayList<>();
        // Select All Query

        String selectQuery = "SELECT  d.*, md." + COLUMN_MOOD_OPERATION + ", md." + COLUMN_MOOD_DETAIL_ID + " FROM " + TABLE_DEVICE + " d, " + TABLE_MOOD_DEVICE + " md WHERE md." + COLUMN_M_ID + "=" + moodId + " AND md." + COLUMN_MOOD_DEV_TYPE + "=d." + COLUMN_DEV_TYPE + " AND md." + COLUMN_MOOD_DEV_MAC + "=d." + COLUMN_DEV_MAC + " AND d." + COLUMN_DEV_IS_USED + "=1 ORDER BY d." + COLUMN_DEV_ID + " AND d." + COLUMN_DEV_POSITION;


        //       SELECT d.*,md.operation,md.mood_detail_id from m_mood_detail md, m_device d where md.mood_header_id=143  AND md.dev_mac=d.dev_mac AND md.dev_type=d.dev_type AND d.dev_is_used=1 ORDER BY d.dev_id AND d.dev_position

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();

                device.setDevId(Integer.parseInt(cursor.getString(0)));
                device.setDevCaption(cursor.getString(1));
                device.setDevIp(cursor.getString(2));
                device.setDevMac(cursor.getString(3));
                device.setDevType(cursor.getInt(4));
                device.setDevRoomId(cursor.getInt(5));
                device.setDevPosition(cursor.getInt(6));
                device.setDevSsid(cursor.getString(7));
                device.setDevIsUsed(cursor.getInt(8));
                device.setOperation(cursor.getInt(11));
                device.setDetailId(cursor.getInt(12));

                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.e("MOOD DEV LIST : ", "-------------------***************************------------------------- " + deviceList);

        return deviceList;
    }


    public void deleteRow(int moodId, String devMac, int devId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM m_mood_device WHERE mood_id =" + moodId + " AND mood_dev_mac= '" + devMac + "' AND mood_dev_type=" + devId);
        db.close();
    }


    public void deleteMood(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MOOD, COLUMN_MOOD_ID + "=" + id, null);
        db.delete(TABLE_MOOD_DEVICE, COLUMN_M_ID + "=" + id, null);

    }

    public void updateMoodCaption(String caption, int moodId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(COLUMN_MOOD_NAME, caption);
        db.update(TABLE_MOOD, args, COLUMN_MOOD_ID + "=" + moodId, null);

    }


    public void truncateAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DEVICE);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_SCAN_DEVICES);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_ROOM);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_MOOD);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_MOOD_DEVICE);
        db.execSQL("VACUUM");
        db.close();
    }


}
