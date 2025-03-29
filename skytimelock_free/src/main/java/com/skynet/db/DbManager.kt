package com.skynet.db

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.skynet.db.DbInitParam.DbBaseParam

class DbManager : SQLiteOpenHelper {
    private val ctx: Context
    private val PATH: String
    private var SDB: SQLiteDatabase? = null
    var DB_NAME: String

    private var pref: SharedPreferences? = null

    companion object {
        @Suppress("DEPRECATION")
        val MODE_READ = Activity.MODE_WORLD_READABLE
        val MODE_WRITE = Activity.MODE_PRIVATE
        const val SharedPreferencesName = "skynet_framework_database"
    }

    override fun onCreate(db: SQLiteDatabase) {
        addAppendTable()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        addAppendTable()
    }

    private fun updateTable(db: SQLiteDatabase, sql: String) {
        try {
            db.execSQL(sql)
        } catch(e: Exception) {
            setLog("DB Upgrade Add Option Error : ${e.message}")
        }
    }

    /**
     * 첫 실행 여부 리턴
     *  - 1 : 첫 실행, 2 : 첫 실행 이후
     * @return
     */
    fun getFirstExecute(): Boolean {
        pref = ctx.getSharedPreferences(SharedPreferencesName, MODE_READ)
        val isFirst = pref?.getString("SkyNetDataBase_FirstTime", "1")

        return if (isFirst == "1") {
            setFirstExecute()
            false
        } else {
            true
        }
    }

    fun setFirstExecute() {
        pref = ctx.getSharedPreferences(SharedPreferencesName, MODE_WRITE)
        val editor = pref?.edit()
        editor?.putString("SkyNetDataBase_FirstTime", "2")
        editor?.commit() // 저장
    }

    /**
     * 추가 테이블
     */
    private fun addAppendTable() {
        // 어플이 설치되고 최초 한 번만 실행
        if(getFirstExecute()) {
            var script = ""

            // 잠금 어플 리스트
            script = "CREATE TABLE IF NOT EXISTS applist (pkgnme VARCHAR PRIMARY KEY  NOT NULL ,app_stdte VARCHAR,app_eddte VARCHAR,exe_gbn CHAR NOT NULL ,exe_stdte VARCHAR,exe_eddte VARCHAR,lmttime VARCHAR NOT NULL ,spctime VARCHAR,exe_date VARCHAR, option1 VARCHAR, option2 VARCHAR, option3 VARCHAR, option4 VARCHAR)"
            existTable("applist", script)

            // 허용 가능 앱
            script = "CREATE TABLE IF NOT EXISTS allowedlist (pkgnme VARCHAR PRIMARY KEY  NOT NULL)"
            existTable("allowedlist", script)

            // 어플 실행 정보
            script = "CREATE TABLE IF NOT EXISTS appcount (pkgnme VARCHAR NOT NULL , exedte VARCHAR NOT NULL , exetime NUMERIC, execnt NUMERIC, PRIMARY KEY (pkgnme, exedte))"
            existTable("appcount", script)

            // 환경설정
            script = "CREATE TABLE IF NOT EXISTS appsetting (uid VARCHAR PRIMARY KEY  NOT NULL, mode VARCHAR, lock_mode VARCHAR, bg VARCHAR, pwd VARCHAR, pattern VARCHAR, statusicon VARCHAR,smslock VARCHAR,smsunlockmsg VARCHAR, option1 BOOL, option2 BOOL, option3 BOOL, option4 BOOL, option5 BOOL, option6 BOOL, option7 BOOL, option8 BOOL, option9 BOOL, option10 BOOL, prior VARCHAR, prior_value VARCHAR, time_start VARCHAR, time_end VARCHAR, bluelight VARCHAR)"
            existTable("appsetting", script)

            // 일일 요일별 설정
            script = "CREATE TABLE IF NOT EXISTS priorlist (dayweek VARCHAR, lmttime VARCHAR NOT NULL ,spctime VARCHAR, exe_eddte VARCHAR)"
            existTable("priorlist", script)
        }
    }

    /**
     * 초기화
     * @param ctx Context
     * @param DB_NAME Database name
     */
    @SuppressLint("SdCardPath")
    constructor(ctx: Context, DB_NAME: String) : super(ctx, DB_NAME, null, DbBaseParam.DB_VERSION) {
        this.DB_NAME = DB_NAME
        this.ctx = ctx

        PATH = "/data/data/${ctx.packageName}/databases/"

        try {
            SDB = writableDatabase

            // 테이블 추가
            addAppendTable()
        } catch(e: Exception) {
            setLog("DbManager 생성 오류 : ${e.message}")
        }
    }

