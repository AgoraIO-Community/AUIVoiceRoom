//
//  CreateRoomSettingController.swift
//  AUIKitVoiceRoom
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKit
 
final class AUICreateRoomSettingController: UIViewController,UITextFieldDelegate {
    
    private lazy var background: UIImageView = {
        UIImageView(frame: self.view.frame).image(UIImage(named: "bg_img_of_dark_mode"))
    }()
    
    private lazy var seats: UILabel = {
        UILabel(frame: CGRect(x: 44, y: AScreenHeight-200, width: 50, height: 20)).font(.systemFont(ofSize: 17, weight: .medium)).textColor(.white).text("Seats")
    }()
    
    private lazy var seatTypeSegment: UISegmentedControl = {
        let segment = UISegmentedControl(items: ["1","6","8","9"])
        segment.frame = CGRect(x: AScreenWidth-234, y: AScreenHeight-210, width: 190, height: 46)
        segment.setImage(UIImage(named: "dot_1"), forSegmentAt: 0)
        segment.setImage(UIImage(named: "dot_6"), forSegmentAt: 1)
        segment.setImage(UIImage(named: "dot_8"), forSegmentAt: 2)
        segment.setImage(UIImage(named: "dot_9"), forSegmentAt: 3)
        segment.tintColor = UIColor(0x009EFF)
        segment.backgroundColor = .clear
        segment.tag = 11
        segment.selectedSegmentIndex = Int(AUIRoomContext.shared.seatType.rawValue - 1)
        segment.selectedSegmentTintColor = UIColor(0x009EFF)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 18, weight: .medium)], for: .selected)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 16, weight: .regular)], for: .normal)
        segment.addTarget(self, action: #selector(onChanged(sender:)), for: .valueChanged)
        return segment
    }()
    
    lazy var themes: UILabel = {
        UILabel(frame: CGRect(x: 44, y: self.seats.frame.maxY+52, width: 70, height: 20)).font(.systemFont(ofSize: 17, weight: .medium)).textColor(.white).text("Themes")
    }()
    
    private lazy var modeSegment: UISegmentedControl = {
        let segment = UISegmentedControl(items: ["Light","Dark"])
        segment.frame = CGRect(x: AScreenWidth-130, y: self.seatTypeSegment.frame.maxY+26, width: 96, height: 46)
        segment.setImage(UIImage(named: "sun"), forSegmentAt: 0)
        segment.setImage(UIImage(named: "moon"), forSegmentAt: 1)
        segment.tintColor = UIColor(0x009EFF)
        segment.tag = 12
        segment.backgroundColor = .clear
        segment.selectedSegmentIndex = AUIRoomContext.shared.themeIdx
        segment.selectedSegmentTintColor = UIColor(0x009EFF)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 18, weight: .medium)], for: .selected)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 16, weight: .regular)], for: .normal)
        segment.addTarget(self, action: #selector(onChanged(sender:)), for: .valueChanged)
        return segment
    }()
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: true)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.title = "Mic Seat Setting"
        self.view.addSubViews([self.background,self.seats,self.seatTypeSegment,self.themes,self.modeSegment])
        
        // Do any additional setup after loading the view.
    }

    @objc private func onChanged(sender: UISegmentedControl) {
        if sender.tag == 11 {
            AUIRoomContext.shared.seatType = AUIMicSeatViewLayoutType(rawValue: UInt(sender.selectedSegmentIndex+1)) ?? .eight
            
        } else {
            AUIRoomContext.shared.switchTheme(themeName: sender.selectedSegmentIndex == 0 ? "Light":"Dark")
        }
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let text = textField.text,let count = UInt(text),count > 0 {
            AUIRoomContext.shared.seatCount = count
        }
        return true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if let text = textField.text,let count = UInt(text),count > 0 {
            AUIRoomContext.shared.seatCount = count
        }
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        if let text = textField.text,let count = UInt(text),count > 0 {
            AUIRoomContext.shared.seatCount = count
        }
    }
}
