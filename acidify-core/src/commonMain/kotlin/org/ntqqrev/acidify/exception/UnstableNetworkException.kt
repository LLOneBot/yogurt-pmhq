package org.ntqqrev.acidify.exception

import kotlin.js.JsExport

/**
 * 网络不稳定异常，表示用户在进行 WtLogin 过程中被系统判定为网络不稳定 (code=237)
 * @property manualVerifyUrl 手动验证 URL，用户可以通过访问该 URL 来进行手动验证，以恢复正常登录流程
 */
@JsExport
class UnstableNetworkException(
    tag: String,
    msg: String,
    val manualVerifyUrl: String,
) : WtLoginException(code = 237, tag, msg)