    /**
     * 초기화
     * @param ctx Context
     */
    @SuppressLint("SdCardPath")
    constructor(ctx: Context) : super(ctx, DbBaseParam.DB_NAME, null, DbBaseParam.DB_VERSION) {
        this.DB_NAME = DbBaseParam.DB_NAME
        this.ctx = ctx

        PATH = "/data/data/${ctx.packageName}/databases/"

        try {
            SDB = writableDatabase

            // 테이블 추가
            addAppendTable()
        } catch(e: Exception) {
            setLog("DbManager 생성 오류 : ${e.message}")
        }
    }

    /**
     * 테이블 존재 여부 확인
     * @param table
     * @param script
     */
    private fun existTable(table: String, script: String) {
        var c: Cursor? = null
        try {
            open()

            c = SDB?.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$table'", null)
            if(c == null || !c.moveToFirst()) {
                setLog("Table creation : $table")
                SDB?.execSQL(script)
            }
            c?.close()
        } catch(e: Exception) {
            setLog("Table creation :: ${e.message}")
        } finally {
            if (c != null && !c.isClosed) c.close()

            close()
        }
    }

    /**
     * SQL 실행
     * @param script
     */
    fun executeSQL(script: String) {
        try {
            open()

            SDB?.execSQL(script)
        } catch(e: Exception) {
            setLog("Table creation :: ${e.message}")
        } finally {
            close()
        }
    }

    /**
     * DB Close
     */
    override fun close() {
        try {
            if(SDB != null && SDB?.isOpen == true)
                SDB?.close()

        } catch(e: Exception) {
            setLog("DB Close 오류 : ${e.message}")
        }
    }

    /**
     * DB Open
     */
    private fun open() {
        close()

        if(SDB != null && SDB?.isOpen == true) return

        SDB = SQLiteDatabase.openDatabase("$PATH$DB_NAME", null, SQLiteDatabase.NO_LOCALIZED_COLLATORS)
    }

    /**
     * Execute Insert
     *
     * @param table 테이블명
     * @param values HashMap<필드명, value>
     * @param whereClause where절
     * @param whereArgs where절 값
     * @return
     */
    fun executeUpdate(table: String, values: HashMap<Any, Any>, whereClause: String?, whereArgs: Array<String>?): Long {
        var rst: Long = -1

        try {
            open()

            val valueSet = ContentValues()

            val keys = values.keys.iterator()
            var key: String? = null
            var value: String? = null

            while(keys.hasNext()) {
                key = keys.next().toString()
                value = values[key].toString()

                valueSet.put(key, value)
            }
            rst = SDB?.update(table, valueSet, whereClause, whereArgs)?.toLong() ?: -1
        } catch(e: Exception) {
            setLog("executeUpdate $e")
        } finally {
            close()
        }

        return rst
    }

    /**
     * 지정한 쿼리를 실행한다.
     *  : UPSERT -> INSERT OR REPLACE INTO page (id, name, title, content, author)
     *
     * @param query
     * @param values
     * @return
     */
    fun executeSave(query: String, values: Array<Any?>): Boolean {
        var rtn = false

        try {
            open()
            SDB?.execSQL(query, values)
            rtn = true
        } catch(e: Exception) {
            setLog("executeSave $e")
        } finally {
            close()
        }

        return rtn
    }

    /**
     * Execute Insert
     *
     * @param table
     * @param values
     * @return
     */
    fun executeInsert(table: String, values: HashMap<Any, Any>): Long {
        return this.executeInsert(table, values, null)
    }

    /**
     * Execute Insert
     *
     * @param table
     * @param values
     * @param nullColumnHack
     * @return
     */
    fun executeInsert(table: String, values: HashMap<Any, Any>, nullColumnHack: String?): Long {
        var rst: Long = -1
        var key: String? = null
        var value: String? = null

        try {
            open()

            val valueSet = ContentValues()
            val keys = values.keys.iterator()

            while(keys.hasNext()) {
                key = keys.next().toString()
                value = values[key].toString()
                valueSet.put(key, value)
            }
            rst = SDB?.insert(table, nullColumnHack, valueSet) ?: -1
        } catch(e: Exception) {
            setLog("executeInsert $e, $table, key=$key, value=$value")
        } finally {
            close()
        }
        return rst
    }

