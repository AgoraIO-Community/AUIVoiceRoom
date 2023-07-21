package io.agora.app.voice.kit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.app.voice.R
import io.agora.app.voice.databinding.VoiceRoomSettingActivityBinding

class VoiceRoomSettingActivity : AppCompatActivity() {

    private val mViewBinding by lazy { VoiceRoomSettingActivityBinding.inflate(LayoutInflater.from(this)) }
    private var isThemeChange = false
    private var isSeatsChange = false

    private var mThemeSelectorPosition = 0
    private var mSeatsPosition = 2

    companion object {
        private var ThemeId = io.agora.asceneskit.R.style.Theme_VoiceRoom
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extra = intent?.getIntExtra(
            "CurrentThemeId",
            io.agora.asceneskit.R.style.Theme_VoiceRoom
        )
        extra?.let {
            ThemeId = it
            if (it != io.agora.asceneskit.R.style.Theme_VoiceRoom){
                isThemeChange = true
                mThemeSelectorPosition = 1
            }
        }
        initView()
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

        mViewBinding.rvSeats.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false)

        mViewBinding.rvTheme.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false)

        // 给adapter传入图片数据和选中回调事件
        val seatsAdapter = ImageAdapter(getSeatsImages()) { position ->
            isSeatsChange = true
            mSeatsPosition = position
        }

        // 给adapter传入图片数据和选中回调事件
        val themeAdapter = ImageAdapter(getThemImages()) { position ->
            isThemeChange = true
            mThemeSelectorPosition = position
            ThemeId = if (position == 1){
                io.agora.asceneskit.R.style.Theme_VoiceRoom_Voice
            }else{
                io.agora.asceneskit.R.style.Theme_VoiceRoom
            }
            theme.setTo(resources.newTheme())
            initView()
        }

        if (!isThemeChange){
            themeAdapter.setSelectedPosition(0)
            seatsAdapter.setSelectedPosition(2)
        }else{
            themeAdapter.setSelectedPosition(mThemeSelectorPosition)
            if (mSeatsPosition != 2) {
                seatsAdapter.setSelectedPosition(mSeatsPosition)
            }else{
                seatsAdapter.setSelectedPosition(2)
            }
        }

        mViewBinding.btnComplete.setOnClickListener{
            val intent = Intent()
            intent.putExtra("voiceThemeId", ThemeId)
            intent.putExtra("voiceSeatsStyle", getSeatsStyle(mSeatsPosition))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


        mViewBinding.rvSeats.adapter = seatsAdapter
        mViewBinding.rvTheme.adapter = themeAdapter
    }

    private fun getSeatsImages(): List<Int> {
        return listOf(R.drawable.voice_icon_dot_1, R.drawable.voice_icon_dot_6,
            R.drawable.voice_icon_dot_8, R.drawable.voice_icon_dot_9)
    }

    private fun getThemImages(): List<Int> {
        return listOf(R.drawable.voice_icon_sun, R.drawable.voice_icon_moon)
    }

    private fun getSeatsMap():MutableMap<Int,Int>{
        val map = mutableMapOf<Int, Int>()
        map[0] = 1
        map[1] = 6
        map[2] = 8
        map[3] = 9
        return map
    }

    private fun getSeatsStyle(int: Int): Int? {
        return getSeatsMap()[int]
    }

    private inner class ImageAdapter(
        private val images: List<Int>,
        private val onImageSelected: (position: Int) -> Unit)
        : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
        private var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.voice_room_setting_tag_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder,position: Int) {
            val index = position
            holder.bind(images[index], index == selectedPosition)
            holder.itemView.setOnClickListener {
                val previouslySelectedPosition = selectedPosition
                selectedPosition = index
                notifyItemChanged(previouslySelectedPosition)
                notifyItemChanged(selectedPosition)
                onImageSelected(index)
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }

        fun setSelectedPosition(position: Int){
            this.selectedPosition = position
            notifyDataSetChanged()
        }

        private inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.img_tag)

            fun bind(imageResId: Int, isSelected: Boolean) {
                imageView.setImageResource(imageResId)
                if (isSelected){
                    val drawable = resources.getDrawable(R.drawable.voice_setting_tag_select_bg)
                    imageView.background = drawable
                }else{
                    val drawable = resources.getDrawable(R.drawable.voice_setting_tag_bg)
                    imageView.background = drawable
                }
            }
        }
    }

}