//
//  AUIRoomListCell.swift
//  AUICell
//
//  Created by zhaoyongqiang on 2023/4/11.
//

import UIKit
import AScenesKit
import AUIKit
import Kingfisher

class AUIRoomListCell: UICollectionViewCell {
    var roomInfo: AUIRoomInfo? {
        didSet {
            titleLabel.text = roomInfo?.roomName
            statusLabel.text = "\(roomInfo?.memberCount ?? 0)人正在嗨歌"
            avatarImageView.kf.setImage(with: URL(string:roomInfo?.owner?.userAvatar ?? ""), placeholder: UIImage(systemName: "person.circle"))
            ownerLabel.text = "\(roomInfo?.owner?.userName ?? "")的房间"
        }
    }
    
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 6
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        stackView.translatesAutoresizingMaskIntoConstraints = false
        return stackView
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "icon_musical_note"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var statusLabel: UILabel = {
        let label = UILabel()
//        label.text = "2人正在嗨哥"
        label.textColor = UIColor(red: 195/255.0,
                                  green: 197/255.0,
                                  blue: 254/255.0,
                                  alpha: 1.0)
        label.font = .systemFont(ofSize: 12)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.cornerRadius = 24
        imageView.layer.masksToBounds = true
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = .systemFont(ofSize: 17)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var ownerLabel: UILabel = {
        let label = UILabel()
//        label.text = "user的房间"
        label.textColor = UIColor(white: 1.0, alpha: 0.5)
        label.font = .systemFont(ofSize: 12)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        contentView.theme_backgroundColor = "CommonColor.navy35"
        contentView.layer.cornerRadius = 24
        
        contentView.addSubview(stackView)
        stackView.addArrangedSubview(iconImageView)
        stackView.addArrangedSubview(statusLabel)
        contentView.addSubview(avatarImageView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(ownerLabel)
        
        stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 14).isActive = true
        stackView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 12).isActive = true
        
        iconImageView.widthAnchor.constraint(equalToConstant: 16).isActive = true
        iconImageView.heightAnchor.constraint(equalToConstant: 16).isActive = true
        
        avatarImageView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        avatarImageView.topAnchor.constraint(equalTo: stackView.bottomAnchor, constant: 27).isActive = true
        avatarImageView.widthAnchor.constraint(equalToConstant: 48).isActive = true
        avatarImageView.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        titleLabel.topAnchor.constraint(equalTo: avatarImageView.bottomAnchor, constant: 4).isActive = true
        
        ownerLabel.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        ownerLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 2).isActive = true
    }
}
