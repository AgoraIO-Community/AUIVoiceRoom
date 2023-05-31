//
//  AUIRoomGiftBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit
import SVGAPlayer

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
            }
        })
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
        let player = SVGAPlayer(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenHeight))
        player.loops = 1
        player.clearsAfterStop = true
        player.contentMode = .scaleAspectFill
        player.delegate = self
        player.tag(199)
        getWindow()?.addSubview(player)
        let parser = SVGAParser()
//        let effectPath = gift.giftEffect
        guard let folderPath = Bundle.main.path(forResource: "auiVoiceChatTheme", ofType: "bundle") else { return }
        guard let path = Bundle(path: folderPath)?.path(forResource: "AUIKitGift11", ofType: "svga",inDirectory: "UIKit/resource") else { return }
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
    
}
