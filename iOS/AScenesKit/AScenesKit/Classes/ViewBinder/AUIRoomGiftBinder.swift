//
//  AUIRoomGiftBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit
import SVGAPlayer
import Alamofire

public class AUIRoomGiftBinder: NSObject {
    
    private weak var send: AUIRoomGiftDialog?
    
    private weak var receive: AUIReceiveGiftsView?
    
    private weak var giftDelegate: AUIGiftsManagerServiceDelegate? {
        didSet {
            giftDelegate?.unbindRespDelegate(delegate: self)
            giftDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(send: AUIRoomGiftDialog, receive: AUIReceiveGiftsView, giftService: AUIGiftsManagerServiceDelegate) {
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


extension AUIRoomGiftBinder: AUIGiftsManagerRespDelegate,SVGAPlayerDelegate {
    
    public func receiveGift(gift: AUIGiftEntity) {
        self.receive?.gifts.append(gift)
        if gift.giftName == "Rocket" {
            self.effectAnimation(gift: gift)
//            self.notifyHorizontalTextCarousel(gift: gift)
        }
        
    }
    
    public func svgaPlayerDidFinishedAnimation(_ player: SVGAPlayer!) {
        getWindow()?.viewWithTag(199)?.removeFromSuperview()
    }
    
}

extension AUIRoomGiftBinder {
    func effectAnimation(gift: AUIGiftEntity) {
//        let effectName = gift.giftEffect.components(separatedBy: "/").last ?? ""
//        let path = String.documentsPath
//        let documentPath = path + "AUIKitGiftEffect/\(effectName).svga"
//        if effectName.isEmpty,!FileManager.default.fileExists(atPath: documentPath) {
//            return
//        }
        guard let folderPath = Bundle.main.path(forResource: "auiVoiceChatTheme", ofType: "bundle") else { return }
        guard let path = Bundle(path: folderPath)?.path(forResource: "AUIKitGift11", ofType: "svga",inDirectory: "UIKit/resource") else { return }
        let player = SVGAPlayer(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenHeight))
        player.loops = 1
        player.clearsAfterStop = true
        player.contentMode = .scaleAspectFill
        player.delegate = self
        player.tag(199)
        getWindow()?.addSubview(player)
        let parser = SVGAParser()
        parser.parse(with: URL(fileURLWithPath: path)) { entity in
            player.videoItem = entity
            player.startAnimation()
        } failureBlock: { error in
            player.removeFromSuperview()
        }
    }
    
    func refreshGifts(tabs: [AUIGiftTabEntity]) {
        self.send?.tabs = tabs
    }
    
    func sendGift(gift: AUIGiftEntity, completion: @escaping (NSError?) -> Void) {
        self.giftDelegate?.sendGift(gift: gift, completion: completion)
    }
    
    func downloadEffectResource(tabs: [AUIGiftTabEntity]) {
        for tab in tabs {
            if let gifts = tab.gifts {
                for gift in gifts {
                    if !gift.giftEffect.isEmpty{//"https://download.agora.io/null/AUIKitGift11.svga"  测试下载链接需要去除下面fileName判断
                        AF.download(URL(string: gift.giftEffect)!).responseData { response in
                            if let data = response.value {
                                let path = String.documentsPath
                                let documentPath = path + "AUIKitGiftEffect"
                                do {
                                    if !FileManager.default.fileExists(atPath: documentPath) {
                                        try FileManager.default.createDirectory(atPath: documentPath, withIntermediateDirectories: true)
                                    }
                                    let fileName = gift.giftEffect.components(separatedBy: "/").last ?? ""
                                    if fileName.isEmpty {
                                        return
                                    }
                                    let effectName = gift.giftEffect.components(separatedBy: "/").last ?? ""
                                    let filePath = documentPath + "/" + "\(effectName).svga"
//                                    if FileManager.default.fileExists(atPath: filePath) {
//                                        try FileManager.default.removeItem(atPath: filePath)
//                                    }
                                    if !FileManager.default.fileExists(atPath: filePath) {
                                        try data.write(to: URL(fileURLWithPath: filePath))
                                    }
                                } catch {
                                    assert(false,"\(error.localizedDescription)")
                                }
                            }
                        }
                    }
                }
            }
            
        }
    }
    
}