    /**
     * Execute Delete
     *
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
    fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?): Long {
        var rtn: Long = -1

        try {
            open()
            rtn = SDB?.delete(table, whereClause, whereArgs)?.toLong() ?: -1
        } catch(e: Exception) {
            setLog("executeDelete $e, $table, $whereClause, $whereArgs")
        } finally {
            close()
        }

        return rtn
    }

    fun executeSelect(sql: String, selectionArgs: Array<String>?): ArrayList<HashMap<Any, Any>> {
        val al = ArrayList<HashMap<Any, Any>>()
        var c: Cursor? = null

        try {
            open()

            c = SDB?.rawQuery(sql, selectionArgs)
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    do {
                        val result = HashMap<Any, Any>()

                        for (j in 0 until c.columnCount) {
                            result[c.getColumnName(j)] = c.getString(j) ?: ""
                        }
                        al.add(result)
                    } while (c.moveToNext())
                }
            }
            c?.close()
        } catch(e: Exception) {
            var tmp = ""
            if(selectionArgs != null && selectionArgs.isNotEmpty()) {
                for(i in selectionArgs.indices)
                    tmp += "${selectionArgs[i]}, "
            }
            setLog("executeSelect $e, $sql, $tmp")
        } finally {
            if(c != null && !c.isClosed)
                c.close()
            close()
        }
        return al
    }

    fun executeSelect(table: String, columns: Array<String>?, selection: String?, selectionArgs: Array<String>?, groupBy: String?, having: String?, orderBy: String?): ArrayList<HashMap<String, String>> {
        val al = ArrayList<HashMap<String, String>>()
        var c: Cursor? = null

        try {
            open()

            c = SDB?.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)

            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        val result = HashMap<String, String>()
                        for (j in 0 until c.columnCount) {
                            result[c.getColumnName(j)] = c.getString(j) ?: ""
                        }
                        al.add(result)
                    } while (c.moveToNext())
                }
            }
            c?.close()
        } catch(e: Exception) {
            setLog("executeSelect $e, $table, $columns, $selection, $selectionArgs, $groupBy, $having, $orderBy")
        } finally {
            if(c != null && !c.isClosed)
                c.close()
            close()
        }
        return al
    }

    /**
     * DB 파일을 SD카드로 복사한다.
     *
     * @return
     */
    fun copyDbFile(): String {
        return copyDbFile(1, PATH, DB_NAME)
    }

    fun copyDbFile(dbfileName: String): String {
        return copyDbFile(1, PATH, dbfileName)
    }

    /**
     * DB 파일을 SD카드로 복사한다.
     *
     * @param fileCnt : 1M 넘는 파일의 분할된 파일 갯수
     * @param path 경로
     * @param filename 파일명
     * @return
     */
    fun copyDbFile(fileCnt: Int, path: String, filename: String): String {
        // 첫 실행인 경우만 파일을 복사한다.
        if(!getFirstExecute())
            return "OK"

        var rtn = ""
        val exec = ""
        var am: AssetManager? = null

        // 1Mb가 넘는 파일을 assets 폴더에서 읽고 쓰는 작업은 오류가 많아서 1Mb 미만으로 분할한다.
        // 그때 asset 폴더에 있는 분할된 파일 갯수 = fileCnt
        val arrIs = arrayOfNulls<InputStream>(fileCnt)
        val arrBis = arrayOfNulls<BufferedInputStream>(fileCnt)

        var fos: FileOutputStream? = null
        var bos: BufferedOutputStream? = null

        try {
            var f = File(path)

            if (!f.exists()) f.mkdirs()

            f = File("$path$filename$exec")

            // 혹시나 DB가 있으면 지우고 0바이트의 DB파일을 새로 만든다.
            if(f.exists() && f.length() > 0) {
                val values = HashMap<Any, Any>()
                values["locale"] = Locale.getDefault().toString()

                executeUpdate("android_metadata", values, null, null)
                return "OK"
            }

            am = ctx.resources.assets

            if (arrIs.size > 1) {
                for(i in arrIs.indices) {
                    arrIs[i] = am.open("$DB_NAME${i + 1}$exec")
                    arrBis[i] = BufferedInputStream(arrIs[i])
                }
            } else {
                arrIs[0] = am.open("$DB_NAME$exec")
                arrBis[0] = BufferedInputStream(arrIs[0])
            }

            fos = FileOutputStream(f)
            bos = BufferedOutputStream(fos)

            var read = -1
            val buffer = ByteArray(1024)

            for(i in arrIs.indices) {
                while((arrBis[i]?.read(buffer, 0, 1024)?.also { read = it } ?: -1) != -1) {
                    bos.write(buffer, 0, read)
                }

                bos.flush()
            }

            setLog("DB Copy OK!")

            rtn = "OK"
        } catch(e: Exception) {
            rtn = e.toString()
        } finally {
            for(i in arrIs.indices) {
                try { arrIs[i]?.close() } catch(e: Exception) {}
                try { arrBis[i]?.close() } catch(e: Exception) {}
            }

            try { fos?.close() } catch(e: Exception) {}
            try { bos?.close() } catch(e: Exception) {}
        }
        return rtn
    }

    /**
     * 로그 기록
     * @param msg
     */
    @SuppressLint("SdCardPath")
    private fun setLog(msg: String) {
        Log.e("", "### SKY ### $msg")
    }
}