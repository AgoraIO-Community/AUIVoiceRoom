//
//  AUIRoomGiftBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit
import libpag
import Alamofire

public class AUIRoomGiftBinder: NSObject {
    
    private weak var send: IAUIRoomGiftDialog?
    
    private weak var receive: IAUIGiftBarrageView?
    
    private weak var giftDelegate: AUIGiftsManagerServiceDelegate? {
        didSet {
            giftDelegate?.unbindRespDelegate(delegate: self)
            giftDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(send: IAUIRoomGiftDialog, receive: IAUIGiftBarrageView, giftService: AUIGiftsManagerServiceDelegate) {
        self.send = send
        self.receive = receive
        self.giftDelegate = giftService
        self.giftDelegate?.giftsFromService(roomId: giftService.getChannelName(), completion: { [weak self] tabs, error in
            if error == nil {
                self?.refreshGifts(tabs: tabs)
                self?.downloadEffectResource(tabs: tabs)
            }
        })
    }

}

extension String {
    static var documentsPath: String {
        return NSHomeDirectory() + "/Documents/"
    }
    
}


extension AUIRoomGiftBinder: AUIGiftsManagerRespDelegate,PAGViewListener {
    
    public func receiveGift(gift: AUIGiftEntity) {
        self.receive?.receiveGift(gift: gift)
        if !gift.giftEffect.isEmpty {
            self.effectAnimation(gift: gift)
//            self.notifyHorizontalTextCarousel(gift: gift)
        }
        
    }
    
    public func onAnimationEnd(_ pagView: PAGView!) {
        aui_info("gift effect animation end.")
        pagView.removeFromSuperview()
    }
    
}

extension AUIRoomGiftBinder {
    func effectAnimation(gift: AUIGiftEntity) {
        let effectName = gift.effectMD5
        let path = String.documentsPath
        let documentPath = path + "AUIKitGiftEffect/\(effectName)"
        if effectName.isEmpty,!FileManager.default.fileExists(atPath: documentPath) {
            return
        }
        let file = PAGFile.load(documentPath)
        let pagView = PAGView(frame:CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenHeight))
        getWindow()?.addSubview(pagView)
        pagView.setComposition(file)
        pagView.setRepeatCount(1)
        pagView.add(self)
        pagView.play()
        
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


