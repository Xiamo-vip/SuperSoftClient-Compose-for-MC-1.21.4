package com.xiamo.utils.misc

import com.xiamo.gui.ComposeScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URLEncoder


object NeteaseCloudApi {
    val host = "127.0.0.1"
    val port = 3000
    val url = "http://$host:$port"

    val client = OkHttpClient()




    fun getUrl(id : Long): String {
        val body = FormBody.Builder()


        val request = Request.Builder()
            .url(url + "/song/url?id=${id.toString()}")
            .header("Cookie", "MUSIC_U=00BCE1A962BD832C89353973D024F67C5ACD2400E18968A01F9F38494529C4EC51410353E39FD45BE8DD3F9C9F069D5DC18FD712E5DA4CFEE2975606C5FEEA7B8166B421C86F5ED6BEB753FEF5A057A0195B520E1E65ED2FA3428E02E158D3067B09A4EC23111FD6BF77DEFE4B97EB8BF1635DF4ADCE8E39CBFFF1D68075AC051452055C77C846FCAB7D002C6FD6594CE9FC4DD344FAF72685FF29AC5940C450BBEC59ECCA0089A2DA6AD09A5C77FA0432EAB488A370625FC2794CD4355E72C1AC33D2325D40344F2F22EFC1506C2A21A4D3076F30D001A7EF9753A26B052652A839DCB3A8089474D76C68AABA0D2CFDBEC6445BE7678377B768B304C238DD894F19DC44B632EEF12755042E1E03C10F207D7AE1FA1452D27E0E2AFD13B742EE6595219464455B134CF57AB43F98A67AB5915E99D9AC675791303715510E5E683F4E34666BC47382D40A584675DC615C83E9216734147E544CF6BC36E0BA95D9CB")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if(response.isSuccessful){
                val json = Json.parseToJsonElement(response.body.string())

                return json.jsonObject["data"]?.jsonArray[0]?.jsonObject["url"]!!.jsonPrimitive.content
            }
        }



        return ""
    }

    fun search(name : String): String? {
        val body = FormBody.Builder()
        val encode = URLEncoder.encode(name, "utf-8")
        val request = Request.Builder()
            .url(url + "/cloudsearch?keywords=$encode")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if(response.isSuccessful){
                    return response.body.string()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }



        return null
    }


}


@Serializable
data class Song(val image: String ,val name: String,val singer: String,val id : Long) {


}