package moe.kooritea.oos2cosm.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.core.annotation.LegacyResourcesHook
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Modifier


@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            isEnable = false
        }
        isDebug = false
    }

    @OptIn(LegacyResourcesHook::class)
    override fun onHook() = encase {
        loadApp(name = "com.oplus.notificationmanager"){
            "com.oplus.notificationmanager.NotificationBackend".toClass().apply {
                method {
                    name = "registerUpdateBroadcastReceiver"
                    paramCount = 1
                }.hook {
                    before {
                        field {
                            name = "mAllowNotificationSwitchDisabled"
                        }.get(instance).set(ArrayList<String?>())
                    }
                }
            }
            "com.oplus.comm.OplusNotificationManagerHelper".toClass().apply {
                method {
                    name = "canModifyNotificationPermissionForPackage"
                    paramCount = 2
                }.hook {
                    replaceToTrue()
                }
            }
            "com.oplus.notificationmanager.property.uicontroller.PropertyUIController".toClass().apply {
                method {
                    name = "channelEnabled"
                    paramCount = 2
                }.hook {
                    replaceToTrue()
                }
            }
        }
        loadApp(name = "com.oplus.battery"){
            System.loadLibrary("dexkit")
            DexKitBridge.create(appInfo.sourceDir).use { bridge ->
                bridge.findClass {
                    matcher {
                        source("StartupManager.java", StringMatchType.Equals, false)
                    }
                }.findMethod {
                    matcher {
                        returnType = "int"
                        modifiers = Modifier.PUBLIC
                        params{
                            count = 0
                        }
                        usingNumbers(5,20)
                    }
                }.single().let {
                    val methodName = it.name;
                    it.className.toClass().apply {
                        method{
                            name = methodName
                            emptyParam()
                        }.hook {
                            replaceTo(any = 99)
                        }
                    }
                }
            }
        }
    }
}