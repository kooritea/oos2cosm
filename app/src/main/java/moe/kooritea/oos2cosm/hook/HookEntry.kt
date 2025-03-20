package moe.kooritea.oos2cosm.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.core.annotation.LegacyResourcesHook
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Modifier


@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    companion object {
        init {
            System.loadLibrary("dexkit")
        }
    }

    override fun onInit() = configs {
        debugLog {
            isEnable = false
        }
        isDebug = false
    }

    @OptIn(LegacyResourcesHook::class)
    override fun onHook() = encase {
        loadSystem {
            "com.android.server.pm.PackageManagerServiceExtImpl".toClass().apply {
                method {
                    name = "getInstalledPackagesAsUserExt"
                    paramCount = 1
                }.hook {
                    replaceTo(any = null)
                }
            }
            "com.android.server.pm.PackageManagerServiceExtImpl".toClass().apply {
                method {
                    name = "getInstalledApplicationsAsUserExt"
                    paramCount = 1
                }.hook {
                    replaceTo(any = null)
                }
            }
        }
        loadApp(name = "com.oplus.account"){
            "com.platform.account.base.utils.os.DeviceUtil".toClass().apply {
                method {
                    name = "isExp"
                    emptyParam()
                }.hook {
                    replaceTo(any = false)
                }
            }
        }
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