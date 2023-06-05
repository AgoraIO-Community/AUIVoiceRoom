//
//  CreateRoomSettingController.swift
//  AUIKitVoiceRoom
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKit
 
final class AUICreateRoomSettingController: UIViewController,UITextFieldDelegate {
    
    @AUserDefault("MicSeatType",defaultValue: 2) var seatType
    
    @AUserDefault("MicSeatCount",defaultValue: 8) var seatCount
    
    private lazy var seatTypeSegment: UISegmentedControl = {
        let segment = UISegmentedControl(items: ["One","Six","Eight","Nine"])
        segment.frame = CGRect(x: 50, y: ANavigationHeight+100, width: AScreenWidth - 100, height: 30)
        segment.tintColor = UIColor(0x0066ff)
        segment.backgroundColor = .white
        segment.selectedSegmentIndex = self.seatType
        segment.selectedSegmentTintColor = UIColor(0x0066ff)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.white,.font:UIFont.systemFont(ofSize: 18, weight: .medium)], for: .selected)
        segment.setTitleTextAttributes([.foregroundColor : UIColor.lightGray,.font:UIFont.systemFont(ofSize: 16, weight: .regular)], for: .normal)
        segment.addTarget(self, action: #selector(onChanged(sender:)), for: .valueChanged)
        return segment
    }()
    
    private lazy var seatCountField: UITextField = {
        UITextField(frame: CGRect(x: 20, y: self.seatTypeSegment.frame.maxY+30, width: AScreenWidth-40, height: 40)).clearButtonMode(.whileEditing).tag(123).textColor(.black).font(.systemFont(ofSize: 16, weight: .semibold)).placeholder("Input mic seat count,you wish.").textAlignment(.center).delegate(self).layerProperties(UIColor(0x009FFF), 2).cornerRadius(3).text("\(self.seatCount)")
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
        self.view.addSubViews([self.seatTypeSegment,self.seatCountField])
        self.seatCountField.keyboardType = .numberPad
        self.seatCountField.returnKeyType = .done
        self.seatCountField.attributedPlaceholder = NSAttributedString(string: "Input mic seat count,you wish.", attributes: [NSAttributedString.Key.foregroundColor: UIColor.lightText])
        
        // Do any additional setup after loading the view.
    }

    @objc private func onChanged(sender: UISegmentedControl) {
        self.seatType = sender.selectedSegmentIndex
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let text = textField.text,let count = Int(text),count > 0 {
            self.seatCount = count
        }
        return true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if let text = textField.text,let count = Int(text),count > 0 {
            self.seatCount = count
        }
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        if let text = textField.text,let count = Int(text),count > 0 {
            self.seatCount = count
        }
    }
}
