#!/bin/bash
if [ ! -d "AUIKit" ]; then
  echo "AUIKit not exist!! download it..."
  curl -L -o AUIKit-dev-rename_voicechat_ios.zip https://codeload.github.com/AgoraIO-Community/AUIKit/zip/dev/rename_voicechat_ios
  unzip AUIKit-dev-rename_voicechat_ios.zip
  mv AUIKit-dev-rename_voicechat_ios AUIKit
  rm AUIKit-dev-rename_voicechat_ios.zip
fi
