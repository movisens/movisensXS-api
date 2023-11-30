package com.movisens.xs.api.test

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayway.awaitility.Awaitility
import com.movisens.xs.api.XSApi
import com.movisens.xs.api.XSService
import com.movisens.xs.api.exceptions.AuthorizationException
import com.movisens.xs.api.exceptions.MovisensXSException
import com.movisens.xs.api.models.*
import kotlinx.coroutines.test.runTest
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

/*
 * This Java source file was auto generated by running 'gradle init --type java-library'
 * by 'Juergen' at '22.06.14 22:50' with Gradle 1.11
 *
 * @author Juergen, @date 22.06.14 22:50
 */
class ApiTest {
    private val service =
        XSApi.Builder(API_KEY)
            .setServer(SERVER_URL)
            .setLogLevel(HttpLoggingInterceptor.Level.BASIC)
            .build()
            .create(XSService::class.java)
    private val asyncProbands: MutableList<Proband> = mutableListOf()
    private val asyncResults: MutableList<Result> = mutableListOf()

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetMessages() {
        val call = service.getMessages(STUDY_ID, PARTICIPANT_ID)
        val messages = call.execute().body()!!
        Assert.assertEquals(
            "getMessages should return list with first message text is 'Hallo'", "Hallo",
            messages[0].message
        )
    }

// TODO: Deactivated for now, should be activated with own instance
//
//    @Test
//    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
//    fun testSendMessage() {
//        var call = service.getMessages(STUDY_ID, 2)
//        val nrOfMessages = call.execute().body()!!.size
//        val sendMessageCall = service.sendMessage(STUDY_ID, 2, USER_EMAIL, "Unit Test")
//        val message = sendMessageCall.execute().body()
//        call = service.getMessages(STUDY_ID, 2)
//        val nrOfMessagesAfterSending = call.execute().body()!!.size
//        Assert.assertEquals(
//            "getMessages should return one more message after sending", 1, (
//                    nrOfMessagesAfterSending - nrOfMessages).toLong()
//        )
//        Assert.assertEquals(
//            "sendMessage should return one message with the text 'Unit Test'", "Unit Test",
//            message!!.message
//        )
//    }
//
//    @Test
//    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
//    fun testPushNotification() {
//        val service =
//            XSApi.Builder("8qo65wghj7t09of920tcfo01zbub7olkjsjf2tzl")
//                .setServer("http://localhost:9000")
//                .setLogLevel(HttpLoggingInterceptor.Level.BASIC)
//                .build()
//                .create(XSService::class.java)
//        val response = service.sendPushNotification(12, 2).execute().body()
//        println(response)
//    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetStudy() {
        val study = service.getStudy(STUDY_ID).execute().body()
        Assert.assertEquals("getStudy should return study with id STUDY_ID", STUDY_ID.toLong(), study!!.id.toLong())
        Assert.assertEquals(
            "getStudy should return study which name is 'movisensXS API for Java",
            "movisensXS API for Java",
            study.name
        )
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetProbandsSynchronous() {
        val probands = service.getProbands(STUDY_ID).execute().body()!!
        Assert.assertEquals("getProbands should return 3 result", 7, probands.size.toLong())
        Assert.assertEquals(
            "getProbands user 3 should have status 'unknown'",
            Proband.ProbandStatus.UNKNOWN,
            probands[2].status
        )
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetProbandsCoRoutines() = runTest {
        val probands = service.getProbands(STUDY_ID).await()
        Assert.assertEquals("getProbands should return 3 result", 7, probands.size.toLong())
        Assert.assertEquals(
            "getProbands user 3 should have status 'unknown'",
            Proband.ProbandStatus.UNKNOWN,
            probands[2].status
        )
    }

    @Test
    @Throws(MovisensXSException::class)
    fun testGetProbandsAsync() {
        val call = service.getProbands(STUDY_ID)
        call.enqueue(object : Callback<List<Proband>> {
            override fun onResponse(call: Call<List<Proband>>, response: Response<List<Proband>>) {
                asyncProbands.addAll(response.body()!!.toList())
            }

            override fun onFailure(call: Call<List<Proband>?>, t: Throwable) {
                Assert.fail("Error receiving probands: " + t.message)
            }
        })
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until<Any> { asyncProbands.size > 0 }
        Assert.assertEquals("getProbands should return 3 result", 7, asyncProbands.size.toLong())
        Assert.assertEquals(
            "getProbands user 3 should have status 'unknown'",
            Proband.ProbandStatus.UNKNOWN,
            asyncProbands[2].status
        )
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetResults() {
        val results = service.getResults(STUDY_ID).execute().body()!!
        Assert.assertEquals("getResults should return 2 results", 2, results.size.toLong())
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetResultsPerParticipant() {
        val results = service.getResults(STUDY_ID).execute().body()!!
        Assert.assertEquals("getResults should return 2 results", 2, results.size.toLong())
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetResultsAsJson() {
        val jsonResults = service.getResultsAsJson(STUDY_ID).execute().body()
        val gson = Gson()
        val collectionType = object : TypeToken<List<MyResult?>?>() {}.type
        val results = gson.fromJson<List<MyResult>>(jsonResults, collectionType)
        Assert.assertEquals("getResults should return 2 results", 2, results.size.toLong())
        Assert.assertEquals("getResults first result should have others_1 set to 0", 0, results[0].others_1.toLong())
        Assert.assertEquals("getResults first result should have others_2 set to 1", 1, results[0].others_2.toLong())
        Assert.assertEquals(
            "getResults second result should have happy_sad set to 25",
            69,
            results[1].happy_sad.toLong()
        )
    }

    @Test
    @Throws(MovisensXSException::class)
    fun testGetResultsAsync() {
        val call = service.getResults(STUDY_ID)
        call.enqueue(object : Callback<List<Result>> {
            override fun onResponse(call: Call<List<Result>>, response: Response<List<Result>>) {
                asyncResults.addAll(response.body()!!.toList())
            }

            override fun onFailure(call: Call<List<Result>>, t: Throwable) {
                Assert.fail("Error receiving results: " + t.message)
            }
        })
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until<Any> { asyncResults.size > 0 }
        Assert.assertEquals("getResults should return 2 results", 2, asyncResults.size.toLong())
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetResultsAsXlsx() {
        val xlsxStream = service.getResultsAsXLSX(STUDY_ID, PARTICIPANT_ID).execute().body()!!
            .byteStream()
        val targetFile = folder.newFile("participant" + PARTICIPANT_ID + "Results.xlsx")
        Assert.assertEquals(0L, targetFile.length())
        FileUtils.copyInputStreamToFile(xlsxStream, targetFile)
        xlsxStream.close()
        Assert.assertTrue(targetFile.length() > 0L)
        Assert.assertEquals("XLSX file should be valid zip ;-)", zipIsValid(targetFile), true)
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetAllResultsAsXlsx() {
        val xlsxStream = service.getResultsAsXLSX(STUDY_ID, PARTICIPANT_ID).execute().body()!!
            .byteStream()
        val targetFile = folder.newFile("allResults.xlsx")
        Assert.assertEquals(0L, targetFile.length())
        FileUtils.copyInputStreamToFile(xlsxStream, targetFile)
        xlsxStream.close()
        Assert.assertTrue(targetFile.length() > 0L)
        Assert.assertEquals("XLSX file should be valid zip ;-)", zipIsValid(targetFile), true)
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testGetMobileSensingResultsAsUnisens() {
        val unisensZipStream = service.getMobileSensingAsUnisensZip(STUDY_ID, PARTICIPANT_ID).execute().body()!!
            .byteStream()
        val targetFile = folder.newFile("test.unisenszip")
        Assert.assertEquals(0L, targetFile.length())
        FileUtils.copyInputStreamToFile(unisensZipStream, targetFile)
        unisensZipStream.close()
        Assert.assertTrue(targetFile.length() > 0L)
        Assert.assertEquals("Unisens file should be valid", zipIsValid(targetFile), true)
    }

    @Test
    @Throws(AuthorizationException::class, IOException::class, MovisensXSException::class)
    fun testSendMonitoring() {
        val monitoringCompliance1 = MonitoringCompliance(
            1, "2019-08-13", "Completed",
            "<h2>No participation in the study</h2>",
            true, MonitoringCompliance.Category.FORMS, 30
        )
        val monitoringCompliance2 = MonitoringCompliance(
            7, "2019-08-09",
            "Smartphone ON",
            "<h2>What sup</h2>",
            true, MonitoringCompliance.Category.MOBILE_SENSING, 86
        )
        val monitoringCompliance3 = MonitoringCompliance(
            5, "2019-07-29",
            "Smartphone ON",
            "<h2>Smartphone is ON</h2>",
            true, MonitoringCompliance.Category.MOBILE_SENSING, 85
        )
        val monitoringAlert1 = MonitoringAlert(
            2, "2019-08-13",
            "Stress episode",
            "<h2>There has been a stress episode detected</h2>", true, false
        )
        val monitoringRequest = MonitoringRequest()
        monitoringRequest.add(monitoringCompliance1)
        monitoringRequest.add(monitoringCompliance2)
        monitoringRequest.add(monitoringCompliance3)
        monitoringRequest.add(monitoringAlert1)
        val response: Response<*> = service.sendMonitoring(STUDY_ID, monitoringRequest).execute()
        Assert.assertEquals(201, response.code().toLong())
    }

    companion object {
        private const val SERVER_URL = "https://xs.movisens.com"
        private const val API_KEY = "0qlvej2aosjwv7mimvebd7dsz4won2kj4zun4x4o"
        private const val USER_EMAIL = "Juergen.Stumpp+movisensXSContinuousIntegration@gmail.com"
        private const val STUDY_ID = 5180
        private const val PARTICIPANT_ID = 1
        private fun zipIsValid(file: File): Boolean {
            var zipfile: ZipFile? = null
            return try {
                zipfile = ZipFile(file)
                true
            } catch (e: IOException) {
                false
            } finally {
                try {
                    if (zipfile != null) {
                        zipfile.close()
                        zipfile = null
                    }
                } catch (e: IOException) {
                }
            }
        }
    }
}