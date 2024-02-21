//
//  AUIRoomGiftBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKitCore
import libpag
import Alamofire


public class AUIRoomGiftBinder: NSObject {
    
    private var effectView: AUIGiftEffectView?
    
    private weak var send: IAUIRoomGiftDialog?
    
    private weak var receive: IAUIGiftBarrageView?
    
    private var queue: AnimationQueue = AnimationQueue()
    
    private var animationPaths = [String]()
    
    private weak var giftDelegate: AUIGiftsManagerServiceDelegate? {
        didSet {
            giftDelegate?.unbindRespDelegate(delegate: self)
            giftDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(send: IAUIRoomGiftDialog, receive: IAUIGiftBarrageView, giftService: AUIGiftsManagerServiceDelegate) {
        self.send = send
        self.receive = receive
        self.send?.addActionHandler(actionHandler: self)
        self.giftDelegate = giftService
        self.giftDelegate?.giftsFromService(roomId: giftService.getChannelName(), completion: { [weak self] tabs, error in
            if error == nil {
                self?.refreshGifts(tabs: tabs)
                self?.downloadEffectResource(tabs: tabs)
            }
        })
        
        let pag = AUIGiftEffectView(frame:CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenHeight))
        pag.isHidden = true
        pag.isUserInteractionEnabled = false
        pag.setScaleMode(PAGScaleModeZoom)
        pag.setRepeatCount(1)
        pag.add(self)
        getWindow()?.addSubview(pag)
        self.effectView = pag
    }

    deinit {
        effectView?.removeFromSuperview()
    }
}

extension String {
    static var documentsPath: String {
        return NSHomeDirectory() + "/Documents/"
    }
    
}


extension AUIRoomGiftBinder: AUIGiftsManagerRespDelegate,PAGViewListener,AUIRoomGiftDialogEventsDelegate {
    public func sendGiftAction(gift: AUIGiftEntity) {
        if !gift.giftEffect.isEmpty {
            AUICommonDialog.hidden()
            AUIToast.hidden()
        }
        self.sendGift(gift: gift) { error in
            AUIToast.show(text: error == nil ? "Sent successful!":"Sent failed!")
            if error == nil {
                self.receiveGift(gift: gift)
            }
        }
    }
    
    
    public func receiveGift(gift: AUIGiftEntity) {
        self.receive?.receiveGift(gift: gift)
        if !gift.effectMD5.isEmpty {
            AUICommonDialog.hidden()
            AUIToast.hidden()
            self.effectAnimation(gift: gift)
//            self.notifyHorizontalTextCarousel(gift: gift)
        } else {
            self.effectView?.isHidden = true
        }
        
    }
    
    public func onAnimationEnd(_ pagView: PAGView!) {
        self.animationPaths.removeFirst()
        if self.animationPaths.count <= 0 {
            self.effectView?.isHidden = true
        } else {
            self.playDelayAnimation()
        }
    }
    
    public func onAnimationCancel(_ pagView: PAGView!) {
        self.effectView?.isHidden = true
    }
    
    public func onAnimationRepeat(_ pagView: PAGView!) {
        self.effectView?.isHidden = true
    }
    
}

extension AUIRoomGiftBinder {
    func effectAnimation(gift: AUIGiftEntity) {
        let effectName = gift.effectMD5
        let path = String.documentsPath
        let documentPath = path + "AUIKitGiftEffect/\(effectName)"
        if effectName.isEmpty || !FileManager.default.fileExists(atPath: documentPath) {
            aui_info("effectMD5 is empty!")
            return
        }
        self.effectView?.isHidden = false
        if !self.animationPaths.contains(documentPath) {
            self.animationPaths.append(documentPath)
        }
        if self.effectView?.isPlaying() == false {
            self.playAnimation(path: documentPath)
        }
    }
    
    private func playAnimation(path: String) {
        aui_info("play effect animation")
        let file = PAGFile.load(path)
        self.effectView?.setComposition(file)
        self.effectView?.play()
    }
    
    private func playDelayAnimation() {
        if let animationPath = self.animationPaths.first {
            aui_info("playDelayAnimation animation")
            let file = PAGFile.load(animationPath)
            self.effectView?.setComposition(file)
            self.effectView?.play()
        }
    }
    
    func refreshGifts(tabs: [AUIGiftTabEntity]) {
        self.send?.fillTabs(tabs: tabs)
    }
    
    func sendGift(gift: AUIGiftEntity, completion: @escaping (NSError?) -> Void) {
        self.giftDelegate?.sendGift(gift: gift, completion: completion)
    }
    
    func downloadEffectResource(tabs: [AUIGiftTabEntity]) {
        for tab in tabs {
            if let gifts = tab.gifts {
                for gift in gifts {
                    if !gift.giftEffect.isEmpty,!gift.effectMD5.isEmpty {
                        let filePath = self.getFilePath(gift: gift)
                        if !FileManager.default.fileExists(atPath: filePath) {
                            AF.download(URL(string: gift.giftEffect)!).responseData { response in
                                if let data = response.value {
                                    do {
                                        try data.write(to: URL(fileURLWithPath: filePath))
                                    } catch {
                                        assert(false,"write file error:\(error.localizedDescription)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
    }
    
    func getFilePath(gift: AUIGiftEntity) -> String {
        let path = String.documentsPath
        let documentPath = path + "AUIKitGiftEffect"
        if !FileManager.default.fileExists(atPath: documentPath) {
            do {
                try FileManager.default.createDirectory(atPath: documentPath, withIntermediateDirectories: true)
            } catch {
                assert(false,"createDirectory error:\(error.localizedDescription)")
            }
        }
        let effectName = gift.effectMD5
        let filePath = documentPath + "/" + "\(effectName)"
        return filePath
    }
    
}


final class AnimationQueue {
    var animations: [() -> Void] = []
    private var isAnimating: Bool = false
    
    func addAnimation(animation: @escaping () -> Void, delay: TimeInterval = 3) {
        let delayedAnimation = {
            DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                animation()
                self.startNextAnimation()
            }
        }
        
        self.animations.append(delayedAnimation)
        if !self.isAnimating {
            self.startNextAnimation()
        }
    }
    
    private func startNextAnimation() {
        guard !self.isAnimating else {
            return
        }
        
        if let animation = self.animations.first {
            self.isAnimating = true
            animation()
            self.animations.removeFirst()
        } else {
            self.isAnimating = false
        }
    }
}
