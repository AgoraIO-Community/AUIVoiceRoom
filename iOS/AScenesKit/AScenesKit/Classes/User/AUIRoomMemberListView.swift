//
//  AUIRoomMemberListView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import AUIKitCore
import SDWebImage

private let kMemberListCellID = "kMemberListCellID"

public func auikaraoke_localized(_ string: String) -> String {
    return aui_localized(string, bundleName: "auiVoiceChatLocalizable")
}





/// 用户列表cell
public class AUIRoomMemberUserCell: UITableViewCell {
    
    public var actionClosure: ((AUIUserCellUserDataProtocol?) -> ())?
    
    private var user: AUIUserCellUserDataProtocol?
    
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
    
    lazy var action: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.contentView.frame.width - 80, y: (self.contentView.frame.height-28)/2.0, width: 80, height: 28)).setGradient([UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)], [ CGPoint(x: 0, y: 0.25),  CGPoint(x: 1, y: 0.75)]).title(aui_localized("kick", bundleName: "auiVoiceChatLocalizable"), .normal).textColor(.white, .normal).font(.systemFont(ofSize: 14, weight: .medium)).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside).cornerRadius(14)
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
        contentView.addSubview(action)
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
        self.action.frame = CGRect(x: self.contentView.frame.width - 90, y: (self.contentView.frame.height-28)/2.0, width: 80, height: 28)
        self.action.aui_centerY = self.contentView.aui_centerY
    }
    
    public func setUserInfo(user: AUIUserCellUserDataProtocol?, ownerPreview: Bool) {
        self.user = user
        avatarImageView.sd_setImage(with: URL(string: user?.userAvatar ?? ""), placeholderImage: UIImage.aui_Image(named: "aui_micseat_dialog_avatar_idle"))
        userNameLabel.text = user?.userName
        if let index = user?.seatIndex ,index >= 0 {
            seatNoLabel.text = "\(index + 1)号麦"
        } else {
            seatNoLabel.text = ""
        }
        
        if let isOwner = user?.isOwner {
            if isOwner {
                self.action.isHidden = true
            } else {
                self.action.isHidden = !ownerPreview
            }
        }
        
        userNameLabel.sizeToFit()
        seatNoLabel.sizeToFit()
    }
    
    @objc private func sendAction() {
        self.actionClosure?(self.user)
    }
}

@objc public protocol AUIRoomMemberListViewEventsDelegate:NSObjectProtocol {
    func kickUser(user: AUIUserCellUserDataProtocol)
}

/// 用户列表
public class AUIRoomMemberListView: UIView {
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public var ownerPreview = false
    
    public func addActionHandler(actionHandler: AUIRoomMemberListViewEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIRoomMemberListViewEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
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
    
    public func refreshView() {
        self.tableView.reloadData()
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
        let user = memberList[safe: indexPath.row]
        let seatIdx = seatMap[user?.userId ?? ""] ?? -1
        _ = seatIdx >= 0 ? String(format: auikaraoke_localized("micSeatDesc1Format"), seatIdx + 1) : ""
        cell.actionClosure = { [weak self] user in
            guard let `self` = self,let kickUser = user else { return }
            self.eventHandlers.allObjects.forEach({
                $0.kickUser(user: kickUser)
            })
        }
        cell.setUserInfo(user: user,ownerPreview: self.ownerPreview)
        
        return cell
    }
}
