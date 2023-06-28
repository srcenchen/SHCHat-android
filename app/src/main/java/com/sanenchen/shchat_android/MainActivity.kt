package com.sanenchen.shchat_android

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.sanenchen.shchat_android.data.chatlist.Chat
import com.sanenchen.shchat_android.server.ServerConnect
import com.sanenchen.shchat_android.ui.theme.SHChat_androidTheme
import okhttp3.WebSocket
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private lateinit var webSocket: WebSocket
    private lateinit var chatListMutable: SnapshotStateList<Chat>
    var chatJson = ""

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SHChat_androidTheme {
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("SHChatting") },
                        scrollBehavior = scrollBehavior,
                    )
                }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    bottomBar = {
                        BottomBar()
                    }) {
                    Box(Modifier.padding(it)) {
                        MainUI()
                    }
                }
            }
        }
    }


    @Composable
    fun MainUI() {
        chatListMutable = remember { mutableStateListOf() }
        LazyColumn(
            reverseLayout = true
        ) {
            itemsIndexed(chatListMutable) { index, item ->
                ChatComponents(
                    item.NickName,
                    item.Content,
                    item.Date.replace("T", " ").replace("Z", ""),
                    item.Id,
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (chatListMutable.isEmpty()) {
                if (chatJson == "")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else {
                    Text(
                        text = "没有更多消息了",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomBar() {
        var contentEdit by remember { mutableStateOf("") }
        BottomAppBar(tonalElevation = 2.dp) {
            Row {
                OutlinedTextField(
                    value = contentEdit,
                    onValueChange = { contentEdit = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Button(
                    onClick = {
                        thread {
                            ServerConnect().sendMessage(contentEdit)
                            contentEdit = ""
                        }
                    }, modifier = Modifier
                        .padding(start = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Text(text = "发送")
                }
            }

        }
    }

    @Composable
    fun ChatComponents(nickName: String, chatContent: String, date: String, id: Int) {
        val dialog = remember { mutableStateOf(false) }
        if (dialog.value) {
            // 弹出一个是否确认删除的对话框
            DeleteDialog(dialog, Chat(chatContent, date, id, nickName))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            Log.i("LongPress", "LongPress")
                            dialog.value = true
                        }
                    )
                }) {
            Image(
                painter = painterResource(id = if (nickName == "Ehsan") R.drawable.ehsan else R.drawable.ye),
                contentDescription = "headImage",
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Column(Modifier.padding(start = 8.dp)) {
                Row {
                    Text(nickName, fontSize = 14.sp)
                    Text(date, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    chatContent,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    @Composable
    fun DeleteDialog(dialog: MutableState<Boolean>, item: Chat) {
        AlertDialog(onDismissRequest = { dialog.value = false }, title = { Text("删除确认") }, confirmButton = {
            OutlinedButton(onClick = {
                thread {
                    ServerConnect().deleteMessage(item.Id)
                }
                dialog.value = false
            }) {
                Text("确认")
            }
        }, dismissButton = {
            Button(onClick = {
                dialog.value = false
            }) {
                Text("取消")
            }
        }, text = {
            Text("是否确认删除来自${item.NickName}的消息，消息为\"${item.Content}\"的消息？")
        })
    }

    // 结束ws
    override fun onPause() {
        super.onPause()
        webSocket.close(1000, "bye")
    }

    // 启动ws
    override fun onResume() {
        super.onResume()
        val chatListHandler = Handler.Callback { message ->
            chatJson = message.data.getString("chatJson", "{\"code\":0,\"data\":{\"chatList\":[]},\"msg\":\"\"}")
            Log.i("Websocket", chatJson)
            parseChatJson()
            return@Callback true
        }
        webSocket = ServerConnect().websocket(chatListHandler)
    }

    // 解析chatJson
    private fun parseChatJson() {
        val temp = arrayListOf<Chat>()
        val chatList = JSONObject(chatJson).getJSONObject("data").getJSONArray("chatList")
        for (i in 0 until chatList.length()) {
            val item = chatList.getJSONObject(i)
            temp.add(
                Chat(item.getString("Content"), item.getString("Date"), item.getInt("Id"), item.getString("NickName"))
            )
        }
        temp.reverse()
        if (chatListMutable.size != 0)
            chatListMutable.clear()
        temp.forEach {
            chatListMutable.add(it)
        }
    }

}