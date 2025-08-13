## Keep Urovo RFID SDK classes so that code shrinking doesn't remove them.
-keep class com.ubx.usdk.** { *; }
-keep class com.ubx.usdk.rfid.** { *; }