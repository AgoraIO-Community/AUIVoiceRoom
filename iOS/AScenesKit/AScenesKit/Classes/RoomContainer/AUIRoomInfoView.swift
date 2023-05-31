//
//  AUIRoomInfoView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit
import Kingfisher
import SwiftTheme

private let headImageWidth: CGFloat = 40

/// 房间信息展示
public class AUIRoomInfoView: UIView {
    
    private var headImageView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = headImageWidth * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        return imgview
    }()
    
    private var roomNameLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.theme_textColor = AUIColor("Room.roomInfoTitleColor")
        label.theme_font = "CommonFont.middle"
        label.text =  auikaraoke_localized("roomInfoRoomName")
        return label
    }()
    
    private var roomIdLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.theme_textColor = AUIColor("Room.roomInfoSubTitleColor")
        label.theme_font = "CommonFont.small"
        label.text = auikaraoke_localized("roomInfoRoomID")
        return label
    }()
    
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _createSubviews(){
        self.theme_backgroundColor = "Room.roomInfoBackgrounfColor"
        layer.masksToBounds = true
        
        addSubview(headImageView)
        addSubview(roomNameLabel)
        addSubview(roomIdLabel)
        
        headImageView.translatesAutoresizingMaskIntoConstraints = false
        roomNameLabel.translatesAutoresizingMaskIntoConstraints = false
        roomIdLabel.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            headImageView.centerYAnchor.constraint(equalTo: self.centerYAnchor),
            headImageView.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 8),
            headImageView.widthAnchor.constraint(equalToConstant: headImageWidth),
            headImageView.heightAnchor.constraint(equalToConstant: headImageWidth)
        ])
        
        NSLayoutConstraint.activate([
            roomNameLabel.topAnchor.constraint(equalTo: headImageView.topAnchor),
            roomNameLabel.leftAnchor.constraint(equalTo: headImageView.rightAnchor, constant: 8),
            roomNameLabel.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -8)
        ])
        
        NSLayoutConstraint.activate([
            roomIdLabel.bottomAnchor.constraint(equalTo: headImageView.bottomAnchor),
            roomIdLabel.leftAnchor.constraint(equalTo: roomNameLabel.leftAnchor),
            roomIdLabel.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -8)
        ])
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = bounds.height * 0.5
    }
}


extension AUIRoomInfoView {
    
    public func updateRoomInfo(withRoomId roomId:String, roomName: String?, ownerHeadImg:String?){
        roomNameLabel.text = (roomName ?? "") + auikaraoke_localized("roomInfoRoomName")
        roomIdLabel.text = auikaraoke_localized("roomInfoRoomID") + roomId
        headImageView.kf.setImage(with: URL(string: ownerHeadImg ?? ""))
    }
}

