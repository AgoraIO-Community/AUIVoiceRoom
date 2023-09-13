package io.agora.app.voice.kit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.app.voice.R
import io.agora.app.voice.databinding.VoiceRoomListActivityBinding
import io.agora.app.voice.databinding.VoiceRoomListItemBinding
import io.agora.asceneskit.voice.AUIVoiceRoomUikit
import io.agora.asceneskit.voice.VoiceRoomActivity
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.ui.basic.AUISpaceItemDecoration
import io.agora.auikit.ui.micseats.MicSeatType
import io.agora.auikit.utils.BindingViewHolder
import java.util.Random

class VoiceRoomListActivity: AppCompatActivity() {

    private val mViewBinding by lazy { VoiceRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private var mList = listOf<AUIRoomInfo>()
    private val listAdapter by lazy { RoomListAdapter() }
    private var seatStyle = MicSeatType.EightTag.value.toString()
    private var seatCount = 8

    private val launcher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            registerForActivity(it)
        }

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
        AUIVoiceRoomUikit.release()
    }

    private fun initView() {
        if (ThemeId != View.NO_ID) {
            setTheme(ThemeId)
        }
        setContentView(mViewBinding.root)

        //val out = TypedValue()
        //if (theme.resolveAttribute(android.R.attr.windowBackground, out, true)) {
        //    window.setBackgroundDrawableResource(out.resourceId)
        //}
        val isDarkTheme = ThemeId != io.agora.asceneskit.R.style.Theme_VoiceRoom
        var systemUiVisibility: Int = window.decorView.systemUiVisibility
        if (isDarkTheme) {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }else{
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
        window.setBackgroundDrawable(ColorDrawable(if(isDarkTheme) Color.parseColor("#171A1C") else Color.parseColor("#F9FAFA")))
        mViewBinding.tvEmptyList.setCompoundDrawables(
            null,
            getDrawable(
                if (isDarkTheme) R.drawable.voice_icon_empty_dark else R.drawable.voice_icon_empty_light
            ).apply {
                this?.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            },
            null,
            null
        )

        mViewBinding.btnCreateRoom.setOnClickListener {
            AUIAlertDialog(this@VoiceRoomListActivity).apply {
                setTitle("房间主题")
                setInput("房间主题", "room${Random().nextInt(100000)}", true)
                setPositiveButton("一起嗨") {
                    dismiss()
                    createRoom(inputText)
                }
                show()
            }
        }

        mViewBinding.btnConfig.setOnClickListener {
            val intent = Intent(this, VoiceRoomSettingActivity::class.java)
            intent.putExtra("CurrentThemeId", ThemeId)
            launcher.launch(intent)
        }

        mViewBinding.rvList.addItemDecoration(
            AUISpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.voice_room_list_item_space_h),
                resources.getDimensionPixelSize(R.dimen.voice_room_list_item_space_v)
            )
        )
        mViewBinding.rvList.adapter = listAdapter

        mViewBinding.swipeRefresh.setOnRefreshListener {
            refreshRoomList()
        }

    }


    private fun initService() {
        // init AUiKit
        AUIVoiceRoomUikit.init(
            rtmClient = null, // option
            rtcEngineEx = null, // option
            ktvApi = null// option
        )
        fetchRoomList()
    }

    private fun createRoom(roomName: String) {
        val createRoomInfo = AUICreateRoomInfo()
        createRoomInfo.roomName = roomName
        createRoomInfo.micSeatCount = seatCount
        createRoomInfo.micSeatStyle = seatStyle
        AUIVoiceRoomUikit.createRoom(
            createRoomInfo,
            success = { roomInfo ->
                Log.e("apex", "createRoom success ${roomInfo.roomId}  ${roomInfo.roomOwner?.userId}")
                gotoRoomDetailPage(roomInfo)
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
        AUIVoiceRoomUikit.getRoomList(lastCreateTime,10,
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
                        mViewBinding.tvEmptyList.visibility = if (mList.isEmpty()) View.VISIBLE else View.GONE
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

    private fun gotoRoomDetailPage(roomInfo: AUIRoomInfo) {
        VoiceRoomActivity.launch(this, roomInfo, ThemeId)
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
                this@VoiceRoomListActivity.gotoRoomDetailPage(item) }

            Glide.with(holder.binding.ivAvatar)
                .load(item.roomOwner?.userAvatar)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.binding.ivAvatar)

            if (loadingMoreState == LoadingMoreState.Normal && itemCount > 0 && position == itemCount - 1) {
                this@VoiceRoomListActivity.loadMore()
            }
        }
    }

    private fun registerForActivity(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data.let { intent->
                val voiceThemeId = intent?.getIntExtra(
                    "voiceThemeId",
                    io.agora.asceneskit.R.style.Theme_VoiceRoom
                )
                voiceThemeId?.let {
                    ThemeId = it
                    theme.setTo(resources.newTheme())
                    initView()
                }
                when (intent?.getIntExtra("voiceSeatsStyle", 8)) {
                    1 -> {
                        seatCount = 1
                        seatStyle = MicSeatType.OneTag.value.toString()
                    }
                    6 -> {
                        seatCount = 6
                        seatStyle = MicSeatType.SixTag.value.toString()
                    }
                    9 -> {
                        seatCount = 9
                        seatStyle = MicSeatType.NineTag.value.toString()
                    }
                    else -> {
                        seatCount = 8
                        seatStyle = MicSeatType.EightTag.value.toString()
                    }
                }
                Log.e("apex","registerForActivity $seatCount $seatStyle")
            }
        }
    }

}