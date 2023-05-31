//
//  AUIRoomMemberListView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import AUIKit

private let kMemberListCellID = "kMemberListCellID"

public func auikaraoke_localized(_ string: String) -> String {
    return aui_localized(string, bundleName: "auiVoiceChatLocalizable")
}


public protocol AUIUserCellUserDataProtocol: NSObjectProtocol {
    var userAvatar: String {get}
    var userId: String {get}
    var userName: String {get}
}


/// 用户列表cell
public class AUIRoomMemberUserCell: UITableViewCell {
    
    public lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_width = "MemberUserCell.avatarWidth"
        imageView.theme_height = "MemberUserCell.avatarHeight"
        imageView.layer.theme_cornerRadius = "MemberUserCell.avatarCornerRadius"
        imageView.clipsToBounds = true
        return imageView
    }()
    
    public lazy var userNameLabel: UILabel = {
       let label = UILabel()
        label.theme_textColor = "MemberUserCell.titleColor"
        label.theme_font = "MemberUserCell.bigTitleFont"
        return label
    }()
    
    public lazy var seatNoLabel: UILabel = {
       let label = UILabel()
        label.theme_textColor = "MemberUserCell.subTitleColor"
        label.theme_font = "MemberUserCell.smallTitleFont"
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        backgroundColor = .clear
        contentView.backgroundColor = .clear
        
        contentView.addSubview(avatarImageView)
        contentView.addSubview(userNameLabel)
        contentView.addSubview(seatNoLabel)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarImageView.aui_centerY = bounds.height * 0.5
        avatarImageView.aui_left = 15
        avatarImageView.aui_size = CGSize(width: 56, height: 56)
        
        userNameLabel.aui_left = avatarImageView.aui_right + 12
        userNameLabel.aui_top = 16
        
        seatNoLabel.aui_left = userNameLabel.aui_left
        seatNoLabel.aui_bottom = bounds.height - 18
    }
    
    public func setUserInfo(withAvatar avatar: String?, title: String?, subTitle: String) {
        avatarImageView.kf.setImage(with: URL(string: avatar ?? ""), placeholder: UIImage.aui_Image(named: "aui_micseat_dialog_avatar_idle"))
        userNameLabel.text = title
        seatNoLabel.text = subTitle
        userNameLabel.sizeToFit()
        seatNoLabel.sizeToFit()
    }
    
    
}


/// 用户列表
public class AUIRoomMemberListView: UIView {
    
    public var memberList: [AUIUserCellUserDataProtocol] = [] {
        didSet {
            tableView.reloadData()
        }
    }
    
    public var seatMap: [String: Int] = [:] {
        didSet {
            tableView.reloadData()
        }
    }
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.theme_textColor = "MemberUserCell.titleColor"
        label.theme_font = "CommonFont.big"
        label.text = auikaraoke_localized("memberlistTitle")
        return label
    }()
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.theme_backgroundColor = "MemberList.backgroundColor"
        tableView.theme_separatorColor = "MemberList.separatorColor"
        tableView.register(AUIRoomMemberUserCell.self, forCellReuseIdentifier: kMemberListCellID)
        tableView.allowsSelection = false
        return tableView
    }()

    deinit {
        aui_info("deinit AUIRoomMemberListView", tag:"AUIRoomMemberListView")
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        aui_info("init AUIRoomMemberListView", tag:"AUIRoomMemberListView")
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    
    private func _loadSubViews() {
        theme_backgroundColor = "MemberList.backgroundColor"
        addSubview(titleLabel)
        addSubview(tableView)
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        tableView.frame = CGRect(x: 0, y: 60, width: bounds.width, height: bounds.height)
        titleLabel.aui_left = 16
        titleLabel.aui_top = 18
        titleLabel.sizeToFit()
    }
}

extension AUIRoomMemberListView: UITableViewDelegate, UITableViewDataSource {

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 80
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return memberList.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell: AUIRoomMemberUserCell = tableView.dequeueReusableCell(withIdentifier: kMemberListCellID, for: indexPath) as! AUIRoomMemberUserCell
        let user = memberList[indexPath.row]
        let seatIdx = seatMap[user.userId] ?? -1
        let subTitle = seatIdx >= 0 ? String(format: auikaraoke_localized("micSeatDesc1Format"), seatIdx + 1) : ""
        cell.setUserInfo(withAvatar: user.userAvatar, title: user.userName, subTitle: subTitle)
        return cell
    }
}
