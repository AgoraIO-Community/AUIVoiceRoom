//
//  CreateRoomSettingController.swift
//  AUIVoiceRoom
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKitCore
#if DEBUG
    import DoraemonKit
#endif
 
final class AUICreateRoomSettingController: UIViewController,UITextFieldDelegate {
    
    private lazy var background: UIImageView = {
        UIImageView(frame: self.view.frame).isUserInteractionEnabled(true)
    }()
    
    private lazy var seats: UILabel = {
        UILabel(frame: CGRect(x: 44, y: AScreenHeight-200, width: 50, height: 20)).font(.systemFont(ofSize: 17, weight: .medium)).textColor(.white).text("Seats")
    }()
    
    private lazy var seatTypeSegment: UISegmentedControl = {
        let segment = UISegmentedControl(items: ["1","6","8","9"])
        segment.frame = CGRect(x: AScreenWidth-246, y: AScreenHeight-210, width: 190, height: 46)
        segment.setImage(UIImage(named: "dot_1"), forSegmentAt: 0)
        segment.setImage(UIImage(named: "dot_6"), forSegmentAt: 1)
        segment.setImage(UIImage(named: "dot_8"), forSegmentAt: 2)
        segment.setImage(UIImage(named: "dot_9"), forSegmentAt: 3)
        segment.tintColor = UIColor(0x009EFF)
        segment.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
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
        segment.frame = CGRect(x: AScreenWidth-152, y: self.seatTypeSegment.frame.maxY+26, width: 96, height: 46)
        segment.setImage(UIImage(named: "sun"), forSegmentAt: 0)
        segment.setImage(UIImage(named: "moon"), forSegmentAt: 1)
        segment.tintColor = UIColor(0x009EFF)
        segment.tag = 12
        segment.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)
        if let themeName = AUIRoomContext.shared.currentThemeName {
            if themeName == "Light" {
                segment.selectedSegmentIndex = 0
            }
            if themeName == "Dark" {
                segment.selectedSegmentIndex = 1
            }
        }
        
        segment.selectedSegmentTintColor = UIColor(0x009EFF)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 18, weight: .medium)], for: .selected)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 16, weight: .regular)], for: .normal)
        segment.addTarget(self, action: #selector(onChanged(sender:)), for: .valueChanged)
        return segment
    }()
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        UINavigationBar.appearance().tintColor = .white
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.background.image(UIImage(named: "bg_img_of_\(AUIRoomContext.shared.themeIdx == 0 ? "light":"dark")_mode"))
        let tap = UITapGestureRecognizer(target: self, action: #selector(showDebug))
        tap.numberOfTapsRequired = 3
        tap.numberOfTouchesRequired = 1
        self.background.addGestureRecognizer(tap)
    }
    
    @objc private func showDebug() {
#if DEBUG
    DoraemonManager.shareInstance().showDoraemon()
#endif
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: true)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.view.addSubViews([self.background,self.seats,self.seatTypeSegment,self.themes,self.modeSegment])
    #if DEBUG
        DoraemonManager.shareInstance().install()
    #endif
        // Do any additional setup after loading the view.
    }

    @objc private func onChanged(sender: UISegmentedControl) {
        if sender.tag == 11 {
            AUIRoomContext.shared.seatType = AUIMicSeatViewLayoutType(rawValue: UInt(sender.selectedSegmentIndex+1)) ?? .eight
            
        } else {
            var themeName = ""
            if sender.selectedSegmentIndex == 0 {
                self.background.image = UIImage(named: "bg_img_of_light_mode")
                themeName = "Light"
            } else {
                self.background.image = UIImage(named: "bg_img_of_dark_mode")
                themeName = "Dark"
            }
            AUIThemeManager.shared.switchTheme(themeName: themeName)
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
