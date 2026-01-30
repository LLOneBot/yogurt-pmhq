package org.ntqqrev.yogurt.api.handler

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.*
import org.ntqqrev.milky.*
import org.ntqqrev.yogurt.api.MilkyApiException
import org.ntqqrev.yogurt.api.define
import org.ntqqrev.yogurt.transform.toMilkyEntity
import org.ntqqrev.yogurt.util.resolveUri

val UploadPrivateFile = ApiEndpoint.UploadPrivateFile.define {
    val bot = application.dependencies.resolve<Bot>()
    val fileData = resolveUri(it.fileUri)
    val fileId = bot.uploadPrivateFile(
        friendUin = it.userId,
        fileName = it.fileName,
        fileData = fileData
    )
    UploadPrivateFileOutput(fileId)
}

val UploadGroupFile = ApiEndpoint.UploadGroupFile.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getGroup(it.groupId) ?: throw MilkyApiException(-404, "Group not found")
    val fileData = resolveUri(it.fileUri)
    val fileId = bot.uploadGroupFile(
        groupUin = it.groupId,
        fileName = it.fileName,
        fileData = fileData,
        parentFolderId = it.parentFolderId
    )
    UploadGroupFileOutput(fileId)
}

val GetPrivateFileDownloadUrl = ApiEndpoint.GetPrivateFileDownloadUrl.define {
    val bot = application.dependencies.resolve<Bot>()
    val url = bot.getPrivateFileDownloadUrl(it.userId, it.fileId, it.fileHash)
    GetPrivateFileDownloadUrlOutput(url)
}

val GetGroupFileDownloadUrl = ApiEndpoint.GetGroupFileDownloadUrl.define {
    val bot = application.dependencies.resolve<Bot>()
    val url = bot.getGroupFileDownloadUrl(it.groupId, it.fileId)
    GetGroupFileDownloadUrlOutput(url)
}

val GetGroupFiles = ApiEndpoint.GetGroupFiles.define {
    val bot = application.dependencies.resolve<Bot>()
    val result = bot.getGroupFileList(it.groupId, it.parentFolderId, 0)
    GetGroupFilesOutput(
        files = result.files.map { file -> file.toMilkyEntity(it.groupId) },
        folders = result.folders.map { folder -> folder.toMilkyEntity(it.groupId) }
    )
}

val MoveGroupFile = ApiEndpoint.MoveGroupFile.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.moveGroupFile(it.groupId, it.fileId, it.parentFolderId, it.targetFolderId)
    MoveGroupFileOutput()
}

val RenameGroupFile = ApiEndpoint.RenameGroupFile.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.renameGroupFile(it.groupId, it.fileId, it.parentFolderId, it.newFileName)
    RenameGroupFileOutput()
}

val DeleteGroupFile = ApiEndpoint.DeleteGroupFile.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.deleteGroupFile(it.groupId, it.fileId)
    DeleteGroupFileOutput()
}

val CreateGroupFolder = ApiEndpoint.CreateGroupFolder.define {
    val bot = application.dependencies.resolve<Bot>()
    val folderId = bot.createGroupFolder(it.groupId, it.folderName)
    CreateGroupFolderOutput(folderId)
}

val RenameGroupFolder = ApiEndpoint.RenameGroupFolder.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.renameGroupFolder(it.groupId, it.folderId, it.newFolderName)
    RenameGroupFolderOutput()
}

val DeleteGroupFolder = ApiEndpoint.DeleteGroupFolder.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.deleteGroupFolder(it.groupId, it.folderId)
    DeleteGroupFolderOutput()
}
