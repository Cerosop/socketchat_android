## socketchat_android
use socket to implement a chatroom by Android Studio

### 使用語言及技術
Android Studio、Java、socket

### 執行方法 
去SDK路徑底下的 platform-tools資料夾開啟cmd，輸入adb -s emulator-5554 forward tcp:6100 tcp:7100，其中5554是server的模擬器
client端ip輸入10.0.2.2，port輸入6100

### 執行結果
#### 伺服器連接<br>
<img src="https://github.com/user-attachments/assets/c0a98a57-c7df-4c1f-87c3-94da2e0e0efd" width="800" /><br>
#### 多人聊天室<br>
<img src="https://github.com/user-attachments/assets/5bf9a392-add1-4c8e-a544-180c2d75d58d" width="800" /><br>
#### client離開<br>
<img src="https://github.com/user-attachments/assets/a1181ae3-3d0d-4333-96c6-ac9c69a8c3bb" width="800" /><br>
#### server離開<br>
<img src="https://github.com/user-attachments/assets/75e5fe7d-0de1-4368-94f5-00a0a5d39b03" width="800" /><br>

