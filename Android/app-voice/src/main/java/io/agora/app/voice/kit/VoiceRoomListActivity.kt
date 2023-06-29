package io.agora.app.voice.kit

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.app.voice.BuildConfig
import io.agora.app.voice.databinding.VoiceRoomListActivityBinding
import io.agora.app.voice.databinding.VoiceRoomListItemBinding
import io.agora.asceneskit.voice.VoiceRoomActivity
import io.agora.asceneskit.voice.VoiceRoomUikit
import io.agora.auikit.model.*
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.BindingViewHolder
import java.util.*

class VoiceRoomListActivity: AppCompatActivity() {
    private val mUserId = Random().nextInt(99999999).toString()
    private val mViewBinding by lazy { VoiceRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private var mList = listOf<AUIRoomInfo>()
    private val listAdapter by lazy { RoomListAdapter() }

    companion object {
        private var ThemeId = io.agora.asceneskit.R.style.Theme_VoiceRoom
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initService()
    }

    override fun onDestroy() {
        super.onDestroy()
        VoiceRoomUikit.release()
    }

    private fun initView() {
        if (ThemeId != View.NO_ID) {
            setTheme(ThemeId)
        }
        setContentView(mViewBinding.root)

        val out = TypedValue()
        if (theme.resolveAttribute(android.R.attr.windowBackground, out, true)) {
            window.setBackgroundDrawableResource(out.resourceId)
        }

        mViewBinding.btnCreateRoom.setOnClickListener {
            AUIAlertDialog(this@VoiceRoomListActivity).apply {
                setTitle("房间主题")
                setInput("房间主题", "", true)
                setPositiveButton("一起嗨") {
                    dismiss()
                    createRoom(inputText)
                }
                show()
            }
        }

        mViewBinding.btnSwitchTheme.setOnClickListener {
            ThemeId = if (ThemeId == io.agora.asceneskit.R.style.Theme_VoiceRoom) {
                io.agora.asceneskit.R.style.Theme_VoiceRoom_Voice
            } else {
                io.agora.asceneskit.R.style.Theme_VoiceRoom
            }
            theme.setTo(resources.newTheme())
            initView()
        }

        mViewBinding.btnConfig.setOnClickListener {

        }

        mViewBinding.rvList.adapter = listAdapter

        mViewBinding.swipeRefresh.setOnRefreshListener {
            refreshRoomList()
        }

    }


    private fun initService() {
        // Create Common Config
        val config = AUICommonConfig()
        config.context = application
        config.appId = BuildConfig.AGORA_APP_ID
        config.userId = mUserId
        config.userName = "user_$mUserId"
        config.userAvatar = randomAvatar()
        // init AUiKit
        VoiceRoomUikit.init(
            config = config, // must
            rtmClient = null, // option
            rtcEngineEx = null, // option
            ktvApi = null,// option
            serverHost = BuildConfig.SERVER_HOST
        )
        fetchRoomList()
    }

    private fun createRoom(roomName: String) {
        val createRoomInfo = AUICreateRoomInfo()
        Log.e("apex","createRoom ${MicSeatType.SixTag.value} ")
        createRoomInfo.roomName = roomName
        createRoomInfo.micSeatCount = 8
        createRoomInfo.micSeatStyle= MicSeatType.EightTag.value.toString()
        VoiceRoomUikit.createRoom(
            createRoomInfo,
            success = { roomInfo ->
                Log.e("apex", "createRoom success ${roomInfo.roomId}  ${roomInfo.roomOwner?.userId}")
                gotoRoomDetailPage(VoiceRoomUikit.LaunchType.CREATE,roomInfo)
            },
            failure = {
                Toast.makeText(this@VoiceRoomListActivity, "Create room failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun fetchRoomList(){
        var lastCreateTime: Long? = null
        if (!mViewBinding.swipeRefresh.isRefreshing) {
            mList.lastOrNull()?.let {
                lastCreateTime = it.createTime
            }
        }
        VoiceRoomUikit.getRoomList(lastCreateTime,10,
                success = { roomList ->
                    if (roomList.size < 10) {
                        listAdapter.loadingMoreState = LoadingMoreState.NoMoreData
                    } else {
                        listAdapter.loadingMoreState = LoadingMoreState.Normal
                    }
                    mList = if (mViewBinding.swipeRefresh.isRefreshing) { // 下拉刷新则重新设置数据
                        roomList
                    } else {
                        val temp = mutableListOf<AUIRoomInfo>()
                        temp.addAll(mList)
                        temp.addAll(roomList)
                        temp
                    }
                    runOnUiThread {
                        mViewBinding.swipeRefresh.isRefreshing = false
                        listAdapter.submitList(mList)
                    }
        },
            failure = {
                Toast.makeText(this@VoiceRoomListActivity, "get roomList failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun refreshRoomList() {
        mViewBinding.swipeRefresh.isRefreshing = true
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun loadMore() {
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun gotoRoomDetailPage(lunchType: VoiceRoomUikit.LaunchType, roomInfo: AUIRoomInfo) {
        val config = AUIRoomConfig(roomInfo.roomId)
        config.themeId = ThemeId
        VoiceRoomActivity.launch(lunchType,this, roomInfo, ThemeId)
    }

    private fun randomAvatar(): String {
        val randomValue = Random().nextInt(8) + 1
        return "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_${randomValue}.png"
    }

    enum class LoadingMoreState {
        Normal,
        Loading,
        NoMoreData,
    }

    inner class RoomListAdapter :
        ListAdapter<AUIRoomInfo, BindingViewHolder<VoiceRoomListItemBinding>>(object :
            DiffUtil.ItemCallback<AUIRoomInfo>() {

            override fun areItemsTheSame(oldItem: AUIRoomInfo, newItem: AUIRoomInfo) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(
                oldItem: AUIRoomInfo,
                newItem: AUIRoomInfo
            ) = false
        }) {

        var loadingMoreState: LoadingMoreState = LoadingMoreState.NoMoreData

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) =
            BindingViewHolder(VoiceRoomListItemBinding.inflate(LayoutInflater.from(parent.context)))

        override fun onBindViewHolder(
            holder: BindingViewHolder<VoiceRoomListItemBinding>,
            position: Int
        ) {
            val item = getItem(position)
            holder.binding.tvRoomName.text = item.roomName
            holder.binding.tvRoomOwner.text = item.roomOwner?.userName ?: "unKnowUser"
            holder.binding.root.setOnClickListener {
                this@VoiceRoomListActivity.gotoRoomDetailPage(VoiceRoomUikit.LaunchType.JOIN,item) }

            Glide.with(holder.binding.ivAvatar)
                .load(item.roomOwner?.userAvatar)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.binding.ivAvatar)

            if (loadingMoreState == LoadingMoreState.Normal && itemCount > 0 && position == itemCount - 1) {
                this@VoiceRoomListActivity.loadMore()
            }
        }
    }


}