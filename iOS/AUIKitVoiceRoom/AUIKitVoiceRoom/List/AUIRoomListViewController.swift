//
//  AUIRoomListViewController.swift
//  AUICell
//
//  Created by zhaoyongqiang on 2023/4/11.
//

import UIKit
import AScenesKit
import AUIKit
import MJRefresh
import SwiftTheme


private let kButtonWidth: CGFloat = 327
private let kListCountPerPage: Int = 10
final class AUIRoomListViewController: UIViewController {
    private var roomList: [AUIRoomInfo] = []
    private var userInfo: UserInfo = UserInfo()
    
    private lazy var setting: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: AScreenWidth - 60, y: ANavigationHeight - 40, width: 50, height: 50)).tag(20).addTargetFor(self, action: #selector(changeSetting), for: .touchUpInside).image("setting", .normal)
    }()
    
    private lazy var collectionView: UICollectionView = {
       let flowLayout = UICollectionViewFlowLayout()
        flowLayout.scrollDirection = .vertical
        flowLayout.minimumLineSpacing = 8
        flowLayout.minimumInteritemSpacing = 8
        let w = (UIScreen.main.bounds.width - 8 * 3) * 0.5
        flowLayout.itemSize = CGSize(width: w, height: 180)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.dataSource = self
        collectionView.delegate = self
        collectionView.register(AUIRoomListCell.self, forCellWithReuseIdentifier: "cell")
        collectionView.backgroundColor = .clear
        return collectionView
    }()
    
    private lazy var createButton: AUIButton = {
        let button = AUIButton()
        let style = AUIButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWidth = ThemeCGFloatPicker(floats: 327)
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("创建房间", for: .normal)
        button.addTarget(self, action: #selector(self.onCreateAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var themeButton: AUIButton = {
        let button = AUIButton()
        let style = AUIButtonDynamicTheme()
        style.backgroundColor = "CommonColor.primary"
        style.buttonWidth = ThemeCGFloatPicker(floats: 327)
        button.style = style
        button.layoutIfNeeded()
        button.setTitle("换肤", for: .normal)
        button.addTarget(self, action: #selector(self.onThemeChangeAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var gradientLayer: CAGradientLayer = {
        let gradientLayer = CAGradientLayer()
        gradientLayer.locations = [0, 1]
        gradientLayer.startPoint = CGPoint.zero
        gradientLayer.endPoint = CGPoint(x: 0, y: 1)
        return gradientLayer
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        AUIRoomContext.shared.resetTheme()
        UIScrollView.appearance().contentInsetAdjustmentBehavior = .never
        view.layer.addSublayer(gradientLayer)
        gradientLayer.frame = view.bounds
        gradientLayer.theme_colors = AUIGradientColor("CommonColor.normalGradient")

        collectionView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [weak self] in
            self?.onRefreshAction()
        })
        view.addSubview(collectionView)
        view.addSubview(createButton)
        view.addSubview(themeButton)
        view.addSubview(setting)
        collectionView.frame = view.bounds
        _layoutButton()
        initEngine()
        onRefreshAction()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    private func initEngine() {
        //设置基础信息到KaraokeUIKit里
        let commonConfig = AUICommonConfig()
        commonConfig.host = KeyCenter.HostUrl
        commonConfig.userId = userInfo.userId
        commonConfig.userName = userInfo.userName
        commonConfig.userAvatar = userInfo.userAvatar
        VoiceChatUIKit.shared.setup(roomConfig: commonConfig,
                                  rtcEngine: nil,
                                  rtmClient: nil)
    }
    
    private func _layoutButton() {
        if self.roomList.count > 0 {
            createButton.frame = CGRect(origin: CGPoint(x: (view.frame.width - createButton.frame.width) / 2,
                                                        y: view.frame.height - 74 - UIDevice.current.aui_SafeDistanceBottom),
                                        size: createButton.frame.size)
        } else {
            createButton.center = CGPoint(x: view.frame.size.width / 2, y: view.frame.size.height / 2)
        }
        
        themeButton.frame = CGRect(origin: CGPoint(x: createButton.frame.origin.x, y: createButton.frame.origin.y - themeButton.frame.size.height - 5),
                                   size: themeButton.frame.size)
    }
    
    func onRefreshAction() {
        self.roomList = []
        self.collectionView.reloadData()
        self.collectionView.mj_footer = nil
        VoiceChatUIKit.shared.getRoomInfoList(lastCreateTime: nil, pageSize: kListCountPerPage, callback: {[weak self] error, list in
            guard let self = self else {return}
            defer {
                self.collectionView.mj_header?.endRefreshing()
            }
            if let error = error {
                AUIToast.show(text: error.localizedDescription)
                return
            }
            self.roomList = list ?? []
            self.collectionView.reloadData()

            if self.roomList.count == kListCountPerPage {
                self.collectionView.mj_footer = MJRefreshAutoNormalFooter(refreshingBlock: { [weak self] in
                    self?.onLoadMoreAction()
                })
            }
            self._layoutButton()
        })
    }
    
    func onLoadMoreAction() {
        let lastCreateTime = roomList.last?.createTime
        VoiceChatUIKit.shared.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: kListCountPerPage, callback: {[weak self] error, list in
            guard let self = self else {return}
            self.roomList += list ?? []
            self.collectionView.reloadData()
            self.collectionView.mj_footer?.endRefreshing()

            if list?.count ?? 0 < kListCountPerPage {
                self.collectionView.mj_footer?.endRefreshingWithNoMoreData()
            }
            self._layoutButton()
        })
    }
    
    
    @objc func onCreateAction() {
        AUIAlertView()
            .theme_background(color: "CommonColor.black")
            .isShowCloseButton(isShow: true)
            .title(title: "房间主题")
            .titleColor(color: .white)
            .rightButton(title: "一起嗨歌")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                guard let text = text, text.count > 0 else {
                    AUIToast.show(text: "请输入房间名")
                    return
                }
                print("create room with name(\(text))")
                let room = AUICreateRoomInfo()
                room.roomName = text
                room.thumbnail = self.userInfo.userAvatar
                room.seatCount = UInt(AUIRoomContext.shared.seatCount)
                room.seatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
                VoiceChatUIKit.shared.createRoom(roomInfo: room) { roomInfo in
                    let vc = RoomViewController()
                    roomInfo?.seatCount = UInt(AUIRoomContext.shared.seatCount)
                    roomInfo?.seatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
                    vc.roomInfo = roomInfo
                    self.navigationController?.pushViewController(vc, animated: true)
                } failure: { error in
                    AUIToast.show(text: error.localizedDescription)
                }
            })
            .textFieldPlaceholder(placeholder: "房间主题")
            .textFieldPlaceholder(color: UIColor(hex: "#5a5a5a"))
            .textFieldPlaceholder(placeholder: "请输入房间主题")
            .textField(color: .white)
            .textField(text: "room\(arc4random_uniform(99999))")
            .theme_textFieldBackground(color: "CommonColor.navy35")
            .textField(cornerRadius: 25)
            .show()
    }
    
    @objc func onThemeChangeAction() {
        AUIRoomContext.shared.switchThemeToNext()
    }
    
    @objc func changeSetting() {
        let setting = AUICreateRoomSettingController()
        self.navigationController?.pushViewController(setting, animated: true)
    }
}

//extension AUIRoomListViewController: AgoraRtmClientDelegate {
//    func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
//        print("rtm token WillExpire channel: \(channel ?? "")")
//    }
//}


extension AUIRoomListViewController: UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.roomList.count
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUIRoomListCell
        cell.roomInfo = self.roomList[indexPath.row]
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        UIEdgeInsets(top: 84, left: 8, bottom: 80, right: 8)
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let roomInfo = self.roomList[indexPath.row]
        
//        roomManager?.rtmManager.getUserCount(channelName: roomInfo.roomId) { err, count in
//
//        }
//        return
        
        let vc = RoomViewController()
        vc.roomInfo = roomInfo
        self.navigationController?.pushViewController(vc, animated: true)
    }
}
