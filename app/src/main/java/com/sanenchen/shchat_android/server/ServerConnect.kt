package com.sanenchen.shchat_android.server

import android.os.Handler
import android.os.Message
import android.util.Log
import com.sanenchen.shchat_android.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

class ServerConnect {
    private val hostname = "shchat.luckysan.top"
    private val baseURL = "http://$hostname/api"

    fun websocket(chatListHandler: Handler.Callback): WebSocket {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url("ws://$hostname:8070/api/message/chat-list-w-s").build()
        val ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                // Log.i("Websocket", text)
                val message = Message.obtain()
                message.data.putString("chatJson", text)
                chatListHandler.handleMessage(message)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.i("Websocket","嘟嘟，关闭咯")
            }
        })
        return ws
    }

    fun sendMessage(message:String) {
        val url = "$baseURL/message/send-chat"
        val client = OkHttpClient()
        // 发送 PUT 请求 内容是 JSON
        val jsonObject = JSONObject()
        jsonObject.put("Message", message)
        jsonObject.put("NickName", "业")
        val requestBody = jsonObject.toString()
        val request = Request.Builder().put(requestBody.toRequestBody()).url(url).build()
        client.newCall(request).execute().body!!.string()
    }

    fun deleteMessage(chatId:Int) {
        val url = "$baseURL/message/remove-chat?ChatId=$chatId"
        val client = OkHttpClient()
        val request = Request.Builder().delete().url(url).build()
        client.newCall(request).execute().body!!.string()
    }

    fun getChatList(): String {
        val url = "$baseURL/message/get-chat-list"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }
}