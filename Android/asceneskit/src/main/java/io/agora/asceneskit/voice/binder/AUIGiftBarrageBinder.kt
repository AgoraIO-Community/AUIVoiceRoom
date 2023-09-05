package io.agora.asceneskit.voice.binder

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.ui.gift.AUIGiftInfo
import io.agora.auikit.ui.gift.AUIGiftTabInfo
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView
import io.agora.auikit.utils.ThreadManager
import org.libpag.PAGFile
import org.libpag.PAGView
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

class AUIGiftBarrageBinder constructor(
    private val activity: FragmentActivity?,
    private val giftView: IAUIGiftBarrageView,
    private val data:List<AUIGiftTabEntity>,
    private val giftService: IAUIGiftsService,
    private val chatManager: AUIChatManager
):IAUIBindable,IAUIGiftsService.AUIGiftRespDelegate {

    private val TAG = "AUIGift_LOG"
    private var roomContext = AUIRoomContext.shared()
    private var mPAGView: PAGView? = null

    init {
        downloadEffectResource(data)
    }

    override fun bind() {
        giftService.bindRespDelegate(this)
    }

    override fun unBind() {
        giftService.unbindRespDelegate(this)
    }

    fun showBottomGiftDialog(){
        activity?.let {
            val dialog = AUiGiftListView(it, data.map {tabEntity ->
                AUIGiftTabInfo(
                    tabEntity.tabId, tabEntity.displayName ?: "", tabEntity.gifts.map { entity ->
                        AUIGiftInfo(
                            entity?.giftId ?: "", entity?.giftName ?: "",
                            entity?.giftIcon ?: "", entity?.giftCount ?: 0,
                            entity?.giftPrice ?: "", entity?.giftEffect ?: "",
                            entity?.effectMD5?:"", entity?.sendUser?.userId ?: "",
                            entity?.sendUser?.userName?: "", entity?.sendUser?.userAvatar ?: "")
                    }
                )
            })
            dialog.setDialogActionListener(object : AUiGiftListView.ActionListener{
                override fun onGiftSend(bean: AUIGiftInfo?) {
                    bean?.let { it1 ->
                        val giftEntity = AUIGiftEntity(
                            it1.giftId,
                            it1.giftName,
                            it1.giftPrice,
                            it1.giftIcon,
                            it1.giftEffect,
                            it1.giftEffectMD5,
                            roomContext?.currentUserInfo,
                            false,
                            1
                        )
                        giftService.sendGift(giftEntity) { error ->
                            if (error == null) {
                                ThreadManager.getInstance().runOnMainThread{
                                    effectAnimation(giftEntity)
                                    val path = filePath(giftEntity.effectMD5 ?: "")
                                    if (path != null && path.isNotEmpty() && File(path).exists()) {
                                        dialog.dismiss()
                                    }
                                    chatManager.addGiftList(giftEntity)
                                    giftView.refresh(chatManager.getGiftList().map {entity ->
                                        AUIGiftInfo(
                                            entity.giftId ?: "", entity.giftName?: "", entity.giftIcon?: "",
                                            entity.giftCount, entity.giftPrice?: "", entity.giftEffect?: "",
                                            entity.effectMD5?: "", entity.sendUser?.userId?: "",
                                            entity?.sendUser?.userName ?: "", entity?.sendUser?.userAvatar ?: ""
                                        )
                                    })
                                }
                            } else {
                                Log.e("AUIGiftViewBinder", "sendGift error ${error.code} ${error.message}")
                            }
                        }
                    }
                }
            })
            dialog.show(it.supportFragmentManager, "gift_dialog")
        }

    }
    private fun effectAnimation(gift: AUIGiftEntity) {
        val path = filePath(gift.effectMD5 ?: "")
        if (path == null || path.isEmpty() || !File(path).exists()) {
            return
        }
        if (mPAGView == null) {
            val pagView = PAGView(activity)
            pagView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            pagView.elevation = 3F
            val contentView = activity?.window?.decorView as ViewGroup
            contentView.addView(pagView)
            pagView.addListener(object: PAGView.PAGViewListener {
                override fun onAnimationStart(p0: PAGView?) {
                    pagView.visibility = View.VISIBLE
                    Log.d(TAG, "gift pag: play start")
                }
                override fun onAnimationEnd(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    Log.d(TAG, "gift pag: play end")
                }
                override fun onAnimationCancel(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    Log.d(TAG, "gift pag: play cancel")
                }
                override fun onAnimationRepeat(p0: PAGView?) {}
                override fun onAnimationUpdate(p0: PAGView?) {}
            })
            mPAGView = pagView
        }

        val file = PAGFile.Load(path)
        mPAGView?.bringToFront()
        mPAGView?.setScaleMode(3)
        mPAGView?.composition = file
        mPAGView?.setRepeatCount(1)
        mPAGView?.play()
    }

    private fun downloadEffectResource(tabs: List<AUIGiftTabEntity>) {
        tabs.forEach { tab ->
            tab.gifts.forEach { gift ->
                Log.d(TAG, "for each gift: $gift")
                val url = gift?.giftEffect
                val savePath = filePath(gift?.effectMD5 ?: "")
                savePath?.let {
                    val file = File(it)
                    if (!file.exists() && url != null){
                        Log.d(TAG, "down load resource $url to path $savePath")
                        val task = NetworkTask(url, savePath)
                        task.execute()
                    }
                }
            }
        }
    }

    private fun filePath(fileName: String): String? {
        return if (fileName.isEmpty()) {
            null
        } else {
            val dir = File(activity?.cacheDir,"giftEffects")
            if (!dir.exists()){
                dir.mkdirs()
            }
            val file = dir.resolve("$fileName.pag")
            return file.absoluteFile.toString()
        }
    }

    private fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private class NetworkTask constructor(
        val url: String,
        val path: String,
    ) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val inputStream = URL(url).openStream()
            val outputStream = FileOutputStream(File(path))
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return ""
        }
    }

    override fun onReceiveGiftMsg(giftEntity:AUIGiftEntity?) {
        ThreadManager.getInstance().runOnMainThread{
            giftEntity?.let { effectAnimation(it) }
            giftView.refresh(chatManager.getGiftList().map {entity ->
                AUIGiftInfo(
                    entity.giftId ?: "", entity.giftName?: "", entity.giftIcon?: "",
                    entity.giftCount, entity.giftPrice?: "", entity.giftEffect?: "",
                    entity.effectMD5?: "", entity.sendUser?.userId?: "",
                    entity?.sendUser?.userName ?: "", entity?.sendUser?.userAvatar ?: ""
                )
            })
        }
    }

}