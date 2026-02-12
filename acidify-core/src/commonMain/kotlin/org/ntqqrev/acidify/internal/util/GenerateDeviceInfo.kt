package org.ntqqrev.acidify.internal.util

import org.ntqqrev.acidify.internal.KuromeClient
import org.ntqqrev.acidify.internal.LagrangeClient
import org.ntqqrev.acidify.internal.proto.system.DeviceInfo

internal fun LagrangeClient.generateDeviceInfo() = DeviceInfo(
    devName = sessionStore.deviceName,
    devType = appInfo.kernel,
    osVer = "Windows 10.0.19042",
    vendorOsName = appInfo.vendorOs,
)

internal fun KuromeClient.generateDeviceInfo() = DeviceInfo(
    devName = sessionStore.deviceName,
    devType = appInfo.kernel,
    osVer = "Android 12.0",
    vendorOsName = appInfo.vendorOs,
)